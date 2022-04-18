package ie.wit.hive.views.sensor

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.JsonObject
import ie.wit.hive.helpers.createDefaultLocationRequest
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.AlarmEvents
import ie.wit.hive.models.HiveModel
import ie.wit.hive.views.aboutus.AboutUsView
import ie.wit.hive.views.ble.BleScanView
import ie.wit.hive.views.login.LoginView
import kotlinx.coroutines.runBlocking

class SensorPresenter(private val view: SensorView) {
    private lateinit var device: BluetoothDevice
    var app: MainApp = view.application as MainApp
    lateinit var hive: HiveModel

    //location service
    var locationService: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(view)
    private lateinit var editIntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    init {
        registerEditCallback()
        if (view.intent.hasExtra("hive_edit")) {
            var tagNum = view.intent.extras!!["hive_edit"] as Long
            hive = runBlocking { getHive(tagNum) }
        }


    }

    fun getBLEdevice(): BluetoothDevice {
        device = view.intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
        return device
    }

    suspend fun getHives() = FirebaseAuth.getInstance().currentUser?.let {
        app.hives.findByOwner(it.uid).sortedBy { it.tag }
    }

    private suspend fun getHive(tagNum: Long): HiveModel {
        hive = app.hives.findByTag(tagNum)
        return hive
    }

    suspend fun doUpdateHive(values: ArrayList<JsonObject>) {
        //Gson().fromJson(value, JsonObject::class.java)
        var alarmEvents = arrayListOf<AlarmEvents>()
        var alm:AlarmEvents = AlarmEvents()
        var tempAlarm = hive.tempAlarm
        var alarmDetected = false
        var test = hive.recordedData
        for (i in 0 until values.size) {
            if (i == 0 && test.isBlank()) {
                hive.recordedData = values[i].toString()
                if (values[i].get("Temperature").asFloat < tempAlarm && !alarmDetected) {
                    alm.hiveid = hive.fbid
                    alm.alarmEvent = values[i].toString()
                    alm.dateActive = values[i].asJsonObject.get("timeStamp").toString()
                    alm.recordedValue = values[i].asJsonObject.get("Temperature").toString().toFloat()
                    alm.tempAlarm = hive.tempAlarm
                    alarmEvents.add(alm.copy())
                    alarmDetected = true
                }
            } else {
                hive.recordedData += "," + values[i].toString()
                if (values[i].get("Temperature").asFloat < tempAlarm && !alarmDetected) {
                    alm.hiveid = hive.fbid
                    alm.alarmEvent = values[i].toString()
                    alm.dateActive = values[i].asJsonObject.get("timeStamp").toString()
                    alm.recordedValue = values[i].asJsonObject.get("Temperature").toString().toFloat()
                    alm.tempAlarm = hive.tempAlarm
                    alarmEvents.add(alm.copy())
                    alarmDetected = true
                }
                if (values[i].get("Temperature").asFloat > tempAlarm && alarmDetected) {
                    alarmDetected = false
                }

            }

        }
        for (i in 0 until alarmEvents.size){
            app.hives.createAlarm(alarmEvents[i])
        }
        print(hive)
        runBlocking { app.hives.deleteRecordData(hive) }
        runBlocking { app.hives.update(hive) }
    }

    fun doShowBleScanner() {
        val launcherIntent = Intent(view, BleScanView::class.java)
        launcherIntent.putExtra("hive_edit", hive.tag)
        editIntentLauncher.launch(launcherIntent)
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


    private fun registerEditCallback() {
        editIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { }

    }

    fun doShowAboutUs() {
        val launcherIntent = Intent(view, AboutUsView::class.java)
        editIntentLauncher.launch(launcherIntent)
    }


    suspend fun doLogout() {
        FirebaseAuth.getInstance().signOut()
        app.hives.clear()
        app.users.clear()
        val launcherIntent = Intent(view, LoginView::class.java)
        editIntentLauncher.launch(launcherIntent)
    }


}