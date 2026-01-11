package com.example.apptask1

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Date

class ScoresAdapter(
    private val items: List<ScoreRecord>,
    private val onClick: (ScoreRecord) -> Unit
) : RecyclerView.Adapter<ScoresAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val rank: TextView = v.findViewById(R.id.txtRank)
        val score: TextView = v.findViewById(R.id.txtScore)
        val time: TextView = v.findViewById(R.id.txtTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_score, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.rank.text = "${position + 1}."
        holder.score.text = "${r.score} m"
        holder.time.text = DateFormat.format("dd/MM HH:mm", Date(r.timeMillis))

        holder.itemView.setOnClickListener { onClick(r) }
    }
}
