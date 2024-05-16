package controller

import com.example.WebSocketClient
import models.*
import models.MessageData
import userinterface.ViewEvent
import kotlinx.coroutines.*

class UserController(val model: UserModel) {
    var client: WebSocketClient? = null

    // we can cast `Any` later since each event has an associated type
    fun invoke(event: ViewEvent, value: Any?) {
        when(event) {
            ViewEvent.SelectProblemEvent -> { model.selectedProblem = value as Problem? }
            ViewEvent.SelectTestCases -> { model.testCases = value as List<TestCase>}
            ViewEvent.ResetGameEvent -> {
                GlobalScope.launch {
                    client!!.sendMessage(
                        MessageData.LEAVE_LOBBY_TYPE,
                        null
                    )
                    println("MESSAGE SENT")
                }
                model.gameStatus = GameStatus(PlayerStatus.IDLE, 0, null, 0, -1, -1)
                model.lobbyStatus = LobbyStatus(null,  List<String>(0) { "" })
            }
            ViewEvent.MatchGameEvent -> {
                GlobalScope.launch {
                    client!!.sendMessage(
                        MessageData.MATCH_TYPE,
                        null
                    )
                    println("MESSAGE SENT")
                }
            }
            ViewEvent.CodeUpdateEvent -> model.selectedProblem?.code = value as String
            ViewEvent.UserLoginEvent -> {
                model.currentUser = value as User?
                if (client != null) {
                    GlobalScope.launch {
                        client?.close()
                    }
                }
                val connect_name = value?.userName ?: ""
                client = WebSocketClient(connect_name, model)
                client?.connect()
            }
            ViewEvent.UserLogoutEvent -> {
                model.currentUser = value as User?
                GlobalScope.launch {
                    client?.close()
                }
                client = null
            }
            ViewEvent.FriendRequestEvent -> {
                GlobalScope.launch {
                    client?.sendMessage(
                        MessageData.FRIEND_REQUEST_TYPE,
                        value as MessageData.FriendRequest
                    )
                }
            }
            ViewEvent.FriendRequestResponseEvent -> {
                val request = value as MessageData.FriendRequestResponse
                if (request.response == Response.ACCEPTED) {
                    println("Friend request to ${request.from} accepted")
                    // notify user that the friend has accepted their request
                }
                GlobalScope.launch {
                    client?.sendMessage(
                        MessageData.FRIEND_REQUEST_RESPONSE_TYPE, request
                    )
                }
            }
            ViewEvent.LobbyInviteEvent -> {
                GlobalScope.launch {
                    client?.sendLobbyInvite(value as String)
                }
            }
            ViewEvent.LobbyInviteResponseEvent -> {
                val invite = value as MessageData.LobbyInviteResponse
                if (invite.response == Response.ACCEPTED) {
                    client?.lobby = invite.lobbyId
                }
                GlobalScope.launch {
                    client?.sendMessage(
                        MessageData.LOBBY_INVITE_RESPONSE_TYPE, invite
                    )
                }
            }
        }
    }

    suspend fun invokeTestSubmission(code: String) {
        println("SUBMIT SOLUTION TRIGGERED")
        println(code)
        model.submissionHistory.add(
            UserSubmission(
                id = 0,
                username = model.currentUser?.userName ?: "",
                problemId = model.selectedProblem?.id ?: -1,
                submittedCode = code
            )
        )
        // you can call api here and grab test cases via model.testcases
        val judge0ServiceManager = Judge0ServiceManager(model.selectedProblem, model.testCases)
        val codeExecutionResult = judge0ServiceManager.getUserOutputAndExpectedOutput()
        val userOutputArr = codeExecutionResult.first
        val expectedOutputArr = codeExecutionResult.second
        if (userOutputArr.size != model.testCases.size || expectedOutputArr.size != model.testCases.size) {
            println("Submission Failed") //todo handle error
            for (i in 0 until model.testCases.size) {
                invokeTestUpdateOutput(i,
                    "Error",
                    "Error")
                invokeChangeInCodeExecutionStatus()
            }
        } else {
            println("Code execution successful")
            // invoke testcase updates for each test case
            for (i in 0 until model.testCases.size) {
                var userOutput = ""
                var expectedOutput = ""
                if (userOutputArr[i].second == null || userOutputArr[i].second == "null") {
                    userOutput = userOutputArr[i].first ?: ""
                } else {
                    userOutput = userOutputArr[i].second ?: ""
                }
                if (expectedOutputArr[i].second == null || expectedOutputArr[i].second == "null") {
                    expectedOutput = expectedOutputArr[i].first ?: ""
                } else {
                    expectedOutput = expectedOutputArr[i].second ?: ""
                }
                invokeTestUpdateOutput(i,
                    userOutput,
                    expectedOutput)
                invokeChangeInCodeExecutionStatus()
            }
        }
        // todo update the model with the results
        //testing
    }

    // need to have two params, can't just cast Any
    // since already made on, made all other test-related as functions
    fun invokeTestUpdate(idx : Int, input : String) {
        var tmp = model.testCases.map{it.copy()}.toMutableList()
        tmp[idx] = TestCase(
            tmp[idx].id,
            tmp[idx].problemId,
            input,
            tmp[idx].expectedOutput,
            tmp[idx].userOutput
        )
        model.testCases = tmp
    }

    //    todo: add a function to update the model with the results of the execution (finished)
    fun invokeTestUpdateOutput(idx : Int, userOutput : String, expectedOutput : String) {
        var tmp = model.testCases.map{it.copy()}.toMutableList()
        tmp[idx] = TestCase(
            tmp[idx].id,
            tmp[idx].problemId,
            tmp[idx].input,
            userOutput,
            expectedOutput
        )
        model.testCases = tmp
    }


    fun invokeChangeInCodeExecutionStatus() {
        model.codeExecutionStatus = model.codeExecutionStatus + 1
    }

    fun invokeTestDelete(idx : Int) {
        var tmp = model.testCases.toMutableList()
        tmp.removeAt(idx)
        model.testCases = tmp
    }

    fun invokeTestAdd(last: TestCase) {
        var tmp = model.testCases!!.toMutableList()
        tmp.add(last)
        model.testCases = tmp
    }

    fun invokeAnswerQuestion(problemId: Int, answer: Int) {
        model.gameStatus = GameStatus(
            model.gameStatus.playerStatus,
            model.gameStatus.round,
            model.gameStatus.puzzle,
            model.gameStatus.score,
            model.gameStatus.correctAnswer,
            answer,
            )
        GlobalScope.launch {
            client!!.sendMessage(
                MessageData.ANSWER_PUZZLE_TYPE,
                MessageData.AnswerPuzzle(model.gameStatus.round, answer)
            )
            println("MESSAGE SENT")
        }
        println("answered $problemId with $answer")
    }
}