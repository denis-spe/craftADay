// Bless be the LORD GOD of hosts
package com.den.craftaday.backend.repositories

import android.util.Log
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.blueprints.DataStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import kotlinx.coroutines.flow.stateIn

class DataStorageRepo(
    override val firestore: FirebaseFirestore
): DataStorage {
    companion object {
        const val DATASET_COLLECTION = "craftADayDataset"
        const val TASKS_COLLECTION = "tasks"

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


}