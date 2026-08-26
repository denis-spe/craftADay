// Glory be to name of LORD GOD of host
package com.den.craftaday.helper

import java.util.Locale

/**
 * Extension function to convert a string to title case.
 */
val String.toTitle: String get() {
    return this.replaceFirstChar { it.uppercase() }
}

val Long.toLocalTimeDate: String get() {
    val date = java.util.Date(this)
    val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
        java.util.Locale.getDefault())
    return format.format(date)
}

val Long.toLocalDate: String get() {
    val date = java.util.Date(this)
    val format = java.text.SimpleDateFormat("yyyy-MM-dd",
        java.util.Locale.getDefault())
    return format.format(date)
}

val Long.toLocalTime: String get() {
    val date = java.util.Date(this)
    val format = java.text.SimpleDateFormat("HH:mm:ss",
        java.util.Locale.getDefault())
    return format.format(date)
}

val Long.toMinutes: String get() {
    val minutes = this / 1000 / 60
    return String.format(Locale.getDefault(), "%02d", minutes)
}

val Long.toHours: String get() {
    val hours = this / 1000 / 60 / 60
    return String.format(Locale.getDefault(), "%02d", hours)
}

val Long.CurrentTimeChange: TimeChange get() {
    val currentTime = System.currentTimeMillis()
    val timeDifference = this - currentTime
    val absDiff = kotlin.math.abs(timeDifference)
    val minutes = absDiff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24
    val months = days / 30
    val years = months / 12

    return when {
        years > 0 -> TimeChange.Years(years)
        months > 0 -> TimeChange.Months(months)
        days > 0 -> TimeChange.Days(days)
        hours > 0 -> TimeChange.Hours(hours)
        minutes > 0 -> TimeChange.Minutes(minutes)
        else -> TimeChange.JustNow
    }
}

val List<Long>.CurrentTimeChange: TimeChange get() {
    val lastTimestamp = this.lastOrNull() ?: 0L
    val currentTime = System.currentTimeMillis()
    val timeDifference = lastTimestamp - currentTime
    val absDiff = kotlin.math.abs(timeDifference)
    val minutes = absDiff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24
    val months = days / 30
    val years = months / 12
    val date = java.util.Date(lastTimestamp)
    val format = java.text.SimpleDateFormat("yyyy-MM-dd",
        Locale.getDefault())
    val dateString = format.format(date)

    return when {
        years > 0 -> TimeChange.Years(years)
        months > 0 -> TimeChange.Months(months)
        days > 0 -> TimeChange.Days(days)
        hours > 0 -> TimeChange.Hours(hours)
        minutes > 0 -> TimeChange.Minutes(minutes)
        this.isNotEmpty() -> TimeChange.SpecificDate(dateString)

        else -> TimeChange.JustNow
    }
}
