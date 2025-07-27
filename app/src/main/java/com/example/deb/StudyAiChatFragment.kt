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

    companion object {
        private const val ARG_NEWS_CONTENT = "news_content"
        fun newInstance(newsContent: String) = StudyAiChatFragment().apply {
            arguments = Bundle().apply { putString(ARG_NEWS_CONTENT, newsContent) }
        }
    }

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

        // 전달된 뉴스 컨텐츠(URL or 본문)
        val newsContent = arguments?.getString(ARG_NEWS_CONTENT)
            ?: "뉴스 내용이 없습니다."

        // 프론트 채팅창에 AI 요청 (자동 시작)
        sendNewsToDeBil(newsContent)

        // 2) 사용자가 입력한 답변을 서버로 전송
        sendButton.setOnClickListener {
            val fullInput = editText.text.toString().trim()
            if (fullInput.isNotEmpty()) {
                // 1) 사용자 메시지 표시
                addMessage(fullInput, isUser = true)
                editText.setText("")

                // 2) “용어: 설명” 으로 분리
                val parts = fullInput.split(":", limit = 2)
                if (parts.size == 2) {
                    val coreTerm   = parts[0].trim()
                    val userAnswer = parts[1].trim()
                    // 평가 요청
                    sendAnswerToDeBil(userAnswer, coreTerm)
                } else {
                    // 형식이 잘못됐으면 안내 메시지
                    addMessage(
                        "⚠️ 답변은 ‘용어: 설명’ 형식으로 입력해주세요.\n예) 제로데이: 패치 안된 취약점을 공격하는 기법",
                        isUser = false
                    )
                }
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
