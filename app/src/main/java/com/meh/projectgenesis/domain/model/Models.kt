package com.meh.projectgenesis.domain.model

import java.util.UUID

/**
 * 9 Required Memory Classes
 */
enum class MemoryClass(val displayNameBn: String, val displayNameEn: String) {
    PERMANENT_PREFERENCES("স্থায়ী পছন্দসমূহ", "Permanent Preferences"),
    IDENTITY_AND_PROFILE("পরিচয় ও প্রোফাইল", "Identity and Profile"),
    ACTIVE_PROJECTS("সক্রিয় প্রজেক্টসমূহ", "Active Projects"),
    PROJECT_BASELINES("প্রজেক্ট বেসলাইন", "Project Baselines"),
    ACTIVE_TASKS("সক্রিয় কাজসমূহ", "Active Tasks"),
    VALIDATED_REUSABLE_LEARNING("যাচাইকৃত পুনঃব্যবহারযোগ্য শিক্ষা", "Validated Reusable Learning"),
    TEMPORARY_CONTEXT("অস্থায়ী প্রসঙ্গ", "Temporary Context"),
    HISTORICAL_ARCHIVE("ঐতিহাসিক সংরক্ষণ", "Historical Archive"),
    REJECTED_OR_SUPERSEDED("বাতিল বা প্রতিস্থাপিত তথ্য", "Rejected or Superseded Information")
}

/**
 * 6 Evidence Levels: E1 - E6
 */
enum class EvidenceLevel(val code: String, val titleBn: String, val titleEn: String) {
    E1("E1", "প্রাথমিক যাচাইকৃত প্রমাণ", "Primary verified evidence"),
    E2("E2", "নির্ভরযোগ্য স্বাধীন প্রমাণ", "Reliable independent evidence"),
    E3("E3", "ব্যবহারকারীর প্রদত্ত রেকর্ড", "User-provided record"),
    E4("E4", "ব্যবহারকারীর ব্যক্তিগত অভিজ্ঞতা", "User-reported experience"),
    E5("E5", "জেনেসিস বিশ্লেষণ", "Genesis analysis"),
    E6("E6", "অনুমান বা সম্ভাবনা", "Assumption or possibility")
}

enum class SensitivityLevel {
    PUBLIC, NORMAL, SENSITIVE, RESTRICTED
}

enum class MemoryStatus {
    ACTIVE, CANDIDATE, VERIFIED, INCORRECT, SUPERSEDED, ARCHIVED
}

data class MemoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val memoryClass: MemoryClass,
    val source: String,
    val projectId: String? = null,
    val personId: String? = "user",
    val evidenceLevel: EvidenceLevel = EvidenceLevel.E3,
    val confidenceScore: Float = 0.85f,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis(),
    val lastVerifiedAt: Long = System.currentTimeMillis(),
    val status: MemoryStatus = MemoryStatus.ACTIVE,
    val sensitivityLevel: SensitivityLevel = SensitivityLevel.NORMAL,
    val isUserEditable: Boolean = true,
    val isDeletable: Boolean = true,
    val supersededById: String? = null,
    val retrievalReason: String? = null
)

/**
 * 8 Selectable Reasoning Modes
 */
enum class ReasoningMode(val titleBn: String, val titleEn: String, val descriptionBn: String) {
    BALANCED(
        "ভারসাম্যপূর্ণ",
        "Balanced",
        "প্রমাণ ও বাস্তব প্রয়োগের সমন্বয়ে ভারসাম্যপূর্ণ সিদ্ধান্ত প্রদান করে।"
    ),
    EVIDENCE_FIRST(
        "প্রমাণ-প্রথম",
        "Evidence First",
        "কঠোর প্রমাণ (E1-E3) যাচাই ছাড়া কোনো দাবি বা সিদ্ধান্ত গ্রহণ করে না।"
    ),
    CRITICAL(
        "সমালোচনামূলক",
        "Critical",
        "যুক্তিদোষ, দুর্বল অনুমান ও অস্পষ্টতা সরাসরি ও শ্রদ্ধার সাথে তুলে ধরে।"
    ),
    CREATIVE(
        "সৃজনশীল",
        "Creative",
        "সীমাবদ্ধতার মধ্যে নতুন দৃষ্টিভঙ্গি ও উদ্ভাবনী ধারণার অন্বেষণ করে।"
    ),
    PLANNING(
        "পরিকল্পনা",
        "Planning",
        "সুনির্দিষ্ট মাইলফলক, নির্ভরতা ও ঝুঁকি বিশ্লেষণসহ বিস্তারিত কর্মপরিকল্পনা তৈরি করে।"
    ),
    EDITORIAL(
        "সম্পাদনা",
        "Editorial",
        "ভাষাগত সৌন্দর্য, প্রাঞ্জলতা ও ধারণার গভীরতা নিখুঁতভাবে পরিমার্জন করে।"
    ),
    TECHNICAL(
        "প্রযুক্তিগত",
        "Technical",
        "আর্কিটেকচার, কার্যকারিতা ও সুনির্দিষ্ট স্পেসিফিকেশন অগ্রাধিকার দেয়।"
    ),
    PRIVATE_REFLECTION(
        "ব্যক্তিগত পর্যবেক্ষণ",
        "Private Reflection",
        "চিন্তা, আত্মপর্যালোচনা ও দীর্ঘমেয়াদী লক্ষ্যের শান্ত বিশ্লেষণ।"
    )
}

