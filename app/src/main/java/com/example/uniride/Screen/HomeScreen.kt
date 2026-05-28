package com.example.uniride.Screen

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Viaje
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel   = viewModel(),
    viajeViewModel: ViajeViewModel = viewModel()
) {
    val sesion           = authViewModel.sesionActual
    val viajes           by viajeViewModel.viajes.observeAsState(emptyList())
    val sedes            by viajeViewModel.sedes.observeAsState(emptyList())
    val conductores      by viajeViewModel.conductores.observeAsState(emptyList())
    val viajesReservados by viajeViewModel.viajesReservados.observeAsState(emptySet())
    val viajesPropios    by viajeViewModel.viajesPropios.observeAsState(emptySet())
    val cargando         by viajeViewModel.cargando.observeAsState(false)
    val ahora            = LocalDateTime.now()
    var fechaSel         by remember { mutableStateOf<String?>(null) }

    // Mapa idViaje → reservasCount, cargado en paralelo
    var reservasMap by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }

    LaunchedEffect(sesion?.idUsuario) {
        viajeViewModel.cargarDisponibles(sesion?.idUsuario)
        viajeViewModel.cargarSedes()
        viajeViewModel.cargarConductores()
    }

    val viajesDisponibles = (viajes ?: emptyList()).filter { v ->
        if (v.estado != "disponible") return@filter false
        if (v.idViaje in (viajesReservados ?: emptySet())) return@filter false
        if (v.idViaje in (viajesPropios ?: emptySet())) return@filter false
        val cupos = v.cuposDisponibles ?: (v.vehiculo?.capacidad ?: Int.MAX_VALUE)
        if (cupos <= 0) return@filter false
        val dt = try {
            LocalDateTime.parse(v.fechaHora.substring(0, minOf(19, v.fechaHora.length)))
        } catch (e: Exception) { null }
        dt != null && !dt.isBefore(ahora)
    }
    val fechasDisponibles = viajesDisponibles.map { it.fechaHora.take(10) }.distinct().sorted()
    val viajesMostrados   = if (fechaSel == null) viajesDisponibles
    else viajesDisponibles.filter { it.fechaHora.take(10) == fechaSel }

    // Cargar conteos de reservas en paralelo cuando cambia la lista visible
    LaunchedEffect(viajesMostrados) {
        val visibles = viajesMostrados.take(10)
        if (visibles.isNotEmpty()) {
            val resultado = visibles.map { v ->
                async(Dispatchers.IO) {
                    val count = try {
                        RetrofitClient.apiService.reservasPorViaje(v.idViaje).size
                    } catch (e: Exception) { 0 }
                    v.idViaje to count
                }
            }.awaitAll().toMap()
            reservasMap = resultado
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("UniRide", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        RefreshableContent(
            isRefreshing = cargando,
            onRefresh    = {
                reservasMap = emptyMap()
                viajeViewModel.cargarDisponibles(sesion?.idUsuario)
                viajeViewModel.cargarSedes()
                viajeViewModel.cargarConductores()
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HorizontalDatePicker(fechasDisponibles, fechaSel) { fechaSel = it }
                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Hola, ${sesion?.nombre ?: "usuario"} 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)

                    if (fechaSel != null) {
                        Card(shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Row(modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.FilterAlt, null,
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Mostrando viajes del $fechaSel",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f))
                                TextButton(onClick = { fechaSel = null }) { Text("Limpiar") }
                            }
                        }
                    }

                    // ── Por sede ─────────────────────────────────────────
                    if ((sedes ?: emptyList()).isNotEmpty()) {
                        Text("Por sede", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        (sedes ?: emptyList()).forEach { sede ->
                            Card(
                                onClick   = {
                                    val ruta = if (fechaSel != null)
                                        "viajes_por_sede/${sede.idSede}?fecha=$fechaSel"
                                    else "viajes_por_sede/${sede.idSede}"
                                    navController.navigate(ruta)
                                },
                                modifier  = Modifier.fillMaxWidth(),
                                shape     = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.School, null,
                                        tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sede.nombreSede, fontWeight = FontWeight.SemiBold)
                                        Text(sede.ciudad, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    Icon(Icons.Filled.ChevronRight, null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }

                    // ── Por conductor ────────────────────────────────────
                    if ((conductores ?: emptyList()).isNotEmpty()) {
                        Text("Por conductor", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        (conductores ?: emptyList()).forEach { conductor ->
                            Card(
                                onClick   = {
                                    val ruta = if (fechaSel != null)
                                        "viajes_por_conductor/${conductor.idUsuario}?fecha=$fechaSel"
                                    else "viajes_por_conductor/${conductor.idUsuario}"
                                    navController.navigate(ruta)
                                },
                                modifier  = Modifier.fillMaxWidth(),
                                shape     = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    if (!conductor.fotoPerfil.isNullOrBlank()) {
                                        AsyncImage(model = conductor.fotoPerfil, null,
                                            modifier = Modifier.size(40.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop)
                                    } else {
                                        Box(modifier = Modifier.size(40.dp).clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center) {
                                            Text(conductor.nombre.firstOrNull()
                                                ?.uppercaseChar()?.toString() ?: "?",
                                                fontSize   = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color      = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(conductor.nombre, fontWeight = FontWeight.SemiBold)
                                        Text(conductor.correo,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    Icon(Icons.Filled.ChevronRight, null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }

                    // ── Viajes disponibles ───────────────────────────────
                    HorizontalDivider()
                    Text(if (fechaSel != null) "Viajes del $fechaSel" else "Viajes disponibles",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)

                    if (viajesMostrados.isEmpty() && !cargando) {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.SearchOff, null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(36.dp))
                                    Spacer(Modifier.height(6.dp))
                                    Text(if (fechaSel != null) "No hay viajes para esa fecha"
                                    else "No hay viajes disponibles",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    Text("Desliza hacia abajo para recargar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    } else {
                        // Usar ViajeMiniCard con reservasCount del mapa
                        viajesMostrados.take(10).forEach { v ->
                            ViajeMiniCard(
                                v             = v,
                                reservasCount = reservasMap[v.idViaje],
                                onClick       = { navController.navigate("viaje_detalle/${v.idViaje}") }
                            )
                        }
                        if (viajesMostrados.size > 10) {
                            TextButton(
                                onClick  = {
                                    val ruta = if (fechaSel != null) "viajes?fecha=$fechaSel" else "viajes"
                                    navController.navigate(ruta)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Ver todos los ${viajesMostrados.size} viajes") }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}