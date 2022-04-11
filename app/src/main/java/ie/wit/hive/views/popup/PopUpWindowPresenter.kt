package ie.wit.hive.views.hive

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ie.wit.hive.helpers.checkLocationPermissions
import ie.wit.hive.helpers.createDefaultLocationRequest
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.AlarmEvents
import ie.wit.hive.models.Comments
import ie.wit.hive.models.Location
import ie.wit.hive.models.HiveModel
import ie.wit.hive.showImagePicker
import ie.wit.hive.views.alarmlist.AlarmListView
import ie.wit.hive.views.ble.BleScanView
import ie.wit.hive.views.charts.LineChartView
import ie.wit.hive.views.hivelist.HiveListView
import ie.wit.hive.views.location.EditLocationView
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import timber.log.Timber.i
import java.text.SimpleDateFormat

class PopUpWindowPresenter(private val view: PopUpWindowView) {
    lateinit var hive: HiveModel
    lateinit var comments:List<Comments>
    var app: MainApp = view.application as MainApp
    private lateinit var editIntentLauncher: ActivityResultLauncher<Intent>


    lateinit var alarms:List<AlarmEvents>


    init {



        if (view.intent.hasExtra("hive_tag")) {
            var tagNo = view.intent.extras!!["hive_tag"] as Long
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


    suspend fun getHiveComments() = app.hives.getHiveComments(hive.fbid)

    suspend fun addComment(newComment: String) {
        var comment: Comments = Comments()
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