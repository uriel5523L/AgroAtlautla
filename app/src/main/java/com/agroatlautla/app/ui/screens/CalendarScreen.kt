package com.agroatlautla.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroatlautla.app.data.local.CalendarActivityEntity
import com.agroatlautla.app.ui.AgroViewModel
import com.agroatlautla.app.ui.screens.components.AssistChipCompat
import com.agroatlautla.app.ui.screens.components.ScreenWithHeader
import com.agroatlautla.app.ui.screens.components.itemCard
import com.agroatlautla.app.ui.theme.AgroGreen
import com.agroatlautla.app.ui.theme.AgroInfo
import com.agroatlautla.app.ui.theme.AgroMuted
import com.agroatlautla.app.ui.theme.AgroWarning

private val activityTypes = listOf("Siembra", "Riego", "Fertilizacion", "Cosecha", "Actividad")
private val months = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

@Composable
fun CalendarScreen(viewModel: AgroViewModel, onAddActivity: () -> Unit) {
    val activities by viewModel.activities.collectAsState()
    var filter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Siembra", "Riego", "Fertilizacion", "Cosecha")
    val visibleActivities = if (filter == "Todos") {
        activities
    } else {
        activities.filter { it.type == filter }
    }

    ScreenWithHeader(title = "Calendario Agricola") {
        item {
            MonthSelector()
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.take(3).forEach { item ->
                    FilterChip(
                        selected = filter == item,
                        onClick = { filter = item },
                        label = { Text(item) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.drop(3).forEach { item ->
                    FilterChip(
                        selected = filter == item,
                        onClick = { filter = item },
                        label = { Text(item) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        items(visibleActivities) { activity -> ActivityCard(activity) }
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAddActivity,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("+", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text("Agregar actividad")
            }
        }
    }
}

@Composable
fun AddActivityScreen(viewModel: AgroViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("JUL") }
    var type by remember { mutableStateOf(activityTypes.first()) }
    var cropName by remember { mutableStateOf("General") }
    val crops by viewModel.crops.collectAsState()
    val parsedDay = day.toIntOrNull()
    val canSave = title.isNotBlank() && parsedDay != null && parsedDay in 1..31 && cropName.isNotBlank()

    ScreenWithHeader(title = "Agregar Actividad", onBack = onBack) {
        item {
            itemCard {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titulo de la actividad *") },
                        placeholder = { Text("Ej: Riego de maiz") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = day,
                            onValueChange = { day = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text("Dia *") },
                            placeholder = { Text("15") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(2f)) {
                            Text("Mes", color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                months.take(6).forEach { m ->
                                    MonthChip(selected = month == m, label = m, onClick = { month = m })
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                months.drop(6).forEach { m ->
                                    MonthChip(selected = month == m, label = m, onClick = { month = m })
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Tipo", color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        activityTypes.take(3).forEach { item ->
                            FilterChip(
                                selected = type == item,
                                onClick = { type = item },
                                label = { Text(item, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        activityTypes.drop(3).forEach { item ->
                            FilterChip(
                                selected = type == item,
                                onClick = { type = item },
                                label = { Text(item, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Cultivo", color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        (listOf("General") + crops.map { it.name }).forEach { name ->
                            FilterChip(
                                selected = cropName == name,
                                onClick = { cropName = name },
                                label = { Text(name, fontSize = 11.sp, maxLines = 1) }
                            )
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = {
                    viewModel.addActivity(title, parsedDay ?: 1, month, type, cropName)
                    onSaved()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Guardar actividad", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MonthChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 10.sp) }
    )
}

@Composable
private fun MonthSelector() {
    Surface(color = AgroGreen, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("<", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Junio 2026", color = Color.White, fontWeight = FontWeight.Bold)
            Text(">", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActivityCard(activity: CalendarActivityEntity) {
    itemCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = tagColor(activity.colorTag).copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(64.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(activity.day.toString(), color = tagColor(activity.colorTag), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(activity.month, color = AgroMuted, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                AssistChipCompat(label = activity.type)
                Text(activity.title, fontWeight = FontWeight.Bold)
                Text("Cultivo: ${activity.cropName}", color = AgroMuted, fontSize = 12.sp)
            }
        }
    }
}

private fun tagColor(tag: String): Color = when (tag) {
    "blue" -> AgroInfo
    "orange" -> AgroWarning
    else -> AgroGreen
}