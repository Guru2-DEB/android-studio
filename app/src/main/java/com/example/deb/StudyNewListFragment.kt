// app/src/main/java/com/example/deb/StudyNewListFragment.kt
package com.example.deb

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StudyNewListFragment : Fragment() {

  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: NewsAdapter
  private val newsList = mutableListOf<NewsItem>()

  private val newsViewModel: NewsViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val view = inflater.inflate(R.layout.fragment_study_new_list, container, false)
    recyclerView = view.findViewById(R.id.recyclerView)
    recyclerView.layoutManager = LinearLayoutManager(requireContext())

    adapter = NewsAdapter(newsList) { selectedNews ->
      // newInstance 호출 시 세 개 인자 모두 넘겨 줍니다
      val detailFragment = StudyNewDetailFragment.newInstance(
        title   = selectedNews.title,
        content = selectedNews.description,
        url     = selectedNews.link
      )
      requireActivity().supportFragmentManager.beginTransaction()
        .replace(R.id.mainContainer, detailFragment)
        .addToBackStack(null)
        .commit()
    }
    recyclerView.adapter = adapter
    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    NewsRepository.fetchNews(
      context = requireContext(),
      onSuccess = { fetchedNews ->
        newsList.clear()
        newsList.addAll(fetchedNews)

        if (isAdded) {
          adapter.notifyDataSetChanged()
          newsViewModel.setNews(newsList.toList())

          if (newsList.isEmpty()) {
            Toast.makeText(requireContext(), "뉴스가 없어요", Toast.LENGTH_SHORT).show()
          }
        }
      },
      onFailure = { error ->
        if (isAdded) {
          Log.e("API_ERROR", error.toString())
          Toast.makeText(requireContext(), "뉴스를 불러오는 데 문제가 발생했어요.", Toast.LENGTH_SHORT).show()
        }
      }
    )
  }
}
