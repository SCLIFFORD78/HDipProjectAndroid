package ie.wit.hive.room

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import ie.wit.hive.models.HiveModel
import ie.wit.hive.models.HiveStore

class HiveStoreRoom(val context: Context) : HiveStore {

    var dao: HiveDao

    init {
        val database = Room.databaseBuilder(context, Database::class.java, "room_sample.db")
            .fallbackToDestructiveMigration()
            .build()
        dao = database.hiveDao()
    }

    override suspend fun findAll(): List<HiveModel> {
        return dao.findAll()
    }

    override suspend fun findByOwner(userID: String): List<HiveModel> {
        return dao.findAll()
    }

    override suspend fun findByType(type: String): List<HiveModel> {
        return dao.findByType(type)
    }

    override suspend fun findById(id: Long): HiveModel? {
        return dao.findById(id)
    }

    override suspend fun findByTag(tag: Long): HiveModel? {
        return dao.findByTag(tag)
    }

    override suspend fun create(hive: HiveModel) {
        dao.create(hive)
    }

    override suspend fun update(hive: HiveModel) {
        dao.update(hive)
    }

    override suspend fun delete(hive: HiveModel) {
        dao.deleteHive(hive)
    }
    override suspend fun clear() {
    }

    override suspend fun getTag(): Long {
        var num:Long = 1
        var hives = FirebaseAuth.getInstance().currentUser?.let { this.findByOwner(it.uid) }
        if (hives != null) {
            while (hives.find { p -> p.tag == num } != null){
                num++
            }
        }
        return num
    }
}