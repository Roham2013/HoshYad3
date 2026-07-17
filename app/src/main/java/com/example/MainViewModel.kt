package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.data.AppDatabase
import com.example.data.Reminder
import com.example.data.ReminderRepository
import com.example.utils.ThemePrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReminderRepository
    private val alarmScheduler: AlarmScheduler
    private val themePrefs = ThemePrefs(application)

    val isDarkMode: Flow<Boolean?> = themePrefs.isDarkMode

    val reminders: StateFlow<List<Reminder>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReminderRepository(database.reminderDao())
        alarmScheduler = AlarmScheduler(application)

        reminders = repository.allReminders.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            val id = repository.insert(reminder)
            val newReminder = reminder.copy(id = id.toInt())
            alarmScheduler.schedule(newReminder)
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.update(reminder)
            if (reminder.isActive) {
                alarmScheduler.schedule(reminder)
            } else {
                alarmScheduler.cancel(reminder)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteById(reminder.id)
            alarmScheduler.cancel(reminder)
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            themePrefs.setDarkMode(isDark)
        }
    }
}
