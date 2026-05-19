package com.copito.comunidadactiva.model

data class Evento(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val hora: String = "",
    val ubicacion: String = "",
    val creadoPor: String = "",
    val estado: String = "activo",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)