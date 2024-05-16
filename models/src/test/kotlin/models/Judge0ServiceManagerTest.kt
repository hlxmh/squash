package models

import kotlin.test.*
import kotlinx.coroutines.runBlocking


class Judge0ServiceManagerTest {
    @Test
    fun TestgetUserOutputAndExpectedOutput() : Unit = runBlocking {
        val prob = Problem(1, "Hello, World!", "Hello, World!", "print(input())",
            "print(input())", listOf("1", "python", ""))
        val testCase = TestCase(1, 1, "Hello, World!", "Hello, World!", "Hello, World!")
        val testCases = listOf(testCase)

        val judge0ServiceManager = Judge0ServiceManager(prob, testCases)
        val userOutput = "Hello, World!\n"
        val expectedOutput = "Hello, World!\n"
        val result = judge0ServiceManager.getUserOutputAndExpectedOutput()
        assertEquals(userOutput, result.first[0].first)
        assertEquals(expectedOutput, result.second[0].first)



    }
//    @Test
//    fun Test2getUserOutputAndExpectedOutput():Unit = runBlocking {
//        val prob = Problem(1, "Hello, World!", "Hello, World!", "fun main() {\n" +
//                "    println(\"Enter the numbers separated by space:\")\n" +
//                "    val nums = readLine()!!.split(' ').map(String::toInt).toIntArray()\n" +
//                "\n" +
//                "    println(\"Enter the target number:\")\n" +
//                "    val target = readLine()!!.toInt()\n" +
//                "\n" +
//                "    try {\n" +
//                "        val indices = twoSum(nums, target)\n" +
//                "        println(\"Indices found: ${indices.joinToString()}\")\n" +
//                "    } catch (e: IllegalStateException) {\n" +
//                "        println(e.message)\n" +
//                "    }\n" +
//                "}\n" +
//                "\n" +
//                "fun twoSum(nums: IntArray, target: Int): IntArray {\n" +
//                "    val hashMap = mutableMapOf<Int, Int>() // value to index\n" +
//                "\n" +
//                "    for ((i, v) in nums.withIndex()) {\n" +
//                "        val diff = target - v\n" +
//                "        val indexForDiffValue = hashMap[diff]\n" +
//                "\n" +
//                "        indexForDiffValue?.let {\n" +
//                "            return intArrayOf(indexForDiffValue, i)\n" +
//                "        }\n" +
//                "\n" +
//                "        hashMap[v] = i\n" +
//                "    }\n" +
//                "\n" +
//                "    throw IllegalStateException(\"no solution found\")\n" +
//                "}",
//            "fun main() {\n" +
//                    "    println(\"Enter the numbers separated by space:\")\n" +
//                    "    val nums = readLine()!!.split(' ').map(String::toInt).toIntArray()\n" +
//                    "\n" +
//                    "    println(\"Enter the target number:\")\n" +
//                    "    val target = readLine()!!.toInt()\n" +
//                    "\n" +
//                    "    try {\n" +
//                    "        val indices = twoSum(nums, target)\n" +
//                    "        println(\"Indices found: ${indices.joinToString()}\")\n" +
//                    "    } catch (e: IllegalStateException) {\n" +
//                    "        println(e.message)\n" +
//                    "    }\n" +
//                    "}\n" +
//                    "\n" +
//                    "fun twoSum(nums: IntArray, target: Int): IntArray {\n" +
//                    "    val hashMap = mutableMapOf<Int, Int>() // value to index\n" +
//                    "\n" +
//                    "    for ((i, v) in nums.withIndex()) {\n" +
//                    "        val diff = target - v\n" +
//                    "        val indexForDiffValue = hashMap[diff]\n" +
//                    "\n" +
//                    "        indexForDiffValue?.let {\n" +
//                    "            return intArrayOf(indexForDiffValue, i)\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        hashMap[v] = i\n" +
//                    "    }\n" +
//                    "\n" +
//                    "    throw IllegalStateException(\"no solution found\")\n" +
//                    "}", listOf("1", "Kotlin", ""))
//        val testCase = TestCase(1, 1, "2 7 11 15\n 9", "Hello, World!", "Hello, World!")
//        val testCases = listOf(testCase)
//
//        val judge0ServiceManager = Judge0ServiceManager(prob, testCases)
//        val userOutput = "Hello, World!\n"
//        val expectedOutput = "Hello, World!\n"
//        val result = judge0ServiceManager.getUserOutputAndExpectedOutput()
//        assertEquals(userOutput, result.first[0].first)
//        assertEquals(expectedOutput, result.second[0].first)
//
//
//
//    }

}