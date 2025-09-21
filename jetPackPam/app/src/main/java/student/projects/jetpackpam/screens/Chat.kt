package student.projects.jetpackpam.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import student.projects.jetpackpam.screens.accounthandler.LoginFormSection
import student.projects.jetpackpam.screens.accounthandler.LoginHeader
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun ChatScreen()
{
    Scaffold (
        modifier = Modifier
            .fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ){innerPadding ->
        //dynamic variables
        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clip(
                RoundedCornerShape(
                    topStart = 15.dp,
                    topEnd = 15.dp
                )
            )
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(
                horizontal = 16.dp,
                vertical = 24.dp

            )
            .consumeWindowInsets(WindowInsets.navigationBars) // so that the column doesn't overlap


        //This part of the code deals with the adaptiveness of the screen
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
        when(deviceConfiguration){
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Column (
                    modifier = rootModifier
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ){

                }

            }
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row (
                    modifier = rootModifier
                        .windowInsetsPadding(WindowInsets.displayCutout)
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ){

                }

            }
            DeviceConfiguration.TABLET_PORTRAIT,
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.DESKTOP -> {
                Column (
                    modifier = rootModifier
                        .verticalScroll(rememberScrollState())
                        .padding(top = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){

                }

            }
        }


    }
}