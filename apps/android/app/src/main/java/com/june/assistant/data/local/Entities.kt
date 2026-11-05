package com.june.assistant.data.local

data class MessageEntity(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val actionId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ActionEntity(
    val id: String,
    val actionType: String,
    val target: String,
    val riskLevel: String,
    val status: String,
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
