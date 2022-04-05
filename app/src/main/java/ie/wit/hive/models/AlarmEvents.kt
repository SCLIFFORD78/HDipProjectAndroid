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
data class AlarmEvents(
    var alarmEvent: String = "",
    var act: Boolean = false,
    var fbid: String = "",
    var hiveid: String = ""
) : Parcelable



