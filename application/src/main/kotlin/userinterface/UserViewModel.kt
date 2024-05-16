package userinterface

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import models.*

class UserViewModel(val model: UserModel) : ISubscriber {
    var selectedProblem: Problem? = null
    var testCases = mutableStateOf(model.testCases)
    // this init value technically doesn't matter, should get overwritten by model
    var currentUser = mutableStateOf<User?>(null)
    var submissionHistory: MutableList<UserSubmission> = mutableListOf()

    var pendingLobbyInvitations: MutableList<MessageData.LobbyInvite> = mutableListOf()
    var lobbyInvites = mutableStateOf(0)

    var pendingFriendRequests: MutableList<MessageData.FriendRequest> = mutableListOf()
    var friendRequests = mutableStateOf(0)

    var gameStatus = mutableStateOf(model.gameStatus)
    var lobbyStatus = mutableStateOf(model.lobbyStatus)
    var codeExecutionStatus: Int = 0


    init {
        model.subscribe(this)
        update()
    }

    override fun update() {
        println("update view model")
        selectedProblem = model.selectedProblem
        testCases.value = model.testCases
        submissionHistory = model.submissionHistory
        currentUser.value = model.currentUser

        pendingLobbyInvitations = model.pendingLobbyInvitations
        lobbyInvites.value = model.lobbyInvites

        pendingFriendRequests= model.pendingFriendRequests
        friendRequests.value = model.friendRequests

        gameStatus.value = model.gameStatus
        lobbyStatus.value = model.lobbyStatus
        codeExecutionStatus = model.codeExecutionStatus
    }
}