package com.example.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.LevelInfo
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameOrange

@Composable
fun LevelSelectScreen(
    engine: GameEngine,
    onSelectLevel: (LevelInfo) -> Unit,
    onBack: () -> Unit
) {
    val isBangla = engine.preferences.languageIsBangla
    val levels = remember { engine.preferences.getLevels() }

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
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                        .testTag("level_select_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = if (isBangla) "লেভেল নির্বাচন (১ - ১০০)" else "Level Select (1 - 100)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = GameGold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )
            }

            // Grid of 100 levels
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 95.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(levels) { lvl ->
                    LevelGridCard(
                        level = lvl,
                        isBangla = isBangla,
                        onClick = {
                            if (lvl.isUnlocked) {
                                onSelectLevel(lvl)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelGridCard(
    level: LevelInfo,
    isBangla: Boolean,
    onClick: () -> Unit
) {
    val isUnlocked = level.isUnlocked
    val cardBg = if (isUnlocked) {
        Brush.verticalGradient(listOf(Color(0xFF2C3440), Color(0xFF1E232B)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF15181E), Color(0xFF101216)))
    }
    val borderColor = if (isUnlocked) GameGold.copy(alpha = 0.8f) else Color(0x22FFFFFF)

    Card(
        modifier = Modifier
            .height(115.dp)
            .shadow(if (isUnlocked) 4.dp else 0.dp, RoundedCornerShape(16.dp))
            .clickable(enabled = isUnlocked) { onClick() }
            .testTag("level_card_${level.levelNumber}"),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBg)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isUnlocked) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF546E7A),
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "${level.levelNumber}",
                        color = Color(0xFF546E7A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "${level.levelNumber}",
                        color = GameGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )

                    Text(
                        text = "${level.targetDistance}m",
                        color = Color(0xFF90CAF9),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )

                    // Stars row
                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        for (s in 1..3) {
                            Text(
                                text = if (s <= level.stars) "⭐" else "☆",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
