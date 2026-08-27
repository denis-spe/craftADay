// Bless be the LORD GOD of hosts
package com.den.craftaday.backend.repositories

import android.util.Log
import com.den.craftaday.R
import com.den.craftaday.backend.entities.MapNodeEntity
import com.den.craftaday.backend.entities.TaskEntity
import com.den.craftaday.backend.repositories.services.DataStorageService
import com.den.craftaday.backend.entities.MapEntity
import com.den.craftaday.backend.entities.ListCollectionEntity
import com.den.craftaday.backend.entities.types.MarkType
import com.den.craftaday.backend.entities.types.TaskAlarmType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.dataObjects
import kotlinx.coroutines.flow.map

class DataStorageServiceRepo(
    override val firestore: FirebaseFirestore
): DataStorageService {
    companion object {
        const val DATASET_COLLECTION = "craftADayDataset"
        const val TASKS_COLLECTION = "tasks"
        const val MAPS_COLLECTION = "projects"
        const val NODES_COLLECTION = "nodes"
        const val COLLECTION = "collection"

        const val TAG = "DataStorageServiceRepo"
    }

    private val docRef = firestore
        .collection(DATASET_COLLECTION)


    override fun getAllDatasets(userId: String) = docRef
        .document(userId)
        .collection(TASKS_COLLECTION)
        .snapshots()
        .map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                doc.toTask()
            }
        }

    override fun getTasksInCollection(userId: String, collectionId: String) = docRef
        .document(userId)
        .collection(COLLECTION)
        .document(collectionId)
        .collection(TASKS_COLLECTION)
        .orderBy("createdAt", Query.Direction.ASCENDING)
        .snapshots()
        .map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                doc.toTask()
            }
        }

    /**
     * Get a single task by ID
     * @param userId The user ID
     * @param collectionId The collection ID
     * @param taskId The task ID
     * @return A Flow of TaskEntity
     */
    override fun getTask(userId: String, collectionId: String, taskId: String) = docRef
        .document(userId)
        .collection(COLLECTION)
        .document(collectionId)
        .collection(TASKS_COLLECTION)
        .document(taskId)
        .snapshots()
        .map { snapshot ->
            snapshot.toTask()
        }

    private fun DocumentSnapshot.toTask(): TaskEntity? {
        return try {
            val markTypeStr = getString("markType") ?: MarkType.Initial.name
            val markType = try {
                MarkType.valueOf(markTypeStr)
            } catch (_: IllegalArgumentException) {
                // Handle case-insensitivity or legacy names
                MarkType.entries.find { it.name.equals(markTypeStr, ignoreCase = true) } ?: MarkType.Initial
            }

            TaskEntity(
                id = id,
                collectionId = getString("collectionId") ?: "",
                title = getString("title") ?: "",
                description = getString("description") ?: "",
                createdAt = getLongSafe("createdAt") ?: System.currentTimeMillis(),
                remainder = getLongSafe("remainder") ?: 0L,
                markType = markType,
                chosenIcon = getLongSafe("chosenIcon")?.toInt()?.takeIf { it != 0 } ?: R.drawable.ic_launcher_foreground,
                taskAlarmType = TaskAlarmType.fromMap(get("taskAlarmType") as? Map<*, *> ?: emptyMap<String, Any>())
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping document $id to TaskEntity", e)
            null
        }
    }

    override fun addCollection(
        userId: String,
        collection: ListCollectionEntity
    ) {
        val collectionRef = docRef.document(userId)
            .collection(COLLECTION)
            .document()

        val collectionCopy = collection.copy(id = collectionRef.id)
        collectionRef.set(collectionCopy)
    }

    override fun addTask(
        userId: String,
        collectionId: String,
        taskEntity: TaskEntity
    ) {
         val taskRef = docRef.document(userId)
             .collection(COLLECTION)
            .document(collectionId)
            .collection(TASKS_COLLECTION)
             .document()

        val taskCopy = taskEntity.copy(id = taskRef.id)
        taskRef.set(taskCopy.toMap)
            .addOnSuccessListener {
                Log.w(TAG, "TaskEntity added successfully to collection $collectionId")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error adding taskEntity: $it")
            }
    }

    override fun deleteTask(
        userId: String,
        collectionId: String,
        taskEntity: TaskEntity
    ) {
        docRef.document(userId)
            .collection(COLLECTION)
            .document(collectionId)
            .collection(TASKS_COLLECTION)
            .document(taskEntity.id)
            .delete()
    }

    override fun updateTask(
        userId: String,
        collectionId: String,
        taskEntity: TaskEntity
    ) {
        docRef.document(userId)
            .collection(COLLECTION)
            .document(collectionId)
            .collection(TASKS_COLLECTION)
            .document(taskEntity.id)
            .set(taskEntity.toMap)
            .addOnSuccessListener {
                Log.w(TAG, "TaskEntity updated successfully in collection $collectionId")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error updating taskEntity: $it")
            }
    }

    override fun getMap(
        userId: String,
        collectionId: String,
        mapId: String
    ) = docRef
        .document(userId)
        .collection(COLLECTION)
        .document(collectionId)
        .collection(MAPS_COLLECTION)
        .whereEqualTo(FieldPath.documentId(), mapId)
        .dataObjects<MapEntity>()
        .map { it.firstOrNull() }

    override fun addMap(userId: String, collectionId: String, map: MapEntity) {
        docRef.document(userId)
            .collection(COLLECTION)
            .document(collectionId)
            .collection(MAPS_COLLECTION)
            .add(map)
    }

    override fun deleteMap(userId: String, collectionId: String, map: MapEntity) {
        docRef.document(userId)
            .collection(COLLECTION)
            .document(collectionId)
            .collection(MAPS_COLLECTION)
            .document(map.id)
            .delete()
    }


    override fun updateMap(userId: String, collectionId: String, map: MapEntity) {
        docRef.document(userId)
            .collection(COLLECTION)
            .document(collectionId)
            .collection(MAPS_COLLECTION)
            .document(map.id)
            .set(map)
    }


    override fun getAllMaps(userId: String) = docRef
        .document(userId)
        .collection(MAPS_COLLECTION)
        .dataObjects<MapEntity>()

    override fun getMapsInCollection(userId: String, collectionId: String) = docRef
        .document(userId)
        .collection(COLLECTION)
        .document(collectionId)
        .collection(MAPS_COLLECTION)
        .dataObjects<MapEntity>()

    override fun getMapNodes(
        userId: String,
        collectionId: String,
        mapId: String,
    ): kotlinx.coroutines.flow.Flow<List<MapNodeEntity>> {
        val path = if (collectionId.isEmpty()) {
            // Root path for old maps
            docRef.document(userId)
                .collection(MAPS_COLLECTION)
                .document(mapId)
        } else {
            // Nested path for new maps
            docRef.document(userId)
                .collection(COLLECTION)
                .document(collectionId)
                .collection(MAPS_COLLECTION)
                .document(mapId)
        }
        return path.collection(NODES_COLLECTION).dataObjects<MapNodeEntity>()
    }

    override fun addMapNode(
        userId: String,
        collectionId: String,
        mapId: String,
        node: MapNodeEntity
    ) {
        if (userId.isEmpty()) return
        
        val collection = if (collectionId.isEmpty()) {
            docRef.document(userId)
                .collection(MAPS_COLLECTION)
                .document(mapId)
        } else {
            docRef.document(userId)
                .collection(COLLECTION)
                .document(collectionId)
                .collection(MAPS_COLLECTION)
                .document(mapId)
        }.collection(NODES_COLLECTION)
            
        val dbTask = if (node.id.isEmpty()) {
            collection.add(node)
        } else {
            collection.document(node.id).set(node)
        }

        dbTask.addOnSuccessListener {
            Log.w(TAG, "SUCCESS: Added/Set node '${node.title}' in map: $mapId")
        }
    }

    override fun deleteMapNode(
        userId: String,
        collectionId: String,
        mapId: String,
        nodeId: String
    ) {
        if (userId.isEmpty() || nodeId.isEmpty()) return
        val path = if (collectionId.isEmpty()) {
            docRef.document(userId)
                .collection(MAPS_COLLECTION)
                .document(mapId)
        } else {
            docRef.document(userId)
                .collection(COLLECTION)
                .document(collectionId)
                .collection(MAPS_COLLECTION)
                .document(mapId)
        }
        path.collection(NODES_COLLECTION)
            .document(nodeId)
            .delete()
    }

    override fun updateMapNode(
        userId: String,
        collectionId: String,
        mapId: String,
        node: MapNodeEntity
    ) {
        if (userId.isEmpty() || node.id.isEmpty()) return
        val path = if (collectionId.isEmpty()) {
            docRef.document(userId)
                .collection(MAPS_COLLECTION)
                .document(mapId)
        } else {
            docRef.document(userId)
                .collection(COLLECTION)
                .document(collectionId)
                .collection(MAPS_COLLECTION)
                .document(mapId)
        }
        path.collection(NODES_COLLECTION)
            .document(node.id)
            .set(node)
    }

    override fun incrementUserStats(userId: String, isSuccess: Boolean) {
        if (userId.isEmpty()) return
        val field = if (isSuccess) "successCount" else "failureCount"
        docRef.document(userId)
            .update(field, FieldValue.increment(1))
            .addOnFailureListener {
                docRef.document(userId).set(mapOf(field to 1), SetOptions.merge())
            }
    }

    override fun updateMapNodeFields(
        userId: String,
        collectionId: String,
        mapId: String,
        nodeId: String,
        fields: Map<String, Any>
    ) {
        if (userId.isEmpty() || mapId.isEmpty() || nodeId.isEmpty()) return
        val path = if (collectionId.isEmpty()) {
            docRef.document(userId)
                .collection(MAPS_COLLECTION)
                .document(mapId)
        } else {
            docRef.document(userId)
                .collection(COLLECTION)
                .document(collectionId)
                .collection(MAPS_COLLECTION)
                .document(mapId)
        }
        path.collection(NODES_COLLECTION)
            .document(nodeId)
            .update(fields)
    }

    override fun getAllCollections(userId: String) = docRef
        .document(userId)
        .collection(COLLECTION)
        .snapshots()
        .map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                doc.toListCollection()
            }
        }

    private fun DocumentSnapshot.toListCollection(): ListCollectionEntity? {
        return try {
            ListCollectionEntity(
                id = id,
                name = getString("name") ?: "",
                createdAt = getLongSafe("createdAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping document $id to ListCollectionEntity", e)
            null
        }
    }

    private fun DocumentSnapshot.getLongSafe(field: String): Long? {
        return try {
            when (val value = get(field)) {
                is Number -> value.toLong()
                is Timestamp -> value.toDate().time
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
