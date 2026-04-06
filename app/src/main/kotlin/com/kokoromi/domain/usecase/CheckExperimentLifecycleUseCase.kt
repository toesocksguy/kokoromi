package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.ExperimentStatus
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class CheckExperimentLifecycleUseCase @Inject constructor(
    private val experimentRepository: ExperimentRepository,
) {
    suspend operator fun invoke() {
        val today = LocalDate.now()
        experimentRepository.getActiveExperiments().first()
            .filter { it.endDate < today }
            .forEach { experiment ->
                experimentRepository.updateExperimentStatus(
                    id = experiment.id,
                    status = ExperimentStatus.COMPLETED,
                )
            }
    }
}
