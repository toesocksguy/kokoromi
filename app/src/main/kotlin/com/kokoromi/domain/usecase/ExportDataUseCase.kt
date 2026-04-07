package com.kokoromi.domain.usecase

import com.kokoromi.data.export.JsonExporter
import com.kokoromi.data.repository.CompletionRepository
import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.data.repository.FieldNoteRepository
import com.kokoromi.data.repository.ReflectionRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    private val experimentRepository: ExperimentRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val reflectionRepository: ReflectionRepository,
    private val completionRepository: CompletionRepository,
    private val fieldNoteRepository: FieldNoteRepository,
) {
    suspend operator fun invoke(): Result<String> = runCatching {
        val experiments = experimentRepository.getAllExperiments().first()
        val fieldNotes = fieldNoteRepository.getAllNotes().first()

        val logsByExperiment = experiments.associate { experiment ->
            experiment.id to dailyLogRepository.getLogsForExperiment(experiment.id).first()
        }
        val reflectionsByExperiment = experiments.associate { experiment ->
            experiment.id to reflectionRepository.getReflectionsForExperiment(experiment.id).first()
        }
        val completionByExperiment = experiments.mapNotNull { experiment ->
            completionRepository.getCompletion(experiment.id)?.let { experiment.id to it }
        }.toMap()

        JsonExporter.export(
            experiments = experiments,
            logsByExperiment = logsByExperiment,
            reflectionsByExperiment = reflectionsByExperiment,
            completionByExperiment = completionByExperiment,
            fieldNotes = fieldNotes,
            exportedAt = Instant.now(),
        )
    }
}
