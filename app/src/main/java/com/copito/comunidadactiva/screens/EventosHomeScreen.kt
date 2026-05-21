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
import com.copito.comunidadactiva.repository.AsistenciaRepository
import com.copito.comunidadactiva.repository.EventoRepository
import com.copito.comunidadactiva.repository.ComentarioRepository
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext


@Composable
fun EventosHomeScreen(
    modifier: Modifier = Modifier,
    email: String,
    userId: String,
    db: FirebaseFirestore,
    onLogout: () -> Unit
) {
    var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var idsEventosConfirmados by remember { mutableStateOf<List<String>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var eventoEditando by remember { mutableStateOf<Evento?>(null) }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }

    val asistenciaRepository = remember { AsistenciaRepository() }
    val comentarioRepository = remember { ComentarioRepository() }
    val context = LocalContext.current

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

    fun cargarMisEventosConfirmados() {
        asistenciaRepository.cargarIdsEventosConfirmadosPorUsuario(
            userId = userId,
            onResult = { ids ->
                idsEventosConfirmados = ids
            },
            onError = {
                mensaje = "No se pudieron cargar tus eventos confirmados."
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
        cargarMisEventosConfirmados()
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

        val misEventosConfirmados = eventos.filter { evento ->
            idsEventosConfirmados.contains(evento.id)
        }

        Text(
            text = "Mis eventos confirmados",
            style = MaterialTheme.typography.titleLarge
        )

        if (misEventosConfirmados.isEmpty()) {
            Text("Aun no has confirmado asistencia a ningun evento.")
        } else {
            misEventosConfirmados.forEach { evento ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = evento.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(text = "Fecha: ${evento.fecha}")
                        Text(text = "Hora: ${evento.hora}")
                        Text(text = "Ubicacion: ${evento.ubicacion}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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

                        AsistenciaEventoSection(
                            eventId = evento.id,
                            userId = userId,
                            asistenciaRepository = asistenciaRepository,
                            onAsistenciaActualizada = {
                                cargarMisEventosConfirmados()
                            }
                        )

                        ComentariosEventoSection(
                            eventId = evento.id,
                            userId = userId,
                            comentarioRepository = comentarioRepository
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = { compartirEvento(context, evento) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Compartir evento")
                        }

                        Button(
                            onClick = { prepararEdicion(evento) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !cargando
                        ) {
                            Text("Editar evento")
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

@Composable
fun AsistenciaEventoSection(
    eventId: String,
    userId: String,
    asistenciaRepository: AsistenciaRepository,
    onAsistenciaActualizada: () -> Unit = {}
) {
    var asistenciaConfirmada by remember { mutableStateOf(false) }
    var totalAsistentes by remember { mutableStateOf(0) }
    var mensaje by remember { mutableStateOf("") }

    fun cargarEstadoAsistencia() {
        if (eventId.isBlank() || userId.isBlank()) {
            asistenciaConfirmada = false
            totalAsistentes = 0
            return
        }

        asistenciaRepository.verificarAsistenciaConfirmada(
            eventId = eventId,
            userId = userId,
            onResult = { confirmado ->
                asistenciaConfirmada = confirmado
            },
            onError = {
                mensaje = "No se pudo verificar la asistencia"
            }
        )

        asistenciaRepository.contarAsistentesConfirmados(
            eventId = eventId,
            onResult = { total ->
                totalAsistentes = total
            },
            onError = {
                mensaje = "No se pudo cargar el total de asistentes"
            }
        )
    }

    LaunchedEffect(eventId, userId) {
        cargarEstadoAsistencia()
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Asistentes confirmados: $totalAsistentes",
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(6.dp))

    if (asistenciaConfirmada) {
        OutlinedButton(
            onClick = {
                asistenciaRepository.cancelarAsistencia(
                    eventId = eventId,
                    userId = userId,
                    onSuccess = {
                        mensaje = "Asistencia cancelada."
                        cargarEstadoAsistencia()
                        onAsistenciaActualizada()
                    },
                    onError = {
                        mensaje = "No se pudo cancelar la asistencia."
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar asistencia")
        }
    } else {
        Button(
            onClick = {
                asistenciaRepository.confirmarAsistencia(
                    eventId = eventId,
                    userId = userId,
                    onSuccess = {
                        mensaje = "Asistencia confirmada."
                        cargarEstadoAsistencia()
                        onAsistenciaActualizada()
                    },
                    onError = {
                        mensaje = "No se pudo confirmar la asistencia."
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar asistencia")
        }
    }

    if (mensaje.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

fun compartirEvento(
    context: Context,
    evento: Evento
) {
    val textoCompartir = """
    Te invito al evento: ${evento.titulo}

    Fecha: ${evento.fecha}
    Hora: ${evento.hora}
    Ubicacion: ${evento.ubicacion}

    Descripcion:
    ${evento.descripcion}

    Para confirmar asistencia, inicia sesion en la app ComunidadActiva y presiona el boton "Confirmar asistencia" dentro del evento.

    Compartido desde ComunidadActiva.
""".trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Evento comunitario: ${evento.titulo}")
        putExtra(Intent.EXTRA_TEXT, textoCompartir)
    }

    context.startActivity(
        Intent.createChooser(intent, "Compartir evento")
    )
}

@Composable
fun ComentariosEventoSection(
    eventId: String,
    userId: String,
    comentarioRepository: ComentarioRepository
) {
    var comentarios by remember { mutableStateOf<List<com.copito.comunidadactiva.model.Comentario>>(emptyList()) }
    var comentarioTexto by remember { mutableStateOf("") }
    var calificacionTexto by remember { mutableStateOf("") }
    var mensajeComentario by remember { mutableStateOf("") }

    fun cargarComentarios() {
        if (eventId.isBlank()) {
            comentarios = emptyList()
            return
        }

        comentarioRepository.cargarComentariosPorEvento(
            eventId = eventId,
            onSuccess = { lista ->
                comentarios = lista
            },
            onError = {
                mensajeComentario = "No se pudieron cargar los comentarios."
            }
        )
    }

    LaunchedEffect(eventId) {
        cargarComentarios()
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "Comentarios y calificaciones",
        style = MaterialTheme.typography.titleSmall
    )

    OutlinedTextField(
        value = comentarioTexto,
        onValueChange = { comentarioTexto = it },
        label = { Text("Comentario") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = calificacionTexto,
        onValueChange = { calificacionTexto = it },
        label = { Text("Calificacion 1 a 5") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Button(
        onClick = {
            val calificacion = calificacionTexto.toIntOrNull()

            if (comentarioTexto.isBlank()) {
                mensajeComentario = "Escribe un comentario."
                return@Button
            }

            if (calificacion == null || calificacion !in 1..5) {
                mensajeComentario = "La calificacion debe ser un numero del 1 al 5."
                return@Button
            }

            comentarioRepository.guardarComentario(
                eventId = eventId,
                userId = userId,
                comentario = comentarioTexto,
                calificacion = calificacion,
                onSuccess = {
                    mensajeComentario = "Comentario guardado."
                    comentarioTexto = ""
                    calificacionTexto = ""
                    cargarComentarios()
                },
                onError = {
                    mensajeComentario = "No se pudo guardar el comentario."
                }
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Guardar comentario")
    }

    if (mensajeComentario.isNotBlank()) {
        Text(
            text = mensajeComentario,
            style = MaterialTheme.typography.bodySmall
        )
    }

    if (comentarios.isNotEmpty()) {
        val promedio = comentarios.map { it.calificacion }.average()

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Resumen: ${comentarios.size} comentario(s) - Promedio: %.1f/5".format(promedio),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Comentarios del evento:",
            style = MaterialTheme.typography.bodyMedium
        )

        comentarios.forEach { comentario ->
            Text(
                text = "★ ${comentario.calificacion}/5 - ${comentario.comentario}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}