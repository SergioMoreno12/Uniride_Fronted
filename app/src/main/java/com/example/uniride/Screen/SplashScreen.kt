package com.example.uniride.Screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.R
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.ui.theme.Blue500
import com.example.uniride.ui.theme.Blue900
import com.example.uniride.ui.theme.Routes
import kotlinx.coroutines.delay

private enum class ConexionEstado { VERIFICANDO, OK, ERROR }

@Composable
fun SplashScreen(
    navController: NavController,
    viajeViewModel: ViajeViewModel,
    authViewModel: AuthViewModel = viewModel()
) {

    val alpha    = remember { Animatable(0f) }
    var estado   by remember { mutableStateOf(ConexionEstado.VERIFICANDO) }
    var reintentando by remember { mutableStateOf(false) }

    suspend fun verificarBackend(): Boolean = try {
        RetrofitClient.apiService.obtenerViajes(); true
    } catch (e: Exception) { false }

    LaunchedEffect(reintentando) {
        if (!reintentando) {
            alpha.animateTo(1f, tween(600))
        }
        estado = ConexionEstado.VERIFICANDO
        val ok = verificarBackend()
        if (ok) {
            if (!reintentando) delay(500)
            estado = ConexionEstado.OK

            val sesion = authViewModel.sesionActual
            val destino = when {
                sesion == null              -> Routes.LOGIN
                sesion.rol == "administrador" -> Routes.ADMIN
                sesion.rol == "conductor"   -> Routes.MIS_VIAJES
                else                        -> Routes.HOME
            }
            navController.navigate(destino) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        } else {
            estado = ConexionEstado.ERROR
            reintentando = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue900),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp).alpha(alpha.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.uniridelogo),
                contentDescription = "UniRide",
                modifier = Modifier.size(120.dp)
            )

            Spacer(Modifier.height(20.dp))

            Text("UniRide", style = MaterialTheme.typography.headlineLarge,
                color = Color.White, fontWeight = FontWeight.ExtraBold)

            Text("Carpooling · Universidad de Cundinamarca",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)

            Spacer(Modifier.height(40.dp))

            when (estado) {
                ConexionEstado.VERIFICANDO -> {
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                    Spacer(Modifier.height(12.dp))
                    Text("Conectando...", color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium)
                }
                ConexionEstado.OK -> {
                    Icon(Icons.Filled.CheckCircle, null, tint = Color.White,
                        modifier = Modifier.size(36.dp))
                }
                ConexionEstado.ERROR -> {
                    Card(shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.WifiOff, null, tint = Color.White,
                                modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("Servidor despertando", color = Color.White,
                                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(6.dp))
                            Text("El servidor puede tardar 30-60 seg en responder (Render gratuito).",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(14.dp))
                            Button(onClick = { reintentando = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White, contentColor = Blue500),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Filled.Refresh, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Reintentar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}