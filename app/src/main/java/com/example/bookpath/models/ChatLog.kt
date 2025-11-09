package com.example.bookpath.com.example.bookpath.models

data class ChatLog(
    var chatId: String = "",
    var userId: String = "",
    var message: String = "",
    var timestamp: Long = 0L,
    var document: String = ""
)
