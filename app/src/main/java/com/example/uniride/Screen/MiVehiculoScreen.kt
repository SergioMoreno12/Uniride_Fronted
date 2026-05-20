package com.example.uniride.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Vehiculo
import com.example.uniride.model.dto.VehiculoDTO
import com.example.uniride.ui.theme.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiVehiculoScreen(
    navController: NavController,
    authViewModel: AuthViewModel     = viewModel(),
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val sesion       = authViewModel.sesionActual
    val misVehiculos by perfilViewModel.misVehiculos.observeAsState(emptyList())
    val mensaje      by perfilViewModel.mensaje.observeAsState(null)
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()

    var vehiculoActual by remember { mutableStateOf<Vehiculo?>(null) }
    var editando       by remember { mutableStateOf(false) }
    var placa    by remember { mutableStateOf("") }
    var marca    by remember { mutableStateOf("") }
    var modelo   by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }
    var cargando  by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        sesion?.idUsuario?.let {
            perfilViewModel.cargarMisVehiculos(it)
        }
    }

    LaunchedEffect(misVehiculos) {
        val v = (misVehiculos ?: emptyList()).firstOrNull()
        vehiculoActual = v
        v?.let {
            placa     = it.placa
            marca     = it.marca
            modelo    = it.modelo
            capacidad = it.capacidad.toString()
        }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            perfilViewModel.limpiarMensaje()
            if (it.contains("éxito")) editando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi vehículo") },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (vehiculoActual == null && !editando) {
                // Sin vehículo registrado
                Box(Modifier.fillMaxWidth().padding(top = 48.dp),
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.DirectionsCar, null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Spacer(Modifier.height(8.dp))
                        Text("No tienes un vehículo registrado",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { editando = true },
                            shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Filled.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Registrar vehículo")
                        }
                    }
                }
            } else if (!editando) {
                // Mostrar datos del vehículo
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DirectionsCar, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("${vehiculoActual?.marca} ${vehiculoActual?.modelo}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider()
                        VehiculoInfoRow("Placa",    vehiculoActual?.placa ?: "-")
                        VehiculoInfoRow("Marca",    vehiculoActual?.marca ?: "-")
                        VehiculoInfoRow("Modelo",   vehiculoActual?.modelo ?: "-")
                        VehiculoInfoRow("Puestos",  "${vehiculoActual?.capacidad}")
                    }
                }

                Button(
                    onClick = { editando = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Edit, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Editar vehículo", fontWeight = FontWeight.Bold)
                }
            } else {
                // Formulario editar/registrar
                Text(
                    if (vehiculoActual == null) "Registrar vehículo" else "Editar vehículo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                TextField(placa.uppercase(), { placa = it.uppercase() },
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

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { editando = false },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancelar") }

                    Button(
                        onClick = {
                            cargando = true
                            scope.launch {
                                try {
                                    val cap = capacidad.toIntOrNull() ?: 0
                                    if (vehiculoActual != null) {
                                        RetrofitClient.apiService.editarVehiculo(
                                            vehiculoActual!!.idVehiculo,
                                            VehiculoDTO(placa, marca, modelo, cap,
                                                sesion?.idUsuario ?: 0L)
                                        )
                                    } else {
                                        perfilViewModel.registrarVehiculo(
                                            placa, marca, modelo, cap, sesion?.idUsuario ?: 0L)
                                    }
                                    sesion?.idUsuario?.let {
                                        perfilViewModel.cargarMisVehiculos(it)
                                    }
                                    editando = false
                                    Toast.makeText(context, "Vehículo guardado con éxito",
                                        Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}",
                                        Toast.LENGTH_SHORT).show()
                                }
                                cargando = false
                            }
                        },
                        enabled = !cargando && placa.isNotBlank() && marca.isNotBlank()
                                && modelo.isNotBlank() && capacidad.isNotBlank(),
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (cargando) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun VehiculoInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}