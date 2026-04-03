package com.kokoromi.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kokoromi.data.db.dao.CompletionDao
import com.kokoromi.data.db.dao.DailyLogDao
import com.kokoromi.data.db.dao.ExperimentDao
import com.kokoromi.data.db.dao.FieldNoteDao
import com.kokoromi.data.db.dao.ReflectionDao
import com.kokoromi.data.db.entity.CompletionEntity
import com.kokoromi.data.db.entity.DailyLogEntity
import com.kokoromi.data.db.entity.ExperimentEntity
import com.kokoromi.data.db.entity.FieldNoteEntity
import com.kokoromi.data.db.entity.ReflectionEntity

@Database(
    entities = [
        ExperimentEntity::class,
        DailyLogEntity::class,
        ReflectionEntity::class,
        CompletionEntity::class,
        FieldNoteEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KokoromiDatabase : RoomDatabase() {
    abstract fun experimentDao(): ExperimentDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun reflectionDao(): ReflectionDao
    abstract fun completionDao(): CompletionDao
    abstract fun fieldNoteDao(): FieldNoteDao
}
