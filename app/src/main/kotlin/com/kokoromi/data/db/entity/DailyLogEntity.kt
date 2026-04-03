package com.kokoromi.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "daily_logs",
    foreignKeys = [
        ForeignKey(
            entity = ExperimentEntity::class,
            parentColumns = ["id"],
            childColumns = ["experiment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["experiment_id", "date"], unique = true),
        Index(value = ["experiment_id"])
    ]
)
data class DailyLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "experiment_id")
    val experimentId: String,

    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "completed")
    val completed: Boolean,

    @ColumnInfo(name = "mood_before")
    val moodBefore: Int?,

    @ColumnInfo(name = "mood_after")
    val moodAfter: Int?,

    @ColumnInfo(name = "notes")
    val notes: String?,

    @ColumnInfo(name = "logged_at")
    val loggedAt: Instant
)
