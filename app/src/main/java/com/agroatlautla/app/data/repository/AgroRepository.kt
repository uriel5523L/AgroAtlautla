package com.agroatlautla.app.data.repository

import android.content.Context
import com.agroatlautla.app.data.local.AgroDatabase
import com.agroatlautla.app.data.local.CalendarActivityEntity
import com.agroatlautla.app.data.local.CropEntity
import com.agroatlautla.app.data.local.ExpenseEntity
import com.agroatlautla.app.data.local.PestEntity
import com.agroatlautla.app.data.local.SeedData
import com.agroatlautla.app.data.local.SyncQueueEntity
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class AgroRepository(
    context: Context,
    private val database: AgroDatabase
) {
    private val cloudRepository = CloudRepository(context)
    private val prefs = context.getSharedPreferences("agroatlautla_local", Context.MODE_PRIVATE)

    val crops: Flow<List<CropEntity>> = database.cropDao().observeAll()
    val activities: Flow<List<CalendarActivityEntity>> = database.calendarActivityDao().observeAll()
    val pests: Flow<List<PestEntity>> = database.pestDao().observeAll()
    val expenses: Flow<List<ExpenseEntity>> = database.expenseDao().observeAll()
    val pendingSync: Flow<List<SyncQueueEntity>> = database.syncQueueDao().observePending()

    suspend fun seed() {
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        SeedData.ensureInitialData(database)
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    suspend fun addCrop(
        name: String,
        sowDate: String = "Sin fecha de siembra",
        irrigationType: String = "Temporal (lluvia)",
        notes: String = "",
        surfaceArea: String = ""
    ) {
        val cleanedArea = surfaceArea.trim().ifBlank { "Sin superficie asignada" }
            .let { if (it.endsWith("ha")) it else "$it ha" }
        val crop = CropEntity(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Nuevo cultivo" },
            stage = "Registrado",
            sowDate = sowDate.ifBlank { "Sin fecha de siembra" },
            surfaceArea = cleanedArea,
            irrigationType = irrigationType.ifBlank { "Temporal (lluvia)" },
            notes = notes,
            nextActivity = "Pendiente de revision",
            icon = "leaf"
        )
        database.cropDao().insert(crop)
        database.syncQueueDao().insert(
            SyncQueueEntity(entityName = "crops", entityId = crop.id, action = "create")
        )
    }

    suspend fun addActivity(
        title: String,
        day: Int,
        month: String,
        type: String = "Actividad",
        cropName: String = "General"
    ) {
        val colorTag = when (type) {
            "Riego" -> "blue"
            "Fertilizacion" -> "orange"
            else -> "green"
        }
        val activity = CalendarActivityEntity(
            id = UUID.randomUUID().toString(),
            day = day.coerceIn(1, 31),
            month = month.ifBlank { "JUL" },
            type = type,
            title = title.ifBlank { "Nueva actividad" },
            cropName = cropName.ifBlank { "General" },
            colorTag = colorTag
        )
        database.calendarActivityDao().insert(activity)
        database.syncQueueDao().insert(
            SyncQueueEntity(entityName = "calendar_activities", entityId = activity.id, action = "create")
        )
    }

    suspend fun addExpense(concept: String, amount: Int, date: String, category: String) {
        val expense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            concept = concept.ifBlank { "Gasto sin concepto" },
            amount = amount,
            date = date.ifBlank { "Sin fecha" },
            category = category.ifBlank { "Semillas" }
        )
        database.expenseDao().insert(expense)
        database.syncQueueDao().insert(
            SyncQueueEntity(entityName = "expenses", entityId = expense.id, action = "create")
        )
    }

    suspend fun refreshFromCloud(uid: String): Result<Unit> = runCatching {
        if (!cloudRepository.isConfigured()) error(FirebaseConfig.MissingConfigMessage)

        cloudRepository.downloadCrops(uid).forEach { cloud ->
            if (database.syncQueueDao().hasPendingDelete("crops", cloud.id) > 0) return@forEach
            val local = database.cropDao().getById(cloud.id)
            when {
                local == null -> {
                    val sameName = database.cropDao().getByName(cloud.name)
                    if (sameName == null || sameName.needsSync) {
                        database.cropDao().insert(cloud)
                    } else {
                        database.cropDao().update(sameName.copy(
                            stage = cloud.stage,
                            sowDate = cloud.sowDate,
                            surfaceArea = cloud.surfaceArea,
                            irrigationType = cloud.irrigationType,
                            notes = cloud.notes,
                            nextActivity = cloud.nextActivity,
                            icon = cloud.icon,
                            updatedAt = cloud.updatedAt,
                            needsSync = false
                        ))
                    }
                }
                !local.needsSync -> database.cropDao().insert(cloud)
            }
        }
        cloudRepository.downloadActivities(uid).forEach { cloud ->
            if (database.syncQueueDao().hasPendingDelete("calendar_activities", cloud.id) > 0) return@forEach
            val local = database.calendarActivityDao().getById(cloud.id)
            when {
                local == null -> {
                    val sameTitle = database.calendarActivityDao().getByTitle(cloud.title)
                    if (sameTitle == null || sameTitle.needsSync) {
                        database.calendarActivityDao().insert(cloud)
                    } else {
                        database.calendarActivityDao().update(sameTitle.copy(
                            day = cloud.day,
                            month = cloud.month,
                            type = cloud.type,
                            cropName = cloud.cropName,
                            colorTag = cloud.colorTag,
                            needsSync = false
                        ))
                    }
                }
                !local.needsSync -> database.calendarActivityDao().insert(cloud)
            }
        }
        cloudRepository.downloadPests(uid).forEach { cloud ->
            if (database.syncQueueDao().hasPendingDelete("pests", cloud.id) > 0) return@forEach
            val existing = database.pestDao().getByName(cloud.name)
            if (existing == null) database.pestDao().insert(cloud)
        }
        cloudRepository.downloadExpenses(uid).forEach { cloud ->
            if (database.syncQueueDao().hasPendingDelete("expenses", cloud.id) > 0) return@forEach
            val local = database.expenseDao().getById(cloud.id)
            when {
                local == null -> {
                    val sameConcept = database.expenseDao().getByConcept(cloud.concept)
                    if (sameConcept == null || sameConcept.needsSync) {
                        database.expenseDao().insert(cloud)
                    } else {
                        database.expenseDao().update(sameConcept.copy(
                            amount = cloud.amount,
                            date = cloud.date,
                            category = cloud.category,
                            needsSync = false
                        ))
                    }
                }
                !local.needsSync -> database.expenseDao().insert(cloud)
            }
        }
    }

    suspend fun syncPendingToCloud(uid: String?): Result<Int> = runCatching {
        if (uid.isNullOrBlank()) error("Inicia sesion para sincronizar.")
        if (!cloudRepository.isConfigured()) error(FirebaseConfig.MissingConfigMessage)

        val pending = database.syncQueueDao().getPending()
        pending.forEach { item ->
            when (item.entityName) {
                "crops" -> when (item.action) {
                    "delete" -> cloudRepository.deleteCrop(uid, item.entityId)
                    else -> database.cropDao().getById(item.entityId)?.let {
                        cloudRepository.uploadCrop(uid, it)
                    }
                }
                "calendar_activities" -> when (item.action) {
                    "delete" -> cloudRepository.deleteActivity(uid, item.entityId)
                    else -> database.calendarActivityDao().getById(item.entityId)?.let {
                        cloudRepository.uploadActivity(uid, it)
                    }
                }
                "pests" -> when (item.action) {
                    "delete" -> cloudRepository.deletePest(uid, item.entityId)
                    else -> database.pestDao().getById(item.entityId)?.let {
                        cloudRepository.uploadPest(uid, it)
                    }
                }
                "expenses" -> when (item.action) {
                    "delete" -> cloudRepository.deleteExpense(uid, item.entityId)
                    else -> database.expenseDao().getById(item.entityId)?.let {
                        cloudRepository.uploadExpense(uid, it)
                    }
                }
            }
            database.syncQueueDao().markSynced(item.id, System.currentTimeMillis())
        }
        pending.size
    }

    suspend fun deleteCrop(cropId: String) {
        val crop = database.cropDao().getById(cropId) ?: return
        if (!crop.needsSync) {
            database.syncQueueDao().insert(
                SyncQueueEntity(entityName = "crops", entityId = cropId, action = "delete")
            )
        }
        database.cropDao().deleteById(cropId)
    }

    suspend fun updateCrop(
        cropId: String,
        name: String,
        sowDate: String,
        surfaceArea: String,
        irrigationType: String,
        notes: String
    ) {
        val current = database.cropDao().getById(cropId) ?: return
        val cleanedArea = surfaceArea.trim().ifBlank { "Sin superficie asignada" }
            .let { if (it.endsWith("ha")) it else "$it ha" }
        if (!current.needsSync) {
            database.syncQueueDao().insert(
                SyncQueueEntity(entityName = "crops", entityId = cropId, action = "create")
            )
        }
        database.cropDao().update(
            current.copy(
                name = name.ifBlank { current.name },
                sowDate = sowDate.ifBlank { current.sowDate },
                surfaceArea = cleanedArea,
                irrigationType = irrigationType.ifBlank { current.irrigationType },
                notes = notes,
                updatedAt = System.currentTimeMillis(),
                needsSync = true
            )
        )
    }

    suspend fun deleteActivity(activityId: String) {
        val activity = database.calendarActivityDao().getById(activityId) ?: return
        if (!activity.needsSync) {
            database.syncQueueDao().insert(
                SyncQueueEntity(entityName = "calendar_activities", entityId = activityId, action = "delete")
            )
        }
        database.calendarActivityDao().deleteById(activityId)
    }

    suspend fun deleteExpense(expenseId: String) {
        val expense = database.expenseDao().getById(expenseId) ?: return
        if (!expense.needsSync) {
            database.syncQueueDao().insert(
                SyncQueueEntity(entityName = "expenses", entityId = expenseId, action = "delete")
            )
        }
        database.expenseDao().deleteById(expenseId)
    }

    suspend fun deletePest(pestId: String) {
        if (database.pestDao().getById(pestId) == null) return
        database.syncQueueDao().insert(
            SyncQueueEntity(entityName = "pests", entityId = pestId, action = "delete")
        )
        database.pestDao().deleteById(pestId)
    }

    companion object {
        private const val KEY_SEEDED = "demo_seeded"
    }
}
