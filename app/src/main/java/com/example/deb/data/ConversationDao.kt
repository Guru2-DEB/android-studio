// app/src/main/java/com/example/deb/data/ConversationDao.kt
package com.example.deb.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(conv: ConversationEntity): Long

    @Query("SELECT * FROM conversations ORDER BY date DESC")
    fun getAllConversations(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId")
    fun getConversationById(conversationId: Long): ConversationEntity?
}