/**
 * 7 Autonomy / Permission Levels
 */
enum class AutonomyLevel(val level: Int, val titleBn: String, val titleEn: String, val descriptionBn: String) {
    LEVEL_0(0, "স্তর ০: শুধু কথোপকথন", "Level 0: Conversation only", "কোনো লোকাল তথ্য পড়া বা অ্যাকশন সম্পাদনের অনুমতি নেই।"),
    LEVEL_1(1, "স্তর ১: অনুমোদিত ডেটা পাঠ", "Level 1: Read approved local data", "ব্যবহারকারীর অনুমোদিত লোকাল মেমোরি ও প্রজেক্ট তথ্য পড়ার অনুমতি।"),
    LEVEL_2(2, "স্তর ২: খসড়া ও সুপারিশ", "Level 2: Drafts & Recommendations", "পরিকল্পনা, বার্তা বা কোডের খসড়া তৈরি করার অনুমতি।"),
    LEVEL_3(3, "স্তর ৩: লোকাল ফাইল তৈরি", "Level 3: Create local files", "ডিভাইসে অনুমোদিত ফাইল ও এক্সপোর্ট তৈরি করার অনুমতি।"),
    LEVEL_4(4, "স্তর ৪: পরিবর্তনযোগ্য লোকাল কাজ", "Level 4: Reversible local actions", "বাতিলযোগ্য স্থানীয় পরিবর্তন করার অনুমতি।"),
    LEVEL_5(5, "স্তর ৫: বাহ্যিক কাজের প্রস্তুতি", "Level 5: Prepare external actions", "পর্যালোচনার জন্য বাহ্যিক পদক্ষেপের খসড়া তৈরি।"),
    LEVEL_6(6, "স্তর ৬: অনুমোদিত বাহ্যিক সম্পাদন", "Level 6: Execute approved actions", "ব্যবহারকারীর প্রত্যক্ষ সম্মতিতে নির্দিষ্ট বাহ্যিক কাজ সম্পন্ন করা।")
}

/**
 * 14 Distinct Task Statuses
 */
enum class TaskStatus(val titleBn: String, val titleEn: String) {
    IDEA("আইডিয়া", "Idea"),
    CONCEPT("ধারণা", "Concept"),
    PLAN("পরিকল্পনা", "Plan"),
    RESEARCH("গবেষণা", "Research"),
    DRAFT("খসড়া", "Draft"),
    IMPLEMENTATION("বাস্তবায়ন", "Implementation"),
    TESTING("পরীক্ষণ", "Testing"),
    APPROVED("অনুমোদিত", "Approved"),
    COMPLETED("সম্পন্ন", "Completed"),
    PUBLISHED("প্রকাশিত", "Published"),
    OPERATIONAL("কার্যকরী", "Operational"),
    PAUSED("স্থগিত", "Paused"),
    CLOSED("বন্ধ", "Closed"),
    ARCHIVED("আর্কাইভ", "Archived")
}

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val goal: String,
    val role: String,
    val instructions: String,
    val baseline: String,
    val currentStatus: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val projectId: String? = null,
    val priority: Int = 2, // 1: High, 2: Medium, 3: Low
    val deadline: Long? = null,
    val status: TaskStatus = TaskStatus.PLAN,
    val subtasksJson: String = "[]",
    val dependencies: String = "",
    val completionEvidence: String = "",
    val reviewResult: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ActionStatus {
    PENDING_APPROVAL, APPROVED, REJECTED, EXECUTED, ROLLED_BACK
}

data class ActionProposal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val purpose: String,
    val requiredPermission: AutonomyLevel,
    val affectedData: String,
    val expectedResult: String,
    val risk: String,
    val isReversible: Boolean,
    val status: ActionStatus = ActionStatus.PENDING_APPROVAL,
    val createdAt: Long = System.currentTimeMillis()
)

data class ClaimItem(
    val type: String, // Fact, UserRecord, Experience, Opinion, Allegation, Analysis, Inference, Assumption, Plan, CompletedAction
    val text: String,
    val evidenceLevel: EvidenceLevel
)

data class ContradictionItem(
    val issue: String,
    val conflictingMemory: String?,
    val severity: String // Warning, Critical, Note
)

data class CriticReviewResult(
    val hasWeakAssumptions: Boolean = false,
    val missingEvidenceDetected: Boolean = false,
    val overconfidenceCorrected: Boolean = false,
    val identitySeparated: Boolean = true,
    val safeForExecution: Boolean = true,
    val notes: String = ""
)

data class ReasoningPipelineResult(
    val interpretedRequest: String,
    val language: String, // "bn" or "en"
    val relevantContextSummaries: List<String>,
    val claimsSeparated: List<ClaimItem>,
    val contradictions: List<ContradictionItem>,
    val evidenceSummary: String,
    val highestEvidenceLevel: EvidenceLevel,
    val requiredAutonomy: AutonomyLevel,
    val criticReview: CriticReviewResult,
    val decisionExplanationBn: String,
    val synthesizedResponseBn: String,
    val candidateLearning: MemoryRecord? = null,
    val diagnostics: com.meh.projectgenesis.core.ai.EngineDiagnostics? = null
)
