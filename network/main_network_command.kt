package com.example.database_part_3.network

import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.database_part_3.R
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.network_testing.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.net.URL
import kotlin.concurrent.thread

class main_network_command : AppCompatActivity() , MessageListener {

    private val serverUrl = "ws://137.184.72.247:80/"
    val mapper = jacksonObjectMapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.network_testing)

        WebSocketManager.init(serverUrl,this)

        var msg : EditText = findViewById(R.id.enter_message)
        var person_name : EditText = findViewById(R.id.persons_name)

        val login_ : login =login("u","Nitish","a_r")
        val login_data : String = mapper.writeValueAsString(login_)

//        val login_ : JSONObject = JSONObject()
//        login_.put("_s","u")
//        login_.put("N","Nitish")
//        login_.put("type","a_r")

        // this one is for connecting the server
        connectBtn.setOnClickListener {
            thread {
                kotlin.run {
                    WebSocketManager.connect()
                }
            }
            WebSocketManager.sendMessage(login_data)     // login form for server
        }


        // this one is for sending text
        clientSendBtn.setOnClickListener {

            val _data : json_object = json_object("s","Nitish_${person_name.text}",msg.text.toString(),person_name.text.toString(),"Nitish",10,"3:50pm")
            val sending_data : String = mapper.writeValueAsString(_data)

            if (WebSocketManager.sendMessage(sending_data)) {
                addText("the msg send: ${msg.text}\n")
            }
        }


        // this one is for closing connections
        closeConnectionBtn.setOnClickListener {
            WebSocketManager.close()
        }


    }

    override fun onConnectSuccess() {
        addText("connections is success\n")
    }

    override fun onConnectFailed() {
        addText("connections failed!! \n")
    }

    override fun onClose() {
        addText("connections closed!!\n")
    }

    override fun onMessage(text: String?) {
        addText("message: $text\n")
    }

    private fun addText(text: String?) {
        runOnUiThread {
            contentEt.setText(text)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.close()
    }
}