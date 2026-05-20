package com.example.uniride.Screen

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Usuario
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaConductoresScreen(
    navController: NavController,
    authViewModel: AuthViewModel   = viewModel(),
    viajeViewModel: ViajeViewModel = viewModel()
) {
    val viajes  by viajeViewModel.viajes.observeAsState(emptyList())
    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    var conductoresConViaje by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var promedios           by remember { mutableStateOf<Map<Long, Double>>(emptyMap()) }
    var cargando            by remember { mutableStateOf(true) }

    LaunchedEffect(viajes) {
        val lista = (viajes ?: emptyList())
            .mapNotNull { it.vehiculo?.usuario }
            .distinctBy { it.idUsuario }

        // Cargar promedios
        val promediosMap = mutableMapOf<Long, Double>()
        lista.forEach { conductor ->
            try {
                promediosMap[conductor.idUsuario] =
                    RetrofitClient.apiService.promedioConductor(conductor.idUsuario)
            } catch (e: Exception) { promediosMap[conductor.idUsuario] = 0.0 }
        }

        conductoresConViaje = lista
        promedios = promediosMap
        cargando = false
    }

    LaunchedEffect(true) {
        if ((viajes ?: emptyList()).isEmpty()) {
            viajeViewModel.cargarDisponibles(null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conductores disponibles") },
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
        } else if (conductoresConViaje.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.DirectionsCar, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("No hay conductores disponibles por el momento",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("${conductoresConViaje.size} conductor${if (conductoresConViaje.size > 1) "es" else ""} con viajes publicados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                conductoresConViaje.forEach { cond ->
                    val prom = promedios[cond.idUsuario] ?: 0.0
                    val viajesConductor = (viajes ?: emptyList())
                        .filter { it.vehiculo?.usuario?.idUsuario == cond.idUsuario }

                    Card(modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(52.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center) {
                                    Text(cond.nombre.first().uppercaseChar().toString(),
                                        fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cond.nombre, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        (1..5).forEach { i ->
                                            Icon(
                                                if (i <= prom.toInt()) Icons.Filled.Star
                                                else Icons.Filled.StarBorder,
                                                null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(14.dp))
                                        }
                                        Spacer(Modifier.width(4.dp))
                                        Text("${"%.1f".format(prom)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary)
                                    }
                                    Text("${viajesConductor.size} viaje${if (viajesConductor.size > 1) "s" else ""} disponible${if (viajesConductor.size > 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        navController.navigate("viajes_por_conductor/${cond.idUsuario}")
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.DirectionsCar, null,
                                        modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Ver viajes", style = MaterialTheme.typography.labelMedium)
                                }

                                if (!cond.telefono.isNullOrBlank()) {
                                    Button(
                                        onClick = {
                                            val numero = cond.telefono.filter { it.isDigit() }
                                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                                Uri.parse("https://wa.me/57$numero")))
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Icon(Icons.Filled.Chat, null,
                                            modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("WhatsApp",
                                            style = MaterialTheme.typography.labelMedium)
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
}