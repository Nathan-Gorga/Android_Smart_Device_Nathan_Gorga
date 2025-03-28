package fr.isen.nathangorga.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity

class DeviceActivity : ComponentActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private lateinit var deviceNameTextView: TextView
    private lateinit var statusTextView: TextView

    @SuppressLint("MissingPermission", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        // Initialisation des vues
        deviceNameTextView = findViewById(R.id.deviceName)
        statusTextView = findViewById(R.id.status)

        // Récupération des informations de l'appareil depuis l'intent
        val deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Appareil inconnu"
        val deviceAddress = intent.getStringExtra("DEVICE_ADDRESS")

        // Mise à jour de l'UI
        deviceNameTextView.text = "Connexion à : $deviceName"
        statusTextView.text = "Connexion BLE en cours..." // Ajout du message de connexion

        // Démarrer la connexion BLE
        if (deviceAddress != null) {
            val bluetoothAdapter =
                (getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager).adapter
            val device: BluetoothDevice? = bluetoothAdapter.getRemoteDevice(deviceAddress)

            if (device != null) {
                bluetoothGatt = device.connectGatt(this, false, gattCallback)
            } else {
                statusTextView.text = "Appareil non trouvé"
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            runOnUiThread {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    statusTextView.text = "Connecté à ${gatt?.device?.name ?: "l'appareil"}"
                    Log.d("DeviceActivity", "Connecté à ${gatt?.device?.address}")
                    gatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    statusTextView.text = "Déconnecté"
                    Log.d("DeviceActivity", "Déconnecté")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val services = gatt?.services ?: listOf()
                Log.d("DeviceActivity", "Services découverts: ${services.size}")
                services.forEach { service ->
                    Log.d("DeviceActivity", "Service UUID: ${service.uuid}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
    }
}
