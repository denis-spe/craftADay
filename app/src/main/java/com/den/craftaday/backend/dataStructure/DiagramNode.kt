// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.dataStructure

import androidx.compose.runtime.Stable
import com.google.firebase.firestore.DocumentId

@Stable
data class DiagramNode(
    @DocumentId val id: String = "",
    val parentId: String? = null,
    val title: String = "",
    val description: String = "",
    val status: String = "TODO", // "TODO", "IN_PROGRESS", "COMPLETED"
    val priority: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH", "URGENT"
    val dueDate: String = "",
    val progress: Float = 0f,
    val x: Float = 0f,
    val y: Float = 0f,
    val color: String = "#3F51B5",
    val nodeType: String = "TASK" // "ROOT", "TASK"
)
