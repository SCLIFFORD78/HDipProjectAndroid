package ie.wit.hive.views.charts

import android.graphics.Color.green
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
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
        var values = Gson().fromJson("["+presenter.hive.recordedData+"]", JsonArray::class.java)
        for (value in values){
            tempData.add(Entry(value.asJsonObject.get("timeStamp").asFloat,value.asJsonObject.get("Temperature").asFloat))
            humData.add(Entry(value.asJsonObject.get("timeStamp").asFloat,value.asJsonObject.get("Humidity").asFloat))
        }


        val linedataset = LineDataSet(tempData, "Temp")
        val linedataset2 = LineDataSet(humData, "Hum")
        //We add features to our chart
        linedataset.color = resources.getColor(R.color.purple_200)
        linedataset.circleRadius = 1f
        linedataset.setDrawFilled(true)
        linedataset.valueTextSize = 20F
        linedataset.fillColor = resources.getColor(R.color.black)
        linedataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        linedataset2.color = resources.getColor(R.color.purple_200)
        linedataset2.circleRadius = 1f
        linedataset2.setDrawFilled(true)
        linedataset2.valueTextSize = 20F
        linedataset2.fillColor = resources.getColor(R.color.black)
        linedataset2.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        //We connect our data to the UI Screen
        val data = LineData(linedataset)
        binding.tempGraph.data = data
        binding.tempGraph.setBackgroundColor(resources.getColor(R.color.white))
        binding.tempGraph.animateXY(2000, 2000, Easing.EaseInCubic)

        val data2 = LineData(linedataset2)
        binding.humGraph.data = data2
        binding.humGraph.setBackgroundColor(resources.getColor(R.color.white))
        binding.humGraph.animateXY(2000, 2000, Easing.EaseInCubic)

    }






}