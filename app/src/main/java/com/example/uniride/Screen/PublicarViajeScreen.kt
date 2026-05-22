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

    var ciudadOrigen     by remember { mutableStateOf("") }
    var fechaHora        by remember { mutableStateOf("") }
    var horaSalidaHora   by remember { mutableIntStateOf(6) }
    var horaSalidaMin    by remember { mutableIntStateOf(0) }
    var horaSalidaAMPM   by remember { mutableStateOf("AM") }
    var horaLlegadaHora  by remember { mutableIntStateOf(7) }
    var horaLlegadaMin   by remember { mutableIntStateOf(0) }
    var horaLlegadaAMPM  by remember { mutableStateOf("AM") }
    var costo            by remember { mutableStateOf("") }
    var descripcionPunto by remember { mutableStateOf("") }
    var sedeSeleccionada by remember { mutableStateOf<com.example.uniride.model.Sede?>(null) }
    var idVehiculo       by remember { mutableStateOf<Long?>(null) }
    var error            by remember { mutableStateOf<String?>(null) }

    // Convierte hora 12h a formato 24h string
    fun to24h(hora: Int, min: Int, ampm: String): String {
        val h24 = when {
            ampm == "AM" && hora == 12 -> 0
            ampm == "PM" && hora != 12 -> hora + 12
            else -> hora
        }
        return "%02d:%02d".format(h24, min)
    }

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
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Filled.Warning, null,
                            tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("Primero registra tu vehículo en Mi perfil → Mi vehículo",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Text("Ruta del viaje", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            TextField(ciudadOrigen, { ciudadOrigen = it },
                label = { Text("Ciudad de origen") },
                leadingIcon = { Icon(Icons.Filled.LocationCity, null) },
                placeholder = { Text("Ej: Bogotá, Fusagasugá...") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp))

            SedeDropdown(sedes = sedes ?: emptyList(),
                seleccionada = sedeSeleccionada, onSelect = { sedeSeleccionada = it })

            HorizontalDivider()
            Text("Fecha", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            DateTimePickerField(label = "Fecha del viaje",
                value = fechaHora.take(10),
                onDateSelected = { fechaHora = it },
                modifier = Modifier.fillMaxWidth())

            HorizontalDivider()
            Text("Horario", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            // Hora de salida en formato 12h
            Text("Hora de salida", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                // Hora
                OutlinedTextField(
                    value = "%02d".format(horaSalidaHora),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let { if (it in 1..12) horaSalidaHora = it }
                    },
                    label = { Text("HH") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp))
                Text(":", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge)
                // Minutos
                OutlinedTextField(
                    value = "%02d".format(horaSalidaMin),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let { if (it in 0..59) horaSalidaMin = it }
                    },
                    label = { Text("MM") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp))
                // AM/PM selector
                Column {
                    listOf("AM", "PM").forEach { ampm ->
                        FilterChip(
                            selected = horaSalidaAMPM == ampm,
                            onClick  = { horaSalidaAMPM = ampm },
                            label    = { Text(ampm, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Hora de llegada en formato 12h
            Text("Hora de llegada", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = "%02d".format(horaLlegadaHora),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let { if (it in 1..12) horaLlegadaHora = it }
                    },
                    label = { Text("HH") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp))
                Text(":", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = "%02d".format(horaLlegadaMin),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let { if (it in 0..59) horaLlegadaMin = it }
                    },
                    label = { Text("MM") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp))
                Column {
                    listOf("AM", "PM").forEach { ampm ->
                        FilterChip(
                            selected = horaLlegadaAMPM == ampm,
                            onClick  = { horaLlegadaAMPM = ampm },
                            label    = { Text(ampm, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(32.dp))
                    }
                }
            }

            HorizontalDivider()
            Text("Detalles", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            TextField(costo, { costo = it },
                label = { Text("Costo por puesto ($)") },
                leadingIcon = { Icon(Icons.Filled.AttachMoney, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp))

            TextField(descripcionPunto, { descripcionPunto = it },
                label = { Text("Punto de encuentro") },
                leadingIcon = { Icon(Icons.Filled.Place, null) },
                placeholder = { Text("Describe el punto exacto de encuentro...") },
                maxLines = 3, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp))

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    if (ciudadOrigen.isBlank()) { error = "Ingresa la ciudad de origen"; return@Button }
                    if (sedeSeleccionada == null) { error = "Selecciona la sede destino"; return@Button }
                    if (fechaHora.isBlank()) { error = "Selecciona una fecha"; return@Button }
                    if (costo.isBlank()) { error = "Ingresa el costo"; return@Button }
                    if (idVehiculo == null) { error = "Registra tu vehículo primero"; return@Button }
                    error = null

                    val salida24  = to24h(horaSalidaHora,  horaSalidaMin,  horaSalidaAMPM)
                    val llegada24 = to24h(horaLlegadaHora, horaLlegadaMin, horaLlegadaAMPM)
                    val fechaSalida  = "${fechaHora.take(10)}T$salida24:00"
                    val fechaLlegada = "${fechaHora.take(10)}T$llegada24:00"

                    scope.launch {
                        viajeViewModel.publicarViajeCompleto(
                            ViajeDTO(
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