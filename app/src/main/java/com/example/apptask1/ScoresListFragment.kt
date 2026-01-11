package com.example.apptask1

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScoresListFragment : Fragment(R.layout.fragment_scores_list) {

    interface Listener {
        fun onScoreSelected(record: ScoreRecord)
    }

    private var listener: Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? Listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rvScores)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val repo = ScoresRepository(requireContext())
        val items = repo.loadTop10()
        if (items.isNotEmpty()) {
            listener?.onScoreSelected(items[0])
        }
        val adapter = ScoresAdapter(items) { record ->
            listener?.onScoreSelected(record)
        }
        rv.adapter = adapter
    }
}
