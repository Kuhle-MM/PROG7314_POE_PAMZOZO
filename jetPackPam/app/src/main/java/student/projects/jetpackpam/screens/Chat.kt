package student.projects.jetpackpam.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch
import student.projects.jetpackpam.design_system.MessageTextField
import student.projects.jetpackpam.design_system.PrimaryIconButton
import student.projects.jetpackpam.models.Message
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun ChatScreen() {
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }

    val listState = rememberLazyListState()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            when (deviceConfiguration) {
                DeviceConfiguration.MOBILE_PORTRAIT,
                DeviceConfiguration.MOBILE_LANDSCAPE -> {
                    ChatMessageInput(
                        messageText = messageText,
                        onMessageTextChange = { messageText = it },
                        onSendClick = {
                            if (messageText.isNotBlank()) {
                                messages = messages + Message(messageText, true)
                                messageText = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                DeviceConfiguration.TABLET_PORTRAIT,
                DeviceConfiguration.TABLET_LANDSCAPE,
                DeviceConfiguration.DESKTOP -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        ChatMessageInput(
                            messageText = messageText,
                            onMessageTextChange = { messageText = it },
                            onSendClick = {
                                if (messageText.isNotBlank()) {
                                    messages = messages + Message(messageText, true)
                                    messageText = ""
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .widthIn(max = 540.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .consumeWindowInsets(WindowInsets.navigationBars)

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }

        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Column(
                    modifier = rootModifier.background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChatList(messages = messages, listState = listState, modifier = Modifier.fillMaxSize())
                }
            }

            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row(
                    modifier = rootModifier
                        .windowInsetsPadding(WindowInsets.displayCutout)
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    ChatList(
                        messages = messages,
                        listState = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            DeviceConfiguration.TABLET_PORTRAIT,
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.DESKTOP -> {
                Column(
                    modifier = rootModifier
                        .verticalScroll(rememberScrollState())
                        .padding(top = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(modifier = Modifier.widthIn(max = 540.dp)) {
                        ChatList(
                            messages = messages,
                            listState = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatList(
    messages: List<Message>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        reverseLayout = true,
        verticalArrangement = Arrangement.Bottom,
        contentPadding = PaddingValues(bottom = 8.dp, top = 8.dp)
    ) {
        items(messages) { message ->
            ChatBubble(message = message, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ChatBubble(message: Message, modifier: Modifier = Modifier) {
    val isUserMe = message.isFromMe
    Row(
        modifier = modifier,
        horizontalArrangement = if (isUserMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isUserMe) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUserMe) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ChatMessageInput(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceDim, shape = MaterialTheme.shapes.medium)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PrimaryIconButton(
            icon = {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach",
                    tint = MaterialTheme.colorScheme.background
                )
            },
            onClick = { /* TODO: implement attach */ }
        )

        Spacer(modifier = Modifier.width(8.dp))

        MessageTextField(
            modifier = Modifier.weight(1f),
            text = messageText,
            onValueChange = onMessageTextChange,
            label = "Message",
            hint = "Type your message..."
        )

        Spacer(modifier = Modifier.width(8.dp))

        PrimaryIconButton(
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.background
                )
            },
            onClick = onSendClick
        )
    }
}
