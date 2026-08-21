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
import com.den.craftaday.backend.blueprints.DataStorage
import com.den.craftaday.backend.blueprints.AccountService
import com.den.craftaday.helper.ReminderUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_DONE = "com.den.craftaday.ACTION_MARK_DONE"
        const val ACTION_MARK_FAILED = "com.den.craftaday.ACTION_MARK_FAILED"
    }

    @Inject
    lateinit var dataStorage: DataStorage

    @Inject
    lateinit var accountService: AccountService

    @Inject
    lateinit var alarmManager: MapAlarmManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val collectionId = intent.getStringExtra("collectionId") ?: ""
        val mapId = intent.getStringExtra("mapId") ?: return
        val nodeId = intent.getStringExtra("nodeId") ?: return
        val nodeTitle = intent.getStringExtra("nodeTitle") ?: "Task"
        val status = intent.getStringExtra("status") ?: "TODO"
        val userId = accountService.currentUserId
        if (userId.isEmpty()) return

        if (action == ACTION_MARK_DONE) {
            handleMarkDone(context, userId, collectionId, mapId, nodeId)
            return
        }

        if (action == ACTION_MARK_FAILED) {
            handleMarkFailed(context, userId, collectionId, mapId, nodeId)
            return
        }

        Log.d("AlarmReceiver", "Alarm triggered for node: $nodeId ($nodeTitle) in map: $mapId")

        showNotification(context, nodeTitle, status, nodeId.hashCode(), collectionId, mapId, nodeId)

        val pendingResult = goAsync()

        scope.launch {
            try {
                // Fetch nodes for this map
                val nodesFlow = dataStorage.getMapNodes(userId, collectionId, mapId)
                val allNodes = nodesFlow.first()
                val targetNode = allNodes.find { it.id == nodeId }

                if (targetNode != null) {
                    if (targetNode.status != "COMPLETED") {
                        Log.d("AlarmReceiver", "Node $nodeId is not completed. Marking as FAILED and incrementing stats.")
                        
                        // Increment failureCount
                        dataStorage.incrementUserStats(userId, isSuccess = false)

                        if (targetNode.alarmRepeat != "NONE" && targetNode.remainder != null) {
                            val nextTimestamp = ReminderUtils.calculateNextTimestamp(targetNode.remainder!!, targetNode.alarmRepeat)
                            Log.d("AlarmReceiver", "Rescheduling recurring node $nodeId for $nextTimestamp")
                            
                            // Reset recurring task for the next cycle
                            dataStorage.updateMapNodeFields(
                                userId,
                                collectionId,
                                mapId, 
                                nodeId,
                                mapOf(
                                    "status" to "TODO",
                                    "remainder" to nextTimestamp,
                                    "progress" to 0f
                                )
                            )

                            // Schedule next alarm
                            alarmManager.scheduleAlarm(
                                collectionId = collectionId,
                                mapId = mapId,
                                nodeId = nodeId,
                                nodeTitle = nodeTitle,
                                timestamp = nextTimestamp,
                                status = "TODO"
                            )
                        } else {
                            // Update node status to FAILED
                            dataStorage.updateMapNodeFields(
                                userId,
                                collectionId,
                                mapId, 
                                nodeId,
                                mapOf("status" to "FAILED")
                            )
                        }
                    } else {
                        Log.d("AlarmReceiver", "Node $nodeId was already COMPLETED. No action taken.")
                    }
                } else {
                    Log.e("AlarmReceiver", "Target node $nodeId not found in map $mapId")
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error processing alarm: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleMarkDone(context: Context, userId: String, collectionId: String, mapId: String, nodeId: String) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                dataStorage.incrementUserStats(userId, isSuccess = true)
                dataStorage.updateMapNodeFields(
                    userId,
                    collectionId,
                    mapId,
                    nodeId,
                    mapOf("status" to "COMPLETED", "progress" to 1f)
                )
                
                // Dismiss the notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(nodeId.hashCode())
                
                Log.d("AlarmReceiver", "Node $nodeId marked as DONE from notification")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleMarkFailed(context: Context, userId: String, collectionId: String, mapId: String, nodeId: String) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                dataStorage.incrementUserStats(userId, isSuccess = false)
                dataStorage.updateMapNodeFields(
                    userId,
                    collectionId,
                    mapId,
                    nodeId,
                    mapOf("status" to "FAILED", "progress" to 0f)
                )
                
                // Dismiss the notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(nodeId.hashCode())
                
                Log.d("AlarmReceiver", "Node $nodeId marked as FAILED from notification")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context, 
        nodeTitle: String,
        status: String, 
        notificationId: Int,
        collectionId: String,
        mapId: String, 
        nodeId: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra("collectionId", collectionId)
            putExtra("mapId", mapId)
            putExtra("nodeId", nodeId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context, nodeId.hashCode() + 1, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val failedIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_MARK_FAILED
            putExtra("collectionId", collectionId)
            putExtra("mapId", mapId)
            putExtra("nodeId", nodeId)
        }
        val failedPendingIntent = PendingIntent.getBroadcast(
            context, nodeId.hashCode() + 2, failedIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val statusText = when (status) {
            "TODO" -> "Deadline reached for this task."
            "IN_PROGRESS" -> "Task is currently in progress."
            "FAILED" -> "Task was previously marked as failed."
            "COMPLETED" -> "Task is already completed."
            else -> "You have a task to complete."
        }
        
        val builder = NotificationCompat.Builder(context, "task_reminders")
            .setSmallIcon(R.drawable.menu_icon)
            .setContentTitle("Task Deadline Reached")
            .setContentText("The deadline for '$nodeTitle' has passed. $statusText")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.menu_icon, "Mark as Done", donePendingIntent)
            .addAction(R.drawable.menu_icon, "Mark as Failed", failedPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                Log.e("AlarmReceiver", "Missing permission to show notification: ${e.message}")
            }
        }
    }
}
