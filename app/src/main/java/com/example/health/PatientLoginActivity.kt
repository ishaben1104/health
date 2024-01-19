package com.example.health

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

private lateinit var usernameEditText: EditText
private lateinit var passwordEditText: EditText
private lateinit var loginButton: Button
private lateinit var registerButton: Button
private lateinit var errorTextView: TextView
private lateinit var auth: FirebaseAuth
private lateinit var patientsRef: DatabaseReference

class PatientLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_login)
        supportActionBar!!.hide() // to hide the app bar from this page

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
        patientsRef = database.getReference("patient")

        //to give reference
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        errorTextView = findViewById(R.id.errorTextView)

        //Login Page
        loginButton.setOnClickListener(View.OnClickListener {
            // Get the entered username and password
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Check for non-empty fields
            if (username.isEmpty() || password.isEmpty()) {
                showError("Username and password are required")
                return@OnClickListener
            }

            getEmailForUsername(username) { email ->
                if (email != null) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this@PatientLoginActivity) { task ->
                            if (task.isSuccessful) {
                                val patient = auth.currentUser
                                val patientId = patient?.uid ?: ""
                                // Login successful
                                showSuccessfulLoginMessage()
                                val sharedPreferences = getSharedPreferences("MySession", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()

                                // Assuming userToken is the session value you want to store
                                editor.putString("username", username)
                                editor.putString("patientLoginId", patientId)
                                editor.apply()
                                val intent = Intent(this@PatientLoginActivity, PaymentActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // If sign in fails, display a message to the user.
                                showInvalidCredentialsMessage()
                                Log.w("PatientLoginActivity", "signInWithEmailAndPassword:failure", task.exception)
                            }
                        }
                } else {
                    // Username not found
                    showInvalidCredentialsMessage()
                    Log.w("PatientLoginActivity", "Username not found")
                }
            }
        })

        //Register Page
        registerButton.setOnClickListener(View.OnClickListener {
            // Navigate to the RegisterActivity
            val intent = Intent(this@PatientLoginActivity, PatientRegisterActivity::class.java)
            startActivity(intent)
        })
    }

    private fun getEmailForUsername(username: String, callback: (String?) -> Unit) {
        val query = patientsRef.orderByChild("username").equalTo(username)

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
                Log.e("PatientLoginActivity", "onCancelled: ${databaseError.message}")
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