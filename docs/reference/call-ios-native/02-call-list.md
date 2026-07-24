# 02 — Call List / Call Ticket (话单)

> After a call, the Call Kit produces a **call ticket** — an IM event-notification message recording the call outcome (connected? duration? type?). It arrives as an IM session message copy (CC).
> ⬅️ Back to [README](./README.md) · Integration: [`01`](./01-integration-ios.md).

There are **5 ticket types**: 4 "not-connected" tickets sent by the **caller client** (reject / busy / timeout / caller-cancel) and 1 "normal ticket with duration" sent **directly by the server**.

> Multi-person calls do **not** ship a packaged call-list function — implement it yourself if needed.

---

## Method 1 — Use the component's built-in ticket

### Step 1: Enable ticket copy
In the [NetEase console](https://app.yunxin.163.com/index): enable IM instant messaging, audio & video calls, and **signaling** (enabled separately). Then enable the call-component ticket copy in CC message settings.

### Step 2: Sending
Once enabled, the ticket message is sent automatically. The component does **not** receive/parse it for you — you parse it like a normal IM message.

### Step 3: Receive & parse
Tickets arrive through the standard NIM `V2NIMMessageListener`:

```objc
@interface NEMenuViewController () <V2NIMMessageListener>
@end

@implementation NEMenuViewController

- (void)viewDidLoad {
    [[[NIMSDK sharedSDK] v2MessageService] addMessageListener:self];
}
- (void)dealloc {
    [[[NIMSDK sharedSDK] v2MessageService] removeMessageListener:self];
}

#pragma mark - IM delegate
- (void)onSendMessage:(V2NIMMessage *)message {
    if (message.sendingState == V2NIM_MESSAGE_SENDING_STATE_SENDING) {
        [self assembleRecordWithV2Message:message withCaller:YES];
    }
}
- (void)onReceiveMessages:(NSArray<V2NIMMessage *> *)messages {
    for (V2NIMMessage *message in messages) {
        if ([message.senderId isEqualToString:[NEAccount shared].userModel.imAccid]) {
            [self assembleRecordWithV2Message:message withCaller:YES];
            return;
        }
        [self assembleRecordWithV2Message:message withCaller:NO];
    }
}
@end
```

### Ticket status types (`NIMRtcCallStatus`)

| Status | Value | Meaning | Sent by |
|---|:---:|---|---|
| `NIMRtcCallStatusComplete` | 1 | Normal ticket, both sides entered the call and hung up | Server |
| `NIMRtcCallStatusCanceled` | 2 | Caller canceled the call after dialing | Client (caller) |
| `NIMRtcCallStatusRejected` | 3 | Callee rejected | Client (caller) |
| `NIMRtcCallStatusTimeout`  | 4 | Callee didn't act → timeout | Client (caller) |
| `NIMRtcCallStatusBusy`     | 5 | Callee was on another call → auto-rejected | Client (caller) |

The ticket is sent as an IM message CC of **session type** (`eventType = 1`). Its `msgType` is `NRTC_NETCALL`. Call details live in the `attach` field:

| Field | Type | Meaning |
|---|---|---|
| `type` | Number | 1 = audio, 2 = video |
| `channelId` | Number | Room ID |
| `status` | Number | Ticket status (table above) |
| `durations` | JSON array | Per-member: `accid` + `duration` (seconds) |

Received ticket JSON:

```json
{
  "type": 1,
  "channelId": 123,
  "status": 1,
  "durations": [
    { "accid": "acc01", "duration": 10 },
    { "accid": "acc02", "duration": 12 }
  ]
}
```

Parse example:

```objc
- (void)assembleRecordWithV2Message:(V2NIMMessage *)message withCaller:(BOOL)isCaller {
    V2NIMMessageCallAttachment *record = (V2NIMMessageCallAttachment *)message.attachment;

    NIMRtcCallType type = (NIMRtcCallType)record.type;        // audio / video
    NIMRtcCallStatus status = (NIMRtcCallStatus)record.status; // ticket type
    NSArray<V2NIMMessageCallDuration *> *durations = record.durations;

    switch (status) {
        case NIMRtcCallStatusComplete: break;  // answered successfully
        case NIMRtcCallStatusCanceled: break;  // caller canceled
        case NIMRtcCallStatusRejected: break;  // callee rejected
        case NIMRtcCallStatusTimeout:  break;  // callee timeout
        case NIMRtcCallStatusBusy:     break;  // callee busy
        default: break;
    }
}
```

---

## Method 2 — Implement the ticket yourself

If the built-in ticket doesn't fit:

1. Turn off the **server-side** ticket copy.
2. Enable the room-duration message copy (`eventType=8`) in the console.
3. Define your own ticket protocol (usually JSON), e.g. `{ "type": 1, "data": ... }`.
4. Set `setCallRecordProvider`. After setting it, the client no longer sends the component's internal ticket — you send your own via the `onRecordSend:` callback (only *not-connected* tickets come through here; handle connected tickets yourself after the call ends):

```objc
[[NECallEngine sharedInstance] setCallRecordProvider:self];

- (void)onRecordSend:(NERecordConfig *)config {
    // send your own not-connected ticket
}
```

`NERecordConfig` fields:

| Field | Type | Meaning |
|---|---|---|
| `accId` | NSString | The other end's user ID |
| `callType` | NECallType | `NECallType.AUDIO` / `NECallType.VIDEO` |
| `callState` | NIMRtcCallStatus | Ticket type (`NERecordCallStatus`) |

---

## Turn OFF the call-list feature

1. Turn off the **server-side** ticket copy.
2. Turn off **client** ticket sending — set `setCallRecordProvider` with an empty `onRecordSend:`:

```objc
[[NECallEngine sharedInstance] setCallRecordProvider:self];

- (void)onRecordSend:(NERecordConfig *)config {
    // empty = do not send
}
```
