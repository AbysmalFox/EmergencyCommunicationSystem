package com.example.emergencycommunicationsystem.webrtc

import android.content.Context
import android.util.Log
import org.webrtc.*
import io.socket.client.Socket

class WebRTCManager(private val context: Context) {
    
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var socket: Socket? = null
    
    companion object {
        private const val TAG = "WebRTCManager"
    }
    
    init {
        initializePeerConnectionFactory()
    }
    
    private fun initializePeerConnectionFactory() {
        try {
            val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
            
            PeerConnectionFactory.initialize(options)
            
            val eglBase = EglBase.create()
            
            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
                .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
                .createPeerConnectionFactory()
                
            Log.d(TAG, "PeerConnectionFactory initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize PeerConnectionFactory", e)
        }
    }
    
    fun createPeerConnection(socket: Socket): PeerConnection? {
        this.socket = socket
        
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                .createIceServer()
        )
        
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        // Unified Plan is default in modern WebRTC
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
                Log.d(TAG, "Signaling state changed: $signalingState")
            }
            
            override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                Log.d(TAG, "ICE connection state changed: $iceConnectionState")
            }
            
            override fun onIceConnectionReceivingChange(receiving: Boolean) {
                Log.d(TAG, "ICE connection receiving change: $receiving")
            }
            
            override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
                Log.d(TAG, "ICE gathering state changed: $iceGatheringState")
            }
            
            override fun onIceCandidate(iceCandidate: IceCandidate) {
                Log.d(TAG, "ICE candidate generated: ${iceCandidate.sdp}")
                
                val candidateData = org.json.JSONObject().apply {
                    put("candidate", iceCandidate.sdp)
                    put("sdpMid", iceCandidate.sdpMid)
                    put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
                }
                socket.emit("candidate", candidateData)
            }
            
            override fun onIceCandidatesRemoved(iceCandidates: Array<out IceCandidate>) {
                Log.d(TAG, "ICE candidates removed: ${iceCandidates.size}")
            }
            
            override fun onAddStream(mediaStream: MediaStream) {
                Log.d(TAG, "Media stream added: ${mediaStream.id}")
            }
            
            override fun onRemoveStream(mediaStream: MediaStream) {
                Log.d(TAG, "Media stream removed: ${mediaStream.id}")
            }
            
            override fun onDataChannel(dataChannel: DataChannel) {
                Log.d(TAG, "Data channel created: ${dataChannel.label()}")
            }
            
            override fun onRenegotiationNeeded() {
                Log.d(TAG, "Renegotiation needed")
            }
            
            override fun onAddTrack(rtpReceiver: RtpReceiver, mediaStreams: Array<out MediaStream>) {
                Log.d(TAG, "Track added: ${rtpReceiver.track()?.id()}")
            }

            // In some versions of WebRTC SDK, onTrack might have a different signature or not exist in the interface
            // If it causes error, we can use onAddTrack instead
        })
        
        Log.d(TAG, "PeerConnection created successfully")
        return peerConnection
    }
    
    fun getPeerConnection(): PeerConnection? = peerConnection
    
    fun addLocalAudioTrack(): Boolean {
        return try {
            val factory = peerConnectionFactory ?: return false
            val pc = peerConnection ?: return false
            
            val audioConstraints = MediaConstraints()
            val audioSource = factory.createAudioSource(audioConstraints)
            val audioTrack = factory.createAudioTrack("audio", audioSource)
            audioTrack.setEnabled(true)
            
            val mediaStream = factory.createLocalMediaStream("stream")
            mediaStream.addTrack(audioTrack)
            pc.addStream(mediaStream)
            
            Log.d(TAG, "Local audio track added successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add local audio track", e)
            false
        }
    }
    
    fun close() {
        peerConnection?.close()
        peerConnection = null
        peerConnectionFactory = null
        Log.d(TAG, "WebRTC resources cleaned up")
    }
}
