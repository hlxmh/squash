package models

import kotlinx.serialization.Serializable
import java.security.MessageDigest

// Data class representing a problem
@Serializable
data class Problem(
    val id: Int,                 // Unique identifier for the problem
    val title: String,           // Title of the problem
    val description: String,     // Description of the problem
    var code: String,            // Initial code provided with bugs
    var solution: String,        // Solution code to test answer
    val tags: List<String>       // Tags to filter problems
)

// TODO: replace tags with this class (made)
// Data class for holding the tags
@Serializable
data class Tags(
    val difficulty: String,      // Difficulty of the problem
    val language: String,        // Coding language of the problem
    val topic: String,           // The type of the problem
)

// Data class representing a test case
@Serializable
data class TestCase(
    val id: Int,                 // Unique identifier for the test case
    val problemId: Int,          // Foreign key referencing the associated problem
    val input: String,           // Input for the test case
    val userOutput: String,         // Output generated by the user's code
    val expectedOutput: String    // Expected output for the test case
)

// Data class representing a user submission
@Serializable
data class UserSubmission(
    val id: Int,                 // Unique identifier for the user submission
    val problemId: Int,          // Foreign key referencing the associated problem
    val username: String,        // User who submitted the code
    val submittedCode: String    // Code submitted by the user
)

// Data class representing a user
@Serializable
data class User(
    val userName: String,        // Unique identifier for the user
    val displayName: String,     // User's display name
    val hashedPassword: String,  // Hashed user's password
    val email: String,           // User's email
    // TODO: (made) Add additional information about the user (date of registration or other metrics)
) {
    companion object {
        // Function to hash a password using SHA-256
        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}

@Serializable
data class Puzzle(
    val id: Int,                    // Unique identifier for the problem
    val description: String,        // Description of the problem
    val choices: List<String>       // Possible choices for the problem, the correct answer is always the first element, i.e. element 0
) {
    init {
        require(choices.size == 4) { "A puzzle must have exactly four choices" }
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Puzzle

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

// Data class representing collected user metrics
@Serializable
data class UserMetrics(
    val userName: String,
    val totalCompleted: Int = 0,
    val easyCompleted: Int = 0,
    val mediumCompleted: Int = 0,
    val hardCompleted: Int = 0,
    val lifetimeHints: Int = 0,
    val submittedProblems: Int = 0,
    val topPuzzleScore: Int = 0,
)

// Data class representing friendships
@Serializable
data class Friendship(
    val userName: String,   // Username of the first user in the friendship
    val friendName: String  // Username of the second user in the friendship
)

// Data class representing game status
@Serializable
data class GameStatus(
    val playerStatus: PlayerStatus,  // Status of player
    val round: Int,                  // Current round of the game
    val puzzle: Puzzle?,             // Current puzzle of the game
    val score: Int,                  // Current total score for the player
    val correctAnswer: Int,          // Correct answer for the current puzzle (-1 if not in answer reveal period)
    val playerAnswer: Int,           // Player's answer for the current puzzle (-1 if no answer yet)
)

// Data class representing game status
@Serializable
data class LobbyStatus(
    val id: Int?,                    // Current lobby id, null if not in lobby
    val users: List<String>,         // Current list of users in the lobby
)