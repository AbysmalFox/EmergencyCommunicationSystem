# Production Readiness Guide: Emergency Communication System

This guide outlines the critical steps required to move from a development prototype to a production-ready application, specifically focusing on fixing the "no alerts" issue in signed release builds.

## 1. Fix: Release Build Obfuscation (R8/ProGuard) [COMPLETED ✅]
In signed builds, Android's R8 compiler renames classes and fields to save space. This breaks JSON parsing (Retrofit/Gson) if the field names don't match your PHP backend.

**Status:**
- [x] Updated `app/proguard-rules.pro` to "keep" the `data` and `network` packages.
- [x] Added `@SerializedName` annotations to DTOs in `MessagingApiService.kt`, `AuthModels.kt`, `LocationModels.kt`, and `SubscriptionModels.kt`.

---

## 2. Push Notifications (FCM) Implementation [CODE COMPLETED ✅]
The app now supports a "push" model where the backend can actively send notifications to the phone.

**Status:**
- [x] Added Firebase Messaging dependencies to `app/build.gradle.kts`.
- [x] Created `MyFirebaseMessagingService.kt` to handle incoming alerts and display them via the correct Notification Channels (Fire, Weather, etc.).
- [x] Registered the service in `AndroidManifest.xml`.
- [x] **Automatic Token Sync:** Updated `AuthManager.kt` and `MyFirebaseMessagingService.kt` to automatically send the device's FCM token to the server upon login or token refresh.

---

## 3. Firebase Console Configuration [MANUAL STEP REQUIRED ⚠️]
Google Services (Login, FCM) will **fail** in a signed APK if the SHA-1 fingerprint of your release keystore isn't registered.

1.  Open your terminal in Android Studio.
2.  Run the signing report: `./gradlew signingReport`
3.  Look for the `release` variant (not debug).
4.  Copy the **SHA-1** and **SHA-256** fingerprints.
5.  Go to **Firebase Console > Project Settings > General**.
6.  Under "Your Apps" > "Android app", click **Add Fingerprint** and paste them.
7.  Download the new `google-services.json` and replace the one in your `app/` folder.

---

## 4. Backend (PHP) Requirements [IN PROGRESS 🔄]
The backend must store device tokens and trigger the "Push" signal.

**Status:**
- [x] **Store FCM Tokens:** `fcm_token` column added to the `users` table.
- [x] **Token Update Script:** `update_fcm_token.php` created on Hostinger.
- [x] **Topic Subscription:** App now automatically subscribes to the `emergency-room` topic in `MainActivity.kt`.
- [ ] **Notification Trigger:** In your `create_alert.php` (or similar), add the logic to send a request to the Firebase Cloud Messaging API when a new alert is posted.

### Recommended PHP Logic for sending Push:
Use the FCM v1 API to send to the `emergency-room` topic:
```php
// Example Payload
$message = [
    "message" => [
        "topic" => "emergency-room",
        "data" => [
            "title" => "FIRE ALERT",
            "body" => "Fire reported in Barangay 123",
            "category" => "fire"
        ]
    ]
];
```

---

## 5. Summary Checklist
- [x] ProGuard rules updated (Gemini)
- [x] Network DTOs annotated (Gemini)
- [x] Add Firebase Messaging dependency (Gemini)
- [x] Implement `FirebaseMessagingService` (Gemini)
- [x] Automatic FCM Token upload logic (Gemini)
- [x] Global Topic Subscription (Gemini)
- [x] Backend `fcm_token` column and `update_fcm_token.php` (User)
- [ ] Register Release SHA-1 in Firebase Console (User)
- [ ] Update PHP backend to send FCM triggers (User)
