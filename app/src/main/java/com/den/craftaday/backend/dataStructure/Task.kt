// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.dataStructure

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import com.den.craftaday.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

@Stable
data class Task(
    @DocumentId val id: String = "",
    val collectionId: String = "",
    val title: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val remainder: Long = 0L,
    val mark: Mark = Mark.Initial,
    val chosenIcon: Int = R.drawable.ic_task_default,
    val startedAt: Long = System.currentTimeMillis(),
    val onReset: Reset = Reset.No
) {
    val toMap: Map<String, Any>
        get() {
            return mapOf(
                "collectionId" to collectionId,
                "title" to title,
                "description" to description,
                "createdAt" to createdAt,
                "remainder" to remainder,
                "mark" to mark.name,
                "startedAt" to startedAt,
                "chosenIcon" to chosenIcon,
                "onReset" to onReset.toMap()
            )
        }
}
