package com.agroatlautla.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroatlautla.app.data.repository.FirebaseConfig
import com.agroatlautla.app.sync.SyncScheduler
import com.agroatlautla.app.ui.AgroApp
import com.agroatlautla.app.ui.AgroViewModel
import com.agroatlautla.app.ui.theme.AgroAtlautlaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseConfig.ensureInitialized(this)
        SyncScheduler.schedule(this)
        setContent {
            AgroAtlautlaTheme {
                val viewModel: AgroViewModel = viewModel()
                AgroApp(viewModel = viewModel)
            }
        }
    }
}
