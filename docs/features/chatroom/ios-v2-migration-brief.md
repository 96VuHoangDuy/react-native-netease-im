# Brief: viết lại bridge chatroom iOS theo V2

**Ngày:** 2026-08-21 · **Dành cho:** session làm iOS · **Android đã xong và chạy thật** — nhiệm vụ là làm iOS **khớp hệt** Android, không thiết kế lại.

Đọc trước: `docs/reference/chat-room/README.md` (index docs NIM V2, có line number từng section).
Quyết định gốc: `pyeon-chinese-mobile/docs/features/chat-room/decisions/DECISIONS.md` — [D-015], **[D-020]**.
Luồng đã trace: `pyeon-chinese-mobile/docs/flows/chatroom-enter-message.md`.

## 1. Hiện trạng iOS

- `ios/RNNeteaseIm/RNNeteaseIm/ChatroomViewController.m` — dùng old-gen `[NIMSDK sharedSDK].chatroomManager`, conform `NIMChatroomManagerDelegate` nhưng **không implement delegate nào** → không có realtime.
- `fetchMessageHistory` luôn `success(@[])` (`:278`, `:332-334`) → luôn trả rỗng.
- `NTESSDKConfigDelegate.m:33` trả `nil` (dynamic token cũ) → bỏ hẳn.
- ⚠️ nghi bug: `fetchMessageHistory` dùng `NIMSessionTypeSuperTeam` khi tra `currentMessageId`.
- **KHÔNG cần pod mới**: `NIMSDK_LITE 10.9.53` đã có sẵn V2 — `V2NIMChatroomClient` khai trong
  `V2NIMChatroomServiceProtocol.h:122`. Tránh được `pod install` (xem Critical Rules repo mobile).

## 2. Auth — phần dễ sai nhất, đọc kỹ

**Token backend cấp là DYNAMIC (JWT ~120 ký tự), KHÔNG phải static.** [D-020]

Đối chứng A/B đã chạy trên Android (cùng accid/token/appKey, chỉ đổi kiểu auth):
`dynamic` → `ENTERED`; `static` (`withToken`) → **`102302 invalid token`**.

iOS phải làm y hệt (`V2NIMChatroomServiceProtocol.h`):
```objc
V2NIMChatroomLoginOption *opt = [V2NIMChatroomLoginOption new];
opt.authType = V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN;   // :828
opt.tokenProvider = <đối tượng conform V2NIMChatroomTokenProvider>;  // :831, protocol tại :767

V2NIMChatroomEnterParams *p = [V2NIMChatroomEnterParams new];         // :872
p.accountId = accid;      // KHÔNG set p.token khi dùng dynamic
p.loginOption = opt;      // :887
p.linkProvider = ...;     // :889 — addr từ backend, có thể nil
p.enableLbs = YES;        // :891
```

Ba giá trị `accid` + `token` + `appKey` là **matched set**, phải lấy từ **cùng một lần** gọi
`POST /client/chat/login`. JS lo việc này rồi (`ChatroomDebugScreen.resolveCredential`), native chỉ nhận.

## 3. Contract JS ↔ native — iOS phải khớp từng tên

Đây là phần bắt buộc giống Android, nếu lệch thì JS phải rẽ nhánh platform (không chấp nhận).

### 3.1 Method (7)

| JS gọi | Native method | Tham số |
|---|---|---|
| `NimChatroom.login` | `loginChatroom` | 1 object: `{roomId, nickname, avatar, accid, token, appKey, authType?, addrs?}` |
| `logout` | `logoutChatroom` | `roomId` |
| `fetchChatroomInfo` | `fetchChatroomInfo` | `roomId` |
| `fetchChatroomMember` | `fetchChatroomMember` | `roomId`, **mảng** accountId |
| `fetchChatroomMembers` | `fetchChatroomMembers` | `roomId`, `limit`, `pageToken` |
| `fetchMessageHistory` | `fetchMessageHistory` | `roomId`, `limit`, **`beginTime`**, `orderBy` |
| `sendTextMessage` | `sendChatroomTextMessage` | `roomId`, `text` |

`fetchMessageHistory` phân trang theo **thời gian**, không theo messageId như old-gen.
`beginTime = 0` + `DESC` = từ tin mới nhất; truyền mốc = lấy tin **cũ hơn** mốc đó.

### 3.2 Event (12) — tên hằng, JS nghe qua `NIMEventListenerEnum`

`observeChatroomStatus` · `observeChatroomKicked` · `observeChatroomMessage` ·
`observeChatroomSendMessage` · `observeChatroomMessageRevoked` · `observeChatroomMemberIn` ·
`observeChatroomMemberOut` · `observeChatroomMemberRoleUpdated` · `observeChatroomMemberInfoUpdated` ·
`observeChatroomSelfBanned` · `observeChatroomChatBanned` · `observeChatroomInfoUpdated`

