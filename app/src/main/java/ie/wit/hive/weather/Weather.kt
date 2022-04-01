package ie.wit.hive.weather

import android.net.http.HttpResponseCache.install
import android.os.SystemClock
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
import timber.log.Timber
import java.lang.reflect.Array
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
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
data class WeatherHistory(
    @Embedded var main: Main,
    @Embedded var wind: Wind,
    val dt: Long
)

@Serializable
data class List(
    var list: ArrayList<WeatherHistory>,
)

@Serializable
data class Weather(
    @Embedded var main: Main,
    @Embedded var wind: Wind,
    val visibility: Long,
    val dt: Long
)

@Serializable
data class WeatherHistoryReport(
    var Temperature: Float,
    var Humidity: Float,
    var timeStamp: Long
)


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
            "temp" to (weather.main.temp - 273.15f),
            "humidity" to (weather.main.humidity),
            "windSpeed" to (weather.wind.speed),
            "windDirection" to (weather.wind.deg)
        )

    }

    return weatherReport
}

suspend fun readWeatherHistory(
    lat: Double,
    lon: Double,
    dateLogged: String
): kotlin.collections.List<WeatherHistoryReport> {
    var returnWeather = ArrayList<WeatherHistoryReport>()
    var weatherHistoryReport: WeatherHistoryReport = WeatherHistoryReport(0.0f, 0.0f, 0)
    var test = System.currentTimeMillis() / 1000;
    var dateNow = Math.round(
        (System.currentTimeMillis() / 1000) - ((System.currentTimeMillis()
            .toDouble() / 1000) % 3600)
    );

    runBlocking {
        var epocDate = dateLogged.toLong()
        var list: List =
            client.get("http://history.openweathermap.org/data/2.5/history/city?lat=${lat}&lon=${lon}&type=hour&start=${epocDate}&end=${dateNow}&appid=${weatherapiKey}")
        var lastDate = list.list.get(list.list.size - 1).dt
        print(lastDate)
        list.list.forEach {
            weatherHistoryReport.Temperature = it.main.temp - 273.15f
            weatherHistoryReport.Humidity = it.main.humidity
            weatherHistoryReport.timeStamp = it.dt
            returnWeather.add(weatherHistoryReport.copy())
        }


        while (lastDate < dateNow - 86400) {
            epocDate = list.list.get(list.list.size - 1).dt
            list =
                client.get("http://history.openweathermap.org/data/2.5/history/city?lat=${lat}&lon=${lon}&type=hour&start=${epocDate}&end=${dateNow}&appid=${weatherapiKey}")
            list.list.forEach {
                weatherHistoryReport.Temperature = it.main.temp - 273.15f
                weatherHistoryReport.Humidity = it.main.humidity
                weatherHistoryReport.timeStamp = it.dt
                returnWeather.add(weatherHistoryReport.copy())
            }
            lastDate = list.list.get(list.list.size - 1).dt
        }

    }
    Timber.i("returneddd weathe" + returnWeather)
    return returnWeather
}
