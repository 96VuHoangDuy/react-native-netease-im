# NERTC Call Kit ↔ NIM SDK — Version Compatibility (bằng chứng version pin)

> Nguồn: official changelog `doc.yunxin.163.com/nertccallkit`. Crawl bằng Playwright ngày **2026-07-15**.
> Dùng để xác nhận combo version cho initiative `rn-im-call` (Phase 2 bump NIM, Phase 3 tích hợp Call Kit).

## Combo target của project (đã xác nhận hợp lệ chính thức)

> 🔄 **Cập nhật 2026-07-17** — bật 来电横幅 (call banner). Android buộc phải lên `4.3.0`; iOS thực tế đang chạy `4.8.1` (xem "Sai lệch iOS" bên dưới).

| Nền tảng | call-ui | NIM SDK | NERTC SDK | Nguồn changelog |
|---|---|---|---|---|
| **Android** | `4.3.0` (bump 2026-07-17, từ `4.1.0`) | **10.9.52** | 5.9.10 | [Android 更新日志](https://doc.yunxin.163.com/nertccallkit/concept/DMzOTI3NTA?platform=client) |
| **iOS** | `4.8.1` (thực tế — podspec **không pin**) | **10.9.53** | 5.9.10 | [iOS 更新日志](https://doc.yunxin.163.com/nertccallkit/concept/jIzNDA4Nzc?platform=client) |

### Vì sao Android chọn 4.3.0 (không phải 4.8.2 mới nhất)

`CallKitUI.enableIncomingBanner` **không tồn tại** ở 4.1.0 (verify bằng `javap` full method list trên aar 4.1.0) — có từ **4.3.0**. Chọn bản thấp nhất có API cần, vì dep matrix của nó **trùng khít** bản đang dùng → drop-in, zero drift:

| call-ui | nimlib (basesdk/avsignalling) | nertc-base | corekit-im2 |
|---|---|---|---|
| `4.1.0` (cũ) | 10.9.52 | 5.9.10 | 1.10.1 |
| **`4.3.0`** ← chọn | **10.9.52** | **5.9.10** | **1.10.1** |
| `4.7.5` / `4.8.2` | 10.9.81 | 5.9.10 | 1.12.0 |

> Verify bằng pom thật trên Maven Central (`call-<v>.pom`), 2026-07-17. 4.8.2 đòi NIM **10.9.81** → phải nâng cả IM stack vừa ổn định xong sau migrate V9→V10 ⇒ loại.
> Versions có trên mavenCentral: `…4.0.0, 4.1.0, 4.3.0, 4.7.0, 4.7.5, 4.8.0, 4.8.2` (latest = 4.8.2).

### ⚠️ Sai lệch iOS (có sẵn từ trước, không do change 2026-07-17)

`RNNeteaseIm.podspec` **không pin version** CallKit:
```ruby
s.dependency "NERtcCallKit/NOS_Special"      # ← không có version
s.dependency "NERtcCallUIKit/NOS_Special"    # ← không có version
```
→ `pod install` resolve ra **4.8.1** (`Podfile.lock`), trong khi doc này trước đây ghi target iOS là `4.1.0`. Theo bảng changelog iOS bên dưới, `4.8.1` kỳ vọng **NIM 10.9.76** nhưng project pin `NIMSDK_LITE 10.9.53` ⇒ **đang có version skew trên iOS**.

Hệ quả tình cờ: iOS *sẵn* có API banner + LiveCommunicationKit (vì trôi lên 4.8.1), còn Android pin cứng 4.1.0 nên không có gì → đó là lý do 2 platform lệch nhau.

**Chưa xử lý (chờ owner quyết):** pin `NERtcCallKit`/`NERtcCallUIKit` = `4.8.1` để `pod install` không tự trôi tiếp. **Không** hạ iOS xuống 4.3.0 cho đối xứng: chưa verify `reportIncomingCallWithParam` (LiveCommunicationKit) có ở 4.3.0 hay không — nhiều khả năng là API mới hơn, hạ sẽ mất tính năng.

- Lệch patch-level `.52` (Android) vs `.53` (iOS) là **do NetEase quy định**, không phải lỗi cấu hình → không đồng bộ được ở patch-level, chỉ đồng bộ major **V10**.
- Artifact đã verify tồn tại (2026-07-15):
  - Android Maven Central: `com.netease.nimlib:{basesdk,push,lucene}:10.9.52` → HTTP 200.
  - iOS CocoaPods trunk: `NIMSDK 10.9.53` (dải 10.9.50–10.9.56 có sẵn).
- iOS `NIMSDK 10.9.53` pod **vẫn ship `NIMAVChat.xcframework`** (subspec NOS/FCS/BASE) → code legacy net-call display hiện tại không vỡ compile.

## Bối cảnh version (Android changelog trích yếu)

| call-ui | NIM SDK | NERTC | Ghi chú |
|---|---|---|---|
| 4.8.0 | 10.9.81 | 5.9.10 | |
| **4.3.0** | **10.9.52** | **5.9.10** | ← **target Android (từ 2026-07-17)**. thêm `enableIncomingBanner` |
| 4.1.0 | 10.9.52 | 5.9.10 | target cũ. AI 字幕, `getUserWithRtcUid`. **Không có** `enableIncomingBanner` |
| 3.8.0 | 10.9.52 | 5.9.10 | xoá `setPushConfigProviderForGroup`, `NECallParam.rtcUid` |
| 3.3.0 | 10.6.0 | 5.6.50 | adapt targetSdk 34 |
| 3.0.0 | 10.5.0 | 5.5.40 | nâng IM lên V10 |

## Bối cảnh version (iOS changelog trích yếu)

| call-ui | NIM SDK | NERTC | Ghi chú |
|---|---|---|---|
| **4.8.1** | **10.9.76** | **5.9.10** | ← **thực tế đang cài** (podspec không pin). Skew: project dùng NIM 10.9.53 |
| 4.3.0 | 10.9.53 | 5.9.10 | thêm `enableIncomingBanner` |
| 4.1.0 | 10.9.53 | 5.9.10 | target ghi trong doc cũ. AI 字幕, `getUserWithRtcUid` |
| 3.8.0 | 10.9.53 | 5.9.10 | |
| 3.3.0 | 10.6.0 | 5.6.50 | hỗ trợ XCFramework |
| 3.0.0 | 10.5.0 | 5.5.40 | nâng IM lên V10 |

## Yêu cầu toolchain (từ changelog)

- **Android**: `compileSdkVersion ≥ 31` (bắt buộc từ call-ui 1.8.0); call-ui 3.3.0 adapt `targetSdk 34`; API level 21+.
- **iOS**: NIMSDK 10.9.53 podspec khai báo `platforms.ios = 9.0`; NERTC/Call Kit thực tế cần cao hơn — align deployment target ≥ 12.

## Lưu ý khi pin (Option B — project tự tích hợp NIM)

Khi tới Phase 3 tích hợp call-ui, phải `exclude` NIM/NERTC bundled trong call-ui rồi tự khai báo version của project, và **giữ mọi NIM sub-package cùng version** (`basesdk`/`push`/`lucene` = 10.9.52). Chi tiết: `docs/call-android-native/01-integration-android.md`, `docs/call-ios-native/01-integration-ios.md`.
