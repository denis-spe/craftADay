package com.den.craftaday.backend.blueprints

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
}