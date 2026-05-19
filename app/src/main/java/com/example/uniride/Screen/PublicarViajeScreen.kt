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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.model.Sede
import com.example.uniride.ui.theme.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarViajeScreen(
    navController: NavController,
    authViewModel: AuthViewModel  = viewModel(),
    viajeViewModel: ViajeViewModel = viewModel()
) {
    val sesion   = authViewModel.sesionActual
    val sedes    by viajeViewModel.sedes.observeAsState(emptyList())
    val cargando by viajeViewModel.cargando.observeAsState(false)
    val mensaje  by viajeViewModel.mensaje.observeAsState(null)
    val context = LocalContext.current

    var origen    by remember { mutableStateOf("") }
    var destino   by remember { mutableStateOf("") }
    var fechaHora by remember { mutableStateOf("") }
    var costo     by remember { mutableStateOf("") }
    var idVehiculo by remember { mutableStateOf("") }
    var sedeSeleccionada by remember { mutableStateOf<Sede?>(null) }

    LaunchedEffect(true) { viajeViewModel.cargarSedes() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viajeViewModel.limpiarMensaje()
            if (it.contains("éxito")) navController.navigate(Routes.HOME) {
                popUpTo(Routes.PUBLICAR) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Publicar viaje") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface))
        },
        bottomBar = {
            BottomNavBar(currentRoute = "publicar", rol = "conductor") { route ->
                navController.navigate(route) {
                    popUpTo(Routes.HOME) { saveState = true }
                    launchSingleTop = true; restoreState = true
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Datos del viaje", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            TextField(origen, { origen = it }, label = { Text("Punto de salida") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp))

            TextField(destino, { destino = it }, label = { Text("Destino (dirección)") },
                leadingIcon = { Icon(Icons.Filled.LocationCity, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp))

            SedeDropdown(
                sedes = sedes ?: emptyList(),
                seleccionada = sedeSeleccionada,
                onSelect = { sedeSeleccionada = it }
            )

            DateTimePickerField(
                label = "Fecha de salida",
                value = fechaHora.take(10),
                onDateSelected = { fechaHora = it },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(costo, { costo = it }, label = { Text("Costo por puesto ($)") },
                leadingIcon = { Icon(Icons.Filled.AttachMoney, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp))

            TextField(idVehiculo, { idVehiculo = it }, label = { Text("ID del vehículo") },
                leadingIcon = { Icon(Icons.Filled.DirectionsCar, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp))

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    viajeViewModel.publicarViaje(
                        origen, destino, fechaHora,
                        costo.toDoubleOrNull() ?: 0.0,
                        idVehiculo.toLongOrNull() ?: 0L,
                        sedeSeleccionada?.idSede ?: 0L
                    )
                },
                enabled = !cargando && origen.isNotBlank() && fechaHora.isNotBlank()
                        && costo.isNotBlank() && idVehiculo.isNotBlank() && sedeSeleccionada != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (cargando) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text("Publicar viaje", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}