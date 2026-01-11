package com.example.apptask1

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class ScoreMapFragment : Fragment(R.layout.fragment_score_map) {

    private var googleMap: GoogleMap? = null
    private var marker: Marker? = null
    private var pendingRecord: ScoreRecord? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.mapHost, mapFragment)
            .commitNow()

        mapFragment.getMapAsync { map ->
            googleMap = map
            pendingRecord?.let {
                showRecord(it)
                pendingRecord = null
            }
        }
    }

    fun showRecord(record: ScoreRecord) {
        val map = googleMap
        if (map == null) {
            pendingRecord = record
            return
        }

        val pos = LatLng(record.lat, record.lng)
        marker?.remove()
        marker = map.addMarker(MarkerOptions().position(pos).title("Score: ${record.score}"))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f))
    }
}
