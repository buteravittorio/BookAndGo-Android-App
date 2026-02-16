package com.example.bookandgo.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import com.example.bookandgo.R


//Funzioni Home

// Funzione per ottenere la posizione dell'utente
fun getUserLocation(
    context: Context,
    googleMap: GoogleMap,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    selectedKeyword: String,
    apiKey: String,
    restaurantMarkers: MutableList<Marker>
) {
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(context as HomeActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        return
    }

    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            val userLatLng = LatLng(location.latitude, location.longitude)
            googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
            findNearbyRestaurants(context, location.latitude, location.longitude, googleMap, selectedKeyword, apiKey, restaurantMarkers) // ← Passiamo restaurantMarkers
        } else {
            Toast.makeText(context, "Impossibile ottenere la posizione", Toast.LENGTH_LONG).show()
        }
    }
}


// Funzione per cercare ristoranti nelle vicinanze
fun findNearbyRestaurants(
    context: Context,
    latitude: Double,
    longitude: Double,
    googleMap: GoogleMap,
    selectedKeyword: String,
    apiKey: String,
    restaurantMarkers: MutableList<Marker> //Lista per bottone Random
) {
    val radius = 5000
    val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
            "location=$latitude,$longitude&radius=$radius&type=restaurant" +
            "&keyword=$selectedKeyword&key=$apiKey"

    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("API Error", e.message.toString())
        }

        override fun onResponse(call: Call, response: Response) {
            val responseData = response.body?.string()
            if (responseData != null) {
                (context as HomeActivity).runOnUiThread {
                    parsePlacesResponse(responseData, googleMap, restaurantMarkers)
                }
            }
        }
    })
}


// Funzione per per aggiungere i marker sulla mappa e lista maker per Raandom
fun parsePlacesResponse(responseData: String, googleMap: GoogleMap, restaurantMarkers: MutableList<Marker>) {
    googleMap.clear()
    restaurantMarkers.clear() //lista Random

    val jsonObject = JSONObject(responseData)
    val results = jsonObject.getJSONArray("results")

    for (i in 0 until results.length()) {
        val place = results.getJSONObject(i)
        val name = place.getString("name")
        val placeId = place.getString("place_id")
        val location = place.getJSONObject("geometry").getJSONObject("location")
        val lat = location.getDouble("lat")
        val lng = location.getDouble("lng")

        val address = if (place.has("vicinity")) place.getString("vicinity") else "Indirizzo non disponibile"

        val placeLatLng = LatLng(lat, lng)
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(placeLatLng)
                .title(name)
                .snippet("$address\n$placeId")
        )

        marker?.let { restaurantMarkers.add(it) } //Maker lista Random
    }
}


// Funzione per ottenere i dettagli del ristorante
fun fetchRestaurantDetails(context: Context, marker: Marker, apiKey: String,username: String) {
    val placeId = marker.snippet?.split("\n")?.last() ?: return
    val url = "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId" +
            "&fields=name,vicinity,opening_hours,formatted_phone_number,rating,user_ratings_total,website,price_level,types" +
            "&key=$apiKey"


    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("API Error", e.message.toString())
        }

        override fun onResponse(call: Call, response: Response) {
            val responseData = response.body?.string()
            if (responseData != null) {
                (context as HomeActivity).runOnUiThread {
                    mostraDettagliRistorante(context, marker, responseData,username)
                }
            }
        }
    })
}

