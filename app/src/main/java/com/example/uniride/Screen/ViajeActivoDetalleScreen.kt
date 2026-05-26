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
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ReservaViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Calificacion
import com.example.uniride.model.Reserva
import com.example.uniride.model.Viaje
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajeActivoDetalleScreen(
    idViaje: Long,
    navController: NavController,
    authViewModel: AuthViewModel       = viewModel(),
    reservaViewModel: ReservaViewModel = viewModel(),
    viajeViewModel: ViajeViewModel     = viewModel()
) {
    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    var viaje          by remember { mutableStateOf<Viaje?>(null) }
    var reservas       by remember { mutableStateOf<List<Reserva>>(emptyList()) }
    var calificaciones by remember { mutableStateOf<List<Calificacion>>(emptyList()) }
    var cargando       by remember { mutableStateOf(true) }
    var viajeACompletar by remember { mutableStateOf(false) }
    var viajeACancelar  by remember { mutableStateOf(false) }

    val mensaje by reservaViewModel.mensaje.observeAsState(null)

    suspend fun recargar() {
        try {
            viaje    = RetrofitClient.apiService.obtenerViaje(idViaje)
            reservas = RetrofitClient.apiService.reservasPorViaje(idViaje)
            viaje?.vehiculo?.usuario?.idUsuario?.let { idConductor ->
                calificaciones = RetrofitClient.apiService
                    .calificacionesConductor(idConductor)
                    .filter { cal ->
                        reservas.any { r -> r.usuario?.idUsuario == cal.pasajero?.idUsuario }
                    }
            }
        } catch (e: Exception) { }
        cargando = false
    }

    LaunchedEffect(idViaje) {
        scope.launch { recargar() }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            reservaViewModel.limpiarMensaje()
            scope.launch { recargar() }
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
            isRefreshing = cargando,
            onRefresh    = { scope.launch { recargar() } },
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                viaje?.let { v ->
                    Card(modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Información del viaje", fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                            InfoRowDetalle(Icons.Filled.LocationOn,  "Origen",   v.origen)
                            InfoRowDetalle(Icons.Filled.School,      "Destino",  v.sede?.nombreSede ?: v.destino)
                            InfoRowDetalle(Icons.Filled.AccessTime,  "Salida",   v.fechaHora.take(16).replace("T", " "))
                            v.horaLlegada?.let {
                                InfoRowDetalle(Icons.Filled.Schedule, "Llegada",
                                    it.take(16).replace("T", " "))
                            }
                            InfoRowDetalle(Icons.Filled.AttachMoney, "Costo",
                                "$ ${"%.0f".format(v.costo)}")
                            v.descripcionPunto?.let {
                                InfoRowDetalle(Icons.Filled.Place, "Punto de encuentro", it)
                            }
                            InfoRowDetalle(Icons.Filled.Badge, "Estado", v.estado.uppercase())
                        }
                    }

                    // ── Acciones del conductor sobre su viaje ─────────
                    if (v.estado == "disponible" || v.estado == "lleno") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viajeACompletar = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Filled.CheckCircle, null,
                                    modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Completar viaje",
                                    style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { viajeACancelar = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Filled.Cancel, null,
                                    modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Cancelar viaje",
                                    style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                Text("Pasajeros (${reservas.size})", fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)

                if (reservas.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)) {
                        Box(modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            contentAlignment = Alignment.Center) {
                            Text("Sin reservas en este viaje",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    reservas.forEach { reserva ->
                        Card(modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(reserva.usuario?.nombre ?: "-",
                                            fontWeight = FontWeight.SemiBold)
                                        Text(reserva.usuario?.telefono ?: "Sin teléfono",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    AssistChip(onClick = {}, label = {
                                        Text(if (reserva.confirmada) "Confirmado" else "Pendiente",
                                            style = MaterialTheme.typography.labelSmall)
                                    })
                                }

                                Spacer(Modifier.height(8.dp))

                                val cal = calificaciones.find {
                                    it.pasajero?.idUsuario == reserva.usuario?.idUsuario
                                }
                                if (cal != null) {
                                    HorizontalDivider()
                                    Spacer(Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        (1..5).forEach { i ->
                                            Icon(
                                                if (i <= cal.puntuacion) Icons.Filled.Star
                                                else Icons.Filled.StarBorder,
                                                null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        if (!cal.comentario.isNullOrBlank()) {
                                            Spacer(Modifier.width(8.dp))
                                            Text("\"${cal.comentario}\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            navController.navigate(
                                                "conductor_perfil/${reserva.usuario?.idUsuario}")
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Person, null,
                                            modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Ver perfil",
                                            style = MaterialTheme.typography.labelSmall)
                                    }

                                    if (!reserva.confirmada) {
                                        Button(
                                            onClick = {
                                                reservaViewModel.confirmarReserva(reserva.idReserva)
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.CheckCircle, null,
                                                modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Confirmar",
                                                style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Diálogos ──────────────────────────────────────────
            if (viajeACompletar) {
                AlertDialog(
                    onDismissRequest = { viajeACompletar = false },
                    shape = RoundedCornerShape(20.dp),
                    title = { Text("Completar viaje") },
                    text  = { Text("Marca el viaje como completado. " +
                            "Los pasajeros podrán calificarte después de esto.") },
                    confirmButton = {
                        Button(onClick = {
                            scope.launch {
                                try {
                                    RetrofitClient.apiService.completarViaje(idViaje)
                                    recargar()
                                    Toast.makeText(context, "Viaje marcado como completado",
                                        Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                            viajeACompletar = false
                        }) { Text("Sí, completar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { viajeACompletar = false }) { Text("Cancelar") }
                    }
                )
            }

            if (viajeACancelar) {
                AlertDialog(
                    onDismissRequest = { viajeACancelar = false },
                    shape = RoundedCornerShape(20.dp),
                    title = { Text("Cancelar viaje") },
                    text  = { Text("¿Cancelar este viaje? Los pasajeros con reserva " +
                            "serán notificados.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        RetrofitClient.apiService.cancelarViaje(idViaje)
                                        Toast.makeText(context, "Viaje cancelado",
                                            Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                                viajeACancelar = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Sí, cancelar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { viajeACancelar = false }) { Text("Volver") }
                    }
                )
            }
        }
    }
}
}

@Composable
private fun InfoRowDetalle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}