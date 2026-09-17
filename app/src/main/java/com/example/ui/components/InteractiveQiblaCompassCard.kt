package com.example.ui.components

import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.util.rememberCompassSensorState
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Real-Time Qibla Compass Card for Prayer Times Screen and Dedicated Views.
 * Features:
 * - Live device orientation tracking via hardware magnetometer and rotation vector sensors.
 * - Dynamic 360° rotating compass dial with N, S, E, W and degree markers.
 * - Kaaba pointer with real-time target alignment detection.
 * - Haptic vibration feedback upon exact Kaaba alignment (±4°).
 * - Full-screen interactive dialog view.
 * - Calibration warning alert.
 */
@Composable
fun InteractiveQiblaCompassCard(
    qiblaBearingDeg: Float,
    cityName: String,
    onDetectLocation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isFullScreenOpen by remember { mutableStateOf(false) }
    var hapticEnabled by remember { mutableStateOf(true) }

    val sensorState by rememberCompassSensorState(
        targetQiblaBearing = qiblaBearingDeg,
        hapticFeedbackEnabled = hapticEnabled
    )

    val isAligned = sensorState.isAligned
    val currentHeading = sensorState.azimuth

    val statusColor by animateColorAsState(
        targetValue = if (isAligned) Color(0xFF10B981) else Gold500,
        animationSpec = tween(250),
        label = "statusColor"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isAligned) 2.dp else 1.dp,
            color = if (isAligned) Color(0xFF10B981).copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_qibla_compass_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title, Live Indicator, Fullscreen Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Compass",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Live Qibla Compass",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (sensorState.isSensorAvailable) Color(0xFF10B981).copy(alpha = 0.18f) else Color.Gray.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (sensorState.isSensorAvailable) "LIVE SENSOR" else "STATIC MODE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sensorState.isSensorAvailable) Color(0xFF10B981) else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "$cityName • Kaaba Direction: ${String.format(Locale.US, "%.1f", qiblaBearingDeg)}°",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { isFullScreenOpen = true },
                    modifier = Modifier.testTag("open_fullscreen_compass_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Full Screen Compass",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Compass Dial Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.Center
            ) {
                CompassDialView(
                    azimuth = currentHeading,
                    qiblaBearing = qiblaBearingDeg,
                    isAligned = isAligned,
                    sizeDp = 190
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Bar Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isAligned) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAligned) Color(0xFF10B981).copy(alpha = 0.5f) else Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isAligned) Icons.Outlined.CheckCircle else Icons.Default.Navigation,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAligned) {
                                "Directly Facing Kaaba (Qibla Aligned)"
                            } else {
                                val diff = ((qiblaBearingDeg - currentHeading + 540f) % 360f - 180f)
                                val turnDir = if (diff > 0) "Turn right ${abs(diff).toInt()}°" else "Turn left ${abs(diff).toInt()}°"
                                "Current Heading: ${currentHeading.toInt()}° • $turnDir"
                            },
                            fontSize = 12.sp,
                            fontWeight = if (isAligned) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAligned) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(
                        onClick = { isFullScreenOpen = true },
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "Expand",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // Full-Screen Compass Dialog
    if (isFullScreenOpen) {
        FullScreenQiblaDialog(
            qiblaBearingDeg = qiblaBearingDeg,
            cityName = cityName,
            currentHeading = currentHeading,
            isAligned = isAligned,
            isSensorAvailable = sensorState.isSensorAvailable,
            sensorAccuracy = sensorState.accuracy,
            hapticEnabled = hapticEnabled,
            onToggleHaptic = { hapticEnabled = it },
            onDetectLocation = onDetectLocation,
            onDismiss = { isFullScreenOpen = false }
        )
    }
}

/**
 * Fullscreen immersive Qibla Compass screen.
 */
