package student.projects.jetpackpam.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            ChatMessageInput(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        messages = messages + Message(messageText, true)
                        messageText = ""
                        coroutineScope.launch { listState.animateScrollToItem(0) }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { innerPadding ->

        // Only one Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Chat list
            ChatList(messages = messages, listState = listState, modifier = Modifier.weight(1f))

            // Optional buttons
             ExternalAppButtons()
        }
    }
}

@Composable
fun ExternalAppButtons() {
    val context = LocalContext.current

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {

        // Spotify
        Button(onClick = {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Spotify not installed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error opening Spotify", Toast.LENGTH_SHORT).show()
            }
        }) { Text("Spotify") }

//        // Phone
        Button(onClick = {
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    //data = Uri.parse("tel:+27123456789")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Phone app not available", Toast.LENGTH_SHORT).show()
            }
        }) { Text("Call") }

        // WhatsApp
        Button(onClick = {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                   // data = Uri.parse("https://wa.me/+27123456789")
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }) { Text("WhatsApp") }
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
            if (message.text == "__typing__") {
                TypingIndicator()
            } else {
                Text(
                    text = message.text,
                    color = if (isUserMe) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typingAnim")

    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, delayMillis = 150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, delayMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dot3"
    )

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(".", modifier = Modifier.offset(y = dot1.dp))
        Spacer(modifier = Modifier.width(2.dp))
        Text(".", modifier = Modifier.offset(y = dot2.dp))
        Spacer(modifier = Modifier.width(2.dp))
        Text(".", modifier = Modifier.offset(y = dot3.dp))
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
