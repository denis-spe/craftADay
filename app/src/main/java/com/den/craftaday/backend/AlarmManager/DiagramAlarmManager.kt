package com.den.craftaday.backend.AlarmManager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.Timestamp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagramAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(
        projectId: String,
        nodeId: String,
        nodeTitle: String,
        timestamp: Timestamp,
        status: String = "TODO"
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.den.craftaday.ACTION_TASK_REMINDER"
            putExtra("projectId", projectId)
            putExtra("nodeId", nodeId)
            putExtra("nodeTitle", nodeTitle)
            putExtra("status", status)
        }

        // Use nodeId.hashCode() as a unique request code for each node
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            nodeId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = timestamp.toDate().time
        
        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.w("DiagramAlarmManager", "Attempted to schedule alarm in the past for node $nodeId")
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d("DiagramAlarmManager", "Scheduled EXACT alarm for node $nodeId at $triggerAtMillis")
            } else {
                // Fallback for missing permission or older devices
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d("DiagramAlarmManager", "Scheduled INEXACT fallback alarm for node $nodeId at $triggerAtMillis")
            }
        } catch (e: Exception) {
            Log.e("DiagramAlarmManager", "Error scheduling alarm: ${e.message}")
        }
    }

    fun cancelAlarm(nodeId: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.den.craftaday.ACTION_TASK_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            nodeId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("DiagramAlarmManager", "Cancelled alarm for node $nodeId")
    }
}
