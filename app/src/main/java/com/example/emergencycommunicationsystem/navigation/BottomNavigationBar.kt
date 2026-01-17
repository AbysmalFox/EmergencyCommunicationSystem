package com.example.emergencycommunicationsystem.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
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
import com.example.emergencycommunicationsystem.ui.theme.ThemeManager
import com.example.emergencycommunicationsystem.ui.theme.BrandTealAccent
import com.example.emergencycommunicationsystem.ui.theme.BrandDeepTeal
import androidx.compose.ui.graphics.Color
import com.example.emergencycommunicationsystem.util.getLocaleContext

const val navOverlayHeight = 84 
const val navOverlayLift = 20 

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

    // CRITICAL FIX: Use ThemeManager to detect selected appearance, not system default
    val isDark = ThemeManager.isDarkMode()
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(navOverlayHeight.dp)
            .padding(horizontal = 24.dp)
            .offset(y = (-navOverlayLift).dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color.Black.copy(alpha = if (isDark) 0.5f else 0.2f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (isDark) Color(0xFF1E293B) else Color.White
            )
            .border(
                width = 1.dp,
                color = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        val containerWidth = LocalConfiguration.current.screenWidthDp.dp - 48.dp
        val itemWidth = containerWidth / screens.size

        val indicatorOffset: Dp by animateDpAsState(
            targetValue = itemWidth * currentSelectionIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy, 
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "indicatorOffset"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(itemWidth)
                .fillMaxHeight()
                .padding(vertical = 10.dp, horizontal = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else Color(0xFF00897B).copy(alpha = 0.08f)
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEachIndexed { index, screen ->
                val isSelected = currentSelectionIndex == index
                NavItem(screen = screen, isSelected = isSelected, isDarkMode = isDark) {
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
private fun RowScope.NavItem(screen: Screen, isSelected: Boolean, isDarkMode: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val localeContext = getLocaleContext()

    val accentColor = if (isDarkMode) Color(0xFF4DB6AC) else Color(0xFF00897B)
    val inactiveColor = if (isDarkMode) Color.Gray.copy(alpha = 0.6f) else Color.LightGray

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else inactiveColor,
        animationSpec = tween(400),
        label = "iconColor"
    )

    val itemScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "itemScale"
    )

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
            .scale(itemScale)
            .noRippleClickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        val tablerIconRes = when (screen) {
            is Screen.Home -> R.drawable.ic_tabler_home
            is Screen.Alerts -> R.drawable.ic_tabler_bell_ringing
            is Screen.Map -> R.drawable.ic_tabler_map_pin
            is Screen.Profile -> R.drawable.ic_tabler_user
            else -> null
        }
        
        if (tablerIconRes != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = tablerIconRes),
                    contentDescription = localizedTitle,
                    tint = iconColor,
                    modifier = Modifier.size(if (isSelected) 28.dp else 24.dp)
                )

                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 10 }),
                    exit = fadeOut()
                ) {
                    Text(
                        text = localizedTitle,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 2.dp)
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
