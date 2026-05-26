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
import com.example.uniride.model.Sede
import com.example.uniride.model.Viaje
import com.example.uniride.model.dto.ViajeDTO
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarViajeScreen(
    idViaje: Long,
    navController: NavController,
    authViewModel: AuthViewModel   = viewModel(),
    viajeViewModel: ViajeViewModel = viewModel()
) {
    val sedes   by viajeViewModel.sedes.observeAsState(emptyList())
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var viaje      by remember { mutableStateOf<Viaje?>(null) }
    var cargando   by remember { mutableStateOf(true) }
    var guardando  by remember { mutableStateOf(false) }
    var error      by remember { mutableStateOf<String?>(null) }

    // Campos del formulario
    var tipoViaje by remember { mutableStateOf("ida") }
    var ciudad    by remember { mutableStateOf("") }
    var fechaTxt  by remember { mutableStateOf("") }
    var hSalHora  by remember { mutableStateOf("06") }
    var hSalMin   by remember { mutableStateOf("00") }
    var hSalAmpm  by remember { mutableStateOf("AM") }
    var hLleHora  by remember { mutableStateOf("07") }
    var hLleMin   by remember { mutableStateOf("00") }
    var hLleAmpm  by remember { mutableStateOf("AM") }
    var costo     by remember { mutableStateOf("") }
    var punto     by remember { mutableStateOf("") }
    var sedeSel   by remember { mutableStateOf<Sede?>(null) }

    LaunchedEffect(idViaje) {
        viajeViewModel.cargarSedes()
        try {
            val v = RetrofitClient.apiService.obtenerViaje(idViaje)
            viaje = v
            tipoViaje = v.tipoViaje ?: "ida"
            fechaTxt  = v.fechaHora.take(10)
            sedeSel   = v.sede
            // Para "ida": ciudad = origen, sede = destino. Para "vuelta": al revés
            ciudad    = if (tipoViaje == "ida") v.origen else v.destino
            costo     = v.costo.toInt().toString()
            punto     = v.descripcionPunto ?: ""

            // Parsear hora salida
            val h24Sal = v.fechaHora.substring(11, 13).toIntOrNull() ?: 6
            val mmSal  = v.fechaHora.substring(14, 16)
            hSalHora = when {
                h24Sal == 0  -> "12"
                h24Sal > 12  -> "%02d".format(h24Sal - 12)
                else         -> "%02d".format(h24Sal)
            }
            hSalMin  = mmSal
            hSalAmpm = if (h24Sal >= 12) "PM" else "AM"

            // Parsear hora llegada
            v.horaLlegada?.let { hl ->
                val h24Lle = hl.substring(11, 13).toIntOrNull() ?: 7
                val mmLle  = hl.substring(14, 16)
                hLleHora = when {
                    h24Lle == 0  -> "12"
                    h24Lle > 12  -> "%02d".format(h24Lle - 12)
                    else         -> "%02d".format(h24Lle)
                }
                hLleMin  = mmLle
                hLleAmpm = if (h24Lle >= 12) "PM" else "AM"
            }
        } catch (e: Exception) {
            error = "No se pudo cargar el viaje"
        }
        cargando = false
    }

    fun to24h(txt: String, minTxt: String, ampm: String): Pair<Int, Int> {
        val h12 = txt.toIntOrNull()?.coerceIn(1, 12) ?: 12
        val mm  = minTxt.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val h24 = when {
            ampm == "AM" && h12 == 12 -> 0
            ampm == "PM" && h12 != 12 -> h12 + 12
            else -> h12
        }
        return h24 to mm
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar viaje") },
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
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tipo de viaje", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = tipoViaje == "ida",
                    onClick  = { tipoViaje = "ida" },
                    label    = { Text("Ida (a la U)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = tipoViaje == "vuelta",
                    onClick  = { tipoViaje = "vuelta" },
                    label    = { Text("Vuelta (desde la U)") },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()
            Text("Ruta", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            if (tipoViaje == "ida") {
                TextField(value = ciudad, onValueChange = { ciudad = it },
                    label = { Text("Ciudad de origen") },
                    leadingIcon = { Icon(Icons.Filled.LocationCity, null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp))
                SedeDropdown(sedes = sedes ?: emptyList(),
                    seleccionada = sedeSel, onSelect = { sedeSel = it })
            } else {
                SedeDropdown(sedes = sedes ?: emptyList(),
                    seleccionada = sedeSel, onSelect = { sedeSel = it })
                TextField(value = ciudad, onValueChange = { ciudad = it },
                    label = { Text("Ciudad de destino") },
                    leadingIcon = { Icon(Icons.Filled.LocationCity, null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp))
            }

            HorizontalDivider()
            Text("Fecha y horario", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            DateTimePickerField(
                label = "Fecha del viaje",
                value = fechaTxt,
                onDateSelected = { fechaTxt = it },
                modifier = Modifier.fillMaxWidth(),
                minDate = LocalDate.now()
            )

            // Hora salida
            Text("Hora de salida",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = hSalHora,
                    onValueChange = { hSalHora = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("HH") }, singleLine = true,
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                Text(":", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(value = hSalMin,
                    onValueChange = { hSalMin = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("MM") }, singleLine = true,
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                Column {
                    listOf("AM", "PM").forEach { ap ->
                        FilterChip(selected = hSalAmpm == ap,
                            onClick = { hSalAmpm = ap },
                            label = { Text(ap, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Hora llegada
            Text("Hora de llegada",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = hLleHora,
                    onValueChange = { hLleHora = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("HH") }, singleLine = true,
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                Text(":", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(value = hLleMin,
                    onValueChange = { hLleMin = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("MM") }, singleLine = true,
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                Column {
                    listOf("AM", "PM").forEach { ap ->
                        FilterChip(selected = hLleAmpm == ap,
                            onClick = { hLleAmpm = ap },
                            label = { Text(ap, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(32.dp))
                    }
                }
            }

            HorizontalDivider()
            Text("Detalles", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            TextField(value = costo,
                onValueChange = { costo = it.filter { c -> c.isDigit() } },
                label = { Text("Costo por puesto ($)") },
                leadingIcon = { Icon(Icons.Filled.AttachMoney, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp))

            TextField(value = punto, onValueChange = { punto = it },
                label = { Text("Punto de encuentro") },
                leadingIcon = { Icon(Icons.Filled.Place, null) },
                maxLines = 3, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp))

            error?.let {
                Card(shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Filled.Error, null,
                            tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = {
                    if (ciudad.isBlank()) { error = "Ingresa la ciudad"; return@Button }
                    if (sedeSel == null) { error = "Selecciona la sede"; return@Button }
                    if (fechaTxt.isBlank()) { error = "Selecciona la fecha"; return@Button }
                    if (costo.isBlank()) { error = "Ingresa el costo"; return@Button }

                    val (h24Sal, mmSal) = to24h(hSalHora, hSalMin, hSalAmpm)
                    val (h24Lle, mmLle) = to24h(hLleHora, hLleMin, hLleAmpm)
                    val fecha = try { LocalDate.parse(fechaTxt) }
                    catch (e: Exception) { null }
                    if (fecha == null) { error = "Fecha inválida"; return@Button }

                    val dtSal = LocalDateTime.of(fecha, LocalTime.of(h24Sal, mmSal))
                    val dtLle = LocalDateTime.of(fecha, LocalTime.of(h24Lle, mmLle))

                    if (dtSal.isBefore(LocalDateTime.now())) {
                        error = "La hora de salida ya pasó"; return@Button
                    }
                    if (!dtLle.isAfter(dtSal)) {
                        error = "La llegada debe ser posterior a la salida"; return@Button
                    }
                    error = null

                    val origenFinal  = if (tipoViaje == "ida") ciudad else sedeSel!!.nombreSede
                    val destinoFinal = if (tipoViaje == "ida") sedeSel!!.nombreSede else ciudad
                    val fechaSal = "$fechaTxt" + "T%02d:%02d:00".format(h24Sal, mmSal)
                    val fechaLle = "$fechaTxt" + "T%02d:%02d:00".format(h24Lle, mmLle)

                    guardando = true
                    scope.launch {
                        try {
                            RetrofitClient.apiService.actualizarViaje(
                                idViaje,
                                ViajeDTO(
                                    origen           = origenFinal,
                                    destino          = destinoFinal,
                                    fechaHora        = fechaSal,
                                    horaLlegada      = fechaLle,
                                    costo            = costo.toDoubleOrNull() ?: 0.0,
                                    estado           = viaje?.estado ?: "disponible",
                                    descripcionPunto = punto.ifBlank { null },
                                    tipoViaje        = tipoViaje,
                                    idVehiculo       = viaje?.vehiculo?.idVehiculo ?: 0L,
                                    idSede           = sedeSel!!.idSede
                                )
                            )
                            Toast.makeText(context, "Viaje actualizado",
                                Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            error = e.message ?: "Error al actualizar"
                        } finally {
                            guardando = false
                        }
                    }
                },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (guardando) CircularProgressIndicator(
                    modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else {
                    Icon(Icons.Filled.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar cambios", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}