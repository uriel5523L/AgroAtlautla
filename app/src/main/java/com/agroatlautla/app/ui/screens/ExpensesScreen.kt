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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.agroatlautla.app.data.local.ExpenseEntity
import com.agroatlautla.app.ui.AgroViewModel
import com.agroatlautla.app.ui.screens.components.ContentSection
import com.agroatlautla.app.ui.screens.components.MiniRoundIcon
import com.agroatlautla.app.ui.screens.components.ScreenWithHeader
import com.agroatlautla.app.ui.screens.components.SeverityPill
import com.agroatlautla.app.ui.screens.components.itemCard
import com.agroatlautla.app.ui.theme.AgroBrown
import com.agroatlautla.app.ui.theme.AgroDanger
import com.agroatlautla.app.ui.theme.AgroGreen
import com.agroatlautla.app.ui.theme.AgroInfo
import com.agroatlautla.app.ui.theme.AgroMuted
import com.agroatlautla.app.ui.theme.AgroWarning

val expenseCategories = listOf("Semillas", "Fertilizante", "Transporte", "Mano de obra", "Herramientas")

@Composable
fun ExpensesScreen(viewModel: AgroViewModel, onBack: () -> Unit, onAddExpense: () -> Unit) {
    val expenses by viewModel.expenses.collectAsState()
    val total = expenses.sumOf { it.amount }
    val byCategory = expenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
    var toDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    ScreenWithHeader(title = "Gastos", onBack = onBack, headerColor = AgroBrown) {
        item {
            itemCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Total de gastos", color = AgroMuted, fontSize = 13.sp)
                    Text("${'$'}$total", color = AgroBrown, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text("${expenses.size} gastos registrados", color = AgroMuted, fontSize = 12.sp)
                }
            }
        }
        item {
            ContentSection(title = "GASTOS POR CATEGORIA") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    byCategory.entries.take(3).forEach { (category, amount) ->
                        CategoryChip(category, amount, Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    byCategory.entries.drop(3).forEach { (category, amount) ->
                        CategoryChip(category, amount, Modifier.weight(1f))
                    }
                }
            }
        }
        items(expenses) { expense -> ExpenseCard(expense, onDelete = { toDelete = expense }) }
        item {
            Button(
                onClick = onAddExpense,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroBrown),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Registrar gasto", fontWeight = FontWeight.Bold)
            }
        }
    }

    toDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar gasto") },
            text = { Text("¿Seguro que quieres eliminar \"${expense.concept}\" (-$${expense.amount})? Esta accion no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    toDelete = null
                    viewModel.deleteExpense(expense.id)
                }) { Text("Eliminar", color = AgroDanger, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun AddExpenseScreen(viewModel: AgroViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    var concept by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(expenseCategories.first()) }
    val parsedAmount = amount.toIntOrNull()
    val canSave = concept.isNotBlank() && parsedAmount != null && date.isNotBlank()

    LaunchedEffect(Unit) { viewModel.clearMessage() }

    ScreenWithHeader(title = "Registrar Gasto", onBack = onBack, headerColor = AgroBrown) {
        item {
            itemCard {
                Column {
                    OutlinedTextField(
                        value = concept,
                        onValueChange = { concept = it },
                        label = { Text("Concepto *") },
                        placeholder = { Text("Ej: Fertilizante DAP") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Cantidad ($) *") },
                        placeholder = { Text("Ej: 850") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Fecha *") },
                        placeholder = { Text("Ej: 10 Mar 2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Categoria", color = AgroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        expenseCategories.take(3).forEach { item ->
                            FilterChip(
                                selected = category == item,
                                onClick = { category = item },
                                label = { Text(item, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        expenseCategories.drop(3).forEach { item ->
                            FilterChip(
                                selected = category == item,
                                onClick = { category = item },
                                label = { Text(item, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = {
                    viewModel.addExpense(concept, parsedAmount ?: 0, date, category)
                    onSaved()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgroBrown),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Guardar gasto", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CategoryChip(category: String, amount: Int, modifier: Modifier = Modifier) {
    val color = expenseColor(category)
    Surface(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(category, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${'$'}$amount", color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExpenseCard(expense: ExpenseEntity, onDelete: () -> Unit) {
    val color = expenseColor(expense.category)
    itemCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MiniRoundIcon("G", color)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(expense.concept, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SeverityPill(expense.category, color)
                    Spacer(Modifier.width(8.dp))
                    Text(expense.date, color = AgroMuted, fontSize = 11.sp)
                }
            }
            Text("-${'$'}${expense.amount}", color = AgroDanger, fontWeight = FontWeight.Bold)
            Text(
                "✕",
                color = AgroDanger,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onDelete).padding(start = 8.dp)
            )
        }
    }
}

fun expenseColor(category: String): Color = when (category) {
    "Semillas" -> AgroGreen
    "Fertilizante" -> AgroWarning
    "Transporte" -> AgroInfo
    "Mano de obra" -> Color(0xFF7B1FA2)
    else -> AgroDanger
}