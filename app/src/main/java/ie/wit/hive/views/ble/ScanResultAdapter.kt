package ie.wit.hive.adapters

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.wit.hive.databinding.CardHiveBinding
import ie.wit.hive.databinding.RowScanResultBinding
import ie.wit.hive.models.HiveModel



class ScanResultAdapter constructor(private val items: List<ScanResult>,
                                    private val onClickListener: ((device: ScanResult) -> Unit)) :
    RecyclerView.Adapter<ScanResultAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = RowScanResultBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val item = items[holder.adapterPosition]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    class MainHolder(private val binding : RowScanResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingPermission")
        fun bind(result: ScanResult) {
            binding.deviceName.text = result.device.name ?: "Unnamed"
            binding.macAddress.text = result.device.address
            binding.signalStrength.text = "${result.rssi} dBm"
            binding.root.setOnClickListener { result }
        }
    }
}
