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
import androidx.compose.ui.Alignment
import org.json.JSONObject
import com.example.myapplication111.ui.theme.MyApplication111Theme


data class Message(val sender: String, val content: String)

class MainActivity : ComponentActivity() {

    private val apiKey = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplication111Theme {
                ChatScreen()
            }
        }
    }

    @Composable
    fun ChatScreen() {
        var userMessage by remember { mutableStateOf("") }
        val messages = remember { mutableStateListOf<Message>() }
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
                            messages.add(Message("user", userMessage))
                            callChatGPTAPI(context, userMessage) { response ->
                                messages.add(Message("bot", response ?: "No se recibió respuesta"))
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
        messages.put(
            JSONObject().apply {
                put("role", "system")
                put("content", """
            Eres un asistente virtual experto en la Escuela Politécnica Nacional (EPN) de Quito. Tu objetivo es ayudar a los estudiantes con todo lo relacionado con trámites universitarios, fechas importantes, formatos oficiales, etc. Responde siempre con pasos claros y específicos para el contexto EPN, usando términos y enlaces oficiales.
        """.trimIndent())
            }
        )

        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", messages)
        }

        val requestBody = jsonBody.toString()

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val messageObj = choices.getJSONObject(0).getJSONObject("message")
                        val botText = messageObj.getString("content")
                        onResult(botText.trim())
                    } else {
                        onResult("Sin respuesta")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult("Error parseando respuesta")
                }
            },
            { error ->
                error.printStackTrace()
                onResult("Error en la petición: ${error.message}")
            }) {

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
