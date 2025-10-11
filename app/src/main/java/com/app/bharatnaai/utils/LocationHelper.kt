package com.app.bharatnaai.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val geocoder: Geocoder by lazy {
        Geocoder(context, Locale.getDefault())
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check if location services are enabled
     */
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Get current location using FusedLocationProviderClient
     */
    suspend fun getCurrentLocation(): LocationResult = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermissions()) {
            continuation.resume(LocationResult.Error("Location permissions not granted"))
            return@suspendCancellableCoroutine
        }

        if (!isLocationEnabled()) {
            continuation.resume(LocationResult.Error("Location services are disabled"))
            return@suspendCancellableCoroutine
        }

        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(15000)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    result.lastLocation?.let { location ->
                        fusedLocationClient.removeLocationUpdates(this)
                        continuation.resume(LocationResult.Success(location))
                    } ?: continuation.resume(LocationResult.Error("Unable to get location"))
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            // Also try to get last known location as fallback
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null && !continuation.isCompleted) {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    continuation.resume(LocationResult.Success(location))
                }
            }.addOnFailureListener { exception ->
                if (!continuation.isCompleted) {
                    continuation.resume(LocationResult.Error("Failed to get location: ${exception.message}"))
                }
            }

            // Set up cancellation
            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }

        } catch (e: SecurityException) {
            continuation.resume(LocationResult.Error("Security exception: ${e.message}"))
        } catch (e: Exception) {
            continuation.resume(LocationResult.Error("Error getting location: ${e.message}"))
        }
    }

    /**
     * Get address from location coordinates
     */
    suspend fun getAddressFromLocation(location: Location): AddressResult = suspendCancellableCoroutine { continuation ->
        try {
            if (Geocoder.isPresent()) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val locationName = when {
                            !address.locality.isNullOrEmpty() -> address.locality
                            !address.subAdminArea.isNullOrEmpty() -> address.subAdminArea
                            !address.adminArea.isNullOrEmpty() -> address.adminArea
                            !address.countryName.isNullOrEmpty() -> address.countryName
                            else -> "Unknown Location"
                        }
                        continuation.resume(AddressResult.Success(locationName, address))
                    } else {
                        continuation.resume(AddressResult.Error("No address found"))
                    }
                }
            } else {
                continuation.resume(AddressResult.Error("Geocoder not available"))
            }
        } catch (e: Exception) {
            continuation.resume(AddressResult.Error("Error getting address: ${e.message}"))
        }
    }

    /**
     * Get current location with address
     */
    suspend fun getCurrentLocationWithAddress(): LocationWithAddressResult {
        return when (val locationResult = getCurrentLocation()) {
            is LocationResult.Success -> {
                when (val addressResult = getAddressFromLocation(locationResult.location)) {
                    is AddressResult.Success -> {
                        LocationWithAddressResult.Success(
                            location = locationResult.location,
                            locationName = addressResult.locationName,
                            address = addressResult.address
                        )
                    }
                    is AddressResult.Error -> {
                        LocationWithAddressResult.Error(addressResult.message)
                    }
                }
            }
            is LocationResult.Error -> {
                LocationWithAddressResult.Error(locationResult.message)
            }
        }
    }
}

/**
 * Sealed classes for different result types
 */
sealed class LocationResult {
    data class Success(val location: Location) : LocationResult()
    data class Error(val message: String) : LocationResult()
}

sealed class AddressResult {
    data class Success(val locationName: String, val address: Address) : AddressResult()
    data class Error(val message: String) : AddressResult()
}

sealed class LocationWithAddressResult {
    data class Success(
        val location: Location,
        val locationName: String,
        val address: Address
    ) : LocationWithAddressResult()
    data class Error(val message: String) : LocationWithAddressResult()
}
