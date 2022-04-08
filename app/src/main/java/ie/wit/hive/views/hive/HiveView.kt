package ie.wit.hive.views.hive

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import ie.wit.hive.R
import ie.wit.hive.cloudinary.Cloudinary
import ie.wit.hive.databinding.ActivityHiveBinding
import ie.wit.hive.models.HiveModel
import ie.wit.hive.models.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.alert
import timber.log.Timber.i


class HiveView : AppCompatActivity() {

    private lateinit var binding: ActivityHiveBinding
    private lateinit var presenter: HivePresenter
    lateinit var map: GoogleMap
    lateinit var hive : HiveModel
    lateinit var cloudinary:Cloudinary


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarAdd.title = title
        //setSupportActionBar(binding.toolbarAdd)

        presenter = HivePresenter(this)

        binding.chooseImage.setOnClickListener {
            presenter.cacheHive(
                binding.hiveTitle.text.toString().toLong(),
                binding.description.text.toString()
            )
            presenter.doSelectImage()
        }

        binding.mapView2.setOnClickListener {
            presenter.cacheHive(
                binding.hiveTitle.text.toString().toLong(),
                binding.description.text.toString()
            )
            presenter.doSetLocation()
        }

        binding.mapView2.onCreate(savedInstanceState);
        binding.mapView2.getMapAsync {
            map = it
            presenter.doConfigureMap(map)
            it.setOnMapClickListener { presenter.doSetLocation() }
        }




    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_hive, menu)
        val deleteMenu: MenuItem = menu.findItem(R.id.item_delete)
        if (presenter.hive.sensorNumber != "") {
            val bluetoothMenu: MenuItem = menu.findItem(R.id.bluetooth)
            bluetoothMenu.isVisible = true
        }
        if (presenter.edit) {
            deleteMenu.setVisible(true)
        } else {
            deleteMenu.setVisible(false)
        }
        var alarmCount:Int = 0
        for(alarm in presenter.alarms){
            if(alarm.hiveid == presenter.hive.fbid){
                alarmCount +=1
            }
        }
        menu.findItem(R.id.item_alarms).isVisible = alarmCount > 0
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_save -> {
                if (binding.description.text.toString().isEmpty()) {
                    Snackbar.make(binding.root, R.string.enter_hive_title, Snackbar.LENGTH_LONG)
                        .show()
                } else {
                    GlobalScope.launch(Dispatchers.IO) {
                        presenter.doAddOrSave(
                            binding.hiveTypeSpinner.selectedItem.toString(),
                            binding.description.text.toString()
                        )
                    }
                }
            }
            R.id.item_delete -> {
                runOnUiThread {
                    alert {
                        title = "Delete Hive"
                        message = "Confirm Delete"
                        positiveButton("OK") { runBlocking {  presenter.doDelete() } }
                    }.show()
                }
            }
            R.id.item_cancel -> {
                presenter.doCancel()
            }
            R.id.item_chart -> {
                presenter.chartNAv()
            }
            R.id.bluetooth -> {
                presenter.doShowBleScanner()
            }
            R.id.item_alarms -> {
                presenter.doShowAlarms()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
    }

    fun showHive(hive: HiveModel) {
        if (binding.hiveTitle.text.isEmpty()) binding.hiveTitle.setText(hive.tag.toString())
        if (binding.description.text.isEmpty()) binding.description.setText(hive.description)


        if (hive.image != "") {
            Picasso.get()
                .load(hive.image)
                .into(binding.hiveImage)

            binding.chooseImage.setText(R.string.change_hive_image)
        }
        this.showLocation(hive.location)
    }

    private fun showLocation(loc: Location) {
        binding.lat.setText("%.6f".format(loc.lat))
        binding.lng.setText("%.6f".format(loc.lng))
    }

    fun updateImage(image: String) {
        i("Image updated")
        Picasso.get()
            .load(image)
            .into(binding.hiveImage)
        binding.chooseImage.setText(R.string.change_hive_image)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView2.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView2.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView2.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView2.onResume()
        presenter.doRestartLocationUpdates()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView2.onSaveInstanceState(outState)
    }


}