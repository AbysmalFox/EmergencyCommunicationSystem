# Production Ready Guide: Emergency Communication System

Last updated: February 15, 2026

This guide tracks what is complete, what is still required, and the exact release checks before distributing APKs to other devices.

## 1. Build and Serialization Stability [COMPLETED]
Signed builds can break parsing if DTOs are obfuscated or annotation classes are missing at compile time.

Status:
- [x] Gson dependency explicitly added: `com.google.code.gson:gson:2.10.1` in `app/build.gradle.kts`.
- [x] Retrofit Gson converter present: `com.squareup.retrofit2:converter-gson:2.9.0`.
- [x] `@SerializedName` is used in network DTOs (including `FcmTokenRequest` in `SettingsApiService.kt`).
- [x] ProGuard rules keep API models, including `com.example.emergencycommunicationsystem.data.models.**`.
- [x] Release crash fixed: Gson `TypeToken<List<ForecastItem>>` in Room converter no longer depends on stripped generic signatures (`Converters.kt` now uses `TypeToken.getParameterized(...)`).
- [x] Release crash fixed: Retrofit/R8 reflection issues mitigated with additional keep rules in `app/proguard-rules.pro` (keeps signature + Kotlin metadata + Retrofit service interfaces).

## 2. Release Backend Behavior [COMPLETED]
Release builds must never depend on local/LAN backend routes.

Status:
- [x] Release is forced to production backend only (`ALLOW_LOCAL_FALLBACK=false` for release).
- [x] Local fallback is now opt-in for debug only via `local.properties` (`ALLOW_LOCAL_FALLBACK=true|false`).
- [x] Local device IP is configurable via `LOCAL_DEVICE_HOST` in `local.properties` (debug use only).

Important:
- Production/distributed APKs should always use the public HTTPS backend.
- Do not rely on `192.168.x.x`, `10.0.2.2`, `localhost`, or USB/Wi-Fi proximity for release behavior.

## 3. Google Sign-In Status [WORKING - RELEASE VERIFIED]
Google Sign-In is sensitive to:
- Android signing certificate SHA-1/SHA-256 registration (Firebase/Google Cloud)
- Correct `applicationId` (package) match
- Backend ID token audience (`aud`) allowlist

Status:
- [x] Android OAuth cert mismatch (`DEVELOPER_ERROR`) resolved by using a dedicated debug keystore and updating `app/google-services.json` accordingly.
- [x] Backend token verification updated to accept current token `aud` and `azp` claims (server-side fix).
- [x] App now requests ID tokens using `GOOGLE_WEB_CLIENT_ID` from `local.properties` to align with backend audience.
- [x] Release SHA-1 + SHA-256 registered in Firebase for `com.lgu.emergencycommunicationsystem`.
- [x] `app/google-services.json` updated to include both Android OAuth clients (debug + release certificate hashes).
- [x] Backend authorized party allowlist updated to include both Android clients (debug + release) so release tokens don't fail with "authorized party is not allowed".

Release note:
- If Google Sign-In breaks again on release, re-check that Firebase still has the correct release SHA-1/SHA-256 for the current keystore used by `releaseCustom` signing.

## 4. Release Signing (APK) [WORKING]
An APK can be produced, but Play Store distribution requires a dedicated release keystore and consistent signatures.

Status:
- [x] Release signing config added in Gradle and driven by `local.properties` (`RELEASE_*` values).
- [x] Release signing is enforced: `assembleRelease` fails if `RELEASE_*` values are missing.
- [x] `:app:assembleRelease` produces `app/build/outputs/apk/release/app-release.apk`.

Required steps:
1. Create a dedicated release keystore (do not reuse debug keystores).
2. Add to `local.properties`:
   - `RELEASE_STORE_FILE`
   - `RELEASE_STORE_PASSWORD`
   - `RELEASE_KEY_ALIAS`
   - `RELEASE_KEY_PASSWORD`
3. Build:
   - `.\gradlew.bat clean :app:assembleRelease`
4. Output:
   - `app/build/outputs/apk/release/app-release.apk`

Windows caveat:
- If `clean`/R8 fails because a file is locked, stop Gradle (`.\gradlew.bat --stop`), close Android Studio, and delete `app/build` before rebuilding.

## 5. Firebase Release Configuration (FCM + Sign-In) [COMPLETED]
Google Sign-In and FCM can fail on release APKs if the release signing fingerprints are not registered.

Status:
- [x] Release SHA-1 and SHA-256 are registered in Firebase Console.
- [x] `app/google-services.json` re-downloaded and replaced after fingerprint registration.

## 6. Backend Requirements for Alerts [WORKING - WITH LIMITATIONS]
The app can receive tokens and subscribe to topic(s), but backend must actively send push notifications.

Status:
- [x] `fcm_token` storage and token update flow implemented (`update_fcm_token.php`).
- [x] Client side FCM service and topic subscription implemented.
- [x] `active_poll.php` endpoint added server-side (currently returns `poll: null` safely when polls are not implemented).
- [ ] Admin alert creation endpoint must trigger FCM HTTP v1 send (topic or per-user token).
- [ ] Backend should handle invalid/expired tokens and clean them up.

## 7. Security Requirements Before Public Release [REQUIRED]

Status:
- [x] `local.properties` is git-ignored.
- [x] `.gitignore` corruption fixed.
- [ ] Rotate any exposed keys/secrets immediately.
- [ ] Remove `GOOGLE_WEB_CLIENT_SECRET` from Android app build usage (keep this secret server-side only).
- [ ] Keep release API keys minimal and scoped by package/signature restrictions where possible.
- [ ] Ensure debug-only logging never ships in release (avoid logging tokens / request bodies).

## 8. Release Validation Checklist (Fresh Install)
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

## 9. Quick Status Summary

Completed:
- [x] Serialization/proguard hardening.
- [x] FCM client integration + token sync path.
- [x] Strict production-only backend mode for release builds.
- [x] Debug-only local fallback toggle.
- [x] Google Sign-In works end-to-end in release (including backend token verification and release SHA registration).
- [x] Release APK builds and runs without the previous R8/TypeToken/Retrofit crashes.

Still required for full production readiness:
- [ ] Backend FCM trigger on alert creation (push notification sending).
- [ ] Rotate any exposed secrets/credentials and remove any server secrets from the Android app.
- [ ] Decide whether to implement real "active poll" database logic (currently returns `poll: null`).
