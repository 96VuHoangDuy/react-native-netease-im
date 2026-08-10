# Native Integration

## Mục tiêu

File này ghi lại những gì app host cần làm để tích hợp thư viện này, dựa trên source hiện tại của repo.

## Inputs Mà App Host Phải Có

- NetEase NIM `appKey`
- token đăng nhập cho từng account
- cấu hình push/certificate phía app host
- nếu dùng iOS custom env path:
  - `IM_CER_NAME`
  - `API_URL`
  - `API_AUTH_KEY`
  - `IM_BUSINESS_ID`

## Android

### 1. Init SDK trong `Application`

Source hiện tại định nghĩa:

```java
IMApplication.init(context, mainActivityClass, notifySmallIconRes)
```

Quan trọng:

- README cũ còn ghi overload 4 tham số với `ImPushConfig`, nhưng source hiện tại chỉ còn overload 3 tham số.
- `IMApplication` sẽ init NIM SDK, pinyin, cache, storage, log và revoke observer.

### 2. Manifest và metadata

Library manifest đã đóng gói:

- nhiều `uses-permission`
- NIM services/receivers/providers
- Mi Push permission placeholder

Host app vẫn phải tự bổ sung ít nhất:

- NIM app key metadata, ví dụ:

```xml
<meta-data
    android:name="com.netease.nim.appKey"
    android:value="${NIM_KEY}" />
```

Ghi chú:

- metadata `com.netease.nim.appKey` không có sẵn trong library manifest.
- README cũ còn nhắc permission `${applicationId}.permission.RECEIVE_MSG`, nhưng library manifest hiện tại chỉ khai báo `${applicationId}.permission.MIPUSH_RECEIVE`.

### 3. Push open intent

#### App bị cold start

Host activity cần set launch intent để JS đọc lại được:

```java
if (ReceiverMsgParser.checkOpen(getIntent())) {
    RNNeteaseImModule.launch = getIntent();
}
```

`NimSession.getLaunch()` sẽ đọc biến này ở Android.

#### App đang background / foreground

`RNNeteaseImModule.onNewIntent(...)` đã xử lý:

- check push intent
- emit `observeBackgroundPushEvent`

Điều kiện là host activity phải nhận được `Intent` mới đúng chuẩn Android.

### 4. Runtime permission callback

README cũ chỉ ra việc forward `onRequestPermissionsResult(...)` sang `MPermission`. Source Android vẫn dùng `MPermission`, nên consumer app nên giữ bước này nếu có flow media/audio.

### 5. Android-specific observed gaps

- `IMApplication.getOptions(...)` đang chứa hard-coded push config values.
- `CacheUsers.setApiUrl(...)` và `setAuthKey(...)` tồn tại nhưng chưa được expose qua JS bridge.
- `SessionService.setBusinessId(...)` tồn tại nhưng chưa được expose qua JS bridge.

## iOS

### 1. Pod integration

Podspec của library kéo:

- `React-Core`
- `NIMSDK 9.12.1`
- `Reachability`

Source iOS còn import `react-native-config/RNCConfig.h`, vì vậy app host hoặc workspace build phải cung cấp dependency này nếu muốn build thành công.

### 2. SDK registration

> ⚠️ **ĐÃ ĐỔI (login V9→V10, 2026-07-16).** Xem `docs/reference/netease-im/LOGIN_V9_TO_V10_MIGRATION.md`.

Register V2 (login V10) đặt ở `AppDelegate.swift` (app) lúc launch + đổi appKey runtime lúc login:

```objc
// AppDelegate: register 1 lần, V2 mode (bắt buộc cho V2 signalling của CallKit)
[[NIMSDK sharedSDK] registerWithOptionV2:option v2Option:v2Option];  // v2Option.useV1Login = NO
// RNNeteaseIm.m (login/autoLogin): đổi appKey backend runtime (first-register-wins nên không re-register)
[[NIMSDK sharedSDK] updateAppKey:appKey];
[[NIMSDK sharedSDK].v2LoginService login:account token:token option:option success:... failure:...];
```

*(Cũ V9 — rollback: `[[NIMSDK sharedSDK] registerWithAppID:appKey cerName:cerName]` + `loginManager login:`, giữ dạng comment trong code.)*

`cerName`/appKey vẫn dùng ở cả `login(...)` và `autoLogin(...)`.

`cerName` được lấy từ:

- `IM_CER_NAME`
- fallback mặc định `"ZYZJIM"` nếu env không có

### 3. AppDelegate responsibilities

Source iOS bridge đang lắng nghe notification tên:

- `ObservePushNotification`

Vì vậy app host phải tự forward push tap vào `NSNotificationCenter`, như README cũ minh họa.

Ngoài ra app host phải tự xử lý:

- đăng ký APNs
- update device token cho NIM SDK
- push callback khi app launch/background
- nếu muốn dùng `NTESSDKConfigDelegate`, set delegate cho `NIMSDKConfig`

### 4. Extra env-based behavior

#### `IM_CER_NAME`

- dùng trong `RNNeteaseIm.m`

#### `API_URL`, `API_AUTH_KEY`

- dùng trong `CacheUsers.m`
- phục vụ fetch user info từ backend riêng ngoài NIM SDK

#### `IM_BUSINESS_ID`

- dùng trong `Util/NIMMessageMaker.m`
- set anti-spam option cho message iOS

### 5. iOS-specific observed gaps

- Podspec không khai báo `react-native-config`.
- Xcode project hiện còn target iOS `8.0`, trong khi podspec ghi `12.0`.
- iOS có env-based config cho anti-spam và user cache; Android chưa expose cấu hình tương đương ra JS public API.

## Shared Push / Event Notes

- Push open event:
  - `observeLaunchPushEvent`
  - `observeBackgroundPushEvent`
- Photo/media library observer event:
  - `observePhotoLibraryDidChange`
- Các event trên hiện không nằm đầy đủ trong enum JS type.

## Khi Nào Phải Cập Nhật File Này

- thay đổi AppDelegate/Manifest requirement
- thêm env var mới
- đổi cách init NIM SDK
- đổi flow push open / APNs / activity intent
- đổi dependency native mà consumer app phải tự cung cấp
