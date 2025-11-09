package com.example.bookpath

import android.os.Bundle
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        auth = FirebaseAuth.getInstance()

        currentPasswordEditText = findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        backButton = findViewById(R.id.backButton)

        changePasswordButton.setOnClickListener {
            changePassword()
        }

        backButton.setOnClickListener {
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun changePassword() {
        val currentPassword = currentPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (currentPassword.isEmpty()) {
            currentPasswordEditText.error = "Current password is required"
            return
        }

        if (newPassword.isEmpty()) {
            newPasswordEditText.error = "New password is required"
            return
        }

        if (newPassword.length < 6) {
            newPasswordEditText.error = "Password must be at least 6 characters"
            return
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.error = "Please confirm your password"
            return
        }

        if (newPassword != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            return
        }

        changePasswordButton.isEnabled = false
        changePasswordButton.text = "Changing Password..."

        val user = auth.currentUser

        if (user != null) {
            if (user.email != null) {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    changePasswordButton.isEnabled = true
                                    changePasswordButton.text = "Change Password"

                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Failed to change password: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            changePasswordButton.isEnabled = true
                            changePasswordButton.text = "Change Password"
                            currentPasswordEditText.error = "Current password is incorrect"
                            Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                changePasswordButton.isEnabled = true
                changePasswordButton.text = "Change Password"
                Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            changePasswordButton.isEnabled = true
            changePasswordButton.text = "Change Password"
            Toast.makeText(this, "User not found or not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}