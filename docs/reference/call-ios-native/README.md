# Call Feature — Knowledge Base (iOS)

> Consolidated knowledge & integration guide for the **Voice Customer Service** call feature on **iOS**.
> Client taps a "Voice Call" button in the customer-service chat → places an **audio-only** call to an idle CSR agent via **NetEase NERTC Call Kit** (built on NIM SDK V10 + NERTC SDK).
>
> Android counterpart: [`docs/call-android-native/`](../call-android-native/README.md). The **business flow, allocation strategy, and idle-CSR API are platform-agnostic** (shared with Android); the integration is iOS-specific.
> Last updated: 2026-07-15.

---

## 1. Why this exists

The call knowledge was scattered across internal requirement/tracking docs + a raw Yunxin doc dump. This folder is the **single, organized flow** to consult when implementing the feature on iOS.

| This folder answers | Where |
|---|---|
| Business flow, dependency chain, constraints, decisions | this README |
| How to integrate Call Kit on iOS (pods, init, 1v1 call) | [`01-integration-ios.md`](./01-integration-ios.md) |
| Call ticket / call list (话单) parsing | [`02-call-list.md`](./02-call-list.md) |
| Floating window, banner, intercept inbound, ringtone, user info | [`03-advanced.md`](./03-advanced.md) |
| No-UI scheme (raw engine APIs), group call | [`04-no-ui-scheme.md`](./04-no-ui-scheme.md) |
| **System call answering (LiveCommunicationKit / PushKit VoIP)** — app backgrounded/killed | [`05-system-call-lck.md`](./05-system-call-lck.md) · raw: [`05-…official-raw.md`](./05-system-call-lck.official-raw.md) |
| Call banner (来电横幅) — app alive | [`03-advanced.md` §Call banner](./03-advanced.md#call-banner) · raw: [`06-…official-raw.md`](./06-incoming-banner.official-raw.md) |
| Original raw source + official links | [`99-reference-raw.md`](./99-reference-raw.md) |

**Related (do not duplicate):** live status/tracking lives in the initiative docs in the **`pyeon-chinese-mobile`** repo — `pyeon-chinese-mobile/docs/initiatives/rn-im-call/README.md` (dashboard) and `.../phase-3-voice-call.md`. This folder is the **"how"**, the initiative is the **"status"**.

---

## 2. Dependency chain (cannot be reordered)

```
Phase 1  RN upgrade + New Architecture stable
   │  Native module must not break mid-migration.
   ▼
Phase 2  NIM SDK 9.x → 10.x  (iOS 10.9.53 / Android 10.9.52)
   │  Call Kit requires NIM V10. Cannot add call on NIM 9.x.
   ▼
Phase 3  Voice Call (Call Kit)  ← this feature
```

- Call Kit is built on **NIM SDK V10** + **NERTC SDK**. On iOS the NIM SDK is bundled inside the Call Kit, so you integrate the Call Kit + NERTC (see [`01`](./01-integration-ios.md)).
- The call is emitted through the native module `react-native-netease-im` (git-URL dependency; per repo `CLAUDE.md` this lib is NOT patched via patch-package — source is synced directly into `node_modules`).
- Version pinning is a hard constraint: the Call Kit version ↔ NIM/NERTC versions have a fixed mapping (see [`99-reference-raw.md`](./99-reference-raw.md) → Yunxin changelog). **Use the project's actual target versions** (iOS NIM `10.9.53`), not the example numbers copied from the Yunxin doc.

---

## 3. Business flow — outbound App → CSR (shared with Android)

Source: `pyeon-chinese-mobile/docs/Online customer service voice function document (1)/2026-05-22.md` + `service.md`.
Available since CSR **PC client v0.2.4** (2026-05-22).

### Steps

1. Customer taps **"Voice Call"** in the CS chat screen.
2. App shows a **loading / scheduling UI** (Figma "two people" animation).
3. App calls the **idle CSR API** (`service/idle_tc_csr`) to get an idle agent.
4. Server allocates an agent per the **allocation strategy** (below), or returns empty.
5. Two branches:
   - **Got a CSR** → place the audio call to that agent's `accid` via Call Kit → wait for the CSR to answer on PC.
   - **No CSR** → show error prompt, end.
6. Even with a CSR, if the agent is actually busy → the PC client auto-rejects → App shows a prompt and ends.
7. On answer, NERTC establishes the audio connection; call continues until either side hangs up.

> **Inbound (CSR → user) is NOT in scope.** See project constraint below + [`03-advanced.md` → Intercept inbound](./03-advanced.md#intercept-inbound-requests).

### Allocation strategy (server-side, priority high → low)

1. Currently-bound agent, if online **and** idle.
2. Else the last-bound agent, if online **and** idle.
3. Else a random online idle agent.
4. If `customer_service_type` is specified, allocate only within that type.

### UI states (Figma)

`online` · `robot scheduling (loading)` · `scheduling failed`.

### Error prompts (i18n: cn / vi / en)

| Case | CN | EN |
|---|---|---|
| Non-working-hours | `当前不是工作时间，无客服应答` | Currently not working hours, no customer service available. |
| Busy | `当前客服正忙，请稍后再试！` | Customer service is busy at the moment, please try again later! |

---

## 4. Idle CSR API — `service/idle_tc_csr`

Must be called **before** the Call Kit `call` to obtain the target agent's `accid`.

**Request**

| Param | Type | Required | Description |
|---|---|:---:|---|
| `account` | String | Yes | ZYZJ account of the client |
| `customer_service_type` | String | No | `1`=recharge staff, `2`=air-ticket staff; blank/`0` = no restriction |

**Response — idle staff available**

```json
{
  "account": "20257",   // CSR ZYZJ account
  "accid": "ss20257",    // CSR Yunxin accid → used as remoteUserAccid / callee accId
  "name": "Xiao Li"      // CSR nickname
}
```

**Response — no idle staff**

```json
[]
```

> Mobile does **not yet** have this endpoint wired (open question Q6). Full CSR API spec: `pyeon-chinese-mobile/docs/Online customer service voice function document (1)/service.md`.

---

## 5. Project-specific constraints (differ from generic Yunxin doc)

The Yunxin doc targets a generic 1-to-1 **video** app. This project needs a narrower behavior:

| Constraint | What it means | Where |
|---|---|---|
| **Audio-only** | Use `NECallTypeAudio` (not `NECallTypeVideo`). Skip camera usage; only mic permission needed. | [`01`](./01-integration-ios.md#6-place-a-1v1-audio-call) |
| **Outbound only / block inbound** | Requirement never triggers CSR→user calls. Decide whether to intercept the default callee page. | [`03` → Intercept inbound](./03-advanced.md#intercept-inbound-requests) |
| **Call before dialing** | Always resolve an idle CSR `accid` via `service/idle_tc_csr` before calling. | §4 above |
| **Version pin** | Match Call Kit ↔ NIM/NERTC to the project's target (iOS NIM `10.9.53`), not the doc's example versions. | §2 above |
| **RN bridge** | Call is triggered through `react-native-netease-im`; a JS bridge (`startVoiceCall(csrAccid)` / `hangup()` / state observer) must expose it to RN. | initiative phase-3 §3.2 |
| **Info.plist** | Add `NSMicrophoneUsageDescription` (and `NSCameraUsageDescription` only if video). | [`01`](./01-integration-ios.md#3-infoplist--permissions) |

---

## 6. Open decisions & risks (Phase 3)

Tracked in `pyeon-chinese-mobile/docs/initiatives/rn-im-call/phase-3-voice-call.md`. Summary:

| # | Open decision |
|:---:|---|
| Q3 | Call UI: prebuilt Call Kit UI vs custom (Figma) vs hybrid |
| Q4 | Inbound call: intercept / allow / default |
| Q5 | "Two people" scheduling animation asset (owner) |
| Q6 | BE `idle_tc_csr` endpoint readiness (BE) |

---

## 7. Console prerequisites (one-time, before any code)

In the [NetEase Yunxin console](https://app.yunxin.163.com/global/home):

1. Create the app, obtain the **App Key**.
2. Enable services: **IM instant messaging**, **Audio & video call 2.0 (NERTC)**, **Signaling** (sub-feature of IM, enabled separately), and **Call list** (话单) if you want call-ticket messages.
3. Configure APNs / PushKit certificates for the app (see [`01` init](./01-integration-ios.md#4-initialize-nim--call-kit)).
4. (Integration stage only) enable NERTC **debug mode**; switch back to **secure mode** before production.
