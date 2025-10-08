package student.projects.jetpackpam.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import student.projects.jetpackpam.design_system.MessageTextField
import student.projects.jetpackpam.design_system.PrimaryIconButton
import student.projects.jetpackpam.models.Message
import student.projects.jetpackpam.retrofit.AskRequest
import student.projects.jetpackpam.retrofit.languageApi

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val activity = context as Activity
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Pair<Message, Message?>>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var typingMessageIndex by remember { mutableStateOf<Int?>(null) }

    // --- Speech Recognition ---
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val isListening = remember { mutableStateOf(false) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-UK")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening...")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListening.value = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening.value = false }
            override fun onError(error: Int) { isListening.value = false }
            override fun onResults(results: Bundle?) {
                isListening.value = false
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()?.let { messageText = it }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose { speechRecognizer.destroy() }
    }

    fun startListening() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) speechRecognizer.startListening(speechIntent)
        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        isListening.value = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
                .imePadding()
        ) {
            // --- Chat messages ---
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Bottom,
                reverseLayout = true
            ) {
                itemsIndexed(messages) { index, (userMsg, geminiMsg) ->
                    ChatBubble(userMsg)
                    if (typingMessageIndex == index) {
                        ChatBubble(Message("__typing__", false))
                    } else {
                        geminiMsg?.let { ChatBubble(it) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Input area ---
            MessageInput(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        val userMsg = Message(messageText, true)
                        messages.add(userMsg to null)
                        val index = messages.indexOfFirst { it.first == userMsg }
                        coroutineScope.launch { listState.animateScrollToItem(0) }
                        messageText = ""

                        // Show typing indicator
                        typingMessageIndex = index

                        // --- Launch API simulation in a coroutine ---
                        coroutineScope.launch {
                            delay(800)
                            try {
                                sendMessageToApi(userMsg.text, { response ->
                                    typingMessageIndex = null
                                    messages[index] = userMsg to Message(response, false)
                                    coroutineScope.launch { listState.animateScrollToItem(0) }
                                }, { error ->
                                    typingMessageIndex = null
                                    messages[index] = userMsg to Message("Error: $error", false)
                                })
                            } catch (e: Exception) {
                                typingMessageIndex = null
                                messages[index] = userMsg to Message("Error: ${e.localizedMessage}", false)
                            }
                        }
                    }
                },
                onMicHold = { startListening() },
                onMicRelease = { stopListening() },
                isListening = isListening.value,
                onSpotifyClick = { openSpotify(context) },
                onCallClick = { openDialer(context) }
            )

            if (isListening.value) {
                Text(
                    text = "🎙 Listening...",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            }
        }
    }
}


@Composable
fun ChatBubble(message: Message, modifier: Modifier = Modifier) {
    val isUserMe = message.isFromMe
    Row(
        modifier = modifier.padding(vertical = 4.dp),
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
                    color = if (isUserMe) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val dots = listOf(0, 150, 300).map { delay ->
        infiniteTransition.animateFloat(
            0f, -6f,
            animationSpec = infiniteRepeatable(tween(300, delayMillis = delay, easing = LinearEasing), RepeatMode.Reverse)
        )
    }

    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dots.forEach { dot -> Text(".", modifier = Modifier.offset(y = dot.value.dp), style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
fun MessageInput(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,   // ✅ Normal lambda
    onMicHold: () -> Unit,
    onMicRelease: () -> Unit,
    isListening: Boolean,
    onSpotifyClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceDim, shape = MaterialTheme.shapes.medium)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Spotify button
        PrimaryIconButton(
            icon = { Icon(Icons.Default.MusicNote, contentDescription = "Spotify") },
            onClick = onSpotifyClick
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Call button
        PrimaryIconButton(
            icon = { Icon(Icons.Default.Phone, contentDescription = "Call") },
            onClick = onCallClick
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Mic button
        Box(
            modifier = Modifier
                .size(48.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onMicHold()
                            tryAwaitRelease()
                            onMicRelease()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(if (isListening) scale else 1f)
                    .clip(CircleShape)
                    .background(if (isListening) Color.Red.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Mic", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Message input field
        MessageTextField(
            modifier = Modifier.weight(1f),
            text = messageText,
            onValueChange = onMessageTextChange,
            label = "Message",
            hint = if (isListening) "Listening..." else "Type a message..."
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Send button
        PrimaryIconButton(
            icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send") },
            onClick = onSendClick // ✅ Correct usage
        )
    }
}


fun sendMessageToApi(
    message: String,
    onResponse: (String) -> Unit,
    onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = languageApi.askGemini(AskRequest(question = message))
                 onResponse(response.answer)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
}

fun openSpotify(context: android.content.Context) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
        if (intent != null) context.startActivity(intent)
        else context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.music")))
    } catch (e: Exception) { Toast.makeText(context, "Could not open Spotify", Toast.LENGTH_SHORT).show() }
}

fun openDialer(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_DIAL)
    context.startActivity(intent)
}
