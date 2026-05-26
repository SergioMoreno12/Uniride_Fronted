package com.example.uniride.Screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajesScreen(
    navController: NavController,
    viajeViewModel: ViajeViewModel = viewModel(),
    authViewModel: AuthViewModel   = viewModel(),
    fechaFiltro: String?           = null
) {
    val viajes   by viajeViewModel.viajes.observeAsState(emptyList())
    val cargando by viajeViewModel.cargando.observeAsState(false)
    val ahora    = LocalDateTime.now()
    var fechaSel by remember(fechaFiltro) { mutableStateOf(fechaFiltro) }

    LaunchedEffect(true) { viajeViewModel.cargarDisponibles() }

    val viajesBase = (viajes ?: emptyList()).filter { v ->
        if (v.estado != "disponible") return@filter false
        val dt = try {
            LocalDateTime.parse(v.fechaHora.substring(0, minOf(19, v.fechaHora.length)))
        } catch (e: Exception) { null }
        dt != null && !dt.isBefore(ahora)
    }
    val fechas = viajesBase.map { it.fechaHora.take(10) }.distinct().sorted()
    val viajesMostrados = if (fechaSel == null) viajesBase
    else viajesBase.filter { it.fechaHora.take(10) == fechaSel }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Viajes disponibles") },
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
            onRefresh    = { viajeViewModel.cargarDisponibles() },
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(Modifier.fillMaxSize()) {
                HorizontalDatePicker(fechas, fechaSel) { fechaSel = it }
                HorizontalDivider()

                if (viajesMostrados.isEmpty()) {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        Spacer(Modifier.height(180.dp))
                        Text("No hay viajes disponibles",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                } else {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("${viajesMostrados.size} viaje${if (viajesMostrados.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        viajesMostrados.forEach { v ->
                            ViajeMiniCard(v) {
                                navController.navigate("viaje_detalle/${v.idViaje}")
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}