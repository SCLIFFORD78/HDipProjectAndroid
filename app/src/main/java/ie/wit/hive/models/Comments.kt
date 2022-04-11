package ie.wit.hive.models

import android.net.Uri
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject
import java.util.*
import kotlin.math.floor

@Parcelize
@Entity
data class Comments(
    var fbid: String = "",
    var comment: String = "",
    var hiveid: String = "",
    var userid: String = "",
    var dateLogged: String = floor((System.currentTimeMillis() / 1000).toDouble()).toLong().toString()
) : Parcelable



