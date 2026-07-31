# NERTC Call Kit — Integration Notes (bản triển khai thật)

> Ghi lại **cấu hình thật + các gotcha** khi tích hợp NERTC Call Kit (voice call, Phase 3) vào
> `react-native-netease-im` + app `pyeon-chinese-mobile`. Docs `docs/call-*-native/` là hướng dẫn
> "how" (dùng version **ví dụ**); file này là **những gì thực sự chạy** + lỗi đã gặp & cách fix.
> Cập nhật: 2026-07-17. Trạng thái: Android + iOS **build/chạy OK**; login đã chuyển **V10** (mở khóa V2 signalling
> — hết `191001 misuse`); chat gửi/nhận/hiển thị OK. **Call P2P E2E (2 máy thật) chưa nghiệm thu.**
> 2026-07-17: bật **来电横幅** (2 platform, Android bump call-ui 4.3.0) + **LiveCommunicationKit** iOS (PushKit VoIP).
> Cả hai **mới chỉ verify compile** (Android `BUILD SUCCESSFUL`, iOS `BUILD SUCCEEDED`) — **chưa chạy máy thật**; LCK còn chờ VoIP cert.
> Chi tiết migration login V9→V10 (điều kiện tiên quyết của signalling): `LOGIN_V9_TO_V10_MIGRATION.md`.

## Version pin thật (đã verify tồn tại)

| | Android | iOS |
|---|---|---|
| NIM | `com.netease.nimlib:{basesdk,push,lucene,avsignalling}:10.9.52` | pod `NIMSDK_LITE` + `NIMSDK_LITE/FTS` `10.9.53` |
| call-ui / CallKit | `com.netease.yunxin.kit.call:call-ui:4.3.0` (bump 2026-07-17 từ 4.1.0 — cần cho banner) | pod `NERtcCallKit/NOS_Special` + `NERtcCallUIKit/NOS_Special` → thực tế **4.8.1** (podspec **không pin**) |
| NERTC | `com.netease.yunxin:nertc:5.9.10` | pod `NERtcSDK/RtcBasic:5.9.10` |

Version-compat + lý do chọn 4.3.0 + sai lệch iOS 4.8.1: `CALLKIT_VERSION_COMPAT.md`.

## Kiến trúc bridge (đã code)

- **JS**: `index.ts` export `NimCall`; `src/Call/Call.ts` (`startVoiceCall(accid)` / `hangupCall()`),
  `src/Call/call.type.ts` (`NIMCallStateEnum`); event `observeCallState` trong `src/utils/eventListener.type.ts`.
- **Android**: `CallService.java` (init `CallKitUI` + `startSingleCall` + delegate → `ReactCache.emit`),
  `RNNeteaseImModule.java` (`@ReactMethod startVoiceCall/hangupCall`), init trong `IMApplication.init()`.
- **iOS**: `RNNeteaseIm.m` — `setupCallKitWithAppKey` gọi trong `ensureRegisterV2WithAppKey` (sau `registerWithOptionV2`
  V10, thay `registerWithAppID` V9), `startVoiceCall`/`hangupCall`. iOS **chưa** forward `observeCallState` (best-effort).
- **App**: `chatStore` gắn `nim.NimCall` + log `observeCallState`; nút 📞 ở `ChatDetail.Header.tsx` (P2P).
- Dùng **prebuilt CallKit UI**, **audio-only** (`NECallType.AUDIO` / `NECallTypeAudio`), **inbound giữ bật** (không intercept).

## Gotchas & fix (theo thứ tự đã gặp)

### Android
1. **Package path call-ui sai** (đoán) → `cannot find symbol`. Đúng (verify bằng `javap` trên AAR):
   - `com.netease.yunxin.nertc.ui.{CallKitUI,CallKitUIOptions}` (KHÔNG phải `...kit.call.ui`)
   - `com.netease.yunxin.kit.call.p2p.model.{NECallEngineDelegateAbs,NECallInitRtcMode,NECallType}`
   - `com.netease.yunxin.kit.call.p2p.param.{NEHangupParam}` ; call param dùng `com.netease.yunxin.nertc.ui.base.CallParam`
   - `startSingleCall(Context, CallParam)` ; `callType`/`initRtcMode` là **int** (`NECallType.AUDIO` = 1).
2. **Duplicate class `com.netease.lava.*`**: `nertc` (full) + `nertc-base` (call-ui kéo transitively) trùng.
   → exclude đúng module **`nertc-base`** (không phải `nertc-base-sdk` như doc), giữ full `nertc`.
3. **`NoClassDefFoundError: V2NIMSignallingChannelType`** khi `startVoiceCall`: do `exclude com.netease.nimlib`
   ở call-ui làm mất module signalling. → thêm `com.netease.nimlib:avsignalling:10.9.52`.
