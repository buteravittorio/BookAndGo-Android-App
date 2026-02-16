package com.example.bookandgo.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bookandgo.R
import java.text.SimpleDateFormat
import java.util.*

class ReservationActivity : AppCompatActivity() {

    private lateinit var etData: EditText
    private lateinit var etOra: EditText
    private lateinit var etPersone: EditText
    private lateinit var btnConfermaPrenotazione: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        etData = findViewById(R.id.etData)
        etOra = findViewById(R.id.etOra)
        etPersone = findViewById(R.id.etPersone)
        btnConfermaPrenotazione = findViewById(R.id.btnConfermaPrenotazione)

        val calendar = Calendar.getInstance()

        etData.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)

                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    etData.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        etOra.setOnClickListener {
            val timePicker = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    etOra.setText(formattedTime)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }

        btnConfermaPrenotazione.setOnClickListener {
            val data = etData.text.toString()
            val ora = etOra.text.toString()
            val persone = etPersone.text.toString()
            val NomeRistorante = intent.getStringExtra("restaurant_name") ?: "Sconosciuto"
            val username = intent.getStringExtra("USERNAME")?: ""
            if (data.isEmpty() || ora.isEmpty() || persone.isEmpty()) {
                Toast.makeText(this, "Compila tutti i campi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reservation = "📍 Ristorante: $NomeRistorante\n📅 Data: $data\n🕒 Orario: $ora\n👥 Persone: $persone"
            salvaPrenotazione(this, username, reservation)

            Toast.makeText(this, "Prenotazione confermata!", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    private fun salvaPrenotazione(context: Context, username: String, reservation: String) {
        val sharedPreferences = context.getSharedPreferences("reservations", Context.MODE_PRIVATE)
        val userKey = "reservation_list_$username"

        val prenotazione = sharedPreferences.getStringSet(userKey, mutableSetOf()) ?: mutableSetOf()
        val updatedReservations = prenotazione.toMutableSet()
        updatedReservations.add(reservation)

        sharedPreferences.edit()
            .putStringSet(userKey, updatedReservations)
            .apply()
    }

}




