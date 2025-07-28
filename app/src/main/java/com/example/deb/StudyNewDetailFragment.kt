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
    private const val ARG_NEWS_URL = "news_url"
    fun newInstance(newsUrl: String): StudyNewDetailFragment {
      return StudyNewDetailFragment().apply {
        arguments = Bundle().apply {
          putString(ARG_NEWS_URL, newsUrl)
        }
      }
    }
  }

  private lateinit var webView: WebView
  private lateinit var startButton: Button
  private lateinit var newsUrl: String

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val view = inflater.inflate(R.layout.fragment_study_new_detail, container, false)
    webView = view.findViewById(R.id.webView)
    startButton = view.findViewById(R.id.startStudyButton)
    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // WebView 세팅
    webView.webViewClient = WebViewClient()
    webView.settings.javaScriptEnabled = true

    // 인자로 받은 URL 읽기
    newsUrl = arguments?.getString(ARG_NEWS_URL)
      ?: "https://example.com/news"

    webView.loadUrl(newsUrl)

    // 버튼 클릭 시 StudyAiChatFragment 로 전환 & URL 전달
    startButton.setOnClickListener {
      val chatFrag = StudyAiChatFragment.newInstance(newsUrl)
      parentFragmentManager.beginTransaction()
        .replace(R.id.mainContainer, chatFrag)
        .addToBackStack(null)
        .commit()
    }
  }
}
