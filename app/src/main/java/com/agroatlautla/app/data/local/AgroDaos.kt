package com.agroatlautla.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UserEntity?
}

@Dao
interface CropDao {
    @Query("SELECT * FROM crops ORDER BY rowid ASC")
    fun observeAll(): Flow<List<CropEntity>>

    @Query("SELECT COUNT(*) FROM crops")
    suspend fun count(): Int

    @Query("SELECT * FROM crops WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): CropEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crop: CropEntity): Long

    @Update
    suspend fun update(crop: CropEntity)

    @Query("SELECT * FROM crops WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CropEntity?

    @Query("DELETE FROM crops WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface CalendarActivityDao {
    @Query("SELECT * FROM calendar_activities ORDER BY rowid ASC")
    fun observeAll(): Flow<List<CalendarActivityEntity>>

    @Query("SELECT COUNT(*) FROM calendar_activities")
    suspend fun count(): Int

    @Query("SELECT * FROM calendar_activities WHERE title = :title LIMIT 1")
    suspend fun getByTitle(title: String): CalendarActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: CalendarActivityEntity): Long

    @Update
    suspend fun update(activity: CalendarActivityEntity)

    @Query("SELECT * FROM calendar_activities WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CalendarActivityEntity?
}

@Dao
interface PestDao {
    @Query("SELECT * FROM pests ORDER BY rowid ASC")
    fun observeAll(): Flow<List<PestEntity>>

    @Query("SELECT COUNT(*) FROM pests")
    suspend fun count(): Int

    @Query("SELECT * FROM pests WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): PestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pest: PestEntity): Long

    @Update
    suspend fun update(pest: PestEntity)

    @Query("SELECT * FROM pests WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PestEntity?
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY rowid ASC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun count(): Int

    @Query("SELECT * FROM expenses WHERE concept = :concept LIMIT 1")
    suspend fun getByConcept(concept: String): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExpenseEntity?
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'pending' ORDER BY createdAt ASC")
    fun observePending(): Flow<List<SyncQueueEntity>>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'pending'")
    suspend fun pendingCount(): Int

    @Query("SELECT * FROM sync_queue WHERE status = 'pending' ORDER BY createdAt ASC")
    suspend fun getPending(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = 'synced', syncedAt = :syncedAt WHERE status = 'pending'")
    suspend fun markAllSynced(syncedAt: Long)

    @Query("UPDATE sync_queue SET status = 'synced', syncedAt = :syncedAt WHERE id = :id")
    suspend fun markSynced(id: Int, syncedAt: Long)

    @Query("SELECT COUNT(*) FROM sync_queue WHERE entityName = :name AND entityId = :id AND action = 'delete' AND status = 'pending'")
    suspend fun hasPendingDelete(name: String, id: String): Int
}
