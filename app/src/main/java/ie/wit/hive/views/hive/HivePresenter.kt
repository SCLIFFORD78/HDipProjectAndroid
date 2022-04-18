package ie.wit.hive.views.hive

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
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
import ie.wit.hive.models.AlarmEvents
import ie.wit.hive.models.Comments
import ie.wit.hive.models.Location
import ie.wit.hive.models.HiveModel
import ie.wit.hive.showImagePicker
import ie.wit.hive.views.alarmlist.AlarmListView
import ie.wit.hive.views.ble.BleScanView
import ie.wit.hive.views.charts.LineChartView
import ie.wit.hive.views.hivelist.HiveListView
import ie.wit.hive.views.location.EditLocationView
import ie.wit.hive.views.popup.PopUpWindowView
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import timber.log.Timber.i
import java.text.SimpleDateFormat

class HivePresenter(private val view: HiveView) {
    private val locationRequest = createDefaultLocationRequest()
    var map: GoogleMap? = null
    lateinit var hive: HiveModel
    lateinit var comments:List<Comments>
    var app: MainApp = view.application as MainApp
    var locationManualyChanged = false;
    lateinit var alarms:List<AlarmEvents>

    //location service
    var locationService: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(view)
    private lateinit var imageIntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var mapIntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var editIntentLauncher: ActivityResultLauncher<Intent>
    var edit = false;
    private val location = Location(52.0634310, -9.6853542, 15f)
    var weather: Map<String, Float>

    init {

        doPermissionLauncher()
        registerImagePickerCallback()
        registerMapCallback()
        registerEditCallback()


        if (view.intent.hasExtra("hive_edit")) {
            edit = true
            var tagNo = view.intent.extras!!["hive_edit"] as Long
            hive =  runBlocking { getHive(tagNo) }
            comments = runBlocking { getHiveComments()  }
            //hive = view.intent.extras?.getParcelable("hive_edit")!!
            view.showHive(hive)
        }  else {
            hive = HiveModel()
            if (checkLocationPermissions(view)) {
                doSetCurrentLocation()
            }
            //hive.location.lat = location.lat
            //hive.location.lng = location.lng
        }
        runBlocking { alarms = app.hives.getHiveAlarms(hive.fbid) }
        runBlocking { comments = app.hives.getHiveComments(hive.fbid) }
        weather = runBlocking { getWeather() }


    }

    private suspend fun getHive(tagNum:Long):HiveModel{
        hive = app.hives.findByTag(tagNum)
        return  hive
    }

    suspend fun getWeather(): Map<String, Float>{
        var weather =
            ie.wit.hive.weather.getWeather(
                hive.location.lat,
                hive.location.lng
            )
        return weather
    }

    suspend fun getHiveComments() = app.hives.getHiveComments(hive.fbid)

    suspend fun getAlarms() = app.hives.getHiveAlarms(hive.fbid)
    //suspend fun findByType(type: String)= app.hives.findByType(type)

    fun formatComments() : ArrayList<String>{
        var response: ArrayList<String> = arrayListOf()
        comments.forEach {
            response .add(  SimpleDateFormat("dd/MM/YY HH:mm:ss").format(it.dateLogged.toLong()*1000)+"\n"+it.comment)
        }
        return response
    }

    suspend fun getActiveAlarms():List<AlarmEvents>{
        val resp: MutableList<AlarmEvents> = mutableListOf()
        val alarms = runBlocking { getAlarms() }
        for (alarm in alarms){
            if (!alarm.act)resp.add(resp.size,alarm)
        }
        this.alarms = alarms
        return if (resp.isNotEmpty()){
            resp
        } else emptyList()
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
    suspend fun doUpdateHive(hiveModel: HiveModel){
        app.hives.update(hive)
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
        launcherIntent.putExtra("hive_edit", hive.tag)
        editIntentLauncher.launch(launcherIntent)
    }

    fun openComments(){
        val launcherIntent = Intent(view, PopUpWindowView::class.java)
        launcherIntent.putExtra("hive_tag", hive.tag)
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
        launcherIntent.putExtra("hive_edit", hive.tag)
        editIntentLauncher.launch(launcherIntent)
    }

    fun doShowAlarms() {
        val launcherIntent = Intent(view, AlarmListView::class.java)
        launcherIntent.putExtra("hive_edit", hive.tag)
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
                            runBlocking {  uploadCloudinary(result.data!!.data.toString().toUri(),hive.fbid) }
                            //view.updateImage(hive.image)
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
    private fun uploadCloudinary(uri: Uri, fbid:String){
        val requestId: String = MediaManager.get().upload(uri).callback(object : UploadCallback {
            override fun onStart(requestId: String) {
                // your code here
            }


            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                // example code starts here
                val progress = bytes.toDouble() / totalBytes
                // post progress to app UI (e.g. progress bar, notification)
                // example code ends here
                Timber.i("cloud $progress")
            }

            override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                // your code here
                if (resultData != null) {
                    hive.image = resultData.get("url") .toString()
                    view.updateImage(hive.image)
                    runBlocking { doUpdateHive(hive)  }
                    Timber.i(resultData.get("url") .toString())
                }
            }

            override fun onError(requestId: String?, error: ErrorInfo?) {
                // your code here
                Timber.i("cloud $error")
            }

            override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                // your code here
            }
        }).unsigned("hive_tracker")
            .option("cloud_name", "digabwjfx")
            .option("tags", fbid)
            .dispatch()
    }
}