package com.kokoromi.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val KokoromiShapes = Shapes(
    // Small: chips, text fields, small buttons
    small = RoundedCornerShape(8.dp),
    // Medium: cards, dialogs
    medium = RoundedCornerShape(16.dp),
    // Large: bottom sheets, larger containers
    large = RoundedCornerShape(24.dp),
    // Extra large: full-screen sheets
    extraLarge = RoundedCornerShape(28.dp)
)
