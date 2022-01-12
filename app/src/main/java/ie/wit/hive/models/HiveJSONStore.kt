package ie.wit.hive.models

import android.content.Context
import android.net.Uri
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import ie.wit.hive.helpers.*
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*

const val JSON_FILE = "hives.json"
val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting()
    .registerTypeAdapter(Uri::class.java, UriParser())
    .create()
val listType: Type = object : TypeToken<ArrayList<HiveModel>>() {}.type

fun generateRandomId(): Long {
    return Random().nextLong()
}

class HiveJSONStore(private val context: Context) : HiveStore {

    var hives = mutableListOf<HiveModel>()

    init {
        if (exists(context, JSON_FILE)) {
            deserialize()
        }
    }

    override suspend fun findAll(): MutableList<HiveModel> {
        logAll()
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

    override suspend fun create(hive: HiveModel) {
        hive.id = generateRandomId()
        hive.tag = getTag()
        hives.add(hive)
        serialize()
    }


    override suspend fun update(hive: HiveModel) {
        val hivesList = findAll() as ArrayList<HiveModel>
        var foundHive: HiveModel? = hivesList.find { p -> p.id == hive.id }
        if (foundHive != null) {
            foundHive.tag = hive.tag
            foundHive.description = hive.description
            foundHive.image = hive.image
            foundHive.location = hive.location
            foundHive.type = hive.type
        }
        serialize()
    }

    override suspend fun delete(hive: HiveModel) {
        val foundHive: HiveModel? = hives.find { it.id == hive.id }
        hives.remove(foundHive)
        serialize()
    }
    override suspend fun findById(id:Long) : HiveModel? {
        val foundHive: HiveModel? = hives.find { it.id == id }
        return foundHive
    }

    override suspend fun findByTag(tag: Long): HiveModel? {
        return hives.find { p -> p.tag == tag }
    }

    private fun serialize() {
        val jsonString = gsonBuilder.toJson(hives, listType)
        write(context, JSON_FILE, jsonString)
    }

    private fun deserialize() {
        val jsonString = read(context, JSON_FILE)
        hives = gsonBuilder.fromJson(jsonString, listType)
    }

    private fun logAll() {
        hives.forEach { Timber.i("$it") }
    }
    override suspend fun clear(){
        hives.clear()
    }

    override suspend fun getTag(): Long {
        var num:Long = 1
        while (hives.find { p -> p.tag == num } != null){
            num++
        }
        return num
    }
}

class UriParser : JsonDeserializer<Uri>,JsonSerializer<Uri> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Uri {
        return Uri.parse(json?.asString)
    }

    override fun serialize(
        src: Uri?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }
}