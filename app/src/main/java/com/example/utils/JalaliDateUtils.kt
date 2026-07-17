package com.example.utils

import android.icu.util.Calendar
import android.icu.util.ULocale

object JalaliDateUtils {
    fun formatJalaliDate(timeMillis: Long): String {
        val locale = ULocale("fa_IR@calendar=persian")
        val calendar = Calendar.getInstance(locale)
        calendar.timeInMillis = timeMillis

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return String.format("%04d/%02d/%02d", year, month, day)
    }

    fun getJalaliDateParts(timeMillis: Long): IntArray {
        val locale = ULocale("fa_IR@calendar=persian")
        val calendar = Calendar.getInstance(locale)
        calendar.timeInMillis = timeMillis
        return intArrayOf(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun getMillisFromJalali(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val locale = ULocale("fa_IR@calendar=persian")
        val calendar = Calendar.getInstance(locale)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
