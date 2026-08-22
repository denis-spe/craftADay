package com.den.craftaday.backend.dataStructure

import androidx.compose.runtime.Stable
import com.google.firebase.Timestamp

@Stable
sealed class Reset {
    object No : Reset()

    @Stable
    data class Once(val dateTime: Long = System.currentTimeMillis()) : Reset()
    @Stable
    data class Daily(val dateTime: Long = System.currentTimeMillis()) : Reset()
    @Stable
    data class Weekly(val dateTime: Long = System.currentTimeMillis()) : Reset()
    @Stable
    data class Monthly(val dateTime: Long = System.currentTimeMillis()) : Reset()
    @Stable
    data class Yearly(val dateTime: Long = System.currentTimeMillis()) : Reset()

    fun toMap(): Map<String, Any> {
        return when (this) {
            No -> emptyMap()
            is Once -> mapOf("once" to dateTime)
            is Daily -> mapOf("daily" to dateTime)
            is Weekly -> mapOf("weekly" to dateTime)
            is Monthly -> mapOf("monthly" to dateTime)
            is Yearly -> mapOf("yearly" to dateTime)
        }
    }

    companion object {
        private fun Any?.toLongSafe(): Long = when (this) {
            is Number -> this.toLong()
            is Timestamp -> this.toDate().time
            else -> System.currentTimeMillis()
        }

        fun fromMap(map: Map<*, *>): Reset {
            return when {
                map.containsKey("once") -> Once(map["once"].toLongSafe())
                map.containsKey("daily") -> Daily(map["daily"].toLongSafe())
                map.containsKey("weekly") -> Weekly(map["weekly"].toLongSafe())
                map.containsKey("monthly") -> Monthly(map["monthly"].toLongSafe())
                map.containsKey("yearly") -> Yearly(map["yearly"].toLongSafe())
                else -> No
            }
        }
    }
}
