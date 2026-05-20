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
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Calificacion
import com.example.uniride.model.Usuario
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConductorPerfilScreen(
    idConductor: Long,
    navController: NavController,
    authViewModel: AuthViewModel   = viewModel(),
    viajeViewModel: ViajeViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    var conductor      by remember { mutableStateOf<Usuario?>(null) }
    var calificaciones by remember { mutableStateOf<List<Calificacion>>(emptyList()) }
    var promedio       by remember { mutableStateOf(0.0) }
    var cargando       by remember { mutableStateOf(true) }

    LaunchedEffect(idConductor) {
        scope.launch {
            try {
                conductor      = RetrofitClient.apiService.obtenerUsuario(idConductor)
                calificaciones = RetrofitClient.apiService.calificacionesConductor(idConductor)
                promedio       = RetrofitClient.apiService.promedioConductor(idConductor)
            } catch (e: Exception) { }
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil del conductor") },
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
        if (cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        conductor?.nombre?.first()?.uppercaseChar()?.toString() ?: "C",
                        fontSize = 32.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(conductor?.nombre ?: "-",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)

                // Estrellas
                Row(verticalAlignment = Alignment.CenterVertically) {
                    (1..5).forEach { i ->
                        Icon(
                            if (i <= promedio.toInt()) Icons.Filled.Star else Icons.Filled.StarBorder,
                            null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("${"%.1f".format(promedio)} · ${calificaciones.size} calificaciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary)
                }

                // Datos de contacto
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Phone, null,
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(conductor?.telefono ?: "No disponible")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Email, null,
                                tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(conductor?.correo ?: "-")
                        }
                    }
                }

                // Calificaciones de pasajeros
                if (calificaciones.isNotEmpty()) {
                    Text("Calificaciones de pasajeros",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)

                    calificaciones.forEach { cal ->
                        Card(modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cal.pasajero?.nombre ?: "Pasajero",
                                        fontWeight = FontWeight.SemiBold)
                                    Row {
                                        (1..5).forEach { i ->
                                            Icon(
                                                if (i <= cal.puntuacion) Icons.Filled.Star
                                                else Icons.Filled.StarBorder,
                                                null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                if (!cal.comentario.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("\"${cal.comentario}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                                Text(cal.fechaCalificacion.take(10),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }
    }
}