// Funzione per mostrare i dettagli del ristorante in un dialog
fun mostraDettagliRistorante(context: Context, marker: Marker, dettagli: String,username: String) {
    val jsonObject = JSONObject(dettagli)
    val result = jsonObject.getJSONObject("result")

    val nome = result.getString("name")
    val indirizzo = result.getString("vicinity")
    val phone = result.optString("formatted_phone_number", "Nessun numero disponibile")
    val website = result.optString("website", "")

    val rating = result.optDouble("rating", -1.0)
    val reviewCount = result.optInt("user_ratings_total", 0)

    val priceLevel = result.optInt("price_level", -1)
    val priceText = when (priceLevel) {
        0 -> "💲 Economico"
        1 -> "💲💲 Accessibile"
        2 -> "💲💲💲 Medio"
        3 -> "💲💲💲💲 Costoso"
        4 -> "💲💲💲💲💲 Molto Costoso"
        else -> "💰 Prezzo non disponibile"
    }

    val typesArray = result.optJSONArray("types")
    val typesText = if (typesArray != null) {
        val typesList = mutableListOf<String>()
        for (i in 0 until typesArray.length()) {
            typesList.add(typesArray.getString(i))
        }
        typesList.joinToString(", ").replace("_", " ").capitalize()
    } else {
        "Tipologia non disponibile"
    }

    val openingHours = if (result.has("opening_hours")) {
        val hoursArray = result.getJSONObject("opening_hours").getJSONArray("weekday_text")
        val hoursText = StringBuilder()
        for (i in 0 until hoursArray.length()) {
            hoursText.append(hoursArray.getString(i)).append("\n")
        }
        hoursText.toString()
    } else {
        "Orari non disponibili"
    }

    val dialogView = (context as HomeActivity).layoutInflater.inflate(R.layout.dialog_restaurant_details, null)
    val dialog = AlertDialog.Builder(context)
        .setView(dialogView)
        .create()

    val tvNome = dialogView.findViewById<TextView>(R.id.tvNomeRistorante)
    val tvIndirizzo = dialogView.findViewById<TextView>(R.id.tvIndirizzoRistorante)
    val tvDettagli = dialogView.findViewById<TextView>(R.id.tvDettagliRistorante)
    val btnMostraDettagli = dialogView.findViewById<Button>(R.id.btnMostraDettagli)
    val btnChiudi = dialogView.findViewById<Button>(R.id.btnChiudi)

    val btnChiama = dialogView.findViewById<Button>(R.id.btnChiamaRistorante)
    val btnSalvaNeiPreferiti = dialogView.findViewById<Button>(R.id.btnSalvaNeiPreferiti)
    val btnPrenota = dialogView.findViewById<Button>(R.id.btnPrenotaRistorante)
    val btnTakeaway = dialogView.findViewById<Button>(R.id.btnTakeaway)

    tvNome.text = nome
    tvIndirizzo.text = indirizzo

    val detailsText = StringBuilder()
        .append("📞 Telefono: $phone\n\n")
        .append("🕒 Orari di apertura:\n$openingHours\n\n")
        .append("⭐ Valutazione: ${if (rating != -1.0) "$rating/5 ($reviewCount recensioni)" else "Non disponibile"}\n\n")
        .append("💰 Prezzo: $priceText\n\n")
        .append("🍽️ Tipologia: $typesText\n\n")

    if (website.isNotEmpty()) {
        detailsText.append("🔗 Sito Web: $website")
    }

    tvDettagli.text = detailsText.toString()
    tvDettagli.movementMethod = android.text.method.LinkMovementMethod.getInstance() // Rende i link cliccabili

    tvDettagli.visibility = View.GONE

    btnMostraDettagli.setOnClickListener {
        tvDettagli.visibility = if (tvDettagli.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    btnChiama.visibility = View.VISIBLE
    btnChiama.setOnClickListener {
        if (phone != "Nessun numero disponibile") {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone")
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Numero di telefono non disponibile", Toast.LENGTH_SHORT).show()
        }
    }

    btnSalvaNeiPreferiti.setOnClickListener {
        salvaNeiPreferiti(context, username, nome, indirizzo)
        Toast.makeText(context, "$nome aggiunto ai preferiti!", Toast.LENGTH_SHORT).show()
    }


    btnPrenota.visibility = View.VISIBLE
    btnPrenota.setOnClickListener {
        val intent = Intent(context, ReservationActivity::class.java)
        intent.putExtra("USERNAME", username)
        intent.putExtra("restaurant_name", nome)
        context.startActivity(intent)
    }

    btnTakeaway.setOnClickListener {
        Toast.makeText(context, "Asporto non disponibile", Toast.LENGTH_SHORT).show()
    }

    btnChiudi.setOnClickListener { dialog.dismiss() }

    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.show()

}




// Funzione per salvare un ristorante nei preferiti
fun salvaNeiPreferiti(context: Context, username: String, name: String, address: String) {
    val sharedPref = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()

    val key = "favorites_$username" // Chiave personalizzata per ogni utente
    val favoritesSet = sharedPref.getStringSet(key, setOf())?.toMutableSet() ?: mutableSetOf()

    favoritesSet.add("$name - $address")

    editor.putStringSet(key, favoritesSet)
    editor.apply()
}
