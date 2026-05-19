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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.ui.theme.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    viajeViewModel: ViajeViewModel = viewModel()
) {
    val sesion   = authViewModel.sesionActual
    val viajes   by viajeViewModel.viajes.observeAsState(emptyList())
    val cargando by viajeViewModel.cargando.observeAsState(false)
    val mensaje  by viajeViewModel.mensaje.observeAsState(null)
    val context = LocalContext.current
    val navBack by navController.currentBackStackEntryAsState()
    val currentRoute = navBack?.destination?.route ?: ""

    LaunchedEffect(true) { viajeViewModel.cargarDisponibles() }
    LaunchedEffect(mensaje) {
        mensaje?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viajeViewModel.limpiarMensaje() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${sesion?.nombre?.split(" ")?.first() ?: ""}",
                            fontWeight = FontWeight.Bold)
                        Text("Viajes disponibles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                actions = {
                    IconButton(onClick = { viajeViewModel.cargarDisponibles() }) {
                        Icon(Icons.Filled.Refresh, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            BottomNavBar(currentRoute = "home", rol = sesion?.rol ?: "usuario") { route ->
                navController.navigate(route) {
                    popUpTo(Routes.HOME) { saveState = true }
                    launchSingleTop = true; restoreState = true
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (cargando) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val lista = viajes ?: emptyList()
                if (lista.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.DirectionsCar, null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            Spacer(Modifier.height(8.dp))
                            Text("No hay viajes disponibles",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        lista.forEach { viaje ->
                            ViajeCard(viaje = viaje) {
                                navController.navigate("viaje_detalle/${viaje.idViaje}")
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── ViajeCard ─────────────────────────────────────────────────────
@Composable
fun ViajeCard(viaje: com.example.uniride.model.Viaje, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Column {
                    Text("Desde", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(viaje.origen, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.School, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Column {
                    Text("Sede destino", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(viaje.sede?.nombreSede ?: viaje.destino, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccessTime, null,
                        tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(viaje.fechaHora.take(16).replace("T", " "),
                        style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AttachMoney, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Text("${"%.0f".format(viaje.costo)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DirectionsCar, null,
                    tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${viaje.vehiculo?.marca} ${viaje.vehiculo?.modelo} · ${viaje.vehiculo?.capacidad} puestos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}