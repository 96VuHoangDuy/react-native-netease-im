# 06 — 来电横幅 / Call banner (OFFICIAL RAW — iOS)

> **Nguồn:** docs chính chủ NetEase Yunxin (NERTC CallKit — 来电横幅, iOS). Lưu verbatim để tra cứu nhanh.
> Bản chắt lọc + call site thật trong project: [`03-advanced.md` §Call banner](./03-advanced.md#call-banner).
> Bản Android: [`../call-android-native/06-incoming-banner.official-raw.md`](../call-android-native/06-incoming-banner.official-raw.md).

---

Since v4.3.0, NetEase Cloud Voice and Video Call Components have supported the call banner function. When receiving a call, a lightweight banner is displayed at the top of the screen, which supports one-click answering or rejection without interrupting the user's current operation.

## Overview

The audio and video call component (CallKit) turns off the call banner function by default (consistent with the original full-screen call interface behavior). If you turn on this function, the top banner will be displayed when you receive a call, and the original full-screen call page will no longer appear.

- This function supports switching at any time during operation and takes effect on the **next call** (not effective for the currently displayed banner).
- If there is a second call during the banner display, the SDK will automatically reply to the busy line.
- During the banner display, the ringtone is played normally; when clicking the main body of the banner to enter the full-screen incoming call page, the ringtone **is not interrupted**.

## Effect display

![image-netease](https://yx-web-nosdn.netease.im/common/c9362efa9b53f8b1a395bd0f0e62c8b5/横幅.png)

## Enable the call banner function

When the cloud audio video component receives an incoming call, it will display a lightweight banner at the top of the screen, which supports one-click answering or rejection without interrupting the user's current operation.

iOS 横幅基于独立 `UIWindow` 实现，无需额外权限申请。如果需要启用该功能，可以使用 `enableIncomingBanner` 方法，在 NERtcCallKit 组件初始化后开启该功能：

```objc
// 开启
[[NERtcCallUIKit sharedInstance] enableIncomingBanner:YES];

// 关闭
[[NERtcCallUIKit sharedInstance] enableIncomingBanner:NO];
```

## Frequently asked questions

**Q: After the banner is turned on, will the original full-screen call interface still appear?**

No way. After turning on the banner mode, the full-screen incoming call interface is completely replaced by the banner display. If you need to restore, just call `enableIncomingBanner:NO`.

**Q: How to deal with receiving a second call during the banner display?**

SDK will automatically reply to the busy line to the second caller, and the behavior is consistent with the new call received during the call.

**Q: Click the main body of the banner to enter the full-screen call page. Will the ringtone play again?**

No way. The ringtone will be played continuously, and it will not be interrupted or re-triggered when entering the full-screen call page. The ringtone stops at the end of the call (answer/reject/timeout/cancel).
