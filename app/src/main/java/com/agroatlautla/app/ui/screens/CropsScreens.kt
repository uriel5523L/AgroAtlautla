package com.agroatlautla.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
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
import com.agroatlautla.app.data.local.CropEntity
import com.agroatlautla.app.ui.AgroViewModel
import com.agroatlautla.app.ui.screens.components.AssistChipCompat
import com.agroatlautla.app.ui.screens.components.MiniRoundIcon
import com.agroatlautla.app.ui.screens.components.MessageBanner
import com.agroatlautla.app.ui.screens.components.ScreenWithHeader
import com.agroatlautla.app.ui.screens.components.itemCard
import com.agroatlautla.app.ui.theme.AgroGreen
import com.agroatlautla.app.ui.theme.AgroMuted

val irrigationTypes = listOf("Temporal (lluvia)", "Goteo", "Aspersion", "Gravedad", "Manual")

@Composable
fun CropsScreen(
    viewModel: AgroViewModel,
    onAddCrop: () -> Unit,
    onCropSelected: (String) -> Unit
) {
    val crops by viewModel.crops.collectAsState()
    val totalHa = formatHectares(crops.sumOf { parseHectares(it.surfaceArea) })
    val pending = crops.count { it.needsSync }

    ScreenWithHeader(title = "Mis Cultivos") {
        item {
            SummaryStrip(
                first = "${crops.size}\nCultivos",
                second = "$totalHa ha\nTotal",
                third = "$pending\nPendientes"
            )
        }
        items(crops) { crop -> CropCard(crop, onClick = { onCropSelected(crop.id) }) }
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAddCrop,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("+", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text("Agregar cultivo")
            }
        }
    }
}

@Composable
fun AddCropScreen(viewModel: AgroViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var sowDate by remember { mutableStateOf("") }
    var surfaceArea by remember { mutableStateOf("") }
    var irrigation by remember { mutableStateOf(irrigationTypes.first()) }
    var notes by remember { mutableStateOf("") }
    val canSave = name.isNotBlank() && sowDate.isNotBlank()

    LaunchedEffect(Unit) { viewModel.clearMessage() }

    ScreenWithHeader(title = "Agregar Cultivo", onBack = onBack) {
        item {
            itemCard {
                Column {
                    MessageBanner(viewModel.message)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del cultivo *") },
                        placeholder = { Text("Ej: Maiz, Frijol, Avena") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = sowDate,
                        onValueChange = { sowDate = it },
                        label = { Text("Fecha de siembra *") },
                        placeholder = { Text("Ej: 15 Mar 2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = surfaceArea,
                        onValueChange = { surfaceArea = it },
                        label = { Text("Superficie (ha)") },
                        placeholder = { Text("Ej: 2.5") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Tipo de riego", color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        irrigationTypes.take(3).forEach { type ->
                            FilterChip(
                                selected = irrigation == type,
                                onClick = { irrigation = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas adicionales") },
                        placeholder = { Text("Observaciones del terreno") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item {
            Button(
                onClick = {
                    viewModel.addCrop(name, sowDate, irrigation, notes, surfaceArea)
                    onSaved()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Guardar cultivo", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditCropScreen(viewModel: AgroViewModel, cropId: String, onBack: () -> Unit, onSaved: () -> Unit) {
    val crops by viewModel.crops.collectAsState()
    val crop = crops.firstOrNull { it.id == cropId }
    var name by remember(cropId) { mutableStateOf(crop?.name ?: "") }
    var sowDate by remember(cropId) { mutableStateOf(crop?.sowDate ?: "") }
    var surfaceArea by remember(cropId) { mutableStateOf((crop?.surfaceArea ?: "").removeSuffix(" ha")) }
    var irrigation by remember(cropId) { mutableStateOf(crop?.irrigationType ?: irrigationTypes.first()) }
    var notes by remember(cropId) { mutableStateOf(crop?.notes ?: "") }
    val canSave = name.isNotBlank() && sowDate.isNotBlank()

    LaunchedEffect(Unit) { viewModel.clearMessage() }

    ScreenWithHeader(title = "Editar Cultivo", onBack = onBack) {
        if (crop == null) {
            item { itemCard { Text("Cultivo no encontrado", color = AgroMuted) } }
        } else {
            item {
                itemCard {
                    Column {
                        MessageBanner(viewModel.message)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nombre del cultivo *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = sowDate,
                            onValueChange = { sowDate = it },
                            label = { Text("Fecha de siembra *") },
                            placeholder = { Text("Ej: 15 Mar 2026") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = surfaceArea,
                            onValueChange = { surfaceArea = it },
                            label = { Text("Superficie (ha)") },
                            placeholder = { Text("Ej: 2.5") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Tipo de riego", color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            irrigationTypes.take(3).forEach { type ->
                                FilterChip(
                                    selected = irrigation == type,
                                    onClick = { irrigation = type },
                                    label = { Text(type, fontSize = 11.sp) }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notas adicionales") },
                            placeholder = { Text("Observaciones del terreno") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        viewModel.updateCrop(cropId, name, sowDate, irrigation, notes, surfaceArea, onSaved)
                    },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Guardar cambios", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SummaryStrip(first: String, second: String, third: String) {
    itemCard {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryBox(first, Modifier.weight(1f))
            SummaryBox(second, Modifier.weight(1f))
            SummaryBox(third, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryBox(text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = AgroGreen, shape = RoundedCornerShape(12.dp)) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun CropCard(crop: CropEntity, onClick: () -> Unit) {
    itemCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MiniRoundIcon(cropIconText(crop), AgroGreen)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(crop.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(crop.surfaceArea, color = AgroMuted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChipCompat(label = crop.stage)
                    Text(crop.nextActivity, color = AgroMuted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
            Text(">", color = AgroMuted)
        }
    }
}

