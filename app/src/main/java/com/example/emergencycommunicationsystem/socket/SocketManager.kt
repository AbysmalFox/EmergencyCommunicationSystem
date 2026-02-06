package com.example.emergencycommunicationsystem.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException

class SocketManager(
    private val baseUrl: String = SocketConfig.PROD_BASE_URL,
) {

    private var socket: Socket? = null

    companion object {
        private const val TAG = "SocketManager"
    }

    fun connect(
        onConnected: (() -> Unit)? = null,
        onConnectError: ((Any?) -> Unit)? = null,
        onDisconnected: ((Any?) -> Unit)? = null,
    ) {
        if (socket?.connected() == true) return

        val opts = IO.Options().apply {
            path = SocketConfig.SOCKET_PATH
            transports = arrayOf("websocket", "polling")
            reconnection = true
            forceNew = true
            timeout = 8000
        }

        try {
            socket = IO.socket(baseUrl, opts)
        } catch (e: URISyntaxException) {
            throw RuntimeException("Invalid Socket URL: $baseUrl", e)
        }

        socket?.on(Socket.EVENT_CONNECT) {
            joinRoom(SocketConfig.ROOM)
            onConnected?.invoke()
        }

        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            onConnectError?.invoke(args.firstOrNull())
        }

        socket?.on(Socket.EVENT_DISCONNECT) { args ->
            onDisconnected?.invoke(args.firstOrNull())
        }

        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    fun joinRoom(room: String) {
        socket?.emit("join", room)
        Log.d(TAG, "Joined room: $room")
    }

    fun onOffer(listener: Emitter.Listener) = socket?.on("offer", listener)
    fun onAnswer(listener: Emitter.Listener) = socket?.on("answer", listener)
    fun onCandidate(listener: Emitter.Listener) = socket?.on("candidate", listener)
    fun onHangup(listener: Emitter.Listener) = socket?.on("hangup", listener)
    fun onCallMessage(listener: Emitter.Listener) = socket?.on("call-message", listener)

    fun offAllSignaling() {
        socket?.off("offer")
        socket?.off("answer")
        socket?.off("candidate")
        socket?.off("hangup")
        socket?.off("call-message")
    }

    fun sendOffer(payload: JSONObject) {
        socket?.emit("offer", payload, SocketConfig.ROOM)
    }

    fun sendAnswer(payload: JSONObject) {
        socket?.emit("answer", payload, SocketConfig.ROOM)
    }

    fun sendCandidate(payload: JSONObject) {
        socket?.emit("candidate", payload, SocketConfig.ROOM)
    }

    fun sendHangup(payload: JSONObject) {
        socket?.emit("hangup", payload, SocketConfig.ROOM)
    }

    fun sendCallMessage(payload: JSONObject) {
        socket?.emit("call-message", payload, SocketConfig.ROOM)
    }

    fun isConnected(): Boolean {
        return socket?.connected() ?: false
    }
}
