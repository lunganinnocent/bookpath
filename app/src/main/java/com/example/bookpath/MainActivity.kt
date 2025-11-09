package com.example.bookpath

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle system bar insets (status bar, nav bar, etc.)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ SAFELY Get userId from Intent or use default
        userId = intent.getStringExtra("logged_in_user_id") ?: "default_user_id"

        // Initialize BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.home

        // Handle navigation clicks
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> true

                R.id.add_task -> {
                    val intent = Intent(this, AddTask::class.java)
                    intent.putExtra("logged_in_user_id", userId)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }

                R.id.task_dashboard -> {
                    val intent = Intent(this, AssignmentOverview::class.java)
                    intent.putExtra("logged_in_user_id", userId)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }

                R.id.chat -> {
                    val intent = Intent(this, ChatBot::class.java)
                    intent.putExtra("logged_in_user_id", userId)
                    startActivity(intent)
                    overridePendingTransition(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                    true
                }

                R.id.profile -> {
                    // ✅ Always pass userId to Profile
                    val intent = Intent(this, Profile::class.java)
                    intent.putExtra("logged_in_user_id", userId)
                    startActivity(intent)
                    overridePendingTransition(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                    )
                    true
                }

                else -> false
            }
        }
    }

    // ✅ Handle when coming back from other activities
    override fun onResume() {
        super.onResume()
        // Update userId if it was passed back
        intent?.getStringExtra("logged_in_user_id")?.let {
            userId = it
        }
    }
}