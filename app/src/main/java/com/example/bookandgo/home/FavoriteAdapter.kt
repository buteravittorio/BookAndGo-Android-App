package com.example.bookandgo.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.bookandgo.R

class FavoriteAdapter(
    private val context: Context,
    private var preferiti: MutableList<String>,
    private val onRemove: (String) -> Unit,
    private val onViewDetails: (String) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = preferiti.size
    override fun getItem(position: Int): String = preferiti[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false)

        val tvNomePreferito = view.findViewById<TextView>(R.id.tvNomePreferito)
        val btnVediDettagli = view.findViewById<Button>(R.id.btnVediDettagli)
        val btnRimuoviPreferito = view.findViewById<Button>(R.id.btnRimuoviPreferito)

        val ristorante = preferiti[position]
        val nomeIndirizzo = ristorante.split(" - ")
        val nome = nomeIndirizzo[0]
        val indirizzo = if (nomeIndirizzo.size > 1) nomeIndirizzo[1] else "Indirizzo non disponibile"

        tvNomePreferito.text = nome

        val tvIndirizzoPreferito = view.findViewById<TextView>(R.id.tvIndirizzoPreferito)
        tvIndirizzoPreferito.text = indirizzo


        btnVediDettagli.setOnClickListener {
            onViewDetails(ristorante)
        }

        btnRimuoviPreferito.setOnClickListener {
            onRemove(ristorante)

        }
        return view
    }
}
