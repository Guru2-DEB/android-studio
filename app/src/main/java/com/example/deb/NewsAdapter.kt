package com.example.deb

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NewsAdapter(
  private val newsList: List<NewsItem>,
  private val onItemClick: (NewsItem) -> Unit // ⬅️ 클릭 콜백 추가
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

  inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val titleView: TextView = itemView.findViewById(R.id.newsTitle)
    val descriptionView: TextView = itemView.findViewById(R.id.newsDescription)
    val dateView: TextView = itemView.findViewById(R.id.newsDate)

    init {
      itemView.setOnClickListener {
        val position = adapterPosition
        if (position != RecyclerView.NO_POSITION) {
          onItemClick(newsList[position]) // ⬅️ 클릭 시 콜백 호출
        }
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_news, parent, false)
    return NewsViewHolder(view)
  }

  override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
    val news = newsList[position]
    holder.titleView.text = news.title
    holder.descriptionView.text = news.description
    holder.dateView.text = news.pubDate
  }

  override fun getItemCount(): Int = newsList.size
}
