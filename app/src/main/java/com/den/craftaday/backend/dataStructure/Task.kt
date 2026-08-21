// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.dataStructure

import androidx.compose.runtime.Stable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

@Stable
data class Task(
    @DocumentId val id: String = "",
    val collectionId: String = "",
    val title: String = "",
    val description: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val remainder: Long = 0L,
    val mark: Mark = Mark.Initial,
)
