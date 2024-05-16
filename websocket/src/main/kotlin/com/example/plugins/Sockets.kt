package com.example.plugins

import com.example.WebSocketManager
import io.ktor.server.application.*
import io.ktor.websocket.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import java.time.Duration

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        val socketManager = WebSocketManager()
        webSocket("/{userId}") {
            val userId = call.parameters["userId"] ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "User ID not provided"))
            if (socketManager.hasConnection(userId)) {
                return@webSocket close(CloseReason(CloseReason.Codes.NORMAL, "User already has an active WebSocket connection"))
            }
            socketManager.addConnection(userId, this)

            // TODO: Pull all cached friend requests and send them to user

            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        socketManager.handleWebSocketMessage(userId, frame.readText())
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                println("Connection closed: $e")
            } finally {
                socketManager.removeConnection(userId)
            }
        }
    }
}
