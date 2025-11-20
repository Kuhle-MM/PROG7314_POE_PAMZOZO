package student.projects.jetpackpam.appNavigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Games
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.VideoCall
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomBarScreen (
    val route: String,
    val title: String,
    val icon: ImageVector
    ) {

        object Home : BottomBarScreen(
            route = "home",
            title = "Home",
            icon = Icons.Outlined.Home
        )

        object Video : BottomBarScreen(
            route = "video",
            title = "Video",
            icon = Icons.Outlined.VideoCall
        )

        object Games : BottomBarScreen(
            route = "followMe",
            title = "Follow Me",
            icon = Icons.Outlined.People
        )
}