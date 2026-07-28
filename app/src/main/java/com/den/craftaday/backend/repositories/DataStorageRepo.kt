// Bless be the LORD GOD of hosts
package com.den.craftaday.backend.repositories

import android.util.Log
import com.den.craftaday.backend.dataStructure.DiagramNode
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.blueprints.DataStorage
import com.den.craftaday.backend.dataStructure.DiagramProject
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import kotlinx.coroutines.flow.map

class DataStorageRepo(
    override val firestore: FirebaseFirestore
): DataStorage {
    companion object {
        const val DATASET_COLLECTION = "craftADayDataset"
        const val TASKS_COLLECTION = "tasks"
        const val PROJECTS_COLLECTION = "projects"
        const val NODES_COLLECTION = "nodes"

        const val TAG = "DataStorageRepo"
    }

    val docRef = firestore
        .collection(DATASET_COLLECTION)


    override fun getAllDatasets(userId: String) = docRef
        .document(userId)
        .collection(TASKS_COLLECTION)
        .dataObjects<Task>()

    override fun addTask(userId: String, task: Task) {
         docRef.document(userId)
            .collection(TASKS_COLLECTION)
            .add(task)
            .addOnSuccessListener {
                Log.w(TAG, "Task added successfully")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error adding task: $it")
            }
    }

    override fun deleteTask(
        userId: String,
        task: Task
    ) {
        docRef.document(userId)
            .collection(TASKS_COLLECTION)
            .document(task.id)
            .delete()
            .addOnSuccessListener {
                Log.w(TAG, "Task deleted successfully")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error deleting task: $it")
            }
    }

    override fun updateTask(
        userId: String,
        task: Task
    ) {
        docRef.document(userId)
            .collection(TASKS_COLLECTION)
            .document(task.id)
            .set(task)
            .addOnSuccessListener {
                Log.w(TAG, "Task updated successfully")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error updating task: $it")
            }
    }

    override fun getProject(userId: String, projectId: String) = docRef
        .document(userId)
        .collection(PROJECTS_COLLECTION)
        .whereEqualTo(FieldPath.documentId(), projectId)
        .dataObjects<DiagramProject>()
        .map { it.firstOrNull() }

    override fun addProject(userId: String, project: DiagramProject) {
        docRef.document(userId)
            .collection(PROJECTS_COLLECTION)
            .add(project)
    }

    override fun deleteProject(userId: String, project: DiagramProject) {
        docRef.document(userId)
            .collection(PROJECTS_COLLECTION)
            .document(project.id)
            .delete()
    }


    override fun updateProject(userId: String, project: DiagramProject) {
        docRef.document(userId)
            .collection(PROJECTS_COLLECTION)
            .document(project.id)
            .set(project)
    }


    override fun getAllProjects(userId: String) = docRef
        .document(userId)
        .collection(PROJECTS_COLLECTION)
        .dataObjects<DiagramProject>()

    override fun getDiagramNodes(
        userId: String,
        diagramProjectId: String,
    ) = docRef
        .document(userId)
        .collection(PROJECTS_COLLECTION)
        .document(diagramProjectId)
        .collection(NODES_COLLECTION)
        .dataObjects<DiagramNode>()

    override fun addDiagramNode(
        userId: String,
        diagramProjectId: String,
        node: DiagramNode
    ) {
        if (userId.isEmpty()) {
            Log.e(TAG, "FAILED to add node: userId is empty. Node: ${node.title}")
            return
        }
        
        val collection = docRef.document(userId)
            .collection(PROJECTS_COLLECTION)
            .document(diagramProjectId)
            .collection(NODES_COLLECTION)
            
        // If node has an ID, use it (pre-generated), otherwise let Firestore add it
        val task = if (node.id.isEmpty()) {
            collection.add(node)
        } else {
            collection.document(node.id).set(node)
        }

        task.addOnSuccessListener {
            Log.w(TAG, "SUCCESS: Added/Set node '${node.title}' for user: $userId in project: $diagramProjectId")
        }
        .addOnFailureListener {
            Log.e(TAG, "FAILURE: Error adding node '${node.title}' for user: $userId in project: $diagramProjectId. Error: ${it.message}", it)
        }
    }

    override fun deleteDiagramNode(
        userId: String,
        diagramProjectId: String,
        nodeId: String
    ) {
        if (userId.isEmpty() || nodeId.isEmpty()) {
            Log.e(TAG, "FAILED to delete node: userId or nodeId is empty. User: $userId, Node: $nodeId")
            return
        }
        docRef.document(userId)
            .collection(PROJECTS_COLLECTION)
            .document(diagramProjectId)
            .collection(NODES_COLLECTION)
            .document(nodeId)
            .delete()
            .addOnSuccessListener {
                Log.w(TAG, "SUCCESS: Deleted node ID: $nodeId for user: $userId in project: $diagramProjectId")
            }
            .addOnFailureListener {
                Log.e(TAG, "FAILURE: Error deleting node $nodeId for user: $userId in project: $diagramProjectId. Error: ${it.message}", it)
            }
    }

    override fun updateDiagramNode(
        userId: String,
        diagramProjectId: String,
        node: DiagramNode
    ) {
        if (userId.isEmpty() || node.id.isEmpty()) {
            Log.e(TAG, "FAILED to update node: userId or nodeId is empty. User: $userId, Node: ${node.id}")
            return
        }
        docRef.document(userId)
            .collection(PROJECTS_COLLECTION)
            .document(diagramProjectId)
            .collection(NODES_COLLECTION)
            .document(node.id)
            .set(node)
            .addOnSuccessListener {
                Log.w(TAG, "SUCCESS: Updated node '${node.title}' (ID: ${node.id}) for user: $userId in project: $diagramProjectId isFilledColor: ${node.isColorFilled}")
            }
            .addOnFailureListener {
                Log.e(TAG, "FAILURE: Error updating node ${node.id} for user: $userId in project: $diagramProjectId. Error: ${it.message}", it)
            }
    }

    override fun incrementUserStats(userId: String, isSuccess: Boolean) {
        if (userId.isEmpty()) return
        val field = if (isSuccess) "successCount" else "failureCount"
        docRef.document(userId)
            .update(field, FieldValue.increment(1))
            .addOnFailureListener {
                // If the document doesn't exist, create it
                docRef.document(userId).set(mapOf(field to 1), com.google.firebase.firestore.SetOptions.merge())
            }
    }

    override fun updateDiagramNodeFields(
        userId: String,
        projectId: String,
        nodeId: String,
        fields: Map<String, Any>
    ) {
        if (userId.isEmpty() || projectId.isEmpty() || nodeId.isEmpty()) return
        docRef.document(userId)
            .collection(PROJECTS_COLLECTION)
            .document(projectId)
            .collection(NODES_COLLECTION)
            .document(nodeId)
            .update(fields)
            .addOnSuccessListener {
                Log.d(TAG, "SUCCESS: Updated partial fields for node $nodeId: $fields")
            }
            .addOnFailureListener {
                Log.e(TAG, "FAILURE: Error updating partial fields for node $nodeId", it)
            }
    }
}
