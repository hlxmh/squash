package models// models.DatabaseManager.kt

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet


class DatabaseManager(
    hostname: String = "35.194.47.150",
    database: String = "data",
    username: String = "root",
    password: String = "y3PwQcHyTd08gMMx2nVi",
) {
    private val jdbcUrl: String = "jdbc:mysql://$hostname/$database?user=$username&password=$password"
    private var connection: Connection = establishConnection()

    // Establish a database connection
    private fun establishConnection(): Connection {
        Class.forName("com.mysql.cj.jdbc.Driver")
        return DriverManager.getConnection(jdbcUrl)
    }

    // Get the connection to the database
    private fun getConnection(): Connection {
        if (connection.isClosed) {
            connection = establishConnection()
        }
        return connection
    }

    // Test the database connection
    fun testConnection(): Boolean {
        return try {
            getConnection().use {
                println("Connection to database successful")
            }
            true
        } catch (e: Exception) {
            println("Failed to connect to the database: ${e.message}")
            false
        }
    }

    // Execute a SQL statement without result set
    private fun executeStatement(sql: String) {
        try {
            getConnection().createStatement().executeUpdate(sql)
            println("SQL statement executed successfully")
        } catch (e: Exception) {
            println("Failed to execute SQL statement: $sql ${e.message}")
        }
    }

    // Execute a SQL query with a result set
    private fun executeQuery(sql: String): ResultSet? {
        return try {
            getConnection().createStatement().executeQuery(sql)
        } catch (e: Exception) {
            println("Failed to execute SQL query: ${e.message}")
            null
        }
    }

    // Create a table in the database
    private fun createTable(sql: String, tableName: String) {
        try {
            executeStatement(sql)
            println("$tableName table created successfully")
        } catch (e: Exception) {
            println("Failed to create $tableName table: ${e.message}")
        }
    }

    // Insert data into a table
    private fun insertData(sql: String, tableName: String) {
        try {
            executeStatement(sql)
            println("Data inserted into $tableName table successfully")
        } catch (e: Exception) {
            println("Failed to insert data into $tableName table: ${e.message}")
        }
    }

    // Drop a table from the database
    fun dropTable(table: String) {
        try {
            executeStatement("DROP TABLE IF EXISTS $table")
            println("Table $table dropped successfully")
        } catch (e: Exception) {
            println("Failed to drop table $table: ${e.message}")
        }
    }

    // Create the problems table in the database
    fun createProblemsTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS Problems (
                Id INT PRIMARY KEY AUTO_INCREMENT,
                Title VARCHAR(255) NOT NULL,
                Description TEXT NOT NULL,
                InitialCode TEXT NOT NULL,
                SolutionCode TEXT NOT NULL,
                Tags TEXT NOT NULL
            )
        """.trimIndent()
        createTable(sql, "Problems")
    }

    // General function to create a problem
    fun createProblem(problem: Problem) {
        val sql = """
            INSERT INTO Problems (Title, Description, InitialCode, SolutionCode, Tags) 
            VALUES ('${problem.title}', '${problem.description}', '${problem.code}', '${problem.solution}', '${problem.tags.joinToString(",")}')
        """.trimIndent()
        insertData(sql, "Problems")
    }

    // General function to retrieve problems
    fun getProblems(): List<Problem> {
        val problems = mutableListOf<Problem>()
        val sql = "SELECT * FROM Problems"
        executeQuery(sql)?.use { resultSet ->
            while (resultSet.next()) {
                val problem = Problem(
                    id = resultSet.getInt("Id"),
                    title = resultSet.getString("Title"),
                    description = resultSet.getString("Description"),
                    code = resultSet.getString("initialCode"),
                    solution = resultSet.getString("solutionCode"),
                    tags = resultSet.getString("tags").split(","),
                )
                problems.add(problem)
            }
        }
        return problems
    }
    fun getProblemById(id: Int): Problem? {
        val sql = "SELECT * FROM Problems WHERE Id = $id"
        executeQuery(sql)?.use { resultSet ->
            if (resultSet.next()) {
                return Problem(
                    id = resultSet.getInt("Id"),
                    title = resultSet.getString("Title"),
                    description = resultSet.getString("Description"),
                    code = resultSet.getString("initialCode"),
                    solution = resultSet.getString("solutionCode"),
                    tags = resultSet.getString("tags").split(","),
                )
            }
        }
        return null
    }

    // General function to retrieve a random problem
    fun getRandomProblem(): Problem? {
        val sql = "SELECT * FROM Problems ORDER BY RAND() LIMIT 1"
        executeQuery(sql)?.use { resultSet ->
            print(resultSet)
            print(resultSet.isClosed)
            if (resultSet.next()) {
                return Problem(
                    id = resultSet.getInt("id"),
                    title = resultSet.getString("title"),
                    description = resultSet.getString("description"),
                    code = resultSet.getString("initialCode"),
                    solution = resultSet.getString("solutionCode"),
                    tags = resultSet.getString("tags").split(",")
                )
            }
        }
        return null
    }

    // Create the puzzles table in the database
    fun createPuzzlesTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS Puzzles (
                Id INT PRIMARY KEY AUTO_INCREMENT,
                Description TEXT NOT NULL,
                Choices TEXT NOT NULL
            )
        """.trimIndent()
        createTable(sql, "Puzzles")
    }

    // General function to create a puzzle
    fun createPuzzle(puzzle: Puzzle) {
        val sql = """
            INSERT INTO Puzzles (Description, Choices) 
            VALUES ('${puzzle.description}', '${puzzle.choices.joinToString(",")}')
        """.trimIndent()
        insertData(sql, "Puzzles")
    }

    // General function to retrieve problems
    fun getPuzzles(): List<Puzzle> {
        val puzzles = mutableListOf<Puzzle>()
        val sql = "SELECT * FROM Puzzles"
        executeQuery(sql)?.use { resultSet ->
            while (resultSet.next()) {
                val puzzle = Puzzle(
                    id = resultSet.getInt("Id"),
                    description = resultSet.getString("Description"),
                    choices = resultSet.getString("Choices").split(","),
                )
                puzzles.add(puzzle)
            }
        }
        return puzzles
    }

    // General function to retrieve a random problem
    fun getRandomPuzzle(): Puzzle? {
        val sql = "SELECT * FROM Puzzles ORDER BY RAND() LIMIT 1"
        executeQuery(sql)?.use { resultSet ->
            print(resultSet)
            print(resultSet.isClosed)
            if (resultSet.next()) {
                return Puzzle(
                    id = resultSet.getInt("id"),
                    description = resultSet.getString("description"),
                    choices = resultSet.getString("Choices").split(","),
                )
            }
        }
        return null
    }

    // Create the users table in the database
    fun createUsersTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS Users (
                UserName VARCHAR(255) PRIMARY KEY, 
                DisplayName VARCHAR(255) NOT NULL,
                HashedPassword VARCHAR(64) NOT NULL,
                Email VARCHAR(255) UNIQUE NOT NULL
            )
        """.trimIndent()
        createTable(sql, "Users")
    }

    // General function to create a user
    fun createUser(user: User) {
        val sql = """
            INSERT INTO Users (UserName, DisplayName, HashedPassword, Email)
            VALUES ('${user.userName}', '${user.displayName}', '${user.hashedPassword}', '${user.email}')
        """.trimIndent()
        insertData(sql, "Users")
        createUserMetrics(UserMetrics(user.userName))
    }

    // General function to retrieve a user by username
    fun getUser(userName: String): User? {
        val sql = "SELECT * FROM Users WHERE UserName = '$userName'"
        executeQuery(sql)?.use { resultSet ->
            if (resultSet.next()) {
                return User(
                    userName = resultSet.getString("UserName"),
                    displayName = resultSet.getString("DisplayName"),
                    hashedPassword = resultSet.getString("HashedPassword"),
                    email = resultSet.getString("Email")
                )
            }
        }
        return null
    }

    // Create the friendships table in the database
    fun createFriendshipsTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS Friendships (
                UserName VARCHAR(255) NOT NULL,
                FriendName VARCHAR(255) NOT NULL,
                PRIMARY KEY (UserName, FriendName),
                FOREIGN KEY (UserName) REFERENCES Users(UserName),
                FOREIGN KEY (FriendName) REFERENCES Users(UserName)
            )
        """.trimIndent()
        createTable(sql, "Friendships")
    }

    // General function to create a friendship
    fun createFriendship(user1: String, user2: String) {
        val sql = """
            INSERT INTO Friendships (UserName, FriendName)
            VALUES ('$user1', '$user2'), ('$user2', '$user1')
        """.trimIndent()
        insertData(sql, "Friendships")
    }

    // Function to remove a friendship
    fun removeFriendship(user1: String, user2: String) {
        val sql = """
        DELETE FROM Friendships 
        WHERE (UserName = '$user1' AND FriendName = '$user2') 
        OR (UserName = '$user2' AND FriendName = '$user1')
    """.trimIndent()
        insertData(sql, "Friendships")
    }


    // General function to retrieve friendships for a user
    fun getFriendships(userName: String): List<User> {
        val friendships = mutableListOf<User>()
        val sql = """
        SELECT u.UserName, u.DisplayName, u.HashedPassword, u.Email
        FROM Friendships f
        JOIN Users u ON f.FriendName = u.UserName
        WHERE f.UserName = '$userName'
    """.trimIndent()
        executeQuery(sql)?.use { resultSet ->
            while (resultSet.next()) {
                val friend = User(
                    userName = resultSet.getString("UserName"),
                    displayName = resultSet.getString("DisplayName"),
                    hashedPassword = resultSet.getString("HashedPassword"),
                    email = resultSet.getString("Email")
                )
                friendships.add(friend)
            }
        }
        return friendships
    }

    // Create the user metrics table in the database
    fun createUserMetricsTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS UserMetrics (
                UserName VARCHAR(255) PRIMARY KEY,
                TotalCompleted INT NOT NULL,
                EasyCompleted INT NOT NULL,
                MediumCompleted INT NOT NULL,
                HardCompleted INT NOT NULL,
                LifetimeHints INT NOT NULL,
                SubmittedProblems INT NOT NULL,
                TopPuzzleScore INT NOT NULL,
                FOREIGN KEY (UserName) REFERENCES Users(UserName)
            )
        """.trimIndent()
        createTable(sql, "UserMetrics")
    }

    // General function to create user metrics
    fun createUserMetrics(userMetrics: UserMetrics) {
        val sql = """
            INSERT INTO UserMetrics (
                UserName, TotalCompleted, EasyCompleted, MediumCompleted, 
                HardCompleted, LifetimeHints, SubmittedProblems, TopPuzzleScore
            )
            VALUES (
                '${userMetrics.userName}', ${userMetrics.totalCompleted}, ${userMetrics.easyCompleted}, 
                ${userMetrics.mediumCompleted}, ${userMetrics.hardCompleted}, 
                ${userMetrics.lifetimeHints}, ${userMetrics.submittedProblems}, ${userMetrics.topPuzzleScore}
            )
        """.trimIndent()
        insertData(sql, "UserMetrics")
    }

    // General function to retrieve user metrics
    fun getUserMetrics(userName: String): UserMetrics? {
        val sql = "SELECT * FROM UserMetrics WHERE UserName = '$userName'"
        executeQuery(sql)?.use { resultSet ->
            if (resultSet.next()) {
                return UserMetrics(
                    userName = resultSet.getString("UserName"),
                    totalCompleted = resultSet.getInt("TotalCompleted"),
                    easyCompleted = resultSet.getInt("EasyCompleted"),
                    mediumCompleted = resultSet.getInt("MediumCompleted"),
                    hardCompleted = resultSet.getInt("HardCompleted"),
                    lifetimeHints = resultSet.getInt("LifetimeHints"),
                    submittedProblems = resultSet.getInt("SubmittedProblems"),
                    topPuzzleScore = resultSet.getInt("TopPuzzleScore"),
                )
            }
        }
        return null
    }

    // Function to update user metrics
    fun updateUserMetrics(userMetrics: UserMetrics) {
        val oldUserMetrics = getUserMetrics(userMetrics.userName)
        if (oldUserMetrics != null) {
            val sql = """
                UPDATE UserMetrics
                SET TotalCompleted = ${userMetrics.totalCompleted},
                    EasyCompleted = ${userMetrics.easyCompleted},
                    MediumCompleted = ${userMetrics.mediumCompleted},
                    HardCompleted = ${userMetrics.hardCompleted},
                    LifetimeHints = ${userMetrics.lifetimeHints},
                    SubmittedProblems = ${userMetrics.submittedProblems},
                    TopPuzzleScore = ${userMetrics.topPuzzleScore}
                WHERE UserName = '${userMetrics.userName}'
            """.trimIndent()
            executeStatement(sql)
        } else {
            println("User metrics not found for username: ${userMetrics.userName}")
        }
    }

    // Create the test case table in the database
    fun createTestCasesTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS TestCases (
                Id INT AUTO_INCREMENT PRIMARY KEY,
                ProblemId INT NOT NULL,
                Input TEXT NOT NULL,
                ExpectedOutput TEXT NOT NULL,
                FOREIGN KEY (ProblemID) REFERENCES Problems(ID)
            )
        """.trimIndent()
        createTable(sql, "TestCases")
    }

    // Function to create a test case
    fun createTestCase(testCase: TestCase) {
        val sql = """
            INSERT INTO TestCases (ProblemId, Input, ExpectedOutput)
            VALUES (${testCase.problemId}, '${testCase.input}', '${testCase.expectedOutput}')
        """.trimIndent()
        insertData(sql, "TestCases")
    }

    // Function to retrieve a test case by ID
    fun getTestCaseById(id: Int): TestCase? {
        val sql = "SELECT * FROM TestCases WHERE Id = $id"
        executeQuery(sql)?.use { resultSet ->
            if (resultSet.next()) {
                return TestCase(
                    id = resultSet.getInt("Id"),
                    problemId = resultSet.getInt("ProblemId"),
                    input = resultSet.getString("Input"),
                    expectedOutput = resultSet.getString("ExpectedOutput"),
                    userOutput = ""
                )
            }
        }
        return null
    }

    // Function to retrieve test cases by problem ID
    fun getTestCasesByProblemId(problemId: Int): List<TestCase> {
        val testCases = mutableListOf<TestCase>()
        val sql = "SELECT * FROM TestCases WHERE ProblemId = $problemId"
        executeQuery(sql)?.use { resultSet ->
            while (resultSet.next()) {
                testCases.add(
                    TestCase(
                        id = resultSet.getInt("Id"),
                        problemId = resultSet.getInt("ProblemId"),
                        input = resultSet.getString("Input"),
                        expectedOutput = resultSet.getString("ExpectedOutput"),
                        userOutput = ""
                    )
                )
            }
        }
        return testCases
    }


    // Function to update a test case
    fun updateTestCase(testCase: TestCase) {
        val sql = """
            UPDATE TestCases
            SET ProblemId = ${testCase.problemId},
                Input = '${testCase.input}',
                ExpectedOutput = '${testCase.expectedOutput}'
            WHERE Id = ${testCase.id}
        """.trimIndent()
        executeStatement(sql)
    }

    // Function to delete a test case
    fun deleteTestCase(id: Int) {
        val sql = "DELETE FROM TestCases WHERE Id = $id"
        executeStatement(sql)
    }
}
