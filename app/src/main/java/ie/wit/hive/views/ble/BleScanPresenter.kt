package ie.wit.hive.views.ble

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.HiveModel
import ie.wit.hive.views.aboutus.AboutUsView
import ie.wit.hive.views.login.LoginView
import ie.wit.hive.views.hive.HiveView
import ie.wit.hive.views.hivelist.HiveListView
import ie.wit.hive.views.map.HiveMapView
import ie.wit.hive.views.sensor.SensorView
import kotlinx.coroutines.runBlocking

class BleScanPresenter(private val view: BleScanView) {

    var app: MainApp = view.application as MainApp
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var editIntentLauncher : ActivityResultLauncher<Intent>
    lateinit var hive : HiveModel

    init {
        registerRefreshCallback()
        registerEditCallback()
        if (view.intent.hasExtra("hive")) {
            hive = view.intent.extras!!["hive"] as HiveModel
        }
    }

    fun doAddHive() {
        val launcherIntent = Intent(view, HiveView::class.java)
        editIntentLauncher.launch(launcherIntent)
    }

    fun doSensorView(device: BluetoothDevice) {
        val launcherIntent = Intent(view, SensorView::class.java)
        launcherIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
        launcherIntent.putExtra("hive", hive)
        editIntentLauncher.launch(launcherIntent)
    }

    fun backNAv(){
        view.showProgress()
        val launcherIntent = Intent(view, HiveView::class.java)
        launcherIntent.putExtra("hive", hive)
        refreshIntentLauncher.launch(launcherIntent)
    }


    fun doShowAboutUs() {
        val launcherIntent = Intent(view, AboutUsView::class.java)
        editIntentLauncher.launch(launcherIntent)
    }

    suspend fun doLogout(){
        FirebaseAuth.getInstance().signOut()
        app.hives.clear()
        app.users.clear()
        val launcherIntent = Intent(view, LoginView::class.java)
        editIntentLauncher.launch(launcherIntent)
    }

    private fun registerEditCallback() {
        editIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {  }

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