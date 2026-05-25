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
    var editFecha      by remember { mutableStateOf("") }
    var editHora       by remember { mutableStateOf("06") }
    var editMin        by remember { mutableStateOf("00") }
    var editAmPm       by remember { mutableStateOf("AM") }
    var editCosto      by remember { mutableStateOf("") }
    var editPunto      by remember { mutableStateOf("") }

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
                    IconButton(onClick = { navController.navigate(Routes.HISTORIAL_VIAJES) }) {
                        Icon(Icons.Filled.History, "Historial")
                    }
                    IconButton(onClick = { scope.launch { cargarViajes() } }) {
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
                .filter { it.estado != "cancelado" && it.estado != "completado" }
                .sortedByDescending { it.fechaHora }

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
                        Card(
                            onClick = {
                                navController.navigate("viaje_activo/${viaje.idViaje}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
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

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (viaje.estado == "disponible" || viaje.estado == "lleno") {
                                        OutlinedButton(
                                            onClick = {
                                                viajeAEditar = viaje
                                                editOrigen  = viaje.origen
                                                editFecha   = viaje.fechaHora.take(10)
                                                // Parsear hora (formato 24h)
                                                val h24 = viaje.fechaHora
                                                    .substring(11, 13).toIntOrNull() ?: 6
                                                val mm  = viaje.fechaHora
                                                    .substring(14, 16)
                                                editHora = when {
                                                    h24 == 0  -> "12"
                                                    h24 > 12  -> "%02d".format(h24 - 12)
                                                    else      -> "%02d".format(h24)
                                                }
                                                editMin   = mm
                                                editAmPm  = if (h24 >= 12) "PM" else "AM"
                                                editCosto = viaje.costo.toInt().toString()
                                                editPunto = viaje.descripcionPunto ?: ""
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.Edit, null,
                                                modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Editar",
                                                style = MaterialTheme.typography.labelSmall)
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
                                            Text("Cancelar",
                                                style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

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

    // ── Cancelar viaje ────────────────────────────────────────────────
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

    // ── Editar viaje (con DatePicker y selector de hora) ──────────────
    viajeAEditar?.let { v ->
        AlertDialog(
            onDismissRequest = { viajeAEditar = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Editar viaje") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    TextField(
                        value = editOrigen, onValueChange = { editOrigen = it },
                        label = { Text("Ciudad de origen") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // DatePicker
                    DateTimePickerField(
                        label = "Fecha del viaje",
                        value = editFecha,
                        onDateSelected = { editFecha = it.take(10) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Selector de hora 12h
                    Text("Hora de salida",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = editHora,
                            onValueChange = { v2 ->
                                val n = v2.toIntOrNull()
                                if (n != null && n in 1..12) editHora = "%02d".format(n)
                                else if (v2.isEmpty()) editHora = ""
                            },
                            label = { Text("HH") }, singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Text(":", fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(
                            value = editMin,
                            onValueChange = { v2 ->
                                val n = v2.toIntOrNull()
                                if (n != null && n in 0..59) editMin = "%02d".format(n)
                                else if (v2.isEmpty()) editMin = ""
                            },
                            label = { Text("MM") }, singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Column {
                            listOf("AM", "PM").forEach { ap ->
                                FilterChip(
                                    selected = editAmPm == ap,
                                    onClick  = { editAmPm = ap },
                                    label    = { Text(ap, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.height(32.dp))
                            }
                        }
                    }

                    TextField(
                        value = editCosto, onValueChange = { editCosto = it.filter { c -> c.isDigit() } },
                        label = { Text("Costo por puesto") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    TextField(
                        value = editPunto, onValueChange = { editPunto = it },
                        label = { Text("Punto de encuentro") }, maxLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val h12  = editHora.toIntOrNull() ?: 6
                    val mm   = editMin.toIntOrNull() ?: 0
                    val h24  = when {
                        editAmPm == "AM" && h12 == 12 -> 0
                        editAmPm == "PM" && h12 != 12 -> h12 + 12
                        else -> h12
                    }
                    val fechaSalida = "$editFecha" + "T%02d:%02d:00".format(h24, mm)
                    viajeViewModel.editarViaje(
                        v.idViaje, editOrigen, v.destino, fechaSalida,
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