package models

import kotlinx.serialization.Serializable
import models.Puzzle

@Serializable
sealed class MessageData {
    companion object {
        const val FRIEND_REQUEST_TYPE = "friendRequest"
        const val FRIEND_REQUEST_RESPONSE_TYPE = "respondToFriendRequest"
        const val LOBBY_INVITE_TYPE = "lobbyInvite"
        const val LOBBY_INVITE_RESPONSE_TYPE = "respondToLobbyInvite"
        const val CREATE_LOBBY_TYPE = "createLobby"
        const val LEAVE_LOBBY_TYPE = "leaveLobby"
        const val NEXT_PUZZLE_TYPE = "nextPuzzle"
        const val ANSWER_PUZZLE_TYPE = "answerPuzzle"
        const val START_GAME_TYPE = "startGame"
        const val END_GAME_TYPE = "endGame"
        const val REVIEW_PUZZLE_TYPE = "reviewPuzzle"
        const val LOBBY_UPDATE_TYPE = "lobbyUpdate"
        const val ERROR_TYPE = "error"
        const val MATCH_TYPE = "matchType"
    }

    @Serializable
    data class FriendRequest(
        val from: String, val to: String
    ) : MessageData()

    @Serializable
    data class FriendRequestResponse(
        val from: String, val to: String, val response: Response
    ) : MessageData()

    @Serializable
    data class LobbyInvite(
        val from: String, val to: String, val lobbyId: Int
    ) : MessageData()

    @Serializable
    data class LobbyInviteResponse(
        val from: String, val to: String, val lobbyId: Int, val response: Response
    ) : MessageData()

    @Serializable
    data class LobbyUpdate(
        val lobby: Lobby?
    ) : MessageData()

    @Serializable
    data class NextPuzzle(
        val puzzle: Puzzle,
        val round: Int,
    ) : MessageData()

    @Serializable
    data class LobbyCreate(
        val lobbyId: Int
    ) : MessageData()

    @Serializable
    data class LobbyLeave(
        val lobbyId: Int
    ) : MessageData()

    @Serializable
    data class AnswerPuzzle(
        val round: Int,
        val answer: Int,
    ) : MessageData()

    @Serializable
    data class AnswerPuzzleScore(
        val score: Int,
    ) : MessageData()

    @Serializable
    data class ReviewPuzzle(
        val correctIdx: Int,
    ) : MessageData()

    // add other fields as needed
    @Serializable
    data class StartGame(
        val puzzle: Puzzle,
        val round: Int
    ) : MessageData()

    @Serializable
    data class EndGame(
        val round: Int
    ) : MessageData()

    @Serializable
    data class Error(
        val message: String
    ) : MessageData()
}