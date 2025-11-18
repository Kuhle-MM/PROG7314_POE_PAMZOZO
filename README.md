# P.A.M. (Personal Assistant Machine)

> **PAM** is a compact, portable robotic personal assistant built with a Raspberry Pi + AlphaBotV2 platform, a REST API backend, and an Android Jetpack Compose frontend. She performs object avoidance and fetching, autonomous navigation, live camera streaming, voice & text control, Gemini-powered conversational personalization, and multi-language translation.

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture & Visuals](#architecture--visuals)
3. [Features](#features)
4. [Tech Stack](#tech-stack)
5. [Hardware Setup (Raspberry Pi + AlphaBotV2)](#hardware-setup-raspberry-pi--alphabotv2)
6. [Backend (REST API)](#backend-rest-api)

   * Camera
   * Motor
   * Gemini Chat
   * Translation
7. [Android Frontend (Jetpack Compose)](#android-frontend-jetpack-compose)

   * Package structure
   * Key composables & snippets
   * Permissions & Manifest
   * Retrofit + API models
   * Text-to-Speech & Speech-to-Text
   * Google SSO
8. [Examples: API calls and responses](#examples-api-calls-and-responses)
9. [Deployment & CI](#deployment--ci)
10. [Testing & Troubleshooting](#testing--troubleshooting)
11. [Contributing](#contributing)
12. [License](#license)

---

## Overview

PAM brings together robotics, cloud APIs, and a modern Android UI to create a helpful companion that can: follow a user, fetch small objects, provide live camera feedback, play games, schedule with Google Calendar, and hold a personalized conversational experience via Gemini. The system architecture separates concerns into: hardware controller scripts (Raspberry Pi), a REST API that the mobile app talks to, and the Jetpack Compose mobile app that serves as the UX layer and remote control.

---

## Architecture & Visuals

```
+-----------------+       HTTPS/WS      +------------------+       Serial/I2C/HTTP      +------------------+
|  Android App    | <-----------------> |  REST API Server | <------------------------> | Raspberry Pi     |
| (Jetpack Comp.) |                     |  (Python/Node)   |                            | + AlphaBotV2     |
| - Retrofit      |                     | - Camera endpoints|                           | - Motors        |
| - SSO/Google    |                     | - Motor control  |                           | - Sensors (ultra)|
+-----------------+                     +------------------+                           +------------------+
      | WebSocket (live camera)                | Webhooks/3rd-party                                    
      v                                        v
  Google Calendar  <------------------------>  Gemini / LLM
 (OAuth2)                                     (Secure API)
```

**ASCII UI mock (Main screen)**

```
---------------------------
| PAM — Live Camera Feed  |
| [Video Stream]          |  <--- camera mirrored to app
|-------------------------|
| Movement Controls (JSK) |
| [Forward] [Left] [Stop] |
| [Right] [Backward]      |
|-------------------------|
| Chat (Gemini)           |
| [ > Hello PAM ]         |
|-------------------------|
| Bottom Nav: Home | Games |
---------------------------
```

---

## Features

**Core features**

* Live camera streaming mirrored to the mobile app
* Motor control (move, rotate, speed control)
* Environment scanning & mapping for obstacle avoidance
* Fetching routine (extendable arms simulated in the firmware)
* Voice and text command input
* Games: Charades (multiplayer via app)
* Gemini integration for personalized chat responses
* Translation service for local languages
* Authentication: SSO + biometric support
* Offline mode with sync

---

## Tech Stack

* Hardware: Raspberry Pi 3, AlphaBotV2
* Backend: Python (Flask/FastAPI) or Node (Express) — examples use Flask + uvicorn if FastAPI
* Database: lightweight (SQLite / PostgreSQL) for persistent state; Redis for pub/sub (optional)
* Mobile: Android (Kotlin) + Jetpack Compose
* Networking: Retrofit (Android), WebSocket for camera streaming
* LLM: Gemini API (via backend)
* Translation: Google Translate API or Open-source alternative

---

## Hardware Setup (Raspberry Pi + AlphaBotV2)

### 1. Flash OS

```bash
# Flash Raspberry Pi OS (Lite recommended for headless)
# Using Raspberry Pi Imager or balenaEtcher
```

### 2. Initial Pi setup

```bash
sudo apt update && sudo apt upgrade -y
sudo raspi-config    # enable ssh, camera, I2C if needed
sudo apt install python3-pip git -y
pip3 install flask pigpio opencv-python numpy
```

### 3. Wiring & AlphaBot setup

* Follow AlphaBot V2 wiring guide for your Pi header.
* Ensure motor power supply is adequate.

### 4. Run the robot controller (example Flask)

`robot_controller.py` (simplified)

```python
from flask import Flask, request, jsonify
import alphabot   # hypothetical wrapper for AlphaBotV2

app = Flask(__name__)
robot = alphabot.AlphaBot()

@app.route('/motor', methods=['POST'])
def motor_control():
    data = request.json
    cmd = data.get('cmd')  # forward, backward, left, right, stop
    speed = data.get('speed', 50)
    if cmd == 'forward':
        robot.forward(speed)
    elif cmd == 'backward':
        robot.backward(speed)
    elif cmd == 'left':
        robot.left(speed)
    elif cmd == 'right':
        robot.right(speed)
    elif cmd == 'stop':
        robot.stop()
    return jsonify({'status': 'ok', 'cmd': cmd})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
```

> NOTE: Replace `alphabot` with your actual AlphaBot control library calls.

---

## Backend (REST API)

We recommend using **FastAPI** for type-safety and automatic docs; Flask is fine for smaller deployments.

### API Modules & Endpoints (high-level)

* `/camera/stream` — WebSocket or MJPEG endpoint for live stream
* `/camera/snapshot` — GET a single frame
* `/motor` — POST motor commands (`{ cmd: "forward", speed: 60 }`)
* `/mapping/scan` — POST start scan / GET map data
* `/mapping/navigation` — POST navigation goals
* `/games/charades` — POST/GET game state, players
* `/calendar/connect` — Begin OAuth flow
* `/calendar/events` — GET/POST events
* `/chat/gemini` — POST user message -> returns LLM response
* `/translate` — POST text + target language -> returns translated text

### Example FastAPI camera endpoint (mjepg or websocket)

```python
from fastapi import FastAPI, WebSocket
import cv2

app = FastAPI()
cap = cv2.VideoCapture(0)

@app.websocket('/ws/camera')
async def camera_ws(websocket: WebSocket):
    await websocket.accept()
    while True:
        ret, frame = cap.read()
        if not ret:
            continue
        _, jpg = cv2.imencode('.jpg', frame)
        await websocket.send_bytes(jpg.tobytes())
```

---

## Mapping & Navigation (High level)

PAM's navigation stack can be split into three layers:

1. **Perception** — sensors/camera + object detection (YOLO/MediaPipe/Edge models) for obstacle detection.
2. **Mapping** — build a 2D occupancy grid or SLAM map using simple Lidar/ultrasonic + odometry. With a Pi, you can use simple occupancy mapping rather than full ROS.
3. **Planning** — A* or D* Lite on the occupancy grid; local collision avoidance uses Reactive methods (e.g., potential fields or simple obstacle stopping + replan).

**Scan workflow**

* `POST /mapping/scan` -> robot rotates slowly and collects depth/ultrasonic/camera frames.
* Backend composes occupancy grid and returns a simple JSON map.

**Example map JSON**

```json
{
  "width": 100,
  "height": 100,
  "resolution": 0.05,
  "data": [0,0,1,1,0,...]  // 0 free, 1 occupied
}
```

---

## Android Frontend (Jetpack Compose)

### Project-level overview (packages)

```
com.yourapp.pam
├─ ui
│  ├─ home
│  ├─ chat
│  ├─ camera
│  ├─ games
│  ├─ settings
│  └─ auth
├─ data
│  ├─ api
│  ├─ models
│  └─ repo
├─ di
├─ utils
└─ MainActivity.kt
```

### Key dependencies (Gradle)

```gradle
implementation "androidx.compose.ui:ui:1.5.0"
implementation "com.squareup.retrofit2:retrofit:2.9.0"
implementation "com.squareup.retrofit2:converter-moshi:2.9.0"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0"
implementation "androidx.navigation:navigation-compose:2.6.0"
implementation "com.google.android.gms:play-services-auth:20.6.0" // Google SSO
implementation "com.google.accompanist:accompanist-permissions:0.32.0"
implementation "androidx.biometric:biometric:1.2.0"
```

### Retrofit interface example

```kotlin
interface PamApi {
    @POST("/motor")
    suspend fun motorCommand(@Body cmd: MotorCommand): Response<ApiResponse>

    @GET("/camera/snapshot")
    suspend fun snapshot(): Response<ResponseBody>

    @POST("/mapping/scan")
    suspend fun startScan(): Response<MapResponse>

    @POST("/chat/gemini")
    suspend fun chat(@Body msg: ChatRequest): Response<ChatResponse>

    @POST("/translate")
    suspend fun translate(@Body req: TranslateRequest): Response<TranslateResponse>
}

// Models
@Serializable
data class MotorCommand(val cmd: String, val speed: Int = 50)
```

### Camera view via WebSocket (Compose)

```kotlin
@Composable
fun CameraView(wsUrl: String, modifier: Modifier = Modifier) {
    val bitmapState = remember { mutableStateOf<Bitmap?>(null) }

    // Launch a coroutine to connect to WebSocket and update bitmapState
    // Show an Image composable from bitmapState
    Box(modifier.fillMaxSize()) {
        bitmapState.value?.let { bmp ->
            Image(bitmap = bmp.asImageBitmap(), contentDescription = "PAM Camera")
        } ?: Text("Connecting...")
    }
}
```

> Implementation note: use OkHttp WebSocket and decode byte arrays into Bitmaps.

### Motor controls composable

```kotlin
@Composable
fun MotorControls(onCommand: suspend (MotorCommand) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row { Button(onClick = { /* send forward */ }) { Text("Forward") } }
        Row {
            Button(onClick = { /* left */ }) { Text("Left") }
            Button(onClick = { /* stop */ }) { Text("Stop") }
            Button(onClick = { /* right */ }) { Text("Right") }
        }
        Row { Button(onClick = { /* back */ }) { Text("Back") } }
    }
}
```

### Chat package (Gemini integration)

* `ui.chat` — composables showing chat history and composer
* `data.api` — endpoint `POST /chat/gemini` used by repository

**Chat UI snippet**

```kotlin
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val state by viewModel.uiState.collectAsState()
    Column {
        LazyColumn(Modifier.weight(1f)) {
            items(state.messages) { msg -> Text(msg.text) }
        }
        ChatInput(onSend = { text -> viewModel.sendMessage(text) })
    }
}
```

### Permissions & AndroidManifest snippet

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.CAMERA" />

<application ...>
    <!-- OAuth redirect, activities etc -->
</application>
```

---

**Server-side example (Python / FastAPI)**

```python
from fastapi import FastAPI
# Use google-auth and google-api-python-client

# Endpoint to exchange code for tokens and save refresh token.
```

**Client-side**: Use Play Services `GoogleSignIn` with `requestScopes(Scope(Scopes.CALENDAR))` and send token to backend.

---

## Gemini Integration (LLM)

* All requests to Gemini should be proxied via your backend to keep API keys secure.
* The backend endpoint `/chat/gemini` receives user messages and forwards them to the Gemini API, possibly using user context (calendar events, preferences) for personalization.

**Server flow**

1. Client -> POST `/chat/gemini` with `{ message, userId }`.
2. Backend enriches prompt (e.g., user's name, timezone, recent events).
3. Backend calls Gemini API securely and returns response.

---

## Translation

* Endpoint `/translate` — proxies to Google Translate, DeepL, or an open-source model.
* UI allows user to set preferred language in `Settings` and translations are applied to chat and UI prompts.

---

## Examples: API calls and responses

**Motor command**

```bash
curl -X POST http://raspi.local:5000/motor \
 -H "Content-Type: application/json" \
 -d '{"cmd":"forward","speed":60}'
```

**Snapshot**

```bash
curl http://raspi.local:5000/camera/snapshot --output snapshot.jpg
```

**Chat (Gemini)**

```bash
curl -X POST https://api.yourserver.com/chat/gemini \
 -H "Authorization: Bearer <user-token>" \
 -H "Content-Type: application/json" \
 -d '{"message":"Hi PAM, how are you?"}'
```

---


## Testing & Troubleshooting

**Common issues**

* *Camera not found*: ensure `raspi-config` camera support enabled and correct device index.
* *Motor not responding*: check wiring and power supply to AlphaBot.
* *WebSocket lag*: compress frames, reduce resolution, or reduce frame rate.

**Testing tips**

* Use SWAGGER to hit the REST endpoints while on the same network.
* Add structured logs on both backend and Pi for debugging.

---


## Reference List

Admin, 2023. Google Lens Vs Pinterest Lens: The Rising War in the Visual Search Domain - August 2025. [online] Skyram Blog. Available at: https://www.skyramtechnologies.com/blog/google-lens-vs-pinterest-lens-visual-search-showdown/#:~:text=Google%20Lens's%20primary%20advantage%20is,visual%20search%20for%20many%20users.
 [Accessed 19 August 2025].

Android Developers, 2025. Biometric. [online] Android Developers. Available at: https://developer.android.com/jetpack/androidx/releases/biometric
 [Accessed 15 November 2025].

Android Developers, 2025. Card. [online] Android Developers. Available at: https://developer.android.com/develop/ui/compose/components/card
 [Accessed 7 October 2025].

Android Knowledge, 2024a. Navigation Component in Jetpack Compose using Kotlin | Android Studio. [online] YouTube. Available at: https://www.youtube.com/watch?v=a3Y2uncgAMM
 [Accessed 7 October 2025].

Android Knowledge, 2024b. Navigation Drawer + Bottom Navigation + Bottom Sheet in Jetpack Compose | Android Studio. [online] YouTube. Available at: https://www.youtube.com/watch?v=KkJb6rx0gC4
 [Accessed 4 October 2025].

Android Makers, 2024. Building a Joystick Controller using Compose Multiplatform. [online] YouTube. Available at: https://www.youtube.com/watch?v=6xHeRprn_34&t=100s
 [Accessed 7 October 2025].

AntMan232, 2012. Raspberry Pi I2C (Python). [online] Instructables. Available at: https://www.instructables.com/Raspberry-Pi-I2C-Python/
 [Accessed 7 October 2025].

Bukk, A., 2019. Ep.04 - Joystick and touch events | Android Studio 2D Game Development. [online] YouTube. Available at: https://www.youtube.com/watch?v=3oZ2jt0hQmo&t=25s
 [Accessed 7 October 2025].

Custer, C., 2025. How to Use an API in Python – Dataquest. [online] Dataquest. Available at: https://www.dataquest.io/blog/api-in-python/
 [Accessed 23 September 2025].

Data Slayer, 2018. How to Setup Camera Module for Raspberry Pi 3 Model B+. [online] YouTube. Available at: https://www.youtube.com/watch?v=tHjwx2AQHxU
 [Accessed 7 October 2025].

Drummond, R., 2015. REST API on a Pi, Part 2: control your GPIO I/O ports over the internet. [online] Robert-Drummond. Available at: https://robert-drummond.com/2015/06/01/rest-api-on-a-pi-part-2-control-your-gpio-io-ports-over-the-internet/
 [Accessed 7 October 2025].

Google Lens, 2025. Google Lens. [online] Google Lens. Available at: https://lens.google/
 [Accessed 20 August 2025].

Lacker, P., 2025. Firebase Google Sign-In with Jetpack Compose & Clean Architecture - Android Studio Tutorial. [online] YouTube. Available at: https://www.youtube.com/watch?v=zCIfBbm06QM&t=28s
 [Accessed 6 October 2025].

Laimonas Naradauskas, 2024. Google Lens: Revolutionising Visual Search Experiences. [online] Smarter Digital Marketing. Available at: https://www.smarterdigitalmarketing.co.uk/google-lens/
 [Accessed 19 August 2025].

Lima, L., 2015. Building a Rest API with the Bottle Framework. [online] Toptal Engineering Blog. Available at: https://www.toptal.com/python/building-a-rest-api-with-bottle-framework
 [Accessed 7 October 2025].

Milvus, 2025. What Is the Technology behind Google Lens?. [online] Milvus.io. Available at: https://milvus.io/ai-quick-reference/what-is-the-technology-behind-google-lens
 [Accessed 19 August 2025].

Microsoft, 2025. Real-time ASP.NET with SignalR | .NET. [online] Microsoft. Available at: https://dotnet.microsoft.com/en-us/apps/aspnet/signalr
 [Accessed 15 November 2025].

Net, in, 2022. Stack Overflow. [online] Stack Overflow. Available at: https://stackoverflow.com/questions/73540601/writing-unit-test-cases-for-web-api-in-net
 [Accessed 6 October 2025].

philipplackner, 2023. GitHub - philipplackner/NestedNavigationGraphsGuide. [online] GitHub. Available at: https://github.com/philipplackner/NestedNavigationGraphsGuide
 [Accessed 4 October 2025].

philipplackner, 2025. BiometricAuth/app/src/main/java/com/plcoding/biometricauth/BiometricPromptManager.kt at master · philipplackner/BiometricAuth. [online] GitHub. Available at: https://github.com/philipplackner/BiometricAuth/blob/master/app/src/main/java/com/plcoding/biometricauth/BiometricPromptManager.kt
 [Accessed 17 November 2025].

Philipp Lackner, 2024. How to Implement Biometric Auth in Your Android App. [online] YouTube. Available at: https://www.youtube.com/watch?v=_dCRQ9wta-I
.

Rick-Anderson, 2022. Unit Testing ASP.NET Web API 2. [online] Microsoft.com. Available at: https://learn.microsoft.com/en-us/aspnet/web-api/overview/testing-and-debugging/unit-testing-with-aspnet-web-api
 [Accessed 6 October 2025].

Sattar, S., 2023. Biometric Login System in Android with Jetpack Compose. [online] Medium. Available at: https://isaacsufyan.medium.com/biometric-login-system-in-android-with-jetpack-compose-d5f38afa5356
 [Accessed 15 November 2025].

SK RADWOAN, 2025. Login Page - Walking Application. [online] Dribbble. Available at: https://dribbble.com/shots/26177865-Login-Page-Walking-Application
 [Accessed 20 August 2025].

Visual Paradigm, 2025. UML Class Diagram Tutorial. [online] Visual-paradigm.com. Available at: https://www.visual-paradigm.com/guide/uml-unified-modeling-language/uml-class-diagram-tutorial/
 [Accessed 22 August 2025].

W L PROJECT, 2023. Change App Icon In Android Studio Jetpack Compose | Change The Launcher Logo In Android Studio. [online] YouTube. Available at: https://www.youtube.com/watch?v=HkNcLyiKM6U
 [Accessed 7 October 2025].

Wadepickett, 2023. Get started with ASP.NET Core SignalR. [online] Microsoft.com. Available at: https://learn.microsoft.com/en-za/aspnet/core/tutorials/signalr?view=aspnetcore-10.0&WT.mc_id=dotnet-35129-website&tabs=visual-studio
 [Accessed 15 November 2025].

Waveshare, 2025. AlphaBot2-Pi - Waveshare Wiki. [online] Waveshare. Available at: https://www.waveshare.com/wiki/AlphaBot2-Pi
 [Accessed 7 October 2025].

