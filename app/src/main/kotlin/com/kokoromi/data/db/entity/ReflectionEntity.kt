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
    tableName = "reflections",
    foreignKeys = [
        ForeignKey(
            entity = ExperimentEntity::class,
            parentColumns = ["id"],
            childColumns = ["experiment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["experiment_id", "reflection_date"], unique = true)
    ]
)
data class ReflectionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "experiment_id")
    val experimentId: String,

    @ColumnInfo(name = "reflection_date")
    val reflectionDate: LocalDate,

    @ColumnInfo(name = "plus")
    val plus: String?,

    @ColumnInfo(name = "minus")
    val minus: String?,

    @ColumnInfo(name = "next")
    val next: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant
)
