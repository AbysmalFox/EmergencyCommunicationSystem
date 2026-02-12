# Production Ready Guide: Emergency Communication System

Last updated: February 12, 2026

This guide tracks what is complete, what is still required, and the exact release checks before distributing APKs to other devices.

## 1. Build and Serialization Stability [COMPLETED]
Signed builds can break parsing if DTOs are obfuscated or annotation classes are missing at compile time.

Status:
- [x] Gson dependency explicitly added: `com.google.code.gson:gson:2.10.1` in `app/build.gradle.kts`.
- [x] Retrofit Gson converter present: `com.squareup.retrofit2:converter-gson:2.9.0`.
- [x] `@SerializedName` is used in network DTOs (including `FcmTokenRequest` in `SettingsApiService.kt`).
- [x] ProGuard rules keep API models, including `com.example.emergencycommunicationsystem.data.models.**`.

## 2. Release Backend Behavior [COMPLETED]
Release builds must never depend on local/LAN backend routes.

Status:
- [x] Release is forced to production backend only (`ALLOW_LOCAL_FALLBACK=false` for release).
- [x] Local fallback is now opt-in for debug only via `local.properties` (`ALLOW_LOCAL_FALLBACK=true|false`).
- [x] Local device IP is configurable via `LOCAL_DEVICE_HOST` in `local.properties` (debug use only).

Important:
- Production/distributed APKs should always use the public HTTPS backend.
- Do not rely on `192.168.x.x`, `10.0.2.2`, `localhost`, or USB/Wi-Fi proximity for release behavior.

## 3. Firebase Release Configuration [MANUAL - REQUIRED]
Google Sign-In and FCM can fail on release APKs if release signing fingerprints are not registered.

Required steps:
1. Run: `./gradlew signingReport`
2. Copy SHA-1 and SHA-256 for the `release` variant.
3. Add both in Firebase Console:
Project Settings -> Your Apps -> Android -> Add Fingerprint.
4. Download updated `google-services.json`.
5. Replace `app/google-services.json` and rebuild release.

## 4. Backend Requirements for Alerts [IN PROGRESS]
The app can receive tokens and subscribe to topic(s), but backend must actively send push notifications.

Status:
- [x] `fcm_token` storage and token update flow implemented (`update_fcm_token.php`).
- [x] Client side FCM service and topic subscription implemented.
- [ ] Admin alert creation endpoint must trigger FCM HTTP v1 send (topic or per-user token).
- [ ] Backend should handle invalid/expired tokens and clean them up.

## 5. Security Requirements Before Public Release [REQUIRED]

Status:
- [x] `local.properties` is git-ignored.
- [x] `.gitignore` corruption fixed.
- [ ] Rotate any exposed keys/secrets immediately.
- [ ] Remove `GOOGLE_WEB_CLIENT_SECRET` from Android app build usage (keep this secret server-side only).
- [ ] Keep release API keys minimal and scoped by package/signature restrictions where possible.

## 6. Release Validation Checklist (Fresh Install)
Run this checklist before publishing or sharing APK publicly.

1. Build signed release APK/AAB.
2. Install on a clean device (uninstall old app first).
3. Test on a different network (mobile data or another Wi-Fi).
4. Verify:
   - Login works.
   - Alerts load from backend.
   - Push notifications arrive.
   - FCM token updates successfully.
5. Confirm logs show production base URL usage in release.
6. Confirm no local fallback behavior in release.

## 7. Quick Status Summary

Completed:
- [x] Serialization/proguard hardening.
- [x] FCM client integration + token sync path.
- [x] Strict production-only backend mode for release builds.
- [x] Debug-only local fallback toggle.

Still required for full production readiness:
- [ ] Firebase release SHA registration and `google-services.json` refresh.
- [ ] Backend FCM trigger on alert creation.
- [ ] Key rotation and removal of client-side web client secret usage.
