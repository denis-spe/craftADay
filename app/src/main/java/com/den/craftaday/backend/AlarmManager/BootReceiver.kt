// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.AlarmManager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.den.craftaday.backend.useCase.DiagramUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var diagramUseCase: DiagramUseCase

    @Inject
    lateinit var alarmManager: DiagramAlarmManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d("BootReceiver", "Device rebooted. Re-scheduling active alarms.")
            
            val pendingResult = goAsync()
            
            scope.launch {
                try {
                    // Fetch all projects
                    val projects = diagramUseCase.getAllProjects().first()
                    
                    projects.forEach { project ->
                        // Fetch nodes for each project
                        val nodes = diagramUseCase.getDiagramNodes(project.id).first()
                        
                        nodes.forEach { node ->
                            // If node has a future reminder and is not completed
                            if (node.status != "COMPLETED" && 
                                node.remainder != null && 
                                node.remainder!!.toDate().time > System.currentTimeMillis()) {
                                
                                Log.d("BootReceiver", "Re-scheduling alarm for node: ${node.id}")
                                alarmManager.scheduleAlarm(
                                    projectId = project.id,
                                    nodeId = node.id,
                                    nodeTitle = node.title,
                                    timestamp = node.remainder!!,
                                    status = node.status
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error re-scheduling alarms: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
