package com.example.service
import com.example.common.ProgrammingLanguage
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Base64
import okhttp3.HttpUrl.Companion.toHttpUrl
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class SubmissionResult(
    val stdout: String?,
    val stderr: String?,
    val status: Status?,
    val time: Float?,
    val memory: Int?
)

@Serializable
data class Status(
    val id: Int,
    val description: String
)
// Service class to interact with the Judge0 API, ideally the only function that needs to be called is getExecutionResult
open class Judge0Service(private val apiKey: String = "a7cbe494d5mshdc706136167678ep1db750jsne456357e04e6") {
    private val client = OkHttpClient()
    private val baseUrl = "https://judge0-ce.p.rapidapi.com"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getExecutionResultBatch2(
        userCodes: Array<String> = emptyArray(),
        correctCodes: Array<String> = emptyArray(),
        inputs: Array<String>,
        languages: Array<ProgrammingLanguage>
    ):Pair<Array<Pair<String?, String?>>, Array<Pair<String?, String?>>>{
        if (userCodes.isEmpty() && correctCodes.isEmpty()) {
            throw Exception("Both userCodes and correctCodes are empty")
        }
        val userTokens = createSubmissionBatch(userCodes, emptyArray(), inputs, languages)
        val correctTokens = createSubmissionBatch(emptyArray(), correctCodes, inputs, languages)

        val userResults = getSubmissionResultBatch(userTokens)
        val correctResults = getSubmissionResultBatch(correctTokens)

        fun parseResults(resultsJson: String): List<Pair<String?, String?>> {
            val jsonElement = Json.parseToJsonElement(resultsJson)
            val submissionsArray = jsonElement.jsonObject["submissions"]?.jsonArray
            return submissionsArray?.mapNotNull { submission ->
                val stdout = submission.jsonObject["stdout"]?.jsonPrimitive?.contentOrNull
                val stderr = submission.jsonObject["stderr"]?.jsonPrimitive?.contentOrNull
                stdout to stderr
            } ?: emptyList()
        }

        val userResultsParsed = parseResults(userResults)
        val correctResultsParsed = parseResults(correctResults)

        val retval = Pair(userResultsParsed.toTypedArray(), correctResultsParsed.toTypedArray())
        return retval

//        return userResultsParsed.zip(correctResultsParsed) { user, correct ->
//            when {
//                user.second != null && user.second != "null" -> "Error"
//                user.first == correct.first -> "Correct"
//                else -> "Incorrect"
//            }
//        }.toTypedArray()
    }

