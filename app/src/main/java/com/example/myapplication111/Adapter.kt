package com.example.myapplication111


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication111.R // ← reemplaza con el nombre correcto de tu paquete si es necesario

class Adapter(private val messageList: ArrayList<Model>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Tipo de remitente
    private val USER = "user"
    private val BOT = "bot"

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].sender == USER) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_messages, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bot_messages, parent, false)
            BotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position].message
        if (holder is UserViewHolder) {
            holder.userMessage.text = message
        } else if (holder is BotViewHolder) {
            holder.botMessage.text = message
        }
    }

    override fun getItemCount(): Int = messageList.size

    // ViewHolder para mensajes del usuario
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userMessage: TextView = itemView.findViewById(R.id.idTVUser)
    }

    // ViewHolder para mensajes del bot
    inner class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val botMessage: TextView = itemView.findViewById(R.id.idTVBot)
    }
}
