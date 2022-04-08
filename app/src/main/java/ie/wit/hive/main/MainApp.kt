package ie.wit.hive.main

import android.app.Application
import com.cloudinary.android.MediaManager
import com.cloudinary.android.download.glide.GlideDownloadRequestBuilderFactory
import ie.wit.hive.models.HiveFireStore
import ie.wit.hive.models.HiveStore
import ie.wit.hive.models.UserFireStore
import ie.wit.hive.models.UserStore
import timber.log.Timber
import timber.log.Timber.i


class MainApp : Application() {

    lateinit var hives: HiveStore
    lateinit var users: UserStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        hives = HiveFireStore(applicationContext)
        users = UserFireStore(applicationContext)
        i("Hive started")
        MediaManager.init(this);
        MediaManager.get().setDownloadRequestBuilderFactory(GlideDownloadRequestBuilderFactory())
    }
}