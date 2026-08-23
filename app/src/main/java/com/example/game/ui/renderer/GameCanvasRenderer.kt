package com.example.game.ui.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.game.engine.GameEngine
import com.example.game.graphics.GameSpriteManager
import com.example.game.model.Collectible
import com.example.game.model.CollectibleType
import com.example.game.model.EnvironmentTheme
import com.example.game.model.Obstacle
import com.example.game.model.ObstacleType
import com.example.game.model.Particle
import com.example.game.model.ParticleShape
import com.example.game.model.PowerUpItem
import com.example.game.model.PowerUpType
import com.example.ui.theme.GameCurbRed
import com.example.ui.theme.GameGrassGreen
import com.example.ui.theme.GameMoneyGold
import com.example.ui.theme.GameMoneyGreen
import com.example.ui.theme.GameMoneyPink
import com.example.ui.theme.GameOrange
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class GameCanvasRenderer {

    fun render(drawScope: DrawScope, engine: GameEngine, spriteManager: GameSpriteManager? = null) {
        val width = drawScope.size.width
        val height = drawScope.size.height
        val scaleX = width / engine.worldWidth
        val scaleY = height / engine.worldHeight

        drawScope.translate(engine.shakeOffsetX * scaleX, engine.shakeOffsetY * scaleY) {
            // Background Sky & Parallax Scenery (using provided Road Background asset if present)
            drawSkyAndScenery(this, engine, scaleX, scaleY, spriteManager)

            // Road, Markings, Curbs
            drawRoadAndLanes(this, engine, scaleX, scaleY)

            // Collectibles & PowerUp items on Road
            drawCollectibles(this, engine, scaleX, scaleY)
            drawPowerUps(this, engine, scaleX, scaleY)

            // Obstacles
            drawObstacles(this, engine, scaleX, scaleY)

            // Particles (Dust, Sparkles, Flying Money)
            drawParticles(this, engine, scaleX, scaleY)

            // Characters (Old Woman Chaser, Old Man Player)
            drawChaserWoman(this, engine, scaleX, scaleY, spriteManager)
            drawOldManPlayer(this, engine, scaleX, scaleY, spriteManager)

            // Weather effects (Raindrops, Night glow)
            drawWeatherOverlay(this, engine, scaleX, scaleY)
        }
    }

    private fun drawSkyAndScenery(drawScope: DrawScope, engine: GameEngine, sx: Float, sy: Float, spriteManager: GameSpriteManager? = null) {
        val bgBmp = spriteManager?.roadBgBitmap
        if (bgBmp != null) {
            val bgWidth = drawScope.size.width
            val bgHeight = drawScope.size.height
            val scrollX = ((engine.distanceMeters * 22f) % bgWidth).toInt()

            drawScope.drawImage(
                image = bgBmp,
                dstOffset = IntOffset(-scrollX, 0),
                dstSize = IntSize(bgWidth.toInt() + 2, bgHeight.toInt())
            )
            drawScope.drawImage(
                image = bgBmp,
                dstOffset = IntOffset(bgWidth.toInt() - scrollX, 0),
                dstSize = IntSize(bgWidth.toInt() + 2, bgHeight.toInt())
            )
            return
        }

        val theme = engine.currentTheme
        val horizonY = 340f * sy

        // Sky Gradient
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(theme.skyColorTop, theme.skyColorBottom),
                startY = 0f,
                endY = horizonY
            ),
            size = Size(drawScope.size.width, horizonY)
        )

        // Sun or Moon
        if (theme.isNight) {
            drawScope.drawCircle(
                color = Color(0x33FFF59D),
                radius = 45f * sx,
                center = Offset(1050f * sx, 80f * sy)
            )
            drawScope.drawCircle(
                color = Color(0xFFFFF9C4),
                radius = 32f * sx,
                center = Offset(1050f * sx, 80f * sy)
            )
            for (i in 0..12) {
                val starX = ((i * 110 + 40) % 1200) * sx
                val starY = ((i * 47 + 25) % 180) * sy
                val twinkle = (sin(engine.player.runAnimTime * 3f + i) * 0.4f + 0.6f)
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = twinkle),
                    radius = 2.5f * sx,
                    center = Offset(starX, starY)
                )
            }
        } else {
            val sunCenter = Offset(1100f * sx, 75f * sy)
            val sunRadius = 38f * sx
            drawScope.drawCircle(
                color = Color(0x44FFD54F),
                radius = sunRadius * 1.6f,
                center = sunCenter
            )
            drawScope.drawCircle(
                color = Color(0xFFFFD54F),
                radius = sunRadius,
                center = sunCenter
            )
            drawScope.drawCircle(
                color = Color(0xFFFFF59D),
                radius = sunRadius * 0.75f,
                center = sunCenter
            )
        }

        // Parallax Layer 1: Silhouette
        val bgOffset1 = (engine.distanceMeters * 8f) % 600f
        for (i in -1..4) {
            val bx = (i * 350f - bgOffset1) * sx
            val buildingH = (90f + (i % 3) * 35f) * sy
            val col = if (theme.isNight) Color(0xFF15193B) else Color(0xFFB0BEC5).copy(alpha = 0.5f)
            drawScope.drawRect(
                color = col,
                topLeft = Offset(bx, horizonY - buildingH),
                size = Size(160f * sx, buildingH)
            )
        }

        // Parallax Layer 2: Clouds
        val cloudOffset = (engine.distanceMeters * 14f) % 900f
        val cloudColor = if (theme.isNight) Color(0x33FFFFFF) else Color(0xDDFFFFFF)
        for (c in 0..3) {
            val cx = ((c * 320f) - cloudOffset + 1280f) % 1280f * sx
            val cy = (50f + (c % 2) * 45f) * sy
            drawScope.drawCircle(cloudColor, 28f * sx, Offset(cx, cy))
            drawScope.drawCircle(cloudColor, 36f * sx, Offset(cx + 25f * sx, cy - 8f * sy))
            drawScope.drawCircle(cloudColor, 30f * sx, Offset(cx + 52f * sx, cy))
            drawScope.drawCircle(cloudColor, 22f * sx, Offset(cx + 70f * sx, cy + 4f * sy))
        }

        // Parallax Layer 3: Trees & Street Lights along roadside
        val treeOffset = (engine.distanceMeters * 26f) % 800f
        for (t in -1..5) {
            val tx = (t * 260f - treeOffset) * sx
            val ty = horizonY - 15f * sy

            if (t % 2 == 0) {
                // Cartoon Mango/Banyan Tree
                val trunkCol = Color(0xFF5D4037)
                drawScope.drawRect(
                    color = trunkCol,
                    topLeft = Offset(tx + 22f * sx, ty - 65f * sy),
                    size = Size(14f * sx, 75f * sy)
                )
                val leafCol = if (theme.isNight) Color(0xFF1B5E20) else Color(0xFF388E3C)
                drawScope.drawCircle(leafCol, 36f * sx, Offset(tx + 30f * sx, ty - 75f * sy))
                drawScope.drawCircle(leafCol.copy(alpha = 0.85f), 28f * sx, Offset(tx + 12f * sx, ty - 60f * sy))
                drawScope.drawCircle(leafCol.copy(alpha = 0.9f), 30f * sx, Offset(tx + 48f * sx, ty - 60f * sy))
            } else {
                // Street Light Pole
                val poleCol = Color(0xFF37474F)
                drawScope.drawLine(
                    color = poleCol,
                    start = Offset(tx + 15f * sx, ty + 10f * sy),
                    end = Offset(tx + 15f * sx, ty - 90f * sy),
                    strokeWidth = 5f * sx,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = poleCol,
                    start = Offset(tx + 15f * sx, ty - 90f * sy),
                    end = Offset(tx + 38f * sx, ty - 85f * sy),
                    strokeWidth = 4f * sx,
                    cap = StrokeCap.Round
                )
                // Light Glow
                val lightGlow = if (theme.isNight) Color(0x66FFEE58) else Color(0x33FFF59D)
                drawScope.drawCircle(lightGlow, 22f * sx, Offset(tx + 38f * sx, ty - 80f * sy))
                drawScope.drawCircle(Color(0xFFFFEB3B), 7f * sx, Offset(tx + 38f * sx, ty - 80f * sy))
            }
        }
    }

    private fun drawRoadAndLanes(drawScope: DrawScope, engine: GameEngine, sx: Float, sy: Float) {
        val roadTop = 340f * sy
        val roadBottom = 660f * sy
        val roadHeight = roadBottom - roadTop

        // Roadside Grass Strip (Top and Bottom)
        drawScope.drawRect(
            color = GameGrassGreen,
            topLeft = Offset(0f, roadTop - 25f * sy),
            size = Size(drawScope.size.width, 25f * sy)
        )
        drawScope.drawRect(
            color = GameGrassGreen,
            topLeft = Offset(0f, roadBottom),
            size = Size(drawScope.size.width, drawScope.size.height - roadBottom)
        )

        // Red & White Striped Curb Stone Border (Top & Bottom)
        val curbOffset = (engine.distanceMeters * 35f) % 60f
        val curbH = 14f * sy
        for (i in -1..25) {
            val cx = (i * 60f - curbOffset) * sx
            val isRed = (i % 2 == 0)
            val curbCol = if (isRed) GameCurbRed else Color.White

            // Top Curb
            drawScope.drawRect(
                color = curbCol,
                topLeft = Offset(cx, roadTop - curbH),
                size = Size(30f * sx, curbH)
            )
            // Bottom Curb
            drawScope.drawRect(
                color = curbCol,
                topLeft = Offset(cx, roadBottom),
                size = Size(30f * sx, curbH)
            )
        }

        // Main Dark Asphalt Road Surface
        drawScope.drawRect(
            color = engine.currentTheme.roadColor,
            topLeft = Offset(0f, roadTop),
            size = Size(drawScope.size.width, roadHeight)
        )

        // Road Lane Dividers (Dashed White & Yellow Markings)
        val lane1Y = 445f * sy
        val lane2Y = 550f * sy
        val dashOffset = (engine.distanceMeters * 45f) % 80f

        for (i in -1..20) {
            val dx = (i * 80f - dashOffset) * sx
            // Lane 1 divider
            drawScope.drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(dx, lane1Y),
                end = Offset(dx + 45f * sx, lane1Y),
                strokeWidth = 4.5f * sy,
                cap = StrokeCap.Round
            )
            // Lane 2 divider (Yellow line)
            drawScope.drawLine(
                color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                start = Offset(dx, lane2Y),
                end = Offset(dx + 45f * sx, lane2Y),
                strokeWidth = 4.5f * sy,
                cap = StrokeCap.Round
            )
        }
    }

    private fun drawCollectibles(drawScope: DrawScope, engine: GameEngine, sx: Float, sy: Float) {
        for (col in engine.collectibles) {
            val cx = col.x * sx
            val cy = (engine.laneY[col.lane] + col.yOffset) * sy
            val animBob = sin(col.animOffset) * 4f * sy

            when (col.type) {
                CollectibleType.NOTE_100, CollectibleType.NOTE_500, CollectibleType.NOTE_1000 -> {
                    // Fluttering Bangladeshi Taka Paper Note (৳100 Cyan, ৳500 Green, ৳1000 Pink)
                    val noteCol = when (col.type) {
                        CollectibleType.NOTE_100 -> Color(0xFF0091EA)
                        CollectibleType.NOTE_500 -> GameMoneyGreen
                        else -> GameMoneyPink
                    }
                    val noteLight = when (col.type) {
                        CollectibleType.NOTE_100 -> Color(0xFF80D8FF)
                        CollectibleType.NOTE_500 -> Color(0xFFB9F6CA)
                        else -> Color(0xFFFF80AB)
                    }
                    val noteW = 46f * sx
                    val noteH = 26f * sy
                    val rot = sin(col.animOffset * 2f) * 10f

                    drawScope.rotate(rot, pivot = Offset(cx, cy + animBob)) {
                        // Note glow
                        drawScope.drawRoundRect(
                            color = noteCol.copy(alpha = 0.35f),
                            topLeft = Offset(cx - noteW / 2f - 3f * sx, cy - noteH / 2f - 3f * sy + animBob),
                            size = Size(noteW + 6f * sx, noteH + 6f * sy),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * sx, 6f * sy)
                        )
                        // Note body
                        drawScope.drawRoundRect(
                            color = noteCol,
                            topLeft = Offset(cx - noteW / 2f, cy - noteH / 2f + animBob),
                            size = Size(noteW, noteH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * sx, 4f * sy)
                        )
                        // Security border
                        drawScope.drawRoundRect(
                            color = noteLight,
                            topLeft = Offset(cx - noteW / 2f + 3f * sx, cy - noteH / 2f + 3f * sy + animBob),
                            size = Size(noteW - 6f * sx, noteH - 6f * sy),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * sx, 2f * sy),
                            style = Stroke(width = 1.5f * sx)
                        )
                        // Center ৳ mark
                        drawScope.drawCircle(
                            color = Color.White.copy(alpha = 0.95f),
                            radius = 6f * sx,
                            center = Offset(cx, cy + animBob)
                        )
                        drawScope.drawCircle(
                            color = noteCol,
                            radius = 3.5f * sx,
                            center = Offset(cx, cy + animBob)
                        )
                    }
                }

                CollectibleType.BUNDLE_5000 -> {
                    // Stack of cash notes with gold band (৳5000 Bundle)
                    val bW = 48f * sx
                    val bH = 30f * sy
                    val rot = sin(col.animOffset * 1.5f) * 6f

                    drawScope.rotate(rot, pivot = Offset(cx, cy + animBob)) {
                        drawScope.drawRoundRect(
                            color = Color(0xFF00C853),
                            topLeft = Offset(cx - bW / 2f, cy - bH / 2f + animBob),
                            size = Size(bW, bH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f * sx, 5f * sy)
                        )
                        // Gold ribbon band
                        drawScope.drawRect(
                            color = GameMoneyGold,
                            topLeft = Offset(cx - 8f * sx, cy - bH / 2f + animBob),
                            size = Size(16f * sx, bH)
                        )
                    }
                }

                CollectibleType.LARGE_BAG_10000 -> {
                    // Big Golden Money Bag (৳10000)
                    val bagR = 26f * sx
                    drawScope.drawCircle(
                        color = Color(0x66FFD54F),
                        radius = bagR * 1.3f,
                        center = Offset(cx, cy + animBob)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFFFFB300),
                        radius = bagR,
                        center = Offset(cx, cy + animBob + 4f * sy)
                    )
                    // Bag tie neck
                    drawScope.drawRect(
                        color = Color(0xFFE65100),
                        topLeft = Offset(cx - 8f * sx, cy - bagR + animBob),
                        size = Size(16f * sx, 8f * sy)
                    )
                    // ৳ mark
                    drawScope.drawCircle(
                        color = Color.White,
                        radius = 8f * sx,
                        center = Offset(cx, cy + animBob + 4f * sy)
                    )
                }
            }
        }
    }

    private fun drawPowerUps(drawScope: DrawScope, engine: GameEngine, sx: Float, sy: Float) {
        for (pw in engine.powerUpItems) {
            val px = pw.x * sx
            val py = (engine.laneY[pw.lane] + pw.yOffset) * sy
            val animBob = sin(pw.animOffset) * 6f * sy
            val r = 24f * sx

            // Glowing Power-up Bubble Aura
            drawScope.drawCircle(
                color = pw.type.color.copy(alpha = 0.35f),
                radius = r * 1.5f,
                center = Offset(px, py + animBob)
            )
            drawScope.drawCircle(
                color = pw.type.color,
                radius = r,
                center = Offset(px, py + animBob)
            )
            drawScope.drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = r * 0.7f,
                center = Offset(px - 4f * sx, py - 4f * sy + animBob)
            )
        }
    }

    private fun drawObstacles(drawScope: DrawScope, engine: GameEngine, sx: Float, sy: Float) {
        for (ob in engine.obstacles) {
            val ox = ob.x * sx
            val oy = engine.laneY[ob.lane] * sy
            val w = ob.type.width * sx
            val h = ob.type.height * sy

            when (ob.type) {
                ObstacleType.LOW_BARRIER -> {
                    // Construction Road Barrier (Yellow & Black striped)
                    val barH = h * 0.7f
                    drawScope.drawRect(
                        color = Color(0xFFFFD600),
                        topLeft = Offset(ox - w / 2f, oy - barH + 10f * sy),
                        size = Size(w, barH)
                    )
                    // Black diagonal stripes
                    val stripeW = 12f * sx
                    for (s in 0..4) {
                        val sxPos = ox - w / 2f + s * stripeW * 2f
                        drawScope.drawRect(
                            color = Color.Black,
                            topLeft = Offset(sxPos, oy - barH + 10f * sy),
                            size = Size(stripeW, barH)
                        )
                    }
                    // Stand legs
                    drawScope.drawLine(
                        color = Color(0xFF424242),
                        start = Offset(ox - w / 2f + 8f * sx, oy + 10f * sy),
                        end = Offset(ox - w / 2f - 4f * sx, oy + 25f * sy),
                        strokeWidth = 6f * sx
                    )
                    drawScope.drawLine(
                        color = Color(0xFF424242),
                        start = Offset(ox + w / 2f - 8f * sx, oy + 10f * sy),
                        end = Offset(ox + w / 2f + 4f * sx, oy + 25f * sy),
                        strokeWidth = 6f * sx
                    )
                }

                ObstacleType.TRAFFIC_CONE -> {
                    // Bright Orange Traffic Cone
                    val conePath = Path().apply {
                        moveTo(ox, oy - h + 15f * sy)
                        lineTo(ox - w / 2f, oy + 15f * sy)
                        lineTo(ox + w / 2f, oy + 15f * sy)
                        close()
                    }
                    drawScope.drawPath(conePath, Color(0xFFFF6D00))
                    // Reflective white band
                    drawScope.drawRect(
                        color = Color.White,
                        topLeft = Offset(ox - w * 0.25f, oy - h * 0.45f + 15f * sy),
                        size = Size(w * 0.5f, 10f * sy)
                    )
                    // Base platform
                    drawScope.drawRect(
                        color = Color(0xFFE65100),
                        topLeft = Offset(ox - w * 0.6f, oy + 10f * sy),
                        size = Size(w * 1.2f, 8f * sy)
                    )
                }

                ObstacleType.WOODEN_BOX -> {
                    // Wooden Crate
                    drawScope.drawRoundRect(
                        color = Color(0xFF8D6E63),
                        topLeft = Offset(ox - w / 2f, oy - h + 15f * sy),
                        size = Size(w, h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * sx, 4f * sy)
                    )
                    // X-brace on box
                    drawScope.drawLine(
                        color = Color(0xFF4E342E),
                        start = Offset(ox - w / 2f + 4f * sx, oy - h + 19f * sy),
                        end = Offset(ox + w / 2f - 4f * sx, oy + 11f * sy),
                        strokeWidth = 3.5f * sx
                    )
                    drawScope.drawLine(
                        color = Color(0xFF4E342E),
                        start = Offset(ox + w / 2f - 4f * sx, oy - h + 19f * sy),
                        end = Offset(ox - w / 2f + 4f * sx, oy + 11f * sy),
                        strokeWidth = 3.5f * sx
                    )
                }

                ObstacleType.BROKEN_ROAD -> {
                    // Pothole Crater
                    drawScope.drawOval(
                        color = Color(0xFF1A1A1A),
                        topLeft = Offset(ox - w / 2f, oy - 5f * sy),
                        size = Size(w, h)
                    )
                    drawScope.drawOval(
                        color = Color(0xFF424242),
                        topLeft = Offset(ox - w / 2f + 6f * sx, oy - 2f * sy),
                        size = Size(w - 12f * sx, h - 8f * sy)
                    )
                }

                ObstacleType.WATER_PUDDLE -> {
                    // Reflective Blue Water Puddle
                    drawScope.drawOval(
                        color = Color(0xFF0288D1).copy(alpha = 0.75f),
                        topLeft = Offset(ox - w / 2f, oy - 5f * sy),
                        size = Size(w, h)
                    )
                    drawScope.drawOval(
                        color = Color.White.copy(alpha = 0.5f),
                        topLeft = Offset(ox - w * 0.25f, oy),
                        size = Size(w * 0.5f, h * 0.35f)
                    )
                }

                ObstacleType.DOG -> {
                    // Cute sleeping village dog
                    val bodyCol = Color(0xFFD7CCC8)
                    drawScope.drawOval(
                        color = bodyCol,
                        topLeft = Offset(ox - w / 2f, oy - 15f * sy),
                        size = Size(w, h * 0.8f)
                    )
                    // Head & Ear
                    drawScope.drawCircle(Color(0xFF8D6E63), 14f * sx, Offset(ox + w / 2f - 8f * sx, oy - 10f * sy))
                    drawScope.drawOval(Color(0xFF5D4037), Offset(ox + w / 2f - 14f * sx, oy - 18f * sy), Size(8f * sx, 16f * sy))
                }

                // High Obstacles (Player must SLIDE under)
                ObstacleType.HIGH_BEAM -> {
                    // Tall Construction Overhead Beam (Gap below for sliding)
                    val poleL = ox - w / 2f
                    val poleR = ox + w / 2f
                    val beamY = oy - h + 5f * sy

                    // Left & Right high support poles
                    drawScope.drawLine(
                        color = Color(0xFFF57C00),
                        start = Offset(poleL, oy + 20f * sy),
                        end = Offset(poleL, beamY),
                        strokeWidth = 8f * sx
                    )
                    drawScope.drawLine(
                        color = Color(0xFFF57C00),
                        start = Offset(poleR, oy + 20f * sy),
                        end = Offset(poleR, beamY),
                        strokeWidth = 8f * sx
                    )
                    // High Barricade Bar (High up, leaving sliding clearance)
                    drawScope.drawRect(
                        color = Color(0xFFFFD600),
                        topLeft = Offset(poleL - 10f * sx, beamY - 18f * sy),
                        size = Size(w + 20f * sx, 24f * sy)
                    )
                    // Warning Sign in middle
                    drawScope.drawCircle(Color.Red, 12f * sx, Offset(ox, beamY - 6f * sy))
                    drawScope.drawRect(Color.White, Offset(ox - 7f * sx, beamY - 9f * sy), Size(14f * sx, 6f * sy))
                }

                ObstacleType.CLOTHES_LINE -> {
                    // Village clothesline with hanging colorful clothes
                    val poleL = ox - w / 2f
                    val poleR = ox + w / 2f
                    val ropeY = oy - h + 15f * sy

                    // Bamboo poles
                    drawScope.drawLine(Color(0xFF8D6E63), Offset(poleL, oy + 20f * sy), Offset(poleL, ropeY), strokeWidth = 5f * sx)
                    drawScope.drawLine(Color(0xFF8D6E63), Offset(poleR, oy + 20f * sy), Offset(poleR, ropeY), strokeWidth = 5f * sx)
                    // Rope
                    drawScope.drawLine(Color.White, Offset(poleL, ropeY), Offset(poleR, ropeY + 4f * sy), strokeWidth = 2f * sx)
                    // Hanging clothes
                    drawScope.drawRect(Color(0xFFE91E63), Offset(ox - 24f * sx, ropeY + 2f * sy), Size(18f * sx, 34f * sy))
                    drawScope.drawRect(Color(0xFF29B6F6), Offset(ox, ropeY + 3f * sy), Size(20f * sx, 36f * sy))
                }

                ObstacleType.OVERHEAD_SIGN -> {
                    // Road overhead signboard
                    val poleL = ox - w / 2f
                    val poleR = ox + w / 2f
                    val signY = oy - h + 5f * sy

                    drawScope.drawLine(Color(0xFF455A64), Offset(poleL, oy + 20f * sy), Offset(poleL, signY), strokeWidth = 7f * sx)
                    drawScope.drawLine(Color(0xFF455A64), Offset(poleR, oy + 20f * sy), Offset(poleR, signY), strokeWidth = 7f * sx)
                    drawScope.drawRoundRect(
                        color = Color(0xFF1565C0),
                        topLeft = Offset(poleL - 15f * sx, signY - 25f * sy),
                        size = Size(w + 30f * sx, 35f * sy),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * sx, 6f * sy)
                    )
                    // Arrow symbol on sign
                    drawScope.drawLine(Color.White, Offset(ox - 15f * sx, signY - 8f * sy), Offset(ox + 15f * sx, signY - 8f * sy), strokeWidth = 4f * sx)
                }

                // Vehicles
                ObstacleType.CNG_RICKSHAW -> {
                    // Iconic Green Dhaka CNG 3-wheeler!
                    val vTop = oy - h + 20f * sy
                    // Green Body
                    drawScope.drawRoundRect(
                        color = Color(0xFF2E7D32),
                        topLeft = Offset(ox - w / 2f, vTop),
                        size = Size(w, h - 10f * sy),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * sx, 10f * sy)
                    )
                    // Black hood / roof
                    drawScope.drawRoundRect(
                        color = Color(0xFF212121),
                        topLeft = Offset(ox - w / 2f + 5f * sx, vTop - 5f * sy),
                        size = Size(w - 10f * sx, 16f * sy),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * sx, 6f * sy)
                    )
                    // Windshield glass
                    drawScope.drawRect(
                        color = Color(0xFF81D4FA),
                        topLeft = Offset(ox - w / 2f + 10f * sx, vTop + 14f * sy),
                        size = Size(25f * sx, 20f * sy)
                    )
                    // Wheels
                    drawScope.drawCircle(Color.Black, 11f * sx, Offset(ox - w / 3f, oy + 12f * sy))
                    drawScope.drawCircle(Color.Black, 11f * sx, Offset(ox + w / 3f, oy + 12f * sy))
                    // Headlight
                    drawScope.drawCircle(Color(0xFFFFEE58), 6f * sx, Offset(ox - w / 2f + 4f * sx, vTop + 24f * sy))
                }

                ObstacleType.CAR -> {
                    // Yellow Taxi / Car
                    val cTop = oy - h + 22f * sy
                    // Car body
                    drawScope.drawRoundRect(
                        color = Color(0xFFFFB300),
                        topLeft = Offset(ox - w / 2f, cTop + 16f * sy),
                        size = Size(w, h - 22f * sy),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * sx, 8f * sy)
                    )
                    // Roof cabin
                    drawScope.drawRoundRect(
                        color = Color(0xFFFFA000),
                        topLeft = Offset(ox - w * 0.25f, cTop),
                        size = Size(w * 0.55f, 22f * sy),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * sx, 6f * sy)
                    )
                    // Windows
                    drawScope.drawRect(
                        color = Color(0xFFB3E5FC),
                        topLeft = Offset(ox - w * 0.2f, cTop + 4f * sy),
                        size = Size(w * 0.45f, 12f * sy)
                    )
                    // Wheels
                    drawScope.drawCircle(Color.Black, 12f * sx, Offset(ox - w * 0.3f, oy + 12f * sy))
                    drawScope.drawCircle(Color.Black, 12f * sx, Offset(ox + w * 0.3f, oy + 12f * sy))
                    drawScope.drawCircle(Color(0xFFB0BEC5), 5f * sx, Offset(ox - w * 0.3f, oy + 12f * sy))
                    drawScope.drawCircle(Color(0xFFB0BEC5), 5f * sx, Offset(ox + w * 0.3f, oy + 12f * sy))
                }

                ObstacleType.BUS -> {
                    // Colorful Dhaka Bus
                    val bTop = oy - h + 15f * sy
                    drawScope.drawRoundRect(
                        color = Color(0xFFD32F2F),
                        topLeft = Offset(ox - w / 2f, bTop),
                        size = Size(w, h - 8f * sy),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * sx, 8f * sy)
                    )
                    // Yellow decorative stripe
                    drawScope.drawRect(
                        color = Color(0xFFFFD54F),
                        topLeft = Offset(ox - w / 2f, bTop + 24f * sy),
                        size = Size(w, 14f * sy)
                    )
                    // Row of windows
                    for (win in 0..4) {
                        drawScope.drawRect(
                            color = Color(0xFFE1F5FE),
                            topLeft = Offset(ox - w / 2f + 14f * sx + win * 32f * sx, bTop + 8f * sy),
                            size = Size(22f * sx, 14f * sy)
                        )
                    }
                    // Wheels
                    drawScope.drawCircle(Color.Black, 14f * sx, Offset(ox - w * 0.32f, oy + 12f * sy))
                    drawScope.drawCircle(Color.Black, 14f * sx, Offset(ox + w * 0.32f, oy + 12f * sy))
                }
            }

            // Visual Action Badges (Jump / Slide) to clearly guide the player!
            if (ob.type.isHigh) {
                // High obstacle -> Prompt player to SLIDE / নিচু হোন
                val badgeY = oy - h - 18f * sy
                val pulse = sin(engine.player.runAnimTime * 8f) * 2f * sy
                drawScope.drawRoundRect(
                    color = Color(0xFFFF6D00),
                    topLeft = Offset(ox - 38f * sx, badgeY - 12f * sy + pulse),
                    size = Size(76f * sx, 24f * sy),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * sx, 6f * sy)
                )
                drawScope.drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(ox - 38f * sx, badgeY - 12f * sy + pulse),
                    size = Size(76f * sx, 24f * sy),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * sx, 6f * sy),
                    style = Stroke(width = 1.5f * sx)
                )
                // Down arrow indicating duck / slide
                val arrowY = badgeY + pulse
                drawScope.drawLine(Color.White, Offset(ox, arrowY - 6f * sy), Offset(ox, arrowY + 6f * sy), strokeWidth = 3f * sx, cap = StrokeCap.Round)
                drawScope.drawLine(Color.White, Offset(ox - 4f * sx, arrowY + 2f * sy), Offset(ox, arrowY + 6f * sy), strokeWidth = 3f * sx, cap = StrokeCap.Round)
                drawScope.drawLine(Color.White, Offset(ox + 4f * sx, arrowY + 2f * sy), Offset(ox, arrowY + 6f * sy), strokeWidth = 3f * sx, cap = StrokeCap.Round)
            } else if (ob.type == ObstacleType.LOW_BARRIER || ob.type == ObstacleType.TRAFFIC_CONE || ob.type == ObstacleType.WOODEN_BOX) {
                // Low barrier / barricade -> Up arrow indicating JUMP / লাফ দিন
                val badgeY = oy - h - 12f * sy
                val pulse = sin(engine.player.runAnimTime * 8f) * 2f * sy
                drawScope.drawCircle(
                    color = Color(0xFF00C853),
                    radius = 11f * sx,
                    center = Offset(ox, badgeY + pulse)
                )
                drawScope.drawCircle(
                    color = Color.White,
                    radius = 11f * sx,
                    center = Offset(ox, badgeY + pulse),
                    style = Stroke(width = 1.5f * sx)
                )
                val arrowY = badgeY + pulse
                drawScope.drawLine(Color.White, Offset(ox, arrowY + 5f * sy), Offset(ox, arrowY - 5f * sy), strokeWidth = 2.5f * sx, cap = StrokeCap.Round)
                drawScope.drawLine(Color.White, Offset(ox - 4f * sx, arrowY - 2f * sy), Offset(ox, arrowY - 5f * sy), strokeWidth = 2.5f * sx, cap = StrokeCap.Round)
                drawScope.drawLine(Color.White, Offset(ox + 4f * sx, arrowY - 2f * sy), Offset(ox, arrowY - 5f * sy), strokeWidth = 2.5f * sx, cap = StrokeCap.Round)
            }
        }
    }

    private fun drawOldManPlayer(drawScope: DrawScope, engine: GameEngine, sx: Float, sy: Float, spriteManager: GameSpriteManager? = null) {
        val p = engine.player
        val px = p.x * sx
        val py = (p.currentLaneY - p.jumpY) * sy

        // If hurt, flicker alpha
        if (p.isHurt && ((p.hurtTimer * 10).toInt() % 2 == 0)) {
            return
        }

        // Jump shadow on ground
        if (p.isJumping) {
            val shadowScale = (1f - (p.jumpY / 180f)).coerceIn(0.3f, 1f)
            val shadowY = (p.currentLaneY + 18f) * sy
            drawScope.drawOval(
                color = Color.Black.copy(alpha = 0.3f * shadowScale),
                topLeft = Offset(px - 28f * sx * shadowScale, shadowY - 8f * sy * shadowScale),
                size = Size(56f * sx * shadowScale, 16f * sy * shadowScale)
            )
        }

        // Active Shield Sphere
        if (engine.activePowerUps.containsKey(PowerUpType.SHIELD)) {
            val pulse = (sin(p.runAnimTime * 6f) * 4f + 48f) * sx
            drawScope.drawCircle(
                color = Color(0x4400E5FF),
                radius = pulse,
                center = Offset(px, py - 30f * sy)
            )
            drawScope.drawCircle(
                color = Color(0xFF00E5FF),
                radius = pulse,
                center = Offset(px, py - 30f * sy),
                style = Stroke(width = 3.5f * sx)
            )
        }

        // Active Speed Boost Fire Trails
        if (engine.activePowerUps.containsKey(PowerUpType.SPEED_BOOST)) {
            for (i in 0..4) {
                val fOff = (i * 22f + (p.runAnimTime * 40f) % 30f) * sx
                drawScope.drawLine(
                    color = Color(0xFFFFD600).copy(alpha = 0.8f - i * 0.15f),
                    start = Offset(px - fOff, py - 20f * sy - i * 5f * sy),
                    end = Offset(px - fOff - 45f * sx, py - 20f * sy - i * 5f * sy),
                    strokeWidth = 4f * sx,
                    cap = StrokeCap.Round
                )
            }
        }

        // Draw provided OLD_MAN_PLAYER character asset if loaded!
        val playerBmp = spriteManager?.playerRunBitmap
        if (playerBmp != null) {
            val spriteW = 105f * sx
            val spriteH = 115f * sy
            val animBob = if (p.isJumping) 0f else sin(p.runAnimTime * 14f) * 4f * sy

            val rot = when {
                p.isJumping -> -14f
                p.isSliding -> 28f
                else -> sin(p.runAnimTime * 14f) * 4f
            }
            val scaleX = if (p.isSliding) 1.25f else 1.0f
            val scaleY = if (p.isSliding) 0.55f else 1.0f

            val drawCenterX = px
            val drawCenterY = py - (if (p.isSliding) 18f * sy else 42f * sy) + animBob

            drawScope.translate(drawCenterX, drawCenterY) {
                drawScope.rotate(rot) {
                    drawScope.scale(scaleX, scaleY) {
                        drawScope.drawImage(
                            image = playerBmp,
                            dstOffset = IntOffset((-spriteW / 2f).toInt(), (-spriteH / 2f).toInt()),
                            dstSize = IntSize(spriteW.toInt(), spriteH.toInt())
                        )
                    }
                }
            }
            return
        }

        // Procedural 2D Cartoon Old Man Character
        val equippedOutfit = engine.preferences.equippedOutfit
        val equippedBag = engine.preferences.equippedBag

        // Body Colors based on Equipped Outfit
        val skinColor = Color(0xFFDE9966)
        val lungiColor = when (equippedOutfit) {
            "outfit_dhuti" -> Color(0xFFF5F5F5)
            "outfit_detective" -> Color(0xFF5D4037)
            "outfit_superhero" -> Color(0xFF1565C0)
            else -> Color(0xFFFF6D00) // Classic bright orange lungi
        }
        val vestColor = when (equippedOutfit) {
            "outfit_dhuti" -> Color(0xFFE0E0E0)
            "outfit_detective" -> Color(0xFF3E2723)
            "outfit_superhero" -> Color(0xFFD50000)
            else -> Color(0xFFFFFFFF) // Classic white vest
        }

        // Animation Calculations
        val animCycle = p.runAnimTime * 12f
        val legSwing1 = sin(animCycle) * 26f
        val legSwing2 = sin(animCycle + PI.toFloat()) * 26f
        val armSwing = sin(animCycle) * 22f
        val headBob = abs(sin(animCycle)) * 4f * sy

        if (p.isSliding) {
            // Sliding / Crouched Low Pose
            val slideY = py + 5f * sy

            // Lungi & Legs sliding forward
            drawScope.drawRoundRect(
                color = lungiColor,
                topLeft = Offset(px - 35f * sx, slideY - 14f * sy),
                size = Size(65f * sx, 24f * sy),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * sx, 10f * sy)
            )
            // Vest body
            drawScope.drawRoundRect(
                color = vestColor,
                topLeft = Offset(px - 15f * sx, slideY - 26f * sy),
                size = Size(35f * sx, 20f * sy),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * sx, 6f * sy)
            )
            // Head low
            drawScope.drawCircle(skinColor, 15f * sx, Offset(px + 24f * sx, slideY - 20f * sy))
            // Mustache
            drawScope.drawOval(Color.White, Offset(px + 24f * sx, slideY - 18f * sy), Size(16f * sx, 8f * sy))
            // Big Money Bag being dragged safely
            drawMoneyBag(drawScope, px - 35f * sx, slideY - 22f * sy, equippedBag, sx, sy)
        } else {
            // Upright Sprinting Pose
            val bodyY = py - 32f * sy - headBob

            // Superhero Cape if equipped
            if (equippedOutfit == "outfit_superhero") {
                val capePath = Path().apply {
                    moveTo(px - 12f * sx, bodyY - 10f * sy)
                    lineTo(px - 55f * sx + sin(animCycle) * 10f * sx, bodyY + 15f * sy)
                    lineTo(px - 45f * sx, bodyY + 35f * sy)
                    lineTo(px - 6f * sx, bodyY + 10f * sy)
                    close()
                }
                drawScope.drawPath(capePath, Color(0xFFD50000))
            }

            // Legs (Running cycle)
            if (p.isJumping) {
                // Legs tucked up
                drawScope.drawLine(skinColor, Offset(px - 10f * sx, bodyY + 28f * sy), Offset(px - 22f * sx, bodyY + 38f * sy), strokeWidth = 9f * sx, cap = StrokeCap.Round)
                drawScope.drawLine(skinColor, Offset(px + 8f * sx, bodyY + 28f * sy), Offset(px + 18f * sx, bodyY + 38f * sy), strokeWidth = 9f * sx, cap = StrokeCap.Round)
            } else {
                // Back leg
                drawScope.drawLine(skinColor, Offset(px - 8f * sx, bodyY + 26f * sy), Offset(px - 8f * sx + legSwing2 * 0.8f * sx, bodyY + 48f * sy), strokeWidth = 9f * sx, cap = StrokeCap.Round)
                // Front leg
                drawScope.drawLine(skinColor, Offset(px + 8f * sx, bodyY + 26f * sy), Offset(px + 8f * sx + legSwing1 * 0.8f * sx, bodyY + 48f * sy), strokeWidth = 9f * sx, cap = StrokeCap.Round)
            }

            // Lungi (Traditional garment with folds)
            drawScope.drawRoundRect(
                color = lungiColor,
                topLeft = Offset(px - 18f * sx, bodyY + 6f * sy),
                size = Size(36f * sx, 28f * sy),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * sx, 6f * sy)
            )
            // Lungi border stripe
            drawScope.drawRect(
                color = Color.White.copy(alpha = 0.7f),
                topLeft = Offset(px - 18f * sx, bodyY + 28f * sy),
                size = Size(36f * sx, 5f * sy)
            )

            // Torso / Vest
            drawScope.drawRoundRect(
                color = vestColor,
                topLeft = Offset(px - 16f * sx, bodyY - 16f * sy),
                size = Size(32f * sx, 26f * sy),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * sx, 8f * sy)
            )

            // Large Money Bag carried in arms/on back!
            drawMoneyBag(drawScope, px - 28f * sx, bodyY - 8f * sy, equippedBag, sx, sy)

            // Arms holding bag & pumping
            drawScope.drawLine(skinColor, Offset(px + 12f * sx, bodyY - 8f * sy), Offset(px + 26f * sx + armSwing * 0.4f * sx, bodyY + 6f * sy), strokeWidth = 8f * sx, cap = StrokeCap.Round)
            drawScope.drawLine(skinColor, Offset(px - 12f * sx, bodyY - 8f * sy), Offset(px - 22f * sx, bodyY - 4f * sy), strokeWidth = 8f * sx, cap = StrokeCap.Round)

            // Head (Round, bald with white hair tufts)
            val headCenter = Offset(px + 4f * sx, bodyY - 32f * sy)
            drawScope.drawCircle(skinColor, 18f * sx, headCenter)

            // White hair tufts on sides
            drawScope.drawCircle(Color.White, 8f * sx, Offset(headCenter.x - 14f * sx, headCenter.y - 2f * sy))
            drawScope.drawCircle(Color.White, 7f * sx, Offset(headCenter.x - 16f * sx, headCenter.y + 6f * sy))

            // Detective Hat if equipped
            if (equippedOutfit == "outfit_detective") {
                drawScope.drawRect(Color(0xFF3E2723), Offset(headCenter.x - 22f * sx, headCenter.y - 18f * sy), Size(44f * sx, 8f * sy))
                drawScope.drawRoundRect(Color(0xFF4E342E), Offset(headCenter.x - 14f * sx, headCenter.y - 32f * sy), Size(28f * sx, 16f * sy), androidx.compose.ui.geometry.CornerRadius(4f * sx, 4f * sy))
            }

            // Eyes: Panicked wide cartoon eyes looking forward or looking back!
            val lookingBack = p.lookBackTimer < 1.2f
            val eyeX1 = if (lookingBack) headCenter.x - 6f * sx else headCenter.x + 4f * sx
            val eyeX2 = if (lookingBack) headCenter.x + 4f * sx else headCenter.x + 14f * sx
            val pupilOffset = if (lookingBack) -3f * sx else 3f * sx

            // White eye balls
            drawScope.drawCircle(Color.White, 6.5f * sx, Offset(eyeX1, headCenter.y - 5f * sy))
            drawScope.drawCircle(Color.White, 6.5f * sx, Offset(eyeX2, headCenter.y - 5f * sy))
            // Black pupils
            drawScope.drawCircle(Color.Black, 3.5f * sx, Offset(eyeX1 + pupilOffset, headCenter.y - 5f * sy))
            drawScope.drawCircle(Color.Black, 3.5f * sx, Offset(eyeX2 + pupilOffset, headCenter.y - 5f * sy))

            // Bushy White Mustache (চিরচেনা গোঁফ)
            drawScope.drawOval(
                color = Color.White,
                topLeft = Offset(headCenter.x + (if (lookingBack) -12f else 0f) * sx, headCenter.y + 2f * sy),
                size = Size(20f * sx, 10f * sy)
            )

            // Open screaming / panting mouth
            drawScope.drawOval(
                color = Color(0xFFB71C1C),
                topLeft = Offset(headCenter.x + (if (lookingBack) -4f else 4f) * sx, headCenter.y + 9f * sy),
                size = Size(10f * sx, 8f * sy)
            )
        }
    }

    private fun drawMoneyBag(drawScope: DrawScope, bx: Float, by: Float, bagType: String, sx: Float, sy: Float) {
        val bagColor = when (bagType) {
            "bag_golden" -> Color(0xFFFFD54F)
            "bag_vault" -> Color(0xFF455A64)
            else -> Color(0xFFBCAAA4) // Burlap jute sack
        }

        // Big round bag body
        drawScope.drawOval(
            color = bagColor,
            topLeft = Offset(bx - 26f * sx, by - 24f * sy),
            size = Size(52f * sx, 48f * sy)
        )
        // Bag tied neck
        drawScope.drawRoundRect(
            color = if (bagType == "bag_golden") Color(0xFFFF8F00) else Color(0xFF8D6E63),
            topLeft = Offset(bx - 12f * sx, by - 36f * sy),
            size = Size(24f * sx, 16f * sy),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * sx, 4f * sy)
        )
        // Green paper money notes visibly sticking out of top opening!
        drawScope.drawRoundRect(
            color = GameMoneyGreen,
            topLeft = Offset(bx - 16f * sx, by - 44f * sy),
            size = Size(18f * sx, 12f * sy),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * sx, 2f * sy)
        )
        drawScope.drawRoundRect(
            color = GameMoneyPink,
            topLeft = Offset(bx, by - 42f * sy),
            size = Size(16f * sx, 10f * sy),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * sx, 2f * sy)
        )
        // ৳ Dollar / Taka insignia on the bag
        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = 10f * sx,
            center = Offset(bx, by - 2f * sy)
        )
        drawScope.drawCircle(
            color = Color(0xFF00C853),
            radius = 6f * sx,
            center = Offset(bx, by - 2f * sy)
        )
    }

    private fun drawChaserWoman(drawScope: DrawScope, engine: GameEngine, sx: Float, sy: Float, spriteManager: GameSpriteManager? = null) {
        val c = engine.chaser
        val p = engine.player

        // Calculate Chaser X position based on dynamic distance (0.0 to 1.0)
        // At 1.0: far back (e.g. X = 80f), at 0.0: caught! (X = p.x - 25f)
        val targetX = if (p.isCaught) {
            p.x - 22f // Grabbing him from behind!
        } else {
            p.x - (260f * c.distancePercent).coerceAtLeast(40f)
        }

        val cx = targetX * sx
        val cy = (p.currentLaneY) * sy

        // Draw provided OLD_WOMAN_CHASER character asset if loaded!
        val chaserBmp = spriteManager?.chaserRunBitmap
        if (chaserBmp != null) {
            val spriteW = 100f * sx
            val spriteH = 110f * sy
            val animBob = sin(c.animTime * 14f) * 4f * sy
            val rot = sin(c.animTime * 14f) * 5f

            val drawCenterX = cx
            val drawCenterY = cy - 40f * sy + animBob

            drawScope.translate(drawCenterX, drawCenterY) {
                drawScope.rotate(rot) {
                    drawScope.drawImage(
                        image = chaserBmp,
                        dstOffset = IntOffset((-spriteW / 2f).toInt(), (-spriteH / 2f).toInt()),
                        dstSize = IntSize(spriteW.toInt(), spriteH.toInt())
                    )
                }
            }

            // Comic Speech Bubble when shouting
            if (c.shoutTimer > 0f && c.shoutMessageBn.isNotEmpty()) {
                val bubbleX = cx + 20f * sx
                val bubbleY = cy - 85f * sy
                drawScope.drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(bubbleX - 10f * sx, bubbleY - 14f * sy),
                    size = Size(150f * sx, 34f * sy),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * sx, 10f * sy)
                )
                drawScope.drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(bubbleX - 10f * sx, bubbleY - 14f * sy),
                    size = Size(150f * sx, 34f * sy),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * sx, 10f * sy),
                    style = Stroke(width = 2f * sx)
                )
                val pointer = Path().apply {
                    moveTo(bubbleX + 15f * sx, bubbleY + 20f * sy)
                    lineTo(bubbleX + 8f * sx, bubbleY + 32f * sy)
                    lineTo(bubbleX + 28f * sx, bubbleY + 20f * sy)
                    close()
                }
                drawScope.drawPath(pointer, Color.White)
                drawScope.drawPath(pointer, Color.Black, style = Stroke(width = 1.5f * sx))
            }
            return
        }

        val animCycle = c.animTime * 14f
        val legSwing1 = sin(animCycle) * 24f
        val legSwing2 = sin(animCycle + PI.toFloat()) * 24f
        val fistShake = sin(c.angryFistTime * 12f) * 16f

        val skinColor = Color(0xFFDE9966)
        val sariColor = Color(0xFF2E7D32) // Bright Green Sari
        val sariBorderColor = Color(0xFFD50000) // Red border

        val bodyY = cy - 30f * sy

        // Running Legs
        drawScope.drawLine(skinColor, Offset(cx - 6f * sx, bodyY + 26f * sy), Offset(cx - 6f * sx + legSwing2 * sx, bodyY + 46f * sy), strokeWidth = 8f * sx, cap = StrokeCap.Round)
        drawScope.drawLine(skinColor, Offset(cx + 6f * sx, bodyY + 26f * sy), Offset(cx + 6f * sx + legSwing1 * sx, bodyY + 46f * sy), strokeWidth = 8f * sx, cap = StrokeCap.Round)

        // Green Sari Dress (Aanchaal flowing behind)
        drawScope.drawRoundRect(
            color = sariColor,
            topLeft = Offset(cx - 16f * sx, bodyY + 4f * sy),
            size = Size(34f * sx, 30f * sy),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * sx, 8f * sy)
        )
        // Red Sari Border
        drawScope.drawRect(
            color = sariBorderColor,
            topLeft = Offset(cx - 16f * sx, bodyY + 30f * sy),
            size = Size(34f * sx, 5f * sy)
        )

        // Torso / Blouse
        drawScope.drawRoundRect(
            color = sariBorderColor,
            topLeft = Offset(cx - 14f * sx, bodyY - 14f * sy),
            size = Size(28f * sx, 22f * sy),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * sx, 6f * sy)
        )

        // Right Arm Raised with Wooden Rolling Pin (বেলুন)
        val handX = cx + 18f * sx
        val handY = bodyY - 25f * sy + fistShake * 0.5f * sy
        drawScope.drawLine(skinColor, Offset(cx + 10f * sx, bodyY - 8f * sy), Offset(handX, handY), strokeWidth = 7f * sx, cap = StrokeCap.Round)

        // Wooden Rolling Pin (Belun)
        drawScope.drawLine(
            color = Color(0xFF8D6E63),
            start = Offset(handX - 8f * sx, handY - 18f * sy),
            end = Offset(handX + 18f * sx, handY + 12f * sy),
            strokeWidth = 6f * sx,
            cap = StrokeCap.Round
        )

        // Left Arm reaching forward
        drawScope.drawLine(skinColor, Offset(cx - 8f * sx, bodyY - 6f * sy), Offset(cx + 20f * sx, bodyY + 4f * sy), strokeWidth = 7f * sx, cap = StrokeCap.Round)

        // Head (Round, furious expression)
        val headCenter = Offset(cx + 4f * sx, bodyY - 28f * sy)
        drawScope.drawCircle(skinColor, 16f * sx, headCenter)

        // Gray Hair Bun (খোপা) behind head
        drawScope.drawCircle(Color(0xFFB0BEC5), 10f * sx, Offset(headCenter.x - 14f * sx, headCenter.y - 4f * sy))
        drawScope.drawCircle(Color(0xFF78909C), 7f * sx, Offset(headCenter.x - 12f * sx, headCenter.y - 12f * sy))

        // Red Bindi (টিপ)
        drawScope.drawCircle(Color(0xFFD50000), 3.5f * sx, Offset(headCenter.x + 6f * sx, headCenter.y - 5f * sy))

        // Round Spectacles / Angry Eyes
        drawScope.drawCircle(Color.White, 5.5f * sx, Offset(headCenter.x + 8f * sx, headCenter.y - 2f * sy))
        drawScope.drawCircle(Color.Black, 3f * sx, Offset(headCenter.x + 9f * sx, headCenter.y - 2f * sy))
        // Glasses frame
        drawScope.drawCircle(Color(0xFF37474F), 6f * sx, Offset(headCenter.x + 8f * sx, headCenter.y - 2f * sy), style = Stroke(width = 1.5f * sx))

        // Furrowed Angry Eyebrows
        drawScope.drawLine(Color.Black, Offset(headCenter.x + 2f * sx, headCenter.y - 8f * sy), Offset(headCenter.x + 14f * sx, headCenter.y - 5f * sy), strokeWidth = 2.5f * sx)

        // Furious Shouting Mouth
        drawScope.drawOval(
            color = Color(0xFFB71C1C),
            topLeft = Offset(headCenter.x + 4f * sx, headCenter.y + 5f * sy),
            size = Size(12f * sx, 9f * sy)
        )

        // Comic Speech Bubble when shouting
        if (c.shoutTimer > 0f && c.shoutMessageBn.isNotEmpty()) {
            val bubbleX = cx + 25f * sx
            val bubbleY = bodyY - 70f * sy
            drawScope.drawRoundRect(
                color = Color.White,
                topLeft = Offset(bubbleX - 10f * sx, bubbleY - 14f * sy),
                size = Size(150f * sx, 34f * sy),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * sx, 10f * sy)
            )
            drawScope.drawRoundRect(
                color = Color.Black,
                topLeft = Offset(bubbleX - 10f * sx, bubbleY - 14f * sy),
                size = Size(150f * sx, 34f * sy),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * sx, 10f * sy),
                style = Stroke(width = 2f * sx)
            )
            // Speech pointer
            val pointer = Path().apply {
                moveTo(bubbleX + 15f * sx, bubbleY + 20f * sy)
                lineTo(bubbleX + 8f * sx, bubbleY + 32f * sy)
                lineTo(bubbleX + 28f * sx, bubbleY + 20f * sy)
                close()
            }
            drawScope.drawPath(pointer, Color.White)
            drawScope.drawPath(pointer, Color.Black, style = Stroke(width = 1.5f * sx))
        }
    }

    private fun drawParticles(drawScope: DrawScope, engine: GameEngine, sx: Float, sy: Float) {
        for (p in engine.particles) {
            val px = p.x * sx
            val py = p.y * sy
            val col = p.color.copy(alpha = p.alpha)

            when (p.shape) {
                ParticleShape.CIRCLE -> {
                    drawScope.drawCircle(col, p.size * sx, Offset(px, py))
                }
                ParticleShape.STAR -> {
                    drawScope.rotate(p.rotation, pivot = Offset(px, py)) {
                        drawScope.drawRect(col, Offset(px - p.size * 0.5f * sx, py - p.size * 0.5f * sy), Size(p.size * sx, p.size * sy))
                    }
                }
                ParticleShape.MONEY_NOTE -> {
                    drawScope.rotate(p.rotation, pivot = Offset(px, py)) {
                        drawScope.drawRoundRect(
                            color = col,
                            topLeft = Offset(px - 10f * sx, py - 6f * sy),
                            size = Size(20f * sx, 12f * sy),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * sx, 2f * sy)
                        )
                    }
                }
                ParticleShape.SMOKE_DUST -> {
                    drawScope.drawCircle(col, p.size * sx, Offset(px, py))
                }
                ParticleShape.TEXT, ParticleShape.COIN -> {
                    drawScope.drawCircle(col, p.size * sx * 0.6f, Offset(px, py))
                }
            }
        }
    }

    private fun drawWeatherOverlay(drawScope: DrawScope, engine: GameEngine, sx: Float, sy: Float) {
        // Rainy weather
        if (engine.currentTheme.hasRain) {
            val rainOffset = (engine.player.runAnimTime * 800f) % 720f
            for (i in 0..60) {
                val rx = ((i * 37 + (engine.player.runAnimTime * 200f)) % 1350f) * sx
                val ry = ((i * 49 + rainOffset) % 720f) * sy
                drawScope.drawLine(
                    color = Color(0x99B0BEC5),
                    start = Offset(rx, ry),
                    end = Offset(rx - 12f * sx, ry + 22f * sy),
                    strokeWidth = 1.8f * sx
                )
            }
        }

        // Close chaser danger vignette when woman is within 30% distance
        if (engine.chaser.distancePercent < 0.35f && engine.screenState == com.example.game.model.ScreenState.PLAYING) {
            val dangerAlpha = ((0.35f - engine.chaser.distancePercent) / 0.35f * 0.45f).coerceIn(0f, 0.5f)
            val pulse = (sin(engine.player.runAnimTime * 8f) * 0.1f + 0.9f)
            drawScope.drawRect(
                color = Color.Red.copy(alpha = dangerAlpha * pulse),
                size = drawScope.size
            )
        }
    }
}
