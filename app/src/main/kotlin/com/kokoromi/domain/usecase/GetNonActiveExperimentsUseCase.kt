package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetNonActiveExperimentsUseCase @Inject constructor(
    private val experimentRepository: ExperimentRepository,
) {
    operator fun invoke(): Flow<List<Experiment>> =
        experimentRepository.getAllExperiments()
            .map { experiments -> experiments.filter { it.status != ExperimentStatus.ACTIVE } }
}
