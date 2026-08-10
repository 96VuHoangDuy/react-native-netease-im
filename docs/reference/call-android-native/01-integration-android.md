# 01 — Call Kit Integration (Android, with UI)

> How to integrate the **NERTC Call Kit** (UI-included) and place a 1-to-1 call on Android.
> This is the recommended path (prebuilt call UI). For the raw-engine / no-UI path see [`04-no-ui-scheme.md`](./04-no-ui-scheme.md).
> ⬅️ Back to [README](./README.md) · Prerequisites (console app + services): [README §7](./README.md#7-console-prerequisites-one-time-before-any-code).

> **Project note:** this feature is **audio-only outbound to a CSR**. Wherever the doc uses `NECallType.VIDEO`, use `NECallType.AUDIO`, and skip the camera permission. See [README §5](./README.md#5-project-specific-constraints-differ-from-generic-yunxin-doc).

---

## 1. Development environment

| Requirement | Value |
|---|---|
| Android Studio | Latest recommended |
| Android API level | 21+ (Android 5.0+) |
| Android SDK | 31+ (Platform-Tools 31.x+) |
| Gradle | 7.4.1 (AGP 7.1.3) — align with the project's actual toolchain |
| Kotlin | 1.6.21+ |
| CPU arch | ARM64, ARMv7 |
| Other | AndroidX only (no support library). **Real device required** (simulator lacks mic/camera). |

The call component is built on **NIM SDK (V10)** + **NERTC SDK**; both are bundled inside the Call Kit, so you integrate only the Call Kit unless you pin your own SDK versions.

---

## 2. Gradle dependencies

**Root `build.gradle`** — add Maven Central:

```gradle
allprojects {
    repositories {
        mavenCentral()
    }
}
```

**App `build.gradle`** — supported SO architectures:

```gradle
android {
    defaultConfig {
        ndk {
            abiFilters "armeabi-v7a", "x86", "arm64-v8a", "x86_64"
        }
    }
}
```

**Introduce the call component.** Two options:

*Option A — do not pin the SDK version* (component picks a compatible NIM/NERTC automatically):

```gradle
implementation 'com.netease.yunxin.kit.call:call-ui:3.3.0'   // use the project's actual version
```

*Option B — the project already integrates NIM/NERTC and needs to pin versions* (exclude the component's bundled SDKs, then declare your own):

```gradle
implementation('com.netease.yunxin.kit.call:call-ui:3.3.0') {
    exclude group: 'com.netease.nimlib'
    exclude group: 'com.netease.yunxin', module: 'nertc-base-sdk'
}

// Your project's actual SDK versions:
implementation 'com.netease.nimlib:basesdk:10.6.0'  // IM base (example version)
// implementation "com.netease.nimlib:chatroom:10.6.0" // if chatroom used
implementation 'com.netease.yunxin:nertc:5.6.50'    // RTC (example version)
```

> ⚠️ **This project uses Option B** — NIM is already integrated via `react-native-netease-im`. Pin to the Phase-2 target versions (Android NIM `10.9.52`), and keep **all NIM sub-packages on the same version** (base and chatroom must match or it errors). Version ↔ version mapping: [Yunxin changelog](https://doc.yunxin.163.com/nertccallkit/concept/DMzOTI3NTA?platform=client).

If you hit `More than one file was found with OS independent path 'lib/arm64-v8a/libc++_shared.so'`:

```gradle
android {
    packagingOptions {
        pickFirst 'lib/arm64-v8a/libc++_shared.so'
        pickFirst 'lib/armeabi-v7a/libc++_shared.so'
    }
}
```

---

## 3. AndroidManifest — permissions & NIM services

Add to `AndroidManifest.xml`. Replace `com.netease.nim.demo` / `${applicationId}` with the real package.

> **Audio-only:** the `CAMERA` permission below is only needed for video calls. For this feature, **omit `CAMERA`** and keep `RECORD_AUDIO`.

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="com.netease.nim.demo">

    <!-- Network -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>

    <!-- Storage -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

    <!-- Media -->
    <uses-permission android:name="android.permission.CAMERA"/>       <!-- omit for audio-only -->
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <!-- Android11: not needed on V8.6.1+. Others: not needed on V4.4.0+ -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/>

    <!-- New-message reminders -->
    <uses-permission android:name="android.permission.FLASHLIGHT" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <!-- Android 8.0+ -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

    <permission
        android:name="${applicationId}.permission.RECEIVE_MSG"
        android:protectionLevel="signature"/>
    <uses-permission android:name="${applicationId}.permission.RECEIVE_MSG"/>

    <application ...>
        <!-- App Key (or provide via SDKOptions; SDKOptions wins if both set) -->
        <meta-data android:name="com.netease.nim.appKey" android:value="key_of_your_app" />

        <!-- NIM background service (separate process) -->
        <service android:name="com.netease.nimlib.service.NimService" android:process=":core"/>
        <!-- Required to use the V10 interface -->
        <service android:name="com.netease.nimlib.service.NimServiceV2" />

        <service
            android:name="com.netease.nimlib.job.NIMJobService"
            android:exported="false"
            android:permission="android.permission.BIND_JOB_SERVICE"
            android:process=":core"/>

        <receiver android:name="com.netease.nimlib.service.NimReceiver"
            android:process=":core" android:exported="false">
            <intent-filter>
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
            </intent-filter>
        </receiver>

        <receiver android:name="com.netease.nimlib.service.ResponseReceiver"/>
        <service  android:name="com.netease.nimlib.service.ResponseService"/>

        <provider
            android:name="com.netease.nimlib.ipc.NIMContentProvider"
            android:authorities="${applicationId}.ipc.provider"
            android:exported="false" android:process=":core" />
        <!-- SDK force-checks this declaration at startup; wrong config → crash -->
        <provider
            android:name="com.netease.nimlib.ipc.cp.provider.PreferenceContentProvider"
            android:authorities="${applicationId}.ipc.provider.preference"
            android:exported="false" />
    </application>
</manifest>
```

---

## 4. ProGuard

Add to `proguard-rules.pro`:

```proguard
# NIM SDK (skip if already added for IM)
-dontwarn com.netease.nim.**
-keep class com.netease.nim.** {*;}
-dontwarn com.netease.nimlib.**
-keep class com.netease.nimlib.** {*;}
-dontwarn com.netease.share.**
-keep class com.netease.share.** {*;}
-dontwarn com.netease.mobsec.**
-keep class com.netease.mobsec.** {*;}

# NERTC SDK
-keep class com.netease.lava.** {*;}
-keep class com.netease.yunxin.** {*;}

# Call component
-dontwarn com.netease.yunxin.kit.**
-keep class com.netease.yunxin.kit.** {*;}
-keep public class * extends com.netease.yunxin.kit.corekit.XKitInitOptions
-keep class * implements com.netease.yunxin.kit.corekit.XKitService {*;}
```

---

## 5. Initialize the component

Two steps: init NIM (V10), then init the Call Kit UI. Put init where the user logs in; release on logout. **Avoid** releasing inside `MainActivity#onDestroy()`. Re-calling init destroys the previous init.

```java
// 1) IM init (V10)
// sdkOptions.disableV2Login = true; // default false → use V10 login
NIMClient.initV2(context, sdkOptions);

// 2) Call Kit UI init
CallKitUIOptions options = new CallKitUIOptions.Builder()
    // Required: NERTC AppKey (used during the call)
    .rtcAppKey(appKey)
    // Answer timeout, ms (default 30s)
    .timeOutMillisecond(30 * 1000L)
    // Incoming-call notification config (icon, text) when app is backgrounded
    .notificationConfigFetcher(neInviteInfo -> new CallKitNotificationConfig(R.drawable.ic_logo))
    // If app is in background when called, auto-resume the callee page on foreground (default true)
    .resumeBGInvitation(true)
    // NERTC init options
    .rtcSdkOption(new NERtcOption())
    // RTC init scope: GLOBAL = init once (faster first-frame); IN_NEED = init/destroy per call
    // (use IN_NEED / IN_NEED_DELAY_TO_ACCEPT if there's an RTC init conflict with other components)
    .initRtcMode(NECallInitRtcMode.IN_NEED)
    .build();
CallKitUI.init(getApplicationContext(), options);   // do not double-init
```

Full option table: [`03-advanced.md` → Init parameters](./03-advanced.md#initialization-parameters).

---

## 6. Log in to IM

Static-token login example (dynamic-token & auto-login: see Yunxin IM docs):

```java
NIMClient.getService(V2NIMLoginService.class).login("account", "token", null,
    new V2NIMSuccessCallback<Void>() {
        @Override public void onSuccess(Void unused) { /* TODO */ }
    },
    new V2NIMFailureCallback() {
        @Override public void onFailure(V2NIMError error) {
            int code = error.getCode();
            String desc = error.getDesc();
            // TODO
        }
    });
```

---

## 7. Place a 1v1 (audio) call

With the UI kit, a call is a few lines. Business flow: both sides logged into IM + Call Kit initialized → caller has both accounts → caller calls `startSingleCall`.

```java
// For this project: resolve the CSR accid from service/idle_tc_csr first (README §4),
// then use it as calledAccId. Use NECallType.AUDIO for audio-only.
CallParam param = new CallParam.Builder()
    .callType(NECallType.AUDIO)     // AUDIO (not VIDEO) for voice-only CSR call
    .calledAccId(csrAccid)          // accid returned by idle_tc_csr
    .build();
CallKitUI.startSingleCall(getActivity(), param);
```

- Callee taps answer on the called page → call connects.
- Either side taps hang up to end.

---

## 8. Next

- Custom UI, ringtone, banner, floating window, intercept inbound → [`03-advanced.md`](./03-advanced.md).
- Call ticket (话单) after a call → [`02-call-list.md`](./02-call-list.md).
- Raw engine (no UI) if the prebuilt UI can't match Figma → [`04-no-ui-scheme.md`](./04-no-ui-scheme.md).
- Official custom-UI guide: <https://doc.yunxin.163.com/nertccallkit/guide/zYzNzI5NDI?platform=android>
