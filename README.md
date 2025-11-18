P.A.M. (Personal Assistant Machine)

PAM is a compact, portable robotic personal assistant built with a Raspberry Pi + AlphaBotV2 platform, a REST API backend, and an Android Jetpack Compose frontend. She performs object detection and fetching, autonomous navigation, live camera streaming, voice & text control, games (charades), Google Calendar integration, Gemini-powered conversational personalization, and multi-language translation.

Table of Contents

Overview

Architecture & Visuals

Core Features

Updated Section (New Features)

Tech Stack

Hardware Setup (Raspberry Pi + AlphaBotV2)

Backend (REST API)

Camera

Motor

Mapping

Games

Google Calendar

Gemini Chat

Translation

Android Frontend (Jetpack Compose)

UI & Navigation

Key composables & snippets

Permissions & Manifest

Retrofit + API models

Text-to-Speech & Speech-to-Text

Security (Biometrics & SSO)

Offline Data (RoomDB)

Mapping & Navigation (High level)

Examples: API calls and responses

Deployment

Testing & Troubleshooting

Future Improvements

Contributing

License

Overview

PAM brings together robotics, cloud APIs, and a modern Android UI to create a helpful companion that can: follow a user via BLE, fetch small objects, provide live MPEG camera feedback, play games, schedule with Google Calendar, and hold a personalized conversational experience via Gemini. The system architecture separates concerns into: hardware controller scripts (Raspberry Pi), a REST API that the mobile app talks to, and the Jetpack Compose mobile app that serves as the UX layer and remote control.

Architecture & Visuals

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


ASCII UI mock (Main screen)

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


Core Features

These are the foundational capabilities of the PAM system:

Motor Control & Navigation: Full omnidirectional control using the AlphaBotV2's driver board. Supports precise speed adjustments via PWM and rotational logic for smooth turning.

Voice & Text Command: Multi-modal input allows users to speak commands via Speech-to-Text (STT) or type them manually.

Gemini Conversational AI: A context-aware chatbot powered by Google's Gemini. It maintains session context to provide persona-based, helpful, and human-like responses rather than static commands.

Multi-Language Translation: Real-time translation service capable of converting spoken or typed text between supported languages, making PAM a useful travel companion.

Updated Section (New Features)

The latest version of PAM includes significant architectural and feature enhancements:

Enhanced User Interface (UI):

Welcome & Splash Screens: A polished entry experience with animated branding and initialization status.

Material 3 Design: Updated color palettes, typography, and component styling for a modern, accessible look.

Live Camera Feed (MJPG): * Moved from frame-by-frame WebSocket delivery to a raw MJPG stream via rpicam-vid.

Benefit: Drastically reduces video latency to milliseconds, enabling smoother remote driving and real-time visual feedback.

"Follow Me" Mode (BLE): * Autonomous tethering using Bluetooth Low Energy (BLE).

PAM scans for the user's phone signal strength (RSSI) to estimate distance and automatically drives to maintain a set proximity to the user.

Live Sensor Logs: * A real-time WebSocket channel that streams internal state data (ultrasonic distance, battery voltage, CPU temp) directly to the app.

UI Overlay: These logs appear as a transparent overlay on the camera feed, letting the user see exactly what the robot "sees" (e.g., "Obstacle Detected: 15cm").

Biometric Security: * Integration with Android's BiometricPrompt API.

Users can lock the application using their device's native Fingerprint or Face Unlock hardware, ensuring unauthorized users cannot control the robot.

Push Notifications: * Integration with Firebase Cloud Messaging (FCM).

PAM can send alerts for critical events (e.g., "Battery Low," "Intruder Detected," "Meeting Starting") even when the app is closed.

Offline Capability (RoomDB): * Implementation of a local SQLite database using Android Room.

Benefit: Chat history, application settings, and cached calendar events are stored locally, allowing core app functions to work without an active internet connection.

Expanded Settings: * New configuration screens allowing users to fine-tune robot sensitivity, change turn speeds, toggle "Follow Me" distance thresholds, and manage API keys directly from the app.

Tech Stack

Hardware: Raspberry Pi 3, AlphaBotV2, Bluetooth Module

Backend: Python (Bottle Framework)

Database: * Remote: PostgreSQL/Redis

Local Mobile: RoomDB (Android) for offline caching

Mobile: Android (Kotlin) + Jetpack Compose

Networking: Retrofit (REST), MJPG Stream (Video), BLE (Positioning)

Security: Android BiometricManager, OAuth2

LLM: Gemini API (via backend)

Hardware Setup (Raspberry Pi + AlphaBotV2)

1. Flash OS

# Flash Raspberry Pi OS (Legacy, 32-bit) Lite
# Using Raspberry Pi Imager
# Ensure 'rpicam-vid' is available (default on Bullseye/Bookworm)


