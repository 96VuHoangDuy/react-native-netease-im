# 02 — Call List / 通话话单 (OFFICIAL RAW — Android)

> **Nguồn:** docs chính chủ NetEase Yunxin (NERTC CallKit — 通话话单, Android). Lưu verbatim để tra cứu nhanh, khỏi mở lại console/site.
> Bản "how-to đã lược dịch + relevance" của project: [`02-call-list.md`](./02-call-list.md). File này là **nguyên văn upstream**.
> ⬅️ Back to [README](./README.md).

**Đã verify khớp code thật (basesdk 10.9.52):** ticket đến dưới dạng `MsgTypeEnum.nrtc_netcall`, attachment `com.netease.nimlib.sdk.msg.attachment.NetCallAttachment`
(`getType():int` 1=audio/2=video, `getStatus():int` 1..5, `getChannelId():String`, `getDurations():List<Duration>` với `Duration.getAccid()/getDuration()` giây).
Bridge đã map ở `ReactCache.getMessageType` (`case nrtc_netcall → "call"`) + build block set `extend{callType,callStatus,callDuration,channelId}`.

---

# 通话话单 (Call Ticket)

The call component provides the call list function. After a call, you will receive the corresponding call list. The call list is an event notification message that marks the status of this call. The call list is sent in the form of an IM session type message copy. After receiving the call list, you can parse the message body and get call details such as call time.

There are 5 types of single messages of the cloud message call component, of which 4 types are **the list when it is not connected** (sent by the caller client), and 1 type is the **normal list with the call time** (sent directly by the server).

The unanswered call orders sent by the caller's client include **refusal call orders**, **busy call orders**, **timeout unanswered call orders**, and **caller cancellation call orders**.

Common call lists (top→bottom): caller-cancel, callee-reject, timeout, callee-busy, normal-with-duration.
`https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdoc%2FNERtcCallKit-CallTicket01.png`

Multi-person calls default to unencapsulated call list function.

## Use the call list function

### Method 1: Use the component's built-in ticket

#### Step 1: Open the call list CC copy
1. NetEase Console: enable IM instant messaging, audio & video calls, and **signaling** (separate sub-function).
2. Enable the call-component ticket copy in CC message settings.

#### Step 2: Send the order
After turning on, the ticket message is sent by default. The component does **not** receive/parse it — you parse it yourself like a custom message.

#### Step 3: Receive & parse (V2 API)

```java
private void registerObserver() {
    V2NIMMessageListener messageListener = new V2NIMMessageListener() {
        @Override
        public void onReceiveMessages(List<V2NIMMessage> messages) {
            if (messages != null) {
                for (V2NIMMessage msg : messages) {
                    V2NIMMessageAttachment attachment = msg.getAttachment();
                    if (attachment instanceof V2NIMMessageCallAttachment) {
                        parseForNetCall(msg, (V2NIMMessageCallAttachment) attachment);
                    }
                }
            }
        }
        @Override
        public void onSendMessage(V2NIMMessage message) {
            if (message.getSendingState() == V2NIMMessageSendingState.V2NIM_MESSAGE_SENDING_STATE_SUCCEEDED) {
                V2NIMMessageAttachment attachment = message.getAttachment();
                if (attachment instanceof V2NIMMessageCallAttachment) {
                    parseForNetCall(message, (V2NIMMessageCallAttachment) attachment);
                }
            }
        }
    };
    NIMClient.getService(V2NIMMessageService.class).addMessageListener(messageListener);
}
```

**Ticket status enum (`NERecordCallStatus`)**

| Status | Value | Explain |
|---|---|---|
| `NERecordCallStatus.COMPLETE` | 1 | Normal ticket, both entered the call and hung up. Sent by **server**. |
| `NERecordCallStatus.CANCELED` | 2 | Caller canceled after dialing. Sent by **client caller**. |
| `NERecordCallStatus.REJECTED` | 3 | Callee rejected. Client caller sends after receiving reject. |
| `NERecordCallStatus.TIMEOUT` | 4 | Callee didn't act → timeout. Sent by client caller. |
| `NERecordCallStatus.BUSY` | 5 | Callee busy → auto-reject. Client caller sends after message. |

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

```java
private void parseForNetCall(V2NIMMessage message, V2NIMMessageCallAttachment attachment) {
    if (message == null || attachment == null) return;
    String targetId = V2NIMConversationIdUtil.conversationTargetId(message.getConversationId());
    int type = attachment.getType();           // audio/video
    long channelId = attachment.getChannelId();
    int status = attachment.getStatus();        // ticket type
    List<V2NIMMessageCallDuration> durations = attachment.getDurations();
    switch (status) {
        case NERecordCallStatus.COMPLETE:
            if (durations != null) {
                for (V2NIMMessageCallDuration duration : durations) {
                    if (duration != null) {
                        String accId = duration.getAccountId();
                        int seconds = duration.getDuration();  // giây
                    }
                }
            }
            break;
        case NERecordCallStatus.CANCELED: break; // 主叫用户取消
        case NERecordCallStatus.REJECTED: break; // 被叫用户拒接
        case NERecordCallStatus.TIMEOUT:  break; // 被叫接听超时
        case NERecordCallStatus.BUSY:     break; // 被叫用户在通话中，占线
    }
}
```

### Method 2: Implement the ticket yourself
1. Turn off **server-side** ticket copy.
2. Enable room-duration message copy (`eventType=8`) in console.
3. Define your own ticket protocol (JSON), e.g. `{ "type": 1, "data": ... }`.
4. Set `setCallRecordProvider` (after set, client no longer sends component's internal ticket).

```java
NECallEngine.sharedInstance().setCallRecordProvider(new NERecordProvider() {
    @Override
    public void onRecordSend(NERecord record) {
        // 发送未成功通话话单 (only not-connected tickets come here)
    }
});
```

**`NERecord` fields:** `accid` (other end user ID), `callType` (`NECallType.AUDIO`/`VIDEO`), `callState` (`NERecordCallStatus`).

## Turn OFF the call list function
1. Turn off **server-side** ticket copy.
2. Turn off **client** ticket: `setCallRecordProvider` + empty `onRecordSend`.

```java
NECallEngine.sharedInstance().setCallRecordProvider(new NERecordProvider() {
    @Override
    public void onRecordSend(NERecord record) { /* 空实现即可 */ }
});
```
