package com.example.apptask1

import android.content.Context
import android.graphics.Rect
import android.os.*
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apptask1.databinding.ActivityMainBinding
import android.os.VibrationEffect
import android.os.Vibrator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var obstacles: List<ImageView>

    private var playerPos = 1

    private var lives = 3
    private var gameRunning = false

    private val handler = Handler(Looper.getMainLooper())
    private val tickMs =  14L
    private var fallSpeedPx = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        obstacles = listOf(
            binding.obstacleImage1,
            binding.obstacleImage2
        )

        movePlayerTo(playerPos)

        binding.btnLeft.setOnClickListener {
            if (playerPos > 0) {
                playerPos--
                movePlayerTo(playerPos)
            }
        }

        binding.btnRight.setOnClickListener {
            if (playerPos < 2) {
                playerPos++
                movePlayerTo(playerPos)
            }
        }

        startGame()
    }

    override fun onPause() {
        super.onPause()
        pauseGame()
    }

    override fun onResume() {
        super.onResume()
        resumeGame()
    }
    override fun onDestroy() {
        super.onDestroy()
        stopGame()
    }


    private fun startGame(reset: Boolean = true) {
        if (reset) {
            lives = 3
            updateHearts()

            binding.gameArea.post {
                obstacles.forEach { spawnObstacle(it) }
            }
        }

        gameRunning = true
        handler.post(gameLoop)
    }

    private fun pauseGame() {
        gameRunning = false
        handler.removeCallbacks(gameLoop)
    }

    private fun resumeGame() {
        if (lives <= 0) return

        if (!gameRunning) {
            gameRunning = true
            handler.post(gameLoop)
        }
    }

    private fun stopGame() {
        gameRunning = false
        handler.removeCallbacks(gameLoop)
    }

    private fun movePlayerTo(position: Int) {
        val params = binding.playerImage.layoutParams as RelativeLayout.LayoutParams

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        params.bottomMargin = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.removeRule(RelativeLayout.ALIGN_PARENT_START)
            params.removeRule(RelativeLayout.CENTER_HORIZONTAL)
            params.removeRule(RelativeLayout.ALIGN_PARENT_END)
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_START, 0)
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, 0)
            params.addRule(RelativeLayout.ALIGN_PARENT_END, 0)
        }

        when (position) {
            0 -> params.addRule(RelativeLayout.ALIGN_PARENT_START)
            1 -> params.addRule(RelativeLayout.CENTER_HORIZONTAL)
            2 -> params.addRule(RelativeLayout.ALIGN_PARENT_END)
        }

        binding.playerImage.layoutParams = params
    }

    private fun spawnObstacle(obstacle: ImageView) {
        val lane = (0..2).random()

        val params = obstacle.layoutParams as RelativeLayout.LayoutParams
        params.topMargin = 0
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.removeRule(RelativeLayout.ALIGN_PARENT_START)
            params.removeRule(RelativeLayout.CENTER_HORIZONTAL)
            params.removeRule(RelativeLayout.ALIGN_PARENT_END)
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_START, 0)
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, 0)
            params.addRule(RelativeLayout.ALIGN_PARENT_END, 0)
        }

        when (lane) {
            0 -> params.addRule(RelativeLayout.ALIGN_PARENT_START)
            1 -> params.addRule(RelativeLayout.CENTER_HORIZONTAL)
            2 -> params.addRule(RelativeLayout.ALIGN_PARENT_END)
        }

        obstacle.layoutParams = params
        obstacle.visibility = View.VISIBLE
    }

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!gameRunning) return

            moveObstaclesDown()
            handler.postDelayed(this, tickMs)
        }
    }

    private fun moveObstaclesDown() {
        for (obstacle in obstacles) {
            val params = obstacle.layoutParams as RelativeLayout.LayoutParams
            params.topMargin += fallSpeedPx
            obstacle.layoutParams = params

            if (checkCollision(obstacle)) {
                onCrash(obstacle)
                return
            }

            if (obstacle.bottom > binding.gameArea.height) {
                spawnObstacle(obstacle)
            }
        }
    }

    private fun checkCollision(obstacle: ImageView): Boolean {
        if (obstacle.visibility != View.VISIBLE) return false

        val playerRect = Rect()
        val obstacleRect = Rect()
        binding.playerImage.getHitRect(playerRect)
        obstacle.getHitRect(obstacleRect)

        return Rect.intersects(playerRect, obstacleRect)
    }

    private fun onCrash(hitObstacle: ImageView) {

        hitObstacle.visibility = View.INVISIBLE

        lives--
        updateHearts()

        Toast.makeText(this, "You've got hit", Toast.LENGTH_SHORT).show()
        vibrateOnce()

        if (lives <= 0) {
            Toast.makeText(this, "Game Over - Starting again", Toast.LENGTH_SHORT).show()
            lives = 3
            updateHearts()
        }

        spawnObstacle(hitObstacle)
    }


    private fun updateHearts() {
        binding.heart1.visibility = if (lives >= 1) View.VISIBLE else View.INVISIBLE
        binding.heart2.visibility = if (lives >= 2) View.VISIBLE else View.INVISIBLE
        binding.heart3.visibility = if (lives >= 3) View.VISIBLE else View.INVISIBLE
    }


    private fun vibrateOnce() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    150,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(150)
        }
    }
}
