package com.example.khaugali

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class MenuItem(
    val name: String,
    val images: List<String>,
    val category: String,
    val price: String,
    val rating: Float,
    val prepTime: String,
    val ingredients: String
)

class AddMenuActivity : AppCompatActivity() {

    private val PICK_IMAGES = 100
    private var selectedImageUris: List<Uri> = emptyList()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MenuAdapter
    private var menuList = mutableListOf<MenuItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_menu)

        val addMenuCapsule = findViewById<CardView>(R.id.addMenuCapsule)
        recyclerView = findViewById(R.id.recyclerMenu)

        // Load saved data
        menuList = loadMenuItems().toMutableList()

        adapter = MenuAdapter(menuList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        addMenuCapsule.setOnClickListener {
            showAddMenuForm()
        }
    }

    private fun showAddMenuForm() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_menu, null)

        val etProductName = dialogView.findViewById<EditText>(R.id.etProductName)
        val etCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.etCategory)
        val etPrice = dialogView.findViewById<EditText>(R.id.etPrice)
        val etPrepTime = dialogView.findViewById<EditText>(R.id.etPrepTime)
        val etIngredients = dialogView.findViewById<EditText>(R.id.etIngredients)
        val btnUploadImage = dialogView.findViewById<Button>(R.id.btnUploadImage)
        val etRating = dialogView.findViewById<EditText>(R.id.etRating)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)

        // Setup category dropdown
        val categories = listOf("Beverages", "Starters", "Soups", "Salads", "Main Course", "Desserts")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(adapter)
        etCategory.setOnClickListener { etCategory.showDropDown() }

        // Update stars when user types rating
        etRating.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val value = s.toString().toFloatOrNull()
                if (value != null && value in 0.0..5.0) {
                    ratingBar.rating = value
                } else {
                    ratingBar.rating = 0f
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Auto-add ₹ for price
        etPrice.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                val text = s.toString().replace("₹", "").trim()
                if (text.isNotEmpty()) {
                    etPrice.setText("₹$text")
                    etPrice.setSelection(etPrice.text.length)
                }
                isEditing = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Upload images
        btnUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(intent, PICK_IMAGES)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Menu Item")
            .setPositiveButton("Save") { _, _ ->
                if (etProductName.text.isNullOrEmpty() ||
                    etCategory.text.isNullOrEmpty() ||
                    etPrice.text.isNullOrEmpty() ||
                    etRating.text.isNullOrEmpty() ||
                    etPrepTime.text.isNullOrEmpty() ||
                    etIngredients.text.isNullOrEmpty()
                ) {
                    Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                } else {
                    val name = etProductName.text.toString()
                    val category = etCategory.text.toString()
                    val priceValue = etPrice.text.toString().replace("₹", "").trim()
                    val price = "₹$priceValue"
                    val rating = etRating.text.toString().toFloat()
                    val prepTime = etPrepTime.text.toString() + " min"
                    val ingredients = etIngredients.text.toString()

                    val menuItem = MenuItem(
                        name = name,
                        images = selectedImageUris.map { it.toString() },
                        category = category,
                        price = price,
                        rating = rating,
                        prepTime = prepTime,
                        ingredients = ingredients
                    )

                    saveMenuItem(menuItem)
                }
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.show()
    }

    private fun saveMenuItem(menuItem: MenuItem) {
        menuList.add(menuItem)
        adapter.notifyItemInserted(menuList.size - 1)

        val prefs = getSharedPreferences("menu_data", MODE_PRIVATE)
        val gson = Gson()
        prefs.edit().putString("menu_list", gson.toJson(menuList)).apply()
    }

    private fun loadMenuItems(): List<MenuItem> {
        val prefs = getSharedPreferences("menu_data", MODE_PRIVATE)
        val gson = Gson()
        val jsonList = prefs.getString("menu_list", "[]")
        val type = object : TypeToken<MutableList<MenuItem>>() {}.type
        return gson.fromJson(jsonList, type)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES && resultCode == Activity.RESULT_OK) {
            val uris = mutableListOf<Uri>()
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    uris.add(data.clipData!!.getItemAt(i).uri)
                }
            } else if (data?.data != null) {
                uris.add(data.data!!)
            }
            selectedImageUris = uris
            Toast.makeText(this, "Selected ${uris.size} images", Toast.LENGTH_SHORT).show()
        }
    }
}
