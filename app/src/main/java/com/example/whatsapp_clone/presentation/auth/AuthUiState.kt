package com.example.whatsapp_clone.presentation.auth

data class AuthUiState(
    val countryName: String = "India",
    val countryCode: String = "+91",
    val phoneNumber: String = "",
    val otpCode: String = "",
    val verificationId: String = "",
    val userName: String = "",
    val userStatus: String = "Hey there! I am using WhatsApp.",
    val resendCountdown: Int = 60,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCodeSent: Boolean = false,
    val isVerified: Boolean = false,
    val isProfileCompleted: Boolean = false
)