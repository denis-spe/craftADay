// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.dataStructure

import androidx.compose.runtime.Stable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

@Stable
data class DiagramNode(
    @DocumentId val id: String = "",
    val parentId: String? = null,
    val title: String = "",
    val description: String = "",
    val status: String = "TODO", // "TODO", "IN_PROGRESS", "COMPLETED"
    val priority: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH", "URGENT"
    val remainder: Timestamp = Timestamp.now(),
    val alarmRepeat: String = "NONE", // "NONE", "DAILY", "WEEKLY", "MONTHLY"
    val progress: Float = 0f,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val x: Float = 0f,
    val y: Float = 0f,
    val color: String = "#3F51B5",
    @get:PropertyName("isColorFilled")
    @set:PropertyName("isColorFilled")
    var isColorFilled: Boolean = false,
    val side: String = "RIGHT", // "LEFT", "RIGHT"
    val nodeType: String = "TASK" // "ROOT", "TASK"
)
