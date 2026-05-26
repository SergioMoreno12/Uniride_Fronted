package com.example.uniride.Screen

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AdminViewModel
import com.example.uniride.model.Reporte

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportesScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val reportes by viewModel.reportes.observeAsState(emptyList())
    val cargando by viewModel.cargando.observeAsState(false)
    val mensaje  by viewModel.mensaje.observeAsState(null)
    val context = LocalContext.current
    var reporteSeleccionado by remember { mutableStateOf<Reporte?>(null) }
    var nuevoEstado by remember { mutableStateOf("") }

    LaunchedEffect(true) { viewModel.cargarReportes() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes de conducta") },
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
            isRefreshing = cargando,                          // estado del adminViewModel
            onRefresh    = {viewModel.cargarReportes()},    // metodo de recarga
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (cargando) {
                Box(Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val lista = reportes ?: emptyList()

                Text("${lista.size} reportes recibidos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                if (lista.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.CheckCircle, null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            Spacer(Modifier.height(8.dp))
                            Text("Sin reportes pendientes",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    lista.forEach { reporte ->
                        Card(modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.Report, null,
                                            tint = if (reporte.estado == "pendiente")
                                                MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(reporte.titulo, fontWeight = FontWeight.SemiBold)
                                    }
                                    AssistChip(onClick = {}, label = {
                                        Text(reporte.estado,
                                            style = MaterialTheme.typography.labelSmall)
                                    })
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(reporte.descripcion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Spacer(Modifier.height(4.dp))
                                Text("Reportado por: ${reporte.usuario?.nombre ?: "-"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            reporteSeleccionado = reporte
                                            nuevoEstado = "archivado"
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Archivar",
                                            style = MaterialTheme.typography.labelSmall)
                                    }
                                    Button(
                                        onClick = {
                                            reporteSeleccionado = reporte
                                            nuevoEstado = "revisado"
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Marcar revisado",
                                            style = MaterialTheme.typography.labelSmall)
                                    }
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

    reporteSeleccionado?.let { r ->
        AlertDialog(
            onDismissRequest = { reporteSeleccionado = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Actualizar reporte") },
            text = { Text("¿Marcar el reporte \"${r.titulo}\" como $nuevoEstado?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.actualizarEstadoReporte(r.idReporte, nuevoEstado)
                    reporteSeleccionado = null
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { reporteSeleccionado = null }) { Text("Cancelar") }
            }
        )
    }
}