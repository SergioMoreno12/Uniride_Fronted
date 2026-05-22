package com.example.uniride.Screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.PerfilViewModel

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
    var fotoUri     by remember { mutableStateOf<Uri?>(null) }
    var tab         by remember { mutableIntStateOf(0) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> fotoUri = uri }

    LaunchedEffect(perfilUsuario) {
        perfilUsuario?.let { nombre = it.nombre; telefono = it.telefono ?: "" }
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

            // Solo dos pestañas: Datos y Contraseña
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 },
                    text = { Text("Datos personales") })
                Tab(selected = tab == 1, onClick = { tab = 1 },
                    text = { Text("Contraseña") })
            }

            Spacer(Modifier.height(20.dp))

            when (tab) {

                // ── Datos personales + foto ────────────────────────────
                0 -> {
                    // Foto de perfil
                    Box(contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.align(Alignment.CenterHorizontally)) {

                        if (fotoUri != null) {
                            AsyncImage(
                                model = fotoUri,
                                contentDescription = null,
                                modifier = Modifier.size(90.dp).clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else if (!perfilUsuario?.fotoPerfil.isNullOrBlank()) {
                            AsyncImage(
                                model = perfilUsuario!!.fotoPerfil,
                                contentDescription = null,
                                modifier = Modifier.size(90.dp).clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(90.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                    fontSize = 36.sp, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        SmallFloatingActionButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.CameraAlt, null,
                                modifier = Modifier.size(14.dp),
                                tint = androidx.compose.ui.graphics.Color.White)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

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
                            val fotoStr = fotoUri?.toString()
                                ?: perfilUsuario?.fotoPerfil
                            sesion?.idUsuario?.let {
                                perfilViewModel.actualizarPerfil(
                                    it, nombre, telefono,
                                    fotoPerfil = fotoStr)
                                // Actualizar foto en sesión local
                                fotoStr?.let { f -> authViewModel.actualizarFoto(f) }
                            }
                        },
                        enabled = !cargando && nombre.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (cargando) CircularProgressIndicator(
                            modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        else Text("Guardar cambios", fontWeight = FontWeight.Bold)
                    }
                }

                // ── Contraseña ─────────────────────────────────────────
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
                        if (cargando) CircularProgressIndicator(
                            modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        else Text("Cambiar contraseña", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}