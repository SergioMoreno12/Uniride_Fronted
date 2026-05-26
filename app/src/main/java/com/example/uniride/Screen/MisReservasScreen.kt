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
import com.example.uniride.ui.theme.Routes
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisReservasScreen(
    navController: NavController,
    authViewModel: AuthViewModel       = viewModel(),
    reservaViewModel: ReservaViewModel = viewModel()
) {
    val sesion   = authViewModel.sesionActual
    val reservas by reservaViewModel.misReservas.observeAsState(emptyList())
    val cargando by reservaViewModel.cargando.observeAsState(false)
    val mensaje  by reservaViewModel.mensaje.observeAsState(null)
    val context  = LocalContext.current
    val ahora    = LocalDateTime.now()

    LaunchedEffect(true) {
        sesion?.idUsuario?.let { reservaViewModel.cargarMisReservas(it) }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            reservaViewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis reservas") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Routes.HISTORIAL_RESERVAS)
                    }) { Icon(Icons.Filled.History, "Historial") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        RefreshableContent(
            isRefreshing = cargando,
            onRefresh    = {
                sesion?.idUsuario?.let { reservaViewModel.cargarMisReservas(it) }
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            val activas = (reservas ?: emptyList()).filter { r ->
                val dt = try {
                    val raw = r.viaje?.fechaHora ?: ""
                    LocalDateTime.parse(raw.substring(0, minOf(19, raw.length)))
                } catch (e: Exception) { null }
                dt != null && !dt.isBefore(ahora) &&
                        r.viaje?.estado != "cancelado"
            }.sortedBy { it.viaje?.fechaHora }

            if (activas.isEmpty()) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.height(180.dp))
                    Icon(Icons.Filled.BookmarkBorder, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("No tienes reservas activas",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))
                    Text("Desliza hacia abajo para recargar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = {
                        navController.navigate(Routes.HISTORIAL_RESERVAS)
                    }) { Text("Ver historial") }
                    Button(onClick = {
                        navController.navigate(Routes.HOME)
                    }, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.Search, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Buscar viajes")
                    }
                }
            } else {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("${activas.size} reserva${if (activas.size > 1) "s" else ""} activa${if (activas.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                    activas.forEach { reserva ->
                        Card(
                            onClick = {
                                navController.navigate("reserva_detalle/${reserva.idReserva}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${reserva.viaje?.origen ?: "-"} → ${reserva.viaje?.destino ?: "-"}",
                                            fontWeight = FontWeight.SemiBold)
                                        Text("📅 ${reserva.viaje?.fechaHora?.take(10) ?: "-"}  " +
                                                "🕐 ${reserva.viaje?.fechaHora?.let {
                                                    if (it.length >= 16) it.substring(11, 16) else "--:--"
                                                } ?: "--:--"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        Text("Conductor: ${reserva.viaje?.vehiculo?.usuario?.nombre ?: "-"}",
                                            style = MaterialTheme.typography.bodySmall)
                                        Text("$ ${"%.0f".format(reserva.viaje?.costo ?: 0.0)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        AssistChip(onClick = {},
                                            label = {
                                                Text(if (reserva.confirmada) "Confirmada" else "Pendiente",
                                                    style = MaterialTheme.typography.labelSmall)
                                            })
                                        Spacer(Modifier.height(4.dp))
                                        Icon(Icons.Filled.ChevronRight, null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}