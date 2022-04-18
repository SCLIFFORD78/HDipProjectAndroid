package ie.wit.hive.views.sensor

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import ie.wit.hive.R
import ie.wit.hive.bleandroid.ble.ConnectionEventListener
import ie.wit.hive.bleandroid.ble.ConnectionManager
import ie.wit.hive.bleandroid.ble.toHexString
import ie.wit.hive.databinding.ActivitySensorControlBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.alert
import org.jetbrains.anko.collections.forEachByIndex
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class SensorView : AppCompatActivity() {

    private lateinit var binding: ActivitySensorControlBinding
    private lateinit var presenter: SensorPresenter

    private lateinit var device: BluetoothDevice
    private val dateFormatter = SimpleDateFormat("MMM d, HH:mm:ss", Locale.US)
    private val characteristics by lazy {
        ConnectionManager.servicesOnDevice(device)?.flatMap { service ->
            service.characteristics ?: listOf()
        } ?: listOf()
    }

    private var notifyingCharacteristics = mutableListOf<UUID>()
    private var intervilTime: Int = 0
    private var loggerTimeReference: Int = 0
    private var loggerFlashUsage: Int = 16
    private var flashUsageReference: Int = 0
    private var sensorLogData = arrayListOf<JsonObject>()
    private var loggerActive: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        ConnectionManager.registerListener(connectionEventListener)
        super.onCreate(savedInstanceState)

        binding = ActivitySensorControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //binding.toolbarAdd.title = title
        //setSupportActionBar(binding.toolbarAdd)


        presenter = SensorPresenter(this)
        device = presenter.getBLEdevice()
        showProgress()


        //setContentView(R.layout.activity_sensor_control)
        //supportActionBar?.apply {
        //    setDisplayHomeAsUpEnabled(true)
        //   setDisplayShowTitleEnabled(true)
        //    //title = getString(R.string.ble_playground)
        //}
        flashUsageReference = 0

        //readLoggerIntervalTime()
        //readLoggerTimeReference()
        //readLoggerFlashUsage()

        binding.readParams.setOnClickListener {
            readParams()
        }

        binding.data.setOnClickListener {
            readData()
        }

        binding.getLog.setOnClickListener {
            readLoggerData()
        }

        //readParams()

    }

    override fun onBackPressed() {
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bluetooth, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.back -> {
                presenter.doShowBleScanner()
            }
            R.id.aboutus -> {
                presenter.doShowAboutUs()
            }
            R.id.item_logout -> {
                GlobalScope.launch(Dispatchers.IO) {
                    presenter.doLogout()
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }


    @SuppressLint("SetTextI18n")
    private fun log(message: String) {
        val formattedMessage = String.format("%s: %s", dateFormatter.format(Date()), message)
        runOnUiThread {
            var currentLogText = binding.logTextView.text
            binding.logTextView.text.ifEmpty {
                currentLogText = "Beginning of log."
                hideProgress()
                readParams()
            }
            binding.logTextView.text = "$currentLogText\n$formattedMessage"
            binding.logScrollView.post { binding.logScrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onDisconnect = {
                runOnUiThread {
                    alert {
                        title = "Disconnected"
                        message = "Disconnected from device."
                        positiveButton("OK") { onBackPressed() }
                    }.show()
                }
            }

            onCharacteristicRead = { _, characteristic ->
                if (characteristic.uuid == UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")){
                    binding.batteryLevel.text = Integer.decode(characteristic.value.toHexString()).toString()
                }else if(characteristic.uuid == UUID.fromString("a8a82636-10a4-11e3-ab8c-f23c91aec05e")){
                    val timestamp = toInt32(characteristic.value)
                    loggerTimeReference = timestamp
                    val date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date(timestamp.toLong()*1000))
                    binding.loggerRefTime.text = date.toString()
                }else if(characteristic.uuid == UUID.fromString("a8a82634-10a4-11e3-ab8c-f23c91aec05e")){
                    val interValSeconds = toInt16(characteristic.value)
                    binding.intervalText.text = interValSeconds.toString()
                    intervilTime = interValSeconds
                }else if(characteristic.uuid == UUID.fromString("a8a82950-10a4-11e3-ab8c-f23c91aec05e")) {
                    val flashSizeTemp = toInt32(characteristic.value)
                    binding.flashSize.text = flashSizeTemp.toString()
                }else if(characteristic.uuid == UUID.fromString("a8a82646-10a4-11e3-ab8c-f23c91aec05e")) {
                    val flashUsageTemp = toInt32(characteristic.value)
                    loggerFlashUsage = flashUsageTemp+16
                    binding.flashUsage.text = flashUsageTemp.toString()
                }else if(characteristic.uuid == UUID.fromString("a8a82633-10a4-11e3-ab8c-f23c91aec05e")) {
                    loggerActive =  characteristic.value[0].toInt()
                    val test = loggerActive
                    print(test)
                    //binding.flashSize.text = "Flash size: ${flashSizeTemp.toString()}"
                }
                log("Read from ${characteristic.uuid}: ${characteristic.value.toHexString()}")
            }

            onCharacteristicWrite = { _, characteristic ->
                log("Wrote to ${characteristic.uuid}")
            }

            onMtuChanged = { _, mtu ->
                log("MTU updated to $mtu")

            }

            onCharacteristicChanged = { _, characteristic ->
                if (characteristic.uuid == UUID.fromString("a8a82631-10a4-11e3-ab8c-f23c91aec05e")) {
                    val result = characteristic.value
                    val converted = convertTempAndHumidity(result, 0)
                    binding.temperature.text = converted.get("Temperature").toString()
                    binding.humidity.text = converted.get("Humidity").toString()
                    stopReadData()
                } else if (characteristic.uuid == UUID.fromString("a8a82637-10a4-11e3-ab8c-f23c91aec05e")) {
                    val loggerData = characteristic.value

                    if (flashUsageReference <= loggerFlashUsage && (flashUsageReference != 0)) {
                        if (loggerData.isNotEmpty()) {
                            if (loggerData[0].toInt() != -1 && loggerData[1].toInt() != -1 && loggerData[2].toInt() != -1 && loggerData[3].toInt() != -1) {
                                sensorLogData.add(
                                    convertTempAndHumidity(
                                        loggerData.copyOfRange(
                                            0,
                                            4
                                        ), loggerTimeReference
                                    )
                                )
                                loggerTimeReference += intervilTime
                            }
                        }
                        if (loggerData.size > 4) {
                            if (loggerData[4].toInt() != -1 && loggerData[5].toInt() != -1 && loggerData[6].toInt() != -1 && loggerData[7].toInt() != -1) {
                                sensorLogData.add(
                                    convertTempAndHumidity(
                                        loggerData.copyOfRange(
                                            4,
                                            8
                                        ), loggerTimeReference
                                    )
                                )
                                loggerTimeReference += intervilTime
                            }

                        }
                        if (loggerData.size > 8) {
                            if (loggerData[8].toInt() != -1 && loggerData[9].toInt() != -1 && loggerData[10].toInt() != -1 && loggerData[11].toInt() != -1) {
                                sensorLogData.add(
                                    convertTempAndHumidity(
                                        loggerData.copyOfRange(
                                            8,
                                            12
                                        ), loggerTimeReference
                                    )
                                )
                                loggerTimeReference += intervilTime
                            }
                        }
                        if (loggerData.size > 12) {
                            if (loggerData[12].toInt() != -1 && loggerData[13].toInt() != -1 && loggerData[14].toInt() != -1 && loggerData[15].toInt() != -1) {
                                sensorLogData.add(
                                    convertTempAndHumidity(
                                        loggerData.copyOfRange(
                                            12,
                                            16
                                        ), loggerTimeReference
                                    )
                                )
                                loggerTimeReference += intervilTime
                            }
                        }
                        flashUsageReference += loggerData.size
                    }
                    if (flashUsageReference == 0) {
                        flashUsageReference += loggerData.size
                    }
                    if (flashUsageReference >= loggerFlashUsage) {
                        flashUsageReference = 0
                        loggerTimeReference = 0
                        intervilTime - 0
                        loggerFlashUsage = 16
                        runBlocking { presenter.doUpdateHive(sensorLogData) }
                        sensorLogData.clear()
                        resetLogger()
                        readLoggerActive()
                        if (loggerActive == 0) {
                            writeLoggerTimeReference()
                            writeLoggerActive()
                        } else {
                            writeLoggerTimeReference()
                        }
                        readParams()
                    }

                }

                log("Value changed on ${characteristic.uuid}: ${characteristic.value.toHexString()}")
            }

            onNotificationsEnabled = { _, characteristic ->
                log("Enabled notifications on ${characteristic.uuid}")
                notifyingCharacteristics.add(characteristic.uuid)
            }

            onNotificationsDisabled = { _, characteristic ->
                log("Disabled notifications on ${characteristic.uuid}")
                notifyingCharacteristics.remove(characteristic.uuid)
            }
        }
    }

    private fun toInt32(bytes: ByteArray): Int {
        if (bytes.size != 4) {
            log("toInt32: Wrong length")
            throw Exception("wrong len")
        }
        bytes.reverse()
        return ByteBuffer.wrap(bytes).int
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun toInt16(bytes: ByteArray): Int {
        if (bytes.size != 2) {
            throw Exception("wrong len")
        }
        return (bytes[0].toInt().and(0xff)).or((bytes[1].toInt().rotateLeft(8)).and(0xff00))
    }

    private fun numberToByteArray(data: Number, size: Int = 4): ByteArray =
        ByteArray(size) { i -> (data.toLong() shr (i * 8)).toByte() }

    private fun readParams() {
        checkBattery()
        readLoggerTimeReference()
        readLoggerIntervalTime()
        readLoggerFlashSize()
        readLoggerFlashUsage()
        //readData()
        readLoggerActive()
    }

    private fun checkBattery() {
        val batteryLevelCharUuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
        ConnectionManager.readSensorCharacteristic(device, batteryLevelCharUuid)
    }

    private fun readData() {
        val bluSensorData = UUID.fromString("a8a82631-10a4-11e3-ab8c-f23c91aec05e")
        ConnectionManager.enableSensorNotifications(device, bluSensorData)
    }

    private fun stopReadData() {
        val bluSensorData = UUID.fromString("a8a82631-10a4-11e3-ab8c-f23c91aec05e")
        characteristics.forEachByIndex { t ->
            if (t.uuid == bluSensorData) {
                ConnectionManager.disableNotifications(device, t)
            }
        }
    }

    private fun readLoggerTimeReference() {
        val loggerTimeReference = UUID.fromString("a8a82636-10a4-11e3-ab8c-f23c91aec05e")
        ConnectionManager.readSensorCharacteristic(device, loggerTimeReference)
    }

    private fun readLoggerIntervalTime() {
        val loggerIntervalTime = UUID.fromString("a8a82634-10a4-11e3-ab8c-f23c91aec05e")
        ConnectionManager.readSensorCharacteristic(device, loggerIntervalTime)
    }

    private fun readLoggerFlashSize() {
        val loggerFlashSize = UUID.fromString("a8a82950-10a4-11e3-ab8c-f23c91aec05e")
        ConnectionManager.readSensorCharacteristic(device, loggerFlashSize)
    }

    private fun readLoggerFlashUsage() {
        val loggerFlashUsage = UUID.fromString("a8a82646-10a4-11e3-ab8c-f23c91aec05e")
        ConnectionManager.readSensorCharacteristic(device, loggerFlashUsage)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun readLoggerData() {
        val loggerControl = UUID.fromString("a8a82635-10a4-11e3-ab8c-f23c91aec05e")
        val loggerData = UUID.fromString("a8a82637-10a4-11e3-ab8c-f23c91aec05e")
        readLoggerFlashUsage()
        readLoggerTimeReference()
        readLoggerIntervalTime()
        characteristics.forEachByIndex { t ->
            if (t.uuid == loggerData) {
                ConnectionManager.enableNotifications(device, t)
            }
        }

        characteristics.forEachByIndex { t ->
            if (t.uuid == loggerControl) {
                ConnectionManager.writeCharacteristic(device, t, byteArrayOf(0x1))
            }
        }
    }

    private fun resetLogger() {
        val loggerControl = UUID.fromString("a8a82635-10a4-11e3-ab8c-f23c91aec05e")
        characteristics.forEachByIndex { t ->
            if (t.uuid == loggerControl) {
                ConnectionManager.writeCharacteristic(device, t, byteArrayOf(0x2))
            }
        }
    }

    private fun readLoggerActive() {
        val loggerOnOff = UUID.fromString("a8a82633-10a4-11e3-ab8c-f23c91aec05e")
        ConnectionManager.readSensorCharacteristic(device, loggerOnOff)
    }

    private fun writeLoggerActive() {
        val loggerOnOff = UUID.fromString("a8a82633-10a4-11e3-ab8c-f23c91aec05e")
        characteristics.forEachByIndex { t ->
            if (t.uuid == loggerOnOff) {
                ConnectionManager.writeCharacteristic(device, t, byteArrayOf(0x1))
            }
        }
    }

    private fun writeLoggerTimeReference() {
        val loggerTimeReference = UUID.fromString("a8a82636-10a4-11e3-ab8c-f23c91aec05e")
        val timeNow = numberToByteArray(
            (((System.currentTimeMillis() / 1000).toInt()) - (((System.currentTimeMillis() / 1000).toInt()) % 3600)),
            4
        )
        characteristics.forEachByIndex { t ->
            if (t.uuid == loggerTimeReference) {
                ConnectionManager.writeCharacteristic(device, t, timeNow)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun convertTempAndHumidity(sensorData: ByteArray, timeStamp: Int): JsonObject {
        val result = JsonObject()
        val temp = (sensorData[0].toInt().and(0xff)).or(
            (sensorData[1].toInt().rotateLeft(8)).and(0xff00)
        )
        val tempC = -46.85f + 175.72f * temp.toFloat() / 65536.toFloat()
        result.addProperty("Temperature", tempC)
        val hum = (sensorData[2].toInt().and(0xff)).or(
            (sensorData[3].toInt().rotateLeft(8)).and(0xff00)
        )//.and(0xff.toByte())
        val relHum = -6.0f + 125.0f * hum.toFloat() / 65536.toFloat()
        result.addProperty("Humidity", relHum)
        if (timeStamp > 0)
            result.addProperty("timeStamp", timeStamp)
        return result

    }

    fun showProgress() {
        binding.progressBar3.visibility = View.VISIBLE
    }

    fun hideProgress() {
        binding.progressBar3.visibility = View.GONE
    }


}