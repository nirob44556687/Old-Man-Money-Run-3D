package com.example.game.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.game.model.Difficulty
import com.example.game.model.EnvironmentTheme
import com.example.game.model.LevelInfo
import com.example.game.model.MissionItem
import com.example.game.model.ShopCategory
import com.example.game.model.ShopItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bura_takar_bag_prefs", Context.MODE_PRIVATE)

    // Economy & High Scores
    var bestScore: Long
        get() = prefs.getLong("best_score", 0L)
        set(value) = prefs.edit().putLong("best_score", value).apply()

    var totalDistance: Int
        get() = prefs.getInt("total_distance", 0)
        set(value) = prefs.edit().putInt("total_distance", value).apply()

    var totalCoins: Int
        get() = prefs.getInt("total_coins", 0)
        set(value) = prefs.edit().putInt("total_coins", value).apply()

    var totalMoney: Long
        get() = prefs.getLong("total_money", 0L)
        set(value) = prefs.edit().putLong("total_money", value).apply()

    var highestLevelUnlocked: Int
        get() = prefs.getInt("highest_level_unlocked", 1)
        set(value) = prefs.edit().putInt("highest_level_unlocked", value).apply()

    // Settings
    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var musicEnabled: Boolean
        get() = prefs.getBoolean("music_enabled", true)
        set(value) = prefs.edit().putBoolean("music_enabled", value).apply()

    var onScreenButtonsEnabled: Boolean
        get() = prefs.getBoolean("onscreen_buttons", true)
        set(value) = prefs.edit().putBoolean("onscreen_buttons", value).apply()

    var languageIsBangla: Boolean
        get() = prefs.getBoolean("lang_is_bangla", true)
        set(value) = prefs.edit().putBoolean("lang_is_bangla", value).apply()

    var difficulty: Difficulty
        get() {
            val name = prefs.getString("difficulty", Difficulty.NORMAL.name) ?: Difficulty.NORMAL.name
            return try {
                Difficulty.valueOf(name)
            } catch (e: Exception) {
                Difficulty.NORMAL
            }
        }
        set(value) = prefs.edit().putString("difficulty", value.name).apply()

    // Equipped customizations
    var equippedOutfit: String
        get() = prefs.getString("equipped_outfit", "outfit_classic") ?: "outfit_classic"
        set(value) = prefs.edit().putString("equipped_outfit", value).apply()

    var equippedBag: String
        get() = prefs.getString("equipped_bag", "bag_burlap") ?: "bag_burlap"
        set(value) = prefs.edit().putString("equipped_bag", value).apply()

    // Upgrades (levels 1-5)
    var magnetLevel: Int
        get() = prefs.getInt("upgrade_magnet", 1)
        set(value) = prefs.edit().putInt("upgrade_magnet", value).apply()

    var shieldLevel: Int
        get() = prefs.getInt("upgrade_shield", 1)
        set(value) = prefs.edit().putInt("upgrade_shield", value).apply()

    var speedBoostLevel: Int
        get() = prefs.getInt("upgrade_speed_boost", 1)
        set(value) = prefs.edit().putInt("upgrade_speed_boost", value).apply()

    var multiplierLevel: Int
        get() = prefs.getInt("upgrade_multiplier", 1)
        set(value) = prefs.edit().putInt("upgrade_multiplier", value).apply()

    // Stars per level
    fun getLevelStars(levelNum: Int): Int = prefs.getInt("level_${levelNum}_stars", 0)

    fun setLevelStars(levelNum: Int, stars: Int) {
        val current = getLevelStars(levelNum)
        if (stars > current) {
            prefs.edit().putInt("level_${levelNum}_stars", stars).apply()
        }
    }

    // Unlocked items
    fun isItemUnlocked(id: String): Boolean {
        if (id == "outfit_classic" || id == "bag_burlap") return true
        return prefs.getBoolean("unlocked_$id", false)
    }

    fun setItemUnlocked(id: String, unlocked: Boolean = true) {
        prefs.edit().putBoolean("unlocked_$id", unlocked).apply()
    }

    // Generate Level list up to Level 100
    fun getLevels(): List<LevelInfo> {
        val levelNamesBn = arrayOf(
            "গ্রামের কাঁচা রাস্তা", "শহরের হাইওয়ে", "ব্যস্ত বাজার এলাকা", "পদ্মা সেতু / নদী ব্রিজ",
            "রাতের হাই স্পিড রোড", "ঝড়ো বৃষ্টির রাস্তা", "ট্রাফিক জ্যাম এলাকা", "শিল্প এলাকা",
            "পাহাড়ী আঁকাবাঁকা পথ", "বিপজ্জনক স্পিড ট্র‍্যাক"
        )
        val levelNamesEn = arrayOf(
            "Village Dirt Road", "City Expressway", "Crowded Bazaar", "Mega Bridge",
            "Night Highway", "Rainy Stormway", "Traffic Chaos", "Industrial Zone",
            "Hilly Curves", "Danger Speed Track"
        )
        val themes = arrayOf(
            EnvironmentTheme.VILLAGE,
            EnvironmentTheme.CITY,
            EnvironmentTheme.BAZAAR,
            EnvironmentTheme.BRIDGE,
            EnvironmentTheme.NIGHT,
            EnvironmentTheme.RAIN,
            EnvironmentTheme.CITY,
            EnvironmentTheme.HIGHWAY,
            EnvironmentTheme.VILLAGE,
            EnvironmentTheme.DANGER
        )

        val list = ArrayList<LevelInfo>(100)
        val highestUnlocked = highestLevelUnlocked

        for (i in 1..100) {
            val themeIdx = (i - 1) % themes.size
            val nameSuffix = if (i > 10) " ${((i - 1) / 10) + 1}" else ""
            val nameBn = "লেভেল $i: ${levelNamesBn[themeIdx]}$nameSuffix"
            val nameEn = "Level $i: ${levelNamesEn[themeIdx]}$nameSuffix"
            val targetDist = 400 + (i * 120)

            list.add(
                LevelInfo(
                    levelNumber = i,
                    nameBn = nameBn,
                    nameEn = nameEn,
                    theme = themes[themeIdx],
                    targetDistance = targetDist,
                    isUnlocked = i <= highestUnlocked,
                    stars = getLevelStars(i)
                )
            )
        }
        return list
    }

    // Default & Loaded Missions
    fun getMissions(): List<MissionItem> {
        val m1 = prefs.getInt("mission_dist_run", 0)
        val m2 = prefs.getInt("mission_coins_collected", 0)
        val m3 = prefs.getInt("mission_money_collected", 0)
        val m4 = prefs.getInt("mission_obstacles_avoided", 0)
        val m5 = prefs.getInt("mission_slides_performed", 0)
        val m6 = prefs.getInt("mission_powerups_collected", 0)

        return listOf(
            MissionItem(
                id = "m_run_500",
                titleBn = "৫০০ মিটার দৌড়াও",
                titleEn = "Run 500 Meters",
                descriptionBn = "যেকোনো এক গেমে ৫০০ মিটার রাস্তা অতিক্রম করো",
                descriptionEn = "Reach 500 meters in a single run",
                target = 500,
                progress = m1.coerceAtMost(500),
                rewardCoins = 50,
                rewardMoney = 2500,
                isClaimed = prefs.getBoolean("claimed_m_run_500", false)
            ),
            MissionItem(
                id = "m_coins_100",
                titleBn = "১০০টি স্বর্ণমুদ্রা সংগ্রহ",
                titleEn = "Collect 100 Coins",
                descriptionBn = "রাস্তা থেকে মোট ১০০টি স্বর্ণমুদ্রা জড়ো করো",
                descriptionEn = "Collect a total of 100 gold coins",
                target = 100,
                progress = m2.coerceAtMost(100),
                rewardCoins = 100,
                rewardMoney = 5000,
                isClaimed = prefs.getBoolean("claimed_m_coins_100", false)
            ),
            MissionItem(
                id = "m_money_50k",
                titleBn = "৫০,০০০ টাকার বান্ডিল",
                titleEn = "Collect ৳50,000",
                descriptionBn = "ব্যাগে মোট ৫০,০০০ টাকার নোট সংগ্রহ করো",
                descriptionEn = "Collect ৳50,000 in cash notes",
                target = 50000,
                progress = m3.coerceAtMost(50000),
                rewardCoins = 150,
                rewardMoney = 15000,
                isClaimed = prefs.getBoolean("claimed_m_money_50k", false)
            ),
            MissionItem(
                id = "m_jump_20",
                titleBn = "২০টি বাধা পার হও",
                titleEn = "Dodge 20 Obstacles",
                descriptionBn = "লাফিয়ে বা পাশ কাটিয়ে ২০টি বাধা এড়িয়ে চলো",
                descriptionEn = "Successfully avoid 20 road obstacles",
                target = 20,
                progress = m4.coerceAtMost(20),
                rewardCoins = 80,
                rewardMoney = 4000,
                isClaimed = prefs.getBoolean("claimed_m_jump_20", false)
            ),
            MissionItem(
                id = "m_slide_10",
                titleBn = "১০ বার স্লাইড করো",
                titleEn = "Perform 10 Slides",
                descriptionBn = "উঁচু পাইপ বা বাধার নিচ দিয়ে ১০ বার স্লাইড করো",
                descriptionEn = "Slide under high obstacles 10 times",
                target = 10,
                progress = m5.coerceAtMost(10),
                rewardCoins = 60,
                rewardMoney = 3000,
                isClaimed = prefs.getBoolean("claimed_m_slide_10", false)
            ),
            MissionItem(
                id = "m_powerups_5",
                titleBn = "৫টি পাওয়ার-আপ সংগ্রহ",
                titleEn = "Collect 5 Power-ups",
                descriptionBn = "ম্যাগনেট, শিল্ড বা বুস্ট সংগ্রহ করো",
                descriptionEn = "Grab 5 power-ups during runs",
                target = 5,
                progress = m6.coerceAtMost(5),
                rewardCoins = 120,
                rewardMoney = 8000,
                isClaimed = prefs.getBoolean("claimed_m_powerups_5", false)
            )
        )
    }

    fun updateMissionProgress(
        dist: Int,
        coins: Int,
        money: Long,
        obstacles: Int,
        slides: Int,
        powerups: Int
    ) {
        val e = prefs.edit()
        val prevDist = prefs.getInt("mission_dist_run", 0)
        e.putInt("mission_dist_run", maxOf(prevDist, dist))
        e.putInt("mission_coins_collected", prefs.getInt("mission_coins_collected", 0) + coins)
        e.putInt("mission_money_collected", (prefs.getInt("mission_money_collected", 0) + money).toInt())
        e.putInt("mission_obstacles_avoided", prefs.getInt("mission_obstacles_avoided", 0) + obstacles)
        e.putInt("mission_slides_performed", prefs.getInt("mission_slides_performed", 0) + slides)
        e.putInt("mission_powerups_collected", prefs.getInt("mission_powerups_collected", 0) + powerups)
        e.apply()
    }

    fun claimMission(id: String, rewardCoins: Int, rewardMoney: Int): Boolean {
        if (prefs.getBoolean("claimed_$id", false)) return false
        prefs.edit().putBoolean("claimed_$id", true).apply()
        totalCoins += rewardCoins
        totalMoney += rewardMoney
        return true
    }

    // Shop Catalog
    fun getShopItems(): List<ShopItem> {
        val equippedOut = equippedOutfit
        val equippedB = equippedBag

        return listOf(
            // Outfits
            ShopItem(
                id = "outfit_classic",
                titleBn = "ক্লাসিক লুঙ্গি ও গেঞ্জি",
                titleEn = "Classic Lungi & Vest",
                descriptionBn = "আমাদের চটপটে বুড়োর চিরচেনা ঐতিহ্যবাহী পোশাক",
                descriptionEn = "The iconic traditional running outfit",
                priceCoins = 0,
                priceMoney = 0,
                category = ShopCategory.OUTFIT,
                isUnlocked = true,
                isEquipped = equippedOut == "outfit_classic",
                iconEmoji = "🩳"
            ),
            ShopItem(
                id = "outfit_dhuti",
                titleBn = "সাদা ধুতি ও পাঞ্জাবি",
                titleEn = "White Dhuti & Kurta",
                descriptionBn = "উৎসবের শুভ্র সাজে দ্রুততম বুড়ো",
                descriptionEn = "Festive white kurta for speedy escape",
                priceCoins = 150,
                priceMoney = 10000,
                category = ShopCategory.OUTFIT,
                isUnlocked = isItemUnlocked("outfit_dhuti"),
                isEquipped = equippedOut == "outfit_dhuti",
                iconEmoji = "🥻"
            ),
            ShopItem(
                id = "outfit_detective",
                titleBn = "ডিটেকটিভ কোট ও হ্যাট",
                titleEn = "Detective Hat & Coat",
                descriptionBn = "রহস্যময় গোয়েন্দা সাজে টাকার ব্যাগ রক্ষা",
                descriptionEn = "Mysterious detective style with fedora",
                priceCoins = 350,
                priceMoney = 25000,
                category = ShopCategory.OUTFIT,
                isUnlocked = isItemUnlocked("outfit_detective"),
                isEquipped = equippedOut == "outfit_detective",
                iconEmoji = "🕵️‍♂️"
            ),
            ShopItem(
                id = "outfit_superhero",
                titleBn = "সুপার বুড়ো লাল কেপ",
                titleEn = "Super Grandpa Cape",
                descriptionBn = "লাল কেপ উড়িয়ে বাতাসের গতিতে দৌড়!",
                descriptionEn = "Flying red cape with superhuman speed",
                priceCoins = 600,
                priceMoney = 50000,
                category = ShopCategory.OUTFIT,
                isUnlocked = isItemUnlocked("outfit_superhero"),
                isEquipped = equippedOut == "outfit_superhero",
                iconEmoji = "🦸‍♂️"
            ),

            // Bags
            ShopItem(
                id = "bag_burlap",
                titleBn = "চটের টাকার বস্তা",
                titleEn = "Burlap Money Sack",
                descriptionBn = "ঐতিহাসিক টাকার চটের বস্তা, নোট উড়তে থাকে",
                descriptionEn = "Classic jute sack bursting with cash",
                priceCoins = 0,
                priceMoney = 0,
                category = ShopCategory.MONEY_BAG,
                isUnlocked = true,
                isEquipped = equippedB == "bag_burlap",
                iconEmoji = "🎒"
            ),
            ShopItem(
                id = "bag_golden",
                titleBn = "স্বর্ণখচিত রেশমি ব্যাগ",
                titleEn = "Golden Silk Sack",
                descriptionBn = "সোনার সুতা দিয়ে বোনা আকর্ষণীয় টাকার ব্যাগ",
                descriptionEn = "Woven with gold thread, extra shiny",
                priceCoins = 200,
                priceMoney = 15000,
                category = ShopCategory.MONEY_BAG,
                isUnlocked = isItemUnlocked("bag_golden"),
                isEquipped = equippedB == "bag_golden",
                iconEmoji = "✨"
            ),
            ShopItem(
                id = "bag_vault",
                titleBn = "রয়েল ব্যাংক সেফ ব্রিফকেস",
                titleEn = "Bank Safe Briefcase",
                descriptionBn = "লক করা প্রিমিয়াম ব্রিফকেস",
                descriptionEn = "Heavy duty locked security briefcase",
                priceCoins = 500,
                priceMoney = 40000,
                category = ShopCategory.MONEY_BAG,
                isUnlocked = isItemUnlocked("bag_vault"),
                isEquipped = equippedB == "bag_vault",
                iconEmoji = "💼"
            ),

            // Upgrades
            ShopItem(
                id = "upgrade_magnet",
                titleBn = "চৌম্বক সময় বৃদ্ধি (লেভেল $magnetLevel)",
                titleEn = "Magnet Duration (Lvl $magnetLevel)",
                descriptionBn = "কয়েন ও নোট টানার সময় +২ সেকেন্ড বৃদ্ধি",
                descriptionEn = "+2s duration for magnet powerup",
                priceCoins = 100 * magnetLevel,
                priceMoney = (5000 * magnetLevel).toInt(),
                category = ShopCategory.UPGRADE,
                isUnlocked = magnetLevel >= 5,
                isEquipped = false,
                iconEmoji = "🧲"
            ),
            ShopItem(
                id = "upgrade_shield",
                titleBn = "শিল্ড ক্ষমতা (লেভেল $shieldLevel)",
                titleEn = "Shield Duration (Lvl $shieldLevel)",
                descriptionBn = "শিল্ড অ্যাক্টিভ থাকার সময় +৩ সেকেন্ড বৃদ্ধি",
                descriptionEn = "+3s duration for protection shield",
                priceCoins = 100 * shieldLevel,
                priceMoney = (5000 * shieldLevel).toInt(),
                category = ShopCategory.UPGRADE,
                isUnlocked = shieldLevel >= 5,
                isEquipped = false,
                iconEmoji = "🛡️"
            ),
            ShopItem(
                id = "upgrade_speed_boost",
                titleBn = "সুপার বুস্ট (লেভেল $speedBoostLevel)",
                titleEn = "Speed Boost (Lvl $speedBoostLevel)",
                descriptionBn = "রকেট গতির স্থায়িত্ব বৃদ্ধি",
                descriptionEn = "+1.5s longer rocket dash",
                priceCoins = 120 * speedBoostLevel,
                priceMoney = (6000 * speedBoostLevel).toInt(),
                category = ShopCategory.UPGRADE,
                isUnlocked = speedBoostLevel >= 5,
                isEquipped = false,
                iconEmoji = "🚀"
            ),
            ShopItem(
                id = "upgrade_multiplier",
                titleBn = "কয়েন গুণক (লেভেল $multiplierLevel)",
                titleEn = "Multiplier Duration (Lvl $multiplierLevel)",
                descriptionBn = "২ গুণ পয়েন্ট পাওয়ার সময় বৃদ্ধি",
                descriptionEn = "+2s duration for double points",
                priceCoins = 120 * multiplierLevel,
                priceMoney = (6000 * multiplierLevel).toInt(),
                category = ShopCategory.UPGRADE,
                isUnlocked = multiplierLevel >= 5,
                isEquipped = false,
                iconEmoji = "✖️2"
            )
        )
    }

    fun purchaseItem(item: ShopItem): Boolean {
        if (totalCoins < item.priceCoins || totalMoney < item.priceMoney) return false
        totalCoins -= item.priceCoins
        totalMoney -= item.priceMoney

        when (item.category) {
            ShopCategory.OUTFIT -> {
                setItemUnlocked(item.id, true)
                equippedOutfit = item.id
            }
            ShopCategory.MONEY_BAG -> {
                setItemUnlocked(item.id, true)
                equippedBag = item.id
            }
            ShopCategory.UPGRADE -> {
                when (item.id) {
                    "upgrade_magnet" -> magnetLevel = (magnetLevel + 1).coerceAtMost(5)
                    "upgrade_shield" -> shieldLevel = (shieldLevel + 1).coerceAtMost(5)
                    "upgrade_speed_boost" -> speedBoostLevel = (speedBoostLevel + 1).coerceAtMost(5)
                    "upgrade_multiplier" -> multiplierLevel = (multiplierLevel + 1).coerceAtMost(5)
                }
            }
        }
        return true
    }

    fun equipItem(item: ShopItem) {
        when (item.category) {
            ShopCategory.OUTFIT -> equippedOutfit = item.id
            ShopCategory.MONEY_BAG -> equippedBag = item.id
            else -> {}
        }
    }

    fun saveRunScore(score: Long, dist: Int, coins: Int, money: Long) {
        if (score > bestScore) {
            bestScore = score
        }
        totalDistance += dist
        totalCoins += coins
        totalMoney += money

        // Save into local top 10 leaderboard
        val list = getLeaderboard().toMutableList()
        val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
        list.add(
            com.example.game.model.LeaderboardEntry(
                rank = 0,
                playerName = if (languageIsBangla) "তুমি (চ্যাম্পিয়ন)" else "You (Champion)",
                score = score,
                distance = dist,
                coins = coins,
                date = dateStr
            )
        )
        list.sortByDescending { it.score }
        val trimmed = list.take(10)
        val e = prefs.edit()
        e.putInt("lb_count", trimmed.size)
        trimmed.forEachIndexed { index, entry ->
            e.putString("lb_name_$index", entry.playerName)
            e.putLong("lb_score_$index", entry.score)
            e.putInt("lb_dist_$index", entry.distance)
            e.putInt("lb_coins_$index", entry.coins)
            e.putString("lb_date_$index", entry.date)
        }
        e.apply()
    }

    fun getLeaderboard(): List<com.example.game.model.LeaderboardEntry> {
        val count = prefs.getInt("lb_count", 0)
        if (count == 0) {
            // Seed sample top scores
            return listOf(
                com.example.game.model.LeaderboardEntry(1, "করিম চাচা 🏃", 42500, 3200, 320, "23 Aug"),
                com.example.game.model.LeaderboardEntry(2, "রফিক মামা 💰", 31200, 2450, 240, "22 Aug"),
                com.example.game.model.LeaderboardEntry(3, "সুলতান ভাই ⚡", 25400, 1980, 195, "20 Aug"),
                com.example.game.model.LeaderboardEntry(4, "হাশেম চাচা 🎒", 18900, 1500, 140, "19 Aug"),
                com.example.game.model.LeaderboardEntry(5, "জব্বার মামা 🚀", 12300, 950, 85, "18 Aug")
            )
        }
        val list = ArrayList<com.example.game.model.LeaderboardEntry>(count)
        for (i in 0 until count) {
            val name = prefs.getString("lb_name_$i", "Player") ?: "Player"
            val score = prefs.getLong("lb_score_$i", 0L)
            val dist = prefs.getInt("lb_dist_$i", 0)
            val coins = prefs.getInt("lb_coins_$i", 0)
            val date = prefs.getString("lb_date_$i", "") ?: ""
            list.add(
                com.example.game.model.LeaderboardEntry(
                    rank = i + 1,
                    playerName = name,
                    score = score,
                    distance = dist,
                    coins = coins,
                    date = date
                )
            )
        }
        return list
    }

    fun resetAllProgress() {
        prefs.edit().clear().apply()
    }
}
