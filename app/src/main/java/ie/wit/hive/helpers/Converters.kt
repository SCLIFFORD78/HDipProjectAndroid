package ie.wit.hive.helpers

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject


class Converters {
    @TypeConverter
    fun fromUri(value: Uri): String {
        return value.toString()
    }

    @TypeConverter
    fun toUri(string: String): Uri {
        return Uri.parse(string)
    }

    @TypeConverter
    fun fromJSONObject(value: JSONObject): String {
        return value.toString()
    }

    @TypeConverter
    fun toJSONObject(value: String): JsonObject? {
        return Gson().fromJson(value, JsonObject::class.java)
    }

}