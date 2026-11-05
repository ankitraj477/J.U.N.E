package com.june.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.june.assistant.domain.model.ActionProposal
import com.june.assistant.domain.model.ActionStatus
import com.june.assistant.domain.model.ContextMode
import com.june.assistant.domain.model.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    messages: List<Message>,
    currentMode: ContextMode,
    isVoiceEnabled: Boolean,
    onSendMessage: (String) -> Unit,
    onApproveAction: (ActionProposal) -> Unit,
    onCancelAction: (ActionProposal) -> Unit,
    onModeSelected: (ContextMode) -> Unit,
    onToggleVoice: () -> Unit,
    onStartVoiceInput: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                TopAppBar(
                    title = { Text("J.U.N.E.") },
                    actions = {
                        TextButton(onClick = onToggleVoice) {
                            Text(if (isVoiceEnabled) "Voice: ON" else "Voice: OFF")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                // Context Mode selection chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ContextMode.values().forEach { mode ->
                        FilterChip(
                            selected = mode == currentMode,
                            onClick = { onModeSelected(mode) },
                            label = { Text(mode.displayName) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("State a command or ask...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onStartVoiceInput) {
                        Text("Mic", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                            }
                        }
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    onApproveAction = onApproveAction,
                    onCancelAction = onCancelAction
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    onApproveAction: (ActionProposal) -> Unit,
    onCancelAction: (ActionProposal) -> Unit
) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bg = if (message.isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(bg, shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = if (message.isUser) "You" else "J.U.N.E.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium
                )

                message.actionProposal?.let { action ->
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Action: ${action.summary()}",
                        style = MaterialTheme.typography.labelMedium
                    )

                    if (action.status == ActionStatus.PROPOSED) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onApproveAction(action) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Approve")
                            }
                            OutlinedButton(onClick = { onCancelAction(action) }) {
                                Text("Cancel")
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Status: ${action.status.value}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
