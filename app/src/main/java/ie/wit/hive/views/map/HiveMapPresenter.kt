package ie.wit.hive.views.map

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import ie.wit.hive.main.MainApp
import ie.wit.hive.views.hivelist.HiveListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HiveMapPresenter(val view: HiveMapView) {
    var app: MainApp
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>

    init {
        app = view.application as MainApp
        registerRefreshCallback()
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

    fun backNAv(){
        val launcherIntent = Intent(view, HiveListView::class.java)
        refreshIntentLauncher.launch(launcherIntent)
    }

    private fun registerRefreshCallback() {
        refreshIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {
                GlobalScope.launch(Dispatchers.Main){
                    //getHives()
                }
            }
    }
}