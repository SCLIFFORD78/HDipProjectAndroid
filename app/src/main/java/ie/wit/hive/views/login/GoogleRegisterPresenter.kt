package ie.wit.hive.views.login

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.HiveFireStore
import ie.wit.hive.models.UserFireStore
import ie.wit.hive.models.UserModel
import ie.wit.hive.views.hivelist.HiveListView
import kotlinx.coroutines.runBlocking

class GoogleRegisterPresenter(val view: GoogleRegisterView) {
    private lateinit var registerIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var editIntentLauncher : ActivityResultLauncher<Intent>
    var app: MainApp = view.application as MainApp
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var fireStore: HiveFireStore? = null
    var userFireStore: UserFireStore? = null
    var user: UserModel = UserModel()

    init{
        registerLoginCallback()
        if (app.hives is HiveFireStore) {
            fireStore = app.hives as HiveFireStore
        }
        if (app.users is UserFireStore) {
            userFireStore = app.users as UserFireStore
        }
    }

     suspend fun create(user:UserModel) {
        app.users.create(user)

    }

    suspend fun doLogout(){
        FirebaseAuth.getInstance().signOut()
        app.hives.clear()
        app.users.clear()
        val launcherIntent = Intent(view, LoginView::class.java)
        editIntentLauncher.launch(launcherIntent)
    }

     suspend fun doSignUp(userName: String, firstName: String, secondName: String) {
        view.showProgress()
        user.firstName = firstName
        user.secondName = secondName
        user.userName = userName
        userFireStore!!.fetchUsers {
            runBlocking { create(user) }
            view?.hideProgress()
            val launcherIntent = Intent(view, HiveListView::class.java)
            registerIntentLauncher.launch(launcherIntent)

        }
        view.hideProgress()
    }

    private fun registerLoginCallback(){
        registerIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {  }
    }
}