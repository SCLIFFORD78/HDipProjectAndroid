package ie.wit.hive.models

import android.content.Context
import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import ie.wit.hive.readImageFromPath
import timber.log.Timber.i
import java.io.ByteArrayOutputStream
import java.io.File

class HiveFireStore(val context: Context) : HiveStore {
    val hives = ArrayList<HiveModel>()
    lateinit var userId: String
    lateinit var db: DatabaseReference
    lateinit var st: StorageReference
    override suspend fun findAll(): List<HiveModel> {
        return hives
    }

    override suspend fun findByOwner(userID: String): List<HiveModel> {
        val resp: MutableList<HiveModel> = mutableListOf()
        for (hive in hives) if(hive.user == userID) {
            resp.add(0,hive)
        }
        return if (resp.isNotEmpty()){
            resp
        } else emptyList()
    }

    override suspend fun findByType(type: String): List<HiveModel> {
        val resp: MutableList<HiveModel> = mutableListOf()
        for (hive in hives) if(hive.type == type) {
            resp.add(0,hive)
        }
        return if (resp.isNotEmpty()){
            resp
        } else emptyList()
    }

    override suspend fun findById(id: Long): HiveModel? {
        val foundHive: HiveModel? = hives.find { p -> p.id == id }
        return foundHive
    }

    override suspend fun findByTag(tag: Long): HiveModel? {
        return hives.find { p -> p.tag == tag }
    }

    override suspend fun create(hive: HiveModel) {
        val key = db.child("hives").push().key
        key?.let {
            hive.fbId = key
            hive.tag = getTag()
            hive.user = userId
            hives.add(hive)
            db.child("hives").child(key).setValue(hive)
            updateImage(hive)
        }
    }

    override suspend fun update(hive: HiveModel) {
        var foundHive: HiveModel? = hives.find { p -> p.fbId == hive.fbId }
        if (foundHive != null) {
            foundHive.tag = hive.tag
            foundHive.description = hive.description
            foundHive.image = hive.image
            foundHive.location = hive.location
            foundHive.type = hive.type
        }

        db.child("hives").child(hive.fbId).setValue(hive)
        if(hive.image.length > 0){
            updateImage(hive)
        }
    }

    override suspend fun delete(hive: HiveModel) {
        deleteImage(hive)
        db.child("hives").child(hive.fbId).removeValue()
        hives.remove(hive)

    }

    override suspend fun clear() {
        hives.clear()
    }

    override suspend fun getTag(): Long {
        var num:Long = 1
        while (hives.find { p -> p.tag == num } != null){
            num++
        }
        return num
    }

    fun fetchHives(hivesReady: () -> Unit) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot!!.children.mapNotNullTo(hives) {
                    it.getValue<HiveModel>(
                        HiveModel::class.java
                    )
                }
                hivesReady()
            }
        }
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        st = FirebaseStorage.getInstance().reference
        db = FirebaseDatabase.getInstance("https://hivetrackerapp3-default-rtdb.firebaseio.com/").reference
        hives.clear()
        db.child("hives")
            .addListenerForSingleValueEvent(valueEventListener)
    }
    fun deleteImage(hive: HiveModel){
        if(hive.image != "") {
            val fileName = File(hive.image)
            val imageName = fileName.getName()

            var imageRef = st.child(userId + '/' + imageName)
            if (imageRef.downloadUrl != null){
                st.child(userId + '/' + imageName).delete()
            }
        }
    }
    fun updateImage(hive: HiveModel){
        if(hive.image != ""){
            val fileName = File(hive.image)
            val imageName = fileName.getName()

            var imageRef = st.child(userId + '/' + imageName)
            val baos = ByteArrayOutputStream()
            val bitmap = readImageFromPath(context, hive.image)

            bitmap?.let {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)

                val data = baos.toByteArray()
                val uploadTask = imageRef.putBytes(data)

                uploadTask.addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        hive.image = it.toString()
                        db.child("hives").child(hive.fbId).setValue(hive)
                    }
                }.addOnFailureListener{
                    var errorMessage = it.message
                    i("Failure: $errorMessage")
                }
            }

        }
    }
}