package ie.wit.hive.views.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ie.wit.hive.databinding.ActivityHiveMapsBinding
import ie.wit.hive.databinding.ContentHiveMapsBinding
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.HiveModel
import ie.wit.hive.readImageFromPath

class HiveMapView : AppCompatActivity() , GoogleMap.OnMarkerClickListener{

    private lateinit var binding: ActivityHiveMapsBinding
    private lateinit var contentBinding: ContentHiveMapsBinding
    lateinit var app: MainApp
    lateinit var presenter: HiveMapPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as MainApp
        binding = ActivityHiveMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        presenter = HiveMapPresenter(this)

        contentBinding = ContentHiveMapsBinding.bind(binding.root)

        contentBinding.mapView.onCreate(savedInstanceState)
        contentBinding.mapView.getMapAsync{
            GlobalScope.launch(Dispatchers.Main) {
                presenter.doPopulateMap(it)
            }
        }
    }
    override fun onMarkerClick(marker: Marker): Boolean {
        GlobalScope.launch(Dispatchers.Main) {
            presenter.doMarkerSelected(marker)
        }
        return true
    }
    fun showHive(hive: HiveModel) {
        contentBinding.currentTitle.text = hive.tag.toString()
        contentBinding.currentType.text = hive.type
        if (hive.image != ""){
            Picasso.get()
                .load(hive.image)
                .into(contentBinding.imageView2)
        }else{
            contentBinding.imageView2.setImageBitmap(readImageFromPath(this,"app/src/main/res/drawable/basic.png"))
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        contentBinding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        contentBinding.mapView.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        contentBinding.mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        contentBinding.mapView.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        contentBinding.mapView.onSaveInstanceState(outState)
    }


}