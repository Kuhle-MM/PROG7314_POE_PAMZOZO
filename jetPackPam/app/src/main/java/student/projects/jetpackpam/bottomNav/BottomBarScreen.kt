package student.projects.jetpackpam.bottomNav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Games
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.VideoCall
import androidx.compose.ui.Modifier
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
            route = "games",
            title = "Games",
            icon = Icons.Outlined.Games
        )
}