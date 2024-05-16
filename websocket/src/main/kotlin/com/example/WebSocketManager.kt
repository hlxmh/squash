package com.example

import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import models.Lobby
import models.MessageData
import models.Response
import models.DatabaseManager
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlin.math.pow

class WebSocketManager {
    private val connections = ConcurrentHashMap<String, WebSocketSession>()
    private val lobbies = ConcurrentHashMap<Int, Lobby>()
    private var lobbyIdCounter = 0
    private val databaseManager = DatabaseManager()

    val MAX_ROUNDS = 5
    val ROUND_TIME = 15000L
    val REVIEW_TIME = 5000L
    val MAX_POINTS = 1000.0


    suspend fun sendMessage(userId: String, messageType: String, data: MessageData) {
        val messageObject = JsonObject(mapOf(
            "type" to JsonPrimitive(messageType),
            "data" to Json.encodeToJsonElement(data)
        ))
        val message = Json.encodeToString(JsonObject.serializer(), messageObject)
        connections[userId]?.send(Frame.Text(message))
    }

    suspend fun handleFriendRequest(userId: String, friendRequest: MessageData.FriendRequest) {
        sendMessage(friendRequest.to, MessageData.FRIEND_REQUEST_TYPE, friendRequest)
        println("$userId sent friend request to ${friendRequest.to}")
        // TODO: If user not connected, cache the friend request and resend when the user connects
    }

    suspend fun handleFriendRequestResponse(userId: String, friendRequestResponse: MessageData.FriendRequestResponse) {
        sendMessage(friendRequestResponse.from, MessageData.FRIEND_REQUEST_RESPONSE_TYPE, friendRequestResponse)
        when (friendRequestResponse.response) {
            Response.ACCEPTED -> {
                println("$userId accepted friend request from ${friendRequestResponse.from}")
            }
            Response.DECLINED -> {
                println("$userId declined friend request from ${friendRequestResponse.from}")
            }
        }
    }

    private suspend fun handleCreateLobby(userId: String) {
        val existingLobby = lobbies.values.find { it.users.contains(userId) }
        if (existingLobby != null) {
            // If the user is already in a lobby, reject creating a new lobby
            val error = "You are already in lobby ${existingLobby.lobbyId}"
            sendMessage(userId, MessageData.ERROR_TYPE, MessageData.Error(error))
            return
        }

        val lobbyId = lobbyIdCounter++

        val lobby = Lobby(lobbyId = lobbyId, puzzles = databaseManager.getPuzzles().shuffled().distinctBy { it.id }.take(MAX_ROUNDS))
        lobbies[lobbyId] = lobby
        sendMessage(userId, MessageData.CREATE_LOBBY_TYPE, MessageData.LobbyCreate(lobbyId))
        println("$userId created new lobby $lobbyId")

        addUserToLobby(lobby, userId) // Add host to the lobby
    }
    suspend fun handleMatch(userId: String) {
        val existingLobby = lobbies.values.find { it.users.contains(userId) }
        if (existingLobby != null) {
            // If the user is already in a lobby, reject matching
            val error = "You are already in lobby ${existingLobby.lobbyId}"
            sendMessage(userId, MessageData.ERROR_TYPE, MessageData.Error(error))
            return
        }
        val waitingLobby = lobbies.values.find { !it.inGame }
        if (waitingLobby != null) {
            addUserToLobby(waitingLobby, userId) // Add host to the lobby
        } else {
            handleCreateLobby(userId)
        }
    }

    suspend fun handleLobbyInvite(userId: String, lobbyInvite: MessageData.LobbyInvite) {
        // Check if the invited user is already in a lobby
        val existingLobby = lobbies.values.find { it.users.contains(lobbyInvite.to) }
        if (existingLobby != null) {
            val error = "User ${lobbyInvite.to} is already in lobby ${existingLobby.lobbyId}"
            sendMessage(userId, MessageData.ERROR_TYPE, MessageData.Error(error))
            return
        }

        sendMessage(lobbyInvite.to, MessageData.LOBBY_INVITE_TYPE, lobbyInvite)
        println("$userId sent invitation to ${lobbyInvite.to} to join lobby ${lobbyInvite.lobbyId}")
    }

