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

        val linevalues = ArrayList<Entry>()
        linevalues.add(Entry(20f, 0.0F))
        linevalues.add(Entry(30f, 3.0F))
        linevalues.add(Entry(40f, 2.0F))
        linevalues.add(Entry(50f, 1.0F))
        linevalues.add(Entry(60f, 8.0F))
        linevalues.add(Entry(70f, 10.0F))
        linevalues.add(Entry(80f, 1.0F))
        linevalues.add(Entry(90f, 2.0F))
        linevalues.add(Entry(100f, 5.0F))
        linevalues.add(Entry(110f, 1.0F))
        linevalues.add(Entry(120f, 20.0F))
        linevalues.add(Entry(130f, 40.0F))
        linevalues.add(Entry(140f, 50.0F))

        val linedataset = LineDataSet(linevalues, "First")
        //We add features to our chart
        linedataset.color = resources.getColor(R.color.purple_200)

        linedataset.circleRadius = 10f
        linedataset.setDrawFilled(true)
        linedataset.valueTextSize = 20F
        linedataset.fillColor = resources.getColor(R.color.black)
        linedataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        //We connect our data to the UI Screen
        val data = LineData(linedataset)
        binding.getTheGraph.data = data
        binding.getTheGraph.setBackgroundColor(resources.getColor(R.color.white))
        binding.getTheGraph.animateXY(2000, 2000, Easing.EaseInCubic)

    }






}