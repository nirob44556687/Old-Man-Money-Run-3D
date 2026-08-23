package com.example.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.ScreenState
import com.example.game.ui.renderer.GameCanvasRenderer
import com.example.ui.theme.GameAngryRed
import com.example.ui.theme.GameDarkOrange
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameMoneyGold
import com.example.ui.theme.GameMoneyGreen
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.platform.LocalContext
import com.example.game.graphics.GameSpriteManager
import com.example.ui.theme.GameMoneyPink
import com.example.ui.theme.GameOrange

@Composable
fun MainGameScreen(
    engine: GameEngine,
    onPauseClick: () -> Unit
) {
    val context = LocalContext.current
    val spriteManager = remember { GameSpriteManager(context) }
    val renderer = remember { GameCanvasRenderer() }
    var frameTick by remember { mutableFloatStateOf(0f) }

    // 60 FPS Game Loop
    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (true) {
            withFrameNanos { now ->
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now
                engine.update(dt)
                frameTick = (frameTick + dt) % 1000f
            }
        }
    }

    // Swipe gesture detection
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // 16:9 Aspect Ratio Widescreen Game Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(16f / 9f, matchHeightConstraintsFirst = false)
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            totalDragX = 0f
                            totalDragY = 0f
                        },
                        onDragEnd = {
                            val threshold = 35f
                            if (kotlin.math.abs(totalDragY) > kotlin.math.abs(totalDragX)) {
                                if (totalDragY < -threshold) {
                                    engine.onJump()
                                } else if (totalDragY > threshold) {
                                    engine.onSlide()
                                }
                            } else {
                                if (totalDragX < -threshold) {
                                    engine.onMoveUp()
                                } else if (totalDragX > threshold) {
                                    engine.onMoveDown()
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                        }
                    )
                }
        ) {
            // Main Game Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val tick = frameTick
                renderer.render(this, engine, spriteManager)
            }

        // Top In-Game HUD
        InGameHUD(
            engine = engine,
            onPauseClick = onPauseClick,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )

        // Special Event Banner (e.g. MONEY STORM!)
        engine.currentSpecialEvent?.let { event ->
            val infiniteTransition = rememberInfiniteTransition(label = "event_pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "banner_scale"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 80.dp)
                    .scale(scale)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF1744), Color(0xFFFF9100), Color(0xFFFF1744))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${event.emoji} ${if (engine.preferences.languageIsBangla) event.titleBn else event.titleEn}",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        }

        // Chaser Distance Gauge (Bottom Center)
        ChaserDistanceGauge(
            distancePercent = engine.chaser.distancePercent,
            isBangla = engine.preferences.languageIsBangla,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (engine.preferences.onScreenButtonsEnabled) 85.dp else 16.dp)
        )

        // Optional On-Screen Touch Buttons (Large, highly responsive mobile controls)
        if (engine.preferences.onScreenButtonsEnabled) {
            OnScreenControlsHUD(
                isBangla = engine.preferences.languageIsBangla,
                onJump = { engine.onJump() },
                onSlide = { engine.onSlide() },
                onMoveUp = { engine.onMoveUp() },
                onMoveDown = { engine.onMoveDown() },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
}

@Composable
fun InGameHUD(
    engine: GameEngine,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBangla = engine.preferences.languageIsBangla

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Currency & Hearts
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Money ৳ (Pure Bangladeshi Taka)
            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .background(Color(0xEE000000), RoundedCornerShape(12.dp))
                    .border(1.5.dp, GameMoneyGreen, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("💵", fontSize = 15.sp)
                    Text(
                        text = "৳${engine.moneyCollected}",
                        color = GameMoneyGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }

            // Health Hearts ❤️❤️❤️
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (i in 1..3) {
                    val isFull = i <= engine.hearts
                    Text(
                        text = if (isFull) "❤️" else "🖤",
                        fontSize = 18.sp
                    )
                }
            }
        }

        // Center Column: Score & Distance
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "${engine.score}",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                modifier = Modifier.shadow(4.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "${engine.distanceMeters.toInt()}m",
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                if (engine.comboMultiplier > 1) {
                    Box(
                        modifier = Modifier
                            .background(GameDarkOrange, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "x${engine.comboMultiplier} COMBO",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Right Column: Level / Mode badge & Pause Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xBB1E1E2C), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF64B5F6), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (engine.isEndlessMode) (if (isBangla) "অনন্ত দৌড়" else "Endless") else "Lvl ${engine.currentLevel?.levelNumber}",
                    color = Color(0xFF90CAF9),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(42.dp)
                    .shadow(4.dp, CircleShape)
                    .background(GameOrange, CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ChaserDistanceGauge(
    distancePercent: Float,
    isBangla: Boolean,
    modifier: Modifier = Modifier
) {
    val isDanger = distancePercent < 0.35f
    val barColor = when {
        distancePercent < 0.3f -> GameAngryRed
        distancePercent < 0.6f -> GameOrange
        else -> GameMoneyGreen
    }

    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(Color(0xDD000000), RoundedCornerShape(16.dp))
            .border(
                1.5.dp,
                if (isDanger) GameAngryRed else Color(0x66FFFFFF),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "👵",
                fontSize = 16.sp
            )

            Column(modifier = Modifier.width(140.dp)) {
                Text(
                    text = if (isDanger) {
                        if (isBangla) "⚠️ বুড়ি কাছে চলে এসেছে!" else "⚠️ Chaser is right behind!"
                    } else {
                        if (isBangla) "দূরত্ব: ${(distancePercent * 100).toInt()}%" else "Distance: ${(distancePercent * 100).toInt()}%"
                    },
                    color = if (isDanger) GameAngryRed else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                LinearProgressIndicator(
                    progress = { distancePercent.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = barColor,
                    trackColor = Color(0xFF37474F)
                )
            }

            Text(
                text = "🏃‍♂️",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun OnScreenControlsHUD(
    isBangla: Boolean,
    onJump: () -> Unit,
    onSlide: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Left D-Pad Controls: Lane Up / Lane Down
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Lane Up
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF1565C0))),
                        RoundedCornerShape(16.dp)
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .clickable { onMoveUp() }
                    .testTag("lane_up_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Lane Up",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("লেন ⬆️", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Lane Down
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF1565C0))),
                        RoundedCornerShape(16.dp)
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .clickable { onMoveDown() }
                    .testTag("lane_down_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Lane Down",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("লেন ⬇️", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Right Action Controls: Jump & Slide
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Slide / Duck Button (নিচু হোন)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(8.dp, RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFFFF9800), Color(0xFFE65100))),
                        RoundedCornerShape(18.dp)
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(18.dp))
                    .clickable { onSlide() }
                    .testTag("slide_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⬇️", fontSize = 20.sp)
                    Text(
                        text = if (isBangla) "নিচু হোন" else "SLIDE",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }

            // Jump Button (Large & Primary - লাফ দিন)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF00E676), Color(0xFF00897B))),
                        RoundedCornerShape(20.dp)
                    )
                    .border(2.5.dp, Color.White, RoundedCornerShape(20.dp))
                    .clickable { onJump() }
                    .testTag("jump_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⬆️", fontSize = 22.sp)
                    Text(
                        text = if (isBangla) "লাফ দিন" else "JUMP",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
