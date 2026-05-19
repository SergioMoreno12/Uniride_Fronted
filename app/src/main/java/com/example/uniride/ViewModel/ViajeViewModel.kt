package com.example.uniride.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniride.Service.SedeService
import com.example.uniride.Service.ViajeService
import com.example.uniride.interfaces.RetrofitClient
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

    private val _sedes     = MutableLiveData<List<Sede>?>(emptyList())
    val sedes: MutableLiveData<List<Sede>?> = _sedes

    private val _mensaje   = MutableLiveData<String?>(null)
    val mensaje: MutableLiveData<String?> = _mensaje

    private val _cargando  = MutableLiveData(false)
    val cargando: MutableLiveData<Boolean> = _cargando

    fun cargarDisponibles() {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) { viajeService.obtenerDisponibles() }
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

    fun publicarViaje(
        origen: String, destino: String, fechaHora: String,
        costo: Double, idVehiculo: Long, idSede: Long
    ) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                withContext(Dispatchers.IO) {
                    viajeService.crearViaje(
                        ViajeDTO(origen, destino, fechaHora, costo, "disponible", idVehiculo, idSede)
                    )
                }
                _mensaje.postValue("¡Viaje publicado con éxito!")
                cargarDisponibles()
            } catch (e: Exception) {
                _mensaje.postValue("Error al publicar: ${e.message}")
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
                cargarDisponibles()
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
                        mapOf(
                            "origen"    to origen,
                            "destino"   to destino,
                            "fechaHora" to fechaHora,
                            "costo"     to costo,
                            "idSede"    to idSede
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