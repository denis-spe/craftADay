// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.alarmManager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.den.craftaday.backend.repositories.services.AccountService
import com.den.craftaday.backend.repositories.services.DataStorageService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class TaskBootReceiver: BroadcastReceiver() {

    @Inject
    lateinit var dataStorageService: DataStorageService

    @Inject
    lateinit var accountService: AccountService

    @Inject
    lateinit var taskAlarmManager: TaskAlarmManager

    companion object {
        const val TAG = "TaskBootReceiver"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.d(TAG, "Device rebooted. Re-scheduling task alarms.")

            val userId = accountService.currentUserId
            if (userId.isEmpty()) return

            val pendingResult = goAsync()

            scope.launch {
                try {
                    val collections = dataStorageService.getAllCollections(userId).first()

                    collections.forEach { collection ->
                        val tasks = dataStorageService.getTasksInCollection(userId, collection.id).first()
                        tasks.forEach { task ->
                            if (task.taskAlarmType.primaryTimestamp > System.currentTimeMillis()) {
                                Log.d(TAG, "Re-scheduling alarm for task: ${task.title}")
                                taskAlarmManager.scheduleAlarm(task)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error re-scheduling task alarms: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}