    suspend fun handleLobbyInviteResponse(userId: String, lobbyInviteResponse: MessageData.LobbyInviteResponse) {
        val lobby = lobbies.getOrDefault(lobbyInviteResponse.lobbyId, null)
        if (lobby != null) {
            when (lobbyInviteResponse.response) {
                Response.ACCEPTED -> {
                    if (addUserToLobby(lobby, userId)) {
                        println(
                            "$userId accepted invitation from ${lobbyInviteResponse.from} to join lobby " +
                                    "${lobbyInviteResponse.lobbyId}"
                        )
                    } else {
                        println(
                            "$userId accepted invitation from ${lobbyInviteResponse.from} to join lobby " +
                                    "${lobbyInviteResponse.lobbyId} but it was full"
                        )
                    }
                }
                Response.DECLINED -> {
                    println(
                        "$userId declined invitation from ${lobbyInviteResponse.from} to join lobby " +
                                "${lobbyInviteResponse.lobbyId}"
                    )
                }
            }
            sendMessage(lobbyInviteResponse.from, MessageData.LOBBY_INVITE_RESPONSE_TYPE, lobbyInviteResponse)
        }
    }

    suspend fun handleLeaveLobby(userId: String) {
        val lobby = lobbies.values.find { it.users.contains(userId) }
        if (lobby != null) {
            removeUserFromLobby(lobby, userId)
            println("$userId left lobby ${lobby.lobbyId}")
        } else {
            println("$userId is not in any lobby.")
        }
    }

    suspend fun handleAnswerPuzzle(userId: String, answerData: MessageData.AnswerPuzzle) {
        println("handling user answer")
        val lobby = lobbies.values.find { it.users.contains(userId) }
        if (lobby != null) {
            if (answerData.answer == lobby.currentCorrectIdx && answerData.round == lobby.round) { // right answer, right round
                println("right answer, right round")
                val currentInstant: java.time.Instant = java.time.Instant.now()
                val timeTaken =  currentInstant.toEpochMilli() - lobby.timer
                if (timeTaken < ROUND_TIME) {
                    val remainingTime = ROUND_TIME - timeTaken
                    println("remaining time: $remainingTime")
                    // so any answer within first 5s counts towards full score
                    val timeFactor = min(10000.0, remainingTime.toDouble()) / 10000.0
                    println("time factor: $timeFactor")
                    val score =  MAX_POINTS.pow(timeFactor).toInt()
                    println("score: $score")
                    sendMessage(userId, MessageData.ANSWER_PUZZLE_TYPE, MessageData.AnswerPuzzleScore(score))
                } else {
                    println("$userId is de-synced in ${lobby.lobbyId}")
                }
            } else if (answerData.round == lobby.round) { // wrong answer, right round
                println("wrong answer, right round")
                sendMessage(userId, MessageData.ANSWER_PUZZLE_TYPE, MessageData.AnswerPuzzleScore(0))
            } else { // wrong answer, wrong round
                println("wrong answer, wrong round")
                println("$userId is de-synced in ${lobby.lobbyId}")
            }
        } else {
            println("$userId is not in any lobby.")
        }
    }

    suspend fun handleWebSocketMessage(userId: String, message: String) {
        val json = Json.parseToJsonElement(message).jsonObject
        val type = json["type"]?.jsonPrimitive?.content
        val data = if (json["data"] != JsonNull) Json.decodeFromJsonElement<MessageData>(json["data"]!!) else null
        println("Message received with type $type")
        when (type) {
            MessageData.FRIEND_REQUEST_TYPE -> {
                handleFriendRequest(userId, data as MessageData.FriendRequest)
            }
            MessageData.FRIEND_REQUEST_RESPONSE_TYPE -> {
                handleFriendRequestResponse(userId, data as MessageData.FriendRequestResponse)
            }
            MessageData.MATCH_TYPE -> {
                GlobalScope.launch {
                    handleMatch(userId)
                }
            }
            MessageData.LOBBY_INVITE_TYPE -> {
                handleLobbyInvite(userId, data as MessageData.LobbyInvite)
            }
            MessageData.LOBBY_INVITE_RESPONSE_TYPE -> {
                handleLobbyInviteResponse(userId, data as MessageData.LobbyInviteResponse)
            }
            MessageData.LEAVE_LOBBY_TYPE -> {
                handleLeaveLobby(userId)
            }
            MessageData.ANSWER_PUZZLE_TYPE -> {
                handleAnswerPuzzle(userId, data as MessageData.AnswerPuzzle)
            }
            else -> {
                println("Unknown message type: $type")
            }
        }
    }

