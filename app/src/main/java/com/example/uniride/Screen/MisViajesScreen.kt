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
import com.example.uniride.model.Viaje
import com.example.uniride.ui.theme.Routes
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisViajesScreen(
    navController: NavController,
    authViewModel: AuthViewModel       = viewModel(),
    viajeViewModel: ViajeViewModel     = viewModel(),
    reservaViewModel: ReservaViewModel = viewModel(),
    onPublicarViaje: () -> Unit = {}
) {
    val sesion    = authViewModel.sesionActual
    val misViajes by viajeViewModel.misViajes.observeAsState(emptyList())
    val cargando  by viajeViewModel.cargando.observeAsState(false)
    val mensaje   by viajeViewModel.mensaje.observeAsState(null)
    val context   = LocalContext.current
    val scope     = rememberCoroutineScope()
    val ahora     = LocalDateTime.now()

    var viajeACancelar by remember { mutableStateOf<Viaje?>(null) }

    // FIX: Ahora cargarMisViajes() internamente carga de todos los vehículos
    fun cargar() {
        sesion?.idUsuario?.let { viajeViewModel.cargarMisViajes(it) }
    }

    LaunchedEffect(true) { cargar() }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viajeViewModel.limpiarMensaje()
            if (it.contains("cancelado") || it.contains("actualizado")) cargar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Viajes publicados") },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.HISTORIAL_VIAJES) }) {
                        Icon(Icons.Filled.History, "Historial")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        RefreshableContent(
            isRefreshing = cargando,
            onRefresh    = { cargar() },
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
            val activos = (misViajes ?: emptyList()).filter { v ->
                if (v.estado == "cancelado" || v.estado == "completado") return@filter false
                val dt = try {
                    LocalDateTime.parse(v.fechaHora.substring(0, minOf(19, v.fechaHora.length)))
                } catch (e: Exception) { null }
                dt != null && !dt.isBefore(ahora)
            }.sortedBy { it.fechaHora }

            if (activos.isEmpty()) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.height(180.dp))
                    Icon(Icons.Filled.DirectionsCar, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("No tienes viajes activos",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("Desliza hacia abajo para recargar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = {
                        navController.navigate(Routes.HISTORIAL_VIAJES)
                    }) { Text("Ver historial de viajes") }
                    Button(
                        onClick = { onPublicarViaje() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Add, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Publicar viaje")
                    }
                }
            } else {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("${activos.size} viaje${if (activos.size > 1) "s" else ""} activo${if (activos.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                    activos.forEach { viaje ->
                        Card(
                            onClick = { navController.navigate("viaje_activo/${viaje.idViaje}") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            AssistChip(onClick = {},
                                                label = {
                                                    Text(if (viaje.tipoViaje == "vuelta") "Vuelta" else "Ida",
                                                        style = MaterialTheme.typography.labelSmall)
                                                },
                                                leadingIcon = {
                                                    Icon(if (viaje.tipoViaje == "vuelta")
                                                        Icons.Filled.Home else Icons.Filled.School,
                                                        null, modifier = Modifier.size(12.dp))
                                                })
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text("${viaje.origen} → ${viaje.destino}",
                                            fontWeight = FontWeight.SemiBold)
                                        Text("📅 ${viaje.fechaHora.take(10)}  " +
                                                "🕐 ${if (viaje.fechaHora.length >= 16)
                                                    viaje.fechaHora.substring(11, 16) else "--:--"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        if (!viaje.horaLlegada.isNullOrBlank()) {
                                            Text("🏁 Llegada: ${if (viaje.horaLlegada.length >= 16)
                                                viaje.horaLlegada.substring(11, 16) else "--:--"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
                                        }
                                        // Mostrar el vehículo si tiene más de uno
                                        if (viaje.vehiculo != null) {
                                            Text("🚗 ${viaje.vehiculo.marca} ${viaje.vehiculo.modelo} · ${viaje.vehiculo.placa}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        }
                                        Text("$ ${"%.0f".format(viaje.costo)} · " +
                                                "${viaje.vehiculo?.capacidad ?: "-"} puestos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        AssistChip(onClick = {},
                                            label = { Text(viaje.estado.replaceFirstChar { it.uppercase() },
                                                style = MaterialTheme.typography.labelSmall) })
                                        Spacer(Modifier.height(4.dp))
                                        Icon(Icons.Filled.ChevronRight, null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = {
                                            navController.navigate("editar_viaje/${viaje.idViaje}")
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Edit, null,
                                            modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Editar", style = MaterialTheme.typography.labelSmall)
                                    }
                                    OutlinedButton(
                                        onClick = { viajeACancelar = viaje },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Filled.Cancel, null,
                                            modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Cancelar", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Text("Toca para ver pasajeros y detalles",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    viajeACancelar?.let { v ->
        AlertDialog(
            onDismissRequest = { viajeACancelar = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Cancelar viaje") },
            text  = { Text("¿Cancelar el viaje de ${v.origen} a ${v.destino}?\n\nLos pasajeros con reserva serán notificados.") },
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
}