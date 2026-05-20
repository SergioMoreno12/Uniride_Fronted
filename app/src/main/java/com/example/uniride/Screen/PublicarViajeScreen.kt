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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.dto.ViajeDTO
import com.example.uniride.ui.theme.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarViajeScreen(
    navController: NavController,
    authViewModel: AuthViewModel   = viewModel(),
    viajeViewModel: ViajeViewModel = viewModel()
) {
    val sesion   = authViewModel.sesionActual
    val sedes    by viajeViewModel.sedes.observeAsState(emptyList())
    val mensaje  by viajeViewModel.mensaje.observeAsState(null)
    val cargando by viajeViewModel.cargando.observeAsState(false)
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()

    var ciudadOrigen    by remember { mutableStateOf("") }
    var fechaHora       by remember { mutableStateOf("") }
    var horaSalida      by remember { mutableStateOf("06:00") }
    var horaLlegada     by remember { mutableStateOf("07:00") }
    var costo           by remember { mutableStateOf("") }
    var descripcionPunto by remember { mutableStateOf("") }
    var sedeSeleccionada by remember { mutableStateOf<com.example.uniride.model.Sede?>(null) }
    var idVehiculo      by remember { mutableStateOf<Long?>(null) }
    var error           by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(true) {
        viajeViewModel.cargarSedes()
        sesion?.idUsuario?.let { id ->
            try {
                val vehiculos = RetrofitClient.apiService.vehiculosPorUsuario(id)
                idVehiculo = vehiculos.firstOrNull()?.idVehiculo
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viajeViewModel.limpiarMensaje()
            if (it.contains("éxito")) navController.navigate(Routes.MIS_VIAJES) {
                popUpTo(Routes.PUBLICAR) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publicar viaje") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            BottomNavBar(currentRoute = "publicar", rol = "conductor") { route ->
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (idVehiculo == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Filled.Warning, null,
                            tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material3.Text(
                            "Primero debes registrar tu vehículo en Mi perfil → Mi vehículo",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Text("Ruta del viaje", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            // Ciudad de origen
            TextField(ciudadOrigen, { ciudadOrigen = it },
                label = { Text("Ciudad de origen") },
                leadingIcon = { Icon(Icons.Filled.LocationCity, null) },
                placeholder = { Text("Ej: Bogotá, Fusagasugá...") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp))

            // Sede destino
            SedeDropdown(
                sedes = sedes ?: emptyList(),
                seleccionada = sedeSeleccionada,
                onSelect = { sedeSeleccionada = it }
            )

            HorizontalDivider()
            Text("Fecha y hora", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            DateTimePickerField(
                label = "Fecha del viaje",
                value = fechaHora.take(10),
                onDateSelected = { fechaHora = it },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextField(horaSalida, { horaSalida = it },
                    label = { Text("Hora salida") },
                    leadingIcon = { Icon(Icons.Filled.AccessTime, null) },
                    placeholder = { Text("06:00") },
                    singleLine = true, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp))

                TextField(horaLlegada, { horaLlegada = it },
                    label = { Text("Hora llegada") },
                    leadingIcon = { Icon(Icons.Filled.AccessTime, null) },
                    placeholder = { Text("07:00") },
                    singleLine = true, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp))
            }

            HorizontalDivider()
            Text("Detalles del viaje", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            TextField(costo, { costo = it },
                label = { Text("Costo por puesto ($)") },
                leadingIcon = { Icon(Icons.Filled.AttachMoney, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp))

            TextField(descripcionPunto, { descripcionPunto = it },
                label = { Text("Punto de encuentro") },
                leadingIcon = { Icon(Icons.Filled.Place, null) },
                placeholder = { Text("Describe el punto de encuentro exacto...") },
                maxLines = 3, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp))

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (ciudadOrigen.isBlank()) { error = "Ingresa la ciudad de origen"; return@Button }
                    if (sedeSeleccionada == null) { error = "Selecciona la sede destino"; return@Button }
                    if (fechaHora.isBlank()) { error = "Selecciona una fecha"; return@Button }
                    if (costo.isBlank()) { error = "Ingresa el costo"; return@Button }
                    if (idVehiculo == null) { error = "Registra tu vehículo primero"; return@Button }

                    error = null
                    val fechaSalida   = "${fechaHora.take(10)}T$horaSalida:00"
                    val fechaLlegada  = "${fechaHora.take(10)}T$horaLlegada:00"

                    scope.launch {
                        viajeViewModel.publicarViajeCompleto(
                            dto = ViajeDTO(
                                origen           = ciudadOrigen,
                                destino          = sedeSeleccionada!!.nombreSede,
                                fechaHora        = fechaSalida,
                                horaLlegada      = fechaLlegada,
                                costo            = costo.toDoubleOrNull() ?: 0.0,
                                estado           = "disponible",
                                descripcionPunto = descripcionPunto.ifBlank { null },
                                idVehiculo       = idVehiculo!!,
                                idSede           = sedeSeleccionada!!.idSede
                            )
                        )
                    }
                },
                enabled = !cargando && idVehiculo != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (cargando) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text("Publicar viaje", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}