package com.cs407.lab4_milestone2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = LocationListener { location ->
            updateLocationInfo(location)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            startLocationUpdates()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0L,
            0f,
            locationListener
        )
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
                val addressComponents = listOfNotNull(
                    address.subThoroughfare?.trim(),
                    address.thoroughfare?.trim(),
                    address.locality?.trim(),
                    address.postalCode?.trim(),
                    address.countryName?.trim()
                )
                val fullAddressText = addressComponents.joinToString(separator = ", ")
                findViewById<TextView>(R.id.addr).text = fullAddressText
            } else {
                findViewById<TextView>(R.id.addr).text = "Could not find address"
            }
        }
    }
}
