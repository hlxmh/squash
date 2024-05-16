package models

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DataClassesTest {
    @Test
    fun testProblem() {
        val problem = Problem(
            1234, "title", "description", "code", "solution", listOf("tag1", "tag2")
        )
        assertEquals(1234, problem.id)
        assertEquals("title", problem.title)
        assertEquals("description", problem.description)
        assertEquals("code", problem.code)
        assertEquals(listOf("tag1", "tag2"), problem.tags)
    }

    @Test
    fun testTestCase() {
        val testCase = TestCase(
            1234, 11111, "input", "output", "expectedOutput"
        )
        assertEquals(1234, testCase.id)
        assertEquals(11111, testCase.problemId)
        assertEquals("input", testCase.input)
        assertEquals("output", testCase.expectedOutput)
    }

    @Test
    fun testUserSubmission() {
        val submission = UserSubmission(
            1234, 11111, "username", "code"
        )
        assertEquals(1234, submission.id)
        assertEquals(11111, submission.problemId)
        assertEquals("username", submission.username)
        assertEquals("code", submission.submittedCode)
    }

    @Test
    fun testUser() {
        val hashedPassword = User.hashPassword("password")
        val user = User(
            "username", "name", hashedPassword, "email"
        )
        assertEquals("username", user.userName)
        assertEquals("name", user.displayName)
        assertEquals(hashedPassword, user.hashedPassword)
        assertEquals("email", user.email)
    }

    @Test
    fun testUserMetrics() {
        val metrics = UserMetrics(
            "username", 1, 2, 3, 4, 5, 6, 7
        )
        assertEquals("username", metrics.userName)
        assertEquals(1, metrics.totalCompleted)
        assertEquals(2, metrics.easyCompleted)
        assertEquals(3, metrics.mediumCompleted)
        assertEquals(4, metrics.hardCompleted)
        assertEquals(5, metrics.lifetimeHints)
        assertEquals(6, metrics.submittedProblems)
        assertEquals(7, metrics.topPuzzleScore)
    }

    @Test
    fun testFriendship() {
        val friendship = Friendship("user", "friend")
        assertEquals("user", friendship.userName)
        assertEquals("friend", friendship.friendName)
    }
}