package com.debin.mywanderapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = MapsActivity::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 101

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val myHome = LatLng(51.562014, 0.064850)
        mMap.addMarker(MarkerOptions().position(myHome).title("Marker in My Home"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myHome, 15f))
//        val androidOverlay =  GroundOverlayOptions()
//                .image(BitmapDescriptorFactory.fromResource(R.drawable.android))
//                .position(myHome, 100f)
//        googleMap.addGroundOverlay(androidOverlay)
        setMapOnLongClick(googleMap)
        setPoiClick(googleMap)
        setMapStyle(googleMap)
        enableMyLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId){
        R.id.normal_map -> { mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true}
        R.id.hybrid_map -> { mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true}
        R.id.satellite_map -> { mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true}
        R.id.terrain_map -> {mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true}
        else ->super.onOptionsItemSelected(item)
    }

    private fun setMapOnLongClick(googleMap : GoogleMap) {
        googleMap.setOnMapLongClickListener {
            val snippet = String.format(
                    Locale.getDefault(),
                    "Lat: %1$.5f, Long: %2$.5f",
                    it.latitude,
                    it.longitude
            )
          googleMap.addMarker(MarkerOptions().position(it)
                  .title(getString(R.string.dropped_pin)).snippet(snippet)
                  .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

      }
    }

    override fun onResume() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
           enableMyLocation()
        }
        super.onResume()
    }

    private fun setPoiClick(googleMap: GoogleMap) {
        googleMap.setOnPoiClickListener {
            val poiMarker = googleMap.addMarker(
                    MarkerOptions().position(it.latLng).title(it.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            poiMarker.showInfoWindow()
        }
    }

    private fun setMapStyle(googleMap: GoogleMap) {
        try {
            val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style
                    )
            )
            if(!success) {
                Log.e(TAG, "Style parsing failed")
            }
        } catch ( e : Resources.NotFoundException) {
            Log.e(TAG, "Can't find the file :", e)
        }
    }

    private fun checkIsPermissionGranded() : Boolean {
        return ContextCompat.checkSelfPermission(
                this,
                 Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if(checkIsPermissionGranded()) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(this,
            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == REQUEST_LOCATION_PERMISSION) {
            if(grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    enableMyLocation()
                } else {
                    val settingIntent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val packageUri =
                        Uri.fromParts("package", packageName, "Permissions")
                    settingIntent.data = packageUri
                    startActivity(settingIntent)
                }
            }
        }
    }
}