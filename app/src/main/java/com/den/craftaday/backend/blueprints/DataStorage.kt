package com.den.craftaday.backend.blueprints

import com.den.craftaday.backend.dataStructure.MapNode
import com.den.craftaday.backend.dataStructure.ProjectMap
import com.den.craftaday.backend.dataStructure.ListCollection
import com.den.craftaday.backend.dataStructure.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

interface DataStorage {
    val firestore: FirebaseFirestore

    /**
     * Get all datasets from the database
     * @param userId The user's ID
     */
    fun getAllDatasets(userId: String): Flow<List<Task>>

    fun getTasksInCollection(userId: String, collectionId: String): Flow<List<Task>>

    /**
     * Add a task to the database
     */
    fun addTask(userId: String, collectionId: String, task: Task)

    /**
     * Delete a task from the database
     */
    fun deleteTask(userId: String, collectionId: String, task: Task)

    /**
     * Update a task in the database
     */
    fun updateTask(userId: String, collectionId: String, task: Task)

    /**
     * Update node in map
     */
    fun updateMapNode(userId: String, collectionId: String, mapId: String, node: MapNode)

    /**
     * Delete node from map
     */
    fun deleteMapNode(userId: String, collectionId: String, mapId: String, nodeId: String)

    /**
     * Add node to map
     */
    fun addMapNode(userId: String, collectionId: String, mapId: String, node: MapNode)

    /**
     * Get all map nodes from the database
     */
    fun getMapNodes(userId: String, collectionId: String, mapId: String): Flow<List<MapNode>>

    /**
     * Get all maps from the database (deprecated/global)
     */
    fun getAllMaps(userId: String): Flow<List<ProjectMap>>
    
    fun getMapsInCollection(userId: String, collectionId: String): Flow<List<ProjectMap>>

    fun addMap(userId: String, collectionId: String, map: ProjectMap)
    fun deleteMap(userId: String, collectionId: String, map: ProjectMap)
    fun updateMap(userId: String, collectionId: String, map: ProjectMap)
    
    fun getMap(userId: String, collectionId: String, mapId: String): Flow<ProjectMap?>

    fun incrementUserStats(userId: String, isSuccess: Boolean)
    
    fun updateMapNodeFields(userId: String, collectionId: String, mapId: String, nodeId: String, fields: Map<String, Any>)
    
    fun addCollection(userId: String, collection: ListCollection)
    fun getAllCollections(userId: String): Flow<List<ListCollection>>
}
