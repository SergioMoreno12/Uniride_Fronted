package com.example.uniride.Screen

import android.content.Intent
import android.net.Uri
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
import com.example.uniride.model.Reserva
import com.example.uniride.ui.theme.Routes
import java.time.LocalDate

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
    val hoy      = LocalDate.now()
    var reservaACancelar by remember { mutableStateOf<Reserva?>(null) }

    LaunchedEffect(true) {
        sesion?.idUsuario?.let { reservaViewModel.cargarMisReservas(it) }
    }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            reservaViewModel.limpiarMensaje()
        }
    }

    // Solo reservas activas (fecha del viaje hoy o futura)
    val activas = (reservas ?: emptyList()).filter { reserva ->
        val fechaViaje = try {
            LocalDate.parse(reserva.viaje?.fechaHora?.take(10) ?: "")
        } catch (e: Exception) { null }
        fechaViaje != null && !fechaViaje.isBefore(hoy)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (sesion?.rol == "conductor") "Mis reservas como pasajero"
                        else "Mis reservas"
                    )
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Routes.HISTORIAL_RESERVAS)
                    }) { Icon(Icons.Filled.History, "Historial") }
                    IconButton(onClick = {
                        sesion?.idUsuario?.let { reservaViewModel.cargarMisReservas(it) }
                    }) { Icon(Icons.Filled.Refresh, "Actualizar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            BottomNavBar(currentRoute = "mis_reservas", rol = sesion?.rol ?: "usuario") { route ->
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
        } else if (activas.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.BookOnline, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("No tienes reservas activas",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = {
                        navController.navigate(Routes.HISTORIAL_RESERVAS)
                    }) { Text("Ver historial de reservas") }
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
                activas.forEach { reserva ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // Encabezado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Reserva #${reserva.idReserva}",
                                    fontWeight = FontWeight.Bold)
                                AssistChip(onClick = {}, label = {
                                    Text(
                                        if (reserva.confirmada) "Confirmada" else "Pendiente",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                })
                            }

                            Spacer(Modifier.height(8.dp))

                            reserva.viaje?.let { v ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocationOn, null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${v.origen} → ${v.sede?.nombreSede ?: v.destino}",
                                        style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccessTime, null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(v.fechaHora.take(16).replace("T", " "),
                                        style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AttachMoney, null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("$ ${"%.0f".format(v.costo)}",
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary)
                                }

                                // Punto de encuentro si confirmada
                                if (reserva.confirmada && !v.descripcionPunto.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Place, null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(v.descripcionPunto,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary)
                                    }
                                }

                                // Contacto conductor si confirmada
                                if (reserva.confirmada) {
                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    Text("Contacto del conductor",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(v.vehiculo?.usuario?.nombre ?: "-",
                                                style = MaterialTheme.typography.bodySmall)
                                            Text(v.vehiculo?.usuario?.telefono ?: "No disponible",
                                                fontWeight = FontWeight.SemiBold)
                                        }
                                        // Botón WhatsApp
                                        val telefono = v.vehiculo?.usuario?.telefono
                                        if (!telefono.isNullOrBlank()) {
                                            IconButton(onClick = {
                                                val numero = telefono.filter { it.isDigit() }
                                                val url = "https://wa.me/57$numero"
                                                context.startActivity(
                                                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                )
                                            }) {
                                                Icon(Icons.Filled.Chat, "WhatsApp",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(28.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!reserva.confirmada) {
                                    OutlinedButton(
                                        onClick = { reservaACancelar = reserva },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Filled.Cancel, null,
                                            modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Cancelar",
                                            style = MaterialTheme.typography.labelMedium)
                                    }
                                }

                                if (reserva.confirmada) {
                                    val idConductor =
                                        reserva.viaje?.vehiculo?.usuario?.idUsuario ?: 0L
                                    Button(
                                        onClick = {
                                            navController.navigate(
                                                "calificar/${reserva.idReserva}/$idConductor"
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Star, null,
                                            modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Calificar",
                                            style = MaterialTheme.typography.labelMedium)
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

    reservaACancelar?.let { r ->
        AlertDialog(
            onDismissRequest = { reservaACancelar = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Cancelar reserva") },
            text = {
                Text("¿Deseas cancelar tu reserva en el viaje de " +
                        "${r.viaje?.origen} a ${r.viaje?.sede?.nombreSede}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        sesion?.idUsuario?.let {
                            reservaViewModel.cancelarReserva(r.idReserva, it)
                        }
                        reservaACancelar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Cancelar reserva") }
            },
            dismissButton = {
                TextButton(onClick = { reservaACancelar = null }) { Text("No, volver") }
            }
        )
    }
}