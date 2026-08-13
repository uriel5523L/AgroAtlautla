package com.agroatlautla.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agroatlautla.app.data.local.AgroDatabase
import com.agroatlautla.app.data.local.CalendarActivityEntity
import com.agroatlautla.app.data.local.CropEntity
import com.agroatlautla.app.data.local.ExpenseEntity
import com.agroatlautla.app.data.local.PestEntity
import com.agroatlautla.app.data.local.SyncQueueEntity
import com.agroatlautla.app.data.local.UserEntity
import com.agroatlautla.app.data.repository.AgroRepository
import com.agroatlautla.app.data.repository.AuthRepository
import com.agroatlautla.app.data.repository.FirebaseConfig
import com.agroatlautla.app.sync.NetworkMonitor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AgroViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AgroDatabase.getDatabase(application)
    private val agroRepository = AgroRepository(application, database)
    private val authRepository = AuthRepository(application, database.userDao())

    val crops: StateFlow<List<CropEntity>> = agroRepository.crops.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val activities: StateFlow<List<CalendarActivityEntity>> = agroRepository.activities.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val pests: StateFlow<List<PestEntity>> = agroRepository.pests.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val expenses: StateFlow<List<ExpenseEntity>> = agroRepository.expenses.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
    val pendingSync: StateFlow<List<SyncQueueEntity>> = agroRepository.pendingSync.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    var currentUser by mutableStateOf<UserEntity?>(null)
        private set
    var isOnline by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            agroRepository.seed()
            currentUser = authRepository.currentUser()
            currentUser?.id?.let { agroRepository.refreshFromCloud(it) }
            refreshNetworkStatus()
            if (!FirebaseConfig.ensureInitialized(application)) {
                message = FirebaseConfig.MissingConfigMessage
            }
        }
    }

    fun clearMessage() {
        message = null
    }

    fun refreshNetworkStatus() {
        isOnline = NetworkMonitor.isOnline(getApplication())
    }

    fun register(fullName: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = authRepository.register(fullName, email, password)
            result.fold(
                onSuccess = {
                    currentUser = it
                    agroRepository.refreshFromCloud(it.id)
                    message = "Cuenta creada en Firebase correctamente."
                    onSuccess()
                },
                onFailure = { message = it.message }
            )
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = {
                    currentUser = it
                    agroRepository.refreshFromCloud(it.id)
                    message = "Sesion iniciada con Firebase."
                    onSuccess()
                },
                onFailure = { message = it.message }
            )
        }
    }

    fun recover(email: String) {
        viewModelScope.launch {
            val result = authRepository.recoverAccount(email)
            message = result.fold(
                onSuccess = { "Correo de recuperacion enviado desde Firebase." },
                onFailure = { it.message ?: "No se pudo recuperar la cuenta." }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        currentUser = null
        message = "Sesion cerrada."
    }

    fun addCrop(
        name: String = "Nuevo cultivo",
        sowDate: String = "Sin fecha de siembra",
        irrigationType: String = "Temporal (lluvia)",
        notes: String = "",
        surfaceArea: String = ""
    ) {
        viewModelScope.launch {
            agroRepository.addCrop(name, sowDate, irrigationType, notes, surfaceArea)
            message = "Cultivo guardado localmente y pendiente de sincronizacion."
        }
    }

    fun addActivity(
        title: String = "Nueva actividad",
        day: Int = 1,
        month: String = "JUL",
        type: String = "Actividad",
        cropName: String = "General"
    ) {
        viewModelScope.launch {
            agroRepository.addActivity(title, day, month, type, cropName)
            message = "Actividad guardada localmente y pendiente de sincronizacion."
        }
    }

    fun addExpense(concept: String, amount: Int, date: String, category: String) {
        viewModelScope.launch {
            agroRepository.addExpense(concept, amount, date, category)
            message = "Gasto guardado localmente y pendiente de sincronizacion."
        }
    }

    fun deleteCrop(cropId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            agroRepository.deleteCrop(cropId)
            message = "Cultivo eliminado. La baja se sincronizara con tu cuenta."
            onDeleted()
        }
    }

    fun updateCrop(
        cropId: String,
        name: String,
        sowDate: String,
        irrigationType: String,
        notes: String,
        surfaceArea: String,
        onSaved: () -> Unit = {}
    ) {
        viewModelScope.launch {
            agroRepository.updateCrop(cropId, name, sowDate, surfaceArea, irrigationType, notes)
            message = "Cultivo actualizado y pendiente de sincronizacion."
            onSaved()
        }
    }

    fun deleteActivity(activityId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            agroRepository.deleteActivity(activityId)
            message = "Actividad eliminada. La baja se sincronizara con tu cuenta."
            onDeleted()
        }
    }

    fun deleteExpense(expenseId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            agroRepository.deleteExpense(expenseId)
            message = "Gasto eliminado. La baja se sincronizara con tu cuenta."
            onDeleted()
        }
    }

    fun deletePest(pestId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            agroRepository.deletePest(pestId)
            message = "Plaga eliminada del catalogo. La baja se sincronizara con tu cuenta."
            onDeleted()
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            refreshNetworkStatus()
            if (!isOnline) {
                message = "Sin internet: los cambios quedan pendientes."
                return@launch
            }
            val result = agroRepository.syncPendingToCloud(authRepository.currentUserId())
            message = result.fold(
                onSuccess = { count -> "Datos sincronizados con Firestore. Cambios enviados: $count." },
                onFailure = { it.message ?: "No se pudo sincronizar." }
            )
        }
    }
}
