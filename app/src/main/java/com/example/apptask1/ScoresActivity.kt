package com.example.apptask1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ScoresActivity : AppCompatActivity(), ScoresListFragment.Listener {

    private var mapFragment: ScoreMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scores)

        val listFrag = ScoresListFragment()
        mapFragment = ScoreMapFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.listContainer, listFrag)
            .replace(R.id.mapContainer, mapFragment!!)
            .commit()
    }

    override fun onScoreSelected(record: ScoreRecord) {
        mapFragment?.showRecord(record)
    }
}
