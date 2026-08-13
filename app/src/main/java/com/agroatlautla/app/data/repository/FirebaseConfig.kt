package com.agroatlautla.app.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp

object FirebaseConfig {
    const val MissingConfigMessage =
        "Firebase no esta configurado. Agrega app/google-services.json para usar cuentas globales."

    fun ensureInitialized(context: Context): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
        } catch (_: IllegalStateException) {
            false
        } catch (_: Exception) {
            false
        }
    }
}
