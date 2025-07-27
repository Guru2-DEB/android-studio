package com.example.deb

import android.widget.Toast
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

  private val newsViewModel: NewsViewModel by activityViewModels()
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

  private lateinit var titleView: TextView
  private lateinit var descriptionView: TextView
  private lateinit var newsBtn: Button

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_home, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    titleView = view.findViewById(R.id.latestNewsTitle)
    descriptionView = view.findViewById(R.id.latestNewsDescription)
    newsBtn = view.findViewById(R.id.NewNewsBtn)

    newsViewModel.newsList.observe(viewLifecycleOwner) { newsList ->
      val sortedNews = newsList.sortedByDescending { news ->
        try {
          dateFormat.parse(news.pubDate)
        } catch (e: Exception) {
          null
        }
      }

      val latestNews = sortedNews.firstOrNull()

      if (latestNews != null) {
        titleView.text = latestNews.title
        descriptionView.text = latestNews.description

        titleView.setOnClickListener {
          val detailFragment = StudyNewDetailFragment.newInstance(latestNews.link)
          requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, detailFragment)
            .addToBackStack(null)
            .commit()
        }

        newsBtn.setOnClickListener {
          val detailFragment = StudyNewDetailFragment.newInstance(latestNews.link)
          requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, detailFragment)
            .addToBackStack(null)
            .commit()
        }

      } else {
        titleView.text = "최신 뉴스가 없습니다."
        descriptionView.text = " "

        titleView.setOnClickListener {
          Toast.makeText(requireContext(), "뉴스를 기다려 주세요", Toast.LENGTH_SHORT).show()
        }

        newsBtn.setOnClickListener {
          Toast.makeText(requireContext(), "뉴스를 기다려 주세요", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }
}
