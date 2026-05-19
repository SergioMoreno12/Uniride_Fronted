package com.example.uniride.Screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.PerfilViewModel
import com.example.uniride.ui.theme.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    authViewModel: AuthViewModel     = viewModel(),
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val sesion        = authViewModel.sesionActual
    val cargando      by perfilViewModel.cargando.observeAsState(false)
    val mensaje       by perfilViewModel.mensaje.observeAsState(null)
    val perfilUsuario by perfilViewModel.perfilUsuario.observeAsState(null)
    val context       = LocalContext.current

    var nombre          by remember { mutableStateOf("") }
    var telefono        by remember { mutableStateOf("") }
    var claveActual     by remember { mutableStateOf("") }
    var claveNueva      by remember { mutableStateOf("") }
    var tabSeleccionada by remember { mutableIntStateOf(0) }

    // Cargar perfil completo al abrir la pantalla
    LaunchedEffect(sesion?.idUsuario) {
        sesion?.idUsuario?.let { perfilViewModel.cargarPerfil(it) }
    }

    // Cuando llega el perfil desde el backend, inicializa los campos
    LaunchedEffect(perfilUsuario) {
        perfilUsuario?.let {
            nombre   = it.nombre
            telefono = it.telefono ?: ""
        }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            perfilViewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            BottomNavBar(currentRoute = "perfil", rol = sesion?.rol ?: "usuario") { route ->
                navController.navigate(route) {
                    popUpTo(Routes.HOME) { saveState = true }
                    launchSingleTop = true; restoreState = true
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Avatar ─────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                nombre.ifBlank { sesion?.nombre ?: "Usuario" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                when (sesion?.rol) {
                    "conductor"     -> "Conductor"
                    "administrador" -> "Administrador"
                    else            -> "Pasajero"
                },
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(20.dp))

            // ── Tabs ───────────────────────────────────────────────────────
            TabRow(selectedTabIndex = tabSeleccionada) {
                Tab(
                    selected = tabSeleccionada == 0,
                    onClick  = { tabSeleccionada = 0 },
                    text     = { Text("Perfil") }
                )
                Tab(
                    selected = tabSeleccionada == 1,
                    onClick  = { tabSeleccionada = 1 },
                    text     = { Text("Contraseña") }
                )
            }

            Spacer(Modifier.height(20.dp))

            when (tabSeleccionada) {

                // ── Tab Perfil ─────────────────────────────────────────────
                0 -> {
                    if (cargando && perfilUsuario == null) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        TextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre completo") },
                            leadingIcon = { Icon(Icons.Filled.Person, null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        TextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono") },
                            leadingIcon = { Icon(Icons.Filled.Phone, null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                sesion?.idUsuario?.let {
                                    perfilViewModel.actualizarPerfil(it, nombre, telefono)
                                }
                            },
                            enabled = !cargando && nombre.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (cargando)
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            else Text("Guardar cambios", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ── Tab Contraseña ─────────────────────────────────────────
                1 -> {
                    TextField(
                        value = claveActual,
                        onValueChange = { claveActual = it },
                        label = { Text("Contraseña actual") },
                        leadingIcon = { Icon(Icons.Filled.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = claveNueva,
                        onValueChange = { claveNueva = it },
                        label = { Text("Nueva contraseña") },
                        leadingIcon = { Icon(Icons.Filled.LockOpen, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            sesion?.idUsuario?.let {
                                perfilViewModel.cambiarContrasena(it, claveActual, claveNueva)
                                claveActual = ""
                                claveNueva  = ""
                            }
                        },
                        enabled = !cargando && claveActual.isNotBlank() && claveNueva.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (cargando)
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        else Text("Cambiar contraseña", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Botón Mis vehículos (solo conductores) ─────────────────────
            if (sesion?.rol == "conductor") {
                OutlinedButton(
                    onClick = { navController.navigate(Routes.REGISTRAR_VEHICULO) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.DirectionsCar, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Mis vehículos", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Cerrar sesión ──────────────────────────────────────────────
            OutlinedButton(
                onClick = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}