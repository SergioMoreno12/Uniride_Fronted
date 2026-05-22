package com.example.uniride.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniride.Service.SedeService
import com.example.uniride.Service.ViajeService
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Reserva
import com.example.uniride.model.Sede
import com.example.uniride.model.Viaje
import com.example.uniride.model.dto.ViajeDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViajeViewModel : ViewModel() {

    private val viajeService = ViajeService()
    private val sedeService  = SedeService()

    private val _viajes    = MutableLiveData<List<Viaje>?>(emptyList())
    val viajes: MutableLiveData<List<Viaje>?> = _viajes

    private val _misViajes = MutableLiveData<List<Viaje>?>(emptyList())
    val misViajes: MutableLiveData<List<Viaje>?> = _misViajes

    // IDs de viajes que el usuario ya reservó (para no mostrar duplicados en lista)
    private val _viajesReservados = MutableLiveData<Set<Long>>(emptySet())
    val viajesReservados: MutableLiveData<Set<Long>> = _viajesReservados

    // YA NO usamos viajesPropios para filtrar — un usuario tiene un rol fijo
    // Un pasajero no tiene viajes propios; un conductor no reserva
    private val _viajesPropios = MutableLiveData<Set<Long>>(emptySet())
    val viajesPropios: MutableLiveData<Set<Long>> = _viajesPropios

    private val _sedes    = MutableLiveData<List<Sede>?>(emptyList())
    val sedes: MutableLiveData<List<Sede>?> = _sedes

    private val _mensaje  = MutableLiveData<String?>(null)
    val mensaje: MutableLiveData<String?> = _mensaje

    private val _cargando = MutableLiveData(false)
    val cargando: MutableLiveData<Boolean> = _cargando

    // ── Carga todos los viajes disponibles (sin filtro de propios) ──────
    fun cargarDisponibles(idUsuario: Long? = null) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                // Cargar TODOS los viajes disponibles
                val lista = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.obtenerViajes()
                        .filter { it.estado == "disponible" }
                }

                // Solo filtra viajes ya reservados por el usuario
                if (idUsuario != null) {
                    try {
                        val reservas = withContext(Dispatchers.IO) {
                            RetrofitClient.apiService.reservasPorUsuario(idUsuario)
                        }
                        val idsReservados = reservas.mapNotNull { it.viaje?.idViaje }.toSet()
                        _viajesReservados.postValue(idsReservados)
                    } catch (e: Exception) { }
                }

                _viajesPropios.postValue(emptySet()) // ya no filtramos propios
                _viajes.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar viajes: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun cargarSedes() {
        viewModelScope.launch {
            try {
                val lista = withContext(Dispatchers.IO) { sedeService.obtenerSedes() }
                _sedes.postValue(lista)
            } catch (e: Exception) { }
        }
    }

    fun cargarMisViajes(idVehiculo: Long) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) {
                    viajeService.viajesPorVehiculo(idVehiculo)
                }
                _misViajes.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun publicarViajeCompleto(dto: ViajeDTO) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.crearViaje(dto)
                }
                _mensaje.postValue("¡Viaje publicado con éxito!")
            } catch (e: Exception) {
                _mensaje.postValue(e.message ?: "Error al publicar el viaje")
            }
            _cargando.postValue(false)
        }
    }

    fun cancelarViaje(idViaje: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.cancelarViaje(idViaje)
                }
                _mensaje.postValue("Viaje cancelado")
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
        }
    }

    fun editarViaje(
        idViaje: Long, origen: String, destino: String,
        fechaHora: String, costo: Double, idSede: Long
    ) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.editarViaje(
                        idViaje,
                        ViajeDTO(
                            origen = origen, destino = destino,
                            fechaHora = fechaHora, horaLlegada = null,
                            costo = costo, estado = "disponible",
                            descripcionPunto = null, idVehiculo = 0L, idSede = idSede
                        )
                    )
                }
                _mensaje.postValue("Viaje actualizado con éxito")
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun limpiarMensaje() { _mensaje.postValue(null) }
}