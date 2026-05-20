package com.example.uniride.Screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.uniride.ui.theme.ThemeMode
import com.example.uniride.ui.theme.ThemeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(navController: NavController) {

    var themeMode      by ThemeState.themeMode
    var notifActivas   by remember { mutableStateOf(true) }
    var notifViajes    by remember { mutableStateOf(true) }
    var notifReservas  by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Apariencia ─────────────────────────────────────────
            Text("Apariencia", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Opción: Seguir el sistema
                    ThemeOption(
                        icon   = Icons.Filled.Brightness4,
                        titulo = "Seguir el tema del dispositivo",
                        subtitulo = "Cambia automáticamente con el sistema",
                        seleccionado = themeMode == ThemeMode.SYSTEM,
                        onClick = { themeMode = ThemeMode.SYSTEM }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ThemeOption(
                        icon   = Icons.Filled.LightMode,
                        titulo = "Modo claro",
                        subtitulo = "Siempre usar tema claro",
                        seleccionado = themeMode == ThemeMode.LIGHT,
                        onClick = { themeMode = ThemeMode.LIGHT }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ThemeOption(
                        icon   = Icons.Filled.DarkMode,
                        titulo = "Modo oscuro",
                        subtitulo = "Siempre usar tema oscuro (negro puro)",
                        seleccionado = themeMode == ThemeMode.DARK,
                        onClick = { themeMode = ThemeMode.DARK }
                    )
                }
            }

            // ── Notificaciones ─────────────────────────────────────
            Text("Notificaciones", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    AjusteSwitch(Icons.Filled.Notifications,
                        "Notificaciones", "Recibir todas las notificaciones",
                        notifActivas) { notifActivas = it }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AjusteSwitch(Icons.Filled.DirectionsCar,
                        "Viajes", "Alertas de nuevos viajes",
                        notifViajes && notifActivas, notifActivas) { notifViajes = it }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AjusteSwitch(Icons.Filled.BookOnline,
                        "Reservas", "Confirmaciones y cambios",
                        notifReservas && notifActivas, notifActivas) { notifReservas = it }
                }
            }

            // ── Acerca de ──────────────────────────────────────────
            Text("Acerca de", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("UniRide", fontWeight = FontWeight.Bold)
                    Text("Versión 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("Carpooling universitario · Universidad de Cundinamarca",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, null,
                tint = if (seleccionado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(titulo, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
        RadioButton(selected = seleccionado, onClick = onClick)
    }
}

@Composable
private fun AjusteSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String, subtitulo: String,
    valor: Boolean, habilitado: Boolean = true, onChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, null,
                tint = if (habilitado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(titulo, fontWeight = FontWeight.SemiBold,
                    color = if (habilitado) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                Text(subtitulo, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
        Switch(checked = valor, onCheckedChange = onChange, enabled = habilitado)
    }
}