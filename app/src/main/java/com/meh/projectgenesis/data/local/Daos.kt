package com.meh.projectgenesis.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY createdAt DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE status = 'CANDIDATE' ORDER BY createdAt DESC")
    fun getCandidateMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE status = 'ACTIVE' OR status = 'VERIFIED' ORDER BY lastUsedAt DESC")
    fun getActiveMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE (status = 'ACTIVE' OR status = 'VERIFIED')")
    suspend fun getActiveMemoriesList(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: String): MemoryEntity?

    @Query("SELECT * FROM memories WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getMemoriesByProject(projectId: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE memoryClass = :className ORDER BY createdAt DESC")
    fun getMemoriesByClass(className: String): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: MemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memories: List<MemoryEntity>)

    @Update
    suspend fun update(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM memories")
    suspend fun deleteAll()
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getActiveProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE isArchived = 0")
    suspend fun getActiveProjectsList(): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<ProjectEntity>)

    @Update
    suspend fun update(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM projects")
    suspend fun deleteAll()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY priority ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY priority ASC, createdAt DESC")
    fun getTasksByProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' AND status != 'CLOSED' AND status != 'ARCHIVED' ORDER BY priority ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Update
    suspend fun update(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()
}

@Dao
interface ActionProposalDao {
    @Query("SELECT * FROM action_proposals ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ActionProposalEntity>>

    @Query("SELECT * FROM action_proposals WHERE status = 'PENDING_APPROVAL' ORDER BY createdAt DESC")
    fun getPending(): Flow<List<ActionProposalEntity>>

    @Query("SELECT * FROM action_proposals WHERE id = :id")
    suspend fun getById(id: String): ActionProposalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: ActionProposalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(actions: List<ActionProposalEntity>)

    @Update
    suspend fun update(action: ActionProposalEntity)

    @Query("DELETE FROM action_proposals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM action_proposals")
    suspend fun deleteAll()
}
