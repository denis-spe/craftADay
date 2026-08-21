package com.den.craftaday.backend.alarmManager

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
class MapAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(
        collectionId: String = "", // Default to empty for root maps
        mapId: String,
        nodeId: String,
        nodeTitle: String,
        timestamp: Timestamp,
        status: String = "TODO"
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.den.craftaday.ACTION_TASK_REMINDER"
            putExtra("collectionId", collectionId)
            putExtra("mapId", mapId)
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
            Log.w("MapAlarmManager", "Attempted to schedule alarm in the past for node $nodeId")
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d("MapAlarmManager", "Scheduled EXACT alarm for node $nodeId at $triggerAtMillis")
            } else {
                // Fallback for missing permission or older devices
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d("MapAlarmManager", "Scheduled INEXACT fallback alarm for node $nodeId at $triggerAtMillis")
            }
        } catch (e: Exception) {
            Log.e("MapAlarmManager", "Error scheduling alarm: ${e.message}")
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
        Log.d("MapAlarmManager", "Cancelled alarm for node $nodeId")
    }
}