    // function to get the execution result of a single submission batch. Ideally, this is the only function that is needed to be called
    suspend fun getExecutionResultBatch(
        userCodes: Array<String> = emptyArray(),
        correctCodes: Array<String> = emptyArray(),
        inputs: Array<String>,
        languages: Array<ProgrammingLanguage>
    ): Array<String> {
        if (userCodes.isEmpty() && correctCodes.isEmpty()) {
            throw Exception("Both userCodes and correctCodes are empty")
        }
        val userTokens = createSubmissionBatch(userCodes, emptyArray(), inputs, languages)
        val correctTokens = createSubmissionBatch(emptyArray(), correctCodes, inputs, languages)

        val userResults = getSubmissionResultBatch(userTokens)
        val correctResults = getSubmissionResultBatch(correctTokens)

//        val userResultsJsonArray = kotlinx.serialization.json.Json.parseToJsonElement(userResults).jsonArray
//        val correctResultsJsonArray = kotlinx.serialization.json.Json.parseToJsonElement(correctResults).jsonArray
//
        fun parseResults(resultsJson: String): List<Pair<String?, String?>> {
            val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(resultsJson)
            val submissionsArray = jsonElement.jsonObject["submissions"]?.jsonArray
            return submissionsArray?.mapNotNull { submission ->
                val stdout = submission.jsonObject["stdout"]?.jsonPrimitive?.contentOrNull
                val stderr = submission.jsonObject["stderr"]?.jsonPrimitive?.contentOrNull
                stdout to stderr
            } ?: emptyList()
        }

        val userResultsParsed = parseResults(userResults)
        val correctResultsParsed = parseResults(correctResults)


        // Compare the results and construct the output array
        return userResultsParsed.zip(correctResultsParsed) { user, correct ->
            when {
                user.second != null && user.second != "null" -> "Error"
                user.first == correct.first -> "Correct"
                else -> "Incorrect"
            }
        }.toTypedArray()
    }
    suspend  fun getExecutionResult(userCode: String, correctCode: String, input: String, language: ProgrammingLanguage): String {
        val tokenPair = createSubmission(userCode, correctCode, input, language)
        val userToken = tokenPair.first
        val correctToken = tokenPair.second
        val resultUser = getSubmissionResult(userToken)
        val resultCorrect = getSubmissionResult(correctToken)
//        val stderr = Regex("\"stderr\":(\\d+)").find(resultUser)?.groupValues?.get(1)?.toString()
        val resultUserJson = kotlinx.serialization.json.Json.parseToJsonElement(resultUser)
        val resultCorrectJson = kotlinx.serialization.json.Json.parseToJsonElement(resultCorrect)
        val stderr = resultUserJson.jsonObject["stderr"]?.jsonPrimitive?.content.toString()
        if (stderr != "null") {
            return "Error"
        } else {
//            val stdoutUser = Regex("\"stdout\":\"(.*?)\"").find(resultUser)?.groupValues?.get(1)?.toString()
//            val stdoutCorrect = Regex("\"stdout\":\"(.*?)\"").find(resultCorrect)?.groupValues?.get(1)?.toString()
            val stdoutUser = resultUserJson.jsonObject["stdout"]?.jsonPrimitive?.content.toString()
            val stdoutCorrect = resultCorrectJson.jsonObject["stdout"]?.jsonPrimitive?.content.toString()
            if (stdoutUser == stdoutCorrect) {
                return "Correct"
            } else {
                return "Incorrect"
            }
        }


    }

