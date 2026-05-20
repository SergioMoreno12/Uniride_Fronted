package com.example.uniride.Screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AdminViewModel
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ui.theme.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavController,
    adminViewModel: AdminViewModel = viewModel(),
    authViewModel: AuthViewModel   = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Administrador") },
                actions = {
                    IconButton(onClick = {
                        // Fix: cerrar sesión correctamente
                        authViewModel.cerrarSesion()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Filled.Logout, "Cerrar sesión")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Gestión del sistema", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            AdminMenuItem(Icons.Filled.People,     "Gestionar usuarios",
                "Ver, editar y administrar usuarios") {
                navController.navigate(Routes.ADMIN_USUARIOS)
            }
            AdminMenuItem(Icons.Filled.DirectionsCar, "Gestionar viajes",
                "Ver y administrar todos los viajes") {
                navController.navigate(Routes.ADMIN_VIAJES)
            }
            AdminMenuItem(Icons.Filled.CarRental,  "Gestionar vehículos",
                "Ver todos los vehículos registrados") {
                navController.navigate(Routes.ADMIN_VEHICULOS)
            }
            AdminMenuItem(Icons.Filled.School,     "Gestionar sedes",
                "Crear y eliminar sedes de la Udec") {
                navController.navigate(Routes.ADMIN_SEDES)
            }
            AdminMenuItem(Icons.Filled.BarChart,   "Estadísticas",
                "Resumen del sistema") {
                navController.navigate(Routes.ADMIN_ESTADISTICAS)
            }
            AdminMenuItem(Icons.Filled.Report,     "Reportes",
                "Ver reportes de usuarios") {
                navController.navigate(Routes.ADMIN_REPORTES)
            }
            AdminMenuItem(Icons.Filled.Notifications, "Notificaciones masivas",
                "Enviar notificaciones a todos") {
                navController.navigate(Routes.ADMIN_NOTIFICACIONES)
            }

            Spacer(Modifier.height(8.dp))

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
        }
    }
}

@Composable
private fun AdminMenuItem(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)) {
        Row(modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.Filled.ChevronRight, null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        }
    }
}