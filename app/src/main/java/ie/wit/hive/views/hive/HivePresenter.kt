package ie.wit.hive.views.hive

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
import ie.wit.hive.helpers.checkLocationPermissions
import ie.wit.hive.helpers.createDefaultLocationRequest
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.Location
import ie.wit.hive.models.HiveModel
import ie.wit.hive.showImagePicker
import ie.wit.hive.views.ble.BleScanView
import ie.wit.hive.views.charts.ChartView
import ie.wit.hive.views.charts.LineChartView
import ie.wit.hive.views.hivelist.HiveListView
import ie.wit.hive.views.location.EditLocationView
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import timber.log.Timber.i

class HivePresenter(private val view: HiveView) {
    private val locationRequest = createDefaultLocationRequest()
    var map: GoogleMap? = null
    lateinit var hive: HiveModel
    var app: MainApp = view.application as MainApp
    var locationManualyChanged = false;

    //location service
    var locationService: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(view)
    private lateinit var imageIntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var mapIntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var editIntentLauncher: ActivityResultLauncher<Intent>
    var edit = false;
    private val location = Location(52.0634310, -9.6853542, 15f)

    init {

        doPermissionLauncher()
        registerImagePickerCallback()
        registerMapCallback()
        registerEditCallback()

        if (view.intent.hasExtra("hive_edit")) {
            edit = true
            var tagNo = view.intent.extras!!["hive_edit"]
            hive = runBlocking { app.hives.findByTag(tagNo as Long) }
            //hive = view.intent.extras?.getParcelable("hive_edit")!!
            view.showHive(hive)
        } else if (view.intent.hasExtra("hive")) {
            hive = view.intent.extras!!["hive"] as HiveModel
        } else {

            if (checkLocationPermissions(view)) {
                doSetCurrentLocation()
            }
            //hive.location.lat = location.lat
            //hive.location.lng = location.lng
        }

    }


    suspend fun doAddOrSave(type: String, description: String) {
        hive.type = type
        hive.description = description
        if (edit) {
            app.hives.update(hive)
        } else {
            app.hives.create(hive)
        }

        view.finish()

    }

    fun doCancel() {
        val launcherIntent = Intent(view, HiveListView::class.java)
        editIntentLauncher.launch(launcherIntent)

    }

    suspend fun doDelete() {
        app.hives.delete(hive)
        view.finish()

    }

    fun doSelectImage() {
        showImagePicker(imageIntentLauncher)
    }

    fun chartNAv() {
        val launcherIntent = Intent(view, LineChartView::class.java)
        launcherIntent.putExtra("hive", hive)
        editIntentLauncher.launch(launcherIntent)
    }

    fun doSetLocation() {
        locationManualyChanged = true;

        if (hive.location.zoom != 0f) {

            location.lat = hive.location.lat
            location.lng = hive.location.lng
            location.zoom = hive.location.zoom
            locationUpdate(hive.location.lat, hive.location.lng)
        }
        val launcherIntent = Intent(view, EditLocationView::class.java)
            .putExtra("location", location)
        mapIntentLauncher.launch(launcherIntent)
    }

    @SuppressLint("MissingPermission")
    fun doSetCurrentLocation() {

        locationService.lastLocation.addOnSuccessListener {
            locationUpdate(it.latitude, it.longitude)
            locationUpdate(location.lat, location.lng)
        }
    }

    @SuppressLint("MissingPermission")
    fun doRestartLocationUpdates() {
        var locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult != null && locationResult.locations != null) {
                    val l = locationResult.locations.last()
                    if (!locationManualyChanged) {
                        locationUpdate(l.latitude, l.longitude)
                    }
                }
            }
        }
        if (!edit) {
            locationService.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    fun doConfigureMap(m: GoogleMap) {
        map = m
        locationUpdate(hive.location.lat, hive.location.lng)
    }

    fun locationUpdate(lat: Double, lng: Double) {
        hive.location.lat = lat
        hive.location.lng = lng
        map?.clear()
        map?.uiSettings?.setZoomControlsEnabled(true)
        val options = MarkerOptions().title(hive.tag.toString())
            .position(LatLng(hive.location.lat, hive.location.lng))
        map?.addMarker(options)
        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    hive.location.lat,
                    hive.location.lng
                ), hive.location.zoom
            )
        )
        view.showHive(hive)
    }

    fun cacheHive(tag: Long, description: String) {
        hive.tag = tag;
        hive.description = description
    }

    fun doShowBleScanner() {
        val launcherIntent = Intent(view, BleScanView::class.java)
        launcherIntent.putExtra("hive", hive)
        editIntentLauncher.launch(launcherIntent)
    }

    private fun registerImagePickerCallback() {

        imageIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${result.data!!.data}")
                            hive.image = result.data!!.data!!.toString()
                            view.updateImage(hive.image)
                        }
                    }
                    AppCompatActivity.RESULT_CANCELED -> {}
                    else -> {}
                }

            }
    }

    private fun registerMapCallback() {
        mapIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Location ${result.data.toString()}")
                            val location =
                                result.data!!.extras?.getParcelable<Location>("location")!!
                            Timber.i("Location == $location")
                            hive.location = location
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> {}
                    else -> {}
                }

            }
    }

    private fun doPermissionLauncher() {
        i("permission check called")
        requestPermissionLauncher =
            view.registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted: Boolean ->
                if (isGranted) {
                    doSetCurrentLocation()
                } else {
                    locationUpdate(location.lat, location.lng)
                }
            }
    }

    private fun registerEditCallback() {
        editIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { }

    }
}