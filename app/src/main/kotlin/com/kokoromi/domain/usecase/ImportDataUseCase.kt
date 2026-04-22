package com.kokoromi.domain.usecase

import com.kokoromi.data.imports.ImportPayload
import com.kokoromi.data.imports.JsonImporter
import com.kokoromi.data.repository.CompletionRepository
import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.data.repository.FieldNoteRepository
import com.kokoromi.data.repository.ReflectionRepository
import javax.inject.Inject

data class ImportResult(
    val experimentsAdded: Int,
    val logsProcessed: Int,
    val reflectionsProcessed: Int,
    val completionsAdded: Int,
    val fieldNotesAdded: Int,
)

class ImportDataUseCase @Inject constructor(
    private val experimentRepository: ExperimentRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val reflectionRepository: ReflectionRepository,
    private val completionRepository: CompletionRepository,
    private val fieldNoteRepository: FieldNoteRepository,
) {
    suspend operator fun invoke(json: String): Result<ImportResult> {
        val payload = JsonImporter.parse(json).getOrElse { return Result.failure(it) }
        return runCatching { persist(payload) }
    }

    private suspend fun persist(payload: ImportPayload): ImportResult {
        var experimentsAdded = 0
        for (experiment in payload.experiments) {
            if (experimentRepository.getExperiment(experiment.id) == null) {
                experimentRepository.createExperiment(experiment)
                experimentsAdded++
            }
        }

        for (log in payload.logs) {
            dailyLogRepository.upsertDailyLog(log)
        }

        for (reflection in payload.reflections) {
            reflectionRepository.saveReflection(reflection)
        }

        var completionsAdded = 0
        for (completion in payload.completions) {
            if (completionRepository.getCompletion(completion.experimentId) == null) {
                completionRepository.saveCompletion(completion)
                completionsAdded++
            }
        }

        var fieldNotesAdded = 0
        for (note in payload.fieldNotes) {
            fieldNoteRepository.saveNote(note)
            fieldNotesAdded++
        }

        return ImportResult(
            experimentsAdded = experimentsAdded,
            logsProcessed = payload.logs.size,
            reflectionsProcessed = payload.reflections.size,
            completionsAdded = completionsAdded,
            fieldNotesAdded = fieldNotesAdded,
        )
    }
}
