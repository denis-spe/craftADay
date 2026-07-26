package com.den.craftaday.backend.AlarmManager

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
import com.den.craftaday.backend.useCase.DiagramUseCase
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var diagramUseCase: DiagramUseCase

    @Inject
    lateinit var alarmManager: DiagramAlarmManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val projectId = intent.getStringExtra("projectId") ?: return
        val nodeId = intent.getStringExtra("nodeId") ?: return
        val nodeTitle = intent.getStringExtra("nodeTitle") ?: "Task"

        Log.d("AlarmReceiver", "Alarm triggered for node: $nodeId ($nodeTitle) in project: $projectId")

        showNotification(context, nodeTitle, nodeId.hashCode())

        val pendingResult = goAsync()

        scope.launch {
            try {
                // Fetch nodes for this project
                val nodesFlow = diagramUseCase.getDiagramNodes(projectId)
                val allNodes = nodesFlow.first()
                val targetNode = allNodes.find { it.id == nodeId }

                if (targetNode != null) {
                    if (targetNode.status != "COMPLETED") {
                        Log.d("AlarmReceiver", "Node $nodeId is not completed. Marking as FAILED and incrementing stats.")
                        
                        // Increment failureCount
                        diagramUseCase.incrementUserStats(isSuccess = false)

                        if (targetNode.alarmRepeat != "NONE") {
                            val nextTimestamp = calculateNextTimestamp(targetNode.remainder, targetNode.alarmRepeat)
                            Log.d("AlarmReceiver", "Rescheduling recurring node $nodeId for $nextTimestamp")
                            
                            // Reset recurring task for the next cycle
                            diagramUseCase.updateDiagramNode(
                                projectId, 
                                targetNode.copy(
                                    status = "TODO",
                                    remainder = nextTimestamp
                                )
                            )

                            // Schedule next alarm
                            alarmManager.scheduleAlarm(
                                projectId = projectId,
                                nodeId = nodeId,
                                nodeTitle = nodeTitle,
                                timestamp = nextTimestamp
                            )
                        } else {
                            // Update node status to FAILED
                            diagramUseCase.updateDiagramNode(
                                projectId, 
                                targetNode.copy(status = "FAILED")
                            )
                        }
                    } else {
                        Log.d("AlarmReceiver", "Node $nodeId was already COMPLETED. No action taken.")
                    }
                } else {
                    Log.e("AlarmReceiver", "Target node $nodeId not found in project $projectId")
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error processing alarm: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun calculateNextTimestamp(current: Timestamp, repeat: String): Timestamp {
        val calendar = Calendar.getInstance().apply {
            time = current.toDate()
        }
        when (repeat) {
            "DAILY" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
        }
        // Ensure the next time is in the future
        while (calendar.timeInMillis <= System.currentTimeMillis()) {
            when (repeat) {
                "DAILY" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
            }
        }
        return Timestamp(calendar.time)
    }

    private fun showNotification(context: Context, nodeTitle: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val builder = NotificationCompat.Builder(context, "task_reminders")
            .setSmallIcon(R.drawable.menu_icon)
            .setContentTitle("Task Deadline Reached")
            .setContentText("The deadline for '$nodeTitle' has passed.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setContentIntent(pendingIntent)
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
