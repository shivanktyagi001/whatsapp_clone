package com.example.whatsapp_clone.data.model

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserAvatar: String = ""
)

