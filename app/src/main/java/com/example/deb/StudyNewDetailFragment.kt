// app/src/main/java/com/example/deb/StudyNewDetailFragment.kt
package com.example.deb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.fragment.app.Fragment

class StudyNewDetailFragment : Fragment() {

  companion object {
    private const val ARG_NEWS_TITLE   = "news_title"
    private const val ARG_NEWS_CONTENT = "news_content"
    private const val ARG_NEWS_URL     = "news_url"

    /**
     * @param title   선택한 기사 제목 (화면 표시용)
     * @param content 기사 본문(또는 요약)  → ChatFragment로 보낼 내용
     * @param url     기사 URL (WebView 로드용)
     */
    fun newInstance(
      title: String,
      content: String,
      url: String
    ): StudyNewDetailFragment = StudyNewDetailFragment().apply {
      arguments = Bundle().apply {
        putString(ARG_NEWS_TITLE, title)
        putString(ARG_NEWS_CONTENT, content)
        putString(ARG_NEWS_URL, url)
      }
    }
  }

  private lateinit var webView: WebView
  private lateinit var startButton: Button

  private lateinit var newsTitle: String
  private lateinit var newsContent: String
  private lateinit var newsUrl: String

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(
    R.layout.fragment_study_new_detail, container, false
  ).also { view ->
    webView     = view.findViewById(R.id.webView)
    startButton = view.findViewById(R.id.startStudyButton)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // 1) 인자로 넘어온 값 꺼내기
    newsTitle   = requireArguments().getString(ARG_NEWS_TITLE)   ?: "(제목 없음)"
    newsContent = requireArguments().getString(ARG_NEWS_CONTENT) ?: ""
    newsUrl     = requireArguments().getString(ARG_NEWS_URL)     ?: "https://example.com/news"

    // 2) WebView 세팅
    webView.webViewClient             = WebViewClient()
    webView.settings.javaScriptEnabled = true
    webView.loadUrl(newsUrl)

    // 3) 버튼 클릭 시 Chat 화면으로 이동 (newsContent만 넘기면 됩니다)
    startButton.setOnClickListener {
      val chatFrag = StudyAiChatFragment.newInstance(newsContent)
      parentFragmentManager.beginTransaction()
        .replace(R.id.mainContainer, chatFrag)
        .addToBackStack(null)
        .commit()
    }
  }
}
