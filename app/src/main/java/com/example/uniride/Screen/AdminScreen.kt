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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AdminViewModel
import com.example.uniride.ui.theme.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavController,
    adminViewModel: AdminViewModel = viewModel()
) {
    val mensaje by adminViewModel.mensaje.observeAsState(null)
    val context = LocalContext.current

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            adminViewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de administración") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) { Icon(Icons.Filled.Logout, "Salir") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer)
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
            // Bienvenida
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AdminPanelSettings, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Bienvenido, Administrador",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Text("Universidad de Cundinamarca",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Text("Gestión", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            AdminMenuCard(
                icon = Icons.Filled.People,
                titulo = "Gestionar usuarios",
                descripcion = "Ver, cambiar rol o eliminar cuentas de estudiantes",
                onClick = { navController.navigate(Routes.ADMIN_USUARIOS) }
            )
            AdminMenuCard(
                icon = Icons.Filled.DirectionsCar,
                titulo = "Gestionar viajes",
                descripcion = "Supervisar y eliminar viajes del sistema",
                onClick = { navController.navigate(Routes.ADMIN_VIAJES) }
            )
            AdminMenuCard(
                icon = Icons.Filled.CarRental,
                titulo = "Gestionar vehículos",
                descripcion = "Ver y eliminar vehículos registrados",
                onClick = { navController.navigate(Routes.ADMIN_VEHICULOS) }
            )
            AdminMenuCard(
                icon = Icons.Filled.School,
                titulo = "Gestionar sedes",
                descripcion = "Crear y eliminar sedes de la Udec",
                onClick = { navController.navigate(Routes.ADMIN_SEDES) }
            )

            Text("Reportes", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            AdminMenuCard(
                icon = Icons.Filled.BarChart,
                titulo = "Estadísticas de uso",
                descripcion = "Viajes, reservas y usuarios registrados",
                onClick = { navController.navigate(Routes.ADMIN_ESTADISTICAS) }
            )
            AdminMenuCard(
                icon = Icons.Filled.Report,
                titulo = "Reportes de conducta",
                descripcion = "Revisar quejas entre usuarios",
                onClick = { navController.navigate(Routes.ADMIN_REPORTES) }
            )
            AdminMenuCard(
                icon = Icons.Filled.Notifications,
                titulo = "Enviar notificaciones",
                descripcion = "Comunicados a todos los estudiantes",
                onClick = { navController.navigate(Routes.ADMIN_NOTIFICACIONES) }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun AdminMenuCard(icon: ImageVector, titulo: String, descripcion: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold)
                Text(descripcion, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.Filled.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}