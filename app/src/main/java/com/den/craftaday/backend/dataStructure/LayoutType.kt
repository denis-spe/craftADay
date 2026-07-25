package com.den.craftaday.backend.dataStructure

/**
 * The available automatic arrangement algorithms for the diagram/tree screen.
 */
enum class LayoutType(val label: String) {
    TOP_DOWN("Top to Bottom"),
    LEFT_RIGHT("Left to Right"),
    RADIAL("Radial"),
    GRID("Grid")
}