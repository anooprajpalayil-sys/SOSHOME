package com.example.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    private val context: Context,
    private val onShakeTriggered: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var isListening = false
    private var lastShakeTime = 0L
    private var shakeCount = 0
    private var firstShakeTime = 0L

    var thresholdG: Float = 14.5f // m/s^2

    fun start() {
        if (isListening || sensorManager == null || accelerometer == null) return
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        isListening = true
    }

    fun stop() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        if (acceleration > thresholdG) {
            val now = System.currentTimeMillis()

            if (firstShakeTime == 0L) {
                firstShakeTime = now
            }

            // Window: 1.5 seconds for at least 2 strong shakes
            if (now - firstShakeTime < 1500L) {
                if (now - lastShakeTime > 250L) { // Debounce individual peaks
                    shakeCount++
                    lastShakeTime = now

                    if (shakeCount >= 2) {
                        shakeCount = 0
                        firstShakeTime = 0L
                        onShakeTriggered()
                    }
                }
            } else {
                // Window expired, reset
                firstShakeTime = now
                shakeCount = 1
                lastShakeTime = now
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
