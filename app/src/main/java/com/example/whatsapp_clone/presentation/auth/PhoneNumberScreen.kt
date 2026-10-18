package com.example.whatsapp_clone.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PhoneNumberScreen(
    onNavigateToOtp: (fullPhoneNumber: String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    // Collect the reactive state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Top: Header & Instructions ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter your phone number",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF008069) // WhatsApp Green
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "WhatsApp will need to verify your phone number. Carrier charges may apply.",
                fontSize = 13.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Country Selector (India / +91)
            Text(
                text = uiState.countryName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color(0xFF00A884)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Phone Number Input Row
            Row(
                modifier = Modifier.fillMaxWidth(0.7f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country Code (+91)
                Text(
                    text = uiState.countryCode,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Input Field for Phone Number
                OutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = { viewModel.onPhoneNumberChanged(it) },
                    placeholder = { Text(text = "phone number", color = Color.LightGray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF00A884),
                        unfocusedIndicatorColor = Color.LightGray
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            // Error Message (if any)
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    fontSize = 12.sp,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- Bottom: "NEXT" Button & Loader ---
        val context = androidx.compose.ui.platform.LocalContext.current
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = Color(0xFF00A884),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    val activity = context as? android.app.Activity
                    viewModel.onNextClicked(
                        activity = activity,
                        onSuccess = {
                            val fullNumber = "${uiState.countryCode}${uiState.phoneNumber}"
                            onNavigateToOtp(fullNumber)
                        }
                    )
                },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A884)),
                modifier = Modifier
                    .width(120.dp)
                    .height(42.dp)
            ) {
                Text(
                    text = "NEXT",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}