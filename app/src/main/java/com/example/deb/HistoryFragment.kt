package com.example.deb

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deb.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment(R.layout.fragment_history) {
    private val db by lazy { AppDatabase.getInstance(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.historyRecyclerView)
        rv.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch(Dispatchers.IO) {
            val sessions = db.conversationDao().getAllConversations()
            withContext(Dispatchers.Main) {
                rv.adapter = ConversationAdapter(sessions) { sessionId ->
                    // 블록 클릭 시 상세 화면으로 이동
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.mainContainer,
                            HistoryDetailFragment.newInstance(sessionId))
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }
}
