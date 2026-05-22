package com.example.uniride.Screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.R
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ui.theme.Blue500
import com.example.uniride.ui.theme.Routes
import com.example.uniride.ui.theme.ThemeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val loginResult by authViewModel.loginResult.observeAsState(null)
    val loginError  by authViewModel.loginError.observeAsState(null)
    val context     = LocalContext.current
    val isDark      = ThemeState.isDarkMode

    var correo     by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verPass    by remember { mutableStateOf(false) }
    var cargando   by remember { mutableStateOf(false) }

    LaunchedEffect(loginResult) {
        loginResult?.let { sesion ->
            cargando = false
            when (sesion.rol) {
                "conductor"     -> navController.navigate(Routes.MIS_VIAJES) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
                "administrador" -> navController.navigate(Routes.ADMIN) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
                else            -> navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(loginError) {
        loginError?.let {
            cargando = false
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.limpiarError()
        }
    }

    val bgColor = if (isDark) com.example.uniride.ui.theme.BgDark
    else MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier.fillMaxSize().background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.uniridelogo),
                contentDescription = "UniRide",
                modifier = Modifier.size(90.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text("UniRide", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
                color = Blue500)
            Text("Carpooling · Udec",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Spacer(Modifier.height(28.dp))

            // Card de login
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Iniciar sesión", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge)

                    TextField(
                        value = correo, onValueChange = { correo = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Filled.Email, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant)
                    )

                    TextField(
                        value = contrasena, onValueChange = { contrasena = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { verPass = !verPass }) {
                                Icon(if (verPass) Icons.Filled.VisibilityOff
                                else Icons.Filled.Visibility, null)
                            }
                        },
                        visualTransformation = if (verPass) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant)
                    )

                    Button(
                        onClick = {
                            if (correo.isBlank() || contrasena.isBlank()) {
                                Toast.makeText(context, "Completa todos los campos",
                                    android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            cargando = true
                            authViewModel.login(correo.trim(), contrasena)
                        },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (cargando) CircularProgressIndicator(
                            modifier = Modifier.size(22.dp), strokeWidth = 2.dp,
                            color = Color.White)
                        else Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info sobre los roles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¿Cómo funciona?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Pasajero: busca y reserva viajes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DirectionsCar, null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Conductor: publica y gestiona viajes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                    Text("Selecciona tu rol al registrarte. Cada rol requiere una cuenta.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta? ",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                TextButton(onClick = { navController.navigate(Routes.REGISTER) }) {
                    Text("Regístrate", fontWeight = FontWeight.Bold, color = Blue500)
                }
            }
        }
    }
}