package student.projects.jetpackpam.screens.sidenavscreen


import androidx.compose.ui.graphics.vector.ImageVector

data class SideNavItem(
    val route: String,
    val labelKey: String, // <-- this is used to fetch the text from uiTexts
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)