package com.agroatlautla.app.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroatlautla.app.ui.theme.AgroBackground
import com.agroatlautla.app.ui.theme.AgroDanger
import com.agroatlautla.app.ui.theme.AgroGreen
import com.agroatlautla.app.ui.theme.AgroGreenDark
import com.agroatlautla.app.ui.theme.AgroGreenSoft
import com.agroatlautla.app.ui.theme.AgroMuted

@Composable
fun ScreenWithHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    headerColor: Color = AgroGreenDark,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroBackground)
    ) {
        item {
            GreenHeader(color = headerColor) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val backModifier = if (onBack == null) Modifier else Modifier.clickable(onClick = onBack)
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.15f), modifier = backModifier) {
                        Text("<", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        content()
    }
}

@Composable
fun GreenHeader(color: Color = AgroGreenDark, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(20.dp),
        content = content
    )
}

@Composable
fun ContentSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(title, color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun MiniRoundIcon(icon: String, color: Color) {
    Surface(color = color.copy(alpha = 0.14f), shape = CircleShape, modifier = Modifier.size(42.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(icon, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun itemCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Box(Modifier.padding(14.dp)) { content() }
    }
}

@Composable
fun MessageBanner(message: String?) {
    if (!message.isNullOrBlank()) {
        Surface(color = AgroGreenSoft, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text(message, color = AgroGreenDark, modifier = Modifier.padding(12.dp), fontSize = 12.sp)
        }
    }
}

@Composable
fun AlertBanner(text: String) {
    Surface(color = Color(0xFFFFE3E3), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text, color = AgroDanger, modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SeverityPill(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(12.dp)) {
        Text(text, color = color, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AssistChipCompat(label: String) {
    AssistChip(onClick = {}, label = { Text(label, fontSize = 11.sp) })
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = AgroMuted, fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}