package fr.isen.nathangorga.androidsmartdevice

import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScanActivity : ComponentActivity() {
    // Bluetooth components
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val handler = Handler(Looper.getMainLooper())

    // List to hold scanned devices
    private val scanResults = mutableListOf<android.bluetooth.BluetoothDevice>()
    private lateinit var deviceListAdapter: DeviceListAdapter

    private val SCAN_PERIOD: Long = 10000 // Stop scan after 10 seconds
    private var scanning = false

    // Permissions
    private val REQUEST_CODE_PERMISSIONS = 1
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // UI components
    private lateinit var deviceListRecyclerView: RecyclerView
    private lateinit var scanButton: ImageButton

    // Scan callback that updates the list of devices
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            val device = result.device
            Log.d("ScanActivity", "Found device: ${device.address}, name: ${device.name}")
            // Use a default name if null
            if (!scanResults.any { it.address == device.address }) {
                scanResults.add(device)
                deviceListAdapter.notifyDataSetChanged()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(this@ScanActivity, "Scan failed with code: $errorCode", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        // Initialize UI components
        deviceListRecyclerView = findViewById(R.id.deviceList)
        scanButton = findViewById(R.id.scanButton)
        deviceListRecyclerView.layoutManager = LinearLayoutManager(this)
        // Adapter with click handling: when a device is clicked, go to DeviceActivity
        deviceListAdapter = DeviceListAdapter(scanResults) { selectedDevice ->
            onDeviceSelected(selectedDevice)
        }
        deviceListRecyclerView.adapter = deviceListAdapter

        // Initialize BluetoothAdapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        // Check runtime permissions
        if (!hasPermissions()) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the scan button click listener
        scanButton.setOnClickListener {
            toggleScan()
        }
    }

    @SuppressLint("MissingPermission")
    private fun onDeviceSelected(device: android.bluetooth.BluetoothDevice) {
        Toast.makeText(this, "Connexion à ${device.name ?: "l'appareil"}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, DeviceActivity::class.java).apply {
            putExtra("DEVICE_NAME", device.name ?: "Appareil inconnu")
            putExtra("DEVICE_ADDRESS", device.address)
        }
        startActivity(intent)
    }

    private fun toggleScan() {
        if (!isBluetoothAvailable()) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            return
        }
        if (!isBluetoothEnabled()) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_LONG).show()
            return
        }
        if (!hasPermissions()) {
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
        if (scanning) return

        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner ?: return

        // Clear previous scan results
        scanResults.clear()
        deviceListAdapter.notifyDataSetChanged()

        val scanFilters = listOf<ScanFilter>()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
            scanning = true
            scanButton.setImageResource(R.drawable.bouton_pause)
            Toast.makeText(this, "Scanning in progress...", Toast.LENGTH_SHORT).show()

            // Stop the scan automatically after SCAN_PERIOD milliseconds
            handler.postDelayed({
                stopBleScan()
            }, SCAN_PERIOD)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        if (!scanning) return

        bluetoothLeScanner.stopScan(scanCallback)
        scanning = false
        scanButton.setImageResource(R.drawable.bouton_play)
        Toast.makeText(this, "Scan stopped", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth permissions are required for scanning", Toast.LENGTH_LONG).show()
            }
        }
    }
}
