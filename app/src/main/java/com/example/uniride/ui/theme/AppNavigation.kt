package com.example.uniride.ui.theme

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.uniride.Screen.*
import com.example.uniride.ViewModel.*

object Routes {
    const val SPLASH                = "splash"
    const val LOGIN                 = "login"
    const val REGISTER              = "register"

    const val MAIN                  = "main?page={page}"
    fun main(page: Int = 0)         = "main?page=$page"

    const val HOME                  = "home"
    const val MIS_RESERVAS          = "mis_reservas"
    const val PUBLICAR              = "publicar"
    const val MIS_VIAJES            = "mis_viajes"
    const val NOTIFICACIONES        = "notificaciones"
    const val PERFIL                = "perfil"

    const val VIAJES                = "viajes?fecha={fecha}"
    const val VIAJE_DETALLE         = "viaje_detalle/{idViaje}"
    const val HISTORIAL_RESERVAS    = "historial_reservas"
    const val HISTORIAL_VIAJES      = "historial_viajes"
    const val VIAJE_ACTIVO_DETALLE  = "viaje_activo/{idViaje}"
    const val EDITAR_VIAJE          = "editar_viaje/{idViaje}"
    const val EDITAR_PERFIL         = "editar_perfil"
    const val AJUSTES               = "ajustes"
    const val MI_VEHICULO           = "mi_vehiculo"
    const val REGISTRAR_VEHICULO    = "registrar_vehiculo"
    const val CALIFICAR             = "calificar/{idReserva}/{idConductor}"
    const val CONDUCTOR_PERFIL      = "conductor_perfil/{idConductor}"
    const val VIAJES_POR_SEDE       = "viajes_por_sede/{idSede}?fecha={fecha}"
    const val VIAJES_POR_CIUDAD     = "viajes_por_ciudad/{ciudad}?fecha={fecha}"
    const val VIAJES_POR_CONDUCTOR  = "viajes_por_conductor/{idConductor}?fecha={fecha}"
    const val ADMIN                 = "admin"
    const val ADMIN_USUARIOS        = "admin_usuarios"
    const val ADMIN_USUARIO_DETALLE = "admin_usuario_detalle/{idUsuario}"
    const val ADMIN_VIAJES          = "admin_viajes"
    const val ADMIN_VEHICULOS       = "admin_vehiculos"
    const val ADMIN_SEDES           = "admin_sedes"
    const val ADMIN_ESTADISTICAS    = "admin_estadisticas"
    const val ADMIN_REPORTES        = "admin_reportes"
    const val ADMIN_NOTIFICACIONES  = "admin_notificaciones"
    const val RESERVA_DETALLE       = "reserva_detalle/{idReserva}"
    const val LISTA_CONDUCTORES     = "lista_conductores"
    const val CREAR_REPORTE         = "crear_reporte"
    const val PASAJERO_PERFIL = "pasajero_perfil/{idPasajero}"
}

