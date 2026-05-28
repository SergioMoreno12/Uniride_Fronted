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
import com.example.uniride.model.dto.CalificacionRequest
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
    val sesion   = authViewModel.sesionActual
    val scope    = rememberCoroutineScope()
    val context  = LocalContext.current

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

    fun volverInicio() {
        val destino = when (sesion?.rol) {
            "conductor"     -> Routes.MIS_VIAJES
            "administrador" -> Routes.ADMIN
            else            -> Routes.HOME
        }
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

                // ── Selector numérico 1-5 ──────────────────────────
                Text("Calificación",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..5).forEach { i ->
                        val selected = puntuacion == i
                        Button(
                            onClick = { puntuacion = i },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = if (selected)
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary)
                            else
                                ButtonDefaults.outlinedButtonColors(),
                            contentPadding = PaddingValues(0.dp),
                            border = if (!selected) ButtonDefaults.outlinedButtonBorder else null
                        ) {
                            Text(
                                "$i",
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected)
                                    MaterialTheme.colorScheme.onTertiary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // ── Estrellas ──────────────────────────────────────
                Row(horizontalArrangement = Arrangement.Center) {
                    (1..5).forEach { i ->
                        IconButton(onClick = { puntuacion = i }) {
                            Icon(
                                if (i <= puntuacion) Icons.Filled.Star
                                else Icons.Filled.StarBorder,
                                null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                // ── Descripción textual de la puntuación ──────────
                if (puntuacion > 0) {
                    Text(
                        when (puntuacion) {
                            1    -> "😞 Muy malo"
                            2    -> "😐 Malo"
                            3    -> "🙂 Regular"
                            4    -> "😊 Bueno"
                            5    -> "🤩 Excelente"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ── Comentario ─────────────────────────────────────
                TextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario (opcional)") },
                    placeholder = { Text("Cuéntanos cómo fue el viaje...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // ── Botón enviar ───────────────────────────────────
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
                                val response = RetrofitClient.apiService.calificarConductor(
                                    CalificacionRequest(
                                        puntuacion  = puntuacion,
                                        comentario  = comentario,
                                        idReserva   = idReserva,
                                        idConductor = idConductor,
                                        idPasajero  = sesion?.idUsuario ?: 0L
                                    )
                                )
                                response.body()?.close()

                                if (!response.isSuccessful) {
                                    val errorMsg = when (response.code()) {
                                        409  -> "Ya calificaste este viaje anteriormente"
                                        400  -> "Datos inválidos, verifica la calificación"
                                        401  -> "Sesión expirada, vuelve a iniciar sesión"
                                        else -> "Error al calificar (${response.code()})"
                                    }
                                    cargando = false
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                try {
                                    RetrofitClient.apiService.marcarReservaCalificada(idReserva)
                                } catch (e: Exception) { /* ignorar */ }

                                cargando = false
                                Toast.makeText(context, "¡Gracias por tu calificación!",
                                    Toast.LENGTH_SHORT).show()
                                volverInicio()

                            } catch (e: Exception) {
                                cargando = false
                                val msg = when {
                                    e.message?.contains("timeout", ignoreCase = true) == true ->
                                        "El servidor tardó mucho. Intenta de nuevo."
                                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                                        "Sin conexión a internet."
                                    else ->
                                        "Error de conexión. Intenta de nuevo."
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !cargando && puntuacion > 0,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Send, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar calificación", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}