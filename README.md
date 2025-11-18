# P.A.M. (Personal Assistant Machine)

> **PAM** is a compact, portable robotic personal assistant built with a Raspberry Pi + AlphaBotV2 platform, a REST API backend, and an Android Jetpack Compose frontend. She performs object avoidance and fetching, autonomous navigation, live camera streaming, voice & text control, Gemini-powered conversational personalization, and multi-language translation.

---

## 📑 Table of Contents

1.  [Overview](#overview)
2.  [Architecture & Visuals](#architecture--visuals)
3.  [Core Features](#core-features)
4.  [Updated Section (New Features)](#updated-section-new-features)
5.  [Tech Stack](#tech-stack)
6.  [Hardware Setup (Raspberry Pi + AlphaBotV2)](#hardware-setup-raspberry-pi--alphabotv2)
7.  [Backend (REST API)](#backend-rest-api)
      * [Camera](#camera)
      * [Motor](#motor)
      * [Mapping](#mapping)
      * [Games](#games)
      * [Google Calendar](#google-calendar)
      * [Gemini Chat](#gemini-chat)
      * [Translation](#translation)
8.  [Android Frontend (Jetpack Compose)](#android-frontend-jetpack-compose)
      * [UI & Navigation](#ui--navigation)
      * [Key composables & snippets](#key-composables--snippets)
      * [Permissions & Manifest](#permissions--manifest)
      * [Retrofit + API models](#retrofit--api-models)
      * [Text-to-Speech & Speech-to-Text](#text-to-speech--speech-to-text)
      * [Security (Biometrics & SSO)](#security-biometrics--sso)
      * [Offline Data (RoomDB)](#offline-data-roomdb)
9.  [Mapping & Navigation (High level)](#mapping--navigation-high-level)
10. [Examples: API calls and responses](#examples-api-calls-and-responses)
11. [Deployment](#deployment)
12. [Testing & Troubleshooting](#testing--troubleshooting)
13. [Future Improvements](#future-improvements)
14. [Contributing](#contributing)
15. [License](#license)

---

## 🤖 Overview

PAM brings together robotics, cloud APIs, and a modern Android UI to create a helpful companion that can: follow a user via BLE, fetch small objects, provide live MPEG camera feedback, play games, schedule with Google Calendar, and hold a personalized conversational experience via Gemini. The system architecture separates concerns into: hardware controller scripts (Raspberry Pi), a REST API that the mobile app talks to, and the Jetpack Compose mobile app that serves as the UX layer and remote control.

---

## 🏗️ Architecture & Visuals



```text
+-----------------+        HTTPS/WS/MPEG        +------------------+        Serial/I2C/HTTP       +------------------+
|  Android App    | <-------------------------> |  REST API Server | <--------------------------> | Raspberry Pi     |
| (Jetpack Comp.) |                         |  (Python/Node)   |                            | + AlphaBotV2     |
| - Retrofit      |                         | - MPEG Stream    |                            | - Motors         |
| - RoomDB (Loc.) |                         | - Motor control  |                            | - Sensors (ultra)|
| - Biometrics    |                         | - Push Notif.    |                            | - BLE Beacon     |
+-----------------+                         +------------------+                            +------------------+
      | WebSocket (Live Logs)                        | Webhooks/3rd-party                                      
      v                                              v
  Google Calendar  <------------------------>  Gemini / LLM
 (OAuth2)                                     (Secure API)
```
ASCII UI mock (Main screen)
```
---------------------------
| PAM — Live Camera Feed  |
| [ MPEG Video Stream ]   |  <--- Low latency live feed
| [Overlay: Object Log]   |  <--- "Obstacle detected: 20cm"
|-------------------------|
| Movement Controls (JSK) |
| [Forward] [Left] [Stop] |
| [Right] [Backward]      |
|-------------------------|
| Features                |
| [Follow Me (BLE)] [Chat]|
|-------------------------|
| Bottom Nav: Home | Games|
---------------------------
```

##🌟 Core Features

These are the foundational capabilities of the PAM system:

* Motor Control & Navigation: Full omnidirectional control using the AlphaBotV2's driver board. Supports precise speed adjustments via PWM and rotational logic for smooth turning.

* Object Fetching: Utilizes computer vision (OpenCV) to identify specific targets (e.g., a cup or keys) and sequences motor commands to approach and "retrieve" them using the robot's gripper/arms.

* Voice & Text Command: Multi-modal input allows users to speak commands via Speech-to-Text (STT) or type them manually.

* Interactive Games (Charades): A multiplayer mode where PAM uses movement patterns to "act out" words for the user to guess, or uses vision recognition to guess what the user is acting out.

* Google Calendar Integration: Secure OAuth2 access allowing PAM to read upcoming schedules, remind users of events, and add new appointments through voice commands.

* Gemini Conversational AI: A context-aware chatbot powered by Google's Gemini. It maintains session context to provide persona-based, helpful, and human-like responses rather than static commands.

* Multi-Language Translation: Real-time translation service capable of converting spoken or typed text between supported languages, making PAM a useful travel companion.

##🚀 Updated Section (New Features)

The latest version of PAM includes significant architectural and feature enhancements:

* Enhanced User Interface (UI):

  * Welcome & Splash Screens: A polished entry experience with animated branding and initialization status.

  * Material 3 Design: Updated color palettes, typography, and component styling for a modern, accessible look.

* Live Camera Feed (MPEG):

  * Moved from frame-by-frame WebSocket delivery to a raw MPEG stream via rpicam-vid.

  * Benefit: Drastically reduces video latency to milliseconds, enabling smoother remote driving and real-time visual feedback.

* "Follow Me" Mode (BLE):

  * Autonomous tethering using Bluetooth Low Energy (BLE).

  * PAM scans for the user's phone signal strength (RSSI) to estimate distance and automatically drives to maintain a set proximity to the user.

* Live Sensor Logs:

  * A real-time WebSocket channel that streams internal state data (ultrasonic distance, battery voltage, CPU temp) directly to the app.

  * UI Overlay: These logs appear as a transparent overlay on the camera feed, letting the user see exactly what the robot "sees" (e.g., "Obstacle Detected: 15cm").

* Biometric Security:

  * Integration with Android's BiometricPrompt API.

  * Users can lock the application using their device's native Fingerprint or Face Unlock hardware, ensuring unauthorized users cannot control the robot.

* Push Notifications:

  * Integration with Firebase Cloud Messaging (FCM).

  * PAM can send alerts for critical events (e.g., "Battery Low," "Intruder Detected," "Meeting Starting") even when the app is closed.

* Offline Capability (RoomDB):

  *  Implementation of a local SQLite database using Android Room.

  * Benefit: Chat history, application settings, and cached calendar events are stored locally, allowing core app functions to work without an active internet connection.

* Expanded Settings:

  * New configuration screens allowing users to fine-tune robot sensitivity, change turn speeds, toggle "Follow Me" distance thresholds, and manage API keys directly from the app.

##🛠️ Tech Stack

* **Hardware:** Raspberry Pi 3b, AlphaBotV2, Bluetooth Module

* **Backend:** Python (Bottle Framework)

* **Database:**

  * Remote: PostgreSQL/Redis

  * Local Mobile: RoomDB (Android) for offline caching

* **Mobile:** Android (Kotlin) + Jetpack Compose

* **Networking:** Retrofit (REST), MPEG Stream (Video), BLE (Positioning)

* **Security:** Android BiometricManager, OAuth2

* **LLM:** Gemini API (via backend)

##🔌 Hardware Setup (Raspberry Pi + AlphaBotV2)

**1. Flash OS**
```
# Flash Raspberry Pi OS (Lite recommended for headless)
# Using Raspberry Pi Imager or balenaEtcher
# Ensure 'rpicam-vid' is available (default on Bullseye/Bookworm)
```

**2. Initial Pi setup**
```
sudo apt update && sudo apt upgrade -y
sudo raspi-config    # enable ssh, camera, I2C if needed
sudo apt install python3-pip git libatlas-base-dev -y
# Install dependencies (Bottle is used for the API now)
pip3 install bottle pigpio opencv-python numpy bluepy
```

**3. Wiring & AlphaBot setup**

* Follow AlphaBot V2 wiring guide for your Pi header.

* Ensure motor power supply is adequate.

* Ensure AlphaBot.py and PCA9685.py driver files are in the same directory as the scripts below.

**4. Run the robot controller**

The system is now split into three files for stability and performance.

 1. main.py: Handles Motor control and Servos (Port 5000).

 2. stream.py: Handles low-latency video streaming (Port 8000).

 3. pam.py: A launcher script that starts both services simultaneously.

**Create main.py (Motor & Servo Logic):**

```python
#!/usr/bin/env python3
from bottle import post, request, run
from AlphaBot import AlphaBot
from PCA9685 import PCA9685
import math, time

# Robot setup
Ab = AlphaBot()
pwm = PCA9685(0x40)
pwm.setPWMFreq(50)

# Camera servo setup
pan_servo = 0
tilt_servo = 1
pan_angle = 90
tilt_angle = 45

def set_servo_angle(channel, angle):
    pulse = int(4096 * ((0.5 + (2.0 * angle / 180.0)) / 20))
    pwm.setPWM(channel, 0, pulse)

# --- Proper Steering Logic ---
def move_robot_analog(x, y, max_speed):
    # 1. Calculate mix for tank drive
    left = y + x
    right = y - x

    # 2. Normalize results so they don't exceed 1.0
    maximum = max(abs(left), abs(right))
    if maximum > 1.0:
        left /= maximum
        right /= maximum

    # 3. Apply Speed Multiplier
    left_pwm = int(left * max_speed)
    right_pwm = int(right * max_speed)

    # 4. Send to AlphaBot
    Ab.setMotor(left_pwm, right_pwm)

@post('/api/command')
def command():
    data = request.json
    cmd = data.get("cmd")
    speed = int(data.get("speed", 50))

    if cmd == "stop":
        Ab.stop()
    elif cmd == "forward":
        Ab.setMotor(speed, speed)
    elif cmd == "backward":
        Ab.setMotor(-speed, -speed)
    elif cmd == "left":
        Ab.setMotor(-speed, speed) # Spin turn
    elif cmd == "right":
        Ab.setMotor(speed, -speed) # Spin turn
    else:
        return {"status": "error", "msg": "Invalid command"}

    return {"status": "ok", "cmd": cmd, "speed": speed}

@post('/api/joystick')
def joystick():
    data = request.json
    x = float(data.get("x", 0))
    y = float(data.get("y", 0))
    speed = int(data.get("speed", 50))

    # Deadzone
    if abs(x) < 0.05 and abs(y) < 0.05:
        Ab.stop()
        return {"cmd": "stop"}

    move_robot_analog(x, y, speed)
    return {"status": "ok", "x": x, "y": y}

@post('/camera/move')
def move_camera():
    global pan_angle, tilt_angle
    data = request.json
    pan = float(data.get("pan", pan_angle))
    tilt = float(data.get("tilt", tilt_angle))
    pan_angle = max(0, min(180, pan))
    tilt_angle = max(0, min(90, tilt))
    set_servo_angle(pan_servo, pan_angle)
    set_servo_angle(tilt_servo, tilt_angle)
    return {"status": "ok", "pan": pan_angle, "tilt": tilt_angle}

@post('/camera/reset')
def reset_camera():
    global pan_angle, tilt_angle
    pan_angle, tilt_angle = 90, 45
    set_servo_angle(pan_servo, pan_angle)
    set_servo_angle(tilt_servo, tilt_angle)
    return {"status": "ok", "pan": pan_angle, "tilt": tilt_angle}

if __name__ == "__main__":
    run(host="0.0.0.0", port=5000, debug=True)
```

**Create stream.py (Secure MJPEG Streamer):**
```python
#!/usr/bin/env python3
import subprocess
import threading
from http import server
import socketserver
import logging
from urllib.parse import urlparse, parse_qs

SECRET_TOKEN = "mySuperSecretRobotKey123"
PORT = 8000
WIDTH = 640
HEIGHT = 480
FRAMERATE = 15

# Simple HTML viewer
PAGE = f"""\
<html>
<head><title>Secure Robot Stream</title>
<style>html, body {{ height: 100%; margin: 0; padding: 0; background: #000; }} img {{ width: 100%; height: 100%; object-fit: contain; }}</style>
</head>
<body><img src="stream.mjpg?token={SECRET_TOKEN}"/></body>
</html>
"""

class CameraStream:
    def __init__(self):
        self.frame = None
        self.lock = threading.Lock()
        self.cmd = [
            "rpicam-vid", "-t", "0", "--inline",
            "--width", str(WIDTH), "--height", str(HEIGHT),
            "--framerate", str(FRAMERATE), "--codec", "mjpeg", "-o", "-"
        ]
        self.process = None
        self.thread = threading.Thread(target=self.update, daemon=True)
        self.thread.start()

    def update(self):
        self.process = subprocess.Popen(self.cmd, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, bufsize=10**6)
        stream_buffer = b''
        while True:
            chunk = self.process.stdout.read(4096)
            if not chunk: break
            stream_buffer += chunk
            a = stream_buffer.find(b'\xff\xd8')
            b = stream_buffer.find(b'\xff\xd9')
            if a != -1 and b != -1:
                jpg = stream_buffer[a:b+2]
                stream_buffer = stream_buffer[b+2:]
                with self.lock: self.frame = jpg

camera = CameraStream()

class StreamingHandler(server.BaseHTTPRequestHandler):
    def do_GET(self):
        parsed = urlparse(self.path)
        token = parse_qs(parsed.query).get('token', [None])[0]

        if token != SECRET_TOKEN:
            self.send_response(403)
            self.end_headers()
            self.wfile.write(b"Access Denied")
            return

        if parsed.path == '/':
            self.send_response(301)
            self.send_header('Location', '/index.html?token=' + SECRET_TOKEN)
            self.end_headers()
        elif parsed.path == '/index.html':
            self.send_response(200)
            self.send_header('Content-Type', 'text/html')
            self.end_headers()
            self.wfile.write(PAGE.encode('utf-8'))
        elif parsed.path == '/stream.mjpg':
            self.send_response(200)
            self.send_header('Content-Type', 'multipart/x-mixed-replace; boundary=FRAME')
            self.end_headers()
            try:
                while True:
                    with camera.lock: frame = camera.frame
                    if frame:
                        self.wfile.write(b'--FRAME\r\n')
                        self.send_header('Content-Type', 'image/jpeg')
                        self.send_header('Content-Length', len(frame))
                        self.end_headers()
                        self.wfile.write(frame)
                        self.wfile.write(b'\r\n')
            except Exception: pass
        else:
            self.send_error(404)

try:
    server = socketserver.ThreadingMixIn.server_factory((('', PORT)), StreamingHandler)
    # Manual server setup omitted for brevity, see full file
    server = socketserver.TCPServer(('', PORT), StreamingHandler)
    print(f"STREAM: http://<IP>:{PORT}/stream.mjpg?token={SECRET_TOKEN}")
    server.serve_forever()
finally:
    if camera.process: camera.process.terminate()
```

**Create pam.py (Launcher):**

To start PAM, simply run this file. It handles the lifecycle of the other two scripts.
```python
#!/usr/bin/env python3
import subprocess, sys, signal, time

commands = [
    ["python3", "main.py"],   # Motor Controls (Port 5000)
    ["python3", "stream.py"]  # Video Stream (Port 8000)
]
processes = []

def cleanup(signum, frame):
    print("\n[PAM] Shutting down systems...")
    for p in processes:
        try: p.terminate()
        except: pass
    sys.exit(0)

signal.signal(signal.SIGINT, cleanup)

print("[PAM] Starting Robot Systems...")
try:
    for cmd in commands:
        processes.append(subprocess.Popen(cmd))
        time.sleep(1)
    
    print("System is LIVE.")
    print("Motor API:  http://<IP>:5000")
    print("Video Feed: http://<IP>:8000")
    
    for p in processes: p.wait()
except Exception as e:
    cleanup(None, None)
```
---

# 🧠 Backend (REST API)

The API has been updated to use the Bottle framework on Port 5000.

## API Modules & Endpoints

* POST /api/command — Standard Directional Control

  * Payload: { "cmd": "forward", "speed": 60 }

  * Commands: forward, backward, left, right, stop

* POST /api/joystick — Analog Control (Joystick)

  * Payload: { "x": 0.5, "y": 1.0, "speed": 50 }

  * Logic: Uses move_robot_analog mixing for smooth turning.

* POST /camera/move — Pan/Tilt Servo Control

  * Payload: { "pan": 90, "tilt": 45 }

*POST /camera/reset — Camera Reset

  * Resets camera to center position.

* GET http://<IP>:8000/stream.mjpg?token=<KEY> — Port 8000 Dedicated Camera Stream

**Live Log WebSocket Example**
```python
@app.websocket('/ws/logs')
async def log_ws(websocket: WebSocket):
    await websocket.accept()
    while True:
        # Push sensor data to app for UI overlay
        data = {"sensor": "ultrasonic", "distance": 15, "avoidance_active": True}
        await websocket.send_json(data)
```

## 📱 Android Frontend (Jetpack Compose)

Project-level overview (packages)
```
com.yourapp.pam
├─ ui
│  ├─ splash        <-- New Splash/Welcome screens
│  ├─ home
│  ├─ camera        <-- MPEG view implementation
│  ├─ settings      <-- Expanded settings UI
│  └─ auth          <-- Biometrics & Google Auth
├─ data
│  ├─ db            <-- RoomDB (DAO, Entities)
│  ├─ api
│  ├─ ble           <-- BLE Manager
│  └─ repo
├─ di
└─ MainActivity.kt
```

**Key dependencies (Gradle)**
```kotlin
implementation "androidx.compose.ui:ui:1.5.0"
implementation "androidx.room:room-runtime:2.6.0" // Offline DB
kapt "androidx.room:room-compiler:2.6.0"
implementation "androidx.biometric:biometric:1.2.0" // Security
implementation "com.google.android.exoplayer:exoplayer:2.19.0" // Video handling
```

**RoomDB (Offline Database)**

Used to store chat history and user settings locally.
```kotlin
@Entity
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long
)
```

**Biometric Authentication**

Implementing phone's integrated security for app entry.
```kotlin
fun authenticateUser(activity: FragmentActivity) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Navigate to Home
            }
        })
    // Show prompt
}
```

**MJPG Camera View (Compose)**
```kotlin
@Composable
fun MpegCameraStream(url: String) {
    // Implementation using AndroidView to embed a surface
    // capable of rendering MJPEG streams via player or webview
    AndroidView(factory = { context -> 
        WebView(context).apply { loadUrl(url) } 
    })
}
```

**Live Logs Overlay**
```kotlin
@Composable
fun ObstacleOverlay(distance: Int) {
    if(distance < 20) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.Red)) {
            Text("OBSTACLE DETECTED: ${distance}cm", color = Color.White)
        }
    }
}
```
---

## 🗺️ Mapping & Navigation (High level)

PAM's navigation stack includes:

1. Perception — Sensors + MPEG camera feed.

2. Live Logging — The backend streams obstacle data to the app, allowing the user to see exactly what the robot "sees" in real-time.

3. Follow Me (BLE) — Uses RSSI (Received Signal Strength Indicator) from the phone's Bluetooth to triangulate and follow the user.
---
## 🚢 Deployment

* **Docker:** We recommend building the backend services into a Docker container for consistency across development and the Raspberry Pi.

* **Systemd (Raspberry Pi):** create a systemd service to auto-start the robot controller on boot to ensure PAM is ready as soon as the Pi is powered on.

* **Firebase:** Deploy the Firebase Cloud Functions for push notifications and ensure google-services.json is correctly configured in the Android app.
---
## 🧪 Testing & Troubleshooting

Common issues

BLE Disconnects: Ensure location permissions are granted for Bluetooth scanning on Android.

MPEG Latency: Adjust resolution on the Raspberry Pi camera script to reduce bandwidth.

Biometrics fail: Ensure the device has a lock screen security (PIN/Pattern) set up.

🔮 Future Improvements

Use ultrasonic sensors for object detection for increased mapping accuracy.

Enhance games with checkpoint racing around a scanned area.

Integration of advanced SLAM for complex room mapping.

📚 Reference List

1. Admin, 2023. Google Lens Vs Pinterest Lens: The Rising War in the Visual Search Domain - August 2025. Skyram Blog.

2. Android Developers, 2025. Card.

3. Android Knowledge, 2024a. Navigation Component in Jetpack Compose using Kotlin. YouTube.

4. Android Knowledge, 2024b. Navigation Drawer + Bottom Navigation + Bottom Sheet. YouTube.

5. Android Makers, 2024. Building a Joystick Controller using Compose Multiplatform. YouTube.

6. AntMan232, 2012. Raspberry Pi I2C (Python). Instructables.

7. Bukk, A., 2019. Ep.04 - Joystick and touch events. YouTube.

8. Custer, C., 2025. How to Use an API in Python. Dataquest.

9. Data Slayer, 2018. How to Setup Camera Module for Raspberry Pi 3 Model B+. YouTube.

10. Drummond, R., 2015. REST API on a Pi, Part 2: control your GPIO I/O ports over the internet.

11. Google Lens, 2025. Google Lens.

12. Lacker, P., 2025. Firebase Google Sign-In with Jetpack Compose & Clean Architecture. YouTube.

13. Laimonas Naradauskas, 2024. Google Lens: Revolutionising Visual Search Experiences.

14. Lima, L., 2015. Building a Rest API with the Bottle Framework. Toptal Engineering Blog.

15. Milvus, 2025. What Is the Technology behind Google Lens?.

16. Net, in, 2022. Stack Overflow.

17. philipplackner, 2023. GitHub - philipplackner/NestedNavigationGraphsGuide.

18. Rick-Anderson, 2022. Unit Testing ASP.NET Web API 2. Microsoft.com.

19. SK RADWOAN, 2025. Login Page - Walking Application. Dribbble.

20. Visual Paradigm, 2025. UML Class Diagram Tutorial.

21. W L PROJECT, 2023. Change App Icon In Android Studio Jetpack Compose. YouTube.

22. Waveshare, 2025. AlphaBot2-Pi - Waveshare Wiki.
