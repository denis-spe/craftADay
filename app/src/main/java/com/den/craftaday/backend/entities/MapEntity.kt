// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.entities

import androidx.compose.runtime.Stable
import com.google.firebase.firestore.DocumentId

@Stable
data class MapEntity(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val layoutType: String = "TOP_DOWN",
    val connectorType: String = "BEZIER"
)
