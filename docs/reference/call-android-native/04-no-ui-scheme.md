# 04 — No-UI Scheme (raw engine)

> Integrate the Call Kit **base package (no UI)** and drive calls directly through `NECallEngine`. Use this only if the prebuilt UI ([`01`](./01-integration-android.md)) can't match the Figma design — you then build the call UI yourself.
> ⬅️ Back to [README](./README.md).

> Yunxin recommends the UI-included path. Consider this for open decision **Q3** (prebuilt vs custom vs hybrid) in [README §6](./README.md#6-open-decisions--risks-phase-3).

Dependency, ABI filters, permissions, and ProGuard are the **same as [`01`](./01-integration-android.md)** except you import the base package instead of `call-ui`:

```gradle
implementation 'com.netease.yunxin.kit.call:call:3.3.0'          // base package (no UI)
// Optional: exclude bundled SDKs to pin your own versions
// implementation('com.netease.yunxin.kit.call:call:3.3.0') {
//     exclude group: 'com.netease.nimlib'
//     exclude group: 'com.netease.yunxin', module: 'nertc-base'
// }
```

> SDK version mapping is enforced (e.g. call `V3.3.0` ↔ NIM `V10.6.0` + NERTC `V5.6.50`). Use the project's Phase-2 target versions; keep all NIM sub-packages on the same version. [Yunxin changelog](https://doc.yunxin.163.com/nertccallkit/concept/DMzOTI3NTA?platform=client).

---

## Initialize (no UI)

Init IM (V10) first, then `NECallEngine.setup`. Init after IM login; release on logout (not in `onDestroy`). Re-init crashes unless the previous instance is destroyed first.

`NESetupConfig` parameters:

| Param | Type | Required | Notes |
|---|---|:---:|---|
| `appKey` | string | Yes | From the console |
| `currentUserRtcUid` | long | No | RTC uid; auto-generated when `0L` |
| `rtcConfig` | `NERtcOption` | No | NERTC init config |
| `enableAutoJoinSignalChannel` | boolean | No | Callee auto-joins signaling on invite (default false). **Mutually exclusive with multi-device login.** |
| `enableJoinRtcWhenCall` | boolean | No | Caller joins RTC room immediately when calling (default false) |
| `initRtcMode` | int | No | `GLOBAL`(1, default) / `IN_NEED`(2) / `IN_NEED_DELAY_TO_ACCEPT`(3) |
| `rtcCallExtension` | `CallExtension` | No | Resolution / RTC behavior |

```java
NIMClient.initV2(context, sdkOptions);   // IM V10 init

NERtcOption rtcOption = new NERtcOption();
NESetupConfig config = new NESetupConfig.Builder(appKey)
    .rtcOption(rtcOption)
    .build();
// After IM init + login:
NECallEngine.sharedInstance().setup(this, config);
```

Login is the same as [`01` §6](./01-integration-android.md#6-log-in-to-im).

---

## 1-to-1 call (point-to-point)

Call sequence: `addCallDelegate` → `call` → peer `onReceiveInvited` → `accept` → `onCallConnected` → `hangup` → `onCallEnd`.

**1) Add call listener** (set globally to avoid missing incoming calls):

```java
NECallEngine.sharedInstance().addCallDelegate(new NECallEngineDelegateAbs() {
    @Override
    public void onReceiveInvited(NEInviteInfo info) {
        // show notification, or open the callee page with info
    }
});
```

**2) Caller initiates** — enter the caller page, add an in-call delegate (remove it on destroy to avoid leaks), then `call`.

`NECallParam`:

| Param | Type | Required | Notes |
|---|---|:---:|---|
| `accid` | string | Yes | Callee IM account (for this project: CSR `accid` from `idle_tc_csr`) |
| `callType` | `NECallType` | Yes | `AUDIO` / `VIDEO` — use **AUDIO** for this feature |
| extraInfo | string | No | Passed to callee `onReceiveInvited` |
| globalExtraCopy | string | No | Global copy info for business identification |
| rtcChannelName | string | No | Auto-generated if omitted |
| pushConfig | `NECallPushConfig` | No | Custom push (title/content/payload; `needPush` default true) |

