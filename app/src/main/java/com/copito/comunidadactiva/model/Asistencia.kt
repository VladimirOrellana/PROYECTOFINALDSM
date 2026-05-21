package com.copito.comunidadactiva.model

data class Asistencia(
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val estado: String = "confirmado",
    val fechaConfirmacion: Long = 0L,
    val updatedAt: Long = 0L
)