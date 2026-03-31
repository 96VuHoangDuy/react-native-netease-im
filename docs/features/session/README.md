# Session Module

## Scope

`NimSession` là module lớn nhất của repo. Nó gộp các trách nhiệm:

- login / auto-login / logout
- recent session list
- active conversation runtime
- query/search message history
- gửi các loại message chuẩn và custom
- audio record / playback trigger
- revoke/resend/delete/download attachment
- reaction, temporary session, chatbot / CSR helper
- push open retrieval trên Android

Repo hiện không có session UI ở JS; mọi logic chat thực thi ở native.

## Entry Points

### JS

- `src/Session/Session.ts`
- `src/Session/session.type.ts`
- `src/Message/message.type.ts`

### Android

- `android/src/main/java/com/netease/im/RNNeteaseImModule.java`
- `android/src/main/java/com/netease/im/session/SessionService.java`
- `android/src/main/java/com/netease/im/session/AudioMessageService.java`
- `android/src/main/java/com/netease/im/session/AudioPlayService.java`
- `android/src/main/java/com/netease/im/login/RecentContactObserver.java`
- `android/src/main/java/com/netease/im/ReceiverMsgParser.java`
- `android/src/main/java/com/netease/im/session/extension/*`

### iOS

- `ios/RNNeteaseIm/RNNeteaseIm/RNNeteaseIm.m`
- `ios/RNNeteaseIm/RNNeteaseIm/ConversationViewController.h`
- `ios/RNNeteaseIm/RNNeteaseIm/ConversationViewController.m`
- `ios/RNNeteaseIm/RNNeteaseIm/NIMViewController.h`
- `ios/RNNeteaseIm/RNNeteaseIm/NIMViewController.m`
- `ios/RNNeteaseIm/RNNeteaseIm/Util/NIMMessageMaker.m`
- `ios/RNNeteaseIm/RNNeteaseIm/RNNotificationCenter.m`
- `ios/RNNeteaseIm/RNNeteaseIm/UserStrangers.m`

## State And Data Flow

1. JS gọi `NimSession.login(...)` hoặc `autoLogin(...)`.
2. Native login service/controller đăng ký observer với NIM SDK.
3. Recent session, message status, unread count và incoming message được emit về JS bằng event.
4. `startSession(...)` chọn active session ở native.
5. Các thao tác gửi, query, revoke, reaction, temporary session đều chạy ở native rồi trả Promise hoặc event.

## Public API Groups

### Auth / lifecycle

- `login`
- `autoLogin`
- `logout`
- `startSession`
- `stopSession`
- `getLaunch`
- `startObserverMediaChange`
- `stopObserverMediaChange`

### Recent sessions

- `getRecentContactList`
- `deleteRecentContact`
- `removeSession`
- `addEmptyRecentSession`
- `addEmptyRecentSessionWithoutMessage`
- `addEmptyRecentSessionCustomerService`
- `addEmptyPinRecentSession`
- `addEmptyTemporarySession`
- `removeTemporarySessionRef`
- `updateRecentToTemporarySession`
- `updateRecentSessionIsCsrOrChatbot`
- `updateActionHideRecentSession`

### History / search / read state

- `queryMessageListEx`
- `queryMessageListHistory`
- `searchFileMessages`
- `searchTextMessages`
- `searchMessages`
- `searchMessagesInCurrentSession`
- `getMessageById`
- `readAllMessageOnlineServiceByListSession`
- `readAllMessageBySession`
- `clearMessage`

### Send standard messages

- `replyMessage`
- `sendTextMessage`
- `sendGifMessage`
- `sendImageMessages`
- `sendAudioMessage`
- `sendMultiMediaMessage`
- `sendVideoMessage`
- `sendLocationMessage`
- `sendTipMessage`
- `sendFileMessage`
- session-targeted variants:
  - `sendFileMessageWithSession`
  - `sendTextMessageWithSession`
  - `sendImageMessageWithSession`
  - `sendVideoMessageWithSession`
  - `sendGifMessageWithSession`

