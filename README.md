# ConnectX

Modern Android social networking app built with **Kotlin**, **Jetpack Compose**, and **Firebase**.

**Package:** `com.connectx.social`  
**Firebase project:** `connectx-c9ea3`

## Features

| Feature | Status |
|---------|--------|
| Registration / Login (Firebase Auth) | Implemented |
| User profiles + editing | Implemented |
| Social feed (posts, likes, comments) | Implemented |
| Image posts (Firebase Storage) | Ready |
| Private messaging | Implemented (no E2E encryption) |
| Firebase Cloud Messaging | Service + channels + permission |
| Admin dashboard | Users, flagged content, security flags, update config |
| AI security assistant | Rule-based flagging |
| In-app update checker | Reads config/app from Firestore |
| Notification permissions | Android 13+ POST_NOTIFICATIONS |
| Modern dark-first Material 3 UI | Yes |
| Firestore + Storage security rules | Provided |
| GitHub Actions APK build | Workflow included |

## Setup (required external steps)

### 1. Firebase Console

1. Open Firebase Console → project **connectx-c9ea3**.
2. Add Android app with package **com.connectx.social** if needed.
3. Download real **google-services.json** and replace `app/google-services.json`.
4. Enable Authentication (Email/Password), Firestore, Storage, Cloud Messaging.
5. Deploy `firestore.rules` and `storage.rules`.
6. Create composite indexes when the app prompts (posts by authorId+createdAt, conversations by participants+lastMessageTime).

### 2. Make yourself admin

After registering, in Firestore set `users/{your-uid}.isAdmin = true`.

### 3. App updates

Bump versionCode in `app/build.gradle.kts`, host the APK, then set latest version + download URL in Admin → Updates.

### 4. Build

```bash
./gradlew assembleDebug
```

Or use GitHub Actions → Build ConnectX APK → download artifacts.

## License

Private project for ConnectX.
