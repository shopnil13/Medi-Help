package com.medihelp.app.core.designsystem.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.medihelp.app.R
import com.medihelp.app.core.designsystem.theme.Red600
import com.medihelp.app.core.designsystem.theme.Warm100

// Outlined icons per the design system's iconography rule (Material Symbols
// Outlined). A "Tips" destination also appears in the dashboard mockup but is
// not wired here: health tips are a later phase and an empty tab would be a
// dead end.
enum class BottomNavTab(@StringRes val labelRes: Int, val icon: ImageVector) {
    HOME(R.string.nav_home, Icons.Outlined.Home),
    MEDICINES(R.string.nav_medicines, Icons.Outlined.Medication),
    VITALS(R.string.nav_vitals, Icons.Outlined.MonitorHeart),
    DOCUMENTS(R.string.nav_upload, Icons.Outlined.FileUpload),
}

@Composable
fun MediHelpBottomNavBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        BottomNavTab.entries.forEach { tab ->
            val label = stringResource(tab.labelRes)
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(imageVector = tab.icon, contentDescription = null) },
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
                    indicatorColor = Warm100,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
