package com.agroatlautla.app.data.repository

import android.content.Context
import com.agroatlautla.app.data.local.UserDao
import com.agroatlautla.app.data.local.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(
    private val context: Context,
    private val userDao: UserDao
) {
    private val prefs = context.getSharedPreferences("agroatlautla_session", Context.MODE_PRIVATE)

    suspend fun register(fullName: String, email: String, password: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            if (!FirebaseConfig.ensureInitialized(context)) {
                return@withContext Result.failure(IllegalStateException(FirebaseConfig.MissingConfigMessage))
            }

            val cleanEmail = email.trim().lowercase()
            if (fullName.isBlank() || cleanEmail.isBlank() || password.length < 6) {
                return@withContext Result.failure(
                    IllegalArgumentException("Completa los datos. La contrasena debe tener al menos 6 caracteres.")
                )
            }

            runCatching {
                val credential = FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(cleanEmail, password)
                    .await()
                val firebaseUser = credential.user
                    ?: error("Firebase no devolvio usuario.")

                val user = UserEntity(
                    id = firebaseUser.uid,
                    fullName = fullName.trim(),
                    email = cleanEmail,
                    passwordHash = "firebase_auth"
                )

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.id)
                    .set(user.toCloudMap())
                    .await()

                userDao.insert(user)
                setCurrentUser(user.id)
                user
            }
        }

    suspend fun login(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        if (!FirebaseConfig.ensureInitialized(context)) {
            return@withContext Result.failure(IllegalStateException(FirebaseConfig.MissingConfigMessage))
        }

        val cleanEmail = email.trim().lowercase()
        runCatching {
            val credential = FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(cleanEmail, password)
                .await()
            val firebaseUser = credential.user
                ?: error("Firebase no devolvio usuario.")

            val user = loadOrCreateCloudUser(
                uid = firebaseUser.uid,
                email = cleanEmail,
                fallbackName = firebaseUser.displayName ?: cleanEmail.substringBefore('@')
            )
            userDao.insert(user)
            setCurrentUser(user.id)
            user
        }
    }

    suspend fun recoverAccount(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!FirebaseConfig.ensureInitialized(context)) {
            return@withContext Result.failure(IllegalStateException(FirebaseConfig.MissingConfigMessage))
        }

        runCatching {
            FirebaseAuth.getInstance()
                .sendPasswordResetEmail(email.trim().lowercase())
                .await()
            Unit
        }
    }

    suspend fun currentUser(): UserEntity? = withContext(Dispatchers.IO) {
        if (!FirebaseConfig.ensureInitialized(context)) return@withContext null
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return@withContext null
        userDao.getById(firebaseUser.uid) ?: loadOrCreateCloudUser(
            uid = firebaseUser.uid,
            email = firebaseUser.email.orEmpty(),
            fallbackName = firebaseUser.displayName ?: "Productor"
        ).also { userDao.insert(it) }
    }

    fun logout() {
        if (FirebaseConfig.ensureInitialized(context)) {
            FirebaseAuth.getInstance().signOut()
        }
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    fun currentUserId(): String? {
        if (!FirebaseConfig.ensureInitialized(context)) return null
        return FirebaseAuth.getInstance().currentUser?.uid ?: prefs.getString(KEY_USER_ID, null)
    }

    private suspend fun loadOrCreateCloudUser(
        uid: String,
        email: String,
        fallbackName: String
    ): UserEntity {
        val docRef = FirebaseFirestore.getInstance().collection("users").document(uid)
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            return UserEntity(
                id = uid,
                fullName = snapshot.getString("fullName") ?: fallbackName,
                email = snapshot.getString("email") ?: email,
                passwordHash = "firebase_auth",
                location = snapshot.getString("location") ?: "Atlautla, Estado de Mexico",
                productionArea = snapshot.getString("productionArea") ?: "2018 - 4.8 ha",
                createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis()
            )
        }

        val user = UserEntity(
            id = uid,
            fullName = fallbackName,
            email = email,
            passwordHash = "firebase_auth"
        )
        docRef.set(user.toCloudMap()).await()
        return user
    }

    private fun setCurrentUser(id: String) {
        prefs.edit().putString(KEY_USER_ID, id).apply()
    }

    private fun UserEntity.toCloudMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "fullName" to fullName,
        "email" to email,
        "location" to location,
        "productionArea" to productionArea,
        "createdAt" to createdAt,
        "updatedAt" to System.currentTimeMillis()
    )

    companion object {
        private const val KEY_USER_ID = "current_user_id"
    }
}
