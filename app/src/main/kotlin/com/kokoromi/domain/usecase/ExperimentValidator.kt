package com.kokoromi.domain.usecase

import com.kokoromi.util.Constants

object ExperimentValidator {

    fun validate(
        hypothesis: String,
        action: String,
        why: String?,
        durationDays: Int,
    ): Result<Unit> {
        if (hypothesis.isBlank())
            return Result.failure(IllegalArgumentException("Hypothesis cannot be empty"))
        if (hypothesis.length > Constants.HYPOTHESIS_MAX_CHARS)
            return Result.failure(IllegalArgumentException("Hypothesis exceeds ${Constants.HYPOTHESIS_MAX_CHARS} characters"))

        if (action.isBlank())
            return Result.failure(IllegalArgumentException("Action cannot be empty"))
        if (action.length > Constants.ACTION_MAX_CHARS)
            return Result.failure(IllegalArgumentException("Action exceeds ${Constants.ACTION_MAX_CHARS} characters"))

        if (why != null && why.length > Constants.WHY_MAX_CHARS)
            return Result.failure(IllegalArgumentException("Why exceeds ${Constants.WHY_MAX_CHARS} characters"))

        if (durationDays < Constants.MIN_EXPERIMENT_DURATION_DAYS || durationDays > Constants.MAX_EXPERIMENT_DURATION_DAYS)
            return Result.failure(IllegalArgumentException("Duration must be between ${Constants.MIN_EXPERIMENT_DURATION_DAYS} and ${Constants.MAX_EXPERIMENT_DURATION_DAYS} days"))

        return Result.success(Unit)
    }
}
