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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.interfaces.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearReporteScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val sesion  = authViewModel.sesionActual
    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    var titulo      by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var cargando    by remember { mutableStateOf(false) }
    var error       by remember { mutableStateOf<String?>(null) }

    // Tipos de reporte predefinidos para facilitar al usuario
    val tiposReporte = listOf(
        "Conductor no se presentó",
        "Comportamiento inapropiado",
        "Vehículo en mal estado",
        "Ruta diferente a la publicada",
        "Cobro incorrecto",
        "Otro"
    )
    var tipoSeleccionado by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar un problema") },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mensaje informativo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Tu reporte será revisado por el equipo de UniRide. " +
                                "Usa este formulario para reportar problemas, mal comportamiento " +
                                "o situaciones de seguridad.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Tipo de reporte (accesos rápidos)
            Text("Tipo de problema", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tiposReporte.forEach { tipo ->
                    FilterChip(
                        selected = tipoSeleccionado == tipo,
                        onClick  = {
                            tipoSeleccionado = tipo
                            if (tipo != "Otro") titulo = tipo
                        },
                        label    = { Text(tipo, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider()

            // Título personalizado
            Text("Título del reporte", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            TextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                leadingIcon = { Icon(Icons.Filled.Report, null) },
                placeholder = { Text("Ej: El conductor no llegó al punto acordado") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = titulo.isBlank() && cargando
            )

            // Descripción detallada
            Text("Descripción detallada", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            TextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Describe lo que ocurrió") },
                leadingIcon = { Icon(Icons.Filled.Description, null) },
                placeholder = { Text("Proporciona todos los detalles relevantes: fecha, hora, nombres involucrados...") },
                maxLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(12.dp),
                isError = descripcion.isBlank() && cargando
            )

            // Error
            error?.let {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Error, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Botón enviar
            Button(
                onClick = {
                    error = null
                    if (titulo.isBlank()) { error = "Ingresa un título para el reporte"; return@Button }
                    if (descripcion.isBlank()) { error = "Describe el problema con más detalle"; return@Button }

                    cargando = true
                    scope.launch {
                        try {
                            RetrofitClient.apiService.crearReporte(
                                mapOf(
                                    "titulo"      to titulo,
                                    "descripcion" to descripcion,
                                    "idUsuario"   to (sesion?.idUsuario ?: 0L)
                                )
                            )
                            Toast.makeText(context,
                                "✅ Reporte enviado con éxito. Lo revisaremos pronto.",
                                Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            error = "Error al enviar el reporte. Intenta de nuevo."
                        }
                        cargando = false
                    }
                },
                enabled  = !cargando,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Enviar reporte", fontWeight = FontWeight.Bold)
                }
            }

            // Nota sobre privacidad
            Text(
                "Tu reporte es confidencial y será atendido por el equipo de administración.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}