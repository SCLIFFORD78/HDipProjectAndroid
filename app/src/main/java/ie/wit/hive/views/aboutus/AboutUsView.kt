package ie.wit.hive.views.aboutus

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ie.wit.hive.R
import ie.wit.hive.databinding.ActivityAboutusBinding

class AboutUsView : AppCompatActivity() {
    lateinit var presenter: AboutUsPresenter
    private lateinit var binding: ActivityAboutusBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        presenter = AboutUsPresenter(this)
        super.onCreate(savedInstanceState)
        binding = ActivityAboutusBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbarAdd)
        setContentView(binding.root)





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



}