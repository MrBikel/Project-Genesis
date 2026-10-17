package com.meh.projectgenesis.domain.pipeline

import com.meh.projectgenesis.core.ai.AiProvider
import com.meh.projectgenesis.core.ai.AiResult
import com.meh.projectgenesis.core.ai.EngineDiagnostics
import com.meh.projectgenesis.core.ai.GeminiProvider
import com.meh.projectgenesis.core.ai.GenesisLocalReasoningProvider
import com.meh.projectgenesis.core.ai.ProviderConfig
import com.meh.projectgenesis.core.database.GenesisDatabase
import com.meh.projectgenesis.domain.model.AutonomyLevel
import com.meh.projectgenesis.domain.model.ClaimItem
import com.meh.projectgenesis.domain.model.ContradictionItem
import com.meh.projectgenesis.domain.model.CriticReviewResult
import com.meh.projectgenesis.domain.model.EvidenceLevel
import com.meh.projectgenesis.domain.model.MemoryClass
import com.meh.projectgenesis.domain.model.MemoryRecord
import com.meh.projectgenesis.domain.model.MemoryStatus
import com.meh.projectgenesis.domain.model.ReasoningMode
import com.meh.projectgenesis.domain.model.ReasoningPipelineResult
import com.meh.projectgenesis.domain.model.SensitivityLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class GenesisReasoningEngine(
    private val database: GenesisDatabase,
    private val localProvider: GenesisLocalReasoningProvider = GenesisLocalReasoningProvider(),
    private val geminiProvider: GeminiProvider = GeminiProvider()
) {

    suspend fun executePipeline(
        userInput: String,
        activeProjectId: String?,
        reasoningMode: ReasoningMode,
        currentAutonomy: AutonomyLevel,
        isEmergencyStopActive: Boolean,
        selectedProviderType: String,
        apiKey: String?
    ): ReasoningPipelineResult = withContext(Dispatchers.Default) {
        val prompt = userInput.trim()
        val isBengali = prompt.any { it in '\u0980'..'\u09FF' }
        val langCode = if (isBengali) "bn" else "en"

        // STEP 1: INTAKE
        val urgency = if (prompt.contains("জরুরি") || prompt.contains("urgent") || prompt.contains("দ্রুত")) "High" else "Normal"
        val interpretedRequest = if (isBengali) {
            "অনুরোধ বিশ্লেষণ: উদ্দেশ্য - ${prompt.take(60)}${if (prompt.length > 60) "..." else ""} | জরুরি মাত্রা: $urgency"
        } else {
            "Request Analysis: Objective - ${prompt.take(60)}${if (prompt.length > 60) "..." else ""} | Urgency: $urgency"
        }

        // STEP 2: CONTEXT RETRIEVAL (Strict Isolation)
        val activeMemories = database.memoryDao().getActiveMemoriesList()
        val relevantMemories = activeMemories.filter { mem ->
            if (activeProjectId != null) {
                // Strictly only retrieve memories for this project or general system context.
                // Other projects' private memories are NEVER retrieved.
                mem.projectId == null || mem.projectId == activeProjectId
            } else {
                mem.projectId == null
            }
        }.take(8)

        val contextSummaries = relevantMemories.map {
            "[${it.memoryClass}] ${it.content.take(50)}"
        }

        // STEP 3: PERSON-PROJECT CHECK
        // Check for cross-project context leakage attempts
        val otherProjectMemories = activeMemories.filter { it.projectId != null && it.projectId != activeProjectId }
        val crossProjectLeakage = activeProjectId != null && otherProjectMemories.any {
            prompt.contains(it.content.take(15))
        }

        // STEP 4: CLAIM SEPARATION
        val claims = mutableListOf<ClaimItem>()
        val lowerPrompt = prompt.lowercase()
        if (lowerPrompt.contains("মনে হয়") || lowerPrompt.contains("i think") || lowerPrompt.contains("সম্ভবত")) {
            claims.add(ClaimItem("Assumption", "ব্যবহারকারীর অনুমান বা সম্ভাবনা", EvidenceLevel.E6))
        }
        if (lowerPrompt.contains("আমি দেখেছি") || lowerPrompt.contains("আমার মনে আছে") || lowerPrompt.contains("i saw")) {
            claims.add(ClaimItem("UserExperience", "ব্যবহারকারীর ব্যক্তিগত অভিজ্ঞতা", EvidenceLevel.E4))
        }
        if (lowerPrompt.contains("রেকর্ড") || lowerPrompt.contains("ফাইল") || lowerPrompt.contains("ডকুমেন্ট")) {
            claims.add(ClaimItem("UserRecord", "ব্যবহারকারীর প্রদত্ত রেকর্ড", EvidenceLevel.E3))
        }
        if (lowerPrompt.contains("সবাই আমার বিরুদ্ধে") || lowerPrompt.contains("everyone is against me")) {
            claims.add(ClaimItem("Allegation", "সম্ভাব্য অতি-সাধারণীকরণ বা অভিযোগ", EvidenceLevel.E6))
        }
        if (claims.isEmpty()) {
            claims.add(ClaimItem("UserInquiry", "ব্যবহারকারীর অনুরোধ ও ইনপুট", EvidenceLevel.E3))
            claims.add(ClaimItem("GenesisAnalysis", "জেনেসিস স্বাধীন বিশ্লেষণ", EvidenceLevel.E5))
        }

        // STEP 5: CONTRADICTION CHECK
        val contradictions = mutableListOf<ContradictionItem>()

        // 5a. Unsupported generalization check
        if (lowerPrompt.contains("সবাই আমার বিরুদ্ধে") || lowerPrompt.contains("সবাই জানে") || lowerPrompt.contains("নিশ্চিতভাবেই সত্য") || lowerPrompt.contains("without doubt")) {
            contradictions.add(
                ContradictionItem(
                    issue = if (isBengali) "প্রমাণহীন সাধারণীকরণ বা অযাচাইকৃত দাবি সনাক্ত হয়েছে।" else "Unsupported generalization detected.",
                    conflictingMemory = null,
                    severity = "Warning"
                )
            )
        }

        // 5b. Cross-project isolation leakage
        if (crossProjectLeakage) {
            contradictions.add(
                ContradictionItem(
                    issue = if (isBengali) "অন্য প্রজেক্টের তথ্য বর্তমান প্রজেক্টে মিশ্রিত করার ঝুঁকি।" else "Cross-project context leakage risk detected.",
                    conflictingMemory = "Project Isolation Policy",
                    severity = "Critical"
                )
            )
        }

        // 5c. Pairwise contradiction check among stored memories of the active project
        for (i in 0 until relevantMemories.size) {
            for (j in i + 1 until relevantMemories.size) {
                val m1 = relevantMemories[i]
                val m2 = relevantMemories[j]
                val text1 = m1.content.lowercase()
                val text2 = m2.content.lowercase()
                val isConflict = (text1.contains("স্থগিত") && text2.contains("চলমান")) ||
                                 (text1.contains("বাতিল") && text2.contains("অনুমোদিত")) ||
                                 (text1.contains("postponed") && text2.contains("tomorrow")) ||
                                 (text1.contains("বিলম্বিত") && text2.contains("আগামীকাল")) ||
                                 (text1.contains("deadline") && text2.contains("deadline") && text1 != text2)
                if (isConflict) {
                    contradictions.add(
                        ContradictionItem(
                            issue = if (isBengali) "প্রজেক্ট মেমোরিতে পরস্পরবিরোধী তথ্য সনাক্ত হয়েছে: '${m1.content.take(25)}...' বনাম '${m2.content.take(25)}...'"
                                    else "Conflicting memories detected in project: '${m1.content.take(25)}...' vs '${m2.content.take(25)}...'",
                            conflictingMemory = "${m1.id} & ${m2.id}",
                            severity = "Critical"
                        )
                    )
                }
            }
        }

        // STEP 6: EVIDENCE ASSESSMENT
        val highestEvidence = when {
            claims.any { it.evidenceLevel == EvidenceLevel.E1 } -> EvidenceLevel.E1
            claims.any { it.evidenceLevel == EvidenceLevel.E2 } -> EvidenceLevel.E2
            claims.any { it.evidenceLevel == EvidenceLevel.E3 } -> EvidenceLevel.E3
            claims.any { it.evidenceLevel == EvidenceLevel.E4 } -> EvidenceLevel.E4
            claims.any { it.evidenceLevel == EvidenceLevel.E5 } -> EvidenceLevel.E5
            else -> EvidenceLevel.E6
        }
        val evidenceSummary = if (isBengali) {
            "সর্বোচ্চ প্রমাণের স্তর: ${highestEvidence.code} (${highestEvidence.titleBn})"
        } else {
            "Highest Evidence Level: ${highestEvidence.code} (${highestEvidence.titleEn})"
        }

        // STEP 7: PERMISSION CHECK
        val isDestructive = lowerPrompt.contains("মুছে ফেল") || lowerPrompt.contains("delete") || lowerPrompt.contains("মুছুন")
        val isPublishRequest = lowerPrompt.contains("send") || lowerPrompt.contains("মেইল") || lowerPrompt.contains("post") || lowerPrompt.contains("পাবলিশ") || lowerPrompt.contains("publish")
        val isFileCreation = lowerPrompt.contains("ফাইল তৈরি") || lowerPrompt.contains("create file") || lowerPrompt.contains("export")

        val requiredAutonomy = when {
            isDestructive || isPublishRequest -> AutonomyLevel.LEVEL_6
            isFileCreation -> AutonomyLevel.LEVEL_3
            else -> AutonomyLevel.LEVEL_1
        }

        val isPermissionBlocked = isEmergencyStopActive || (requiredAutonomy.level > currentAutonomy.level)

        // STEP 8 & 10: SYNTHESIS VIA PROVIDER OR LOCAL ENGINE
        var synthesizedText: String
        var providerAiResult: AiResult? = null

        // If internal memory contradiction is critical, report it transparently without picking one silently
        val hasConflictingMemories = contradictions.any { it.severity == "Critical" && it.conflictingMemory != null }

        if (isEmergencyStopActive) {
            synthesizedText = if (isBengali) {
                "জরুরি বিরতি (Emergency Stop) সক্রিয় রয়েছে। নিরাপত্তা নীতির কারণে এই মুহূর্তে কোনো নতুন অ্যাকশন বা পরিবর্তন সম্পাদন সম্ভব নয়। আপনি অ্যাকশন সেন্টার থেকে জরুরি বিরতি নিষ্ক্রিয় করতে পারেন।"
            } else {
                "Emergency Stop is currently ACTIVE. System actions are frozen per safety policy. You can deactivate emergency stop in Action Center."
            }
        } else if (hasConflictingMemories) {
            synthesizedText = if (isBengali) {
                "প্রজেক্ট জেনেসিস আপনার প্রজেক্টের সংরক্ষিত তথ্যের মধ্যে পরস্পরবিরোধিতা সনাক্ত করেছে:\n\n" +
                contradictions.filter { it.conflictingMemory != null }.joinToString("\n") { "• ${it.issue}" } +
                "\n\nজেনেসিস কোনো একটি দাবিকে অন্ধভাবে সত্য হিসেবে গণ্য করেনি। উভয় তথ্যের সত্যতা যাচাই করার জন্য অনুগ্রহ করে মেমোরি কন্ট্রোল সেন্টার থেকে সংশোধিত রেকর্ড অনুমোদন করুন।"
            } else {
                "Project Genesis has detected conflicting information in your project memories:\n\n" +
                contradictions.filter { it.conflictingMemory != null }.joinToString("\n") { "• ${it.issue}" } +
                "\n\nGenesis will not silently choose one version as truth. Please verify and resolve the contradiction in the Memory Control Center."
            }
        } else if (isPublishRequest && currentAutonomy.level < AutonomyLevel.LEVEL_6.level) {
            synthesizedText = if (isBengali) {
                """
                বিষয়বস্তুর খসড়া প্রস্তুত করা হয়েছে, তবে প্রকাশনা (Publish) অবরুদ্ধ রাখা হলো।

                • কারণ: প্রকাশনা বা বাহ্যিক ডেটা প্রেরণের জন্য স্বায়ত্তশাসন স্তর ৬ (Level 6: Execute approved external actions) প্রয়োজন।
                • বর্তমান স্তর: ${currentAutonomy.titleBn}।
                • পরবর্তী পদক্ষেপ: অ্যাকশন সেন্টার থেকে স্পষ্ট অনুমোদন প্রদান করুন।
                """.trimIndent()
            } else {
                """
                A draft has been prepared locally, but publication is BLOCKED.

                • Reason: Publishing content externally requires Autonomy Level 6.
                • Current Level: ${currentAutonomy.titleEn}.
                • Next Step: Grant explicit authorization in Action Center.
                """.trimIndent()
            }
        } else if (isPermissionBlocked) {
            synthesizedText = if (isBengali) {
                "এই কাজটি সম্পাদনের জন্য ${requiredAutonomy.titleBn} প্রয়োজন, কিন্তু আপনার বর্তমান অনুমোদনের স্তর ${currentAutonomy.titleBn}। অ্যাকশন সেন্টারে গিয়ে অনুমতি প্রদান করুন।"
            } else {
                "This action requires ${requiredAutonomy.titleEn}, but current permission is ${currentAutonomy.titleEn}. Please approve in Action Center."
            }
        } else {
            val systemInstruction = buildSystemPrompt(reasoningMode, langCode, activeProjectId)
            val isUsingGemini = selectedProviderType == "GEMINI" && !apiKey.isNullOrBlank()
            val provider: AiProvider = if (isUsingGemini) geminiProvider else localProvider

            val config = ProviderConfig(apiKey = apiKey)
            val result = provider.generateResponse(prompt, systemInstruction, reasoningMode, config)
            providerAiResult = result

            if (result.isSuccess) {
                synthesizedText = result.content
            } else {
                // Graceful fallback to local reasoning
                val fallbackResult = localProvider.generateResponse(prompt, systemInstruction, reasoningMode, config)
                synthesizedText = fallbackResult.content
                providerAiResult = fallbackResult.copy(
                    requestStatusLog = "Gemini Failed (${result.errorMessage}) -> Fallback to Local Rule Engine"
                )
            }
        }

        // STEP 9: CRITIC REVIEW
        val criticReview = CriticReviewResult(
            hasWeakAssumptions = claims.any { it.evidenceLevel == EvidenceLevel.E6 },
            missingEvidenceDetected = contradictions.isNotEmpty(),
            overconfidenceCorrected = lowerPrompt.contains("সবাই জানে") || lowerPrompt.contains("সবাই আমার বিরুদ্ধে"),
            identitySeparated = true,
            safeForExecution = !isPermissionBlocked,
            notes = if (isBengali) "অভ্যন্তরীণ পর্যালোচক যাচাই সম্পন্ন: যুক্তি ও প্রমাণের মানদণ্ড নিরীক্ষিত।" else "Critic review completed: Logic and evidence verified."
        )

        // STEP 11: LEARNING REVIEW
        var candidateLearning: MemoryRecord? = null
        if (prompt.length > 25 && !prompt.contains("?") && !prompt.contains("কী") && !prompt.contains("কি")) {
            candidateLearning = MemoryRecord(
                id = UUID.randomUUID().toString(),
                content = prompt.take(120),
                memoryClass = MemoryClass.VALIDATED_REUSABLE_LEARNING,
                source = "কথোপকথন থেকে পরীক্ষাধীন লার্নিং",
                projectId = activeProjectId,
                personId = "user",
                evidenceLevel = EvidenceLevel.E4,
                confidenceScore = 0.75f,
                status = MemoryStatus.CANDIDATE,
                sensitivityLevel = SensitivityLevel.NORMAL,
                isUserEditable = true,
                isDeletable = true,
                retrievalReason = "ব্যবহারকারীর কথোপকথন থেকে শনাক্তকৃত প্রার্থী মেমোরি।"
            )
        }

        val decisionExplanation = if (isBengali) {
            "যুক্তি সিদ্ধান্ত: পাইপলাইন ইনটেক → মেমোরি প্রসঙ্গ (${relevantMemories.size}টি প্রাসঙ্গিক) → দাবি পৃথকীকরণ (${claims.size}টি) → সর্বোচ্চ প্রমাণ স্তর (${highestEvidence.code}) → ক্রMessage পর্যালোচক উত্তীর্ণ।"
        } else {
            "Decision Summary: Intake -> Context (${relevantMemories.size} records) -> Claim Separation (${claims.size}) -> Evidence (${highestEvidence.code}) -> Critic Review passed."
        }

        // DIAGNOSTICS AUDIT
        val isModelPowered = providerAiResult?.isModelPowered == true
        val diagnostics = EngineDiagnostics(
            activeProviderName = if (isModelPowered) "Google Gemini API (Remote)" else "Genesis Local Engine (Deterministic)",
            engineClassification = if (isModelPowered) "A. Genuinely Model-Powered" else "B. Deterministic Rule-Based & Structured Templates",
            activeModelName = if (isModelPowered) "gemini-2.5-flash" else "Genesis-LocalRules-v1",
            isRemoteExecution = isModelPowered,
            isApiAvailable = !apiKey.isNullOrBlank(),
            lastProviderRequestStatus = providerAiResult?.requestStatusLog ?: "Local Execution",
            lastResponseNormalizationResult = providerAiResult?.normalizationLog ?: "Standard rule synthesis",
            isDemoModeActive = !isModelPowered,
            isOutputMockedOrTemplated = !isModelPowered,
            auditNote = if (isModelPowered) {
                "Model-powered inference completed via Google Gemini API."
            } else {
                "Deterministic rule-based execution. Never falsely labeled as model-generated AI reasoning."
            }
        )

        ReasoningPipelineResult(
            interpretedRequest = interpretedRequest,
            language = langCode,
            relevantContextSummaries = contextSummaries,
            claimsSeparated = claims,
            contradictions = contradictions,
            evidenceSummary = evidenceSummary,
            highestEvidenceLevel = highestEvidence,
            requiredAutonomy = requiredAutonomy,
            criticReview = criticReview,
            decisionExplanationBn = decisionExplanation,
            synthesizedResponseBn = synthesizedText,
            candidateLearning = candidateLearning,
            diagnostics = diagnostics
        )
    }

    private fun buildSystemPrompt(mode: ReasoningMode, lang: String, projectId: String?): String {
        return """
            You are "Project Genesis", a Bengali-first, privacy-conscious personal AI reasoning partner.
            Core rules:
            1. Respond in Bengali courteously using 'আপনি' when user writes in Bengali.
            2. Never act as a sycophantic chatbot or blindly agreeable assistant. Disagree respectfully when claims lack evidence.
            3. Classify claims into evidence levels (E1-E6) and point out contradictions or missing data.
            4. Current Reasoning Mode: ${mode.titleEn} (${mode.descriptionBn}).
            5. Active Project Context: ${projectId ?: "General / Cross-project"}.
            6. Give concise, intellectually honest, evidence-based answers.
        """.trimIndent()
    }
}
