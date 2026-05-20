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
import androidx.navigation.NavController
import com.example.uniride.ui.theme.ThemeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(navController: NavController) {

    var modoOscuro by ThemeState.isDarkMode
    var notifActivas by remember { mutableStateOf(true) }
    var notifViajes  by remember { mutableStateOf(true) }
    var notifReservas by remember { mutableStateOf(true) }

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Apariencia
            Text("Apariencia", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (modoOscuro) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                null, tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Modo oscuro", fontWeight = FontWeight.SemiBold)
                                Text(if (modoOscuro) "Activado" else "Desactivado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                        Switch(checked = modoOscuro, onCheckedChange = { modoOscuro = it })
                    }
                }
            }

            // Notificaciones
            Text("Notificaciones", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    AjusteSwitch(
                        icon = Icons.Filled.Notifications,
                        titulo = "Notificaciones",
                        subtitulo = "Recibir todas las notificaciones",
                        valor = notifActivas,
                        onChange = { notifActivas = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AjusteSwitch(
                        icon = Icons.Filled.DirectionsCar,
                        titulo = "Viajes",
                        subtitulo = "Alertas de nuevos viajes disponibles",
                        valor = notifViajes && notifActivas,
                        onChange = { notifViajes = it },
                        habilitado = notifActivas
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AjusteSwitch(
                        icon = Icons.Filled.BookOnline,
                        titulo = "Reservas",
                        subtitulo = "Confirmaciones y cambios de reservas",
                        valor = notifReservas && notifActivas,
                        onChange = { notifReservas = it },
                        habilitado = notifActivas
                    )
                }
            }

            // Acerca de
            Text("Acerca de", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DirectionsCar, null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("UniRide", fontWeight = FontWeight.Bold)
                            Text("Versión 1.0.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    Text("Carpooling universitario · Universidad de Cundinamarca",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun AjusteSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String,
    valor: Boolean,
    onChange: (Boolean) -> Unit,
    habilitado: Boolean = true
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