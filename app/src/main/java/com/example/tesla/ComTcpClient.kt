package com.example.tesla

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.UnknownHostException

class ComTcpClient(private val ip: String, private val port: Int, private val channel: Channel<Int>) {

    private var socket: Socket? = null
    private val TAG = ComTcpClient::class.java.simpleName

    fun connect() {
        GlobalScope.launch(Dispatchers.Default) {
            Log.d(TAG, "接続開始...")
            try {
                socket = Socket(ip, port)
                channel.send(MSG_CONNECTION_SUCCESS)
            } catch (e: IOException) {
                Log.e(TAG, "IOException", e)
                channel.send(MSG_IOEXCEPTION)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "UnknownHostException", e)
                channel.send(MSG_CONNECTION_FAILED)
            }
        }
    }
    fun sendOrReceive(callback: (OutputStream, InputStream) -> Unit) {
        if (socket == null) throw java.lang.IllegalStateException()
        socket?.also { socket ->
            GlobalScope.launch(Dispatchers.Default) {
                try {
                    if (socket.isConnected) {
                        callback(socket.outputStream, socket.inputStream)
                    } else {
                        channel.send(MSG_CONNECTION_FAILED)
                    }
                } catch (e: IOException) {
                    channel.send(MSG_IOEXCEPTION)
                }
            }
        }
    }
    fun close() {
        if (socket == null) throw java.lang.IllegalStateException()
        socket?.also { socket ->
            GlobalScope.launch(Dispatchers.Default) {
                try {
                    if (socket.isConnected) socket.close()
                } catch (e: IOException) {
                    channel.send(MSG_IOEXCEPTION)
                }
            }
        }
    }
}