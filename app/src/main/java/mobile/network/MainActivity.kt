package mobile.network

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import mobile.network.databinding.ActivityMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    }

    private val viewModel:MainViewModel by viewModels()

    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        requestPermissionsIfNecessary(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        binding.viewmodel = viewModel
        setupMap()
        showCurrentLocation()
    }


    private fun showCurrentLocation() {
        val mMyLocationOverlay =
            MyLocationNewOverlay(GpsMyLocationProvider(this), binding.map)
        val mapController = binding.map.controller
        mMyLocationOverlay.disableMyLocation()
        mMyLocationOverlay.disableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true
        mMyLocationOverlay.runOnFirstFix {
            runOnUiThread {
                mapController.setCenter(mMyLocationOverlay.myLocation)
                mapController.animateTo(mMyLocationOverlay.myLocation)
                mapController.setZoom(binding.map.maxZoomLevel)
            }
        }
        binding.map.overlays.add(mMyLocationOverlay)
    }

    private fun setupMap() {
        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        binding.map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            val mLocationOverlay =
                MyLocationNewOverlay(GpsMyLocationProvider(context), binding.map).apply {
                    enableMyLocation()
                }
            overlays.add(mLocationOverlay)
            maxZoomLevel = 19.0
            minZoomLevel = 10.0
        }
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        val permissionsToRequest: ArrayList<String?> = ArrayList()
        for (i in grantResults.indices) {
            permissionsToRequest.add(permissions[i])
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

}