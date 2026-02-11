# Production Readiness Guide: Emergency Communication System

This guide outlines the critical steps required to move from a development prototype to a production-ready application, specifically focusing on fixing the "no alerts" issue in signed release builds.

## 1. Fix: Release Build Obfuscation (R8/ProGuard)
In signed builds, Android's R8 compiler renames classes and fields to save space. This breaks JSON parsing (Retrofit/Gson) if the field names don't match your PHP backend.

**What was done:**
- Updated `app/proguard-rules.pro` to "keep" the `data` and `network` packages.
- Added `@SerializedName` annotations to DTOs in `MessagingApiService.kt`.

**What you should do:**
- Ensure every field in your data classes (like `Alert`, `User`, `Message`) has a `@SerializedName("key_name")` that matches your PHP database keys exactly.

---

## 2. Critical Missing Feature: Push Notifications (FCM)
Currently, the app only "pulls" data when opened. In an emergency, the backend must "push" notifications to the phone.

### Step A: Add Dependency
Add this to `app/build.gradle.kts` dependencies:
```kotlin
implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
implementation("com.google.firebase:firebase-messaging-ktx")
```

### Step B: Create the Messaging Service
Create `com.example.emergencycommunicationsystem.services.MyFirebaseMessagingService`:

```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"]
        val category = remoteMessage.data["category"]

        val channelId = NotificationChannels.getChannelIdForCategory(category)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_tabler_bell_ringing)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        // Send this token to your PHP backend (e.g., update_token.php)
        // so the server knows which device belongs to which user.
    }
}
```

### Step C: Register in AndroidManifest.xml
```xml
<service
    android:name=".services.MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

---

## 3. Firebase Console Configuration (Crucial for Signed APK)
Google Services (Login, FCM) will **fail** in a signed APK if the SHA-1 fingerprint of your release keystore isn't registered.

1.  Open your terminal in Android Studio.
2.  Run the signing report: `./gradlew signingReport`
3.  Look for the `release` variant (not debug).
4.  Copy the **SHA-1** and **SHA-256** fingerprints.
5.  Go to **Firebase Console > Project Settings > General**.
6.  Under "Your Apps" > "Android app", click **Add Fingerprint** and paste them.
7.  Download the new `google-services.json` and replace the one in your `app/` folder.

---

## 4. Backend (PHP) Requirements
Your Hostinger backend must be modified to trigger the "Push" whenever an alert is added:

1.  **Store FCM Tokens:** Create a column in your `users` table for `fcm_token`.
2.  **Notification Trigger:** In your `create_alert.php` (or similar), use a CURL request to the Firebase Cloud Messaging API to send the notification to the `emergency-room` topic or individual user tokens.

---

## 5. Summary Checklist
- [x] ProGuard rules updated (Completed by Gemini)
- [x] Network DTOs annotated (Completed by Gemini)
- [ ] Add Firebase Messaging dependency
- [ ] Implement `FirebaseMessagingService`
- [ ] Register Release SHA-1 in Firebase Console
- [ ] Update PHP backend to send FCM triggers
