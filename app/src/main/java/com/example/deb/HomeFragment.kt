package com.example.deb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.deb.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

  // 기존 뷰모델, 포맷터
  private val newsViewModel: NewsViewModel by activityViewModels()
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
  // 오늘 날짜 비교용
  private val dayFormat  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

  // 뉴스 뷰
  private lateinit var titleView: TextView
  private lateinit var descriptionView: TextView
  private lateinit var newsBtn: Button

  // 여기에 추가!
  private lateinit var totalNumView: TextView
  private lateinit var todoProgressView: TextView

  // Room DAO
  private val conversationDao by lazy {
    AppDatabase.getInstance(requireContext()).conversationDao()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? = inflater.inflate(R.layout.fragment_home, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // 1) 뷰 레퍼런스 획득
    titleView        = view.findViewById(R.id.latestNewsTitle)
    descriptionView  = view.findViewById(R.id.latestNewsDescription)
    newsBtn          = view.findViewById(R.id.NewNewsBtn)

    totalNumView     = view.findViewById(R.id.TotalNum)       // ← 추가
    todoProgressView = view.findViewById(R.id.TodoProgress)   // ← 추가

    // 2) 뉴스 불러오기 (이전과 동일)
    NewsRepository.fetchNews(
      context   = requireContext(),
      onSuccess = { fetchedNews ->
        newsViewModel.setNews(fetchedNews)
      },
      onFailure = {
        Toast.makeText(requireContext(), "뉴스를 불러오는 데 실패했어요.", Toast.LENGTH_SHORT).show()
      }
    )

    // 3) 뉴스 옵저버 (이전과 동일)
    newsViewModel.newsList.observe(viewLifecycleOwner) { newsList ->
      val sortedNews = newsList.sortedByDescending { news ->
        try { dateFormat.parse(news.pubDate) } catch (e: Exception) { null }
      }
      val latestNews = sortedNews.firstOrNull()

      if (latestNews != null) {
        titleView.text       = latestNews.title
        descriptionView.text = latestNews.description

        val openDetail = {
          val detailFragment = StudyNewDetailFragment.newInstance(
            title   = latestNews.title,
            content = latestNews.description,
            url     = latestNews.link
          )
          requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, detailFragment)
            .addToBackStack(null)
            .commit()
        }

        titleView.setOnClickListener { openDetail() }
        newsBtn.setOnClickListener   { openDetail() }
      } else {
        titleView.text       = "최신 뉴스가 없습니다."
        descriptionView.text = " "

        val showToast = {
          Toast.makeText(requireContext(), "뉴스를 기다려 주세요", Toast.LENGTH_SHORT).show()
        }
        titleView.setOnClickListener { showToast() }
        newsBtn.setOnClickListener   { showToast() }
      }
    }

    // 4) **여기**: Room 에 저장된 세션(히스토리) 개수와 오늘 완료 여부 표시
    lifecycleScope.launch(Dispatchers.IO) {
      // ConversationEntity.date 가 Long(타임스탬프)이라고 가정
      val sessions   = conversationDao.getAllConversations()  // suspend fun
      val totalCount = sessions.size
      val todayStr   = dayFormat.format(Date())
      val doneToday  = sessions.any { dayFormat.format(Date(it.date)) == todayStr }

      withContext(Dispatchers.Main) {
        totalNumView.text     = totalCount.toString()
        todoProgressView.text = if (doneToday) "Done" else "Not done"
      }
    }
  }
}
