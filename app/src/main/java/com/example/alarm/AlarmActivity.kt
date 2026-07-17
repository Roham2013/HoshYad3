package com.example.alarm

import android.app.NotificationManager
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

class AlarmActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var titleText: String = ""
    private var descText: String = ""
    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure the screen turns on and shows over the lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val reminderId = intent.getIntExtra("REMINDER_ID", -1)
        titleText = intent.getStringExtra("TITLE") ?: ""
        descText = intent.getStringExtra("DESCRIPTION") ?: ""
        val bgColor = intent.getIntExtra("BG_COLOR", android.graphics.Color.BLACK)
        val textColor = intent.getIntExtra("TEXT_COLOR", android.graphics.Color.WHITE)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(reminderId)

        tts = TextToSpeech(this, this)
        
        // Vibrate and sound fallback
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 1000, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, 0)
        }

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        ringtone?.play()

        setContent {
            var currentTime by remember { mutableStateOf(getCurrentTime()) }
            
            LaunchedEffect(Unit) {
                while(true) {
                    delay(1000)
                    currentTime = getCurrentTime()
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(bgColor)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentTime,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(textColor)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = titleText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(textColor)
                    )
                }

                IconButton(
                    onClick = {
                        vibrator.cancel()
                        ringtone?.stop()
                        tts.stop()
                        finish()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp)
                        .size(80.dp)
                        .background(Color(textColor).copy(alpha = 0.2f), shape = MaterialTheme.shapes.extraLarge)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Stop Alarm",
                        tint = Color(textColor),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locFa = Locale("fa", "IR")
            val result = tts.setLanguage(locFa)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale.ENGLISH)
            }
            speakText()
        }
    }

    private fun speakText() {
        val textToSpeak = "$titleText. $descText"
        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "AlarmTTS")
        
        // Loop the speech periodically
        Thread {
            while(!isDestroyed) {
                Thread.sleep(10000)
                if (!isDestroyed && !tts.isSpeaking) {
                    tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "AlarmTTS")
                }
            }
        }.start()
    }

    override fun onDestroy() {
        if (this::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        ringtone?.stop()
        super.onDestroy()
    }
}
