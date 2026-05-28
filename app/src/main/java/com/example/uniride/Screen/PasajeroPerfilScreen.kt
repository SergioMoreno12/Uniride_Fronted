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
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Reserva
import com.example.uniride.model.Usuario
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasajeroPerfilScreen(
    idPasajero: Long,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // sesion = el conductor que está viendo este perfil
    val sesion  = authViewModel.sesionActual
    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    var pasajero     by remember { mutableStateOf<Usuario?>(null) }
    var viajesJuntos by remember { mutableStateOf<List<Reserva>>(emptyList()) }
    var cargando     by remember { mutableStateOf(true) }

    LaunchedEffect(idPasajero) {
        scope.launch {
            try {
                pasajero = RetrofitClient.apiService.obtenerUsuario(idPasajero)

                // Viajes juntos: reservas del pasajero cuyo vehículo pertenece al conductor actual
                val reservasPasajero = RetrofitClient.apiService.reservasPorUsuario(idPasajero)
                viajesJuntos = reservasPasajero
                    .filter { r -> r.viaje?.vehiculo?.usuario?.idUsuario == sesion?.idUsuario }
                    .sortedByDescending { it.viaje?.fechaHora }
            } catch (e: Exception) { }
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil del pasajero") },
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
        } else if (pasajero == null) {
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
            val p = pasajero!!
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
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        p.nombre.first().uppercaseChar().toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ── Nombre y chip rol ────────────────────────────────────
                Text(
                    p.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                AssistChip(
                    onClick = {},
                    label = { Text("Pasajero", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, null, modifier = Modifier.size(16.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor          = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor              = MaterialTheme.colorScheme.secondary,
                        leadingIconContentColor = MaterialTheme.colorScheme.secondary
                    )
                )

                Spacer(Modifier.height(16.dp))

                // ── Botón WhatsApp ───────────────────────────────────────
                val tel = p.telefono
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

                // ── Datos personales ─────────────────────────────────────
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        FilaDatoPasajero(Icons.Filled.Person, "Nombre", p.nombre)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        FilaDatoPasajero(Icons.Filled.Phone,  "Teléfono",
                            if (!p.telefono.isNullOrBlank()) p.telefono!! else "No registrado")
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        FilaDatoPasajero(Icons.Filled.Email,  "Correo", p.correo)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        FilaDatoPasajero(Icons.Filled.CalendarToday, "Miembro desde",
                            p.fechaRegistro.take(10).ifBlank { "—" })
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Historial de viajes juntos ───────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Viajes juntos",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${viajesJuntos.size} viaje${if (viajesJuntos.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (viajesJuntos.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.DirectionsCar, null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(36.dp))
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Aún no han hecho viajes juntos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        viajesJuntos.forEach { r ->
                            val v = r.viaje
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "${v?.origen ?: "—"} → ${v?.destino ?: "—"}",
                                                fontWeight = FontWeight.SemiBold
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
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        )
                                    }
                                    // Estado del viaje
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
                                                Text(
                                                    "· $ ${"%.0f".format(v.costo)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
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
private fun FilaDatoPasajero(
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