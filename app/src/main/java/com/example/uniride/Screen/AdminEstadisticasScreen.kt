package com.example.uniride.Screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEstadisticasScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val usuarios  by viewModel.usuarios.observeAsState(emptyList())
    val viajes    by viewModel.viajes.observeAsState(emptyList())
    val vehiculos by viewModel.vehiculos.observeAsState(emptyList())
    val reservas  by viewModel.reservas.observeAsState(emptyList())
    val cargando  by viewModel.cargando.observeAsState(false)

    LaunchedEffect(true) {
        viewModel.cargarUsuarios()
        viewModel.cargarViajes()
        viewModel.cargarVehiculos()
        viewModel.cargarReservas()
    }

    val totalUsuarios   = (usuarios ?: emptyList()).size
    val totalConductores = (usuarios ?: emptyList()).count { it.rol == "conductor" }
    val totalPasajeros  = (usuarios ?: emptyList()).count { it.rol == "pasajero" }
    val totalViajes     = (viajes ?: emptyList()).size
    val viajesDisp      = (viajes ?: emptyList()).count { it.estado == "disponible" }
    val viajesLlenos    = (viajes ?: emptyList()).count { it.estado == "lleno" }
    val totalVehiculos  = (vehiculos ?: emptyList()).size
    val totalReservas   = (reservas ?: emptyList()).size
    val reservasConf    = (reservas ?: emptyList()).count { it.confirmada }

    // Sedes con más viajes
    val viajesPorSede = (viajes ?: emptyList())
        .groupBy { it.sede?.nombreSede ?: "Sin sede" }
        .mapValues { it.value.size }
        .entries.sortedByDescending { it.value }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas de uso") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.cargarUsuarios()
                        viewModel.cargarViajes()
                        viewModel.cargarVehiculos()
                        viewModel.cargarReservas()
                    }) { Icon(Icons.Filled.Refresh, null) }
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Usuarios
                Text("Usuarios", fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadCard(Icons.Filled.People, "Total", "$totalUsuarios",
                        modifier = Modifier.weight(1f))
                    EstadCard(Icons.Filled.DirectionsCar, "Conductores", "$totalConductores",
                        modifier = Modifier.weight(1f))
                    EstadCard(Icons.Filled.Person, "Pasajeros", "$totalPasajeros",
                        modifier = Modifier.weight(1f))
                }

                // Viajes
                Text("Viajes", fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadCard(Icons.Filled.Route, "Total", "$totalViajes",
                        modifier = Modifier.weight(1f))
                    EstadCard(Icons.Filled.CheckCircle, "Disponibles", "$viajesDisp",
                        modifier = Modifier.weight(1f))
                    EstadCard(Icons.Filled.Block, "Llenos", "$viajesLlenos",
                        modifier = Modifier.weight(1f))
                }

                // Reservas y vehiculos
                Text("Reservas y vehículos", fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadCard(Icons.Filled.BookOnline, "Reservas", "$totalReservas",
                        modifier = Modifier.weight(1f))
                    EstadCard(Icons.Filled.CheckCircle, "Confirmadas", "$reservasConf",
                        modifier = Modifier.weight(1f))
                    EstadCard(Icons.Filled.DirectionsCar, "Vehículos", "$totalVehiculos",
                        modifier = Modifier.weight(1f))
                }

                // Viajes por sede
                if (viajesPorSede.isNotEmpty()) {
                    Text("Viajes por sede", fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            viajesPorSede.forEach { (sede, cantidad) ->
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.School, null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(sede, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text("$cantidad viajes",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                                if (sede != viajesPorSede.last().key) HorizontalDivider()
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EstadCard(icon: ImageVector, label: String, valor: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(valor, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}