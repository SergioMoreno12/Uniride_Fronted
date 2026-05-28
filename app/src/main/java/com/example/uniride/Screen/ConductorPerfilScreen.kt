package com.example.uniride.Screen

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Calificacion
import com.example.uniride.model.Reserva
import com.example.uniride.model.Usuario
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConductorPerfilScreen(
    idConductor: Long,
    navController: NavController,
    authViewModel: AuthViewModel   = viewModel(),
    viajeViewModel: ViajeViewModel = viewModel()
) {
    // sesion = el pasajero que está viendo este perfil
    val sesion  = authViewModel.sesionActual
    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    var conductor      by remember { mutableStateOf<Usuario?>(null) }
    var calificaciones by remember { mutableStateOf<List<Calificacion>>(emptyList()) }
    var promedio       by remember { mutableStateOf(0.0) }
    var viajesJuntos   by remember { mutableStateOf<List<Reserva>>(emptyList()) }
    var cargando       by remember { mutableStateOf(true) }

    // Controles de expansión UI
    var mostrarTodosComentarios by remember { mutableStateOf(false) }
    var mostrarViajesJuntos     by remember { mutableStateOf(true) }

    LaunchedEffect(idConductor) {
        scope.launch {
            try {
                conductor      = RetrofitClient.apiService.obtenerUsuario(idConductor)
                calificaciones = RetrofitClient.apiService.calificacionesConductor(idConductor)
                promedio       = RetrofitClient.apiService.promedioConductor(idConductor)

                // Viajes juntos: reservas del pasajero actual en viajes de este conductor
                sesion?.idUsuario?.let { idPasajero ->
                    val misReservas = RetrofitClient.apiService.reservasPorUsuario(idPasajero)
                    viajesJuntos = misReservas
                        .filter { r -> r.viaje?.vehiculo?.usuario?.idUsuario == idConductor }
                        .sortedByDescending { it.viaje?.fechaHora }
                }
            } catch (e: Exception) { }
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil del conductor") },
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
        if (cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (conductor == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.ErrorOutline, null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No se pudo cargar el perfil",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { navController.popBackStack() }) {
                        Text("Volver")
                    }
                }
            }
        } else {
            val c = conductor!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // ── Avatar ───────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        c.nombre.first().uppercaseChar().toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ── Nombre ───────────────────────────────────────────────
                Text(
                    c.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))

                // ── Chip de rol ──────────────────────────────────────────
                AssistChip(
                    onClick = {},
                    label = { Text("Conductor", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(Icons.Filled.DirectionsCar, null, modifier = Modifier.size(16.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor          = MaterialTheme.colorScheme.primaryContainer,
                        labelColor              = MaterialTheme.colorScheme.primary,
                        leadingIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(8.dp))

                // ── Estrellas debajo del nombre ──────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { i ->
                        Icon(
                            if (i <= promedio.toInt()) Icons.Filled.Star
                            else Icons.Filled.StarBorder,
                            null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${"%.1f".format(promedio)} · ${calificaciones.size} calificacion${if (calificaciones.size != 1) "es" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Botón WhatsApp ───────────────────────────────────────
                val tel = c.telefono
                if (!tel.isNullOrBlank()) {
                    Button(
                        onClick = {
                            val numero = tel.filter { it.isDigit() }
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://wa.me/57$numero"))
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Filled.Chat, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Contactar por WhatsApp", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // ── Datos de contacto ────────────────────────────────────
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        FilaDatoConductor(Icons.Filled.Phone, "Teléfono",
                            if (!c.telefono.isNullOrBlank()) c.telefono!! else "No registrado")
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        FilaDatoConductor(Icons.Filled.Email, "Correo", c.correo)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        FilaDatoConductor(Icons.Filled.CalendarToday, "Miembro desde",
                            c.fechaRegistro.take(10).ifBlank { "—" })
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Viajes juntos ────────────────────────────────────────
                if (sesion?.rol != "administrador") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            // Cabecera clickeable para expandir/colapsar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.DirectionsCar, null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Mis viajes con este conductor",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${viajesJuntos.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { mostrarViajesJuntos = !mostrarViajesJuntos },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            if (mostrarViajesJuntos) Icons.Filled.ExpandLess
                                            else Icons.Filled.ExpandMore,
                                            null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }

                            if (mostrarViajesJuntos) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                                if (viajesJuntos.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Filled.SearchOff, null,
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                                modifier = Modifier.size(32.dp))
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "Aún no has viajado con este conductor",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.padding(
                                            start = 16.dp, end = 16.dp,
                                            bottom = 16.dp, top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        viajesJuntos.forEach { r ->
                                            val v = r.viaje
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                "${v?.origen ?: "—"} → ${v?.destino ?: "—"}",
                                                                fontWeight = FontWeight.SemiBold,
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                            Text(
                                                                v?.fechaHora?.take(16)?.replace("T", " ") ?: "—",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                            )
                                                        }
                                                        AssistChip(
                                                            onClick = {},
                                                            label = {
                                                                Text(
                                                                    if (r.confirmada) "Confirmado" else "Pendiente",
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            },
                                                            colors = AssistChipDefaults.assistChipColors(
                                                                containerColor = if (r.confirmada)
                                                                    MaterialTheme.colorScheme.primaryContainer
                                                                else MaterialTheme.colorScheme.surface
                                                            )
                                                        )
                                                    }
                                                    v?.estado?.let { estado ->
                                                        Spacer(Modifier.height(4.dp))
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                when (estado) {
                                                                    "completado" -> Icons.Filled.CheckCircle
                                                                    "cancelado"  -> Icons.Filled.Cancel
                                                                    else         -> Icons.Filled.Schedule
                                                                },
                                                                null,
                                                                tint = when (estado) {
                                                                    "completado" -> MaterialTheme.colorScheme.secondary
                                                                    "cancelado"  -> MaterialTheme.colorScheme.error
                                                                    else         -> MaterialTheme.colorScheme.primary
                                                                },
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Spacer(Modifier.width(4.dp))
                                                            Text(
                                                                estado.replaceFirstChar { it.uppercase() },
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                            )
                                                            if (v.costo > 0) {
                                                                Spacer(Modifier.width(10.dp))
                                                                Text("· $ ${"%.0f".format(v.costo)}",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.primary,
                                                                    fontWeight = FontWeight.SemiBold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }

                // ── Sección de comentarios ───────────────────────────────
                if (calificaciones.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.StarRate, null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Comentarios de pasajeros",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            "${calificaciones.size} total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Mostrar 3 comentarios por defecto; expandir al tocar "Ver todos"
                    val calificacionesMostradas = if (mostrarTodosComentarios)
                        calificaciones
                    else
                        calificaciones.take(3)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        calificacionesMostradas.forEach { cal ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    (cal.pasajero?.nombre?.firstOrNull()
                                                        ?.uppercaseChar()?.toString()) ?: "P",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                cal.pasajero?.nombre ?: "Pasajero",
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Row {
                                            (1..5).forEach { i ->
                                                Icon(
                                                    if (i <= cal.puntuacion) Icons.Filled.Star
                                                    else Icons.Filled.StarBorder,
                                                    null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (!cal.comentario.isNullOrBlank()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "\"${cal.comentario}\"",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        cal.fechaCalificacion.take(10),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }

                        // Botón "Ver todos / Ver menos"
                        if (calificaciones.size > 3) {
                            OutlinedButton(
                                onClick = { mostrarTodosComentarios = !mostrarTodosComentarios },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    if (mostrarTodosComentarios) Icons.Filled.ExpandLess
                                    else Icons.Filled.ExpandMore,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (mostrarTodosComentarios)
                                        "Ver menos comentarios"
                                    else
                                        "Ver todos los ${calificaciones.size} comentarios"
                                )
                            }
                        }
                    }
                } else {
                    // Sin calificaciones aún
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.StarBorder, null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(36.dp))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Aún no tiene calificaciones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FilaDatoConductor(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}