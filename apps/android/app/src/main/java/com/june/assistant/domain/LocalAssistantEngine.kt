package com.june.assistant.domain

import com.june.assistant.domain.model.ActionProposal
import com.june.assistant.domain.model.ActionStatus
import com.june.assistant.domain.model.RiskLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LocalAssistantEngine {

    fun processInput(text: String): Pair<String, ActionProposal?> {
        val trimmed = text.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        if (trimmed.isEmpty()) {
            return Pair("I didn't catch that. How can I assist you?", null)
        }

        // 1. Identity, Help & Capabilities
        if (lower in listOf("who are you", "who are u", "what is june", "what are you", "introduce yourself")) {
            return Pair(
                "I am J.U.N.E. (Just Useful, Never Excessive) — your personal, offline-first AI assistant. " +
                "I learn how you work to reduce your cognitive load, but I never act on your behalf without permission.",
                null
            )
        }

        if (lower in listOf("help", "what can you do", "commands", "features", "options")) {
            return Pair(
                "Here is what I can do for you:\n" +
                "• 📝 Notes: Say 'create note <text>' or 'take note <text>'\n" +
                "• ⏰ Reminders: Say 'remind me to <task>'\n" +
                "• 🚀 Launch Apps: Say 'open <app>' or 'launch <app>'\n" +
                "• 🛡️ Safe Deletions: Say 'delete <target>' (requires your explicit approval)\n" +
                "• 🎯 Modes: Tap Focus, Office, Home, or Sleep above\n" +
                "• 🎙️ Voice: Tap the Mic to speak or toggle Voice ON/OFF",
                null
            )
        }

        // 2. Greetings
        if (lower in listOf("hi", "hello", "hey", "hey june", "june", "good morning", "good evening", "good afternoon", "namaste")) {
            return Pair("Hello! J.U.N.E. is online and ready. What would you like to do today?", null)
        }

        // 3. Time and Date
        if (lower in listOf("time", "what time is it", "current time", "what is the time")) {
            val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            return Pair("The current time is $time.", null)
        }
        if (lower in listOf("date", "today", "what is today", "what's today's date", "what is the date")) {
            val date = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
            return Pair("Today is $date.", null)
        }

        // 4. Note Creation
        val noteTriggers = listOf("create note ", "take note ", "add note ", "new note ", "note ")
        for (trigger in noteTriggers) {
            if (lower.startsWith(trigger)) {
                val target = trimmed.substring(trigger.length).trim()
                if (target.isNotEmpty()) {
                    val proposal = ActionProposal(
                        id = UUID.randomUUID().toString(),
                        actionType = "notes.create",
                        target = target,
                        riskLevel = RiskLevel.LOW,
                        status = ActionStatus.AUTHORIZED,
                        reason = "Offline local note creation"
                    )
                    return Pair("Got it. Note saved: '$target'", proposal)
                }
            }
        }

        // 5. Reminders (WorkManager)
        val reminderTriggers = listOf("remind me to ", "remind me ", "set reminder for ", "set reminder to ", "reminder ")
        for (trigger in reminderTriggers) {
            if (lower.startsWith(trigger)) {
                val target = trimmed.substring(trigger.length).trim()
                if (target.isNotEmpty()) {
                    val proposal = ActionProposal(
                        id = UUID.randomUUID().toString(),
                        actionType = "reminder.create",
                        target = target,
                        riskLevel = RiskLevel.LOW,
                        status = ActionStatus.AUTHORIZED,
                        reason = "Offline scheduled reminder"
                    )
                    return Pair("Reminder scheduled: '$target'", proposal)
                }
            }
        }

        // 6. Application Launch
        val launchTriggers = listOf("open ", "launch ", "start ")
        for (trigger in launchTriggers) {
            if (lower.startsWith(trigger)) {
                val target = trimmed.substring(trigger.length).trim()
                if (target.isNotEmpty()) {
                    val proposal = ActionProposal(
                        id = UUID.randomUUID().toString(),
                        actionType = "system.app_open",
                        target = target,
                        riskLevel = RiskLevel.MEDIUM,
                        status = ActionStatus.AUTHORIZED,
                        reason = "Application launch request"
                    )
                    return Pair("Opening $target...", proposal)
                }
            }
        }

        // 7. High-Risk Operations (Protected by Action Approval UI)
        val deleteTriggers = listOf("delete ", "remove ", "erase ", "destroy ")
        for (trigger in deleteTriggers) {
            if (lower.startsWith(trigger)) {
                val target = trimmed.substring(trigger.length).trim()
                if (target.isNotEmpty()) {
                    val proposal = ActionProposal(
                        id = UUID.randomUUID().toString(),
                        actionType = "file.delete",
                        target = target,
                        riskLevel = RiskLevel.HIGH,
                        status = ActionStatus.PROPOSED,
                        reason = "High-risk deletion requires explicit confirmation"
                    )
                    return Pair("High-risk action: Are you sure you want to delete '$target'?", proposal)
                }
            }
        }

        // 8. Polite Conversational Fallback
        return Pair(
            "I heard: \"$trimmed\". I can save this as a note, set a reminder, or open an app. " +
            "Type 'help' to see available commands.",
            null
        )
    }
}
