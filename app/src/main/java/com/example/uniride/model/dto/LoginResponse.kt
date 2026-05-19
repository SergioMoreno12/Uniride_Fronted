package com.example.uniride.model.dto

data class LoginResponse(
    val mensaje: String = "",
    val rol: String = "",
    val idUsuario: Long? = null,
    val nombre: String = ""
)