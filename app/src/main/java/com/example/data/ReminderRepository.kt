package com.example.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()

    suspend fun getReminderById(id: Int): Reminder? = reminderDao.getReminderById(id)

    suspend fun insert(reminder: Reminder): Long = reminderDao.insertReminder(reminder)

    suspend fun update(reminder: Reminder) = reminderDao.updateReminder(reminder)

    suspend fun deleteById(id: Int) = reminderDao.deleteReminderById(id)
}
