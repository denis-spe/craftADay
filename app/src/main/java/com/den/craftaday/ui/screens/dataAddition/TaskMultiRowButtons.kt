// Glory be to the name of the LORD of hosts
package com.den.craftaday.ui.screens.dataAddition

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun TaskMultiRowButtons(
    onDescriptionShow: Boolean,
    onDateShow: Boolean,
    onDescriptionClick: (Boolean) -> Unit,
    onDateClick: (Boolean) -> Unit
) {
    MultiChoiceSegmentedButtonRow {
        SegmentedButton(
            checked = onDescriptionShow,
            onCheckedChange = onDescriptionClick,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Description")
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
