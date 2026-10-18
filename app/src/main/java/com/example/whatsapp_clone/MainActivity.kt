package com.example.whatsapp_clone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.whatsapp_clone.navigation.AppNavigation
import com.example.whatsapp_clone.ui.theme.Whatsapp_cloneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            Whatsapp_cloneTheme {
                AppNavigation()
            }
        }
    }
}