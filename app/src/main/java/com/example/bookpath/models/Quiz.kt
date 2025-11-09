package com.example.bookpath.com.example.bookpath.models

data class Quiz(
    var quizId: String = "",
    var userId: String = "",
    var taskId: String = "",      // optional: related task
    var score: Double = 0.0,
    var dateOfCompletion: String = "",
    var timeOfCompletion: String = ""

)
