package com.example.health

import androidx.appcompat.app.AppCompatActivity
import android.os.*
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat

private lateinit var welcomeTextView: TextView

class PatientWelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_welcome)

        // this helped to show app bar and icon
        supportActionBar!!.setDisplayShowTitleEnabled(false) // this is to hide project name
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setIcon(R.drawable.original_logo_white)

        welcomeTextView = findViewById(R.id.welcomeTextView)

        // Get a reference to the "btn_frag" button from the layout
        val btn: Button = findViewById(R.id.fragmentBtn2)

        // Set an OnClickListener on the button
        btn.setOnClickListener {
            // When the button is clicked, replace
            // the current fragment with a new
            // instance of the FirstFragment
            replaceFragment(FirstFragment())

            // Hide the button
            btn.visibility = View.GONE
        }
        // Change the color of the status bar
        changeStatusBarColor()
    }

    // This method replaces the current fragment
    // with a new fragment
    fun replaceFragment(fragment: Fragment) {
        // Get a reference to the FragmentManager
        val fragmentManager = supportFragmentManager

        // Start a new FragmentTransaction
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Replace the current fragment with the new fragment
        fragmentTransaction.replace(R.id.frame_layout, fragment)

        // Commit the FragmentTransaction
        fragmentTransaction.commit()
    }

    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.skyblue)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appbar, menu)
        return true
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
}