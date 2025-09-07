package com.example.khaugali   // 👈 match your package structure

import java.io.Serializable

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val imageUri: String?,
    val userType: String
) : Serializable
