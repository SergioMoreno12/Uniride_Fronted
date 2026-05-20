package com.example.uniride.Screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.uniride.ViewModel.ViajeViewModel
import com.example.uniride.ui.theme.Blue500
import com.example.uniride.ui.theme.Blue900
import com.example.uniride.ui.theme.Routes
import kotlinx.coroutines.delay

private enum class ConexionEstado { VERIFICANDO, OK, ERROR }

@Composable
fun SplashScreen(navController: NavController, viajeViewModel: ViajeViewModel) {

    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    var estado by remember { mutableStateOf(ConexionEstado.VERIFICANDO) }
    var reintentando by remember { mutableStateOf(false) }

    val rotation = rememberInfiniteTransition(label = "rot")
    val angle by rotation.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "angle"
    )

    suspend fun verificarBackend(): Boolean {
        return try {
            com.example.uniride.interfaces.RetrofitClient.apiService.obtenerViajes()
            true
        } catch (e: Exception) { false }
    }

    LaunchedEffect(reintentando) {
        if (!reintentando) {
            scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
            alpha.animateTo(1f, tween(500))
        }
        estado = ConexionEstado.VERIFICANDO
        val ok = verificarBackend()
        if (ok) {
            if (!reintentando) delay(600)
            estado = ConexionEstado.OK
            navController.navigate(Routes.LOGIN) {
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
            .background(Brush.verticalGradient(listOf(Blue500, Blue900))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                Icons.Filled.DirectionsCar,
                contentDescription = null,
                modifier = Modifier
                    .size(110.dp)
                    .graphicsLayer {
                        scaleX = scale.value; scaleY = scale.value
                        if (estado == ConexionEstado.VERIFICANDO) rotationY = angle
                    },
                tint = Color.White
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "UniRide",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "Carpooling · Universidad de Cundinamarca",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(Modifier.height(40.dp))

            when (estado) {
                ConexionEstado.VERIFICANDO -> {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Conectando con el servidor...",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                ConexionEstado.OK -> {
                    Icon(Icons.Filled.CheckCircle, null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("¡Conectado! Entrando...",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold)
                }
                ConexionEstado.ERROR -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.WifiOff, null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Servidor despertando",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "El servidor está en reposo (plan gratuito de Render). " +
                                        "Puede tardar 30-60 segundos en despertar. " +
                                        "Pulsa Reintentar en unos segundos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { reintentando = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Blue500),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
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