package com.den.craftaday.backend.repositories.services

import com.den.craftaday.backend.entities.MapEntity
import com.den.craftaday.backend.entities.MapNodeEntity
import com.den.craftaday.backend.entities.ListCollectionEntity
import com.den.craftaday.backend.entities.TaskEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

interface DataStorageService {
    val firestore: FirebaseFirestore

    /**
     * Get all datasets from the database
     * @param userId The user's ID
     */
    fun getAllDatasets(userId: String): Flow<List<TaskEntity>>

    fun getTasksInCollection(userId: String, collectionId: String): Flow<List<TaskEntity>>

    /**
     * Add a taskEntity to the database
     */
    fun addTask(userId: String, collectionId: String, taskEntity: TaskEntity)

    /**
     * Delete a taskEntity from the database
     */
    fun deleteTask(userId: String, collectionId: String, taskEntity: TaskEntity)

    /**
     * Update a taskEntity in the database
     */
    fun updateTask(userId: String, collectionId: String, taskEntity: TaskEntity)

    /**
     * Update node in map
     */
    fun updateMapNode(userId: String, collectionId: String, mapId: String, node: MapNodeEntity)

    /**
     * Delete node from map
     */
    fun deleteMapNode(userId: String, collectionId: String, mapId: String, nodeId: String)

    /**
     * Add node to map
     */
    fun addMapNode(userId: String, collectionId: String, mapId: String, node: MapNodeEntity)

    /**
     * Get all map nodes from the database
     */
    fun getMapNodes(userId: String, collectionId: String, mapId: String): Flow<List<MapNodeEntity>>

    /**
     * Get all maps from the database (deprecated/global)
     */
    fun getAllMaps(userId: String): Flow<List<MapEntity>>
    
    fun getMapsInCollection(userId: String, collectionId: String): Flow<List<MapEntity>>

    fun addMap(userId: String, collectionId: String, map: MapEntity)
    fun deleteMap(userId: String, collectionId: String, map: MapEntity)
    fun updateMap(userId: String, collectionId: String, map: MapEntity)
    
    fun getMap(userId: String, collectionId: String, mapId: String): Flow<MapEntity?>

    fun incrementUserStats(userId: String, isSuccess: Boolean)
    
    fun updateMapNodeFields(userId: String, collectionId: String, mapId: String, nodeId: String, fields: Map<String, Any>)
    
    fun addCollection(userId: String, collection: ListCollectionEntity)
    fun getAllCollections(userId: String): Flow<List<ListCollectionEntity>>
}
