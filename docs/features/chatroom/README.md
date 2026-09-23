# Chatroom Module

## Scope

`NimChatroom` là module nhỏ, tập trung vào:

- login vào chatroom
- logout chatroom
- lấy chatroom info
- lấy member / danh sách member
- lấy message history
- cấm chat / danh sách đen thành viên (chỉ creator + administrator phòng)

API imperative gọi qua Promise; event realtime của phòng đi qua `NativeAppEventEmitter` với các tên
`observeChatroom*` trong `NIMEventListenerEnum` (`src/utils/eventListener.type.ts`).

**Thế hệ SDK: V2 (`V2NIMChatroomClient`)** trên cả Android và iOS.

## Entry Points

### JS

- `src/Chatroom/chatroom.ts`
- `src/Chatroom/chatroom.type.ts`

### Android

- `android/src/main/java/com/netease/im/RNNeteaseImModule.java`

### iOS

- `ios/RNNeteaseIm/RNNeteaseIm/ChatroomViewController.h`
- `ios/RNNeteaseIm/RNNeteaseIm/ChatroomViewController.m`

## State And Data Flow

1. JS gọi `login(...)` với matched set `accid`/`token`/`appKey` + `roomId`, `nickname`, `avatar`.
2. Native tạo `V2NIMChatroomClient` riêng cho phòng, gắn listener, rồi `enter` bằng dynamic token.
3. JS gọi các API fetch info/member/history khi cần; event realtime bắn qua `observeChatroom*`.

## Public API

- `login`
- `logout`
- `fetchChatroomInfo`
- `fetchChatroomMember`
- `fetchChatroomMembers`
- `fetchMessageHistory`
- `sendTextMessage(roomId, text, serverExtension?)` — tham số 3 optional, chở metadata của app
- `getSdkAppKey` (chẩn đoán `102302`: đọc appKey SDK đang dùng lúc runtime)
- `setMemberChatBanned` — cấm chat vĩnh viễn / gỡ ⚠️ chưa verify runtime
- `setMemberTempChatBanned` — cấm chat tạm thời / gỡ ⚠️ chưa verify runtime
- `setMemberBlocked` — thêm/gỡ danh sách đen NIM ⚠️ chưa verify runtime

## Business Rules

- Auth dùng **dynamic token** [D-020]: `accid` + `token` + `appKey` là **matched set**, lấy từ cùng
  một lần gọi `POST /client/chat/login`. Dynamic token phải đi qua `tokenProvider` +
  `authType = DYNAMIC_TOKEN`, KHÔNG truyền qua `token`/`withToken()` — sai đường là `102302 invalid
  token`. Nhánh `authType: 'static'` chỉ còn để đối chứng khi debug, hiện không dùng.
- `addrs` (link address) lấy từ backend `chatroom/addr`; bỏ trống thì SDK tự dò bằng LBS.
- `fetchMessageHistory(roomId, limit, beginTime, orderBy)` — V2 phân trang theo **thời gian**
  (`createTime` của tin cũ nhất đang có), không theo `currentMessageId` như old-gen.
- `fetchChatroomMembers` trả `{ pageToken, finished, members }`, phân trang bằng `pageToken`.
- **`serverExtension` là kênh metadata của app đi kèm tin.** Chuỗi tuỳ ý (thường là JSON), bên gửi
  truyền vào tham số 3 của `sendTextMessage`, bên nhận đọc lại ở `NIMChatroomMessage.serverExtension`
  — đã serialize sẵn từ trước ở cả 2 platform. Rỗng thì **không set** (absent, không phải `""`).
  ⚠️ chưa verify runtime.

  **Quy ước bắt buộc — JSON namespaced theo tính năng.** Kênh này **dùng chung cho mọi tính năng**,
  nên nội dung luôn là một JSON object có khoá phân loại, **không bao giờ JSON phẳng**:
  ```json
  { "quote": { ... } }
  ```
  Tính năng sau thêm khoá riêng ở cùng cấp. Bên gửi phải **đọc-merge-ghi** chứ không ghi đè cả
  chuỗi; bên đọc chỉ lấy khoá của mình và bỏ qua khoá lạ. Ghi phẳng thì tính năng thứ hai xuất hiện
  là hai bên xoá nhau — **im lặng, không lỗi**, và tin cũ đã gửi thì không migrate được.
  Ruột `{...}` của `quote` chưa chốt (chờ MB-3 có design).
