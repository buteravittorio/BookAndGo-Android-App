package com.example.bookandgo.login_signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookandgo.home.HomeActivity
import com.example.bookandgo.R
import org.json.JSONArray

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnAccedi = findViewById<Button>(R.id.btnAccedi)
        val tvCreaAccount= findViewById<TextView>(R.id.tvCreaAccount)
        val checkBox = findViewById<CheckBox>(R.id.checkBox)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val isRemembered = sharedPreferences.getBoolean("REMEMBER_ME", false)
        if (isRemembered) {
            etUsername.setText(sharedPreferences.getString("REMEMBERED_USER", ""))
            etPassword.setText(sharedPreferences.getString("REMEMBERED_PASS", ""))
            checkBox.isChecked = true
        }

        btnAccedi.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val usersJsonString = sharedPreferences.getString("USERS_LIST", "[]")
            val usersArray = JSONArray(usersJsonString)

            var loginSuccess = false

            for (i in 0 until usersArray.length()) {
                val userObject = usersArray.getJSONObject(i)
                if (userObject.getString("username") == username && userObject.getString("password") == password) {
                    loginSuccess = true
                    break
                }
            }

            if (loginSuccess) {
                val editor = sharedPreferences.edit()

                if (checkBox.isChecked) {
                    editor.putString("REMEMBERED_USER", username)
                    editor.putString("REMEMBERED_PASS", password)
                    editor.putBoolean("REMEMBER_ME", true)
                } else {
                    editor.putBoolean("REMEMBER_ME", false)
                }

                editor.apply()

                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("USERNAME", username)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Nome utente o password errati!", Toast.LENGTH_LONG).show()
            }
        }

        tvCreaAccount.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
