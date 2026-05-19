package com.copito.comunidadactiva.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.copito.comunidadactiva.model.Evento
import com.copito.comunidadactiva.repository.EventoRepository
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EventosHomeScreen(
    modifier: Modifier = Modifier,
    email: String,
    userId: String,
    db: FirebaseFirestore,
    onLogout: () -> Unit
) {
    var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var eventoEditando by remember { mutableStateOf<Evento?>(null) }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }

    fun cargarEventos() {
        cargando = true
        mensaje = ""

        EventoRepository.cargarEventos(
            db = db,
            onSuccess = { lista ->
                eventos = lista
                cargando = false
            },
            onError = { error ->
                mensaje = error
                cargando = false
            }
        )
    }

    fun limpiarFormulario() {
        titulo = ""
        descripcion = ""
        fecha = ""
        hora = ""
        ubicacion = ""
        eventoEditando = null
        mostrarFormulario = false
    }

    fun prepararEdicion(evento: Evento) {
        eventoEditando = evento
        titulo = evento.titulo
        descripcion = evento.descripcion
        fecha = evento.fecha
        hora = evento.hora
        ubicacion = evento.ubicacion
        mostrarFormulario = true
        mensaje = ""
    }

    fun guardarEvento() {
        if (
            titulo.isBlank() ||
            descripcion.isBlank() ||
            fecha.isBlank() ||
            hora.isBlank() ||
            ubicacion.isBlank()
        ) {
            mensaje = "Completa todos los campos del evento."
            return
        }

        cargando = true
        mensaje = ""

        val nuevoEvento = Evento(
            titulo = titulo.trim(),
            descripcion = descripcion.trim(),
            fecha = fecha.trim(),
            hora = hora.trim(),
            ubicacion = ubicacion.trim(),
            creadoPor = userId,
            estado = "activo",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        EventoRepository.guardarEvento(
            db = db,
            evento = nuevoEvento,
            onSuccess = {
                limpiarFormulario()
                mensaje = "Evento creado correctamente."
                cargando = false
                cargarEventos()
            },
            onError = { error ->
                mensaje = error
                cargando = false
            }
        )
    }

    fun actualizarEvento() {
        val eventoActual = eventoEditando

        if (eventoActual == null) {
            mensaje = "No hay evento seleccionado para actualizar."
            return
        }

        if (
            titulo.isBlank() ||
            descripcion.isBlank() ||
            fecha.isBlank() ||
            hora.isBlank() ||
            ubicacion.isBlank()
        ) {
            mensaje = "Completa todos los campos del evento."
            return
        }

        cargando = true
        mensaje = ""

        val eventoActualizado = eventoActual.copy(
            titulo = titulo.trim(),
            descripcion = descripcion.trim(),
            fecha = fecha.trim(),
            hora = hora.trim(),
            ubicacion = ubicacion.trim(),
            updatedAt = System.currentTimeMillis()
        )

        EventoRepository.actualizarEvento(
            db = db,
            evento = eventoActualizado,
            onSuccess = {
                limpiarFormulario()
                mensaje = "Evento actualizado correctamente."
                cargando = false
                cargarEventos()
            },
            onError = { error ->
                mensaje = error
                cargando = false
            }
        )
    }

    fun eliminarEvento(evento: Evento) {
        cargando = true
        mensaje = ""

        EventoRepository.eliminarEventoLogico(
            db = db,
            eventId = evento.id,
            onSuccess = {
                mensaje = "Evento eliminado correctamente."
                cargando = false
                cargarEventos()
            },
            onError = { error ->
                mensaje = error
                cargando = false
            }
        )
    }

    LaunchedEffect(Unit) {
        cargarEventos()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Comunidad Activa",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Sesion iniciada como: $email",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (mostrarFormulario) {
                        limpiarFormulario()
                    } else {
                        mostrarFormulario = true
                        eventoEditando = null
                        mensaje = ""
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (mostrarFormulario) "Cancelar" else "Crear evento")
            }

            OutlinedButton(
                onClick = { cargarEventos() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Actualizar")
            }
        }

        if (mostrarFormulario) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (eventoEditando == null) "Nuevo evento" else "Editar evento",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Titulo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripcion") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha. Ejemplo: 2026-05-18") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        label = { Text("Hora. Ejemplo: 3:00 PM") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ubicacion,
                        onValueChange = { ubicacion = it },
                        label = { Text("Ubicacion") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (eventoEditando == null) {
                                guardarEvento()
                            } else {
                                actualizarEvento()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !cargando
                    ) {
                        Text(
                            if (eventoEditando == null) {
                                "Guardar evento"
                            } else {
                                "Actualizar evento"
                            }
                        )
                    }
                }
            }
        }

        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                color = if (mensaje.startsWith("Error")) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Eventos disponibles",
            style = MaterialTheme.typography.titleLarge
        )

        if (eventos.isEmpty() && !cargando) {
            Text("Aun no hay eventos registrados.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(eventos) { evento ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = evento.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(text = evento.descripcion)
                        Text(text = "Fecha: ${evento.fecha}")
                        Text(text = "Hora: ${evento.hora}")
                        Text(text = "Ubicacion: ${evento.ubicacion}")

                        Button(
                            onClick = { prepararEdicion(evento) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !cargando
                        ) {
                            Text("Editar evento")
                        }

                        OutlinedButton(
                            onClick = { eliminarEvento(evento) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !cargando
                        ) {
                            Text("Eliminar evento")
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesion")
        }
    }
}