package com.example.myapplication

import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.androidtask.R
import com.example.androidtask.databinding.ActivityMapsBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.myapplication.models.Tracker
import com.example.myapplication.receivera.CallReceiver
import com.example.truptiassignment.API.RetrofitHelper
import com.example.truptiassignment.API.RocketService
import com.example.truptiassignment.repository.MainViewModelFactory
import com.example.truptiassignment.repository.RocketRepository
import com.example.truptiassignment.viewmodels.MainViewModel
import com.google.android.gms.maps.model.*
import java.util.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var mainViewModel: MainViewModel
    private var callReceiver: CallReceiver? = null
    val latLngList = ArrayList<LatLng>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if ((ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.READ_PHONE_STATE
                ),
                369
            )
        }

        val rocketService= RetrofitHelper.getInstance().create(RocketService::class.java)
        val respository= RocketRepository(rocketService)
        mainViewModel=
            ViewModelProvider(this, MainViewModelFactory(respository)).get(MainViewModel::class.java)

    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(callReceiver)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mainViewModel.rockets.observe(this) { mDeveloperModel ->
            for (tracker in mDeveloperModel.data[0]?.tracker ?: emptyList()) {
                val latLng = LatLng(tracker.latitude, tracker.longitude)
                latLngList.add(latLng)

                Log.e("Trupti", "Latitude: ${latLng.latitude}, Longitude: ${latLng.longitude}")
                mMap.addMarker(MarkerOptions().position(latLng).title("Marker"))
                val cameraPosition = CameraPosition.Builder()
                    .target(latLng) // set the target location to animate to
                    .zoom(12f) // set the zoom level
                    .build()
                val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
                mMap.animateCamera(cameraUpdate)

                val polylineOptions = PolylineOptions()
                polylineOptions.addAll(latLngList)
                polylineOptions.width(5f)
                polylineOptions.color(Color.RED)
                mMap.addPolyline(polylineOptions)

            }
        }

    }
}