package com.example

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.*
import kotlinx.coroutines.*
import models.*
import kotlin.math.pow

class WebSocketClient(
    private val userId: String,
    private val model: UserModel
) {
    private val client = HttpClient {
        install(WebSockets)
    }
    private var session: DefaultClientWebSocketSession? = null
    private var job: Job? = null
    private var reconnectAttempts = 0
    var lobby: Int? = null

    fun connect() {
        job = GlobalScope.launch {
            while (true) {
                try {
                    establishConnection()
                    break
                } catch (e: Exception) {
                    println("$userId: WebSocket connection error: ${e.message}")
                    reconnectAttempts++
                    // Exponential backoff before the next reconnection attempt
                    delay(2.toDouble().pow(reconnectAttempts).toLong())
                }
            }
            println("$userId: Connection closed")
        }
    }

    private suspend fun establishConnection() {
        client.webSocket(
            method = io.ktor.http.HttpMethod.Get,
            host = "localhost",
            port = 8080,
            path = "/$userId"
        ) {
            session = this
            // Reset the reconnect attempts counter upon successful connection
            reconnectAttempts = 0
            println("$userId: Connection established")
            while (isActive) {
                val message = incoming.receive()
                val data = (message as? Frame.Text)?.readText()
                if (data != null) {
                    handleMessage(data)
                }
            }
            session = null
        }
    }

    suspend fun sendLobbyInvite(to: String) {
        lobby?.let {
            sendMessage(
                MessageData.LOBBY_INVITE_TYPE,
                MessageData.LobbyInvite(userId, to, it)
            )
        }
    }

    suspend fun sendMessage(messageType: String, data: MessageData?) {
        println("$userId: Sending message $messageType: $data")
        val messageObject = JsonObject(mapOf(
            "type" to JsonPrimitive(messageType),
            "data" to if (data != null) Json.encodeToJsonElement(data) else JsonNull
        ))
        val message = Json.encodeToString(JsonObject.serializer(), messageObject)
        session?.send(Frame.Text(message))
    }

    private fun handleMessage(message: String) {
        println("$userId: Received message from server: $message")
        val json = Json.parseToJsonElement(message).jsonObject
        val type = json["type"]?.jsonPrimitive?.content
        val data = Json.decodeFromJsonElement<MessageData>(json["data"]!!)

        when (type) {
            MessageData.FRIEND_REQUEST_TYPE -> {
                handleFriendRequest(data as MessageData.FriendRequest)
            }
            MessageData.FRIEND_REQUEST_RESPONSE_TYPE -> {
                handleFriendRequestResponse(data as MessageData.FriendRequestResponse)
            }
            MessageData.LOBBY_INVITE_TYPE -> {
                handleLobbyInvite(data as MessageData.LobbyInvite)
            }
            MessageData.LOBBY_INVITE_RESPONSE_TYPE -> {
                handleLobbyInviteResponse(data as MessageData.LobbyInviteResponse)
            }
            MessageData.CREATE_LOBBY_TYPE -> {
                handleCreateLobby(data as MessageData.LobbyCreate)
            }
            MessageData.ANSWER_PUZZLE_TYPE -> {
                handleAnswerPuzzle(data as MessageData.AnswerPuzzleScore)
            }
            MessageData.ERROR_TYPE -> {
                handleError(data as MessageData.Error)
            }
            MessageData.START_GAME_TYPE -> {
                handleStartGame(data as MessageData.StartGame)
            }
            MessageData.NEXT_PUZZLE_TYPE -> {
                handleNextPuzzle(data as MessageData.NextPuzzle)
            }
            MessageData.END_GAME_TYPE -> {
                handleEndGame(data as MessageData.EndGame)
            }
            MessageData.REVIEW_PUZZLE_TYPE -> {
                handleReviewPuzzle(data as MessageData.ReviewPuzzle)
            }
            MessageData.LOBBY_UPDATE_TYPE -> {
                handleLobbyUpdate(data as MessageData.LobbyUpdate)
            }
            else -> {
                println("$userId: Unknown message type: $type")
            }
        }
    }

    private fun handleFriendRequest(data: MessageData.FriendRequest) {
        println("Handling friend request from ${data.from} to ${data.to}")
        model.pendingFriendRequests.add(data)
        model.friendRequests += 1
    }

    private fun handleFriendRequestResponse(data: MessageData.FriendRequestResponse) {
        println("Handling friend request response from ${data.from} to ${data.to}")
    }

    private fun handleLobbyInvite(data: MessageData.LobbyInvite) {
        println("Handling lobby invite from ${data.from} to ${data.to} for lobby ${data.lobbyId}")
        model.pendingLobbyInvitations.add(data)
        model.lobbyInvites += 1
    }

    private fun handleLobbyInviteResponse(data: MessageData.LobbyInviteResponse) {
        println("Handling lobby invite response from ${data.from} to ${data.to} for lobby ${data.lobbyId}")
    }

    private fun handleCreateLobby(data: MessageData.LobbyCreate) {
        println("Handling create lobby message for lobby ${data.lobbyId}")
        lobby = data.lobbyId
    }

    private fun handleLobbyUpdate(data: MessageData.LobbyUpdate) {
        if (data.lobby != null) {
            model.lobbyStatus = LobbyStatus(data.lobby!!.lobbyId, data.lobby!!.users)
        } else {
            lobby = null
            model.lobbyStatus = LobbyStatus(null, List<String>(0) { "" })
        }
    }
    private fun handleAnswerPuzzle(data: MessageData.AnswerPuzzleScore) {
        model.gameStatus = GameStatus(
            model.gameStatus.playerStatus,
            model.gameStatus.round,
            model.gameStatus.puzzle,
            model.gameStatus.score + data.score,
            model.gameStatus.correctAnswer,
            model.gameStatus.playerAnswer,
        )
        println("Handling giving score: ${data.score}")
    }

    private fun handleStartGame(data: MessageData.StartGame) {
        model.gameStatus = GameStatus(
            PlayerStatus.IN_GAME,
            data.round,
            data.puzzle,
            model.gameStatus.score,
            model.gameStatus.correctAnswer,
            model.gameStatus.playerAnswer,
        )
        println("starting game")
    }

    private fun handleNextPuzzle(data: MessageData.NextPuzzle) {
        model.gameStatus = GameStatus(
            model.gameStatus.playerStatus,
            data.round,
            data.puzzle,
            model.gameStatus.score,
            -1,
            -1,
        )
        println("going to next question")
    }

    private fun handleReviewPuzzle(data: MessageData.ReviewPuzzle) {
        model.gameStatus = GameStatus(
            model.gameStatus.playerStatus,
            model.gameStatus.round,
            model.gameStatus.puzzle,
            model.gameStatus.score,
            data.correctIdx,
            model.gameStatus.playerAnswer,
        )
        println("reviewing answer with ${model.gameStatus}")
    }

    private fun handleEndGame(data: MessageData.EndGame) {
        model.gameStatus = GameStatus(
            model.gameStatus.playerStatus,
            data.round,
            model.gameStatus.puzzle,
            model.gameStatus.score,
            model.gameStatus.correctAnswer,
            model.gameStatus.playerAnswer,
        )
        println("ending game")
    }

    private fun handleError(data: MessageData.Error) {
        println("Handling error message: ${data.message}")
    }
    suspend fun close() {
        job?.cancelAndJoin()
        client.close()
    }
}

