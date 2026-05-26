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
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajesPorSedeScreen(
    idSede: Long,
    navController: NavController,
    authViewModel: AuthViewModel       = viewModel(),
    viajeViewModel: ViajeViewModel     = viewModel(),
    reservaViewModel: ReservaViewModel = viewModel(),
    fechaFiltro: String?               = null
) {
    val scope = rememberCoroutineScope()
    val ahora = LocalDateTime.now()
    var todos    by remember { mutableStateOf<List<Viaje>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var fechaSel by remember(fechaFiltro) { mutableStateOf(fechaFiltro) }

    suspend fun cargar() {
        cargando = true
        try { todos = RetrofitClient.apiService.viajesPorSede(idSede) }
        catch (e: Exception) { }
        cargando = false
    }

    LaunchedEffect(idSede) { scope.launch { cargar() } }

    val base = todos.filter { v ->
        if (v.estado != "disponible") return@filter false
        val dt = try {
            LocalDateTime.parse(v.fechaHora.substring(0, minOf(19, v.fechaHora.length)))
        } catch (e: Exception) { null }
        dt != null && !dt.isBefore(ahora)
    }
    val fechas = base.map { it.fechaHora.take(10) }.distinct().sorted()
    val lista  = if (fechaSel == null) base
    else base.filter { it.fechaHora.take(10) == fechaSel }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Viajes por sede") },
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

                if (lista.isEmpty()) {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        Spacer(Modifier.height(180.dp))
                        Text("Sin viajes disponibles",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                } else {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        lista.forEach { v ->
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