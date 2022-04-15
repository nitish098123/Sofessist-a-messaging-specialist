package com.example.database_part_3.network

import android.util.Log
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

object WebSocketManager {
    private val TAG = WebSocketManager::class.java.simpleName
    private const val MAX_NUM = 5
    private const val MILLIS = 5000
    private lateinit var client: OkHttpClient
    private lateinit var request: Request
    private lateinit var messageListener : MessageListener
    private var isConnect = false
    private var connectNum = 0
    private lateinit var mWebSocket: WebSocket

    fun init(url: String, _messageListener: MessageListener){

        client = OkHttpClient.Builder()
            .writeTimeout(7, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .build()
        request = Request.Builder().url(url).build()
        messageListener = _messageListener
    }


    // connect to the websocket
    fun connect() {
        if (isConnect()) {
            Log.i(TAG, "web socket connected")
            return
        }
        client.newWebSocket(request, createListener())
    }


    // reconnections the websocket API
    fun reconnect() {
        if (connectNum <= MAX_NUM) {
            try {
                Thread.sleep(MILLIS.toLong())
                connect()
                connectNum++
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        } else {
            Log.i(TAG,"reconnect over $MAX_NUM,please check url or network")
        }
    }


    fun isConnect(): Boolean {
        return isConnect
    }


    // this send messages directly to the server
    fun sendMessage(text: String): Boolean {
        return if (!isConnect()) false else mWebSocket.send(text)
    }


    // closing the connections
    fun close() {
        if (isConnect()) {
            mWebSocket.cancel()
            mWebSocket.close(1001, "connections is closed!!")
    } }

    private fun createListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(
                webSocket: WebSocket,
                response : Response
            ) {
                super.onOpen(webSocket, response)
                Log.d(TAG, "open:$response")
                mWebSocket = webSocket
//                isConnect = response.code() == 101

                if(response.code==101){        // this means client is connected to the server
                    isConnect = true
                }
                if (!isConnect) {          //  if  teh above isConnect status is not true then this functions will activate to reconnect
                    reconnect()
                } else {
                    Log.i(TAG, "connect success.")
                    messageListener.onConnectSuccess()
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                messageListener.onMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                messageListener.onMessage(bytes.base64())
            }

            override fun onClosing(
                webSocket: WebSocket,
                code: Int,
                reason: String
            ){
                super.onClosing(webSocket, code, reason)
                isConnect = false
                messageListener.onClose()
            }

            override fun onClosed(
                webSocket: WebSocket,
                code: Int,
                reason: String
            ){
                super.onClosed(webSocket, code, reason)
                isConnect = false
                messageListener.onClose()
            }

        override fun onFailure(
            webSocket: WebSocket,
            t: Throwable,
            response: Response?
           ){
             super.onFailure(webSocket, t, response)
                Log.i(TAG,"connect failed throwable：" + t.message)
                isConnect = false
                messageListener.onConnectFailed()
                reconnect()
           }
        }
    }
}