@Composable
fun AppNavigation() {
    val navController     = rememberNavController()
    val authViewModel:    AuthViewModel    = viewModel()
    val viajeViewModel:   ViajeViewModel   = viewModel()
    val reservaViewModel: ReservaViewModel = viewModel()
    // FIX: adminViewModel compartido en toda la navegación
    val adminViewModel:   AdminViewModel   = viewModel()
    val perfilViewModel:  PerfilViewModel  = viewModel()
    val notifViewModel:   NotifViewModel   = viewModel()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(navController, viajeViewModel, authViewModel)
        }
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        composable("register") {
            RegisterScreen(navController, authViewModel)
        }

        // ── Pager principal ─────────────────────────────────────────
        composable(
            "main?page={page}",
            arguments = listOf(navArgument("page") {
                type = NavType.IntType; defaultValue = 0
            })
        ) { back ->
            val startPage = back.arguments?.getInt("page") ?: 0
            MainPagerScreen(
                navController    = navController,
                startPage        = startPage,
                authViewModel    = authViewModel,
                viajeViewModel   = viajeViewModel,
                reservaViewModel = reservaViewModel,
                // FIX: pasar adminViewModel compartido
                adminViewModel   = adminViewModel,
                perfilViewModel  = perfilViewModel,
                notifViewModel   = notifViewModel
            )
        }

        // Pasajero
        composable("home") {
            MainPagerScreen(navController, 0, authViewModel, viajeViewModel, reservaViewModel, adminViewModel, perfilViewModel, notifViewModel)
        }
        composable("mis_reservas") {
            MainPagerScreen(navController, 1, authViewModel, viajeViewModel, reservaViewModel, adminViewModel, perfilViewModel, notifViewModel)
        }

        // Conductor
        composable("mis_viajes") {
            MainPagerScreen(navController, 0, authViewModel, viajeViewModel, reservaViewModel, adminViewModel, perfilViewModel, notifViewModel)
        }
        composable("publicar") {
            MainPagerScreen(navController, 1, authViewModel, viajeViewModel, reservaViewModel, adminViewModel, perfilViewModel, notifViewModel)
        }

        // Compartidos
        composable("notificaciones") {
            MainPagerScreen(navController, 2, authViewModel, viajeViewModel, reservaViewModel, adminViewModel, perfilViewModel, notifViewModel)
        }
        composable("perfil") {
            MainPagerScreen(navController, 3, authViewModel, viajeViewModel, reservaViewModel, adminViewModel, perfilViewModel, notifViewModel)
        }

        // ── Pantallas secundarias ────────────────────────────────────
        composable(
            "viajes?fecha={fecha}",
            arguments = listOf(navArgument("fecha") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { back ->
            ViajesScreen(navController, viajeViewModel, authViewModel,
                back.arguments?.getString("fecha"))
        }

        composable("viaje_detalle/{idViaje}") { back ->
            val id = back.arguments?.getString("idViaje")?.toLongOrNull() ?: 0L
            ViajeDetalleScreen(id, navController, authViewModel, reservaViewModel, viajeViewModel)
        }

        composable("historial_reservas") {
            HistorialReservasScreen(navController, authViewModel, reservaViewModel)
        }

        composable("historial_viajes") {
            HistorialViajesScreen(navController, authViewModel, viajeViewModel)
        }

        composable("viaje_activo/{idViaje}") { back ->
            val id = back.arguments?.getString("idViaje")?.toLongOrNull() ?: 0L
            ViajeActivoDetalleScreen(id, navController, authViewModel, reservaViewModel, viajeViewModel)
        }

        composable("editar_viaje/{idViaje}") { back ->
            val id = back.arguments?.getString("idViaje")?.toLongOrNull() ?: 0L
            EditarViajeScreen(id, navController, authViewModel, viajeViewModel)
        }

        composable("editar_perfil") {
            EditarPerfilScreen(navController, authViewModel, perfilViewModel)
        }

        composable("ajustes") {
            AjustesScreen(navController)
        }

        composable("mi_vehiculo") {
            MiVehiculoScreen(navController, authViewModel, perfilViewModel)
        }

        composable("registrar_vehiculo") {
            RegistrarVehiculoScreen(navController, authViewModel, perfilViewModel)
        }

        composable("calificar/{idReserva}/{idConductor}") { back ->
            val r = back.arguments?.getString("idReserva")?.toLongOrNull()  ?: 0L
            val c = back.arguments?.getString("idConductor")?.toLongOrNull() ?: 0L
            CalificarConductorScreen(r, c, navController, authViewModel)
        }

        composable("conductor_perfil/{idConductor}") { back ->
            val id = back.arguments?.getString("idConductor")?.toLongOrNull() ?: 0L
            ConductorPerfilScreen(id, navController, authViewModel, viajeViewModel)
        }

        composable(
            "viajes_por_sede/{idSede}?fecha={fecha}",
            arguments = listOf(
                navArgument("idSede") { type = NavType.LongType },
                navArgument("fecha")  { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { back ->
            ViajesPorSedeScreen(
                back.arguments?.getLong("idSede") ?: 0L,
                navController, authViewModel, viajeViewModel, reservaViewModel,
                back.arguments?.getString("fecha"))
        }

        composable(
            "viajes_por_ciudad/{ciudad}?fecha={fecha}",
            arguments = listOf(
                navArgument("ciudad") { type = NavType.StringType },
                navArgument("fecha")  { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { back ->
            ViajesPorCiudadScreen(
                back.arguments?.getString("ciudad") ?: "",
                navController, authViewModel, viajeViewModel, reservaViewModel,
                back.arguments?.getString("fecha"))
        }

        composable(
            "viajes_por_conductor/{idConductor}?fecha={fecha}",
            arguments = listOf(
                navArgument("idConductor") { type = NavType.LongType },
                navArgument("fecha")       { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { back ->
            ViajesPorConductorScreen(
                back.arguments?.getLong("idConductor") ?: 0L,
                navController, authViewModel, viajeViewModel, reservaViewModel,
                back.arguments?.getString("fecha"))
        }

        composable("admin")             { AdminScreen(navController, adminViewModel, authViewModel) }
        composable("admin_usuarios")    { AdminUsuariosScreen(navController, adminViewModel) }
        composable("admin_usuario_detalle/{idUsuario}") { back ->
            val id = back.arguments?.getString("idUsuario")?.toLongOrNull() ?: 0L
            AdminUsuarioDetalleScreen(id, navController, adminViewModel)
        }
        composable("admin_viajes")        { AdminViajesScreen(navController, adminViewModel) }
        composable("admin_vehiculos")     { AdminVehiculosScreen(navController, adminViewModel) }
        composable("admin_sedes")         { AdminSedesScreen(navController, adminViewModel) }
        composable("admin_estadisticas")  { AdminEstadisticasScreen(navController, adminViewModel) }
        composable("admin_reportes")      { AdminReportesScreen(navController, adminViewModel) }
        composable("admin_notificaciones"){ AdminNotificacionesScreen(navController, adminViewModel) }

        composable("reserva_detalle/{idReserva}") { back ->
            val id = back.arguments?.getString("idReserva")?.toLongOrNull() ?: 0L
            ReservaDetalleScreen(id, navController, authViewModel)
        }

        composable("lista_conductores") {
            ListaConductoresScreen(navController, authViewModel, viajeViewModel)
        }

        composable("crear_reporte") {
            CrearReporteScreen(navController, authViewModel)
        }

        composable("pasajero_perfil/{idPasajero}") { back ->
            val id = back.arguments?.getString("idPasajero")?.toLongOrNull() ?: 0L
            PasajeroPerfilScreen(id, navController, authViewModel)
        }
    }
}