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
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ReservaViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.model.Viaje
import com.example.uniride.ui.theme.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisViajesScreen(
    navController: NavController,
    authViewModel: AuthViewModel       = viewModel(),
    viajeViewModel: ViajeViewModel     = viewModel(),
    reservaViewModel: ReservaViewModel = viewModel()
) {
    val sesion    = authViewModel.sesionActual
    val misViajes by viajeViewModel.misViajes.observeAsState(emptyList())
    val cargando  by viajeViewModel.cargando.observeAsState(false)
    val mensaje   by viajeViewModel.mensaje.observeAsState(null)
    val context   = LocalContext.current
    val scope     = rememberCoroutineScope()

    var viajeACancelar by remember { mutableStateOf<Viaje?>(null) }
    var viajeAEditar   by remember { mutableStateOf<Viaje?>(null) }
    var editOrigen     by remember { mutableStateOf("") }
    var editDestino    by remember { mutableStateOf("") }
    var editFecha      by remember { mutableStateOf("") }
    var editCosto      by remember { mutableStateOf("") }

    suspend fun cargarViajes() {
        sesion?.idUsuario?.let { idUsuario ->
            try {
                val vehiculos = RetrofitClient.apiService.vehiculosPorUsuario(idUsuario)
                vehiculos.firstOrNull()?.let { vehiculo ->
                    viajeViewModel.cargarMisViajes(vehiculo.idVehiculo)
                }
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(true) { cargarViajes() }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viajeViewModel.limpiarMensaje()
            // Recargar lista si se canceló o editó
            if (it.contains("cancelado") || it.contains("actualizado")) {
                cargarViajes()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Viajes publicados") },
                actions = {
                    // Botón historial
                    IconButton(onClick = {
                        navController.navigate(Routes.HISTORIAL_VIAJES)
                    }) {
                        Icon(Icons.Filled.History, "Historial")
                    }
                    // Botón recargar
                    IconButton(onClick = {
                        scope.launch { cargarViajes() }
                    }) {
                        Icon(Icons.Filled.Refresh, "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            BottomNavBar(currentRoute = "mis_viajes", rol = "conductor") { route ->
                navController.navigate(route) {
                    popUpTo(Routes.HOME) { saveState = true }
                    launchSingleTop = true; restoreState = true
                }
            }
        }
    ) { padding ->
        if (cargando) {
            Box(Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val lista = (misViajes ?: emptyList())
                .filter { it.estado != "cancelado" } // solo activos
                .sortedByDescending { it.fechaHora }  // más recientes primero

            if (lista.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.DirectionsCar, null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Spacer(Modifier.height(8.dp))
                        Text("No tienes viajes publicados",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate(Routes.PUBLICAR) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Add, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Publicar viaje")
                        }
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
                    lista.forEach { viaje ->
                        // ── Cada viaje es clickable para ver el detalle ──
                        Card(
                            onClick = {
                                // Al tocar la tarjeta → ViajeActivoDetalleScreen
                                navController.navigate("viaje_activo/${viaje.idViaje}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                // ── Encabezado ─────────────────────────────
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "${viaje.origen} → ${viaje.sede?.nombreSede ?: viaje.destino}",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        // Fecha y hora separadas
                                        Text(
                                            "📅 ${viaje.fechaHora.take(10)}  " +
                                                    "🕐 ${if (viaje.fechaHora.length >= 16)
                                                        viaje.fechaHora.substring(11, 16) else "--:--"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        if (!viaje.horaLlegada.isNullOrBlank()) {
                                            Text(
                                                "🏁 Llegada: ${if (viaje.horaLlegada.length >= 16)
                                                    viaje.horaLlegada.substring(11, 16) else "--:--"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                            )
                                        }
                                        Text(
                                            "$ ${"%.0f".format(viaje.costo)} · ${viaje.vehiculo?.capacidad} puestos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        AssistChip(
                                            onClick = {},
                                            label = {
                                                Text(viaje.estado.replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.labelSmall)
                                            }
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Icon(Icons.Filled.ChevronRight, "Ver detalle",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))

                                // ── Botones de acción (no navegan al detalle) ───
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (viaje.estado == "disponible" || viaje.estado == "lleno") {
                                        // Botón editar
                                        OutlinedButton(
                                            onClick = {
                                                viajeAEditar = viaje
                                                editOrigen  = viaje.origen
                                                editDestino = viaje.destino
                                                editFecha   = viaje.fechaHora
                                                editCosto   = viaje.costo.toString()
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.Edit, null,
                                                modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Editar",
                                                style = MaterialTheme.typography.labelSmall)
                                        }
                                        // Botón cancelar
                                        OutlinedButton(
                                            onClick = { viajeACancelar = viaje },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Icon(Icons.Filled.Cancel, null,
                                                modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Cancelar",
                                                style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                // Hint para tap
                                Text(
                                    "Toca para ver pasajeros y detalles",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // ── Dialog cancelar viaje ─────────────────────────────────────────
    viajeACancelar?.let { v ->
        AlertDialog(
            onDismissRequest = { viajeACancelar = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Cancelar viaje") },
            text = {
                Text("¿Cancelar el viaje de ${v.origen} a " +
                        "${v.sede?.nombreSede ?: v.destino} " +
                        "del ${v.fechaHora.take(10)}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viajeViewModel.cancelarViaje(v.idViaje)
                        viajeACancelar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sí, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { viajeACancelar = null }) { Text("No, volver") }
            }
        )
    }

    // ── Dialog editar viaje ───────────────────────────────────────────
    viajeAEditar?.let { v ->
        AlertDialog(
            onDismissRequest = { viajeAEditar = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Editar viaje") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextField(
                        value = editOrigen, onValueChange = { editOrigen = it },
                        label = { Text("Origen") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = editDestino, onValueChange = { editDestino = it },
                        label = { Text("Destino") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = editFecha, onValueChange = { editFecha = it },
                        label = { Text("Fecha hora (yyyy-MM-ddTHH:mm:ss)") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = editCosto, onValueChange = { editCosto = it },
                        label = { Text("Costo") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viajeViewModel.editarViaje(
                        v.idViaje, editOrigen, editDestino, editFecha,
                        editCosto.toDoubleOrNull() ?: v.costo,
                        v.sede?.idSede ?: 0L
                    )
                    viajeAEditar = null
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { viajeAEditar = null }) { Text("Cancelar") }
            }
        )
    }
}