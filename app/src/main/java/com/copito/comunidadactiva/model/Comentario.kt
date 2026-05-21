package com.copito.comunidadactiva.model

data class Comentario(
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val comentario: String = "",
    val calificacion: Int = 0,
    val estado: String = "activo",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)