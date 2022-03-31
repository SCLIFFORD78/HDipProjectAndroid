package ie.wit.hive.weather

import android.net.http.HttpResponseCache.install
import androidx.room.Embedded
import ie.wit.hive.models.Location

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
import kotlin.math.roundToInt

//var lat = 52.01758450221158
//var lon = -9.522837437689304
var weatherapiKey = "21f9785cb8764c4eef7c01a6219fa7f5"

@Serializable
data class Wind(val speed: Float, val deg: Float)

@Serializable
data class Main(
    val temp: Float,
    val feels_like: Float,
    val temp_min: Float,
    val temp_max: Float,
    val pressure: Float,
    val humidity: Float
)

@Serializable
data class Weather(@Embedded var main: Main, @Embedded var wind: Wind, val visibility: Long)


val client = HttpClient(CIO) {
    install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

suspend fun getWeather(lat: Double, lon: Double): Map<String, Float> {
    var weatherReport: Map<String, Float>
    runBlocking {
        val weather: Weather =
            client.get("http://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${weatherapiKey}")
        println(weather)
        weatherReport = mapOf(
            "temp" to ( weather.main.temp - 273.15f),
            "humidity" to (weather.main.humidity),
            "windSpeed" to (weather.wind.speed),
            "windDirection" to (weather.wind.deg)
        )

    }

    return weatherReport
}
