package com.example.locationtracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class MainActivity : AppCompatActivity() {

    // Reference to the map view to display the map as well as managing location providers
    // listening for location updates and a button to recenter the map to your location.

    private lateinit var map: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var recenterButton: Button
    private var shouldCenterMap = true
    private val LOCATION_PERMISSION_REQUEST = 1000

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ensures that requests to the server are identifiable as coming from my app.
        //this makes the app adhere to usage policies
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_main)

        //initialize views and location manager
        map = findViewById(R.id.map)
        recenterButton = findViewById(R.id.recenterButton)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // defining what to do on location changes. The challenge has the app periodically displaying
        //the location on the app. So I wanted to update the map center to the location constantly
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (shouldCenterMap) {
                    map.controller.setCenter(GeoPoint(location.latitude, location.longitude))
                }
            }

        }

        //defines our recenter button and its functionality
        recenterButton.setOnClickListener {
            shouldCenterMap = true
            val lastKnownLocation =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastKnownLocation?.let {
                map.controller.setCenter(GeoPoint(it.latitude, it.longitude))
            }
        }

        //initializing map settings and checking/requesting location permissions upon first launch
        initializeMap()
        requestLocationPermission()
    }

    private fun initializeMap() {

        //map settings and initial view settings
        //I did not like how the map was repetitive so found a way to disable it
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.setHorizontalMapRepetitionEnabled(false)
        map.setVerticalMapRepetitionEnabled(false)
        map.minZoomLevel = 5.0
        map.maxZoomLevel = 20.0
        map.controller.setZoom(9.0)
        shouldCenterMap = true
    }

    //check for location permission and request if it is not granted. If it's already granted request updates
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0L, 0f, locationListener
            )
        }
    }

    @SuppressLint("MissingSuperCall", "MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        //handle location permission result
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0L, 0f, locationListener
            )
        } else {

        }
    }

    //resume and pause map view when activity is resumed/paused
    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    //remove location updates when activity is destroyed for performance reasons
    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
    }
}
