package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.Campaign
import com.example.model.HibretStrings
import com.example.ui.components.CampaignGridCard
import com.example.ui.components.FeaturedInitiativeCard
import com.example.ui.components.HibretTopBar
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldSoft
import com.example.ui.theme.BackgroundWarm
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HibretViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Redesigned Home Screen:
 * - NO "Start a Campaign" button or banner (moved exclusively to Campaign tab).
 * - NO category chips/tabs/carousels (completely removed).
 * - Three prominent Featured Initiatives at top: ENDF, GERD, and Mekedonia in an elegant horizontal swipeable carousel.
 * - TWO CAMPAIGN CARDS SIDE BY SIDE (2-column responsive grid) for all campaign rows with vertical scrolling.
 */
@Composable
fun HomeScreen(
    viewModel: HibretViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val approvedCampaigns by viewModel.approvedCampaigns.collectAsState()
    val unreadNotifs by viewModel.unreadNotificationsCount.collectAsState()
    val activeDraft by viewModel.activeDraft.collectAsState()
    val currencyFormatter = NumberFormat.getNumberInstance(Locale.US)

    // Dedicated Three National Initiatives: ENDF, GERD, Mekedonia
    val nationalInitiatives = approvedCampaigns.filter {
        it.id.startsWith("camp_endf") || it.id.startsWith("camp_gerd") || it.id.startsWith("camp_mekedonia")
    }

    // Regular community & medical campaigns for the 2-column grid
    val gridCampaigns = approvedCampaigns.filterNot {
        it.id.startsWith("camp_endf") || it.id.startsWith("camp_gerd") || it.id.startsWith("camp_mekedonia")
    }

    // Chunk into pairs of 2 for side-by-side two-column row rendering
    val campaignRows = gridCampaigns.chunked(2)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundWarm)
    ) {
        // Top App Bar with Logo & Language Switcher
        HibretTopBar(
            currentLanguage = language,
            unreadNotifications = unreadNotifs,
            onLanguageSelected = { viewModel.setLanguage(it) },
            onSearchClick = { viewModel.navigateTo(AppScreen.EXPLORE) },
            onNotificationsClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) },
            onProfileClick = { viewModel.navigateTo(AppScreen.PROFILE) },
            onScreenSelect = { screen -> viewModel.navigateTo(screen) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Draft Resume Banner (if owner has an unfinished campaign draft)
            activeDraft?.let { draft ->
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { viewModel.loadDraft(draft) }
                            .testTag("resume_draft_banner"),
                        shape = RoundedCornerShape(12.dp),
                        color = AccentGoldSoft,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = EmeraldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${HibretStrings.resumeDraft.get(language)} (Step ${draft.step}/6)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark
                                    )
                                    Text(
                                        text = if (draft.title.isNotBlank()) draft.title else "Untitled draft",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = EmeraldDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ==============================================================
            // 1. THREE PROMINENT FEATURED FUNDRAISING INITIATIVES (TOP OF HOME)
            // A. ENDF (Defense Force)
            // B. GERD (Abay Dam)
            // C. MEKEDONIA (Humanitarian Shelter)
            // ==============================================================
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Featured National Initiatives",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "Official • Verified",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Horizontally swipeable featured cards
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(nationalInitiatives) { initiative ->
                            FeaturedInitiativeCard(
                                campaign = initiative,
                                language = language,
                                onCardClick = { viewModel.selectCampaign(initiative.id) },
                                onDonateClick = { viewModel.startDonationFlow(initiative.id) }
                            )
                        }
                    }
                }
            }

            // Trust & Escrow Guarantee Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldSoft)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Hibret 100% Verified Giving",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Direct Telebirr & CBE Birr settlement to verified hospital invoices and community committee accounts.",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // ==============================================================
            // 2. DISCOVER CAMPAIGNS — TWO-COLUMN RESPONSIVE GRID
            // Strictly 2 campaign cards side by side in each row with vertical scroll.
            // ==============================================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Discover Campaigns",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "${gridCampaigns.size} Causes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.EXPLORE) }
                    )
                }
            }

            // Grid Rows (Two Cards Side by Side per Row)
            items(campaignRows) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Column Card
                    Box(modifier = Modifier.weight(1f)) {
                        CampaignGridCard(
                            campaign = rowItems[0],
                            language = language,
                            onCardClick = { viewModel.selectCampaign(rowItems[0].id) },
                            onDonateClick = { viewModel.startDonationFlow(rowItems[0].id) }
                        )
                    }

                    // Right Column Card (or spacer if odd count)
                    if (rowItems.size > 1) {
                        Box(modifier = Modifier.weight(1f)) {
                            CampaignGridCard(
                                campaign = rowItems[1],
                                language = language,
                                onCardClick = { viewModel.selectCampaign(rowItems[1].id) },
                                onDonateClick = { viewModel.startDonationFlow(rowItems[1].id) }
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
