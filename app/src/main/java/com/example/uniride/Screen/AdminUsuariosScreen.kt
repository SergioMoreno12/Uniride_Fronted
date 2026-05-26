package com.example.uniride.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.uniride.ui.theme.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuariosScreen(
    navController:  NavController,
    adminViewModel: AdminViewModel = viewModel()
) {
    val usuarios by adminViewModel.usuarios.observeAsState(emptyList())
    val cargando by adminViewModel.cargando.observeAsState(false)
    val mensaje  by adminViewModel.mensaje.observeAsState(null)
    val context  = LocalContext.current

    var busqueda       by remember { mutableStateOf("") }
    var filtroRol      by remember { mutableStateOf("Todos") }
    var expandedFiltro by remember { mutableStateOf(false) }

    LaunchedEffect(true) { adminViewModel.cargarUsuarios() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            adminViewModel.limpiarMensaje()
        }
    }

    val filtrados = (usuarios ?: emptyList()).filter { u ->
        val coincideTexto = u.nombre.contains(busqueda, ignoreCase = true) ||
                u.correo.contains(busqueda, ignoreCase = true)
        val coincideRol = filtroRol == "Todos" ||
                u.rol.equals(filtroRol, ignoreCase = true)
        coincideTexto && coincideRol
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar usuarios") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        RefreshableContent(
            isRefreshing = cargando,                          // estado del adminViewModel
            onRefresh    = { adminViewModel.cargarUsuarios() },    // metodo de recarga
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Buscador ────────────────────────────────────────────────
            TextField(
                value         = busqueda,
                onValueChange = { busqueda = it },
                placeholder   = { Text("Buscar por nombre o correo...") },
                leadingIcon   = { Icon(Icons.Filled.Search, null) },
                trailingIcon  = {
                    if (busqueda.isNotEmpty())
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Filled.Clear, null)
                        }
                },
                singleLine = true,
                modifier   = Modifier.fillMaxWidth(),
                shape      = RoundedCornerShape(12.dp),
                colors     = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(Modifier.height(8.dp))

            // ── Filtro por rol ──────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("${filtrados.size} usuario${if (filtrados.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                ExposedDropdownMenuBox(
                    expanded         = expandedFiltro,
                    onExpandedChange = { expandedFiltro = it }
                ) {
                    FilterChip(
                        selected    = filtroRol != "Todos",
                        onClick     = { expandedFiltro = true },
                        label       = { Text(filtroRol) },
                        leadingIcon = {
                            Icon(Icons.Filled.FilterList, null,
                                modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded         = expandedFiltro,
                        onDismissRequest = { expandedFiltro = false }
                    ) {
                        listOf("Todos", "Pasajero", "Conductor").forEach { opcion ->
                            DropdownMenuItem(
                                text    = { Text(opcion) },
                                onClick = { filtroRol = opcion; expandedFiltro = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtrados, key = { it.idUsuario }) { usuario ->
                        // ── Tarjeta simple — toca para ver detalle ──────
                        Card(
                            onClick  = {
                                adminViewModel.limpiarDetalleUsuario()
                                navController.navigate(
                                    Routes.ADMIN_USUARIO_DETALLE
                                        .replace("{idUsuario}", usuario.idUsuario.toString())
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = CardDefaults.cardColors(
                                containerColor = if (usuario.activo)
                                    MaterialTheme.colorScheme.surface
                                else
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier          = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icono de rol
                                Surface(
                                    shape    = RoundedCornerShape(8.dp),
                                    color    = when (usuario.rol) {
                                        "conductor" -> MaterialTheme.colorScheme.primaryContainer
                                        else        -> MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (usuario.rol == "conductor")
                                                Icons.Filled.DirectionsCar
                                            else Icons.Filled.Person,
                                            null,
                                            tint     = if (usuario.rol == "conductor")
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(usuario.nombre, fontWeight = FontWeight.Bold)
                                    Text(usuario.correo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                            .copy(alpha = 0.6f))
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AssistChip(
                                            onClick = {},
                                            label   = {
                                                Text(
                                                    usuario.rol.replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.labelSmall)
                                            }
                                        )
                                        if (!usuario.activo) {
                                            AssistChip(
                                                onClick = {},
                                                label   = {
                                                    Text("Bloqueado",
                                                        style = MaterialTheme.typography.labelSmall)
                                                },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme
                                                        .errorContainer,
                                                    labelColor     = MaterialTheme.colorScheme.error)
                                            )
                                        }
                                    }
                                }

                                Icon(Icons.Filled.ChevronRight, null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}
}