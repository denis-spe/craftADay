// Bless be the LORD GOD of hosts
package com.den.craftaday.backend.repositories

import android.util.Log
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.impl.DataStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import kotlinx.coroutines.flow.toList

class DataStorageRepo(
    override val firestore: FirebaseFirestore
): DataStorage {
    companion object {
        const val DATASET_COLLECTION = "craftADayDataset"
        const val TASKS_COLLECTION = "tasks"

        const val TAG = "DataStorageRepo"
    }

    override fun getAllDatasets(userId: String) = firestore
        .collection(DATASET_COLLECTION)
        .document(userId)
        .collection(TASKS_COLLECTION)
        .dataObjects<Task>()

    override fun addTask(userId: String, task: Task) {
        firestore
            .document(userId)
            .collection(TASKS_COLLECTION)
            .add(task)
            .addOnSuccessListener {
                Log.w(TAG, "Task added successfully")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error adding task: $it")
            }
    }
}