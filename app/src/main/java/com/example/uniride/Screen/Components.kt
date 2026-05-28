package com.example.uniride.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uniride.model.Sede
import com.example.uniride.ui.theme.Routes
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ════════════════════════════════════════════════════════════════
// CuposProgressBar
// ════════════════════════════════════════════════════════════════
@Composable
fun CuposProgressBar(
    cuposDisponibles: Int,
    capacidadTotal: Int,
    modifier: Modifier = Modifier,
    mostrarTexto: Boolean = true
) {
    if (capacidadTotal <= 0) return

    val cuposOcupados = (capacidadTotal - cuposDisponibles).coerceAtLeast(0)
    val progreso      = cuposOcupados.toFloat() / capacidadTotal.toFloat()

    val colorBarra = when {
        cuposDisponibles <= 0 -> MaterialTheme.colorScheme.error
        progreso >= 0.75f     -> Color(0xFFFF9800)
        else                  -> MaterialTheme.colorScheme.primary
    }
    val colorTexto = when {
        cuposDisponibles <= 0 -> MaterialTheme.colorScheme.error
        progreso >= 0.75f     -> Color(0xFFFF9800)
        else                  -> MaterialTheme.colorScheme.secondary
    }

    Column(modifier = modifier) {
        if (mostrarTexto) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        cuposDisponibles <= 0 -> "🔴 Sin cupos disponibles"
                        cuposDisponibles == 1 -> "🟡 ¡Solo 1 cupo disponible!"
                        progreso >= 0.75f     -> "🟠 $cuposDisponibles cupos disponibles"
                        else                  -> "🟢 $cuposDisponibles cupos disponibles"
                    },
                    style      = MaterialTheme.typography.labelSmall,
                    color      = colorTexto,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "$cuposOcupados/$capacidadTotal ocupados",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
            Spacer(Modifier.height(4.dp))
        }
        LinearProgressIndicator(
            progress   = { progreso },
            modifier   = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color      = colorBarra,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// ════════════════════════════════════════════════════════════════
// BottomNavBar
// ════════════════════════════════════════════════════════════════
@Composable
fun BottomNavBar(
    currentRoute: String,
    rol: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        when (rol) {
            "conductor" -> {
                NavigationBarItem(
                    selected = currentRoute == "mis_viajes",
                    onClick  = { onNavigate(Routes.MIS_VIAJES) },
                    icon     = { Icon(Icons.Filled.DirectionsCar, null) },
                    label    = { Text("Mis viajes") }
                )
                NavigationBarItem(
                    selected = currentRoute == "publicar",
                    onClick  = { onNavigate(Routes.PUBLICAR) },
                    icon     = { Icon(Icons.Filled.AddCircle, null) },
                    label    = { Text("Publicar") }
                )
            }
            "administrador" -> {
                NavigationBarItem(
                    selected = currentRoute == "admin",
                    onClick  = { onNavigate(Routes.ADMIN) },
                    icon     = { Icon(Icons.Filled.AdminPanelSettings, null) },
                    label    = { Text("Panel") }
                )
            }
            else -> {
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick  = { onNavigate(Routes.HOME) },
                    icon     = { Icon(Icons.Filled.Home, null) },
                    label    = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = currentRoute == "mis_reservas",
                    onClick  = { onNavigate(Routes.MIS_RESERVAS) },
                    icon     = { Icon(Icons.Filled.BookOnline, null) },
                    label    = { Text("Reservas") }
                )
            }
        }
        NavigationBarItem(
            selected = currentRoute == "notificaciones",
            onClick  = { onNavigate(Routes.NOTIFICACIONES) },
            icon     = { Icon(Icons.Filled.Notifications, null) },
            label    = { Text("Avisos") }
        )
        NavigationBarItem(
            selected = currentRoute == "perfil",
            onClick  = { onNavigate(Routes.PERFIL) },
            icon     = { Icon(Icons.Filled.Person, null) },
            label    = { Text("Perfil") }
        )
    }
}

// ════════════════════════════════════════════════════════════════
// SedeDropdown
// ════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SedeDropdown(
    sedes: List<Sede>,
    seleccionada: Sede?,
    onSelect: (Sede) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value            = seleccionada?.let { "${it.nombreSede} - ${it.ciudad}" } ?: "",
            onValueChange    = {},
            readOnly         = true,
            label            = { Text("Sede de la universidad") },
            leadingIcon      = { Icon(Icons.Filled.School, null) },
            trailingIcon     = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier         = Modifier.menuAnchor().fillMaxWidth(),
            shape            = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sedes.forEach { sede ->
                DropdownMenuItem(
                    text    = { Text("${sede.nombreSede} - ${sede.ciudad}") },
                    onClick = { onSelect(sede); expanded = false }
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// DateTimePickerField
// ════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate = LocalDate.now()
) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    val minMillis = minDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= minMillis
            override fun isSelectableYear(year: Int)           = year >= minDate.year
        }
    )
    OutlinedTextField(
        value         = value,
        onValueChange = {},
        readOnly      = true,
        label         = { Text(label) },
        leadingIcon   = { Icon(Icons.Filled.CalendarToday, null) },
        trailingIcon  = { IconButton(onClick = { mostrarDialogo = true }) { Icon(Icons.Filled.Edit, null) } },
        modifier      = modifier,
        shape         = RoundedCornerShape(12.dp)
    )
    if (mostrarDialogo) {
        DatePickerDialog(
            onDismissRequest = { mostrarDialogo = false },
            confirmButton    = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // FIX: interpretar los millis como fecha UTC, no local
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC).toLocalDate()
                        onDateSelected(date.toString())
                    }
                    mostrarDialogo = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }
}

