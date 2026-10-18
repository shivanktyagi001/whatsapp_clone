package com.example.whatsapp_clone.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsapp_clone.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel @JvmOverloads constructor(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadChats()
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            repository.getCurrentUserProfile().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getChats().collect { chatList ->
                _uiState.update { it.copy(chats = chatList, isLoading = false) }
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        repository.signOut()
        onSignedOut()
    }
}

