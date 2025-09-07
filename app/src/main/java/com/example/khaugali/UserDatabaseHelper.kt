package com.example.khaugali

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "UserDB", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE Users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "email TEXT, " +
                    "password TEXT, " +
                    "imageUri TEXT, " +
                    "userType TEXT)"
        )

        // ✅ Menu table (linked with userId)
        db.execSQL(
            "CREATE TABLE Menu (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userId INTEGER, " +
                    "name TEXT, " +
                    "category TEXT, " +
                    "price TEXT, " +
                    "rating REAL, " +
                    "prepTime TEXT, " +
                    "ingredients TEXT, " +
                    "images TEXT, " +
                    "FOREIGN KEY(userId) REFERENCES Users(id))"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Users")
        db.execSQL("DROP TABLE IF EXISTS Menu")
        onCreate(db)
    }

    // ✅ Add userType as parameter
    fun insertUser(name: String, email: String, password: String, imageUri: String?, userType: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("name", name)
        contentValues.put("email", email)
        contentValues.put("password", password)
        contentValues.put("imageUri", imageUri)
        contentValues.put("userType", userType) // ✅ Now this works
        val result = db.insert("Users", null, contentValues)
        db.close()
        return result != -1L
    }

    // ✅ Add userType as parameter for filtering
    fun getUser(identifier: String, password: String, userType: String): User? {
        val db = this.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM Users WHERE (email=? OR name=?) AND password=? AND userType=?",
            arrayOf(identifier, identifier, password, userType)
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val emailDb = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri"))
            val userType = cursor.getString(cursor.getColumnIndexOrThrow("userType"))

            user = User(id, name, emailDb, imageUri, userType) // ✅ all params
        }

        cursor.close()
        db.close()
        return user
    }

    // ---------- MENU METHODS ----------

    fun insertMenuItem(menuItem: MenuItem, userId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("userId", userId)
            put("name", menuItem.name)
            put("category", menuItem.category)
            put("price", menuItem.price.toString())
            put("rating", menuItem.rating)
            put("prepTime", menuItem.prepTime)
            put("ingredients", menuItem.ingredients)
            put("images", menuItem.images.joinToString(",")) // convert list to string
        }
        db.insert("Menu", null, values)
        db.close()
    }

    fun getMenuItems(userId: Int): List<MenuItem> {
        val list = mutableListOf<MenuItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Menu WHERE userId = ?", arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val price = cursor.getString(cursor.getColumnIndexOrThrow("price"))
                val rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating"))
                val prepTime = cursor.getString(cursor.getColumnIndexOrThrow("prepTime"))
                val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients"))
                val images = cursor.getString(cursor.getColumnIndexOrThrow("images")).split(",")

                list.add(MenuItem(name, images, category, price, rating, prepTime, ingredients))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
    }
}
