package com.example.uniride.Screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uniride.model.Sede
import java.text.SimpleDateFormat
import java.util.*

// ── DatePicker reutilizable ──────────────────────────────────────
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
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        placeholder = { Text("Seleccionar fecha") },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Fecha")
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
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

// ── Dropdown de sedes reutilizable ────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SedeDropdown(
    sedes: List<Sede>,
    seleccionada: Sede?,
    onSelect: (Sede) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = seleccionada?.let { "${it.nombreSede} - ${it.ciudad}" } ?: "Seleccionar sede",
            onValueChange = {},
            readOnly = true,
            label = { Text("Sede de la Udec") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sedes.forEach { sede ->
                DropdownMenuItem(
                    text = { Text("${sede.nombreSede} - ${sede.ciudad}") },
                    onClick = { onSelect(sede); expanded = false }
                )
            }
        }
    }
}

// ── BottomNavBar ─────────────────────────────────────────────────
@Composable
fun BottomNavBar(
    currentRoute: String,
    rol: String,
    onNavigate: (String) -> Unit
) {
    // Items del conductor — incluye acceso a vehículos desde perfil
    val itemsConductor = listOf(
        Triple("home",        "Inicio",     Icons.Filled.Home),
        Triple("publicar",    "Publicar",   Icons.Filled.Add),
        Triple("mis_viajes",  "Mis viajes", Icons.Filled.DirectionsCar),
        Triple("perfil",      "Perfil",     Icons.Filled.Person),
    )

    // Items del pasajero
    val itemsPasajero = listOf(
        Triple("home",         "Inicio",   Icons.Filled.Home),
        Triple("viajes",       "Viajes",   Icons.Filled.Search),
        Triple("mis_reservas", "Reservas", Icons.Filled.BookOnline),
        Triple("perfil",       "Perfil",   Icons.Filled.Person),
    )

    val items = if (rol == "conductor") itemsConductor else itemsPasajero

    NavigationBar {
        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick  = { if (currentRoute != route) onNavigate(route) },
                icon     = { Icon(icon, label, modifier = Modifier.size(24.dp)) },
                label    = { Text(label) }
            )
        }
    }
}