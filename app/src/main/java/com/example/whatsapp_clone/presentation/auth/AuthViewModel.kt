package com.example.whatsapp_clone.presentation.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsapp_clone.data.model.User
import com.example.whatsapp_clone.data.repository.FirebaseRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AuthViewModel @JvmOverloads constructor(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onPhoneNumberChanged(phoneNumber: String) {
        val filtered = phoneNumber.filter { it.isDigit() }.take(10)
        _uiState.update { currentState ->
            currentState.copy(
                phoneNumber = filtered,
                errorMessage = null
            )
        }
    }

    fun onOtpCodeChanged(code: String) {
        val filtered = code.filter { it.isDigit() }.take(6)
        _uiState.update { currentState ->
            currentState.copy(
                otpCode = filtered,
                errorMessage = null
            )
        }
    }

    fun onUserNameChanged(name: String) {
        _uiState.update { it.copy(userName = name, errorMessage = null) }
    }

    fun onUserStatusChanged(status: String) {
        _uiState.update { it.copy(userStatus = status) }
    }

    fun onNextClicked(activity: Activity? = null, onSuccess: () -> Unit) {
        if (_uiState.value.phoneNumber.length < 10) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid 10-digit number") }
            return
        }
        if (activity != null) {
            sendVerificationCode(activity, onSuccess)
        } else {
            onSuccess()
        }
    }

    // --- Send Phone Verification Code ---
    fun sendVerificationCode(activity: Activity, onSuccess: () -> Unit) {
        val fullNumber = "${_uiState.value.countryCode}${_uiState.value.phoneNumber}"
        if (_uiState.value.phoneNumber.length < 10) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid 10-digit number") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant verification
                signInWithPhoneAuthCredential(credential) {
                    onSuccess()
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Verification failed. Try again."
                    )
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        verificationId = verificationId,
                        isCodeSent = true
                    )
                }
                startResendTimer()
                onSuccess()
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(fullNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun startResendTimer() {
        viewModelScope.launch {
            for (i in 60 downTo 0) {
                _uiState.update { it.copy(resendCountdown = i) }
                delay(1000L)
            }
        }
    }

    // --- Verify OTP Code ---
    fun verifyOtp(onSuccess: () -> Unit) {
        val code = _uiState.value.otpCode
        val verificationId = _uiState.value.verificationId

        if (code.length < 6) {
            _uiState.update { it.copy(errorMessage = "Please enter all 6 digits") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        if (verificationId.isNotEmpty()) {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential, onSuccess)
        } else {
            // Test fallback
            _uiState.update { it.copy(isLoading = false, isVerified = true) }
            onSuccess()
        }
    }

    private fun signInWithPhoneAuthCredential(
        credential: PhoneAuthCredential,
        onSuccess: () -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                _uiState.update { it.copy(isLoading = false) }
                if (task.isSuccessful) {
                    _uiState.update { it.copy(isVerified = true) }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(errorMessage = task.exception?.localizedMessage ?: "Invalid code")
                    }
                }
            }
    }

    // --- Save Profile Setup to Firestore ---
    fun saveUserProfile(onSuccess: () -> Unit) {
        val name = _uiState.value.userName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter your name") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val currentUser = User(
            uid = repository.currentUserId ?: "",
            phoneNumber = "${_uiState.value.countryCode}${_uiState.value.phoneNumber}",
            name = name,
            status = _uiState.value.userStatus
        )

        repository.saveUserProfile(currentUser) { success ->
            _uiState.update { it.copy(isLoading = false) }
            if (success) {
                _uiState.update { it.copy(isProfileCompleted = true) }
                onSuccess()
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to save profile. Try again.") }
            }
        }
    }
}