package com.example.uniride.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ui.theme.Routes

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    var nombre     by remember { mutableStateOf("") }
    var correo     by remember { mutableStateOf("") }
    var telefono   by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rol        by remember { mutableStateOf("pasajero") }

    val cargando by viewModel.cargando.observeAsState(false)
    val mensaje  by viewModel.mensaje.observeAsState(null)
    val context = LocalContext.current

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Text("Crear cuenta",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)

        Text("Universidad de Cundinamarca",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

        Spacer(Modifier.height(32.dp))

        // ── Campos de texto ───────────────────────────────────────────
        TextField(
            value = nombre, onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            leadingIcon = { Icon(Icons.Filled.Person, null) },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(12.dp))

        TextField(
            value = correo, onValueChange = { correo = it },
            label = { Text("Correo institucional") },
            leadingIcon = { Icon(Icons.Filled.Email, null) },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(12.dp))

        TextField(
            value = telefono, onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            leadingIcon = { Icon(Icons.Filled.Phone, null) },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(12.dp))

        TextField(
            value = contrasena, onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(20.dp))

        // ── Selector de rol ────────────────────────────────────────────
        Text("¿Cómo vas a usar la app?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start))

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pasajero
            Card(
                onClick = { rol = "pasajero" },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (rol == "pasajero")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                ),
                border = if (rol == "pasajero")
                    androidx.compose.foundation.BorderStroke(
                        2.dp, MaterialTheme.colorScheme.primary)
                else
                    androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Person, null,
                        tint = if (rol == "pasajero")
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Pasajero",
                        fontWeight = if (rol == "pasajero") FontWeight.Bold else FontWeight.Normal,
                        color = if (rol == "pasajero")
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium)
                    Text("Busca y reserva viajes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            // Conductor
            Card(
                onClick = { rol = "conductor" },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (rol == "conductor")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                ),
                border = if (rol == "conductor")
                    androidx.compose.foundation.BorderStroke(
                        2.dp, MaterialTheme.colorScheme.primary)
                else
                    androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.DirectionsCar, null,
                        tint = if (rol == "conductor")
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Conductor",
                        fontWeight = if (rol == "conductor") FontWeight.Bold else FontWeight.Normal,
                        color = if (rol == "conductor")
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium)
                    Text("Publica y ofrece viajes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.registrar(nombre, correo, contrasena, telefono, rol) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            },
            enabled = !cargando && nombre.isNotBlank()
                    && correo.isNotBlank() && contrasena.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (cargando)
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            else
                Text("Registrarme", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = { navController.popBackStack() }) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }

        Spacer(Modifier.height(24.dp))
    }
}