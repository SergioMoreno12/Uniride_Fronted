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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.interfaces.RetrofitClient
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
    val viajesReservados by viajeViewModel.viajesReservados.observeAsState(emptySet())
    val viajesPropios    by viajeViewModel.viajesPropios.observeAsState(emptySet())
    val cargando         by viajeViewModel.cargando.observeAsState(false)
    val ahora            = LocalDateTime.now()

    var fechaSel     by remember { mutableStateOf<String?>(null) }
    var tipoViajeSel by remember { mutableStateOf<String?>(null) }
    var reservasMap  by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }

    LaunchedEffect(sesion?.idUsuario) {
        viajeViewModel.cargarDisponibles(sesion?.idUsuario)
        viajeViewModel.cargarSedes()
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

    // Nombres de sedes para excluirlos de la lista de ciudades
    val sedeNombres = (sedes ?: emptyList())
        .map { it.nombreSede.trim().lowercase() }
        .toSet()

    // Ciudades únicas de origen (excluye nombres de sedes)
    val ciudadesOrigen = viajesDisponibles
        .map { it.origen.trim() }
        .filter { it.lowercase() !in sedeNombres }
        .distinct()
        .sorted()

    // Ciudades únicas de destino (excluye nombres de sedes)
    val ciudadesDestino = viajesDisponibles
        .map { it.destino.trim() }
        .filter { it.lowercase() !in sedeNombres }
        .distinct()
        .sorted()

    val fechasDisponibles = viajesDisponibles
        .map { it.fechaHora.take(10) }
        .distinct()
        .sorted()

    val viajesMostrados = viajesDisponibles
        .let { l -> if (fechaSel == null) l else l.filter { it.fechaHora.take(10) == fechaSel } }
        .let { l -> if (tipoViajeSel == null) l else l.filter { it.tipoViaje == tipoViajeSel } }

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
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Filtro de fecha ──────────────────────────────────────
                HorizontalDatePicker(fechasDisponibles, fechaSel) { fechaSel = it }

                // ── Filtro de tipo de viaje ──────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tipo:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    FilterChip(
                        selected = tipoViajeSel == null,
                        onClick  = { tipoViajeSel = null },
                        label    = { Text("Todos") }
                    )
                    FilterChip(
                        selected = tipoViajeSel == "ida",
                        onClick  = { tipoViajeSel = if (tipoViajeSel == "ida") null else "ida" },
                        label    = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.School, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Ida")
                            }
                        }
                    )
                    FilterChip(
                        selected = tipoViajeSel == "vuelta",
                        onClick  = { tipoViajeSel = if (tipoViajeSel == "vuelta") null else "vuelta" },
                        label    = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Home, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Vuelta")
                            }
                        }
                    )
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Hola, ${sesion?.nombre ?: "usuario"} 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // ── Banner de filtros activos ─────────────────────────
                    if (fechaSel != null || tipoViajeSel != null) {
                        Card(
                            shape  = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.FilterAlt, null,
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                val desc = listOfNotNull(
                                    fechaSel?.let { "fecha: $it" },
                                    tipoViajeSel?.let { "tipo: $it" }
                                ).joinToString(", ")
                                Text(
                                    "Filtros activos: $desc",
                                    style    = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = {
                                    fechaSel     = null
                                    tipoViajeSel = null
                                }) { Text("Limpiar") }
                            }
                        }
                    }

                    // ── Por sede ─────────────────────────────────────────
                    if ((sedes ?: emptyList()).isNotEmpty()) {
                        Text("Por sede", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        (sedes ?: emptyList()).forEach { sede ->
                            Card(
                                onClick = {
                                    val ruta = buildString {
                                        append("viajes_por_sede/${sede.idSede}")
                                        if (fechaSel != null) append("?fecha=$fechaSel")
                                    }
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
                                        Text(sede.nombreSede,
                                            fontWeight = FontWeight.SemiBold)
                                        Text(sede.ciudad,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    Icon(Icons.Filled.ChevronRight, null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }

                    // ── Por conductor ─────────────────────────────────────
                    Text("Por conductor", fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Card(
                        onClick   = { navController.navigate("lista_conductores") },
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.People, null,
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Buscar por conductor",
                                    fontWeight = FontWeight.SemiBold)
                                Text("Ver todos los conductores disponibles",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Icon(Icons.Filled.ChevronRight, null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }
                    }

                    // ── Por ciudad de origen ──────────────────────────────
                    if (ciudadesOrigen.isNotEmpty()) {
                        Text("Por ciudad de origen", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        ciudadesOrigen.take(5).forEach { ciudad ->
                            Card(
                                onClick = {
                                    val ruta = buildString {
                                        append("viajes_por_ciudad/$ciudad")
                                        if (fechaSel != null) append("?fecha=$fechaSel")
                                    }
                                    navController.navigate(ruta)
                                },
                                modifier  = Modifier.fillMaxWidth(),
                                shape     = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.TripOrigin, null,
                                        tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ciudad, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${viajesDisponibles.count { it.origen.equals(ciudad, ignoreCase = true) }} viaje(s) disponible(s)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    Icon(Icons.Filled.ChevronRight, null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                        if (ciudadesOrigen.size > 5) {
                            TextButton(
                                onClick  = { navController.navigate("viajes") },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Ver ${ciudadesOrigen.size - 5} ciudades más") }
                        }
                    }

                    // ── Por ciudad de destino ─────────────────────────────
                    if (ciudadesDestino.isNotEmpty()) {
                        Text("Por ciudad de destino", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        ciudadesDestino.take(5).forEach { ciudad ->
                            Card(
                                onClick = {
                                    val ruta = buildString {
                                        append("viajes_por_destino/$ciudad")
                                        if (fechaSel != null) append("?fecha=$fechaSel")
                                    }
                                    navController.navigate(ruta)
                                },
                                modifier  = Modifier.fillMaxWidth(),
                                shape     = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Place, null,
                                        tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ciudad, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${viajesDisponibles.count { it.destino.equals(ciudad, ignoreCase = true) }} viaje(s) disponible(s)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    Icon(Icons.Filled.ChevronRight, null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                        if (ciudadesDestino.size > 5) {
                            TextButton(
                                onClick  = { navController.navigate("viajes") },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Ver ${ciudadesDestino.size - 5} ciudades más") }
                        }
                    }

                    // ── Viajes disponibles ────────────────────────────────
                    HorizontalDivider()
                    Text(
                        when {
                            fechaSel != null && tipoViajeSel != null ->
                                "Viajes de $tipoViajeSel del $fechaSel"
                            fechaSel != null     -> "Viajes del $fechaSel"
                            tipoViajeSel != null -> "Viajes de $tipoViajeSel"
                            else                 -> "Viajes disponibles"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (viajesMostrados.isEmpty() && !cargando) {
                        Card(modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.SearchOff, null,
                                        tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(36.dp))
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        if (fechaSel != null || tipoViajeSel != null)
                                            "No hay viajes con esos filtros"
                                        else "No hay viajes disponibles",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text("Desliza hacia abajo para recargar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    } else {
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
                                    val ruta = if (fechaSel != null)
                                        "viajes?fecha=$fechaSel" else "viajes"
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