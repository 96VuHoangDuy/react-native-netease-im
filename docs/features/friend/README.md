# Friend Module

## Scope

`NimFriend` quản lý:

- friend list
- user info local/remote
- update profile và alias
- add/delete friend
- blacklist

Ngoài ra feature contact còn liên quan tới stranger/user enrichment do native xử lý.

## Entry Points

### JS

- `src/Friend/Friend.ts`
- `src/Friend/friend.type.ts`
- `src/User/user.type.ts`

### Android

- `android/src/main/java/com/netease/im/contact/FriendListService.java`
- `android/src/main/java/com/netease/im/contact/FriendObserver.java`
- `android/src/main/java/com/netease/im/contact/BlackListObserver.java`
- `android/src/main/java/com/netease/im/UserStrangers.java`
- `android/src/main/java/com/netease/im/CacheUsers.java`

### iOS

- `ios/RNNeteaseIm/RNNeteaseIm/ContactViewController.h`
- `ios/RNNeteaseIm/RNNeteaseIm/ContactViewController.m`
- `ios/RNNeteaseIm/RNNeteaseIm/UserStrangers.m`
- `ios/RNNeteaseIm/RNNeteaseIm/CacheUsers.m`

## State And Data Flow

1. JS gọi `startFriendList()` để native bắt đầu observer và load friend list.
2. Native cache/user observer refresh lại list khi dữ liệu đổi.
3. Friend list được emit qua `observeFriend`.
4. Blacklist được emit qua `observeBlackList`.
5. Unknown/stranger user có thể được enrich riêng và emit qua `observeUserStranger`.

## Public API

- `startFriendList`
- `stopFriendList`
- `getUserInfo`
- `fetchUserInfo`
- `updateMyUserInfo`
- `updateUserInfo`
- `getFriendList`
- `addFriend`
- `addFriendWithType`
- `deleteFriend`
- `startBlackList`
- `stopBlackList`
- `getBlackList`
- `addToBlackList`
- `removeFromBlackList`

## Events

- `observeFriend`
- `observeBlackList`
- `observeUserStranger`

## Business Rules

- `getUserInfo` đọc local/native cache trước.
- `fetchUserInfo` yêu cầu dữ liệu remote.
- Add friend có 2 mode verify:
  - verify request
  - direct add
- Blacklist là flow riêng, không gộp vào friend list API.
- Stranger enrichment phụ thuộc backend cache custom:
  - iOS dùng env `API_URL` và `API_AUTH_KEY`
  - Android có lớp `CacheUsers` nhưng chưa thấy public bridge để set config

## Platform Notes

### Android

- Friend list được group/query bằng contact data provider trong `uikit/contact`.
- Refresh dựa vào `FriendDataCache` và `UserInfoHelper`.
- Blacklist observer fetch user info thiếu từ cache remote.

### iOS

- `ContactViewController` làm friend/user actions.
- `UserStrangers` debounce fetch unknown user rồi emit `observeUserStranger`.
- `CacheUsers` fetch thông tin từ backend riêng ngoài NIM SDK.

## Error Handling Notes

- Hầu hết method trả Promise từ bridge native.
- Một số thay đổi danh sách sẽ phản ánh rõ hơn qua event thay vì data trả về tức thời.
- Nếu backend enrich user không có `API_URL` hoặc `API_AUTH_KEY`, iOS sẽ lỗi ở `CacheUsers`.

## Gaps

- Repo không mô tả chuẩn backend response shape cho `CacheUsers`.
- Android và iOS không có cấu hình public đối xứng cho custom user-cache flow.
