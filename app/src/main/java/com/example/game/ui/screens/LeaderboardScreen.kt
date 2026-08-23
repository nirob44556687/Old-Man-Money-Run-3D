package com.example.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.LeaderboardEntry
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameMoneyGold

@Composable
fun LeaderboardScreen(
    engine: GameEngine,
    onBack: () -> Unit
) {
    val isBangla = engine.preferences.languageIsBangla
    val entries = remember { engine.preferences.getLeaderboard() }

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
                        .testTag("leaderboard_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = if (isBangla) "শীর্ষ স্কোরবোর্ড (Leaderboard)" else "Top Leaderboard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = GameGold,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(entries) { index, entry ->
                    LeaderboardCard(
                        rank = index + 1,
                        entry = entry,
                        isBangla = isBangla
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardCard(
    rank: Int,
    entry: LeaderboardEntry,
    isBangla: Boolean
) {
    val rankBadge = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$rank"
    }

    val borderColor = when (rank) {
        1 -> GameGold
        2 -> Color(0xFFE0E0E0)
        3 -> Color(0xFFCD7F32)
        else -> Color(0x22FFFFFF)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp))
            .testTag("leaderboard_row_$rank"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Rank badge
            Text(
                text = rankBadge,
                fontSize = if (rank <= 3) 24.sp else 16.sp,
                fontWeight = FontWeight.Black,
                color = if (rank <= 3) GameGold else Color(0xFFB0BEC5)
            )

            // Player Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.playerName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "🏃 ${entry.distance}m", color = Color(0xFF90CAF9), fontSize = 12.sp)
                    Text(text = "🪙 ${entry.coins}", color = GameMoneyGold, fontSize = 12.sp)
                }
            }

            // Score Column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.score}",
                    color = GameGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
                if (entry.date.isNotEmpty()) {
                    Text(
                        text = entry.date,
                        color = Color(0xFF78909C),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
