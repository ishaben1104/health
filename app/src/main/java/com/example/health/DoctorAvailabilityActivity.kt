package com.example.health

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import java.util.Locale

class DoctorAvailabilityActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val availabilityReference = database.getReference("doctor_availability")
    private lateinit var buttonSaveAvailability: Button

    private val daysOfWeek = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_availability)

        buttonSaveAvailability = findViewById(R.id.buttonSaveAvailability)

        buttonSaveAvailability.setOnClickListener {
            Log.d("DoctorAvailability", "Save button clicked")
            val sharedPreferences = getSharedPreferences("MySession", Context.MODE_PRIVATE)
            val doctorId = sharedPreferences.getString("doctorLoginId", "")

            if (!doctorId.isNullOrBlank()) {
                var shouldRedirect = true
                for (day in daysOfWeek) {
                    val checkBoxId = resources.getIdentifier("checkBox$day", "id", packageName)
                    val timeInId = resources.getIdentifier("editText${day}In", "id", packageName)
                    val timeOutId = resources.getIdentifier("editText${day}Out", "id", packageName)

                    val checkBox = findViewById<CheckBox>(checkBoxId)
                    val timeIn = findViewById<EditText>(timeInId)
                    val timeOut = findViewById<EditText>(timeOutId)

                    if (!(checkBox.isChecked && timeIn.text.isNotBlank() && timeOut.text.isNotBlank()) &&
                        (checkBox.isChecked || timeIn.text.isNotBlank() || timeOut.text.isNotBlank())
                    ) {
                        // If either checkbox or time fields are partially filled, show an error
                        shouldRedirect = false
                        Toast.makeText(
                            this,
                            "Please leave all checkboxes unchecked and all time fields empty or fill all 3 of them for each day to save.",
                            Toast.LENGTH_SHORT
                        ).show()
                        break
                    }
                }

                if (shouldRedirect) {
                    saveAvailability(doctorId)
                }
            } else {
                Toast.makeText(this, "Error: Doctor ID not found.", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up time picker for each day
        for (day in daysOfWeek) {
            val timeInId = resources.getIdentifier("editText${day}In", "id", packageName)
            val timeOutId = resources.getIdentifier("editText${day}Out", "id", packageName)

            val timeInEditText = findViewById<TextInputEditText>(timeInId)
            val timeOutEditText = findViewById<TextInputEditText>(timeOutId)

            timeInEditText.filters = arrayOf(FixedTimeFormatInputFilter())
            timeOutEditText.filters = arrayOf(FixedTimeFormatInputFilter())

            // Set OnClickListener for opening the TimePickerDialog
            setEditTextClickListener(timeInEditText)
            setEditTextClickListener(timeOutEditText)
        }
    }

    private fun setEditTextClickListener(editText: EditText) {
        editText.setOnClickListener { showTimePickerDialog(editText) }

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showTimePickerDialog(editText)
            }
        }
    }

    private fun showTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                editText.setText(formattedTime)
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }

    private fun saveAvailability(doctorId: String) {
        if (doctorId.isNotBlank()) {
            val doctorAvailabilityReference = availabilityReference.child(doctorId)
            for (day in daysOfWeek) {
                val checkBoxId = resources.getIdentifier("checkBox$day", "id", packageName)
                val timeInId = resources.getIdentifier("editText${day}In", "id", packageName)
                val timeOutId = resources.getIdentifier("editText${day}Out", "id", packageName)

                val checkBox = findViewById<CheckBox>(checkBoxId)
                val timeIn = findViewById<EditText>(timeInId).text.toString()
                val timeOut = findViewById<EditText>(timeOutId).text.toString()

                val dayReference = doctorAvailabilityReference.child(day)
                dayReference.child("isAvailable").setValue(checkBox.isChecked)
                dayReference.child("timeIn").setValue(timeIn)
                dayReference.child("timeOut").setValue(timeOut)
            }

            val intent = Intent(
                this@DoctorAvailabilityActivity,
                WelcomeActivity::class.java
            )
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Error: Doctor ID is null or empty.", Toast.LENGTH_SHORT).show()
        }
    }
}