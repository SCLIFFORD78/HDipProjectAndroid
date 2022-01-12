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

class RegisterPresenter(val view: RegisterView) {
    private lateinit var registerIntentLauncher : ActivityResultLauncher<Intent>
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

    fun backNAv(){
        val launcherIntent = Intent(view, LoginView::class.java)
        registerIntentLauncher.launch(launcherIntent)
    }

     suspend fun doSignUp(email: String, password: String, userName: String, firstName: String, secondName: String) {
        view.showProgress()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(view) { task ->
            if (task.isSuccessful) {
                user.firstName = firstName
                user.secondName = secondName
                user.userName = userName
                fireStore?.fetchHives {  }
                userFireStore!!.fetchUsers {
                    runBlocking { create(user) }
                    view?.hideProgress()
                    val launcherIntent = Intent(view, HiveListView::class.java)
                    registerIntentLauncher.launch(launcherIntent)
                }
            } else {
                view.showSnackBar("Login failed: ${task.exception?.message}")
            }
            view.hideProgress()
        }
    }
    private fun registerLoginCallback(){
        registerIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {  }
    }
}