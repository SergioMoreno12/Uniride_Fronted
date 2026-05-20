package com.example.uniride.ui.theme

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.uniride.Screen.*
import com.example.uniride.ViewModel.*

object Routes {
    const val SPLASH                = "splash"
    const val LOGIN                 = "login"
    const val REGISTER              = "register"
    const val HOME                  = "home"
    const val VIAJES                = "viajes"
    const val VIAJE_DETALLE         = "viaje_detalle/{idViaje}"
    const val MIS_RESERVAS          = "mis_reservas"
    const val HISTORIAL_RESERVAS    = "historial_reservas"
    const val PUBLICAR              = "publicar"
    const val MIS_VIAJES            = "mis_viajes"
    const val HISTORIAL_VIAJES      = "historial_viajes"
    const val VIAJE_ACTIVO_DETALLE  = "viaje_activo/{idViaje}"
    const val PERFIL                = "perfil"
    const val EDITAR_PERFIL         = "editar_perfil"
    const val AJUSTES               = "ajustes"
    const val MI_VEHICULO           = "mi_vehiculo"
    const val REGISTRAR_VEHICULO    = "registrar_vehiculo"
    const val CALIFICAR             = "calificar/{idReserva}/{idConductor}"
    const val NOTIFICACIONES        = "notificaciones"
    const val CONDUCTOR_PERFIL      = "conductor_perfil/{idConductor}"
    const val VIAJES_POR_SEDE       = "viajes_por_sede/{idSede}"
    const val VIAJES_POR_CIUDAD     = "viajes_por_ciudad/{ciudad}"
    const val VIAJES_POR_CONDUCTOR  = "viajes_por_conductor/{idConductor}"
    const val ADMIN                 = "admin"
    const val ADMIN_USUARIOS        = "admin_usuarios"
    const val ADMIN_VIAJES          = "admin_viajes"
    const val ADMIN_VEHICULOS       = "admin_vehiculos"
    const val ADMIN_SEDES           = "admin_sedes"
    const val ADMIN_ESTADISTICAS    = "admin_estadisticas"
    const val ADMIN_REPORTES        = "admin_reportes"
    const val ADMIN_NOTIFICACIONES  = "admin_notificaciones"
}

@Composable
fun AppNavigation() {
    val navController       = rememberNavController()
    val authViewModel:      AuthViewModel      = viewModel()
    val viajeViewModel:     ViajeViewModel     = viewModel()
    val reservaViewModel:   ReservaViewModel   = viewModel()
    val adminViewModel:     AdminViewModel     = viewModel()
    val perfilViewModel:    PerfilViewModel    = viewModel()
    val notifViewModel:     NotifViewModel     = viewModel()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH)   { SplashScreen(navController, viajeViewModel) }
        composable(Routes.LOGIN)    { LoginScreen(navController, authViewModel) }
        composable(Routes.REGISTER) { RegisterScreen(navController, authViewModel) }
        composable(Routes.HOME)     { HomeScreen(navController, authViewModel, viajeViewModel) }
        composable(Routes.VIAJES)   { ViajesScreen(navController, viajeViewModel, authViewModel) }
        composable(Routes.VIAJE_DETALLE) { back ->
            val idViaje = back.arguments?.getString("idViaje")?.toLongOrNull() ?: 0L
            ViajeDetalleScreen(idViaje, navController, authViewModel, reservaViewModel, viajeViewModel)
        }
        composable(Routes.MIS_RESERVAS) {
            MisReservasScreen(navController, authViewModel, reservaViewModel)
        }
        composable(Routes.HISTORIAL_RESERVAS) {
            HistorialReservasScreen(navController, authViewModel, reservaViewModel)
        }
        composable(Routes.PUBLICAR) {
            PublicarViajeScreen(navController, authViewModel, viajeViewModel)
        }
        composable(Routes.MIS_VIAJES) {
            MisViajesScreen(navController, authViewModel, viajeViewModel, reservaViewModel)
        }
        composable(Routes.HISTORIAL_VIAJES) {
            HistorialViajesScreen(navController, authViewModel, viajeViewModel)
        }
        composable(Routes.VIAJE_ACTIVO_DETALLE) { back ->
            val idViaje = back.arguments?.getString("idViaje")?.toLongOrNull() ?: 0L
            ViajeActivoDetalleScreen(idViaje, navController, authViewModel, reservaViewModel, viajeViewModel)
        }
        composable(Routes.PERFIL) {
            PerfilScreen(navController, authViewModel, perfilViewModel)
        }
        composable(Routes.EDITAR_PERFIL) {
            EditarPerfilScreen(navController, authViewModel, perfilViewModel)
        }
        composable(Routes.AJUSTES) {
            AjustesScreen(navController)
        }
        composable(Routes.MI_VEHICULO) {
            MiVehiculoScreen(navController, authViewModel, perfilViewModel)
        }
        composable(Routes.REGISTRAR_VEHICULO) {
            RegistrarVehiculoScreen(navController, authViewModel, perfilViewModel)
        }
        composable(Routes.CALIFICAR) { back ->
            val idReserva   = back.arguments?.getString("idReserva")?.toLongOrNull() ?: 0L
            val idConductor = back.arguments?.getString("idConductor")?.toLongOrNull() ?: 0L
            CalificarConductorScreen(idReserva, idConductor, navController, authViewModel)
        }
        composable(Routes.NOTIFICACIONES) {
            NotificacionesScreen(navController, authViewModel, notifViewModel)
        }
        composable(Routes.CONDUCTOR_PERFIL) { back ->
            val idConductor = back.arguments?.getString("idConductor")?.toLongOrNull() ?: 0L
            ConductorPerfilScreen(idConductor, navController, authViewModel, viajeViewModel)
        }
        composable(Routes.VIAJES_POR_SEDE) { back ->
            val idSede = back.arguments?.getString("idSede")?.toLongOrNull() ?: 0L
            ViajesPorSedeScreen(idSede, navController, authViewModel, viajeViewModel, reservaViewModel)
        }
        composable(Routes.VIAJES_POR_CIUDAD) { back ->
            val ciudad = back.arguments?.getString("ciudad") ?: ""
            ViajesPorCiudadScreen(ciudad, navController, authViewModel, viajeViewModel, reservaViewModel)
        }
        composable(Routes.VIAJES_POR_CONDUCTOR) { back ->
            val idConductor = back.arguments?.getString("idConductor")?.toLongOrNull() ?: 0L
            ViajesPorConductorScreen(idConductor, navController, authViewModel, viajeViewModel, reservaViewModel)
        }
        composable(Routes.ADMIN)              { AdminScreen(navController, adminViewModel) }
        composable(Routes.ADMIN_USUARIOS)     { AdminUsuariosScreen(navController, adminViewModel) }
        composable(Routes.ADMIN_VIAJES)       { AdminViajesScreen(navController, adminViewModel) }
        composable(Routes.ADMIN_VEHICULOS)    { AdminVehiculosScreen(navController, adminViewModel) }
        composable(Routes.ADMIN_SEDES)        { AdminSedesScreen(navController, adminViewModel) }
        composable(Routes.ADMIN_ESTADISTICAS) { AdminEstadisticasScreen(navController, adminViewModel) }
        composable(Routes.ADMIN_REPORTES)     { AdminReportesScreen(navController, adminViewModel) }
        composable(Routes.ADMIN_NOTIFICACIONES){ AdminNotificacionesScreen(navController, adminViewModel) }
    }
}