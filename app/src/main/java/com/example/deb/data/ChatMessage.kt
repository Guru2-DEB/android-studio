package com.example.deb.data

//프론트 UI용 메시지 모델 (RecyclerView에서 사용자/AI 메시지를 구분하는 데 사용)
data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

//백엔드 API 통신용 메시지 포맷
data class ChatRequestMessage(
    val role: String,
    val content: String
)

//백엔드 API 요청
data class ChatRequest(
    val messages: List<ChatRequestMessage>
)

//백엔드 API 응답
data class ChatResponse(
    val response: String
)

data class FeedbackRequest(
    val user_answer: String,
    val core_term: String
)

data class StudyRequest(
    val news_content: String
)