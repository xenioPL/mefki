package com.mefki.mainactivity.maps

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mefki.mainactivity.R
import com.mefki.mainactivity.datasource.APIImpl
import io.reactivex.disposables.Disposable
import kotlin.math.sqrt

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = MapsActivity::class.java.simpleName
    private lateinit var mMap: GoogleMap

    // The entry point to the Fused Location Provider.
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var mLastKnownLocation: Location? = null

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val mDefaultLocation = LatLng(-33.8523341, 151.2106085)
    private val DEFAULT_ZOOM = 15
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mLocationPermissionGranted = false
    private val handler = Handler()
    private val interval : Long = 1
    private var getMarkers : Disposable? = null

    private var circle: Circle? = null

    private val mStatusChecker = object : Runnable {
        override fun run(){
            try {
                getDeviceLocation()
            } finally {
                handler.postDelayed(this, interval)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        getMarkers?.dispose()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.current_place_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_get_place) {

        }
        return true
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        getMarkers = APIImpl().getStationsLocalizations().subscribe { stationsList ->
            for(station in stationsList) {
                mMap.addMarker(
                    MarkerOptions()
                        .title("sample")
                        .position(LatLng(station.latitude.toDouble(),station.longitude.toDouble()))
                        .snippet("titties")
                )
            }
        }

        circle = mMap.addCircle(
            CircleOptions().apply {
                center(LatLng(0.0,0.0))
                radius(100.0)
                strokeWidth(0.0f)
                //strokeColor(strokeColorArgb)
                fillColor(0x503db4ff)
                //strokePattern(getSelectedPattern(strokePatternSpinner.selectedItemPosition))
            })

        map.uiSettings.isScrollGesturesEnabled = false
        map.uiSettings.isZoomGesturesEnabled = false

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            // Return null here, so that getInfoContents() is called next.
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                // Inflate the layouts for the info window, title and snippet.
                val infoWindow = layoutInflater.inflate(
                    R.layout.custom_info_contents,
                    findViewById<FrameLayout>(R.id.map), false
                )

                val title = infoWindow.findViewById(R.id.title) as TextView
                title.text = marker.title

                val snippet = infoWindow.findViewById(R.id.snippet) as TextView
                snippet.text = marker.snippet

                return infoWindow
            }
        })

        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
        mStatusChecker.run()
    }

    private fun getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient?.lastLocation
                locationResult?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        mLastKnownLocation = task.result
                        val zoomRadius = 900

                        val zoomFinal = when(zoomRadius){
                            in 0..200 -> 16
                            in 201..500 -> 15
                            in 501..950 -> 14
                            in 951..Int.MAX_VALUE ->13
                            else -> 12
                        }

                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    mLastKnownLocation!!.latitude,
                                    mLastKnownLocation!!.longitude
                                ), zoomFinal.toFloat()
                            )
                        )

                        circle?.center = LatLng(mLastKnownLocation!!.latitude, mLastKnownLocation!!.longitude)
                        circle?.radius = zoomRadius.toDouble()
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI() {
        try {
            mMap.uiSettings.isMyLocationButtonEnabled = false
            if (mLocationPermissionGranted) {
                mMap.isMyLocationEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mLastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }
}
