package com.kokoromi.domain.model

data class ExperimentWithLogs(
    val experiment: Experiment,
    val logs: List<DailyLog>,
    val todayLog: DailyLog?,
)
