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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.GameAngryRed
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameMoneyGold
import com.example.ui.theme.GameMoneyGreen
import com.example.ui.theme.GameOrange

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    isBangla: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(2.dp, GameGold)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isBangla) "খেলা স্থগিত ⏸️" else "GAME PAUSED ⏸️",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = GameGold
                )

                // Resume Button
                DialogActionButton(
                    text = if (isBangla) "খেলা চালিয়ে যান (RESUME)" else "RESUME",
                    icon = Icons.Default.PlayArrow,
                    gradient = listOf(Color(0xFF00E676), Color(0xFF00C853)),
                    tag = "resume_game_button",
                    onClick = onResume
                )

                // Restart Button
                DialogActionButton(
                    text = if (isBangla) "নতুন করে খেলুন (RESTART)" else "RESTART",
                    icon = Icons.Default.Refresh,
                    gradient = listOf(Color(0xFFFF9800), Color(0xFFE65100)),
                    tag = "restart_game_button",
                    onClick = onRestart
                )

                // Main Menu Button
                DialogActionButton(
                    text = if (isBangla) "মূল মেনু (MAIN MENU)" else "MAIN MENU",
                    icon = Icons.Default.Home,
                    gradient = listOf(Color(0xFF455A64), Color(0xFF263238)),
                    tag = "main_menu_button",
                    onClick = onHome
                )
            }
        }
    }
}

@Composable
fun CaughtGameOverDialog(
    engine: GameEngine,
    onTryAgain: () -> Unit,
    onHome: () -> Unit
) {
    val isBangla = engine.preferences.languageIsBangla

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(2.5.dp, GameAngryRed)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Funny Catch Header
                Text(
                    text = if (isBangla) "ধরা খাইছো! বুড়া ধরা পড়ছে 😂" else "CAUGHT! Game Over 😂",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = GameAngryRed,
                    textAlign = TextAlign.Center
                )

                // Landscape side-by-side content
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cartoon Caught Illustration (using downloaded user image)
                    Card(
                        modifier = Modifier
                            .size(130.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFCC80)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_caught_character),
                            contentDescription = "Caught Character Illustration",
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Run Stats Table
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B26)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            StatRow(
                                label = if (isBangla) "অতিক্রান্ত দূরত্ব:" else "Distance:",
                                value = "${engine.distanceMeters.toInt()}m",
                                valueColor = Color(0xFFFFD54F)
                            )
                            StatRow(
                                label = if (isBangla) "সংগৃহীত টাকা:" else "Money Collected:",
                                value = "💵 ৳${engine.moneyCollected}",
                                valueColor = GameMoneyGreen
                            )
                            StatRow(
                                label = if (isBangla) "মোট স্কোর:" else "Score:",
                                value = "${engine.score}",
                                valueColor = Color.White
                            )
                            StatRow(
                                label = if (isBangla) "সেরা স্কোর:" else "Best Score:",
                                value = "${engine.preferences.bestScore}",
                                valueColor = GameGold
                            )
                        }
                    }
                }

                // Action Buttons (Try Again & Home)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Try Again Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(6.dp, RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF00C853))),
                                RoundedCornerShape(14.dp)
                            )
                            .border(1.5.dp, Color.White, RoundedCornerShape(14.dp))
                            .clickable { onTryAgain() }
                            .testTag("try_again_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Refresh, "Try Again", tint = Color.White, modifier = Modifier.size(20.dp))
                            Text(
                                text = if (isBangla) "আবার খেলো" else "TRY AGAIN",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Home Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(6.dp, RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF455A64), Color(0xFF263238))),
                                RoundedCornerShape(14.dp)
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                            .clickable { onHome() }
                            .testTag("gameover_home_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Home, "Home", tint = Color.White, modifier = Modifier.size(20.dp))
                            Text(
                                text = if (isBangla) "মূল মেনু" else "HOME",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelCompleteDialog(
    engine: GameEngine,
    onNextLevel: () -> Unit,
    onHome: () -> Unit
) {
    val isBangla = engine.preferences.languageIsBangla
    val lvl = engine.currentLevel
    val stars = engine.preferences.getLevelStars(lvl?.levelNumber ?: 1)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(2.5.dp, GameGold)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "🎉 LEVEL COMPLETE! 🎉",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = GameGold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isBangla) "${lvl?.nameBn}" else "${lvl?.nameEn}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFCC80)
                )

                // 3-Star Rating Display
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (s in 1..3) {
                        Text(
                            text = if (s <= stars) "⭐" else "☆",
                            fontSize = 32.sp
                        )
                    }
                }

                // Summary Stats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B26)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatRow(
                            label = if (isBangla) "অতিক্রান্ত দূরত্ব:" else "Distance:",
                            value = "${engine.distanceMeters.toInt()}m / ${lvl?.targetDistance}m",
                            valueColor = Color(0xFFFFD54F)
                        )
                        StatRow(
                            label = if (isBangla) "সংগৃহীত কয়েন:" else "Coins:",
                            value = "🪙 ${engine.coinsCollected}",
                            valueColor = GameMoneyGold
                        )
                        StatRow(
                            label = if (isBangla) "মোট টাকা:" else "Money:",
                            value = "💵 ৳${engine.moneyCollected}",
                            valueColor = GameMoneyGreen
                        )
                        StatRow(
                            label = if (isBangla) "অর্জিত স্কোর:" else "Score:",
                            value = "${engine.score}",
                            valueColor = Color.White
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Next Level Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF00C853))),
                                RoundedCornerShape(16.dp)
                            )
                            .border(1.5.dp, Color.White, RoundedCornerShape(16.dp))
                            .clickable { onNextLevel() }
                            .testTag("next_level_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(22.dp))
                            Text(
                                text = if (isBangla) "পরবর্তী লেভেল" else "NEXT LEVEL",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Home Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF455A64), Color(0xFF263238))),
                                RoundedCornerShape(16.dp)
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                            .clickable { onHome() }
                            .testTag("levelcomplete_home_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Home, "Home", tint = Color.White, modifier = Modifier.size(20.dp))
                            Text(
                                text = if (isBangla) "মূল মেনু" else "HOME",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFFB0BEC5), fontSize = 13.sp)
        Text(text = value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun DialogActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(gradient), RoundedCornerShape(16.dp))
            .border(1.5.dp, Color.White, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(24.dp))
            Text(text = text, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
        }
    }
}