2. Initial Pi setup

sudo apt update && sudo apt upgrade -y
sudo raspi-config    # enable ssh, camera, I2C if needed
sudo apt install python3-pip git libatlas-base-dev -y
# Install dependencies (Bottle is used for the API now)
pip3 install bottle pigpio opencv-python numpy bluepy


3. Wiring & AlphaBot setup

Follow AlphaBot V2 wiring guide for your Pi header.

Ensure motor power supply is adequate.

Ensure AlphaBot.py and PCA9685.py driver files are in the same directory as the scripts below.

4. Run the robot controller

The system is now split into three files for stability and performance.

main.py: Handles Motor control and Servos (Port 5000).

stream.py: Handles low-latency video streaming (Port 8000).

pam.py: A launcher script that starts both services simultaneously.

Create main.py (Motor & Servo Logic):

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


Create stream.py (Secure MJPEG Streamer):

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


Create pam.py (Launcher):

To start PAM, simply run this file. It handles the lifecycle of the other two scripts.

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


Backend (REST API)

The API has been updated to use the Bottle framework on Port 5000.

API Modules & Endpoints

POST /api/command — Standard Directional Control

Payload: { "cmd": "forward", "speed": 60 }

Commands: forward, backward, left, right, stop

POST /api/joystick — Analog Control (Joystick)

Payload: { "x": 0.5, "y": 1.0, "speed": 50 }

Logic: Uses move_robot_analog mixing for smooth turning.

POST /camera/move — Pan/Tilt Servo Control

Payload: { "pan": 90, "tilt": 45 }

POST /camera/reset — Resets camera to center position.

GET http://<IP>:8000/stream.mjpg?token=<KEY> — Port 8000 Dedicated Camera Stream.

Live Log WebSocket Example

@app.websocket('/ws/logs')
async def log_ws(websocket: WebSocket):
    await websocket.accept()
    while True:
        # Push sensor data to app for UI overlay
        data = {"sensor": "ultrasonic", "distance": 15, "avoidance_active": True}
        await websocket.send_json(data)


Android Frontend (Jetpack Compose)

Project-level overview (packages)

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


Key dependencies (Gradle)

implementation "androidx.compose.ui:ui:1.5.0"
implementation "androidx.room:room-runtime:2.6.0" // Offline DB
kapt "androidx.room:room-compiler:2.6.0"
implementation "androidx.biometric:biometric:1.2.0" // Security
implementation "com.google.android.exoplayer:exoplayer:2.19.0" // Video handling


RoomDB (Offline Database)

Used to store chat history and user settings locally.

@Entity
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long
)


Biometric Authentication

Implementing phone's integrated security for app entry.

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


MJPG Camera View (Compose)

@Composable
fun MJPG CameraStream(url: String) {
    // Implementation using AndroidView to embed a surface
    // capable of rendering MJPEG streams via player or webview
    AndroidView(factory = { context -> 
        WebView(context).apply { loadUrl(url) } 
    })
}


Live Logs Overlay

@Composable
fun ObstacleOverlay(distance: Int) {
    if(distance < 20) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.Red)) {
            Text("OBSTACLE DETECTED: ${distance}cm", color = Color.White)
        }
    }
}


Mapping & Navigation (High level)

PAM's navigation stack includes:

Perception — Sensors + MJPG camera feed.

Live Logging — The backend streams obstacle data to the app, allowing the user to see exactly what the robot "sees" in real-time.

Follow Me (BLE) — Uses RSSI (Received Signal Strength Indicator) from the phone's Bluetooth to triangulate and follow the user.

Deployment

Docker: We recommend building the backend services into a Docker container for consistency across development and the Raspberry Pi.

Systemd (Raspberry Pi): create a systemd service to auto-start the robot controller on boot to ensure PAM is ready as soon as the Pi is powered on.

Firebase: Deploy the Firebase Cloud Functions for push notifications and ensure google-services.json is correctly configured in the Android app.

Testing & Troubleshooting

Common issues

BLE Disconnects: Ensure location permissions are granted for Bluetooth scanning on Android.

MJPG Latency: Adjust resolution on the Raspberry Pi camera script to reduce bandwidth.

Biometrics fail: Ensure the device has a lock screen security (PIN/Pattern) set up.

Future Improvements

Use ultrasonic sensors for object detection for increased mapping accuracy.

Enhance games with checkpoint racing around a scanned area.

Integration of advanced SLAM for complex room mapping.

Reference List

Admin, 2023. Google Lens Vs Pinterest Lens: The Rising War in the Visual Search Domain - August 2025. [online] Skyram Blog. Available at: https://www.skyramtechnologies.com/blog/google-lens-vs-pinterest-lens-visual-search-showdown/#:~:text=Google%20Lens's%20primary%20advantage%20is,visual%20search%20for%20many%20users. [Accessed 19 August 2025].

