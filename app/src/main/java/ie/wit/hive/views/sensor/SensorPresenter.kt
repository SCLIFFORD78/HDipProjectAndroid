package ie.wit.hive.views.sensor

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
import ie.wit.hive.views.location.EditLocationView
import timber.log.Timber
import timber.log.Timber.i

class SensorPresenter(private val view: SensorView) {
    private val locationRequest = createDefaultLocationRequest()

    var app: MainApp = view.application as MainApp
    var locationManualyChanged = false;
    //location service
    var locationService: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(view)
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var mapIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    var edit = false;
    private val location = Location(52.0634310, -9.6853542, 15f)

    init {



    }


    suspend fun doAddOrSave(type: String, description: String) {


        view.finish()

    }

    fun doCancel() {
        view.finish()

    }

    suspend fun doDelete() {
        view.finish()

    }

    fun doSelectImage() {
        showImagePicker(imageIntentLauncher)
    }














}