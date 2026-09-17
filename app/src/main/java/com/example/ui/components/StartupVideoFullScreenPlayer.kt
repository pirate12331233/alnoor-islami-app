package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import kotlinx.coroutines.delay

@Composable
fun StartupVideoFullScreenPlayer(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val totalSeconds = 10
    var remainingSeconds by remember { mutableIntStateOf(totalSeconds) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isMuted by remember { mutableStateOf(false) }
    var isVideoReady by remember { mutableStateOf(false) }
    var isVideoError by remember { mutableStateOf(false) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    // Intercept back button to skip intro
    BackHandler {
        onFinished()
    }

    // Permanent 10-second startup countdown
    LaunchedEffect(Unit) {
        val intervalMs = 100L
        val totalSteps = (totalSeconds * 1000L / intervalMs).toFloat()
        var currentStep = 0

        while (currentStep < totalSteps) {
            delay(intervalMs)
            currentStep++
            progress = (currentStep / totalSteps).coerceIn(0f, 1f)
            remainingSeconds = (totalSeconds - (currentStep * intervalMs / 1000)).toInt().coerceAtLeast(0)
        }

        delay(200)
        onFinished()
    }

    // Infinite breathing and pulsing animation for the compiled 10s animated logo intro
    val infiniteTransition = rememberInfiniteTransition(label = "startup_logo_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("startup_video_player_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Built-in Video Player layer for compiled raw resource
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                    )
                    val rawResourceUri = Uri.parse("android.resource://${ctx.packageName}/raw/app_startup_video")
                    setVideoURI(rawResourceUri)

                    setOnPreparedListener { mp ->
                        mediaPlayerRef = mp
                        mp.isLooping = false
                        if (isMuted) {
                            mp.setVolume(0f, 0f)
                        } else {
                            mp.setVolume(1f, 1f)
                        }
                        isVideoReady = true
                        mp.start()
                    }

                    setOnCompletionListener {
                        onFinished()
                    }

                    setOnErrorListener { _, _, _ ->
                        isVideoError = true
                        isVideoReady = true
                        true
                    }
                }
            },
            update = {
                mediaPlayerRef?.let { mp ->
                    try {
                        if (isMuted) {
                            mp.setVolume(0f, 0f)
                        } else {
                            mp.setVolume(1f, 1f)
                        }
                    } catch (_: Exception) {}
                }
            },
            modifier = Modifier.fillMaxSize()
        )

            // Permanent 10-Second Animated Islamic Logo Video Sequence
            if (isVideoError || !isVideoReady) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF02170E),
                                    Emerald900,
                                    Color(0xFF02170E)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Radiant Background Aura
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Gold500.copy(alpha = glowAlpha * 0.4f),
                                        Emerald800.copy(alpha = glowAlpha * 0.2f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Compiled Emblem Container with dynamic pulse
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .scale(pulseScale)
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "Alnoor Islami Official Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Sacred Bismillah Calligraphy Header
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            color = Gold300,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.alpha(glowAlpha + 0.15f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "ALNOOR ISLAMI",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "ALNOOR INTERNATIONAL TRUST",
                            color = Gold400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "@AlnoorislamiMushahidat",
                            color = Gold300,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Emerald800.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold400.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Gold300,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "10-Second Welcome Sequence",
                                    color = Gold300,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Top Bar Overlay (Skip, Mute, Watermark)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Watermark badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Emerald900.copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold500.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Gold400,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Alnoor Islami",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold300
                            )
                        }
                    }

                    // Audio Mute & Skip Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Sound Toggle
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    isMuted = !isMuted
                                    mediaPlayerRef?.let { mp ->
                                        try {
                                            if (isMuted) mp.setVolume(0f, 0f) else mp.setVolume(1f, 1f)
                                        } catch (_: Exception) {}
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = "Toggle Mute",
                                    tint = Gold400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Skip Button with remaining seconds counter
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Gold500,
                            modifier = Modifier
                                .clickable { onFinished() }
                                .testTag("skip_startup_video_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Skip (${remainingSeconds}s)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald900
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = null,
                                    tint = Emerald900,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom hairline progress indicator (non-intrusive, so video animation is 100% visible)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Gold500,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
