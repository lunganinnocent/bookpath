package com.example.bookpath

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookpath.com.example.bookpath.models.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

class AssignmentOverview : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var databaseRef: DatabaseReference
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var userId: String
    private lateinit var noTasksText: TextView
    private val taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assignment_overview)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userId = intent.getStringExtra("logged_in_user_id") ?: run {
            Toast.makeText(this, "Error: user not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("tasks")
        recyclerView = findViewById(R.id.recyclerView)
        noTasksText = findViewById(R.id.noTasksText)
        bottomNav = findViewById(R.id.bottom_navigation)

        setupRecyclerView()
        loadUserTasks()
        setupBottomNav()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(taskList) { task ->
            val intent = Intent(this, ViewTask::class.java)
            intent.putExtra("task_id", task.taskId)
            intent.putExtra("logged_in_user_id", userId)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskAdapter
    }

    private fun loadUserTasks() {
        databaseRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    taskList.clear()
                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        if (task != null) {
                            task.taskId = taskSnapshot.key ?: ""
                            taskList.add(task)
                        }
                    }

                    if (taskList.isEmpty()) {
                        noTasksText.visibility = TextView.VISIBLE
                        recyclerView.visibility = RecyclerView.GONE
                    } else {
                        noTasksText.visibility = TextView.GONE
                        recyclerView.visibility = RecyclerView.VISIBLE
                        taskAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AssignmentOverview, "Error loading tasks", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.task_dashboard
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        putExtra("logged_in_user_id", userId)
                    })
                    true
                }
                R.id.add_task -> {
                    startActivity(Intent(this, AddTask::class.java).apply {
                        putExtra("logged_in_user_id", userId)
                    })
                    true
                }
                R.id.task_dashboard -> true
                R.id.chat -> {
                    startActivity(Intent(this, ChatBot::class.java).apply {
                        putExtra("logged_in_user_id", userId)
                    })
                    true
                }
                R.id.profile -> {
                    startActivity(Intent(this, Profile::class.java).apply {
                        putExtra("logged_in_user_id", userId)
                    })
                    true
                }
                else -> false
            }
        }
    }
}

class TaskAdapter(
    private val tasks: List<Task>,
    private val onItemClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        val taskDescription: TextView = itemView.findViewById(R.id.taskDescription)
        val taskDueDate: TextView = itemView.findViewById(R.id.taskDueDate)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TaskViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item_layout, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.taskTitle.text = task.category
        holder.taskDescription.text = task.description
        holder.taskDueDate.text = "Due: ${task.endDate} ${task.dueTime}"

        holder.itemView.setOnClickListener {
            onItemClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size
}