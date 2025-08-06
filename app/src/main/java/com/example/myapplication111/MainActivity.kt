package com.example.myapplication111

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import androidx.compose.ui.Alignment
import org.json.JSONObject
import com.example.myapplication111.ui.theme.MyApplication111Theme
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.myapplication111.data.AppDatabase
import com.example.myapplication111.data.MessageEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


data class Message(val sender: String, val content: String)

class MainActivity : ComponentActivity() {

    private val apiKey = BuildConfig.OPENAI_KEY
    private lateinit var db: AppDatabase
    private val messages = mutableStateListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "messages.db").build()
        runBlocking {
            val stored = db.messageDao().getAll()
            messages.addAll(stored.map { Message(it.sender, it.content) })
        }
        setContent {
            MyApplication111Theme {
                ChatScreen()
            }
        }
    }

    @Composable
    fun ChatScreen() {
        var userMessage by remember { mutableStateOf("") }
        val messages = this@MainActivity.messages
        val context = this

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages) { message ->
                    if (message.sender == "user") {
                        UserMessageBubble(message.content)
                    } else {
                        BotMessageBubble(message.content)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = userMessage,
                    onValueChange = { userMessage = it },
                    placeholder = { Text("Escribe tu mensaje") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (userMessage.isNotBlank()) {
                            val content = userMessage
                            this@MainActivity.lifecycleScope.launch {
                                db.messageDao().insert(
                                    MessageEntity(sender = "user", content = content, timestamp = System.currentTimeMillis())
                                )
                            }
                            messages.add(Message("user", content))
                            callChatGPTAPI(context, content) { response ->
                                val botReply = response ?: "No se recibió respuesta"
                                this@MainActivity.lifecycleScope.launch {
                                    db.messageDao().insert(
                                        MessageEntity(sender = "bot", content = botReply, timestamp = System.currentTimeMillis())
                                    )
                                }
                                messages.add(Message("bot", botReply))
                            }
                            userMessage = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Enviar")
                }
            }
        }
    }

    @Composable
    fun UserMessageBubble(text: String) {
        Surface(
            color = Color(0xFFE0E0E0),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = text, modifier = Modifier.padding(8.dp))
        }
    }

    @Composable
    fun BotMessageBubble(text: String) {
        Surface(
            color = Color(0xFFF5F5F5),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = text, modifier = Modifier.padding(8.dp))
        }
    }

    private fun callChatGPTAPI(context: ComponentActivity, mensaje: String, onResult: (String?) -> Unit) {
        val url = "https://api.openai.com/v1/chat/completions"
        val queue = Volley.newRequestQueue(context)

        val messages = org.json.JSONArray()
        // Mensaje de sistema con instrucciones en español
        messages.put(
            JSONObject().apply {
                put("role", "system")
                put("content", """
            Eres un asistente virtual experto en la Escuela Politécnica Nacional (EPN) de Quito. Tu objetivo es ayudar a los estudiantes con todo lo relacionado con trámites universitarios, fechas importantes, formatos oficiales, etc. Responde siempre con pasos claros y específicos para el contexto EPN, usando términos y enlaces oficiales.
        """.trimIndent())
            }
        )

        // Mensajes previos almacenados en la base de datos
        runBlocking {
            val prevMessages = db.messageDao().getAll()
            prevMessages.forEach { msg ->
                val role = if (msg.sender == "user") "user" else "assistant"
                messages.put(
                    JSONObject().apply {
                        put("role", role)
                        put("content", msg.content)
                    }
                )
            }
        }

        // Nuevo mensaje del usuario
        messages.put(
            JSONObject().apply {
                put("role", "user")
                put("content", mensaje)
            }
        )

        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", messages)
            put("temperature", 0.7)
            put("max_tokens", 150)
        }

        val requestBody = jsonBody.toString()

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                onResult(response)
            },
            { error ->
                error.printStackTrace()
                onResult("Error en la petición: ${error.message}")
            }) {

            override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
                val parsed = response?.data?.let { String(it, Charsets.UTF_8) } ?: ""
                return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response))
            }

            override fun getBodyContentType(): String = "application/json"

            override fun getBody(): ByteArray = requestBody.toByteArray(Charsets.UTF_8)

            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $apiKey",
                    "Content-Type" to "application/json"
                )
            }
        }

        queue.add(stringRequest)
    }
}
