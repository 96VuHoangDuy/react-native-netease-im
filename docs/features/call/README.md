# Call (Voice CSKH — NERTC Call Kit)

> Snapshot: theo working tree branch `feat/final-version-for-setup-notification`, 2026-07-31. Một phần code mô tả dưới đây (custom fragment Android, branding/controllers iOS, notification channel) thuộc branch này và **chưa merge master**.

## Scope

- Cuộc gọi thoại 1-1 (audio-only) qua NERTC Call Kit (call-ui 4.3.0), dùng **prebuilt Call Kit UI** — UI gọi/nhận do native SDK dựng, JS chỉ trigger + observe state.
- Use case chính: user gọi CSR (CSKH). Có branding CSKH riêng (tên cố định, nền blur logo) khi accid đối phương có prefix `csr`.
- Không làm: video call, group call, JS tự vẽ UI cuộc gọi, custom inbound flow (`incomingCallEx`) — inbound hiện hoàn toàn do prebuilt SDK xử lý.
- Doc này mô tả **as-is**. Các thiết kế chưa code (notification 2 nút Answer/Reject) xem `docs/plans/incoming-call-notification-answer-reject-2026-07-31.md` — **plan, chưa implement**.

## Entry Points

- JS files:
  - `src/Call/Call.ts` — class `NimCall`, export singleton qua `index.ts:7,26`
  - `src/Call/call.type.ts` — `NIMCallStateEnum`, `NIMCallStateEvent`
  - `src/utils/eventListener.type.ts:20` — event name `observeCallState`
- Android native files (`android/src/main/java/com/netease/im/`):
  - `CallService.java` — trung tâm bridge CallKitUI: init, start/hangup, delegate → emit event, ringtone, notification channel, dọn foreground service
  - `CsP2PCallFragmentActivity.java` — activity p2p audio custom, chỉ override `provideUIConfig` để inject 3 fragment dưới
  - `CsAudioCallerFragment.java` / `CsAudioCalleeFragment.java` / `CsAudioOnTheCallFragment.java` — 3 màn caller / callee đổ chuông / in-call
  - `CsCallUiUtils.java` — helper branding (blur nền logo, safe-area)
  - `RNNeteaseImModule.java` — các `@ReactMethod` `startVoiceCall` / `hangupCall` / `setCustomerServiceCallName` / `hasOverlayPermission` / `requestOverlayPermission`
  - `login/LoginService.java` — login đã migrate V10 (`V2NIMLoginService`), điều kiện bắt buộc cho CallKit V2 signalling
- iOS native files (`ios/RNNeteaseIm/RNNeteaseIm/`):
  - `RNNeteaseIm.m` — `setupCallKitWithAppKey` (`:304`, gọi sau login V10 tại `:379`), `RCT_EXPORT_METHOD` start/hangup, native fast-path login cho VoIP wake (`ensureNativeLoginForIncomingCall`, `:512`), `reportSystemIncomingCallWithPayload:` (`:539`)
  - `RNNIMCsCallBranding.h/.m` — branding CSKH: `isCsrAccid`, display name, logo, control labels
  - `RNNIMCsCallControllers.h/.m` — subclass `NECalledViewController` / `NEAudioInCallController`, đăng ký qua `setCustomCallClass:` (`RNNeteaseIm.m:322`)

## State And Data Flow

### Outbound (user → CSR)

1. JS: `NimCall.startVoiceCall(accid, {pushTitle, pushContent})` — pushOptions bắt buộc để callee bị KILL nhận offline push (chỉ text, không avatar).
2. Android: `RNNeteaseImModule.startVoiceCall` → `CallService.startVoiceCall(activity, accid, ...)` → build `NECallPushConfig(needPush=true, ...)` + `CallParam(AUDIO)` → `CallKitUI.startSingleCall` (`CallService.java:320`). iOS: path tương đương trong `RNNeteaseIm.m`.
3. Prebuilt UI tự bung màn caller; state vòng đời forward về JS qua `observeCallState`.

### Inbound (CSR → user)

