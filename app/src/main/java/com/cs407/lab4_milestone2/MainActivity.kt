package com.cs407.lab4_milestone2

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var gpsLocationManager: LocationManager
    private lateinit var gpsLocationListener: LocationListener

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBarInsets.left, systemBarInsets.top, systemBarInsets.right, systemBarInsets.bottom)
            windowInsets
        }

        gpsLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsLocationListener = LocationListener { currentLocation: Location ->
            updateLocationInfo(currentLocation)
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            startListening()
            gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, gpsLocationListener)
            val lastKnownLocation: Location? = gpsLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                updateLocationInfo(lastKnownLocation)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening()
            }
        }
    }

    private fun startListening() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gpsLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                gpsLocationListener
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun updateLocationInfo(currentLocation: Location) {
        val latitudeTextView = findViewById<TextView>(R.id.lat)
        val longitudeTextView = findViewById<TextView>(R.id.lon)
        val altitudeTextView = findViewById<TextView>(R.id.alt)
        val accuracyTextView = findViewById<TextView>(R.id.acc)
        latitudeTextView.text = "Latitude: ${currentLocation.latitude}"
        longitudeTextView.text = "Longitude: ${currentLocation.longitude}"
        altitudeTextView.text = "Altitude: ${currentLocation.altitude}"
        accuracyTextView.text = "Accuracy: ${currentLocation.accuracy}"

        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        geocoder.getFromLocation(
            currentLocation.latitude,
            currentLocation.longitude,
            1
        ) { addressList ->
            if (addressList.isNotEmpty()) {
                val address = addressList[0]
                var fullAddressText = getString(R.string.address)
                if (address.subThoroughfare != null) fullAddressText += "${address.subThoroughfare} "
                if (address.thoroughfare != null) fullAddressText += "${address.thoroughfare}\n"
                if (address.locality != null) fullAddressText += "${address.locality}\n"
                if (address.postalCode != null) fullAddressText += "${address.postalCode}\n"
                if (address.countryName != null) fullAddressText += "${address.countryName}"
                findViewById<TextView>(R.id.addr).text = fullAddressText
            } else {
                findViewById<TextView>(R.id.addr).text = "Could not find address"
            }
        }
    }
}
