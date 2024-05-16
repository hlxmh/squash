package models

class UserModel : IPresenter() {
    var selectedProblem: Problem? = null
        set(value) {
            field = value
            notifySubscribers()
        }

    // ensures at least one test case always
    var testCases: List<TestCase> = List<TestCase>(1) { TestCase(1, 1, "", "", "") }
        set(value) {
            field = value
            notifySubscribers()
        }

    var currentUser: User? = null
        set(value) {
            field = value
            notifySubscribers()
        }

    var submissionHistory: MutableList<UserSubmission> = mutableListOf()
        set(value) {
            field = value
            notifySubscribers()
        }

    var pendingLobbyInvitations: MutableList<MessageData.LobbyInvite> = mutableListOf()
        set(value) {
            field = value
            notifySubscribers()
        }

    var lobbyInvites: Int = 0
        set(value) {
            field = value
            notifySubscribers()
        }

    var pendingFriendRequests: MutableList<MessageData.FriendRequest> = mutableListOf()
        set(value) {
            field = value
            notifySubscribers()
        }

    var friendRequests: Int = 0
        set(value) {
            field = value
            notifySubscribers()
        }

    var gameStatus: GameStatus = GameStatus(PlayerStatus.IDLE, 0, null, 0, -1, -1)
        set(value) {
            field = value
            notifySubscribers()
        }

    var lobbyStatus: LobbyStatus = LobbyStatus(null,  List<String>(0) { "" })
        set(value) {
            field = value
            notifySubscribers()
        }

    var codeExecutionStatus: Int = 0
        set(value) {
            field = value
            notifySubscribers()
        }
}