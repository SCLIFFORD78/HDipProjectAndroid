package ie.wit.hive.main

import android.app.Application
import com.cloudinary.android.MediaManager
import ie.wit.hive.models.*
import ie.wit.hive.room.HiveStoreRoom
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
    }
}