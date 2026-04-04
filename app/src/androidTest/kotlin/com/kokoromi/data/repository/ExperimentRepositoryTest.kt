package com.kokoromi.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kokoromi.data.db.KokoromiDatabase
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Frequency
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ExperimentRepositoryTest {

    private lateinit var db: KokoromiDatabase
    private lateinit var repository: DefaultExperimentRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KokoromiDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = DefaultExperimentRepository(
            experimentDao = db.experimentDao(),
            completionDao = db.completionDao()
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun createAndRetrieveExperiment() = runTest {
        val experiment = experiment()
        repository.createExperiment(experiment)

        val fetched = repository.getExperiment(experiment.id)
        assertNotNull(fetched)
        assertEquals(experiment.hypothesis, fetched!!.hypothesis)
        assertEquals(experiment.startDate, fetched.startDate)
        assertEquals(experiment.status, fetched.status)
        assertEquals(experiment.frequency, fetched.frequency)
    }

    @Test
    fun getActiveExperimentsEmitsOnlyActive() = runTest {
        repository.createExperiment(experiment(status = ExperimentStatus.ACTIVE))
        repository.createExperiment(experiment(status = ExperimentStatus.ARCHIVED))

        val active = repository.getActiveExperiments().first()
        assertEquals(1, active.size)
        assertEquals(ExperimentStatus.ACTIVE, active[0].status)
    }

    @Test
    fun getActiveExperimentCountReflectsInserts() = runTest {
        assertEquals(0, repository.getActiveExperimentCount())

        repository.createExperiment(experiment(status = ExperimentStatus.ACTIVE))
        repository.createExperiment(experiment(status = ExperimentStatus.ACTIVE))
        assertEquals(2, repository.getActiveExperimentCount())
    }

    @Test
    fun updateExperimentStatusPersists() = runTest {
        val experiment = experiment(status = ExperimentStatus.ACTIVE)
        repository.createExperiment(experiment)

        repository.updateExperimentStatus(experiment.id, ExperimentStatus.COMPLETED)

        val fetched = repository.getExperiment(experiment.id)
        assertEquals(ExperimentStatus.COMPLETED, fetched!!.status)
    }

    @Test
    fun getExperimentReturnsNullForUnknownId() = runTest {
        assertNull(repository.getExperiment("does-not-exist"))
    }

    @Test
    fun getAllExperimentsReturnsAll() = runTest {
        repository.createExperiment(experiment(status = ExperimentStatus.ACTIVE))
        repository.createExperiment(experiment(status = ExperimentStatus.ARCHIVED))
        repository.createExperiment(experiment(status = ExperimentStatus.PAUSED))

        val all = repository.getAllExperiments().first()
        assertEquals(3, all.size)
    }

    // --- Helpers ---

    private fun experiment(
        id: String = UUID.randomUUID().toString(),
        status: ExperimentStatus = ExperimentStatus.ACTIVE
    ) = Experiment(
        id = id,
        hypothesis = "Does walking help me think?",
        action = "Walk 15 minutes each morning",
        why = "Clear my head before work",
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusDays(14),
        frequency = Frequency.DAILY,
        status = status,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )
}
