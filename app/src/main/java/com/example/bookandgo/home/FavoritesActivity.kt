package com.example.bookandgo.home

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bookandgo.R

class FavoritesActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: FavoriteAdapter
    private var preferiti: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        listView = findViewById(R.id.lvPreferiti)
        val btnIndietro = findViewById<Button>(R.id.btnIndietro)
        val btnEliminaPreferiti = findViewById<Button>(R.id.btnEliminaPreferiti)

        val username = intent.getStringExtra("USERNAME")?:""
        caricaPreferiti(username)


        adapter = FavoriteAdapter(this, preferiti,
            onRemove = { ristorante -> rimuoviPreferiti(ristorante,username) },
            onViewDetails = { ristorante -> mostraDettagliRistorante(ristorante) }
        )
        listView.adapter = adapter

        btnEliminaPreferiti.setOnClickListener {
            EliminaTuttiPreferiti(username)
            finish()
        }

        btnIndietro.setOnClickListener {
            finish()
        }
    }

    private fun caricaPreferiti(username: String) {
        val sharedPref = getSharedPreferences("Favorites", MODE_PRIVATE)
        preferiti = sharedPref.getStringSet("favorites_$username", mutableSetOf())?.toMutableList() ?: mutableListOf()
    }

    private fun rimuoviPreferiti( restaurant: String,username: String,) {
        preferiti.remove(restaurant)

        val sharedPref = getSharedPreferences("Favorites", MODE_PRIVATE).edit()
        sharedPref.putStringSet("favorites_$username", preferiti.toSet())
        sharedPref.apply()

        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Ristorante rimosso dai preferiti!", Toast.LENGTH_SHORT).show()
    }

    private fun EliminaTuttiPreferiti(username: String) {
        preferiti.clear()

        val sharedPref = getSharedPreferences("Favorites", MODE_PRIVATE).edit()
        sharedPref.remove("favorites_$username")
        sharedPref.apply()

        Toast.makeText(this, "Preferiti eliminati!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun mostraDettagliRistorante(ristorante: String) {
        val nome = ristorante.split(" - ")[0]
        val indirizzo = ristorante.split(" - ").getOrNull(1) ?: "Indirizzo non disponibile"

        val dialogView = layoutInflater.inflate(R.layout.dialog_restaurant_details, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val tvNome = dialogView.findViewById<TextView>(R.id.tvNomeRistorante)
        val tvIndirizzo = dialogView.findViewById<TextView>(R.id.tvIndirizzoRistorante)
        val tvDettagli = dialogView.findViewById<TextView>(R.id.tvDettagliRistorante)
        val btnChiudi = dialogView.findViewById<Button>(R.id.btnChiudi)

        tvNome.text = nome
        tvIndirizzo.text = indirizzo
        tvDettagli.text = "Dettagli non disponibili"

        btnChiudi.setOnClickListener { dialog.dismiss() }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}

