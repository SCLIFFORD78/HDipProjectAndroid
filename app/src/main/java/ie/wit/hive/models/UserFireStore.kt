package ie.wit.hive.models

import android.content.Context
import android.graphics.Bitmap
import android.service.autofill.Dataset
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import ie.wit.hive.readImageFromPath
import timber.log.Timber.i
import java.io.ByteArrayOutputStream
import java.io.File

class UserFireStore(val context: Context) : UserStore {
    val users = ArrayList<UserModel>()
    val userCheck : Array<Iterable<DataSnapshot>?> = arrayOfNulls(1)
    lateinit var userId: String
    lateinit var db: DatabaseReference
    lateinit var st: StorageReference



    override suspend fun findAll(): List<UserModel> {
        return users
    }


    override suspend fun create(user: UserModel) {
        val key = db.child("users").child(userId).push().key
        key?.let {
            user.fbId = userId
            users.add(user)
            db.child("users").child(userId).setValue(user)
            updateImage(user)
        }
    }



    override suspend fun delete(user: UserModel) {
        deleteImage(user)
        db.child("users").child(userId).removeValue()
        users.remove(user)

    }

    override suspend fun clear() {
        users.clear()
    }



    fun fetchUsers(usersReady: () -> Unit) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot!!.children.mapNotNullTo(users) {
                    var test = users
                    it.getValue<UserModel>(
                        UserModel::class.java
                    )
                }
                usersReady()
            }
        }
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        st = FirebaseStorage.getInstance().reference
        db = FirebaseDatabase.getInstance("https://hivetrackerapp3-default-rtdb.firebaseio.com/").reference
        users.clear()
        db.child("users")
            .addListenerForSingleValueEvent(valueEventListener)
    }
    fun deleteImage(user: UserModel){
        if(user.image != "") {
            val fileName = File(user.image)
            val imageName = fileName.getName()

            var imageRef = st.child(userId + '/' + imageName)
            if (imageRef.downloadUrl != null){
                st.child(userId + '/' + imageName).delete()
            }
        }
    }
    fun updateImage(user: UserModel){
        if(user.image != ""){
            val fileName = File(user.image)
            val imageName = fileName.getName()

            var imageRef = st.child(userId + '/' + imageName)
            val baos = ByteArrayOutputStream()
            val bitmap = readImageFromPath(context, user.image)

            bitmap?.let {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)

                val data = baos.toByteArray()
                val uploadTask = imageRef.putBytes(data)

                uploadTask.addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        user.image = it.toString()
                        db.child("users").child(userId).setValue(user)
                    }
                }.addOnFailureListener{
                    var errorMessage = it.message
                    i("Failure: $errorMessage")
                }
            }

        }
    }
}