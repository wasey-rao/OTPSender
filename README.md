📱 OTP Forwarder — Jetpack Compose + Hilt + Retrofit

A secure and smart Android utility app that automatically reads incoming OTP messages and forwards them either via SMS to selected contacts or through a secure API to a server.
Built with Jetpack Compose, Hilt, and Retrofit, it emphasizes privacy, user control, and modern Android architecture.

🚀 Features

🔐 Automatic OTP Detection
Continuously monitors incoming SMS and detects OTP codes using regex matching.

👥 Contact Management
Add or remove trusted contacts to forward OTPs automatically.

🌐 Server Integration
Sends OTPs to your backend via a REST API using Retrofit.

📲 Smart Forwarding Control
Toggle forwarding to SMS or Server independently with easy Compose switches.

💬 Last OTP Log
Displays the most recent detected OTP and sender name (if available).

🎨 Modern UI
Built entirely with Jetpack Compose, following a clean dark green + charcoal gray theme.

🧠 Clean Architecture + Hilt DI
Repository + ViewModel layers for easy testing and maintenance.

🧩 Tech Stack
Layer	Library / Tool
UI	Jetpack Compose
DI	Hilt (Dagger)
Data Persistence	DataStore
Network	Retrofit + OkHttp
Background	BroadcastReceiver + ContentResolver
Language	Kotlin (Flows + Coroutines)
⚙️ Permissions Required

The following permissions are required for full functionality:

<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.INTERNET" />


⚠️ These must also be requested at runtime for Android 6.0 (API 23) and above.

🏗️ Architecture Overview

This project follows Clean Architecture and MVVM principles — separating responsibilities into well-defined layers for scalability, testability, and maintainability.

otsender/
├── data/
│   ├── network/
│   │   ├── dto/
│   │   │   ├── OtpPayload.kt              # Data model for OTP API payload
│   │   │   └── OTPApiService.kt           # Retrofit service interface
│   │
│   ├── repository/
│   │   └── OtpRepository.kt               # Core business logic for OTP handling and forwarding
│   │
│   ├── source/
│   │   └── dataStore.kt                   # DataStore for persisting user preferences (toggles)
│
├── di/
│   ├── DataStoreModule.kt                 # Hilt module providing DataStore dependencies
│   └── NetworkModule.kt                   # Hilt module for Retrofit and OkHttp setup
│
├── domain/
│   └── models/
│       └── SmsMessageData.kt              # Domain model for parsed SMS messages
│
├── presentation/
│   ├── MainActivity.kt                    # Entry point and Compose host
│   └── OtpViewModel.kt                    # ViewModel using Kotlin Flows to handle state and logic
│
├── ui/
│   └── theme/
│       ├── Color.kt                       # App color palette (dark green + charcoal gray)
│       ├── Theme.kt                       # Jetpack Compose Material3 theme setup
│       └── Type.kt                        # Typography definitions
│
├── OtpApplication.kt                      # Application class with Hilt initialization
└── OtpNotificationListener.kt             # Notification service for reading incoming OTP messages

🧪 How It Works

Incoming SMS Detected → ContentResolver queries the latest SMS.

OTP Extracted → Regex identifies numeric OTP codes (4–8 digits).

Contact Name Lookup → Matches phone number with stored contacts.

Forwarding Actions

If SMS Forwarding is enabled → OTP sent to selected contacts.

If Server Forwarding is enabled → OTP posted to the configured API endpoint.

Log Updated → The last OTP and sender appear on the app screen.

🧰 Setup Instructions

Clone the repository:

git clone https://github.com/yourusername/otp-forwarder.git


Open in Android Studio (Arctic Fox or newer).

Update the API base URL inside NetworkModule.kt:

private const val BASE_URL = "https://your-server-url.com/api/"


Grant required permissions manually (or implement a runtime permission flow).

Build and run on a physical device (SMS permissions don’t work on emulator).

📡 API Example

Expected endpoint:

POST /receive-otp
Content-Type: application/json
{
  "otp": "123456",
  "sender": "Bank XYZ",
  "timestamp": 1730000000000
}

🎨 UI Preview
Feature	Screenshot
Main Screen	🔄 Forward toggles, Contact list, and Last OTP log
Dark Theme	✅ Jetpack Compose material theme with dark green + charcoal
🔒 Privacy Notes

The app only reads OTP messages from the SMS inbox, not personal conversations.

Contacts are stored locally and never uploaded.

Server communication is done via secure HTTPS.

🧑‍💻 Author

Abdul Wasey Rao
Android Developer | Kotlin, Jetpack Compose, and Clean Architecture Enthusiast
