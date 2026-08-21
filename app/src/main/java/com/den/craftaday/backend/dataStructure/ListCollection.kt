package com.den.craftaday.backend.dataStructure

import androidx.compose.runtime.Immutable

@Immutable
data class ListCollection(
    val id: String = "",
    val name: String = "",
    val createdAt: Long = 0L,
)
