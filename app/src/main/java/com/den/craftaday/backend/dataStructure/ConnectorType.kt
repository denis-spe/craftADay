package com.den.craftaday.backend.dataStructure

/**
 * Available visual styles for node connector lines.
 */
enum class ConnectorType(val label: String) {
    BEZIER("Curved"),
    STRAIGHT("Straight"),
    STEP("Orthogonal")
}
