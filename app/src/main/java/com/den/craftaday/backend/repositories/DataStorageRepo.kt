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
        docRef.document(userId)
            .collection(DIAGRAM_NODES_COLLECTION)
            .add(node)
            .addOnSuccessListener {
                Log.w(TAG, "Diagram node added successfully")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error adding diagram node: $it")
            }
    }

    override fun deleteDiagramNode(userId: String, nodeId: String) {
        docRef.document(userId)
            .collection(DIAGRAM_NODES_COLLECTION)
            .document(nodeId)
            .delete()
            .addOnSuccessListener {
                Log.w(TAG, "Diagram node deleted successfully")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error deleting diagram node: $it")
            }
    }

    override fun updateDiagramNode(userId: String, node: DiagramNode) {
        docRef.document(userId)
            .collection(DIAGRAM_NODES_COLLECTION)
            .document(node.id)
            .set(node)
            .addOnSuccessListener {
                Log.w(TAG, "Diagram node updated successfully")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error updating diagram node: $it")
            }
    }
}
