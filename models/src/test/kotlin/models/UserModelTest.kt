package models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserModelTest {

    @Test
    fun testSelectedProblem() {
        val userModel = UserModel()
        val testSubscriber = TestSubscriber()
        val problem = Problem(
            0, "title", "description", "code", "solution", listOf("tag1", "tag2")
        )

        userModel.subscribe(testSubscriber)
        userModel.selectedProblem = problem

        assertTrue(testSubscriber.updateCalled)
        assertEquals(problem, userModel.selectedProblem)
    }

    @Test
    fun testTestCases() {
        val userModel = UserModel()
        val testSubscriber = TestSubscriber()

        val test = TestCase(
            0, 0, "", "", ""
        )

        userModel.subscribe(testSubscriber)
        userModel.testCases = List(1) {
            test
        }

        assertTrue(testSubscriber.updateCalled)
        assertTrue(userModel.testCases.contains(test))
    }

    @Test
    fun testSelectedProblemNull() {
        val userModel = UserModel()
        val testSubscriber = TestSubscriber()

        userModel.subscribe(testSubscriber)
        userModel.selectedProblem = null

        assertTrue(testSubscriber.updateCalled)
        assertEquals(null, userModel.selectedProblem)
    }

    @Test
    fun testCurrentUser() {
        val userModel = UserModel()
        val testSubscriber = TestSubscriber()
        val user = User(
            "username", "name", "password", "email"
        )

        userModel.subscribe(testSubscriber)
        userModel.currentUser = user

        assertTrue(testSubscriber.updateCalled)
        assertEquals(user, userModel.currentUser)
    }

    @Test
    fun testCurrentUserNull() {
        val userModel = UserModel()
        val testSubscriber = TestSubscriber()

        userModel.subscribe(testSubscriber)
        userModel.currentUser = null

        assertTrue(testSubscriber.updateCalled)
        assertEquals(null, userModel.currentUser)
    }

    @Test
    fun testSubmissionHistory() {
        val userModel = UserModel()
        val testSubscriber = TestSubscriber()
        val submission = UserSubmission(
            9, 123, "username", "code"
        )

        userModel.subscribe(testSubscriber)
        userModel.submissionHistory = mutableListOf(submission)

        assertTrue(testSubscriber.updateCalled)
        assertTrue(userModel.submissionHistory.contains(submission))
    }
}
