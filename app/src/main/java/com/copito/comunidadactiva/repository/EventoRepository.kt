package com.copito.comunidadactiva.repository

import com.copito.comunidadactiva.model.Evento
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object EventoRepository {

    fun cargarEventos(
        db: FirebaseFirestore,
        onSuccess: (List<Evento>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("events")
            .get()
            .addOnSuccessListener { snapshot ->
                val eventos = snapshot.documents.mapNotNull { document ->
                    document.toObject(Evento::class.java)?.copy(
                        id = document.id
                    )
                }
                    .filter { evento ->
                        evento.estado != "eliminado"
                    }
                    .sortedByDescending { evento ->
                        evento.createdAt
                    }

                onSuccess(eventos)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al cargar eventos.")
            }
    }

    fun guardarEvento(
        db: FirebaseFirestore,
        evento: Evento,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val ahora = System.currentTimeMillis()

        val eventoParaGuardar = evento.copy(
            estado = "activo",
            createdAt = if (evento.createdAt == 0L) ahora else evento.createdAt,
            updatedAt = ahora
        )

        db.collection("events")
            .add(eventoParaGuardar)
            .addOnSuccessListener { documentReference ->
                documentReference.update("id", documentReference.id)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Error al actualizar id del evento.")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al guardar evento.")
            }
    }

    fun actualizarEvento(
        db: FirebaseFirestore,
        evento: Evento,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (evento.id.isBlank()) {
            onError("No se pudo actualizar el evento porque no tiene id.")
            return
        }

        val eventoActualizado = evento.copy(
            updatedAt = System.currentTimeMillis()
        )

        db.collection("events")
            .document(evento.id)
            .set(eventoActualizado, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al actualizar evento.")
            }
    }

    fun eliminarEventoLogico(
        db: FirebaseFirestore,
        eventId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (eventId.isBlank()) {
            onError("No se pudo eliminar el evento porque no tiene id.")
            return
        }

        db.collection("events")
            .document(eventId)
            .update(
                mapOf(
                    "estado" to "eliminado",
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al eliminar evento.")
            }
    }

    fun finalizarEvento(
        db: FirebaseFirestore,
        eventId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (eventId.isBlank()) {
            onError("No se pudo finalizar el evento porque no tiene id.")
            return
        }

        db.collection("events")
            .document(eventId)
            .update(
                mapOf(
                    "estado" to "finalizado",
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al finalizar evento.")
            }
    }
}