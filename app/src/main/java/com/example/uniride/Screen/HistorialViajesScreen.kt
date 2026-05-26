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
                    historial = todos.filter { v ->
                        val dt = try {
                            LocalDateTime.parse(
                                v.fechaHora.substring(0, minOf(19, v.fechaHora.length)))
                        } catch (e: Exception) { null }
                        (dt != null && dt.isBefore(ahora)) ||
                                v.estado == "completado" ||
                                v.estado == "cancelado"
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        RefreshableContent(
            isRefreshing = cargando,
            onRefresh    = { scope.launch { cargar() } },
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
            if (historial.isEmpty()) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Spacer(Modifier.height(180.dp))
                    Icon(Icons.Filled.History, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Text("Aún no tienes viajes completados",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            } else {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${historial.size} viaje${if (historial.size > 1) "s" else ""} en historial",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                    historial.forEach { viaje ->
                        val estadoMostrado = when {
                            viaje.estado == "cancelado" -> "cancelado"
                            else -> "completado"
                        }
                        Card(
                            onClick = { navController.navigate("viaje_activo/${viaje.idViaje}") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${viaje.origen} → ${viaje.destino}",
                                            fontWeight = FontWeight.SemiBold)
                                        Text(viaje.fechaHora.take(16).replace("T", " "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    Icon(
                                        if (estadoMostrado == "cancelado") Icons.Filled.Cancel
                                        else Icons.Filled.CheckCircle,
                                        null,
                                        tint = if (estadoMostrado == "cancelado")
                                            MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.height(6.dp))
                                Text("Toca para ver pasajeros y comentarios",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}