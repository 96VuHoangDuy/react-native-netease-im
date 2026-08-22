# Chatroom Module

## Scope

`NimChatroom` là module nhỏ, tập trung vào:

- login vào chatroom
- logout chatroom
- lấy chatroom info
- lấy member / danh sách member
- lấy message history

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
- `sendTextMessage`
- `getSdkAppKey` (chẩn đoán `102302`: đọc appKey SDK đang dùng lúc runtime)

## Business Rules

- Auth dùng **dynamic token** [D-020]: `accid` + `token` + `appKey` là **matched set**, lấy từ cùng
  một lần gọi `POST /client/chat/login`. Dynamic token phải đi qua `tokenProvider` +
  `authType = DYNAMIC_TOKEN`, KHÔNG truyền qua `token`/`withToken()` — sai đường là `102302 invalid
  token`. Nhánh `authType: 'static'` chỉ còn để đối chứng khi debug, hiện không dùng.
- `addrs` (link address) lấy từ backend `chatroom/addr`; bỏ trống thì SDK tự dò bằng LBS.
- `fetchMessageHistory(roomId, limit, beginTime, orderBy)` — V2 phân trang theo **thời gian**
  (`createTime` của tin cũ nhất đang có), không theo `currentMessageId` như old-gen.
- `fetchChatroomMembers` trả `{ pageToken, finished, members }`, phân trang bằng `pageToken`.
- Mobile **không** gọi `updateMemberRole` (SDK chỉ cho creator gọi) — role phòng do portal/backend
  set, mobile chỉ đọc và nghe `observeChatroomMemberRoleUpdated`.

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