- **Tên/avatar người gửi đi KÈM message**: `senderNickname?` / `senderAvatar?` lấy từ
  `V2NIMChatroomMessage.getUserInfoConfig()` (Android) — **không** phải trường cấp 1 của message.
  Cả 3 đường đều có (realtime, history, tin tự gửi) vì cùng đi qua một hàm serialize.
  Optional: vắng khi SDK không đính kèm. Ưu tiên dùng thay vì tra member list — member list trả
  `nickname` rỗng với CREATOR và không tra được người đã rời phòng.
- **Tin hệ thống vào/ra/ban đi qua `observeChatroomMessage`**, không phải callback riêng. Message có
  `msgType = V2NIM_MESSAGE_TYPE_NOTIFICATION` mang dữ liệu trong **attachment**, `text` luôn rỗng và
  `subType: 0`. Bridge serialize ra các field flat: `notificationType`, `targetIds`, `targetNicks`,
  `operatorId`, `operatorNick`, `notificationExtension` — absent khi rỗng.
  `notificationType` = tên hằng `V2NIMChatroomMessageNotificationType` bỏ tiền tố
  `V2NIM_CHATROOM_MESSAGE_NOTIFICATION_TYPE_`, 19 giá trị (`javap`).
  Riêng notification **loại ban** bơm thêm `chatBanned`, `tempChatBanned`,
  `tempChatBannedDuration` — cần cho nhánh GỠ ban, vì gỡ vĩnh viễn và hết hạn ban tạm nhìn giống
  hệt nhau nếu thiếu [GAP-5]. `tempChatBannedDuration` đơn vị **GIÂY ở cả 2 platform**
  (`long` Android / `NSTimeInterval` iOS, không quy đổi — khác `timestamp` là chỗ phải ×1000),
  `0` = đã gỡ. ⚠️ chưa verify runtime.
  `V2NIMChatroomMemberEnterNotificationAttachment` cũng có 3 property này nhưng **cố ý không bơm**:
  tin vào/ra đang chạy thật, thêm field vào đó là rủi ro regression thuần.
- ⚠️ **`observeChatroomMemberIn` KHÔNG bắn nếu chưa bật cấu hình console Yunxin**
  "聊天室用户进出消息系统下发" (chatroom user in/out message system delivery) —
  `raw-docs/chat-room-auth.md:33` cho `onChatroomMemberEnter`, `:2989` cho `onChatroomMemberExit`.
  Đây là **cấu hình console, không phải tham số `enter()`**: `V2NIMChatroomEnterParams` không có cờ
  nào liên quan (`javap` — chỉ `anonymousMode`, `enableLbs`, `notificationExtension`).
  Đo thật 2026-08-26: exit CÓ, enter KHÔNG, giống nhau trên cả 2 platform.
  ⇒ Dùng `notificationType: MEMBER_ENTER` từ đường notification, đừng chờ callback.
- ⚠️ **`senderNickname`/`senderAvatar` không tự truyền sang người nhận.** SDK điền bản local cho
  chính người gửi (tin `SUCCEEDED` của mình có đủ), nhưng tin nhận cross-device thì vắng — đo thật
  2026-08-26. **Cả hai platform đều tự đính khi gửi** — đã đo có tác dụng thật: tin iOS-gửi sang
  máy Android nhận có đủ 2 field (msgId `2d4749d4-b8b0-49f3-9a5f-f46d144a3d0b`, 20:56), trong khi
  lượt trước cùng điều kiện thì không. `userInfoTimestamp` set = epoch **giây** ở cả hai.
  Chi phí hai bên khác nhau:
  - **iOS — public API.** Property `userInfoConfig` là readwrite (`ChatroomViewController.m:598`).
  - **Android — INTERNAL API, phải fail-soft.** Public API không có đường: `V2NIMChatroomMessage`
    chỉ có getter, `V2NIMSendChatroomMessageParams` không nhận. Đường duy nhất là
    `V2NIMChatroomMessageBuilder.userInfoConfig()` — lớp ở `com.netease.nimlib.v2.chatroom.builder`,
    **ngoài** namespace `sdk`, nên nâng SDK có thể đổi chữ ký/package hoặc bị obfuscate. Vì vậy
    `withSelfUserInfo()` bọc try/catch và trả message gốc khi hỏng: **tin vẫn gửi, chỉ mất tên**.
    Đừng bỏ catch, và khi nâng NIM SDK thì kiểm lại đúng chỗ này trước.

  Hiển thị tên **không** phụ thuộc hai field này — store lo bằng `RULE-NAME-01` + member list.
  Tin gửi trước khi bật cơ chế này vẫn vắng field vĩnh viễn trong history; không phải bug.
