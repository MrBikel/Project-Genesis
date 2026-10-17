package com.meh.projectgenesis.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.meh.projectgenesis.data.local.ActionProposalDao
import com.meh.projectgenesis.data.local.ActionProposalEntity
import com.meh.projectgenesis.data.local.ChatMessageDao
import com.meh.projectgenesis.data.local.ChatMessageEntity
import com.meh.projectgenesis.data.local.MemoryDao
import com.meh.projectgenesis.data.local.MemoryEntity
import com.meh.projectgenesis.data.local.ProjectDao
import com.meh.projectgenesis.data.local.ProjectEntity
import com.meh.projectgenesis.data.local.TaskDao
import com.meh.projectgenesis.data.local.TaskEntity
import com.meh.projectgenesis.domain.model.EvidenceLevel
import com.meh.projectgenesis.domain.model.MemoryClass
import com.meh.projectgenesis.domain.model.MemoryStatus
import com.meh.projectgenesis.domain.model.SensitivityLevel
import com.meh.projectgenesis.domain.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        MemoryEntity::class,
        ProjectEntity::class,
        TaskEntity::class,
        ChatMessageEntity::class,
        ActionProposalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GenesisDatabase : RoomDatabase() {

    abstract fun memoryDao(): MemoryDao
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun actionProposalDao(): ActionProposalDao

    companion object {
        @Volatile
        private var INSTANCE: GenesisDatabase? = null

        fun getInstance(context: Context): GenesisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GenesisDatabase::class.java,
                    "genesis_master.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial Genesis starter environment
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { prepopulate(it) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun prepopulate(database: GenesisDatabase) {
            val now = System.currentTimeMillis()
            val sampleProjectId = "proj_genesis_core"

            // 1. Initial Project
            val defaultProject = ProjectEntity(
                id = sampleProjectId,
                name = "প্রজেক্ট জেনেসিস রিসার্চ",
                description = "বাংলায় প্রথম বুদ্ধিবৃত্তিক ও স্মৃতি-সমৃদ্ধ ব্যক্তিগত এআই সহকারী গড়ে তোলার প্রাথমিক প্রজেক্ট।",
                goal = "ব্যক্তিগত গোপনীয়তা রক্ষা করে দীর্ঘমেয়াদী স্মৃতি ও স্বাধীন যুক্তিবাদী বিশ্লেষণ প্রতিষ্ঠা করা।",
                role = "বুদ্ধিবৃত্তিক সহযোগী ও যুক্তি সমালোচক",
                instructions = "কোনো অন্ধ সম্মতি নয়; প্রমাণের স্তর (E1-E6) বিশ্লেষণ করে যুক্তিসঙ্গত সিদ্ধান্ত প্রদান করতে হবে।",
                baseline = "সংস্করণ ১.০ বেসলাইন: স্থানীয় অফলাইন মেমোরি, ডাটাবেস এনক্রিপশন ও বাঙালি প্রথম ইন্টারঅ্যাকশন।",
                currentStatus = "সক্রিয় বাস্তবায়ন",
                isArchived = false,
                createdAt = now,
                updatedAt = now
            )
            database.projectDao().insert(defaultProject)

            // 2. Initial Sample Memories across classes
            val initialMemories = listOf(
                MemoryEntity(
                    id = "mem_pref_lang",
                    content = "ব্যবহারকারী বাংলায় কথোপকথনে 'আপনি' সম্বোধন ও প্রাঞ্জল শব্দচয়ন পছন্দ করেন।",
                    memoryClass = MemoryClass.PERMANENT_PREFERENCES.name,
                    source = "Onboarding Selection",
                    projectId = null,
                    personId = "user",
                    evidenceLevel = EvidenceLevel.E3.code,
                    confidenceScore = 1.0f,
                    createdAt = now,
                    lastUsedAt = now,
                    lastVerifiedAt = now,
                    status = MemoryStatus.VERIFIED.name,
                    sensitivityLevel = SensitivityLevel.NORMAL.name,
                    isUserEditable = true,
                    isDeletable = false,
                    supersededById = null,
                    retrievalReason = "বাংলা ভাষা ও সম্বোধনের স্থায়ী অগ্রাধিকার বজায় রাখতে ব্যবহৃত।"
                ),
                MemoryEntity(
                    id = "mem_prof_identity",
                    content = "ব্যবহারকারী প্রজেক্ট জেনেসিসের প্রতিষ্ঠাতা ও নীতি-নির্ধারক।",
                    memoryClass = MemoryClass.IDENTITY_AND_PROFILE.name,
                    source = "User Profile",
                    projectId = null,
                    personId = "user",
                    evidenceLevel = EvidenceLevel.E3.code,
                    confidenceScore = 0.95f,
                    createdAt = now,
                    lastUsedAt = now,
                    lastVerifiedAt = now,
                    status = MemoryStatus.ACTIVE.name,
                    sensitivityLevel = SensitivityLevel.NORMAL.name,
                    isUserEditable = true,
                    isDeletable = true,
                    supersededById = null,
                    retrievalReason = "ভূমিকা ও সহযোগিতার প্রেক্ষাপট।"
                ),
                MemoryEntity(
                    id = "mem_proj_base",
                    content = "প্রজেক্ট জেনেসিসে কোনো অন্ধ সম্মতি গ্রহণযোগ্য নয়; প্রতিটি যুক্তিতে প্রমাণের ভিত্তি স্পষ্ট থাকতে হবে।",
                    memoryClass = MemoryClass.PROJECT_BASELINES.name,
                    source = "Genesis Core Vision",
                    projectId = sampleProjectId,
                    personId = "user",
                    evidenceLevel = EvidenceLevel.E1.code,
                    confidenceScore = 1.0f,
                    createdAt = now,
                    lastUsedAt = now,
                    lastVerifiedAt = now,
                    status = MemoryStatus.ACTIVE.name,
                    sensitivityLevel = SensitivityLevel.NORMAL.name,
                    isUserEditable = true,
                    isDeletable = false,
                    supersededById = null,
                    retrievalReason = "যুক্তি বিশ্লেষণ পাইপলাইনের মূল নীতি।"
                ),
                MemoryEntity(
                    id = "mem_learn_candidate",
                    content = "স্থানীয় মেমোরি ব্যাকআপে যেন কখনোই এপিআই কি অথবা ডায়াগনস্টিক সিক্রেট অন্তর্ভুক্ত না হয়।",
                    memoryClass = MemoryClass.VALIDATED_REUSABLE_LEARNING.name,
                    source = "Security Architecture Directive",
                    projectId = sampleProjectId,
                    personId = "user",
                    evidenceLevel = EvidenceLevel.E2.code,
                    confidenceScore = 0.99f,
                    createdAt = now,
                    lastUsedAt = now,
                    lastVerifiedAt = now,
                    status = MemoryStatus.CANDIDATE.name,
                    sensitivityLevel = SensitivityLevel.SENSITIVE.name,
                    isUserEditable = true,
                    isDeletable = true,
                    supersededById = null,
                    retrievalReason = "লার্নিং রিভিউতে অনুমোদনের অপেক্ষায়।"
                )
            )
            database.memoryDao().insertAll(initialMemories)

            // 3. Initial Task with distinct status
            val initialTask = TaskEntity(
                id = "task_01",
                title = "মেমোরি কন্ট্রোল সেন্টার অডিট ও ব্যাকআপ পরীক্ষা",
                description = "মেমোরি ফিল্টারিং, অনুমোদন ও ব্যাকআপ এক্সপোর্ট ফাংশনালিটি যাচাই করা।",
                projectId = sampleProjectId,
                priority = 1,
                deadline = now + 86400000L * 3, // 3 days
                status = TaskStatus.IMPLEMENTATION.name,
                subtasksJson = "[{\"id\":\"sub_1\",\"title\":\"রুম ডাটাবেস যাচাই\",\"done\":true},{\"id\":\"sub_2\",\"title\":\"ব্যাকআপ এক্সপোর্ট ও ইমপোর্ট টেস্ট\",\"done\":false}]",
                dependencies = "Room Database Schema v1",
                completionEvidence = "লোকাল ইউনিট টেস্ট সম্পন্ন",
                reviewResult = "বাস্তবায়ন চলমান",
                createdAt = now,
                updatedAt = now
            )
            database.taskDao().insert(initialTask)
        }
    }
}
