import kotlin.test.Test
import com.example.service.Judge0Service
import com.example.common.ProgrammingLanguage
import kotlinx.coroutines.runBlocking
import models.DatabaseManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class testForCodeSubWithDB {
    @Test
    fun testDB() {
        val db = DatabaseManager()
        val prob = db.getRandomProblem()
        for (i in 0..3) {
            val prob = db.getRandomProblem()
            if (prob != null) {
                println(prob.tags)
                println(prob.id)
                println(db.getTestCasesByProblemId(prob.id))
                println()
            }
        }
        if (prob != null) {
            println(prob.tags)
//            println(prob.)
        }
//        println(lop)
        println("hello world")
    }

    // This test case is for testing the code submission functionality with no input
    @Test
    fun testCodeSubmission(): Unit = runBlocking {
        val judge0Service = Judge0Service()
        val db = DatabaseManager()

        val prob = db.getProblemById(1)
        val userSourceCode1 = prob?.solution
        //add null check
        val correctSourceCode1 = prob?.solution
//        val input1 = db.getTestCasesByProblemId(1)[0].input
        val language1 = prob?.tags?.get(1)?.let { ProgrammingLanguage.fromName(it) }

        val prob2 = db.getProblemById(2)
        val userSourceCode2 = prob2?.solution
        val correctSourceCode2 = prob2?.solution
//        val input2 = db.getTestCasesByProblemId(2)[0].input
        val language2 = prob2?.tags?.get(1)?.let { ProgrammingLanguage.fromName(it) }

        val prob3 = db.getProblemById(3)
        val userSourceCode3 = prob3?.solution
        val correctSourceCode3 = prob3?.solution
//        val input3 = db.getTestCasesByProblemId(4)[0].input
        val language3 = prob3?.tags?.get(1)?.let { ProgrammingLanguage.fromName(it) }


        // createSubmission now returns a Pair<String, String>
        var sourceCodes = arrayOf(userSourceCode1, userSourceCode2, userSourceCode3).map { it ?: "nullStr" }.toTypedArray()
        var correctCodes = arrayOf(correctSourceCode1, correctSourceCode2, correctSourceCode3).map { it ?: "nullStr" }.toTypedArray()
        var inputs = arrayOf("", "", "").map { it ?: "nullStr" }.toTypedArray()
        var languages = arrayOf(language1, language2, language3).map { it ?: ProgrammingLanguage.PYTHON }.toTypedArray()

        var results = judge0Service.getExecutionResultBatch(sourceCodes, correctCodes, inputs, languages)

        assertNotNull(results, "Result should not be null")
        results.forEach {
            assertEquals("Correct", it.toString(), "Result should be 'Correct' after successful submission.")
        }
    }

}