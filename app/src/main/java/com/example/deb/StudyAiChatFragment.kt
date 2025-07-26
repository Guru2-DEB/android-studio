package com.example.deb

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudyAiChatFragment : Fragment() {

    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_study_ai_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // RecyclerView 세팅
        val recyclerView = view.findViewById<RecyclerView>(R.id.chatRecyclerView)
        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.adapter = chatAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // EditText, SendButton 레퍼런스
        val editText = view.findViewById<EditText>(R.id.messageEditText)
        val sendButton = view.findViewById<ImageButton>(R.id.sendButton)

        // 1) 예시 뉴스 한 번만 전송
        sendNewsToDeBil(
            "2025년 7월 25일, 국내 대형 온라인 쇼핑몰인 EZShop이 해킹 공격을 받아 " +
                    "고객 개인정보 20만 건이 유출되었습니다. 공격자는 제로데이 취약점을 이용해 " +
                    "오래된 시스템에 침투했으며, SQL 인젝션 및 피싱 이메일을 동시 사용했습니다. " +
                    "현재 EZShop은 보안 패치를 적용하고 사고 수습에 나선 상태입니다."
        )

        // 2) 사용자가 입력한 답변을 서버로 전송
        sendButton.setOnClickListener {
            val userInput = editText.text.toString().trim()
            if (userInput.isNotEmpty()) {
                // 2-1) 사용자 메시지 표시
                addMessage(userInput, isUser = true)
                editText.setText("")

                // 2-2) AI 평가 요청 (여기서는 예시 핵심 용어를 '제로데이'로 고정)
                sendAnswerToDeBil(userInput, coreTerm = "제로데이")
            }
        }
    }

    // 채팅창에 메시지 추가
    private fun addMessage(text: String, isUser: Boolean) {
        chatMessages.add(ChatMessage(text, isUser))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        view?.findViewById<RecyclerView>(R.id.chatRecyclerView)
            ?.scrollToPosition(chatMessages.size - 1)
    }

    // 1. 뉴스 전달 → 요약 + 핵심단어 + 질문 생성
    private fun sendNewsToDeBil(newsContent: String) {
        val request = StudyRequest(news_content = newsContent)
        RetrofitClient.apiService
            .sendNewsToStudyAI(request)
            .enqueue(object : Callback<ChatResponse> {
                override fun onResponse(
                    call: Call<ChatResponse>,
                    response: Response<ChatResponse>
                ) {
                    if (response.isSuccessful) {
                        val aiReply = response.body()?.response
                            ?: "DeBil이 말을 잃었어요..."
                        addMessage(aiReply, isUser = false)
                    } else {
                        addMessage("DeBil 응답 실패: ${response.code()}", isUser = false)
                        Log.e("DeBil", "sendNews 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                    addMessage("DeBil 연결 오류: ${t.localizedMessage}", isUser = false)
                    Log.e("DeBil", "sendNews 실패: ${t.localizedMessage}")
                }
            })
    }

    // 2. 사용자 답변 → 평가 + 조언 + 마무리 멘트
    private fun sendAnswerToDeBil(userAnswer: String, coreTerm: String) {
        val request = FeedbackRequest(
            user_answer = userAnswer,
            core_term   = coreTerm
        )
        RetrofitClient.apiService
            .sendAnswerToStudyAI(request)
            .enqueue(object : Callback<ChatResponse> {
                override fun onResponse(
                    call: Call<ChatResponse>,
                    response: Response<ChatResponse>
                ) {
                    if (response.isSuccessful) {
                        val aiReply = response.body()?.response
                            ?: "DeBil이 조용하네?"
                        addMessage(aiReply, isUser = false)
                    } else {
                        addMessage("DeBil 응답 실패: ${response.code()}", isUser = false)
                        Log.e("DeBil", "sendAnswer 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                    addMessage("DeBil 연결 실패: ${t.localizedMessage}", isUser = false)
                    Log.e("DeBil", "sendAnswer 실패: ${t.localizedMessage}")
                }
            })
    }
}
