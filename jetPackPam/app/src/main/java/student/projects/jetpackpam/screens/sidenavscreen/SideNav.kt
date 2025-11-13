package student.projects.jetpackpam.screens.sidenavscreen
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SideNav(
    currentRoute: String?,
    onItemSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    val items = listOf(
        SideNavItem("profile", "Profile", Icons.Default.AccountCircle),
        SideNavItem("language", "Language", Icons.Default.Language),
        SideNavItem("fontSize", "Font Size", Icons.Default.FormatSize),
        SideNavItem("pamTheme", "Pam Theme", Icons.Default.Palette),
       // SideNavItem("personality", "Personality", Icons.Default.Person)
    )

    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 24.dp)
        )

        // 🔹 Side navigation items
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onItemSelected(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout option at the bottom
        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = { onLogout() },
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Logout") },
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .align(Alignment.CenterHorizontally)
        )
    }
}
