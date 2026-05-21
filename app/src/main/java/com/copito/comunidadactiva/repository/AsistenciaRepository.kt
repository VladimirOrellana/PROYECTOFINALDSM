package com.copito.comunidadactiva.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AsistenciaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val asistenciasRef = db.collection("attendances")

    fun confirmarAsistencia(
        eventId: String,
        userId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (eventId.isBlank() || userId.isBlank()) {
            onError(Exception("No se pudo confirmar la asistencia porque faltan datos."))
            return
        }

        val asistenciaId = "${eventId}_${userId}"
        val ahora = System.currentTimeMillis()

        val data = hashMapOf<String, Any>(
            "id" to asistenciaId,
            "eventId" to eventId,
            "userId" to userId,
            "estado" to "confirmado",
            "fechaConfirmacion" to ahora,
            "updatedAt" to ahora
        )

        asistenciasRef.document(asistenciaId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun cancelarAsistencia(
        eventId: String,
        userId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (eventId.isBlank() || userId.isBlank()) {
            onError(Exception("No se pudo cancelar la asistencia porque faltan datos."))
            return
        }

        val asistenciaId = "${eventId}_${userId}"
        val ahora = System.currentTimeMillis()

        val data = hashMapOf<String, Any>(
            "id" to asistenciaId,
            "eventId" to eventId,
            "userId" to userId,
            "estado" to "cancelado",
            "updatedAt" to ahora
        )

        asistenciasRef.document(asistenciaId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun verificarAsistenciaConfirmada(
        eventId: String,
        userId: String,
        onResult: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (eventId.isBlank() || userId.isBlank()) {
            onResult(false)
            return
        }

        val asistenciaId = "${eventId}_${userId}"

        asistenciasRef.document(asistenciaId)
            .get()
            .addOnSuccessListener { document ->
                val estado = document.getString("estado")
                onResult(estado == "confirmado")
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun contarAsistentesConfirmados(
        eventId: String,
        onResult: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (eventId.isBlank()) {
            onResult(0)
            return
        }

        asistenciasRef
            .whereEqualTo("eventId", eventId)
            .whereEqualTo("estado", "confirmado")
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.size())
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun cargarIdsEventosConfirmadosPorUsuario(
        userId: String,
        onResult: (List<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (userId.isBlank()) {
            onResult(emptyList())
            return
        }

        asistenciasRef
            .whereEqualTo("userId", userId)
            .whereEqualTo("estado", "confirmado")
            .get()
            .addOnSuccessListener { snapshot ->
                val idsEventos = snapshot.documents.mapNotNull { document ->
                    document.getString("eventId")
                }

                onResult(idsEventos)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}