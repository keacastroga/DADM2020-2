package co.edu.unal.openmaptutorial

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.apache.commons.lang3.StringUtils
import org.json.JSONArray
import org.json.JSONException
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.w3c.dom.Text


class MainActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private var mMapView: MapView? = null
    private val poiMarkers = FolderOverlay()

    private var localidad = ""
    private var barrio = ""
    private var tipo = ""

    private val urlBase = "https://www.datos.gov.co/resource/pk4g-5xyt.json?"

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
        mapController.setZoom(12.5)

        val startPoint = GeoPoint(4.633318, -74.081690)
        mapController.setCenter(startPoint)
        mMapView!!.overlays.add(poiMarkers)
        getLocalidades()
        getBarrios()

        val barriosAutoView = findViewById<AutoCompleteTextView>(R.id.barrioAuto)
        barriosAutoView.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                barrio = s.toString()
            }
        })
        findViewById<AutoCompleteTextView>(R.id.localidadAuto).addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                localidad = s.toString()
                barriosAutoView.setText("")
                getBarrios()
            }
        })

        findViewById<Button>(R.id.search_button).setOnClickListener {
            poiMarkers.items.clear()
            getViviendas()
        }
    }

    private fun getViviendas() {
        val queue = Volley.newRequestQueue(this)
        val poiIcon =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_baseline_house_24,
                null
            )
        var urlReq = urlBase
        if (localidad != "")
            urlReq = urlReq + "localidad=" + localidad + "&"
        if (barrio != "")
            urlReq = urlReq + "barrio=" + barrio + "&"
        if (tipo != "")
            urlReq = urlReq + "tipo_vivienda=" + tipo + "&"

        val req: JsonArrayRequest = object : JsonArrayRequest(
            Method.GET, urlReq,
            null,
            Response.Listener { response: JSONArray ->
                try {
                    for (i in 0 until response.length()) {
                        // Get current json object
                        val vivienda = response.getJSONObject(i)
                        val poiMarker = Marker(mMapView)
                        val longitude = vivienda.getDouble("longitud")
                        val latitude = vivienda.getDouble("latitud")
                        val location = GeoPoint(longitude, latitude)
                        poiMarker.title = vivienda.getString("proyecto")
                        poiMarker.snippet = vivienda.getString("direccion")
                        poiMarker.position = location
                        poiMarker.icon = poiIcon
                        poiMarkers.add(poiMarker)
                    }
                    mMapView!!.invalidate()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                VolleyLog.d("REQUEST", "Error: " + error.message)
                Log.e("REQUEST", "Site Info Error: " + error.message)
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json";
                headers["X-App-Token"] = "etIRJ9Sct1XnoaLDHpcCNR6XW"
                return headers
            }
        }
        queue.add(req)
    }

    private fun getBarrios() {
        val queue = Volley.newRequestQueue(this)
        var url = urlBase + "\$select=barrio&"
        if (localidad != "")
            url = url + "localidad=" + localidad
        val req: JsonArrayRequest = object : JsonArrayRequest(
            Method.GET, url,
            null,
            Response.Listener { response: JSONArray ->
                try {
                    val barrios: MutableSet<String> = HashSet()
                    for (i in 0 until response.length()) {
                        val barrio = response.getJSONObject(i).optString("barrio")
                        if (barrio != "") {
                            barrios.add(barrio)
                        }
                    }
                    val barriosAdapter: ArrayAdapter<String> = ArrayAdapter(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        barrios.toList()
                    )
                    val barriosAuto: AutoCompleteTextView = findViewById(R.id.barrioAuto)
                    barriosAuto.setAdapter(barriosAdapter)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                VolleyLog.d("REQUEST", "Error: " + error.message)
                Log.e("REQUEST", "Site Info Error: " + error.message)
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json";
                headers["X-App-Token"] = "etIRJ9Sct1XnoaLDHpcCNR6XW"
                return headers
            }
        }
        queue.add(req)
    }

    fun getLocalidades() {
        val queue = Volley.newRequestQueue(this)
        val url = urlBase + "\$select=localidad"
        val req: JsonArrayRequest = object : JsonArrayRequest(
            Method.GET, url,
            null,
            Response.Listener { response: JSONArray ->
                try {
                    val barrios: MutableSet<String> = HashSet()
                    val localidades: MutableSet<String> = HashSet()
                    for (i in 0 until response.length()) {
                        val localidad = response.getJSONObject(i).optString("localidad")
                        if (localidad != "") {
                            localidades.add(localidad)
                        }
                    }
                    val localidadesAdapter: ArrayAdapter<String> = ArrayAdapter(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        localidades.toList()
                    )
                    val localidadesAuto: AutoCompleteTextView = findViewById(R.id.localidadAuto)
                    localidadesAuto.setAdapter(localidadesAdapter)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                VolleyLog.d("REQUEST", "Error: " + error.message)
                Log.e("REQUEST", "Site Info Error: " + error.message)
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json";
                headers["X-App-Token"] = "etIRJ9Sct1XnoaLDHpcCNR6XW"
                return headers
            }
        }
        queue.add(req)
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            when (view.getId()) {
                R.id.radioAny ->
                    if (checked) {
                        tipo = ""
                    }
                R.id.radioNoVIS ->
                    if (checked) {
                        tipo = "No VIS"
                    }
                R.id.radioVIS ->
                    if (checked) {
                        tipo = "VIS"
                    }
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