package com.agroatlautla.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.agroatlautla.app.ui.screens.components.InfoRow
import com.agroatlautla.app.ui.screens.components.MessageBanner
import com.agroatlautla.app.ui.screens.components.MiniRoundIcon
import com.agroatlautla.app.ui.screens.components.ScreenWithHeader
import com.agroatlautla.app.ui.theme.AgroDanger
import com.agroatlautla.app.ui.theme.AgroGreen
import com.agroatlautla.app.ui.theme.AgroGreenSoft
import com.agroatlautla.app.ui.theme.AgroMuted
import com.agroatlautla.app.ui.theme.AgroWarning

@Composable
fun ProfileScreen(viewModel: AgroViewModel, onLogout: () -> Unit) {
    val pendingSync by viewModel.pendingSync.collectAsState()
    val user = viewModel.currentUser

    LaunchedEffect(Unit) { viewModel.clearMessage() }

    ScreenWithHeader(title = "Perfil") {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                Surface(shape = CircleShape, color = AgroGreenSoft, modifier = Modifier.size(64.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("U", color = AgroGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(user?.fullName ?: "Usuario", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(user?.location ?: "Atlautla, Estado de Mexico", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text(user?.productionArea ?: "Productor registrado", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                }
            }
        }
        item {
            ProfileCard(title = "CONECTIVIDAD") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MiniRoundIcon("W", AgroGreen)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (viewModel.isOnline) "En linea" else "Sin internet", fontWeight = FontWeight.Bold)
                        Text(if (viewModel.isOnline) "Conexion activa" else "Cambios se guardan localmente", color = AgroMuted, fontSize = 12.sp)
                    }
                    Switch(checked = viewModel.isOnline, onCheckedChange = null)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { viewModel.syncNow() }, modifier = Modifier.fillMaxWidth()) {
                    Text("S", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text("Sincronizar datos")
                }
                Spacer(Modifier.height(8.dp))
                Text("${pendingSync.size} cambios pendientes", color = AgroGreen, fontSize = 12.sp)
                MessageBanner(viewModel.message)
            }
            ProfileCard(title = "INFORMACION DEL PRODUCTOR") {
                InfoRow("Nombre completo", user?.fullName ?: "Sin registrar")
                InfoRow("Correo", user?.email ?: "Sin correo")
                InfoRow("Zona", user?.location ?: "Atlautla")
            }
            ProfileCard(title = "NOTIFICACIONES") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("N", color = AgroWarning, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    Text("Alertas de riego, plagas y actividades")
                    Spacer(Modifier.weight(1f))
                    Text(">", color = AgroMuted)
                }
            }
            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AgroDanger),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("X", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesion")
            }
        }
    }
}

@Composable
private fun ProfileCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}