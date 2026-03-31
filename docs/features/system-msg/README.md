# System Message Module

## Scope

`NimSystemMsg` quản lý:

- subscribe/unsubscribe system notification stream
- query danh sách system message
- unread count của system message
- xử lý accept/reject cho friend apply hoặc team invite/apply
- expose enum cho custom notification data

## Entry Points

### JS

- `src/SystemMsg/SystemMsg.ts`
- `src/SystemMsg/systemMsg.type.ts`

### Android

- `android/src/main/java/com/netease/im/login/SysMessageObserver.java`
- `android/src/main/java/com/netease/im/login/LoginService.java`

### iOS

- `ios/RNNeteaseIm/RNNeteaseIm/NoticeViewController.h`
- `ios/RNNeteaseIm/RNNeteaseIm/NoticeViewController.m`
- `ios/RNNeteaseIm/RNNeteaseIm/RNNotificationCenter.m`

## State And Data Flow

1. `startSystemMsg()` gắn delegate/observer ở native.
2. Native load hoặc nhận system message mới từ NIM SDK.
3. Dữ liệu emit về JS qua `observeReceiveSystemMsg`.
4. Unread count emit qua `observeUnreadCountChange`.
5. Nếu system/custom notification liên quan workflow đặc biệt, JS có thể dùng `observeCustomNotification`.

## Public API

- `startSystemMsg`
- `stopSystemMsg`
- `startSystemMsgUnreadCount`
- `stopSystemMsgUnreadCount`
- `querySystemMessagesBlock`
- `onSystemNotificationDeal`
- `ackAddFriendRequest`
- `passApply`
- `acceptInvite`
- `deleteSystemMessage`
- `clearSystemMessages`
- `resetSystemMessageUnreadCount`

## Events

- `observeReceiveSystemMsg`
- `observeUnreadCountChange`
- `observeCustomNotification`

## Business Rules

- JS convenience method `onSystemNotificationDeal(...)` chỉ branch cho:
  - pass friend apply
  - accept invite
  - ack add friend request
- Android `SysMessageObserver` đang bật `MERGE_ADD_FRIEND_VERIFY = true`, nghĩa là cùng một account chỉ giữ request add-friend mới nhất.
- `NIMCustomNotificationTypeEnum` còn được dùng để mô hình hóa temporary session, typing và các signal khác ngoài system message cổ điển.
- `ackAddFriendRequest(...)` có khác biệt tham số giữa iOS và Android; JS wrapper đã che bớt phần này.

## Platform Notes

### Android

- `SysMessageObserver` vừa load lịch sử, vừa dedupe, vừa merge friend verify requests.
- `LoginService` quản lý observer unread count.

### iOS

- `NoticeViewController` làm phần lớn thao tác system notification.
- `RNNotificationCenter` còn lắng nghe chat/system delegate ở cấp SDK.

## Error Handling Notes

- Một phần API dùng `timestamp` như identity phụ trợ ở iOS.
- Với accept/reject, cần xem Promise reject code từ native nếu muốn map UX chi tiết.

## Gaps

- Repo không có docs riêng cho custom notification payload ngoài enum TypeScript và source native.
