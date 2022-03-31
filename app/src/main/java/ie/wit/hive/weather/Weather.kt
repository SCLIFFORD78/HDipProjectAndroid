package ie.wit.hive.weather

import android.net.http.HttpResponseCache.install

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.StructureKind
import java.util.*
import kotlin.collections.ArrayList

var lat = 52.01758450221158
var lon = -9.522837437689304
var weatherapiKey = "21f9785cb8764c4eef7c01a6219fa7f5"

@Serializable

data class Weather( val main : Map<String, Float>,  val wind : Map<String, Float>, val visibility: Long)

val client = HttpClient(CIO) {
    install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

suspend fun getWeather() {

    runBlocking {
        val weather: Weather = client.get("http://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${weatherapiKey}")
        println(weather)
    }

    //val client = HttpClient(CIO)
    //val response: HttpResponse = client.get("http://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${weatherapiKey}")
    //println(response.status)
    //client.close()

}
