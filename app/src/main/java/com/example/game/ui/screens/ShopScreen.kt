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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.model.ShopCategory
import com.example.game.model.ShopItem
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameMoneyGold
import com.example.ui.theme.GameMoneyGreen
import com.example.ui.theme.GameOrange

@Composable
fun ShopScreen(
    engine: GameEngine,
    onBack: () -> Unit
) {
    val isBangla = engine.preferences.languageIsBangla
    var selectedTab by remember { mutableIntStateOf(0) }
    var shopVersion by remember { mutableIntStateOf(0) }

    val shopItems = remember(shopVersion) { engine.preferences.getShopItems() }
    val filteredItems = when (selectedTab) {
        0 -> shopItems.filter { it.category == ShopCategory.OUTFIT }
        1 -> shopItems.filter { it.category == ShopCategory.MONEY_BAG }
        else -> shopItems.filter { it.category == ShopCategory.UPGRADE }
    }

    val tabTitles = if (isBangla) {
        listOf("🩳 পোশাক", "🎒 ব্যাগ", "⚡ আপগ্রেড")
    } else {
        listOf("🩳 Outfits", "🎒 Bags", "⚡ Upgrades")
    }

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
                            .testTag("shop_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = if (isBangla) "দোকান ও কালেকশন" else "Shop & Collection",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = GameGold,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                // Balance chips
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

            // Categories Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkCard,
                contentColor = GameGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GameGold
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems) { item ->
                    ShopItemCard(
                        item = item,
                        isBangla = isBangla,
                        canAfford = engine.preferences.totalCoins >= item.priceCoins && engine.preferences.totalMoney >= item.priceMoney,
                        onBuy = {
                            if (engine.preferences.purchaseItem(item)) {
                                engine.soundManager.playPowerUp()
                                shopVersion++
                            }
                        },
                        onEquip = {
                            engine.preferences.equipItem(item)
                            engine.soundManager.playButtonClick()
                            shopVersion++
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    isBangla: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit,
    onEquip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .testTag("shop_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (item.isEquipped) GameMoneyGreen else if (item.isUnlocked) GameGold.copy(alpha = 0.5f) else Color(0x33FFFFFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Item Icon Box
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFF1E212B), RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.iconEmoji, fontSize = 28.sp)
            }

            // Description Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBangla) item.titleBn else item.titleEn,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = if (isBangla) item.descriptionBn else item.descriptionEn,
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp
                )

                if (!item.isUnlocked) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.priceCoins > 0) {
                            Text("🪙 ${item.priceCoins}", color = GameMoneyGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        if (item.priceMoney > 0) {
                            Text("💵 ৳${item.priceMoney}", color = GameMoneyGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Action Button
            if (item.isEquipped) {
                Box(
                    modifier = Modifier
                        .background(GameMoneyGreen.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.dp, GameMoneyGreen, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isBangla) "সজ্জিত ✓" else "Equipped",
                        color = GameMoneyGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            } else if (item.isUnlocked) {
                if (item.category != ShopCategory.UPGRADE) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1565C0), RoundedCornerShape(12.dp))
                            .clickable { onEquip() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isBangla) "ব্যবহার করো" else "Equip",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        text = "MAX",
                        color = GameGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(
                            if (canAfford) Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF00C853)))
                            else Brush.horizontalGradient(listOf(Color(0xFF546E7A), Color(0xFF37474F))),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = canAfford) { onBuy() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isBangla) "কিনুন" else "Buy",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
