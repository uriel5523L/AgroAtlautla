package com.agroatlautla.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.agroatlautla.app.data.local.AgroDatabase
import com.agroatlautla.app.data.repository.AgroRepository
import com.agroatlautla.app.data.repository.FirebaseConfig
import com.google.firebase.auth.FirebaseAuth

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (!NetworkMonitor.isOnline(applicationContext)) return Result.retry()
        if (!FirebaseConfig.ensureInitialized(applicationContext)) return Result.success()

        val database = AgroDatabase.getDatabase(applicationContext)
        val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
            ?: return Result.success()
        val result = AgroRepository(applicationContext, database).syncPendingToCloud(uid)
        if (result.isFailure) return Result.retry()

        return Result.success()
    }
}
