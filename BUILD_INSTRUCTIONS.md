# 📦 Build Instructions: Generating the Updated APK

Follow these instructions to generate the latest version of the **AlertaraQc EMC** application. This ensures all recent changes to the messaging system, backend synchronization, and UI refinements are included in the package.

---

## 🛠 Step 1: Clean and Sync Project
Before building, it is mandatory to clean the project to ensure no old cached data or resources are included.

1.  Open your project in **Android Studio**.
2.  Go to the top menu: **Build > Clean Project**.
3.  Wait for the process to finish.
4.  Go to **File > Sync Project with Gradle Files** to ensure all dependencies are ready.

---

## 🚀 Step 2: Generate Debug APK (For Testing)
Use this method to get the `.apk` file for your phone.

### Option A: Via the Top Menu
1.  Go to the **Build** menu at the top.
2.  Look for **Build APK(s)**. 
    *   *If you don't see it directly, it might be under "Build Bundle(s) / APK(s)".*
3.  Click it and wait for the notification at the bottom right.
4.  Click **"locate"** to find the `app-debug.apk`.

### Option B: Via Terminal (Guaranteed to work)
If you cannot find the menu option, you can use the built-in terminal at the bottom of Android Studio:
1.  Open the **Terminal** tab at the bottom.
2.  Type this command and press Enter:
    ```bash
    ./gradlew assembleDebug
    ```
3.  Once it says "BUILD SUCCESSFUL", the APKs will be located at:
    `app\build\outputs\apk\debug\`

**NOTE:** Because of the new optimizations, you will see multiple APK files (e.g., `app-arm64-v8a-debug.apk`, `app-armeabi-v7a-debug.apk`). 
*   Use **`app-arm64-v8a-debug.apk`** for most modern Android phones.
*   Use **`app-universal-debug.apk`** if you want one file that works on all phones (though it will be larger).

---

## 🔐 Step 3: Generate Signed Release APK (For Production)
Use this method if you need a finalized, optimized version of the app.

1.  Go to **Build > Generate Signed Bundle / APK...**.
2.  Select **APK** and click **Next**.
3.  **Key Store Path:**
    *   If you have an existing `.jks` key, select it.
    *   If not, click **Create new...** and follow the prompts to create a signing key.
4.  Enter your passwords and alias, then click **Next**.
5.  **Build Variant:** Select **release**.
6.  **V1/V2 Signature:** Ensure both (or at least V2 Full APK Signature) are checked.
7.  Click **Finish**.
8.  The signed APK will be located in:
    *   *Default Path:* `appelease\app-release.apk`

---

## 📲 Step 4: Installation
1.  Transfer the `.apk` file to your Android device.
2.  Open the file on your phone.
3.  If prompted, allow **"Install from unknown sources"**.
4.  Open the app, **Clear Cache** (if necessary), and log in to see the updated **Kim E. Sis** profile and messaging features.

## 🌐 Step 5: Publishing to your Website
Follow these steps to make the APK available for your users to download.

1.  **Upload via Hostinger File Manager:**
    *   Log in to your Hostinger account.
    *   Go to **File Manager** -> **public_html**.
    *   (Recommended) Create a folder named `/downloads`.
    *   Upload the APK file there.
    *   Rename it to `AlertaraQc_EMC.apk`.

2.  **The Download Link:**
    *   Your public link will be: `https://emergency-comm.alertaraqc.com/downloads/AlertaraQc_EMC.apk`

3.  **HTML Snippet for Website:**
    ```html
    <a href="https://emergency-comm.alertaraqc.com/downloads/AlertaraQc_EMC.apk" 
       class="download-btn" download>
       📥 Download AlertaraQc EMC App
    </a>
    ```
*   **Old Name Showing:** If the app still shows "User 7", please **Uninstall** the old version from your phone before installing the new APK.
*   **Build Errors:** If the build fails, run **Build > Rebuild Project** to force a fresh compilation of all Kotlin classes.
