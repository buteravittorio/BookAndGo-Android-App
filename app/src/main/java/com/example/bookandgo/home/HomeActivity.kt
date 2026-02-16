package com.example.bookandgo.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.bookandgo.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Marker

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var spinnerTipoRistorante: Spinner
    private var selectedKeyword: String = "restaurant"
    private val apiKey = ""
    private val restaurantMarkers = mutableListOf<Marker>()
    private lateinit var username: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        username = intent.getStringExtra("USERNAME")?: ""
        val username1 = username.replaceFirstChar { it.uppercaseChar() }
        val welcomeTextView = findViewById<TextView>(R.id.textView)
        welcomeTextView.text = "Ciao, $username1!"


        mapView = findViewById(R.id.mapView)
        spinnerTipoRistorante = findViewById(R.id.spinnerTipoRistorante)
        val btnCercaRistorante: Button = findViewById(R.id.btnCercaRistorante)
        val btnMostraPreferiti: Button = findViewById(R.id.btnMostraPreferiti)
        val btnCronologia: Button = findViewById(R.id.btnCronologia)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnMostraPreferiti.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }


        btnCronologia.setOnClickListener {
            val intent =Intent(this, HistoryActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
        }

        spinnerTipoRistorante.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedKeyword = when (position) {
                    1 -> "pizzeria"
                    2 -> "sushi"
                    3 -> "fast food"
                    4 -> "vegetarian"
                    5 -> "kebab"
                    6 -> "takeaway"
                    else -> "restaurant"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnCercaRistorante.setOnClickListener {
            getUserLocation(this, googleMap, fusedLocationClient, selectedKeyword, apiKey, restaurantMarkers)
        }

        val btnRandom: Button = findViewById(R.id.btnRandom)

        btnRandom.setOnClickListener {
            if (restaurantMarkers.isNotEmpty()) {
                val randomMarker = restaurantMarkers.random()
                fetchRestaurantDetails(this, randomMarker, apiKey,username)
            } else {
                Toast.makeText(this, "Premi 'Cerca' prima.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnMarkerClickListener { marker ->
            fetchRestaurantDetails(this, marker, apiKey,username)
            true
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            getUserLocation(this, googleMap, fusedLocationClient, selectedKeyword, apiKey, restaurantMarkers)
        } else {
            Toast.makeText(this, "Permesso negato! Impossibile mostrare la posizione.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }
}