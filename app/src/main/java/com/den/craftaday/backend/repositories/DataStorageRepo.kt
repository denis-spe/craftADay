// Bless be the LORD GOD of hosts
package com.den.craftaday.backend.repositories

import android.util.Log
import com.den.craftaday.R
import com.den.craftaday.backend.dataStructure.MapNode
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.blueprints.DataStorage
import com.den.craftaday.backend.dataStructure.ProjectMap
import com.den.craftaday.backend.dataStructure.ListCollection
import com.den.craftaday.backend.dataStructure.Mark
import com.den.craftaday.backend.dataStructure.Reset
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.dataObjects
import kotlinx.coroutines.flow.map

class DataStorageRepo(
    override val firestore: FirebaseFirestore
): DataStorage {
    companion object {
        const val DATASET_COLLECTION = "craftADayDataset"
        const val TASKS_COLLECTION = "tasks"
        const val MAPS_COLLECTION = "projects"
        const val NODES_COLLECTION = "nodes"
        const val COLLECTION = "collection"

        const val TAG = "DataStorageRepo"
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

    private fun DocumentSnapshot.toTask(): Task? {
        return try {
            val markStr = getString("mark") ?: Mark.Initial.name
            val mark = try {
                Mark.valueOf(markStr)
            } catch (_: IllegalArgumentException) {
                // Handle case-insensitivity or legacy names
                Mark.entries.find { it.name.equals(markStr, ignoreCase = true) } ?: Mark.Initial
            }

            Task(
                id = id,
                collectionId = getString("collectionId") ?: "",
                title = getString("title") ?: "",
                description = getString("description") ?: "",
                createdAt = getLongSafe("createdAt") ?: System.currentTimeMillis(),
                remainder = getLongSafe("remainder") ?: 0L,
                mark = mark,
                startedAt = getLongSafe("startedAt") ?: System.currentTimeMillis(),
                chosenIcon = getLongSafe("chosenIcon")?.toInt()?.takeIf { it != 0 } ?: R.drawable.ic_launcher_foreground,
                onReset = Reset.fromMap(get("onReset") as? Map<*, *> ?: emptyMap<String, Any>())
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping document $id to Task", e)
            null
        }
    }

    override fun addCollection(
        userId: String,
        collection: ListCollection
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
        task: Task
    ) {
         val taskRef = docRef.document(userId)
             .collection(COLLECTION)
            .document(collectionId)
            .collection(TASKS_COLLECTION)
             .document()

        val taskCopy = task.copy(id = taskRef.id)
        taskRef.set(taskCopy.toMap)
            .addOnSuccessListener {
                Log.w(TAG, "Task added successfully to collection $collectionId")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error adding task: $it")
            }
    }

    override fun deleteTask(
        userId: String,
        collectionId: String,
        task: Task
    ) {
        docRef.document(userId)
            .collection(COLLECTION)
            .document(collectionId)
            .collection(TASKS_COLLECTION)
            .document(task.id)
            .delete()
    }

    override fun updateTask(
        userId: String,
        collectionId: String,
        task: Task
    ) {
        docRef.document(userId)
            .collection(COLLECTION)
            .document(collectionId)
            .collection(TASKS_COLLECTION)
            .document(task.id)
            .set(task.toMap)
            .addOnSuccessListener {
                Log.w(TAG, "Task updated successfully in collection $collectionId")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error updating task: $it")
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
        .dataObjects<ProjectMap>()
        .map { it.firstOrNull() }

    override fun addMap(userId: String, collectionId: String, map: ProjectMap) {
        docRef.document(userId)
            .collection(COLLECTION)
            .document(collectionId)
            .collection(MAPS_COLLECTION)
            .add(map)
    }

    override fun deleteMap(userId: String, collectionId: String, map: ProjectMap) {
        docRef.document(userId)
            .collection(COLLECTION)
            .document(collectionId)
            .collection(MAPS_COLLECTION)
            .document(map.id)
            .delete()
    }


    override fun updateMap(userId: String, collectionId: String, map: ProjectMap) {
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
        .dataObjects<ProjectMap>()

    override fun getMapsInCollection(userId: String, collectionId: String) = docRef
        .document(userId)
        .collection(COLLECTION)
        .document(collectionId)
        .collection(MAPS_COLLECTION)
        .dataObjects<ProjectMap>()

    override fun getMapNodes(
        userId: String,
        collectionId: String,
        mapId: String,
    ): kotlinx.coroutines.flow.Flow<List<MapNode>> {
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
        return path.collection(NODES_COLLECTION).dataObjects<MapNode>()
    }

    override fun addMapNode(
        userId: String,
        collectionId: String,
        mapId: String,
        node: MapNode
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
        node: MapNode
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

    private fun DocumentSnapshot.toListCollection(): ListCollection? {
        return try {
            ListCollection(
                id = id,
                name = getString("name") ?: "",
                createdAt = getLongSafe("createdAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping document $id to ListCollection", e)
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
