package com.mefki.mainactivity.maps

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.mefki.mainactivity.R
import com.mefki.mainactivity.datasource.APIImpl
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_find_now_top.view.*
import kotlinx.android.synthetic.main.fragment_find_now_bottom_button.view.*
import android.content.Intent
import com.mefki.mainactivity.searchservice.SearchService


class MapsFragment: Fragment(), OnMapReadyCallback {

    private val TAG = MapsFragment::class.java.simpleName
    private lateinit var mMap: GoogleMap
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mLastKnownLocation: Location? = null
    private val mDefaultLocation = LatLng(-33.8523341, 151.2106085)
    private val DEFAULT_ZOOM = 15
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mLocationPermissionGranted = false
    private val handler = Handler()
    private val interval : Long = 1000
    private var getMarkers : Disposable? = null
    private var circle: Circle? = null
    private var locationResult: Task<Location>? = null
    private var searchBar : SeekBar? = null
    private var distance : TextView? = null
    private var time : TextView? = null
    private var radius = 500

    private lateinit var mView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_maps, container, false)
        mView.findNowButton.setOnClickListener {
            requireActivity().startService(Intent(requireContext(), SearchService::class.java))
        }
        searchBar = mView.seekBar2
        distance = mView.seekBarTextMeters3
        time = mView.seekBarTextTime
        searchBar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                distance?.text = progress.toString() +"m"//To change body of created functions use File | Settings | File Templates.
                time?.text = (progress*0.01).toInt().toString() +"min"
                radius = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                 //To change body of created functions use File | Settings | File Templates.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //To change body of created functions use File | Settings | File Templates.
            }
        })

        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(mStatusChecker)
    }

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

    override fun onMapReady(map: GoogleMap) {
        mMap = map

        getMarkers = APIImpl().getStationsLocalizations().subscribe { stationsList ->
            for(station in stationsList) {
                mMap.addMarker(
                    MarkerOptions()
                        .title("Stacja")
                        .position(LatLng(station.latitude.toDouble(),station.longitude.toDouble()))
                        .snippet(station.id.toString())
                )

                map.setOnMarkerClickListener { marker ->
                    return@setOnMarkerClickListener true
                }
            }
        }

        circle = mMap.addCircle(
            CircleOptions().apply {
                center(LatLng(0.0,0.0))
                radius(100.0)
                strokeWidth(0.0f)
                fillColor(0x503db4ff)
            })

        map.uiSettings.isScrollGesturesEnabled = false
        map.uiSettings.isZoomGesturesEnabled = false
        map.uiSettings.isRotateGesturesEnabled = false

        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoWindow(arg0: Marker) = null

            override fun getInfoContents(marker: Marker): View {
                val infoWindow = layoutInflater.inflate(
                    R.layout.custom_info_contents,
                    mView.findViewById(R.id.map), false
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
                locationResult = mFusedLocationProviderClient?.lastLocation
                locationResult?.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        mLastKnownLocation = task.result

                        val zoomFinal = when(radius){
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
                        circle?.radius = radius.toDouble()
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

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

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