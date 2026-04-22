package com.kokoromi.data.imports

import com.kokoromi.domain.model.Completion
import com.kokoromi.domain.model.DailyLog
import com.kokoromi.domain.model.DecisionType
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.FieldNote
import com.kokoromi.domain.model.Frequency
import com.kokoromi.domain.model.Reflection
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ImportPayload(
    val experiments: List<Experiment>,
    val logs: List<DailyLog>,
    val reflections: List<Reflection>,
    val completions: List<Completion>,
    val fieldNotes: List<FieldNote>,
)

object JsonImporter {

    fun parse(json: String): Result<ImportPayload> = runCatching {
        val root = JSONObject(json)
        val version = root.optInt("version", 0)
        require(version == 1) { "Unsupported export version: $version" }

        val experiments = mutableListOf<Experiment>()
        val logs = mutableListOf<DailyLog>()
        val reflections = mutableListOf<Reflection>()
        val completions = mutableListOf<Completion>()

        val experimentsArray = root.optJSONArray("experiments")
        if (experimentsArray != null) {
            for (i in 0 until experimentsArray.length()) {
                val obj = runCatching { experimentsArray.getJSONObject(i) }.getOrNull() ?: continue
                val experiment = parseExperiment(obj) ?: continue
                experiments += experiment

                obj.optJSONArray("logs")?.let { logsArray ->
                    for (j in 0 until logsArray.length()) {
                        val logObj = runCatching { logsArray.getJSONObject(j) }.getOrNull() ?: continue
                        parseLog(logObj, experiment.id)?.let { logs += it }
                    }
                }

                obj.optJSONArray("reflections")?.let { rArray ->
                    for (j in 0 until rArray.length()) {
                        val rObj = runCatching { rArray.getJSONObject(j) }.getOrNull() ?: continue
                        parseReflection(rObj, experiment.id)?.let { reflections += it }
                    }
                }

                obj.optJSONObject("completion")?.let { cObj ->
                    parseCompletion(cObj, experiment.id)?.let { completions += it }
                }
            }
        }

        val fieldNotes = mutableListOf<FieldNote>()
        root.optJSONArray("fieldNotes")?.let { notesArray ->
            for (i in 0 until notesArray.length()) {
                val nObj = runCatching { notesArray.getJSONObject(i) }.getOrNull() ?: continue
                parseFieldNote(nObj)?.let { fieldNotes += it }
            }
        }

        ImportPayload(experiments, logs, reflections, completions, fieldNotes)
    }

    private fun parseExperiment(obj: JSONObject): Experiment? = runCatching {
        Experiment(
            id = obj.getString("id"),
            hypothesis = obj.getString("hypothesis"),
            action = obj.getString("action"),
            why = nullableString(obj, "why"),
            startDate = LocalDate.parse(obj.getString("startDate")),
            endDate = LocalDate.parse(obj.getString("endDate")),
            frequency = Frequency.valueOf(obj.getString("frequency")),
            status = ExperimentStatus.valueOf(obj.getString("status")),
            createdAt = Instant.parse(obj.getString("createdAt")),
            updatedAt = Instant.parse(obj.getString("updatedAt")),
        )
    }.getOrNull()

    private fun parseLog(obj: JSONObject, experimentId: String): DailyLog? = runCatching {
        DailyLog(
            id = obj.getString("id"),
            experimentId = experimentId,
            date = LocalDate.parse(obj.getString("date")),
            completed = obj.getBoolean("completed"),
            moodBefore = if (obj.isNull("moodBefore")) null else obj.getInt("moodBefore"),
            moodAfter = if (obj.isNull("moodAfter")) null else obj.getInt("moodAfter"),
            notes = nullableString(obj, "notes"),
            loggedAt = Instant.parse(obj.getString("loggedAt")),
        )
    }.getOrNull()

    private fun parseReflection(obj: JSONObject, experimentId: String): Reflection? = runCatching {
        Reflection(
            id = obj.getString("id"),
            experimentId = experimentId,
            reflectionDate = LocalDate.parse(obj.getString("reflectionDate")),
            plus = nullableString(obj, "plus"),
            minus = nullableString(obj, "minus"),
            next = nullableString(obj, "next"),
            createdAt = Instant.parse(obj.getString("createdAt")),
        )
    }.getOrNull()

    private fun parseCompletion(obj: JSONObject, experimentId: String): Completion? = runCatching {
        Completion(
            id = UUID.randomUUID().toString(),
            experimentId = experimentId,
            completionDate = LocalDate.parse(obj.getString("completionDate")),
            completionRate = obj.getDouble("completionRate").toFloat(),
            decision = DecisionType.valueOf(obj.getString("decision")),
            learnings = nullableString(obj, "learnings"),
            nextExperimentId = null,
            createdAt = Instant.now(),
        )
    }.getOrNull()

    private fun parseFieldNote(obj: JSONObject): FieldNote? = runCatching {
        FieldNote(
            id = obj.getString("id"),
            content = obj.getString("content"),
            createdAt = Instant.parse(obj.getString("createdAt")),
            updatedAt = Instant.parse(obj.getString("updatedAt")),
        )
    }.getOrNull()

    private fun nullableString(obj: JSONObject, key: String): String? =
        if (obj.isNull(key)) null else obj.optString(key).takeIf { it.isNotEmpty() }
}
