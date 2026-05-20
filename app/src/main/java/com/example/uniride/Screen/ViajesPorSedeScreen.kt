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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajesPorSedeScreen(
    idSede: Long,
    navController: NavController,
    authViewModel: AuthViewModel       = viewModel(),
    viajeViewModel: ViajeViewModel     = viewModel(),
    reservaViewModel: ReservaViewModel = viewModel()
) {
    val scope    = rememberCoroutineScope()
    var viajes   by remember { mutableStateOf<List<Viaje>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var nombreSede by remember { mutableStateOf("") }

    LaunchedEffect(idSede) {
        scope.launch {
            try {
                val todos = RetrofitClient.apiService.viajesPorSede(idSede)
                viajes = todos.filter { it.estado == "disponible" }
                nombreSede = viajes.firstOrNull()?.sede?.nombreSede ?: "Sede"
            } catch (e: Exception) { }
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreSede) },
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
        if (cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (viajes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.School, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("No hay viajes disponibles a esta sede",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                viajes.forEach { viaje ->
                    ViajeCard(viaje = viaje) {
                        navController.navigate("viaje_detalle/${viaje.idViaje}")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}