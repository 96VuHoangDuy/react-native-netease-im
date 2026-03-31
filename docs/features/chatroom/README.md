# Chatroom Module

## Scope

`NimChatroom` là module nhỏ, tập trung vào:

- login vào chatroom
- logout chatroom
- lấy chatroom info
- lấy member / danh sách member
- lấy message history

Không thấy JS event stream riêng cho chatroom; API hiện mang tính imperative.

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

1. JS gọi `login(...)` với `roomId`, `nickname`, `avatar`.
2. Native chatroom manager login vào room.
3. JS gọi các API fetch info/member/history khi cần.

## Public API

- `login`
- `logout`
- `fetchChatroomInfo`
- `fetchChatroomMember`
- `fetchChatroomMembers`
- `fetchMessageHistory`

## Business Rules

- Payload login chatroom hiện không chứa dynamic token trong JS type.
- iOS `NTESSDKConfigDelegate.dynamicChatRoomTokenForAccount:room:appKey:` đang trả `nil`.
- `fetchMessageHistory(...)` cho phép truyền `currentMessageId` và `orderBy`.

## Platform Notes

### Android

- Chatroom bridge nằm chung trong `RNNeteaseImModule.java`.

### iOS

- `ChatroomViewController` bọc trực tiếp `chatroomManager`.
- `fetchChatroomMember` còn map thêm thông tin user/friend/blacklist từ user manager.

## Error Handling Notes

- API trả Promise; không có event side channel riêng được type hóa trong JS.

## Gaps

- Repo chưa mô tả rõ chatroom auth strategy nếu cần token động.
- Không có docs consumer flow cho chatroom reconnect.
