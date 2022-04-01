package ie.wit.hive.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.wit.hive.databinding.CardHiveBinding
import ie.wit.hive.models.HiveModel
import ie.wit.hive.weather.getWeather
import kotlinx.coroutines.runBlocking

interface HiveListener {
    fun onHiveClick(hive: HiveModel)
}

class HiveAdapter constructor(private var hives: List<HiveModel>,
                                   private val listener: HiveListener) :
        RecyclerView.Adapter<HiveAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardHiveBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val hive = hives[holder.adapterPosition]
        holder.bind(hive, listener)

    }

    override fun getItemCount(): Int = hives.size

    class MainHolder(private val binding : CardHiveBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(hive: HiveModel, listener: HiveListener) {

            var weather = runBlocking { getWeather(hive.location.lat, hive.location.lng) }
            binding.hiveTitle.text = hive.tag.toString()
            binding.type.text = hive.type
            binding.temp.text = "%.2f".format(weather["temp"])+"\u00B0"+"C"
            binding.hum.text = "%.2f".format(weather["humidity"])+"%"
            if (hive.sensorNumber != ""){
                binding.blueToothIcon.visibility = View.VISIBLE
            }
            if (hive.image != ""){
                Picasso.get()
                    .load(hive.image)
                    .resize(200, 200)
                    .into(binding.imageIcon)
            }
            binding.root.setOnClickListener { listener.onHiveClick(hive) }
        }
    }
}
