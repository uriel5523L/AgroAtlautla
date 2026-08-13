package com.agroatlautla.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroatlautla.app.ui.screens.components.MiniRoundIcon
import com.agroatlautla.app.ui.theme.AgroDanger
import com.agroatlautla.app.ui.theme.AgroGreen
import com.agroatlautla.app.ui.theme.AgroGreenDark
import com.agroatlautla.app.ui.theme.AgroText
import com.agroatlautla.app.ui.theme.AgroYellow

@Composable
fun SplashScreen(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroGreenDark)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(128.dp),
                color = Color(0xFF69C98B),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("A", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("AgroAtlautla", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Apoyo digital para productores del campo", color = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(36.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniRoundIcon("C", AgroGreen)
                MiniRoundIcon("R", AgroGreen)
                MiniRoundIcon("P", AgroDanger)
                MiniRoundIcon("G", AgroGreen)
            }
        }

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = AgroYellow, contentColor = AgroText),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Iniciar", fontWeight = FontWeight.Bold)
        }
    }
}