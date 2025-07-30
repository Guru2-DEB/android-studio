package com.example.deb

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deb.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private var conversationId: Long = 0L

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    private val db by lazy { AppDatabase.getInstance(requireContext()) }

    private var loadingPos: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_study_ai_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.chatRecyclerView)
        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = chatAdapter

        val editText = view.findViewById<EditText>(R.id.messageEditText)
        val sendButton = view.findViewById<ImageButton>(R.id.sendButton)

        val newsContent = arguments?.getString(ARG_NEWS_CONTENT) ?: "뉴스 내용이 없습니다."

        lifecycleScope.launch(Dispatchers.IO) {
            val convId = db.conversationDao().insert(
                ConversationEntity(
                    newsTitle = newsContent.take(20),
                    newsSnippet = newsContent.take(50),
                    date = System.currentTimeMillis()
                )
            )
            Log.d("DB_DEBUG", "✅ Conversation inserted. ID = $convId")
            conversationId = convId

            withContext(Dispatchers.Main) {
                sendNewsToDeBil(newsContent)
            }
        }

        sendButton.setOnClickListener {
            val text = editText.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            addMessage(text, isUser = true)
            editText.setText("")

            lifecycleScope.launch(Dispatchers.IO) {
                val entity = ChatMessageEntity(
                    conversationId = conversationId,
                    sender = "USER",
                    message = text,
                    timestamp = System.currentTimeMillis()
                )
                db.chatDao().insert(entity)
                Log.d("DB_DEBUG", "💾 USER message inserted: ${entity.message}")
            }

            showLoading()

            val parts = text.split(":", limit = 2)
            if (parts.size == 2) {
                sendAnswerToDeBil(
                    userAnswer = parts[1].trim(),
                    coreTerm = parts[0].trim()
                )
            } else {
                hideLoading()
                addMessage("⚠️ ‘용어: 설명’ 형식으로 입력해주세요.", isUser = false)
            }
        }
    }

    private fun showLoading() {
        val placeholder = ChatMessage("…", isUser = false)
        chatMessages.add(placeholder)
        loadingPos = chatMessages.lastIndex
        chatAdapter.notifyItemInserted(loadingPos!!)
        recyclerView.scrollToPosition(loadingPos!!)
    }

    private fun hideLoading() {
        loadingPos?.let { pos ->
            if (pos in chatMessages.indices && chatMessages[pos].text == "…") {
                chatMessages.removeAt(pos)
                chatAdapter.notifyItemRemoved(pos)
            }
        }
        loadingPos = null
    }

    private fun addMessage(text: String, isUser: Boolean) {
        hideLoading()
        chatMessages.add(ChatMessage(text, isUser))
        chatAdapter.notifyItemInserted(chatMessages.lastIndex)
        recyclerView.scrollToPosition(chatMessages.lastIndex)
    }

    private fun sendNewsToDeBil(newsContent: String) {
        showLoading()
        RetrofitClient.apiService
            .sendNewsToStudyAI(StudyRequest(news_content = newsContent))
            .enqueue(object : Callback<ChatResponse> {
                override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                    hideLoading()
                    if (response.isSuccessful) {
                        val aiReply = response.body()?.response ?: "DeBil이 말을 잃었어요..."

                        lifecycleScope.launch(Dispatchers.IO) {
                            val entity = ChatMessageEntity(
                                conversationId = conversationId,
                                sender = "AI",
                                message = aiReply,
                                timestamp = System.currentTimeMillis()
                            )
                            db.chatDao().insert(entity)
                            Log.d("DB_DEBUG", "💾 AI message inserted: ${entity.message}")
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

    private fun sendAnswerToDeBil(userAnswer: String, coreTerm: String) {
        RetrofitClient.apiService
            .sendAnswerToStudyAI(FeedbackRequest(user_answer = userAnswer, core_term = coreTerm))
            .enqueue(object : Callback<ChatResponse> {
                override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                    hideLoading()
                    if (response.isSuccessful) {
                        val aiReply = response.body()?.response ?: "DeBil이 조용하네?"

                        lifecycleScope.launch(Dispatchers.IO) {
                            val entity = ChatMessageEntity(
                                conversationId = conversationId,
                                sender = "AI",
                                message = aiReply,
                                timestamp = System.currentTimeMillis()
                            )
                            db.chatDao().insert(entity)
                            Log.d("DB_DEBUG", "💾 AI 평가 inserted: ${entity.message}")
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
