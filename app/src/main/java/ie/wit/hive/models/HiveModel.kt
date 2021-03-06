package ie.wit.hive.models

import android.net.Uri
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

@Parcelize
@Entity
data class HiveModel(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var fbid: String = "",
    var user: String = "",
    var tag: Long = 0,
    var description: String = "",
    var recordedData: String = "",
    var image: String = "",
    var type: String = "",
    var dateRegistered: String = floor((System.currentTimeMillis() / 1000).toDouble()).toLong()
        .toString(),
    var sensorNumber: String = "",
    var tempAlarm: Float = 18f,
    @Embedded var location: Location = Location(),
) : Parcelable


@Parcelize
data class Location(
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var zoom: Float = 0f
) : Parcelable



