package com.example.uniride.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.uniride.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuariosScreen(
    navController: NavController,
    adminViewModel: AdminViewModel = viewModel()
) {
    val usuarios by adminViewModel.usuarios.observeAsState(emptyList())
    val cargando by adminViewModel.cargando.observeAsState(false)
    val mensaje  by adminViewModel.mensaje.observeAsState(null)
    val context  = LocalContext.current
    var busqueda by remember { mutableStateOf("") }
    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(true) { adminViewModel.cargarUsuarios() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            adminViewModel.limpiarMensaje()
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { adminViewModel.cargarUsuarios() }) {
                        Icon(Icons.Filled.Refresh, "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            // Buscador
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
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(Modifier.height(8.dp))

            Text("${filtrados.size} usuario${if (filtrados.size != 1) "s" else ""} registrado${if (filtrados.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

            Spacer(Modifier.height(8.dp))

            if (cargando) {
                Box(Modifier.fillMaxWidth().padding(top = 48.dp),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filtrados.forEach { usuario ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (usuario.activo)
                                    MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {

                                // Encabezado: avatar inicial + nombre + rol
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (usuario.rol) {
                                            "conductor"     -> MaterialTheme.colorScheme.primaryContainer
                                            "administrador" -> MaterialTheme.colorScheme.tertiaryContainer
                                            else            -> MaterialTheme.colorScheme.secondaryContainer
                                        },
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                when (usuario.rol) {
                                                    "conductor"     -> Icons.Filled.DirectionsCar
                                                    "administrador" -> Icons.Filled.AdminPanelSettings
                                                    else            -> Icons.Filled.Person
                                                },
                                                null,
                                                tint = when (usuario.rol) {
                                                    "conductor"     -> MaterialTheme.colorScheme.primary
                                                    "administrador" -> MaterialTheme.colorScheme.tertiary
                                                    else            -> MaterialTheme.colorScheme.secondary
                                                },
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(usuario.nombre, fontWeight = FontWeight.Bold)
                                        Text(usuario.correo,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }

                                    // Chip de rol
                                    AssistChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                usuario.rol.replaceFirstChar { it.uppercase() },
                                                style = MaterialTheme.typography.labelSmall)
                                        }
                                    )
                                }

                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(10.dp))

                                // Controles: Activo toggle + Cambiar rol + Eliminar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Toggle activo/inactivo
                                    Column {
                                        Text(
                                            if (usuario.activo) "Cuenta activa" else "Cuenta bloqueada",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (usuario.activo)
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            if (usuario.activo) "Puede iniciar sesión"
                                            else "No puede acceder",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                    Switch(
                                        checked = usuario.activo,
                                        onCheckedChange = {
                                            adminViewModel.toggleActivoUsuario(usuario.idUsuario)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor   = MaterialTheme.colorScheme.primary,
                                            uncheckedThumbColor = MaterialTheme.colorScheme.error
                                        )
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                // Cambiar rol + Eliminar
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Cambiar rol
                                    if (usuario.rol != "administrador") {
                                        OutlinedButton(
                                            onClick = {
                                                val nuevoRol = if (usuario.rol == "pasajero")
                                                    "conductor" else "pasajero"
                                                adminViewModel.cambiarRol(usuario.idUsuario, nuevoRol)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(
                                                if (usuario.rol == "pasajero")
                                                    Icons.Filled.DirectionsCar
                                                else Icons.Filled.Person,
                                                null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                if (usuario.rol == "pasajero") "→ Conductor"
                                                else "→ Pasajero",
                                                style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    // Eliminar usuario
                                    OutlinedButton(
                                        onClick = { usuarioAEliminar = usuario },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Filled.Delete, null,
                                            modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Eliminar",
                                            style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Dialog confirmar eliminación
    usuarioAEliminar?.let { u ->
        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null },
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Filled.Warning, null,
                tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar usuario") },
            text = {
                Text("¿Eliminar la cuenta de ${u.nombre}?\n\n" +
                        "Esta acción también eliminará todos sus datos asociados y no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.eliminarUsuario(u.idUsuario)
                        usuarioAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sí, eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}