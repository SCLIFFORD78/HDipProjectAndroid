package ie.wit.hive.views.alarmlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.wit.hive.adapters.HiveListener
import ie.wit.hive.databinding.CardAlarmBinding
import ie.wit.hive.databinding.CardHiveBinding
import ie.wit.hive.models.AlarmEvents
import ie.wit.hive.models.HiveModel
import ie.wit.hive.weather.getWeather
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat

interface AlarmListener {
    fun onAlarmClick(alarm: AlarmEvents)
}

class AlarmAdapter constructor(private var alarms: List<AlarmEvents>,
                               private val listener: AlarmListener
) :
        RecyclerView.Adapter<AlarmAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardAlarmBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val alarm = alarms[holder.adapterPosition]

        holder.bind(alarm, listener)

    }

    override fun getItemCount(): Int = alarms.size

    class MainHolder(private val binding : CardAlarmBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: AlarmEvents, listener: AlarmListener) {

            binding.alarmDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(alarm.dateActive.toLong()*1000)
            binding.alarmRecordedTime.text = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(alarm.dateLogged.toLong()*1000)
            binding.recordedTemp.text = "%.2f".format(alarm.recordedValue)+"\u00B0"+"C"
            binding.tempSetPoint.text = "%.2f".format(alarm.tempAlarm)+"\u00B0"+"C"

            if (alarm.act){
                binding.alarmAck.visibility = View.VISIBLE
                binding.notAck.visibility = View.INVISIBLE
            }else{
                binding.alarmAck.visibility = View.INVISIBLE
                binding.notAck.visibility = View.VISIBLE
            }
            binding.root.setOnClickListener { listener.onAlarmClick(alarm) }

        }
    }
}
