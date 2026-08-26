// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.entities

import androidx.compose.runtime.Stable
import com.den.craftaday.R
import com.den.craftaday.backend.entities.types.MarkType
import com.den.craftaday.backend.entities.types.TaskAlarmType
import com.google.firebase.firestore.DocumentId

@Stable
data class TaskEntity(
    @DocumentId val id: String = "",
    val collectionId: String = "",
    val title: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val remainder: Long = 0L,
    val markType: MarkType = MarkType.Initial,
    val chosenIcon: Int = R.drawable.ic_task_default,
    val taskAlarmType: TaskAlarmType = TaskAlarmType.Once()
) {
    val toMap: Map<String, Any>
        get() {
            return mapOf(
                "collectionId" to collectionId,
                "title" to title,
                "description" to description,
                "createdAt" to createdAt,
                "remainder" to remainder,
                "markType" to markType.name,
                "chosenIcon" to chosenIcon,
                "taskAlarmType" to taskAlarmType.toMap()
            )
        }
}
