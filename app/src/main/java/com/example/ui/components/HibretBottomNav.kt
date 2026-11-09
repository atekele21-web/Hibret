package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.HibretStrings
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppScreen

/**
 * Premium 3D-inspired fintech bottom navigation for Hibret.
 * Features:
 * - Exactly four destinations: HOME, CAMPAIGN, MY ACTIVITY, PROFILE.
 * - Elevated floating container with subtle depth and soft rounded corners.
 * - Active State: Emerald green icon & label, elevated soft-green pill background with subtle top-light gradient and depth.
 * - Inactive State: Elegant neutral gray icon and readable gray label.
 * - Spring press-scale interaction on tap.
 * - Strict Android safe-area and navigation bar insets.
 */
@Composable
fun HibretBottomNav(
    currentScreen: AppScreen,
    language: AppLanguage,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = EmeraldDark.copy(alpha = 0.18f),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. HOME
            Nav3DItem(
                label = HibretStrings.navHome.get(language),
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                isSelected = currentScreen == AppScreen.HOME,
                testTag = "nav_home",
                onClick = { onNavigate(AppScreen.HOME) },
                modifier = Modifier.weight(1f)
            )

            // 2. CAMPAIGN (Replaces Explore, holds Start a Campaign)
            Nav3DItem(
                label = HibretStrings.navCampaign.get(language),
                selectedIcon = Icons.Filled.VolunteerActivism,
                unselectedIcon = Icons.Outlined.VolunteerActivism,
                isSelected = currentScreen == AppScreen.EXPLORE,
                testTag = "nav_campaign",
                onClick = { onNavigate(AppScreen.EXPLORE) },
                modifier = Modifier.weight(1f)
            )

            // 3. MY ACTIVITY
            Nav3DItem(
                label = HibretStrings.navActivity.get(language),
                selectedIcon = Icons.Filled.Campaign,
                unselectedIcon = Icons.Outlined.Campaign,
                isSelected = currentScreen == AppScreen.MY_ACTIVITY,
                testTag = "nav_activity",
                onClick = { onNavigate(AppScreen.MY_ACTIVITY) },
                modifier = Modifier.weight(1f)
            )

            // 4. PROFILE
            Nav3DItem(
                label = HibretStrings.navProfile.get(language),
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                isSelected = currentScreen == AppScreen.PROFILE,
                testTag = "nav_profile",
                onClick = { onNavigate(AppScreen.PROFILE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Nav3DItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth subtle spring scale when tapped
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "navPressScale"
    )

    // Animated colors for text & icon
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) EmeraldPrimary else TextSecondary,
        label = "navContentColor"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elevated 3D-inspired pill for selected state
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (isSelected) {
                            Modifier
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            EmeraldSoft,
                                            Color(0xFFD8EFE1)
                                        )
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        } else {
                            Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}
