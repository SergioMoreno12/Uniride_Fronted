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
                    }) { Icon(Icons.Filled.Refresh, "Actualizar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (viaje == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val capacidad = viaje.vehiculo?.capacidad ?: 0
            val cupos     = viaje.cuposDisponibles ?: capacidad

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Info del viaje ─────────────────────────────────
                Card(modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        Text("Información del viaje",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)

                        DetalleRowViaje(Icons.Filled.LocationOn, "Origen", viaje.origen)
                        DetalleRowViaje(Icons.Filled.School,
                            "Sede destino", viaje.sede?.nombreSede ?: viaje.destino)
                        DetalleRowViaje(Icons.Filled.LocationCity,
                            "Ciudad destino", viaje.sede?.ciudad ?: "-")

                        // ── Fecha y hora SEPARADAS ──────────────────
                        DetalleRowViaje(Icons.Filled.CalendarToday,
                            "Fecha",
                            viaje.fechaHora.take(10))

                        DetalleRowViaje(Icons.Filled.AccessTime,
                            "Hora de salida",
                            if (viaje.fechaHora.length >= 16)
                                viaje.fechaHora.substring(11, 16)
                            else "--:--")

                        if (!viaje.horaLlegada.isNullOrBlank()) {
                            DetalleRowViaje(Icons.Filled.Schedule,
                                "Hora de llegada",
                                if (viaje.horaLlegada.length >= 16)
                                    viaje.horaLlegada.substring(11, 16)
                                else "--:--")
                        }

                        DetalleRowViaje(Icons.Filled.AttachMoney,
                            "Costo por puesto",
                            "$ ${"%.0f".format(viaje.costo)}")

                        if (!viaje.descripcionPunto.isNullOrBlank()) {
                            DetalleRowViaje(Icons.Filled.Place,
                                "Punto de encuentro",
                                viaje.descripcionPunto)
                        }

                        // ── Cupos disponibles ───────────────────────
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

                // ── Info del vehículo y conductor ─────────────────
                Card(modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        Text("Vehículo y conductor",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)

                        DetalleRowViaje(Icons.Filled.DirectionsCar,
                            "Vehículo",
                            "${viaje.vehiculo?.marca} ${viaje.vehiculo?.modelo}")
                        DetalleRowViaje(Icons.Filled.ConfirmationNumber,
                            "Placa",
                            viaje.vehiculo?.placa ?: "-")
                        DetalleRowViaje(Icons.Filled.EventSeat,
                            "Capacidad total",
                            "$capacidad puestos")
                        DetalleRowViaje(Icons.Filled.Person,
                            "Conductor",
                            viaje.vehiculo?.usuario?.nombre ?: "-")

                        // Botón ver perfil conductor
                        viaje.vehiculo?.usuario?.let { conductor ->
                            OutlinedButton(
                                onClick = {
                                    navController.navigate("conductor_perfil/${conductor.idUsuario}")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Person, null,
                                    modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Ver perfil del conductor")
                            }
                        }
                    }
                }

                // ── Estado del viaje ──────────────────────────────
                when {
                    esPropio -> {
                        Card(modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(modifier = Modifier.padding(14.dp),
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
                            Row(modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, null,
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("Ya tienes una reserva en este viaje",
                                        fontWeight = FontWeight.SemiBold)
                                    Text("Ve a Mis Reservas para ver el estado",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }

                    else -> {
                        // Botón reservar (solo para no administradores)
                        if (sesion?.rol != "administrador") {
                            Button(
                                onClick = { showDialog = true },
                                enabled = !cargando &&
                                        viaje.estado == "disponible" &&
                                        cupos > 0,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (cargando) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.BookOnline, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        when {
                                            viaje.estado != "disponible" ->
                                                "Viaje ${viaje.estado}"
                                            cupos <= 0 -> "Sin cupos disponibles"
                                            else ->
                                                "Reservar · $cupos cupo${if (cupos > 1) "s" else ""}"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // ── Dialog confirmar reserva ───────────────────────────
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    shape = RoundedCornerShape(20.dp),
                    title = { Text("Confirmar reserva") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("¿Deseas reservar un puesto en este viaje?")
                            Spacer(Modifier.height(4.dp))
                            Text("Origen: ${viaje.origen}",
                                style = MaterialTheme.typography.bodySmall)
                            Text("Destino: ${viaje.sede?.nombreSede}",
                                style = MaterialTheme.typography.bodySmall)
                            Text("Fecha: ${viaje.fechaHora.take(10)}",
                                style = MaterialTheme.typography.bodySmall)
                            Text("Salida: ${if (viaje.fechaHora.length >= 16)
                                viaje.fechaHora.substring(11, 16) else "--:--"}",
                                style = MaterialTheme.typography.bodySmall)
                            if (!viaje.horaLlegada.isNullOrBlank()) {
                                Text("Llegada: ${if (viaje.horaLlegada.length >= 16)
                                    viaje.horaLlegada.substring(11, 16) else "--:--"}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Costo: $ ${"%.0f".format(viaje.costo)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold)
                            if (!viaje.descripcionPunto.isNullOrBlank()) {
                                Text("Punto de encuentro: ${viaje.descripcionPunto}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            sesion?.idUsuario?.let {
                                reservaViewModel.reservar(it, viaje.idViaje)
                            }
                            showDialog = false
                        }) { Text("Confirmar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetalleRowViaje(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}