- `ChatroomMemberType` = tên hằng `V2NIMChatroomMemberRole` bỏ tiền tố, đúng 6 giá trị SDK V2 trả:
  `NORMAL` `CREATOR` `MANAGER` `NORMAL_GUEST` `ANONYMOUS_GUEST` `VIRTUAL`
  (verify bằng `javap` trên `chatroom-10.9.52.aar`). `GUEST`/`LIMIT` trong enum JS là di sản
  old-gen, SDK V2 **không bao giờ** trả — chưa xoá vì chưa rà hết chỗ dùng.
- Mobile **không** gọi `updateMemberRole` (SDK chỉ cho creator gọi) — role phòng do portal/backend
  set, mobile chỉ đọc và nghe `observeChatroomMemberRoleUpdated`.

### Cấm chat / danh sách đen (3 hàm `setMember*`) — ⚠️ chưa verify runtime

Ba hàm này gọi **thẳng Client SDK**, không đi qua middleware TQ. Đường này chỉ dùng được cho
**admin phòng thao tác từ mobile** (có sẵn session chatroom); nhánh portal vẫn phải chờ middleware
vì web không giữ session chatroom.

**Quyền — giống hệt nhau cho cả 3 hàm** (nguồn: `docs/reference/chat-room/raw-docs/member-management.md`):

- Chỉ **creator** và **administrator** của phòng gọi được.
- Administrator **không** thao tác được lên creator và administrator khác — chỉ creator mới làm được.
  Tức admin phòng chỉ cấm được **thành viên thường**.
- Không thao tác được lên **fictitious user** và **anonymous tourist**.

Bridge **không** tự kiểm quyền trước — gọi sai thì SDK trả lỗi, JS phải bắt reject.

Ngữ nghĩa cần nhớ:

- `tempChatBannedDuration` đơn vị **giây**, tối đa 30 ngày một lần, `0` = gỡ. Set lại là **ghi đè**
  hạn cũ, không cộng dồn. Bridge không quy đổi đơn vị.
- Cấm vĩnh viễn và cấm tạm là **hai trạng thái độc lập** ở server: gỡ cấm vĩnh viễn không đụng tới
  hạn cấm tạm đang chạy.
- **Danh sách đen chạy HAI TẦNG** [D-032 — supersede phần cấm của D-019]. Blocked ở NIM là bị
  **đá khỏi phòng** (`observeChatroomKicked`) và mất kết nối, không chỉ mất quyền gửi.
  - **DB backend = nguồn sự thật.** Giữ trạng thái qua phiên, portal đọc từ đó, và `/me` trả
    `isBlacklisted` làm lớp chặn thứ hai — NIM chưa sync thì app vẫn chặn đúng theo DB.
  - **NIM `setMemberBlocked` = enforcement tức thì.** Kick + chặn nhận, ngay lập tức.
  - **Lệch nhau thì DB thắng.** NIM blocked mà DB không có ⇒ rác, phải gỡ ở NIM.

  ⇒ `setMemberBlocked` gọi **đi kèm** API blacklist của backend, **không gọi đơn lẻ** — gọi lẻ thì
  người bị chặn vào lại được ở phiên sau, hoặc portal không thấy.

Event phát sinh: mọi thành viên nhận `observeChatroomMemberInfoUpdated`; riêng người bị cấm nhận
`observeChatroomSelfBanned`. ⚠️ Callback SDK `onChatroomMemberInfoUpdated` **chỉ mang object member,
KHÔNG mang notification type** (verify bằng `javap` trên `chatroom-10.9.52.aar`) — nên JS không biết
member đổi vì lý do gì, phải tự so `isMuted`/`isTempMuted`/`isBlocked` với giá trị đang giữ.

## Platform Notes

### Android

- Artifact riêng bắt buộc: `com.netease.nimlib:chatroom` (`android/build.gradle`) — `basesdk` KHÔNG
  chứa `V2NIMChatroomClient`.
