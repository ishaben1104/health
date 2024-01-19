package com.example.health

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Locale

private lateinit var editTextFirstname: EditText
private lateinit var editTextLastname: EditText
private lateinit var editTextPhone: EditText
private lateinit var editTextEmailAddress: EditText
private lateinit var editTextDOB: AutoCompleteTextView
private lateinit var editTextUsername: EditText
private lateinit var editTextPassword: EditText
private lateinit var registerButton: Button
private lateinit var loginButton: Button
private lateinit var switchIsVIP: Switch
private lateinit var patientTypeInfo: TextView
private var paidAmount: Int = 0

class PatientRegisterActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val patientsRef = database.getReference("patient")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_register)
        supportActionBar!!.hide() // to hide the app bar from this page

        //to give reference
        editTextFirstname = findViewById(R.id.editTextFirstname)
        editTextLastname = findViewById(R.id.editTextLastname)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextEmailAddress = findViewById(R.id.editTextEmailAddress)
        editTextDOB = findViewById(R.id.editTextDOB)
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        switchIsVIP = findViewById(R.id.VIP)
        patientTypeInfo = findViewById(R.id.patientTypeInfo)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        editTextDOB.setOnClickListener {
            showDatePickerDialog()
        }

        // Validate other fields as needed...
        // Inside your registerButton click listener
        registerButton.setOnClickListener(View.OnClickListener {
            // Get the entered fields
            val firstname = editTextFirstname.text.toString().trim()
            val lastname = editTextLastname.text.toString().trim()
            val phone = editTextPhone.text.toString().trim()
            val email = editTextEmailAddress.text.toString().trim()
            val dob = editTextDOB.text.toString().trim()
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val isVIP = switchIsVIP.isChecked

            if (firstname.isEmpty() || lastname.isEmpty() || phone.isEmpty() ||
                email.isEmpty() || dob.isEmpty() || username.isEmpty() || password.isEmpty()) {
                isRegistrationValid(firstname, lastname, phone, email, dob, username, password)
            }

            // Check if the username is already taken
            if (isRegistrationValid(firstname, lastname, phone, email, dob, username, password)) {
                patientsRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                editTextUsername.error = "Username is already taken. Please choose another one."
                            } else {
                                // Username is available, proceed with registration
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(this@PatientRegisterActivity) { task ->
                                        if (task.isSuccessful) {
                                            val patient = auth.currentUser
                                            val patientId = patient?.uid ?: ""

                                            // Create a User object and add it to the database
                                            val userObject = Patient(
                                                patientId,
                                                firstname,
                                                lastname,
                                                phone,
                                                email,
                                                dob,
                                                username,
                                                isVIP,
                                                paidAmount,
                                                "Pending"
                                            )
                                            patientsRef.child(patientId).setValue(userObject)

                                            Toast.makeText(
                                                this@PatientRegisterActivity,
                                                "Registration Successful",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            val sharedPreferences = getSharedPreferences("MySession", Context.MODE_PRIVATE)
                                            val editor = sharedPreferences.edit()

                                            // Assuming userToken is the session value you want to store
                                            editor.putString("username", username)
                                            editor.putString("isVIP", isVIP.toString())
                                            editor.putString("paidAmount", paidAmount.toString())
                                            editor.putString("patientLoginId", patientId)
                                            editor.apply()

                                            // Authentication successful, navigate to the login activity
                                            val intent = Intent(
                                                this@PatientRegisterActivity,
                                                PatientLoginActivity::class.java
                                            )
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(
                                                this@PatientRegisterActivity,
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

        switchIsVIP.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                patientTypeInfo.text = "Note: VIP patients are required to make an annual payment of £350, which covers dental treatments and minor surgeries"
                switchIsVIP.isChecked = true
                paidAmount = 350
            } else {
                patientTypeInfo.text = "Note: Normal patients are required to make an annual payment of £50, and they may incur additional charges for dental treatments."
                switchIsVIP.isChecked = false
                paidAmount = 50
            }
        }

        //Login Page
        loginButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@PatientRegisterActivity, PatientLoginActivity::class.java)
            startActivity(intent)
        })
    }

    private fun isRegistrationValid(firstname: String, lastname: String, phone: String, email: String, dob: String, username: String, password: String): Boolean {
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

        if(dob.isBlank()) {
            editTextDOB.error = "Date of birth is required"
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
        else if (!isPasswordValid(password)) {
            editTextPassword.error = "Password must be at least 8 characters long and contain at least one digit."
            isBlank = false
        }
        return isBlank
    }

    // Validate Phone Number
    private fun isValidPhoneNumber(phoneNumber: CharSequence?): Boolean {
        return !TextUtils.isEmpty(phoneNumber) && Patterns.PHONE.matcher(phoneNumber).matches() && phoneNumber!!.length == 10
    }

    // Validate Email
    private fun isValidEmail(email: CharSequence?): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Validate Password
    private fun isPasswordValid(password: String): Boolean {
        // Define your password requirements here
        val passwordRegex = Regex("^(?=.*[0-9]).{8,}$")
        return passwordRegex.matches(password)
    }

    // Validate Date of birth
    private fun showDatePickerDialog() {
        val currentDate = System.currentTimeMillis() - 1000  // To prevent the selection of the current date

        val datePickerDialog = DatePickerDialog(
            this@PatientRegisterActivity,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)

                if (selectedCalendar.timeInMillis < currentDate) {
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    editTextDOB.setText(sdf.format(selectedCalendar.time))
                } else {
                    Toast.makeText(
                        this@PatientRegisterActivity,
                        "Invalid date. Please select a date before today.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            // Set the initial date in the dialog
            2000, // Initial year
            0,    // Initial month (0-based)
            1     // Initial day
        )

        // Display the date picker dialog
        datePickerDialog.show()
    }
}