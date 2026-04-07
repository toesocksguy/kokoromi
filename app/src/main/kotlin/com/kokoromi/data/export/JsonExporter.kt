package com.kokoromi.data.export

import com.kokoromi.domain.model.Completion
import com.kokoromi.domain.model.DailyLog
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.FieldNote
import com.kokoromi.domain.model.Reflection
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

object JsonExporter {

    fun export(
        experiments: List<Experiment>,
        logsByExperiment: Map<String, List<DailyLog>>,
        reflectionsByExperiment: Map<String, List<Reflection>>,
        completionByExperiment: Map<String, Completion>,
        fieldNotes: List<FieldNote>,
        exportedAt: Instant,
    ): String {
        val root = JSONObject()
        root.put("exportedAt", exportedAt.toString())
        root.put("version", 1)

        val experimentsArray = JSONArray()
        for (experiment in experiments) {
            val obj = JSONObject()
            obj.put("id", experiment.id)
            obj.put("hypothesis", experiment.hypothesis)
            obj.put("action", experiment.action)
            obj.put("why", experiment.why)
            obj.put("startDate", experiment.startDate.toString())
            obj.put("endDate", experiment.endDate.toString())
            obj.put("frequency", experiment.frequency.name)
            obj.put("status", experiment.status.name)
            obj.put("createdAt", experiment.createdAt.toString())
            obj.put("updatedAt", experiment.updatedAt.toString())

            val logs = logsByExperiment[experiment.id] ?: emptyList()
            val logsArray = JSONArray()
            for (log in logs) {
                val logObj = JSONObject()
                logObj.put("id", log.id)
                logObj.put("date", log.date.toString())
                logObj.put("completed", log.completed)
                logObj.put("moodBefore", log.moodBefore)
                logObj.put("moodAfter", log.moodAfter)
                logObj.put("notes", log.notes)
                logObj.put("loggedAt", log.loggedAt.toString())
                logsArray.put(logObj)
            }
            obj.put("logs", logsArray)

            val reflections = reflectionsByExperiment[experiment.id] ?: emptyList()
            val reflectionsArray = JSONArray()
            for (reflection in reflections) {
                val rObj = JSONObject()
                rObj.put("id", reflection.id)
                rObj.put("reflectionDate", reflection.reflectionDate.toString())
                rObj.put("plus", reflection.plus)
                rObj.put("minus", reflection.minus)
                rObj.put("next", reflection.next)
                rObj.put("createdAt", reflection.createdAt.toString())
                reflectionsArray.put(rObj)
            }
            obj.put("reflections", reflectionsArray)

            completionByExperiment[experiment.id]?.let { completion ->
                val cObj = JSONObject()
                cObj.put("completionDate", completion.completionDate.toString())
                cObj.put("completionRate", completion.completionRate)
                cObj.put("decision", completion.decision.name)
                cObj.put("learnings", completion.learnings)
                obj.put("completion", cObj)
            }

            experimentsArray.put(obj)
        }
        root.put("experiments", experimentsArray)

        val notesArray = JSONArray()
        for (note in fieldNotes) {
            val nObj = JSONObject()
            nObj.put("id", note.id)
            nObj.put("content", note.content)
            nObj.put("createdAt", note.createdAt.toString())
            nObj.put("updatedAt", note.updatedAt.toString())
            notesArray.put(nObj)
        }
        root.put("fieldNotes", notesArray)

        return root.toString(2)
    }
}
