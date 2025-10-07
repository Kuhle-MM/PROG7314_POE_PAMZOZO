package student.projects.jetpackpam.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
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
import kotlinx.coroutines.launch
import student.projects.jetpackpam.design_system.MessageTextField
import student.projects.jetpackpam.design_system.PrimaryIconButton
import student.projects.jetpackpam.models.Message
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val activity = context as Activity
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }

    val listState = rememberLazyListState()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
    val coroutineScope = rememberCoroutineScope()

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
        if (!isGranted) {
            Toast.makeText(context, "Microphone permission is required for speech input", Toast.LENGTH_SHORT).show()
        }
    }

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
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected."
                    SpeechRecognizer.ERROR_NO_MATCH -> "Didn’t catch that. Try again."
                    else -> "Speech recognition error: $error"
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
            Toast.makeText(context, "No speech recognition service found.", Toast.LENGTH_LONG).show()
            return
        }
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            speechRecognizer.startListening(speechIntent)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        isListening.value = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                onMicHold = { startListening() },
                onMicRelease = { stopListening() },
                isListening = isListening.value
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            ChatList(messages = messages, listState = listState, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ChatMessageInput(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onMicHold: () -> Unit,
    onMicRelease: () -> Unit,
    isListening: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    Row(
        modifier = Modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceDim, shape = MaterialTheme.shapes.medium)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mic button with hold detection
        Box(
            modifier = Modifier
                .size(60.dp)
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
                    .size(48.dp)
                    .scale(if (isListening) scale else 1f)
                    .clip(CircleShape)
                    .background(if (isListening) Color.Red.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Hold to talk",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        MessageTextField(
            modifier = Modifier.weight(1f),
            text = messageText,
            onValueChange = onMessageTextChange,
            label = "Message",
            hint = if (isListening) "Listening..." else "Type or hold mic..."
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

    if (isListening) {
        Text(
            text = "🎙 Listening...",
            color = Color.Red,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
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
