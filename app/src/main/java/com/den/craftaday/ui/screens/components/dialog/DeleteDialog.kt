// Bless be the LORD GOD
package com.den.craftaday.ui.screens.components.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.den.craftaday.helper.limitTo

@Composable
fun DeleteDialog(
    title: String = "Task",
    onShow: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    if (onShow) {
        AlertDialog(
            onDismissRequest = onCancel,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFE8574A)
                )
            },
            title = {
                Text(text = "Delete ${title.limitTo(20)}")
            },
            text = {
                Text(text = "Are you sure you want to permanently delete this task? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE8574A),
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancel
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
