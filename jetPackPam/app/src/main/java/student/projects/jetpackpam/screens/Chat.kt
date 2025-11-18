package student.projects.jetpackpam.screens

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.util.DeviceConfiguration
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.withContext
import student.projects.jetpackpam.retrofit.AskRequest
import student.projects.jetpackpam.retrofit.languageApi

// Simple message model
data class Message(val text: String, val isFromMe: Boolean)

// Public ChatScreen to use in your NavGraph
@Composable
fun ChatScreen(languageViewModel: LanguageViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // -----------------------
        // Canvas background circles
        // -----------------------
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 15f)

            // Top-right circle
            drawCircle(
                color = Color(0xFFF0A1F8),
                radius = 325f,

                center = Offset(x = size.width - 50f, y = 50f),
                style = stroke
            )

            // Bottom-left circle
            drawCircle(
                color = Color(0xFFFF9BC9),
                radius = 720f,
                center = Offset(x = 50f, y = size.height - 50f),
                style = stroke
            )
        }}
    // Read ui texts snapshot (like your Home screen)
    val uiTexts = languageViewModel.uiTexts
    val context = LocalContext.current
    val activity = context as? Activity

    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isTyping by remember { mutableStateOf(false) }

    // Speech recognition
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val isListening = remember { mutableStateOf(false) }

    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, uiTexts["listening"] ?: "Listening...")
        }
    }

    // Permission launcher replacement: we assume calling screen has requested RECORD_AUDIO or will ask; still check here
    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isListening.value = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening.value = false }
            override fun onError(error: Int) {
                isListening.value = false
                val message = when (error) {
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> uiTexts["no_speech"] ?: "No speech detected."
                    SpeechRecognizer.ERROR_NO_MATCH -> uiTexts["no_match"] ?: "Didn’t catch that. Try again."
                    else -> uiTexts["speech_error"] ?: "Speech recognition error: $error"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            override fun onResults(results: Bundle?) {
                isListening.value = false
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                data?.firstOrNull()?.let { recognizedText ->
                    messageText = recognizedText
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose { speechRecognizer.destroy() }
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, uiTexts["no_speech_service"] ?: "No speech recognition service found.", Toast.LENGTH_LONG).show()
            return
        }
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            speechRecognizer.startListening(speechIntent)
        } else {
            Toast.makeText(context, uiTexts["mic_permission_needed"] ?: "Microphone permission is required.", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        isListening.value = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                reverseLayout = true,
                verticalArrangement = Arrangement.Bottom,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message, modifier = Modifier.fillMaxWidth())
                }
                if (isTyping) {
                    item { ChatBubble(message = Message(uiTexts["typing"] ?: "Typing...", false)) }
                }
            }

            // Input
            MessageInput(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        val userMessage = messageText
                        messages.add(0, Message(userMessage, true))
                        messageText = ""
                        coroutineScope.launch { listState.animateScrollToItem(0) }

                        isTyping = true
                        coroutineScope.launch {
                            sendMessageToApi(userMessage,
                                onResponse = { response ->
                                    isTyping = false
                                    messages.add(0, Message(response, false))
                                    coroutineScope.launch { listState.animateScrollToItem(0) }
                                },
                                onError = { err ->
                                    isTyping = false
                                    messages.add(0, Message(uiTexts["api_error"] ?: "Error: $err", false))
                                })
                        }
                    }
                },
                onMicHold = { startListening() },
                onMicRelease = { stopListening() },
                isListening = isListening.value,
                onSpotifyClick = {
                    try {
                        val spotifyIntent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                        if (spotifyIntent != null) {
                            spotifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(spotifyIntent)
                        } else {
                            // Play Store fallback
                            val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("market://details?id=com.spotify.music")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(playStoreIntent)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, uiTexts["open_spotify_failed"] ?: "Could not open Spotify", Toast.LENGTH_SHORT).show()
                    }
                },
                onCallClick = {
                    try {
                        val callIntent = Intent(Intent.ACTION_DIAL)
                        context.startActivity(callIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, uiTexts["open_dial_failed"] ?: "Could not open dialer", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            if (isListening.value) {
                Text(
                    text = uiTexts["listening"] ?: "Listening...",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

/** Input row — uses dynamic labels from uiTexts via parent */
@Composable
fun MessageInput(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onMicHold: () -> Unit,
    onMicRelease: () -> Unit,
    isListening: Boolean,
    onSpotifyClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Spotify button
        IconButton(onClick = onSpotifyClick) {
            Icon(Icons.Default.MusicNote, contentDescription = "Open Spotify")
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Phone button
        IconButton(onClick = onCallClick) {
            Icon(Icons.Default.Phone, contentDescription = "Open Phone")
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Hold-to-talk mic button (long press)
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
            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse)
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isListening) Color.Red.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary)
                    .then(Modifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Hold to talk", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Text field
        OutlinedTextField(
            value = messageText,
            onValueChange = onMessageTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(text = "Type a message...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = { onSendClick() }
            )
        )


        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onSendClick) {
            Icon(Icons.Default.Send, contentDescription = "Send")
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
                .clip(MaterialTheme.shapes.medium)
                .background(if (isUserMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUserMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/** Simple typing indicator */
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
        dots.forEach { dot ->
            Text(".", modifier = Modifier.offset(y = dot.value.dp), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

suspend fun sendMessageToApi(
    message: String,
    onResponse: (String) -> Unit,
    onError: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            // OLD FUNCTIONALITY RESTORED HERE
            val response = languageApi.askGemini(
                AskRequest(question = message)
            )
            onResponse(response.answer)
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Unknown error")
        }
    }
}
