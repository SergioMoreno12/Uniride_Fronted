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
import com.example.uniride.model.Viaje

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminViajesScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val viajes   by viewModel.viajes.observeAsState(emptyList())
    val cargando by viewModel.cargando.observeAsState(false)
    val mensaje  by viewModel.mensaje.observeAsState(null)
    val context = LocalContext.current
    var viajeAEliminar by remember { mutableStateOf<Viaje?>(null) }
    var busqueda by remember { mutableStateOf("") }

    LaunchedEffect(true) { viewModel.cargarViajes() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    val filtrados = (viajes ?: emptyList()).filter {
        it.origen.contains(busqueda, ignoreCase = true) ||
                it.sede?.nombreSede?.contains(busqueda, ignoreCase = true) == true ||
                it.vehiculo?.usuario?.nombre?.contains(busqueda, ignoreCase = true) == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar viajes") },
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
            onRefresh    = { viewModel.cargarViajes() },    // método de recarga
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            TextField(
                value = busqueda, onValueChange = { busqueda = it },
                placeholder = { Text("Buscar por origen, sede o conductor...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (busqueda.isNotEmpty())
                        IconButton(onClick = { busqueda = "" }) { Icon(Icons.Filled.Clear, null) }
                },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))
            Text("${filtrados.size} viajes en el sistema",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))

            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filtrados.forEach { viaje ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Row(modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${viaje.origen} → ${viaje.sede?.nombreSede ?: viaje.destino}",
                                        fontWeight = FontWeight.SemiBold)
                                    Text(viaje.fechaHora.take(16).replace("T", " "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Person, null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(4.dp))
                                        Text(viaje.vehiculo?.usuario?.nombre ?: "-",
                                            style = MaterialTheme.typography.bodySmall)
                                        Spacer(Modifier.width(8.dp))
                                        AssistChip(onClick = {}, label = {
                                            Text(viaje.estado,
                                                style = MaterialTheme.typography.labelSmall)
                                        })
                                    }
                                }
                                IconButton(onClick = { viajeAEliminar = viaje }) {
                                    Icon(Icons.Filled.Delete, null,
                                        tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    viajeAEliminar?.let { v ->
        AlertDialog(
            onDismissRequest = { viajeAEliminar = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Eliminar viaje") },
            text = { Text("¿Eliminar el viaje de ${v.origen} a ${v.sede?.nombreSede ?: v.destino}?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminarViaje(v.idViaje); viajeAEliminar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { viajeAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}
}