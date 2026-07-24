// Bless be the LORD GOD of hosts
package com.den.craftaday.backend.repositories

import android.util.Log
import com.den.craftaday.backend.dataStructure.DiagramNode
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.blueprints.DataStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects

class DataStorageRepo(
    override val firestore: FirebaseFirestore
): DataStorage {
    companion object {
        const val DATASET_COLLECTION = "craftADayDataset"
        const val TASKS_COLLECTION = "tasks"
        const val DIAGRAM_NODES_COLLECTION = "diagramNodes"

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

    fun continuousTaskAdd(userId: String, taskId: String, task: Task) {
        docRef.document(userId)
            .collection(TASKS_COLLECTION)
            .document(taskId)
            .collection(task.id)
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

    override fun getDiagramNodes(userId: String) = docRef
        .document(userId)
        .collection(DIAGRAM_NODES_COLLECTION)
        .dataObjects<DiagramNode>()

    override fun addDiagramNode(userId: String, node: DiagramNode) {
        if (userId.isEmpty()) {
            Log.e(TAG, "FAILED to add node: userId is empty. Node: ${node.title}")
            return
        }
        docRef.document(userId)
            .collection(DIAGRAM_NODES_COLLECTION)
            .add(node)
            .addOnSuccessListener { ref ->
                Log.w(TAG, "SUCCESS: Added node '${node.title}' (ID: ${ref.id}) for user: $userId")
            }
            .addOnFailureListener {
                Log.e(TAG, "FAILURE: Error adding node '${node.title}' for user: $userId. Error: ${it.message}", it)
            }
    }

    override fun deleteDiagramNode(userId: String, nodeId: String) {
        if (userId.isEmpty() || nodeId.isEmpty()) {
            Log.e(TAG, "FAILED to delete node: userId or nodeId is empty. User: $userId, Node: $nodeId")
            return
        }
        docRef.document(userId)
            .collection(DIAGRAM_NODES_COLLECTION)
            .document(nodeId)
            .delete()
            .addOnSuccessListener {
                Log.w(TAG, "SUCCESS: Deleted node ID: $nodeId for user: $userId")
            }
            .addOnFailureListener {
                Log.e(TAG, "FAILURE: Error deleting node $nodeId for user: $userId. Error: ${it.message}", it)
            }
    }

    override fun updateDiagramNode(userId: String, node: DiagramNode) {
        if (userId.isEmpty() || node.id.isEmpty()) {
            Log.e(TAG, "FAILED to update node: userId or nodeId is empty. User: $userId, Node: ${node.id}")
            return
        }
        docRef.document(userId)
            .collection(DIAGRAM_NODES_COLLECTION)
            .document(node.id)
            .set(node)
            .addOnSuccessListener {
                Log.w(TAG, "SUCCESS: Updated node '${node.title}' (ID: ${node.id}) for user: $userId")
            }
            .addOnFailureListener {
                Log.e(TAG, "FAILURE: Error updating node ${node.id} for user: $userId. Error: ${it.message}", it)
            }
    }
}
