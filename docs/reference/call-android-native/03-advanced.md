# 03 — Advanced (UI-included)

> Advanced configuration for the UI-included Call Kit: init parameters, intercept inbound, floating window, call banner, virtual background, custom nickname/avatar/ringtone/answer-background, RTC audio-video attributes, privatization.
> ⬅️ Back to [README](./README.md) · Base integration: [`01`](./01-integration-android.md).

**Relevance to this project (audio-only outbound to CSR):**
- 🔵 **Directly useful:** [Intercept inbound](#intercept-inbound-requests) (block CSR→user), [Init parameters](#initialization-parameters), [RTC audio/video attributes](#rtc-audiovideo-attributes) (audio profile), [Custom nickname/avatar](#custom-nickname--avatar), [Custom ringtone](#custom-ringtone).
- 🟢 **Implemented:** [Call banner](#call-banner) (enabled 2026-07-17 — required bumping call-ui `4.1.0` → `4.3.0`).
- ⚪ **Reference-only:** [Virtual background](#virtual-background) (video feature), [Floating window](#floating-window), [Custom answer background](#custom-answer-background) — keep for completeness.

> **No Android equivalent of iOS LiveCommunicationKit.** That is an Apple framework; Android handles the app-killed case with offline push (MixPush) + full-screen-intent notification. The Yunxin Android doc sidebar has no 接听系统电话 entry for this reason — it is not a missing doc. See [`../call-ios-native/05-system-call-lck.md`](../call-ios-native/05-system-call-lck.md).

---

## Intercept inbound requests

> 🔵 Key for this project: the requirement is **outbound only**. `DefaultIncomingCallEx` is what surfaces the built-in incoming-call page/notification. Override it to intercept — either to route to a custom page, or (for this project) to suppress the unwanted CSR→user incoming UI.

Pass `incomingCallEx` in `CallKitUIOptions`:

```java
CallKitUIOptions options = new CallKitUIOptions.Builder()
    .incomingCallEx(new DefaultIncomingCallEx(){
        // Triggered on incoming call. Return value = whether the call is "consumed":
        //   true  → this call is consumed, do NOT launch the page
        //   false → not consumed, the page needs launching
        @Override
        public boolean onIncomingCall(@NonNull InvitedInfo invitedInfo){
            if (!isValidParam(invitedInfo)) {
                return true;
            }
            MainActivity.this.startActivity(toCallIntent(invitedInfo));  // launch custom page
            generateNotificationAndNotify(invitedInfo);                  // notify
            return false;
        }
    })
    .build();
CallKitUI.init(getApplicationContext(), options);
```

> For this project, returning `true` (consume without launching a page) is the lever to block incoming CSR calls — pending decision Q4. Custom UI guide: <https://doc.yunxin.163.com/nertccallkit/guide/zYzNzI5NDI?platform=android>.

---

## Initialization parameters

Configure via `CallKitUIOptions.Builder` (after IM login, before/at Call Kit init). Common items:

| Config | Type | Version | Notes |
|---|---|---|---|
| `rtcAppKey` | string | all | **Required.** NERTC AppKey. |
| `currentUserRtcUId` | long | 1.5.7+ | Optional RTC `uid`; auto-generated if unset. Custom uid → can't use the built-in ticket. |
| `enableOrder` | boolean | 1.3.3+ | Send local not-connected tickets (default true). |
| `rtcSdkOption` | `NERtcOption` | all | NERTC init config (incl. logging / privatization). |
| `timeOutMillisecond` | long | all | Call/answer timeout, ms (default 30s). |
| `resumeBGInvitation` | boolean | all | When app in background at call time, auto-show callee page on foreground (default true). |
| `notificationConfigFetcher` | Function1<NEInviteInfo, CallKitNotificationConfig> | all | Incoming notification icon/channelId/title/content. |
| `userInfoHelper` | `UserInfoHelper` | all | Custom nickname/avatar (defaults to IM SDK info). |
| `incomingCallEx` | `IncomingCallEx` | all | Override callee-received behavior (see [Intercept inbound](#intercept-inbound-requests)). |
| `rtcCallExtension` | `CallExtension` | all | Modify NERTC behavior (resolution, profile). See [RTC attrs](#rtc-audiovideo-attributes). |
| `soundHelper` | `SoundHelper` | all | Ringtone on/off & custom sounds. |
| `initRtcMode` | int | 2.0.0+ | `GLOBAL` (default) / `IN_NEED` / `IN_NEED_DELAY_TO_ACCEPT`. |
| `audio2video` / `video2Audio` | boolean | all | Whether the peer must confirm audio↔video switch (both sides must match; default false → caller's config wins). |
| `p2pAudioActivity` / `p2pVideoActivity` | class | all | Custom point-to-point audio/video page. |
| `language` | `NECallUILanguage` | 2.4.0+ | `AUTO` (default) / `ZH_HANS` / `EN`. |

**Deprecated (reference only):** `pushConfigProvider` (dropped 2.0.0), `rtcInitScope` (→ `initRtcMode`, 2.0.0), `logRootPath` (2.0.0), `enableAutoJoinWhenCalled` (dropped 3.5.0).

Template:

```java
CallKitUIOptions options = new CallKitUIOptions.Builder()
    .rtcAppKey("NERTC SDK AppKey")
    .timeOutMillisecond(30 * 1000)   // ms
    .build();
CallKitUI.init(getApplicationContext(), options);
```

### NotificationConfigFetcher

When the app is backgrounded it may be unable to show the incoming page directly → show a notification instead; tapping it opens the callee page.

```java
new Function1<NEInviteInfo, CallKitNotificationConfig>() {
    @Override
    public CallKitNotificationConfig invoke(NEInviteInfo inviteInfo) {
        // Build from inviteInfo — must NOT return null
        return new CallKitNotificationConfig(
            R.mipmap.ic_launcher,   // icon
            "channel_id",           // notification channel ID
            "You have a new call",  // title
            "User " + inviteInfo.callerAccId + " is calling you"  // content
        );
    }
}
```

> `NEInviteInfo.callerAccId` is the **accId**, not a nickname. Map it locally, or have the caller pass a nickname via the call's extra field and read `NEInviteInfo.extraInfo` (string; extend as JSON).

---

## RTC audio/video attributes

> 🔵 For audio-only, set the audio profile to speech. `rtcCallExtension` is the component's abstraction over NERTC SDK — subclass `NERtcCallExtension` and override.

```java
CallKitUIOptions options = new CallKitUIOptions.Builder()
    .rtcCallExtension(new NERtcCallExtension(){
        @Override
        protected void configVideoConfigBeforeJoin(){
            NERtcVideoConfig videoConfig = new NERtcVideoConfig();
            videoConfig.frameRate = NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_15;
            // Use width/height (not profile); if both set, profile is ignored.
            videoConfig.width = 640;
            videoConfig.height = 360;
            NERtcEx.getInstance().setLocalVideoConfig(videoConfig);
        }
        @Override
        protected void configChannelProfileBeforeJoin(){
            NERtcEx.getInstance().setChannelProfile(NERtcConstants.RTCChannelProfile.STANDARD_VIDEOCALL);
        }
    })
    .build();
CallKitUI.init(getApplicationContext(), options);
```

> The no-UI path also exposes `configAudioProfileBeforeJoin()` — for voice, set `STANDARD` + `SPEECH` and use `COMMUNICATION` channel profile. See [`04-no-ui-scheme.md`](./04-no-ui-scheme.md#set-call-resolution--audio-profile).

---

## Custom nickname & avatar

> 🟢 **Implemented** (2026-07-20) in `CallService.userInfoHelper()`. Only for UI-included integration. Nickname/avatar default to IM SDK user info; override via `UserInfoHelper` at init.

> ⚠️ **Cold-cache là lý do call UI hiện accid.** SDK default chỉ đọc **cache NIM local**; máy callee chưa từng fetch profile người gọi ⇒ fallback accid. Impl của ta: `NimUserInfoCache.getUserName/getAvatar` trước, **miss → `getUserInfoFromRemote` (async)** rồi `notify.invoke(...)`. App đã đẩy nick/avatar vào NIM profile lúc connect (`ConnectStatusIMStore.updateMyUserInfo`) nên fetch remote lấy được. `notify` callback = async, `return true`.

```java
new UserInfoHelper() {
    // Notify the component of the nickname for accId. Return true → use notify.invoke(newName); false → default.
    @Override
    public boolean fetchNickname(@NonNull String accId, @NonNull Function1<? super String, Unit> notify) {
        return false;
    }
    // Notify avatar. notify(url, placeholderResId). Return true → use it; false → default.
    @Override
    public boolean fetchAvatar(@NonNull Context context, @NonNull String accId,
                               @NonNull Function2<? super String, ? super Integer, Unit> notify) {
        return false;
    }
}
```

---

## Custom ringtone

`SoundHelper` sets waiting/called ringtones. Put custom sounds in `res/raw`, set resource IDs in `soundResources`. Ringer types (`AVChatSoundPlayer.RingerTypeEnum`):

| Type | Meaning |
|---|---|
| `CONNECTING` | Caller call prompt |
| `NO_RESPONSE` | Callee timeout, no response |
| `PEER_BUSY` | Callee busy |
| `PEER_REJECT` | Callee rejected |
| `ring` | Callee ringing |

```java
CallKitUIOptions options = new CallKitUIOptions.Builder()
    .soundHelper(new SoundHelper(){
        @Nullable
        @Override
        public Integer soundResources(@NonNull AVChatSoundPlayer.RingerTypeEnum type) {
            return super.soundResources(type);  // return null to not play
        }
        // Optional: override play/stop for your own playback logic
        @Override public void play(@NonNull Context context, @NonNull AVChatSoundPlayer.RingerTypeEnum type) { super.play(context, type); }
        @Override public void stop(@NonNull Context context, @Nullable AVChatSoundPlayer.RingerTypeEnum type) { super.stop(context, type); }
    })
    .build();
CallKitUI.init(getApplicationContext(), options);
```

> Since 2.0.0 playback uses `SoundPool` (some file limits). Before 2.0.0, `SoundHelper#isEnable` (true=ring / false=mute) toggles ringing.

---

## Floating window

> ⚪ Reference. Lets the user minimize the call to a floating window. Enable via a custom activity that overrides `provideUIConfig`.

```java
CallKitUIOptions options = new CallKitUIOptions.Builder()
    .p2pVideoActivity(CustomP2PCallActivity.class).build();
CallKitUI.init(context, options);

public class CustomP2PCallActivity extends CommonCallActivity {
    @NonNull
    @Override
    protected P2PUIConfig provideUIConfig(CallParam param) {
        return new P2PUIConfig.Builder()
            .enableFloatingWindow(true)
            .enableAutoFloatingWindowWhenHome(true)
            .build();
    }
    @Override protected int provideLayoutId() { return 0; }  // custom layout
}
```

`CommonCallActivity` floating-window methods: `getUiConfig`, `provideUIConfig`, `showOverlayPermissionDialog(View.OnClickListener)`, `doShowFloatingWindow()`. Config field: `P2PUIConfig.enableFloatingWindow` (default false).

---

## Call banner

> 🟢 **Implemented** (2026-07-17). Since v4.3.0 — this is **why** `call-ui` was bumped `4.1.0` → `4.3.0`; `CallKitUI` in 4.1.0 has no `enableIncomingBanner` at all (verified by `javap` on the 4.1.0 aar). Off by default — we turn it on.

**Where:** `CallService.java` → `init()`, right after `CallKitUI.init(...)`, inside the existing `try` (a banner failure must not break IM init).

> ⚠️ **Banner only works while the app is alive**, and only with overlay permission.

- Runtime toggle (takes effect **next** call): `CallKitUI.enableIncomingBanner(true)` / `enableIncomingBanner(false)`. This is a runtime method, **not** a Builder option (the same-named Builder field is internal, not public API).
- Android banner uses the system floating window (`SYSTEM_ALERT_WINDOW`) → needs overlay permission on Android 6.0+. Without it, the SDK jumps to `ACTION_MANAGE_OVERLAY_PERMISSION` and downgrades to a notification.
- A second call during the banner → SDK auto-replies busy. Ringtone plays during banner and is not interrupted when tapping into the full-screen page.

### Overlay permission — how we do it

We **ask proactively** instead of letting the SDK jump to Settings mid-call (owner decision — the SDK's auto-jump is jarring right when a call arrives).

- Manifest: **nothing to add.** The `call-ui:4.3.0` aar already declares `SYSTEM_ALERT_WINDOW` (+ `POST_NOTIFICATIONS`, `USE_FULL_SCREEN_INTENT`), and the manifest merger pulls it into the app.
- Native: `RNNeteaseImModule.hasOverlayPermission` / `requestOverlayPermission`.
- JS: `NimCall.hasOverlayPermission()` / `requestOverlayPermission()` (no-op → `true` on iOS).
- Called from: `pyeon-chinese-mobile` → `AppProvider.tsx`, in a `useEffect` gated on `isAuthorized`. Asked **after login**, not at call time, because overlay permission is needed by the **callee** — anyone can be called at any moment.

> ⚠️ `react-native-permissions` **cannot** do this. `SYSTEM_ALERT_WINDOW` is a special appop, not a runtime permission, so `check(PERMISSIONS.ANDROID.*)` does not cover it and `CustomPermissionModal` cannot be reused.
>
> ⚠️ Android gives **no callback** when the user returns from the Settings screen. `requestOverlayPermission` is fire-and-forget; re-check with `hasOverlayPermission()` if you need the result.

```kotlin
if (!Settings.canDrawOverlays(context)) {
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${packageName}"))
    startActivity(intent)
}
```

---

## Virtual background

> ⚪ Reference (video-only; N/A for audio-only). Background blur during a video call. Enable `enableVirtualBlur(true)` in `provideUIConfig`, and additionally integrate `com.netease.yunxin:nertc-nenn` + `com.netease.yunxin:nertc-segment` libraries.

```java
public class CustomP2PCallActivity extends CommonCallActivity {
    @NonNull
    @Override
    protected P2PUIConfig provideUIConfig(CallParam param) {
        return new P2PUIConfig.Builder().enableVirtualBlur(true).build();
    }
    @Override protected int provideLayoutId() { return 0; }
}
```

---

## Custom answer background

> ⚪ Reference (since v4.8.0). Custom background image on the callee ringing/answering page. Local drawable or public `http/https` URL. Falls back to default avatar-blur on failure.

```kotlin
val source = NECallUIIncomingBackgroundSource.Builder()
    .resource(R.drawable.custom_answer_bg)   // or .url("https://example.com/bg.jpg")
    .build()
val config = NECallUIDynamicConfig.Builder().incomingCallBackground(source).build()
CallKitUI.setDynamicUIConfig(config)

// Reset to default:
CallKitUI.setDynamicUIConfig(null)
```

Only affects the ringing/answering page (not in-call). Applies to subsequent calls, not the currently-displayed one.

---

## Privatization

> ⚪ Reference (v1.4.2+). Point the component's NERTC at a privatized deployment. IM SDK privatization must be configured separately (contact Yunxin support). In privatization, the built-in ticket no longer works — implement the ticket yourself ([`02`](./02-call-list.md#method-2--implement-the-ticket-yourself)).

```java
NERtcOption option = new NERtcOption();
NERtcServerAddresses serverAddresses = new NERtcServerAddresses();
serverAddresses.channelServer = "...";   // privatized service URLs
option.serverAddresses = serverAddresses;

CallKitUIOptions options = new CallKitUIOptions.Builder()
    .rtcSdkOption(option)
    .build();
CallKitUI.init(getApplicationContext(), options);
```