4. compileSdk phải ≥ 31 (call-ui) — app đã compileSdk 35.
5. **ABI NERTC**: `arm64-v8a, armeabi-v7a, x86` — **KHÔNG có x86_64**. → emulator **arm64 (Apple Silicon) OK**;
   emulator x86_64 (Intel) sẽ `UnsatisfiedLinkError`. Emulator cần **bật virtual microphone**.

### iOS
1. **Framework conflict** `nimsdk/nimsocketrocket/nimquic.xcframework`: NERtcCallKit bắt buộc `NIMSDK_LITE`,
   trùng với `NIMSDK` full. → đổi podspec `NIMSDK` → **`NIMSDK_LITE` (+/FTS)**. LITE vẫn cấp module `NIMSDK`.
2. **NIMSDK_LITE không có `NIMAVChat`** → phải **gỡ code legacy NIMAVChat** (net-call record display, obsolete):
   `ImConfig.h`, `NTESBundleSetting.h/.m` (video-quality → NSInteger), `NIMKitUtil.m`, `ConversationViewController.m`,
   `NIMViewController.m`. Giữ `NIMMessageTypeRtcCallRecord` (core V10).
3. **`NIMMessageMaker.m` mất AVFoundation/CoreMedia** (trước lấy transitively qua NIMAVChat) → thêm
   `#import <AVFoundation/AVFoundation.h>`.
4. **API call/hangup sai** (đoán) → sửa theo header pod thật:
   - `NEUICallParam` không có `currentUserAccid`; set `remoteUserAccid` + `callType`.
   - `[[NERtcCallUIKit sharedInstance] callWithParam:callParam]` (1 arg, không `withCallType:`).
   - `[[NECallEngine sharedInstance] hangup:param completion:^{}]` (`NEHangupParam*`).
5. **Simulator arm64 link fail**: NERtcCallKit/UIKit xcframework chỉ có slice `ios-x86_64-simulator`
   (không arm64-sim). → script **`scripts/patch-nertc-callkit-simulator.sh`** (retag arm64-device → iossim,
   lipo vào sim slice + sửa Info.plist + copy-script `archs_for_slice`). Wire trong `scripts/ios-preinstall-simulator.sh`;
   chạy qua `yarn ios:sim:prepare`. **Call thật vẫn cần máy thật** (RTC không chạy trên iOS simulator).
6. **`Library not loaded: @rpath/Masonry.framework`**: NERtcCallUIKit/CallKit là prebuilt **dynamic** framework,
   cần các source pod dạng dynamic. → `pre_install` hook trong `ios/Podfile` ép dynamic:
   `Masonry, NECommonKit, NECommonUIKit, NECoreKit, NEXKitBase, SDWebImage, YXAlog_iOS`.
7. **iOS: `dyld: Library not loaded: @rpath/NECommonKit.framework` (have 'x86_64', need 'arm64')** lúc launch trên
   Simulator arm64: `NECoreKit`/`NECommonKit`/`NECommonUIKit` (dependency của NERtcCallUIKit) cũng là prebuilt
   xcframework chỉ ship slice sim x86_64 — cùng bệnh gotcha #5 nhưng script ban đầu chỉ patch 2 pod NERtc*.
   → đã thêm 3 pod này vào `scripts/patch-nertc-callkit-simulator.sh`.
8. **Android: app release crash ngay lúc launch** — `RuntimeException: Unable to get provider
   ...corekit.startup.InitializationProvider` / `ClassNotFoundException: com.netease.yunxin.nertc.ui.CallKitUIService`.
   `call-ui` bootstrap qua "xkit startup": `InitializationProvider` đọc tên class từ `<meta-data>` trong manifest rồi
   `Class.forName()`. Không có reference tĩnh → **R8 strip hẳn class** ở buildType `release` (`minifyEnabled true`);
   debug không minify nên không lộ. aar `call-ui` có `proguard.txt` **rỗng** → không tự bảo vệ.
   Lưu ý: `android/proguard-rules.pro` của lib có `-keep class com.netease.** {*;}` nhưng nằm trong `proguardFiles`
   (chỉ áp khi tự build lib), **không** merge sang app — đừng nhầm là đã được bảo vệ.
9. **Android: bấm call → native crash** `Fatal signal 6 (SIGABRT)` tại `libnertc_sdk.so (JNI_OnLoad+148)`
   ← `System.loadLibrary` ← `NERtcCore`/`NativeLibLoader`. `JNI_OnLoad` gọi `FindClass()` bằng **tên chuỗi cứng**
   (`com/netease/lava/nertc/impl/NERtcCore`); R8 **rename** class → `FindClass` fail → `abort()`.
   Đo từ `mapping.txt`: `com.netease.lava` 84/96 class bị rename, `com.netease.yunxin.lite` 30/71.
   `NERtcCore`/`NativeLibLoader` **không khai method `native`** nào → chỉ keep class có native method là **không đủ**,
   phải keep cả package.

