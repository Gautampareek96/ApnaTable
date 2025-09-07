package com.example.khaugali

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BusinessMenuActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_menu)

        // Initialize Drawer and Profile Views
        drawerLayout = findViewById(R.id.drawerLayout)
        profileImage = findViewById(R.id.profileImage)
        profileName = findViewById(R.id.profileName)
        profileLayout = findViewById(R.id.profileLayout)

        val drawerProfile = findViewById<TextView>(R.id.drawerProfile)
        val drawerAbout = findViewById<TextView>(R.id.drawerAbout)
        val drawerLogout = findViewById<TextView>(R.id.drawerLogout)

        val addMenuBtn = findViewById<Button>(R.id.Addmenu)
        val editMenuBtn = findViewById<Button>(R.id.Editmenu)

        // Load Business User Data
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val name = sharedPref.getString("loggedInName", "Log In")
        val imageUri = sharedPref.getString("loggedInImageUri", null)

        profileName.text = name

        if (!imageUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(imageUri)
                val inputStream = contentResolver.openInputStream(uri)
                val drawable = Drawable.createFromStream(inputStream, uri.toString())
                profileImage.setImageDrawable(drawable)
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
                profileImage.setImageResource(R.drawable.account)
            }
        } else {
            profileImage.setImageResource(R.drawable.account)
        }

        // Open Drawer when Profile Clicked
        profileLayout.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // Drawer Options
        drawerProfile.setOnClickListener {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
        }

        drawerAbout.setOnClickListener {
            Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show()
        }

        drawerLogout.setOnClickListener {
            sharedPref.edit().clear().apply()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, User_Type::class.java)
            startActivity(intent)
            finish()
        }

        // 🔹 Button Click Listeners
        addMenuBtn.setOnClickListener {
            val intent = Intent(this, AddMenuActivity::class.java)
            startActivity(intent)
        }

        editMenuBtn.setOnClickListener {
            val intent = Intent(this, EditMenuActivity::class.java)
            startActivity(intent)
        }

        // Apply Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainContent)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }
}
