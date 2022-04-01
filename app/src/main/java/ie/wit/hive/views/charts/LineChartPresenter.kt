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
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var editIntentLauncher : ActivityResultLauncher<Intent>
    lateinit var hive: HiveModel

    init {
        registerEditCallback()
        registerRefreshCallback()
        if (view.intent.hasExtra("hive")) {
            hive= view.intent.extras!!["hive"] as HiveModel
        }
    }

    suspend fun getHives() = FirebaseAuth.getInstance().currentUser?.let { app.hives.findByOwner(it.uid).sortedBy { it.tag } }
    suspend fun getUsers() = app.users.findAll()
    //suspend fun findByType(type: String)= app.hives.findByType(type)
    suspend fun findByType(type: String): List<HiveModel> {
        val resp: MutableList<HiveModel> = mutableListOf()
        val hives = getHives()
        if (hives != null) {
            for (hive in hives) if(hive.type == type) {
                resp.add(0,hive)
            }
        }
        return if (resp.isNotEmpty()){
            resp
        } else emptyList()
    }

    suspend fun getHiveByTag(tag:Long):List<HiveModel>{
        var list : ArrayList<HiveModel> = arrayListOf()
        var hives = getHives()
        val foundhive = hives?.find { p -> p.tag == tag }
        if (foundhive != null) {
            list.add(0,foundhive)
        }

        return list
    }



    fun backNAv(){
        val launcherIntent = Intent(view, HiveView::class.java)
        launcherIntent.putExtra("hive", hive)

        refreshIntentLauncher.launch(launcherIntent)
    }

    suspend fun doLogout(){
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
                GlobalScope.launch(Dispatchers.Main){
                    //getHives()
                }
                GlobalScope.launch(Dispatchers.Main){
                    getUsers()
                }
            }
    }
    private fun registerEditCallback() {
        editIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {  }

    }
}