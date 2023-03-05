package com.example.myapplication

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.androidtask.R
import com.example.androidtask.models.MapData

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
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import java.util.ArrayList
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var originLatitude: Double = 28.5021359
    private var originLongitude: Double = 77.4054901
    private var destinationLatitude: Double = 18.5444812
    private var destinationLongitude: Double = 73.9114792

    lateinit var mainViewModel: MainViewModel
    var latLngList = ArrayList<LatLng>()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)

        // Fetching API_KEY which we wrapped
        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val apiKey = value.toString()
        // Initializing the Places API with the help of our API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        // Map Fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val rocketService= RetrofitHelper.getInstance().create(RocketService::class.java)
        val respository= RocketRepository(rocketService)
        mainViewModel=
            ViewModelProvider(this, MainViewModelFactory(respository)).get(MainViewModel::class.java)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mainViewModel.rockets.observe(this) { mDeveloperModel ->
            latLngList = mutableListOf<LatLng>() as ArrayList<LatLng>
            for (tracker in mDeveloperModel.data[0]?.tracker ?: emptyList()) {
                val latLng = LatLng(tracker.latitude, tracker.longitude)
                latLngList.add(latLng)
                val ai: ApplicationInfo = applicationContext.packageManager
                    .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
                val value = ai.metaData["com.google.android.geo.API_KEY"]
                val apiKey = value.toString()

                    val originLocation = LatLng(tracker.latitude, tracker.longitude)
                    mMap.addMarker(MarkerOptions().position(originLocation))
                    val destinationLocation = LatLng(destinationLatitude, destinationLongitude)
                    mMap.addMarker(MarkerOptions().position(destinationLocation))
                    val urll = getDirectionURL(originLocation, destinationLocation, apiKey)
                    GetDirection(urll).execute()
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 14F))

                Log.e("Trupti", "Latitude: ${latLng.latitude}, Longitude: ${latLng.longitude}")
                mMap.addMarker(MarkerOptions().position(latLng).title("Marker"))
                val cameraPosition = CameraPosition.Builder()
                    .target(latLng) // set the target location to animate to
                    .zoom(12f) // set the zoom level
                    .build()
                val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
                mMap.animateCamera(cameraUpdate)
            }
          /*  val polylineOptions = PolylineOptions()
                .addAll(latLngList)
                .width(10f)
                .color(Color.BLUE)
                .geodesic(true)
            mMap.addPolyline(polylineOptions)*/

        }
    }
    private fun getDirectionURL(origin:LatLng, dest:LatLng, secret: String) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=$secret"
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data, MapData::class.java)
                val path =  ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }
    }

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }
}