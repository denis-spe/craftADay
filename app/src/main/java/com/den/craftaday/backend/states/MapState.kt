package com.den.craftaday.backend.states

import androidx.compose.foundation.text.input.TextFieldState

data class MapState(
    val mapId: String? = null,
    val collectionId: String? = null,
    val title: TextFieldState = TextFieldState(),
    val description: TextFieldState = TextFieldState(),
    val showForm: Boolean = false
)
