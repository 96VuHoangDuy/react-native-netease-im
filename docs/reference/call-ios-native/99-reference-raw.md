# 99 — Raw Source & Reference

> Provenance of this knowledge base + links to official docs.
> ⬅️ Back to [README](./README.md).

---

## Raw source

The iOS integration content in this folder was extracted and cleaned from a **manually-copied dump** of the Yunxin NERTC Call Kit (V3 UI) documentation, kept alongside this folder as `_raw-source.md`:

```
_raw-source.md   (2394 lines, mostly Chinese, unstructured)
```

It was copied by hand because the official Yunxin doc site (`doc.yunxin.163.com`) blocks automated fetching (anti-spam bot). Keep this raw file as the source of truth if you need to re-verify anything; the topic files here are the cleaned, organized (English) version.

### Section map of the raw dump

| # | Section (original) | Topic | Raw lines | Landed in |
|:---:|---|---|---|---|
| 1 | 跑通示例项目 | Run the sample project | 3–47 | [`01`](./01-integration-ios.md) (env, prereqs) |
| 2 | 实现单聊呼叫（含 UI） | Single call (with UI) | 51–242 | [`01`](./01-integration-ios.md) |
| 3 | 实现群组通话（含 UI） | Group call (with UI) | 246–331 | [`04`](./04-no-ui-scheme.md) (reference) |
| 4 | 实现单呼转群聊（含 UI） | Single→group (with UI) | 336–528 | [`04`](./04-no-ui-scheme.md) (reference) |
| 5 | 话单 | Call ticket / list | 533–741 | [`02`](./02-call-list.md) |
| 6 | 悬浮窗 | Floating window | 746–786 | [`03`](./03-advanced.md) |
| 7 | 来电横幅 | Call banner | 791–834 | [`03`](./03-advanced.md) |
| 8 | 虚拟背景 | Virtual background | 839–880 | [`03`](./03-advanced.md) |
| 9 | 自定义铃声 | Custom ringtone | 885–951 | [`03`](./03-advanced.md) |
| 10 | 自定义接听背景图 | Custom answer background | 956–1028 | [`03`](./03-advanced.md) |
| 11 | 私有化配置 | Privatization | 1033–1065 | [`03`](./03-advanced.md) |
| 12 | 获取主被叫用户信息 | Get caller/callee info | 1070–1186 | [`03`](./03-advanced.md) |
| 13 | 拦截呼入请求 | Intercept inbound | 1191–1227 | [`03`](./03-advanced.md) |
| 14 | 实现 1 对 1 呼叫（无 UI） | Single call (no UI) | 1232–1657 | [`04`](./04-no-ui-scheme.md) |
| 15 | 群组通话（无 UI） | Group call (no UI) | 1662–1870 | [`04`](./04-no-ui-scheme.md) (reference) |
| 16 | 实现单呼转群聊（无 UI） | Single→group (no UI) | 1871–2394 | [`04`](./04-no-ui-scheme.md) (reference) |

---

## Internal source docs (requirement)

- Requirement: `pyeon-chinese-mobile/docs/Online customer service voice function document (1)/2026-05-22.md`
- CSR API spec (incl. `service/idle_tc_csr`): `pyeon-chinese-mobile/docs/Online customer service voice function document (1)/service.md`
- Android counterpart docs: [`docs/call-android-native/`](../call-android-native/README.md)
- Initiative dashboard & tracking (in `pyeon-chinese-mobile` repo): `pyeon-chinese-mobile/docs/initiatives/rn-im-call/README.md`
- Phase-3 voice-call tracking (in `pyeon-chinese-mobile` repo): `pyeon-chinese-mobile/docs/initiatives/rn-im-call/phase-3-voice-call.md`

---

## Authoritative source & version-of-truth (read this)

