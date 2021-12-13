package com.olamachia.maptrackerweekeighttask

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.olamachia.maptrackerweekeighttask.databinding.ActivityGetMapsLocationBinding
import com.olamachia.maptrackerweekeighttask.models.LocationInfo

class GetMapsLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var secondMap: GoogleMap
    private lateinit var binding: ActivityGetMapsLocationBinding

    private var database = FirebaseDatabase.getInstance()
    private var databaseReference = database.getReference("test")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGetMapsLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Get a reference from the database so that the app can read and write operations
        databaseReference = Firebase.database.reference
        databaseReference.addValueEventListener(locationListener)
    }

    private val locationListener = object : ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            if(snapshot.exists()){
                //get the exact longitude and latitude from the database "test"
                val partnerLocation = snapshot.child("test").getValue(LocationInfo::class.java)
                val partnerLocationLatitude = partnerLocation!!.latitude
                val partnerLocationLongitude = partnerLocation.longitude

                //trigger reading of location from database using the button
                binding.findLocationBtn.setOnClickListener {

                    // check if the latitude and longitude is not null
                    if (partnerLocationLatitude != null && partnerLocationLongitude != null){
                        // create a LatLng object from location
                        val latLng = LatLng(partnerLocationLatitude, partnerLocationLongitude)
                        //create a marker at the read location and display it on the map
                        secondMap.addMarker(MarkerOptions().position(latLng)
                            .title("Your partner is here"))
                        //specify how the map camera is updated
                        val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                        //update the camera with the CameraUpdate object
                        secondMap.moveCamera(update)

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
        secondMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        secondMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        secondMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    companion object{
        private const val TAG = "MapsActivity"
    }
}