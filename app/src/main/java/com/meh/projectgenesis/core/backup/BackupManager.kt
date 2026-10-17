package com.meh.projectgenesis.core.backup

import com.meh.projectgenesis.core.database.GenesisDatabase
import com.meh.projectgenesis.data.local.ActionProposalEntity
import com.meh.projectgenesis.data.local.ChatMessageEntity
import com.meh.projectgenesis.data.local.MemoryEntity
import com.meh.projectgenesis.data.local.ProjectEntity
import com.meh.projectgenesis.data.local.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

data class ImportResult(
    val success: Boolean,
    val memoriesImported: Int,
    val memoriesSkippedDuplicates: Int,
    val projectsImported: Int,
    val tasksImported: Int,
    val errorMessage: String? = null
)

class BackupManager(private val database: GenesisDatabase) {

    companion object {
        const val SCHEMA_VERSION = 1
        const val APPLICATION_IDENTIFIER = "PROJECT_GENESIS_BACKUP"
    }

    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val memories = database.memoryDao().getAllMemories().first()
        val projects = database.projectDao().getAllProjects().first()
        val tasks = database.taskDao().getAllTasks().first()

        val root = JSONObject()
        root.put("appId", APPLICATION_IDENTIFIER)
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("warning", "Keys, secrets and private credentials are excluded from this export.")
        root.put("securityClarification", "SHA-256 is used exclusively as an integrity checksum to detect file corruption. It is neither encryption nor digital signature authentication.")

