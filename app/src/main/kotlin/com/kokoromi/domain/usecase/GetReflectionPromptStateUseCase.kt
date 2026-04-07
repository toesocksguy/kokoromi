package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.ReflectionRepository
import com.kokoromi.domain.model.Experiment
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class ReflectionPromptState(
    val shouldShowPrompt: Boolean,
    val experimentId: String,
    val experimentName: String,
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
)

class GetReflectionPromptStateUseCase @Inject constructor(
    private val reflectionRepository: ReflectionRepository,
) {
    suspend operator fun invoke(
        experiment: Experiment,
        today: LocalDate = LocalDate.now(),
        reflectionDay: DayOfWeek,
    ): ReflectionPromptState {
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val isReflectionDay = today.dayOfWeek == reflectionDay
        if (!isReflectionDay) {
            return ReflectionPromptState(
                shouldShowPrompt = false,
                experimentId = experiment.id,
                experimentName = experiment.hypothesis,
                weekStart = weekStart,
                weekEnd = weekEnd,
            )
        }

        val existingReflection = reflectionRepository.getReflectionInRange(
            experimentId = experiment.id,
            startDate = weekStart,
            endDate = weekEnd,
        )

        return ReflectionPromptState(
            shouldShowPrompt = existingReflection == null,
            experimentId = experiment.id,
            experimentName = experiment.hypothesis,
            weekStart = weekStart,
            weekEnd = weekEnd,
        )
    }
}
