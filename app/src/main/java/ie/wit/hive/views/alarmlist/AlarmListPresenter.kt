package ie.wit.hive.views.alarmlist

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.AlarmEvents
import ie.wit.hive.models.HiveModel
import ie.wit.hive.views.aboutus.AboutUsView
import ie.wit.hive.views.login.LoginView
import ie.wit.hive.views.hive.HiveView
import ie.wit.hive.views.map.HiveMapView
import kotlinx.coroutines.runBlocking

class AlarmListPresenter(private val view: AlarmListView) {

    var app: MainApp = view.application as MainApp
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var editIntentLauncher : ActivityResultLauncher<Intent>
    lateinit var hive : HiveModel

    init {
        registerEditCallback()
        registerRefreshCallback()
        if (view.intent.hasExtra("hive_edit")) {
            var tagNum = view.intent.extras!!["hive_edit"] as Long
            hive = runBlocking { getHive(tagNum) }
        }
    }

    private suspend fun getHive(tagNum:Long):HiveModel{
        hive = app.hives.findByTag(tagNum)
        return  hive
    }

    suspend fun getAlarms() = app.hives.getHiveAlarms(hive.fbid)
    //suspend fun findByType(type: String)= app.hives.findByType(type)

    suspend fun ackAlarm(alarm:AlarmEvents) = app.hives.ackAlarm(alarm)


    fun backNAv(){
        view.showProgress()
        val launcherIntent = Intent(view, HiveView::class.java)
        launcherIntent.putExtra("hive_edit", hive.tag)
        refreshIntentLauncher.launch(launcherIntent)
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

    private fun registerRefreshCallback() {
        refreshIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {
                GlobalScope.launch(Dispatchers.Main){
                    //getHives()
                }
                GlobalScope.launch(Dispatchers.Main){
                    getAlarms()
                }
            }
    }
    private fun registerEditCallback() {
        editIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {  }

    }
}