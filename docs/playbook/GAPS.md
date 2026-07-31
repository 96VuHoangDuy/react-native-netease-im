# Gaps

## Packaging và Verification

### `dist/` chưa có nhưng `package.json` trỏ type tới `dist/index.d.ts`

- Evidence:
  - `package.json` có `"types": "dist/index.d.ts"`
  - repo hiện không có thư mục `dist/`
- Impact:
  - repo source không tự chứng minh artifact publish hiện tại

### Không có build/test pipeline thực tế

- Evidence:
  - `package.json` chỉ có `npm test` placeholder
  - không có CI config trong repo
- Impact:
  - khó verify thay đổi ngoài đọc source và check cục bộ

### `tsconfig.json` không cover toàn bộ public entry

- Evidence:
  - `include` chỉ là `./src/**/*`
  - `index.ts` và `Utils.ts` nằm ngoài `src/`
- Impact:
  - `npx tsc --noEmit` không validate hết bề mặt public

## Native Dependency Drift

### iOS dùng `react-native-config` nhưng package metadata không khai báo

- Evidence:
  - source iOS import `react-native-config/RNCConfig.h`
  - `package.json` và `RNNeteaseIm.podspec` không thấy dependency này
- Impact:
  - consumer app có thể build lỗi nếu không tự kéo dependency

### NIM SDK version lệch giữa Android và iOS

- Evidence:
  - Android: `9.12.2`
  - iOS: `9.12.1`
- Impact:
  - risk behavior drift hoặc bug fix không đồng bộ

### iOS deployment target lệch giữa podspec và Xcode project

- Evidence:
  - podspec: `12.0`
  - project.pbxproj: `8.0`
- Impact:
  - dễ gây hiểu nhầm khi build local hoặc maintenance native project

## Integration Drift

### README cũ lệch với source Android hiện tại

- Evidence:
  - README cũ mô tả `IMApplication.init(..., config)`
  - source hiện tại chỉ có `IMApplication.init(context, mainActivityClass, notifyIcon)`
- Impact:
  - app host dễ bootstrap sai

### Android custom backend config chưa có public bridge

- Evidence:
  - `CacheUsers.setApiUrl(...)`, `CacheUsers.setAuthKey(...)`
  - không thấy JS public API hoặc `@ReactMethod` tương ứng
- Impact:
  - custom user cache flow Android khó cấu hình từ consumer app

### Android anti-spam config chưa có public bridge

- Evidence:
  - `SessionService.setBusinessId(...)` tồn tại
  - không thấy method public tương ứng trên bridge
- Impact:
  - iOS và Android không đối xứng ở `IM_BUSINESS_ID` / anti-spam config

### `RNAppCacheUtilModule` tồn tại nhưng không được register

- Evidence:
  - `RNNeteaseImPackage.java` comment out `new RNAppCacheUtilModule(reactContext)`
- Impact:
  - module này thực tế không dùng được từ JS

## Event Contract Drift

### JS enum không mô tả đủ các event native đang emit

- Evidence:
  - native emit thêm `observeLaunchPushEvent`, `observeBackgroundPushEvent`, `observeAccountNotice`, `observePhotoLibraryDidChange`, `observeStartSend`, `observeEndSend`, `observeReceipt`, `observeDownloadVideoNotice`
  - `src/utils/eventListener.type.ts` không chứa đầy đủ các event này
- Impact:
  - consumer app hoặc maintainer có thể bỏ sót event contract quan trọng

### `observeAttachmentProgress` là dead event trong JS enum

- Evidence:
  - `src/utils/eventListener.type.ts:13` khai báo `observeAttachmentProgress`
  - không Android (`ReactCache`) hay iOS (`ConversationViewController`/`RNNeteaseIm`) nào emit tên event này; event thật cho progress là `observeProgressSend`
- Impact:
  - `addListener("observeAttachmentProgress", ...)` không bao giờ được gọi; nếu FE dựa vào đây để show progress/refetch thì logic chết

## Media / Attachment Drift

### Android mất `url`/`fileUrl` khi `isFilePathDeleted` (image/file/audio), video thì không

- Evidence:
  - `generateImageExtend` (`ReactCache.java:2495-2530`), `generateFileExtend`, `generateRecordExtend` chỉ set `url`/`fileUrl`/`displayName`/kích thước bên trong `if (!isFilePathDeleted)`
  - `generateVideoExtend` (`ReactCache.java:2343`) set `url` vô điều kiện
  - iOS `makeExtend*` set `url`/`coverUrl` vô điều kiện đầu hàm
- Impact:
  - khi cache local bị dọn mà `isReplacePathSuccess=true`, dict image/file/audio trên Android không còn URL để FE tải lại → ảnh hiển thị đen/vỡ; bất đối xứng với iOS và với video
  - chưa reproduce runtime kịch bản "xoá cache → mở lại conversation có media cũ"

### iOS custom download không set trạng thái failed rõ ràng

- Evidence:
  - `moveFiletoSessionDir:` (`ConversationViewController.m:1269-1380`) tự HTTP download, không qua SDK queue; nhánh lỗi network không set `downloadAttStatus=failed`
  - label `type` của progress event gán `"upload"` cho cả nhánh download (`:1365-1375`)
