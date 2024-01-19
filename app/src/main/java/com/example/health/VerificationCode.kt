package com.example.health

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.text.*
import android.widget.*
import androidx.core.content.ContextCompat

private lateinit var backButton: Button
private lateinit var errorTextView: TextView
private lateinit var editTextNumber1: EditText
private lateinit var editTextNumber2: EditText
private lateinit var editTextNumber3: EditText
private lateinit var editTextNumber4: EditText
private lateinit var verifyButton: Button

class VerificationCode : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_code)

        // this helped to show app bar and icon
        supportActionBar!!.setDisplayShowTitleEnabled(false); // this is to hide project name
        supportActionBar!!.setDisplayShowHomeEnabled(true);
        supportActionBar!!.setIcon(R.drawable.original_logo_white)

        // Change the color of the status bar
        changeStatusBarColor()

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Finish the current activity to go back to WelcomeActivity
            finish()
        }

        errorTextView = findViewById(R.id.errorTextView)
        editTextNumber1 = findViewById(R.id.editTextNumber1)
        editTextNumber2 = findViewById(R.id.editTextNumber2)
        editTextNumber3 = findViewById(R.id.editTextNumber3)
        editTextNumber4 = findViewById(R.id.editTextNumber4)
        verifyButton = findViewById(R.id.verifyButton)

        setupEditTextListeners()

        verifyButton.setOnClickListener {

            // Get the entered username and password
            val editTextNumber1 = editTextNumber1.text.toString().trim()
            val editTextNumber2 = editTextNumber2.text.toString().trim()
            val editTextNumber3 = editTextNumber3.text.toString().trim()
            val editTextNumber4 = editTextNumber4.text.toString().trim()

            if (editTextNumber1.isEmpty() || editTextNumber2.isEmpty() || editTextNumber3.isEmpty() || editTextNumber4.isEmpty()) {
                showError("Please enter your verification code")
                return@setOnClickListener
            }
            // Validate the entered verification code
            if (validateVerificationCode()) {
                val intent = Intent(this@VerificationCode, PatientWelcomeActivity::class.java)
                startActivity(intent)
                hideError()
                Toast.makeText(this, "Verification successful", Toast.LENGTH_SHORT).show()
            } else {
                showError("Invalid verification code")
            }
        }
    }

    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.skyblue)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                // Handle the Logout action (e.g., navigate to the logout screen)
                //performLogout()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appbar, menu)
        return true
    }

    private fun setupEditTextListeners() {
        val editTexts = arrayOf(editTextNumber1, editTextNumber2, editTextNumber3, editTextNumber4)

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Move to the next EditText when a digit is entered
                    if (count == 1 && i < editTexts.size - 1) {
                        editTexts[i + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    editTexts[i].error = null
                }
            })
        }
    }

    private fun validateVerificationCode(): Boolean {
        // Check if the entered verification code is correct
        val enteredCode =
            "${editTextNumber1.text}${editTextNumber2.text}${editTextNumber3.text}${editTextNumber4.text}"
        return enteredCode == "1234" // Replace with your actual verification code
    }

    private fun showError(errorMessage: String) {
        errorTextView.text = errorMessage
        errorTextView.visibility = View.VISIBLE
    }

    private fun hideError() {
        errorTextView.visibility = View.GONE
    }
}