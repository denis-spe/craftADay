package com.den.craftaday.backend.entities

import androidx.compose.runtime.Immutable
import com.den.craftaday.R

@Immutable
data class ListCollectionEntity(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: Int = R.drawable.default_collection,
    val createdAt: Long = System.currentTimeMillis(),
)
