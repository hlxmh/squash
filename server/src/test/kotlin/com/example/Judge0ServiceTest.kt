
import com.example.common.ProgrammingLanguage
import com.example.service.Judge0Service
//import com.example.userinterface.DatabaseManager
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import java.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


class Judge0ServiceTest {
    @Test
    fun blah() {
//        val judge0Service = Judge0Service()
//        val dbManager = DatabaseManager()
//        val lop = dbManager.getProblems()
//        println(lop)
        val sourceCode = "print('Hello, World!')"
        val encodedSourceCode = Base64.getEncoder().encodeToString(sourceCode.toByteArray())
        val decoded = Base64.getDecoder().decode(encodedSourceCode).toString(Charsets.UTF_8)
        println(decoded)
        println(encodedSourceCode)
    }
    @Test
    fun testCreateSubmission() {
        val judge0Service = Judge0Service()
        val userSourceCode = "print('Hello, World!')"
        val correctSourceCode = "print('Hello, World!')"
        val input = ""
        val language = ProgrammingLanguage.fromName("python") ?: throw IllegalArgumentException("Invalid language name")

        // createSubmission now returns a Pair<String, String>
        val (userToken, correctToken) = judge0Service.createSubmission(
            userSourceCode,
            correctSourceCode,
            input,
            language
        )

        // Assertions to ensure that the tokens are not null or empty
        assertNotNull(userToken, "User token should not be null")
        assertTrue(userToken.isNotEmpty(), "User token should not be empty")

        assertNotNull(correctToken, "Correct token should not be null")
        assertTrue(correctToken.isNotEmpty(), "Correct token should not be empty")

        // Print the tokens to the shell
        println("User submission token: $userToken")
        println("Correct submission token: $correctToken")
    }


    @Test
    fun testGetSubmissionResult() = runTest {
        val judge0Service = Judge0Service()
        val result = judge0Service.getSubmissionResult("397611cb-6f3c-41d9-bedf-9623544a8a83")
        println("get submission response: $result")
        val resultJson = Json.parseToJsonElement(result)
//        val stdoutEncoded = resultJson.jsonObject["stdout"]?.jsonPrimitive?.content
//        val stdout = Base64.getDecoder().decode(stdoutEncoded).toString(Charsets.UTF_8)
        val stdout = resultJson.jsonObject["stdout"]?.jsonPrimitive?.content
        println(stdout)
        assertNotNull(result, "submission result should not be null")

    }

//    @Test
//    fun testDeleteSubmission() = runTest {
//        val judge0Service = Judge0Service()
//        val result = judge0Service.deleteSubmission("397611cb-6f3c-41d9-bedf-9623544a8a83")
//        println("delete submission response: $result")
//        assertNotNull(result, "submission result should not be null")
//    }

    @Test
    fun testGetExecutionResult() = runBlocking{
        val judge0Service = Judge0Service()
//        val path = Paths.get("com/example/testscriptpython.txt")
//        val path = Paths.get("/Users/juli/IdeaProjects/team-14/server/src/test/kotlin/com/example/testscriptpython.txt")
//        val userSourceCode = Files.readAllLines(path).joinToString("\n")
//        val correctSourceCode = Files.readAllLines(path).joinToString("\n")

        val userSourceCode = "print('Hello, World!')"
        val correctSourceCode = "print('Hello, World!')"
        val input = ""
        val language = ProgrammingLanguage.PYTHON

        val result = judge0Service.getExecutionResult(userSourceCode, correctSourceCode, input, language)
        assertNotNull(result.toString(), "Result should not be null after successful submission.")
        assertEquals("Correct", result.toString(), "Result should be 'Correct' after successful submission.")
    }

    @Test
    fun testcreateSubmissionBatch() {
        val judge0Service = Judge0Service()
        val userSourceCode1 = "print('Hello, World!')"
        val correctSourceCode1 = "print('Hello, World!')"
        val input1 = ""
        val language1 = ProgrammingLanguage.PYTHON

        val userSourceCode2 = "print('Hello, World!')"
        val correctSourceCode2 = "print('Hello, World!')"
        val input2 = ""
        val language2 = ProgrammingLanguage.PYTHON

        val userSourceCode3 = "print('Hello, World!')"
        val correctSourceCode3 = "print('Hello, World!')"
        val input3 = ""
        val language3 = ProgrammingLanguage.PYTHON

        // createSubmission now returns a Pair<String, String>

        val userCodes = arrayOf(userSourceCode1, userSourceCode2, userSourceCode3)
        val correctCodes = arrayOf(correctSourceCode1, correctSourceCode2, correctSourceCode3)
        val inputs = arrayOf(input1, input2, input3)
        val languages = arrayOf(language1, language2, language3)
        val userTokens = judge0Service.createSubmissionBatch(
            userCodes = userCodes,
            inputs = inputs,
            languages = languages
        )
        val correctTokens = judge0Service.createSubmissionBatch(
            correctCodes = correctCodes,
            inputs = inputs,
            languages = languages
        )

        assertNotNull(userTokens, "User tokens should not be null")
        assertTrue(userTokens.isNotEmpty(), "User tokens should not be empty")

        assertNotNull(correctTokens, "Correct tokens should not be null")
        assertTrue(correctTokens.isNotEmpty(), "Correct tokens should not be empty")

        // Print the tokens to the shell
        println("User submission tokens:")
        println(userTokens.contentToString())
        println("Correct submission tokens:")
        println(correctTokens.contentToString())

    }

    @Test
    fun testGetExecutionResultBatch(): Unit = runBlocking {
        val judge0Service = Judge0Service()
        val userSourceCode1 = "print('Hello, World!')"
        val correctSourceCode1 = "print('Hello, World!')"
        val input1 = ""
        val language1 = ProgrammingLanguage.PYTHON

        val userSourceCode2 = "print('Hello, World!')"
        val correctSourceCode2 = "print('Hello, World!')"
        val input2 = ""
        val language2 = ProgrammingLanguage.PYTHON

        val userSourceCode3 = "print('Hello, World!')"
        val correctSourceCode3 = "print('Hello, World!')"
        val input3 = ""
        val language3 = ProgrammingLanguage.PYTHON

        val userCodes = arrayOf(userSourceCode1, userSourceCode2, userSourceCode3)
        val correctCodes = arrayOf(correctSourceCode1, correctSourceCode2, correctSourceCode3)
        val inputs = arrayOf(input1, input2, input3)
        val languages = arrayOf(language1, language2, language3)

        val results = judge0Service.getExecutionResultBatch(userCodes, correctCodes, inputs, languages)
        assertNotNull(results, "Results should not be null after successful submission.")
        // assert that each element in the results array is "Correct"
        results.forEach {
            assertEquals("Correct", it.toString(), "Result should be 'Correct' after successful submission.")
        }
    }

}
