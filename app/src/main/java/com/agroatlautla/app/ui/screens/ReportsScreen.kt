package com.agroatlautla.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
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
import com.agroatlautla.app.ui.screens.components.MiniRoundIcon
import com.agroatlautla.app.ui.screens.components.ScreenWithHeader
import com.agroatlautla.app.ui.screens.components.itemCard
import com.agroatlautla.app.ui.theme.AgroBrown
import com.agroatlautla.app.ui.theme.AgroDanger
import com.agroatlautla.app.ui.theme.AgroGreen
import com.agroatlautla.app.ui.theme.AgroInfo
import com.agroatlautla.app.ui.theme.AgroMuted
import com.agroatlautla.app.ui.theme.AgroText
import com.agroatlautla.app.ui.theme.AgroWarning

@Composable
fun ReportsScreen(viewModel: AgroViewModel, onBack: () -> Unit) {
    val crops by viewModel.crops.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val pests by viewModel.pests.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val total = expenses.sumOf { it.amount }
    val byCategory = expenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
    val maxCategory = (byCategory.values.maxOrNull() ?: 1).coerceAtLeast(1)
    val totalHa = crops.sumOf { parseHectares(it.surfaceArea) }

    ScreenWithHeader(title = "Reportes", onBack = onBack, headerColor = AgroInfo) {
        item {
            Text(
                "Temporada agricola 2026 - Atlautla",
                color = AgroMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                fontSize = 13.sp
            )
        }
        item {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportKpiCard(crops.size.toString(), "Total cultivos", "activos", AgroGreen, Modifier.weight(1f))
                ReportKpiCard("${'$'}$total", "Gastos", "temporada 2026", AgroDanger, Modifier.weight(1f))
            }
        }
        item {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportKpiCard(activities.size.toString(), "Actividades", "en calendario", AgroWarning, Modifier.weight(1f))
                ReportKpiCard("${pests.count { it.severity == "Alta" }}", "Plagas", "riesgo alto", AgroDanger, Modifier.weight(1f))
            }
        }
        item {
            itemCard {
                Column {
                    Text("GASTOS POR CATEGORIA", color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    ProgressLine("Semillas", byCategory["Semillas"] ?: 0, maxCategory, AgroGreen)
                    ProgressLine("Fertilizante", byCategory["Fertilizante"] ?: 0, maxCategory, AgroWarning)
                    ProgressLine("Transporte", byCategory["Transporte"] ?: 0, maxCategory, AgroInfo)
                    ProgressLine("Mano de obra", byCategory["Mano de obra"] ?: 0, maxCategory, AgroBrown)
                    ProgressLine("Herramientas", byCategory["Herramientas"] ?: 0, maxCategory, AgroDanger)
                }
            }
        }
        item {
            itemCard {
                Column {
                    Text("DISTRIBUCION DE CULTIVOS", color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    if (totalHa <= 0.0) {
                        Text("Sin superficies registradas.", color = AgroMuted, fontSize = 13.sp)
                    } else {
                        crops.forEachIndexed { index, crop ->
                            val share = ((parseHectares(crop.surfaceArea) / totalHa) * 100.0)
                            DistributionRow(
                                label = crop.name,
                                value = "${formatShare(share)}% (${crop.surfaceArea})",
                                color = distributionColor(index)
                            )
                        }
                    }
                }
            }
        }
        item {
            itemCard {
                Column {
                    Text("CUENTA DE LA EXPLOTACION", color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    ProgressLine("Cultivos activos", crops.size, crops.size.coerceAtLeast(1), AgroGreen, crops.size.toString())
                    ProgressLine("Actividades registradas", activities.size, activities.size.coerceAtLeast(1), AgroInfo, activities.size.toString())
                    ProgressLine("Gastos registrados", expenses.size, expenses.size.coerceAtLeast(1), AgroWarning, "${'$'}$total")
                }
            }
        }
    }
}

private fun formatShare(value: Double): String = Math.round(value).toInt().toString()

@Composable
private fun ReportKpiCard(value: String, label: String, sub: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            MiniRoundIcon(label.first().toString(), color)
            Spacer(Modifier.height(10.dp))
            Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(label, color = AgroText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(sub, color = AgroMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ProgressLine(label: String, value: Int, max: Int, color: Color, valueLabel: String = "${'$'}$value") {
    val fraction = if (max == 0) 0f else (value.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    Column(Modifier.padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = AgroMuted, fontSize = 12.sp)
            Text(valueLabel, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(5.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFECEFED), RoundedCornerShape(10.dp))) {
            Box(Modifier.fillMaxWidth(fraction).height(8.dp).background(color, RoundedCornerShape(10.dp)))
        }
    }
}

@Composable
private fun DistributionRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 5.dp)) {
        Surface(color = color, shape = CircleShape, modifier = Modifier.size(10.dp)) {}
        Spacer(Modifier.width(10.dp))
        Text(label, color = AgroText, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

private fun distributionColor(index: Int): Color = when (index % 5) {
    0 -> AgroGreen
    1 -> AgroWarning
    2 -> AgroInfo
    3 -> AgroBrown
    else -> AgroDanger
}