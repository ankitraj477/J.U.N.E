package com.june.assistant.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class JuneDatabase private constructor(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    "june_database.db",
    null,
    1
) {
    private val messagesFlow = MutableStateFlow<List<MessageEntity>>(emptyList())

    suspend fun loadInitial() = withContext(Dispatchers.IO) {
        refreshMessages()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS messages (
                id TEXT PRIMARY KEY,
                text TEXT NOT NULL,
                is_user INTEGER NOT NULL,
                action_id TEXT,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS actions (
                id TEXT PRIMARY KEY,
                action_type TEXT NOT NULL,
                target TEXT NOT NULL,
                risk_level TEXT NOT NULL,
                status TEXT NOT NULL,
                reason TEXT,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS messages")
        db.execSQL("DROP TABLE IF EXISTS actions")
        onCreate(db)
    }

    @Synchronized
    private fun refreshMessages() {
        val list = mutableListOf<MessageEntity>()
        val db = readableDatabase
        val cursor = db.query(
            "messages",
            null,
            null,
            null,
            null,
            null,
            "timestamp ASC"
        )
        cursor.use { c ->
            val idIdx = c.getColumnIndexOrThrow("id")
            val textIdx = c.getColumnIndexOrThrow("text")
            val isUserIdx = c.getColumnIndexOrThrow("is_user")
            val actionIdIdx = c.getColumnIndexOrThrow("action_id")
            val timestampIdx = c.getColumnIndexOrThrow("timestamp")

            while (c.moveToNext()) {
                list.add(
                    MessageEntity(
                        id = c.getString(idIdx),
                        text = c.getString(textIdx),
                        isUser = c.getInt(isUserIdx) == 1,
                        actionId = if (c.isNull(actionIdIdx)) null else c.getString(actionIdIdx),
                        timestamp = c.getLong(timestampIdx)
                    )
                )
            }
        }
        messagesFlow.value = list
    }

    val messageDaoInstance = object : MessageDao {
        override fun getAllMessages(): Flow<List<MessageEntity>> = messagesFlow.asStateFlow()

        override suspend fun insertMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("id", message.id)
                put("text", message.text)
                put("is_user", if (message.isUser) 1 else 0)
                put("action_id", message.actionId)
                put("timestamp", message.timestamp)
            }
            db.insertWithOnConflict("messages", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            refreshMessages()
        }

        override suspend fun clearMessages() = withContext(Dispatchers.IO) {
            writableDatabase.delete("messages", null, null)
            refreshMessages()
        }
    }

    val actionDaoInstance = object : ActionDao {
        override suspend fun getActionById(id: String): ActionEntity? = withContext(Dispatchers.IO) {
            val db = readableDatabase
            val cursor = db.query(
                "actions",
                null,
                "id = ?",
                arrayOf(id),
                null,
                null,
                null
            )
            cursor.use { c ->
                if (c.moveToFirst()) {
                    ActionEntity(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        actionType = c.getString(c.getColumnIndexOrThrow("action_type")),
                        target = c.getString(c.getColumnIndexOrThrow("target")),
                        riskLevel = c.getString(c.getColumnIndexOrThrow("risk_level")),
                        status = c.getString(c.getColumnIndexOrThrow("status")),
                        reason = c.getString(c.getColumnIndexOrThrow("reason")) ?: "",
                        timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"))
                    )
                } else null
            }
        }

        override suspend fun insertOrUpdateAction(action: ActionEntity) = withContext(Dispatchers.IO) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("id", action.id)
                put("action_type", action.actionType)
                put("target", action.target)
                put("risk_level", action.riskLevel)
                put("status", action.status)
                put("reason", action.reason)
                put("timestamp", action.timestamp)
            }
            db.insertWithOnConflict("actions", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            Unit
        }
    }

    fun messageDao(): MessageDao = messageDaoInstance
    fun actionDao(): ActionDao = actionDaoInstance

    companion object {
        @Volatile
        private var INSTANCE: JuneDatabase? = null

        fun getDatabase(context: Context): JuneDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: JuneDatabase(context).also { INSTANCE = it }
            }
        }
    }
}
