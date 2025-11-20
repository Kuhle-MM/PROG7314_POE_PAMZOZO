package student.projects.jetpackpam.screens.sidenavscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import student.projects.jetpackpam.models.LanguageViewModel

@Composable
fun SideNav(
    languageViewModel: LanguageViewModel,
    currentRoute: String?,
    onItemSelected: (String) -> Unit,
    onLogout: () -> Unit
) {

    val uiTexts = languageViewModel.uiTexts

    val items = listOf(
        SideNavItem("profile", "profile", Icons.Default.AccountCircle),
        SideNavItem("language", "language", Icons.Default.Language),
        SideNavItem("fontSize", "fontSize", Icons.Default.FormatSize),
        SideNavItem("pamTheme", "pamTheme", Icons.Default.Palette),
        SideNavItem("personality", "personality", Icons.Default.Person),
        //SideNavItem("liveLogs", "liveLogs", Icons.Default.Receipt),
        SideNavItem("settings", "settings", Icons.Default.Settings)
    )

    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = uiTexts["P.A.M"] ?: "P.A.M",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 24.dp)
        )

        items.forEach { item ->
            NavigationDrawerItem(
                label = {
                    Text(
                        text = uiTexts[item.labelKey]
                            ?: item.labelKey.replaceFirstChar { it.uppercaseChar() }
                    )
                },
                selected = currentRoute == item.route,
                onClick = { onItemSelected(item.route) },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = uiTexts[item.labelKey] ?: item.labelKey
                    )
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItem(
            label = { Text(text = uiTexts["logout"] ?: "Logout") },
            selected = false,
            onClick = { onLogout() },
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = uiTexts["logout"] ?: "Logout") },
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .align(Alignment.CenterHorizontally)
        )
    }
}
