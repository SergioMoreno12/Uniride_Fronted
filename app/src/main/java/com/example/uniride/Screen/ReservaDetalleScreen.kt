package com.example.uniride.Screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Calificacion
import com.example.uniride.model.Reserva
import com.example.uniride.model.Usuario
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservaDetalleScreen(
    idReserva: Long,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val scope   = rememberCoroutineScope()
    val context = LocalContext.current
    val hoy     = LocalDate.now()

    var reserva        by remember { mutableStateOf<Reserva?>(null) }
    var conductor      by remember { mutableStateOf<Usuario?>(null) }
    var calificaciones by remember { mutableStateOf<List<Calificacion>>(emptyList()) }
    var promedio       by remember { mutableStateOf(0.0) }
    var cargando       by remember { mutableStateOf(true) }
    var yaCalificado   by remember { mutableStateOf(false) }
    var mostrarDialogoCancelar by remember { mutableStateOf(false) }
    var cancelando     by remember { mutableStateOf(false) }

    LaunchedEffect(idReserva) {
        scope.launch {
            try {
                reserva = RetrofitClient.apiService.obtenerReserva(idReserva)
                reserva?.viaje?.vehiculo?.usuario?.idUsuario?.let { idConductor ->
                    conductor      = RetrofitClient.apiService.obtenerUsuario(idConductor)
                    calificaciones = RetrofitClient.apiService.calificacionesConductor(idConductor)
                    promedio       = RetrofitClient.apiService.promedioConductor(idConductor)
                }
                yaCalificado = RetrofitClient.apiService.yaCalificada(idReserva)
            } catch (e: Exception) { }
            cargando = false
        }
    }

    // ── Diálogo de confirmación para cancelar ──────────────────────
    if (mostrarDialogoCancelar) {
        AlertDialog(
            onDismissRequest = { if (!cancelando) mostrarDialogoCancelar = false },
            icon = {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("¿Cancelar tu reserva?") },
            text = {
                Text(
                    "Se eliminará tu reserva para este viaje. " +
                            "El puesto quedará disponible nuevamente."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        cancelando = true
                        scope.launch {
                            try {
                                val resp = RetrofitClient.apiService.cancelarReserva(idReserva)
                                if (resp.isSuccessful) {
                                    mostrarDialogoCancelar = false
                                    Toast.makeText(
                                        context,
                                        "Reserva cancelada correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No se pudo cancelar (${resp.code()})",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Error de conexión. Intenta de nuevo.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            cancelando = false
                            mostrarDialogoCancelar = false
                        }
                    },
                    enabled = !cancelando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (cancelando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onError
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Sí, cancelar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoCancelar = false },
                    enabled = !cancelando
                ) {
                    Text("Volver")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de reserva") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (cargando) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (reserva == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontró la reserva")
            }
        } else {
            val v = reserva!!
            val fechaViaje = try {
                LocalDate.parse(v.viaje?.fechaHora?.take(10) ?: "")
            } catch (e: Exception) { null }
            val viajeTermino = (fechaViaje != null && fechaViaje.isBefore(hoy)) ||
                    v.viaje?.estado == "completado"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── Estado de la reserva ───────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            v.viaje?.estado == "cancelado" -> MaterialTheme.colorScheme.errorContainer
                            v.confirmada -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when {
                                v.viaje?.estado == "cancelado" -> Icons.Filled.Cancel
                                v.confirmada -> Icons.Filled.CheckCircle
                                else -> Icons.Filled.HourglassEmpty
                            },
                            contentDescription = null,
                            tint = when {
                                v.viaje?.estado == "cancelado" -> MaterialTheme.colorScheme.error
                                v.confirmada -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.tertiary
                            },
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                when {
                                    v.viaje?.estado == "cancelado" -> "Viaje cancelado por el conductor"
                                    v.confirmada -> "Reserva confirmada"
                                    else -> "Reserva pendiente de confirmación"
                                },
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Reserva #${v.idReserva}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // ── Info del viaje ─────────────────────────────────
                v.viaje?.let { viaje ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Información del viaje",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            InfoFilaReserva(Icons.Filled.LocationOn, "Origen", viaje.origen)
                            InfoFilaReserva(
                                Icons.Filled.School, "Destino",
                                viaje.sede?.nombreSede ?: viaje.destino
                            )
                            InfoFilaReserva(
                                Icons.Filled.CalendarToday, "Fecha",
                                viaje.fechaHora.take(10)
                            )
                            InfoFilaReserva(
                                Icons.Filled.AccessTime, "Hora salida",
                                if (viaje.fechaHora.length >= 16)
                                    viaje.fechaHora.substring(11, 16) else "--:--"
                            )
                            if (!viaje.horaLlegada.isNullOrBlank()) {
                                InfoFilaReserva(
                                    Icons.Filled.Schedule, "Hora llegada",
                                    if (viaje.horaLlegada.length >= 16)
                                        viaje.horaLlegada.substring(11, 16) else "--:--"
                                )
                            }
                            InfoFilaReserva(
                                Icons.Filled.AttachMoney, "Costo",
                                "$ ${"%.0f".format(viaje.costo)}"
                            )
                            if (v.confirmada && !viaje.descripcionPunto.isNullOrBlank()) {
                                InfoFilaReserva(
                                    Icons.Filled.Place, "Punto de encuentro",
                                    viaje.descripcionPunto
                                )
                            }
                        }
                    }
                }

                // ── Perfil del conductor ───────────────────────────
                conductor?.let { cond ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Conductor",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        cond.nombre.first().uppercaseChar().toString(),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(cond.nombre, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        (1..5).forEach { i ->
                                            Icon(
                                                if (i <= promedio.toInt()) Icons.Filled.Star
                                                else Icons.Filled.StarBorder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "${"%.1f".format(promedio)} (${calificaciones.size})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }

                            if (v.confirmada) {
                                Spacer(Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Phone, null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        cond.telefono ?: "No disponible",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.height(8.dp))

                                val tel = cond.telefono
                                if (!tel.isNullOrBlank()) {
                                    Button(
                                        onClick = {
                                            val numero = tel.filter { it.isDigit() }
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://wa.me/57$numero")
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(Icons.Filled.Chat, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Contactar por WhatsApp", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (calificaciones.isNotEmpty()) {
                                Spacer(Modifier.height(14.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Calificaciones de otros pasajeros",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                calificaciones.take(5).forEach { cal ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                cal.pasajero?.nombre ?: "Pasajero",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (!cal.comentario.isNullOrBlank()) {
                                                Text(
                                                    "\"${cal.comentario}\"",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                        .copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                        Row {
                                            (1..5).forEach { i ->
                                                Icon(
                                                    if (i <= cal.puntuacion) Icons.Filled.Star
                                                    else Icons.Filled.StarBorder,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }

                // ── Acciones ───────────────────────────────────────

                // Botón calificar (viaje terminado, confirmada, sin calificar aún)
                when {
                    viajeTermino && v.confirmada && !yaCalificado -> {
                        val idConductor = reserva?.viaje?.vehiculo?.usuario?.idUsuario ?: 0L
                        Button(
                            onClick = {
                                navController.navigate("calificar/${v.idReserva}/$idConductor")
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Star, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Calificar este viaje", fontWeight = FontWeight.Bold)
                        }
                    }
                    viajeTermino && yaCalificado -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle, null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Ya calificaste este viaje",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Botón cancelar reserva (solo si el viaje aún no ocurrió ni está cancelado)
                if (!viajeTermino && v.viaje?.estado != "cancelado") {
                    OutlinedButton(
                        onClick = { mostrarDialogoCancelar = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            )
                        )
                    ) {
                        Icon(Icons.Filled.Cancel, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cancelar reserva", fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InfoFilaReserva(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}