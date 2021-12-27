package com.example.judyjjimjilbang

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.judyjjimjilbang.model.RankData

class RankAdapter(var itemList : ArrayList<RankData>, val userTime: String) : RecyclerView.Adapter<RankAdapter.ViewHolder>() {
    class ViewHolder(itemView: View, val userTime: String) : RecyclerView.ViewHolder(itemView){
        val order = itemView.findViewById<TextView>(R.id.rank)
        val score = itemView.findViewById<TextView>(R.id.rank_score)
        val date = itemView.findViewById<TextView>(R.id.rank_date)
        val parent = itemView.findViewById<RelativeLayout>(R.id.parent)

        fun bind(item : RankData, index: Int){
            val title = "${item.score} 점"
            order.text = (index + 1).toString()
            score.text = title
            date.text = item.date
            // 방금 한 게임일 경우 색을 다르게 로 표시해준다.
            if(userTime == item.date) userColorChange("#F4B183")
        }

        private fun userColorChange(color: String){
            parent.setBackgroundColor(Color.parseColor(color))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        return ViewHolder(view, userTime)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position], position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}