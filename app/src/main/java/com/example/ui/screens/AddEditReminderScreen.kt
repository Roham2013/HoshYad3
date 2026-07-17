package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.Reminder
import com.example.utils.JalaliDateUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    viewModel: MainViewModel,
    reminderId: Int?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val reminders by viewModel.reminders.collectAsState()
    val existingReminder = reminderId?.let { id -> reminders.find { it.id == id } }

    var title by remember { mutableStateOf(existingReminder?.title ?: "") }
    var description by remember { mutableStateOf(existingReminder?.description ?: "") }

    val initialDateParts = if (existingReminder != null) {
        JalaliDateUtils.getJalaliDateParts(existingReminder.timeMillis)
    } else {
        JalaliDateUtils.getJalaliDateParts(System.currentTimeMillis())
    }

    var year by remember { mutableStateOf(initialDateParts[0].toString()) }
    var month by remember { mutableStateOf(initialDateParts[1].toString()) }
    var day by remember { mutableStateOf(initialDateParts[2].toString()) }

    val initialCal = Calendar.getInstance().apply {
        if (existingReminder != null) timeInMillis = existingReminder.timeMillis
    }
    var hour by remember { mutableStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(initialCal.get(Calendar.MINUTE)) }

    var bgColor by remember { mutableStateOf(existingReminder?.backgroundColor ?: Color(0xFF1E88E5).toArgb()) }
    var textColor by remember { mutableStateOf(existingReminder?.textColor ?: Color.White.toArgb()) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            hour = selectedHour
            minute = selectedMinute
        },
        hour,
        minute,
        true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingReminder == null) "افزودن یادآور" else "ویرایش یادآور") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("توضیحات") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text("تاریخ شمسی", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = day,
                    onValueChange = { day = it },
                    label = { Text("روز") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("ماه") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("سال") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.5f)
                )
            }

            Text("زمان", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(
                onClick = { timePickerDialog.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("انتخاب ساعت: ${String.format("%02d:%02d", hour, minute)}")
            }

            Text("رنگ پس‌زمینه هشدار", style = MaterialTheme.typography.titleMedium)
            ColorPicker(selectedColor = bgColor, onColorSelected = { bgColor = it })

            Text("رنگ متن هشدار", style = MaterialTheme.typography.titleMedium)
            ColorPicker(selectedColor = textColor, onColorSelected = { textColor = it })

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val y = year.toIntOrNull() ?: initialDateParts[0]
                    val m = month.toIntOrNull() ?: initialDateParts[1]
                    val d = day.toIntOrNull() ?: initialDateParts[2]
                    
                    val timeMillis = JalaliDateUtils.getMillisFromJalali(y, m, d, hour, minute)
                    
                    val reminder = Reminder(
                        id = existingReminder?.id ?: 0,
                        title = title.ifBlank { "بدون عنوان" },
                        description = description,
                        timeMillis = timeMillis,
                        backgroundColor = bgColor,
                        textColor = textColor,
                        isActive = true
                    )
                    
                    if (existingReminder == null) {
                        viewModel.addReminder(reminder)
                    } else {
                        viewModel.updateReminder(reminder)
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("ذخیره", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun ColorPicker(selectedColor: Int, onColorSelected: (Int) -> Unit) {
    val colors = listOf(
        Color(0xFF1E88E5), Color(0xFFE53935), Color(0xFF43A047),
        Color(0xFF8E24AA), Color(0xFFFDD835), Color(0xFF000000), Color(0xFFFFFFFF)
    )
    
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = 2.dp,
                        color = if (selectedColor == color.toArgb()) MaterialTheme.colorScheme.primary else Color.Gray,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color.toArgb()) }
            )
        }
    }
}
