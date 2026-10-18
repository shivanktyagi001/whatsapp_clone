package com.example.whatsapp_clone.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsapp_clone.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel @JvmOverloads constructor(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val currentUserId: String
        get() = repository.currentUserId ?: ""

    fun initChat(chatId: String, otherUserId: String, otherUserName: String) {
        _uiState.update {
            it.copy(
                chatId = chatId,
                otherUserId = otherUserId,
                otherUserName = otherUserName
            )
        }
        observeMessages(chatId)
    }

    private fun observeMessages(chatId: String) {
        viewModelScope.launch {
            repository.getMessages(chatId).collect { msgList ->
                _uiState.update { it.copy(messages = msgList) }
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        val chatId = _uiState.value.chatId
        val receiverId = _uiState.value.otherUserId

        if (text.isEmpty() || chatId.isEmpty() || receiverId.isEmpty()) return

        // Clear input immediately for smooth UX
        _uiState.update { it.copy(inputText = "") }

        repository.sendMessage(
            chatId = chatId,
            receiverId = receiverId,
            text = text
        )
    }
}

