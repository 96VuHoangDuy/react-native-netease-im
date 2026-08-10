# Media & Attachment (ảnh / video / file / voice)

Chi tiết capability native cho **gửi, nhận, download** media message, field mapping native→JS, và event
lifecycle. Là con của [session/README.md](README.md). Consumer app-layer trace ở
`pyeon-chinese-mobile/docs/features/chat/media-message-flow.md`.

## Scope

- Mô tả: gửi media (`sendImageMessages`/`sendVideoMessage`/`sendFileMessage`/`sendAudioMessage`/
  `sendMultiMediaMessage` + `...WithSession`), auto-download attachment (thumb→full), field mapping trong
  `extend`, và event upload/download progress.
- Không mô tả: nén/thumbnail thuật toán chi tiết của NIM SDK, chatroom/team media riêng.

## Entry Points

### JS
- `src/Session/Session.ts` (public API), `src/Message/message.type.ts` (`NimMessageTypeExtend`),
  `src/utils/eventListener.type.ts` (event enum)

### Android
- `RNNeteaseImModule.java` (bridge `@ReactMethod`), `session/SessionService.java` (send/download runtime),
  `ReactCache.java` (`generate*Extend`, `createMessage`, `createAttachmentProgress`), `MessageConstant.java` (JS field names)

### iOS
- `RNNeteaseIm.m` (bridge `RCT_EXPORT_METHOD`), `ConversationViewController.m`
  (`makeExtend*`, `moveFiletoSessionDir:`, `NIMChatManagerDelegate`), `Util/NIMMessageMaker.m` (build message object)

## State And Data Flow

### Gửi
1. JS gọi `NimSession.send<Type>Message(...)` → bridge.
2. Android: resolve path (`Uri.parse().getPath()`), resize ảnh theo `isHighQuality`
   (`ImageUtil.getScaledImageFileWithMD5`), đọc `duration/width/height` video qua `MediaMetadataRetriever`
   (xử lý xoay 90/270°), `MessageBuilder.create<Type>Message(...)` → `sendMessageSelf(...)`
   (`SessionService.java:2064-2207`).
3. iOS: `NIMMessageMaker.msgWith<Type>:` tạo `NIM<Type>Object`, tự tính `duration` video qua `AVURLAsset` nếu
   thiếu, build APNs push body (`setupMessagePushBody:`), anti-spam nếu có `IM_BUSINESS_ID`.
4. Upload progress emit qua `observeProgressSend`; hoàn tất qua `observeMsgStatus` (Android) / batch (iOS).

### Nhận + auto download
1. Message media về → app emit `observeReceiveMessage` (Android: `SessionService.onIncomingMessage`;
   iOS: `onRecvMessages:` → `refrashMessage:From:@"receive"`).
2. **Android**: mỗi lần build dict trong `generate*Extend`, gọi
   `SessionService.downloadAttachment(item, thumbPath == null)` — `isThumb = thumbPath == null` ⇒ lần đầu tải
   thumbnail, lần sau tải bản gốc. Khi xong: `createMessage(item, true)` emit qua **`observeMsgStatus`**
   (`ReactCache.java:3136-3141`), KHÔNG phải `observeReceiveMessage`.
3. **iOS**: `moveFiletoSessionDir:` tự HTTP download riêng qua `NIMObject downLoadAttachment:` (không qua SDK
   queue chính); SDK `fetchMessageAttachment:` delegate vẫn chạy cho thumbnail nhỏ. Refresh sau tải xong đi qua
   **`observeReceiveMessage`** (cùng kênh nhận). `[NIMSDK ... fetchMessageAttachment:]` bị comment out ở
   `downloadAttachment:` (`ConversationViewController.m:230`), chỉ dùng thật trong `resendMessage:`.

## Field mapping native → JS (`extend`)

`Confirmed from source`. Tên field JS lấy từ `MessageConstant.MediaFile` (Android) / string key trực tiếp (iOS).
**Cột "khi `isFilePathDeleted`"** là mấu chốt bug "khung đen": field nào còn để FE tự tải lại.

### IMAGE

| Field JS | Android (`generateImageExtend` `ReactCache.java:2450-2539`) | iOS (`makeExtendImage:` `ConversationViewController.m:1011-1064`) |
| --- | --- | --- |
| `url` (remote) | chỉ set trong nhánh `else` của `if(!isFilePathDeleted)` (`:2528`) → **mất khi `isFilePathDeleted` hoặc sau replace-path** | set **vô điều kiện** đầu hàm (`:1018`) → luôn còn |
| `imageWidth/Height` | set trong `if(!isFilePathDeleted)` (`:2496-2497`) | vô điều kiện (`:1019-1021`) |
| `displayName` | chỉ nhánh `else` (`:2529`) | vô điều kiện |
| `path` (local) | set khi có (`:2506/2527`) | có |
| `thumbPath` / `coverPath` | `thumbPath` (`:2507/2523/2525`) | `coverPath` |
| `isFilePathDeleted` / `isFileDownloading` / `isReplacePathSuccess` / `needRefreshMessage` | có | có (`downloadAttStatus` thêm ở iOS) |
| `parentId` / `indexCount` (gộp nhóm) | từ `remoteExtension` (`:2471-2477`) | từ `remoteExt` |

### VIDEO

