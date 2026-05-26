package com.example.uniride.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AdminViewModel
import com.example.uniride.model.Vehiculo
import com.example.uniride.model.Viaje

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuarioDetalleScreen(
    idUsuario:      Long,
    navController:  NavController,
    adminViewModel: AdminViewModel = viewModel()
) {
    val usuarios           by adminViewModel.usuarios.observeAsState(emptyList())
    val vehiculosUsuario   by adminViewModel.vehiculosUsuario.observeAsState(emptyList())
    val viajesUsuario      by adminViewModel.viajesUsuario.observeAsState(emptyList())
    val reservasUsuario    by adminViewModel.reservasUsuario.observeAsState(emptyList())
    val calificaciones     by adminViewModel.calificacionesUsuario.observeAsState(emptyList())
    val reportesUsuario    by adminViewModel.reportesUsuario.observeAsState(emptyList())
    val promedio           by adminViewModel.promedioUsuario.observeAsState(0.0)
    val cargandoDetalle    by adminViewModel.cargandoDetalle.observeAsState(false)
    val mensaje            by adminViewModel.mensaje.observeAsState(null)
    val context            = LocalContext.current

    val usuario = (usuarios ?: emptyList()).find { it.idUsuario == idUsuario }
    val esConductor = usuario?.rol == "conductor"

    var mostrarDialogoEliminarUsuario  by remember { mutableStateOf(false) }
    var mostrarDialogoEliminarVehiculo by remember { mutableStateOf<Vehiculo?>(null) }
    var mostrarDialogoEliminarViaje    by remember { mutableStateOf<Viaje?>(null) }

    LaunchedEffect(usuario?.idUsuario, usuario?.rol) {
        if (usuario != null) {
            adminViewModel.cargarDetalleUsuario(idUsuario, usuario.rol == "conductor")
        }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            adminViewModel.limpiarMensaje()
        }
    }

    // ── Diálogo eliminar usuario ────────────────────────────────────────
    if (mostrarDialogoEliminarUsuario && usuario != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminarUsuario = false },
            shape            = RoundedCornerShape(20.dp),
            icon             = {
                Icon(Icons.Filled.Warning, null,
                    tint = MaterialTheme.colorScheme.error)
            },
            title = { Text("Eliminar usuario", fontWeight = FontWeight.Bold) },
            text  = {
                Text("¿Eliminar la cuenta de ${usuario.nombre}?\n\n" +
                        "Se eliminarán también todos sus vehículos, viajes, " +
                        "reservas, reportes y calificaciones. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoEliminarUsuario = false
                        adminViewModel.eliminarUsuario(idUsuario) {
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sí, eliminar todo") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminarUsuario = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── Diálogo eliminar vehículo ───────────────────────────────────────
    mostrarDialogoEliminarVehiculo?.let { v ->
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminarVehiculo = null },
            shape            = RoundedCornerShape(20.dp),
            icon             = { Icon(Icons.Filled.DirectionsCar, null,
                tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar vehículo") },
            text  = {
                Text("¿Eliminar el vehículo ${v.marca} ${v.modelo} (${v.placa})?\n\n" +
                        "También se eliminarán todos sus viajes y reservas asociadas.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoEliminarVehiculo = null
                        adminViewModel.eliminarVehiculo(v.idVehiculo, idUsuario, esConductor)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminarVehiculo = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── Diálogo eliminar viaje ──────────────────────────────────────────
    mostrarDialogoEliminarViaje?.let { v ->
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminarViaje = null },
            shape            = RoundedCornerShape(20.dp),
            icon             = { Icon(Icons.Filled.Route, null,
                tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar viaje") },
            text  = {
                Text("¿Eliminar el viaje de ${v.origen} a " +
                        "${v.sede?.nombreSede ?: v.destino}?\n\n" +
                        "También se eliminarán todas las reservas y calificaciones asociadas.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoEliminarViaje = null
                        adminViewModel.eliminarViaje(v.idViaje, idUsuario, esConductor)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminarViaje = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(usuario?.nombre ?: "Detalle de usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        adminViewModel.cargarDetalleUsuario(idUsuario,
                            usuario?.rol == "conductor")
                    }) {
                        Icon(Icons.Filled.Refresh, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        RefreshableContent(
            isRefreshing = cargandoDetalle,
            onRefresh    = {
                adminViewModel.cargarDetalleUsuario(idUsuario,
                    usuario?.rol == "conductor")
            },
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
        if (usuario == null) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Tarjeta de perfil ───────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = if (usuario.activo)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape    = RoundedCornerShape(12.dp),
                            color    = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    usuario.nombre.first().uppercaseChar().toString(),
                                    fontSize   = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(usuario.nombre,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp)
                            Text(usuario.correo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Text(usuario.telefono ?: "Sin teléfono",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(Modifier.height(12.dp))

                    // Chips de estado
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {},
                            label   = {
                                Text(usuario.rol.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall)
                            },
                            leadingIcon = {
                                Icon(
                                    if (esConductor) Icons.Filled.DirectionsCar
                                    else Icons.Filled.Person,
                                    null, modifier = Modifier.size(14.dp))
                            }
                        )
                        AssistChip(
                            onClick = {},
                            label   = {
                                Text(if (usuario.activo) "Activo" else "Bloqueado",
                                    style = MaterialTheme.typography.labelSmall)
                            },
                            leadingIcon = {
                                Icon(
                                    if (usuario.activo) Icons.Filled.CheckCircle
                                    else Icons.Filled.Block,
                                    null, modifier = Modifier.size(14.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (usuario.activo)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.errorContainer,
                                labelColor = if (usuario.activo)
                                    MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.error,
                                leadingIconContentColor = if (usuario.activo)
                                    MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.error)
                        )
                        Text("Desde ${usuario.fechaRegistro.take(10)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.CenterVertically))
                    }

                    if (esConductor && (promedio ?: 0.0) > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { i ->
                                Icon(
                                    if (i < (promedio ?: 0.0).toInt()) Icons.Filled.Star
                                    else Icons.Filled.StarBorder,
                                    null,
                                    tint     = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text("${"%.1f".format(promedio)} (${calificaciones?.size ?: 0} calificaciones)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ── Acciones del admin ──────────────────────────────────────
            SeccionTitulo("Acciones", Icons.Filled.AdminPanelSettings)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = { adminViewModel.toggleActivoUsuario(idUsuario) },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (usuario.activo)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        if (usuario.activo) Icons.Filled.Block
                        else Icons.Filled.CheckCircle,
                        null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (usuario.activo) "Bloquear" else "Activar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick  = {
                        val nuevo = if (esConductor) "pasajero" else "conductor"
                        adminViewModel.cambiarRol(idUsuario, nuevo)
                    },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        if (esConductor) Icons.Filled.Person
                        else Icons.Filled.DirectionsCar,
                        null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (esConductor) "→ Pasajero" else "→ Conductor",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick  = { mostrarDialogoEliminarUsuario = true },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Filled.DeleteForever, null)
                Spacer(Modifier.width(8.dp))
                Text("Eliminar usuario y todos sus datos",
                    fontWeight = FontWeight.Bold)
            }

            // ── Vehículos ───────────────────────────────────────────────
            if (esConductor) {
                SeccionTitulo(
                    "Vehículos (${(vehiculosUsuario ?: emptyList()).size})",
                    Icons.Filled.DirectionsCar
                )
                if ((vehiculosUsuario ?: emptyList()).isEmpty()) {
                    InfoVacia("No tiene vehículos registrados")
                } else {
                    (vehiculosUsuario ?: emptyList()).forEach { vehiculo ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier          = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.DirectionsCar, null,
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${vehiculo.marca} ${vehiculo.modelo}",
                                        fontWeight = FontWeight.SemiBold)
                                    Text("Placa: ${vehiculo.placa} · ${vehiculo.capacidad} puestos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                IconButton(onClick = {
                                    mostrarDialogoEliminarVehiculo = vehiculo
                                }) {
                                    Icon(Icons.Filled.Delete, null,
                                        tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                // ── Viajes ──────────────────────────────────────────────
                SeccionTitulo(
                    "Viajes publicados (${(viajesUsuario ?: emptyList()).size})",
                    Icons.Filled.Route
                )
                if ((viajesUsuario ?: emptyList()).isEmpty()) {
                    InfoVacia("No tiene viajes publicados")
                } else {
                    (viajesUsuario ?: emptyList()).forEach { viaje ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier          = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${viaje.origen} → ${viaje.sede?.nombreSede ?: viaje.destino}",
                                        fontWeight = FontWeight.SemiBold)
                                    Text(
                                        viaje.fechaHora.take(16).replace("T", " "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment     = Alignment.CenterVertically
                                    ) {
                                        AssistChip(
                                            onClick = {},
                                            label   = {
                                                Text(viaje.estado,
                                                    style = MaterialTheme.typography.labelSmall)
                                            },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = when (viaje.estado) {
                                                    "disponible" -> MaterialTheme.colorScheme.secondaryContainer
                                                    "lleno"      -> MaterialTheme.colorScheme.tertiaryContainer
                                                    "cancelado"  -> MaterialTheme.colorScheme.errorContainer
                                                    else         -> MaterialTheme.colorScheme.surfaceVariant
                                                })
                                        )
                                        Text("$${viaje.costo.toInt()}",
                                            style      = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color      = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                IconButton(onClick = {
                                    mostrarDialogoEliminarViaje = viaje
                                }) {
                                    Icon(Icons.Filled.Delete, null,
                                        tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            // ── Reservas como pasajero ──────────────────────────────────
            SeccionTitulo(
                "Reservas como pasajero (${(reservasUsuario ?: emptyList()).size})",
                Icons.Filled.BookOnline
            )
            if ((reservasUsuario ?: emptyList()).isEmpty()) {
                InfoVacia("No tiene reservas registradas")
            } else {
                (reservasUsuario ?: emptyList()).take(5).forEach { reserva ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier          = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.BookOnline, null,
                                tint     = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${reserva.viaje?.origen ?: "-"} → " +
                                            "${reserva.viaje?.sede?.nombreSede ?: reserva.viaje?.destino ?: "-"}",
                                    fontWeight = FontWeight.SemiBold,
                                    style      = MaterialTheme.typography.bodyMedium)
                                Text("Reserva del ${reserva.fechaReserva.take(10)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            AssistChip(
                                onClick = {},
                                label   = {
                                    Text(
                                        if (reserva.confirmada) "Confirmada" else "Pendiente",
                                        style = MaterialTheme.typography.labelSmall)
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (reserva.confirmada)
                                        MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }
                }
                if ((reservasUsuario ?: emptyList()).size > 5) {
                    Text(
                        "+${(reservasUsuario ?: emptyList()).size - 5} reservas más",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp))
                }
            }

            // ── Calificaciones recibidas (solo conductor) ───────────────
            if (esConductor && (calificaciones ?: emptyList()).isNotEmpty()) {
                SeccionTitulo(
                    "Calificaciones recibidas (${(calificaciones ?: emptyList()).size})",
                    Icons.Filled.Star
                )
                (calificaciones ?: emptyList()).take(3).forEach { calif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier          = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(5) { i ->
                                        Icon(
                                            if (i < calif.puntuacion) Icons.Filled.Star
                                            else Icons.Filled.StarBorder,
                                            null,
                                            tint     = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text("${calif.puntuacion}/5",
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.tertiary,
                                        style      = MaterialTheme.typography.bodySmall)
                                }
                                if (!calif.comentario.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("\"${calif.comentario}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                                Text("Por: ${calif.pasajero?.nombre ?: "-"} · ${calif.fechaCalificacion.take(10)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
                if ((calificaciones ?: emptyList()).size > 3) {
                    Text(
                        "+${(calificaciones ?: emptyList()).size - 3} calificaciones más",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp))
                }
            }

            // ── Reportes del usuario ────────────────────────────────────
            SeccionTitulo(
                "Reportes (${(reportesUsuario ?: emptyList()).size})",
                Icons.Filled.Report
            )
            if ((reportesUsuario ?: emptyList()).isEmpty()) {
                InfoVacia("No tiene reportes")
            } else {
                (reportesUsuario ?: emptyList()).forEach { reporte ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = when (reporte.estado) {
                                "pendiente"  -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                "resuelto"   -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                else         -> MaterialTheme.colorScheme.surfaceVariant
                            })
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(reporte.titulo,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier   = Modifier.weight(1f))
                                AssistChip(
                                    onClick = {},
                                    label   = {
                                        Text(reporte.estado.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelSmall)
                                    }
                                )
                            }
                            Text(reporte.descripcion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Text(reporte.fechaReporte.take(10),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))

                            if (reporte.estado == "pendiente") {
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick  = {
                                        adminViewModel.actualizarEstadoReporte(
                                            reporte.idReporte, "resuelto")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape    = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.CheckCircle, null,
                                        modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Marcar como resuelto",
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
}
}

// ── Componentes auxiliares ────────────────────────────────────────────────────
@Composable
private fun SeccionTitulo(titulo: String, icono: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.padding(top = 4.dp)
    ) {
        Icon(icono, null,
            tint     = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(titulo,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun InfoVacia(texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Info, null,
                tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(texto,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}