package com.agroatlautla.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroatlautla.app.ui.AgroViewModel
import com.agroatlautla.app.ui.screens.components.ContentSection
import com.agroatlautla.app.ui.screens.components.GreenHeader
import com.agroatlautla.app.ui.screens.components.MiniRoundIcon
import com.agroatlautla.app.ui.theme.AgroBackground
import com.agroatlautla.app.ui.theme.AgroGreen
import com.agroatlautla.app.ui.theme.AgroGreenSoft
import com.agroatlautla.app.ui.theme.AgroInfo
import com.agroatlautla.app.ui.theme.AgroMuted
import com.agroatlautla.app.ui.theme.AgroWarning

@Composable
fun DashboardScreen(viewModel: AgroViewModel, onNavigate: (String) -> Unit) {
    val crops by viewModel.crops.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val pests by viewModel.pests.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val totalExpenses = expenses.sumOf { it.amount }

    LaunchedEffect(Unit) { viewModel.refreshNetworkStatus() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroBackground)
    ) {
        item {
            GreenHeader {
                Text("Buenos dias,", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            viewModel.currentUser?.fullName ?: "Productor",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Atlautla, Estado de Mexico", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                    ConnectionPill(viewModel.isOnline)
                }
            }
            ContentSection(title = "ACCESO RAPIDO") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAccessCard(
                        title = "Mis Cultivos",
                        subtitle = "${crops.size} cultivos activos",
                        icon = "C",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("crops") }
                    )
                    QuickAccessCard(
                        title = "Calendario Agricola",
                        subtitle = "${activities.size} actividades esta semana",
                        icon = "F",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("calendar") }
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAccessCard(
                        title = "Plagas",
                        subtitle = "${pests.size} plagas registradas",
                        icon = "P",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("pests") }
                    )
                    QuickAccessCard(
                        title = "Gastos",
                        subtitle = "Total: ${'$'}$totalExpenses",
                        icon = "G",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("expenses") }
                    )
                }
                Spacer(Modifier.height(12.dp))
                WideReportCard(onClick = { onNavigate("reports") })
            }
            ContentSection(title = "RESUMEN DE HOY") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(crops.size.toString(), "Cultivos", AgroGreen, Modifier.weight(1f))
                    StatCard("${'$'}$totalExpenses", "Gastos", AgroWarning, Modifier.weight(1f))
                    StatCard(activities.size.toString(), "Actividades", AgroInfo, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    title: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(118.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            MiniRoundIcon(icon, AgroGreen)
            Spacer(Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2)
            Text(subtitle, color = AgroMuted, fontSize = 11.sp, maxLines = 2)
        }
    }
}

@Composable
private fun WideReportCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            MiniRoundIcon("R", AgroInfo)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Reportes", fontWeight = FontWeight.Bold)
                Text("Ver resumen de temporada", color = AgroMuted, fontSize = 12.sp)
            }
            Text("R", color = AgroInfo, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontWeight = FontWeight.Bold)
            Text(label, color = AgroMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ConnectionPill(isOnline: Boolean) {
    Surface(color = AgroGreenSoft, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("W", color = AgroGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(Modifier.width(4.dp))
            Text(if (isOnline) "En linea" else "Offline", color = AgroGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}