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
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Viaje
import kotlinx.coroutines.launch

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
    val viajesReservados by viajeViewModel.viajesReservados.observeAsState(emptySet())
    val viajesPropios    by viajeViewModel.viajesPropios.observeAsState(emptySet())
    val cargandoReserva  by reservaViewModel.cargando.observeAsState(false)
    val mensaje          by reservaViewModel.mensaje.observeAsState(null)
    var showDialog       by remember { mutableStateOf(false) }
    val context          = LocalContext.current
    val scope            = rememberCoroutineScope()

    // Carga el viaje directamente del API y también las reservas para calcular cupos reales
    var viaje         by remember { mutableStateOf<Viaje?>(null) }
    var reservasCount by remember { mutableStateOf(0) }
    var cargandoViaje by remember { mutableStateOf(true) }

    suspend fun recargarViaje() {
        try {
            viaje = RetrofitClient.apiService.obtenerViaje(idViaje)
            // Cargar reservas para calcular cupos reales cuando el backend
            // no devuelve cuposDisponibles en los endpoints de lista
            val reservas = RetrofitClient.apiService.reservasPorViaje(idViaje)
            reservasCount = reservas.size
        } catch (e: Exception) { }
    }

    LaunchedEffect(idViaje) {
        scope.launch {
            recargarViaje()
            cargandoViaje = false
        }
    }

    val yaReservo = idViaje in (viajesReservados ?: emptySet())
    val esPropio  = idViaje in (viajesPropios ?: emptySet()) ||
            (viaje?.vehiculo?.usuario?.idUsuario == sesion?.idUsuario)

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            reservaViewModel.limpiarMensaje()
            if (it.contains("éxito")) {
                scope.launch { recargarViaje() }
                viajeViewModel.cargarDisponibles(sesion?.idUsuario)
                navController.popBackStack()
            }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        RefreshableContent(
            isRefreshing = cargandoViaje || cargandoReserva,
            onRefresh    = {
                scope.launch {
                    cargandoViaje = true
                    recargarViaje()
                    cargandoViaje = false
                }
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            if (cargandoViaje) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (viaje == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.ErrorOutline, null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Text("No se pudo cargar el viaje",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = { navController.popBackStack() }) {
                            Text("Volver")
                        }
                    }
                }
            } else {
                val v         = viaje!!
                val capacidad = v.vehiculo?.capacidad ?: 0

                // Cupos: prioridad 1) campo del backend, 2) calculado de reservas, 3) capacidad total
                val cupos = v.cuposDisponibles
                    ?: if (capacidad > 0) (capacidad - reservasCount).coerceAtLeast(0)
                    else capacidad

                Column(
                    modifier = Modifier
                        .fillMaxSize()
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

                            DetalleRowViaje(Icons.Filled.LocationOn,   "Origen",     v.origen)
                            DetalleRowViaje(Icons.Filled.School,
                                "Sede destino", v.sede?.nombreSede ?: v.destino)
                            DetalleRowViaje(Icons.Filled.LocationCity,
                                "Ciudad", v.sede?.ciudad ?: "-")
                            DetalleRowViaje(Icons.Filled.CalendarToday, "Fecha",     v.fechaHora.take(10))
                            DetalleRowViaje(Icons.Filled.AccessTime,
                                "Hora de salida",
                                if (v.fechaHora.length >= 16) v.fechaHora.substring(11, 16) else "--:--")
                            if (!v.horaLlegada.isNullOrBlank()) {
                                DetalleRowViaje(Icons.Filled.Schedule,
                                    "Hora de llegada",
                                    if (v.horaLlegada.length >= 16) v.horaLlegada.substring(11, 16) else "--:--")
                            }
                            DetalleRowViaje(Icons.Filled.AttachMoney,
                                "Costo por puesto", "$ ${"%.0f".format(v.costo)}")
                            if (!v.descripcionPunto.isNullOrBlank()) {
                                DetalleRowViaje(Icons.Filled.Place,
                                    "Punto de encuentro", v.descripcionPunto)
                            }

                            // ── Barra de disponibilidad ─────────────────
                            if (capacidad > 0) {
                                HorizontalDivider()
                                Text("Disponibilidad",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                // Siempre se muestra porque cargamos las reservas
                                CuposProgressBar(
                                    cuposDisponibles = cupos,
                                    capacidadTotal   = capacidad,
                                    modifier         = Modifier.fillMaxWidth()
                                )
                                // Iconos de asientos individuales
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    repeat(capacidad) { i ->
                                        val ocupado = i >= cupos
                                        Icon(
                                            if (ocupado) Icons.Filled.EventSeat
                                            else Icons.Filled.AirlineSeatReclineNormal,
                                            contentDescription = null,
                                            tint = if (ocupado)
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
                                            else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Vehículo y conductor ───────────────────────────
                    Card(modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {

                            Text("Vehículo y conductor",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)

                            DetalleRowViaje(Icons.Filled.DirectionsCar,
                                "Vehículo",
                                "${v.vehiculo?.marca ?: "-"} ${v.vehiculo?.modelo ?: ""}")
                            DetalleRowViaje(Icons.Filled.ConfirmationNumber,
                                "Placa", v.vehiculo?.placa ?: "-")
                            DetalleRowViaje(Icons.Filled.EventSeat,
                                "Capacidad total", "$capacidad puestos")
                            DetalleRowViaje(Icons.Filled.Person,
                                "Conductor", v.vehiculo?.usuario?.nombre ?: "-")

                            v.vehiculo?.usuario?.let { conductor ->
                                OutlinedButton(
                                    onClick = {
                                        navController.navigate("conductor_perfil/${conductor.idUsuario}")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape    = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Person, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Ver perfil del conductor")
                                }
                            }
                        }
                    }

                    // ── Acción ─────────────────────────────────────────
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
                                    Column {
                                        Text("Este es tu viaje publicado",
                                            fontWeight = FontWeight.SemiBold)
                                        Text("Toca 'Mis viajes' para gestionar tus viajes.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
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

                        sesion?.rol == "administrador" -> { /* Admin solo visualiza */ }

                        else -> {
                            Button(
                                onClick  = { showDialog = true },
                                enabled  = !cargandoReserva && v.estado == "disponible" && cupos > 0,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape    = RoundedCornerShape(12.dp)
                            ) {
                                if (cargandoReserva) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.BookOnline, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        when {
                                            v.estado != "disponible" -> "Viaje ${v.estado}"
                                            cupos <= 0 -> "Sin cupos disponibles"
                                            else -> "Reservar · $cupos cupo${if (cupos > 1) "s" else ""} libre${if (cupos > 1) "s" else ""}"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
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
                        text  = {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("¿Deseas reservar un puesto en este viaje?")
                                Spacer(Modifier.height(4.dp))
                                Text("Origen: ${v.origen}",
                                    style = MaterialTheme.typography.bodySmall)
                                Text("Destino: ${v.sede?.nombreSede ?: v.destino}",
                                    style = MaterialTheme.typography.bodySmall)
                                Text("Fecha: ${v.fechaHora.take(10)}",
                                    style = MaterialTheme.typography.bodySmall)
                                Text("Salida: ${if (v.fechaHora.length >= 16)
                                    v.fechaHora.substring(11, 16) else "--:--"}",
                                    style = MaterialTheme.typography.bodySmall)
                                if (!v.horaLlegada.isNullOrBlank()) {
                                    Text("Llegada: ${if (v.horaLlegada.length >= 16)
                                        v.horaLlegada.substring(11, 16) else "--:--"}",
                                        style = MaterialTheme.typography.bodySmall)
                                }
                                Text("Costo: $ ${"%.0f".format(v.costo)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold)
                                if (!v.descripcionPunto.isNullOrBlank()) {
                                    Text("Punto de encuentro: ${v.descripcionPunto}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.tertiary)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Quedan $cupos cupo${if (cupos > 1) "s" else ""} disponible${if (cupos > 1) "s" else ""}",
                                    style      = MaterialTheme.typography.bodySmall,
                                    color      = if (cupos == 1) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
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