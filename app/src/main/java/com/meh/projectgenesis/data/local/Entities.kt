package com.meh.projectgenesis.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val content: String,
    val memoryClass: String,
    val source: String,
    val projectId: String?,
    val personId: String?,
    val evidenceLevel: String,
    val confidenceScore: Float,
    val createdAt: Long,
    val lastUsedAt: Long,
    val lastVerifiedAt: Long,
    val status: String,
    val sensitivityLevel: String,
    val isUserEditable: Boolean,
    val isDeletable: Boolean,
    val supersededById: String?,
    val retrievalReason: String?
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val goal: String,
    val role: String,
    val instructions: String,
    val baseline: String,
    val currentStatus: String,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val projectId: String?,
    val priority: Int,
    val deadline: Long?,
    val status: String,
    val subtasksJson: String,
    val dependencies: String,
    val completionEvidence: String,
    val reviewResult: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val sender: String, // "user" or "genesis"
    val content: String,
    val language: String,
    val evidenceSummary: String?,
    val highestEvidenceLevel: String?,
    val reasoningSummary: String?,
    val memoryUsedCount: Int,
    val timestamp: Long,
    val userFeedback: String? = null
)

@Entity(tableName = "action_proposals")
data class ActionProposalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val purpose: String,
    val requiredPermission: Int,
    val affectedData: String,
    val expectedResult: String,
    val risk: String,
    val isReversible: Boolean,
    val status: String,
    val createdAt: Long
)
