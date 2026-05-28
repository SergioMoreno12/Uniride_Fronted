package com.example.uniride.Screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uniride.ViewModel.*
import kotlinx.coroutines.launch

@Composable
fun MainPagerScreen(
    navController:    NavController,
    startPage:        Int               = 0,
    authViewModel:    AuthViewModel     = viewModel(),
    viajeViewModel:   ViajeViewModel    = viewModel(),
    reservaViewModel: ReservaViewModel  = viewModel(),
    // FIX: Agregar adminViewModel compartido como parámetro
    adminViewModel:   AdminViewModel    = viewModel(),
    perfilViewModel:  PerfilViewModel   = viewModel(),
    notifViewModel:   NotifViewModel    = viewModel()
) {
    val sesionLive by authViewModel.loginResult.observeAsState(authViewModel.sesionActual)
    val sesion      = sesionLive ?: authViewModel.sesionActual
    val rol         = sesion?.rol ?: "pasajero"
    val scope       = rememberCoroutineScope()

    val pages: List<String> = when (rol) {
        "conductor"     -> listOf("mis_viajes", "publicar", "notificaciones", "perfil")
        "administrador" -> listOf("admin", "notificaciones", "perfil")
        else            -> listOf("home", "mis_reservas", "notificaciones", "perfil")
    }

    val safeStart  = startPage.coerceIn(0, pages.size - 1)
    val pagerState = rememberPagerState(
        initialPage = safeStart,
        pageCount   = { pages.size }
    )

    LaunchedEffect(rol) {
        pagerState.scrollToPage(0)
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = pages[pagerState.currentPage],
                rol          = rol,
                onNavigate   = { route ->
                    val idx = pages.indexOf(route)
                    if (idx >= 0) {
                        scope.launch { pagerState.animateScrollToPage(idx) }
                    } else {
                        navController.navigate(route)
                    }
                }
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            when (pages[page]) {
                "home"           -> HomeScreen(navController, authViewModel, viajeViewModel)
                "mis_reservas"   -> MisReservasScreen(navController, authViewModel, reservaViewModel)
                "mis_viajes"     -> MisViajesScreen(
                    navController    = navController,
                    authViewModel    = authViewModel,
                    viajeViewModel   = viajeViewModel,
                    reservaViewModel = reservaViewModel,
                    onPublicarViaje  = { scope.launch { pagerState.animateScrollToPage(1) } }
                )
                "publicar"       -> PublicarViajeScreen(
                    navController    = navController,
                    authViewModel    = authViewModel,
                    viajeViewModel   = viajeViewModel,
                    onViajePublicado = { scope.launch { pagerState.animateScrollToPage(0) } }
                )
                "notificaciones" -> NotificacionesScreen(navController, authViewModel, notifViewModel)
                "perfil"         -> PerfilScreen(navController, authViewModel, perfilViewModel)
                // FIX: Pasar el adminViewModel compartido en lugar de viewModel()
                "admin"          -> AdminScreen(navController, adminViewModel, authViewModel)
            }
        }
    }
}