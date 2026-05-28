package com.example.uniride.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniride.interfaces.RetrofitClient
import com.example.uniride.model.Notificacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotifViewModel : ViewModel() {

    private val _notificaciones = MutableLiveData<List<Notificacion>?>(emptyList())
    val notificaciones: MutableLiveData<List<Notificacion>?> = _notificaciones

    private val _sinLeer = MutableLiveData(0)
    val sinLeer: MutableLiveData<Int> = _sinLeer

    private val _cargando = MutableLiveData(false)
    val cargando: MutableLiveData<Boolean> = _cargando

    fun cargarNotificaciones(idUsuario: Long) {
        viewModelScope.launch {
            _cargando.postValue(true)
            try {
                val lista = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.notificacionesPorUsuario(idUsuario)
                }
                _notificaciones.postValue(lista)
                _sinLeer.postValue(lista.count { !it.leida })
            } catch (e: Exception) { }
            _cargando.postValue(false)
        }
    }

    fun marcarLeida(idNotificacion: Long, idUsuario: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.marcarLeida(idNotificacion)
                }
                cargarNotificaciones(idUsuario)
            } catch (e: Exception) { }
        }
    }

    fun eliminarNotificacion(idNotificacion: Long, idUsuario: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.eliminarNotificacion(idNotificacion)
                }
                // Actualizar lista localmente sin recargar del servidor
                val listaActual = _notificaciones.value?.toMutableList() ?: mutableListOf()
                listaActual.removeAll { it.idNotificacion == idNotificacion }
                _notificaciones.postValue(listaActual)
                _sinLeer.postValue(listaActual.count { !it.leida })
            } catch (e: Exception) { }
        }
    }
}