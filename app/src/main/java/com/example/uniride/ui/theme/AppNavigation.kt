package com.example.uniride.ui.theme

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.uniride.Screen.*
import com.example.uniride.ViewModel.AdminViewModel
import com.example.uniride.ViewModel.AuthViewModel
import com.example.uniride.ViewModel.PerfilViewModel
import com.example.uniride.ViewModel.ReservaViewModel
import com.example.uniride.ViewModel.ViajeViewModel

object Routes {
    const val SPLASH               = "splash"
    const val LOGIN                = "login"
    const val REGISTER             = "register"
    const val HOME                 = "home"
    const val VIAJES               = "viajes"
    const val VIAJE_DETALLE        = "viaje_detalle/{idViaje}"
    const val MIS_RESERVAS         = "mis_reservas"
    const val PUBLICAR             = "publicar"
    const val MIS_VIAJES           = "mis_viajes"
    const val PERFIL               = "perfil"
    const val REGISTRAR_VEHICULO   = "registrar_vehiculo"
    const val CALIFICAR            = "calificar/{idReserva}/{idConductor}"
    const val ADMIN                = "admin"
    const val ADMIN_USUARIOS       = "admin_usuarios"
    const val ADMIN_VIAJES         = "admin_viajes"
    const val ADMIN_VEHICULOS      = "admin_vehiculos"
    const val ADMIN_SEDES          = "admin_sedes"
    const val ADMIN_ESTADISTICAS   = "admin_estadisticas"
    const val ADMIN_REPORTES       = "admin_reportes"
    const val ADMIN_NOTIFICACIONES = "admin_notificaciones"
}

@Composable
fun AppNavigation() {
    val navController      = rememberNavController()
    val authViewModel:     AuthViewModel    = viewModel()
    val viajeViewModel:    ViajeViewModel   = viewModel()
    val reservaViewModel:  ReservaViewModel = viewModel()
    val adminViewModel:    AdminViewModel   = viewModel()
    val perfilViewModel:   PerfilViewModel  = viewModel()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(navController, viajeViewModel)
        }
        composable(Routes.LOGIN) {
            LoginScreen(navController, authViewModel)
        }
        composable(Routes.REGISTER) {
            RegisterScreen(navController, authViewModel)
        }
        composable(Routes.HOME) {
            HomeScreen(navController, authViewModel, viajeViewModel)
        }
        composable(Routes.VIAJES) {
            ViajesScreen(navController, viajeViewModel)
        }
        composable(Routes.VIAJE_DETALLE) { back ->
            val idViaje = back.arguments?.getString("idViaje")?.toLongOrNull() ?: 0L
            ViajeDetalleScreen(idViaje, navController, authViewModel, reservaViewModel, viajeViewModel)
        }
        composable(Routes.MIS_RESERVAS) {
            MisReservasScreen(navController, authViewModel, reservaViewModel)
        }
        composable(Routes.PUBLICAR) {
            PublicarViajeScreen(navController, authViewModel, viajeViewModel)
        }
        composable(Routes.MIS_VIAJES) {
            MisViajesScreen(navController, authViewModel, viajeViewModel, reservaViewModel)
        }
        composable(Routes.PERFIL) {
            PerfilScreen(navController, authViewModel, perfilViewModel)
        }
        composable(Routes.REGISTRAR_VEHICULO) {
            RegistrarVehiculoScreen(navController, authViewModel, perfilViewModel)
        }
        composable(Routes.CALIFICAR) { back ->
            val idReserva   = back.arguments?.getString("idReserva")?.toLongOrNull() ?: 0L
            val idConductor = back.arguments?.getString("idConductor")?.toLongOrNull() ?: 0L
            CalificarConductorScreen(idReserva, idConductor, navController, authViewModel)
        }
        composable(Routes.ADMIN) {
            AdminScreen(navController, adminViewModel)
        }
        composable(Routes.ADMIN_USUARIOS) {
            AdminUsuariosScreen(navController, adminViewModel)
        }
        composable(Routes.ADMIN_VIAJES) {
            AdminViajesScreen(navController, adminViewModel)
        }
        composable(Routes.ADMIN_VEHICULOS) {
            AdminVehiculosScreen(navController, adminViewModel)
        }
        composable(Routes.ADMIN_SEDES) {
            AdminSedesScreen(navController, adminViewModel)
        }
        composable(Routes.ADMIN_ESTADISTICAS) {
            AdminEstadisticasScreen(navController, adminViewModel)
        }
        composable(Routes.ADMIN_REPORTES) {
            AdminReportesScreen(navController, adminViewModel)
        }
        composable(Routes.ADMIN_NOTIFICACIONES) {
            AdminNotificacionesScreen(navController, adminViewModel)
        }
    }
}