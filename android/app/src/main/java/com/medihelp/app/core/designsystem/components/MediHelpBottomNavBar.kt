package com.medihelp.app.core.designsystem.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.medihelp.app.R
import com.medihelp.app.core.designsystem.theme.Red600

/**
 * Destinations in the order the home-screen reference shows them. Each tab
 * carries a filled icon for its selected state, matching the reference's solid
 * red house on the active tab.
 */
enum class BottomNavTab(
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME(R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    MEDICINES(R.string.nav_medicines, Icons.Filled.Medication, Icons.Outlined.Medication),
    VITALS(R.string.nav_vitals, Icons.Filled.MonitorHeart, Icons.Outlined.MonitorHeart),
    DOCUMENTS(R.string.nav_upload, Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
    TIPS(R.string.nav_tips, Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb),
}

@Composable
fun MediHelpBottomNavBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        BottomNavTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            val label = stringResource(tab.labelRes)
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        // A wrapped nav label ("Medicine/s") reads as a typo;
                        // keep each label on one line.
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Red600,
                    selectedTextColor = Red600,
                    // The reference marks the active tab with colour alone.
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
