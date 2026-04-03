package com.kokoromi.domain.model

import java.time.Instant
import java.time.LocalDate

data class Reflection(
    val id: String,
    val experimentId: String,
    val reflectionDate: LocalDate,
    val plus: String?,   // What went well
    val minus: String?,  // What was hard
    val next: String?,   // What to adjust
    val createdAt: Instant
)
