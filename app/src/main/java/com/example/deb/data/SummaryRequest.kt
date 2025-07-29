package com.example.deb.data

/**
 * AI 요약 요청용 DTO
 * @param session_id  대화 세션 DB ID
 * @param messages    대화 메시지 리스트 (role="USER"|"AI", content=텍스트)
 */
data class SummaryRequest(
    val session_id: Long,
    val messages: List<ChatRequestMessage>
)
