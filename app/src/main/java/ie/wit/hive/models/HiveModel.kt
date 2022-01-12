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
data class HiveModel(@PrimaryKey(autoGenerate = true) var id: Long = 0,
                     var fbId: String = "",
                     var user: String = "",
                     var tag: Long = 0,
                     var description: String = "",
                     var image: String = "",
                     var type: String = "",
                     var dateRegistered: String = Date().toString(),
                     @Embedded var location : Location = Location()): Parcelable


@Parcelize
data class Location(var lat: Double = 0.0,
                    var lng: Double = 0.0,
                    var zoom: Float = 0f) : Parcelable