- Impact:
  - media có thể kẹt trạng thái "downloading"; consumer không phân biệt được upload vs download qua `type`

## Configuration Risk

### Android push config có giá trị hard-coded trong source

- Evidence:
  - `IMApplication.getOptions(...)` set trực tiếp nhiều giá trị `xmAppId`, `xmAppKey`, `mzAppId`, `mzAppKey`, `fcmCertificateName`
- Impact:
  - behavior release phụ thuộc source library, khó tái sử dụng cho app host khác nếu không sửa code

## Missing Repo Context

### Không có consumer app, release process, branch rule hoặc owner note trong repo

- Impact:
  - chưa thể xác thực end-to-end integration
  - chưa thể mô tả workflow release/PR bằng dữ liệu chắc chắn

## Build / Obfuscation Risk

### Docs không nhắc cơ chế R8 phá NERTC/call-ui (reflection + JNI FindClass)

- Evidence:
  - `docs/reference/call-android-native/01-integration-android.md` §4 ProGuard **có** rule official
    (`-keep class com.netease.lava.** {*;}`, `-keep class com.netease.yunxin.** {*;}`)
  - nhưng không docs nào giải thích **vì sao** cần: không hề nhắc `JNI_OnLoad`, `FindClass`,
    `RegisterNatives`, `NativeLibLoader`, `NERtcCore`, hay cơ chế "xkit startup" (`Class.forName()`
    từ manifest `<meta-data>`)
  - mọi aar NetEase (`nertc`, `nertc-base`, `call-ui`, `corekit`) có `proguard.txt` **rỗng** → không tự bảo vệ
- Impact:
  - thiếu rule ⇒ app release crash, **debug không lộ** (`minifyEnabled` chỉ bật ở release):
    strip class ⇒ `ClassNotFoundException: CallKitUIService` lúc launch;
    rename class ⇒ `SIGABRT` tại `libnertc_sdk.so (JNI_OnLoad+148)` lúc bấm call
  - suy rule từ bytecode dễ ra rule thiếu: `NERtcCore`/`NativeLibLoader` **không khai method `native`**
    nên "chỉ keep class có native method" là không đủ — phải keep cả package
  - **Bài học**: đọc `01-integration-android.md` §4 ProGuard TRƯỚC khi tự suy rule

### Lib chưa export rule NIM qua consumerProguardFiles

- Evidence:
  - `android/consumer-rules.pro` chỉ export `com.netease.lava.**` + `com.netease.yunxin.**`
  - rule NIM (`com.netease.nim.**`, `nimlib.**`, `share.**`, `mobsec.**`) hiện chỉ nằm ở
    `android/app/proguard-rules.pro` của app ZYZJ
  - `android/proguard-rules.pro` của lib có `-keep class com.netease.** {*;}` nhưng khai trong
    `proguardFiles` (chỉ áp khi tự build lib), **không** merge sang app
- Impact:
  - app host khác dùng lib mà không tự thêm rule NIM ⇒ có thể crash release; ZYZJ hiện không lộ vì đã có sẵn

## Call — Inbound Ownership

### Không xác định được client nào khởi tạo cuộc gọi CSR→user

- Evidence:
  - repo này chỉ khởi tạo chiều **user→CSR**: `CallService.startVoiceCall(...)`
    (`android/src/main/java/com/netease/im/CallService.java`), gọi qua `src/Call/Call.ts` →
    `startVoiceCall` ở app ZYZJ
  - `pyeon-chinese-portal`: grep `nim|yunxin|netease|nertc` trong `src/` và `package.json` = **0 kết quả**
    → portal không có tích hợp NIM/NERTC nào
  - nhưng cuộc gọi CSR→user có thật: đã reproduce trên máy Xiaomi MIX 2S (log
    `DefaultIncomingCallEx.onIncomingCall`), tức tồn tại một client ngoài 4 repo của workspace
- Impact:
  - 🔬 `NECallPushConfig` (title/content/`pushPayload` của offline push) do **bên gọi** set, không phải bên nhận
  - ⇒ notification mà khách hàng thấy khi app **bị kill** ở luồng CSR→user nằm ngoài tầm kiểm soát của repo này
  - ⇒ mọi thiết kế cho trạng thái app-killed (ringtone channel riêng, data message, full-screen intent)
    đều **chặn ở đây** cho tới khi biết client đó là gì và ai sở hữu nó
  - `incomingCallEx` / `notificationConfigFetcher` không lấp được khoảng này: chúng chỉ chạy khi process còn sống
- Cần hỏi team TQ:
  1. CSR dùng client nào để gọi ra (console Yunxin, app riêng, hay web tự viết)?
  2. Client đó set `NECallPushConfig` như thế nào — có `pushPayload` không?
  3. Ai sở hữu/deploy client đó?
- Liên quan: `docs/reference/call-android-native/03-advanced.md` §Intercept inbound ·
  phân tích đầy đủ ở `pyeon-chinese-mobile/docs/ai-output/call-notification-status-and-proposal-en-2026-07-30.md`
