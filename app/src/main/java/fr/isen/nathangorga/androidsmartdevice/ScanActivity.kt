package fr.isen.nathangorga.androidsmartdevice

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.provider.Settings


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
        if (isBluetoothAvailable()) {
            if (!isBluetoothEnabled()) {
                Toast.makeText(
                    this,
                    "Veuillez activer le Bluetooth pour scanner",
                    Toast.LENGTH_LONG
                ).show()
                return // Arrêter l'exécution si le Bluetooth est désactivé
            }

            if (isScanning) {
                // Arrêter le scan
                isScanning = false
                Toast.makeText(this, "Scan arrêté", Toast.LENGTH_SHORT).show()
                scanButton.setImageResource(R.drawable.bouton_play)
                deviceListRecyclerView.visibility = View.GONE // Cacher la liste
            } else {
                // Démarrer le scan
                isScanning = true
                Toast.makeText(this, "Scan démarré", Toast.LENGTH_SHORT).show()
                scanButton.setImageResource(R.drawable.bouton_pause)
                deviceListRecyclerView.visibility = View.VISIBLE // Afficher la liste
            }
        } else {
            Toast.makeText(
                this,
                "Le Bluetooth n'est pas disponible sur cet appareil",
                Toast.LENGTH_LONG
            ).show()
            return
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
    private fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter.isEnabled
    }
    private fun isBluetoothAvailable(): Boolean {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter != null
    }
}
