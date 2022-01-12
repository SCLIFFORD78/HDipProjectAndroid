package ie.wit.hive.models

import timber.log.Timber.i

var lastId = 0L

internal fun getId(): Long {
    return lastId++
}

class HiveMemStore : HiveStore {

    val hives = ArrayList<HiveModel>()

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

    override suspend fun create(hive: HiveModel) {
        hive.id = getId()
        hive.tag = getTag()
        hives.add(hive)
        logAll()
    }

    override suspend fun update(hive: HiveModel) {
        val foundHive: HiveModel? = hives.find { p -> p.id == hive.id }
        if (foundHive != null) {
            foundHive.tag = hive.tag
            foundHive.description = hive.description
            foundHive.image = hive.image
            foundHive.location = hive.location
            foundHive.type = hive.type
            logAll()
        }
    }
    override suspend fun delete(hive: HiveModel) {
        hives.remove(hive)
        logAll()
    }

    private fun logAll() {
        hives.forEach { i("$it") }
    }
    override suspend fun findById(id:Long) : HiveModel? {
        val foundHive: HiveModel? = hives.find { it.id == id }
        return foundHive
    }

    override suspend fun findByTag(tag: Long): HiveModel? {
        return hives.find { p -> p.tag == tag }
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