package com.example.deb

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.deb.data.AppDatabase
import com.example.deb.data.ChatRequestMessage
import com.example.deb.data.ChatMessageEntity
import com.example.deb.data.ChatResponse
import com.example.deb.data.RetrofitClient
import com.example.deb.data.SummaryRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class StudySummaryFragment : Fragment(R.layout.fragment_study_summary) {

    companion object {
        private const val ARG_SESSION_ID = "session_id"
        fun newInstance(sessionId: Long): StudySummaryFragment =
            StudySummaryFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_SESSION_ID, sessionId)
                }
            }
    }

    private var sessionId: Long = 0L

    private lateinit var dateTv: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var summaryScroll: ScrollView
    private lateinit var summaryText: TextView

    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 뷰 바인딩 (summaryButton 바인딩 코드는 제거했습니다)
        dateTv        = view.findViewById(R.id.toDate)
        progressBar   = view.findViewById(R.id.summaryProgress)
        summaryScroll = view.findViewById(R.id.summaryScroll)
        summaryText   = view.findViewById(R.id.summaryText)

        sessionId = requireArguments().getLong(ARG_SESSION_ID)

        // 2) 세션 날짜 표시
        lifecycleScope.launch(Dispatchers.IO) {
            val conv = AppDatabase
                .getInstance(requireContext())
                .conversationDao()
                .getConversationById(sessionId)
            conv?.let {
                val formatted = dateFormat.format(Date(it.date))
                withContext(Dispatchers.Main) {
                    dateTv.text = formatted
                }
            }
        }

        // 3) AI 요약 API 호출
        progressBar.visibility   = View.VISIBLE
        summaryScroll.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            val msgs: List<ChatMessageEntity> = AppDatabase
                .getInstance(requireContext())
                .chatDao()
                .getMessagesForConversation(sessionId)

            val req = SummaryRequest(
                session_id = sessionId,
                messages = msgs.map {
                    ChatRequestMessage(
                        role = it.sender,
                        content = it.message
                    )
                }
            )

            withContext(Dispatchers.Main) {
                RetrofitClient.apiService
                    .requestSummary(req)
                    .enqueue(object : Callback<ChatResponse> {
                        override fun onResponse(
                            call: Call<ChatResponse>,
                            response: Response<ChatResponse>
                        ) {
                            progressBar.visibility   = View.GONE
                            summaryText.text         = response.body()?.response
                                ?: "요약을 받아올 수 없어요."
                            summaryScroll.visibility = View.VISIBLE
                        }
                        override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                            progressBar.visibility   = View.GONE
                            summaryText.text         = "요약 요청 실패: ${t.localizedMessage}"
                            summaryScroll.visibility = View.VISIBLE
                        }
                    })
            }
        }
    }
}
