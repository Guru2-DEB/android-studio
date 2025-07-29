package com.example.deb

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deb.data.ConversationEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationAdapter(
    private val items: List<ConversationEntity>,
    private val onClick: (Long)->Unit
) : RecyclerView.Adapter<ConversationAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val dateTv: TextView   = v.findViewById(R.id.tvDate)
        val titleTv: TextView  = v.findViewById(R.id.tvTitle)
        val snippetTv: TextView= v.findViewById(R.id.tvSnippet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_session, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val c = items[pos]
        holder.dateTv.text    = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            .format(Date(c.date))
        holder.titleTv.text   = c.newsTitle
        holder.snippetTv.text = c.newsSnippet
        holder.itemView.setOnClickListener { onClick(c.conversationId) }
    }

    override fun getItemCount() = items.size
}
