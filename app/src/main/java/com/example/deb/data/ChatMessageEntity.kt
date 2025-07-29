// app/src/main/java/com/example/deb/data/ChatMessageEntity.kt
package com.example.deb.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    /** PK로 사용할 autoGenerate Long ID */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** 이 메시지가 속한 대화(세션) 고유 ID */
    val conversationId: Long,

    /** 보낸 주체: "USER" 또는 "AI" */
    val sender: String,

    /** 실제 본문 */
    val message: String,

    /** 보통 System.currentTimeMillis() 를 넣습니다 */
    val timestamp: Long
)
