# 02 — Call List / Call Ticket (话单)

> After a call, the Call Kit produces a **call ticket** — an IM event-notification message recording the call outcome (connected? duration? type?). It arrives as an IM session message copy (CC).
> ⬅️ Back to [README](./README.md) · Integration: [`01`](./01-integration-android.md).

There are **5 ticket types**: 4 "not-connected" tickets sent by the **caller client** (reject / busy / timeout / caller-cancel) and 1 "normal ticket with duration" sent **directly by the server**.

> Multi-person calls do **not** ship a packaged call-list function — implement it yourself if needed.

---

## Method 1 — Use the component's built-in ticket

### Step 1: Enable ticket copy
In the [NetEase console](https://app.yunxin.163.com/index): enable IM instant messaging, audio & video calls, and **signaling** (enabled separately). Then enable the call-component ticket single-copy in CC message settings.

### Step 2: Sending
Once enabled, the ticket message is sent automatically. The component does **not** receive/parse it for you — you parse it like a normal IM message.

### Step 3: Receive & parse
Tickets arrive through the standard NIM message listener:

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

### Ticket status types (`NERecordCallStatus`)

| Status | Value | Meaning | Sent by |
|---|:---:|---|---|
| `COMPLETE` | 1 | Normal ticket, both sides entered the call and hung up | Server |
| `CANCELED` | 2 | Caller canceled the call after dialing | Client (caller) |
| `REJECTED` | 3 | Callee rejected | Client (caller) |
| `TIMEOUT`  | 4 | Callee didn't act → timeout | Client (caller) |
| `BUSY`     | 5 | Callee was on another call → auto-rejected | Client (caller) |

The ticket is sent as an IM message CC of **session type** (`eventType = 1`). Its `msgType` is `NRTC_NETCALL`. Call details live in the `attach` field:

| Field | Type | Meaning |
|---|---|---|
| `type` | number | 1 = audio, 2 = video |
| `channelId` | number | Room ID |
| `status` | number | Ticket status (table above) |
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

Parse example (e.g. for RecyclerView rendering):

```java
private void parseForNetCall(V2NIMMessage message, V2NIMMessageCallAttachment attachment) {
    if (message == null || attachment == null) return;

    String targetId = V2NIMConversationIdUtil.conversationTargetId(message.getConversationId());
    int type = attachment.getType();              // 1 audio / 2 video
    long channelId = attachment.getChannelId();   // room ID
    int status = attachment.getStatus();          // ticket type
    List<V2NIMMessageCallDuration> durations = attachment.getDurations();

    switch (status) {
        case NERecordCallStatus.COMPLETE:         // answered successfully
            if (durations != null) {
                for (V2NIMMessageCallDuration d : durations) {
                    if (d != null) {
                        String accId = d.getAccountId();
                        int seconds = d.getDuration();
                    }
                }
            }
            break;
        case NERecordCallStatus.CANCELED: break;  // caller canceled
        case NERecordCallStatus.REJECTED: break;  // callee rejected
        case NERecordCallStatus.TIMEOUT:  break;  // callee timeout
        case NERecordCallStatus.BUSY:     break;  // callee busy
    }
}
```

---

## Method 2 — Implement the ticket yourself

If the built-in ticket doesn't fit:

1. Turn off the **server-side** ticket copy.
2. Enable the room-duration message copy (`eventType=8`) in the console.
3. Define your own ticket protocol (usually JSON), e.g. `{ "type": 1, "data": ... }`.
4. Set `setCallRecordProvider` to implement your own ticket logic. After setting it, the client no longer sends the component's internal ticket — you send your own via `NERecordProvider`:

```java
NECallEngine.sharedInstance().setCallRecordProvider(new NERecordProvider() {
    @Override
    public void onRecordSend(NERecord record) {
        // send your own not-connected ticket
    }
});
```

`NERecord` fields:

| Field | Type | Meaning |
|---|---|---|
| `accid` | string | The other end's user ID |
| `callType` | int | `NECallType.AUDIO` / `NECallType.VIDEO` |
| `callState` | int | Ticket type (`NERecordCallStatus`) |

---

## Turn OFF the call-list feature

1. Turn off the **server-side** ticket copy.
2. Turn off **client** ticket sending — set `setCallRecordProvider` with an empty `onRecordSend`:

```java
NECallEngine.sharedInstance().setCallRecordProvider(new NERecordProvider() {
    @Override
    public void onRecordSend(NERecord record) {
        // empty = do not send
    }
});
```
