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
import com.example.uniride.ViewModel.AdminViewModel
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ui.theme.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController:  NavController,
    adminViewModel: AdminViewModel = viewModel(),
    authViewModel:  AuthViewModel  = viewModel()
) {
    val sesion    = authViewModel.sesionActual
    val usuarios  by adminViewModel.usuarios.observeAsState(emptyList())
    val viajes    by adminViewModel.viajes.observeAsState(emptyList())
    val reservas  by adminViewModel.reservas.observeAsState(emptyList())
    val vehiculos by adminViewModel.vehiculos.observeAsState(emptyList())

    LaunchedEffect(true) { adminViewModel.cargarTodo() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panel Administrador", fontWeight = FontWeight.Bold)
                        Text("Universidad de Cundinamarca",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Bienvenida ──────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier         = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.AdminPanelSettings, null,
                            tint     = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Bienvenido, Admin",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(sesion?.nombre ?: "Administrador",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                .copy(alpha = 0.7f))
                    }
                }
            }

            // ── Estadísticas rápidas ────────────────────────────────────
            Text("Resumen del sistema",
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary)

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EstadisticaChip(Icons.Filled.People,
                    "${(usuarios ?: emptyList()).size}", "Usuarios",
                    Modifier.weight(1f))
                EstadisticaChip(Icons.Filled.DirectionsCar,
                    "${(viajes ?: emptyList()).size}", "Viajes",
                    Modifier.weight(1f))
                EstadisticaChip(Icons.Filled.BookOnline,
                    "${(reservas ?: emptyList()).size}", "Reservas",
                    Modifier.weight(1f))
                EstadisticaChip(Icons.Filled.CarRental,
                    "${(vehiculos ?: emptyList()).size}", "Vehículos",
                    Modifier.weight(1f))
            }

            // ── Menú de gestión ─────────────────────────────────────────
            Text("Gestión del sistema",
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary)

            AdminMenuItem(Icons.Filled.People,
                "Gestionar usuarios", "Ver, editar y administrar cuentas") {
                navController.navigate(Routes.ADMIN_USUARIOS)
            }
            AdminMenuItem(Icons.Filled.DirectionsCar,
                "Gestionar viajes", "Ver y administrar todos los viajes") {
                navController.navigate(Routes.ADMIN_VIAJES)
            }
            AdminMenuItem(Icons.Filled.CarRental,
                "Gestionar vehículos", "Ver los vehículos registrados") {
                navController.navigate(Routes.ADMIN_VEHICULOS)
            }
            AdminMenuItem(Icons.Filled.School,
                "Gestionar sedes", "Crear y eliminar sedes de la Udec") {
                navController.navigate(Routes.ADMIN_SEDES)
            }
            AdminMenuItem(Icons.Filled.BarChart,
                "Estadísticas", "Resumen detallado del sistema") {
                navController.navigate(Routes.ADMIN_ESTADISTICAS)
            }
            AdminMenuItem(Icons.Filled.Report,
                "Reportes", "Gestionar reportes de usuarios") {
                navController.navigate(Routes.ADMIN_REPORTES)
            }
            AdminMenuItem(Icons.Filled.Notifications,
                "Notificaciones masivas",
                "Enviar mensajes a conductores y pasajeros") {
                navController.navigate(Routes.ADMIN_NOTIFICACIONES)
            }

            Spacer(Modifier.height(4.dp))

            // ✅ Único botón de cerrar sesión, sin duplicado en topBar
            OutlinedButton(
                onClick  = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EstadisticaChip(
    icono:    ImageVector,
    valor:    String,
    label:    String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier            = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icono, null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(valor,
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp,
                color      = MaterialTheme.colorScheme.primary)
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun AdminMenuItem(
    icon:      ImageVector,
    titulo:    String,
    subtitulo: String,
    onClick:   () -> Unit
) {
    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold)
                Text(subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.Filled.ChevronRight, null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        }
    }
}