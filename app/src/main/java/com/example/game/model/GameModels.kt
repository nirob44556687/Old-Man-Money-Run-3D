package com.example.game.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.GameMoneyGold
import com.example.ui.theme.GameMoneyGreen
import com.example.ui.theme.GameMoneyPink

enum class ScreenState {
    MENU,
    LEVEL_SELECT,
    PLAYING,
    PAUSED,
    CAUGHT_GAMEOVER,
    LEVEL_COMPLETE,
    SHOP,
    MISSIONS,
    LEADERBOARD,
    SETTINGS
}

enum class Difficulty(val labelEn: String, val labelBn: String, val speedMultiplier: Float, val womanSpeed: Float) {
    EASY("Easy", "সহজ", 0.85f, 0.7f),
    NORMAL("Normal", "সাধারণ", 1.0f, 1.0f),
    HARD("Hard", "কঠিন", 1.25f, 1.35f)
}

enum class ObstacleType(
    val isHigh: Boolean,
    val width: Float,
    val height: Float,
    val labelBn: String
) {
    LOW_BARRIER(false, 65f, 50f, "রোড ব্যারিয়ার"),
    TRAFFIC_CONE(false, 40f, 45f, "ট্রাফিক কোন"),
    WOODEN_BOX(false, 60f, 55f, "কাঠের বাক্স"),
    BROKEN_ROAD(false, 90f, 35f, "ভাঙা রাস্তা"),
    WATER_PUDDLE(false, 80f, 30f, "পানির গর্ত"),
    DOG(false, 55f, 40f, "কুকুর"),
    
    // High obstacles that require slide
    HIGH_BEAM(true, 75f, 90f, "উঁচু পাইপ"),
    CLOTHES_LINE(true, 85f, 85f, "কাপড়ের দড়ি"),
    OVERHEAD_SIGN(true, 80f, 95f, "রোড সাইনবোর্ড"),
    
    // Vehicles / dynamic road obstacles
    CNG_RICKSHAW(false, 110f, 75f, "সিএনজি অটোরিকশা"),
    CAR(false, 130f, 70f, "প্রাইভেট কার"),
    BUS(false, 180f, 90f, "বাস")
}

enum class CollectibleType(
    val moneyValue: Int,
    val scoreValue: Int,
    val color: Color
) {
    NOTE_100(100, 20, Color(0xFF00B0FF)),      // ৳১০০ নোট (নীল/সায়ান)
    NOTE_500(500, 50, GameMoneyGreen),         // ৳৫০০ নোট (সবুজ)
    NOTE_1000(1000, 100, GameMoneyPink),       // ৳১০০০ নোট (গোলাপি/লালচে)
    BUNDLE_5000(5000, 300, Color(0xFF00E676)), // ৳৫০০০ টাকার বান্ডিল
    LARGE_BAG_10000(10000, 1000, Color(0xFFFFD54F)); // ৳১০০০০ টাকার বস্তা

    // Backward compatibility property for coinValue
    val coinValue: Int get() = moneyValue / 100
}

enum class PowerUpType(
    val titleBn: String,
    val titleEn: String,
    val iconEmoji: String,
    val durationSeconds: Float,
    val color: Color
) {
    MAGNET("চৌম্বক", "Magnet", "🧲", 10f, Color(0xFFFF3D00)),
    SHIELD("ঢাল", "Shield", "🛡️", 12f, Color(0xFF00E5FF)),
    SPEED_BOOST("সুপার দৌড়", "Speed Boost", "🚀", 6f, Color(0xFFFFEA00)),
    COIN_MULTIPLIER("২ গুণ টাকা", "2x Taka", "✖️2", 12f, Color(0xFFFF007F)),
    SUPER_JUMP("সুপার লাফ", "Super Jump", "🦘", 10f, Color(0xFF76FF03)),
    MONEY_RAIN("টাকার বৃষ্টি", "Money Rain", "🌧️", 8f, Color(0xFF00E676))
}

enum class EnvironmentTheme(
    val titleBn: String,
    val titleEn: String,
    val skyColorTop: Color,
    val skyColorBottom: Color,
    val roadColor: Color,
    val isNight: Boolean,
    val hasRain: Boolean
) {
    VILLAGE("গ্রামের রাস্তা", "Village Road", Color(0xFF4FC3F7), Color(0xFFE1F5FE), Color(0xFF37474F), false, false),
    CITY("শহরের রাস্তা", "City Highway", Color(0xFF29B6F6), Color(0xFFFFF9C4), Color(0xFF263238), false, false),
    BAZAAR("বাজার এলাকা", "Market Road", Color(0xFFFFB74D), Color(0xFFFFF3E0), Color(0xFF3E2723), false, false),
    BRIDGE("সেতু / ব্রিজ", "Bridge Over River", Color(0xFF0288D1), Color(0xFF81D4FA), Color(0xFF212121), false, false),
    NIGHT("রাতের রাস্তা", "Night Express", Color(0xFF0A0E27), Color(0xFF1A237E), Color(0xFF1E1E24), true, false),
    RAIN("বৃষ্টির রাস্তা", "Rainy Storm", Color(0xFF37474F), Color(0xFF546E7A), Color(0xFF212121), false, true),
    HIGHWAY("দ্রুত হাইওয়ে", "Speed Highway", Color(0xFFFF7043), Color(0xFFFFE082), Color(0xFF263238), false, false),
    DANGER("বিপজ্জনক এলাকা", "Danger Zone", Color(0xFFD32F2F), Color(0xFFFFCDD2), Color(0xFF1A1A1A), false, false)
}

