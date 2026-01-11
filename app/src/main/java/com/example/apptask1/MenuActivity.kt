package com.example.apptask1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MenuActivity : AppCompatActivity() {
    private fun startGame(mode: String) {
        val i = Intent(this, MainActivity::class.java)
        i.putExtra("control_mode", mode)
        startActivity(i)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.menu_activity)

        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }
        Handler(Looper.getMainLooper()).postDelayed({ keepSplash = false }, 1000)

        findViewById<View>(R.id.btnModeBeginner).setOnClickListener {
            startGame("BUTTONS_SLOW")
        }
        findViewById<View>(R.id.btnModeHard).setOnClickListener {
            startGame("BUTTONS_FAST")
        }
        findViewById<View>(R.id.btnModeTilt).setOnClickListener {
            startGame("TILT")
        }

        findViewById<View>(R.id.btnScore).setOnClickListener {
            startActivity(Intent(this, ScoresActivity::class.java))
        }
    }

}
