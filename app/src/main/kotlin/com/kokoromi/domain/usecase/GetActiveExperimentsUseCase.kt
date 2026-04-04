package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.Experiment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveExperimentsUseCase @Inject constructor(
    private val repository: ExperimentRepository,
) {
    operator fun invoke(): Flow<List<Experiment>> = repository.getActiveExperiments()
}
