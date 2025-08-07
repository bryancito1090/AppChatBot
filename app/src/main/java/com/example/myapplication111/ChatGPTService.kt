package com.example.myapplication111

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.UnknownHostException
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import com.android.volley.Response
import com.android.volley.Request
import org.json.JSONObject

class ChatGPTService(private val context: Context, private val apiKey: String) {

    fun enviarMensaje(mensajeUsuario: String, callback: (String?) -> Unit) {
        if (!isNetworkAvailable()) {
            callback("Sin conexión a Internet")
            return
        }

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
                val message = if (error.cause is UnknownHostException) {
                    "No se pudo conectar con el servidor. Revisa tu conexión a Internet."
                } else {
                    "Error: ${error.message}"
                }
                callback(message)
            }) {

            override fun getBody(): ByteArray = cuerpo.toByteArray()

            override fun getHeaders(): MutableMap<String, String> = mutableMapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer $apiKey"
            )
        }

        cola.add(request)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun extraerRespuesta(json: String): String {
        return try {
            val jsonObj = JSONObject(json)
            val choices = jsonObj.getJSONArray("choices")
            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                message.getString("content")
            } else {
                "No se encontró contenido en la respuesta"
            }
        } catch (e: Exception) {
            "No se pudo leer la respuesta"
        }
    }
}
