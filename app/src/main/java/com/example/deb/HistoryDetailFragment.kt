// app/src/main/java/com/example/deb/HistoryDetailFragment.kt
package com.example.deb

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deb.data.AppDatabase
import com.example.deb.data.ChatMessage
import com.example.deb.data.ConversationEntity
import com.example.deb.data.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HistoryDetailFragment : Fragment(R.layout.fragment_history_detail) {

    companion object {
        private const val ARG_ID = "conv_id"
        fun newInstance(sessionId: Long) = HistoryDetailFragment().apply {
            arguments = Bundle().apply { putLong(ARG_ID, sessionId) }
        }
    }

    // 뷰들
    private lateinit var dateTv: TextView
    private lateinit var summaryBtn: Button
    private lateinit var recyclerView: RecyclerView

    private var conversationId: Long = 0L
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 뷰 레퍼런스
        dateTv       = view.findViewById(R.id.historyDate)
        summaryBtn   = view.findViewById(R.id.summaryButton)
        recyclerView = view.findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        conversationId = requireArguments().getLong(ARG_ID)

        // 1) 대화 세션 메타(날짜) 읽어오기
        lifecycleScope.launch(Dispatchers.IO) {
            val conv: ConversationEntity? = AppDatabase
                .getInstance(requireContext())
                .conversationDao()
                .getConversationById(conversationId)

            withContext(Dispatchers.Main) {
                conv?.let {
                    dateTv.text = dateFormat.format(Date(it.date))
                }
            }
        }

        // 2) 대화 메시지 불러와서 RecyclerView에 뿌리기
        lifecycleScope.launch(Dispatchers.IO) {
            val msgs: List<ChatMessageEntity> = AppDatabase
                .getInstance(requireContext())
                .chatDao()
                .getMessagesForConversation(conversationId)

            val uiList = msgs.map { ChatMessage(it.message, it.sender == "USER") }
            withContext(Dispatchers.Main) {
                recyclerView.adapter = ChatAdapter(uiList)
            }
        }

        // 3) Summary 버튼 클릭 → StudySummaryFragment 로 이동
        summaryBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.mainContainer,
                    StudySummaryFragment.newInstance(conversationId)
                )
                .addToBackStack(null)
                .commit()
        }
    }
}
