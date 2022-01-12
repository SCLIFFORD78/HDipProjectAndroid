package ie.wit.hive.views.aboutus

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.HiveFireStore
import ie.wit.hive.models.UserFireStore
import ie.wit.hive.models.UserModel
import ie.wit.hive.views.hivelist.HiveListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AboutUsPresenter(val view: AboutUsView) {
    private lateinit var registerIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var editIntentLauncher : ActivityResultLauncher<Intent>
    var app: MainApp = view.application as MainApp

    init {
        registerEditCallback()
        registerRefreshCallback()
    }

    suspend fun getHives() = FirebaseAuth.getInstance().currentUser?.let { app.hives.findByOwner(it.uid) }



    fun backNAv(){
        val launcherIntent = Intent(view, HiveListView::class.java)
        refreshIntentLauncher.launch(launcherIntent)
    }

    private fun registerRefreshCallback() {
        refreshIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {
                GlobalScope.launch(Dispatchers.Main){
                    getHives()
                }
            }
    }
    private fun registerEditCallback() {
        editIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {  }

    }

}