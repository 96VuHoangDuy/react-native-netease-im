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
