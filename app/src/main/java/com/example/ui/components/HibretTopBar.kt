package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.HibretStrings
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HibretTopBar(
    currentLanguage: AppLanguage,
    unreadNotifications: Int,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onScreenSelect: ((com.example.ui.viewmodel.AppScreen) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showLangMenu by remember { mutableStateOf(false) }
    var showScreenMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Greeting
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Hibret Brand Mark
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_hibret_logo),
                        contentDescription = "Hibret Logo",
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HIBRET",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldDark,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentGold)
                        )
                    }
                    Text(
                        text = HibretStrings.homeGreeting.get(currentLanguage),
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            // Right Action Icons: Screens Menu, Language Pill, Search, Notification Bell
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onScreenSelect != null) {
                    Box {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AccentGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                            modifier = Modifier
                                .clickable { showScreenMenu = true }
                                .testTag("screens_showcase_btn")
                        ) {
                            Text(
                                text = "12 Screens",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showScreenMenu,
                            onDismissRequest = { showScreenMenu = false }
                        ) {
                            Text(
                                text = "Explore Redesigned Screens",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            val screens = listOf(
                                "1. Splash Screen" to com.example.ui.viewmodel.AppScreen.SPLASH,
                                "2. Onboarding" to com.example.ui.viewmodel.AppScreen.ONBOARDING,
                                "3. Login" to com.example.ui.viewmodel.AppScreen.LOGIN,
                                "4. Registration" to com.example.ui.viewmodel.AppScreen.REGISTRATION,
                                "5. Home Feed" to com.example.ui.viewmodel.AppScreen.HOME,
                                "6. Campaign Tab" to com.example.ui.viewmodel.AppScreen.EXPLORE,
                                "7. Campaign Detail" to com.example.ui.viewmodel.AppScreen.CAMPAIGN_DETAIL,
                                "8. Donation Amount" to com.example.ui.viewmodel.AppScreen.DONATION_AMOUNT,
                                "9. Payment Method" to com.example.ui.viewmodel.AppScreen.DONATION_PAYMENT,
                                "10. Donation Confirm" to com.example.ui.viewmodel.AppScreen.DONATION_CONFIRM,
                                "11. Donation Success" to com.example.ui.viewmodel.AppScreen.DONATION_SUCCESS,
                                "12. Campaign Creation" to com.example.ui.viewmodel.AppScreen.CAMPAIGN_CREATION
                            )
                            screens.forEach { (name, scr) ->
                                DropdownMenuItem(
                                    text = { Text(name, fontSize = 12.sp) },
                                    onClick = {
                                        onScreenSelect(scr)
                                        showScreenMenu = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Language Switcher Pill
                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = EmeraldSoft,
                        modifier = Modifier
                            .clickable { showLangMenu = true }
                            .testTag("language_selector")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentLanguage.name.take(2),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showLangMenu,
                        onDismissRequest = { showLangMenu = false }
                    ) {
                        AppLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${lang.displayName} (${lang.nativeName})",
                                        fontWeight = if (lang == currentLanguage) FontWeight.Bold else FontWeight.Normal,
                                        color = if (lang == currentLanguage) EmeraldPrimary else TextPrimary
                                    )
                                },
                                onClick = {
                                    onLanguageSelected(lang)
                                    showLangMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Search Icon
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_bar_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Notification Bell with Badge
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_bar_notifications_btn")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifications > 0) {
                                Badge(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.White
                                ) {
                                    Text(text = unreadNotifications.toString(), fontSize = 10.sp)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
