package fr.isen.nathangorga.androidsmartdevice

import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
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

    private val SCAN_PERIOD: Long = 10000 // Stop scan after 10 seconds
    private var scanning = false

    private val REQUEST_CODE_PERMISSIONS = 1
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var deviceListRecyclerView: RecyclerView
    private lateinit var scanButton: ImageButton

    // Real BLE scan callback
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            val device = result.device
            // Log the device information for debugging
            Log.d("ScanActivity", "Found device: ${device.address}, name: ${device.name}")
            val deviceName = device.name ?: "Unknown Device"
            val deviceAddress = device.address

            // Only add the device if it's not already in the list
            if (!scanResults.any { it.macAddress == deviceAddress }) {
                scanResults.add(Device(deviceName, deviceAddress))
                deviceListAdapter.notifyDataSetChanged()  // Update the list UI
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(this@ScanActivity, "Scan failed with code: $errorCode", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan) // Use the XML layout

        // Initialize views
        deviceListRecyclerView = findViewById(R.id.deviceList)
        scanButton = findViewById(R.id.scanButton)
        deviceListRecyclerView.layoutManager = LinearLayoutManager(this)
        deviceListAdapter = DeviceListAdapter(scanResults)
        deviceListRecyclerView.adapter = deviceListAdapter

        // Initialize BluetoothAdapter using BluetoothManager
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        // Check permissions
        if (!hasPermissions()) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up button click listener
        scanButton.setOnClickListener {
            toggleScan()
        }
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
        if (scanning) return  // Prevent double scanning

        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner ?: return

        // Clear previous results
        scanResults.clear()
        deviceListAdapter.notifyDataSetChanged()

        val scanFilters = listOf<ScanFilter>() // No specific filters
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
            scanning = true
            scanButton.setImageResource(R.drawable.bouton_pause)
            Toast.makeText(this, "Scanning in progress...", Toast.LENGTH_SHORT).show()

            // Automatically stop the scan after the scan period
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