### Send business/custom messages

- `sendCustomMessageOfChatbot`
- `sendRedPacketMessage`
- `sendRedPacketOpenMessage`
- `sendBankTransferMessage`
- `sendCardMessage`
- `sendCustomMessage`
- `createNotificationBirthday`
- `updateMessageSentStickerBirthday`
- `sendCustomNotification`
- `setListCustomerServiceAndChatbot`

### Forward / reaction / control

- `forwardMultipleTextMessage`
- `sendForwardMessage`
- `forwardMessagesToMultipleRecipients`
- `forwardMultiTextMessageToMultipleRecipients`
- `reactionMessage`
- `removeReactionMessage`
- `updateReactionMessage`
- `removeReactedUsers`
- `setCancelResendMessage`
- `cancelSendingMessage`
- `revokeMessage`
- `resendMessage`
- `deleteMessage`
- `removeMessage`
- `updateMessageOfChatBot`
- `updateMessageOfCsr`
- `updateIsSeenMessage`
- `updateIsTransferMessage`
- `setStrangerRecentReplyed`

### Attachment / audio helpers

- `downloadAttachment`
- `updateAudioMessagePlayStatus`
- `onTouchVoice`
- `startAudioRecord`
- `endAudioRecord`
- `cancelAudioRecord`

### Query helpers

- `getOwnedGroupCount`
- `hasMultipleMessages`

## Events Used By This Module

- `observeRecentContact`
- `observeOnlineStatus`
- `observeReceiveMessage`
- `observeMsgStatus`
- `observeUnreadCountChange`
- `observeAudioRecord`
- `observeDeleteMessage`
- `observeCustomNotification`
- `observeOnKick`
- `observeProgressSend`
- `observeUserStranger`
- extra native events liên quan:
  - `observeLaunchPushEvent`
  - `observeBackgroundPushEvent`
  - `observeAccountNotice`
  - `observePhotoLibraryDidChange`
  - `observeReceipt`
  - `observeStartSend`
  - `observeEndSend`
  - `observeDownloadVideoNotice`

## Business Rules

- `messageSubType` và `extendType` mang business meaning thật, không chỉ là metadata.
- Các behavior custom đã thấy trong source:
  - red packet
  - bank transfer
  - forward multiple text
  - profile card
  - gif
  - team notification text
  - temporary session
  - reaction / remove reaction
  - birthday notification
- Unknown user / stranger enrichment có thể đi qua native `CacheUsers` và `UserStrangers`.
- Session module đồng thời kiểm soát message anti-spam option:
  - iOS dùng `IM_BUSINESS_ID`
  - Android có hook nội bộ nhưng chưa bridge public

## Platform Notes

### Android

- `getLaunch()` có ý nghĩa thực tế trên Android để lấy cold-start push intent.
- `startObserverMediaChange()` dùng `GalleryObserver`.
- `ReceiverMsgParser` chuẩn hóa payload push open.
- `SessionService` là nơi giữ state session hiện hành.

### iOS

- `RNNeteaseIm.m` batch `observeReceiveMessage`, `observeMsgStatus`, `observeProgressSend`.
- `ConversationViewController` giữ phần lớn logic conversation.
- `RNNeteaseIm` route event qua `NIMModel.myBlock`.
- `ObservePushNotification` là hook để app host chuyển push tap vào bridge.

## Error Handling Notes

- Nhiều method trả Promise với code NIM hoặc reject string ngắn.
- Một số behavior báo trạng thái qua event thay vì Promise resolve data phong phú.
- Với upload/send message, cần kết hợp Promise ban đầu với:
  - `observeMsgStatus`
  - `observeProgressSend`

## Gaps

- Không có docs publish chính thức cho full message contract ngoài source.
- Android và iOS không đối xứng hoàn toàn ở anti-spam config và event coverage trong type JS.
