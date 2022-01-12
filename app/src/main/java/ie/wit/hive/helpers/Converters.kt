package ie.wit.hive.helpers

import android.net.Uri
import androidx.room.TypeConverter
import java.text.DateFormat
import java.util.*

class Converters {
    @TypeConverter
    fun fromUri(value: Uri): String {
        return value.toString()
    }

    @TypeConverter
    fun toUri(string: String): Uri {
        return Uri.parse(string)
    }

}