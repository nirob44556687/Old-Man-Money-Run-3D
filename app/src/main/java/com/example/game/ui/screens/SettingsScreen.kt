package com.example.game.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.Difficulty
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GameAngryRed
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameMoneyGreen
import com.example.ui.theme.GameOrange

@Composable
fun SettingsScreen(
    engine: GameEngine,
    onBack: () -> Unit
) {
    var soundFx by remember { mutableStateOf(engine.preferences.soundEnabled) }
    var music by remember { mutableStateOf(engine.preferences.musicEnabled) }
    var onScreenBtns by remember { mutableStateOf(engine.preferences.onScreenButtonsEnabled) }
    var isBangla by remember { mutableStateOf(engine.preferences.languageIsBangla) }
    var difficulty by remember { mutableStateOf(engine.preferences.difficulty) }
    var showResetConfirm by remember { mutableStateOf(false) }

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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .background(DarkCard, CircleShape)
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = if (isBangla) "সেটিংস ও নিয়ন্ত্রণ" else "Settings & Controls",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = GameGold,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            // Audio Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isBangla) "শব্দ ও মিউজিক" else "Audio Settings",
                        color = GameGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    // Sound FX Toggle
                    SettingToggleRow(
                        icon = Icons.Default.VolumeUp,
                        title = if (isBangla) "সাউন্ড ইফেক্ট (FX)" else "Sound Effects",
                        checked = soundFx,
                        onCheckedChange = {
                            soundFx = it
                            engine.preferences.soundEnabled = it
                            engine.soundManager.isSoundEnabled = it
                        }
                    )

                    // Music Toggle
                    SettingToggleRow(
                        icon = Icons.Default.MusicNote,
                        title = if (isBangla) "ব্যাকগ্রাউন্ড মিউজিক" else "Background Music",
                        checked = music,
                        onCheckedChange = {
                            music = it
                            engine.preferences.musicEnabled = it
                            engine.soundManager.isMusicEnabled = it
                            if (it) {
                                engine.soundManager.startBgm()
                            } else {
                                engine.soundManager.stopBgm()
                            }
                        }
                    )
                }
            }

            // Controls & Gameplay Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isBangla) "গেমপ্লে ও নিয়ন্ত্রণ" else "Gameplay & Controls",
                        color = GameGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    // On Screen Buttons Toggle
                    SettingToggleRow(
                        icon = Icons.Default.PhoneAndroid,
                        title = if (isBangla) "অন-স্ক্রিন বাটন দেখান" else "On-Screen Buttons",
                        subtitle = if (isBangla) "সোয়াইপ ছাড়াও বাটন দিয়ে খেলুন" else "Show Jump, Slide & Lane buttons",
                        checked = onScreenBtns,
                        onCheckedChange = {
                            onScreenBtns = it
                            engine.preferences.onScreenButtonsEnabled = it
                        }
                    )

                    // Language Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Translate, "Language", tint = Color(0xFF64B5F6))
                            Text(
                                text = if (isBangla) "ভাষা (Language)" else "Language (ভাষা)",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LanguageChip(
                                text = "বাংলা",
                                selected = isBangla,
                                onClick = {
                                    isBangla = true
                                    engine.preferences.languageIsBangla = true
                                }
                            )
                            LanguageChip(
                                text = "EN",
                                selected = !isBangla,
                                onClick = {
                                    isBangla = false
                                    engine.preferences.languageIsBangla = false
                                }
                            )
                        }
                    }

                    // Difficulty Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Speed, "Difficulty", tint = GameOrange)
                            Text(
                                text = if (isBangla) "কঠিনতার স্তর" else "Difficulty Level",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Difficulty.entries.forEach { diff ->
                                val isSelected = difficulty == diff
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) GameOrange else Color(0xFF1E232E),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) Color.White else Color(0x33FFFFFF),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            difficulty = diff
                                            engine.preferences.difficulty = diff
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isBangla) diff.labelBn else diff.labelEn,
                                        color = if (isSelected) Color.White else Color(0xFFB0BEC5),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reset Data Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, GameAngryRed.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isBangla) "ডাটা রিসেট" else "Reset Progress",
                        color = GameAngryRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = if (isBangla) "আপনার সমস্ত স্কোর, লেভেল ও সংগৃহীত কয়েন রিসেট হবে।" else "Reset all scores, unlocked levels, and earned coins.",
                        color = Color(0xFF90A4AE),
                        fontSize = 12.sp
                    )

                    Box(
                        modifier = Modifier
                            .background(GameAngryRed.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .border(1.dp, GameAngryRed, RoundedCornerShape(10.dp))
                            .clickable {
                                engine.preferences.resetAllProgress()
                                showResetConfirm = true
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("reset_progress_button")
                    ) {
                        Text(
                            text = if (isBangla) "রিসেট করুন" else "Reset All Data",
                            color = GameAngryRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, title, tint = GameGold)
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color(0xFF90A4AE),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GameMoneyGreen,
                uncheckedThumbColor = Color(0xFF90A4AE),
                uncheckedTrackColor = Color(0xFF37474F)
            )
        )
    }
}

@Composable
fun LanguageChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (selected) GameGold else Color(0xFF1E232E),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (selected) Color.White else Color(0x33FFFFFF),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
