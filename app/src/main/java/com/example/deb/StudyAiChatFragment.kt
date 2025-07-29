// app/src/main/java/com/example/deb/StudyAiChatFragment.kt
package com.example.deb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deb.data.AppDatabase
import com.example.deb.data.ChatMessage
import com.example.deb.data.ChatMessageEntity
import com.example.deb.data.ChatResponse
import com.example.deb.data.FeedbackRequest
import com.example.deb.data.StudyRequest
import com.example.deb.data.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    // 세션 단위로 묶을 ID (앱 실행 중 Fragment 생성 시 한 번만)
    private val sessionId: Long by lazy { System.currentTimeMillis() }

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    // DAO 인스턴스
    private val chatDao by lazy { AppDatabase.getInstance(requireContext()).chatDao() }

    // 로딩 아이템 위치 추적
    private var loadingPos: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_study_ai_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 세팅
        recyclerView = view.findViewById(R.id.chatRecyclerView)
        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = chatAdapter

        val editText   = view.findViewById<EditText>(R.id.messageEditText)
        val sendButton = view.findViewById<ImageButton>(R.id.sendButton)

        // 자동 시작: 뉴스 → AI
        val newsContent = arguments?.getString(ARG_NEWS_CONTENT) ?: "뉴스 내용이 없습니다."
        sendNewsToDeBil(newsContent)

        // 사용자 입력 처리
        sendButton.setOnClickListener {
            val text = editText.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            // 화면에 바로 표시
            addMessage(text, isUser = true)
            editText.setText("")

            // DB에도 저장 (I/O 스레드)
            lifecycleScope.launch(Dispatchers.IO) {
                chatDao.insert(
                    ChatMessageEntity(
                        conversationId = sessionId,
                        sender         = "USER",
                        message        = text,
                        timestamp      = System.currentTimeMillis()
                    )
                )
            }

            // 로딩 표시
            showLoading()

            // “용어: 설명” 분리 & AI 요청
            val parts = text.split(":", limit = 2)
            if (parts.size == 2) {
                sendAnswerToDeBil(
                    userAnswer = parts[1].trim(),
                    coreTerm   = parts[0].trim()
                )
            } else {
                hideLoading()
                addMessage("⚠️ ‘용어: 설명’ 형식으로 입력해주세요.", isUser = false)
            }
        }
    }

    /** 로딩용 아이템 추가 */
    private fun showLoading() {
        val placeholder = ChatMessage("…", isUser = false)
        chatMessages.add(placeholder)
        loadingPos = chatMessages.lastIndex
        chatAdapter.notifyItemInserted(loadingPos!!)
        recyclerView.scrollToPosition(loadingPos!!)
    }

    /** 로딩 아이템 제거 */
    private fun hideLoading() {
        loadingPos?.let { pos ->
            if (pos in chatMessages.indices && chatMessages[pos].text == "…") {
                chatMessages.removeAt(pos)
                chatAdapter.notifyItemRemoved(pos)
            }
        }
        loadingPos = null
    }

    /** 메시지 추가 (로딩 있으면 제거 후) */
    private fun addMessage(text: String, isUser: Boolean) {
        hideLoading()
        chatMessages.add(ChatMessage(text, isUser))
        chatAdapter.notifyItemInserted(chatMessages.lastIndex)
        recyclerView.scrollToPosition(chatMessages.lastIndex)
    }

    /** 1) 뉴스 전달 → 요약＋핵심단어＋질문 생성 요청 */
    private fun sendNewsToDeBil(newsContent: String) {
        showLoading()
        RetrofitClient.apiService
            .sendNewsToStudyAI(StudyRequest(news_content = newsContent))
            .enqueue(object : Callback<ChatResponse> {
                override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                    hideLoading()
                    if (response.isSuccessful) {
                        val aiReply = response.body()?.response ?: "DeBil이 말을 잃었어요..."

                        // DB 저장 (I/O 스레드)
                        lifecycleScope.launch(Dispatchers.IO) {
                            chatDao.insert(
                                ChatMessageEntity(
                                    conversationId = sessionId,
                                    sender         = "AI",
                                    message        = aiReply,
                                    timestamp      = System.currentTimeMillis()
                                )
                            )
                        }
                        addMessage(aiReply, isUser = false)
                    } else {
                        addMessage("DeBil 응답 실패: ${response.code()}", isUser = false)
                    }
                }

                override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                    hideLoading()
                    addMessage("연결 오류: ${t.localizedMessage}", isUser = false)
                }
            })
    }

    /** 2) 사용자 답변 → 평가＋조언＋마무리 멘트 요청 */
    private fun sendAnswerToDeBil(userAnswer: String, coreTerm: String) {
        RetrofitClient.apiService
            .sendAnswerToStudyAI(FeedbackRequest(user_answer = userAnswer, core_term = coreTerm))
            .enqueue(object : Callback<ChatResponse> {
                override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                    hideLoading()
                    if (response.isSuccessful) {
                        val aiReply = response.body()?.response ?: "DeBil이 조용하네?"

                        // DB 저장 (I/O 스레드)
                        lifecycleScope.launch(Dispatchers.IO) {
                            chatDao.insert(
                                ChatMessageEntity(
                                    conversationId = sessionId,
                                    sender         = "AI",
                                    message        = aiReply,
                                    timestamp      = System.currentTimeMillis()
                                )
                            )
                        }
                        addMessage(aiReply, isUser = false)
                    } else {
                        addMessage("DeBil 응답 실패: ${response.code()}", isUser = false)
                    }
                }

                override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                    hideLoading()
                    addMessage("연결 오류: ${t.localizedMessage}", isUser = false)
                }
            })
    }
}
