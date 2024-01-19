package com.example.health

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        supportActionBar!!.hide(); // to hide the app bar

        val cardNumber = findViewById<EditText>(R.id.cardNumber)
        val cardHolderName = findViewById<EditText>(R.id.cardHolderName)
        val expiryMonth = findViewById<EditText>(R.id.expiryMonth)
        val expiryYear = findViewById<EditText>(R.id.expiryYear)
        val cardCVV = findViewById<EditText>(R.id.cardCVV)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val cardNumber = cardNumber.text.toString()
            val cardHolderName = cardHolderName.text.toString()
            val expiryMonth = expiryMonth.text.toString()
            val expiryYear = expiryYear.text.toString()
            val cardCVV = cardCVV.text.toString()

            if (isValidCardNumber(cardNumber, cardHolderName) && isValidMonthAndYear(expiryMonth, expiryYear) && isValidCVV(cardCVV)) {
                // All fields are valid, you can proceed with further actions
                // For now, let's just display a toast message
                // Retrieve patient ID from SharedPreferences
                val sharedPreferences = getSharedPreferences("MySession", Context.MODE_PRIVATE)
                val patientId = sharedPreferences.getString("patientLoginId", "") // replace "patient_id" with the actual key you used

                if (patientId.isNullOrEmpty()) {
                    showToast("Patient ID not found in session.")
                } else {
                    // Now, use patientId in your Firebase update logic
                    val database = FirebaseDatabase.getInstance()
                    val reference = database.getReference("patient").child(patientId)

                    // ... Firebase update logic
                    // Create a map with the field(s) you want to update
                    val updates = HashMap<String, Any>()
                    updates["paymentStatus"] = "success" // Replace with your actual field and value

                    // Update the specific fields in the patient node
                    reference.updateChildren(updates)
                        .addOnSuccessListener {
                            showToast("Payment done successfully.")
                        }
                        .addOnFailureListener {
                            showToast("Failed to update payment. ${it.message}")
                        }
                    // Payment mode, navigate to the welcome activity
                    val intent = Intent(this@PaymentActivity, WelcomeActivity::class.java)
                    startActivity(intent)
                }

                showToast("Data is valid!")
            } else {
                showToast("Invalid data. Please enter valid information.")
            }
        }
    }

    private fun isValidCardNumber(cardNumber: String, cardHolderName: String): Boolean {
        // Basic validation: Check if the credit card number is numeric and has 16 digits -- 4111111111111111
        if (cardNumber.isEmpty() || !cardNumber.matches(Regex("\\d{16}"))) {
            return false
        }

        if (cardHolderName.isEmpty()) {
            return false
        }

        // Additional validation: Check if the entered card number is in the blocked list
        val blockedCardNumbers = listOf("1234567890123456", "9876543210987654", "1111222233334444", "4111111111111111")
        if (blockedCardNumbers.contains(cardNumber)) {
            // Show an error because the card number is blocked
            showToast("Credit card is blocked or incorrect!")
            return false
        }

        // The credit card number passed all validations
        return true
    }

    private fun isValidCVV(cvv: String): Boolean {
        // Basic validation: Check if the CVV is a valid three-digit number
        return cvv.isNotEmpty() && cvv.matches(Regex("\\d{3}"))
    }

    private fun isValidMonthAndYear(month: String, year: String): Boolean {
        // Basic validation: Check if the month is a valid two-digit number (01-12)

        if (month.isEmpty() || !month.matches(Regex("^(0[1-9]|1[0-2])$"))) {
            return false
        }

        // Basic validation: Check if the year is a valid four-digit number
        if (year.isEmpty() || !year.matches(Regex("^\\d{4}$"))) {
            return false
        }

        // Additional validation: Check if the date is before the current month and year
        val currentDate = Calendar.getInstance()
        val enteredMonth = month.toInt()
        val enteredYear = year.toInt()

        if (enteredMonth < 1 || enteredMonth > 12) {
            showToast("Invalid month")
            return false
        }

        if (enteredYear < currentDate.get(Calendar.YEAR)) {
            showToast("Invalid year")
            return false
        }

        if (enteredYear == currentDate.get(Calendar.YEAR) && enteredMonth < currentDate.get(Calendar.MONTH) + 1) {
            showToast("Invalid month and year")
            return false
        }
        return true
    }

    private fun showToast(message: String) {
        // Display a toast message (you can replace this with your desired UI feedback)
        // For simplicity, I'm not using a Toast in this example
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        println(message)
    }
}