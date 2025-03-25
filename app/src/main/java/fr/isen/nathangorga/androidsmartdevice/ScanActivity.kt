package fr.isen.nathangorga.androidsmartdevice

import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ScanActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val handler = Handler(Looper.getMainLooper())

    private val scanResults = mutableListOf<Device>()
    private lateinit var deviceListAdapter: DeviceListAdapter

    private val SCAN_PERIOD: Long = 10000 // Arrêter le scan après 10s
    private var scanning = false

    private val REQUEST_CODE_PERMISSIONS = 1
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var deviceListRecyclerView: RecyclerView
    private lateinit var scanButton: ImageButton
    private var isScanning = false // État du scan
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            val device = result.device
            val deviceName = if (hasBluetoothPermissions()) {
                device.name ?: "Appareil inconnu"
            } else {
                "Nom non disponible"
            }
            val deviceAddress = device.address

            if (!scanResults.any { it.macAddress == deviceAddress }) {
                scanResults.add(Device(deviceName, deviceAddress))
                deviceListAdapter.notifyDataSetChanged()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(this@ScanActivity, "Scan échoué avec le code : $errorCode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        val bluetoothConnectPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        return bluetoothConnectPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan) // Utilisation du layout XML

        // Initialisation des vues
        deviceListRecyclerView = findViewById(R.id.deviceList)
        scanButton = findViewById(R.id.scanButton)

        // Initialisation du BluetoothAdapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Le Bluetooth n'est pas disponible sur cet appareil", Toast.LENGTH_SHORT).show()
            return
        }

        // Vérifier les permissions
        if (!hasPermissions()) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        } else {
            setupUI() // Initialiser l'interface si les permissions sont déjà accordées
        }

        // Configure RecyclerView pour afficher la liste des appareils
        deviceListRecyclerView.layoutManager = LinearLayoutManager(this)

        // Clic sur le bouton pour lancer/arrêter le scan
        scanButton.setOnClickListener {
            toggleScan() // Lance ou arrête le scan
        }
    }

    private fun toggleScan() {
        if (!isBluetoothAvailable()) {
            Toast.makeText(this, "Le Bluetooth n'est pas disponible", Toast.LENGTH_LONG).show()
            return
        }

        if (!isBluetoothEnabled()) {
            Toast.makeText(this, "Veuillez activer le Bluetooth", Toast.LENGTH_LONG).show()
            return
        }

        if (!hasPermissions()) {
            // Demander les permissions nécessaires si elles ne sont pas accordées
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            return
        }

        if (scanning) {
            stopBleScan()
        } else {
            startBleScan()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startBleScan() {
        if (scanning) return // Éviter les scans en double

        // Vérification des permissions avant de commencer le scan
        if (!hasPermissions()) {
            // Demander les permissions nécessaires si elles ne sont pas accordées
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            return
        }

        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner ?: return

        scanResults.clear()
        deviceListAdapter.notifyDataSetChanged()

        val scanFilters = listOf<ScanFilter>() // Aucun filtre spécifique
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
            scanning = true
            scanButton.setImageResource(R.drawable.bouton_pause)
            Toast.makeText(this, "Scan en cours...", Toast.LENGTH_SHORT).show()

            // Arrêter automatiquement le scan après SCAN_PERIOD
            handler.postDelayed({
                stopBleScan()
            }, SCAN_PERIOD)
        } catch (e: SecurityException) {
            // Gestion des erreurs de permission
            Toast.makeText(this, "Erreur de permission: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        if (!scanning) return

        bluetoothLeScanner.stopScan(scanCallback)
        scanning = false
        scanButton.setImageResource(R.drawable.bouton_play)
        Toast.makeText(this, "Scan arrêté", Toast.LENGTH_SHORT).show()
    }

    private fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    private fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null
    }

    private fun hasPermissions(): Boolean {
        val bluetoothScanPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
        val bluetoothConnectPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        return bluetoothScanPermission == PackageManager.PERMISSION_GRANTED &&
                bluetoothConnectPermission == PackageManager.PERMISSION_GRANTED &&
                locationPermission == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                setupUI() // Lancer l'interface car toutes les permissions sont accordées
            } else {
                Toast.makeText(this, "Permissions Bluetooth requises pour scanner", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupUI() {
        deviceListRecyclerView.layoutManager = LinearLayoutManager(this)
        deviceListAdapter = DeviceListAdapter(scanResults) // Adapter avec la liste dynamique
        deviceListRecyclerView.adapter = deviceListAdapter
    }
}
