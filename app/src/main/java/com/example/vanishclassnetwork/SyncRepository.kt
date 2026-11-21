
package com.example.vanishclassnetwork

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class SyncRepository(
    private val onMessageReceived: (ChatMessage) -> Unit,
    private val onMessageExpired: (String) -> Unit,
    private val onStatusChanged: (String) -> Unit
) {
    companion object {
        private const val TAG = "VanishNetwork"
        private const val RELAY_URL =
            "wss://vanish-relay-sawaira.bonto.page/ws"
        private const val MESSAGE_LIFETIME = 30000L
    }

    private val handler = Handler(Looper.getMainLooper())

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var studentName = ""
    private var deviceId = ""
    private var serverOffset = 0L
    private var lastPongTime = 0L
    private var manuallyDisconnected = false
    private var reconnectAttempt = 0
    private var isConnected = false

    private val seenIds = LinkedHashSet<String>()

    private data class QueuedMessage(
        val id: String,
        val text: String,
        val createdAt: Long,
        val expiresAt: Long
    )

    private val messageQueue = ArrayDeque<QueuedMessage>()

    private val heartbeatRunnable = object : Runnable {
        override fun run() {
            val socket = webSocket ?: return

            if (System.currentTimeMillis() - lastPongTime > 30000) {
                Log.e(TAG, "Heartbeat timeout")
                socket.cancel()
                return
            }

            socket.send(
                JSONObject().put("type", "ping").toString()
            )

            handler.postDelayed(this, 15000)
        }
    }

    fun connect(name: String, id: String) {
        studentName = name
        deviceId = id
        manuallyDisconnected = false
        reconnectAttempt = 0
        connectToRelay()
    }

    private fun connectToRelay() {
        if (manuallyDisconnected) return

        isConnected = false
        handler.removeCallbacks(heartbeatRunnable)

        Log.i(TAG, "Connecting to relay")
        onStatusChanged("Connecting...")

        val request = Request.Builder()
            .url(RELAY_URL)
            .build()

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {

                override fun onOpen(
                    webSocket: WebSocket,
                    response: Response
                ) {
                    Log.i(TAG, "WebSocket opened")

                    val join = JSONObject()
                        .put("type", "join")
                        .put("protocol", 1)
                        .put("device_id", deviceId)
                        .put("name", studentName)

                    webSocket.send(join.toString())
                }

                override fun onMessage(
                    webSocket: WebSocket,
                    text: String
                ) {
                    Log.i(TAG, "Received: $text")

                    try {
                        val json = JSONObject(text)

                        when (json.optString("type")) {

                            "welcome" -> {
                                serverOffset =
                                    json.getLong("server_time") -
                                            System.currentTimeMillis()

                                lastPongTime =
                                    System.currentTimeMillis()

                                isConnected = true
                                reconnectAttempt = 0

                                handler.post {
                                    onStatusChanged("Connected")
                                    sendQueuedMessages()
                                }

                                handler.removeCallbacks(heartbeatRunnable)
                                handler.postDelayed(
                                    heartbeatRunnable,
                                    15000
                                )
                            }

                            "message" -> {
                                val id = json.getString("id")

                                if (seenIds.add(id)) {
                                    if (seenIds.size > 1000) {
                                        val iterator = seenIds.iterator()
                                        if (iterator.hasNext()) {
                                            iterator.next()
                                            iterator.remove()
                                        }
                                    }

                                    val expiry =
                                        json.getLong("expires_at") -
                                                serverOffset

                                    if (expiry > System.currentTimeMillis()) {
                                        val message = ChatMessage(
                                            id = id,
                                            sender = json.getString("sender"),
                                            text = json.getString("text"),
                                            expiresAt = expiry
                                        )

                                        handler.post {
                                            onMessageReceived(message)
                                        }
                                    }
                                }
                            }

                            "expired" -> {
                                val id = json.getString("id")

                                handler.post {
                                    onMessageExpired(id)
                                }
                            }

                            "pong" -> {
                                lastPongTime =
                                    System.currentTimeMillis()
                            }

                            "error" -> {
                                val error = json.optString(
                                    "message",
                                    "Unknown relay error"
                                )

                                Log.e(TAG, "Relay error: $error")

                                handler.post {
                                    onStatusChanged("Relay error: $error")
                                }
                            }
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Message parsing failed", e)
                    }
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?
                ) {
                    isConnected = false
                    handler.removeCallbacks(heartbeatRunnable)

                    val reason = buildString {
                        append(t.javaClass.simpleName)
                        append(": ")
                        append(t.message ?: "Unknown connection error")

                        if (response != null) {
                            append(" (HTTP ${response.code})")
                        }
                    }

                    Log.e(TAG, "Connection failed: $reason", t)

                    handler.post {
                        onStatusChanged("Reconnecting...")
                    }

                    scheduleReconnect()
                }

                override fun onClosed(
                    webSocket: WebSocket,
                    code: Int,
                    reason: String
                ) {
                    isConnected = false
                    handler.removeCallbacks(heartbeatRunnable)

                    Log.w(TAG, "WebSocket closed: $code $reason")

                    handler.post {
                        onStatusChanged("Disconnected: $reason")
                    }

                    scheduleReconnect()
                }
            }
        )
    }

    fun sendMessage(text: String) {
        val cleanText = text.trim()

        if (cleanText.isBlank()) return

        if (cleanText.length > 500) {
            handler.post {
                onStatusChanged("Message must be 500 characters or less")
            }
            return
        }

        val now = System.currentTimeMillis()

        val queuedMessage = QueuedMessage(
            id = UUID.randomUUID().toString(),
            text = cleanText,
            createdAt = now,
            expiresAt = now + MESSAGE_LIFETIME
        )

        if (isConnected && sendToRelay(queuedMessage)) {
            Log.i(TAG, "Message sent directly")
        } else {
            messageQueue.addLast(queuedMessage)
            Log.i(TAG, "Message added to offline queue")
            handler.post {
                onStatusChanged("Message queued - waiting for connection")
            }
        }
    }

    private fun sendToRelay(message: QueuedMessage): Boolean {
        if (!isConnected) return false

        if (System.currentTimeMillis() >= message.expiresAt) {
            Log.i(TAG, "Expired queued message dropped: ${message.id}")
            return true
        }

        val json = JSONObject()
            .put("type", "message")
            .put("id", message.id)
            .put("sender", studentName)
            .put("text", message.text)

        return webSocket?.send(json.toString()) ?: false
    }

    private fun sendQueuedMessages() {
        if (!isConnected) return

        val currentTime = System.currentTimeMillis()

        while (messageQueue.isNotEmpty()) {
            val message = messageQueue.first()

            if (currentTime >= message.expiresAt) {
                messageQueue.removeFirst()
                Log.i(TAG, "Expired queued message removed")
                continue
            }

            if (sendToRelay(message)) {
                messageQueue.removeFirst()
                Log.i(TAG, "Queued message sent: ${message.id}")
            } else {
                break
            }
        }
    }

    private fun scheduleReconnect() {
        if (manuallyDisconnected) return

        isConnected = false
        handler.removeCallbacks(heartbeatRunnable)

        val delays = listOf(1000L, 2000L, 4000L, 8000L)
        val delay = delays[reconnectAttempt.coerceAtMost(3)]

        reconnectAttempt++

        Log.i(TAG, "Reconnecting in ${delay}ms")

        handler.postDelayed({
            if (!manuallyDisconnected) {
                connectToRelay()
            }
        }, delay)
    }

    fun disconnect() {
        manuallyDisconnected = true
        isConnected = false

        handler.removeCallbacksAndMessages(null)

        webSocket?.close(1000, "App disconnected")
        webSocket = null

        Log.i(TAG, "Manually disconnected")
        onStatusChanged("Disconnected")
    }
}