@Composable
private fun FullScreenQiblaDialog(
    qiblaBearingDeg: Float,
    cityName: String,
    currentHeading: Float,
    isAligned: Boolean,
    isSensorAvailable: Boolean,
    sensorAccuracy: Int,
    hapticEnabled: Boolean,
    onToggleHaptic: (Boolean) -> Unit,
    onDetectLocation: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("fullscreen_qibla_dialog"),
            color = Color(0xFF071B14) // Deep Islamic green canvas
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .testTag("close_fullscreen_qibla_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Real-Time Qibla Compass",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Gold400
                        )
                        Text(
                            text = "$cityName • Bearing ${String.format(Locale.US, "%.1f", qiblaBearingDeg)}°",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    IconButton(
                        onClick = onDetectLocation,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "Refresh GPS",
                            tint = Gold400
                        )
                    }
                }

                // Alignment Status Header Pill
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (isAligned) Color(0xFF10B981).copy(alpha = 0.25f) else Emerald900.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isAligned) Color(0xFF10B981) else Gold500.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAligned) Icons.Outlined.CheckCircle else Icons.Default.Mosque,
                            contentDescription = null,
                            tint = if (isAligned) Color(0xFF10B981) else Gold400,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAligned) {
                                "ALIGNED WITH KAABA (Makkah Al-Mukarramah)"
                            } else {
                                val diff = ((qiblaBearingDeg - currentHeading + 540f) % 360f - 180f)
                                if (diff > 0) "Turn right ${abs(diff).toInt()}° to face Kaaba" else "Turn left ${abs(diff).toInt()}° to face Kaaba"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAligned) Color(0xFF10B981) else Color.White
                        )
                    }
                }

                // Large Interactive Rotating Compass Dial
                Box(
                    modifier = Modifier
                        .size(310.dp)
                        .testTag("interactive_compass_dial"),
                    contentAlignment = Alignment.Center
                ) {
                    CompassDialView(
                        azimuth = currentHeading,
                        qiblaBearing = qiblaBearingDeg,
                        isAligned = isAligned,
                        sizeDp = 295
                    )
                }

                // Sensor Info & Calibration Helper
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Heading & Kaaba Info Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF10281F),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold500.copy(alpha = 0.25f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Device Heading",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${currentHeading.toInt()}°",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF10281F),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold500.copy(alpha = 0.25f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Qibla Bearing",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", qiblaBearingDeg)}°",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold400
                                )
                            }
                        }
                    }

                    // Haptic Feedback Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF10281F), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = Gold400,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Vibrate When Facing Qibla",
                                fontSize = 12.5.sp,
                                color = Color.White
                            )
                        }
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = onToggleHaptic,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Gold400,
                                checkedTrackColor = Emerald700
                            )
                        )
                    }

                    // Calibration Advice
                    if (sensorAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW || sensorAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompassCalibration,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Compass calibration needed: Wave your phone in a figure-8 motion.",
                                fontSize = 11.sp,
                                color = Color(0xFFFCD34D)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas Compass Dial:
 * - Rotates opposite of device heading (-azimuth) so that North points to geographic North.
 * - Draws degree ticks, N/S/E/W indicators.
 * - Draws a glowing Kaaba direction pointer at qiblaBearing angle.
 * - Turns vibrant emerald when aligned.
 */
@Composable
private fun CompassDialView(
    azimuth: Float,
    qiblaBearing: Float,
    isAligned: Boolean,
    sizeDp: Int
) {
    // Smooth angle interpolation to prevent needle jumping over 0°/360° boundary
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "animatedAzimuth"
    )

    val dialRotation = -animatedAzimuth
    val qiblaPointerAngle = dialRotation + qiblaBearing

    Box(
        modifier = Modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Compass Background Dial (Canvas)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 8.dp.toPx()

            // Outer golden ring
            drawCircle(
                color = if (isAligned) Color(0xFF10B981) else Gold500.copy(alpha = 0.4f),
                radius = radius,
                center = center,
                style = Stroke(width = if (isAligned) 4.dp.toPx() else 2.dp.toPx())
            )

            // Inner dark dial plate
            drawCircle(
                color = Color(0xFF0B2119),
                radius = radius - 3.dp.toPx(),
                center = center
            )

            // Rotate Canvas with device heading
            rotate(dialRotation, pivot = center) {
                // Draw 360 degree ticks (every 15 degrees, longer every 30 degrees)
                for (deg in 0 until 360 step 15) {
                    val angleRad = Math.toRadians((deg - 90).toDouble())
                    val isMajor = deg % 30 == 0
                    val isCardinal = deg % 90 == 0

                    val tickLength = when {
                        isCardinal -> 14.dp.toPx()
                        isMajor -> 10.dp.toPx()
                        else -> 5.dp.toPx()
                    }

                    val tickColor = when {
                        deg == 0 -> Color(0xFFEF4444) // North is Red
                        isCardinal -> Gold400
                        isMajor -> Color.White.copy(alpha = 0.7f)
                        else -> Color.White.copy(alpha = 0.3f)
                    }

                    val startX = (center.x + (radius - 6.dp.toPx() - tickLength) * cos(angleRad)).toFloat()
                    val startY = (center.y + (radius - 6.dp.toPx() - tickLength) * sin(angleRad)).toFloat()
                    val endX = (center.x + (radius - 6.dp.toPx()) * cos(angleRad)).toFloat()
                    val endY = (center.y + (radius - 6.dp.toPx()) * sin(angleRad)).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isCardinal) 2.5.dp.toPx() else 1.5.dp.toPx()
                    )
                }

                // Draw North Needle (Red triangle pointing up to 0°/North)
                val needlePath = Path().apply {
                    val tipX = center.x
                    val tipY = center.y - radius + 22.dp.toPx()
                    val baseLeft = Offset(center.x - 7.dp.toPx(), center.y - 12.dp.toPx())
                    val baseRight = Offset(center.x + 7.dp.toPx(), center.y - 12.dp.toPx())

                    moveTo(tipX, tipY)
                    lineTo(baseRight.x, baseRight.y)
                    lineTo(center.x, center.y)
                    lineTo(baseLeft.x, baseLeft.y)
                    close()
                }
                drawPath(needlePath, color = Color(0xFFEF4444))

                // South Needle (Silver triangle pointing down to 180°/South)
                val southPath = Path().apply {
                    val tipX = center.x
                    val tipY = center.y + radius - 22.dp.toPx()
                    val baseLeft = Offset(center.x - 7.dp.toPx(), center.y + 12.dp.toPx())
                    val baseRight = Offset(center.x + 7.dp.toPx(), center.y + 12.dp.toPx())

                    moveTo(tipX, tipY)
                    lineTo(baseRight.x, baseRight.y)
                    lineTo(center.x, center.y)
                    lineTo(baseLeft.x, baseLeft.y)
                    close()
                }
                drawPath(southPath, color = Color.White.copy(alpha = 0.6f))
            }
        }

        // 2. Cardinal Labels overlay (N, E, S, W) rotated with dial
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(dialRotation)
        ) {
            Text(
                text = "N",
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Black,
                fontSize = (sizeDp * 0.065f).sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = (sizeDp * 0.11f).dp)
            )
            Text(
                text = "E",
                color = Gold400,
                fontWeight = FontWeight.Bold,
                fontSize = (sizeDp * 0.055f).sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = (sizeDp * 0.11f).dp)
            )
            Text(
                text = "S",
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = (sizeDp * 0.055f).sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = (sizeDp * 0.11f).dp)
            )
            Text(
                text = "W",
                color = Gold400,
                fontWeight = FontWeight.Bold,
                fontSize = (sizeDp * 0.055f).sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = (sizeDp * 0.11f).dp)
            )
        }

        // 3. Kaaba Direction Pointer (Glowing Gold or Emerald Arrow pointing at Kaaba)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(qiblaPointerAngle),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = (sizeDp * 0.035f).dp)
            ) {
                // Kaaba Icon or Glowing Arrow
                Box(
                    modifier = Modifier
                        .size((sizeDp * 0.14f).dp)
                        .clip(CircleShape)
                        .background(if (isAligned) Color(0xFF10B981) else Gold500)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = "Kaaba",
                        tint = Color.White,
                        modifier = Modifier.size((sizeDp * 0.08f).dp)
                    )
                }

                // Triangular pointer downward into dial center
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = if (isAligned) Color(0xFF10B981) else Gold500,
                    modifier = Modifier
                        .size((sizeDp * 0.07f).dp)
                        .rotate(180f)
                )
            }
        }

        // 4. Center Pivot Hub
        Box(
            modifier = Modifier
                .size((sizeDp * 0.13f).dp)
                .clip(CircleShape)
                .background(if (isAligned) Color(0xFF10B981) else Gold500)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((sizeDp * 0.05f).dp)
                    .clip(CircleShape)
                    .background(Color(0xFF071B14))
            )
        }
    }
}
