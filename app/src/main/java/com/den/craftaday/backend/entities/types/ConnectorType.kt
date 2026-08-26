package com.den.craftaday.backend.entities.types

/**
 * Available visual styles for node connector lines.
 */
enum class ConnectorType(val label: String) {
    BEZIER("Curved"),
    STRAIGHT("Straight"),
    STEP("Orthogonal")
}