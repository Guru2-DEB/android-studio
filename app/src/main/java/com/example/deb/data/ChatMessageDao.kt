// app/src/main/java/com/example/deb/data/ChatMessageDao.kt
package com.example.deb.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    // suspend 제거
    fun insert(msg: ChatMessageEntity)

    @Query("""
    SELECT * FROM chat_messages
    WHERE conversationId = :convId
    ORDER BY timestamp ASC
  """)
    // suspend 제거
    fun getMessagesForConversation(convId: Long): List<ChatMessageEntity>

    // (옵션) 전체 메시지 조회도 suspend 제거
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): List<ChatMessageEntity>
}
