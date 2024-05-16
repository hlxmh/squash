package models

import com.example.common.ProgrammingLanguage
import com.example.service.Judge0Service


class Judge0ServiceManager (val problem: Problem?, val testCases: List<TestCase>): Judge0Service(){

    // return a pair of arrays of pairs of strings, th pair type in each array is the stdout and stderr of the code
    suspend fun getUserOutputAndExpectedOutput(): Pair<Array<Pair<String?, String?>>, Array<Pair<String?, String?>>>{
        val length = testCases.size
        var userCodes = Array<String?>(length) { null }
        var correctCodes = Array<String?>(length) { null }
        var languages = Array<ProgrammingLanguage?>(length) { null }
        var inputs = Array<String?>(length) { null }
        for (i in 0..<length) {
            userCodes[i] = problem?.code
            correctCodes[i] = problem?.solution
            languages[i] = problem?.tags?.get(1)?.let { ProgrammingLanguage.fromName(it) }
            inputs[i] = testCases[i].input
        }

        var UserCodesNotNull = userCodes.map { it ?: "nullStr" }.toTypedArray()
        var CorrectCodesNotNull = correctCodes.map { it ?: "nullStr" }.toTypedArray()
        var LanguagesNotNull = languages.map { it ?: ProgrammingLanguage.PYTHON }.toTypedArray()
        var InputsNotNull = inputs.map { it ?: "nullStr" }.toTypedArray()

        var retval = emptyPairOfArrays<Pair<String?, String?>>() //initialize the return value
        try {
            retval = getExecutionResultBatch2(UserCodesNotNull, CorrectCodesNotNull, InputsNotNull, LanguagesNotNull)
            assert(retval.first.size == length)
            assert(retval.second.size == length)
            //assert the second value of each pair in both array is null or "null"
//            for (i in 0..<length){
//                assert(retval.first[i].second == null || retval.first[i].second == "null")
//                assert(retval.second[i].second == null || retval.second[i].second == "null")
//            }
        } catch (e: Exception) {
            // Log the exception or handle it as needed
//            var retval = emptyPairOfArrays<Pair<String?, String?>>()
            println(e.message)
        }
        return retval
    }

    private inline fun <reified T> emptyPairOfArrays(): Pair<Array<T>, Array<T>> = Pair(emptyArray(), emptyArray())

    //todo: add a function to get the results getExecutionResultBatch
}