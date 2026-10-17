package com.meh.projectgenesis.data.repository

import com.meh.projectgenesis.core.backup.BackupManager
import com.meh.projectgenesis.core.backup.ImportResult
import com.meh.projectgenesis.core.database.GenesisDatabase
import com.meh.projectgenesis.core.datastore.UserPreferencesDataStore
import com.meh.projectgenesis.core.security.KeyManager
import com.meh.projectgenesis.data.local.ActionProposalEntity
import com.meh.projectgenesis.data.local.ChatMessageEntity
import com.meh.projectgenesis.data.local.MemoryEntity
import com.meh.projectgenesis.data.local.ProjectEntity
import com.meh.projectgenesis.data.local.TaskEntity
import com.meh.projectgenesis.domain.model.ActionStatus
import com.meh.projectgenesis.domain.model.AutonomyLevel
import com.meh.projectgenesis.domain.model.EvidenceLevel
import com.meh.projectgenesis.domain.model.MemoryClass
import com.meh.projectgenesis.domain.model.MemoryRecord
import com.meh.projectgenesis.domain.model.MemoryStatus
import com.meh.projectgenesis.domain.model.Project
import com.meh.projectgenesis.domain.model.ReasoningMode
import com.meh.projectgenesis.domain.model.ReasoningPipelineResult
import com.meh.projectgenesis.domain.model.SensitivityLevel
import com.meh.projectgenesis.domain.model.Task
import com.meh.projectgenesis.domain.model.TaskStatus
import com.meh.projectgenesis.domain.pipeline.GenesisReasoningEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class GenesisRepository(
    private val database: GenesisDatabase,
    private val prefs: UserPreferencesDataStore,
    private val keyManager: KeyManager,
    private val pipeline: GenesisReasoningEngine = GenesisReasoningEngine(database),
    private val backupManager: BackupManager = BackupManager(database)
) {

    // --- Memories ---
    val allMemories: Flow<List<MemoryRecord>> = database.memoryDao().getAllMemories().map { entities ->
        entities.map { it.toDomain() }
    }

    val candidateMemories: Flow<List<MemoryRecord>> = database.memoryDao().getCandidateMemories().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getMemoriesByClass(className: String): Flow<List<MemoryRecord>> {
        return database.memoryDao().getMemoriesByClass(className).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getMemoriesByProject(projectId: String): Flow<List<MemoryRecord>> {
        return database.memoryDao().getMemoriesByProject(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveMemory(record: MemoryRecord) {
        database.memoryDao().insert(record.toEntity())
    }

    suspend fun updateMemory(record: MemoryRecord) {
        database.memoryDao().update(record.toEntity())
    }

    suspend fun deleteMemory(id: String) {
        database.memoryDao().deleteById(id)
    }

    suspend fun approveCandidateMemory(id: String) {
        val memory = database.memoryDao().getMemoryById(id) ?: return
        val approved = memory.copy(
            status = MemoryStatus.VERIFIED.name,
            lastVerifiedAt = System.currentTimeMillis()
        )
        database.memoryDao().update(approved)
    }

    suspend fun rejectCandidateMemory(id: String) {
        val memory = database.memoryDao().getMemoryById(id) ?: return
        val rejected = memory.copy(
            status = MemoryStatus.INCORRECT.name,
            memoryClass = MemoryClass.REJECTED_OR_SUPERSEDED.name
        )
        database.memoryDao().update(rejected)
    }

    suspend fun correctMemory(oldMemoryId: String, newContent: String): MemoryRecord? {
        val old = database.memoryDao().getMemoryById(oldMemoryId) ?: return null
        val newId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // 1. Mark previous record as SUPERSEDED without deleting it from history
        val supersededOld = old.copy(
            status = MemoryStatus.SUPERSEDED.name,
            supersededById = newId,
            lastVerifiedAt = now
        )
        database.memoryDao().update(supersededOld)

        // 2. Insert new corrected record with verified status
        val newRecord = MemoryRecord(
            id = newId,
            content = newContent,
            memoryClass = try { MemoryClass.valueOf(old.memoryClass) } catch (_: Exception) { MemoryClass.VALIDATED_REUSABLE_LEARNING },
            source = "ব্যবহারকারীর প্রত্যক্ষ সংশোধন (Correction of ${old.id.take(8)})",
            projectId = old.projectId,
            personId = old.personId,
            evidenceLevel = EvidenceLevel.E3,
            confidenceScore = 1.0f,
            createdAt = now,
            lastUsedAt = now,
            lastVerifiedAt = now,
            status = MemoryStatus.VERIFIED,
            sensitivityLevel = try { SensitivityLevel.valueOf(old.sensitivityLevel) } catch (_: Exception) { SensitivityLevel.NORMAL },
            isUserEditable = true,
            isDeletable = true,
            supersededById = null,
            retrievalReason = "পূর্ববর্তী মেমোরি প্রতিস্থাপনপূর্বক সক্রিয় সত্য রেকর্ড।"
        )
        database.memoryDao().insert(newRecord.toEntity())
        return newRecord
    }

    // --- Projects ---
    val allProjects: Flow<List<Project>> = database.projectDao().getAllProjects().map { entities ->
        entities.map { it.toDomain() }
    }

    val activeProjects: Flow<List<Project>> = database.projectDao().getActiveProjects().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun saveProject(project: Project) {
        database.projectDao().insert(project.toEntity())
    }

    suspend fun updateProject(project: Project) {
        database.projectDao().update(project.toEntity())
    }

    suspend fun deleteProject(id: String) {
        database.projectDao().deleteById(id)
    }

    // --- Tasks ---
    val allTasks: Flow<List<Task>> = database.taskDao().getAllTasks().map { entities ->
        entities.map { it.toDomain() }
    }

    val pendingTasks: Flow<List<Task>> = database.taskDao().getPendingTasks().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getTasksByProject(projectId: String): Flow<List<Task>> {
        return database.taskDao().getTasksByProject(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveTask(task: Task) {
        database.taskDao().insert(task.toEntity())
    }

    suspend fun updateTask(task: Task) {
        database.taskDao().update(task.toEntity())
    }

    suspend fun deleteTask(id: String) {
        database.taskDao().deleteById(id)
    }

    // --- Chat Messages ---
    val chatMessages: Flow<List<ChatMessageEntity>> = database.chatMessageDao().getAllMessages()

    suspend fun clearChat() {
        database.chatMessageDao().clearAll()
    }

    suspend fun setMessageFeedback(id: String, feedback: String) {
        val messages = database.chatMessageDao().getAllMessages().first()
        val msg = messages.find { it.id == id } ?: return
        database.chatMessageDao().update(msg.copy(userFeedback = feedback))
    }

    // --- Actions ---
    val actionProposals: Flow<List<ActionProposalEntity>> = database.actionProposalDao().getAll()
    val pendingActionProposals: Flow<List<ActionProposalEntity>> = database.actionProposalDao().getPending()

    suspend fun approveAction(id: String) {
        val action = database.actionProposalDao().getById(id) ?: return
        database.actionProposalDao().update(action.copy(status = ActionStatus.APPROVED.name))
    }

    suspend fun rejectAction(id: String) {
        val action = database.actionProposalDao().getById(id) ?: return
        database.actionProposalDao().update(action.copy(status = ActionStatus.REJECTED.name))
    }

    suspend fun rollbackAction(id: String) {
        val action = database.actionProposalDao().getById(id) ?: return
        database.actionProposalDao().update(action.copy(status = ActionStatus.ROLLED_BACK.name))
    }

    // --- Reasoning Execution ---
    suspend fun processUserMessage(
        content: String,
        activeProjectId: String?
    ): ReasoningPipelineResult {
        val now = System.currentTimeMillis()
        val userMsgId = UUID.randomUUID().toString()

        // 1. Insert user message
        database.chatMessageDao().insert(
            ChatMessageEntity(
                id = userMsgId,
                conversationId = "main",
                sender = "user",
                content = content,
                language = if (content.any { it in '\u0980'..'\u09FF' }) "bn" else "en",
                evidenceSummary = null,
                highestEvidenceLevel = null,
                reasoningSummary = null,
                memoryUsedCount = 0,
                timestamp = now
            )
        )

        // 2. Fetch active settings
        val reasoningMode = prefs.reasoningModeFlow.first()
        val currentAutonomy = prefs.autonomyLevelFlow.first()
        val emergencyStop = prefs.emergencyStopFlow.first()
        val providerType = prefs.aiProviderFlow.first()
        val apiKey = when (providerType) {
            "GEMINI" -> keyManager.getGeminiApiKey()
            "OPENAI" -> keyManager.getOpenAiApiKey()
            else -> null
        }

        // 3. Execute 11-step pipeline
        val result = pipeline.executePipeline(
            userInput = content,
            activeProjectId = activeProjectId,
            reasoningMode = reasoningMode,
            currentAutonomy = currentAutonomy,
            isEmergencyStopActive = emergencyStop,
            selectedProviderType = providerType,
            apiKey = apiKey
        )

        // 4. Save candidate learning if found
        result.candidateLearning?.let { candidate ->
            database.memoryDao().insert(candidate.toEntity())
        }

        // 5. Insert Genesis response message
        database.chatMessageDao().insert(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "main",
                sender = "genesis",
                content = result.synthesizedResponseBn,
                language = result.language,
                evidenceSummary = result.evidenceSummary,
                highestEvidenceLevel = result.highestEvidenceLevel.code,
                reasoningSummary = result.decisionExplanationBn,
                memoryUsedCount = result.relevantContextSummaries.size,
                timestamp = System.currentTimeMillis()
            )
        )

        return result
    }

    // --- Backup & Restore ---
    suspend fun exportBackupJson(): String {
        return backupManager.createBackupJson()
    }

    suspend fun importBackupJson(json: String, overwrite: Boolean = false): ImportResult {
        return backupManager.restoreBackupJson(json, overwrite)
    }

    // --- Helpers / Mappers ---
    private fun MemoryEntity.toDomain() = MemoryRecord(
        id = id,
        content = content,
        memoryClass = try { MemoryClass.valueOf(memoryClass) } catch (_: Exception) { MemoryClass.TEMPORARY_CONTEXT },
        source = source,
        projectId = projectId,
        personId = personId,
        evidenceLevel = try { EvidenceLevel.valueOf(evidenceLevel) } catch (_: Exception) { EvidenceLevel.E3 },
        confidenceScore = confidenceScore,
        createdAt = createdAt,
        lastUsedAt = lastUsedAt,
        lastVerifiedAt = lastVerifiedAt,
        status = try { MemoryStatus.valueOf(status) } catch (_: Exception) { MemoryStatus.ACTIVE },
        sensitivityLevel = try { SensitivityLevel.valueOf(sensitivityLevel) } catch (_: Exception) { SensitivityLevel.NORMAL },
        isUserEditable = isUserEditable,
        isDeletable = isDeletable,
        supersededById = supersededById,
        retrievalReason = retrievalReason
    )

    private fun MemoryRecord.toEntity() = MemoryEntity(
        id = id,
        content = content,
        memoryClass = memoryClass.name,
        source = source,
        projectId = projectId,
        personId = personId,
        evidenceLevel = evidenceLevel.name,
        confidenceScore = confidenceScore,
        createdAt = createdAt,
        lastUsedAt = lastUsedAt,
        lastVerifiedAt = lastVerifiedAt,
        status = status.name,
        sensitivityLevel = sensitivityLevel.name,
        isUserEditable = isUserEditable,
        isDeletable = isDeletable,
        supersededById = supersededById,
        retrievalReason = retrievalReason
    )

    private fun ProjectEntity.toDomain() = Project(
        id = id,
        name = name,
        description = description,
        goal = goal,
        role = role,
        instructions = instructions,
        baseline = baseline,
        currentStatus = currentStatus,
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Project.toEntity() = ProjectEntity(
        id = id,
        name = name,
        description = description,
        goal = goal,
        role = role,
        instructions = instructions,
        baseline = baseline,
        currentStatus = currentStatus,
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun TaskEntity.toDomain() = Task(
        id = id,
        title = title,
        description = description,
        projectId = projectId,
        priority = priority,
        deadline = deadline,
        status = try { TaskStatus.valueOf(status) } catch (_: Exception) { TaskStatus.PLAN },
        subtasksJson = subtasksJson,
        dependencies = dependencies,
        completionEvidence = completionEvidence,
        reviewResult = reviewResult,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Task.toEntity() = TaskEntity(
        id = id,
        title = title,
        description = description,
        projectId = projectId,
        priority = priority,
        deadline = deadline,
        status = status.name,
        subtasksJson = subtasksJson,
        dependencies = dependencies,
        completionEvidence = completionEvidence,
        reviewResult = reviewResult,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
