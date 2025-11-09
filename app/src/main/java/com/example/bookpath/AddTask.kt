package com.example.bookpath

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bookpath.com.example.bookpath.models.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AddTask : AppCompatActivity() {

    private lateinit var categoryEdit: EditText
    private lateinit var descriptionEdit: EditText
    private lateinit var documentEdit: EditText
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var dueTimeEdit: EditText
    private lateinit var saveBtn: Button
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var statusSpinner: Spinner

    private lateinit var userId: String
    private lateinit var taskId: String
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        // Get user ID and check if editing
        userId = intent.getStringExtra("logged_in_user_id") ?: run {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        taskId = intent.getStringExtra("task_id") ?: ""
        isEditing = taskId.isNotEmpty()

        initializeViews()
        setupStatusSpinner()

        if (isEditing) {
            loadTaskData()
            saveBtn.text = "UPDATE TASK"
        } else {
            saveBtn.text = "SAVE TASK"
        }

        setupClickListeners()
        setupBottomNav()
    }

    private fun initializeViews() {
        categoryEdit = findViewById(R.id.categoryEdit)
        descriptionEdit = findViewById(R.id.descriptionEdit)
        documentEdit = findViewById(R.id.documentEdit)
        startDateEditText = findViewById(R.id.startDateEditText)
        endDateEditText = findViewById(R.id.endDateEditText)
        dueTimeEdit = findViewById(R.id.dueTimeEdit)
        saveBtn = findViewById(R.id.saveBtn)
        bottomNav = findViewById(R.id.bottom_navigation)
        statusSpinner = findViewById(R.id.statusSpinner)
    }

    private fun setupStatusSpinner() {
        val statusOptions = arrayOf("Not Started", "In Progress", "Completed", "Pending")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        startDateEditText.setOnClickListener { showDatePicker(startDateEditText) }
        endDateEditText.setOnClickListener { showDatePicker(endDateEditText) }
        dueTimeEdit.setOnClickListener { showTimePicker() }
        saveBtn.setOnClickListener {
            if (isEditing) {
                updateTaskInFirebase()
            } else {
                saveTaskToFirebase()
            }
        }
    }

    private fun loadTaskData() {
        val ref = FirebaseDatabase.getInstance().getReference("tasks").child(taskId)
        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val task = snapshot.getValue(Task::class.java)
                if (task != null) {
                    categoryEdit.setText(task.category)
                    descriptionEdit.setText(task.description)
                    documentEdit.setText(task.documentUpload)
                    startDateEditText.setText(task.startDate)
                    endDateEditText.setText(task.endDate)
                    dueTimeEdit.setText(task.dueTime)

                    // Set the status in spinner
                    val statusOptions = arrayOf("Not Started", "In Progress", "Completed", "Pending")
                    val position = statusOptions.indexOf(task.status)
                    if (position >= 0) {
                        statusSpinner.setSelection(position)
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load task data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                editText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            this,
            { _, hour, minute ->
                val formattedTime = String.format("%02d:%02d", hour, minute)
                dueTimeEdit.setText(formattedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun saveTaskToFirebase() {
        val category = categoryEdit.text.toString().trim()
        val description = descriptionEdit.text.toString().trim()
        val startDate = startDateEditText.text.toString().trim()
        val endDate = endDateEditText.text.toString().trim()
        val dueTime = dueTimeEdit.text.toString().trim()
        val status = statusSpinner.selectedItem.toString()

        if (category.isEmpty() || description.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("tasks")
        val newTaskId = ref.push().key ?: return

        val task = Task(
            taskId = newTaskId,
            userId = userId,
            category = category,
            description = description,
            documentUpload = documentEdit.text.toString().trim(),
            startDate = startDate,
            endDate = endDate,
            dueTime = dueTime,
            status = status
        )

        ref.child(newTaskId).setValue(task).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()
                navigateToAssignmentOverview()
            } else {
                Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTaskInFirebase() {
        val category = categoryEdit.text.toString().trim()
        val description = descriptionEdit.text.toString().trim()
        val startDate = startDateEditText.text.toString().trim()
        val endDate = endDateEditText.text.toString().trim()
        val dueTime = dueTimeEdit.text.toString().trim()
        val status = statusSpinner.selectedItem.toString()

        if (category.isEmpty() || description.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("tasks").child(taskId)

        val updates = hashMapOf<String, Any>(
            "category" to category,
            "description" to description,
            "documentUpload" to documentEdit.text.toString().trim(),
            "startDate" to startDate,
            "endDate" to endDate,
            "dueTime" to dueTime,
            "status" to status
        )

        ref.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Task updated successfully!", Toast.LENGTH_SHORT).show()
                navigateToAssignmentOverview()
            } else {
                Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToAssignmentOverview() {
        val intent = Intent(this, AssignmentOverview::class.java)
        intent.putExtra("logged_in_user_id", userId)
        startActivity(intent)
        finish()
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.add_task
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("logged_in_user_id", userId)
                    startActivity(intent)
                    true
                }
                R.id.add_task -> true
                R.id.task_dashboard -> {
                    navigateToAssignmentOverview()
                    true
                }
                R.id.chat -> {
                    val intent = Intent(this, ChatBot::class.java)
                    intent.putExtra("logged_in_user_id", userId)
                    startActivity(intent)
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this, Profile::class.java)
                    intent.putExtra("logged_in_user_id", userId)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}