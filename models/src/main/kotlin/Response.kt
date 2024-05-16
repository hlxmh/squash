package models

import kotlinx.serialization.Serializable

@Serializable
enum class Response {
    ACCEPTED, DECLINED
}