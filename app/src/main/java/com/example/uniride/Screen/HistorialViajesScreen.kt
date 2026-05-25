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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Viaje
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialViajesScreen(
    navController: NavController,
    authViewModel: AuthViewModel   = viewModel(),
    viajeViewModel: ViajeViewModel = viewModel()
) {
    val sesion = authViewModel.sesionActual
    val scope  = rememberCoroutineScope()
    val ahora  = LocalDateTime.now()

    var historial by remember { mutableStateOf<List<Viaje>>(emptyList()) }
    var cargando  by remember { mutableStateOf(true) }

    suspend fun cargar() {
        cargando = true
        try {
            sesion?.idUsuario?.let { idUsuario ->
                val vehiculos = RetrofitClient.apiService.vehiculosPorUsuario(idUsuario)
                vehiculos.firstOrNull()?.let { vehiculo ->
                    val todos = RetrofitClient.apiService.viajesPorVehiculo(vehiculo.idVehiculo)
                    // ✅ Compara fecha+hora completas, no solo fecha
                    historial = todos.filter { viaje ->
                        val dt = try {
                            val raw = viaje.fechaHora
                            LocalDateTime.parse(raw.substring(0, minOf(19, raw.length)))
                        } catch (e: Exception) { null }
                        // También incluye viajes con estado "completado" o "cancelado"
                        // aunque su fecha sea futura (ya cerraron)
                        (dt != null && dt.isBefore(ahora)) ||
                                viaje.estado == "completado" ||
                                viaje.estado == "cancelado"
                    }.sortedByDescending { it.fechaHora }
                }
            }
        } catch (e: Exception) { }
        cargando = false
    }

    LaunchedEffect(true) { scope.launch { cargar() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de viajes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { cargar() } }) {
                        Icon(Icons.Filled.Refresh, "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (historial.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.History, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("Aún no tienes viajes completados",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("${historial.size} viajes completados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                historial.forEach { viaje ->
                    Card(
                        onClick = {
                            navController.navigate("viaje_activo/${viaje.idViaje}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${viaje.origen} → ${viaje.sede?.nombreSede ?: viaje.destino}",
                                        fontWeight = FontWeight.SemiBold)
                                    Text(viaje.fechaHora.take(16).replace("T", " "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(viaje.estado.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelSmall)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            when (viaje.estado) {
                                                "completado" -> Icons.Filled.CheckCircle
                                                "cancelado"  -> Icons.Filled.Cancel
                                                else         -> Icons.Filled.History
                                            },
                                            null,
                                            modifier = Modifier.size(14.dp),
                                            tint = when (viaje.estado) {
                                                "completado" -> MaterialTheme.colorScheme.secondary
                                                "cancelado"  -> MaterialTheme.colorScheme.error
                                                else         -> MaterialTheme.colorScheme.primary
                                            })
                                    }
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DirectionsCar, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${viaje.vehiculo?.marca} ${viaje.vehiculo?.modelo}",
                                    style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.width(12.dp))
                                Icon(Icons.Filled.AttachMoney, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp))
                                Text("${"%.0f".format(viaje.costo)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}