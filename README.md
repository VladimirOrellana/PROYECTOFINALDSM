# ComunidadActiva

ComunidadActiva es una aplicacion movil Android desarrollada con Kotlin, Jetpack Compose y Firebase. Su objetivo es facilitar la gestion de eventos comunitarios, permitiendo crear eventos, confirmar asistencia, compartir invitaciones, registrar comentarios, calificaciones e historial de participacion.

## Estado del proyecto

Proyecto academico funcional en version demo.

Actualmente la aplicacion permite:

- Registro e inicio de sesion con correo y contraseña.
- Inicio de sesion con Google.
- Registro de usuarios en Firestore.
- Creacion, visualizacion, actualizacion y eliminacion logica de eventos.
- Separacion de eventos por secciones:
    - Mis asistencias confirmadas.
    - Eventos creados por mi.
    - Otros eventos disponibles.
    - Historial de eventos.
- Confirmacion y cancelacion de asistencia.
- Conteo de asistentes confirmados.
- Finalizacion de eventos por parte del creador.
- Comentarios y calificaciones para eventos finalizados.
- Promedio de calificaciones.
- Compartir eventos activos como invitacion.
- Compartir eventos finalizados como resumen.
- Notificaciones locales simples mediante mensajes visuales.
- Formato de fecha visible en dd/mm/aaaa.

## Tecnologias utilizadas

- Kotlin
- Android Studio
- Jetpack Compose
- Firebase Authentication
- Google Sign-In
- Cloud Firestore
- Git y GitHub

## Modulos principales

### Autenticacion

La aplicacion permite registrarse e iniciar sesion mediante correo y contraseña. Tambien integra inicio de sesion con Google para facilitar el acceso de los usuarios.

### Gestion de eventos

Los usuarios pueden crear eventos con titulo, descripcion, fecha, hora y ubicacion. Los eventos pueden editarse, eliminarse logicamente o finalizarse.

La eliminacion es logica, por lo que los eventos no se borran fisicamente de Firestore, sino que cambian su estado.

Estados principales:

```txt
activo
finalizado
eliminado