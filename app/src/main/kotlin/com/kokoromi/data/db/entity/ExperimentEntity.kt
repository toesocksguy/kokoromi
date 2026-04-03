package com.kokoromi.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "experiments")
data class ExperimentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "hypothesis")
    val hypothesis: String,

    @ColumnInfo(name = "action")
    val action: String,

    @ColumnInfo(name = "why")
    val why: String?,

    @ColumnInfo(name = "start_date")
    val startDate: LocalDate,

    @ColumnInfo(name = "end_date")
    val endDate: LocalDate,

    @ColumnInfo(name = "frequency")
    val frequency: String,  // "DAILY" or "CUSTOM"

    @ColumnInfo(name = "frequency_custom")
    val frequencyCustom: String?,  // Reserved for future custom schedules

    @ColumnInfo(name = "status")
    val status: String,  // "ACTIVE", "COMPLETED", "ARCHIVED", "PAUSED"

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)
