package com.den.craftaday.backend.blueprints

import com.den.craftaday.backend.dataStructure.DiagramNode
import com.den.craftaday.backend.dataStructure.DiagramProject
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
     * Get all projects from the database
     * @param userId The user's ID
     * @param diagramProjectId The diagram project ID
     * @return A flow of lists of projects
     */
    fun updateDiagramNode(userId: String, diagramProjectId: String, node: DiagramNode)

    /**
     * Delete node from project
     * @param userId The user's ID
     * @param diagramProjectId The diagram project ID
     * @return A flow of lists of projects
     */
    fun deleteDiagramNode(userId: String, diagramProjectId: String, nodeId: String)

    /**
     * Add node to project
     * @param userId The user's ID
     * @param diagramProjectId The diagram project ID
     * @return A flow of lists of projects
     */
    fun addDiagramNode(userId: String, diagramProjectId: String, node: DiagramNode)

    /**
     * Get all diagram nodes from the database
     * @param userId The user's ID
     * @param diagramProjectId The diagram project ID
     * @return A flow of lists of diagram nodes
     */
    fun getDiagramNodes(userId: String, diagramProjectId: String): Flow<List<DiagramNode>>

    /**
     * Get all projects from the database
     * @param userId The user's ID
     * @return A flow of lists of projects
     */
    fun getAllProjects(userId: String): Flow<List<DiagramProject>>
    fun addProject(userId: String, project: DiagramProject)
    fun deleteProject(userId: String, project: DiagramProject)
    fun updateProject(userId: String, project: DiagramProject)
    fun getProject(userId: String, projectId: String): Flow<DiagramProject?>
    fun incrementUserStats(userId: String, isSuccess: Boolean)
    fun updateDiagramNodeFields(userId: String, projectId: String, nodeId: String, fields: Map<String, Any>)
}
