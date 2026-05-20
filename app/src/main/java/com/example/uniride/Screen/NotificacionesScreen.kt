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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.NotifViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.ui.theme.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    navController: NavController,
    authViewModel: AuthViewModel   = viewModel(),
    notifViewModel: NotifViewModel = viewModel()
) {
    val sesion         = authViewModel.sesionActual
    val notificaciones by notifViewModel.notificaciones.observeAsState(emptyList())
    val cargando       by notifViewModel.cargando.observeAsState(false)
    val scope          = rememberCoroutineScope()

    LaunchedEffect(true) {
        sesion?.idUsuario?.let { notifViewModel.cargarNotificaciones(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                actions = {
                    IconButton(onClick = {
                        sesion?.idUsuario?.let { notifViewModel.cargarNotificaciones(it) }
                    }) { Icon(Icons.Filled.Refresh, "Actualizar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "notificaciones",
                rol = sesion?.rol ?: "usuario"
            ) { route ->
                navController.navigate(route) {
                    popUpTo(Routes.HOME) { saveState = true }
                    launchSingleTop = true; restoreState = true
                }
            }
        }
    ) { padding ->
        if (cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val lista = (notificaciones ?: emptyList())
                .sortedByDescending { it.fechaEnvio }

            if (lista.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.NotificationsNone, null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        Spacer(Modifier.height(8.dp))
                        Text("Sin notificaciones",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    lista.forEach { notif ->
                        Card(
                            onClick = {
                                // Marcar como leída
                                if (!notif.leida) {
                                    sesion?.idUsuario?.let { uid ->
                                        notifViewModel.marcarLeida(notif.idNotificacion, uid)
                                    }
                                }
                                // Navegar a pantalla correspondiente
                                when {
                                    notif.idViaje != null && sesion?.rol == "conductor" ->
                                        navController.navigate("viaje_activo/${notif.idViaje}")
                                    notif.idViaje != null && sesion?.rol == "pasajero" ->
                                        navController.navigate(Routes.MIS_RESERVAS)
                                    notif.titulo.contains("calificación", ignoreCase = true) ->
                                        navController.navigate("conductor_perfil/${sesion?.idUsuario}")
                                    else -> { /* no navegar */ }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (!notif.leida)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Icono
                                Icon(
                                    when {
                                        notif.titulo.contains("confirmada", ignoreCase = true) ->
                                            Icons.Filled.CheckCircle
                                        notif.titulo.contains("reserva", ignoreCase = true) ->
                                            Icons.Filled.BookOnline
                                        notif.titulo.contains("calificación", ignoreCase = true) ->
                                            Icons.Filled.Star
                                        notif.titulo.contains("viaje", ignoreCase = true) ->
                                            Icons.Filled.DirectionsCar
                                        else -> Icons.Filled.Notifications
                                    },
                                    null,
                                    tint = if (!notif.leida)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(notif.titulo, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(notif.mensaje,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    Spacer(Modifier.height(4.dp))
                                    Text(notif.fechaEnvio.take(16).replace("T", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                                // Botón eliminar
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                RetrofitClient.apiService
                                                    .eliminarNotificacion(notif.idNotificacion)
                                                sesion?.idUsuario?.let { uid ->
                                                    notifViewModel.cargarNotificaciones(uid)
                                                }
                                            } catch (e: Exception) { }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, "Eliminar",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}