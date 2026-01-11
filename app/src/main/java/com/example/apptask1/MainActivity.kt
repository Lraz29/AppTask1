package com.example.apptask1

import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.apptask1.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.random.Random
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity(), GameCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gameManager: GameManager
    private lateinit var uiMatrix: Array<Array<ImageView>>
    private lateinit var signalManager: SignalManager
    private lateinit var tiltController: TiltController
    private lateinit var audioManager: AudioManager
    private lateinit var scoresRepo: ScoresRepository

    private val rows = 12
    private val cols = 5

    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private val slowTickMs = 800L
    private val fastTickMs = 400L
    private val tiltTickMs = 600L
    private var tickMs = 600L

    private var distanceMeters = 0
    private var gameOverHandled = false

    private enum class ControlMode { BUTTONS_SLOW, BUTTONS_FAST, TILT }
    private var controlMode: ControlMode = ControlMode.BUTTONS_SLOW

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!running) return

            gameManager.moveObstacles()

            distanceMeters += 1
            binding.txtOdometer.text = "${distanceMeters} m"

            if (Random.nextInt(100) < 15) {
                val col = Random.nextInt(cols)
                val isVerify = Random.nextInt(100) < 25
                gameManager.spawnItem(col, isVerify)
            }

            gameManager.checkCollision()
            refreshUI()

            handler.postDelayed(this, tickMs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestLocationPermissionIfNeeded()

        scoresRepo = ScoresRepository(this)
        signalManager = SignalManager(this)
        audioManager = AudioManager(this)

        gameManager = GameManager(rows, cols).apply { setGameCallback(this@MainActivity) }
        gameOverHandled = false

        initUiGrid()
        initButtons()
        initTiltController()

        updateHeartsUI()
        refreshUI()

        controlMode = parseModeFromIntent()
        applyMode()
    }

    override fun onResume() {
        super.onResume()
        applyMode()
        startGameLoop()
        audioManager.startBackground()
    }

    override fun onPause() {
        super.onPause()
        stopGameLoop()
        tiltController.stop()
        audioManager.pauseBackground()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.release()
    }

    override fun onCrash(livesLeft: Int) {
        signalManager.toast("Crash!")
        signalManager.vibrate()
        audioManager.playCrash()
        updateHeartsUI()

        if (livesLeft <= 0) onGameOver()
    }

    override fun onGameOver() {
        if (gameOverHandled) return
        gameOverHandled = true

        stopGameLoop()
        tiltController.stop()
        audioManager.pauseBackground()

        saveScoreWithLocation(distanceMeters)

        signalManager.toast("Game over!")

        Handler(Looper.getMainLooper()).postDelayed({
            distanceMeters = 0
            binding.txtOdometer.text = "0 m"
            finish()
        }, 800)
    }

    override fun onLivesChanged(lives: Int) {
        updateHeartsUI()
    }

    override fun onVerifyCollected(count: Int) {
        binding.txtVerifyCount.text = count.toString()
        audioManager.playVerify()
    }

    private fun parseModeFromIntent(): ControlMode {
        val modeFromMenu = intent.getStringExtra("control_mode")
        return when (modeFromMenu) {
            ControlMode.BUTTONS_SLOW.name -> ControlMode.BUTTONS_SLOW
            ControlMode.BUTTONS_FAST.name -> ControlMode.BUTTONS_FAST
            ControlMode.TILT.name -> ControlMode.TILT
            else -> ControlMode.BUTTONS_SLOW
        }
    }

    private fun applyMode() {
        tickMs = when (controlMode) {
            ControlMode.BUTTONS_SLOW -> slowTickMs
            ControlMode.BUTTONS_FAST -> fastTickMs
            ControlMode.TILT -> tiltTickMs
        }

        val showButtons = (controlMode != ControlMode.TILT)
        binding.btnLeft.visibility = if (showButtons) View.VISIBLE else View.GONE
        binding.btnRight.visibility = if (showButtons) View.VISIBLE else View.GONE

        tiltController.stop()
        if (controlMode == ControlMode.TILT) tiltController.start()
    }

    private fun startGameLoop() {
        if (running) return
        running = true
        handler.post(gameLoop)
    }

    private fun stopGameLoop() {
        running = false
        handler.removeCallbacks(gameLoop)
    }

    private fun requestLocationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }

    private fun saveScoreWithLocation(score: Int) {
        fun save(lat: Double, lng: Double) {
            scoresRepo.addRecord(
                ScoreRecord(
                    score = score,
                    timeMillis = System.currentTimeMillis(),
                    lat = lat,
                    lng = lng
                )
            )
        }

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            save(32.0853, 34.7818)
            return
        }

        val client = LocationServices.getFusedLocationProviderClient(this)

        client.lastLocation
            .addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    save(loc.latitude, loc.longitude)
                } else {
                    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { fresh ->
                            if (fresh != null) save(fresh.latitude, fresh.longitude)
                            else save(32.0853, 34.7818)
                        }
                        .addOnFailureListener { save(32.0853, 34.7818) }
                }
            }
            .addOnFailureListener { save(32.0853, 34.7818) }
    }

    private fun initTiltController() {
        tiltController = TiltController(
            context = this,
            tiltThreshold = 2.2f,
            moveCooldownMs = 180L,
            onLeft = {
                if (controlMode != ControlMode.TILT) return@TiltController
                gameManager.moveStudent(true)
                refreshUI()
            },
            onRight = {
                if (controlMode != ControlMode.TILT) return@TiltController
                gameManager.moveStudent(false)
                refreshUI()
            }
        )
    }

    private fun initUiGrid() {
        uiMatrix = arrayOf(
            arrayOf(binding.mainIMG00, binding.mainIMG01, binding.mainIMG02, binding.mainIMG03, binding.mainIMG04),
            arrayOf(binding.mainIMG10, binding.mainIMG11, binding.mainIMG12, binding.mainIMG13, binding.mainIMG14),
            arrayOf(binding.mainIMG20, binding.mainIMG21, binding.mainIMG22, binding.mainIMG23, binding.mainIMG24),
            arrayOf(binding.mainIMG30, binding.mainIMG31, binding.mainIMG32, binding.mainIMG33, binding.mainIMG34),
            arrayOf(binding.mainIMG40, binding.mainIMG41, binding.mainIMG42, binding.mainIMG43, binding.mainIMG44),
            arrayOf(binding.mainIMG50, binding.mainIMG51, binding.mainIMG52, binding.mainIMG53, binding.mainIMG54),
            arrayOf(binding.mainIMG60, binding.mainIMG61, binding.mainIMG62, binding.mainIMG63, binding.mainIMG64),
            arrayOf(binding.mainIMG70, binding.mainIMG71, binding.mainIMG72, binding.mainIMG73, binding.mainIMG74),
            arrayOf(binding.mainIMG80, binding.mainIMG81, binding.mainIMG82, binding.mainIMG83, binding.mainIMG84),
            arrayOf(binding.mainIMG90, binding.mainIMG91, binding.mainIMG92, binding.mainIMG93, binding.mainIMG94),
            arrayOf(binding.mainIMG100, binding.mainIMG101, binding.mainIMG102, binding.mainIMG103, binding.mainIMG104),
            arrayOf(binding.mainIMG110, binding.mainIMG111, binding.mainIMG112, binding.mainIMG113, binding.mainIMG114),
        )
    }

    private fun initButtons() {
        binding.btnLeft.setOnClickListener {
            if (controlMode == ControlMode.TILT) return@setOnClickListener
            gameManager.moveStudent(true)
            refreshUI()
        }

        binding.btnRight.setOnClickListener {
            if (controlMode == ControlMode.TILT) return@setOnClickListener
            gameManager.moveStudent(false)
            refreshUI()
        }
    }

    private fun updateHeartsUI() {
        val hearts = arrayOf(binding.heart1, binding.heart2, binding.heart3)
        for (i in hearts.indices) {
            hearts[i].visibility = if (i < gameManager.lives) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun refreshUI() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = gameManager.getCell(r, c)
                uiMatrix[r][c].apply {
                    when (cell) {
                        1 -> {
                            setImageResource(R.drawable.ic_bug)
                            visibility = View.VISIBLE
                        }
                        2 -> {
                            setImageResource(R.drawable.ic_verified)
                            visibility = View.VISIBLE
                        }
                        else -> visibility = View.INVISIBLE
                    }
                }
            }
        }

        for (c in 0 until cols) uiMatrix[rows - 1][c].visibility = View.INVISIBLE

        uiMatrix[rows - 1][gameManager.studentCol].apply {
            setImageResource(R.drawable.ic_student)
            visibility = View.VISIBLE
        }
    }
}
