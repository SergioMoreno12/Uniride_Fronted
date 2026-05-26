package com.example.uniride.model.dto

data class LoginResponse(
    val mensaje:    String  = "",
    val rol:        String  = "",
    val idUsuario:  Long    = 0,
    val nombre:     String  = "",
    val fotoPerfil: String? = null,
    val token:      String? = null
)