package com.example.deb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NewsViewModel : ViewModel() {

  // 내부에서만 수정 가능한 MutableLiveData
  private val _newsList = MutableLiveData<List<NewsItem>>()

  // 외부에서는 읽기만 가능한 LiveData
  val newsList: LiveData<List<NewsItem>> get() = _newsList

  // 뉴스 데이터를 설정하는 함수
  fun setNews(items: List<NewsItem>) {
    _newsList.value = items
  }
}
