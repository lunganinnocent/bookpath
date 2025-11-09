package com.example.bookpath

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookpath.com.example.bookpath.models.User
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🔹 Get references to UI elements
        val fullname = findViewById<EditText>(R.id.fullname)
        val username = findViewById<EditText>(R.id.username)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val registerBtn = findViewById<Button>(R.id.register)
        val goToLoginText = findViewById<TextView>(R.id.go_to_login) // Changed to TextView

        // 🔹 Firebase Database reference
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        // 🔹 Register button click listener
        registerBtn.setOnClickListener {
            val name = fullname.text.toString().trim()
            val user = username.text.toString().trim()
            val mail = email.text.toString().trim()
            val pass = password.text.toString().trim()

            if (name.isEmpty() || user.isEmpty() || mail.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create User object properly
            val newUser = User(
                userId = usersRef.push().key ?: "", // unique ID
                username = user,
                fullName = name,
                email = mail,
                picture = "",
                password = pass
            )

            // Save to Firebase
            usersRef.child(newUser.userId).setValue(newUser)
                .addOnSuccessListener {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    fullname.text.clear()
                    username.text.clear()
                    email.text.clear()
                    password.text.clear()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        // 🔹 Go to Login screen when clicked
        goToLoginText.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish() // closes Register activity
        }
    }
}