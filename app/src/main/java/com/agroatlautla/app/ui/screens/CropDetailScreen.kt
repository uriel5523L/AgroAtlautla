package com.agroatlautla.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroatlautla.app.ui.AgroViewModel
import com.agroatlautla.app.ui.screens.components.GreenHeader
import com.agroatlautla.app.ui.screens.components.InfoRow
import com.agroatlautla.app.ui.screens.components.MiniRoundIcon
import com.agroatlautla.app.ui.screens.components.ScreenWithHeader
import com.agroatlautla.app.ui.screens.components.itemCard
import com.agroatlautla.app.ui.theme.AgroGreen
import com.agroatlautla.app.ui.theme.AgroMuted
import com.agroatlautla.app.ui.theme.AgroText

@Composable
fun CropDetailScreen(viewModel: AgroViewModel, cropId: String, onBack: () -> Unit) {
    val crops by viewModel.crops.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val crop = crops.firstOrNull { it.id == cropId }
    val cropActivities = crop?.let { active -> activities.filter { it.cropName == active.name } } ?: emptyList()

    ScreenWithHeader(title = "Detalle del Cultivo", onBack = onBack) {
        if (crop == null) {
            item { itemCard { Text("Cultivo no encontrado", color = AgroMuted) } }
        } else {
            item {
                GreenHeader {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MiniRoundIcon(cropIconText(crop), Color.White)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(crop.name, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Text(crop.stage, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        }
                    }
                }
            }
            item {
                itemCard {
                    Column {
                        Text("INFORMACION DEL CULTIVO", color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        InfoRow("Fecha de siembra", crop.sowDate)
                        InfoRow("Superficie", crop.surfaceArea)
                        InfoRow("Tipo de riego", crop.irrigationType)
                        InfoRow("Proxima actividad", crop.nextActivity)
                        InfoRow("Estado actual", crop.stage)
                    }
                }
            }
            item {
                itemCard {
                    Column {
                        Text("NOTAS DEL AGRICULTOR", color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            crop.notes.ifBlank { "Sin notas registradas." },
                            color = AgroMuted,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
            item {
                itemCard {
                    Column {
                        Text("HISTORIAL DE ACTIVIDADES", color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        if (cropActivities.isEmpty()) {
                            Text("Sin actividades registradas.", color = AgroMuted, fontSize = 13.sp)
                        } else {
                            cropActivities.forEach { activity ->
                                TimelineRow(activity.day.toString(), activity.title, true)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(date: String, activity: String, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 5.dp)) {
        Spacer(Modifier.width(2.dp))
        Text(date, color = AgroMuted, fontSize = 12.sp, modifier = Modifier.width(48.dp))
        Text(activity, color = if (done) AgroText else AgroMuted, fontSize = 13.sp)
    }
}