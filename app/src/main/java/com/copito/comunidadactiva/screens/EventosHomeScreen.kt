package com.copito.comunidadactiva.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.copito.comunidadactiva.model.Evento
import com.copito.comunidadactiva.repository.AsistenciaRepository
import com.copito.comunidadactiva.repository.ComentarioRepository
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
    var idsEventosConfirmados by remember { mutableStateOf<List<String>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var eventoEditando by remember { mutableStateOf<Evento?>(null) }

    var seccionSeleccionada by remember { mutableStateOf("creados") }

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
        fecha = formatearFechaParaMostrar(evento.fecha)
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
            fecha = normalizarFechaParaGuardar(fecha),
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
                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
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
            fecha = normalizarFechaParaGuardar(fecha),
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
                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                cargando = false
                cargarEventos()
                cargarMisEventosConfirmados()
            },
            onError = { error ->
                mensaje = error
                cargando = false
            }
        )
    }

    fun finalizarEvento(evento: Evento) {
        cargando = true
        mensaje = ""

        EventoRepository.finalizarEvento(
            db = db,
            eventId = evento.id,
            onSuccess = {
                mensaje = "Evento finalizado correctamente."
                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                cargando = false
                cargarEventos()
                cargarMisEventosConfirmados()
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

    val eventosActivos = eventos.filter { it.estado == "activo" }
    val misAsistencias = eventosActivos.filter { evento ->
        idsEventosConfirmados.contains(evento.id)
    }
    val eventosCreadosPorMi = eventosActivos.filter { evento ->
        evento.creadoPor == userId
    }
    val otrosEventosDisponibles = eventosActivos.filter { evento ->
        evento.creadoPor != userId && !idsEventosConfirmados.contains(evento.id)
    }
    val eventosFinalizados = eventos.filter { evento ->
        evento.estado == "finalizado"
    }

    val tituloSeccionActual = when (seccionSeleccionada) {
        "asistencias" -> "Mis asistencias confirmadas"
        "creados" -> "Eventos creados por mi"
        "disponibles" -> "Otros eventos disponibles"
        "finalizados" -> "Historial de eventos"
        else -> "Eventos"
    }

    val mensajeVacioSeccionActual = when (seccionSeleccionada) {
        "asistencias" -> "Aun no has confirmado asistencia a ningun evento."
        "creados" -> "Aun no has creado eventos activos."
        "disponibles" -> "No hay otros eventos disponibles por ahora."
        "finalizados" -> "Aun no hay eventos en el historial."
        else -> "No hay eventos para mostrar."
    }

    val eventosSeccionActual = when (seccionSeleccionada) {
        "asistencias" -> misAsistencias
        "creados" -> eventosCreadosPorMi
        "disponibles" -> otrosEventosDisponibles
        "finalizados" -> eventosFinalizados
        else -> emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
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
                onClick = {
                    cargarEventos()
                    cargarMisEventosConfirmados()
                },
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
                        label = { Text("Fecha. Ejemplo: 25/05/2026") },
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


        SelectorSeccionesEventos(
            seccionSeleccionada = seccionSeleccionada,
            onCambiarSeccion = { nuevaSeccion ->
                seccionSeleccionada = nuevaSeccion
            },
            totalMisAsistencias = misAsistencias.size,
            totalCreados = eventosCreadosPorMi.size,
            totalDisponibles = otrosEventosDisponibles.size,
            totalFinalizados = eventosFinalizados.size
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SeccionEventos(
                    titulo = tituloSeccionActual,
                    mensajeVacio = mensajeVacioSeccionActual,
                    eventos = eventosSeccionActual,
                    userId = userId,
                    asistenciaRepository = asistenciaRepository,
                    comentarioRepository = comentarioRepository,
                    idsEventosConfirmados = idsEventosConfirmados,
                    onAsistenciaActualizada = {
                        cargarMisEventosConfirmados()
                    },
                    onEditar = { prepararEdicion(it) },
                    onEliminar = { eliminarEvento(it) },
                    onFinalizar = { finalizarEvento(it) },
                    onCompartir = { compartirEvento(context, it) }
                )
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
fun SelectorSeccionesEventos(
    seccionSeleccionada: String,
    onCambiarSeccion: (String) -> Unit,
    totalMisAsistencias: Int,
    totalCreados: Int,
    totalDisponibles: Int,
    totalFinalizados: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Filtrar eventos",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BotonSeccionEvento(
                texto = "Asist. ($totalMisAsistencias)",
                seleccionada = seccionSeleccionada == "asistencias",
                onClick = { onCambiarSeccion("asistencias") },
                modifier = Modifier.weight(1f)
            )

            BotonSeccionEvento(
                texto = "Creados ($totalCreados)",
                seleccionada = seccionSeleccionada == "creados",
                onClick = { onCambiarSeccion("creados") },
                modifier = Modifier.weight(1f)
            )

            BotonSeccionEvento(
                texto = "Disp. ($totalDisponibles)",
                seleccionada = seccionSeleccionada == "disponibles",
                onClick = { onCambiarSeccion("disponibles") },
                modifier = Modifier.weight(1f)
            )

            BotonSeccionEvento(
                texto = "Hist. ($totalFinalizados)",
                seleccionada = seccionSeleccionada == "finalizados",
                onClick = { onCambiarSeccion("finalizados") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BotonSeccionEvento(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (seleccionada) {
        Button(
            onClick = onClick,
            modifier = modifier.height(42.dp)
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.bodySmall
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(42.dp)
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
@Composable
fun SeccionEventos(
    titulo: String,
    mensajeVacio: String,
    eventos: List<Evento>,
    userId: String,
    asistenciaRepository: AsistenciaRepository,
    comentarioRepository: ComentarioRepository,
    idsEventosConfirmados: List<String>,
    onAsistenciaActualizada: () -> Unit,
    onEditar: (Evento) -> Unit,
    onEliminar: (Evento) -> Unit,
    onFinalizar: (Evento) -> Unit,
    onCompartir: (Evento) -> Unit
) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleLarge
    )

    if (eventos.isEmpty()) {
        Text(
            text = mensajeVacio,
            style = MaterialTheme.typography.bodyMedium
        )
    } else {
        eventos.forEach { evento ->
            EventoCard(
                evento = evento,
                userId = userId,
                asistenciaRepository = asistenciaRepository,
                comentarioRepository = comentarioRepository,
                asistenciaConfirmadaEnLista = idsEventosConfirmados.contains(evento.id),
                onAsistenciaActualizada = onAsistenciaActualizada,
                onEditar = onEditar,
                onEliminar = onEliminar,
                onFinalizar = onFinalizar,
                onCompartir = onCompartir
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun EventoCard(
    evento: Evento,
    userId: String,
    asistenciaRepository: AsistenciaRepository,
    comentarioRepository: ComentarioRepository,
    asistenciaConfirmadaEnLista: Boolean,
    onAsistenciaActualizada: () -> Unit,
    onEditar: (Evento) -> Unit,
    onEliminar: (Evento) -> Unit,
    onFinalizar: (Evento) -> Unit,
    onCompartir: (Evento) -> Unit
) {
    val esCreador = evento.creadoPor == userId
    val estaFinalizado = evento.estado == "finalizado"

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = evento.titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Text(text = evento.descripcion)
            Text(text = "Fecha: ${formatearFechaParaMostrar(evento.fecha)}")
            Text(text = "Hora: ${evento.hora}")
            Text(text = "Ubicacion: ${evento.ubicacion}")

            if (estaFinalizado) {
                Text(
                    text = "Estado: Evento finalizado",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (asistenciaConfirmadaEnLista) {
                        "Mi asistencia: confirmada"
                    } else {
                        "Mi asistencia: no confirmada"
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                TotalAsistentesEvento(
                    eventId = evento.id,
                    asistenciaRepository = asistenciaRepository
                )

                Text(
                    text = "Este evento forma parte del historial.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                if (asistenciaConfirmadaEnLista) {
                    Text(
                        text = "Ya confirmaste asistencia a este evento.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                AsistenciaEventoSection(
                    eventId = evento.id,
                    userId = userId,
                    asistenciaRepository = asistenciaRepository,
                    onAsistenciaActualizada = onAsistenciaActualizada
                )
            }

            ComentariosEventoSection(
                eventId = evento.id,
                userId = userId,
                comentarioRepository = comentarioRepository,
                comentariosHabilitados = estaFinalizado
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = { onCompartir(evento) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (estaFinalizado) {
                        "Compartir resumen del evento"
                    } else {
                        "Compartir evento"
                    }
                )
            }

            if (esCreador && !estaFinalizado) {
                Button(
                    onClick = { onEditar(evento) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar evento")
                }

                OutlinedButton(
                    onClick = { onFinalizar(evento) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Finalizar evento")
                }

                OutlinedButton(
                    onClick = { onEliminar(evento) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Eliminar evento")
                }
            }
        }
    }
}

@Composable
fun TotalAsistentesEvento(
    eventId: String,
    asistenciaRepository: AsistenciaRepository
) {
    var totalAsistentes by remember { mutableStateOf(0) }

    LaunchedEffect(eventId) {
        if (eventId.isNotBlank()) {
            asistenciaRepository.contarAsistentesConfirmados(
                eventId = eventId,
                onResult = { total ->
                    totalAsistentes = total
                },
                onError = {
                    totalAsistentes = 0
                }
            )
        }
    }

    Text(
        text = "Total de asistentes confirmados: $totalAsistentes",
        style = MaterialTheme.typography.bodySmall
    )
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

    val context = LocalContext.current

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
                mensaje = "No se pudo verificar la asistencia."
            }
        )

        asistenciaRepository.contarAsistentesConfirmados(
            eventId = eventId,
            onResult = { total ->
                totalAsistentes = total
            },
            onError = {
                mensaje = "No se pudo cargar el total de asistentes."
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
                        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
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

@Composable
fun ComentariosEventoSection(
    eventId: String,
    userId: String,
    comentarioRepository: ComentarioRepository,
    comentariosHabilitados: Boolean = false
) {
    val context = LocalContext.current

    var comentarios by remember {
        mutableStateOf<List<com.copito.comunidadactiva.model.Comentario>>(emptyList())
    }
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

    if (!comentariosHabilitados) {
        Text(
            text = "Los comentarios estaran disponibles cuando el evento finalice.",
            style = MaterialTheme.typography.bodySmall
        )
    } else {
        Text(
            text = "El evento ya finalizo. Puedes dejar tu opinion sobre la actividad.",
            style = MaterialTheme.typography.bodySmall
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
                    Toast.makeText(context, mensajeComentario, Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (calificacion == null || calificacion !in 1..5) {
                    mensajeComentario = "La calificacion debe ser un numero del 1 al 5."
                    Toast.makeText(context, mensajeComentario, Toast.LENGTH_SHORT).show()
                    return@Button
                }

                comentarioRepository.guardarComentario(
                    eventId = eventId,
                    userId = userId,
                    comentario = comentarioTexto,
                    calificacion = calificacion,
                    onSuccess = {
                        mensajeComentario = "Comentario guardado."
                        Toast.makeText(context, mensajeComentario, Toast.LENGTH_SHORT).show()
                        comentarioTexto = ""
                        calificacionTexto = ""
                        cargarComentarios()
                    },
                    onError = {
                        mensajeComentario = "No se pudo guardar el comentario."
                        Toast.makeText(context, mensajeComentario, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar comentario")
        }
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
    } else if (comentariosHabilitados) {
        Text(
            text = "Aun no hay comentarios registrados.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

fun compartirEvento(
    context: Context,
    evento: Evento
) {
    val fechaVisible = formatearFechaParaMostrar(evento.fecha)

    val textoCompartir = if (evento.estado == "finalizado") {
        """
            Evento finalizado: ${evento.titulo}

            Fecha: $fechaVisible
            Hora: ${evento.hora}
            Ubicacion: ${evento.ubicacion}

            Descripcion:
            ${evento.descripcion}

            Este evento ya finalizo y forma parte del historial de ComunidadActiva.

            Puedes consultar los eventos comunitarios, asistencias, comentarios y calificaciones desde la app ComunidadActiva.

            Link de descarga:
            https://comunidadactiva.app/descargar

            Compartido desde ComunidadActiva.
        """.trimIndent()
    } else {
        """
            Te invito al evento: ${evento.titulo}

            Fecha: $fechaVisible
            Hora: ${evento.hora}
            Ubicacion: ${evento.ubicacion}

            Descripcion:
            ${evento.descripcion}

            Para confirmar tu asistencia, descarga la app ComunidadActiva e inicia sesion con tu cuenta.

            Link de descarga:
            https://comunidadactiva.app/descargar

            Despues de ingresar, busca el evento y presiona "Confirmar asistencia".

            Compartido desde ComunidadActiva.
        """.trimIndent()
    }

    val asunto = if (evento.estado == "finalizado") {
        "Resumen de evento comunitario: ${evento.titulo}"
    } else {
        "Invitacion a evento comunitario: ${evento.titulo}"
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, asunto)
        putExtra(Intent.EXTRA_TEXT, textoCompartir)
    }

    context.startActivity(
        Intent.createChooser(intent, "Compartir evento")
    )
}

fun formatearFechaParaMostrar(fecha: String): String {
    val limpia = fecha.trim()

    return when {
        limpia.contains("-") -> {
            val partes = limpia.split("-")
            if (partes.size == 3 && partes[0].length == 4) {
                val anio = partes[0]
                val mes = partes[1].padStart(2, '0')
                val dia = partes[2].padStart(2, '0')
                "$dia/$mes/$anio"
            } else {
                limpia
            }
        }

        limpia.contains("/") -> {
            val partes = limpia.split("/")
            if (partes.size == 3) {
                val dia = partes[0].padStart(2, '0')
                val mes = partes[1].padStart(2, '0')
                val anio = partes[2]
                "$dia/$mes/$anio"
            } else {
                limpia
            }
        }

        else -> limpia
    }
}

fun normalizarFechaParaGuardar(fecha: String): String {
    return formatearFechaParaMostrar(fecha)
}

