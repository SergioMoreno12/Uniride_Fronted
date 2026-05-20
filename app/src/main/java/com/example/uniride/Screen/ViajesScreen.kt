package com.example.uniride.Screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ViajeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajesScreen(
    navController: NavController,
    viajeViewModel: ViajeViewModel = viewModel(),
    authViewModel: AuthViewModel   = viewModel()
) {
    var busqueda         by remember { mutableStateOf("") }
    val viajes           by viajeViewModel.viajes.observeAsState(emptyList())
    val cargando         by viajeViewModel.cargando.observeAsState(false)
    val viajesReservados by viajeViewModel.viajesReservados.observeAsState(emptySet())
    val viajesPropios    by viajeViewModel.viajesPropios.observeAsState(emptySet())
    val sesion           = authViewModel.sesionActual

    LaunchedEffect(true) {
        viajeViewModel.cargarDisponibles(sesion?.idUsuario)
    }

    // Filtrar: sin los ya reservados, sin los propios, y por búsqueda
    val filtrados = (viajes ?: emptyList()).filter { viaje ->
        val yaReservo = viaje.idViaje in (viajesReservados ?: emptySet())
        val esPropio  = viaje.idViaje in (viajesPropios ?: emptySet())
        val coincide  = viaje.origen.contains(busqueda, ignoreCase = true) ||
                viaje.sede?.nombreSede?.contains(busqueda, ignoreCase = true) == true ||
                viaje.sede?.ciudad?.contains(busqueda, ignoreCase = true) == true
        !yaReservo && !esPropio && coincide
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar viajes") },
                actions = {
                    IconButton(onClick = {
                        viajeViewModel.cargarDisponibles(sesion?.idUsuario)
                    }) {
                        Icon(Icons.Filled.Refresh, "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TextField(
                value = busqueda, onValueChange = { busqueda = it },
                placeholder = { Text("Buscar por origen o sede...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (busqueda.isNotEmpty())
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Filled.Clear, null)
                        }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(8.dp))

            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filtrados.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(top = 48.dp),
                            contentAlignment = Alignment.Center) {
                            Text("No se encontraron viajes disponibles",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    } else {
                        filtrados.forEach { viaje ->
                            ViajeCard(viaje = viaje) {
                                navController.navigate("viaje_detalle/${viaje.idViaje}")
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}