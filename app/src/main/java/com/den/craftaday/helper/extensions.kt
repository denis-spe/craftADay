// Glory be to name of LORD GOD of host
package com.den.craftaday.helper

/**
 * Extension function to convert a string to title case.
 */
val String.toTitle: String get() {
    return this.replaceFirstChar { it.uppercase() }
}