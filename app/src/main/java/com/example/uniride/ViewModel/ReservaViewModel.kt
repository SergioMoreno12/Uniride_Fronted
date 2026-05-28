package com.example.uniride.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniride.Service.ReservaService
import com.example.uniride.model.Reserva
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReservaViewModel : ViewModel() {

    private val reservaService = ReservaService()

    private val _misReservas   = MutableLiveData<List<Reserva>?>(emptyList())
    val misReservas: MutableLiveData<List<Reserva>?> = _misReservas

    private val _reservasViaje = MutableLiveData<List<Reserva>?>(emptyList())
    val reservasViaje: MutableLiveData<List<Reserva>?> = _reservasViaje

    private val _mensaje       = MutableLiveData<String?>(null)
    val mensaje: MutableLiveData<String?> = _mensaje

    private val _cargando      = MutableLiveData(false)
    val cargando: MutableLiveData<Boolean> = _cargando

    fun cargarMisReservas(idUsuario: Long) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) {
                    reservaService.reservasPorUsuario(idUsuario)
                }
                _misReservas.postValue(lista)
            } catch (e: Exception) {
                _mensaje.postValue("Error al cargar reservas: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun cargarReservasViaje(idViaje: Long) {
        viewModelScope.launch {
            try {
                val lista = withContext(Dispatchers.IO) {
                    reservaService.reservasPorViaje(idViaje)
                }
                _reservasViaje.postValue(lista)
            } catch (e: Exception) { }
        }
    }

    fun reservar(idUsuario: Long, idViaje: Long) {
        viewModelScope.launch {
            _cargando.postValue(true)
            _mensaje.postValue(null)
            try {
                val response = withContext(Dispatchers.IO) {
                    reservaService.crearReserva(idUsuario, idViaje)
                }
                if (response.isSuccessful) {
                    _mensaje.postValue("¡Reserva realizada con éxito!")
                    cargarMisReservas(idUsuario)
                } else {
                    _mensaje.postValue("No se pudo reservar. El viaje puede estar lleno.")
                }
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun confirmarReserva(idReserva: Long) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                withContext(Dispatchers.IO) { reservaService.confirmarReserva(idReserva) }
                _mensaje.postValue("Reserva confirmada")
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun cancelarReserva(idReserva: Long, idUsuario: Long) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val response = withContext(Dispatchers.IO) {
                    reservaService.cancelarReserva(idReserva)
                }
                if (response.isSuccessful) {
                    _mensaje.postValue("Reserva cancelada")
                    cargarMisReservas(idUsuario)
                } else {
                    _mensaje.postValue("Error al cancelar (${response.code()})")
                }
            } catch (e: Exception) {
                _mensaje.postValue("Error: ${e.message}")
            }
            _cargando.postValue(false)
        }
    }

    fun limpiarMensaje() { _mensaje.postValue(null) }
}