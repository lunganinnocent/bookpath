package com.example.bookpath

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.example.bookpath.databinding.ActivityAddTaskBinding // <-- Import the binding class
import java.util.Calendar

class AddTask : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    // Declare a variable for the binding class
    private lateinit var binding: ActivityAddTaskBinding

    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0
    private var savedHour = 0
    private var savedMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout and initialize the binding object
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root) // Use the binding's root view

        pickDate()
    }

    private fun getDateTimeCalendar(): Calendar {
        return Calendar.getInstance()
    }

    private fun pickDate() {
        // Access views directly through the binding object
        binding.btnTimePicker.setOnClickListener {
            val cal = getDateTimeCalendar()
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            DatePickerDialog(this, this, year, month, day).show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month + 1 // Month is 0-based, add 1 for display
        savedYear = year

        val cal = getDateTimeCalendar()
        val hour = cal.get(Calendar.HOUR_OF_DAY) // Use HOUR_OF_DAY for 24-hour format
        val minute = cal.get(Calendar.MINUTE)
        TimePickerDialog(this, this, hour, minute, true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = minute

        // Use the binding object to access tv_TextTime
        // Also, use string templates for cleaner formatting
        binding.tvTextTime.text = "$savedDay-$savedMonth-$savedYear\n Hour: $savedHour Minute: $savedMinute"
    }
}




