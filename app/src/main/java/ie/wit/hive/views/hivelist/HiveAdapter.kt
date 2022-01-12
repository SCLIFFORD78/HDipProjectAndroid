package ie.wit.hive.adapters

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.wit.hive.databinding.CardHiveBinding
import ie.wit.hive.models.HiveModel

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
            binding.hiveTitle.text = hive.tag.toString()
            binding.type.text = hive.type
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
