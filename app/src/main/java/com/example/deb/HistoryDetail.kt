package com.example.deb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryDetail : Fragment() {

    private val dummyMessages = listOf(
        ChatMessage("안녕!", true),
        ChatMessage("안녕하세요! 무엇을 도와드릴까요?", false),
        ChatMessage("요약 부탁해", true),
        ChatMessage("오늘은 이런 주제를 이야기하셨어요: ...", false)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dateTextView = view.findViewById<TextView>(R.id.historyDate)
        // 예: 현재 날짜 표시 (혹은 전달받은 날짜)
        val date = "2025.07.25" // 나중에 파라미터로 대체 가능
        dateTextView.text = date

        val recyclerView = view.findViewById<RecyclerView>(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ChatAdapter(dummyMessages)

        val summaryButton = view.findViewById<Button>(R.id.summaryButton)
        summaryButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainContainer, StudySummaryFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
