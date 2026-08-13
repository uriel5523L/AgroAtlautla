package com.agroatlautla.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroatlautla.app.ui.AgroViewModel
import com.agroatlautla.app.ui.screens.components.MessageBanner
import com.agroatlautla.app.ui.theme.AgroBackground
import com.agroatlautla.app.ui.theme.AgroGreen
import com.agroatlautla.app.ui.theme.AgroGreenDark
import com.agroatlautla.app.ui.theme.AgroMuted

@Composable
fun LoginScreen(
    viewModel: AgroViewModel,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onRecover: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.clearMessage() }

    AuthLayout(title = "Bienvenido", subtitle = "Inicia sesion para continuar") {
        MessageBanner(viewModel.message)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contrasena") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { viewModel.login(email, password, onLogin) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AgroGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Entrar")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRecover) { Text("Recuperar cuenta") }
        TextButton(onClick = onRegister) { Text("Crear cuenta nueva") }
    }
}

@Composable
fun RegisterScreen(viewModel: AgroViewModel, onBack: () -> Unit, onRegistered: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.clearMessage() }

    AuthLayout(title = "Crear cuenta", subtitle = "Registro de productor", onBack = onBack) {
        MessageBanner(viewModel.message)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contrasena") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { viewModel.register(name, email, password, onRegistered) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AgroGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Registrarme")
        }
    }
}

@Composable
fun RecoveryScreen(viewModel: AgroViewModel, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.clearMessage() }

    AuthLayout(title = "Recuperar cuenta", subtitle = "Ingresa tu correo", onBack = onBack) {
        MessageBanner(viewModel.message)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { viewModel.recover(email) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AgroGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Solicitar recuperacion")
        }
    }
}

@Composable
private fun AuthLayout(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroBackground)
            .padding(22.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) { Text("<", fontWeight = FontWeight.Bold) }
        }
        Text(title, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = AgroGreenDark)
        Text(subtitle, color = AgroMuted)
        Spacer(Modifier.height(22.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp), content = content)
        }
    }
}