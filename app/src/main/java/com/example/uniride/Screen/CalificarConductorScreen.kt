package com.example.uniride.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Usuario
import com.example.uniride.ui.theme.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalificarConductorScreen(
    idReserva: Long,
    idConductor: Long,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val sesion       = authViewModel.sesionActual
    val scope        = rememberCoroutineScope()
    val context      = LocalContext.current

    var puntuacion   by remember { mutableIntStateOf(0) }
    var comentario   by remember { mutableStateOf("") }
    var cargando     by remember { mutableStateOf(false) }
    var conductor    by remember { mutableStateOf<Usuario?>(null) }
    var yaCalificado by remember { mutableStateOf(false) }

    LaunchedEffect(idConductor) {
        scope.launch {
            try {
                conductor    = RetrofitClient.apiService.obtenerUsuario(idConductor)
                yaCalificado = RetrofitClient.apiService.yaCalificada(idReserva)
            } catch (e: Exception) { }
        }
    }

    // ✅ Función helper para volver al destino correcto según el rol
    fun volverInicio() {
        val destino = when (sesion?.rol) {
            "conductor"     -> Routes.MIS_VIAJES
            "administrador" -> Routes.ADMIN
            else            -> Routes.HOME
        }
        // Volver al destino limpiando todo el back stack intermedio
        navController.navigate(destino) {
            popUpTo(destino) { inclusive = false }
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calificar conductor") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (yaCalificado) {
                // Ya calificó
                Spacer(Modifier.height(40.dp))
                Icon(Icons.Filled.CheckCircle, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(12.dp))
                Text("Ya calificaste este viaje",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Text("Solo puedes calificar una vez por viaje",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { volverInicio() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Volver al inicio") }
            } else {
                Icon(Icons.Filled.Star, null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(56.dp))

                Text("¿Cómo fue tu viaje?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)

                conductor?.let {
                    Text("Con ${it.nombre}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary)
                }

                // Estrellas
                Row(horizontalArrangement = Arrangement.Center) {
                    (1..5).forEach { i ->
                        IconButton(onClick = { puntuacion = i }) {
                            Icon(
                                if (i <= puntuacion) Icons.Filled.Star else Icons.Filled.StarBorder,
                                null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                if (puntuacion > 0) {
                    Text(
                        when (puntuacion) {
                            1 -> "😞 Muy malo"
                            2 -> "😐 Malo"
                            3 -> "🙂 Regular"
                            4 -> "😊 Bueno"
                            5 -> "🤩 Excelente"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario (opcional)") },
                    placeholder = { Text("Cuéntanos cómo fue el viaje...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        if (puntuacion == 0) {
                            Toast.makeText(context, "Selecciona una calificación",
                                Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        cargando = true
                        scope.launch {
                            try {
                                RetrofitClient.apiService.calificarConductor(
                                    mapOf(
                                        "puntuacion"  to puntuacion,
                                        "comentario"  to comentario,
                                        "idReserva"   to idReserva,
                                        "idConductor" to idConductor,
                                        "idPasajero"  to (sesion?.idUsuario ?: 0L)
                                    )
                                )
                                // Marcar reserva como calificada
                                RetrofitClient.apiService.marcarReservaCalificada(idReserva)
                                cargando = false
                                Toast.makeText(context, "¡Gracias por tu calificación!",
                                    Toast.LENGTH_SHORT).show()
                                volverInicio() //
                            } catch (e: Exception) {
                                cargando = false
                                Toast.makeText(context, e.message ?: "Error al calificar",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !cargando && puntuacion > 0,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (cargando) CircularProgressIndicator(
                        modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    else {
                        Icon(Icons.Filled.Send, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar calificación", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}