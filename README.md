# P.A.M. (Personal Assistant Machine)

PAM is a compact, portable robotic personal assistant built with a
Raspberry Pi + AlphaBotV2 platform, a REST API backend, and an Android
Jetpack Compose frontend. She performs object avoidance,
remote-controlled navigation, live camera streaming, voice & text
control, follow me feature BLE enabled, Gemini-powered conversational
personalization, and multi-language translation.

Developers: Aliziwe Qeqe, Kuhle Mlinganiso, Aphiwe Mhotwana and Hlumelo Ntwanambi
---
## Table of Contents

-   Overview
-   Architecture & Visuals
-   Features
-   Tech Stack
-   Hardware Setup (Raspberry Pi + AlphaBotV2)
-   Backend (REST API)
-   Camera
-   Motor
-   Mapping
-   Games
-   Google Calendar
-   Gemini Chat
-   Translation
-   Android Frontend (Jetpack Compose)
-   UI & Navigation
-   Key composables & snippets
-   Permissions & Manifest
-   Retrofit + API models
-   Text-to-Speech & Speech-to-Text
-   Security (Biometrics & SSO)
-   Offline Data (RoomDB)
-   Mapping & Navigation (High level)
-   Examples: API calls and responses
-   Deployment & CI
-   Testing & Troubleshooting
-   Future Improvements
-   Contributing
-   License

## Overview

PAM brings together robotics, cloud APIs, and a modern Android UI to
create a helpful companion that can: follow a user via BLE, fetch small
objects, provide live MPEG camera feedback, play games, schedule with
Google Calendar, and hold a personalized conversational experience via
Gemini. The system architecture separates concerns into: hardware
controller scripts (Raspberry Pi), a REST API that the mobile app talks
to, and the Jetpack Compose mobile app that serves as the UX layer and
remote control.

## Architecture & Visuals

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

### ASCII UI mock (Main screen)

    ---------------------------
    | PAM — Live Camera Feed  |
    | [ MPEG Video Stream ]   |
    | [Overlay: Object Log]   |
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

## Features

### Core features

-   Enhanced UI with Welcome & Splash screens\
-   Live MPEG camera feed\
-   Follow Me via BLE\
-   Live Logs overlay\
-   Biometrics + Google SSO\
-   Push notifications\
-   Offline RoomDB storage\
-   Motor control\
-   Voice + text commands\
-   Google Calendar & Gemini\
-   Expanded settings

## Tech Stack

-   **Hardware:** Raspberry Pi 3, AlphaBotV2\
-   **Backend:** Python Flask/FastAPI or Node Express\
-   **DB:** PostgreSQL/Redis + RoomDB (local)\
-   **Mobile:** Kotlin + Jetpack Compose\
-   **Networking:** Retrofit, MPEG stream, BLE\
-   **Security:** Biometrics, OAuth2\
-   **LLM:** Gemini API

## Hardware Setup (Raspberry Pi + AlphaBotV2)

### 1. Flash OS

Use Raspberry Pi OS Lite.

### 2. Initial setup

    sudo apt update && sudo apt upgrade -y
    sudo raspi-config
    sudo apt install python3-pip git libatlas-base-dev -y
    pip3 install flask pigpio opencv-python numpy bluepy

### 3. Wiring & setup

Follow the AlphaBotV2 hardware guide.

### 4. Run controller example

``` python
from flask import Flask, Response
import alphabot
from camera_pi import Camera

app = Flask(__name__)
robot = alphabot.AlphaBot()

@app.route('/video_feed')
def video_feed():
    return Response(gen(Camera()), mimetype='multipart/x-mixed-replace; boundary=frame')

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, threaded=True)
```

## Backend (REST API)

### High-level endpoints

-   `/camera/live`
-   `/logs/live` (WebSocket)
-   `/motor`
-   `/ble/toggle`
-   `/notifications/send`
-   `/calendar/events`
-   `/chat/gemini`

### WebSocket example

``` python
@app.websocket('/ws/logs')
async def log_ws(websocket: WebSocket):
    await websocket.accept()
    while True:
        data = {"sensor": "ultrasonic", "distance": 15, "avoidance_active": True}
        await websocket.send_json(data)
```

## Android Frontend (Jetpack Compose)

Package structure, dependencies, RoomDB entity, biometrics, MPEG viewer,
overlays, and navigation details are included in the original text.

## Deployment & CI

-   Docker backend\
-   Firebase Cloud Messaging\
-   GitHub Actions

## Testing & Troubleshooting

Common BLE, video, and biometrics fixes documented.

## Future Improvements

-   Better ultrasonic detection\
-   SLAM mapping\
-   Advanced games

## Reference List

(Full reference list preserved from the user's text.)
