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

    // ✅ Ahora compara fecha+hora completas (no solo fecha)
    val activas = (reservas ?: emptyList()).filter { reserva ->
        val viajeDt = try {
            val raw = reserva.viaje?.fechaHora ?: ""
            LocalDateTime.parse(raw.substring(0, minOf(19, raw.length)))
        } catch (e: Exception) { null }
        viajeDt != null && !viajeDt.isBefore(ahora)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis reservas") },
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
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Filled.BookOnline, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))
                    Text("No tienes reservas activas",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
                Text("${activas.size} reserva${if (activas.size > 1) "s" else ""} activa${if (activas.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                activas.forEach { reserva ->
                    Card(
                        onClick = { navController.navigate("reserva_detalle/${reserva.idReserva}") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // ── Barra de estado superior ───────────────────────
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (reserva.confirmada) Icons.Filled.CheckCircle
                                        else Icons.Filled.HourglassEmpty,
                                        null,
                                        tint = if (reserva.confirmada)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (reserva.confirmada) "Confirmada" else "Pendiente",
                                        fontWeight = FontWeight.Bold,
                                        color = if (reserva.confirmada)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.tertiary,
                                        style = MaterialTheme.typography.bodyMedium)
                                }
                                Text("# ${reserva.idReserva}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(10.dp))

                            // ── Info del viaje ─────────────────────────────────
                            reserva.viaje?.let { v ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocationOn, null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("${v.origen} → ${v.sede?.nombreSede ?: v.destino}",
                                        fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CalendarToday, null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(v.fechaHora.take(10),
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.AccessTime, null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (v.fechaHora.length >= 16)
                                            v.fechaHora.substring(11, 16) else "--:--",
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.AttachMoney, null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp))
                                        Text("${"%.0f".format(v.costo)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Punto de encuentro si confirmada
                                if (reserva.confirmada && !v.descripcionPunto.isNullOrBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Place, null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(v.descripcionPunto,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(10.dp))

                            // ── Botones de acción ──────────────────────────────
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically) {

                                OutlinedButton(
                                    onClick = {
                                        navController.navigate("reserva_detalle/${reserva.idReserva}")
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Info, null,
                                        modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Detalles",
                                        style = MaterialTheme.typography.labelMedium)
                                }

                                val tel = reserva.viaje?.vehiculo?.usuario?.telefono
                                if (reserva.confirmada && !tel.isNullOrBlank()) {
                                    Button(
                                        onClick = {
                                            val numero = tel.filter { it.isDigit() }
                                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                                Uri.parse("https://wa.me/57$numero")))
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Icon(Icons.Filled.Chat, null,
                                            modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("WhatsApp",
                                            style = MaterialTheme.typography.labelMedium)
                                    }
                                }

                                if (!reserva.confirmada) {
                                    OutlinedButton(
                                        onClick = { reservaACancelar = reserva },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Filled.Cancel, null,
                                            modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Cancelar",
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
            text = { Text("¿Cancelar la reserva del viaje de " +
                    "${r.viaje?.origen} a ${r.viaje?.sede?.nombreSede}?") },
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
                ) { Text("Sí, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { reservaACancelar = null }) { Text("No, volver") }
            }
        )
    }
}