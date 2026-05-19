package com.example.uniride.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ui.theme.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    var correo     by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verClave   by remember { mutableStateOf(false) }

    val cargando    by viewModel.cargando.observeAsState(false)
    val mensaje     by viewModel.mensaje.observeAsState(null)
    val loginResult by viewModel.loginResult.observeAsState(null)
    val context = LocalContext.current

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    LaunchedEffect(loginResult) {
        loginResult?.let { result ->
            val destino = if (result.rol == "administrador") Routes.ADMIN else Routes.HOME
            navController.navigate(destino) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.DirectionsCar, null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(8.dp))

        Text("UniRide", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary)

        Text("Carpooling universitario",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

        Spacer(Modifier.height(40.dp))

        TextField(
            value = correo, onValueChange = { correo = it },
            label = { Text("Correo institucional") },
            leadingIcon = { Icon(Icons.Filled.Email, null) },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        TextField(
            value = contrasena, onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { verClave = !verClave }) {
                    Icon(if (verClave) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                }
            },
            visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(correo, contrasena) },
            enabled = !cargando && correo.isNotBlank() && contrasena.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            if (cargando) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            else Text("Ingresar", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate(Routes.REGISTER) }) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}