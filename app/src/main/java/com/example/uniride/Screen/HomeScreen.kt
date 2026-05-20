package com.example.uniride.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.ui.theme.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel   = viewModel(),
    viajeViewModel: ViajeViewModel = viewModel()
) {
    val sesion           = authViewModel.sesionActual
    val viajes           by viajeViewModel.viajes.observeAsState(emptyList())
    val cargando         by viajeViewModel.cargando.observeAsState(false)
    val mensaje          by viajeViewModel.mensaje.observeAsState(null)
    val viajesReservados by viajeViewModel.viajesReservados.observeAsState(emptySet())
    val viajesPropios    by viajeViewModel.viajesPropios.observeAsState(emptySet())
    val context          = LocalContext.current

    LaunchedEffect(true) {
        viajeViewModel.cargarDisponibles(sesion?.idUsuario)
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viajeViewModel.limpiarMensaje()
        }
    }

    // Filtrar viajes: ni los ya reservados ni los propios
    val todosViajes = (viajes ?: emptyList()).filter { v ->
        v.idViaje !in (viajesReservados ?: emptySet()) &&
                v.idViaje !in (viajesPropios ?: emptySet())
    }

    // Agrupaciones para tarjetas de búsqueda
    val conductores = todosViajes
        .mapNotNull { it.vehiculo?.usuario }
        .distinctBy { it.idUsuario }

    val sedes = todosViajes
        .mapNotNull { it.sede }
        .distinctBy { it.idSede }

    val ciudades = todosViajes
        .map { it.origen }
        .filter { it.isNotBlank() }
        .distinct()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${sesion?.nombre?.split(" ")?.first() ?: ""}",
                            fontWeight = FontWeight.Bold)
                        Text("¿A dónde vas hoy?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viajeViewModel.cargarDisponibles(sesion?.idUsuario)
                    }) { Icon(Icons.Filled.Refresh, "Actualizar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            BottomNavBar(currentRoute = "home", rol = sesion?.rol ?: "usuario") { route ->
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
        ) {
            if (cargando) {
                Box(Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {

                // ── Tarjeta Conductores ────────────────────────────
                if (conductores.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("  Conductores disponibles",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(conductores) { conductor ->
                            Card(
                                onClick = {
                                    navController.navigate(
                                        "viajes_por_conductor/${conductor.idUsuario}")
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.width(110.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()) {
                                            Text(
                                                conductor.nombre.first()
                                                    .uppercaseChar().toString(),
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        conductor.nombre.split(" ").first(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Tarjeta Sedes ──────────────────────────────────
                if (sedes.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("  Sedes con viajes",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sedes) { sede ->
                            Card(
                                onClick = {
                                    navController.navigate("viajes_por_sede/${sede.idSede}")
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                modifier = Modifier.width(140.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Icon(Icons.Filled.School, null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.height(6.dp))
                                    Text(sede.nombreSede,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(sede.ciudad,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                            .copy(alpha = 0.6f))
                                    Spacer(Modifier.height(4.dp))
                                    val count = todosViajes.count {
                                        it.sede?.idSede == sede.idSede
                                    }
                                    Text("$count viaje${if (count > 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // ── Tarjeta Ciudades ───────────────────────────────
                if (ciudades.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("  Por ciudad de origen",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ciudades) { ciudad ->
                            Card(
                                onClick = {
                                    navController.navigate("viajes_por_ciudad/$ciudad")
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                modifier = Modifier.width(130.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Icon(Icons.Filled.LocationCity, null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.height(6.dp))
                                    Text(ciudad,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    val count = todosViajes.count {
                                        it.origen.contains(ciudad, ignoreCase = true)
                                    }
                                    Text("$count viaje${if (count > 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // ── Lista de viajes ────────────────────────────────
                Spacer(Modifier.height(20.dp))
                Text("  Todos los viajes disponibles",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))

                if (todosViajes.isEmpty()) {
                    // ── Estado vacío con opción de contactar conductor ──
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.DirectionsCar, null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No hay viajes disponibles en este momento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Puedes contactar directamente a un conductor para coordinar tu viaje",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                navController.navigate(Routes.LISTA_CONDUCTORES)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.People, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Ver conductores disponibles",
                                fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                viajeViewModel.cargarDisponibles(sesion?.idUsuario)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Recargar viajes")
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        todosViajes.forEach { viaje ->
                            ViajeCard(viaje = viaje) {
                                navController.navigate("viaje_detalle/${viaje.idViaje}")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}