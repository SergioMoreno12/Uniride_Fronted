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
    val sesion        = authViewModel.sesionActual
    val misViajes     by viajeViewModel.misViajes.observeAsState(emptyList())
    val cargando      by viajeViewModel.cargando.observeAsState(false)
    val mensaje       by viajeViewModel.mensaje.observeAsState(null)
    val reservasViaje by reservaViewModel.reservasViaje.observeAsState(emptyList())
    val context       = LocalContext.current
    val scope         = rememberCoroutineScope()

    var viajeExpandido by remember { mutableStateOf<Long?>(null) }
    var viajeACancelar by remember { mutableStateOf<Viaje?>(null) }
    var viajeAEditar   by remember { mutableStateOf<Viaje?>(null) }
    var editOrigen     by remember { mutableStateOf("") }
    var editDestino    by remember { mutableStateOf("") }
    var editFecha      by remember { mutableStateOf("") }
    var editCosto      by remember { mutableStateOf("") }

    // Función reutilizable para cargar mis viajes
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

    LaunchedEffect(true) {
        cargarViajes()
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viajeViewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Viajes publicados") },
                actions = {
                    // Botón historial de viajes
                    IconButton(onClick = {
                        navController.navigate(Routes.HISTORIAL_VIAJES)
                    }) {
                        Icon(Icons.Filled.History, "Historial")
                    }
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
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val lista = misViajes ?: emptyList()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (lista.isEmpty()) {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.DirectionsCar, null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            Spacer(Modifier.height(8.dp))
                            Text("No has publicado viajes aún",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    lista.forEach { viaje ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                // ── Encabezado ────────────────────────────
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
                                        Text(
                                            viaje.fechaHora.take(16).replace("T", " "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            "$ ${"%.0f".format(viaje.costo)} · ${viaje.vehiculo?.capacidad} puestos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    AssistChip(onClick = {}, label = {
                                        Text(viaje.estado,
                                            style = MaterialTheme.typography.labelSmall)
                                    })
                                }

                                Spacer(Modifier.height(8.dp))

                                // ── Botones de acción ─────────────────────
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viajeExpandido =
                                                if (viajeExpandido == viaje.idViaje) null
                                                else {
                                                    reservaViewModel.cargarReservasViaje(viaje.idViaje)
                                                    viaje.idViaje
                                                }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            if (viajeExpandido == viaje.idViaje)
                                                Icons.Filled.ExpandLess
                                            else Icons.Filled.Group,
                                            null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Pasajeros",
                                            style = MaterialTheme.typography.labelSmall)
                                    }

                                    if (viaje.estado == "disponible") {
                                        IconButton(onClick = {
                                            viajeAEditar = viaje
                                            editOrigen  = viaje.origen
                                            editDestino = viaje.destino
                                            editFecha   = viaje.fechaHora
                                            editCosto   = viaje.costo.toString()
                                        }) {
                                            Icon(Icons.Filled.Edit, "Editar",
                                                tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            viajeACancelar = viaje
                                        }) {
                                            Icon(Icons.Filled.Cancel, "Cancelar",
                                                tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                // ── Lista de pasajeros expandida ──────────
                                if (viajeExpandido == viaje.idViaje) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    val pasajeros = reservasViaje ?: emptyList()
                                    Text(
                                        "Pasajeros (${pasajeros.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(6.dp))

                                    if (pasajeros.isEmpty()) {
                                        Text("Sin reservas aún",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    } else {
                                        pasajeros.forEach { reserva ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        reserva.usuario?.nombre ?: "-",
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.Phone, null,
                                                            modifier = Modifier.size(13.dp),
                                                            tint = MaterialTheme.colorScheme.primary)
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                            reserva.usuario?.telefono ?: "Sin teléfono",
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }

                                                if (!reserva.confirmada) {
                                                    IconButton(onClick = {
                                                        reservaViewModel.confirmarReserva(reserva.idReserva)
                                                    }) {
                                                        Icon(
                                                            Icons.Filled.CheckCircle,
                                                            "Confirmar reserva",
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                } else {
                                                    AssistChip(onClick = {}, label = {
                                                        Text("Confirmado",
                                                            style = MaterialTheme.typography.labelSmall)
                                                    })
                                                }
                                            }
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
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
                Text("¿Cancelar el viaje de ${v.origen} a ${v.sede?.nombreSede ?: v.destino}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viajeViewModel.cancelarViaje(v.idViaje)
                        viajeACancelar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Cancelar viaje") }
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
                        label = { Text("Origen") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = editDestino, onValueChange = { editDestino = it },
                        label = { Text("Destino") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = editFecha, onValueChange = { editFecha = it },
                        label = { Text("Fecha hora (yyyy-MM-ddTHH:mm:ss)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = editCosto, onValueChange = { editCosto = it },
                        label = { Text("Costo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viajeViewModel.editarViaje(
                        v.idViaje,
                        editOrigen,
                        editDestino,
                        editFecha,
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