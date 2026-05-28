package com.example.uniride.Screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ReservaViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Viaje
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajesPorCiudadScreen(
    ciudad: String,
    navController: NavController,
    authViewModel: AuthViewModel       = viewModel(),
    viajeViewModel: ViajeViewModel     = viewModel(),
    reservaViewModel: ReservaViewModel = viewModel(),
    fechaFiltro: String?               = null,
    tipo: String                       = "origen"
) {
    val sesion = authViewModel.sesionActual
    val scope  = rememberCoroutineScope()
    val ahora  = LocalDateTime.now()

    var todos            by remember { mutableStateOf<List<Viaje>>(emptyList()) }
    var cargando         by remember { mutableStateOf(true) }
    var fechaSel         by remember(fechaFiltro) { mutableStateOf(fechaFiltro) }
    var reservasMap      by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }
    var viajesPropiosIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    suspend fun cargar() {
        cargando = true
        try {
            todos = if (tipo == "destino")
                RetrofitClient.apiService.viajesPorDestino(ciudad)
            else
                RetrofitClient.apiService.viajesPorCiudad(ciudad)

            sesion?.idUsuario?.let { uid ->
                try {
                    val vehiculos    = RetrofitClient.apiService.vehiculosPorUsuario(uid)
                    val idsVehiculos = vehiculos.map { it.idVehiculo }.toSet()
                    viajesPropiosIds = todos
                        .filter { v -> v.vehiculo?.idVehiculo?.let { it in idsVehiculos } == true }
                        .map { it.idViaje }
                        .toSet()
                } catch (e: Exception) { viajesPropiosIds = emptySet() }
            }

            val disponibles = todos.filter { it.estado == "disponible" }
            if (disponibles.isNotEmpty()) {
                reservasMap = coroutineScope {
                    disponibles.map { v ->
                        async(Dispatchers.IO) {
                            val count = try {
                                RetrofitClient.apiService.reservasPorViaje(v.idViaje).size
                            } catch (e: Exception) { 0 }
                            v.idViaje to count
                        }
                    }.awaitAll().toMap()
                }
            }
        } catch (e: Exception) { }
        cargando = false
    }

    LaunchedEffect(ciudad, tipo) { scope.launch { cargar() } }

    val base = todos.filter { v ->
        if (v.estado != "disponible") return@filter false
        if (v.idViaje in viajesPropiosIds) return@filter false
        val cupos = v.cuposDisponibles ?: (v.vehiculo?.capacidad ?: Int.MAX_VALUE)
        if (cupos <= 0) return@filter false
        val dt = try {
            LocalDateTime.parse(v.fechaHora.substring(0, minOf(19, v.fechaHora.length)))
        } catch (e: Exception) { null }
        dt != null && !dt.isBefore(ahora)
    }

    val fechas = base.map { it.fechaHora.take(10) }.distinct().sorted()
    val lista  = if (fechaSel == null) base else base.filter { it.fechaHora.take(10) == fechaSel }

    val tituloTopBar = if (tipo == "destino") "Destino: $ciudad" else "Origen: $ciudad"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloTopBar) },
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
        RefreshableContent(
            isRefreshing = cargando,
            onRefresh    = { scope.launch { cargar() } },
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(Modifier.fillMaxSize()) {

                HorizontalDatePicker(fechas, fechaSel) { fechaSel = it }
                HorizontalDivider()

                if (lista.isEmpty() && !cargando) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(Modifier.height(180.dp))
                        Icon(
                            Icons.Filled.SearchOff, null,
                            tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (fechaSel != null) "Sin viajes para esa fecha"
                            else "Sin viajes disponibles para $ciudad",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        lista.forEach { v ->
                            ViajeMiniCard(
                                v             = v,
                                reservasCount = reservasMap[v.idViaje],
                                onClick       = { navController.navigate("viaje_detalle/${v.idViaje}") }
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}