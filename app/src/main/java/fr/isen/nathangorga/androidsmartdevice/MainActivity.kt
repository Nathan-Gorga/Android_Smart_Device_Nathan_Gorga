package fr.isen.nathangorga.androidsmartdevice

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main) // Utilisation du layout XML

        // Trouver le bouton "Lancer le scan"
        val scanButton = findViewById<Button>(R.id.scanButton)

        // Définir le listener sur le bouton pour démarrer ScanActivity
        scanButton.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent) // Démarrer ScanActivity
        }
    }
}
