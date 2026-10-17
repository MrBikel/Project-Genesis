package com.meh.projectgenesis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meh.projectgenesis.ui.GenesisViewModel
import com.meh.projectgenesis.ui.components.GenesisBottomNav
import com.meh.projectgenesis.ui.screens.actions.ActionCenterScreen
import com.meh.projectgenesis.ui.screens.conversation.ConversationScreen
import com.meh.projectgenesis.ui.screens.home.HomeScreen
import com.meh.projectgenesis.ui.screens.learning.LearningReviewScreen
import com.meh.projectgenesis.ui.screens.memory.MemoryControlCenterScreen
import com.meh.projectgenesis.ui.screens.onboarding.OnboardingScreen
import com.meh.projectgenesis.ui.screens.projects.ProjectsScreen
import com.meh.projectgenesis.ui.screens.reasoning.ReasoningViewScreen
import com.meh.projectgenesis.ui.screens.settings.SettingsScreen
import com.meh.projectgenesis.ui.screens.tasks.TasksScreen
import com.meh.projectgenesis.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GenesisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentTheme by viewModel.theme.collectAsStateWithLifecycle()
            val isDarkTheme = when (currentTheme) {
                "LIGHT" -> false
                "DARK" -> true
                else -> true
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                GenesisApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun GenesisApp(viewModel: GenesisViewModel) {
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.language.collectAsStateWithLifecycle()
    val currentTheme by viewModel.theme.collectAsStateWithLifecycle()
    val reasoningMode by viewModel.reasoningMode.collectAsStateWithLifecycle()
    val autonomyLevel by viewModel.autonomyLevel.collectAsStateWithLifecycle()
    val isEmergencyStopActive by viewModel.isEmergencyStopActive.collectAsStateWithLifecycle()
    val aiProvider by viewModel.aiProvider.collectAsStateWithLifecycle()
    val selectedProjectId by viewModel.selectedProjectId.collectAsStateWithLifecycle()

    val allMemories by viewModel.allMemories.collectAsStateWithLifecycle()
    val candidateMemories by viewModel.candidateMemories.collectAsStateWithLifecycle()
    val filteredMemories by viewModel.filteredMemories.collectAsStateWithLifecycle()
    val selectedFilterClass by viewModel.memoryFilterClass.collectAsStateWithLifecycle()
    val searchQuery by viewModel.memorySearchQuery.collectAsStateWithLifecycle()

    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val activeProject = allProjects.find { it.id == selectedProjectId }

    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val pendingTasks by viewModel.pendingTasks.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val latestReasoningResult by viewModel.latestReasoningResult.collectAsStateWithLifecycle()

    val pendingActions by viewModel.pendingActions.collectAsStateWithLifecycle()
    val allActions by viewModel.allActions.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf("home") }

    if (!hasCompletedOnboarding) {
        OnboardingScreen(
            currentLanguage = currentLanguage,
            onLanguageChange = { viewModel.setLanguage(it) },
            onComplete = { viewModel.completeOnboarding() }
        )
    } else {
        // Back handling to return to Home screen
        BackHandler(enabled = currentScreen != "home") {
            currentScreen = "home"
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                // Show bottom nav on main navigation tabs
                if (currentScreen != "reasoning" && currentScreen != "learning") {
                    GenesisBottomNav(
                        currentRoute = currentScreen,
                        onNavigate = { currentScreen = it }
                    )
                }
            }
        ) { innerPadding ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "home" -> HomeScreen(
                        activeProject = activeProject,
                        activeReasoningMode = reasoningMode,
                        autonomyLevel = autonomyLevel,
                        isEmergencyStopActive = isEmergencyStopActive,
                        candidateMemoriesCount = candidateMemories.size,
                        pendingTasksCount = pendingTasks.size,
                        pendingActionsCount = pendingActions.size,
                        onNavigate = { currentScreen = it },
                        onQuickSend = { prompt ->
                            viewModel.sendMessage(prompt)
                        }
                    )

                    "conversation" -> ConversationScreen(
                        messages = chatMessages,
                        isProcessing = isProcessing,
                        reasoningMode = reasoningMode,
                        onSendMessage = { viewModel.sendMessage(it) },
                        onClearChat = { viewModel.clearChat() },
                        onFeedback = { id, feedback -> viewModel.setMessageFeedback(id, feedback) },
                        onOpenReasoningView = { currentScreen = "reasoning" }
                    )

                    "reasoning" -> ReasoningViewScreen(
                        result = latestReasoningResult,
                        onBack = { currentScreen = "conversation" }
                    )

                    "memory" -> MemoryControlCenterScreen(
                        memories = filteredMemories,
                        selectedFilterClass = selectedFilterClass,
                        searchQuery = searchQuery,
                        onFilterChange = { viewModel.setMemoryFilterClass(it) },
                        onSearchChange = { viewModel.setMemorySearchQuery(it) },
                        onApproveMemory = { viewModel.approveMemory(it) },
                        onRejectMemory = { viewModel.rejectMemory(it) },
                        onDeleteMemory = { viewModel.deleteMemory(it) },
                        onSaveMemory = { viewModel.saveMemory(it) },
                        onCorrectMemory = { oldId, newContent -> viewModel.correctMemory(oldId, newContent) }
                    )

                    "projects" -> ProjectsScreen(
                        projects = allProjects,
                        activeProjectId = selectedProjectId,
                        onSelectActiveProject = { viewModel.setSelectedProject(it) },
                        onSaveProject = { viewModel.saveProject(it) },
                        onDeleteProject = { viewModel.deleteProject(it) }
                    )

                    "tasks" -> TasksScreen(
                        tasks = allTasks,
                        onSaveTask = { viewModel.saveTask(it) },
                        onUpdateStatus = { task, newStatus -> viewModel.updateTaskStatus(task, newStatus) },
                        onDeleteTask = { viewModel.deleteTask(it) }
                    )

                    "learning" -> LearningReviewScreen(
                        candidates = candidateMemories,
                        onApprove = { viewModel.approveMemory(it) },
                        onReject = { viewModel.rejectMemory(it) }
                    )

                    "actions" -> ActionCenterScreen(
                        currentAutonomy = autonomyLevel,
                        isEmergencyStopActive = isEmergencyStopActive,
                        pendingActions = pendingActions,
                        allActions = allActions,
                        onSetAutonomyLevel = { viewModel.setAutonomyLevel(it) },
                        onToggleEmergencyStop = { viewModel.toggleEmergencyStop() },
                        onApproveAction = { viewModel.approveAction(it) },
                        onRejectAction = { viewModel.rejectAction(it) },
                        onRollbackAction = { viewModel.rollbackAction(it) }
                    )

                    "settings" -> SettingsScreen(
                        currentLanguage = currentLanguage,
                        currentTheme = currentTheme,
                        currentReasoningMode = reasoningMode,
                        currentAiProvider = aiProvider,
                        keyManager = viewModel.keyManager,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onThemeChange = { viewModel.setTheme(it) },
                        onReasoningModeChange = { viewModel.setReasoningMode(it) },
                        onAiProviderChange = { viewModel.setAiProvider(it) },
                        onSaveGeminiKey = { viewModel.saveGeminiApiKey(it) },
                        onClearGeminiKey = { viewModel.clearGeminiApiKey() },
                        onOpenDiagnostics = { currentScreen = "diagnostics" },
                        onExportBackup = { viewModel.exportBackupJson() },
                        onImportBackup = { json -> viewModel.importBackupJson(json) }
                    )

                    "diagnostics" -> com.meh.projectgenesis.ui.screens.diagnostics.DiagnosticsScreen(
                        diagnostics = latestReasoningResult?.diagnostics,
                        onBack = { currentScreen = "settings" }
                    )
                }
            }
        }
    }
}
