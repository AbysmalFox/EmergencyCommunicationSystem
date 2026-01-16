package com.example.emergencycommunicationsystem.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import com.example.emergencycommunicationsystem.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.animation.animateContentSize
import com.example.emergencycommunicationsystem.ui.theme.SoftShadow
import com.example.emergencycommunicationsystem.ui.theme.Slate
import com.example.emergencycommunicationsystem.ui.theme.DarkNavy
import androidx.compose.ui.graphics.Color
import com.example.emergencycommunicationsystem.util.getLocaleContext

const val navOverlayHeight = 80 // Reduced slightly
const val navOverlayLift = 24 // Adjusted lift

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val screens = listOf(Screen.Home, Screen.Alerts, Screen.Map, Screen.Profile)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentSelectionIndex = remember(currentDestination) {
        screens.indexOfFirst { screen ->
            currentDestination?.hierarchy?.any { it.route == screen.route } == true
        }.coerceAtLeast(0)
    }

    // Detect dark mode to adjust colors
    val bgColor = MaterialTheme.colorScheme.background
    val isDarkMode = (bgColor.red + bgColor.green + bgColor.blue) / 3f < 0.5f
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(navOverlayHeight.dp)
            .padding(horizontal = 20.dp)
            .offset(y = (-navOverlayLift).dp)
            .shadow(
                elevation = 24.dp, // Stronger shadow
                shape = RoundedCornerShape(32.dp),
                spotColor = Color.Black.copy(alpha = 0.5f),
                ambientColor = Color.Black.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                // Use a significantly different color to stand out
                if (isDarkMode) {
                    // Dark mode: Use a darker, more distinct color (darker than Slate)
                    Color(0xFF1A2332) // Darker blue-gray that stands out from DarkNavy
                } else {
                    // Light mode: Use a slightly darker shade
                    Color(0xFFF5F5F5) // Light gray that stands out from white
                }
            )
            .border(
                width = 2.dp, // Thicker border for more visibility
                color = if (isDarkMode) {
                    Color.White.copy(alpha = 0.2f) // More visible border in dark mode
                } else {
                    Color.Black.copy(alpha = 0.15f) // More visible border in light mode
                },
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        val containerWidth = LocalConfiguration.current.screenWidthDp.dp - 40.dp
        val itemWidth = containerWidth / screens.size

        // Sliding pill indicator with a spring animation
        val indicatorOffset: Dp by animateDpAsState(
            targetValue = itemWidth * currentSelectionIndex,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "indicatorOffset"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(itemWidth)
                .fillMaxHeight()
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                // Subtle indicator background
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEachIndexed { index, screen ->
                val isSelected = currentSelectionIndex == index
                NavItem(screen = screen, isSelected = isSelected) {
                    if (currentDestination?.route != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(screen: Screen, isSelected: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val localeContext = getLocaleContext()

    // Animate color: Active = Primary, Inactive = Gray
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "iconColor"
    )

    // Icon "floats" up when selected by reducing its offset
    val iconOffsetY by animateDpAsState(
        targetValue = if (isSelected) (-2).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "iconOffsetY"
    )

    // Get localized title for the screen
    val localizedTitle = when (screen) {
        is Screen.Home -> localeContext.getString(R.string.home)
        is Screen.Alerts -> localeContext.getString(R.string.alerts)
        is Screen.Profile -> localeContext.getString(R.string.profile)
        is Screen.Map -> localeContext.getString(R.string.map)
        else -> screen.title
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .noRippleClickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        // Use Tabler icons for bottom navigation
        val tablerIconRes = when (screen) {
            is Screen.Home -> R.drawable.ic_tabler_home
            is Screen.Alerts -> R.drawable.ic_tabler_bell_ringing
            is Screen.Map -> R.drawable.ic_tabler_map_pin
            is Screen.Profile -> R.drawable.ic_tabler_user
            else -> null
        }
        
        if (tablerIconRes != null) {
            Column(
                modifier = Modifier.animateContentSize(animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = tablerIconRes),
                    contentDescription = localizedTitle,
                    tint = iconColor,
                    modifier = Modifier
                        .offset(y = iconOffsetY)
                        .size(if (isSelected) 26.dp else 24.dp) // Slightly larger when selected
                )

                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(animationSpec = tween(delayMillis = 150)) + slideInVertically { it / 2 },
                    exit = fadeOut(animationSpec = tween(150))
                ) {
                    Text(
                        text = localizedTitle,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}