    fun addConnection(userId: String, session: WebSocketSession) {
        connections[userId] = session
    }

    suspend fun removeConnection(userId: String) {
        val lobby = lobbies.values.find { it.users.contains(userId) }
        if (lobby != null) {
            removeUserFromLobby(lobby, userId)
        }
        connections.remove(userId)
    }

    fun hasConnection(userId: String): Boolean {
        return connections.containsKey(userId)
    }

    fun getConnection(userId: String): WebSocketSession? {
        return connections[userId]
    }

    private suspend fun addUserToLobby(lobby: Lobby, userId: String) : Boolean {
        if (lobby.users.size < 4) {
            println("adding $userId to lobby")
            lobby.addUser(userId)

            lobby.users.forEach {
                sendMessage(it, MessageData.LOBBY_UPDATE_TYPE, MessageData.LobbyUpdate(lobby))
            }

            // game automatically starts once enough users join
            if (lobby.users.size == 4) {
                println("starting game...")
                lobby.inGame = true
                startTimer(lobby.lobbyId)
            }
            return true
        }
        return false
    }

    private suspend fun removeUserFromLobby(lobby: Lobby, userId: String) {
        lobby.removeUser(userId)
        if (lobby.users.isEmpty()) {
            lobbies.remove(lobby.lobbyId)
        }
        lobby.users.forEach {
            if (it == userId) {
                sendMessage(it, MessageData.LOBBY_UPDATE_TYPE, MessageData.LobbyUpdate(null))
            } else {
                sendMessage(it, MessageData.LOBBY_UPDATE_TYPE, MessageData.LobbyUpdate(lobby))
            }
        }
    }
    private suspend fun startTimer(lobbyId: Int) {
        println("start timer for $lobbyId")
        val lobby = lobbies.values.find { it.lobbyId == lobbyId }

        if (lobby != null) {
            if (lobby.round < MAX_ROUNDS) {
                lobby.goToNextRound()
                lobby.startTimer()
            } else {
                lobby.round++
            }

            if (lobby.round == 1) {
                lobby.users.forEach() {
                    coroutineScope {
                        launch {
                            sendMessage(
                                it,
                                MessageData.START_GAME_TYPE,
                                MessageData.StartGame(lobby.currentPuzzle, lobby.round)
                            )
                            println("$it is starting a game in lobby ${lobby.lobbyId}")
                        }
                    }
                }
            } else if (lobby.round <= MAX_ROUNDS) {
                lobby.users.forEach() {
                    coroutineScope {
                        launch {
                            sendMessage(
                                it,
                                MessageData.NEXT_PUZZLE_TYPE,
                                MessageData.NextPuzzle(lobby.currentPuzzle, lobby.round)
                            )
                            println("$it is moving on to round ${lobby.round} in lobby ${lobby.lobbyId}")
                        }
                    }
                }
            } else {
                lobby.users.forEach() {
                    coroutineScope {
                        launch {
                            sendMessage(it, MessageData.END_GAME_TYPE, MessageData.EndGame(lobby.round))
                        }
                    }
                }
            }

            if (lobby.round <= MAX_ROUNDS) {
                delay(ROUND_TIME)
                lobby.users.forEach() {
                    coroutineScope {
                        launch {
                            sendMessage(it, MessageData.REVIEW_PUZZLE_TYPE, MessageData.ReviewPuzzle(lobby.currentCorrectIdx))
                            println("$it is reviewing round ${lobby.round} in ${lobby.lobbyId}")
                        }
                    }
                }
                delay(REVIEW_TIME)
                startTimer(lobbyId)
            }
        } else {
            println("This lobby does not exist.")
        }
    }
}