- Hoàn toàn do prebuilt SDK xử lý (full-screen intent / notification fallback trên Android; CallKit UI trên iOS). Repo **không** có `incomingCallEx` custom — chỉ có `notificationConfigFetcher` cho nhánh notification fallback (`CallService.java` khu vực `:139-143`).
- iOS app background/killed: PushKit VoIP đánh thức (registrar ở `AppDelegate` app host, ngoài repo này) → `ensureNativeLoginForIncomingCall` login native fast-path (credentials persist `NSUserDefaults`) → `reportSystemIncomingCallWithPayload:` báo LiveCommunicationKit (guard `@available(iOS 17.4,*)`).
- iOS banner in-app của SDK bị tắt (`enableIncomingBanner:NO`, `RNNeteaseIm.m:319`) vì banner mode bỏ qua delegate `didCallComing` → không branding CSKH được (quyết định #18 trong `CALLKIT_INTEGRATION_NOTES.md`).

### Ringtone / notification (Android)

- `SoundHelper` override: cả CONNECTING (caller) lẫn RING (callee) dùng chung `R.raw.caller_ring` (`CallService.java:154`).
- 2 notification channel: `cskh_incoming_call_v2` (app killed, có sound — `CallService.java:78`) và `cskh_incoming_call_alive_v2` (process sống, không sound để tránh đè chuông SoundHelper — `:83`).
- Foreground service in-call là của SDK (id 1026); repo tự `forceStopCallForegroundService()` (`CallService.java:334`) để dọn cứng vì SDK đôi khi không tự stop.
- `android/src/main/res/values/strings.xml` override `tip_notification_channel_name` để giữ tên channel ổn định.

## Public API

- `NimCall.startVoiceCall(accid, pushOptions?)`
- `NimCall.hangupCall()`
- `NimCall.setCustomerServiceCallName(name)` — tên hiển thị cố định khi accid prefix `csr`; gọi lúc init IM + đổi ngôn ngữ
- `NimCall.setCallControlLabels({micOn, micOff, hangup, speakerOn, speakerOff, incomingSubtitle, accept, reject})` — text call UI iOS (3 nút in-call + màn callee); Android dùng fragment riêng nên no-op nếu native thiếu method
- `NimCall.hasOverlayPermission()` / `requestOverlayPermission()` — quyền `SYSTEM_ALERT_WINDOW` cho 来电横幅 (Android only; iOS luôn resolve `true`)
- Event: `observeCallState` → payload `NIMCallStateEvent { state, accId?, code?, message? }`

## Business Rules

- CSKH branding kích hoạt theo accid prefix `csr` (fix cứng) — tên từ `setCustomerServiceCallName`, nền blur logo qua `CsCallUiUtils.applyBrandBlurBackground` (Android) / `RNNIMApplyCsBranding` (iOS).
- Caller phải set pushOptions, nếu không server không gửi offline push cho callee killed.
- CallKit chỉ init sau khi login V10 thành công (Android: `LoginService`; iOS: `ensureRegisterV2WithAppKey` → `setupCallKitWithAppKey`).

## Platform Notes

- Android:
  - UI 3 màn custom qua fragment inject vào `CsP2PCallFragmentActivity` (đăng ký `.p2pAudioActivity(...)`); in-call thay pill nhỏ bằng 3 nút tròn to tái dùng ImageView gốc của SDK.
  - Manifest chỉ đăng ký activity trên; **không** có receiver `CallActionReceiver` (plan Answer/Reject chưa code).
  - `rtcAppKey` đọc từ manifest metadata tĩnh `com.netease.nim.appKey` — có thể lệch appKey runtime (xem Gaps).
- iOS:
  - Custom qua subclass controller (`kCalledState` / `kAudioInCall`), ép branding tại `viewWillAppear`/`refreshUI` — lưới an toàn cho path resume từ VoIP push không qua delegate.
  - Delegate `RNNIMCallUIDelegate<NECallUIKitDelegate>` fill tên/avatar callee từ NIM profile.
  - **iOS không emit `observeCallState`** — contract event hiện lệch với Android (xem Gaps).

## Error Handling Notes

- `observeCallState` state `error` có field `code`/`message` trong type nhưng chưa thấy native emit (xem Gaps).
- Android `emitState` nuốt exception, chỉ `Log.e` (`CallService.java:380-386`).

## Gaps

- **Chiều CSR → user chưa xác định owner**: chưa biết client nào (ngoài 4 repo workspace) khởi tạo cuộc gọi từ phía CSR — chi tiết ở `docs/playbook/GAPS.md` mục "Call — Inbound Ownership". Chặn mọi thiết kế inbound custom cho app-killed.
- **iOS chưa forward `observeCallState`** sang JS (tự nhận trong `docs/reference/netease-im/CALLKIT_INTEGRATION_NOTES.md`).
- **Android chỉ emit 3/8 state** của `NIMCallStateEnum`: `incoming`/`connected`/`ended` (`CallService.java:363,368,373`); `calling`/`ringing`/`rejected`/`timeout`/`error` khai báo nhưng chưa có code emit.
- **Plan notification Answer/Reject chưa code** (`CsIncomingCallEx`, `CallActionReceiver` không tồn tại trong working tree) — chỉ là plan doc.
- **`rtcAppKey` Android** đọc tĩnh từ manifest, có thể stale so với appKey backend dùng lúc login — chưa verify.
- **E2E 2 máy thật (Android ↔ iOS) chưa nghiệm thu** (RTC không chạy trên iOS simulator).

## Tài liệu liên quan

- `docs/reference/netease-im/CALLKIT_INTEGRATION_NOTES.md` — nhật ký tích hợp thực tế: version pin, 20 gotcha đánh số, risk còn mở (nguồn chi tiết nhất)
- `docs/reference/call-android-native/` / `docs/reference/call-ios-native/` — docs "how" chính chủ NetEase (generic, không phải hiện trạng repo)
- `docs/plans/incoming-call-notification-answer-reject-2026-07-31.md` — plan chưa implement
- `docs/playbook/GAPS.md` — mục "Call — Inbound Ownership"