- **Canonical documentation = the Yunxin doc portal** `doc.yunxin.163.com/nertccallkit` — the source of this folder's raw dump. It documents the **V3** Call Kit (`NECallEngine` / `NERtcCallUIKit` / `NESetupConfig` / `callWithParam:` / `NECallType`). This project targets **V3** (NIM V10 + call-ui 4.1.0). There is no "more standard" doc set — this folder already mirrors the canonical portal.
- **Do NOT follow these legacy repos** — they are an older **V1/V2** generation with a different API and old NIM (8.x). Using them will not match this project:
  - `github.com/netease-kit/NERtcCallKit-iOS` — NIMSDK `8.1.0`, NERtcSDK `3.7.1`, `NERtcCallKit.sharedInstance` + `setupAppKey:options:`, `NERtcCallKitDelegate`.
  - `github.com/netease-kit/NERtcCallKit-Android` — `com.netease.yunxin.kit:call:1.1.0`, `NERTCVideoCall` / `NERTCVideoCallActivity`.
- **`github.com/netease-kit/NECallKit`** is the umbrella/sample project (the V3 sample this folder links to). ⚠️ The GitHub sample branch can **lag** the doc portal's versions (it has shown old pins like `NIMSDK_LITE 7.9.1`) — do not pin SDK versions from the sample's `Podfile`.
- **Version-of-truth for this project** = the [Yunxin changelog (iOS)](https://doc.yunxin.163.com/nertccallkit/concept/jIzNDA4Nzc?platform=client) **+** the Phase-2 target (iOS NIM `10.9.53`, call-ui `4.1.0`). Any example numbers in the topic files (`NIMSDK_LITE 10.9.70`, `NERtcSDK 5.9.0`, `NERtcCallKit 3.3.0`) are Yunxin doc illustrations, not the project's pins.

## Official Yunxin links

> Some are blocked by anti-spam automation; open in a browser.

- Call Kit changelog / version-compat (iOS): <https://doc.yunxin.163.com/nertccallkit/concept/jIzNDA4Nzc?platform=client>
- Call Kit changelog / version-compat (Android): <https://doc.yunxin.163.com/nertccallkit/concept/DMzOTI3NTA?platform=client>
- NECallKit sample source (iOS): <https://github.com/netease-kit/NECallKit/tree/main/iOS>
- Custom UI guide (iOS): <https://doc.yunxin.163.com/nertccallkit/docs/zM2Mzk2MjY?platform=iOS>
- Single-call implementation (iOS): <https://doc.yunxin.163.com/nertccallkit/guide/jg0MzU3NjM?platform=iOS>
- IM SDK init (iOS): <https://doc.yunxin.163.com/messaging2/guide/jU5MTUwMDY?platform=client>
- IM login (iOS): <https://doc.yunxin.163.com/messaging2/guide/Dk1MTY4MzA?platform=client>
- Console — create app / get AppKey: <https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console>
- Console — open/close services: <https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console>
- NIM V10 message docs (receive custom messages): <https://doc.yunxin.163.com/messaging2/guide/DYzMjA0Njc?platform=client>
- Figma — Customer service talk: <https://www.figma.com/design/mqpSXsP8Hb93iwbT4kUNas/VN-APP-Release?node-id=21035-20419>

---

## iOS-specific notes vs Android

- iOS bundles the NIM SDK inside the Call Kit; integrate `NERtcCallKit` + `NERtcCallUIKit` + `NERtcSDK` pods (Android pulls IM/NERTC as gradle deps under the `call-ui` artifact).
- Init uses `registerWithOptionV2` + `NECallEngine setup:` + `NERtcCallUIKit setupWithConfig:` (Android: `NIMClient.initV2` + `CallKitUI.init`).
- Call banner needs **no** overlay permission on iOS (Android requires `SYSTEM_ALERT_WINDOW`).
- Out-of-app floating window is **iOS 16+** only (system PiP), needs `NETranscodingKit`.
- Permissions live in **Info.plist** (`NSMicrophoneUsageDescription`), not AndroidManifest.