Android Developers, 2025. Card. [online] Android Developers. Available at: https://developer.android.com/develop/ui/compose/components/card [Accessed 7 October 2025].

Android Knowledge, 2024a. Navigation Component in Jetpack Compose using Kotlin | Android Studio. [online] YouTube. Available at: https://www.youtube.com/watch?v=a3Y2uncgAMM [Accessed 7 October 2025].

Android Knowledge, 2024b. Navigation Drawer + Bottom Navigation + Bottom Sheet in Jetpack Compose | Android Studio. [online] YouTube. Available at: https://www.youtube.com/watch?v=KkJb6rx0gC4 [Accessed 4 October 2025].

Android Makers, 2024. Building a Joystick Controller using Compose Multiplatform. [online] YouTube. Available at: https://www.youtube.com/watch?v=6xHeRprn_34&t=100s [Accessed 7 October 2025].

AntMan232, 2012. Raspberry Pi I2C (Python). [online] Instructables. Available at: https://www.instructables.com/Raspberry-Pi-I2C-Python/ [Accessed 7 October 2025].

Bukk, A., 2019. Ep.04 - Joystick and touch events | Android Studio 2D Game Development. [online] YouTube. Available at: https://www.youtube.com/watch?v=3oZ2jt0hQmo&t=25s [Accessed 7 October 2025].

Custer, C., 2025. How to Use an API in Python – Dataquest. [online] Dataquest. Available at: https://www.dataquest.io/blog/api-in-python/ [Accessed 23 September 2025].

Data Slayer, 2018. How to Setup Camera Module for Raspberry Pi 3 Model B+. [online] YouTube. Available at: https://www.youtube.com/watch?v=tHjwx2AQHxU [Accessed 7 October 2025].

Drummond, R., 2015. REST API on a Pi, Part 2: control your GPIO I/O ports over the internet. [online] Robert-Drummond. Available at: https://robert-drummond.com/2015/06/01/rest-api-on-a-pi-part-2-control-your-gpio-io-ports-over-the-internet/ [Accessed 7 October 2025].

Google Lens, 2025. Google Lens. [online] Google Lens. Available at: https://lens.google/ [Accessed 20 August 2025].

Lacker, P., 2025. Firebase Google Sign-In with Jetpack Compose & Clean Architecture - Android Studio Tutorial. [online] YouTube. Available at: https://www.youtube.com/watch?v=zCIfBbm06QM&t=28s [Accessed 6 October 2025].

Laimonas Naradauskas, 2024. Google Lens: Revolutionising Visual Search Experiences. [online] Smarter Digital Marketing. Available at: https://www.smarterdigitalmarketing.co.uk/google-lens/ [Accessed 19 August 2025].

Lima, L., 2015. Building a Rest API with the Bottle Framework. [online] Toptal Engineering Blog. Available at: https://www.toptal.com/python/building-a-rest-api-with-bottle-framework [Accessed 7 October 2025].

Milvus, 2025. What Is the Technology behind Google Lens? [online] Milvus.io. Available at: https://milvus.io/ai-quick-reference/what-is-the-technology-behind-google-lens [Accessed 19 August 2025].

Net, in, 2022. Stack Overflow. [online] Stack Overflow. Available at: https://stackoverflow.com/questions/73540601/writing-unit-test-cases-for-web-api-in-net [Accessed 6 October 2025].

philipplackner, 2023. GitHub - philipplackner/NestedNavigationGraphsGuide. [online] GitHub. Available at: https://github.com/philipplackner/NestedNavigationGraphsGuide [Accessed 4 October 2025].

Rick-Anderson, 2022. Unit Testing ASP.NET Web API 2. [online] Microsoft.com. Available at: https://learn.microsoft.com/en-us/aspnet/web-api/overview/testing-and-debugging/unit-testing-with-aspnet-web-api [Accessed 6 October 2025].

SK RADWOAN, 2025. Login Page - Walking Application. [online] Dribbble. Available at: https://dribbble.com/shots/26177865-Login-Page-Walking-Application [Accessed 20 August 2025].

Visual Paradigm, 2025. UML Class Diagram Tutorial. [online] Visual-paradigm.com. Available at: https://www.visual-paradigm.com/guide/uml-unified-modeling-language/uml-class-diagram-tutorial/ [Accessed 22 August 2025].

W L PROJECT, 2023. Change App Icon In Android Studio Jetpack Compose | Change The Launcher Logo In Android Studio. [online] YouTube. Available at: https://www.youtube.com/watch?v=HkNcLyiKM6U [Accessed 7 October 2025].

Waveshare, 2025. AlphaBot2-Pi - Waveshare Wiki. [online] Waveshare. Available at: https://www.waveshare.com/wiki/AlphaBot2-Pi [Accessed 7 October 2025].
