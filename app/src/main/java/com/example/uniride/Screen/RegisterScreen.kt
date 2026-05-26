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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ui.theme.Routes

@OptIn(ExperimentalMaterial3Api::class)
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
    var verPass    by remember { mutableStateOf(false) }

    val context         = LocalContext.current

    val cargando    by viewModel.registroCargando.observeAsState(false)
    val registroOk  by viewModel.registroExito.observeAsState(false)
    val errorMsg    by viewModel.registroError.observeAsState(null)

    LaunchedEffect(registroOk) {
        if (registroOk) {
            Toast.makeText(context, "¡Cuenta creada! Inicia sesión", Toast.LENGTH_SHORT).show()
            viewModel.limpiarRegistro()
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.REGISTER) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Únete a UniRide",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Completa tus datos para registrarte",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(4.dp))

            // ── Nombre ──────────────────────────────────────────────────
            TextField(
                value = nombre, onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = nombre.isBlank() && cargando
            )

            // ── Correo ──────────────────────────────────────────────────
            TextField(
                value = correo, onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Filled.Email, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = correo.isBlank() && cargando
            )

            // ── Teléfono ─────────────────────────────────────────────────
            TextField(
                value = telefono, onValueChange = { telefono = it },
                label = { Text("Teléfono (opcional)") },
                leadingIcon = { Icon(Icons.Filled.Phone, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // ── Contraseña ───────────────────────────────────────────────
            TextField(
                value = contrasena, onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { verPass = !verPass }) {
                        Icon(
                            if (verPass) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility, null
                        )
                    }
                },
                visualTransformation = if (verPass) VisualTransformation.None
                else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = contrasena.length < 6 && cargando,
                supportingText = {
                    if (contrasena.isNotEmpty() && contrasena.length < 6)
                        Text("Mínimo 6 caracteres", color = MaterialTheme.colorScheme.error)
                }
            )

            // ── Selector de rol ──────────────────────────────────────────
            Spacer(Modifier.height(4.dp))
            Text(
                "¿Cómo quieres usar UniRide?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Podrás cambiar tu rol en cualquier momento desde tu perfil.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )

            Spacer(Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RolCard(
                    seleccionado = rol == "pasajero",
                    icono        = Icons.Filled.Person,
                    titulo       = "Pasajero",
                    descripcion  = "Busca y reserva viajes publicados por conductores",
                    onClick      = { rol = "pasajero" },
                    modifier     = Modifier.weight(1f)
                )
                RolCard(
                    seleccionado = rol == "conductor",
                    icono        = Icons.Filled.DirectionsCar,
                    titulo       = "Conductor",
                    descripcion  = "Publica tus viajes y lleva pasajeros a la universidad",
                    onClick      = { rol = "conductor" },
                    modifier     = Modifier.weight(1f)
                )
            }

            // ── Error del ViewModel ───────────────────────────────────────
            errorMsg?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Botón registrar ───────────────────────────────────────────
            Button(
                onClick = {
                    // Ya NO se llama a la API directamente desde la pantalla
                    viewModel.register(
                        nombre     = nombre,
                        correo     = correo,
                        contrasena = contrasena,
                        telefono   = telefono.ifBlank { null },
                        rol        = rol
                    )
                },
                enabled  = !cargando,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color       = androidx.compose.ui.graphics.Color.White
                    )
                } else {
                    Icon(Icons.Filled.PersonAdd, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Crear cuenta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // ── Link a login ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿Ya tienes cuenta? ",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(
                        "Inicia sesión",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Componente reutilizable para las tarjetas de rol ──────────────────────────
@Composable
private fun RolCard(
    seleccionado: Boolean,
    icono:        androidx.compose.ui.graphics.vector.ImageVector,
    titulo:       String,
    descripcion:  String,
    onClick:      () -> Unit,
    modifier:     Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = if (seleccionado)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier            = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icono, null,
                tint = if (seleccionado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                titulo,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                color = if (seleccionado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                descripcion,
                style     = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}