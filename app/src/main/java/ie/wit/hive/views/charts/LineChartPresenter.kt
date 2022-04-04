package ie.wit.hive.views.charts

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import ie.wit.hive.helpers.checkLocationPermissions
import ie.wit.hive.helpers.createDefaultLocationRequest
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.Location
import ie.wit.hive.models.HiveModel
import ie.wit.hive.showImagePicker
import ie.wit.hive.views.aboutus.AboutUsView
import ie.wit.hive.views.ble.BleScanView
import ie.wit.hive.views.hive.HiveView
import ie.wit.hive.views.hivelist.HiveListView
import ie.wit.hive.views.location.EditLocationView
import ie.wit.hive.views.login.LoginView
import ie.wit.hive.views.map.HiveMapView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import timber.log.Timber.i

class LineChartPresenter(private val view: LineChartView) {
    var app: MainApp = view.application as MainApp
    private lateinit var refreshIntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var editIntentLauncher: ActivityResultLauncher<Intent>
    lateinit var hive: HiveModel

    init {
        registerEditCallback()
        registerRefreshCallback()
        if (view.intent.hasExtra("hive_edit")) {
            var tagNum = view.intent.extras!!["hive_edit"] as Long
            hive = runBlocking { getHive(tagNum) }
        }
    }

    private suspend fun getHive(tagNum: Long): HiveModel {
        hive = app.hives.findByTag(tagNum)
        return hive
    }


    suspend fun backNAv(tempAlarm: Float) {
        val launcherIntent = Intent(view, HiveView::class.java)
        launcherIntent.putExtra("hive_edit", hive.tag)
        if (tempAlarm != hive.tempAlarm) hive.tempAlarm = tempAlarm
        app.hives.update(hive)
        refreshIntentLauncher.launch(launcherIntent)
    }

    suspend fun doLogout() {
        FirebaseAuth.getInstance().signOut()
        app.hives.clear()
        app.users.clear()
        val launcherIntent = Intent(view, LoginView::class.java)
        editIntentLauncher.launch(launcherIntent)
    }

    private fun registerRefreshCallback() {
        refreshIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {
                GlobalScope.launch(Dispatchers.Main) {
                    //getHives()
                }
                GlobalScope.launch(Dispatchers.Main) {
                    //getUsers()
                }
            }
    }

    private fun registerEditCallback() {
        editIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { }

    }
}