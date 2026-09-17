package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs

/**
 * Real-time orientation sensor state for interactive Qibla Compass.
 *
 * @param azimuth Current device compass heading in degrees (0° = North, 90° = East, 180° = South, 270° = West)
 * @param isAligned True when the device is pointing directly towards the Kaaba (within threshold, default ±4°)
 * @param accuracy Sensor accuracy rating (SensorManager.SENSOR_STATUS_ACCURACY_HIGH / MEDIUM / LOW / UNRELIABLE)
 * @param isSensorAvailable Whether magnetic orientation hardware is available on the device
 */
data class CompassSensorState(
    val azimuth: Float = 0f,
    val isAligned: Boolean = false,
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val isSensorAvailable: Boolean = true
)

/**
 * Remember and observe device orientation in real-time.
 * Handles sensor registration, low-pass smoothing filter to avoid jitter,
 * alignment detection with Kaaba bearing, and subtle haptic feedback upon alignment.
 */
@Composable
fun rememberCompassSensorState(
    targetQiblaBearing: Float,
    hapticFeedbackEnabled: Boolean = true,
    alignmentThresholdDeg: Float = 4.0f
): State<CompassSensorState> {
    val context = LocalContext.current
    val compassState = remember { mutableStateOf(CompassSensorState()) }

    DisposableEffect(context, targetQiblaBearing, hapticFeedbackEnabled, alignmentThresholdDeg) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (sensorManager == null) {
            compassState.value = compassState.value.copy(isSensorAvailable = false)
            return@DisposableEffect onDispose {}
        }

        // We use TYPE_ROTATION_VECTOR for high accuracy fused orientation if available,
        // otherwise fallback to TYPE_ACCELEROMETER + TYPE_MAGNETIC_FIELD.
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometerSensor = if (rotationVectorSensor == null) sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) else null
        val magneticSensor = if (rotationVectorSensor == null) sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) else null

        val hasSensors = rotationVectorSensor != null || (accelerometerSensor != null && magneticSensor != null)
        if (!hasSensors) {
            compassState.value = compassState.value.copy(isSensorAvailable = false)
            return@DisposableEffect onDispose {}
        }

        var lastVibratedAligned = false
        var smoothedAzimuth = 0f

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        val lastAccelerometer = FloatArray(3)
        val lastMagnetometer = FloatArray(3)
        var lastAccelerometerSet = false
        var lastMagnetometerSet = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                var newAzimuth: Float? = null

                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val rawDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    newAzimuth = (rawDeg + 360f) % 360f
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                    lastAccelerometerSet = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                    lastMagnetometerSet = true
                }

                if (newAzimuth == null && lastAccelerometerSet && lastMagnetometerSet) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        val rawDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        newAzimuth = (rawDeg + 360f) % 360f
                    }
                }

                if (newAzimuth != null) {
                    // Angular interpolation / Low-pass filter to eliminate needle jitter
                    val diff = (newAzimuth - smoothedAzimuth + 540f) % 360f - 180f
                    smoothedAzimuth = (smoothedAzimuth + diff * 0.18f + 360f) % 360f

                    // Calculate shortest angular distance to target Qibla bearing
                    val angleDifference = abs((smoothedAzimuth - targetQiblaBearing + 540f) % 360f - 180f)
                    val aligned = angleDifference <= alignmentThresholdDeg

                    // Trigger gentle haptic vibration on edge transition (entering alignment)
                    if (aligned && !lastVibratedAligned && hapticFeedbackEnabled) {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator?.vibrate(
                                    VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE)
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator?.vibrate(45L)
                            }
                        } catch (_: Exception) {
                            // Non-critical vibration fail-safe
                        }
                    }
                    lastVibratedAligned = aligned

                    compassState.value = CompassSensorState(
                        azimuth = smoothedAzimuth,
                        isAligned = aligned,
                        accuracy = event.accuracy,
                        isSensorAvailable = true
                    )
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                compassState.value = compassState.value.copy(accuracy = accuracy)
            }
        }

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometerSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
            magneticSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return compassState
}
