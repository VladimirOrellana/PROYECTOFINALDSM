package com.copito.comunidadactiva.repository

import com.copito.comunidadactiva.model.Evento
import com.google.firebase.firestore.FirebaseFirestore

object EventoRepository {

    fun cargarEventos(
        db: FirebaseFirestore,
        onSuccess: (List<Evento>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("events")
            .whereEqualTo("estado", "activo")
            .get()
            .addOnSuccessListener { resultado ->
                val listaEventos = resultado.documents.map { documento ->
                    Evento(
                        id = documento.id,
                        titulo = documento.getString("titulo") ?: "",
                        descripcion = documento.getString("descripcion") ?: "",
                        fecha = documento.getString("fecha") ?: "",
                        hora = documento.getString("hora") ?: "",
                        ubicacion = documento.getString("ubicacion") ?: "",
                        creadoPor = documento.getString("creadoPor") ?: "",
                        estado = documento.getString("estado") ?: "activo",
                        createdAt = documento.getLong("createdAt") ?: 0L,
                        updatedAt = documento.getLong("updatedAt") ?: 0L
                    )
                }.sortedByDescending { it.createdAt }

                onSuccess(listaEventos)
            }
            .addOnFailureListener { error ->
                onError("Error cargando eventos: ${error.message}")
            }
    }

    fun guardarEvento(
        db: FirebaseFirestore,
        evento: Evento,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val datosEvento = hashMapOf<String, Any>(
            "titulo" to evento.titulo,
            "descripcion" to evento.descripcion,
            "fecha" to evento.fecha,
            "hora" to evento.hora,
            "ubicacion" to evento.ubicacion,
            "creadoPor" to evento.creadoPor,
            "estado" to evento.estado,
            "createdAt" to evento.createdAt,
            "updatedAt" to evento.updatedAt
        )

        db.collection("events")
            .add(datosEvento)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError("Error guardando evento: ${error.message}")
            }
    }

    fun actualizarEvento(
        db: FirebaseFirestore,
        evento: Evento,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (evento.id.isBlank()) {
            onError("No se pudo obtener el ID del evento.")
            return
        }

        val datosActualizados = mapOf(
            "titulo" to evento.titulo,
            "descripcion" to evento.descripcion,
            "fecha" to evento.fecha,
            "hora" to evento.hora,
            "ubicacion" to evento.ubicacion,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("events")
            .document(evento.id)
            .update(datosActualizados)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError("Error actualizando evento: ${error.message}")
            }
    }

    fun eliminarEventoLogico(
        db: FirebaseFirestore,
        eventId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (eventId.isBlank()) {
            onError("No se pudo obtener el ID del evento.")
            return
        }

        val datosActualizados = mapOf(
            "estado" to "eliminado",
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("events")
            .document(eventId)
            .update(datosActualizados)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError("Error eliminando evento: ${error.message}")
            }
    }
}