# NIM Login V9 → V10 Migration — Why & All Changes

> Tài liệu tổng hợp: **vì sao phải migrate login NetEase NIM từ V9 sang V10**, và **mọi thay đổi + lý do từng thay đổi** (2 repo: `react-native-netease-im` lib + `pyeon-chinese-mobile` app; cả iOS + Android).
> Cập nhật: 2026-07-16. Trạng thái: login/chat/gửi-nhận/hiển thị iOS+Android **đã chạy**. Còn dọn log diagnostic (mục 9).
> Liên quan: `CALLKIT_INTEGRATION_NOTES.md` (tích hợp CallKit native), `CALLKIT_VERSION_COMPAT.md` (version pin).

---

## 1. Vì sao phải migrate V9 → V10

| | Chi tiết |
|---|---|
| **Mục tiêu** | Tính năng **Voice Call** (Client → CSR) qua **NERTC Call Kit 4.1.0**. |
| **Ràng buộc SDK** | Call Kit 4.1.0 mời/nhận cuộc gọi bằng **V2 signalling** (`V2NIMSignallingCallParams`). V2 signalling chỉ hoạt động khi IM ở **trạng thái login V10** (`V2NIMLoginService`). |
| **Hiện trạng cũ** | App là fork của **V9 UIKit demo**: login bằng **V9** (`AuthService.login` Android / `registerWithAppID`+`loginManager` iOS). Phase 2 giữ V9 bằng cờ `SDKOptions.disableV2Login = true`. |
| **Triệu chứng** | Bấm gọi → `NimSignallingWrapper: call failed code = 191001, msg = misuse` → cuộc gọi không tới peer, kéo theo crash `ForegroundServiceDidNotStartInTimeException`. |
| **Fact chốt (docs)** | `disableV2Login=true` = **tắt login V10**, buộc dùng login V9. **V9 và V10 login API loại trừ lẫn nhau — chỉ chọn một** (`reference/android/initialization.md:42`). Official CallKit path = `initV2` + `V2NIMLoginService.login`. |
| **Kết luận** | Không có combo "giữ V9 login + bật V2 signalling". **Bắt buộc chuyển login sang V10** → kéo theo hàng loạt thay đổi phụ (mục 2-7) vì cả fork phụ thuộc trạng thái/API login V9. |

---

## 2. (A) Init + Login + Logout API → V10

**Vấn đề:** V9 dùng `NIMClient.init(ctx, loginInfo, options)` + `AuthService.login`. V10 signalling cần init/login V10.

### Android — `react-native-netease-im/android/.../im/`
- `IMApplication.java`: `NIMClient.init(...)` → **`NIMClient.initV2(ctx, options)`** (initV2 **không** nhận LoginInfo); xóa `options.disableV2Login=true` → **`= false`**.
- `login/LoginService.java`: `AuthService.login(LoginInfo)` → **`V2NIMLoginService.login(accountId, token, option, success, failure)`**; `logout` → `V2NIMLoginService.logout`.
  - `option.setAuthType(V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN)` + `setTokenProvider(...)` (giữ dynamic-token như V9 `authType=1`).
  - Callback V2 trả `Void` (không có LoginInfo) → dùng `accountId` do caller truyền.
- **Vì sao:** không có `configV2` → nhánh pre-policy giữ `NIMClient.config` (V9, chỉ stub, không connect).

### iOS — `react-native-netease-im/ios/RNNeteaseIm/RNNeteaseIm/RNNeteaseIm.m`
- `registerWithAppID:cerName:` → **`registerWithOptionV2:option v2Option:`** với `V2NIMSDKOption.useV1Login = NO` (đối xứng `disableV2Login=false`).
- `loginManager login:` / `autoLogin:` → **`v2LoginService login:token:option:success:failure:`** (V2 iOS **không có** autoLogin riêng → autoLogin dùng luôn login); `logout` → `v2LoginService logout:failure:`.
- Token: class `RNNIMTokenProvider` (`getToken:` trả token string) gán vào `V2NIMLoginOption.tokenProvider`, `authType = V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN`.

**API đã verify từ SDK thật** (javap AAR 10.9.52 / NIMSDK_LITE 10.9.53 headers) — không đoán.

---

## 3. (B) appKey — dynamic backend vs init khóa sớm

**Vấn đề gốc:** App lấy **appKey động từ backend** (`response.data.app_key`) và V9 truyền appKey **per-login** (`LoginInfoBuilder.withAppKey` / `registerWithAppID:appKey`). Nhưng **V10 khóa appKey lúc init/register** (initV2 đọc manifest; register 1 lần). Manifest/hardcode để appKey **stale** (`2761e5922c9e63c49ef6f33d0a367ec6`) ≠ appKey backend → login báo **`102404 account not exist`** (account chỉ tồn tại trong namespace của appKey đúng).

