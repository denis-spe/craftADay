// Glory be to the name of the LORD of hosts
package com.den.craftaday.backend.states

import androidx.compose.foundation.text.input.TextFieldState

data class CollectionState(
    val name: TextFieldState = TextFieldState(),
    val description: TextFieldState = TextFieldState(),
    val showForm: Boolean = false
)
