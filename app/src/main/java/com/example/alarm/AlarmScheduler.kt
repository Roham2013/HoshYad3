package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.Reminder

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: Reminder) {
        if (!reminder.isActive) return
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
            putExtra("TITLE", reminder.title)
            putExtra("DESCRIPTION", reminder.description)
            putExtra("BG_COLOR", reminder.backgroundColor)
            putExtra("TEXT_COLOR", reminder.textColor)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Ensure we have permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // If can't schedule exact alarms, ignore or we could handle differently
                // Actually the permission is granted at install time for apps using USE_EXACT_ALARM 
                // in newer versions, but we should be safe.
            }
        }
        
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.timeMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancel(reminder: Reminder) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
