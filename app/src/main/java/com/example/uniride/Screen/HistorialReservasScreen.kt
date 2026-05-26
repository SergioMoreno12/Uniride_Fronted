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
import com.example.uniride.ViewModel.ReservaViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialReservasScreen(
    navController: NavController,
    authViewModel: AuthViewModel       = viewModel(),
    reservaViewModel: ReservaViewModel = viewModel()
) {
    val sesion   = authViewModel.sesionActual
    val reservas by reservaViewModel.misReservas.observeAsState(emptyList())
    val cargando by reservaViewModel.cargando.observeAsState(false)
    val ahora    = LocalDateTime.now()

    LaunchedEffect(true) {
        sesion?.idUsuario?.let { reservaViewModel.cargarMisReservas(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de reservas") },
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
            onRefresh    = {
                sesion?.idUsuario?.let { reservaViewModel.cargarMisReservas(it) }
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            val historial = (reservas ?: emptyList()).filter { r ->
                val dt = try {
                    val raw = r.viaje?.fechaHora ?: ""
                    LocalDateTime.parse(raw.substring(0, minOf(19, raw.length)))
                } catch (e: Exception) { null }
                (dt != null && dt.isBefore(ahora)) ||
                        r.viaje?.estado == "completado" ||
                        r.viaje?.estado == "cancelado"
            }.sortedByDescending { it.viaje?.fechaHora }

            if (historial.isEmpty()) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Spacer(Modifier.height(180.dp))
                    Icon(Icons.Filled.History, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Text("Sin historial de reservas",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            } else {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${historial.size} reserva${if (historial.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    historial.forEach { r ->
                        Card(
                            onClick = {
                                navController.navigate("reserva_detalle/${r.idReserva}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("${r.viaje?.origen ?: "-"} → ${r.viaje?.destino ?: "-"}",
                                    fontWeight = FontWeight.SemiBold)
                                Text(r.viaje?.fechaHora?.take(16)?.replace("T", " ") ?: "-",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("Estado: ${r.viaje?.estado ?: "-"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}