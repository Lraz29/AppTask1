package com.example.apptask1

class GameManager(private val rows: Int, private val cols: Int) {

    private var gameCallback: GameCallback? = null

    var lives = 3
        private set

    var studentCol = 1
        private set

    private var verifyCount = 0

    private val matrix = Array(rows) { IntArray(cols) { 0 } }

    fun setGameCallback(callback: GameCallback) {
        this.gameCallback = callback
    }

    fun moveObstacles() {
        for (r in rows - 1 downTo 1) {
            for (c in 0 until cols) {
                matrix[r][c] = matrix[r - 1][c]
            }
        }
        for (c in 0 until cols) matrix[0][c] = 0
    }

    fun spawnItem(col: Int, isVerify: Boolean) {
        matrix[0][col] = if (isVerify) 2 else 1
    }

    fun moveStudent(toLeft: Boolean) {
        if (toLeft && studentCol > 0) studentCol--
        else if (!toLeft && studentCol < cols - 1) studentCol++
    }

    fun getCell(r: Int, c: Int): Int = matrix[r][c]

    fun checkCollision() {
        val bottomRow = rows - 1
        val cell = matrix[bottomRow][studentCol]
        if (cell == 0) return

        // clear so it won’t trigger again
        matrix[bottomRow][studentCol] = 0

        when (cell) {
            1 -> { // BUG
                lives--
                gameCallback?.onCrash(lives)

                if (lives <= 0) {
                    // reset for endless mode
                    lives = 3
                    verifyCount = 0

                    gameCallback?.onGameOver()
                    gameCallback?.onLivesChanged(lives)
                    gameCallback?.onVerifyCollected(verifyCount)
                }
            }

            2 -> { // VERIFY (GOOD)
                verifyCount++
                gameCallback?.onVerifyCollected(verifyCount)
            }
        }
    }
}