| Field JS | Android (`generateVideoExtend` `:2334`) | iOS (`makeExtendVideo:` `:1110`) |
| --- | --- | --- |
| `url` (remote) | set **vô điều kiện** (`:2343`) → **an toàn** kể cả `isFilePathDeleted` | vô điều kiện (`:1118`) |
| `coverUrl` (remote) | có | vô điều kiện (`:1118-1124`) |
| `coverSizeWidth/Height` (iOS) / `imageWidth/Height` (Android) | có | có |
| `duration` / `size`(`fileLength`) | có | có |
| `path` / `coverPath` | có | có |

### FILE

| Field JS | Android (`generateFileExtend` `:2541`) | iOS (`makeExtendFile:` `:1066`) |
| --- | --- | --- |
| `filePath` / `fileUrl` / `fileName` / `fileMd5` / `fileSize` / `fileType` | `fileUrl` chỉ trong `if(!isFilePathDeleted)` → **mất khi deleted** | set vô điều kiện đầu hàm (`:1079-1084`) |

### AUDIO / VOICE

| Field JS | Android (`generateRecordExtend` `:2626`) | iOS (`makeExtendRecord:` `:1174`) |
| --- | --- | --- |
| `url` (remote) | chỉ trong `if(!isFilePathDeleted)` → **mất khi deleted** | vô điều kiện (`:1177-1179`) |
| `duration` / `isPlayed` | có | có |

## Public API Groups

- Send: `sendImageMessages`, `sendVideoMessage`, `sendFileMessage`, `sendAudioMessage`, `sendGifMessage`,
  `sendMultiMediaMessage`, và `send{Image,Video,File,Gif}MessageWithSession`.
- Attachment/audio: `downloadAttachment`, `updateAudioMessagePlayStatus`, `onTouchVoice`,
  `startAudioRecord`, `endAudioRecord`, `cancelAudioRecord`.

## Event lifecycle

| Event JS | Emit khi | Payload | Nguồn |
| --- | --- | --- | --- |
| `observeProgressSend` | upload/download progress | `{messageId, sessionId, progress (string 0-1), type}` | Android `ReactCache.java:3162-3171` (`type:"update"`); iOS upload `RNNeteaseIm.m:164-168` (`type:"upload"`), custom download `ConversationViewController.m:1365-1375` (`type:"upload"` — nhãn sai), SDK fetch `:2916-2922` (`type:"download"`) |
| `observeReceiveMessage` | message mới về; iOS còn dùng cho refresh sau download | message dict | — |
| `observeMsgStatus` | Android: refresh sau download attachment xong; status change | message dict | `ReactCache.java:3136-3141` |
| `observeStartSend` / `observeEndSend` | bắt đầu/kết thúc gửi | — | native emit (chưa có trong `eventListener.type.ts`) |
| `observeDownloadVideoNotice` | notice download video | — | native emit (chưa có trong `eventListener.type.ts`) |
| `observeAudioRecord` | trạng thái ghi âm | — | — |

## Business Rules

- `field type progress` (`update`/`upload`/`download`) hiện **không nhất quán** giữa 2 platform và giữa upload
  vs download; consumer nên coi progress là 0-1 theo `messageId`, không tin cậy `type`.
- `parentId`/`indexCount` trong `remoteExtension` dùng để gộp nhóm ảnh/video thành `MultipleMediaMessage`.
- `isThumb = thumbPath == null` (Android) quyết định tải thumbnail trước hay full sau.

## Platform Notes

### Android
- `generateImageExtend`/`generateFileExtend`/`generateRecordExtend` chỉ set `url`/`fileUrl`/`displayName`/
  kích thước **bên trong** `if(!isFilePathDeleted)`; `generateVideoExtend` set `url` vô điều kiện.
  ⇒ khi cache local bị dọn mà `isReplacePathSuccess=true`, dict image/file/audio mất URL để tải lại.
- Refresh sau download đi qua `observeMsgStatus`, không phải `observeReceiveMessage`.

### iOS
- `makeExtend*` set `url`/`coverUrl`/kích thước vô điều kiện ở đầu hàm ⇒ luôn giữ được URL remote.
- Custom download `moveFiletoSessionDir:` không qua SDK queue (không cancel/abort); nhánh lỗi network không set
  `downloadAttStatus=failed` rõ ràng → có thể kẹt "downloading".
- Refresh sau download đi qua `observeReceiveMessage` (cùng kênh nhận).

## Error Handling Notes

- Send: Promise ban đầu chỉ báo call thành công; cần kết hợp `observeMsgStatus` + `observeProgressSend` để biết
  đã lên server / tiến độ.
- Download: Android tự retry qua vòng build dict; iOS custom-download lỗi chỉ log.

## Gaps

- `observeAttachmentProgress` khai báo trong `src/utils/eventListener.type.ts:13` nhưng **không native nào emit**
  — event thật là `observeProgressSend`. Listener trên tên này không bao giờ chạy.
- Bất đối xứng Android: image/file/audio mất `url`/`fileUrl` khi `isFilePathDeleted`, video thì không —
  chưa reproduce runtime kịch bản "xoá cache → mở lại conversation có ảnh cũ".
- iOS custom download không set `downloadAttStatus=failed` ở nhánh lỗi — chưa xác nhận hành vi kẹt trên device.
- `field type` của `observeProgressSend` lệch nhãn giữa upload/download và giữa 2 platform.
