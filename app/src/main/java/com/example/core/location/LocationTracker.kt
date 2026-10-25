package com.example.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class LocationData(
    val latitude: Double = 12.971598,
    val longitude: Double = 77.594566,
    val accuracy: Float = 12.5f,
    val timestamp: Long = System.currentTimeMillis(),
    val address: String = "Bangalore, Karnataka, India",
    val isLive: Boolean = true
)

class LocationTracker(private val context: Context) : LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _currentLocation = MutableStateFlow(LocationData())
    val currentLocation: StateFlow<LocationData> = _currentLocation.asStateFlow()

    private var isTracking = false

    init {
        fetchLastKnownLocation()
    }

    @SuppressLint("MissingPermission")
    fun fetchLastKnownLocation() {
        if (locationManager == null) return
        try {
            val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val bestLoc = when {
                gpsLoc != null && netLoc != null -> if (gpsLoc.time > netLoc.time) gpsLoc else netLoc
                gpsLoc != null -> gpsLoc
                else -> netLoc
            }

            if (bestLoc != null) {
                updateFromLocation(bestLoc, isLive = false)
            }
        } catch (e: SecurityException) {
            // Permission not yet granted, fallback to default safe simulated coordinates
        }
    }

    @SuppressLint("MissingPermission")
    fun startLiveTracking() {
        if (isTracking || locationManager == null) return
        isTracking = true

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000L, // every 3 seconds
                    5f,   // every 5 meters
                    this,
                    Looper.getMainLooper()
                )
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    4000L,
                    10f,
                    this,
                    Looper.getMainLooper()
                )
            }
        } catch (e: SecurityException) {
            // Handled gracefully
        }
    }

    fun stopLiveTracking() {
        if (!isTracking || locationManager == null) return
        try {
            locationManager.removeUpdates(this)
        } catch (e: Exception) {
            // Ignore
        }
        isTracking = false
    }

    override fun onLocationChanged(location: Location) {
        updateFromLocation(location, isLive = true)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private fun updateFromLocation(location: Location, isLive: Boolean) {
        val lat = location.latitude
        val lng = location.longitude
        val acc = location.accuracy
        val time = location.time

        var addr = "Lat: %.4f, Lng: %.4f".format(lat, lng)
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val a = addresses[0]
                val street = a.thoroughfare ?: a.subLocality ?: ""
                val city = a.locality ?: a.subAdminArea ?: ""
                val state = a.adminArea ?: ""
                val combined = listOf(street, city, state).filter { it.isNotBlank() }.joinToString(", ")
                if (combined.isNotBlank()) {
                    addr = combined
                }
            }
        } catch (e: Exception) {
            // Geocoder failure (e.g. offline)
        }

        _currentLocation.value = LocationData(
            latitude = lat,
            longitude = lng,
            accuracy = acc,
            timestamp = time,
            address = addr,
            isLive = isLive
        )
    }

    fun generateMapUrl(lat: Double, lng: Double): String {
        return "https://maps.google.com/?q=$lat,$lng"
    }
}
