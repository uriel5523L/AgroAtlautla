package com.agroatlautla.app.data.repository

import android.content.Context
import com.agroatlautla.app.data.local.CalendarActivityEntity
import com.agroatlautla.app.data.local.CropEntity
import com.agroatlautla.app.data.local.ExpenseEntity
import com.agroatlautla.app.data.local.PestEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CloudRepository(private val context: Context) {
    fun isConfigured(): Boolean = FirebaseConfig.ensureInitialized(context)

    suspend fun uploadCrop(uid: String, crop: CropEntity) {
        firestore(uid)
            .collection("crops")
            .document(crop.id)
            .set(crop.toCloudMap())
            .await()
    }

    suspend fun uploadActivity(uid: String, activity: CalendarActivityEntity) {
        firestore(uid)
            .collection("calendar_activities")
            .document(activity.id)
            .set(activity.toCloudMap())
            .await()
    }

    suspend fun uploadPest(uid: String, pest: PestEntity) {
        firestore(uid)
            .collection("pests")
            .document(pest.id)
            .set(pest.toCloudMap())
            .await()
    }

    suspend fun uploadExpense(uid: String, expense: ExpenseEntity) {
        firestore(uid)
            .collection("expenses")
            .document(expense.id)
            .set(expense.toCloudMap())
            .await()
    }

    suspend fun deleteCrop(uid: String, cropId: String) {
        firestore(uid)
            .collection("crops")
            .document(cropId)
            .delete()
            .await()
    }

    suspend fun deleteActivity(uid: String, activityId: String) {
        firestore(uid)
            .collection("calendar_activities")
            .document(activityId)
            .delete()
            .await()
    }

    suspend fun deletePest(uid: String, pestId: String) {
        firestore(uid)
            .collection("pests")
            .document(pestId)
            .delete()
            .await()
    }

    suspend fun deleteExpense(uid: String, expenseId: String) {
        firestore(uid)
            .collection("expenses")
            .document(expenseId)
            .delete()
            .await()
    }

    suspend fun downloadCrops(uid: String): List<CropEntity> {
        return firestore(uid).collection("crops").get().await().documents.mapNotNull { doc ->
            val id = doc.getString("id") ?: doc.id
            CropEntity(
                id = id,
                name = doc.getString("name") ?: return@mapNotNull null,
                stage = doc.getString("stage") ?: "Registrado",
                sowDate = doc.getString("sowDate")
                    ?: doc.getString("areaLabel")?.substringBefore(" - ") ?: "Sin fecha de siembra",
                surfaceArea = doc.getString("surfaceArea")
                    ?: doc.getString("areaLabel")?.substringAfter(" - ") ?: "Sin superficie asignada",
                irrigationType = doc.getString("irrigationType") ?: "Temporal (lluvia)",
                notes = doc.getString("notes") ?: "",
                nextActivity = doc.getString("nextActivity")
                    ?: doc.getString("riskLabel") ?: "Pendiente de revision",
                icon = doc.getString("icon") ?: "leaf",
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                needsSync = false
            )
        }
    }

    suspend fun downloadActivities(uid: String): List<CalendarActivityEntity> {
        return firestore(uid).collection("calendar_activities").get().await().documents.mapNotNull { doc ->
            val id = doc.getString("id") ?: doc.id
            CalendarActivityEntity(
                id = id,
                day = doc.getLong("day")?.toInt() ?: 1,
                month = doc.getString("month") ?: "JUN",
                type = doc.getString("type") ?: "Actividad",
                title = doc.getString("title") ?: return@mapNotNull null,
                cropName = doc.getString("cropName") ?: "General",
                colorTag = doc.getString("colorTag") ?: "green",
                needsSync = false
            )
        }
    }

    suspend fun downloadPests(uid: String): List<PestEntity> {
        return firestore(uid).collection("pests").get().await().documents.mapNotNull { doc ->
            val id = doc.getString("id") ?: doc.id
            PestEntity(
                id = id,
                name = doc.getString("name") ?: return@mapNotNull null,
                affectedCrop = doc.getString("affectedCrop") ?: "General",
                severity = doc.getString("severity") ?: "Baja",
                description = doc.getString("description") ?: "Sin descripcion",
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun downloadExpenses(uid: String): List<ExpenseEntity> {
        return firestore(uid).collection("expenses").get().await().documents.mapNotNull { doc ->
            val id = doc.getString("id") ?: doc.id
            ExpenseEntity(
                id = id,
                concept = doc.getString("concept") ?: return@mapNotNull null,
                amount = doc.getLong("amount")?.toInt() ?: 0,
                date = doc.getString("date") ?: "Sin fecha",
                category = doc.getString("category") ?: "Semillas",
                needsSync = false
            )
        }
    }

    private fun firestore(uid: String) = FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)

    private fun CropEntity.toCloudMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "stage" to stage,
        "sowDate" to sowDate,
        "surfaceArea" to surfaceArea,
        "irrigationType" to irrigationType,
        "notes" to notes,
        "nextActivity" to nextActivity,
        "icon" to icon,
        "updatedAt" to updatedAt
    )

    private fun CalendarActivityEntity.toCloudMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "day" to day,
        "month" to month,
        "type" to type,
        "title" to title,
        "cropName" to cropName,
        "colorTag" to colorTag
    )

    private fun PestEntity.toCloudMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "affectedCrop" to affectedCrop,
        "severity" to severity,
        "description" to description,
        "updatedAt" to updatedAt
    )

    private fun ExpenseEntity.toCloudMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "concept" to concept,
        "amount" to amount,
        "date" to date,
        "category" to category
    )
}
