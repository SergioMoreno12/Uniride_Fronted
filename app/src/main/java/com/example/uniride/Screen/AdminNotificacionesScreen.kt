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
import com.example.uniride.ViewModel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificacionesScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    var titulo       by remember { mutableStateOf("") }
    var mensaje      by remember { mutableStateOf("") }
    // ✅ Valores corregidos: los que espera el backend
    var destinatario by remember { mutableStateOf("todos") }
    var expanded     by remember { mutableStateOf(false) }

    val notificaciones by viewModel.notificaciones.observeAsState(emptyList())
    val cargando       by viewModel.cargando.observeAsState(false)
    val mensajeVM      by viewModel.mensaje.observeAsState(null)
    val context        = LocalContext.current

    // ✅ Mapa label → valor real para el backend
    val opciones = linkedMapOf(
        "Todos los usuarios"  to "todos",
        "Solo conductores"    to "conductor",
        "Solo pasajeros"      to "pasajero"
    )
    val labelActual = opciones.entries.find { it.value == destinatario }?.key
        ?: "Todos los usuarios"

    LaunchedEffect(true) { viewModel.cargarNotificaciones() }
    LaunchedEffect(mensajeVM) {
        mensajeVM?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
            if (it.contains("éxito")) {
                titulo  = ""
                mensaje = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones masivas") },
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
        RefreshableContent(
            isRefreshing = cargando,
            onRefresh    = { viewModel.cargarNotificaciones() },
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            // ── Formulario ──────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Nueva notificación",
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary)

                    TextField(
                        value         = titulo,
                        onValueChange = { titulo = it },
                        label         = { Text("Título") },
                        leadingIcon   = { Icon(Icons.Filled.Title, null) },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp)
                    )

                    TextField(
                        value         = mensaje,
                        onValueChange = { mensaje = it },
                        label         = { Text("Mensaje") },
                        leadingIcon   = { Icon(Icons.Filled.Message, null) },
                        maxLines      = 4,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded        = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        TextField(
                            value         = labelActual,
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Destinatarios") },
                            leadingIcon   = { Icon(Icons.Filled.Group, null) },
                            trailingIcon  = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape    = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded        = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            opciones.forEach { (label, valor) ->
                                DropdownMenuItem(
                                    text    = { Text(label) },
                                    onClick = { destinatario = valor; expanded = false }
                                )
                            }
                        }
                    }

                    Button(
                        onClick  = {
                            if (titulo.isBlank() || mensaje.isBlank()) return@Button
                            viewModel.enviarNotificacion(titulo, mensaje, destinatario)
                        },
                        enabled  = !cargando && titulo.isNotBlank() && mensaje.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color       = androidx.compose.ui.graphics.Color.White)
                        } else {
                            Icon(Icons.Filled.Send, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Enviar notificación", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Historial ───────────────────────────────────────────────
            val lista = (notificaciones ?: emptyList())
                .sortedByDescending { it.fechaEnvio }

            if (lista.isNotEmpty()) {
                Text("Historial (${lista.size})",
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                lista.forEach { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier          = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Notifications, null,
                                        tint     = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(notif.titulo,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier   = Modifier.weight(1f))
                                }
                                AssistChip(
                                    onClick = {},
                                    label   = {
                                        Text(
                                            when (notif.destinatarios) {
                                                "conductor" -> "Conductores"
                                                "pasajero"  -> "Pasajeros"
                                                else        -> "Todos"
                                            },
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(notif.mensaje,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(Modifier.height(4.dp))
                            Text(notif.fechaEnvio.take(16).replace("T", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
}