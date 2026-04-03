package com.kokoromi.domain.model

import java.time.Instant

data class FieldNote(
    val id: String,
    val content: String,
    val createdAt: Instant,  // User-editable: the observation timestamp, not insert time
    val updatedAt: Instant   // System-managed: updated on every save
)
