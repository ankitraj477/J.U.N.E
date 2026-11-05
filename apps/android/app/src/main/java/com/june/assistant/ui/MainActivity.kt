package com.june.assistant.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.june.assistant.data.local.ActionEntity
import com.june.assistant.data.local.JuneDatabase
import com.june.assistant.data.local.MessageEntity
import com.june.assistant.domain.LocalAssistantEngine
import com.june.assistant.domain.model.ActionProposal
import com.june.assistant.domain.model.ActionStatus
import com.june.assistant.domain.model.ContextMode
import com.june.assistant.domain.model.Message
import com.june.assistant.domain.model.RiskLevel
import com.june.assistant.ui.screens.ConversationScreen
import com.june.assistant.ui.theme.JuneTheme
import com.june.assistant.voice.VoiceManager
import com.june.assistant.worker.ReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val engine = LocalAssistantEngine()
    private val messagesState = MutableStateFlow<List<Message>>(emptyList())
    private val contextModeState = MutableStateFlow(ContextMode.HOME)
    private val voiceEnabledState = MutableStateFlow(true)
    private lateinit var db: JuneDatabase
    private lateinit var voiceManager: VoiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = JuneDatabase.getDatabase(this)

        requestRequiredPermissions()

        voiceManager = VoiceManager(this) { spokenText ->
            handleUserMessage(spokenText)
        }

        // Observe local SQLite database messages
        lifecycleScope.launch {
            db.messageDao().getAllMessages().collect { entities ->
                val models = entities.map { entity ->
                    val actionProposal = entity.actionId?.let { actionId ->
                        db.actionDao().getActionById(actionId)?.let { a ->
                            ActionProposal(
                                id = a.id,
                                actionType = a.actionType,
                                target = a.target,
                                riskLevel = RiskLevel.values().find { it.value == a.riskLevel } ?: RiskLevel.LOW,
                                status = ActionStatus.values().find { it.value == a.status } ?: ActionStatus.PROPOSED,
                                reason = a.reason
                            )
                        }
                    }
                    Message(
                        id = entity.id,
                        text = entity.text,
                        isUser = entity.isUser,
                        actionProposal = actionProposal,
                        timestamp = entity.timestamp
                    )
                }
                messagesState.value = models
            }
        }

        setContent {
            JuneTheme {
                val currentMessages by messagesState.collectAsState()
                val currentMode by contextModeState.collectAsState()
                val isVoiceOn by voiceEnabledState.collectAsState()

                ConversationScreen(

                    messages = currentMessages,
                    currentMode = currentMode,
                    isVoiceEnabled = isVoiceOn,
                    onSendMessage = { text -> handleUserMessage(text) },
                    onApproveAction = { action -> handleApproveAction(action) },
                    onCancelAction = { action -> handleCancelAction(action) },
                    onModeSelected = { mode -> contextModeState.value = mode },
                    onToggleVoice = {
                        val newState = !voiceEnabledState.value
                        voiceEnabledState.value = newState
                        voiceManager.isVoiceEnabled = newState
                    },
                    onStartVoiceInput = { voiceManager.startListening() }
                )
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 101)
        }
    }

    private fun handleUserMessage(text: String) {
        lifecycleScope.launch {
            val userMsg = MessageEntity(
                id = java.util.UUID.randomUUID().toString(),
                text = text,
                isUser = true
            )
            db.messageDao().insertMessage(userMsg)

            // Process with local offline engine
            val (reply, proposal) = engine.processInput(text)

            proposal?.let { p ->
                val actionEntity = ActionEntity(
                    id = p.id,
                    actionType = p.actionType,
                    target = p.target,
                    riskLevel = p.riskLevel.value,
                    status = p.status.value,
                    reason = p.reason
                )
                db.actionDao().insertOrUpdateAction(actionEntity)

                // If low-risk reminder, schedule it via WorkManager
                if (p.actionType == "reminder.create" && p.status == ActionStatus.AUTHORIZED) {
                    scheduleReminder(p.target)
                }
            }

            val assistantMsg = MessageEntity(
                id = java.util.UUID.randomUUID().toString(),
                text = reply,
                isUser = false,
                actionId = proposal?.id
            )
            db.messageDao().insertMessage(assistantMsg)

            // Voice response
            voiceManager.speak(reply)
        }
    }

    private fun handleApproveAction(action: ActionProposal) {
        lifecycleScope.launch {
            val updated = ActionEntity(
                id = action.id,
                actionType = action.actionType,
                target = action.target,
                riskLevel = action.riskLevel.value,
                status = ActionStatus.COMPLETED.value,
                reason = "Approved and executed by user"
            )
            db.actionDao().insertOrUpdateAction(updated)

            val confirmMsg = MessageEntity(
                id = java.util.UUID.randomUUID().toString(),
                text = "Action confirmed and executed: ${action.actionType} on '${action.target}'",
                isUser = false
            )
            db.messageDao().insertMessage(confirmMsg)
            voiceManager.speak(confirmMsg.text)
        }
    }

    private fun handleCancelAction(action: ActionProposal) {
        lifecycleScope.launch {
            val updated = ActionEntity(
                id = action.id,
                actionType = action.actionType,
                target = action.target,
                riskLevel = action.riskLevel.value,
                status = ActionStatus.CANCELLED.value,
                reason = "Cancelled by user"
            )
            db.actionDao().insertOrUpdateAction(updated)

            val cancelMsg = MessageEntity(
                id = java.util.UUID.randomUUID().toString(),
                text = "Action cancelled by user.",
                isUser = false
            )
            db.messageDao().insertMessage(cancelMsg)
            voiceManager.speak(cancelMsg.text)
        }
    }

    private fun scheduleReminder(reminderText: String) {
        val workData = Data.Builder()
            .putString(ReminderWorker.KEY_REMINDER_TEXT, reminderText)
            .build()

        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(workData)
            .setInitialDelay(5, TimeUnit.SECONDS) // Schedules notification trigger
            .build()

        WorkManager.getInstance(this).enqueue(reminderRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.release()
    }
}