        // Memories array
        val memArray = JSONArray()
        for (m in memories) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("content", m.content)
                put("memoryClass", m.memoryClass)
                put("source", m.source)
                put("projectId", m.projectId ?: JSONObject.NULL)
                put("personId", m.personId ?: JSONObject.NULL)
                put("evidenceLevel", m.evidenceLevel)
                put("confidenceScore", m.confidenceScore.toDouble())
                put("createdAt", m.createdAt)
                put("lastUsedAt", m.lastUsedAt)
                put("lastVerifiedAt", m.lastVerifiedAt)
                put("status", m.status)
                put("sensitivityLevel", m.sensitivityLevel)
                put("isUserEditable", m.isUserEditable)
                put("isDeletable", m.isDeletable)
                put("supersededById", m.supersededById ?: JSONObject.NULL)
                put("retrievalReason", m.retrievalReason ?: JSONObject.NULL)
            }
            memArray.put(obj)
        }
        root.put("memories", memArray)

        // Projects array
        val projArray = JSONArray()
        for (p in projects) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("description", p.description)
                put("goal", p.goal)
                put("role", p.role)
                put("instructions", p.instructions)
                put("baseline", p.baseline)
                put("currentStatus", p.currentStatus)
                put("isArchived", p.isArchived)
                put("createdAt", p.createdAt)
                put("updatedAt", p.updatedAt)
            }
            projArray.put(obj)
        }
        root.put("projects", projArray)

        // Tasks array
        val taskArray = JSONArray()
        for (t in tasks) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("description", t.description)
                put("projectId", t.projectId ?: JSONObject.NULL)
                put("priority", t.priority)
                put("deadline", t.deadline ?: JSONObject.NULL)
                put("status", t.status)
                put("subtasksJson", t.subtasksJson)
                put("dependencies", t.dependencies)
                put("completionEvidence", t.completionEvidence)
                put("reviewResult", t.reviewResult)
                put("createdAt", t.createdAt)
                put("updatedAt", t.updatedAt)
            }
            taskArray.put(obj)
        }
        root.put("tasks", taskArray)

        // Integrity Checksum computation (not encryption)
        val dataPayload = memArray.toString() + projArray.toString() + taskArray.toString()
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(dataPayload.toByteArray(Charsets.UTF_8))
        val checksum = hashBytes.joinToString("") { "%02x".format(it) }
        root.put("checksumSha256", checksum)

        root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String, allowOverwrite: Boolean = false): ImportResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val appId = root.optString("appId")
            if (appId != APPLICATION_IDENTIFIER) {
                return@withContext ImportResult(
                    success = false,
                    memoriesImported = 0,
                    memoriesSkippedDuplicates = 0,
                    projectsImported = 0,
                    tasksImported = 0,
                    errorMessage = "অবৈধ ব্যাকআপ ফাইল। ফাইলটি প্রজেক্ট জেনেসিসের ব্যাকআপ নয়।"
                )
            }

            val schemaVer = root.optInt("schemaVersion", 0)
            if (schemaVer > SCHEMA_VERSION) {
                return@withContext ImportResult(
                    success = false,
                    memoriesImported = 0,
                    memoriesSkippedDuplicates = 0,
                    projectsImported = 0,
                    tasksImported = 0,
                    errorMessage = "ব্যাকআপ সংস্করণ ($schemaVer) বর্তমান অ্যাপ সংস্করণ ($SCHEMA_VERSION) এর চেয়ে নতুন।"
                )
            }

            // Checksum integrity validation (detect corrupted or tampered file)
            val declaredChecksum = root.optString("checksumSha256", "")
            val memArray = root.optJSONArray("memories") ?: JSONArray()
            val projArray = root.optJSONArray("projects") ?: JSONArray()
            val taskArray = root.optJSONArray("tasks") ?: JSONArray()

            if (declaredChecksum.isNotBlank()) {
                val dataPayload = memArray.toString() + projArray.toString() + taskArray.toString()
                val md = MessageDigest.getInstance("SHA-256")
                val hashBytes = md.digest(dataPayload.toByteArray(Charsets.UTF_8))
                val calculatedChecksum = hashBytes.joinToString("") { "%02x".format(it) }
                if (calculatedChecksum != declaredChecksum) {
                    return@withContext ImportResult(
                        success = false,
                        memoriesImported = 0,
                        memoriesSkippedDuplicates = 0,
                        projectsImported = 0,
                        tasksImported = 0,
                        errorMessage = "ইন্টিগ্রিটি চেকসাম অমিল! ব্যাকআপ ফাইলটি বিকৃত বা পরিবর্তিত হয়েছে (Corrupted Backup Rejected)।"
                    )
                }
            }

            val existingMemories = database.memoryDao().getAllMemories().first().associateBy { it.id }
            val existingProjects = database.projectDao().getAllProjects().first().associateBy { it.id }

            var memImported = 0
            var memDuplicates = 0
            val memoriesToInsert = mutableListOf<MemoryEntity>()

            for (i in 0 until memArray.length()) {
                val obj = memArray.getJSONObject(i)
                val id = obj.getString("id")
                if (existingMemories.containsKey(id) && !allowOverwrite) {
                    memDuplicates++
                    continue
                }
                memoriesToInsert.add(
                    MemoryEntity(
                        id = id,
                        content = obj.getString("content"),
                        memoryClass = obj.getString("memoryClass"),
                        source = obj.optString("source", "Backup Import"),
                        projectId = if (obj.isNull("projectId")) null else obj.getString("projectId"),
                        personId = if (obj.isNull("personId")) null else obj.getString("personId"),
                        evidenceLevel = obj.optString("evidenceLevel", "E3"),
                        confidenceScore = obj.optDouble("confidenceScore", 0.9).toFloat(),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        lastUsedAt = obj.optLong("lastUsedAt", System.currentTimeMillis()),
                        lastVerifiedAt = obj.optLong("lastVerifiedAt", System.currentTimeMillis()),
                        status = obj.optString("status", "ACTIVE"),
                        sensitivityLevel = obj.optString("sensitivityLevel", "NORMAL"),
                        isUserEditable = obj.optBoolean("isUserEditable", true),
                        isDeletable = obj.optBoolean("isDeletable", true),
                        supersededById = if (obj.isNull("supersededById")) null else obj.getString("supersededById"),
                        retrievalReason = if (obj.isNull("retrievalReason")) null else obj.getString("retrievalReason")
                    )
                )
                memImported++
            }

            var projImported = 0
            val projectsToInsert = mutableListOf<ProjectEntity>()
            for (i in 0 until projArray.length()) {
                val obj = projArray.getJSONObject(i)
                val id = obj.getString("id")
                projectsToInsert.add(
                    ProjectEntity(
                        id = id,
                        name = obj.getString("name"),
                        description = obj.optString("description", ""),
                        goal = obj.optString("goal", ""),
                        role = obj.optString("role", ""),
                        instructions = obj.optString("instructions", ""),
                        baseline = obj.optString("baseline", ""),
                        currentStatus = obj.optString("currentStatus", "সক্রিয়"),
                        isArchived = obj.optBoolean("isArchived", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
                projImported++
            }

            var taskImported = 0
            val tasksToInsert = mutableListOf<TaskEntity>()
            for (i in 0 until taskArray.length()) {
                val obj = taskArray.getJSONObject(i)
                tasksToInsert.add(
                    TaskEntity(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        projectId = if (obj.isNull("projectId")) null else obj.getString("projectId"),
                        priority = obj.optInt("priority", 2),
                        deadline = if (obj.isNull("deadline")) null else obj.getLong("deadline"),
                        status = obj.optString("status", "PLAN"),
                        subtasksJson = obj.optString("subtasksJson", "[]"),
                        dependencies = obj.optString("dependencies", ""),
                        completionEvidence = obj.optString("completionEvidence", ""),
                        reviewResult = obj.optString("reviewResult", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
                taskImported++
            }

            // Transactional insertions
            if (memoriesToInsert.isNotEmpty()) {
                database.memoryDao().insertAll(memoriesToInsert)
            }
            if (projectsToInsert.isNotEmpty()) {
                database.projectDao().insertAll(projectsToInsert)
            }
            if (tasksToInsert.isNotEmpty()) {
                database.taskDao().insertAll(tasksToInsert)
            }

            ImportResult(
                success = true,
                memoriesImported = memImported,
                memoriesSkippedDuplicates = memDuplicates,
                projectsImported = projImported,
                tasksImported = taskImported
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                memoriesImported = 0,
                memoriesSkippedDuplicates = 0,
                projectsImported = 0,
                tasksImported = 0,
                errorMessage = "ব্যাকআপ পার্স করতে ত্রুটি: ${e.localizedMessage}"
            )
        }
    }
}
