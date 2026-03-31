# Architecture

## Snapshot

`react-native-netease-im` là thư viện React Native bọc NetEase NIM SDK nhưng chứa nhiều logic native tự quản lý:

- đăng nhập và bootstrap observer
- recent session list
- gửi/nhận message
- custom attachment và custom notification
- audio record/playback
- friend, blacklist, team, system message
- chatroom
- push open event
- cache và enrich user info cho stranger/customer service/chatbot

Repo này không chứa app demo hoặc màn hình React. Phần JS chỉ expose API imperative qua `NativeModules`.

## Layer Model

```text
JS modules (index.ts, src/*, Utils.ts)
  -> NativeModules.RNNeteaseIm / NativeModules.PinYin
  -> Platform bridge
     - Android: RNNeteaseImModule.java + helper modules
     - iOS: RNNeteaseIm.m + singleton controllers
  -> Native domain services/controllers
  -> NetEase NIM SDK
  -> Native observers emit events back to JS
```

## Public JS Modules

| Module | Source | Responsibility |
| --- | --- | --- |
| `NimSession` | `src/Session/Session.ts` | auth, recent sessions, message history, send/receive behavior, custom message flows, reactions, temporary sessions |
| `NimFriend` | `src/Friend/Friend.ts` | friend list, user info, blacklist |
| `NimTeam` | `src/Team/Team.ts` | team list, team detail, member/admin operations |
| `NimSystemMsg` | `src/SystemMsg/SystemMsg.ts` | system notification list và unread count |
| `NimChatroom` | `src/Chatroom/chatroom.ts` | chatroom login/info/member/history |
| `NimUtils` | `Utils.ts` | cache cleanup, audio playback, pinyin sort, net info, device language |

## Android Architecture

### Bridge

- `android/src/main/java/com/netease/im/RNNeteaseImModule.java`
  - module React Native tên `RNNeteaseIm`
  - giữ `LifecycleEventListener` và `ActivityEventListener`
  - expose phần lớn method cho JS
- `android/src/main/java/com/netease/im/RNPinYinModule.java`
  - module React Native tên `PinYin`
  - sort/group dữ liệu theo pinyin
- `android/src/main/java/com/netease/im/RNNeteaseImPackage.java`
  - register `RNPinYinModule` và `RNNeteaseImModule`

### Bootstrap và Runtime

- `android/src/main/java/com/netease/im/IMApplication.java`
  - init `NIMClient`
  - cấu hình status bar notification
  - init cache, storage, pinyin, log, revoke observer
  - đăng ký custom attachment parser
- `android/src/main/AndroidManifest.xml`
  - merge nhiều permission, service, receiver và provider cho NIM/push

### Domain Packages

- `login/*`
  - `LoginService`: login/logout, observer registration, unread system message count
  - `RecentContactObserver`: recent session, online status, incoming message, msg status
  - `SysMessageObserver`: system message load/merge/emit
- `session/*`
  - `SessionService`: core logic của conversation/message
  - `AudioMessageService`, `AudioPlayService`: record/playback
  - `SessionUtil`: helper push/custom notification/session name
  - `extension/*`: custom attachment types
- `contact/*`
  - `FriendListService`, `FriendObserver`, `BlackListObserver`
- `team/*`
  - `TeamListService`, `TeamObserver`
- `receiver/*`
  - `NetworkReceiver`, custom notification receiver
- `uikit/*`
  - helper/cache/query code được nhúng vào library

### Android State Flow

1. Host app gọi `IMApplication.init(...)`.
2. `LoginService` thực hiện login và đăng ký observer.
3. `DataCacheManager` build cache user/friend/team.
4. Observer native emit event qua `ReactCache`.
5. `RNNeteaseImModule` trả Promise hoặc callback cho JS.

## iOS Architecture

### Bridge

- `ios/RNNeteaseIm/RNNeteaseIm/RNNeteaseIm.m`
  - `RCT_EXPORT_MODULE()`
  - route phần lớn method JS sang controller singleton tương ứng
  - khởi tạo `EventSender` cho batch `observeReceiveMessage`, `observeMsgStatus`, `observeProgressSend`
  - nhận `ObservePushNotification` từ `NSNotificationCenter`

### Controllers

- `NIMViewController`
  - login delegate, recent contact list, recent session shaping
- `ConversationViewController`
  - conversation runtime lớn nhất
  - query/send/revoke/resend/download/reaction/temporary session/audio
- `ContactViewController`
  - friend và user info
- `TeamViewController`
  - team list, team detail, membership/admin operations
- `NoticeViewController`
  - system notification
- `ChatroomViewController`
  - chatroom flows
- `RNNotificationCenter`
  - chat/system notification delegate
- `UserStrangers`
  - debounce unknown users và emit `observeUserStranger`
- `CacheUsers`
  - enrich thông tin user từ backend ngoài NIM SDK

### Utility Layer

- `Util/NIMMessageMaker.m`
  - tạo message object, attachment, anti-spam option, birthday notification
- `Util/NTESSDKConfigDelegate.*`
  - NIM SDK config delegate
- `Util/Spelling/*`
  - pinyin helpers

### iOS State Flow

1. `RNNeteaseIm` khởi tạo controller và event router.
2. `login` hoặc `autoLogin` gọi `registerWithAppID:cerName:` rồi login qua `NIMSDK`.
3. Controller singleton giữ delegate tới NIM SDK manager.
4. `NIMModel.myBlock` route event số sang tên event JS trong `RNNeteaseIm.m`.

## Event Model

### Core events đã có type enum ở JS

- `observeRecentContact`
- `observeOnlineStatus`
- `observeFriend`
- `observeTeam`
- `observeBlackList`
- `observeReceiveMessage`
- `observeReceiveSystemMsg`
- `observeUnreadCountChange`
- `observeMsgStatus`
- `observeAudioRecord`
- `observeDeleteMessage`
- `observeAttachmentProgress`
- `observeOnKick`
- `observeCustomNotification`
- `observeProgressSend`
- `observeUserStranger`

### Native vẫn emit nhưng JS enum chưa mô tả đầy đủ

- `observeLaunchPushEvent`
- `observeBackgroundPushEvent`
- `observeAccountNotice`
- `observePhotoLibraryDidChange`
- `observeStartSend`
- `observeEndSend`
- `observeReceipt`
- `observeDownloadVideoNotice`

## Message and Attachment Contract

Repo encode nhiều business behavior qua `remoteExt`, `localExt`, `extendType` và `messageSubType`.

Các type quan trọng quan sát được:

- custom attachment:
  - `redpacket`
  - `transfer`
  - `redpacketOpen`
  - `forwardMultipleText`
  - `card`
  - `url`
  - `account_notice`
- `extendType` text/custom:
  - `TEAM_NOTIFICATION_MESSAGE`
  - `forwardMultipleText`
  - `card`
  - `gif`
- `messageSubType`:
  - `2`: reaction
  - `3`: remove reaction
  - `7`: temporary session
  - `8`: birthday notification

Đây là cross-platform contract, không nên đổi một phía.

## Host App Responsibilities

Các bước tích hợp native nằm chi tiết trong:

- `../playbook/NATIVE_INTEGRATION.md`

Điểm cốt lõi:

- Android host phải init SDK và xử lý push launch intent.
- iOS host phải cấu hình APNs, notification forwarding và env phù hợp.
- Thư viện không tự chứa app-level bootstrap hoàn chỉnh.

## Non-goals

- Không cung cấp React UI component.
- Không có example app trong repo hiện tại.
- Không có New Architecture/TurboModule implementation.
- Không có CI/test automation đáng tin cậy trong repo hiện tại.
