package ie.wit.hive.models

import android.net.Uri
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity
data class UserModel(@PrimaryKey(autoGenerate = true)
                     var fbId: String = "",
                     var firstName: String = "",
                     var secondName: String = "",
                     var image: String = "",
                     var userName : String = "",
                     var dateJoined: String = Date().toString()): Parcelable