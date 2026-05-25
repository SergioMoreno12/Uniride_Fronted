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

    // ✅ Tipo de viaje: "ida" o "vuelta"
    var tipoViaje by remember { mutableStateOf("ida") }

    // Texto libre para la ciudad (origen si ida, destino si vuelta)
    var ciudad   by remember { mutableStateOf("") }
    var fechaHora by remember { mutableStateOf("") }

    // ✅ Estados de hora como STRING para que se puedan editar libremente
    var horaSalidaHoraTxt  by remember { mutableStateOf("06") }
    var horaSalidaMinTxt   by remember { mutableStateOf("00") }
    var horaSalidaAMPM     by remember { mutableStateOf("AM") }
    var horaLlegadaHoraTxt by remember { mutableStateOf("07") }
    var horaLlegadaMinTxt  by remember { mutableStateOf("00") }
    var horaLlegadaAMPM    by remember { mutableStateOf("AM") }

    var costo            by remember { mutableStateOf("") }
    var descripcionPunto by remember { mutableStateOf("") }
    var sedeSeleccionada by remember { mutableStateOf<com.example.uniride.model.Sede?>(null) }
    var idVehiculo       by remember { mutableStateOf<Long?>(null) }
    var error            by remember { mutableStateOf<String?>(null) }

    // Helper: convierte hora 12h a "HH:mm" 24h
    fun to24h(horaTxt: String, minTxt: String, ampm: String): String {
        val h12 = horaTxt.toIntOrNull()?.coerceIn(1, 12) ?: 12
        val mm  = minTxt.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val h24 = when {
            ampm == "AM" && h12 == 12 -> 0
            ampm == "PM" && h12 != 12 -> h12 + 12
            else -> h12
        }
        return "%02d:%02d".format(h24, mm)
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

            // ════════════════════════════════════════════════════════════════
            // ✅ NUEVO: Selector de tipo de viaje
            // ════════════════════════════════════════════════════════════════
            Text("Tipo de viaje", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = tipoViaje == "ida",
                    onClick  = { tipoViaje = "ida" },
                    label    = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.School, null,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Ida (a la U)")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = tipoViaje == "vuelta",
                    onClick  = { tipoViaje = "vuelta" },
                    label    = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Home, null,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Vuelta (desde la U)")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Aclaración del tipo
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Info, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (tipoViaje == "ida")
                            "Recoges pasajeros en una ciudad y los llevas a la universidad."
                        else
                            "Recoges pasajeros en la universidad y los dejas en una ciudad.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            HorizontalDivider()

            Text("Ruta del viaje", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            // ════════════════════════════════════════════════════════════════
            // ✅ UI condicional: ida vs vuelta
            // ════════════════════════════════════════════════════════════════
            if (tipoViaje == "ida") {
                // IDA: ciudad → sede
                TextField(
                    value = ciudad, onValueChange = { ciudad = it },
                    label = { Text("Ciudad de origen") },
                    leadingIcon = { Icon(Icons.Filled.LocationCity, null) },
                    placeholder = { Text("Ej: Bogotá, Fusagasugá...") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                SedeDropdown(
                    sedes = sedes ?: emptyList(),
                    seleccionada = sedeSeleccionada,
                    onSelect = { sedeSeleccionada = it }
                )
            } else {
                // VUELTA: sede → ciudad
                SedeDropdown(
                    sedes = sedes ?: emptyList(),
                    seleccionada = sedeSeleccionada,
                    onSelect = { sedeSeleccionada = it }
                )

                TextField(
                    value = ciudad, onValueChange = { ciudad = it },
                    label = { Text("Ciudad de destino") },
                    leadingIcon = { Icon(Icons.Filled.LocationCity, null) },
                    placeholder = { Text("Ej: Bogotá, Fusagasugá...") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            HorizontalDivider()
            Text("Fecha", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            DateTimePickerField(
                label = "Fecha del viaje",
                value = fechaHora.take(10),
                onDateSelected = { fechaHora = it },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()
            Text("Horario", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            // ════════════════════════════════════════════════════════════════
            // ✅ Hora de salida (campos STRING, editables sin trabas)
            // ════════════════════════════════════════════════════════════════
            Text("Hora de salida",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = horaSalidaHoraTxt,
                    onValueChange = { v ->
                        // ✅ Permite vacío y dígitos, máximo 2 caracteres
                        val cleaned = v.filter { it.isDigit() }.take(2)
                        horaSalidaHoraTxt = cleaned
                    },
                    label = { Text("HH") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(":", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = horaSalidaMinTxt,
                    onValueChange = { v ->
                        val cleaned = v.filter { it.isDigit() }.take(2)
                        horaSalidaMinTxt = cleaned
                    },
                    label = { Text("MM") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Column {
                    listOf("AM", "PM").forEach { ampm ->
                        FilterChip(
                            selected = horaSalidaAMPM == ampm,
                            onClick  = { horaSalidaAMPM = ampm },
                            label    = {
                                Text(ampm,
                                    style = MaterialTheme.typography.labelSmall)
                            },
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }

            // Hora de llegada
            Text("Hora de llegada",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = horaLlegadaHoraTxt,
                    onValueChange = { v ->
                        val cleaned = v.filter { it.isDigit() }.take(2)
                        horaLlegadaHoraTxt = cleaned
                    },
                    label = { Text("HH") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(":", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = horaLlegadaMinTxt,
                    onValueChange = { v ->
                        val cleaned = v.filter { it.isDigit() }.take(2)
                        horaLlegadaMinTxt = cleaned
                    },
                    label = { Text("MM") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Column {
                    listOf("AM", "PM").forEach { ampm ->
                        FilterChip(
                            selected = horaLlegadaAMPM == ampm,
                            onClick  = { horaLlegadaAMPM = ampm },
                            label    = {
                                Text(ampm,
                                    style = MaterialTheme.typography.labelSmall)
                            },
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }

            HorizontalDivider()
            Text("Detalles", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            TextField(
                value = costo,
                onValueChange = { costo = it.filter { c -> c.isDigit() } },
                label = { Text("Costo por puesto ($)") },
                leadingIcon = { Icon(Icons.Filled.AttachMoney, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            TextField(
                value = descripcionPunto, onValueChange = { descripcionPunto = it },
                label = { Text("Punto de encuentro") },
                leadingIcon = { Icon(Icons.Filled.Place, null) },
                placeholder = {
                    Text(
                        if (tipoViaje == "ida")
                            "¿Dónde recoges a los pasajeros en la ciudad?"
                        else
                            "¿Dónde recoges a los pasajeros en la universidad?"
                    )
                },
                maxLines = 3, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            error?.let {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Error, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = {
                    // ── Validaciones del formulario ──
                    if (ciudad.isBlank()) {
                        error = if (tipoViaje == "ida")
                            "Ingresa la ciudad de origen"
                        else "Ingresa la ciudad de destino"
                        return@Button
                    }
                    if (sedeSeleccionada == null) {
                        error = "Selecciona la sede de la universidad"; return@Button
                    }
                    if (fechaHora.isBlank()) {
                        error = "Selecciona una fecha"; return@Button
                    }
                    if (costo.isBlank()) {
                        error = "Ingresa el costo"; return@Button
                    }
                    if (idVehiculo == null) {
                        error = "Registra tu vehículo primero"; return@Button
                    }

                    // Validar que las horas tengan valor
                    val hSal  = horaSalidaHoraTxt.toIntOrNull()
                    val mSal  = horaSalidaMinTxt.toIntOrNull()
                    val hLleg = horaLlegadaHoraTxt.toIntOrNull()
                    val mLleg = horaLlegadaMinTxt.toIntOrNull()
                    if (hSal == null || hSal !in 1..12 ||
                        mSal == null || mSal !in 0..59) {
                        error = "Hora de salida inválida (HH 1-12, MM 0-59)"
                        return@Button
                    }
                    if (hLleg == null || hLleg !in 1..12 ||
                        mLleg == null || mLleg !in 0..59) {
                        error = "Hora de llegada inválida (HH 1-12, MM 0-59)"
                        return@Button
                    }
                    error = null

                    val salida24  = to24h(horaSalidaHoraTxt,  horaSalidaMinTxt,  horaSalidaAMPM)
                    val llegada24 = to24h(horaLlegadaHoraTxt, horaLlegadaMinTxt, horaLlegadaAMPM)
                    val fechaSalida  = "${fechaHora.take(10)}T$salida24:00"
                    val fechaLlegada = "${fechaHora.take(10)}T$llegada24:00"

                    // ✅ Armar origen y destino según el tipo
                    val origenFinal  = if (tipoViaje == "ida")
                        ciudad else sedeSeleccionada!!.nombreSede
                    val destinoFinal = if (tipoViaje == "ida")
                        sedeSeleccionada!!.nombreSede else ciudad

                    scope.launch {
                        viajeViewModel.publicarViajeCompleto(
                            ViajeDTO(
                                origen           = origenFinal,
                                destino          = destinoFinal,
                                fechaHora        = fechaSalida,
                                horaLlegada      = fechaLlegada,
                                costo            = costo.toDoubleOrNull() ?: 0.0,
                                estado           = "disponible",
                                descripcionPunto = descripcionPunto.ifBlank { null },
                                tipoViaje        = tipoViaje,        // ✅
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
                if (cargando) CircularProgressIndicator(
                    modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text("Publicar viaje", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}