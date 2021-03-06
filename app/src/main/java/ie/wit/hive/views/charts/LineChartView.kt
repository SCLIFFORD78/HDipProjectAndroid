package ie.wit.hive.views.charts

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import ie.wit.hive.R
import ie.wit.hive.databinding.ActivityLinechartBinding
import ie.wit.hive.models.AlarmEvents
import ie.wit.hive.models.HiveModel
import ie.wit.hive.weather.WeatherHistory
import ie.wit.hive.weather.WeatherHistoryReport
import ie.wit.hive.weather.readWeatherHistory
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import kotlin.math.floor


class LineChartView : AppCompatActivity(), SeekBar.OnSeekBarChangeListener,
    OnChartValueSelectedListener {

    private lateinit var binding: ActivityLinechartBinding
    private lateinit var presenter: LineChartPresenter
    lateinit var hive: HiveModel
    private lateinit var weatherHistory: ArrayList<WeatherHistoryReport>
    private lateinit var weatherHistorySorted: List<WeatherHistoryReport>
    private lateinit var chart: LineChart
    private lateinit var chart2: LineChart
    val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")
    lateinit var ll1: LimitLine


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
            weatherHistorySorted = weatherHistory.sortedBy { it.timeStamp }
        }

        class MyxAxisFormatter() : ValueFormatter() {
            //private val days = arrayOf("Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su")
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return sdf.format(value * 1000)
            }
        }


        var tvX = binding.tvXMax

        var seekBarX = binding.seekBar1

        seekBarX.setOnSeekBarChangeListener(this)

        seekBarX.max = 50
        //seekBarY.setOnSeekBarChangeListener(this)


        chart = binding.chart1
        chart2 = binding.chart2
        //chart.setOnChartValueSelectedListener(this)

        // no description text

        // no description text

        setData(20, 20f)


        chart.description.isEnabled = false
        chart2.description.isEnabled = false

        // enable touch gestures

        // enable touch gestures
        chart.setTouchEnabled(true)
        chart2.setTouchEnabled(true)

        chart.dragDecelerationFrictionCoef = 0.9f
        chart2.dragDecelerationFrictionCoef = 0.9f

        // enable scaling and dragging

        // enable scaling and dragging
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)
        chart.isHighlightPerDragEnabled = true

        chart2.isDragEnabled = true
        chart2.setScaleEnabled(true)
        chart2.setDrawGridBackground(false)
        chart2.isHighlightPerDragEnabled = true

        // if disabled, scaling can be done on x- and y-axis separately

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false)
        chart2.setPinchZoom(false)

        // set an alternative background color

        // set an alternative background color
        chart.setBackgroundColor(Color.TRANSPARENT)
        chart2.setBackgroundColor(Color.TRANSPARENT)

        // add data

        // add data
        seekBarX.progress = presenter.hive.tempAlarm.toInt()


        chart.animateX(1500)
        chart2.animateX(1500)

        // get the legend (only possible after setting data)

        // get the legend (only possible after setting data)
        val l = chart.legend
        val l2 = chart2.legend

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
        // modify the legend ...
        l2.form = LegendForm.LINE
        //l2.typeface = tfLight
        l2.textSize = 11f
        l2.textColor = Color.BLACK
        l2.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l2.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l2.orientation = Legend.LegendOrientation.HORIZONTAL
        l2.setDrawInside(false)
        l2.isWordWrapEnabled = true
