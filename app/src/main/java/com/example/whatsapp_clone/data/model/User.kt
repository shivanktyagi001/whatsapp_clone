package com.example.whatsapp_clone.data.model

data class User(
    val uid: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val profilePicUrl: String = "",
    val status: String = "Hey there! I am using WhatsApp.",
    val createdAt: Long = System.currentTimeMillis()
)

