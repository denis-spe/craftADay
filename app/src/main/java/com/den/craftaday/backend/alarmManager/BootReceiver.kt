// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.alarmManager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.den.craftaday.backend.blueprints.DataStorage
import com.den.craftaday.backend.blueprints.AccountService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataStorage: DataStorage

    @Inject
    lateinit var accountService: AccountService

    @Inject
    lateinit var alarmManager: MapAlarmManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d("BootReceiver", "Device rebooted. Re-scheduling active alarms.")
            
            val userId = accountService.currentUserId
            if (userId.isEmpty()) return

            val pendingResult = goAsync()
            
            scope.launch {
                try {
                    // 1. Check old structure maps (projects collection)
                    val oldMaps = dataStorage.getAllMaps(userId).first()
                    oldMaps.forEach { map ->
                        processMapNodes(collectionId = "", mapId = map.id, mapTitle = map.title)
                    }

                    // 2. Check new structure (collections)
                    val collections = dataStorage.getAllCollections(userId).first()
                    collections.forEach { collection ->
                        val maps = dataStorage.getMapsInCollection(userId, collection.id).first()
                        maps.forEach { map ->
                            processMapNodes(collectionId = collection.id, mapId = map.id, mapTitle = map.title)
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

    private suspend fun processMapNodes(collectionId: String, mapId: String, mapTitle: String) {
        val userId = accountService.currentUserId
        val nodes = dataStorage.getMapNodes(userId, collectionId, mapId).first()
        nodes.forEach { node ->
            if (node.status != "COMPLETED" && 
                node.remainder != null && 
                node.remainder!!.toDate().time > System.currentTimeMillis()) {
                
                Log.d("BootReceiver", "Re-scheduling alarm for node: ${node.id} in map: $mapTitle")
                alarmManager.scheduleAlarm(
                    mapId = mapId,
                    nodeId = node.id,
                    nodeTitle = node.title,
                    timestamp = node.remainder!!,
                    status = node.status
                )
            }
        }
    }
}
