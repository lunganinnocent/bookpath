package com.example.bookpath

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookpath.com.example.bookpath.models.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

class ViewTask : AppCompatActivity() {

    private lateinit var taskTitle: TextView
    private lateinit var taskCategory: TextView
    private lateinit var taskStatus: TextView
    private lateinit var taskStartDate: TextView
    private lateinit var taskEndDate: TextView
    private lateinit var taskDueTime: TextView
    private lateinit var taskDescription: TextView
    private lateinit var taskDocument: TextView
    private lateinit var editTaskBtn: Button
    private lateinit var deleteTaskBtn: Button
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var databaseRef: DatabaseReference
    private lateinit var taskId: String
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_task)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        taskId = intent.getStringExtra("task_id") ?: run {
            Toast.makeText(this, "Error: Task not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userId = intent.getStringExtra("logged_in_user_id") ?: run {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("tasks")

        initializeViews()
        setupClickListeners()
        loadTaskDetails()
        setupBottomNav()
    }

    private fun initializeViews() {
        taskTitle = findViewById(R.id.taskTitle)
        taskCategory = findViewById(R.id.taskCategory)
        taskStatus = findViewById(R.id.taskStatus)
        taskStartDate = findViewById(R.id.taskStartDate)
        taskEndDate = findViewById(R.id.taskEndDate)
        taskDueTime = findViewById(R.id.taskDueTime)
        taskDescription = findViewById(R.id.taskDescription)
        taskDocument = findViewById(R.id.taskDocument)
        editTaskBtn = findViewById(R.id.editTaskBtn)
        deleteTaskBtn = findViewById(R.id.deleteTaskBtn)
        bottomNav = findViewById(R.id.bottom_navigation)
    }

    private fun setupClickListeners() {
        editTaskBtn.setOnClickListener {
            val intent = Intent(this, AddTask::class.java)
            intent.putExtra("task_id", taskId)
            intent.putExtra("logged_in_user_id", userId)
            startActivity(intent)
        }

        deleteTaskBtn.setOnClickListener {
            deleteTask()
        }

        taskDocument.setOnClickListener {
            val documentUrl = taskDocument.text.toString()
            if (documentUrl.isNotEmpty() && documentUrl != getString(R.string.document_default)) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(documentUrl))
                startActivity(intent)
            }
        }
    }

    private fun loadTaskDetails() {
        databaseRef.child(taskId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val task = snapshot.getValue(Task::class.java)
                if (task != null) {
                    displayTaskDetails(task)
                } else {
                    Toast.makeText(this@ViewTask, "Task not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewTask, "Error loading task", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayTaskDetails(task: Task) {
        taskTitle.text = task.category
        taskCategory.text = task.category
        taskStatus.text = task.status
        taskStartDate.text = task.startDate
        taskEndDate.text = task.endDate
        taskDueTime.text = task.dueTime
        taskDescription.text = task.description

        if (task.documentUpload.isNotEmpty()) {
            taskDocument.text = task.documentUpload
        } else {
            taskDocument.text = getString(R.string.document_default)
        }

        when (task.status.lowercase()) {
            "completed" -> taskStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            "in progress" -> taskStatus.setTextColor(getColor(android.R.color.holo_orange_dark))
            "pending" -> taskStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            else -> taskStatus.setTextColor(getColor(android.R.color.holo_blue_dark))
        }
    }

    private fun deleteTask() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { dialog, which ->
                databaseRef.child(taskId).removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Error deleting task", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.task_dashboard
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.add_task -> {
                    startActivity(Intent(this, AddTask::class.java))
                    true
                }
                R.id.task_dashboard -> {
                    // Already here
                    true
                }
                R.id.chat -> {
                    startActivity(Intent(this, ChatBot::class.java))
                    true
                }
                R.id.profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    true
                }
                else -> false
            }
        }
    }
}