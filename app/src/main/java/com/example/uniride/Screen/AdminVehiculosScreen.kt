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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AdminViewModel
import com.example.uniride.model.Vehiculo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVehiculosScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val vehiculos by viewModel.vehiculos.observeAsState(emptyList())
    val cargando  by viewModel.cargando.observeAsState(false)
    val mensaje   by viewModel.mensaje.observeAsState(null)
    val context = LocalContext.current
    var vehiculoAEliminar by remember { mutableStateOf<Vehiculo?>(null) }

    LaunchedEffect(true) { viewModel.cargarVehiculos() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar vehículos") },
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
            isRefreshing = cargando,                          // estado del adminViewModel
            onRefresh    = { viewModel.cargarVehiculos() },    // método de recarga
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            Text("${(vehiculos ?: emptyList()).size} vehículos registrados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))

            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (vehiculos ?: emptyList()).forEach { vehiculo ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Row(modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DirectionsCar, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${vehiculo.marca} ${vehiculo.modelo}",
                                        fontWeight = FontWeight.SemiBold)
                                    Text("Placa: ${vehiculo.placa} · ${vehiculo.capacidad} puestos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text("Conductor: ${vehiculo.usuario?.nombre ?: "-"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { vehiculoAEliminar = vehiculo }) {
                                    Icon(Icons.Filled.Delete, null,
                                        tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    vehiculoAEliminar?.let { v ->
        AlertDialog(
            onDismissRequest = { vehiculoAEliminar = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Eliminar vehículo") },
            text = { Text("¿Eliminar el vehículo ${v.marca} ${v.modelo} con placa ${v.placa}?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminarVehiculo(v.idVehiculo); vehiculoAEliminar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { vehiculoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}
}