package com.example.deb

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.net.URI
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class StudyNewListFragment : Fragment() {

  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: NewsAdapter
  private val newsList = mutableListOf<NewsItem>()

  // ViewModel 선언
  private val newsViewModel: NewsViewModel by activityViewModels()

  // 도메인 필터 (현재 사용 안 함)
  private val allowedDomains = listOf(
    "itworld.co.kr",
    "heraldcorp.com",
    "dailysecu.com"
  )

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val view = inflater.inflate(R.layout.fragment_study_new_list, container, false)
    recyclerView = view.findViewById(R.id.recyclerView)
    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    adapter = NewsAdapter(newsList) { selectedNews ->
      val detailFragment = StudyNewDetailFragment.newInstance(selectedNews.link)
      requireActivity().supportFragmentManager.beginTransaction()
        .replace(R.id.mainContainer, detailFragment)
        .addToBackStack(null)
        .commit()
    }
    recyclerView.adapter = adapter

    fetchNews()

    return view
  }

  private fun fetchNews() {
    val url = "https://openapi.naver.com/v1/search/news.json"
    val query = "기자"

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val apiDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
    val displayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    val context = context ?: return
    val requestQueue = Volley.newRequestQueue(context)

    val request = object : StringRequest(
      Request.Method.GET,
      "$url?query=${URLEncoder.encode(query, "UTF-8")}&sort=date&display=100",
      { response ->
        val json = JSONObject(response)
        val items = json.getJSONArray("items")
        newsList.clear()

        for (i in 0 until items.length()) {
          val item = items.getJSONObject(i)
          val pubDateStr = item.optString("pubDate")
          val link = item.optString("link")

          try {
            val pubDate = apiDateFormat.parse(pubDateStr)
            if (pubDate != null) {
              val localPubDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(pubDate)
              val domain = URI(link).host?.replace("www.", "") ?: ""

              // 🔽 필터 조건 주석 처리
              // val isToday = localPubDateStr == todayStr
              // val isAllowedDomain = allowedDomains.any { domain.endsWith(it) }

              // if (isToday && isAllowedDomain) {
              val title = Html.fromHtml(item.optString("title"), Html.FROM_HTML_MODE_LEGACY).toString()
              val description = Html.fromHtml(item.optString("description"), Html.FROM_HTML_MODE_LEGACY).toString()

              newsList.add(
                NewsItem(
                  title = title,
                  description = description,
                  pubDate = displayFormat.format(pubDate),
                  link = link
                )
              )
              // }
            }
          } catch (e: Exception) {
            Log.e("DATE_PARSE_ERROR", "날짜 파싱 실패: $pubDateStr", e)
          }
        }

        if (isAdded) {
          adapter.notifyDataSetChanged()

          // ViewModel에 데이터 공유
          newsViewModel.setNews(newsList.toList())

          if (newsList.isEmpty()) {
            Toast.makeText(requireContext(), "뉴스가 없어요", Toast.LENGTH_SHORT).show()
          }
        }
      },
      { error ->
        if (isAdded) {
          Log.e("API_ERROR", error.toString())
          Toast.makeText(requireContext(), "뉴스를 불러오는 데 문제가 발생했어요.", Toast.LENGTH_SHORT).show()
        }
      }
    ) {
      override fun getHeaders(): MutableMap<String, String> {
        return mutableMapOf(
          "X-Naver-Client-Id" to BuildConfig.NAVER_CLIENT_ID,
          "X-Naver-Client-Secret" to BuildConfig.NAVER_CLIENT_SECRET
        )
      }
    }

    requestQueue.add(request)
  }
}
