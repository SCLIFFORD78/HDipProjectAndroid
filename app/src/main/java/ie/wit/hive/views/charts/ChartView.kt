package ie.wit.hive.views.charts

import android.graphics.Color.green
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ie.wit.hive.R
import ie.wit.hive.databinding.ActivityChartsBinding
import ie.wit.hive.databinding.ActivityHiveBinding
import ie.wit.hive.models.Location
import ie.wit.hive.models.HiveModel
import timber.log.Timber.i
import java.lang.reflect.Array
import java.text.SimpleDateFormat
import java.time.*


class ChartView : AppCompatActivity() {

    private lateinit var binding: ActivityChartsBinding
    private lateinit var presenter: ChartPresenter
    var hive = HiveModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChartsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //binding.toolbarAdd.title = title
        //setSupportActionBar(binding.toolbarAdd)

        presenter = ChartPresenter(this)

        setLineChartData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cancel, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.back -> {
                presenter.backNAv()
            }

        }
        return super.onOptionsItemSelected(item)
    }



    fun setLineChartData() {
        val tempData = ArrayList<Entry>()
        val humData = ArrayList<Entry>()
        val formattedTime = arrayListOf<String>()
        var values = Gson().fromJson("["+presenter.hive.recordedData+"]", JsonArray::class.java)
        val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")
        for (value in values){
            tempData.add(Entry(value.asJsonObject.get("timeStamp").asFloat,value.asJsonObject.get("Temperature").asFloat))
            humData.add(Entry(value.asJsonObject.get("timeStamp").asFloat,value.asJsonObject.get("Humidity").asFloat))
            val recordTime = sdf.format(value.asJsonObject.get("timeStamp").asFloat*1000)
            formattedTime.add(recordTime)

        }


        val linedataset = LineDataSet(tempData, "Temp")
        val linedataset2 = LineDataSet(humData, "Hum")
        //We add features to our chart
        linedataset.color = resources.getColor(R.color.purple_200)
        linedataset.circleRadius = 0f
        linedataset.setDrawFilled(true)
        linedataset.valueTextSize = 20F
        linedataset.fillColor = resources.getColor(R.color.black)
        linedataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        linedataset2.color = resources.getColor(R.color.purple_200)
        linedataset2.circleRadius = 0f
        linedataset2.setDrawFilled(true)
        linedataset2.valueTextSize = 20F
        linedataset2.fillColor = resources.getColor(R.color.black)
        linedataset2.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        class MyXAxisFormatter() : ValueFormatter() {
            //private val days = arrayOf("Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su")
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return sdf.format(value*1000)
            }
        }

        //We connect our data to the UI Screen
        val data = LineData(linedataset)
        var xAxis = binding.tempGraph.xAxis
        binding.tempGraph.data = data
        xAxis.valueFormatter = MyXAxisFormatter()
        xAxis.labelRotationAngle = 90f
        xAxis.mLabelWidth = 20
        binding.tempGraph.setBackgroundColor(resources.getColor(R.color.white))
        binding.tempGraph.animateXY(2000, 2000, Easing.EaseInCubic)

        val data2 = LineData(linedataset2)
        xAxis = binding.humGraph.xAxis
        binding.humGraph.data = data2
        xAxis.valueFormatter = MyXAxisFormatter()
        xAxis.labelRotationAngle = 90f
        xAxis.mLabelWidth = 20
        binding.humGraph.xAxis.valueFormatter = MyXAxisFormatter()
        binding.humGraph.setBackgroundColor(resources.getColor(R.color.white))
        binding.humGraph.animateXY(2000, 2000, Easing.EaseInCubic)

    }






}