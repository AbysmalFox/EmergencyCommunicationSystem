package com.example.emergencycommunicationsystem.socket

import android.util.Log
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class SocketManager {
    
    private var socket: Socket? = null
    
    companion object {
        private const val TAG = "SocketManager"
        private const val SERVER_URL = "http://localhost:3000" // Update with your server URL
    }
    
    fun connect() {
        try {
            socket = IO.socket(SERVER_URL)
            
            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected to Socket.IO server")
            }
            
            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Disconnected from Socket.IO server")
            }
            
            socket?.on("offer") { args ->
                Log.d(TAG, "Received offer: ${args[0]}")
            }
            
            socket?.on("answer") { args ->
                Log.d(TAG, "Received answer: ${args[0]}")
            }
            
            socket?.on("candidate") { args ->
                Log.d(TAG, "Received ICE candidate: ${args[0]}")
            }
            
            socket?.on("hangup") { args ->
                Log.d(TAG, "Received hangup: ${args[0]}")
            }
            
            socket?.connect()
            
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Error connecting to Socket.IO server", e)
        }
    }
    
    fun joinRoom(room: String) {
        socket?.emit("join", room)
        Log.d(TAG, "Joined room: $room")
    }
    
    fun sendOffer(offer: JSONObject, room: String) {
        socket?.emit("offer", offer, room)
        Log.d(TAG, "Sent offer to room: $room")
    }
    
    fun sendAnswer(answer: JSONObject, room: String) {
        socket?.emit("answer", answer, room)
        Log.d(TAG, "Sent answer to room: $room")
    }
    
    fun sendIceCandidate(candidate: JSONObject, room: String) {
        socket?.emit("candidate", candidate, room)
        Log.d(TAG, "Sent ICE candidate to room: $room")
    }
    
    fun sendHangup(payload: JSONObject, room: String) {
        socket?.emit("hangup", payload, room)
        Log.d(TAG, "Sent hangup to room: $room")
    }
    
    fun getSocket(): Socket? = socket
    
    fun disconnect() {
        socket?.disconnect()
        socket = null
        Log.d(TAG, "Socket disconnected")
    }
    
    fun isConnected(): Boolean {
        return socket?.connected() ?: false
    }
}
