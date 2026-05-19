package com.example.uniride.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.uniride.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuariosScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val usuarios by viewModel.usuarios.observeAsState(emptyList())
    val cargando by viewModel.cargando.observeAsState(false)
    val mensaje  by viewModel.mensaje.observeAsState(null)
    val context  = LocalContext.current

    var busqueda         by remember { mutableStateOf("") }
    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }
    var usuarioRol       by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(true) { viewModel.cargarUsuarios() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    val filtrados = (usuarios ?: emptyList()).filter {
        it.nombre.contains(busqueda, ignoreCase = true) ||
                it.correo.contains(busqueda, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar usuarios") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarUsuarios() }) {
                        Icon(Icons.Filled.Refresh, null)
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
                .padding(16.dp)
        ) {
            TextField(
                value = busqueda, onValueChange = { busqueda = it },
                placeholder = { Text("Buscar por nombre o correo...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (busqueda.isNotEmpty())
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Filled.Clear, null)
                        }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))
            Text("${filtrados.size} usuarios registrados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))

            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filtrados.forEach { usuario ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar
                                Surface(
                                    shape = CircleShape,
                                    color = if (usuario.activo)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            usuario.nombre.first().uppercaseChar().toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (usuario.activo)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(usuario.nombre, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        usuario.correo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AssistChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    usuario.rol,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    if (usuario.rol == "conductor")
                                                        Icons.Filled.DirectionsCar
                                                    else Icons.Filled.Person,
                                                    null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        )
                                        if (!usuario.activo) {
                                            AssistChip(
                                                onClick = {},
                                                label = {
                                                    Text(
                                                        "Inactivo",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }

                                // Activar / Desactivar
                                IconButton(onClick = {
                                    viewModel.toggleActivoUsuario(usuario.idUsuario)
                                }) {
                                    Icon(
                                        if (usuario.activo) Icons.Filled.ToggleOn
                                        else Icons.Filled.ToggleOff,
                                        contentDescription = "Activar/Desactivar",
                                        tint = if (usuario.activo)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                // Cambiar rol
                                IconButton(onClick = { usuarioRol = usuario }) {
                                    Icon(
                                        Icons.Filled.SwapHoriz,
                                        "Cambiar rol",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                // Eliminar
                                IconButton(onClick = { usuarioAEliminar = usuario }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Dialog eliminar
    usuarioAEliminar?.let { u ->
        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Eliminar usuario") },
            text = { Text("¿Deseas eliminar a ${u.nombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarUsuario(u.idUsuario)
                        usuarioAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    // Dialog cambiar rol
    usuarioRol?.let { u ->
        val nuevoRol = if (u.rol == "conductor") "pasajero" else "conductor"
        AlertDialog(
            onDismissRequest = { usuarioRol = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Cambiar rol") },
            text = { Text("¿Cambiar el rol de ${u.nombre} de ${u.rol} a $nuevoRol?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.cambiarRolUsuario(u.idUsuario, nuevoRol)
                    usuarioRol = null
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { usuarioRol = null }) { Text("Cancelar") }
            }
        )
    }
}