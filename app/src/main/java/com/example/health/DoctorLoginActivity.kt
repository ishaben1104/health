package com.example.health

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private lateinit var editTextUsername: EditText
private lateinit var editTextPassword: EditText
private lateinit var loginButton: Button
private lateinit var registerButton: Button
private lateinit var errorTextView: TextView

class DoctorLoginActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val doctorsRef = database.getReference("doctor")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_login)
        supportActionBar!!.hide() // to hide the app bar from this page

        //to give reference
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        errorTextView = findViewById(R.id.errorTextView)

        //Login Page
        loginButton.setOnClickListener(View.OnClickListener {
            // Get the entered username and password
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            // Check for non-empty fields
            if (username.isEmpty() || password.isEmpty()) {
                showError("Username and password are required")
                return@OnClickListener
            }

            getEmailForUsername(username) { email ->
                if (email != null) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this@DoctorLoginActivity) { task ->
                            if (task.isSuccessful) {
                                // Login successful
                                showSuccessfulLoginMessage()
                                val intent = Intent(this@DoctorLoginActivity, DoctorAvailabilityActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // If sign in fails, display a message to the user.
                                showInvalidCredentialsMessage()
                                Log.w("DoctorLoginActivity", "signInWithEmailAndPassword:failure", task.exception)
                            }
                        }
                } else {
                    // Username not found
                    showInvalidCredentialsMessage()
                    Log.w("DoctorLoginActivity", "Username not found")
                }
            }
        })

        //Register Page
        registerButton.setOnClickListener(View.OnClickListener {
            // Navigate to the RegisterActivity
            val intent = Intent(this@DoctorLoginActivity, DoctorRegisterActivity::class.java)
            startActivity(intent)
        })
    }

    private fun getEmailForUsername(username: String, callback: (String?) -> Unit) {
        val query = doctorsRef.orderByChild("username").equalTo(username)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming there is only one user with the given username
                    for (userSnapshot in dataSnapshot.children) {
                        val email = userSnapshot.child("email").getValue(String::class.java)
                        callback(email)
                        return
                    }
                }
                callback(null) // Username not found
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Log.e("DoctorLoginActivity", "onCancelled: ${databaseError.message}")
                callback(null)
            }
        })
    }

    private fun showSuccessfulLoginMessage() {
        hideError()
        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
    }

    //Toast Messages
    private fun showInvalidCredentialsMessage() {
        showError("Invalid username or password")
    }

    private fun showError(errorMessage: String) {
        errorTextView.text = errorMessage
        errorTextView.visibility = View.VISIBLE
    }

    private fun hideError() {
        errorTextView.visibility = View.GONE
    }
}