### Android
- `initV2` đọc appKey từ manifest metadata `com.netease.nim.appKey` (gradle `APP_KEY=2761e5...`, stale).
- **Fix:** `RNNeteaseImModule.java` — helper `syncAppKey(appKey)` gọi **`NIMClient.updateAppKey(appKey)`** trước mỗi login nếu khác appKey SDK (no-op nếu trùng). `updateAppKey` chỉ chạy khi **chưa login** → đặt trước `LoginService.login`.

### iOS
- `AppDelegate.swift setupNIMSDK()` register **lúc launch** với appKey stale `2761e5` (V9 `register(with:)`). NIM **first-register-wins** → `registerWithOptionV2` của lib lúc login **bị bỏ qua** → bằng chứng: log Artemis `app_key=2761e5...`.
- **Fix 2 chỗ:**
  1. `pyeon-chinese-mobile/ios/ZYZJ/AppDelegate.swift`: `register(with:)` → **`register(withOptionV2:option v2Option:)`** với `V2NIMSDKOption.useV1Login=false` (bật V2 mode ngay từ launch — register này là cái "thắng").
  2. `RNNeteaseIm.m ensureRegisterV2WithAppKey`: bỏ re-register (bị ignore), thay bằng **`[[NIMSDK sharedSDK] updateAppKey:appKey]`** lúc login (đổi appKey khi chưa login; `code 200` = OK).

**Vì sao:** account chỉ "tồn tại" dưới đúng appKey namespace; V10 không cho truyền appKey per-login nên phải `updateAppKey` runtime (env-agnostic — dev/prod tự đúng theo appKey backend trả).

---

## 4. (C) currentAccount iOS nil dưới login V10

**Vấn đề:** iOS đọc `[NIMSDK sharedSDK].loginManager.currentAccount` (V9) ở **29 chỗ / 9 file** (isMe / isOutgoing / session owner). Dưới login V10 API này trả **nil** →
- **Crash gửi tin:** `NIMMessageMaker.m` dựng `@{@"sessionId": strSessionID}` với `strSessionID = currentAccount = nil` → `-[__NSPlaceholderDictionary ...: attempt to insert nil object from objects[0]`.
- Sai self-identity khắp UI chat.

**Fix:** category `NIMSDK+ZYZJ.{h,m}` — `- (NSString *)zyzjCurrentAccount` = `[self.v2LoginService getLoginUser]` (fallback V9 currentAccount). Thay **29 call-site** ở 9 file (`NIMMessageMaker.m`, `ConversationViewController.m`, `ContactViewController.m`, `NIMViewController.m`, `TeamViewController.m`, `NIMKitUtil.m`, `NIMKitDataProviderImpl.m`, `ChatroomViewController.m`, `NTESGroupedDataCollection.m`).

**Vì sao Android không dính:** Android tự lưu account trong `LoginService.account` (JS truyền vào), không đọc thẳng `currentAccount` của SDK.

---

## 5. (D) Login observers — V9 vs V2

**Lo ngại ban đầu:** các observer V9 (`AuthServiceObserver.observeOnlineStatus`/`observeLoginSyncDataStatus` Android; `NIMLoginManagerDelegate` iOS) có thể ngừng fire dưới login V10 → session list/kickout hỏng.

**Kết quả thực tế (verify qua log `MSG_TRACE`):** `sessionInStore=true`, `storeKeys` có session → **message/recent-contact observer VẪN chạy tốt dưới login V10** (session + message store populate bình thường). Tức luồng chat core **không bị** ảnh hưởng bởi login-exclusivity (chỉ login API là exclusive, không phải msg/session service).

**Còn mở:** **auth observer cho kickout/online-status** (multi-device) chưa verify dưới V10 → **Stage 2 deferred** (thêm `V2NIMLoginService.addLoginListener/addLoginDetailListener` nếu cần). Android hiện có **V2 probe listener** trong `LoginService.java` (`addLoginDetailListener` + re-query) — **thuần diagnostic, nên GỠ** (store populate tốt không cần nó).

---

## 6. (E) autoLogin `102302 invalid token` (JS)

**Vấn đề:** kill app + mở lại → autoLogin fail `102302 invalid token`; login thủ công (logout rồi login) thì OK.
- `SessionStore.authenticate()` đọc `imToken` **cache trong MMKV** từ phiên trước → `handleLoginIM(data)` → `autoLogin({token: data.imToken})` — **không refetch**.
- Login thủ công luôn lấy `imToken` **mới** từ `SessionAPI.login()`.
- `imToken` là **dynamic token** (ngắn hạn, client không track TTL) → kill app lâu → hết hạn.
- V9 cũ dùng `loginManager autoLogin:(NIMAutoLoginData)` (cơ chế autoLogin riêng, khoan dung); **V10 không có autoLogin** → cả autoLogin lẫn login đều `v2LoginService.login` full-auth → token cũ bị reject.

**Fix (JS):** `pyeon-chinese-mobile/src/data/session/SessionStore.ts` — trước `handleLoginIM(data)`, refetch bộ **token/appKey/accid mới** qua `SessionAPI.getImToken()` (matched set), cập nhật `data.imToken/appKey/imAccid`. Giống manual login luôn có token mới.