//        l2.setYOffset(11f);

        //        l2.setYOffset(11f);
        val xAxis = chart.xAxis
        //xAxis.typeface = tfLight
        xAxis.textSize = 11f
        xAxis.textColor = Color.BLACK
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.valueFormatter = MyxAxisFormatter()
        xAxis.labelRotationAngle = 45f
        xAxis.mLabelWidth = 3

        val xAxis2 = chart2.xAxis
        //xAxis2.typeface = tfLight
        xAxis2.textSize = 11f
        xAxis2.textColor = Color.BLACK
        xAxis2.setDrawGridLines(true)
        xAxis2.setDrawAxisLine(false)
        xAxis2.valueFormatter = MyxAxisFormatter()
        xAxis2.labelRotationAngle = 45f
        xAxis2.mLabelWidth = 3
        xAxis2.mLabelRotatedWidth = 3


        val leftAxis = chart.axisLeft
        //leftAxis.typeface = tfLight
        leftAxis.textColor = ColorTemplate.getHoloBlue()
        leftAxis.axisMaximum = 100f
        leftAxis.axisMinimum = -10f
        leftAxis.setDrawGridLines(true)
        leftAxis.isGranularityEnabled = true

        val leftAxis2 = chart.axisLeft
        //leftAxis2.typeface = tfLight
        leftAxis2.textColor = ColorTemplate.getHoloBlue()
        leftAxis2.axisMaximum = 50f
        leftAxis2.axisMinimum = -10f
        leftAxis2.setDrawGridLines(true)
        leftAxis2.isGranularityEnabled = true


        val rightAxis = chart.axisRight
        //rightAxis.typeface = tfLight
        rightAxis.textColor = Color.RED
        rightAxis.axisMaximum = 100f
        rightAxis.axisMinimum = -10f
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawZeroLine(false)
        rightAxis.isGranularityEnabled = false

        val rightAxis2 = chart.axisRight
        //rightAxis2.typeface = tfLight
        rightAxis2.textColor = Color.RED
        rightAxis2.axisMaximum = 50f
        rightAxis2.axisMinimum = -10f
        rightAxis2.setDrawGridLines(false)
        rightAxis2.setDrawZeroLine(false)
        rightAxis2.isGranularityEnabled = false


    }

    private fun setData(count: Int, range: Float) {

        var values = Gson().fromJson("[" + presenter.hive.recordedData + "]", JsonArray::class.java)

        val tempData = java.util.ArrayList<Entry>()

        val ambientTempData = java.util.ArrayList<Entry>()

        val humData = java.util.ArrayList<Entry>()

        val ambientHumData = java.util.ArrayList<Entry>()

        val tempLimit = java.util.ArrayList<Entry>()

        val formattedTime = arrayListOf<String>()
        var now:Float = floor((System.currentTimeMillis() / 1000).toDouble()).toLong().toFloat()
        if (weatherHistorySorted.size > 0){
            tempLimit.add(
                Entry(
                    weatherHistorySorted[0].timeStamp.toFloat(),
                    binding.tvXMax.text.toString().toFloat()
                )
            )
            tempLimit.add(
                Entry(
                    now,
                    binding.tvXMax.text.toString().toFloat()
                )
            )
        }else{

            tempLimit.add(
                Entry(
                    now - 86400f,
                    binding.tvXMax.text.toString().toFloat()
                )
            )
            tempLimit.add(
                Entry(
                    now,
                    binding.tvXMax.text.toString().toFloat()
                )
            )
        }


        for (value in weatherHistorySorted) {
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
        val set5: LineDataSet


        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            set1 = chart.data.getDataSetByIndex(0) as LineDataSet
            set2 = chart.data.getDataSetByIndex(1) as LineDataSet
            set5 = chart.data.getDataSetByIndex(2) as LineDataSet
            set1.values = tempData
            set2.values = ambientTempData
            set5.values = tempLimit
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

            set5 = LineDataSet(tempLimit, "Limit")
            set5.axisDependency = AxisDependency.RIGHT
            set5.color = Color.RED
            set5.setDrawCircles(false)
            set5.lineWidth = 4f
            set5.fillAlpha = 65
            set5.enableDashedLine(10f, 10f, 0f)
            set5.highLightColor = Color.rgb(244, 117, 117)

            // create a data object with the data sets
            val data = LineData(set1, set2, set5)
            data.setValueTextColor(Color.BLACK)
            data.setValueTextSize(9f)

            // set data
            chart.data = data
        }

        if (chart2.data != null &&
            chart2.data.dataSetCount > 0
        ) {
            set3 = chart2.data.getDataSetByIndex(0) as LineDataSet
            set4 = chart2.data.getDataSetByIndex(1) as LineDataSet
            set3.values = humData
            set4.values = ambientHumData
            chart2.data.notifyDataChanged()
            chart2.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type

            // create a dataset and give it a type
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
            val data = LineData( set3, set4)
            data.setValueTextColor(Color.BLACK)
            data.setValueTextSize(9f)

            // set data
            chart2.data = data
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cancel, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.back -> {
                runBlocking { presenter.backNAv(binding.tvXMax.text.toString().toFloat()) }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        binding.tvXMax.text = progress.toString()
        chart = binding.chart1
        chart2 = binding.chart2
        chart.invalidate()
        setData(1, 1f)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }

    override fun onBackPressed() {
    }


}