// Bless be the name of the LORD GOD
package com.den.craftaday.ui.screens.components.btn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DescriptionBtn(
    label: String,
    text: TextFieldState,
) {
    val onDismissState = remember { mutableStateOf(false) }
    val textState = rememberTextFieldState()

    val state = remember { mutableStateOf("") }
    val textDescription = remember(state.value) {
        state.value.ifEmpty {
            label
        }
    }


//    Button(onClick = {
//        onDismissState.value = true
//    }) {
//        Text(textDescription)
//    }


    if (onDismissState.value) {
        Dialog(
            onDismissRequest = {
                onDismissState.value = false
            }
        ) {
            Card {
                Column(
                    modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DescriptionHeader(label) {
                        onDismissState.value = false
                    }

                    DescriptionTextField(
                        state = textState,
                        modifier = Modifier.fillMaxWidth()
                    )

                    DescriptionButtons(
                        onConfirm = {
                            state.value = textState.text.toString()
                            text.setTextAndPlaceCursorAtEnd(textState.text.toString())
                            onDismissState.value = false
                        },
                        onCancel = {
                            onDismissState.value = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DescriptionHeader(
    title: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge
        )

        IconButton(
            onClick = onDismiss
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
        }
    }
}

@Composable
fun DescriptionTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            state = state,
            label = { Text("Description") },
            modifier = modifier,
            lineLimits = TextFieldLineLimits.MultiLine(),
            inputTransformation = InputTransformation.maxLength(1000)
        )
    }
}

@Composable
fun DescriptionButtons(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onCancel
        ) {
            Text("Cancel")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onConfirm
        ) {
            Text("Confirm")
        }
    }
}