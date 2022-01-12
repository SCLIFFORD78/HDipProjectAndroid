package ie.wit.hive.views.map

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import ie.wit.hive.main.MainApp

class HiveMapPresenter(val view: HiveMapView) {
    var app: MainApp

    init {
        app = view.application as MainApp
    }

    suspend fun doPopulateMap(map: GoogleMap) {
        map.uiSettings.setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(view)
        FirebaseAuth.getInstance().currentUser?.let {
            app.hives.findByOwner(it.uid).forEach {
                val loc = LatLng(it.location.lat, it.location.lng)
                val options = MarkerOptions().title(it.tag.toString()).position(loc)
                map.addMarker(options)?.tag = it.tag
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, it.location.zoom))
            }
        }
    }

    suspend fun doMarkerSelected(marker: Marker) {
        val tag = marker.tag as Long
        val hive = app.hives.findByTag(tag)
        if (hive != null) view.showHive(hive)
    }
}