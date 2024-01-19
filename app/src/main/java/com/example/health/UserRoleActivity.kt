package com.example.health

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

private lateinit var imageButton3: ImageButton
private lateinit var imageButton4: ImageButton

class UserRoleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_role)
        supportActionBar!!.hide() // to hide the app bar from this page

        imageButton3 = findViewById(R.id.imageButton3)
        imageButton4 = findViewById(R.id.imageButton4)

        imageButton3.setOnClickListener {
            // starting new activity.
            val i = Intent(this@UserRoleActivity, PatientLoginActivity::class.java)
            startActivity(i)
        }

        imageButton4.setOnClickListener {
            // starting new activity.
            val i = Intent(this@UserRoleActivity, DoctorLoginActivity::class.java)
            startActivity(i)
        }
    }
}