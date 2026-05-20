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
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ReservaViewModel
import com.example.uniride.ViewModel.ViajeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajeDetalleScreen(
    idViaje: Long,
    navController: NavController,
    authViewModel: AuthViewModel       = viewModel(),
    reservaViewModel: ReservaViewModel = viewModel(),
    viajeViewModel: ViajeViewModel     = viewModel()
) {
    val sesion           = authViewModel.sesionActual
    val viajes           by viajeViewModel.viajes.observeAsState(emptyList())
    val viajesReservados by viajeViewModel.viajesReservados.observeAsState(emptySet())
    val viajesPropios    by viajeViewModel.viajesPropios.observeAsState(emptySet())
    val viaje            = (viajes ?: emptyList()).find { it.idViaje == idViaje }
    val cargando         by reservaViewModel.cargando.observeAsState(false)
    val mensaje          by reservaViewModel.mensaje.observeAsState(null)
    var showDialog       by remember { mutableStateOf(false) }
    val context          = LocalContext.current

    val yaReservo = idViaje in (viajesReservados ?: emptySet())
    val esPropio  = idViaje in (viajesPropios ?: emptySet())

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            reservaViewModel.limpiarMensaje()
            if (it.contains("éxito")) navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del viaje") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viajeViewModel.cargarDisponibles(sesion?.idUsuario)
                    }) {
                        Icon(Icons.Filled.Refresh, "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        viaje?.let { v ->
            val capacidad = v.vehiculo?.capacidad ?: 0
            val cupos     = v.cuposDisponibles ?: capacidad

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info del viaje
                Card(modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DetalleRow(Icons.Filled.LocationOn,   "Origen",       v.origen)
                        DetalleRow(Icons.Filled.School,       "Sede destino", v.sede?.nombreSede ?: v.destino)
                        DetalleRow(Icons.Filled.LocationCity, "Ciudad",       v.sede?.ciudad ?: "-")
                        DetalleRow(Icons.Filled.AccessTime,   "Fecha y hora", v.fechaHora.take(16).replace("T", " "))
                        DetalleRow(Icons.Filled.AttachMoney,  "Costo/puesto", "$ ${"%.0f".format(v.costo)}")

                        // Cupos disponibles destacado
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.EventSeat, null,
                                tint = if (cupos > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Cupos disponibles",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(
                                    if (cupos > 0) "$cupos de $capacidad cupos libres"
                                    else "Sin cupos disponibles",
                                    fontWeight = FontWeight.Bold,
                                    color = if (cupos > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Info del vehículo y conductor
                Card(modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Vehículo y conductor", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        DetalleRow(Icons.Filled.DirectionsCar,     "Vehículo",  "${v.vehiculo?.marca} ${v.vehiculo?.modelo}")
                        DetalleRow(Icons.Filled.ConfirmationNumber, "Placa",    v.vehiculo?.placa ?: "-")
                        DetalleRow(Icons.Filled.EventSeat,          "Puestos",  "$capacidad")
                        DetalleRow(Icons.Filled.Person,             "Conductor", v.vehiculo?.usuario?.nombre ?: "-")
                    }
                }

                // Mensaje de estado
                when {
                    esPropio -> {
                        Card(modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Info, null,
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Este es tu viaje publicado",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    yaReservo -> {
                        Card(modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Row(modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, null,
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Ya tienes una reserva en este viaje",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    else -> {
                        // Botón reservar
                        if (sesion?.rol != "administrador") {
                            Button(
                                onClick = { showDialog = true },
                                enabled = !cargando && v.estado == "disponible" && cupos > 0,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (cargando)
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                else {
                                    Icon(Icons.Filled.BookOnline, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        when {
                                            v.estado != "disponible" -> "Viaje ${v.estado}"
                                            cupos <= 0 -> "Sin cupos disponibles"
                                            else -> "Reservar puesto · $cupos cupo${if (cupos > 1) "s" else ""}"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    shape = RoundedCornerShape(20.dp),
                    title = { Text("Confirmar reserva") },
                    text = {
                        Text("¿Reservar un puesto?\n\n" +
                                "Origen: ${v.origen}\n" +
                                "Destino: ${v.sede?.nombreSede}\n" +
                                "Costo: $ ${"%.0f".format(v.costo)}\n" +
                                "Cupos disponibles: $cupos")
                    },
                    confirmButton = {
                        Button(onClick = {
                            sesion?.idUsuario?.let {
                                reservaViewModel.reservar(it, v.idViaje)
                            }
                            showDialog = false
                        }) { Text("Confirmar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
                    }
                )
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun DetalleRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}