package com.kokoromi.domain.model

enum class DecisionType {
    PERSIST, // Keep going — experiment is working, start a new round
    PIVOT,   // Adjust approach or try something new
    PAUSE    // Set aside for now — data preserved, no guilt
}
