package com.example.health

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private lateinit var editTextFirstname: EditText
private lateinit var editTextLastname: EditText
private lateinit var editTextPhone: EditText
private lateinit var editTextEmailAddress: EditText
private lateinit var editTextDesignation: EditText
private lateinit var editTextUsername: EditText
private lateinit var editTextPassword: EditText
private lateinit var registerButton: Button
private lateinit var loginButton: Button

class DoctorRegisterActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val doctorsRef = database.getReference("doctor")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_register)
        supportActionBar!!.hide() // to hide the app bar from this page

        //to give reference
        editTextFirstname = findViewById(R.id.editTextFirstname)
        editTextLastname = findViewById(R.id.editTextLastname)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextEmailAddress = findViewById(R.id.editTextEmailAddress)
        editTextDesignation = findViewById(R.id.editTextDesignation)
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        // Set autofocus on firstnameEditText
        editTextFirstname.requestFocus()

        registerButton.setOnClickListener(View.OnClickListener {
            // Get the entered fields
            val firstname = editTextFirstname.text.toString().trim()
            val lastname = editTextLastname.text.toString().trim()
            val phone = editTextPhone.text.toString().trim()
            val email = editTextEmailAddress.text.toString().trim()
            val designation = editTextDesignation.text.toString().trim()
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (firstname.isEmpty() || lastname.isEmpty() || phone.isEmpty() ||
                email.isEmpty() || designation.isEmpty() || username.isEmpty() || password.isEmpty()) {
                isRegistrationValid(firstname, lastname, phone, email, designation, username, password)
            }

            // Check if the username is already taken
            if (isRegistrationValid(firstname, lastname, phone, email, designation, username, password)) {
            doctorsRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            editTextUsername.error = "Username is already taken. Please choose another one."
                        } else {
                            // Username is available, proceed with registration
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this@DoctorRegisterActivity) { task ->
                                    if (task.isSuccessful) {
                                        val doctor = auth.currentUser
                                        val doctorId = doctor?.uid ?: ""

                                        // Create a User object and add it to the database
                                        val userObject = Doctor(
                                            doctorId,
                                            firstname,
                                            lastname,
                                            phone,
                                            email,
                                            designation,
                                            username
                                        )
                                        doctorsRef.child(doctorId).setValue(userObject)

                                        Toast.makeText(
                                            this@DoctorRegisterActivity,
                                            "Registration Successful",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        val sharedPreferences = getSharedPreferences("MySession", Context.MODE_PRIVATE)
                                        val editor = sharedPreferences.edit()

                                        // Assuming userToken is the session value you want to store
                                        editor.putString("doctorLoginId", doctorId)
                                        editor.apply()

                                        // Authentication successful, navigate to the login activity
                                        val intent = Intent(
                                            this@DoctorRegisterActivity,
                                            DoctorLoginActivity::class.java
                                        )
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(
                                            this@DoctorRegisterActivity,
                                            "Registration failed. ${task.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                        Toast.makeText(
                            applicationContext,
                            "Firebase Database Error: ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        })

        //Login Page
        loginButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@DoctorRegisterActivity, DoctorLoginActivity::class.java)
            startActivity(intent)
        })
    }

    private fun isRegistrationValid(firstname: String, lastname: String, phone: String, email: String, designation: String,  username: String, password: String): Boolean {
        var isBlank: Boolean = true

        // Check if all fields are not empty
        if(firstname.isBlank()) {
            editTextFirstname.error = "Firstname is required"
            isBlank = false
        }

        if(lastname.isBlank()) {
            editTextLastname.error = "Lastname is required"
            isBlank = false
        }

        if(phone.isBlank()) {
            editTextPhone.error = "Phone number is required"
            isBlank = false
        }
        else if (!isValidPhoneNumber(phone)) {
            editTextPhone.error = "Please enter a valid phone number"
            isBlank = false
        }

        if(email.isBlank()) {
            editTextEmailAddress.error = "Email is required"
            isBlank = false
        }
        else if (!isValidEmail(email)) {
            editTextEmailAddress.error = "Please enter a valid email address"
            isBlank = false
        }

        if(designation.isBlank()) {
            editTextDesignation.error = "Designation is required"
            isBlank = false
        }

        if(username.isBlank()) {
            editTextUsername.error = "Username is required"
            isBlank = false
        }

        if(password.isBlank()) {
            editTextPassword.error = "Password is required"
            isBlank = false
        }
        return isBlank
    }

    // Validate Email
    private fun isValidEmail(email: CharSequence?): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Validate Phone Number
    private fun isValidPhoneNumber(phoneNumber: CharSequence?): Boolean {
        return !TextUtils.isEmpty(phoneNumber) && Patterns.PHONE.matcher(phoneNumber).matches() && phoneNumber!!.length == 10
    }
}