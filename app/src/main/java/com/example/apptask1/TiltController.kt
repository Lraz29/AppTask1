package com.example.apptask1

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class TiltController(
    context: Context,
    private val tiltThreshold: Float = 2.2f,
    private val moveCooldownMs: Long = 180L,
    private val onLeft: () -> Unit,
    private val onRight: () -> Unit
) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastMoveTime = 0L
    private var running = false

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!running) return
            if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

            val now = System.currentTimeMillis()
            if (now - lastMoveTime < moveCooldownMs) return

            val x = event.values[0]

            when {
                x > tiltThreshold -> {
                    onLeft()
                    lastMoveTime = now
                }
                x < -tiltThreshold -> {
                    onRight()
                    lastMoveTime = now
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun start() {
        if (running) return
        running = true
        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        if (!running) return
        running = false
        sensorManager.unregisterListener(listener)
    }

    fun hasSensor(): Boolean = accelerometer != null
}
