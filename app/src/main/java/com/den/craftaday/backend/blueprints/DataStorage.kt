package com.den.craftaday.backend.blueprints

import com.den.craftaday.backend.dataStructure.DiagramNode
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

    /**
     * Add a task to the database
     * @param userId The user's ID
     * @param task The task to add
     */
    fun addTask(userId: String, task: Task)

    /**
     * Delete a task from the database
     * @param userId The user's ID
     * @param task The task to delete
     */
    fun deleteTask(userId: String, task: Task)

    /**
     * Update a task in the database
     * @param userId The user's ID
     * @param task The task to update
     */
    fun updateTask(userId: String, task: Task)

    /**
     * Get all diagram nodes for a user
     */
    fun getDiagramNodes(userId: String): Flow<List<DiagramNode>>

    /**
     * Add a diagram node
     */
    fun addDiagramNode(userId: String, node: DiagramNode)

    /**
     * Delete a diagram node
     */
    fun deleteDiagramNode(userId: String, nodeId: String)

    /**
     * Update a diagram node (e.g., position, title)
     */
    fun updateDiagramNode(userId: String, node: DiagramNode)
}
