package ie.wit.hive.views.alarmlist

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import ie.wit.hive.R
import ie.wit.hive.adapters.HiveAdapter
import ie.wit.hive.adapters.HiveListener
import ie.wit.hive.databinding.ActivityAlarmListBinding
import ie.wit.hive.databinding.ActivityHiveListBinding
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.AlarmEvents
import ie.wit.hive.models.HiveModel
import timber.log.Timber.i

class AlarmListView : AppCompatActivity(), AlarmListener {

    lateinit var app: MainApp
    lateinit var binding: ActivityAlarmListBinding
    lateinit var presenter: AlarmListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityAlarmListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideProgress()
        //update Toolbar title
        binding.toolbar.title = title
        val user = FirebaseAuth.getInstance().currentUser




        presenter = AlarmListPresenter(this)
        val layoutManager = LinearLayoutManager(this)
        binding.alarmRecycleView.layoutManager = layoutManager
        updateRecyclerView()


    }


    override fun onResume() {

        //update the view
        super.onResume()
        updateRecyclerView()
        binding.alarmRecycleView.adapter?.notifyDataSetChanged()
        i("recyclerView onResume")

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.back -> {
                presenter.backNAv()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAlarmClick(alarm: AlarmEvents) {
        showProgress()
        runBlocking { presenter.ackAlarm(alarm) }
        updateRecyclerView()

    }



    override fun onBackPressed() {
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cancel, menu)

        return super.onCreateOptionsMenu(menu)
    }



    private fun updateRecyclerView() {
            GlobalScope.launch(Dispatchers.Main) {
                binding.alarmRecycleView.adapter =
                    AlarmAdapter(presenter.getAlarms(),this@AlarmListView)
            }
            hideProgress()

    }



    fun showProgress() {
        binding.progressBar4.visibility = View.VISIBLE
    }

    fun hideProgress() {
        binding.progressBar4.visibility = View.GONE
    }

}