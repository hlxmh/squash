package models

import kotlinx.serialization.Serializable

@Serializable
enum class PlayerStatus {
    IDLE, IN_LOBBY, IN_GAME
}