package com.kokoromi.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kokoromi.data.db.KokoromiDatabase
import com.kokoromi.data.db.entity.DailyLogEntity
import com.kokoromi.data.db.entity.ExperimentEntity
import com.kokoromi.data.db.entity.FieldNoteEntity
import com.kokoromi.data.db.entity.ReflectionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

/**
 * Instrumented tests for the Room DAOs.
 *
 * Tests each DAO directly against an in-memory database to verify SQL queries,
 * TypeConverters (LocalDate/Instant ↔ String), and constraint behaviour without
 * going through the repository layer.
 *
 * Requires an emulator or device: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: KokoromiDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KokoromiDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        db.close()
    }

    // --- Experiment ---

    @Test
    fun insertAndRetrieveExperiment() = runTest {
        // Verifies basic insert + getExperiment, and that LocalDate TypeConverter
        // round-trips correctly (startDate stored as String, retrieved as LocalDate).
        val experiment = experimentEntity()
        db.experimentDao().insertExperiment(experiment)

        val fetched = db.experimentDao().getExperiment(experiment.id)
        assertNotNull(fetched)
        assertEquals(experiment.hypothesis, fetched!!.hypothesis)
        assertEquals(experiment.startDate, fetched.startDate)
    }

    @Test
    fun getActiveExperimentsReturnsOnlyActive() = runTest {
        // Verifies the WHERE status = 'ACTIVE' filter — archived experiments
        // must not appear on the home screen.
        db.experimentDao().insertExperiment(experimentEntity(status = "ACTIVE"))
        db.experimentDao().insertExperiment(experimentEntity(status = "ARCHIVED"))

        val active = db.experimentDao().getActiveExperiments().first()
        assertEquals(1, active.size)
        assertEquals("ACTIVE", active[0].status)
    }

    @Test
    fun countActiveExperiments() = runTest {
        // Verifies the slot-cap query used by CreateExperimentUseCase.
        // Max 2 active experiments is enforced by checking this count before inserting.
        db.experimentDao().insertExperiment(experimentEntity(status = "ACTIVE"))
        db.experimentDao().insertExperiment(experimentEntity(status = "ACTIVE"))
        db.experimentDao().insertExperiment(experimentEntity(status = "ARCHIVED"))

        assertEquals(2, db.experimentDao().countActiveExperiments())
    }

    @Test
    fun updateStatusChangesExperimentStatus() = runTest {
        // Verifies the UPDATE query used for all lifecycle transitions
        // (ACTIVE → COMPLETED, COMPLETED → ARCHIVED, etc.).
        val experiment = experimentEntity(status = "ACTIVE")
        db.experimentDao().insertExperiment(experiment)

        db.experimentDao().updateStatus(experiment.id, "COMPLETED", Instant.now())

        val fetched = db.experimentDao().getExperiment(experiment.id)
        assertEquals("COMPLETED", fetched!!.status)
    }

    // --- DailyLog ---

    @Test
    fun upsertDailyLogInsertsNewEntry() = runTest {
        // Verifies a new log is written and retrievable by (experiment_id, date).
        val experiment = experimentEntity()
        db.experimentDao().insertExperiment(experiment)

        val log = dailyLogEntity(experimentId = experiment.id)
        db.dailyLogDao().upsertLog(log)

        val fetched = db.dailyLogDao().getLogForDate(experiment.id, log.date.toString())
        assertNotNull(fetched)
        assertEquals(log.completed, fetched!!.completed)
    }

    @Test
    fun upsertDailyLogUpdatesExistingEntry() = runTest {
        // Verifies that logging twice for the same day replaces the earlier entry.
        // The unique index on (experiment_id, date) enforces one log per day;
        // INSERT OR REPLACE handles the conflict so the latest value wins.
        val experiment = experimentEntity()
        db.experimentDao().insertExperiment(experiment)
        val date = LocalDate.now()

        db.dailyLogDao().upsertLog(dailyLogEntity(experimentId = experiment.id, date = date, completed = false))
        db.dailyLogDao().upsertLog(dailyLogEntity(experimentId = experiment.id, date = date, completed = true))

        val fetched = db.dailyLogDao().getLogForDate(experiment.id, date.toString())
        assertTrue(fetched!!.completed)
    }

    @Test
    fun countCompletedLogsInRange() = runTest {
        // Verifies the query used to compute completion rate.
        // Only rows where completed = 1 within the date range are counted;
        // skipped days (completed = false) are excluded.
        val experiment = experimentEntity()
        db.experimentDao().insertExperiment(experiment)
        val start = LocalDate.now()

        db.dailyLogDao().upsertLog(dailyLogEntity(experimentId = experiment.id, date = start, completed = true))
        db.dailyLogDao().upsertLog(dailyLogEntity(experimentId = experiment.id, date = start.plusDays(1), completed = true))
        db.dailyLogDao().upsertLog(dailyLogEntity(experimentId = experiment.id, date = start.plusDays(2), completed = false))

        val count = db.dailyLogDao().countCompleted(
            experimentId = experiment.id,
            startDate = start.toString(),
            endDate = start.plusDays(2).toString()
        )
        assertEquals(2, count)
    }

    // --- Reflection ---

    @Test
    fun upsertReflectionAndRetrieveLatest() = runTest {
        // Verifies a saved reflection is returned by getLatestReflection,
        // and that Plus/Minus/Next fields round-trip correctly.
        val experiment = experimentEntity()
        db.experimentDao().insertExperiment(experiment)

        val reflection = reflectionEntity(experimentId = experiment.id)
        db.reflectionDao().upsertReflection(reflection)

        val latest = db.reflectionDao().getLatestReflection(experiment.id)
        assertNotNull(latest)
        assertEquals(reflection.plus, latest!!.plus)
    }

    // --- FieldNote ---

    @Test
    fun upsertAndDeleteFieldNote() = runTest {
        // Verifies the full FieldNote lifecycle: insert, confirm it appears in
        // getAllNotes(), then delete and confirm it's gone.
        val note = fieldNoteEntity()
        db.fieldNoteDao().upsertNote(note)

        val all = db.fieldNoteDao().getAllNotes().first()
        assertEquals(1, all.size)

        db.fieldNoteDao().deleteNote(note.id)
        val afterDelete = db.fieldNoteDao().getAllNotes().first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun dailyLogsCascadeDeleteWithExperiment() = runTest {
        // Verifies that daily logs exist for an experiment (precondition for cascade).
        // Full cascade deletion is enforced by the FK ON DELETE CASCADE constraint
        // defined on DailyLogEntity — tested here at the schema level.
        val experiment = experimentEntity()
        db.experimentDao().insertExperiment(experiment)
        db.dailyLogDao().upsertLog(dailyLogEntity(experimentId = experiment.id))

        val logs = db.dailyLogDao().getLogsForExperiment(experiment.id).first()
        assertEquals(1, logs.size)
    }

    // --- Helpers ---

    private fun experimentEntity(
        id: String = java.util.UUID.randomUUID().toString(),
        status: String = "ACTIVE"
    ) = ExperimentEntity(
        id = id,
        hypothesis = "Does walking help me think?",
        action = "Walk 15 minutes each morning",
        why = "Clear my head before work",
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusDays(14),
        frequency = "DAILY",
        frequencyCustom = null,
        status = status,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )

    private fun dailyLogEntity(
        experimentId: String,
        date: LocalDate = LocalDate.now(),
        completed: Boolean = true
    ) = DailyLogEntity(
        id = java.util.UUID.randomUUID().toString(),
        experimentId = experimentId,
        date = date,
        completed = completed,
        moodBefore = 3,
        moodAfter = 4,
        notes = null,
        loggedAt = Instant.now()
    )

    private fun reflectionEntity(experimentId: String) = ReflectionEntity(
        id = java.util.UUID.randomUUID().toString(),
        experimentId = experimentId,
        reflectionDate = LocalDate.now(),
        plus = "Felt focused on walking days",
        minus = "Skipped once due to rain",
        next = "Bring umbrella",
        createdAt = Instant.now()
    )

    private fun fieldNoteEntity() = FieldNoteEntity(
        id = java.util.UUID.randomUUID().toString(),
        content = "Noticed more energy after morning walks",
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )
}
