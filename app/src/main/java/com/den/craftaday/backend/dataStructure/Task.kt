// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.dataStructure

import androidx.compose.runtime.Stable
import com.google.firebase.Timestamp

@Stable
data class Task(
    val title: String,
    val description: String,
    val date: Timestamp = Timestamp.now(),
)
