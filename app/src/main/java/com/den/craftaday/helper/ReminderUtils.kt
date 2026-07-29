// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.helper

import com.google.firebase.Timestamp
import java.util.Calendar

object ReminderUtils {
    fun calculateNextTimestamp(current: Timestamp, repeat: String): Timestamp {
        val calendar = Calendar.getInstance().apply {
            time = current.toDate()
        }
        when (repeat) {
            "DAILY" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
        }
        // Ensure the next time is in the future
        while (calendar.timeInMillis <= System.currentTimeMillis()) {
            when (repeat) {
                "DAILY" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
                else -> break // Should not happen with repeat != NONE
            }
        }
        return Timestamp(calendar.time)
    }
}
