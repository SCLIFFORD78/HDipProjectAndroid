package ie.wit.hive.views.alarmlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import ie.wit.hive.R
import ie.wit.hive.databinding.ActivityAlarmListBinding
import ie.wit.hive.main.MainApp
import ie.wit.hive.models.AlarmEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber.i

class AlarmListView : AppCompatActivity(), AlarmListener {

    lateinit var app: MainApp
    lateinit var binding: ActivityAlarmListBinding
    lateinit var presenter: AlarmListPresenter
    private lateinit var alarmsToggle:SwitchCompat
    var alarmSwitchState = true

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityAlarmListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideProgress()
        //update Toolbar title
        val user = FirebaseAuth.getInstance().currentUser
        binding.alarmsToggle .setOnClickListener {
            if (alarmSwitchState){
                updateRecyclerView(false)
                alarmSwitchState = false
            }else{
                updateRecyclerView(true)
                alarmSwitchState = true}
        }

        presenter = AlarmListPresenter(this)
        val layoutManager = LinearLayoutManager(this)

        binding.alarmRecycleView.layoutManager = layoutManager
        updateRecyclerView(true)


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cancel, menu)



        return super.onCreateOptionsMenu(menu)
    }


    override fun onResume() {

        //update the view
        super.onResume()
        if (!alarmSwitchState){
            updateRecyclerView(false)
        }else{
            updateRecyclerView(true)
        }
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
        if (!alarmSwitchState){
            updateRecyclerView(false)
        }else{
            updateRecyclerView(true)
        }


    }



    override fun onBackPressed() {
    }





    private fun updateRecyclerView(activeAlarms:Boolean) {
            GlobalScope.launch(Dispatchers.Main) {
                if (activeAlarms){
                binding.alarmRecycleView.adapter =
                    AlarmAdapter(presenter.getActiveAlarms(),this@AlarmListView)
                }else{
                    binding.alarmRecycleView.adapter =
                        AlarmAdapter(presenter.getAlarms(),this@AlarmListView)
                }
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