package ie.wit.hive.views.charts

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.slider.Slider
import com.google.gson.Gson
import com.google.gson.JsonArray
import ie.wit.hive.R
import ie.wit.hive.databinding.ActivityLinechartBinding
import ie.wit.hive.models.HiveModel
import ie.wit.hive.weather.WeatherHistoryReport
import ie.wit.hive.weather.readWeatherHistory
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.sdk27.coroutines.onSeekBarChangeListener
import java.text.SimpleDateFormat
import java.time.*
import kotlin.coroutines.coroutineContext


class LineChartView : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    private lateinit var binding: ActivityLinechartBinding
    private lateinit var presenter: LineChartPresenter
    lateinit var hive: HiveModel
    private lateinit var weatherHistory : ArrayList<WeatherHistoryReport>
    private lateinit var chart: LineChart
    val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLinechartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //binding.toolbarAdd.title = title
        //setSupportActionBar(binding.toolbarAdd)

        presenter = LineChartPresenter(this)

        runBlocking {
            weatherHistory = readWeatherHistory(
                presenter.hive.location.lat,
                presenter.hive.location.lng,
                presenter.hive.dateRegistered
            )
        }

        class MyXAxisFormatter() : ValueFormatter() {
            //private val days = arrayOf("Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su")
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return sdf.format(value * 1000)
            }
        }


        var tvX = binding.tvXMax
        var tvY = binding.tvYMax

        var seekBarX = binding.seekBar1

        //seekBarX.onSeekBarChangeListener(coroutineContext)

        var seekBarY = binding.seekBar2
        //seekBarY.setOnSeekBarChangeListener(this)


        chart = binding.chart1
        //chart.setOnChartValueSelectedListener(this)

        // no description text

        // no description text

        setData(20, 20f)


        chart.description.isEnabled = false

        // enable touch gestures

        // enable touch gestures
        chart.setTouchEnabled(true)

        chart.dragDecelerationFrictionCoef = 0.9f

        // enable scaling and dragging

        // enable scaling and dragging
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)
        chart.isHighlightPerDragEnabled = true

        // if disabled, scaling can be done on x- and y-axis separately

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true)

        // set an alternative background color

        // set an alternative background color
        chart.setBackgroundColor(Color.TRANSPARENT)

        // add data

        // add data
        seekBarX.progress = 20
        seekBarY.progress = 30

        chart.animateX(1500)

        // get the legend (only possible after setting data)

        // get the legend (only possible after setting data)
        val l = chart.legend

        // modify the legend ...

        // modify the legend ...
        l.form = LegendForm.LINE
        //l.typeface = tfLight
        l.textSize = 11f
        l.textColor = Color.BLACK
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.isWordWrapEnabled = true
//        l.setYOffset(11f);

        //        l.setYOffset(11f);
        val xAxis = chart.xAxis
        //xAxis.typeface = tfLight
        xAxis.textSize = 11f
        xAxis.textColor = Color.BLACK
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.valueFormatter = MyXAxisFormatter()
        xAxis.labelRotationAngle = 90f
        xAxis.mLabelWidth = 5

        val leftAxis = chart.getAxisLeft()
        //leftAxis.typeface = tfLight
        leftAxis.textColor = ColorTemplate.getHoloBlue()
        leftAxis.axisMaximum = 100f
        leftAxis.axisMinimum = -10f
        leftAxis.setDrawGridLines(true)
        leftAxis.isGranularityEnabled = true

        val rightAxis = chart.getAxisRight()
        //rightAxis.typeface = tfLight
        rightAxis.textColor = Color.RED
        rightAxis.axisMaximum = 100f
        rightAxis.axisMinimum = -10f
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawZeroLine(false)
        rightAxis.isGranularityEnabled = false


    }

    private fun setData(count: Int, range: Float) {

        var values = Gson().fromJson("[" + presenter.hive.recordedData + "]", JsonArray::class.java)

        val tempData = java.util.ArrayList<Entry>()

        val ambientTempData = java.util.ArrayList<Entry>()

        val humData = java.util.ArrayList<Entry>()

        val ambientHumData = java.util.ArrayList<Entry>()


        val formattedTime = arrayListOf<String>()

        for (value in weatherHistory) {
            ambientTempData.add(
                Entry(
                    value.timeStamp.toFloat(),
                    value.Temperature
                )
            )
            ambientHumData.add(
                Entry(
                    value.timeStamp.toFloat(),
                    value.Humidity
                )
            )
        }

        for (value in values) {
            tempData.add(
                Entry(
                    value.asJsonObject.get("timeStamp").asFloat,
                    value.asJsonObject.get("Temperature").asFloat
                )
            )
            humData.add(
                Entry(
                    value.asJsonObject.get("timeStamp").asFloat,
                    value.asJsonObject.get("Humidity").asFloat
                )
            )
            val recordTime = sdf.format(value.asJsonObject.get("timeStamp").asFloat * 1000)
            formattedTime.add(recordTime)

        }

        val set1: LineDataSet
        val set2: LineDataSet
        val set3: LineDataSet
        val set4: LineDataSet


        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            set1 = chart.data.getDataSetByIndex(0) as LineDataSet
            set2 = chart.data.getDataSetByIndex(1) as LineDataSet
            set3 = chart.data.getDataSetByIndex(2) as LineDataSet
            set4 = chart.data.getDataSetByIndex(3) as LineDataSet
            set1.values = tempData
            set2.values = ambientTempData
            set3.values = humData
            set4.values = ambientHumData
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(tempData, "Temperature")
            set1.axisDependency = AxisDependency.LEFT
            set1.color = Color.GREEN
            set1.setDrawCircles(false)
            set1.setCircleColor(Color.WHITE)
            set1.lineWidth = 1f
            set1.circleRadius = 0f
            set1.fillAlpha = 65
            set1.fillColor = ColorTemplate.getHoloBlue()
            set1.highLightColor = Color.rgb(244, 117, 117)
            set1.setDrawCircleHole(false)
            //set1.setFillFormatter(new MyFillFormatter(0f));
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            set2 = LineDataSet(ambientTempData, "Ambient Temperature")
            set2.axisDependency = AxisDependency.LEFT
            set2.color = Color.RED
            set2.setDrawCircles(false)
            set2.setCircleColor(Color.WHITE)
            set2.lineWidth = 1f
            set2.circleRadius = 0f
            set2.fillAlpha = 65
            set2.fillColor = Color.RED
            set2.setDrawCircleHole(false)
            set2.highLightColor = Color.rgb(244, 117, 117)
            //set2.setFillFormatter(new MyFillFormatter(900f));
            set3 = LineDataSet(humData, "Humidity")
            set3.axisDependency = AxisDependency.RIGHT
            set3.color = Color.CYAN
            set3.setDrawCircles(false)
            set3.setCircleColor(Color.WHITE)
            set3.lineWidth = 1f
            set3.circleRadius = 3f
            set3.fillAlpha = 65
            set3.fillColor = ColorTemplate.colorWithAlpha(Color.YELLOW, 200)
            set3.setDrawCircleHole(false)
            set3.highLightColor = Color.rgb(244, 117, 117)

            set4 = LineDataSet(ambientHumData, "Ambient Humidity")
            set4.axisDependency = AxisDependency.RIGHT
            set4.color = Color.BLUE
            set4.setDrawCircles(false)
            set4.setCircleColor(Color.WHITE)
            set4.lineWidth = 1f
            set4.circleRadius = 3f
            set4.fillAlpha = 65
            set4.fillColor = ColorTemplate.colorWithAlpha(Color.YELLOW, 200)
            set4.setDrawCircleHole(false)
            set4.highLightColor = Color.rgb(244, 117, 117)

            // create a data object with the data sets
            val data = LineData(set1, set2, set3, set4)
            data.setValueTextColor(Color.BLACK)
            data.setValueTextSize(9f)

            // set data
            chart.data = data
        }
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

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        TODO("Not yet implemented")
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        TODO("Not yet implemented")
    }


}