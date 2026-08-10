# 02 — Call List / 话单 (OFFICIAL RAW — iOS)

> **Nguồn:** docs chính chủ NetEase Yunxin (NERTC CallKit — 话单, iOS). Lưu verbatim để tra cứu nhanh.
> Bản "how-to đã lược dịch + relevance" của project: [`02-call-list.md`](./02-call-list.md). File này là **nguyên văn upstream**.
> ⬅️ Back to [README](./README.md).

> ⚠️ **Lưu ý V1 vs V2 (quan trọng cho project này):** docs upstream mô tả **V2 API** (`V2NIMMessage` + `V2NIMMessageCallAttachment` + `V2NIMMessageListener`).
> Nhưng pipeline message iOS của app đang chạy **V1** (`ConversationViewController.m refrashMessage:From:` dùng `NIMMessage`/`NIMMessageType`).
> → **Tên class attachment V1 trên iOS chưa xác nhận** (log `CALLREC-DEBUG` iOS chưa bắt được — cần rebuild iOS + reproduce). Schema field (type/channelId/status/durations) thì giống nhau.
> Android đã verify: `MsgTypeEnum.nrtc_netcall` + `NetCallAttachment` (V1). iOS cần log để chốt tương đương.

---

# 话单 (Call Ticket)

The call component provides the call list function. After a call, you will receive the corresponding call list. The call list is an event notification message that marks the status of this call. The call list is sent in the form of an IM session type message copy.

There are 5 types of single messages, of which 4 are **not-connected tickets** (sent by the caller client), and 1 is the **normal ticket with call time** (sent directly by the server).

Not-connected tickets from the caller: **reject**, **busy**, **timeout**, **caller-cancel**.

Common tickets (top→bottom): caller-cancel, callee-reject, timeout, callee-busy, normal-with-duration.
`https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdoc%2FNERtcCallKit-CallTicket01.png`

Multi-person calls default to unencapsulated call list.

## Use the call list function

### Method 1: Use the component's built-in ticket

#### Step 1: Open the call list CC copy
1. NetEase Console: enable IM instant messaging, audio & video calls, and **signaling** (separate sub-function).
2. Enable the call-component ticket copy in CC message settings.

#### Step 2: Send the order
After turning on, the ticket is sent by default. The component does **not** receive/parse it — you parse it yourself like a custom message.

#### Step 3: Receive & parse (V2 API)

```objc
@interface NEMenuViewController ()<V2NIMMessageListener>
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
        [self assmebleRecordWithV2Message:message withCaller:YES];
    }
}
- (void)onReceiveMessages:(NSArray<V2NIMMessage *> *)messages {
    for (V2NIMMessage *message in messages) {
        if ([message.senderId isEqualToString:[NEAccount shared].userModel.imAccid]) {
            [self assmebleRecordWithV2Message:message withCaller:YES];
            return;
        }
        [self assmebleRecordWithV2Message:message withCaller:NO];
    }
}
@end
```

**Ticket status enum (`NIMRtcCallStatus`)**

| Status | Value | Explain |
|---|---|---|
| `NIMRtcCallStatusComplete` | 1 | Normal ticket, both entered the call and hung up. Sent by **server**. |
| `NIMRtcCallStatusCanceled` | 2 | Caller canceled after dialing. Sent by **client caller**. |
| `NIMRtcCallStatusRejected` | 3 | Callee rejected. Client caller sends after receiving reject. |
| `NIMRtcCallStatusTimeout` | 4 | Callee didn't act → timeout. Sent by client caller. |
| `NIMRtcCallStatusBusy` | 5 | Callee busy → auto-reject. Client caller sends after message. |

Ticket = IM message CC of **session type** (`eventType = 1`). `msgType` = `NRTC_NETCALL`. Details in `attach`:

| Field | Type | Example | Explain |
|---|---|---|---|
| type | number | 1 | 1=Audio call, 2=Video call |
| channelId | number | 123 | Room ID |
| status | number | 1 | Call status (1=complete,2=canceled,3=rejected,4=timeout,5=busy) |
| durations | JSON Array | — | Per-member: `accid` + `duration` (seconds) |

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
- (void)assmebleRecordWithV2Message:(V2NIMMessage *)message withCaller:(BOOL)isCaller {
    V2NIMMessageCallAttachment *recordObject = (V2NIMMessageCallAttachment *)message.attachment;
    NIMRtcCallType type = (NIMRtcCallType)recordObject.type;         // audio/video
    NIMRtcCallStatus status = (NIMRtcCallStatus)recordObject.status;  // ticket type
    NSArray<V2NIMMessageCallDuration *> *durations = recordObject.durations;
    switch (status) {
        case NIMRtcCallStatusComplete: break; // 成功接听
        case NIMRtcCallStatusCanceled: break; // 主叫用户取消
        case NIMRtcCallStatusRejected: break; // 被叫用户拒接
        case NIMRtcCallStatusTimeout:  break; // 被叫接听超时
        case NIMRtcCallStatusBusy:     break; // 被叫用户在通话中，占线
        default: break;
    }
}
```

### Method 2: Implement the ticket yourself
1. Turn off **server-side** ticket copy.
2. Enable room-duration message copy (`eventType=8`) in console.
3. Define your own ticket protocol (JSON), e.g. `{ "type": 1, "data": ... }`.
4. Set `setCallRecordProvider` (after set, client no longer sends component's internal ticket).

```objc
[[NECallEngine sharedInstance] setCallRecordProvider:self];
- (void)onRecordSend:(NERecordConfig *)config {
    // 发送未成功通话话单 (only not-connected tickets come here)
}
```

**`NERecordConfig` fields:** `accid` (NSString, other end user ID), `callType` (`NECallType.AUDIO`/`VIDEO`), `callState` (`NIMRtcCallStatus`).

## Turn OFF the call list function
1. Turn off **server-side** ticket copy.
2. Turn off **client** ticket: `setCallRecordProvider` + empty `onRecordSend`.

```objc
[[NECallEngine sharedInstance] setCallRecordProvider:self];
- (void)onRecordSend:(NERecordConfig *)config { /* 空实现即可 */ }
```
