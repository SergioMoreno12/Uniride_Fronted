package com.example.uniride.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Calificacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisCalificacionesScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val sesion = authViewModel.sesionActual
    var calificaciones by remember { mutableStateOf<List<Calificacion>>(emptyList()) }
    var promedio       by remember { mutableStateOf(0.0) }
    var cargando       by remember { mutableStateOf(true) }

    LaunchedEffect(sesion?.idUsuario) {
        sesion?.idUsuario?.let { id ->
            cargando = true
            try {
                calificaciones = RetrofitClient.apiService.calificacionesConductor(id)
                promedio       = RetrofitClient.apiService.promedioConductor(id)
            } catch (e: Exception) { }
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis calificaciones") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Tarjeta resumen ──────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Star,
                        null,
                        tint     = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        if (promedio > 0) "%.1f".format(promedio) else "—",
                        style      = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                    // Estrellas visuales
                    if (promedio > 0) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            (1..5).forEach { i ->
                                Icon(
                                    if (i <= promedio.toInt()) Icons.Filled.Star
                                    else Icons.Filled.StarBorder,
                                    null,
                                    tint     = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    Text(
                        if (calificaciones.isEmpty()) "Aún no tienes calificaciones"
                        else "${calificaciones.size} calificación${if (calificaciones.size != 1) "es" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            if (cargando) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (calificaciones.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.StarBorder,
                            null,
                            tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Aún no has recibido calificaciones",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            "Cuando completes viajes, tus pasajeros podrán calificarte aquí.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                // ── Lista de calificaciones ──────────────────────────────
                Text(
                    "Historial de calificaciones",
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )

                calificaciones.sortedByDescending { it.fechaCalificacion }.forEach { cal ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cabecera: pasajero + fecha
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val inicial = cal.pasajero?.nombre
                                    ?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        inicial,
                                        fontSize   = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        cal.pasajero?.nombre ?: "Pasajero",
                                        fontWeight = FontWeight.SemiBold,
                                        style      = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        cal.fechaCalificacion.take(10),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                // Puntuación numérica
                                Surface(
                                    shape  = RoundedCornerShape(8.dp),
                                    color  = when (cal.puntuacion) {
                                        5    -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                        4    -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        3    -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Star,
                                            null,
                                            tint     = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            "${cal.puntuacion}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize   = 13.sp,
                                            color      = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Estrellas
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                (1..5).forEach { i ->
                                    Icon(
                                        if (i <= (cal.puntuacion ?: 0)) Icons.Filled.Star
                                        else Icons.Filled.StarBorder,
                                        null,
                                        tint     = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    when (cal.puntuacion) {
                                        1 -> "Muy malo"
                                        2 -> "Malo"
                                        3 -> "Regular"
                                        4 -> "Bueno"
                                        5 -> "Excelente"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            // Comentario
                            if (!cal.comentario.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Row(modifier = Modifier.padding(10.dp)) {
                                        Icon(
                                            Icons.Filled.FormatQuote,
                                            null,
                                            tint     = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            cal.comentario,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}