Delegate iOS map 1-1 với Android (`V2NIMChatroomServiceProtocol.h`): `onChatroomStatus:` `:239`,
`onChatroomEntered` `:245`, `onChatroomExited:` `:252`, `onChatroomKicked:` `:259`,
`onSendMessage:` `:536`, `onReceiveMessages:` `:544`, `onChatroomMemberEnter:` `:551`,
`onChatroomMemberExit:` `:558`, `onChatroomMemberRoleUpdated:` `:566`, `onChatroomMemberInfoUpdated:` `:574`,
`onSelfChatBannedUpdated:` `:581`, `onSelfTempChatBannedUpdated:` `:589`, `onChatroomInfoUpdated:` `:597`,
`onChatroomChatBannedUpdated:` `:604`, `onMessageRevokedNotification:` `:612`.
`onChatroomTagsUpdated:` `:620` — Phase 1 **không dùng**, bỏ qua để khỏi đẻ event thừa.

### 3.3 Payload — key phải giống hệt (JS/store đọc theo tên này)

**message**: `msgId` `roomId` `fromAccount` `text` `msgType` `subType` `serverExtension` `sendingState` `timestamp` `isSelf`
**member**: `roomId` `userId` `nickname` `avatar` `avatarThumbnail` `type` `isOnline` `isBlocked` `isMuted` `isTempMuted` `tempMuteDuration` `enterTime` `updateTime`
**info**: `roomId` `name` `announcement` `broadcastUrl` `creatorAccountId` `serverExtension` `onlineUserCount` `isChatBanned` `isValidRoom` (+`isLoginSuccess`, +`selfMember` khi login)

`type` của member = tên role rút gọn: `NORMAL` / `CREATOR` / `MANAGER` / `NORMAL_GUEST` / `ANONYMOUS_GUEST` / `VIRTUAL`
(bỏ tiền tố `V2NIM_CHATROOM_MEMBER_ROLE_`).

Event bọc thêm `roomId` ở cấp ngoài; `observeChatroomMessage` gói mảng trong key `messages`.

## 4. Bẫy đã trả giá trên Android — đừng lặp lại

1. **accid là `user.imAccid`, KHÔNG phải `user.userId`** → sai thì `102404 account not exist`.
2. **Dynamic token phải qua `tokenProvider`**, không qua `token` property → `102302 invalid token`.
3. **`exit` phải kèm huỷ instance**, nếu không listener phòng cũ vẫn bắn → tin nhắn nhân đôi khi
   vào lại phòng. Android: `V2NIMChatroomClient.destroyInstance(instanceId)`.
4. **Vào lại phòng đang mở phải dọn instance cũ trước** khi tạo instance mới.
5. appKey: backend cấp appKey riêng (`e002bccb…`) khác appKey manifest Android (`2761e59…`) — nhưng
   runtime đã khớp sẵn nên **không phải nguyên nhân lỗi**. iOS kiểm lại xem có cơ chế tương đương không.

## 5. Mã lỗi hay gặp

| Code | Nghĩa | Nguyên nhân thật |
|---|---|---|
| `102302` | invalid token | dùng static thay vì dynamic; hoặc bộ credential không matched set |
| `102404` | account not exist | nhầm `userId` với `imAccid` |
| `113404` | chatroom not exist | roomId không tồn tại / đã đóng |

Đọc chuỗi `observeChatroomStatus`: `CONNECTING → CONNECTED → ENTERING → ENTERED` là đủ.
Dừng ở `DISCONNECTED` kèm code = mạng và địa chỉ đều ổn, lỗi ở credential/auth.
SDK tự retry ~3 vòng trước khi bỏ cuộc — bình thường.

## 6. Test

Dùng lại màn debug có sẵn (đã dựng cho Android, chạy chung được):
`pyeon-chinese-mobile/src/screens/Chat/ChatroomDebug/ChatroomDebugScreen.tsx` — route `CHATROOM_DEBUG`,
vào bằng cách bấm phòng ở section "Phòng chat:" tab Tin nhắn.

Ma trận đầy đủ: `pyeon-chinese-mobile/docs/features/chat-room/plans/test-android-bridge.md`.
Android đã pass nhóm A (7/7) và B (3/4). iOS phải đạt ít nhất bằng, cộng **gửi/nhận chéo Android ↔ iOS**.

## 7. Quy tắc repo bắt buộc

- **KHÔNG `pod install` trần** — dùng `yarn ios:sim:prepare` (chạy trần sẽ bật nhầm New Architecture
  và gỡ mất script phase `[SimFix] Switch NIMSDK/YXArtemis Binaries`).
- **KHÔNG patch-package.** Sửa xong phải sync source sang **cả** `node_modules/react-native-netease-im`
  của app, nếu không build/Metro vẫn dùng bản cũ.
- Sửa `.m/.h` → phải build lại; sửa `.ts` → chỉ reload Metro.
- File mới đủ 3 tầng comment (RULE-COMMENT-01). Sửa chặng nào trong
  `docs/flows/chatroom-enter-message.md` thì cập nhật flow doc trong cùng task.
- Regression chat P2P/team sau khi sync — bridge là shared code.
