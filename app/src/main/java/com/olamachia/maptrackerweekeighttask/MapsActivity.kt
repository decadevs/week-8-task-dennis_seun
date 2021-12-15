package com.olamachia.maptrackerweekeighttask

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.olamachia.maptrackerweekeighttask.databinding.ActivityMapsBinding
import com.olamachia.maptrackerweekeighttask.models.LocationInfo

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var myMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var reference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        reference = database.getReference("Dennis")
        reference = Firebase.database.reference
        reference.addValueEventListener(locationListener)

        setupLocationClient()
    }

    private val locationListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if(snapshot.exists()){
                //get the exact longitude and latitude from the database "test"
                val partnerLocation = snapshot.child("Dennis").getValue(LocationInfo::class.java)
                val partnerLocationLatitude = partnerLocation!!.latitude
                val partnerLocationLongitude = partnerLocation.longitude

                //trigger reading of location from database using the button
                binding.findLocationBtn.setOnClickListener {

                    // check if the latitude and longitude is not null
                    if (partnerLocationLatitude != null && partnerLocationLongitude != null){
                        // create a LatLng object from location
                        val latLng = LatLng(partnerLocationLatitude, partnerLocationLongitude)
                        //create a marker at the read location and display it on the map
                        myMap.addMarker(MarkerOptions().position(latLng)
                            .title("Dennis is here"))
                        //specify how the map camera is updated
                        val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                        //update the camera with the CameraUpdate object
                        myMap.moveCamera(update)

                    } else {
                        // if location is null , log an error message
                        Log.e(TAG, "Your partner's location cannot be found")
                    }
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(applicationContext, "Could not read from the database", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        getCurrentLocation()

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        myMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        myMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            requestLocationPermission()
        } else{
            fusedLocationClient.lastLocation.addOnCompleteListener {
                // lastLocation is a task running in the background
                val location = it.result

                reference = database.getReference("Seun")

                if (location != null){
                    val locationCoordinates = LatLng(location.latitude, location.longitude)

                    //create a marker at the exact location
                    myMap.addMarker(MarkerOptions().position(locationCoordinates)
                        .title("Seun is currently here"))!!
                        .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_seun))

                    // create an object that will specify how the camera will be updated
                    val update = CameraUpdateFactory.newLatLngZoom(locationCoordinates, 16.0F)
                    myMap.moveCamera(update)

                    //Save the location data to the database
                    reference.setValue(locationCoordinates)
                } else{
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION){
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            } else{
                Log.e(TAG, "Location permission has been denied")
            }
        }
    }

    companion object{
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }
}