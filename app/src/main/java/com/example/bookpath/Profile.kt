package com.example.bookpath

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.bookpath.com.example.bookpath.models.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class Profile : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var fullNameText: TextView
    private lateinit var emailText: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var changeLanguageOption: CardView
    private lateinit var logoutOption: CardView

    private var imageUri: Uri? = null
    private lateinit var userId: String
    private lateinit var auth: FirebaseAuth
    private var currentUser: User? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            imageUri = result.data?.data

            Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .into(profileImage)

            databaseRef.child(userId).child("picture").setValue(imageUri.toString())
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update picture: ${e.message}", Toast.LENGTH_SHORT).show()
                    loadUserProfile(userId)
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applySavedLanguage()

        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        userId = intent.getStringExtra("logged_in_user_id") ?: run {
            Toast.makeText(this, "Error: user not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("users")

        profileImage = findViewById(R.id.profileImage)
        usernameText = findViewById(R.id.usernameText)
        fullNameText = findViewById(R.id.fullNameText)
        emailText = findViewById(R.id.emailText)
        bottomNav = findViewById(R.id.bottom_navigation)
        changeLanguageOption = findViewById(R.id.changeLanguageOption)
        logoutOption = findViewById(R.id.logoutOption)

        loadUserProfile(userId)

        profileImage.setOnClickListener {
            showImageOptionsDialog()
        }

        changeLanguageOption.setOnClickListener {
            showLanguageDialog()
        }

        logoutOption.setOnClickListener {
            logoutUser()
        }

        setupBottomNavigation()
    }

    private fun logoutUser() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun applySavedLanguage() {
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("app_language", "en") ?: "en"
        setAppLanguage(languageCode)
    }

    private fun showImageOptionsDialog() {
        val options = arrayOf("View Profile Picture", "Change Profile Picture", "Cancel")

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Profile Picture Options")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> showProfilePictureDialog()
                1 -> openImageChooser()
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun showProfilePictureDialog() {
        val imageDialog = ImageView(this)
        imageDialog.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        imageDialog.scaleType = ImageView.ScaleType.FIT_CENTER

        if (profileImage.drawable != null) {
            imageDialog.setImageDrawable(profileImage.drawable)
        } else if (currentUser != null && !currentUser!!.picture.isNullOrEmpty()) {
            Glide.with(this)
                .load(currentUser!!.picture)
                .into(imageDialog)
        } else {
            imageDialog.setImageResource(R.drawable.default_profile_pic)
        }

        val dialogBuilder = android.app.AlertDialog.Builder(this)
            .setView(imageDialog)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun loadUserProfile(userId: String) {
        databaseRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        currentUser = user
                        usernameText.text = user.username.ifEmpty { "Username" }
                        fullNameText.text = user.fullName.ifEmpty { "Full name not set" }
                        emailText.text = user.email.ifEmpty { "Email not set" }

                        if (!user.picture.isNullOrEmpty()) {
                            try {
                                Glide.with(this@Profile)
                                    .load(user.picture)
                                    .placeholder(R.drawable.default_profile_pic)
                                    .error(R.drawable.default_profile_pic)
                                    .circleCrop()
                                    .into(profileImage)
                            } catch (e: Exception) {
                                profileImage.setImageResource(R.drawable.default_profile_pic)
                            }
                        } else {
                            profileImage.setImageResource(R.drawable.default_profile_pic)
                        }
                    }
                } else {
                    createUserProfile(userId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Profile, "Error loading profile: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createUserProfile(userId: String) {
        val currentAuthUser = auth.currentUser
        val user = User(
            userId = userId,
            username = currentAuthUser?.displayName ?: "User",
            email = currentAuthUser?.email ?: "",
            fullName = currentAuthUser?.displayName ?: "",
            picture = ""
        )

        databaseRef.child(userId).setValue(user)
            .addOnSuccessListener {
                loadUserProfile(userId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile(userId)
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Afrikaans", "Zulu")
        val languageCodes = arrayOf("en", "af", "zu")

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Language")
        builder.setItems(languages) { dialog, which ->
            val selectedLanguage = languages[which]
            val languageCode = languageCodes[which]

            saveLanguagePreference(languageCode)
            setAppLanguage(languageCode)

            Toast.makeText(this, "Language changed to: $selectedLanguage", Toast.LENGTH_SHORT).show()
            restartApp()
        }
        builder.show()
    }

    private fun saveLanguagePreference(languageCode: String) {
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("app_language", languageCode)
            apply()
        }
    }

    private fun setAppLanguage(languageCode: String) {
        val locale = if (languageCode == "zu") {
            Locale("zu", "ZA")
        } else {
            Locale(languageCode)
        }
        Locale.setDefault(locale)

        val resources = resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("logged_in_user_id", userId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finishAffinity()
    }

    private fun setupBottomNavigation() {
        bottomNav.selectedItemId = R.id.profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("logged_in_user_id", userId)
                    startActivity(intent)
                    true
                }
                R.id.add_task -> {
                    val intent = Intent(this, AddTask::class.java)
                    intent.putExtra("logged_in_user_id", userId)
                    startActivity(intent)
                    true
                }
                R.id.task_dashboard -> {
                    val intent = Intent(this, AssignmentOverview::class.java)
                    intent.putExtra("logged_in_user_id", userId)
                    startActivity(intent)
                    true
                }
                R.id.chat -> {
                    val intent = Intent(this, ChatBot::class.java)
                    intent.putExtra("logged_in_user_id", userId)
                    startActivity(intent)
                    true
                }
                R.id.profile -> true
                else -> false
            }
        }
    }
}