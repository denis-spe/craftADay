// Great is the LORD GOD of hosts
package com.den.craftaday.backend.alarmManager

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.den.craftaday.MainActivity
import com.den.craftaday.R
import com.den.craftaday.backend.entities.TaskEntity
import com.den.craftaday.backend.entities.types.MarkType
import com.den.craftaday.backend.entities.types.TaskAlarmType
import com.den.craftaday.backend.repositories.services.AccountService
import com.den.craftaday.backend.repositories.services.DataStorageService
import com.den.craftaday.helper.toTitle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskAlarmReceiver: BroadcastReceiver() {
    @Inject
    lateinit var dataStorageService: DataStorageService

    @Inject
    lateinit var accountService: AccountService

    @Inject
    lateinit var taskAlarmManager: TaskAlarmManager

    companion object {
        const val TAG = "TaskAlarmReceiver"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val collectionId = intent.getStringExtra("collectionId") ?: return
        val taskId = intent.getStringExtra("taskId") ?: return
        val taskTitle = intent.getStringExtra("taskTitle") ?: "Task Reminder"

        val userId = accountService.currentUserId
        if (userId.isEmpty()) return

        if (action == context.getString(R.string.action_task_done)) {
            handleMarkDone(context, userId, collectionId, taskId)
            return
        }

        if (action == context.getString(R.string.action_task_failed)) {
            handleMarkFailed(context, userId, collectionId, taskId)
            return
        }

        // Show notification immediately using available intent data
        showNotification(
            context,
            taskId.hashCode(),
            TaskEntity(id = taskId, collectionId = collectionId, title = taskTitle)
        )

        val pendingResult = goAsync()
        scope.launch {
            try {
                val task = dataStorageService.getTask(userId, collectionId, taskId).firstOrNull()

                if (task != null) {
                    // Update notification with full data if needed (e.g. description or latest title)
                    showNotification(context, taskId.hashCode(), task)

                    if (task.taskAlarmType !is TaskAlarmType.Once) {
                        taskAlarmManager.scheduleAlarm(task)
                    }
                    Log.d(TAG, "Scheduled alarm for ${task.title}")
                } else {
                    Log.e(TAG, "Task with ID $taskId not found in collection $collectionId")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error occurred while fetching task", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        task: TaskEntity
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = context.getString(R.string.action_task_done)
            putExtra("collectionId", task.collectionId)
            putExtra("taskId", task.id)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context, task.id.hashCode() + 1, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val failedIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = context.getString(R.string.action_task_failed)
            putExtra("collectionId", task.collectionId)
            putExtra("taskId", task.id)
        }
        val failedPendingIntent = PendingIntent.getBroadcast(
            context, task.id.hashCode() + 2, failedIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val title = task.title.toTitle

        val text = when (task.markType) {
            MarkType.Initial -> "$title task not yet marked."
            MarkType.Done -> "$title task was previously marked as done."
            MarkType.Failed -> "$title task was previously marked as failed."
            MarkType.InProgress -> "$title task is currently in progress."
        }

        val builder = NotificationCompat.Builder(context, "task_reminders")
            .setSmallIcon(R.drawable.ic_task_done)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_task_done, "Mark as Done", donePendingIntent)
            .addAction(R.drawable.ic_task_failed, "Mark as Failed", failedPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                Log.e(TAG, "Missing permission to show notification: ${e.message}")
            }
        }
    }

    fun handleMarkDone(
        context: Context,
        userId: String,
        collectionId: String,
        taskId: String
    ) {
        val pendingResult = goAsync()

        scope.launch {
            try {
                val taskEntity = dataStorageService.getTask(userId, collectionId, taskId).firstOrNull()

                if (taskEntity != null) {
                    dataStorageService.updateTask(
                        userId,
                        collectionId,
                        taskEntity.copy(markType = MarkType.Done)
                    )

                } else {
                    Log.e(TAG, "Task with ID $taskId not found in collection $collectionId")
                }
            } finally {
                pendingResult.finish()
            }

            // Dismiss the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId.hashCode())
        }
    }

    fun handleMarkFailed(
        context: Context,
        userId: String,
        collectionId: String,
        taskId: String
    ) {
        val pendingResult = goAsync()

        scope.launch {
            try {
                val taskEntity = dataStorageService.getTask(userId, collectionId, taskId).firstOrNull()

                if (taskEntity != null) {
                    dataStorageService.updateTask(
                        userId,
                        collectionId,
                        taskEntity.copy(markType = MarkType.Failed)
                    )

                } else {
                    Log.e(TAG, "Task with ID $taskId not found in collection $collectionId")
                }
            } finally {
                pendingResult.finish()
            }

            // Dismiss the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(taskId.hashCode())
        }
    }

}
