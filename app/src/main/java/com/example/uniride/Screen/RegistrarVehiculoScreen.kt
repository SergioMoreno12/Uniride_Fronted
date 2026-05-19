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
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarVehiculoScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val sesion   = authViewModel.sesionActual
    val cargando by perfilViewModel.cargando.observeAsState(false)
    val mensaje  by perfilViewModel.mensaje.observeAsState(null)
    val misVehiculos by perfilViewModel.misVehiculos.observeAsState(emptyList())
    val context  = LocalContext.current

    var placa    by remember { mutableStateOf("") }
    var marca    by remember { mutableStateOf("") }
    var modelo   by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        sesion?.idUsuario?.let { perfilViewModel.cargarMisVehiculos(it) }
    }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            perfilViewModel.limpiarMensaje()
            if (it.contains("éxito")) {
                placa = ""; marca = ""; modelo = ""; capacidad = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis vehículos") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Formulario nuevo vehículo
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Registrar nuevo vehículo", fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)

                    TextField(placa, { placa = it.uppercase() },
                        label = { Text("Placa") },
                        leadingIcon = { Icon(Icons.Filled.ConfirmationNumber, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp))

                    TextField(marca, { marca = it },
                        label = { Text("Marca") },
                        leadingIcon = { Icon(Icons.Filled.DirectionsCar, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp))

                    TextField(modelo, { modelo = it },
                        label = { Text("Modelo") },
                        leadingIcon = { Icon(Icons.Filled.DriveEta, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp))

                    TextField(capacidad, { capacidad = it.filter { c -> c.isDigit() } },
                        label = { Text("Puestos disponibles") },
                        leadingIcon = { Icon(Icons.Filled.EventSeat, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp))

                    Button(
                        onClick = {
                            sesion?.idUsuario?.let {
                                perfilViewModel.registrarVehiculo(
                                    placa, marca, modelo,
                                    capacidad.toIntOrNull() ?: 0, it
                                )
                            }
                        },
                        enabled = !cargando && placa.isNotBlank() && marca.isNotBlank()
                                && modelo.isNotBlank() && capacidad.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (cargando)
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Filled.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Registrar vehículo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Lista de mis vehículos
            val lista = misVehiculos ?: emptyList()
            if (lista.isNotEmpty()) {
                Text("Mis vehículos (${lista.size})", fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                lista.forEach { vehiculo ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DirectionsCar, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${vehiculo.marca} ${vehiculo.modelo}",
                                    fontWeight = FontWeight.SemiBold)
                                Text("Placa: ${vehiculo.placa} · ${vehiculo.capacidad} puestos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}