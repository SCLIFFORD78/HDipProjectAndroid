package ie.wit.hive.views.ble

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
import ie.wit.hive.views.map.HiveMapView

class BleScanPresenter(private val view: BleScanView) {

    var app: MainApp = view.application as MainApp
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var editIntentLauncher : ActivityResultLauncher<Intent>

    init {

    }

    fun doAddHive() {
        val launcherIntent = Intent(view, HiveView::class.java)
        editIntentLauncher.launch(launcherIntent)
    }

    fun doEditHive(hive: HiveModel) {
        val launcherIntent = Intent(view, HiveView::class.java)
        launcherIntent.putExtra("hive_edit", hive)
        editIntentLauncher.launch(launcherIntent)
    }

    fun doShowHivesMap() {
        val launcherIntent = Intent(view, HiveMapView::class.java)
        editIntentLauncher.launch(launcherIntent)
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

}