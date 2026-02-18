package com.example.emergencycommunicationsystem.webrtc

import android.content.Context
import android.util.Log
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.socket.SocketManager
import org.json.JSONObject
import org.webrtc.*

class SignalingManager(private val context: Context) {
    
    private val webRTCManager = WebRTCManager(context)
    private val socketManager = SocketManager()
    private var currentCallId: String? = null
    private var currentUserId: String? = null
    private var currentUserName: String? = null
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var currentLocationLabel: String? = null
    
    companion object {
        private const val TAG = "SignalingManager"
    }
    
    fun connect(
        callId: String? = null,
        userId: String? = null,
        userName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        locationLabel: String? = null
    ) {
        if (!callId.isNullOrBlank()) {
            currentCallId = callId
        }
        currentUserId = userId
        currentUserName = userName
        currentLatitude = latitude
        currentLongitude = longitude
        currentLocationLabel = locationLabel

        socketManager.connect(
            onConnected = {
                currentCallId = currentCallId ?: "call_${System.currentTimeMillis()}"

                setupSocketListeners()

                webRTCManager.createPeerConnection(
                    onIceCandidate = { iceCandidate ->
                        val callId = currentCallId ?: return@createPeerConnection
                        val payload = JSONObject().apply {
                            put(
                                "candidate",
                                JSONObject().apply {
                                    put("sdpMid", iceCandidate.sdpMid)
                                    put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
                                    put("candidate", iceCandidate.sdp)
                                }
                            )
                            put("callId", callId)
                        }
                        socketManager.sendCandidate(payload)
                    }
                )

                webRTCManager.addLocalAudioTrack()

                // This is what makes it “pop” in admin: emit offer as soon as we connect.
                createOffer()
            },
            onConnectError = { err ->
                Log.e(TAG, "Socket connect error: $err")
            },
            onDisconnected = { reason ->
                Log.w(TAG, "Socket disconnected: $reason")
            }
        )
    }
    
    private fun setupSocketListeners() {
        socketManager.onOffer { args ->
            val payload = args.firstOrNull() as? JSONObject ?: return@onOffer
            handleRemoteOffer(payload)
        }

        socketManager.onAnswer { args ->
            val payload = args.firstOrNull() as? JSONObject ?: return@onAnswer
            handleRemoteAnswer(payload)
        }

        socketManager.onCandidate { args ->
            val payload = args.firstOrNull() as? JSONObject ?: return@onCandidate
            handleRemoteIceCandidate(payload)
        }

        socketManager.onHangup { args ->
            val payload = args.firstOrNull() as? JSONObject ?: return@onHangup
            handleHangup(payload)
        }
    }
    
    fun createOffer() {
        val peerConnection = webRTCManager.getPeerConnection() ?: return
        val callId = currentCallId ?: "call_${System.currentTimeMillis()}".also { currentCallId = it }
        
        peerConnection.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        val resolvedUserId = currentUserId ?: AuthManager.getUserId().takeIf { it > 0 }?.toString()
                        val resolvedUserName = currentUserName ?: AuthManager.getUsername() ?: "User"
                        val resolvedPhone = AuthManager.getPhone()
                        val hasCoordinates = currentLatitude != null && currentLongitude != null
                        val resolvedLocationText = currentLocationLabel?.takeIf { it.isNotBlank() }
                            ?: if (hasCoordinates) "${currentLatitude},${currentLongitude}" else "Unknown location"

                        val payload = JSONObject().apply {
                            put(
                                "sdp",
                                JSONObject().apply {
                                    put("type", sessionDescription.type.canonicalForm())
                                    put("sdp", sessionDescription.description)
                                }
                            )
                            put("callId", callId)
                            put("conversationId", callId)
                            put("caller", resolvedUserName)
                            put("location", resolvedLocationText)

                            resolvedUserId?.let { put("userId", it) }
                            put("userName", resolvedUserName)

                            put("callerInfo", JSONObject().apply {
                                resolvedUserId?.let { put("id", it) }
                                put("name", resolvedUserName)
                                resolvedPhone?.let { put("phone", it) }
                                put("source", "android")
                            })

                            put("locationData", JSONObject().apply {
                                put("source", "android")
                                put("hasCoordinates", hasCoordinates)
                                currentLatitude?.let { put("latitude", it) }
                                currentLongitude?.let { put("longitude", it) }
                                currentLocationLabel?.let { put("label", it) }
                            })
                        }

                        socketManager.sendOffer(payload)
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
            val callId = offerData.optString("callId", "")
            if (callId.isNotBlank()) currentCallId = callId

            val sdpObj = offerData.optJSONObject("sdp")
            val typeStr = sdpObj?.optString("type", "") ?: ""
            val sdp = sdpObj?.optString("sdp", "") ?: ""
            if (typeStr.isBlank() || sdp.isBlank()) return

            val type = SessionDescription.Type.fromCanonicalForm(typeStr)
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
        val callId = currentCallId ?: return
        
        peerConnection.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        val payload = JSONObject().apply {
                            put(
                                "sdp",
                                JSONObject().apply {
                                    put("type", sessionDescription.type.canonicalForm())
                                    put("sdp", sessionDescription.description)
                                }
                            )
                            put("callId", callId)
                        }

                        socketManager.sendAnswer(payload)
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
            val callId = answerData.optString("callId", "")
            if (callId.isNotBlank()) currentCallId = callId

            val sdpObj = answerData.optJSONObject("sdp")
            val typeStr = sdpObj?.optString("type", "") ?: ""
            val sdp = sdpObj?.optString("sdp", "") ?: ""
            if (typeStr.isBlank() || sdp.isBlank()) return

            val type = SessionDescription.Type.fromCanonicalForm(typeStr)
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
            val callId = candidateData.optString("callId", "")
            if (callId.isNotBlank()) currentCallId = callId

            val candObj = candidateData.optJSONObject("candidate")
            val sdpMid = candObj?.optString("sdpMid", "") ?: ""
            val sdpMLineIndex = candObj?.optInt("sdpMLineIndex", -1) ?: -1
            val candidate = candObj?.optString("candidate", "") ?: ""
            if (sdpMid.isBlank() || sdpMLineIndex < 0 || candidate.isBlank()) return
            
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
        val callId = currentCallId ?: "call_${System.currentTimeMillis()}"
        val hangupData = JSONObject().apply {
            put("callId", callId)
        }
        socketManager.sendHangup(hangupData)
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
