package com.example.bookandgo.home

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bookandgo.R

class HistoryActivity : AppCompatActivity() {

    private lateinit var lvCronologia: ListView
    private lateinit var btnEliminaCronologia: Button
    private lateinit var btnIndietro: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        lvCronologia = findViewById(R.id.lvCronologia)
        btnEliminaCronologia = findViewById(R.id.btnEliminaCronologia)
        btnIndietro = findViewById(R.id.btnIndietro)

        val username = intent.getStringExtra("USERNAME")?:return

        val prenotazioni = caricaPrenotazioni(this, username).toList()

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, prenotazioni) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(Color.WHITE)
                return view
            }
        }


        lvCronologia.adapter = adapter

        btnEliminaCronologia.setOnClickListener {
            eliminaPrenotazioni(this, username)

            // Lista vuota altrimenti crashaaa
            val listaVuota = mutableListOf<String>()
            val newAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaVuota)
            lvCronologia.adapter = newAdapter
            newAdapter.notifyDataSetChanged()

            Toast.makeText(this, "Cronologia eliminata!", Toast.LENGTH_SHORT).show()
            finish()
        }


        btnIndietro.setOnClickListener { finish() }
    }

    private fun caricaPrenotazioni(context: Context, username: String): List<String> {
        val sharedPreferences = context.getSharedPreferences("reservations", Context.MODE_PRIVATE)
        val userKey = "reservation_list_$username"

        return sharedPreferences.getStringSet(userKey, emptySet())?.toList() ?: emptyList()
    }

    private fun eliminaPrenotazioni(context: Context, username: String) {
        val sharedPreferences = context.getSharedPreferences("reservations", Context.MODE_PRIVATE)
        val userKey = "reservation_list_$username"

        sharedPreferences.edit()
            .putStringSet(userKey, emptySet())
            .apply()
    }
}


