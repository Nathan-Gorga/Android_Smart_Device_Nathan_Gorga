package fr.isen.nathangorga.androidsmartdevice

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScanActivity : ComponentActivity() {
    private lateinit var deviceListRecyclerView: RecyclerView
    private lateinit var scanButton: ImageButton
    private var isScanning = false // État du scan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan) // Utilisation du layout XML

        // Initialiser les vues
        deviceListRecyclerView = findViewById(R.id.deviceList)
        scanButton = findViewById(R.id.scanButton)

        // Configure RecyclerView pour afficher la liste des appareils (fausse liste)
        deviceListRecyclerView.layoutManager = LinearLayoutManager(this)
        deviceListRecyclerView.adapter = DeviceListAdapter(generateFakeDeviceList()) // Adapter avec une fausse liste

        // Clic sur le bouton pour lancer/arrêter le scan
        scanButton.setOnClickListener {
            toggleScan() // Lance ou arrête le scan
        }
    }

    private fun toggleScan() {
        if (isScanning) {
            // Arrêter le scan
            isScanning = false
            Toast.makeText(this, "Scan arrêté", Toast.LENGTH_SHORT).show()
            scanButton.setImageResource(R.drawable.bouton_play) // Icône Play
            deviceListRecyclerView.visibility = View.GONE // Cacher la liste
        } else {
            // Démarrer le scan
            isScanning = true
            Toast.makeText(this, "Scan démarré", Toast.LENGTH_SHORT).show()
            scanButton.setImageResource(R.drawable.bouton_pause) // Icône Pause
            deviceListRecyclerView.visibility = View.VISIBLE // Afficher la liste
        }
    }

    // Fonction pour générer une fausse liste d'appareils détectés
    private fun generateFakeDeviceList(): List<Device> {
        return listOf(
            Device("Appareil 1", "00:11:22:33:44:55"),
            Device("Appareil 2", "66:77:88:99:00:11"),
            Device("Appareil 3", "22:33:44:55:66:77")
        )
    }
}
