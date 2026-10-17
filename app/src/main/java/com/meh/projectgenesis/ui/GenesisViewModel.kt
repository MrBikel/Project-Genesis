package com.meh.projectgenesis.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meh.projectgenesis.core.backup.ImportResult
import com.meh.projectgenesis.core.database.GenesisDatabase
import com.meh.projectgenesis.core.datastore.UserPreferencesDataStore
import com.meh.projectgenesis.core.security.KeyManager
import com.meh.projectgenesis.data.local.ActionProposalEntity
import com.meh.projectgenesis.data.local.ChatMessageEntity
import com.meh.projectgenesis.data.repository.GenesisRepository
import com.meh.projectgenesis.domain.model.AutonomyLevel
import com.meh.projectgenesis.domain.model.MemoryClass
import com.meh.projectgenesis.domain.model.MemoryRecord
import com.meh.projectgenesis.domain.model.Project
import com.meh.projectgenesis.domain.model.ReasoningMode
import com.meh.projectgenesis.domain.model.ReasoningPipelineResult
import com.meh.projectgenesis.domain.model.Task
import com.meh.projectgenesis.domain.model.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GenesisViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GenesisDatabase.getInstance(application)
    private val prefs = UserPreferencesDataStore(application)
    val keyManager = KeyManager(application)
    private val repository = GenesisRepository(database, prefs, keyManager)

    // User Preferences State
    val language: StateFlow<String> = prefs.languageFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "bn")

    val theme: StateFlow<String> = prefs.themeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "DARK")

    val reasoningMode: StateFlow<ReasoningMode> = prefs.reasoningModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ReasoningMode.BALANCED)

    val autonomyLevel: StateFlow<AutonomyLevel> = prefs.autonomyLevelFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AutonomyLevel.LEVEL_2)

    val isEmergencyStopActive: StateFlow<Boolean> = prefs.emergencyStopFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val aiProvider: StateFlow<String> = prefs.aiProviderFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "LOCAL")

    val hasCompletedOnboarding: StateFlow<Boolean> = prefs.onboardingCompletedFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Active Project context
    private val _selectedProjectId = MutableStateFlow<String?>(null)
    val selectedProjectId: StateFlow<String?> = _selectedProjectId.asStateFlow()

    // Deep-dive Reasoning View State
    private val _latestReasoningResult = MutableStateFlow<ReasoningPipelineResult?>(null)
    val latestReasoningResult: StateFlow<ReasoningPipelineResult?> = _latestReasoningResult.asStateFlow()

    // Processing state
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Database Streams
    val allMemories: StateFlow<List<MemoryRecord>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val candidateMemories: StateFlow<List<MemoryRecord>> = repository.candidateMemories
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeProjects: StateFlow<List<Project>> = repository.activeProjects
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pendingTasks: StateFlow<List<Task>> = repository.pendingTasks
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pendingActions: StateFlow<List<ActionProposalEntity>> = repository.pendingActionProposals
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allActions: StateFlow<List<ActionProposalEntity>> = repository.actionProposals
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Memory filter
    private val _memoryFilterClass = MutableStateFlow<String?>(null)
    val memoryFilterClass: StateFlow<String?> = _memoryFilterClass.asStateFlow()

    private val _memorySearchQuery = MutableStateFlow("")
    val memorySearchQuery: StateFlow<String> = _memorySearchQuery.asStateFlow()

    val filteredMemories: StateFlow<List<MemoryRecord>> = combine(
        allMemories,
        _memoryFilterClass,
        _memorySearchQuery
    ) { memories, filterClass, query ->
        memories.filter { mem ->
            val matchClass = filterClass == null || mem.memoryClass.name == filterClass
            val matchQuery = query.isBlank() || mem.content.contains(query, ignoreCase = true) ||
                    mem.source.contains(query, ignoreCase = true)
            matchClass && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSelectedProject(projectId: String?) {
        _selectedProjectId.value = projectId
    }

    fun setMemoryFilterClass(filter: String?) {
        _memoryFilterClass.value = filter
    }

    fun setMemorySearchQuery(query: String) {
        _memorySearchQuery.value = query
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            prefs.setOnboardingCompleted(true)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            prefs.setLanguage(lang)
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            prefs.setTheme(theme)
        }
    }

    fun setReasoningMode(mode: ReasoningMode) {
        viewModelScope.launch {
            prefs.setReasoningMode(mode)
        }
    }

    fun setAutonomyLevel(level: AutonomyLevel) {
        viewModelScope.launch {
            prefs.setAutonomyLevel(level)
        }
    }

    fun toggleEmergencyStop() {
        viewModelScope.launch {
            val current = isEmergencyStopActive.value
            prefs.setEmergencyStop(!current)
        }
    }

    fun setAiProvider(provider: String) {
        viewModelScope.launch {
            prefs.setAiProvider(provider)
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = repository.processUserMessage(
                    content = content,
                    activeProjectId = _selectedProjectId.value
                )
                _latestReasoningResult.value = result
            } catch (e: Exception) {
                // Handled gracefully in repository, ensure processing is released
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun setMessageFeedback(msgId: String, feedback: String) {
        viewModelScope.launch {
            repository.setMessageFeedback(msgId, feedback)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun approveMemory(id: String) {
        viewModelScope.launch {
            repository.approveCandidateMemory(id)
        }
    }

    fun rejectMemory(id: String) {
        viewModelScope.launch {
            repository.rejectCandidateMemory(id)
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun correctMemory(oldId: String, newContent: String) {
        viewModelScope.launch {
            repository.correctMemory(oldId, newContent)
        }
    }

    fun saveMemory(record: MemoryRecord) {
        viewModelScope.launch {
            repository.saveMemory(record)
        }
    }

    fun saveProject(project: Project) {
        viewModelScope.launch {
            repository.saveProject(project)
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
            if (_selectedProjectId.value == id) {
                _selectedProjectId.value = null
            }
        }
    }

    fun saveTask(task: Task) {
        viewModelScope.launch {
            repository.saveTask(task)
        }
    }

    fun updateTaskStatus(task: Task, newStatus: TaskStatus) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun approveAction(id: String) {
        viewModelScope.launch {
            repository.approveAction(id)
        }
    }

    fun rejectAction(id: String) {
        viewModelScope.launch {
            repository.rejectAction(id)
        }
    }

    fun rollbackAction(id: String) {
        viewModelScope.launch {
            repository.rollbackAction(id)
        }
    }

    suspend fun exportBackupJson(): String {
        return repository.exportBackupJson()
    }

    suspend fun importBackupJson(json: String, overwrite: Boolean = false): ImportResult {
        return repository.importBackupJson(json, overwrite)
    }

    fun saveGeminiApiKey(key: String) {
        keyManager.saveGeminiApiKey(key)
    }

    fun clearGeminiApiKey() {
        keyManager.clearGeminiApiKey()
    }

    fun saveOpenAiApiKey(key: String) {
        keyManager.saveOpenAiApiKey(key)
    }

    fun clearOpenAiApiKey() {
        keyManager.clearOpenAiApiKey()
    }
}
