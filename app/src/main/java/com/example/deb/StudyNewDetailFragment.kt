package com.example.deb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class StudyNewDetailFragment : Fragment() {

  companion object {
    private const val ARG_NEWS_URL = "news_url"

    fun newInstance(newsUrl: String): StudyNewDetailFragment {
      val fragment = StudyNewDetailFragment()
      val args = Bundle()
      args.putString(ARG_NEWS_URL, newsUrl)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val view = inflater.inflate(R.layout.fragment_study_new_detail, container, false)

    val webView = view.findViewById<WebView>(R.id.webView)
    webView.webViewClient = WebViewClient()
    webView.settings.javaScriptEnabled = true

    val newsUrl = arguments?.getString(ARG_NEWS_URL) ?: "https://example.com/news"
    webView.loadUrl(newsUrl)

    return view
  }
}