// ════════════════════════════════════════════════════════════════
// HorizontalDatePicker
// ════════════════════════════════════════════════════════════════
@Composable
fun HorizontalDatePicker(
    fechasDisponibles: List<String>,
    fechaSeleccionada: String?,
    modifier: Modifier = Modifier,
    onFechaSelected: (String?) -> Unit
) {
    if (fechasDisponibles.isEmpty()) return
    val formatter    = DateTimeFormatter.ofPattern("EEE", Locale("es", "ES"))
    val formatterDia = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))
    Column(modifier = modifier) {
        Text("Filtrar por fecha",
            style    = MaterialTheme.typography.labelMedium,
            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 6.dp))
        LazyRow(
            modifier       = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            item {
                FechaChip("Todas", "${fechasDisponibles.size} días",
                    fechaSeleccionada == null) { onFechaSelected(null) }
            }
            items(fechasDisponibles) { fechaStr ->
                val date = try { LocalDate.parse(fechaStr) } catch (e: Exception) { null }
                if (date != null) {
                    val esHoy  = date == LocalDate.now()
                    val titulo = if (esHoy) "Hoy"
                    else date.format(formatter).replaceFirstChar { it.uppercase() }
                    FechaChip(titulo, date.format(formatterDia),
                        fechaSeleccionada == fechaStr) { onFechaSelected(fechaStr) }
                }
            }
        }
    }
}

@Composable
private fun FechaChip(titulo: String, subtitulo: String, seleccionada: Boolean, onClick: () -> Unit) {
    val bgColor   = if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (seleccionada) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .widthIn(min = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titulo,    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
            Text(subtitulo, fontSize = 13.sp, fontWeight = FontWeight.Bold,     color = textColor)
        }
    }
}

// ════════════════════════════════════════════════════════════════
// RefreshableContent
// ════════════════════════════════════════════════════════════════
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RefreshableContent(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh    = onRefresh,
        modifier     = modifier
    ) { content() }
}

// ════════════════════════════════════════════════════════════════
// ViajeMiniCard
// reservasCount: si no viene del backend, se calcula externamente
// y se pasa aquí para mostrar la barra real.
// ════════════════════════════════════════════════════════════════
@Composable
fun ViajeMiniCard(
    v: com.example.uniride.model.Viaje,
    reservasCount: Int? = null,
    onClick: () -> Unit
) {
    val capacidad = v.vehiculo?.capacidad ?: 0

    // Prioridad: 1) backend, 2) calculado externamente, 3) capacidad (todo disponible)
    val cupos = v.cuposDisponibles
        ?: reservasCount?.let { (capacidad - it).coerceAtLeast(0) }
        ?: capacidad

    // Mostrar barra si tenemos dato fiable (del backend o cargado externamente)
    val hayDatoCupos = v.cuposDisponibles != null || reservasCount != null

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label   = {
                        Text(if (v.tipoViaje == "vuelta") "Vuelta" else "Ida",
                            style = MaterialTheme.typography.labelSmall)
                    },
                    leadingIcon = {
                        Icon(
                            if (v.tipoViaje == "vuelta") Icons.Filled.Home else Icons.Filled.School,
                            null, modifier = Modifier.size(12.dp))
                    }
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("${v.origen} → ${v.destino}", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "📅 ${v.fechaHora.take(10)}  " +
                            "🕐 ${if (v.fechaHora.length >= 16) v.fechaHora.substring(11,16) else "--:--"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text("$ ${"%.0f".format(v.costo)}",
                    style      = MaterialTheme.typography.bodySmall,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold)
            }
            if (capacidad > 0) {
                Spacer(Modifier.height(8.dp))
                if (hayDatoCupos) {
                    // Barra real con datos precisos
                    CuposProgressBar(
                        cuposDisponibles = cupos,
                        capacidadTotal   = capacidad,
                        modifier         = Modifier.fillMaxWidth()
                    )
                } else {
                    // Fallback mientras cargan los datos
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp)
                        Text("Cargando disponibilidad...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                    }
                }
            }
        }
    }
}