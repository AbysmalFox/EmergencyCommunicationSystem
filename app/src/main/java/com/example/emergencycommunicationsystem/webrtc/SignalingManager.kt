package com.example.emergencycommunicationsystem.webrtc

import android.content.Context
import android.util.Log
import com.example.emergencycommunicationsystem.socket.SocketManager
import org.json.JSONObject
import org.webrtc.*

class SignalingManager(private val context: Context) {
    
    private val webRTCManager = WebRTCManager(context)
    private val socketManager = SocketManager()
    private var currentRoom: String? = null
    
    companion object {
        private const val TAG = "SignalingManager"
    }
    
    fun connect(room: String) {
        currentRoom = room
        
        // Connect to Socket.IO server
        socketManager.connect()
        
        // Join the specified room
        socketManager.joinRoom(room)
        
        // Set up Socket.IO event listeners
        setupSocketListeners()
        
        // Create PeerConnection after socket is connected
        socketManager.getSocket()?.let { socket ->
            webRTCManager.createPeerConnection(socket)
            webRTCManager.addLocalAudioTrack()
        }
    }
    
    private fun setupSocketListeners() {
        val socket = socketManager.getSocket() ?: return
        
        // Handle incoming offers
        socket.on("offer") { args ->
            if (args.isNotEmpty()) {
                val offerData = args[0] as JSONObject
                handleRemoteOffer(offerData)
            }
        }
        
        // Handle incoming answers
        socket.on("answer") { args ->
            if (args.isNotEmpty()) {
                val answerData = args[0] as JSONObject
                handleRemoteAnswer(answerData)
            }
        }
        
        // Handle incoming ICE candidates
        socket.on("candidate") { args ->
            if (args.isNotEmpty()) {
                val candidateData = args[0] as JSONObject
                handleRemoteIceCandidate(candidateData)
            }
        }
        
        // Handle hangup
        socket.on("hangup") { args ->
            if (args.isNotEmpty()) {
                val hangupData = args[0] as JSONObject
                handleHangup(hangupData)
            }
        }
    }
    
    fun createOffer() {
        val peerConnection = webRTCManager.getPeerConnection() ?: return
        
        peerConnection.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        // Send offer to remote peer
                        val offerData = JSONObject().apply {
                            put("type", sessionDescription.type.canonicalForm())
                            put("sdp", sessionDescription.description)
                            put("callId", "emergency-call-${System.currentTimeMillis()}")
                        }
                        currentRoom?.let { room ->
                            socketManager.sendOffer(offerData, room)
                        }
                    }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, sessionDescription)
            }
            
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String) {
                Log.e(TAG, "Failed to create offer: $error")
            }
            override fun onSetFailure(error: String) {
                Log.e(TAG, "Failed to set local description: $error")
            }
        }, MediaConstraints())
    }
    
    private fun handleRemoteOffer(offerData: JSONObject) {
        val peerConnection = webRTCManager.getPeerConnection() ?: return
        
        try {
            val type = SessionDescription.Type.fromCanonicalForm(offerData.getString("type"))
            val sdp = offerData.getString("sdp")
            val sessionDescription = SessionDescription(type, sdp)
            
            peerConnection.setRemoteDescription(object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onSetSuccess() {
                    // Create and send answer
                    createAnswer()
                }
                override fun onCreateFailure(p0: String?) {}
                override fun onSetFailure(p0: String?) {}
            }, sessionDescription)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling remote offer", e)
        }
    }
    
    private fun createAnswer() {
        val peerConnection = webRTCManager.getPeerConnection() ?: return
        
        peerConnection.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        // Send answer to remote peer
                        val answerData = JSONObject().apply {
                            put("type", sessionDescription.type.canonicalForm())
                            put("sdp", sessionDescription.description)
                            put("callId", "emergency-call-${System.currentTimeMillis()}")
                        }
                        currentRoom?.let { room ->
                            socketManager.sendAnswer(answerData, room)
                        }
                    }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, sessionDescription)
            }
            
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String) {
                Log.e(TAG, "Failed to create answer: $error")
            }
            override fun onSetFailure(error: String) {
                Log.e(TAG, "Failed to set local description: $error")
            }
        }, MediaConstraints())
    }
    
    private fun handleRemoteAnswer(answerData: JSONObject) {
        val peerConnection = webRTCManager.getPeerConnection() ?: return
        
        try {
            val type = SessionDescription.Type.fromCanonicalForm(answerData.getString("type"))
            val sdp = answerData.getString("sdp")
            val sessionDescription = SessionDescription(type, sdp)
            
            peerConnection.setRemoteDescription(object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onSetSuccess() {
                    Log.d(TAG, "Remote answer set successfully")
                }
                override fun onCreateFailure(p0: String?) {}
                override fun onSetFailure(p0: String?) {}
            }, sessionDescription)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling remote answer", e)
        }
    }
    
    private fun handleRemoteIceCandidate(candidateData: JSONObject) {
        val peerConnection = webRTCManager.getPeerConnection() ?: return
        
        try {
            val candidate = candidateData.getString("candidate")
            val sdpMid = candidateData.getString("sdpMid")
            val sdpMLineIndex = candidateData.getInt("sdpMLineIndex")
            
            val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, candidate)
            peerConnection.addIceCandidate(iceCandidate)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling remote ICE candidate", e)
        }
    }
    
    private fun handleHangup(hangupData: JSONObject) {
        Log.d(TAG, "Call ended by remote peer")
        endCall()
    }
    
    fun hangup() {
        val hangupData = JSONObject().apply {
            put("callId", "emergency-call-${System.currentTimeMillis()}")
        }
        currentRoom?.let { room ->
            socketManager.sendHangup(hangupData, room)
        }
        endCall()
    }
    
    private fun endCall() {
        webRTCManager.close()
        Log.d(TAG, "Call ended")
    }
    
    fun disconnect() {
        hangup()
        socketManager.disconnect()
    }
    
    fun isConnected(): Boolean {
        return socketManager.isConnected()
    }
}
