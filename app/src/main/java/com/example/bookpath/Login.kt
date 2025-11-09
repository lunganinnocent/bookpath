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
import com.google.firebase.database.*

class Login : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase reference to "users" node
        database = FirebaseDatabase.getInstance().getReference("users")

        val emailEdit = findViewById<EditText>(R.id.email)
        val passwordEdit = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login)
        val registerText = findViewById<TextView>(R.id.register)

        // Login button
        loginButton.setOnClickListener {
            val email = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Look for the user in Firebase by email
            database.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (userSnap in snapshot.children) {
                                val user = userSnap.getValue(User::class.java)
                                if (user != null) {
                                    if (user.password == password) { // Plain-text for now
                                        Toast.makeText(this@Login, "Login successful!", Toast.LENGTH_SHORT).show()

                                        // ✅ Pass userId to MainActivity
                                        val intent = Intent(this@Login, MainActivity::class.java)
                                        intent.putExtra("logged_in_user_id", user.userId)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this@Login, "Wrong password", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@Login, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@Login, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // Register text click
        registerText.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }
    }
}