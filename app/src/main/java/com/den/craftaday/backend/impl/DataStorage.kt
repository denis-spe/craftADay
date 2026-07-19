package com.den.craftaday.backend.impl

import com.den.craftaday.backend.dataStructure.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

interface DataStorage {
    val firestore: FirebaseFirestore

    fun getAllDatasets(userId: String): Flow<List<Task>>

    fun addTask(userId: String, task: Task)
}