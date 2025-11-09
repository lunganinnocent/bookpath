package com.example.bookpath.com.example.bookpath.models

data class Task(
    var taskId: String = "",
    var userId: String = "",
    var category: String = "",
    var description: String = "",
    var documentUpload: String = "",
    var endDate: String = "",
    var startDate: String = "",
    var dueTime: String = "",
    var status: String = "Not Started"
)