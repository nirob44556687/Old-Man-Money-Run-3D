package com.example.game.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.engine.GameEngine
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GameDarkOrange
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameMoneyGold
import com.example.ui.theme.GameMoneyGreen
import com.example.ui.theme.GameOrange

@Composable
fun MainMenuScreen(
    engine: GameEngine,
    onPlayEndless: () -> Unit,
    onOpenLevelSelect: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val isBangla = engine.preferences.languageIsBangla
    var showPlayDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top App Bar / Economy Chips & Language Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Switcher Button (Instant 1-tap Bengali / English toggle)
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .background(
                            if (isBangla) Color(0xFF006A4E) else Color(0xFF012169),
                            RoundedCornerShape(14.dp)
                        )
                        .border(1.5.dp, GameGold, RoundedCornerShape(14.dp))
                        .clickable {
                            engine.preferences.languageIsBangla = !engine.preferences.languageIsBangla
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("language_toggle_button")
                ) {
                    Text(
                        text = if (isBangla) "🇧🇩 বাংলা (BN)" else "🇬🇧 English (EN)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Money ৳ chip
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(14.dp))
                        .background(DarkCard, RoundedCornerShape(14.dp))
                        .border(1.5.dp, GameMoneyGreen, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "💵 ৳${engine.preferences.totalMoney}",
                        color = GameMoneyGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }

                // Settings icon button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(4.dp, CircleShape)
                        .background(DarkCard, CircleShape)
                        .border(1.dp, Color(0x44FFFFFF), CircleShape)
                        .clickable { onOpenSettings() }
                        .testTag("settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Game Hero Banner (Using provided Home Menu image)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, GameGold)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_home_banner),
                    contentDescription = "Game Home Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Title & Subtitle Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "বুড়া টাকার ব্যাগ",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = GameGold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isBangla) "দৌড়াও... বাঁচাও... জিতো!" else "Run... Escape... Win!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFCC80),
                    textAlign = TextAlign.Center
                )
            }

            // Best Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isBangla) "সেরা স্কোর" else "BEST SCORE",
                            fontSize = 12.sp,
                            color = Color(0xFFB0BEC5),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${engine.preferences.bestScore}",
                            fontSize = 22.sp,
                            color = GameGold,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(35.dp)
                            .background(Color(0x33FFFFFF))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isBangla) "মোট দূরত্ব" else "TOTAL RUN",
                            fontSize = 12.sp,
                            color = Color(0xFFB0BEC5),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${engine.preferences.totalDistance}m",
                            fontSize = 22.sp,
                            color = Color(0xFF64B5F6),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Big Direct Play Button (1-Tap start playing!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(10.dp, RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF00E676), Color(0xFF00C853), Color(0xFF00B0FF))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                    .clickable { onPlayEndless() }
                    .testTag("play_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = if (isBangla) "খেলা শুরু করুন (PLAY)" else "PLAY NOW",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Grid of Secondary Menu Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuGridButton(
                    icon = Icons.Default.TrackChanges,
                    title = if (isBangla) "মিশন" else "Missions",
                    subtitle = if (isBangla) "পুরস্কার নাও" else "Earn Rewards",
                    color = Color(0xFFFF9100),
                    modifier = Modifier.weight(1f),
                    tag = "menu_missions_button",
                    onClick = onOpenMissions
                )

                MenuGridButton(
                    icon = Icons.Default.ShoppingBag,
                    title = if (isBangla) "দোকান" else "Shop",
                    subtitle = if (isBangla) "পোশাক ও ব্যাগ" else "Skins & Bags",
                    color = Color(0xFFE040FB),
                    modifier = Modifier.weight(1f),
                    tag = "menu_shop_button",
                    onClick = onOpenShop
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuGridButton(
                    icon = Icons.Default.EmojiEvents,
                    title = if (isBangla) "লিডারবোর্ড" else "Leaderboard",
                    subtitle = if (isBangla) "সেরা রানার" else "Top Runners",
                    color = GameGold,
                    modifier = Modifier.weight(1f),
                    tag = "menu_leaderboard_button",
                    onClick = onOpenLeaderboard
                )

                MenuGridButton(
                    icon = Icons.Default.PlayArrow,
                    title = if (isBangla) "লেভেল ১০০" else "100 Levels",
                    subtitle = if (isBangla) "ম্যাপ চ্যালেঞ্জ" else "Campaign",
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f),
                    tag = "menu_levels_button",
                    onClick = onOpenLevelSelect
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Play Mode Selection Dialog (Endless vs Level Mode)
        if (showPlayDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000))
                    .clickable { showPlayDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clickable(enabled = false) {},
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(2.dp, GameGold)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isBangla) "খেলার মোড বেছে নিন" else "Choose Game Mode",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        // Option 1: Endless Mode
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(Color(0xFFFF9100), Color(0xFFFF3D00))),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    showPlayDialog = false
                                    onPlayEndless()
                                }
                                .padding(16.dp)
                                .testTag("select_endless_mode"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isBangla) "🏃‍♂️ অনন্ত দৌড় (Endless Runner)" else "🏃‍♂️ Endless Mode",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (isBangla) "যতদূর পারো দৌড়াও এবং সর্বোচ্চ স্কোর করো!" else "Survive as long as possible & set high scores!",
                                    color = Color(0xFFFFE082),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Option 2: Level Mode (1-100)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(Color(0xFF29B6F6), Color(0xFF0288D1))),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    showPlayDialog = false
                                    onOpenLevelSelect()
                                }
                                .padding(16.dp)
                                .testTag("select_level_mode"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isBangla) "🗺️ লেভেল মোড (১ - ১০০)" else "🗺️ Level Campaign (1 - 100)",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (isBangla) "১০০টি চ্যালেঞ্জিং ধাপ পার করে নতুন থিম আনলক করো" else "Conquer 100 exciting progressive levels!",
                                    color = Color(0xFFE1F5FE),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuGridButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(82.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp
                )
            }
        }
    }
}
