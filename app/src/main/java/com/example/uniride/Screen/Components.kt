package com.example.uniride.Screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.uniride.model.Sede
import com.example.uniride.model.Viaje
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    TextField(
        value = value, onValueChange = {},
        readOnly = true, label = { Text(label) },
        placeholder = { Text("Seleccionar fecha") },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Filled.DateRange, "Fecha")
            }
        },
        modifier = modifier, shape = RoundedCornerShape(12.dp)
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        onDateSelected("${sdf.format(Date(millis))}T06:00:00")
                    }
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SedeDropdown(sedes: List<Sede>, seleccionada: Sede?, onSelect: (Sede) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        TextField(
            value = seleccionada?.let { "${it.nombreSede} - ${it.ciudad}" } ?: "Seleccionar sede",
            onValueChange = {}, readOnly = true,
            label = { Text("Sede de la Udec") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sedes.forEach { sede ->
                DropdownMenuItem(
                    text = { Text("${sede.nombreSede} - ${sede.ciudad}") },
                    onClick = { onSelect(sede); expanded = false }
                )
            }
        }
    }
}

@Composable
fun ViajeCard(viaje: Viaje, onClick: () -> Unit) {
    val capacidad = viaje.vehiculo?.capacidad ?: 0

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Column {
                    Text("Desde",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(viaje.origen, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.weight(1f))
                Text("$ ${"%.0f".format(viaje.costo)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
            Text("→ ${viaje.destino}", fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("📅 ${viaje.fechaHora.take(10)}  " +
                        "🕐 ${if (viaje.fechaHora.length >= 16)
                            viaje.fechaHora.substring(11, 16) else "--:--"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("$capacidad puestos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ── BottomNavBar ──────────────────────────────────────────────────
@Composable
fun BottomNavBar(
    currentRoute: String,
    rol: String,
    sinLeerNotif: Int = 0,
    onNavigate: (String) -> Unit
) {
    val itemsConductor = listOf(
        Triple("mis_viajes",    "Publicados",    Icons.Filled.DirectionsCar),
        Triple("publicar",      "Publicar",      Icons.Filled.Add),
        Triple("notificaciones","Notificaciones",Icons.Filled.Notifications),
        Triple("perfil",        "Perfil",        Icons.Filled.Person),
    )
    val itemsPasajero = listOf(
        Triple("home",          "Inicio",        Icons.Filled.Home),
        Triple("mis_reservas",  "Reservas",      Icons.Filled.BookOnline),
        Triple("notificaciones","Notificaciones",Icons.Filled.Notifications),
        Triple("perfil",        "Perfil",        Icons.Filled.Person),
    )
    val items = if (rol == "conductor") itemsConductor else itemsPasajero

    NavigationBar {
        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick  = { if (currentRoute != route) onNavigate(route) },
                icon     = {
                    if (route == "notificaciones" && sinLeerNotif > 0) {
                        BadgedBox(badge = { Badge { Text("$sinLeerNotif") } }) {
                            Icon(icon, label, modifier = Modifier.size(22.dp))
                        }
                    } else {
                        Icon(icon, label, modifier = Modifier.size(22.dp))
                    }
                },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}