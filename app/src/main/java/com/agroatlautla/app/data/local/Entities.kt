package com.agroatlautla.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val location: String = "Atlautla, Estado de Mexico",
    val productionArea: String = "2018 - 4.8 ha",
    val pendingRecovery: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "crops")
data class CropEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val stage: String,
    val sowDate: String = "Sin fecha de siembra",
    val surfaceArea: String = "Sin superficie asignada",
    val irrigationType: String = "Temporal (lluvia)",
    val notes: String = "",
    val nextActivity: String = "Pendiente de revision",
    val icon: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val needsSync: Boolean = true
)

@Entity(tableName = "calendar_activities")
data class CalendarActivityEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val day: Int,
    val month: String,
    val type: String,
    val title: String,
    val cropName: String,
    val colorTag: String,
    val needsSync: Boolean = true
)

@Entity(tableName = "pests")
data class PestEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val affectedCrop: String,
    val severity: String,
    val description: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val concept: String,
    val amount: Int,
    val date: String,
    val category: String,
    val needsSync: Boolean = true
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entityName: String,
    val entityId: String,
    val action: String,
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
