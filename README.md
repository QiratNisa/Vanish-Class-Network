# Vanish Class Network

## Project Overview
Vanish Class Network is an Android-based chat application developed for the Mobile Application Development course. It allows students to communicate through a shared class network. Messages automatically disappear 30 seconds after delivery.

## Features
- Connect to a shared WebSocket relay server.
- Send and receive messages between connected devices.
- Automatic 30-second message expiry.
- Message countdown timer.
- Reconnection when network connection is lost.
- Duplicate message filtering.
- Offline message queue support.
- Chat interface built with Jetpack Compose.

## Technologies Used
- Kotlin
- Android Studio
- Jetpack Compose
- OkHttp WebSocket
- JSON Protocol

## Relay Server
`wss://vanish-relay-sawaira.bonto.page/ws`

## How to Run
1. Clone or download this repository.
2. Open the project in Android Studio.
3. Allow Gradle synchronization to complete.
4. Connect an Android device or start an emulator.
5. Ensure that the device has an internet connection.
6. Run the application.
7. Enter your display name and connect to the class network.

## Message Expiry
Messages are designed to disappear 30 seconds after delivery. The application uses message expiry information to manage the countdown and remove expired messages.

## Limitations
- The application depends on the availability of the relay server and internet connection.
- Offline queued messages may be lost if the application process terminates before sending.
- Screenshots or external recording can preserve message content.
- The free relay hosting service may occasionally become unavailable.

## AI Assistance
AI assistance was used for code explanations, debugging guidance, and documentation drafting. The submitted code was reviewed and tested by the student.

## Project
Assignment 3 – Vanish Class Network  
Course: Mobile Application Development  
Semester: 5th Semester
