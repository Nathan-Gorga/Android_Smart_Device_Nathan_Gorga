package fr.isen.nathangorga.androidsmartdevice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Device(val name: String, val macAddress: String)

class DeviceListAdapter(private val deviceList: List<Device>) : RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>() {

    // Création d'une vue pour chaque élément de la liste
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return DeviceViewHolder(view)
    }

    // Lier les données (nom et adresse MAC) à chaque vue
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]
        holder.nameTextView.text = device.name
        holder.macAddressTextView.text = device.macAddress
    }

    // Nombre d'éléments dans la liste
    override fun getItemCount(): Int {
        return deviceList.size
    }

    // ViewHolder pour lier les vues aux éléments de la liste
    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(android.R.id.text1)
        val macAddressTextView: TextView = view.findViewById(android.R.id.text2)
    }
}
