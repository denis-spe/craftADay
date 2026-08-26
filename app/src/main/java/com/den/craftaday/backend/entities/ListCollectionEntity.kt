package com.den.craftaday.backend.entities

import androidx.compose.runtime.Immutable

@Immutable
data class ListCollectionEntity(
    val id: String = "",
    val name: String = "",
    val createdAt: Long = 0L,
)