```java
NECallParam param = new NECallParam.Builder("calledUserAccId")
    .callType(NECallType.AUDIO)   // audio-only for CSR call
    .build();
NEResultObserver<CommonResult<NECallInfo>> observer = new NEResultObserver<CommonResult<NECallInfo>>() {
    @Override
    public void onResult(CommonResult<NECallInfo> result) {
        if (result.isSuccessful()) {
            NECallInfo callInfo = result.data;    // call details
        } else {
            int code = result.code;               // failure code
            String message = result.msg;
        }
    }
};
NECallEngine.sharedInstance().call(param, observer);   // play ringtone here if desired
```

**3) Callee receives** — `onReceiveInvited(NEInviteInfo)`:

| Field | Type | Meaning |
|---|---|---|
| `callerAccid` | string | Caller IM account |
| `callType` | — | `AUDIO` / `VIDEO` |
| `extraInfo` | — | Extra passed from caller |
| `channelId` | string | IM signaling channel ID |

> On Android Q+, the system may block background page popups → a notification shows instead; tapping it opens the callee page. Add `addCallDelegate` on the callee page too; `removeCallDelegate` on destroy.

**4) Callee answers** — `accept` (triggers `onUserEnter` when the peer joins RTC = connected):

```java
NECallEngine.sharedInstance().accept(new NEResultObserver<CommonResult<NECallInfo>>() {
    @Override
    public void onResult(CommonResult<NECallInfo> result) {
        if (result.isSuccessful()) { NECallInfo callInfo = result.data; }
        else { int code = result.code; String message = result.msg; }
    }
});
```

**5) Hang up** — caller cancel / callee reject / in-call hangup all use `hangup`. The peer gets `onCallEnd` with `NECallEndInfo`.

`NEHangupParam(channelId, extraString)`: pass `null` channelId to hang up the current call directly; pass a specific value and the SDK verifies it matches the current call (else hangup fails).

```java
NEHangupParam param = new NEHangupParam("channelId", "extraString");
NECallEngine.sharedInstance().hangup(param, new NEResultObserver<CommonResult<Void>>() {
    @Override
    public void onResult(CommonResult<Void> result) {
        // result.isSuccessful() → hung up
    }
});
```

**6) Busy** — if the callee isn't in `STATE_IDLE` when invited, it auto-`hangup`s; caller gets `onCallEnd` with `NEHangupReasonCode.BUSY` and sends a local busy ticket.

---

## Advanced (no UI)

**Multi-device login:**
- IM multi-device **off**: the signaling channel is kicked → the terminal leaves the RTC room and hangs up.
- IM multi-device **on**: other logins don't affect the active call, but all online terminals show the invite page; when one answers/rejects, the others get error code `2001` or `2002`.

**Call/answer timeout:** if neither cancel nor answer/hangup happens, both sides get `onCallEnd` with `NEHangupReasonCode.TIME_OUT`. Change timeout (takes effect before the next call):

```java
NECallEngine.sharedInstance().setTimeout(long timeMs);
```

**Video local preview / remote view** (video only):

```java
NECallEngine.sharedInstance().setupLocalView(videoView);
NECallEngine.sharedInstance().setupRemoteView(videoView);  // call in onCallConnected
```

### Set call resolution & audio profile

Via `rtcCallExtension` in `NESetupConfig`:

```java
NESetupConfig config = new NESetupConfig.Builder(appKey)
    .rtcCallExtension(new NERtcCallExtension(){
        @Override
        protected void configAudioProfileBeforeJoin() {
            // Audio-only: standard + speech
            NERtcEx.getInstance().setAudioProfile(NERtcConstants.AudioProfile.STANDARD,
                                                  NERtcConstants.AudioScenario.SPEECH);
        }
        @Override
        protected void configVideoConfigBeforeJoin(){
            NERtcVideoConfig videoConfig = new NERtcVideoConfig();
            videoConfig.frameRate = NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_15;
            videoConfig.width = 640;
            videoConfig.height = 360;
            NERtcEx.getInstance().setLocalVideoConfig(videoConfig);
        }
        @Override
        protected void configChannelProfileBeforeJoin(){
            NERtcEx.getInstance().setChannelProfile(NERtcConstants.RTCChannelProfile.COMMUNICATION);
        }
    })
    .build();
NECallEngine.sharedInstance().setup(this, config);
```

