package com.copito.comunidadactiva.repository

import com.copito.comunidadactiva.model.Comentario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ComentarioRepository {

    private val db = FirebaseFirestore.getInstance()
    private val comentariosRef = db.collection("comments")

    fun guardarComentario(
        eventId: String,
        userId: String,
        comentario: String,
        calificacion: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (eventId.isBlank() || userId.isBlank()) {
            onError(Exception("No se pudo guardar el comentario porque faltan datos."))
            return
        }

        if (comentario.isBlank()) {
            onError(Exception("Escribe un comentario antes de guardar."))
            return
        }

        if (calificacion !in 1..5) {
            onError(Exception("La calificacion debe estar entre 1 y 5."))
            return
        }

        val comentarioId = "${eventId}_${userId}"
        val ahora = System.currentTimeMillis()

        val data = hashMapOf<String, Any>(
            "id" to comentarioId,
            "eventId" to eventId,
            "userId" to userId,
            "comentario" to comentario.trim(),
            "calificacion" to calificacion,
            "estado" to "activo",
            "updatedAt" to ahora
        )

        comentariosRef.document(comentarioId)
            .set(data + mapOf("createdAt" to ahora), SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun cargarComentariosPorEvento(
        eventId: String,
        onSuccess: (List<Comentario>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (eventId.isBlank()) {
            onSuccess(emptyList())
            return
        }

        comentariosRef
            .whereEqualTo("eventId", eventId)
            .whereEqualTo("estado", "activo")
            .get()
            .addOnSuccessListener { snapshot ->
                val comentarios = snapshot.documents.mapNotNull { document ->
                    document.toObject(Comentario::class.java)?.copy(
                        id = document.id
                    )
                }

                onSuccess(comentarios)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}