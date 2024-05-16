package models

import kotlinx.serialization.Serializable
import models.Puzzle

@Serializable
data class Lobby(val lobbyId: Int, val users: MutableList<String> = mutableListOf(), val puzzles : List<Puzzle>) {
    var timer : Long = 0
    var round = 0
    var currentCorrectIdx = 0
    var currentPuzzle = puzzles[0]
    var inGame = false


    fun addUser(userId: String) {
        users.add(userId)
    }

    fun removeUser(userId: String) {
        users.remove(userId)
    }
    fun startTimer() {
        val currentInstant: java.time.Instant = java.time.Instant.now()
        timer = currentInstant.toEpochMilli()
    }

    fun goToNextRound() {
        // do these operations before updating round bc the list is 0-indexed but we want first round to be 1
        val (randomizedChoices, correctAnswerIndex) = randomizeChoices(puzzles[round])

        currentPuzzle = Puzzle(currentPuzzle.id, puzzles[round].description, randomizedChoices)
        currentCorrectIdx = correctAnswerIndex
        round++
    }

    private fun randomizeChoices(puzzle: Puzzle): Pair<List<String>, Int> {
        val indices = (0 until puzzle.choices.size).toList().shuffled()
        val randomizedChoices = indices.map { puzzle.choices[it] }
        val correctAnswerIndex = indices.indexOf(0)
        return Pair(randomizedChoices, correctAnswerIndex)
    }
}