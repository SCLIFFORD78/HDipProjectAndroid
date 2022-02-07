package ie.wit.hive.views.sensor

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
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
import com.google.gson.Gson
import com.google.gson.JsonObject
import ie.wit.hive.helpers.checkLocationPermissions
import ie.wit.hive.helpers.createDefaultLocationRequest
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.Location
import ie.wit.hive.models.HiveModel
import ie.wit.hive.showImagePicker
import ie.wit.hive.views.location.EditLocationView
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import timber.log.Timber
import timber.log.Timber.i
import java.util.ArrayList

class SensorPresenter(private val view: SensorView) {
    private val locationRequest = createDefaultLocationRequest()
    private lateinit var device : BluetoothDevice
    var app: MainApp = view.application as MainApp
    var hive = HiveModel()
    //location service
    var locationService: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(view)
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var mapIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    init {



    }

    fun getBLEdevice():BluetoothDevice{
        device =  view.intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
        return device
    }

    suspend fun getHives() = FirebaseAuth.getInstance().currentUser?.let { app.hives.findByOwner(it.uid).sortedBy { it.tag } }

    suspend fun getHive():HiveModel{
        hive = app.hives.findByTag(1)
        return  hive
    }

    suspend fun doUpdateHive(values: ArrayList<JsonObject>){
        //Gson().fromJson(value, JsonObject::class.java)
        var test = hive.recordedData
        for (i in 0 until values.size) {

            test = test+values.get(i).toString()
        }
        hive.recordedData = test
        print(hive)
        app.hives.update(hive)
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