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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroatlautla.app.data.local.PestEntity
import com.agroatlautla.app.ui.AgroViewModel
import com.agroatlautla.app.ui.screens.components.AlertBanner
import com.agroatlautla.app.ui.screens.components.MiniRoundIcon
import com.agroatlautla.app.ui.screens.components.ScreenWithHeader
import com.agroatlautla.app.ui.screens.components.SeverityPill
import com.agroatlautla.app.ui.screens.components.itemCard
import com.agroatlautla.app.ui.theme.AgroDanger
import com.agroatlautla.app.ui.theme.AgroGreen
import com.agroatlautla.app.ui.theme.AgroMuted
import com.agroatlautla.app.ui.theme.AgroWarning

@Composable
fun PestsScreen(viewModel: AgroViewModel, onPestSelected: (String) -> Unit) {
    val pests by viewModel.pests.collectAsState()

    ScreenWithHeader(title = "Catalogo de Plagas") {
        item {
            AlertBanner("${pests.count { it.severity == "Alta" }} plagas de riesgo alto identificadas en la region")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SeverityPill("Alta", AgroDanger)
                SeverityPill("Media", AgroWarning)
                SeverityPill("Baja", AgroGreen)
            }
            Spacer(Modifier.height(12.dp))
        }
        items(pests) { pest -> PestCard(pest, onClick = { onPestSelected(pest.id) }) }
    }
}

@Composable
fun PestDetailScreen(viewModel: AgroViewModel, pestId: String, onBack: () -> Unit) {
    val pests by viewModel.pests.collectAsState()
    val pest = pests.firstOrNull { it.id == pestId }

    ScreenWithHeader(title = "Detalle de Plaga", onBack = onBack, headerColor = AgroDanger) {
        if (pest == null) {
            item { itemCard { Text("Plaga no encontrada", color = AgroMuted) } }
        } else {
            val knowledge = pestKnowledge(pest)
            item {
                itemCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MiniRoundIcon("P", severityColor(pest.severity))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(pest.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("Afecta: ${pest.affectedCrop}", color = AgroMuted, fontSize = 12.sp)
                        }
                        SeverityPill(pest.severity, severityColor(pest.severity))
                    }
                }
            }
            item { PestInfoBlock("SINTOMAS", knowledge.symptoms, AgroDanger) }
            item { PestInfoBlock("RECOMENDACIONES DE CONTROL", knowledge.recommendations, AgroWarning) }
            item { PestInfoBlock("PREVENCION", knowledge.prevention, AgroGreen) }
        }
    }
}

@Composable
private fun PestCard(pest: PestEntity, onClick: () -> Unit) {
    itemCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MiniRoundIcon("P", severityColor(pest.severity))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(pest.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    SeverityPill(pest.severity, severityColor(pest.severity))
                }
                Text("Cultivo: ${pest.affectedCrop}", color = AgroGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(pest.description, color = AgroMuted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(">", color = AgroMuted)
        }
    }
}

@Composable
private fun PestInfoBlock(title: String, text: String, color: Color) {
    itemCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MiniRoundIcon(title.first().toString(), color)
                Spacer(Modifier.width(10.dp))
                Text(title, color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Text(text, color = AgroMuted, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

private fun pestKnowledge(pest: PestEntity): PestKnowledge = when (pest.name) {
    "Gusano cogollero" -> PestKnowledge(
        symptoms = pest.description,
        recommendations = "Aplicar insecticida biologico Bacillus thuringiensis y monitorear semanalmente.",
        prevention = "Rotacion de cultivos, siembra en epoca adecuada y uso de trampas de feromonas."
    )
    "Pulgon negro" -> PestKnowledge(
        symptoms = pest.description,
        recommendations = "Usar jabon potasico o extracto de ajo y fomentar insectos beneficos.",
        prevention = "Evitar exceso de nitrogeno y usar plantas repelentes como albahaca."
    )
    "Roya del frijol" -> PestKnowledge(
        symptoms = pest.description,
        recommendations = "Aplicar fungicida cuprico y retirar plantas afectadas.",
        prevention = "Usar variedades resistentes, espaciamiento adecuado y evitar riego nocturno."
    )
    "Trips del aguacate" -> PestKnowledge(
        symptoms = pest.description,
        recommendations = "Aplicar neem o spinosad y colocar trampas azules adhesivas.",
        prevention = "Eliminar malezas hospederas y mantener cobertura del suelo."
    )
    "Chahuixtle" -> PestKnowledge(
        symptoms = pest.description,
        recommendations = "Aplicar fungicidas preventivos y retirar plantas infectadas.",
        prevention = "Usar semilla certificada libre de enfermedad y rotar cultivos."
    )
    else -> PestKnowledge(
        symptoms = pest.description,
        recommendations = "Aplicar acaricida especifico, aumentar humedad y vigilar la recuperacion.",
        prevention = "Mantener riego adecuado, evitar estres hidrico y usar plantas trampa."
    )
}

private data class PestKnowledge(
    val symptoms: String,
    val recommendations: String,
    val prevention: String
)

private fun severityColor(severity: String): Color = when (severity) {
    "Alta" -> AgroDanger
    "Media" -> AgroWarning
    else -> AgroGreen
}