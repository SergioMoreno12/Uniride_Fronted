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
import com.example.uniride.model.Sede

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSedesScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val sedes    by viewModel.sedes.observeAsState(emptyList())
    val cargando by viewModel.cargando.observeAsState(false)
    val mensaje  by viewModel.mensaje.observeAsState(null)
    val context = LocalContext.current

    var sedeAEliminar by remember { mutableStateOf<Sede?>(null) }
    var showFormulario by remember { mutableStateOf(false) }
    var nombreNueva by remember { mutableStateOf("") }
    var ciudadNueva by remember { mutableStateOf("") }

    LaunchedEffect(true) { viewModel.cargarSedes() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
            if (it.contains("éxito")) {
                showFormulario = false
                nombreNueva = ""
                ciudadNueva = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar sedes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarSedes() }) {
                        Icon(Icons.Filled.Refresh, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showFormulario = true }) {
                Icon(Icons.Filled.Add, "Agregar sede")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("${(sedes ?: emptyList()).size} sedes registradas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

            if (cargando) {
                Box(Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                (sedes ?: emptyList()).forEach { sede ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.School, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sede.nombreSede, fontWeight = FontWeight.SemiBold)
                                Text(sede.ciudad,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            IconButton(onClick = { sedeAEliminar = sede }) {
                                Icon(Icons.Filled.Delete, null,
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    // Dialog nueva sede
    if (showFormulario) {
        AlertDialog(
            onDismissRequest = { showFormulario = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Nueva sede") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = nombreNueva, onValueChange = { nombreNueva = it },
                        label = { Text("Nombre de la sede") },
                        leadingIcon = { Icon(Icons.Filled.School, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = ciudadNueva, onValueChange = { ciudadNueva = it },
                        label = { Text("Ciudad") },
                        leadingIcon = { Icon(Icons.Filled.LocationCity, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombreNueva.isNotBlank() && ciudadNueva.isNotBlank())
                            viewModel.crearSede(nombreNueva, ciudadNueva)
                    },
                    enabled = nombreNueva.isNotBlank() && ciudadNueva.isNotBlank()
                ) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showFormulario = false }) { Text("Cancelar") }
            }
        )
    }

    // Dialog eliminar sede
    sedeAEliminar?.let { s ->
        AlertDialog(
            onDismissRequest = { sedeAEliminar = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Eliminar sede") },
            text = { Text("¿Eliminar la sede ${s.nombreSede} en ${s.ciudad}?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminarSede(s.idSede); sedeAEliminar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { sedeAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}