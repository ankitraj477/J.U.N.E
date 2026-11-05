package com.june.assistant.domain.model

enum class RiskLevel(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high")
}

enum class ContextMode(val displayName: String) {
    FOCUS("Focus"),
    OFFICE("Office"),
    HOME("Home"),
    SLEEP("Sleep")
}

enum class ActionStatus(val value: String) {
    PROPOSED("proposed"),
    VALIDATED("validated"),
    AUTHORIZED("authorized"),
    APPROVED("approved"),
    EXECUTING("executing"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELLED("cancelled")
}


data class ActionProposal(
    val id: String,
    val actionType: String,
    val target: String,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val status: ActionStatus = ActionStatus.PROPOSED,
    val reason: String = ""
) {
    fun summary(): String = "$actionType on '$target' (risk: ${riskLevel.value})"
}

data class Message(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val actionProposal: ActionProposal? = null,
    val timestamp: Long = System.currentTimeMillis()
)