---

## 7. (F) List message rỗng iOS — `estimatedListSize` (KHÔNG do migration)

**Vấn đề:** iOS vào chat, tin fetch về JS đủ (`listMessagesLen=8`) + store đúng key, nhưng **FlashList rỗng**. Android OK.
**Root cause:** prop **chỉ-iOS** `estimatedListSize={Platform.isIos ? estimatedListSize : undefined}` (`ChatDetail.Messages.tsx`) → FlashList render vào vùng size cố định `{windowWidth, Screen.height-200}` lệch container thật → list trống. **Regression từ nâng RN/FlashList ở Phase 1**, KHÔNG liên quan login V9→V10.
**Fix:** bỏ hẳn prop + const orphan → FlashList tự đo container (Android vốn đã `undefined` + chạy OK).
**Ghi ở đây** vì lộ ra trong lúc test iOS chat sau migration (dễ nhầm là do migration).

---

## 8. (G) Nền tảng Phase 2 (đã làm trước, tóm tắt — chi tiết ở `CALLKIT_INTEGRATION_NOTES.md`)

- **iOS `NIMSDK_LITE`:** NERtcCallKit bắt buộc `NIMSDK_LITE` (không phải NIMSDK full) → podspec đổi sang LITE. LITE **thiếu `NIMAVChat`** → gỡ code legacy net-call ở 5 file (ImConfig.h, NTESBundleSetting, NIMKitUtil.m, ConversationViewController.m, NIMViewController.m). Thêm `#import <AVFoundation/AVFoundation.h>` ở NIMMessageMaker.m.
- **Android:** `com.netease.nimlib:{basesdk,push,lucene,avsignalling}:10.9.52` + `call-ui:4.1.0` (exclude nimlib + nertc-base) + `nertc:5.9.10`. `avsignalling` phải khai lại (call-ui exclude nimlib) nếu không `NoClassDefFoundError V2NIMSignallingChannelType`. ABI NERTC không có x86_64 → emulator arm64.

---

## Token model (quan trọng)
- `imToken` = **dynamic token** (server sinh theo AppKey+AppSecret+accountId, ngắn hạn). Client **không** biết TTL, chỉ lưu string.
- `tokenProvider` (cả 2 platform) = **static wrapper** trả string đã cache → **không tự refresh**. Trách nhiệm lấy token mới nằm ở JS (mục 6).
- **Hệ quả reconnect:** mất mạng lâu → SDK re-auth qua tokenProvider (string cũ) có thể lại `invalid token`. Chưa xử lý — xem Rủi ro.

## Rollback
Cả 2 platform gate bằng **1 cờ**, code V9 giữ dạng **comment** tại chỗ:
- Android: `disableV2Login=false→true` + `initV2→init` + `V2NIMLoginService.login/logout→AuthService`.
- iOS: `useV1Login=NO→YES` + `registerWithOptionV2→registerWithAppID` + `v2LoginService→loginManager`.

## Verification (đã pass)
Login thủ công + autoLogin (sau fix token) · gửi/nhận tin 1-1 · **list message hiển thị** (sau fix estimatedListSize) · session list populate · voice call signalling hết `191001`. **Cần test thêm:** kickout đa thiết bị; call P2P Android↔iOS máy thật.

## Rủi ro / còn mở
- **Kickout/online-status** dưới V10 (auth observer) chưa verify → Stage 2 nếu cần.
- **Reconnect token** hết hạn (tokenProvider static) → có thể cần cơ chế refresh.
- **appKey prod** ≠ dev: đã xử lý runtime (`updateAppKey` theo appKey backend) nên tự đúng; không hardcode.
- **Log diagnostic** cần dọn trước production: iOS `IMTRACE_IOS`/`MSG_TRACE_NATIVE`; Android `IMTRACE` (LoginService/RecentContactObserver/IMApplication/RNNeteaseImModule) + **gỡ V2 probe listener**. Fix (`updateAppKey`/`syncAppKey`/`zyzjCurrentAccount`) GIỮ, chỉ bỏ dòng log.

## Danh sách file đã đổi
**Lib `react-native-netease-im`:** `android/build.gradle`, `android/src/main/AndroidManifest.xml`, `.../im/IMApplication.java`, `.../im/login/LoginService.java`, `.../im/login/RecentContactObserver.java`, `.../im/RNNeteaseImModule.java`, `.../im/CallService.java`, `ios/.../RNNeteaseIm.m`, `ios/.../NIMSDK+ZYZJ.{h,m}` (mới), + 9 file iOS thay `currentAccount`, + 5 file gỡ NIMAVChat, `RNNeteaseIm.podspec`, `index.ts` + `src/Call/*`.
**App `pyeon-chinese-mobile`:** `ios/ZYZJ/AppDelegate.swift`, `android/gradle.properties` (NIMVersion), `src/data/session/SessionStore.ts`, `src/screens/Chat/ChatDetail/ChatDetailMessges/ChatDetail.Messages.tsx`, `package.json` (file: local dep).
