package com.example.deb.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val conversationId: Long = 0L,
    val newsTitle: String,          // 선택한 기사 제목
    val newsSnippet: String,        // 기사 내용 일부(예: 50자)
    val date: Long,                 // 세션 생성 시각
    val link: String? = null        // (선택) 기사 URL
)
