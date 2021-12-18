package com.olamachia.maptrackerweekeighttask

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MapsActivity2 : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mapFrag: SupportMapFragment
    private lateinit var mLocationRequest: LocationRequest
    var mCurrLocationMarker: Marker? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    var fireBaseReference  = FirebaseDatabase.getInstance()
    private lateinit var reference : DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps2)

        supportActionBar!!.title = "Dennis Maps"
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapFrag = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFrag!!.getMapAsync(this)

        reference=  fireBaseReference.getReference("Seun")
        reference = Firebase.database.reference
        reference.addValueEventListener(partnerCallback)

    }


    override fun onStop() {
        super.onStop()
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }




    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 30000 // two minute interval
        mLocationRequest!!.fastestInterval = 20000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

//                    mFusedLocationClient.lastLocation.addOnCompleteListener {
//
//                    }
                //Location Permission already granted
                mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest!!, mLocationCallback,
                    Looper.myLooper()!!)
                mGoogleMap!!.isMyLocationEnabled = true
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest!!, mLocationCallback,
                Looper.myLooper()!!
            )
            mGoogleMap!!.isMyLocationEnabled = true
        }
    }



    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            val location = locationResult.lastLocation

                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker!!.remove()
                 }

                //move map camera
                val dennisLocation = LatLng(location.latitude, location.longitude)

            fireBaseReference.reference.child("Dennis").setValue(dennisLocation)

            //create a marker at the exact location

            mGoogleMap.addMarker(
                MarkerOptions().position(dennisLocation)
                .title(" I am currently here"))!!
                .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_1dennis))

                val cameraPosition =
                    CameraPosition.Builder().target(LatLng(dennisLocation.latitude, dennisLocation.longitude))
                        .zoom(17f).build()
                mGoogleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))



         }
    }



    private var partnerCallback = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if(snapshot.exists()){
                //get the exact longitude and latitude from the database "test"
                val seunLocation = snapshot.child("Seun").getValue(LocationInfo::class.java)

                val seunLatitude = seunLocation!!.latitude
                val seunLongitude = seunLocation.longitude
                //trigger reading of location from database using the button

                    // check if the latitude and longitude is not null
                    if (seunLatitude != null && seunLongitude != null){
                        // create a LatLng object from location
                        val latLng = LatLng(seunLatitude, seunLongitude)

                        //create a marker at the read location and display it on the map
                        mGoogleMap.clear()
                        mGoogleMap.addMarker(MarkerOptions().position(latLng)
                            .title("Your partner is here"))
                        //specify how the map camera is updated

                        val update = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
                        //update the camera with the CameraUpdate object
                        mGoogleMap.moveCamera(update)

                    } else {
                        // if location is null , log an error message
                        Log.e(TAG, "Your partner's location cannot be found")
                    }

            }
        }

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(applicationContext, "Could not read from the database", Toast.LENGTH_SHORT).show()
        }
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { dialogInterface, i -> //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this@MapsActivity2,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create()
                    .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        mFusedLocationClient!!.requestLocationUpdates(
                            mLocationRequest!!,
                            mLocationCallback, Looper.myLooper()!!
                        )
                        mGoogleMap!!.isMyLocationEnabled = true
                    }
                } else {
                    // if not allow a permission, the application will exit
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()

                }
            }
        }
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}