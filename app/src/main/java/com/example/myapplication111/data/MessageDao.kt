package com.example.myapplication111.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAll(): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE sender = :sender ORDER BY timestamp ASC")
    suspend fun getMessagesBySender(sender: String): List<MessageEntity>
}
