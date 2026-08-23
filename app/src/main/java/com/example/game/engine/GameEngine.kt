package com.example.game.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.game.audio.SoundManager
import com.example.game.model.ActivePowerUp
import com.example.game.model.ChaserState
import com.example.game.model.Collectible
import com.example.game.model.CollectibleType
import com.example.game.model.Difficulty
import com.example.game.model.EnvironmentTheme
import com.example.game.model.LevelInfo
import com.example.game.model.Obstacle
import com.example.game.model.ObstacleType
import com.example.game.model.Particle
import com.example.game.model.ParticleShape
import com.example.game.model.PlayerState
import com.example.game.model.PowerUpItem
import com.example.game.model.PowerUpType
import com.example.game.model.ScreenState
import com.example.game.model.SpecialEventType
import com.example.game.storage.GamePreferences
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class GameEngine(
    val soundManager: SoundManager,
    val preferences: GamePreferences
) {
    // Canvas reference coordinates
    val worldWidth = 1280f
    val worldHeight = 720f
    val laneY = floatArrayOf(420f, 505f, 590f)

    // Game Loop State (Observable by Compose UI)
    var screenState: ScreenState by mutableStateOf(ScreenState.MENU)
    var isEndlessMode: Boolean by mutableStateOf(true)
    var currentLevel: LevelInfo? by mutableStateOf(null)
    var currentTheme: EnvironmentTheme by mutableStateOf(EnvironmentTheme.VILLAGE)

    // Game stats (Observable by Compose HUD)
    var score: Long by mutableStateOf(0L)
    var distanceMeters: Float by mutableStateOf(0f)
    var coinsCollected: Int by mutableStateOf(0)
    var moneyCollected: Long by mutableStateOf(0L)
    var hearts: Int by mutableStateOf(3)
    var comboCount: Int by mutableStateOf(0)
    var comboMultiplier: Int by mutableStateOf(1)
    var obstaclesAvoidedCount: Int = 0
    var slidesPerformedCount: Int = 0
    var powerupsCollectedCount: Int = 0

    // Speed
    var baseSpeed: Float = 9.0f
    var currentSpeed: Float = 9.0f

    // Entities
    val player = PlayerState()
    val chaser = ChaserState()
    val obstacles = ArrayList<Obstacle>()
    val collectibles = ArrayList<Collectible>()
    val powerUpItems = ArrayList<PowerUpItem>()
    val activePowerUps = HashMap<PowerUpType, ActivePowerUp>()
    val particles = ArrayList<Particle>()

    // Special Event
    var currentSpecialEvent: SpecialEventType? = null
    var specialEventTimer: Float = 0f
    var nextSpecialEventDistance: Float = 200f

    // Camera shake
    var shakeIntensity: Float = 0f
    var shakeOffsetX: Float = 0f
    var shakeOffsetY: Float = 0f

    // Procedural Spawning Timers
    private var nextSpawnDistance: Float = 0f
    private var obstacleIdCounter: Long = 1L
    private var collectibleIdCounter: Long = 1L
    private var powerUpIdCounter: Long = 1L

    // Difficulty multiplier
    var difficulty: Difficulty = Difficulty.NORMAL

    // Caught Sequence
    var caughtTimer: Float = 0f
    var caughtAnimationDone: Boolean = false

    fun startEndlessGame() {
        startEndless()
    }

    fun pauseGame() {
        if (screenState == ScreenState.PLAYING) {
            screenState = ScreenState.PAUSED
        }
    }

    fun resumeGame() {
        if (screenState == ScreenState.PAUSED) {
            screenState = ScreenState.PLAYING
        }
    }

    fun restartCurrentGame() {
        if (isEndlessMode) {
            startEndless()
        } else {
            currentLevel?.let { startLevel(it) } ?: startEndless()
        }
    }

    fun goToMainMenu() {
        screenState = ScreenState.MENU
        soundManager.stopBgm()
    }

    fun startLevel(level: LevelInfo) {
        currentLevel = level
        isEndlessMode = false
        currentTheme = level.theme
        resetGameState()
        screenState = ScreenState.PLAYING
        soundManager.startBgm()
    }

    fun startEndless() {
        currentLevel = null
        isEndlessMode = true
        currentTheme = EnvironmentTheme.VILLAGE
        resetGameState()
        screenState = ScreenState.PLAYING
        soundManager.startBgm()
    }

    private fun resetGameState() {
        difficulty = preferences.difficulty
        score = 0L
        distanceMeters = 0f
        coinsCollected = 0
        moneyCollected = 0L
        hearts = 3
        comboCount = 0
        comboMultiplier = 1
        obstaclesAvoidedCount = 0
        slidesPerformedCount = 0
        powerupsCollectedCount = 0

        baseSpeed = 8.5f * difficulty.speedMultiplier
        currentSpeed = baseSpeed

        player.x = 280f
        player.targetLane = 1
        player.currentLaneY = laneY[1]
        player.isJumping = false
        player.jumpY = 0f
        player.jumpVelocity = 0f
        player.isSliding = false
        player.slideTimer = 0f
        player.isHurt = false
        player.hurtTimer = 0f
        player.lookBackTimer = 0f
        player.runAnimTime = 0f
        player.doubleJumpAvailable = false
        player.isCaught = false
        player.caughtProgress = 0f

        chaser.distancePercent = 0.85f
        chaser.animTime = 0f
        chaser.angryFistTime = 0f
        chaser.shoutMessageBn = ""
        chaser.shoutMessageEn = ""
        chaser.shoutTimer = 0f
        chaser.isLunging = false

        obstacles.clear()
        collectibles.clear()
        powerUpItems.clear()
        activePowerUps.clear()
        particles.clear()

        currentSpecialEvent = null
        specialEventTimer = 0f
        nextSpecialEventDistance = 250f
        nextSpawnDistance = 60f
        shakeIntensity = 0f
        caughtTimer = 0f
        caughtAnimationDone = false

        // Pre-populate road ahead with initial Taka notes and first barricade
        for (i in 0..6) {
            collectibles.add(Collectible(collectibleIdCounter++, 450f + i * 65f, 1, -10f, if (i % 2 == 0) CollectibleType.NOTE_500 else CollectibleType.NOTE_100))
        }
        // First visible road barricade
        obstacles.add(Obstacle(obstacleIdCounter++, 950f, 1, ObstacleType.LOW_BARRIER))
        // Jump guide arc of money notes over barricade
        collectibles.add(Collectible(collectibleIdCounter++, 950f, 1, -75f, CollectibleType.NOTE_1000))
        // High beam requiring duck / slide in lane 0
        obstacles.add(Obstacle(obstacleIdCounter++, 1200f, 0, ObstacleType.HIGH_BEAM))
        collectibles.add(Collectible(collectibleIdCounter++, 1200f, 0, 10f, CollectibleType.BUNDLE_5000))
    }

    // Input handlers
    fun onJump() {
        if (screenState != ScreenState.PLAYING || player.isCaught) return

        if (!player.isJumping) {
            player.isJumping = true
            player.jumpVelocity = if (activePowerUps.containsKey(PowerUpType.SUPER_JUMP)) -23.5f else -18.5f
            player.doubleJumpAvailable = activePowerUps.containsKey(PowerUpType.SUPER_JUMP)
            soundManager.playJump()
            spawnJumpDust(player.x, player.currentLaneY)
        } else if (player.doubleJumpAvailable) {
            player.doubleJumpAvailable = false
            player.jumpVelocity = -20f
            soundManager.playJump()
            spawnSparkles(player.x, player.currentLaneY - player.jumpY, Color(0xFF76FF03), 8)
        }
    }

    fun onSlide() {
        if (screenState != ScreenState.PLAYING || player.isCaught) return
        if (player.isSliding) return

        player.isSliding = true
        player.slideTimer = 0.7f
        slidesPerformedCount++
        soundManager.playSlide()
        spawnSlideDust(player.x, player.currentLaneY)

        // If jumping, slide forces fast downward dive
        if (player.isJumping) {
            player.jumpVelocity = 15f
        }
    }

    fun onMoveUp() {
        if (screenState != ScreenState.PLAYING || player.isCaught) return
        if (player.targetLane > 0) {
            player.targetLane--
            spawnPuff(player.x, player.currentLaneY)
        }
    }

    fun onMoveDown() {
        if (screenState != ScreenState.PLAYING || player.isCaught) return
        if (player.targetLane < 2) {
            player.targetLane++
            spawnPuff(player.x, player.currentLaneY)
        }
    }

    // Core Game Update Loop (Called ~60 FPS with delta seconds)
    fun update(dt: Float) {
        if (screenState != ScreenState.PLAYING) {
            if (screenState == ScreenState.CAUGHT_GAMEOVER) {
                updateCaughtAnimation(dt)
            }
            return
        }

        // 1. Calculate running speed & active effects
        val hasBoost = activePowerUps.containsKey(PowerUpType.SPEED_BOOST)
        val speedFactor = if (hasBoost) 1.55f else 1.0f
        currentSpeed = (baseSpeed + (distanceMeters / 250f).coerceAtMost(6.5f)) * speedFactor

        val frameMoveDistance = currentSpeed * dt * 45f
        distanceMeters += frameMoveDistance * 0.08f
        score += (frameMoveDistance * 0.4f * comboMultiplier * (if (activePowerUps.containsKey(PowerUpType.COIN_MULTIPLIER)) 2 else 1)).toLong()

        // 2. Update Theme dynamically in endless mode
        if (isEndlessMode) {
            val themeIndex = ((distanceMeters / 600).toInt()) % EnvironmentTheme.entries.size
            currentTheme = EnvironmentTheme.entries[themeIndex]
        }

        // 3. Update Camera Shake
        if (shakeIntensity > 0) {
            shakeOffsetX = (Random.nextFloat() * 2f - 1f) * shakeIntensity * 12f
            shakeOffsetY = (Random.nextFloat() * 2f - 1f) * shakeIntensity * 12f
            shakeIntensity = (shakeIntensity - dt * 2.5f).coerceAtLeast(0f)
        } else {
            shakeOffsetX = 0f
            shakeOffsetY = 0f
        }

        // 4. Update Player State & Movement
        player.runAnimTime += dt * (currentSpeed / 8f)
        player.lookBackTimer += dt
        if (player.lookBackTimer > 6.0f) {
            if (Random.nextFloat() < 0.4f) {
                player.lookBackTimer = 0f // Trigger looking back at chaser
            }
        }

        // Smooth Lane Interpolation
        val targetY = laneY[player.targetLane]
        player.currentLaneY += (targetY - player.currentLaneY) * (14f * dt).coerceAtMost(1f)

        // Jump Physics
        if (player.isJumping) {
            player.jumpY += -player.jumpVelocity
            player.jumpVelocity += 0.95f // Gravity

            if (player.jumpY <= 0f) {
                player.jumpY = 0f
                player.isJumping = false
                player.jumpVelocity = 0f
                spawnLandingDust(player.x, player.currentLaneY)
            }
        }

        // Slide Physics
        if (player.isSliding) {
            player.slideTimer -= dt
            if (player.slideTimer <= 0f) {
                player.isSliding = false
            }
        }

        // Invulnerability Hurt Flash
        if (player.isHurt) {
            player.hurtTimer -= dt
            if (player.hurtTimer <= 0f) {
                player.isHurt = false
            }
        }

        // Leaking fluttering money from the burlap bag while running!
        if (Random.nextFloat() < 0.18f) {
            spawnLeakingCash(player.x - 30f, player.currentLaneY - player.jumpY - 25f)
        }
        if (!player.isJumping && !player.isSliding && Random.nextFloat() < 0.35f) {
            spawnRunningDust(player.x - 20f, player.currentLaneY + 15f)
        }

        // 5. Update Active Power-ups
        val powerUpIter = activePowerUps.entries.iterator()
        while (powerUpIter.hasNext()) {
            val entry = powerUpIter.next()
            entry.value.remainingSeconds -= dt
            if (entry.value.remainingSeconds <= 0f) {
                powerUpIter.remove()
            }
        }

        // 6. Update Chaser Woman State
        val womanCatchPressure = difficulty.womanSpeed * (if (currentSpecialEvent == SpecialEventType.ANGRY_WOMAN_BOOST) 1.6f else 1.0f)
        chaser.animTime += dt * 9f
        chaser.angryFistTime += dt

        // Natural distance recovery when running cleanly, but pressure slowly pulls if obstacles hit
        chaser.distancePercent = (chaser.distancePercent + (0.012f * dt)).coerceAtMost(1.0f)

        // Audio tension sync
        soundManager.chaserTension = (1.0f - chaser.distancePercent).coerceIn(0f, 1f)

        // Shouts
        chaser.shoutTimer -= dt
        if (chaser.shoutTimer <= 0f) {
            chaser.shoutTimer = Random.nextFloat() * 5f + 4f
            val shoutsBn = arrayOf("এই দাঁড়া ব্যাটা!", "আমার টাকা ফেরত দে!", "আজ তোরে ছাড়ুম না!", "ধরা পরলে শেষ!", "টাকা নিয়ে কই যাস?!")
            val shoutsEn = arrayOf("Stop right there!", "Give my money back!", "I'll catch you!", "You can't escape!", "Drop the money bag!")
            val idx = Random.nextInt(shoutsBn.size)
            chaser.shoutMessageBn = shoutsBn[idx]
            chaser.shoutMessageEn = shoutsEn[idx]
            soundManager.playWomanShout()
        }

        // 7. Update Special Events
        if (currentSpecialEvent != null) {
            specialEventTimer -= dt
            if (specialEventTimer <= 0f) {
                currentSpecialEvent = null
            }
        } else if (distanceMeters >= nextSpecialEventDistance) {
            triggerRandomSpecialEvent()
            nextSpecialEventDistance = distanceMeters + Random.nextInt(250, 450)
        }

        // 8. Move and Update Obstacles
        val obstacleIter = obstacles.iterator()
        while (obstacleIter.hasNext()) {
            val ob = obstacleIter.next()
            ob.x -= frameMoveDistance

            // Check if avoided
            if (!ob.isPassed && ob.x < player.x - 60f) {
                ob.isPassed = true
                obstaclesAvoidedCount++
                score += 50
                // Successful dodge increases chaser gap slightly
                chaser.distancePercent = (chaser.distancePercent + 0.04f).coerceAtMost(1.0f)
            }

            // Check Collision
            if (!ob.isHit && !player.isHurt) {
                if (checkObstacleCollision(ob)) {
                    ob.isHit = true
                    handleObstacleHit(ob)
                }
            }

            // Remove offscreen
            if (ob.x < -200f) {
                obstacleIter.remove()
            }
        }

        // 9. Move and Update Collectibles (Coins & Notes)
        val hasMagnet = activePowerUps.containsKey(PowerUpType.MAGNET)
        val collectibleIter = collectibles.iterator()
        while (collectibleIter.hasNext()) {
            val col = collectibleIter.next()
            col.x -= frameMoveDistance
            col.animOffset += dt * 5f

            // Magnet attraction
            if (hasMagnet && !col.isCollected && col.x > player.x - 100f && col.x < player.x + 450f) {
                val pY = player.currentLaneY - player.jumpY
                val colY = laneY[col.lane] + col.yOffset
                val dx = player.x - col.x
                val dy = pY - colY
                col.x += dx * 8f * dt
                col.animOffset += dy * 8f * dt
                col.magnetizing = true
            }

            // Check Pickup Collision
            if (!col.isCollected && checkCollectiblePickup(col)) {
                col.isCollected = true
                handleCollectiblePickup(col)
            }

            if (col.x < -100f || col.isCollected) {
                collectibleIter.remove()
            }
        }

        // 10. Move and Update Power-Up Items
        val powerIter = powerUpItems.iterator()
        while (powerIter.hasNext()) {
            val pw = powerIter.next()
            pw.x -= frameMoveDistance
            pw.animOffset += dt * 4f

            if (!pw.isCollected && checkPowerUpPickup(pw)) {
                pw.isCollected = true
                handlePowerUpPickup(pw)
            }

            if (pw.x < -100f || pw.isCollected) {
                powerIter.remove()
            }
        }

        // 11. Update Particles
        val partIter = particles.iterator()
        while (partIter.hasNext()) {
            val p = partIter.next()
            p.life -= dt
            p.x += p.vx * dt * 60f
            p.y += p.vy * dt * 60f
            p.rotation += p.vRot * dt
            p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)

            if (p.life <= 0f) {
                partIter.remove()
            }
        }

        // 12. Procedural Spawner
        nextSpawnDistance -= frameMoveDistance
        if (nextSpawnDistance <= 0f) {
            spawnNextWorldBatch()
            nextSpawnDistance = Random.nextFloat() * 120f + (if (difficulty == Difficulty.HARD) 130f else 180f)
        }

        // 13. Level Completion Check (Level Mode)
        if (!isEndlessMode && currentLevel != null) {
            if (distanceMeters >= currentLevel!!.targetDistance) {
                handleLevelComplete()
                return
            }
        }

        // 14. Check Game Over (Hearts depleted or Chaser reached player)
        if (hearts <= 0 || chaser.distancePercent <= 0.05f) {
            triggerCaughtGameOver()
        }
    }

    private fun checkObstacleCollision(ob: Obstacle): Boolean {
        // Player X bounds: 280f +/- 35f
        val pLeft = player.x - 30f
        val pRight = player.x + 30f
        val obLeft = ob.x - ob.type.width / 2f
        val obRight = ob.x + ob.type.width / 2f

        // Horizontal overlap
        if (pRight < obLeft || pLeft > obRight) return false

        // Lane match (if vehicle/barrier is on player's lane)
        if (ob.lane != player.targetLane) return false

        // Height logic:
        if (ob.type.isHigh) {
            // High obstacle: safe ONLY if sliding!
            if (player.isSliding) {
                return false // Passed safely under!
            }
            return true // Hit high bar!
        } else {
            // Low obstacle: safe if jumped high enough
            if (player.isJumping && player.jumpY > 35f) {
                return false // Successfully jumped over!
            }
            return true // Tripped on low barrier!
        }
    }

    private fun handleObstacleHit(ob: Obstacle) {
        // Check Shield
        if (activePowerUps.containsKey(PowerUpType.SHIELD)) {
            activePowerUps.remove(PowerUpType.SHIELD)
            soundManager.playShieldHit()
            shakeIntensity = 0.5f
            spawnSparkles(player.x, player.currentLaneY - 30f, Color(0xFF00E5FF), 15)
            return
        }

        // Check Speed Boost Invincibility
        if (activePowerUps.containsKey(PowerUpType.SPEED_BOOST)) {
            soundManager.playCrash()
            shakeIntensity = 0.4f
            spawnSparkles(ob.x, laneY[ob.lane], Color(0xFFFFD600), 12)
            return
        }

        // Take Hit
        hearts--
        player.isHurt = true
        player.hurtTimer = 1.6f
        comboCount = 0
        comboMultiplier = 1
        shakeIntensity = 0.8f

        // Chaser gets much closer!
        chaser.distancePercent = (chaser.distancePercent - 0.32f).coerceAtLeast(0f)
        chaser.isLunging = true

        soundManager.playCrash()
        spawnHurtStars(player.x, player.currentLaneY - player.jumpY - 40f)

        // Drop money notes in panic!
        for (i in 0..5) {
            spawnLeakingCash(player.x, player.currentLaneY - 30f)
        }
    }

    private fun checkCollectiblePickup(col: Collectible): Boolean {
        val pY = player.currentLaneY - player.jumpY
        val colY = laneY[col.lane] + col.yOffset
        val dx = abs(player.x - col.x)
        val dy = abs(pY - colY)
        return dx < 48f && dy < 48f
    }

    private fun handleCollectiblePickup(col: Collectible) {
        val mult = if (activePowerUps.containsKey(PowerUpType.COIN_MULTIPLIER)) 2 else 1
        coinsCollected += col.type.coinValue * mult
        moneyCollected += col.type.moneyValue * mult
        score += col.type.scoreValue * comboMultiplier * mult

        // Combo increment
        comboCount++
        comboMultiplier = when {
            comboCount >= 50 -> 5
            comboCount >= 25 -> 3
            comboCount >= 10 -> 2
            else -> 1
        }

        if (col.type.coinValue > 0) {
            soundManager.playCoin()
            spawnSparkles(col.x, laneY[col.lane] + col.yOffset, Color(0xFFFFD700), 6)
        } else {
            soundManager.playMoney()
            val txt = "+৳${col.type.moneyValue}"
            spawnFloatingText(col.x, laneY[col.lane] + col.yOffset - 20f, txt, col.type.color)
            spawnSparkles(col.x, laneY[col.lane] + col.yOffset, col.type.color, 8)
        }
    }

    private fun checkPowerUpPickup(pw: PowerUpItem): Boolean {
        val pY = player.currentLaneY - player.jumpY
        val pwY = laneY[pw.lane] + pw.yOffset
        val dx = abs(player.x - pw.x)
        val dy = abs(pY - pwY)
        return dx < 50f && dy < 50f
    }

    private fun handlePowerUpPickup(pw: PowerUpItem) {
        powerupsCollectedCount++
        soundManager.playPowerUp()
        shakeIntensity = 0.3f

        val duration = when (pw.type) {
            PowerUpType.MAGNET -> pw.type.durationSeconds + (preferences.magnetLevel - 1) * 2f
            PowerUpType.SHIELD -> pw.type.durationSeconds + (preferences.shieldLevel - 1) * 3f
            PowerUpType.SPEED_BOOST -> pw.type.durationSeconds + (preferences.speedBoostLevel - 1) * 1.5f
            PowerUpType.COIN_MULTIPLIER -> pw.type.durationSeconds + (preferences.multiplierLevel - 1) * 2f
            else -> pw.type.durationSeconds
        }

        activePowerUps[pw.type] = ActivePowerUp(pw.type, duration, duration)
        spawnFloatingText(pw.x, laneY[pw.lane] - 30f, "${pw.type.iconEmoji} ${pw.type.titleBn}", pw.type.color)
        spawnSparkles(pw.x, laneY[pw.lane], pw.type.color, 15)

        // If Money Rain, instantly spawn flying notes
        if (pw.type == PowerUpType.MONEY_RAIN) {
            triggerMoneyRain()
        }
    }

    private fun triggerRandomSpecialEvent() {
        val events = SpecialEventType.entries.toTypedArray()
        val ev = events[Random.nextInt(events.size)]
        currentSpecialEvent = ev
        specialEventTimer = ev.durationSeconds
        soundManager.playPowerUp()

        when (ev) {
            SpecialEventType.MONEY_STORM -> triggerMoneyRain()
            SpecialEventType.GOLDEN_ROAD -> spawnGoldenRoadPattern()
            SpecialEventType.ANGRY_WOMAN_BOOST -> {
                chaser.shoutMessageBn = "এইবার তোর একদিন কি আমার একদিন!"
                chaser.shoutMessageEn = "No more mercy!"
                soundManager.playWomanShout()
                shakeIntensity = 0.6f
            }
            SpecialEventType.TRAFFIC_CHAOS -> spawnTrafficWave()
            SpecialEventType.SUPER_RUN -> {
                activePowerUps[PowerUpType.SPEED_BOOST] = ActivePowerUp(PowerUpType.SPEED_BOOST, 6f, 6f)
            }
        }
    }

    private fun triggerMoneyRain() {
        for (i in 0..18) {
            val lane = Random.nextInt(3)
            val spawnX = 1350f + i * 75f
            val noteType = if (i % 3 == 0) CollectibleType.NOTE_1000 else CollectibleType.NOTE_500
            val yOff = if (i % 2 == 0) -60f else -10f
            collectibles.add(Collectible(collectibleIdCounter++, spawnX, lane, yOff, noteType))
        }
    }

    private fun spawnGoldenRoadPattern() {
        for (lane in 0..2) {
            for (i in 0..10) {
                val spawnX = 1350f + i * 55f
                collectibles.add(Collectible(collectibleIdCounter++, spawnX, lane, -10f, CollectibleType.NOTE_500))
            }
        }
    }

    private fun spawnTrafficWave() {
        for (i in 0..3) {
            val lane = i % 3
            val spawnX = 1350f + i * 220f
            val vType = if (i % 2 == 0) ObstacleType.CNG_RICKSHAW else ObstacleType.CAR
            obstacles.add(Obstacle(obstacleIdCounter++, spawnX, lane, vType))
        }
    }

    // Procedural Spawning Logic
    private fun spawnNextWorldBatch() {
        val spawnX = 1350f
        val rand = Random.nextFloat()

        // 1. Spawning Obstacles & Barricades
        if (rand < 0.72f) {
            val lane = Random.nextInt(3)
            val isHighObstacle = Random.nextFloat() < 0.38f

            val obType = if (isHighObstacle) {
                val highTypes = arrayOf(ObstacleType.HIGH_BEAM, ObstacleType.CLOTHES_LINE, ObstacleType.OVERHEAD_SIGN)
                highTypes[Random.nextInt(highTypes.size)]
            } else {
                when (currentTheme) {
                    EnvironmentTheme.CITY, EnvironmentTheme.HIGHWAY -> {
                        val cityTypes = arrayOf(ObstacleType.LOW_BARRIER, ObstacleType.CNG_RICKSHAW, ObstacleType.TRAFFIC_CONE, ObstacleType.CAR)
                        cityTypes[Random.nextInt(cityTypes.size)]
                    }
                    EnvironmentTheme.RAIN -> {
                        val rainTypes = arrayOf(ObstacleType.LOW_BARRIER, ObstacleType.WATER_PUDDLE, ObstacleType.BROKEN_ROAD, ObstacleType.TRAFFIC_CONE)
                        rainTypes[Random.nextInt(rainTypes.size)]
                    }
                    EnvironmentTheme.VILLAGE, EnvironmentTheme.BAZAAR -> {
                        val vilTypes = arrayOf(ObstacleType.LOW_BARRIER, ObstacleType.WOODEN_BOX, ObstacleType.DOG, ObstacleType.BROKEN_ROAD)
                        vilTypes[Random.nextInt(vilTypes.size)]
                    }
                    else -> {
                        val defTypes = arrayOf(ObstacleType.LOW_BARRIER, ObstacleType.TRAFFIC_CONE, ObstacleType.WOODEN_BOX)
                        defTypes[Random.nextInt(defTypes.size)]
                    }
                }
            }

            obstacles.add(Obstacle(obstacleIdCounter++, spawnX, lane, obType))

            // Guide Taka Notes
            if (isHighObstacle) {
                // Ground notes under high obstacle for sliding under
                collectibles.add(Collectible(collectibleIdCounter++, spawnX, lane, 10f, CollectibleType.NOTE_500))
            } else {
                // Jump arc notes over low obstacle
                collectibles.add(Collectible(collectibleIdCounter++, spawnX, lane, -85f, CollectibleType.NOTE_1000))
                collectibles.add(Collectible(collectibleIdCounter++, spawnX + 40f, lane, -75f, CollectibleType.NOTE_500))
            }
        }

        // 2. Spawning Taka Formations (100, 500, 1000 Notes, Bundles, Bags)
        val patternType = Random.nextInt(5)
        val pLane = Random.nextInt(3)

        when (patternType) {
            0 -> { // Straight line of Taka notes
                for (i in 0..4) {
                    collectibles.add(Collectible(collectibleIdCounter++, spawnX + i * 45f, pLane, -10f, if (i % 2 == 0) CollectibleType.NOTE_500 else CollectibleType.NOTE_100))
                }
            }
            1 -> { // Jump Arc of Taka notes
                for (i in 0..4) {
                    val arcY = -sin(i.toFloat() / 4f * 3.14159f) * 85f - 10f
                    val type = if (i == 2) CollectibleType.NOTE_1000 else CollectibleType.NOTE_500
                    collectibles.add(Collectible(collectibleIdCounter++, spawnX + i * 45f, pLane, arcY, type))
                }
            }
            2 -> { // Money Note Trail
                for (i in 0..3) {
                    val type = if (i % 2 == 0) CollectibleType.NOTE_500 else CollectibleType.NOTE_1000
                    collectibles.add(Collectible(collectibleIdCounter++, spawnX + i * 60f, pLane, -15f, type))
                }
            }
            3 -> { // Rare Money Bundle / Big Bag of Taka
                val rareType = if (Random.nextFloat() < 0.25f) CollectibleType.LARGE_BAG_10000 else CollectibleType.BUNDLE_5000
                collectibles.add(Collectible(collectibleIdCounter++, spawnX + 50f, pLane, -15f, rareType))
            }
            4 -> { // Multi-lane stair
                for (l in 0..2) {
                    collectibles.add(Collectible(collectibleIdCounter++, spawnX + l * 55f, l, -10f, CollectibleType.NOTE_100))
                }
            }
        }

        // 3. Spawning Power-ups
        if (Random.nextFloat() < 0.12f && powerUpItems.size < 2) {
            val pwLane = Random.nextInt(3)
            val pTypes = PowerUpType.entries.toTypedArray()
            val selectedPw = pTypes[Random.nextInt(pTypes.size)]
            powerUpItems.add(PowerUpItem(powerUpIdCounter++, spawnX + 150f, pwLane, -20f, selectedPw))
        }
    }

    private fun handleLevelComplete() {
        screenState = ScreenState.LEVEL_COMPLETE
        soundManager.playLevelComplete()

        val lvl = currentLevel!!
        val stars = when {
            hearts == 3 && score > lvl.targetDistance * 12 -> 3
            hearts >= 2 -> 2
            else -> 1
        }

        preferences.setLevelStars(lvl.levelNumber, stars)
        if (lvl.levelNumber == preferences.highestLevelUnlocked && lvl.levelNumber < 100) {
            preferences.highestLevelUnlocked = lvl.levelNumber + 1
        }

        preferences.saveRunScore(score, distanceMeters.toInt(), coinsCollected, moneyCollected)
        preferences.updateMissionProgress(
            dist = distanceMeters.toInt(),
            coins = coinsCollected,
            money = moneyCollected,
            obstacles = obstaclesAvoidedCount,
            slides = slidesPerformedCount,
            powerups = powerupsCollectedCount
        )
    }

    private fun triggerCaughtGameOver() {
        screenState = ScreenState.CAUGHT_GAMEOVER
        player.isCaught = true
        player.caughtProgress = 0f
        caughtTimer = 0f
        soundManager.playCatchGameOver()
        shakeIntensity = 1.0f

        // Massive cash explosion as bag drops
        for (i in 0..25) {
            spawnLeakingCash(player.x + (Random.nextFloat() * 80f - 40f), player.currentLaneY - 30f)
        }

        preferences.saveRunScore(score, distanceMeters.toInt(), coinsCollected, moneyCollected)
        preferences.updateMissionProgress(
            dist = distanceMeters.toInt(),
            coins = coinsCollected,
            money = moneyCollected,
            obstacles = obstaclesAvoidedCount,
            slides = slidesPerformedCount,
            powerups = powerupsCollectedCount
        )
    }

    private fun updateCaughtAnimation(dt: Float) {
        caughtTimer += dt
        player.caughtProgress = (caughtTimer / 1.5f).coerceIn(0f, 1f)
        chaser.animTime += dt * 5f

        // Continue updating particles in game over
        val partIter = particles.iterator()
        while (partIter.hasNext()) {
            val p = partIter.next()
            p.life -= dt
            p.x += p.vx * dt * 60f
            p.y += p.vy * dt * 60f
            p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            if (p.life <= 0f) partIter.remove()
        }
    }

    // Particle Creators
    private fun spawnRunningDust(x: Float, y: Float) {
        particles.add(
            Particle(
                x = x,
                y = y,
                vx = -Random.nextFloat() * 3f - 2f,
                vy = -Random.nextFloat() * 1.5f,
                color = Color(0x66FFFFFF),
                size = Random.nextFloat() * 8f + 5f,
                life = 0.35f,
                maxLife = 0.35f,
                shape = ParticleShape.SMOKE_DUST
            )
        )
    }

    private fun spawnJumpDust(x: Float, y: Float) {
        for (i in 0..5) {
            particles.add(
                Particle(
                    x = x + (Random.nextFloat() * 30f - 15f),
                    y = y + 15f,
                    vx = (Random.nextFloat() * 4f - 2f),
                    vy = -Random.nextFloat() * 3f,
                    color = Color(0x99FFFFFF),
                    size = Random.nextFloat() * 10f + 6f,
                    life = 0.45f,
                    maxLife = 0.45f,
                    shape = ParticleShape.SMOKE_DUST
                )
            )
        }
    }

    private fun spawnLandingDust(x: Float, y: Float) {
        for (i in 0..8) {
            val dir = if (i % 2 == 0) -1f else 1f
            particles.add(
                Particle(
                    x = x + dir * Random.nextFloat() * 15f,
                    y = y + 15f,
                    vx = dir * (Random.nextFloat() * 4f + 2f),
                    vy = -Random.nextFloat() * 2f,
                    color = Color(0xAAFFFFFF),
                    size = Random.nextFloat() * 12f + 8f,
                    life = 0.5f,
                    maxLife = 0.5f,
                    shape = ParticleShape.SMOKE_DUST
                )
            )
        }
    }

    private fun spawnSlideDust(x: Float, y: Float) {
        for (i in 0..7) {
            particles.add(
                Particle(
                    x = x - 20f + (Random.nextFloat() * 20f),
                    y = y + 10f,
                    vx = -Random.nextFloat() * 6f - 3f,
                    vy = -Random.nextFloat() * 2.5f,
                    color = Color(0x88FFA726),
                    size = Random.nextFloat() * 10f + 6f,
                    life = 0.4f,
                    maxLife = 0.4f,
                    shape = ParticleShape.SMOKE_DUST
                )
            )
        }
    }

    private fun spawnPuff(x: Float, y: Float) {
        for (i in 0..4) {
            particles.add(
                Particle(
                    x = x + (Random.nextFloat() * 20f - 10f),
                    y = y,
                    vx = (Random.nextFloat() * 3f - 1.5f),
                    vy = (Random.nextFloat() * 3f - 1.5f),
                    color = Color(0x66FFFFFF),
                    size = Random.nextFloat() * 8f + 4f,
                    life = 0.3f,
                    maxLife = 0.3f,
                    shape = ParticleShape.SMOKE_DUST
                )
            )
        }
    }

    private fun spawnLeakingCash(x: Float, y: Float) {
        val colors = arrayOf(Color(0xFF00E676), Color(0xFFFF4081), Color(0xFFFFD54F))
        particles.add(
            Particle(
                x = x,
                y = y,
                vx = -Random.nextFloat() * 4f - 2f,
                vy = -Random.nextFloat() * 3f + 1f,
                color = colors[Random.nextInt(colors.size)],
                size = 14f,
                life = 0.7f,
                maxLife = 0.7f,
                shape = ParticleShape.MONEY_NOTE,
                rotation = Random.nextFloat() * 360f,
                vRot = (Random.nextFloat() * 360f - 180f)
            )
        )
    }

    private fun spawnSparkles(x: Float, y: Float, color: Color, count: Int) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * 3.14159f
            val speed = Random.nextFloat() * 4f + 1.5f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    size = Random.nextFloat() * 6f + 3f,
                    life = 0.45f,
                    maxLife = 0.45f,
                    shape = ParticleShape.STAR
                )
            )
        }
    }

    private fun spawnHurtStars(x: Float, y: Float) {
        for (i in 0..10) {
            val angle = Random.nextFloat() * 2f * 3.14159f
            val speed = Random.nextFloat() * 5f + 2f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = Color(0xFFFFEB3B),
                    size = Random.nextFloat() * 8f + 5f,
                    life = 0.6f,
                    maxLife = 0.6f,
                    shape = ParticleShape.STAR
                )
            )
        }
    }

    private fun spawnFloatingText(x: Float, y: Float, text: String, color: Color) {
        particles.add(
            Particle(
                x = x,
                y = y,
                vx = 0f,
                vy = -1.8f,
                color = color,
                size = 18f,
                life = 0.85f,
                maxLife = 0.85f,
                shape = ParticleShape.TEXT,
                text = text
            )
        )
    }
}
