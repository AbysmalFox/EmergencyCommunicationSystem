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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.animation.animateContentSize

const val navOverlayHeight = 92
const val navOverlayLift = 36

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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(navOverlayHeight.dp)
            .padding(horizontal = 20.dp)
            .offset(y = (-navOverlayLift).dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.background)
            .border( // Add the border here
                width = 2.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Subtle outline
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
                .background(MaterialTheme.colorScheme.surface)
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

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "iconColor"
    )

    // Icon "floats" up when selected by reducing its offset
    val iconOffsetY by animateDpAsState(
        targetValue = if (isSelected) (-2).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "iconOffsetY"
    )

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
        screen.icon?.let { icon ->
            Column(
                modifier = Modifier.animateContentSize(animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = screen.title,
                    tint = iconColor,
                    modifier = Modifier.offset(y = iconOffsetY)
                )

                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(animationSpec = tween(delayMillis = 150)) + slideInVertically { it / 2 },
                    exit = fadeOut(animationSpec = tween(150))
                ) {
                    Text(
                        text = screen.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
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
