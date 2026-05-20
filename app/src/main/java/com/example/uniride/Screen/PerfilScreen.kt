package com.example.uniride.Screen

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.PerfilViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.ui.theme.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    authViewModel: AuthViewModel     = viewModel(),
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val sesion        = authViewModel.sesionActual
    val perfilUsuario by perfilViewModel.perfilUsuario.observeAsState(null)
    val scope         = rememberCoroutineScope()
    var promedio      by remember { mutableStateOf(0.0) }

    LaunchedEffect(sesion?.idUsuario) {
        sesion?.idUsuario?.let { id ->
            perfilViewModel.cargarPerfil(id)
            if (sesion.rol == "conductor") {
                scope.launch {
                    try {
                        val vehiculos = RetrofitClient.apiService.vehiculosPorUsuario(id)
                        vehiculos.firstOrNull()?.let { v ->
                            promedio = RetrofitClient.apiService.promedioConductor(id)
                        }
                    } catch (e: Exception) { }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.AJUSTES) }) {
                        Icon(Icons.Filled.Settings, "Ajustes")
                    }
                },
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

            // Avatar
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    perfilUsuario?.nombre?.first()?.uppercaseChar()?.toString()
                        ?: sesion?.nombre?.first()?.uppercaseChar()?.toString() ?: "U",
                    fontSize = 36.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                perfilUsuario?.nombre ?: sesion?.nombre ?: "Usuario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                when (perfilUsuario?.rol ?: sesion?.rol) {
                    "conductor"     -> "Conductor"
                    "administrador" -> "Administrador"
                    else            -> "Pasajero"
                },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            // Estrellas promedio para conductores
            if ((perfilUsuario?.rol ?: sesion?.rol) == "conductor" && promedio > 0) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    (1..5).forEach { i ->
                        Icon(
                            if (i <= promedio.toInt()) Icons.Filled.Star else Icons.Filled.StarBorder,
                            null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("${"%.1f".format(promedio)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Datos del usuario
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    InfoRow(Icons.Filled.Person,  "Nombre",   perfilUsuario?.nombre ?: "-")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    InfoRow(Icons.Filled.Email,   "Correo",   perfilUsuario?.correo ?: "-")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    InfoRow(Icons.Filled.Phone,   "Teléfono", perfilUsuario?.telefono ?: "No registrado")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    InfoRow(Icons.Filled.Badge,   "Rol",
                        when (perfilUsuario?.rol) {
                            "conductor"     -> "Conductor"
                            "administrador" -> "Administrador"
                            else            -> "Pasajero"
                        })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    InfoRow(Icons.Filled.CalendarToday, "Miembro desde",
                        perfilUsuario?.fechaRegistro?.take(10) ?: "-")
                }
            }

            Spacer(Modifier.height(20.dp))

            // Botones de acción
            Button(
                onClick = { navController.navigate(Routes.EDITAR_PERFIL) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Edit, null)
                Spacer(Modifier.width(8.dp))
                Text("Editar perfil", fontWeight = FontWeight.Bold)
            }

            if ((perfilUsuario?.rol ?: sesion?.rol) == "conductor") {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { navController.navigate(Routes.MI_VEHICULO) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.DirectionsCar, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Mi vehículo", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
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

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}