enum class SpecialEventType(
    val titleBn: String,
    val titleEn: String,
    val emoji: String,
    val durationSeconds: Float
) {
    MONEY_STORM("টাকার তুফান!", "MONEY STORM!", "💸", 7f),
    ANGRY_WOMAN_BOOST("বুড়ি রেগে গেছে!", "ANGRY WOMAN BOOST!", "👵⚡", 5f),
    GOLDEN_ROAD("স্বর্ণের রাস্তা!", "GOLDEN ROAD!", "✨", 8f),
    TRAFFIC_CHAOS("ট্রাফিক জ্যাম!", "TRAFFIC CHAOS!", "🚗", 6f),
    SUPER_RUN("ঝড়ের গতি!", "SUPER SPRINT!", "⚡", 6f)
}

data class PlayerState(
    var x: Float = 220f,
    var targetLane: Int = 1, // 0: Top, 1: Middle, 2: Bottom
    var currentLaneY: Float = 0f,
    var isJumping: Boolean = false,
    var jumpY: Float = 0f,
    var jumpVelocity: Float = 0f,
    var isSliding: Boolean = false,
    var slideTimer: Float = 0f,
    var isHurt: Boolean = false,
    var hurtTimer: Float = 0f,
    var lookBackTimer: Float = 0f,
    var runAnimTime: Float = 0f,
    var doubleJumpAvailable: Boolean = false,
    var isCaught: Boolean = false,
    var caughtProgress: Float = 0f
)

data class ChaserState(
    var distancePercent: Float = 0.85f, // 1.0 is far behind, 0.0 is caught
    var animTime: Float = 0f,
    var angryFistTime: Float = 0f,
    var shoutMessageBn: String = "",
    var shoutMessageEn: String = "",
    var shoutTimer: Float = 0f,
    var isLunging: Boolean = false
)

data class Obstacle(
    val id: Long,
    var x: Float,
    val lane: Int,
    val type: ObstacleType,
    var isPassed: Boolean = false,
    var isHit: Boolean = false
)

data class Collectible(
    val id: Long,
    var x: Float,
    val lane: Int,
    val yOffset: Float,
    val type: CollectibleType,
    var isCollected: Boolean = false,
    var animOffset: Float = 0f,
    var magnetizing: Boolean = false
)

data class PowerUpItem(
    val id: Long,
    var x: Float,
    val lane: Int,
    val yOffset: Float,
    val type: PowerUpType,
    var isCollected: Boolean = false,
    var animOffset: Float = 0f
)

data class ActivePowerUp(
    val type: PowerUpType,
    var remainingSeconds: Float,
    val totalSeconds: Float
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var alpha: Float = 1f,
    var size: Float,
    var life: Float,
    var maxLife: Float,
    var shape: ParticleShape = ParticleShape.CIRCLE,
    var rotation: Float = 0f,
    var vRot: Float = 0f,
    var text: String? = null
)

enum class ParticleShape {
    CIRCLE,
    STAR,
    MONEY_NOTE,
    COIN,
    SMOKE_DUST,
    TEXT
}

data class LevelInfo(
    val levelNumber: Int,
    val nameBn: String,
    val nameEn: String,
    val theme: EnvironmentTheme,
    val targetDistance: Int, // meters
    val isUnlocked: Boolean = false,
    val stars: Int = 0,
    val bestScore: Long = 0
)

data class MissionItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val target: Int,
    val progress: Int,
    val rewardCoins: Int,
    val rewardMoney: Int,
    val isClaimed: Boolean = false
) {
    val isCompleted: Boolean get() = progress >= target
}

data class ShopItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val priceCoins: Int,
    val priceMoney: Int,
    val category: ShopCategory,
    val isUnlocked: Boolean = false,
    val isEquipped: Boolean = false,
    val iconEmoji: String = "✨"
)

enum class ShopCategory {
    OUTFIT,
    MONEY_BAG,
    UPGRADE
}

data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val score: Long,
    val distance: Int,
    val coins: Int,
    val date: String
)
