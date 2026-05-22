package com.example.uniride.Screen

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

    val lista = (notificaciones ?: emptyList()).sortedByDescending { it.fechaEnvio }
    val sinLeer = lista.count { !it.leida }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notificaciones")
                        if (sinLeer > 0) {
                            Spacer(Modifier.width(8.dp))
                            Badge { Text("$sinLeer") }
                        }
                    }
                },
                actions = {
                    // Marcar todas como leídas
                    if (sinLeer > 0) {
                        IconButton(onClick = {
                            scope.launch {
                                lista.filter { !it.leida }.forEach { notif ->
                                    try {
                                        RetrofitClient.apiService.marcarLeida(notif.idNotificacion)
                                    } catch (e: Exception) { }
                                }
                                sesion?.idUsuario?.let { notifViewModel.cargarNotificaciones(it) }
                            }
                        }) {
                            Icon(Icons.Filled.DoneAll, "Marcar todas leídas")
                        }
                    }
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
                rol = sesion?.rol ?: "usuario",
                sinLeerNotif = sinLeer
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
        } else if (lista.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Filled.NotificationsNone, null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))
                    Text("Sin notificaciones",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("Aquí aparecerán tus alertas de viajes y reservas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (sinLeer > 0) {
                    Text("$sinLeer sin leer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold)
                }

                lista.forEach { notif ->
                    Card(
                        onClick = {
                            if (!notif.leida) {
                                sesion?.idUsuario?.let { uid ->
                                    notifViewModel.marcarLeida(notif.idNotificacion, uid)
                                }
                            }
                            when {
                                notif.idViaje != null && sesion?.rol == "conductor" ->
                                    navController.navigate("viaje_activo/${notif.idViaje}")
                                notif.idViaje != null && sesion?.rol == "pasajero" ->
                                    navController.navigate(Routes.MIS_RESERVAS)
                                else -> { }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!notif.leida)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top) {

                            // Ícono según tipo de notificación
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (!notif.leida)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
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
                                            notif.titulo.contains("bienvenido", ignoreCase = true) ->
                                                Icons.Filled.Celebration
                                            else -> Icons.Filled.Notifications
                                        },
                                        null,
                                        tint = if (!notif.leida)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top) {
                                    Text(notif.titulo,
                                        fontWeight = if (!notif.leida) FontWeight.Bold
                                        else FontWeight.Normal,
                                        modifier = Modifier.weight(1f))
                                    if (!notif.leida) {
                                        Spacer(Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(8.dp)
                                        ) { }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(notif.mensaje,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                                Spacer(Modifier.height(6.dp))
                                Text(notif.fechaEnvio.take(16).replace("T", " a las "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }

                            // Botón eliminar
                            Spacer(Modifier.width(4.dp))
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
                                Icon(Icons.Filled.Close, "Eliminar",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}