package com.copito.comunidadactiva

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.copito.comunidadactiva.ui.theme.ComunidadActivaTheme
import androidx.credentials.ClearCredentialStateRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.copito.comunidadactiva.screens.EventosHomeScreen

class MainActivity : ComponentActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ComunidadActivaTheme {
                AppPrincipal(
                    auth = auth,
                    db = db
                )
            }
        }
    }
}

@Composable
fun AppPrincipal(
    auth: FirebaseAuth,
    db: FirebaseFirestore
) {
    var usuarioActual by remember { mutableStateOf(auth.currentUser) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (usuarioActual == null) {
            AuthScreen(
                modifier = Modifier.padding(innerPadding),
                auth = auth,
                db = db,
                onLoginSuccess = {
                    usuarioActual = auth.currentUser
                }
            )
        } else {
            EventosHomeScreen(
                modifier = Modifier.padding(innerPadding),
                email = usuarioActual?.email ?: "Usuario",
                userId = usuarioActual?.uid ?: "",
                db = db,
                onLogout = {
                    scope.launch {
                        cerrarSesionCompleta(
                            context = context,
                            auth = auth
                        )
                        usuarioActual = null
                    }
                }
            )
        }
    }
}

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onLoginSuccess: () -> Unit
) {
    var esRegistro by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (esRegistro) "Crear cuenta" else "Iniciar sesion",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (esRegistro) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo electronico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Button(
                    onClick = {
                        mensaje = ""

                        if (correo.isBlank() || password.isBlank()) {
                            mensaje = "Completa el correo y la contraseña."
                            return@Button
                        }

                        if (esRegistro && nombre.isBlank()) {
                            mensaje = "Escribe tu nombre."
                            return@Button
                        }

                        cargando = true

                        if (esRegistro) {
                            registrarUsuario(
                                auth = auth,
                                db = db,
                                nombre = nombre.trim(),
                                correo = correo.trim(),
                                password = password,
                                onSuccess = {
                                    cargando = false
                                    onLoginSuccess()
                                },
                                onError = { error ->
                                    cargando = false
                                    mensaje = error
                                }
                            )
                        } else {
                            iniciarSesion(
                                auth = auth,
                                correo = correo.trim(),
                                password = password,
                                onSuccess = {
                                    cargando = false
                                    onLoginSuccess()
                                },
                                onError = { error ->
                                    cargando = false
                                    mensaje = error
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando
                ) {
                    Text(if (esRegistro) "Registrarme" else "Entrar")
                }

                OutlinedButton(
                    onClick = {
                        mensaje = ""
                        cargando = true

                        scope.launch {
                            iniciarSesionConGoogle(
                                context = context,
                                auth = auth,
                                db = db,
                                onSuccess = {
                                    cargando = false
                                    onLoginSuccess()
                                },
                                onError = { error ->
                                    cargando = false
                                    mensaje = error
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando
                ) {
                    Text("Continuar con Google")
                }

                TextButton(
                    onClick = {
                        esRegistro = !esRegistro
                        mensaje = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (esRegistro)
                            "Ya tengo cuenta"
                        else
                            "Crear una cuenta nueva"
                    )
                }

                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                if (mensaje.isNotBlank()) {
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

fun registrarUsuario(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    nombre: String,
    correo: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    auth.createUserWithEmailAndPassword(correo, password)
        .addOnSuccessListener { resultado ->
            val uid = resultado.user?.uid

            if (uid == null) {
                onError("No se pudo obtener el ID del usuario.")
                return@addOnSuccessListener
            }

            val usuario = hashMapOf(
                "uid" to uid,
                "nombre" to nombre,
                "correo" to correo,
                "rol" to "usuario",
                "proveedor" to "correo",
                "fechaRegistro" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(uid)
                .set(usuario)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { error ->
                    onError("Error guardando usuario: ${error.message}")
                }
        }
        .addOnFailureListener { error ->
            onError("Error al registrarse: ${error.message}")
        }
}

fun iniciarSesion(
    auth: FirebaseAuth,
    correo: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    auth.signInWithEmailAndPassword(correo, password)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { error ->
            onError("Error al iniciar sesion: ${error.message}")
        }
}

suspend fun iniciarSesionConGoogle(
    context: Context,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val credentialManager = CredentialManager.create(context)

        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            context.getString(R.string.default_web_client_id)
        ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val result = credentialManager.getCredential(
            context = context,
            request = request
        )

        val credential = result.credential
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken

        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(firebaseCredential)
            .addOnSuccessListener { authResult ->
                val usuarioFirebase = authResult.user

                if (usuarioFirebase == null) {
                    onError("No se pudo obtener el usuario de Google.")
                    return@addOnSuccessListener
                }

                val uid = usuarioFirebase.uid
                val correo = usuarioFirebase.email ?: ""
                val nombre = usuarioFirebase.displayName ?: "Usuario Google"

                val referenciaUsuario = db.collection("users").document(uid)

                referenciaUsuario.get()
                    .addOnSuccessListener { documento ->
                        if (documento.exists()) {
                            onSuccess()
                        } else {
                            val usuario = hashMapOf(
                                "uid" to uid,
                                "nombre" to nombre,
                                "correo" to correo,
                                "rol" to "usuario",
                                "proveedor" to "google",
                                "fechaRegistro" to System.currentTimeMillis()
                            )

                            referenciaUsuario.set(usuario)
                                .addOnSuccessListener {
                                    onSuccess()
                                }
                                .addOnFailureListener { error ->
                                    onError("Error guardando usuario de Google: ${error.message}")
                                }
                        }
                    }
                    .addOnFailureListener { error ->
                        onError("Error consultando usuario: ${error.message}")
                    }
            }
            .addOnFailureListener { error ->
                onError("Error al iniciar con Google: ${error.message}")
            }

    } catch (error: Exception) {
        onError("No se pudo iniciar con Google: ${error.message}")
    }
}

suspend fun cerrarSesionCompleta(
    context: Context,
    auth: FirebaseAuth
) {
    auth.signOut()

    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    } catch (_: Exception) {
        // Si falla la limpieza de credenciales, no bloqueamos el cierre de sesion.
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    email: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Comunidad Activa",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sesion iniciada como:"
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout
        ) {
            Text("Cerrar sesion")
        }
    }
}