---

## Call-end error codes (`NEHangupReasonCode`)

| Code | Name | Meaning |
|:---:|---|---|
| 0 | `NORMAL` | Call disconnected |
| 1 | `TOKEN_ERROR` | RTC token error |
| 2 | `TIME_OUT` | Call timed out |
| 3 | `BUSY` | Callee busy |
| 4 | `RTC_INIT_ERROR` | RTC init failed |
| 5 | `JOIN_RTC_ERROR` | Failed to join RTC channel |
| 6 | `CANCEL_ERROR_PARAM` | Cancel param error |
| 7 | `CALL_FAILED` | Call failed |
| 8 | `KICKED` | User kicked out |
| 9 | `UID_EMPTY` | RTC uid for accId is empty |
| 10 | `SELF_RTC_DISCONNECTED` | Callee is this terminal's RTC user |
| 11 | `CALLER_CANCEL` | Actively canceled |
| 12 | `CALLEE_CANCELED` | Call canceled |
| 13 | `CALLEE_REJECT` | Actively rejected |
| 14 | `CALLER_REJECTED` | Call rejected |
| 15 | `HANG_UP` | Actively hung up |
| 16 | `BE_HUNG_UP` | Call was hung up |
| 17 | `OTHER_REJECTED` | Rejected by another (multi-device) terminal |
| 18 | `OTHER_ACCEPTED` | Answered by another (multi-device) terminal |
| 19 | `USER_RTC_DISCONNECTED` | User's RTC room long-link disconnected |
| 20 | `USER_RTC_LEAVE` | User left the RTC room |
| 21 | `ACCEPT_FAIL` | Failed to answer |

---

## Group call & single-to-group (no UI) — reference only

> ⚪ **Not needed for this project** (1v1 audio only). Kept for completeness. Group call is **Beta** — contact your Yunxin manager to enable.

**Group call** via `NEGroupCall.instance()`:

| API | Purpose |
|---|---|
| `init(GroupConfigParam)` | Initialize group calling (appKey, `currentUserAccId` required) |
| `groupCall(GroupCallParam)` | Start a group call (`callId` + callee list required) |
| `groupInvite(GroupInviteParam)` | Invite more members |
| `groupAccept(GroupAcceptParam)` | Accept invitation |
| `groupJoin(GroupJoinParam)` | Join an ongoing call |
| `groupHangup(GroupHangupParam)` | Hang up |
| `groupQueryCallInfo(...)` | Query call details |
| `groupQueryMembers(...)` | Query member list |
| `configGroupIncomingReceiver(receiver, register)` | Listen for group invitations |
| `configGroupActionObserver(observer, register)` | Observe member changes / hangup |

**Single-call → group call** (since v4.7.0): based on the existing 1v1 signaling + RTC room; invite new members via `NECallEngine.inviteMembers` (does **not** use the legacy `NEGroupCall`). Requires `enableSingleToGroupCall(true)` in `NESetupConfig`, and both ends on a supporting SDK version. Max 10 people. Only after the 1v1 call is connected. Distinguish invites via `NEInviteInfo.multiCallInvite`; a member truly joins when `onCallMembersChanged` reports `NECallMemberState.JOINED`. Once in multi-person mode it stays there (no revert to 1v1 UI, no audio/video switch). The single-to-group ticket is server-generated — contact Yunxin support to enable.

```java
NESetupConfig config = new NESetupConfig.Builder(appKey)
    .enableSingleToGroupCall(true)
    .build();
NECallEngine.sharedInstance().setup(context.getApplicationContext(), config);
NECallEngine.sharedInstance().addCallDelegate(callDelegate);
```

Full details: see the raw source in [`99-reference-raw.md`](./99-reference-raw.md) or the official docs.
