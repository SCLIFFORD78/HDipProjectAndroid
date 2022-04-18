package ie.wit.hive.views.popup

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.AlarmEvents
import ie.wit.hive.models.Comments
import ie.wit.hive.models.HiveModel
import kotlinx.coroutines.runBlocking

class PopUpWindowPresenter(private val view: PopUpWindowView) {
    lateinit var hive: HiveModel
    lateinit var comments:List<Comments>
    var app: MainApp = view.application as MainApp
    private lateinit var editIntentLauncher: ActivityResultLauncher<Intent>


    lateinit var alarms:List<AlarmEvents>


    init {



        if (view.intent.hasExtra("hive_tag")) {
            val tagNo = view.intent.extras!!["hive_tag"] as Long
            hive =  runBlocking { getHive(tagNo) }
            comments = runBlocking { getHiveComments()  }
            //hive = view.intent.extras?.getParcelable("hive_edit")!!
        }
        registerEditCallback()




    }

    private suspend fun getHive(tagNum:Long):HiveModel{
        hive = app.hives.findByTag(tagNum)
        return  hive
    }


    private suspend fun getHiveComments() = app.hives.getHiveComments(hive.fbid)

    suspend fun addComment(newComment: String) {
        val comment = Comments()
        comment.comment = newComment
        comment.hiveid = hive.fbid
        comment.userid = hive.user
        app.hives.createComment(comment)
        val launcherIntent = Intent(view, PopUpWindowView::class.java)
        launcherIntent.putExtra("hive_tag", hive.tag)
        view.finish()
        editIntentLauncher.launch(launcherIntent)
        getHiveComments()

    }

    private fun registerEditCallback() {
        editIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { }

    }

}