    suspend fun getSubmissionResultBatch(tokens: Array<String>): String{
        val url = "$baseUrl/submissions/batch".toHttpUrl().newBuilder()
            .addQueryParameter("tokens", tokens.joinToString(","))
            .addQueryParameter("base64_encoded", "false")
            .addQueryParameter("fields", "*")
            .build()

        var result: SubmissionResult
        while (true) {
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("X-RapidAPI-Key", apiKey)
                .addHeader("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to get the submission results: HTTP ${response.code}")
                }

                val responseBody = response.body!!.string()
                val idRegex = """"id":(\d+)""".toRegex()
                val matchResult = idRegex.find(responseBody)

                val statusId = matchResult?.groups?.get(1)?.value?.toInt()

                if (statusId != 1 && statusId != 2) { // Check if the status is neither 'In Queue' nor 'Processing'
                    return responseBody
                }

                delay(1000)  // Wait for 1 second before polling again

            }
        }
    }


   suspend fun getSubmissionResult(token: String): String {
       val url = "$baseUrl/submissions/$token".toHttpUrl().newBuilder()
           .addQueryParameter("base64_encoded", "false")
           .addQueryParameter("fields", "*")
           .build()

       var result: SubmissionResult
       while (true) {
           val request = Request.Builder()
               .url(url)
               .get()
               .addHeader("X-RapidAPI-Key", apiKey)
               .addHeader("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com")
               .build()

           client.newCall(request).execute().use { response ->
               if (!response.isSuccessful) {
                   throw Exception("Failed to get the submission results: HTTP ${response.code}")
               }

               val responseBody = response.body!!.string()
               val idRegex = """"id":(\d+)""".toRegex()
               val matchResult = idRegex.find(responseBody)

               val statusId = matchResult?.groups?.get(1)?.value?.toInt()

               if (statusId != 1 && statusId != 2) { // Check if the status is neither 'In Queue' nor 'Processing'
                   return responseBody
               }

               delay(1000)  // Wait for 1 second before polling again

           }
       }
   }

    fun deleteSubmission(token: String): String {
        val url = " https://ce.judge0.com/submissions/$token?fields=stdout,stderr,status_id,language_id"

        val request = Request.Builder()
            .url(url)
            .delete()
            .addHeader("X-RapidAPI-Key", apiKey)
            .addHeader("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to delete the submission: HTTP ${response.code}")
            }
            val responseBody = response.body!!.string()
            return responseBody
        }
    }

    fun createSubmission(userCode: String, correctCode: String, input: String, language: ProgrammingLanguage): Pair<String, String> {
        val userPayload = buildPayload(userCode, input, language)
        val correctPayload = buildPayload(correctCode, input, language)

        val userToken = sendSubmissionRequest(userPayload)
        val correctToken = sendSubmissionRequest(correctPayload)

        return userToken to correctToken
    }

    // creates a submission batch with either userCodes or correctCodes not both
    fun createSubmissionBatch(userCodes: Array<String> = emptyArray(),
                              correctCodes: Array<String> = emptyArray(),
                              inputs: Array<String>,
                              languages: Array<ProgrammingLanguage>): Array<String> {

        val userPayload = buildPayloadBatch(userCodes, inputs, languages)
        val correctPayload = buildPayloadBatch(correctCodes, inputs, languages)

//        val userToken = sendSubmissionRequest(userPayload)
//        val correctToken = sendSubmissionRequest(correctPayload)
        return if (userCodes.isEmpty()) {
            sendBatchSubmissionRequest(payload = correctPayload)
        }  else if (correctCodes.isEmpty()) {
            sendBatchSubmissionRequest(payload = userPayload)
        } else {
            throw Exception("Both userCodes and correctCodes are empty")
        }
    }

    private fun buildPayloadBatch(codes: Array<String>,
                                  inputs: Array<String>,
                                  languages: Array<ProgrammingLanguage>): String {

        if (codes.isEmpty()) return "" // No submissions to create
        val encodedSourceCodes = codes.map { Base64.getEncoder().encodeToString(it.toByteArray()) }
        val encodedInputs = inputs.map { Base64.getEncoder().encodeToString(it.toByteArray()) }
        val submissions = mutableListOf<String>()
        for (i in codes.indices) {
            submissions.add("""
            {
                "language_id": ${languages[i].id},
                "source_code": "${encodedSourceCodes[i]}",
                "stdin": "${encodedInputs[i]}"
            }
        """.trimIndent())
        }

        return """
        {
            "submissions": [
                ${submissions.joinToString(",")}
            ]
        }
    """.trimIndent()
    }

    private fun buildPayload(sourceCode: String, input: String, language: ProgrammingLanguage): String {
        val encodedSourceCode = Base64.getEncoder().encodeToString(sourceCode.toByteArray())
        val encodedInput = Base64.getEncoder().encodeToString(input.toByteArray())
        return """
            {
                "language_id": ${language.id},
                "source_code": "$encodedSourceCode",
                "stdin": "$encodedInput"
            }
        """.trimIndent()
    }

    private fun sendBatchSubmissionRequest(payload: String): Array<String> {
        val body = payload.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/submissions/batch?base64_encoded=true&fields=*")
            .post(body)
            .addHeader("X-RapidAPI-Key", apiKey)
            .addHeader("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to create batch submission: ${response.message}")
            }

            val responseBody = response.body!!.string()
            val responseJsonArray = kotlinx.serialization.json.Json.parseToJsonElement(responseBody).jsonArray
//            val tokensJsonArray = responseJson["tokens"]?.jsonArray
//                ?: throw Exception("No tokens array found in response")
            val tokensList = responseJsonArray.mapNotNull {
                it.jsonObject["token"]?.jsonPrimitive?.content
            } ?: throw Exception("No tokens array found in response")
            return tokensList.toTypedArray()
        }
    }



    private fun sendSubmissionRequest(payload: String): String {
        val body = payload.toRequestBody(jsonMediaType)
        println(body)
        val request = Request.Builder()
            .url("$baseUrl/submissions?base64_encoded=true&fields=*")
            .post(body)
            .addHeader("X-RapidAPI-Key", apiKey)
            .addHeader("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to create submission: ${response.message}")
            val responseString = response.body!!.string()
            // Extract the token from the response
            // Assuming the response JSON has a "token" field
            return Regex("\"token\":\"(.*?)\"").find(responseString)?.groupValues?.get(1)
                ?: throw Exception("No token found in response")
        }
    }

    companion object {
        private const val STATUS_PROCESSING = 3
    }
    // Add more methods to interact with the Judge0 API if needed
}