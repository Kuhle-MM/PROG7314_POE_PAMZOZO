package student.projects.jetpackpam.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import student.projects.jetpackpam.R

@Composable
fun SplashScreen(navController: NavHostController) {

    val density = LocalDensity.current

    // ---------------------------
    // Animation States
    // ---------------------------

    // ⭐ Slower flip animation
    var flip by remember { mutableStateOf(0f) }
    val flipAnim by animateFloatAsState(
        targetValue = flip,
        animationSpec = tween(
            durationMillis = 2600,        // slower
            easing = FastOutSlowInEasing
        )
    )

    // Drop animation
    val dropAnim = remember { Animatable(0f) }

    // Trickle animation for pink background
    val trickleAnim = remember { Animatable(0f) }

    val pink = Color(0xFFF1A2D2)

    // ---------------------------
    // Sequence Controller
    // ---------------------------
    LaunchedEffect(Unit) {

        // 1) Flip first image slower
        flip = 180f
        delay(2150)

        // 2) Complete second logo flip (finishing the rotation)
        dropAnim.snapTo(0f)
        flip = 180f          // complete second half of rotation
        delay(900)

        // 3) SECOND IMAGE DROPS
//        dropAnim.animateTo(
//            targetValue = 250f,
//            animationSpec = tween(900, easing = FastOutSlowInEasing)
//        )

       // delay(50)

        // 4) Pink trickle (water effect)
//        trickleAnim.animateTo(
//            targetValue = 1f,
//            animationSpec = tween(
//                durationMillis = 1800,
//                easing = CubicBezierEasing(0.30f, 0.10f, 0.10f, 1.0f) // smoother water feel
//            )
//        )
//
//        delay(600)

        // Go to welcome screen
        navController.navigate("welcome") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // ---------------------------
    // UI Layout
    // ---------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {

        // -------------------------------------
        // FIRST IMAGE (0° → 90°)
        // -------------------------------------
        if (flipAnim < 90f) {
            Image(
                painter = painterResource(id = R.drawable.nobgpamwordlogo),
                contentDescription = "Logo 1",
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer {
                        rotationY = flipAnim
                        cameraDistance = 12 * density.density
                    },
                contentScale = ContentScale.Fit
            )
        }

        // -------------------------------------
        // SECOND IMAGE (appears at 90° and continues flip)
        // -------------------------------------
        if (flipAnim >= 90f) {
            val secondRotation =
                if (flipAnim <= 180f) (flipAnim - 180f) else (flipAnim - 180f)

            Image(
                painter = painterResource(id = R.drawable.pamicon),
                contentDescription = "Logo 2",
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer {
                        // flips in from center to complete rotation
                        rotationY = secondRotation
                        cameraDistance = 12 * density.density
                    }
                   // .offset(y = dropAnim.value.dp),     // drop AFTER flipping
                //contentScale = ContentScale.Fit
            )
        }

        // -------------------------------------
        // PINK TRICKLE (water flowing down)
        // -------------------------------------
//        val trickleHeight = (trickleAnim.value * 2000).dp
//
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(trickleHeight)
//                .align(Alignment.TopCenter)
//                .background(pink)
//        )
    }
}
