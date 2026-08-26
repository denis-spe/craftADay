package com.den.craftaday.backend.entities.types

import androidx.compose.runtime.Stable
import com.google.firebase.Timestamp

@Stable
sealed class TaskAlarmType {
    @Stable
    data class Once(
        val startedAt: Long = System.currentTimeMillis(),
        val duration: Long = System.currentTimeMillis()
    ) : TaskAlarmType()
    @Stable
    data class Daily(
        val startedAt: Long = System.currentTimeMillis(),
        val duration: Long = System.currentTimeMillis()
    ) : TaskAlarmType()
    @Stable
    data class Weekly(
        val startedAt: Long = System.currentTimeMillis(),
        val duration: Long = System.currentTimeMillis()
    ) : TaskAlarmType()
    @Stable
    data class Monthly(
        val startedAt: Long = System.currentTimeMillis(),
        val duration: Long = System.currentTimeMillis()
    ) : TaskAlarmType()
    @Stable
    data class Yearly(
        val startedAt: Long = System.currentTimeMillis(),
        val duration: Long = System.currentTimeMillis()
    ) : TaskAlarmType()
    @Stable
    data class SpecificWeekDay(val dateTimes: List<Long> = emptyList()) : TaskAlarmType()
    @Stable
    data class SpecificDate(val dateTimes: List<Long> = emptyList()) : TaskAlarmType()

    val primaryTimestamp: Long
        get() = when (this) {
            is Once -> duration
            is Daily -> duration
            is Weekly -> duration
            is Monthly -> duration
            is Yearly -> duration
            is SpecificWeekDay -> dateTimes.lastOrNull() ?: 0L
            is SpecificDate -> dateTimes.lastOrNull() ?: 0L
        }

    fun toMap(): Map<String, Any> {
        return when (this) {
            is Once -> mapOf("once" to mapOf("startedAt" to startedAt, "duration" to duration))
            is Daily -> mapOf("daily" to mapOf("startedAt" to startedAt, "duration" to duration))
            is Weekly -> mapOf("weekly" to mapOf("startedAt" to startedAt, "duration" to duration))
            is Monthly -> mapOf("monthly" to mapOf("startedAt" to startedAt, "duration" to duration))
            is Yearly -> mapOf("yearly" to mapOf("startedAt" to startedAt, "duration" to duration))
            is SpecificWeekDay -> mapOf("specificWeekDay" to dateTimes)
            is SpecificDate -> mapOf("specificDate" to dateTimes)
        }
    }

    companion object {
        private fun Any?.toLongSafe(): Long = when (this) {
            is Number -> this.toLong()
            is Timestamp -> this.toDate().time
            else -> System.currentTimeMillis()
        }

        fun fromMap(map: Map<*, *>): TaskAlarmType {
            return when {
                map.containsKey("once") -> Once(
                    startedAt = (map["once"] as? Map<*, *>)?.get("startedAt").toLongSafe(),
                    duration = (map["once"] as? Map<*, *>)?.get("duration").toLongSafe()
                )
                map.containsKey("daily") -> Daily(
                    startedAt = (map["daily"] as? Map<*, *>)?.get("startedAt").toLongSafe(),
                    duration = (map["daily"] as? Map<*, *>)?.get("duration").toLongSafe()
                )
                map.containsKey("weekly") -> Weekly(
                    startedAt = (map["weekly"] as? Map<*, *>)?.get("startedAt").toLongSafe(),
                    duration = (map["weekly"] as? Map<*, *>)?.get("duration").toLongSafe()
                )
                map.containsKey("monthly") -> Monthly(
                    startedAt = (map["monthly"] as? Map<*, *>)?.get("startedAt").toLongSafe(),
                    duration = (map["monthly"] as? Map<*, *>)?.get("duration").toLongSafe()
                )
                map.containsKey("yearly") -> Yearly(
                    startedAt = (map["yearly"] as? Map<*, *>)?.get("startedAt").toLongSafe(),
                    duration = (map["yearly"] as? Map<*, *>)?.get("duration").toLongSafe()
                )
                map.containsKey("specificWeekDay") -> SpecificWeekDay(
                    (map["specificWeekDay"] as List<*>).filterIsInstance<Long>()
                )
                map.containsKey("specificDate") -> SpecificDate(
                    (map["specificDate"] as List<*>).filterIsInstance<Long>()
                )

                else -> { SpecificWeekDay() }
            }
        }
    }
}
