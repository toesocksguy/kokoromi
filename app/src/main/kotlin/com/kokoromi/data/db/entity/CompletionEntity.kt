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
    tableName = "completions",
    foreignKeys = [
        ForeignKey(
            entity = ExperimentEntity::class,
            parentColumns = ["id"],
            childColumns = ["experiment_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExperimentEntity::class,
            parentColumns = ["id"],
            childColumns = ["next_experiment_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["experiment_id"], unique = true),
        Index(value = ["next_experiment_id"])
    ]
)
data class CompletionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "experiment_id")
    val experimentId: String,

    @ColumnInfo(name = "completion_date")
    val completionDate: LocalDate,

    @ColumnInfo(name = "completion_rate")
    val completionRate: Float,

    @ColumnInfo(name = "decision")
    val decision: String,  // "PERSIST", "PIVOT", "PAUSE"

    @ColumnInfo(name = "learnings")
    val learnings: String?,

    @ColumnInfo(name = "next_experiment_id")
    val nextExperimentId: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant
)
