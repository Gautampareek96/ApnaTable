package com.example.khaugali

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

// 👇 Make sure this import exists
import com.example.khaugali.LoginActivity

class User_Type : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_type)

        val buisness = findViewById<MaterialButton>(R.id.Buisness)
        val customer = findViewById<MaterialButton>(R.id.Customer)

        buisness.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("userType", "business")
            startActivity(intent)
            finish()
        }

        customer.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("userType", "customer")
            startActivity(intent)
            finish()
        }
    }
}
