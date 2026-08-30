// Glory be to the name of the LORD of hosts
package com.den.craftaday.ui.screens.dataAddition

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.den.craftaday.helper.limitTo
import com.den.craftaday.helper.toTitle
import com.den.craftaday.ui.screens.components.dialog.DescriptionDialog


@Composable
fun TaskMultiRowButtons(
    descriptionTextState: TextFieldState,
    onDateShow: Boolean,
    onDescriptionClick: () -> Unit,
    onDateClick: (Boolean) -> Unit
) {
    MultiChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            checked = descriptionTextState.text.isNotEmpty(),
            onCheckedChange = {
                onDescriptionClick()
            },
            shape = RoundedCornerShape(10.dp)
        ) {
            val text = descriptionTextState.text.ifEmpty {
                "No description"
            }.toString().limitTo(10).toTitle

            Text(text)
        }

        Spacer(modifier = Modifier.width(8.dp))

        SegmentedButton(
            checked = onDateShow,
            onCheckedChange = onDateClick,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Date")
        }
    }
}
