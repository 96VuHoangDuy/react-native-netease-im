# Call Feature — Knowledge Base (Android)

> Consolidated knowledge & integration guide for the **Voice Customer Service** call feature.
> Client taps a "Voice Call" button in the customer-service chat → places an **audio-only** call to an idle CSR agent via **NetEase NERTC Call Kit** (built on NIM SDK V10 + NERTC SDK).
>
> **Platform: Android first.** iOS is TBD (same structure will be added when the iOS source is provided).
> Last updated: 2026-07-15.

---

## 1. Why this exists

The call knowledge was scattered across 4 sources (internal requirement/tracking + a raw Yunxin doc dump). This folder is the **single, organized flow** to consult when implementing the feature.

| This folder answers | Where |
|---|---|
| Business flow, dependency chain, constraints, decisions | this README |
| How to integrate Call Kit on Android (deps, init, 1v1 call) | [`01-integration-android.md`](./01-integration-android.md) |
| Call ticket / call list (话单) parsing | [`02-call-list.md`](./02-call-list.md) |
| Floating window, banner, intercept inbound, RTC attrs, init params | [`03-advanced.md`](./03-advanced.md) |
| No-UI scheme (raw engine APIs), group call, error codes | [`04-no-ui-scheme.md`](./04-no-ui-scheme.md) |
| Call banner (来电横幅) + overlay permission — needs call-ui ≥ 4.3.0 | [`03-advanced.md` §Call banner](./03-advanced.md#call-banner) · raw: [`06-…official-raw.md`](./06-incoming-banner.official-raw.md) |
| Original raw source + official links | [`99-reference-raw.md`](./99-reference-raw.md) |

**Related (do not duplicate):** live status/tracking of this work lives in the initiative docs in the **`pyeon-chinese-mobile`** repo — `pyeon-chinese-mobile/docs/initiatives/rn-im-call/README.md` (dashboard) and `.../phase-3-voice-call.md`. This folder is the **"how"**, the initiative is the **"status"**.

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

- Call Kit is built on **NIM SDK V10** + **NERTC SDK**. The call is emitted through the native module `react-native-netease-im` (git-URL dependency; see repo `CLAUDE.md` — this lib is NOT patched via patch-package, source is synced directly into `node_modules`).
- Version pinning is a hard constraint: the Call Kit version and NIM/NERTC versions have a fixed mapping (see [`99-reference-raw.md`](./99-reference-raw.md) → Yunxin changelog). **Use the project's actual target versions**, not the example numbers copied from the Yunxin doc (`call-ui:3.3.0` / `basesdk:10.6.0` are Yunxin examples).

---

## 3. Business flow — outbound App → CSR (only)

Source: `pyeon-chinese-mobile/docs/Online customer service voice function document (1)/2026-05-22.md` + `service.md`.
Available since CSR **PC client v0.2.4** (2026-05-22). The IM server added the **idle CSR** API for the app to call *before* placing the call.

### Steps

1. Customer taps **"Voice Call"** in the CS chat screen.
2. App shows a **loading / scheduling UI** (Figma "two people" animation).
3. App calls the **idle CSR API** (`service/idle_tc_csr`) to get an idle agent.
4. Server allocates an agent per the **allocation strategy** (below), or returns empty.
5. Two branches:
   - **Got a CSR** → place the voice call to that agent's `accid` via Call Kit → wait for the CSR to answer on PC.
   - **No CSR** → show error prompt, end.
6. Even with a CSR, if the agent is actually busy → the PC client auto-rejects → App shows a prompt and ends.
7. On answer, NERTC establishes the audio connection; call continues until either side hangs up.

> **Inbound (CSR → user) is NOT in scope.** Call Kit registers an incoming-call listener by default, which can surface an unwanted incoming-call UI. See project constraint below + [`03-advanced.md` → Intercept inbound](./03-advanced.md#intercept-inbound-requests).

### Allocation strategy (server-side, priority high → low)

1. Currently-bound agent, if online **and** idle.
2. Else the last-bound agent, if online **and** idle.
3. Else a random online idle agent.
4. If a `customer_service_type` is specified, allocate only within that type.

### UI states (Figma)

`online` · `robot scheduling (loading)` · `scheduling failed`.

### Error prompts (i18n: cn / vi / en)

| Case | CN | EN |
|---|---|---|
| Non-working-hours | `当前不是工作时间，无客服应答` | Currently not working hours, no customer service available. |
| Busy | `当前客服正忙，请稍后再试！` | Customer service is busy at the moment, please try again later! |

---

## 4. Idle CSR API — `service/idle_tc_csr`

Must be called **before** `startSingleCall` to obtain the target agent's `accid`.

**Request**

| Param | Type | Required | Description |
|---|---|:---:|---|
| `account` | String | Yes | ZYZJ account of the client |
| `customer_service_type` | String | No | `1`=recharge staff, `2`=air-ticket staff; blank/`0` = no restriction |

**Response — idle staff available**

```json
{
  "account": "20257",   // CSR ZYZJ account
  "accid": "ss20257",    // CSR Yunxin accid → used as calledAccId
  "name": "Xiao Li"      // CSR nickname
}
```

**Response — no idle staff**

```json
[]
```

> Mobile does **not yet** have this endpoint wired. Existing chat endpoints (`src/api/chat/chat.endpoint.ts`): `queryProfileBotchat`, `getManualCustomerList`, `queryCurrentlyBound`, `getOnlineManual`. Adding `idle_tc_csr` (likely `/api/v1/client/chat/...`) is a Phase-3 task — confirm the BE contract first (open question Q6).

Related CSR APIs (full spec in `pyeon-chinese-mobile/docs/Online customer service voice function document (1)/service.md`): `service/bound_csr`, `service/csr/onlines`, `service/csr/customer/list`, `service/chatbot/profile`.

---

## 5. Project-specific constraints (differ from generic Yunxin doc)

The Yunxin doc is written for a generic 1-to-1 **video** app. This project needs a narrower behavior:

| Constraint | What it means | Where |
|---|---|---|
| **Audio-only** | Use `NECallType.AUDIO` (not `VIDEO`). Do not request the camera permission; drop `CAMERA` from the manifest sample. | [`01`](./01-integration-android.md#5-place-a-1v1-audio-call) |
| **Outbound only / block inbound** | Requirement never triggers CSR→user calls. Decide whether to intercept/ignore the default incoming listener. | [`03` → Intercept inbound](./03-advanced.md#intercept-inbound-requests) |
| **Call before dialing** | Always resolve an idle CSR `accid` via `service/idle_tc_csr` before `startSingleCall`. | §4 above |
| **Version pin** | Match Call Kit ↔ NIM/NERTC to the project's target (Phase 2), not the doc's example versions. | §2 above |
| **RN bridge** | Call is triggered through `react-native-netease-im`; a JS bridge method (`startVoiceCall(csrAccid)` / `hangup()` / state observer) must expose it to RN. | initiative phase-3 §3.2 |

---

## 6. Open decisions & risks (Phase 3)

Tracked in `pyeon-chinese-mobile/docs/initiatives/rn-im-call/phase-3-voice-call.md`. Summary:

| # | Open decision |
|:---:|---|
| Q3 | Call UI: prebuilt Call Kit UI vs custom (Figma) vs hybrid |
| Q4 | Inbound call: intercept / allow / default |
| Q5 | "Two people" scheduling animation asset (owner) |
| Q6 | BE `idle_tc_csr` endpoint readiness (BE) |

| Risk | Mitigation |
|---|---|
| Call Kit enables incoming listener unexpectedly | Decide Q4 early; intercept inbound (`03-advanced.md`) |
| Cannot distinguish non-working-hours vs busy from API | Confirm BE contract (Q6) |
| Prebuilt UI mismatches Figma | Decide Q3 (hybrid may balance) |
| Audio/mic permission (manifest) | Add on integration (`01`) |

---

## 7. Console prerequisites (one-time, before any code)

In the [NetEase Yunxin console](https://app.yunxin.163.com/global/home):

1. Create the app, obtain the **App Key**.
2. Enable services: **IM instant messaging**, **Audio & video call 2.0 (NERTC)**, **Signaling** (sub-feature of IM, enabled separately), and **Call list** (话单) if you want call-ticket messages.
3. (Integration stage only) enable NERTC **debug mode**; switch back to **secure mode** before production.
