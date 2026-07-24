# 06 — 来电横幅 / Call banner (OFFICIAL RAW — Android)

> **Nguồn:** docs chính chủ NetEase Yunxin (NERTC CallKit — 来电横幅, Android). Lưu verbatim để tra cứu nhanh.
> Bản chắt lọc + call site thật + cách xin quyền overlay của project: [`03-advanced.md` §Call banner](./03-advanced.md#call-banner).
> Bản iOS: [`../call-ios-native/06-incoming-banner.official-raw.md`](../call-ios-native/06-incoming-banner.official-raw.md).
> ⚠️ **Cần call-ui ≥ 4.3.0** — `CallKitUI` ở 4.1.0 không có `enableIncomingBanner`.

---

Since v4.3.0, NetEase Cloud Voice and Video Call Components have supported the call banner function. When receiving a call, a lightweight banner is displayed at the top of the screen, which supports one-click answering or rejection without interrupting the user's current operation.

## Overview

The audio and video call component (CallKit) turns off the call banner function by default (consistent with the original full-screen call interface behavior). If you turn on this function, the top banner will be displayed when you receive a call, and the original full-screen call page will no longer appear.

- This function supports switching at any time during operation and takes effect on the **next call** (not effective for the currently displayed banner).
- If there is a second call during the banner display, the SDK will automatically reply to the busy line.
- During the banner display, the ringtone is played normally; when clicking the main body of the banner to enter the full-screen incoming call page, the ringtone **is not interrupted**.

## Effect display

![image-netease](https://yx-web-nosdn.netease.im/common/c9362efa9b53f8b1a395bd0f0e62c8b5/横幅.png)

## Permission configuration

The banner on the Android side is implemented using the system floating window (`SYSTEM_ALERT_WINDOW`), and you need to apply for permissions on **Android 6.0+** devices.

If the Android terminal does not have floating window permission, the SDK will automatically jump to the system settings page (`ACTION_MANAGE_OVERLAY_PERMISSION`) to guide the user to authorize and return the error code through callback to prompt the business layer.

```kotlin
// 检查权限
if (!Settings.canDrawOverlays(context)) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${packageName}")
    )
    startActivity(intent)
}
```

## Enable the call banner function

When the cloud audio video component receives an incoming call, it will display a lightweight banner at the top of the screen, which supports one-click answering or rejection without interrupting the user's current operation.

如果需要启用该功能，可以使用 `enableIncomingBanner` 方法，在 NERtcCallKit 组件初始化后开启该功能：

```kotlin
// 开启
CallKitUI.enableIncomingBanner(true)

// 关闭（恢复全屏来电界面）
CallKitUI.enableIncomingBanner(false)
```

`enableIncomingBanner` It is a **runtime method** and cannot be passed through `CallKitUIOptions.Builder`. The attribute of the same name in Builder is an internal parameter and is not used as a public API.

## Frequently asked questions

**Q: After the banner is turned on, will the original full-screen call interface still appear?**

No way. After turning on the banner mode, the full-screen incoming call interface is completely replaced by the banner display. If you need to restore, just call `enableIncomingBanner(false)`.

**Q: How to deal with receiving a second call during the banner display?**

SDK will automatically reply to the busy line to the second caller, and the behavior is consistent with the new call received during the call.

**Q: Will the banner still be displayed when Android does not have the floating window permission?**

No way. When there is no floating window permission, the SDK will downgrade the display system notification to prompt incoming calls and guide users to the settings page for authorization. After authorization, the next call can display the banner normally.

**Q: Click the main body of the banner to enter the full-screen call page. Will the ringtone play again?**

No way. The ringtone will be played continuously, and it will not be interrupted or re-triggered when entering the full-screen call page. The ringtone stops at the end of the call (answer/reject/timeout/cancel).
