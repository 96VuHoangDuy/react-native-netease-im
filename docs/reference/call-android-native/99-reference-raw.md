# 99 — Raw Source & Reference

> Provenance of this knowledge base + links to official docs.
> ⬅️ Back to [README](./README.md).

---

## Raw source

The Android integration content in this folder was extracted and cleaned from a **manually-copied dump** of the Yunxin NERTC Call Kit (V3 UI) documentation, kept alongside this folder as `_raw-source.md`:

```
_raw-source.md   (3134 lines, EN + CN mixed, unstructured)
```

It was copied by hand because the official Yunxin doc site (`doc.yunxin.163.com`) blocks automated fetching (anti-spam bot). Keep this raw file as the source of truth if you need to re-verify anything; the topic files here are the cleaned, organized version.

### Section map of the raw dump

| # | Section (original) | Topic | Raw lines | Landed in |
|:---:|---|---|---|---|
| 1 | 跑通示例项目 | Run the sample project | 1–52 | [`01`](./01-integration-android.md) (env, prereqs) |
| 2 | 实现单聊呼叫 | Single call (with UI) | 53–445 | [`01`](./01-integration-android.md) |
| 3 | 实现群组通话 | Group call (with UI) | 446–576 | [`04`](./04-no-ui-scheme.md) (reference) |
| 4 | 实现单呼转群呼 | Single→group (with UI) | 577–767 | [`04`](./04-no-ui-scheme.md) (reference) |
| 5 | 通话话单 | Call ticket / list | 768–996 | [`02`](./02-call-list.md) |
| 6 | 悬浮窗 | Floating window | 997–1058 | [`03`](./03-advanced.md) |
| 7 | 来电横幅 | Call banner | 1059–1129 | [`03`](./03-advanced.md) |
| 8 | 虚拟背景 | Virtual background | 1130–1185 | [`03`](./03-advanced.md) |
| 9 | 自定义用户昵称和头像 | Custom nickname/avatar | 1186–1223 | [`03`](./03-advanced.md) |
| 10 | 自定义呼叫铃声 | Custom ringtone | 1224–1366 | [`03`](./03-advanced.md) |
| 11 | 自定义接听背景图 | Custom answer background | 1367–1448 | [`03`](./03-advanced.md) |
| 12 | 私有化配置 | Privatization | 1449–1490 | [`03`](./03-advanced.md) |
| 13 | 设置初始化参数 | Init parameters | 1491–1601 | [`03`](./03-advanced.md) |
| 14 | 设置 RTC 的音视频属性 | RTC AV attributes | 1602–1648 | [`03`](./03-advanced.md) |
| 15 | 拦截呼入请求 | Intercept inbound | 1649–1687 | [`03`](./03-advanced.md) |
| 16 | 实现单聊呼叫（无 UI） | Single call (no UI) | 1688–2314 | [`04`](./04-no-ui-scheme.md) |
| 17 | 实现群组通话（无 UI） | Group call (no UI) | 2315–2603 | [`04`](./04-no-ui-scheme.md) (reference) |
| 18 | 实现单呼转群聊 | Single→group (no UI) | 2604–3134 | [`04`](./04-no-ui-scheme.md) (reference) |

---

## Internal source docs (requirement)

- Requirement: `pyeon-chinese-mobile/docs/Online customer service voice function document (1)/2026-05-22.md`
- CSR API spec (incl. `service/idle_tc_csr`): `pyeon-chinese-mobile/docs/Online customer service voice function document (1)/service.md`
- Initiative dashboard & tracking (in `pyeon-chinese-mobile` repo): `pyeon-chinese-mobile/docs/initiatives/rn-im-call/README.md`
- Phase-3 voice-call tracking (in `pyeon-chinese-mobile` repo): `pyeon-chinese-mobile/docs/initiatives/rn-im-call/phase-3-voice-call.md`

---

## Authoritative source & version-of-truth (read this)

- **Canonical documentation = the Yunxin doc portal** `doc.yunxin.163.com/nertccallkit` — the source of this folder's raw dump. It documents the **V3** Call Kit (`NECallEngine` / `CallKitUI` / `NESetupConfig` / `startSingleCall` / `NECallType`). This project targets **V3** (NIM V10 + call-ui 4.1.0). There is no "more standard" doc set — this folder already mirrors the canonical portal.
- **Do NOT follow these legacy repos** — they are an older **V1/V2** generation with a different API and old NIM (8.x). Using them will not match this project:
  - `github.com/netease-kit/NERtcCallKit-Android` — `com.netease.yunxin.kit:call:1.1.0`, classes `NERTCVideoCall` / `NERTCVideoCallActivity`, `NERTCCallingDelegate`.
  - `github.com/netease-kit/NERtcCallKit-iOS` — NIMSDK `8.1.0`, `NERtcCallKit.sharedInstance` + `setupAppKey:options:`.
- **`github.com/netease-kit/NECallKit`** is the umbrella/sample project (the V3 sample this folder links to). ⚠️ The GitHub sample branch can **lag** the doc portal's versions — do not pin SDK versions from the sample's `build.gradle`/`Podfile`.
- **Version-of-truth for this project** = the [Yunxin changelog](https://doc.yunxin.163.com/nertccallkit/concept/DMzOTI3NTA?platform=client) **+** the Phase-2 target (Android NIM `10.9.52`, call-ui `4.1.0`). Any example numbers in the topic files (`call-ui:3.3.0`, `basesdk:10.6.0`) are Yunxin doc illustrations, not the project's pins.

## Official Yunxin links

> Some are blocked by anti-spam automation; open in a browser.

- Call Kit changelog / version-compat (Android): <https://doc.yunxin.163.com/nertccallkit/concept/DMzOTI3NTA?platform=client>
- Call Kit version-compat (iOS): <https://doc.yunxin.163.com/nertccallkit/concept/jIzNDA4Nzc?platform=client>
- NECallKit sample source (Android): <https://github.com/netease-kit/NECallKit/tree/main/Android>
- Custom UI guide (Android): <https://doc.yunxin.163.com/nertccallkit/guide/zYzNzI5NDI?platform=android>
- Init parameter config: <https://doc.yunxin.163.com/nertccallkit/guide/DI5Nzg0OTM?platform=android>
- Console — create app / get AppKey: <https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console>
- Console — open/close services: <https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console>
- NIM V10 message docs (receive custom messages): <https://doc.yunxin.163.com/messaging2/guide/DYzMjA0Njc?platform=client>
- Figma — Customer service talk: <https://www.figma.com/design/mqpSXsP8Hb93iwbT4kUNas/VN-APP-Release?node-id=21035-20419>

---

## iOS

**TBD.** iOS integration content will be provided separately and added as `01-integration-ios.md` (same structure). Version targets differ (iOS NIM `10.9.53`).
