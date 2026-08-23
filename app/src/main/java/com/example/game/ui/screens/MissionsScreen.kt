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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.MissionItem
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameMoneyGold
import com.example.ui.theme.GameMoneyGreen

@Composable
fun MissionsScreen(
    engine: GameEngine,
    onBack: () -> Unit
) {
    val isBangla = engine.preferences.languageIsBangla
    var missionVersion by remember { mutableIntStateOf(0) }
    val missions = remember(missionVersion) { engine.preferences.getMissions() }

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .background(DarkCard, CircleShape)
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            .testTag("missions_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = if (isBangla) "মিশন ও অর্জন" else "Missions & Quests",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = GameGold,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🪙 ${engine.preferences.totalCoins}",
                        color = GameMoneyGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "💵 ৳${engine.preferences.totalMoney}",
                        color = GameMoneyGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Missions List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(missions) { mission ->
                    MissionCard(
                        mission = mission,
                        isBangla = isBangla,
                        onClaim = {
                            if (engine.preferences.claimMission(mission.id, mission.rewardCoins, mission.rewardMoney)) {
                                engine.soundManager.playPowerUp()
                                missionVersion++
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MissionCard(
    mission: MissionItem,
    isBangla: Boolean,
    onClaim: () -> Unit
) {
    val progressFraction = (mission.progress.toFloat() / mission.target).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .testTag("mission_item_${mission.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (mission.isClaimed) Color(0x22FFFFFF)
            else if (mission.isCompleted) GameMoneyGreen
            else Color(0x33FFFFFF)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) mission.titleBn else mission.titleEn,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                // Rewards
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🪙 ${mission.rewardCoins}", color = GameMoneyGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("💵 ৳${mission.rewardMoney}", color = GameMoneyGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Text(
                text = if (isBangla) mission.descriptionBn else mission.descriptionEn,
                color = Color(0xFF90A4AE),
                fontSize = 12.sp
            )

            // Progress Bar & Claim Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${mission.progress} / ${mission.target}",
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            color = if (mission.isCompleted) GameMoneyGreen else Color(0xFFB0BEC5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (mission.isCompleted) GameMoneyGreen else GameGold,
                        trackColor = Color(0xFF37474F)
                    )
                }

                if (mission.isClaimed) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x3300E676), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isBangla) "সংগৃহীত ✓" else "Claimed",
                            color = GameMoneyGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else if (mission.isCompleted) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF00C853))),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onClaim() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isBangla) "পুরস্কার নিন" else "Claim",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
