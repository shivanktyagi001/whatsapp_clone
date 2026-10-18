package com.example.whatsapp_clone.presentation.chat

import com.example.whatsapp_clone.data.model.Message

data class ChatUiState(
    val chatId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false
)

