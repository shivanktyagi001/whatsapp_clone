package com.example.whatsapp_clone.presentation.home

import com.example.whatsapp_clone.data.model.Chat
import com.example.whatsapp_clone.data.model.User

data class HomeUiState(
    val currentUser: User? = null,
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTabIndex: Int = 0
)

