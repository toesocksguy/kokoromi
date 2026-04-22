package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.ExperimentWithLogs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetActiveExperimentsWithLogsUseCase @Inject constructor(
    private val experimentRepository: ExperimentRepository,
    private val dailyLogRepository: DailyLogRepository,
) {
    operator fun invoke(): Flow<List<ExperimentWithLogs>> =
        experimentRepository.getAllExperiments()
            .map { all -> all.filter { it.status == ExperimentStatus.ACTIVE } }
            .flatMapLatest { experiments ->
                if (experiments.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(experiments.map { experiment ->
                        dailyLogRepository.getLogsForExperiment(experiment.id)
                            .map { logs ->
                                val today = LocalDate.now()
                                ExperimentWithLogs(
                                    experiment = experiment,
                                    logs = logs,
                                    todayLog = logs.find { it.date == today },
                                )
                            }
                    }) { it.toList() }
                }
            }
}
