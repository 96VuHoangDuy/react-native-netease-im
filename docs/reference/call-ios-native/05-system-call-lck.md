# 05 — System call answering (LiveCommunicationKit) — iOS only

> 🟢 **Implemented** (2026-07-17). Answer a NERTC call from the **system** call UI (Dynamic Island / top card) via **PushKit VoIP + LiveCommunicationKit**, even when the app is backgrounded or killed.
> ⬅️ Back to [README](./README.md) · Base integration: [`01`](./01-integration-ios.md) · Banner: [`03`](./03-advanced.md#call-banner)
> Raw official doc kept verbatim at [`05-system-call-lck.official-raw.md`](./05-system-call-lck.official-raw.md).

---

## 1. Banner vs LiveCommunicationKit — different problems

This is the single most common confusion. They are **not** alternatives; they complement each other.

| | Call banner ([`03`](./03-advanced.md#call-banner)) | LiveCommunicationKit (this file) |
|---|---|---|
| When | App **alive / foreground** | App **backgrounded or killed** |
| What | Light banner at top of screen, replaces the full-screen call page, does not interrupt what the user is doing | System-level call UI (Dynamic Island, or a top card on non-DI devices) |
| Platform | iOS + Android | **iOS only** |
| Needs | Android: overlay permission | VoIP cert, iOS **17.4+**, `voip` background mode |
| Transport | SDK signalling over the live IM connection | APNs **VoIP push** (PushKit) |

**Why Android has no counterpart:** LiveCommunicationKit is an Apple framework. The Yunxin Android doc sidebar has no 接听系统电话 entry — that is correct, not a doc gap. Android covers the app-killed case with offline push (MixPush, already configured in `IMApplication.java`) + full-screen-intent notification.

---

## 2. Requirements

- **iOS 17.4+.** Below that, LiveCommunicationKit does not exist.
- **Xcode 15.3+**.
- **VoIP push certificate** uploaded to the Yunxin console (a *VoIP Services* certificate — **not** the ordinary APNs push cert).
- Real device. VoIP push does not work on Simulator.
- **Caller must set `pushConfig` on the invite** (`NEUICallParam.pushConfig`, `needPush=YES`). Without it the server sends **no** push → PushKit never fires → this whole file is inert. Done in `RNNeteaseIm.m startVoiceCall` (2026-07-20). See `CALLKIT_INTEGRATION_NOTES.md#15`.

> ⚠️ **Registering PushKit / configuring the cert on a device below iOS 17.4 will crash.** Every touch point must be guarded — see §4.
>
> ℹ️ **Không có VoIP cert vẫn có tác dụng một phần:** với `pushConfig.needPush=YES` nhưng chưa cấu hình VoIP cert, server route qua **APNs thường** (cert APNs đã có) → killed callee nhận **notification thường** → tap → resume. Có VoIP cert mới nâng lên **system call UI** (LCK). Tức fix pushConfig có lợi ngay cả khi chưa có cert.

---

## 3. Flow

```
Caller ──► Yunxin server ──► PushKit VoIP push (payload carries call info)
                                   │
                                   ▼
                        iOS wakes the app in background
                                   │
                                   ▼
              AppDelegate.pushRegistry(didReceiveIncomingPushWith:)
                                   │  payload["nim"] present?
                                   ▼
              RNNeteaseIm.reportSystemIncomingCallWithPayload:
                                   │
                                   ▼
              NECallEngine.reportIncomingCallWithParam:  → system call UI
                                   │
                        accept / hangup / mute callbacks
```

---

## 4. What we implemented, and where

### 4.1 `pyeon-chinese-mobile/ios/ZYZJ/Info.plist`

`UIBackgroundModes` += `voip`. Without it PushKit will not deliver.

### 4.2 `pyeon-chinese-mobile/ios/ZYZJ/AppDelegate.swift`

- `import PushKit`; `private var voipRegistry: PKPushRegistry?`
- `setupNIMSDK()`: `option.pkCername = cerName`, wrapped in `if #available(iOS 17.4, *)`.
- `didFinishLaunchingWithOptions`: `if #available(iOS 17.4, *) { setupVoipPushKit() }`.
- `extension AppDelegate: PKPushRegistryDelegate`:
  - `didUpdate` → `NIMSDK.shared().updatePushKitToken(pushCredentials.token)`
  - `didReceiveIncomingPushWith` → guard `payload.dictionaryPayload["nim"] != nil` → call the lib → `completion()`

**Why the registrar lives in `AppDelegate` and not in the native module:** a VoIP push can wake the app **from killed**. At that moment the RN bridge does not exist yet, so a registrar owned by an RN native module would miss exactly the case VoIP push exists for. `AppDelegate` is also already where token plumbing lives (`updateApnsToken`), so this stays consistent.

> ⚠️ Use a **sub-queue** for `PKPushRegistry`, not the main queue — NetEase explicitly warns the UI may fail to appear / crash otherwise:
> ```swift
> PKPushRegistry(queue: DispatchQueue.global(qos: .default))
> ```

### 4.3 `react-native-netease-im` — `RNNeteaseIm.h` / `.m`

```objc
+ (void)reportSystemIncomingCallWithPayload:(NSDictionary *)payload;
```

Takes a plain `NSDictionary` on purpose: the app side never has to import `NERtcCallKit`, so all CallKit types stay inside the lib. `RNNeteaseIm.h` is exposed to Swift through `ZYZJ-Bridging-Header.h` (`#import <RNNeteaseIm.h>`, same pattern the other pods use). Swift call site reads `RNNeteaseIm.reportSystemIncomingCall(withPayload:)`.

Body builds `NECallSystemIncomingCallParam` (`payload` = raw `dictionaryPayload`, SDK parses it) and calls `reportIncomingCallWithParam:acceptCompletion:hangupCompletion:muteCompletion:` inside `if (@available(iOS 17.4, *))`.

---

## 5. Gotchas

- **`autoAccept` does NOT mean "answer without the user".** Read the header carefully: 点击接听按钮时是否自动实现接听逻辑, default `true` — i.e. *when the user taps Accept, does the SDK run the accept logic for you*. Default is what we want; there is no auto-answer risk. This is easy to misread.
- **Below iOS 17.4 there are two experience tiers, by design, not a bug**: no system UI → falls back to the banner (app alive) or an ordinary push notification. Guarded, so no crash.
- **`ringtoneName`** is left unset → system default. `caller_ring.mp3` exists in the bundle but CallKit-style UIs generally want `.caf`.
- LiveCommunicationKit does **not** show full-screen on the lock screen, and no longer leaves an entry in the address book call history.
- `payload` is passed straight through, trusting the SDK's default parse rule. Not statically verifiable — **verify against a real push**.

---

## 6. Open / unverified

| Item | Status |
|---|---|
| **VoIP cert name** | Assumed same as `apnsCername` (`"ZYZJIM"`). If the console issues a separate VoIP cert with a different name, split out a `voipCerName`. **Owner to confirm after upload.** |
| **`pkCername` + placeholder appKey** | `AppDelegate` registers with a **placeholder** appKey and the real one is swapped in later via `updateAppKey`. `apnsCername` has always worked under this exact pattern, so the risk is the same — **but** nothing in the headers proves whether the cert binds to bundle ID or to the runtime appKey, and there is **no** `updatePushKitCername` API. Real risk if appKey differs per env (dev/staging/prod) while the console cert binds one appKey. **Verify with a real push.** |
| **Payload shape** | Passed raw; SDK-side parsing unverified. |
| End-to-end on a real ≥17.4 device | Not yet run — needs the cert. |
