package student.projects.jetpackpam.screens.sidenavscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import student.projects.jetpackpam.models.LanguageViewModel

//data class SideNavItem(
//    val route: String,
//    val labelKey: String,
//    val icon: ImageVector
//)

@Composable
fun SideNav(
    currentRoute: String?,
    languageViewModel: LanguageViewModel,
    onItemSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiTexts = languageViewModel.uiTexts

    val items = listOf(
        SideNavItem("profile", "profile", Icons.Default.AccountCircle),
        SideNavItem("language", "language", Icons.Default.Language),
        SideNavItem("fontSize", "fontSize", Icons.Default.FormatSize),
        SideNavItem("pamTheme", "pamTheme", Icons.Default.Palette),
        SideNavItem("personality", "personality", Icons.Default.Person)
    )

    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {

        Text(
            text = uiTexts["settings"] ?: "Settings",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 24.dp)
        )

        items.forEach { item ->
            NavigationDrawerItem(
                label = {
                    Text(uiTexts[item.labelKey]
                        ?: item.labelKey.replaceFirstChar { it.uppercaseChar() })
                },
                selected = currentRoute == item.route,
                onClick = { onItemSelected(item.route) },
                icon = { Icon(item.icon, contentDescription = item.labelKey) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItem(
            label = { Text(uiTexts["logout"] ?: "Logout") },
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Logout") },
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .align(Alignment.CenterHorizontally)
        )
    }
}
