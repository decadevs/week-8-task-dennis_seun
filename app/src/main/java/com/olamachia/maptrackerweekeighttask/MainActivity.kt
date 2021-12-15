package com.olamachia.maptrackerweekeighttask

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.olamachia.maptrackerweekeighttask.databinding.ActivityMainBinding
import com.olamachia.maptrackerweekeighttask.utils.snackbar
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var locationManager: LocationManager
    private lateinit var location: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.getLocationBtn.setOnClickListener { checkLocationAccessPermission() }

        binding.showMapLocationBtn.setOnClickListener {
            startActivity(Intent(applicationContext, MapsActivity::class.java))
        }

//        binding.showPartnerLocationBtn.setOnClickListener {
//            startActivity(Intent(applicationContext, GetMapsLocationActivity::class.java))
//        }
    }

    private fun getLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                111
            )
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!
        }

        val locationListener = LocationListener { location -> reverseGeolocationCode(location) }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1.2f, locationListener)
    }

    private fun checkLocationAccessPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationUpdates()
        } else {
            requestLocationAccessPermission()
        }
    }

    private fun requestLocationAccessPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            binding.root.snackbar(R.string.location_access_required.toString())
            requestPermissionsCompat(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION_ACCESS)
        } else {
            requestPermissionsCompat(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION_ACCESS)
        }
    }

    private fun requestPermissionsCompat(permissionsArray: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(
            this,
            permissionsArray,
            requestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_LOCATION_ACCESS){
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.root.snackbar(R.string.permission_granted.toString())
                requestLocationAccessPermission()
            } else{
                binding.root.snackbar(R.string.permission_denied.toString())
            }
        }
    }

    private fun reverseGeolocationCode(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 2)
        val address = addresses[0]
        "Current location of your device is: \n${address.getAddressLine(0)}\n${address.locality } \nLatitude: ${address.latitude} \nLongitude: ${address.longitude}".also { binding.currentLocation.text = it }
    }

    companion object{
        private const val PERMISSION_REQUEST_LOCATION_ACCESS = 0
    }
}
