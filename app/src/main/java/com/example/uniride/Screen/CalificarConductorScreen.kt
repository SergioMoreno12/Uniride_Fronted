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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.interfaces.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalificarConductorScreen(
    idReserva: Long,
    idConductor: Long,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val sesion  = authViewModel.sesionActual
    val context = LocalContext.current

    var puntuacion  by remember { mutableIntStateOf(0) }
    var comentario  by remember { mutableStateOf("") }
    var cargando    by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(Icons.Filled.Star, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp))

            Text("¿Cómo fue tu experiencia?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)

            Text("Selecciona una puntuación del 1 al 5",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            // Estrellas
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { estrella ->
                    IconButton(onClick = { puntuacion = estrella },
                        modifier = Modifier.size(48.dp)) {
                        Icon(
                            if (estrella <= puntuacion) Icons.Filled.Star
                            else Icons.Filled.StarBorder,
                            contentDescription = "$estrella estrellas",
                            tint = if (estrella <= puntuacion)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            if (puntuacion > 0) {
                Text(
                    when (puntuacion) {
                        1 -> "Muy malo"
                        2 -> "Malo"
                        3 -> "Regular"
                        4 -> "Bueno"
                        5 -> "Excelente"
                        else -> ""
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            TextField(
                value = comentario, onValueChange = { comentario = it },
                label = { Text("Comentario (opcional)") },
                leadingIcon = { Icon(Icons.Filled.Comment, null) },
                maxLines = 3, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (puntuacion == 0) {
                        Toast.makeText(context, "Selecciona una puntuación", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    cargando = true
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            RetrofitClient.apiService.calificarConductor(
                                mapOf(
                                    "puntuacion" to puntuacion,
                                    "comentario" to comentario,
                                    "fechaCalificacion" to LocalDate.now().toString(),
                                    "idReserva" to idReserva,
                                    "idConductor" to idConductor,
                                    "idPasajero" to (sesion?.idUsuario ?: 0L)
                                )
                            )
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "¡Gracias por tu calificación!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        } catch (e: Exception) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "Ya calificaste este viaje", Toast.LENGTH_SHORT).show()
                            }
                        }
                        cargando = false
                    }
                },
                enabled = !cargando && puntuacion > 0,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (cargando)
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text("Enviar calificación", fontWeight = FontWeight.Bold)
            }
        }
    }
}