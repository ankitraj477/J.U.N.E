package com.june.assistant.data.local

import kotlinx.coroutines.flow.Flow

interface MessageDao {
    fun getAllMessages(): Flow<List<MessageEntity>>
    suspend fun insertMessage(message: MessageEntity)
    suspend fun clearMessages()
}

interface ActionDao {
    suspend fun getActionById(id: String): ActionEntity?
    suspend fun insertOrUpdateAction(action: ActionEntity)
}