- Logic nằm ở `android/src/main/java/com/netease/im/chatroom/ChatroomV2Service.java` (static facade
  theo mẫu `CallService.java`); `RNNeteaseImModule.java` chỉ forward `@ReactMethod`.
- Event emit qua `ReactCache.emit(...)` — tên hằng khai ở `ReactCache.java`, rơi vào nhánh `default`
  nên không bị debounce/gộp.
- Mỗi `roomId` = 1 instance SDK; `exit()` phải kèm `destroyInstance()`, nếu không listener phòng cũ
  vẫn bắn sau khi thoát.

### iOS

- 3 hàm `setMember*`: chữ ký iOS trùng arity với Android, và `tempChatBannedDuration` là **giây** ở
  cả hai (`NSInteger` bên iOS `V2NIMChatroomServiceProtocol.h:395`, `long` bên Android) → bridge
  truyền thẳng, **không** quy đổi. Đừng nhầm với `timestamp`/`beginTime` (iOS `NSTimeInterval` giây
  vs Android ms) — hai chỗ đó mới phải nhân/chia 1000.
- Xác nhận độc lập phía iOS cho ghi chú notification type ở trên: header khai
  `onChatroomMemberInfoUpdated:(V2NIMChatroomMember *)member` — **chỉ có member, không có type**.
  Notification type tới JS bằng **đường message**: cả hai bridge đã serialize `attachment` của tin
  `NOTIFICATION` ra các field flat (Android `ChatroomV2Service.putNotification`, iOS
  `RNNIMFromMessage`) — xem mục shape ở Business Rules. Callback member-info vẫn không mang type,
  nên muốn biết "vì sao member đổi" thì đọc `notificationType` của message, không đọc callback.
- Logic nằm ở `ios/RNNeteaseIm/RNNeteaseIm/ChatroomViewController.m` (viết lại theo V2, giữ nguyên
  tên class vì thêm file mới sẽ phải chạy lại `pod install`); `RNNeteaseIm.m` chỉ forward
  `RCT_EXPORT_METHOD`. `NIMSDK_LITE 10.9.53` đã có sẵn `V2NIMChatroomClient` — **không cần pod mới**.
- Event emit qua `bridge.eventDispatcher sendDeviceEventWithName:` — cùng đường với các `observe*`
  khác của module, không có debounce.
- Delegate V2 **không truyền roomId** → mỗi phòng gắn một `RNNIMChatroomListener` riêng mang sẵn
  roomId (tương đương listener factory bên Android). Listener + token/link provider phải giữ strong,
  SDK không retain.
- **Đơn vị thời gian:** SDK iOS trả `NSTimeInterval` **giây**, Android trả long **mili-giây**. Bridge
  iOS quy về ms khi serialize (`timestamp`/`enterTime`/`updateTime`/`revokeTime`) và chia lại 1000 khi
  nhận `beginTime` — để JS không phải rẽ nhánh platform.
- **Chuỗi enum:** iOS enum là số, Android trả `enum.name()`. Bridge iOS map tay sang đúng chuỗi
  Android. ⚠️ Riêng tin nhắn dạng tip: Android là `V2NIM_MESSAGE_TYPE_TIPS`, header iOS là
  `V2NIM_MESSAGE_TYPE_TIP` — iOS trả theo **Android** để hai bên trùng nhau.
- `getChatroomInfo:` / `checkChatroomLoginStatus:` trong `ChatroomViewController` là API **legacy**
  cho danh sách recent session (`NIMViewController.handleSessionChatroom`), không thuộc bridge V2.
  `getChatroomInfo:` vẫn dùng old-gen `chatroomManager` vì cần info của phòng CHƯA vào.
- Mỗi `roomId` = 1 instance SDK; `exit` phải kèm `destroyInstance:`, và vào lại phòng đang mở phải
  dọn instance cũ trước — giống Android.

## Error Handling Notes

- API trả Promise; không có event side channel riêng được type hóa trong JS.

## Gaps

- Chưa có docs consumer flow cho chatroom reconnect.
- `tokenProvider` (cả 2 platform) trả token cố định của lần enter — token hết hạn giữa phiên thì
  reconnect fail. Phase 1 chưa gặp vì phiên chatroom ngắn.
- iOS: `NTESSDKConfigDelegate.dynamicChatRoomTokenForAccount:room:appKey:` (trả `nil`) là đường
  dynamic token của old-gen, sau khi bridge chuyển V2 thì thành code chết — chưa xoá.
