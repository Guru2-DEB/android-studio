package com.example.deb

import android.content.Context
import android.text.Html
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.net.URI
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*


object NewsRepository {

  private val allowedDomains = listOf( //뉴스 api의 제한으로 도메인 기반 신문사 분리 제거.
    "itworld.co.kr",
    "heraldcorp.com",
    "dailysecu.com"
  )

  fun fetchNews(
    context: Context,
    onSuccess: (List<NewsItem>) -> Unit,
    onFailure: (Exception) -> Unit
  ) {
    val url = "https://openapi.naver.com/v1/search/news.json"
    //검색 기반 단어 선정. 도메인 필터가 불가해짐에 따라 특정 단어를 조합하여 섬색어 지정
    val query = "암호|해킹|정보보호|보안|취약점|랜섬웨어|DDoS|정보|사이버|인증" //모든 뉴스를 확인 하는 경우: 기자로 키워드 변경.

    val apiDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
    val displayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val requestQueue = Volley.newRequestQueue(context)

    val request = object : StringRequest(
      Request.Method.GET,
      "$url?query=${URLEncoder.encode(query, "UTF-8")}&sort=date&display=100",
      { response ->
        val json = JSONObject(response)
        val items = json.getJSONArray("items")
        val newsList = mutableListOf<NewsItem>()

        for (i in 0 until items.length()) {
          val item = items.getJSONObject(i)
          val pubDateStr = item.optString("pubDate")
          val link = item.optString("link")

          try {
            val pubDate = apiDateFormat.parse(pubDateStr)
            if (pubDate != null) {
              val localPubDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(pubDate)
              val domain = URI(link).host?.replace("www.", "") ?: ""

              val isToday = localPubDateStr == todayStr
              // val isAllowedDomain = allowedDomains.any { domain.endsWith(it) }

              if (isToday) {
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
               }
            }
          } catch (e: Exception) {
            Log.e("DATE_PARSE_ERROR", "날짜 파싱 실패: $pubDateStr", e)
          }
        }

        onSuccess(newsList)
      },
      { error ->
        onFailure(Exception(error.toString()))
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
