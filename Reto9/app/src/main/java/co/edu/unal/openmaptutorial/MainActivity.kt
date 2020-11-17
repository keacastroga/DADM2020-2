package co.edu.unal.openmaptutorial

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import android.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MainActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private var mMapView: MapView? = null

    private var radio = 0.01
    private lateinit var mPrefs : SharedPreferences

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //configuracion para asegurar espacio en cache antes de pedir permisos
        Configuration.getInstance()
            .load(
                applicationContext, PreferenceManager.getDefaultSharedPreferences(
                    applicationContext
                )
            )

        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
            )
        )
        //configuracion de la mapView
        setContentView(R.layout.activity_main)
        mMapView = findViewById<View>(R.id.map) as MapView
        mMapView!!.setTileSource(TileSourceFactory.MAPNIK)
        mMapView!!.setMultiTouchControls(true)

        val mapController = mMapView!!.controller
        mapController.setZoom(17.5)

        val poiProvider = NominatimPOIProvider("Reto9Locations")

        val poiMarkers = FolderOverlay()

        val mLocationOverlay = MyLocationNewOverlay(
            GpsMyLocationProvider(applicationContext),
            mMapView!!
        )
        mLocationOverlay.enableMyLocation()
        mLocationOverlay.runOnFirstFix {
            runOnUiThread {
                val startPoint = mLocationOverlay.myLocation
                mapController.setCenter(startPoint)
            }
        }
       mMapView!!.overlays.add(mLocationOverlay)
       mMapView!!.overlays.add(poiMarkers)

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        radio = mPrefs.getString("distance", "1")!!.toInt()/100.0

        val spinner = findViewById<Spinner>(R.id.location_spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.locations_name_array,
            android.R.layout.simple_spinner_item
        ).also{adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
       findViewById<Button>(R.id.search_button).setOnClickListener {
           poiMarkers.items.clear()
           lifecycleScope.launch {
               val startPoint = mLocationOverlay.myLocation
               val locationsArray = resources.getStringArray(R.array.locations_tag_array)
               val facility = locationsArray[spinner.selectedItemPosition]
               val pois: ArrayList<POI> = withContext(Dispatchers.IO){poiProvider.getPOICloseTo(startPoint, facility, 50, radio)}
               val poiIcon =
                   ResourcesCompat.getDrawable(
                       resources,
                       R.drawable.ic_baseline_location_on_24,
                       null
                   )
               for(poi in pois){
                   val poiMarker = Marker(mMapView)
                   poiMarker.title = spinner.selectedItem as String
                   poiMarker.snippet = poi.mDescription
                   poiMarker.position = poi.mLocation
                   poiMarker.icon = poiIcon
                   poiMarkers.add(poiMarker)
               }
               mMapView!!.invalidate()
           }
       }
    }
    public override fun onResume() {
        super.onResume()
        mMapView!!.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mMapView!!.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        val permissionsToRequest =
            ArrayList<String>()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.settings -> {
                startActivityForResult(Intent(this, Settings::class.java), 0)
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            Activity.RESULT_CANCELED ->{
                radio = mPrefs.getString("distance", "1")!!.toInt()/100.0
            }
        }
    }


    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest =
            ArrayList<String>()
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