package com.example.bookandgo.login_signup

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookandgo.R
import org.json.JSONArray
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUsernameR = findViewById<EditText>(R.id.etUsernameR)
        val etPasswordR = findViewById<EditText>(R.id.etPasswordR)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val username = etUsernameR.text.toString()
            val password = etPasswordR.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val sharedPreferences: SharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                // Recupera lista di utent esistente
                val usersJsonString = sharedPreferences.getString("USERS_LIST", "[]")
                val usersArray = JSONArray(usersJsonString)

                // Controlla se l'utente esiste già
                var userExists = false
                for (i in 0 until usersArray.length()) {
                    val userObject = usersArray.getJSONObject(i)
                    if (userObject.getString("username") == username) {
                        userExists = true
                        break
                    }
                }

                if (!userExists) {
                    // Aggiungi nuovo utente
                    val newUser = JSONObject()
                    newUser.put("username", username)
                    newUser.put("password", password)
                    usersArray.put(newUser)

                    // Salva lista aggiornata
                    editor.putString("USERS_LIST", usersArray.toString())
                    editor.apply()

                    Toast.makeText(this, "Registrazione avvenuta con successo!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "L'utente esiste già!", Toast.LENGTH_LONG).show()
                }

            } else {
                Toast.makeText(this, "Per favore, riempi tutti i campi!", Toast.LENGTH_LONG).show()
            }
        }

        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
