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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.PerfilViewModel
import com.example.uniride.model.dto.LoginResponse
import com.example.uniride.ui.theme.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    navController: NavController,
    authViewModel: AuthViewModel     = viewModel(),
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val sesion        = authViewModel.sesionActual
    val perfilUsuario by perfilViewModel.perfilUsuario.observeAsState(null)
    val cargando      by perfilViewModel.cargando.observeAsState(false)
    val mensaje       by perfilViewModel.mensaje.observeAsState(null)
    val context       = LocalContext.current

    var nombre      by remember { mutableStateOf("") }
    var telefono    by remember { mutableStateOf("") }
    var claveActual by remember { mutableStateOf("") }
    var claveNueva  by remember { mutableStateOf("") }
    var rolSel      by remember { mutableStateOf("pasajero") }
    var tab         by remember { mutableIntStateOf(0) }

    LaunchedEffect(perfilUsuario) {
        perfilUsuario?.let {
            nombre   = it.nombre
            telefono = it.telefono ?: ""
            rolSel   = it.rol
        }
    }

    LaunchedEffect(sesion?.idUsuario) {
        sesion?.idUsuario?.let { perfilViewModel.cargarPerfil(it) }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            perfilViewModel.limpiarMensaje()
            if (it.contains("éxito")) navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 },
                    text = { Text("Datos") })
                Tab(selected = tab == 1, onClick = { tab = 1 },
                    text = { Text("Contraseña") })
                Tab(selected = tab == 2, onClick = { tab = 2 },
                    text = { Text("Rol") })
            }

            Spacer(Modifier.height(20.dp))

            when (tab) {

                // ── Datos personales ───────────────────────────────────────
                0 -> {
                    TextField(nombre, { nombre = it },
                        label = { Text("Nombre completo") },
                        leadingIcon = { Icon(Icons.Filled.Person, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(12.dp))
                    TextField(telefono, { telefono = it },
                        label = { Text("Teléfono") },
                        leadingIcon = { Icon(Icons.Filled.Phone, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            sesion?.idUsuario?.let {
                                perfilViewModel.actualizarPerfil(it, nombre, telefono)
                            }
                        },
                        enabled = !cargando && nombre.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (cargando) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        else Text("Guardar datos", fontWeight = FontWeight.Bold)
                    }
                }

                // ── Contraseña ─────────────────────────────────────────────
                1 -> {
                    TextField(claveActual, { claveActual = it },
                        label = { Text("Contraseña actual") },
                        leadingIcon = { Icon(Icons.Filled.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(12.dp))
                    TextField(claveNueva, { claveNueva = it },
                        label = { Text("Nueva contraseña") },
                        leadingIcon = { Icon(Icons.Filled.LockOpen, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            sesion?.idUsuario?.let {
                                perfilViewModel.cambiarContrasena(it, claveActual, claveNueva)
                                claveActual = ""; claveNueva = ""
                            }
                        },
                        enabled = !cargando && claveActual.isNotBlank() && claveNueva.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (cargando) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        else Text("Cambiar contraseña", fontWeight = FontWeight.Bold)
                    }
                }

                // ── Cambiar rol ────────────────────────────────────────────
                2 -> {
                    Text("Selecciona cómo quieres usar la app:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("pasajero" to Icons.Filled.Person,
                            "conductor" to Icons.Filled.DirectionsCar)
                            .forEach { (rol, icon) ->
                                Card(
                                    onClick = { rolSel = rol },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (rolSel == rol)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surface),
                                    border = if (rolSel == rol)
                                        androidx.compose.foundation.BorderStroke(
                                            2.dp, MaterialTheme.colorScheme.primary)
                                    else androidx.compose.foundation.BorderStroke(
                                        1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(icon, null,
                                            tint = if (rolSel == rol)
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(32.dp))
                                        Spacer(Modifier.height(6.dp))
                                        Text(rol.replaceFirstChar { it.uppercase() },
                                            fontWeight = if (rolSel == rol) FontWeight.Bold else FontWeight.Normal,
                                            color = if (rolSel == rol)
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        Text(
                                            if (rol == "pasajero") "Busca y reserva viajes"
                                            else "Publica y ofrece viajes",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                }
                            }
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            sesion?.idUsuario?.let { id ->
                                perfilViewModel.actualizarPerfil(id, nombre.ifBlank { sesion.nombre }, telefono)
                                // Actualizar sesión con nuevo rol
                                val nuevaSesion = LoginResponse(
                                    mensaje = "ok",
                                    rol = rolSel,
                                    idUsuario = sesion.idUsuario,
                                    nombre = sesion.nombre
                                )
                                authViewModel.actualizarSesion(nuevaSesion)
                                Toast.makeText(context, "Rol cambiado a $rolSel", Toast.LENGTH_SHORT).show()
                                navController.navigate(
                                    if (rolSel == "conductor") Routes.MIS_VIAJES else Routes.HOME
                                ) { popUpTo(0) { inclusive = false } }
                            }
                        },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar rol", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}