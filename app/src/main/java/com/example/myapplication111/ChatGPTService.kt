package com.example.myapplication111

import android.content.Context
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import com.android.volley.Response
import com.android.volley.Request

class ChatGPTService(private val context: Context, private val apiKey: String) {

    fun enviarMensaje(mensajeUsuario: String, callback: (String?) -> Unit) {
        val url = "https://api.openai.com/v1/chat/completions"
        val cola = Volley.newRequestQueue(context)

        val cuerpo = """
            {
              "model": "gpt-3.5-turbo",
              "messages": [{"role": "user", "content": "$mensajeUsuario"}]
            }
        """.trimIndent()

        val request = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                val respuesta = extraerRespuesta(response)
                callback(respuesta)
            },
            Response.ErrorListener { error ->
                callback("Error: ${error.message}")
            }) {

            override fun getBody(): ByteArray = cuerpo.toByteArray()

            override fun getHeaders(): MutableMap<String, String> = mutableMapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer $apiKey"
            )
        }

        cola.add(request)
    }

    private fun extraerRespuesta(json: String): String {
        // Extrae el texto de la respuesta
        val indexStart = json.indexOf("content") + 10
        val indexEnd = json.indexOf("\"", indexStart)
        return if (indexStart >= 0 && indexEnd > indexStart) {
            json.substring(indexStart, indexEnd)
        } else {
            "No se pudo leer la respuesta"
        }
    }
}