**→ Fix chung cho #8 + #9** (`android/consumer-rules.pro`, khai qua `consumerProguardFiles` ở `defaultConfig`
của `android/build.gradle`), lấy nguyên từ **docs official** `docs/reference/call-android-native/01-integration-android.md`
§4 ProGuard — **đừng tự suy rule từ bytecode** (lần đầu tự suy ra rule `XKitService` hẹp → fix được #8, vẫn dính #9):
```proguard
-dontwarn com.netease.lava.**
-keep class com.netease.lava.** {*;}
-dontwarn com.netease.yunxin.**   # bao trùm yunxin.kit + yunxin.nertc → cover luôn #8
-keep class com.netease.yunxin.** {*;}
```
Cả #8 lẫn #9 **chỉ lộ ở release** (`minifyEnabled true`); debug không minify. Mọi aar NetEase
(`nertc`, `nertc-base`, `call-ui`, `corekit`…) đều có `proguard.txt` **rỗng** → không tự bảo vệ.
Rule NIM (`com.netease.nim.**`, `nimlib.**`, `share.**`, `mobsec.**`) hiện nằm ở `android/app/proguard-rules.pro`
của app ZYZJ — lib **chưa** export, app khác dùng lib sẽ thiếu (xem `docs/playbook/GAPS.md`).

## 来电横幅 (call banner) + 接听系统电话 (LiveCommunicationKit) — thêm 2026-07-17

Hai feature **khác nhau**, đừng nhầm (nhầm lẫn này lặp lại nhiều lần):

| | Banner | LiveCommunicationKit |
|---|---|---|
| Khi nào | App **đang sống** | App **background / bị kill** |
| Platform | iOS + Android | **iOS only** (framework Apple) |

Android **không có** đối ứng LCK — doc sidebar Android không có mục 接听系统电话 là **đúng**, không phải thiếu doc. Ca app-killed của Android đi bằng offline push (MixPush) → notification → tap mở app → resume (xem #15, **điều kiện: bên gọi set `pushConfig`**).
Chi tiết: `docs/call-ios-native/05-system-call-lck.md`, `docs/call-*-native/03-advanced.md#call-banner`, #15 dưới đây.

### #10 — Banner mặc định TẮT, phải bật runtime
`enableIncomingBanner` là **runtime method**, không phải config lúc init:
- iOS: `[[NERtcCallUIKit sharedInstance] enableIncomingBanner:YES]` — trong `setupCallKitWithAppKey:`, **sau** `setupWithConfig:`.
- Android: `CallKitUI.enableIncomingBanner(true)` — trong `CallService.init()`, **sau** `CallKitUI.init(...)`. **KHÔNG** set qua `CallKitUIOptions.Builder` (field cùng tên là internal, không phải public API).

### #11 — call-ui 4.1.0 KHÔNG có `enableIncomingBanner` → buộc bump 4.3.0
Đã `javap` full method list `CallKitUI` trên aar 4.1.0: không có method banner nào. Có từ **4.3.0**.
Chọn 4.3.0 (không phải 4.8.2 latest) vì dep matrix trùng khít 4.1.0 (NIM 10.9.52 / nertc-base 5.9.10 / corekit-im2 1.10.1) → drop-in. 4.8.2 đòi NIM 10.9.81. Bảng đầy đủ: `CALLKIT_VERSION_COMPAT.md`.
Giữ nguyên 2 `exclude` cũ (`com.netease.nimlib`, `nertc-base`) — lý do exclude không đổi.

### #12 — Overlay permission: `react-native-permissions` KHÔNG dùng được
`SYSTEM_ALERT_WINDOW` là **special appop**, không phải runtime permission → `check(PERMISSIONS.ANDROID.*)` không cover, `CustomPermissionModal` không tái dùng được. Phải viết native: `RNNeteaseImModule.hasOverlayPermission/requestOverlayPermission` (`Settings.canDrawOverlays` + `ACTION_MANAGE_OVERLAY_PERMISSION`).
- **Manifest không cần sửa**: aar `call-ui:4.3.0` tự khai `SYSTEM_ALERT_WINDOW`/`POST_NOTIFICATIONS`/`USE_FULL_SCREEN_INTENT` → manifest merger tự gộp.
- Xin **chủ động** sau login (`AppProvider.tsx`, gate `isAuthorized`) thay vì để SDK tự nhảy Settings lúc có cuộc gọi. Quyền này cần cho **bên NHẬN** → phải xin trước, không đợi lúc gọi.
- Android **không** báo kết quả về khi user quay lại từ Settings → fire-and-forget, muốn biết thì gọi lại `hasOverlayPermission()`.

### #13 — iOS < 17.4 register PushKit / set `pkCername` sẽ CRASH
Guard `@available(iOS 17.4, *)` ở **cả 3 chỗ**: `option.pkCername`, `setupVoipPushKit()`, `reportSystemIncomingCallWithPayload:`. Máy cũ → không có system UI, fallback banner/push thường (2 tier trải nghiệm theo OS, **không phải bug**).
- `PKPushRegistry` phải dùng **sub-queue** (`DispatchQueue.global(qos: .default)`), NetEase cảnh báo main queue có thể không bung UI / crash.
- Registrar sống ở **`AppDelegate.swift`**, không ở native module: VoIP push đánh thức app từ **killed**, lúc đó RN bridge chưa tồn tại.
- `Info.plist` `UIBackgroundModes` phải có **`voip`**.
- Swift gọi lib qua `ZYZJ-Bridging-Header.h` → `#import <RNNeteaseIm.h>`; method khai `+ (void)reportSystemIncomingCallWithPayload:(NSDictionary *)` nhận **NSDictionary thuần** để app không phải import `NERtcCallKit`.

### #14 — `autoAccept` KHÔNG phải "tự nghe máy thay user"
`NECallSystemIncomingCallParam.autoAccept` mặc định `true`. Đọc kỹ header: 点击接听按钮时是否自动实现接听逻辑 = *khi user bấm nút nghe thì SDK có tự chạy logic accept không*. Để mặc định là đúng, **không** có rủi ro auto-answer. Rất dễ đọc nhầm.

### #15 — App KILLED không nhận cuộc gọi → BÊN GỌI phải set `pushConfig` (fix 2026-07-20)
Triệu chứng: app tắt hoàn toàn, cuộc gọi tới **không hiện gì**. Đây **không** phải lỗi banner.

**Cơ chế (verify từ decompile call-ui 4.3.0):** `DefaultIncomingCallEx.onIncomingCall` — nơi chứa **cả 3 nhánh** banner / notification / full-screen — là callback từ `callEngineDelegate`, **chỉ fire khi process SỐNG + có signalling**. App killed ⇒ callback không chạy ⇒ banner (overlay `WindowManager`) không vẽ được, notification cũng không. Manifest `call-ui` **không có receiver/service** nào để push đánh thức.

**Đường duy nhất tới máy killed = offline push của NIM**, chỉ được gửi khi **bên gọi** set `NECallPushConfig(needPush=true, ...)`:
- Android: `new CallParam.Builder()....pushConfig(new NECallPushConfig(true, title, content, null, true))` trong `CallService.startVoiceCall`.
- iOS: `callParam.pushConfig` (`NEUICallParam.pushConfig`) với `needPush=YES` trong `RNNeteaseIm.m startVoiceCall`.
- Trước fix, **cả 2** `startVoiceCall` đều thiếu ⇒ killed callee không nhận gì, **và LiveCommunicationKit iOS (#13) inert** (không có VoIP push nào được yêu cầu).

**Params `NECallPushConfig` (giống hệt 2 platform):** `needPush` · `pushTitle` · `pushContent` · `pushPayload` (opaque, dùng sau khi mở app) · `needBadge`. **KHÔNG có field avatar** — notification killed do OS render **text thuần**; muốn callee thấy tên người gọi ⇒ nhét vào `pushContent`. Text truyền từ JS (`NimCall.startVoiceCall(accid, {pushTitle, pushContent})`) để giữ i18n cn/vi; native có fallback chuỗi trung tính.

**End-state Mức A:** killed → notification → tap → app mở → SDK `resumeBGInvitation(true)` + `tryResumeInvitedUI` resume màn gọi. iOS: chưa có VoIP cert ⇒ nhận APNs notification thường (vẫn Mức A); có cert ⇒ nâng lên system call UI (LCK). Fix pushConfig **độc lập** cert — có tác dụng ngay.

**Điều kiện tiên quyết:** offline push NIM phải hoạt động khi killed (đã verify: message push thường tới bình thường ⇒ hạ tầng OK).

**Liên quan:** call UI LIVE hiện tên+avatar → xem #16 (đã fix 2026-07-20).

### #16 — Call UI LIVE hiện accid thay vì tên/avatar (fix 2026-07-20)
Màn "Calling…" (caller) + banner (callee) hiện **accid + avatar mặc định**.

**Nguyên nhân (khác nhau theo platform):**
- **iOS**: SDK **không** tự lấy NIM info → `NEUICallParam.remoteShowName` mặc định = accid.
- **Android**: SDK default lấy IM info nhưng chỉ từ **cache local** → **cold cache** (callee chưa fetch profile người gọi) ⇒ accid.

**Nguồn đúng = NIM user profile** — app đã đẩy `nickName`+`avatarUrl` lúc IM connect (`ConnectStatusIMStore.updateMyUserInfo`), chat cũng dùng nguồn này. Fix wire call UI vào đó + **async remote fetch cho cold cache**:
- **Android** (`CallService.userInfoHelper()`): `NimUserInfoCache.getUserName/getAvatar` → miss thì `getUserInfoFromRemote` async → `notify.invoke`. Set qua `.userInfoHelper(...)`. Impl `UserInfoHelper` interface trong Java dùng `kotlin.jvm.functions.Function1/Function2` + `kotlin.Unit`, `return true` (async).
- **iOS** (`RNNeteaseIm.m`): caller set `callParam.remoteShowName/remoteAvatar` (`RNNIMFillCallUserInfo`); callee `RNNIMCallUIDelegate : NECallUIKitDelegate` (`didCallComingWithInviteInfo:` → cold cache `fetchUserInfos:` async → `completion(YES)`), gán `[NERtcCallUIKit sharedInstance].delegate` trong `setupCallKitWithAppKey:`.

**Độc lập với #15** (killed push text). Avatar lúc KILLED vẫn không có (OS render text) — không đổi.

### #17 — Android: `CallService.init()` bị GATE bởi `isAgreePolicy`/timing (phát hiện test thật 2026-07-23)
Fix #16 (wire `userInfoHelper`) **chưa đủ**: khi test thật, cuộc gọi CSKH hiện lại **accid thô `csr11648`** + initials, dù đã có `userInfoHelper`.

**Nguyên nhân:** `CallService.init()` (nơi wire `userInfoHelper`) chỉ chạy khi **cả 3**: `isAgreePolicy==true` lúc `IMApplication.init()` **và** init V2 complete **và** main process (`IMApplication.java:94-120`, `CallService.init` gọi trong callback `observeMainProcessInitCompleteResult`). Cold start / policy chưa agreed lúc `init()` → rơi nhánh `else` `NIMClient.config` → **`CallService.init` KHÔNG chạy** → `userInfoHelper` không set → SDK fallback `getNameFromInfo(null, accId)` = accid thô. Cuộc gọi + banner **vẫn tới** nhờ NIM signalling + `DefaultIncomingCallEx` (default SDK), độc lập với init này → dễ nhầm là "CallKit đã init".

**Xác nhận:** logcat 1 cuộc gọi lỗi → **không có** delegate app trong `addCallDelegate` (chỉ SDK `IncomingCallBannerManager`/`P2PCallFragmentActivity`). Khi init chạy (app đã login/policy agreed) → banner + in-call hiện đúng `CSKH ZYZJ` + logo. `UserInfoHelper` là **global, dùng chung mọi màn** (banner + in-call `AudioOnTheCallFragment.renderUserInfo` đều gọi `OthersExtendKt.fetchNickname/loadAvatarByAccId`) nên khi active thì mọi màn đúng.

**Rủi ro killed app (chưa test):** push-wake → `CallService.init` chạy **async** (sau NIM init complete); SDK có thể render UI cuộc gọi **trước** khi init xong → race → hiện accid thô.

**Fix hướng:** đảm bảo `CallService.init` được gọi (idempotent) **sau khi** policy agreed + login, và không phụ thuộc `isAgreePolicy` tại đúng một thời điểm cold-start.

### #18 — iOS: BANNER MODE bỏ qua delegate `didCallComing` (phát hiện test thật 2026-07-23)
Tương tự #17 nhưng cơ chế khác: iOS bật `enableIncomingBanner:YES` → `NERtcCallUIKit -onReceiveInvited:` dùng `NEIncomingCallBannerWindow` và **KHÔNG gọi** delegate `didCallComingWithInviteInfo:` (fill của #16). Banner tự gọi thẳng `[[NIMSDK].userManager fetchUserInfos:@[callerAccId]]` → build `NEIncomingCallerInfo.displayName/avatarUrl` từ **NIM hosted profile** (trống với `csr*` → hiện accid + avatar mặc định). Sau accept, full-screen dùng `callParam` chưa set → `(null)`.

**Không có public hook** cho app override caller-info của banner (đã đọc hết `NECallUIKitConfig`/`NECallUIDynamicConfig`/`NECallUIKitDelegate` — chỉ 3 method: `didCallComing`, `inviteUsers`, `selectInviteUsers`).

**Fix hướng:** hoặc (a) tắt `enableIncomingBanner` cho iOS → full-screen incoming → `didCallComing` chạy → hardcode CS hiện đúng (đánh đổi UX banner); hoặc (b) đẩy NIM profile cho `csr*` (fix gốc, giữ banner).

**Fix (2026-07-28, ĐÃ LÀM — hướng (a) + lưới an toàn):** owner chốt không đụng backend.
- `enableIncomingBanner:NO` trong `setupCallKitWithAppKey:` → incoming bung full-screen callee → `didCallComing` chạy → nhánh CSR của `RNNIMFillCallUserInfo` áp đúng cho cả màn đổ chuông lẫn in-call.
- Thêm short-circuit CSR trong `didCallComing`: accid `csr*` fill branding ngay, **không** chờ `fetchUserInfos:` (branding không phụ thuộc NIM profile).
- Logic branding tách ra `RNNIMCsCallBranding.{h,m}` (`isCsrAccid:` / `displayName` / `setDisplayName:` / `logoFileUrl` / `logoImage`) — trước đó là static function trong `RNNeteaseIm.m`, file khác không dùng được.
- Lưới an toàn `RNNIMCsCallControllers.{h,m}`: `RNNIMCsCalledViewController : NECalledViewController` (`kCalledState`) + `RNNIMCsAudioInCallController : NEAudioInCallController` (`kAudioInCall`), đăng ký qua `setCustomCallClass:`. Override `viewWillAppear:`/`refreshUI` → ép `centerTitleLabel`/`titleLabel`/`remoteAvatorView`/`remoteBigAvatorView`. Guard `isCsrAccid:` nên non-CSR giữ UI mặc định SDK. Cần cho path resume từ VoIP push (không qua `didCallComing`) và khi SDK `refreshUI` ghi đè label.

**Phạm vi tắt banner** — chỉ đổi UI khi app foreground (banner nhỏ → full-screen). Background/killed đi đường offline push NIM (#15), độc lập `enableIncomingBanner`, **không đổi**.

**Song song:** đây cũng là chỗ về sau áp style CSKH của Android (#19: nền tối, avatar nền trắng) cho iOS — **chưa làm**.

### #19 — Logo call UI thành "ĐEN-XANH" thay vì trắng-xanh (phát hiện 2026-07-23)
`cs_call_logo.png` (`android/src/main/res/drawable/`, `ios/`) có **alpha (nền trong suốt)** — `sips` báo `hasAlpha: yes`, 531×470. Màn in-call SDK dùng avatar làm **background phóng to toàn màn** (`ivBg` trong `OthersExtendKt.loadAvatarByAccId(accId, ctx, ivInnerAvatar, ivBg, tvInitials, ...)`) trên nền view **màu đen** → phần trong suốt của logo lộ nền đen → logo trắng-xanh thành **đen-xanh**. Trong chat/app bình thường thấy trắng-xanh vì nền phía sau là trắng.

**Gốc kỹ thuật:** màn in-call load avatar vào **cả** `ivBg` (background phóng to full màn) **lẫn** `ivUserInnerAvatar` (ô nhỏ), rồi vẽ `tvUserName` màu trắng (`@color/colorWhite`) đè lên. → logo nền trong suốt = "đen-xanh"; logo nền trắng = nuốt chữ trắng. Đổi asset KHÔNG giải quyết triệt để (owner bắt buộc giữ logo "xanh nền trắng" gốc).

**Fix (2026-07-24, ĐÃ LÀM — Android): TÁCH background khỏi avatar bằng subclass (không đổi asset).**
SDK `call-ui` mở extension chính thức: `CallKitUIOptions.Builder().p2pAudioActivity(Activity)` → Activity override `provideUIConfig(CallParam)` trả `P2PUIConfig` với `customCallFragmentByKey(AUDIO_ON_THE_CALL=6, fragment)`.
- `CsAudioOnTheCallFragment extends AudioOnTheCallFragment` — override `toRenderView`/`renderUserInfo`, sau `super`: `binding.ivBg` → `GONE` (bỏ avatar-làm-nền), `binding.clRoot` → nền tối `#123D22`, `binding.flUserAvatar`/`ivUserInnerAvatar` → nền trắng (logo hiển thị "xanh nền trắng"). Chữ tên trắng đọc rõ trên nền tối.
- `CsP2PCallFragmentActivity extends P2PCallFragmentActivity` — override `provideUIConfig` (default SDK trả config rỗng nên build mới an toàn).
- Wire: `CallService.init()` thêm `.p2pAudioActivity(CsP2PCallFragmentActivity.class)`; đăng ký `<activity>` trong `AndroidManifest.xml` lib.
- File: `android/src/main/java/com/netease/im/{CsAudioOnTheCallFragment,CsP2PCallFragmentActivity}.java` + `CallService.java` + `AndroidManifest.xml` (sync repo lib ↔ node_modules).
- **Asset `cs_call_logo.png` giữ NGUYÊN bản gốc** (xanh nền trong suốt) — đã khôi phục.
- **Scope (quan trọng):** style tuỳ biến (ẩn `ivBg` + nền tối + avatar nền trắng) **CHỈ áp cho cuộc gọi CSKH** — guard bằng `CallService.isCsrAccid(accId)` (đổi `private`→package-private). Call user thường có avatar ảnh thật → **giữ nguyên mặc định SDK** (avatar phóng to làm nền, trông đúng hơn).
- **Ẩn nút chuyển audio→video** (`binding.ivCallChannelTypeChange` → GONE) áp cho **mọi** cuộc gọi vì app chỉ hỗ trợ voice call (khớp iOS đã set `enableAudioToVideo=NO`).
- ⚠️ Trong fragment **KHÔNG** truy cập `binding.clRoot` (kiểu `ConstraintLayout`) hay `binding.switchTypeTipGroup` (kiểu `ConstraintLayout.Group`) — module lib không có dependency `androidx.constraintlayout` → lỗi compile *"cannot access ConstraintLayout"*. Dùng `getRootView()` (trả `android.view.View`) thay thế.
- Scope màn: chỉ IN-CALL audio (`AUDIO_ON_THE_CALL`). Banner đổ chuông hiển thị OK sẵn (ô nhỏ). Rebuild Android để áp dụng.
- **iOS: CHƯA LÀM** — cần fix banner-mode (#18) trước để iOS hiện được avatar; sau đó áp `setCustomCallClass:` + subclass `NEAudioInCallController`/`NECallUIDynamicConfig.incomingCallBackground` tương tự.

**Cập nhật (2026-07-29) — ĐỔI HƯỚNG sang "logo blur + scrim" để đồng bộ iOS. Thay thế cách "ẩn ivBg + nền phẳng" ở trên.**
Sau khi iOS chạy được (#18), so ảnh 2 platform thấy lệch hẳn: iOS nền **mờ (blur)** + logo 90dp ở giữa **không khung trắng**; Android nền xanh phẳng `#123D22` + logo trong ô vuông trắng. Thêm nữa màn ĐỔ CHUÔNG Android chưa tuỳ biến gì → `ivBg` load avatar sắc nét full màn ⇒ logo phóng to vỡ nét, chữ trong logo tràn màn hình (xấu nhất).
- `CsCallUiUtils.applyBrandBlurBackground(View root, ImageView ivBg)` — dựng bitmap nền: logo vẽ trên nền **TRẮNG** (bắt buộc, nếu blur logo alpha trên nền đen là tái hiện đúng bug đen-xanh ở trên) 64×128 → blur 2 lần bằng downscale/upscale bilinear (**không** dùng RenderScript deprecated, cũng không cần `jp.wasabeef BlurTransformation` dù lib này có sẵn trên classpath qua call-ui) → phủ scrim `0x80000000` để chữ tên trắng đọc rõ → set làm `background` của **root view**. Bitmap **cache static** nên `renderUserInfo` chạy lại nhiều lần không cấp phát lại.
- ⚠️ **Không set bitmap thẳng vào `ivBg`** — lần thử đầu (2026-07-29) làm vậy và **không có tác dụng gì**: `OthersExtendKt.loadAvatarByAccId` load avatar vào `ivBg` qua **Glide bất đồng bộ**, request về sau **ghi đè** bitmap mình vừa set (kể cả khi set trong `renderUserInfo` sau `super`). Triệu chứng: màn call trông y hệt bản chưa custom. Cách chắc chắn duy nhất mà không cần đụng Glide (module lib **không có** Glide trên compile classpath vì call-ui khai `implementation`): `ivBg` → `setImageDrawable(null)` + `GONE`, còn nền đặt lên root view — root SDK không đụng tới.
- `CsAudioOnTheCallFragment.applyCsStyle()` rút gọn còn 1 dòng gọi helper trên; bỏ hằng `BG_DARK`/`AVATAR_BG_WHITE`, không còn ẩn `ivBg`, không còn set nền trắng cho `flUserAvatar`/`ivUserInnerAvatar`.
- `CsAudioCalleeFragment extends AudioCalleeFragment` (**mới**) — đăng ký `customCallFragmentByKey(AUDIO_CALLEE=5, ...)`. Cùng style nền cho CSKH; thêm ẩn `ivSwitchType`/`tvSwitchTypeDesc` cho **mọi** cuộc gọi (app chỉ voice call).
- `CsAudioCallerFragment` (`AUDIO_CALLER=4`) cũng áp cùng style nền cho CSKH — **cả 3 màn audio** đều dùng `ivBg` làm nền avatar phóng to nên đều dính. Ban đầu bỏ sót màn này (plan chỉ chốt callee + in-call) và bị phát hiện khi test outbound.
- Guard `isCsrAccid` giữ nguyên → call user thường vẫn dùng nền mặc định SDK.
- Nút thu nhỏ (PIP) Android **giữ nguyên** dù iOS không có (`enableFloatingWindow` mặc định NO) — owner quyết định giữ.
- Tham số cần tinh chỉnh bằng mắt: `SCRIM_COLOR` (tăng lên `0x80000000` nếu chữ trắng còn chìm).

### #20 — Màn call Android thiếu safe-area top (đè status bar/notch) (fix 2026-07-24)
Layout in-call của SDK **không xử lý window insets** → 2 view ở đỉnh đè lên status bar/notch trên cả call CSKH lẫn call 1-1:
- `binding.ivFloatingWindow` (nút thu nhỏ, góc trên trái) — đè lên đồng hồ hệ thống;
- `binding.tvCountdown` (timer) — ngang hàng notch.

**Fix** trong `CsAudioOnTheCallFragment.applyTopSafeArea()` (áp cho MỌI cuộc gọi): đọc `status_bar_height` từ system resource (`getIdentifier("status_bar_height","dimen","android")`) rồi `setTranslationY(statusBarHeight)` cho 2 view trên.
- Dùng `translationY` **không** `setPadding` để không bóp méo icon; set tuyệt đối nên **idempotent** khi `renderUserInfo`/`toRenderView` chạy lại nhiều lần.
- Không set padding cho root/`clRoot` — sẽ inset luôn background full-screen (`ivBg`) tạo viền trống ở đỉnh.
- Helper chung: `CsCallUiUtils.pushBelowStatusBar(View)`.
- **Màn IN-CALL** (`CsAudioOnTheCallFragment`): đẩy `ivFloatingWindow` + `tvCountdown`.
- **Màn CALLER** (`CsAudioCallerFragment` extends `AudioCallerFragment`, đăng ký thêm `customCallFragmentByKey(AUDIO_CALLER=4, ...)`): đẩy `ivFloatingWindow` (caller **không có** `tvCountdown`) + ẩn nút video switch `ivCallSwitchType`/`tvCallSwitchTypeDesc`.
- **Màn CALLEE full-screen** (`AUDIO_CALLEE=5`): đã có `CsAudioCalleeFragment` từ 2026-07-29 (xem cập nhật ở #19) nhưng **không** áp safe-area — đọc `fragment_p2p_audio_callee.xml` thì layout không có view nào sát đỉnh (`flUserAvatar` marginTop 160dp, `tvSwitchTip` marginTop 80dp), khác màn in-call vốn có `ivFloatingWindow`/`tvCountdown` dính notch. Bổ sung `pushBelowStatusBar` nếu chạy thật thấy đè.
- Màn này chỉ hiện khi **thiếu quyền overlay** (có quyền → SDK dùng banner) → muốn test phải tắt quyền overlay của app.

## Login V10 = điều kiện tiên quyết (ĐÃ GIẢI QUYẾT)

- **V9 login vs signalling** (rủi ro #1 cũ) → **đã xác nhận đúng & fix**: giữ V9 login (`disableV2Login=true`) khiến
  V2 signalling fail `191001 misuse`. Đã migrate login sang **V10** (Android `initV2`+`V2NIMLoginService`;
  iOS `registerWithOptionV2 useV1Login=NO`+`v2LoginService`). Signalling hết lỗi. Chi tiết + mọi thay đổi kéo theo
  (appKey, currentAccount, autoLogin token): **`LOGIN_V9_TO_V10_MIGRATION.md`**.

## ⚠️ Config CallKit rtcAppKey — appKey phải khớp account (GAP cần verify khi test call)

- CallKit cần **rtcAppKey = đúng appKey của account** (tầng RTC/audio). App dùng **appKey động từ backend**;
  IM login đã xử lý (Android `updateAppKey`, iOS `updateAppKey`) nhưng **CallKit init đọc appKey riêng**:
  - **Android** `CallService.init()` đọc appKey từ **manifest metadata** `com.netease.nim.appKey` (gradle `APP_KEY`,
    hiện `2761e5...` — có thể **stale** so với appKey backend) → rtcAppKey có thể sai → RTC join/audio fail dù signalling OK.
    → **Cần**: truyền appKey runtime (giống login) vào `CallService.init`, hoặc re-init CallKit sau khi có appKey đúng.
  - **iOS** `setupCallKitWithAppKey:` nhận appKey từ param login (`ensureRegisterV2WithAppKey`) → dùng đúng appKey runtime.
- **Chưa verify** trên call P2P thật (signalling OK không đảm bảo RTC audio OK nếu rtcAppKey lệch).

## Rủi ro/mở khác

- Call P2P E2E Android↔iOS **2 máy thật** chưa nghiệm thu (RTC không chạy iOS simulator; emulator không có AEC).
- iOS `observeCallState` chưa forward sang JS (prebuilt UI vẫn chạy; JS không nhận trạng thái call chi tiết).
- Kickout/reconnect token dưới V10 — xem `LOGIN_V9_TO_V10_MIGRATION.md` mục Rủi ro.

## Sync repo ↔ node_modules (dev)

App cài lib qua `file:../react-native-netease-im` (yarn 1 copy, không live). Sau khi sửa lib phải đồng bộ node_modules.
⚠️ **`rsync` hay bị `rtk` proxy làm gián đoạn** (panic broken-pipe → copy thiếu file). Kinh nghiệm: **copy trực tiếp
từng file bằng `cp`** rồi **`grep -c` verify** giữa repo vs `node_modules`; đừng tin rsync đã copy đủ.
Đổi native → Android: rebuild. iOS: `pod install` **chỉ khi thêm file mới / đổi pod deps** (sửa file có sẵn thì
chỉ cần rebuild; Podfile pre_install + AppDelegate là của app, không bị ghi đè).
