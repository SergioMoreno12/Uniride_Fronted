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
import com.example.uniride.ui.theme.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    notifViewModel: NotifViewModel = viewModel()
) {
    val sesion         = authViewModel.sesionActual
    val notificaciones by notifViewModel.notificaciones.observeAsState(emptyList())
    val cargando       by notifViewModel.cargando.observeAsState(false)

    fun recargar() {
        sesion?.idUsuario?.let {
            notifViewModel.cargarNotificaciones(it)
        }
    }

    LaunchedEffect(sesion?.idUsuario) { recargar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        RefreshableContent(
            isRefreshing = cargando,
            onRefresh    = { recargar() },
            modifier     = Modifier.fillMaxSize().padding(padding)
        ) {
            val lista = (notificaciones ?: emptyList())
                .sortedByDescending { it.fechaEnvio }

            if (lista.isEmpty()) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.height(200.dp))
                    Icon(Icons.Filled.NotificationsNone, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("Sin notificaciones",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("Desliza hacia abajo para recargar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else {
                Column(modifier = Modifier
                    .fillMaxSize()
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
                                // Navegar según rol y datos disponibles
                                when (sesion?.rol) {
                                    "conductor" -> {
                                        notif.idViaje?.let { idViaje ->
                                            navController.navigate("viaje_activo/$idViaje")
                                        }
                                    }
                                    "pasajero" -> {
                                        when {
                                            notif.idReserva != null ->
                                                navController.navigate("reserva_detalle/${notif.idReserva}")
                                            notif.idViaje != null ->
                                                navController.navigate(Routes.MIS_RESERVAS)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (notif.leida)
                                    MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Ícono de estado (leída/no leída)
                                Icon(
                                    if (notif.leida) Icons.Filled.MarkEmailRead
                                    else Icons.Filled.MarkEmailUnread,
                                    null,
                                    tint = if (notif.leida)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp).padding(top = 2.dp))
                                Spacer(Modifier.width(10.dp))

                                // Contenido
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(notif.titulo.ifBlank { "Notificación" },
                                        fontWeight = if (notif.leida) FontWeight.Normal
                                        else FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(2.dp))
                                    Text(notif.mensaje,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    Spacer(Modifier.height(4.dp))
                                    // Hint visual de que es tappable si tiene destino
                                    val tieneDEstino = when (sesion?.rol) {
                                        "conductor" -> notif.idViaje != null
                                        "pasajero"  -> notif.idReserva != null || notif.idViaje != null
                                        else        -> false
                                    }
                                    if (tieneDEstino) {
                                        Text(
                                            when (sesion?.rol) {
                                                "conductor" -> "Toca para confirmar reserva →"
                                                else        -> "Toca para ver tu reserva →"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        notif.fechaEnvio.take(16).replace("T", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }

                                // Botón eliminar
                                IconButton(
                                    onClick = {
                                        sesion?.idUsuario?.let { uid ->
                                            notifViewModel.eliminarNotificacion(
                                                notif.idNotificacion, uid)
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.DeleteOutline,
                                        contentDescription = "Eliminar notificación",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(18.dp)
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
}