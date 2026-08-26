// Glory be to name LORD GOD
package com.den.craftaday.backend.alarmManager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.den.craftaday.backend.entities.TaskEntity
import com.den.craftaday.helper.toLocalTimeDate
import com.google.firebase.Timestamp
import com.google.firebase.firestore.model.Values.timestamp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val ACTION_TASK_REMINDER = "com.den.craftaday.ACTION_TASK_ALARM"
        const val TAG = "TaskAlarmManager"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(
        task: TaskEntity
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TASK_REMINDER
            putExtra("collectionId", task.collectionId)
        }

        // Use taskId.hashCode() as a unique request code for each task
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = task.taskAlarmType.primaryTimestamp

        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.w(TAG, "Attempted to schedule alarm in the past for ${task.title}")
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled EXACT alarm for ${task.title} at $triggerAtMillis")
            } else {
                // Fallback for missing permission or older devices
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled INEXACT fallback alarm for ${task.title} at $triggerAtMillis")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm: ${e.message}")
        }
    }

    fun cancelAlarm(nodeId: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TASK_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            nodeId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled alarm for node $nodeId")
    }
}