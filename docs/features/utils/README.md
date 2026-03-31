# Utils Module

## Scope

`NimUtils` gom các helper không thuộc một domain chat duy nhất:

- session cache size / cleanup
- audio playback helper
- pinyin sort
- network info
- switch audio output device
- device language

## Entry Points

### JS

- `Utils.ts`

### Android

- `android/src/main/java/com/netease/im/RNPinYinModule.java`
- `android/src/main/java/com/netease/im/RNNeteaseImModule.java`
- `android/src/main/java/com/netease/im/FileCacheUtil.java`
- `android/src/main/java/com/netease/im/GalleryObserver.java`
- `android/src/main/java/com/netease/im/session/AudioPlayService.java`

### iOS

- `ios/RNNeteaseIm/RNNeteaseIm/RNNeteaseIm.m`
- `ios/RNNeteaseIm/RNNeteaseIm/ConversationViewController.m`

## Public API

- `getSessionCacheSize`
- `cleanSessionCache`
- `getListSessionsCacheSize`
- `cleanListSessionsCache`
- `play`
- `playLocal`
- `getIsPlayingRecord`
- `stopPlay`
- `sortPinYin`
- `fetchNetInfo`
- `switchAudioOutputDevice`
- `getDeviceLanguage`

## State And Data Flow

- Cache APIs gọi native file/cache helper trực tiếp.
- Playback APIs dùng native audio service/controller.
- `sortPinYin(...)` gọi native module `PinYin`.
- `getDeviceLanguage()` là native synchronous read.

## Business Rules

- `playLocal(name, type)` có path handling khác nhau:
  - iOS: dùng tên file và type
  - Android: build path `assets:///name.type`
- `getIsPlayingRecord(...)` dùng callback style thay vì Promise.
- Session cache cleanup là theo `sessionId` hoặc list `sessionIds`, không phải global app cache.

## Platform Notes

### Android

- `sortPinYin(...)` thực tế là Android-specific vì module `PinYin` chỉ thấy được register ở Android.
- `fetchNetInfo()` được comment rõ là Android only.
- `GalleryObserver` emit `observePhotoLibraryDidChange`.

### iOS

- `getDeviceLanguage()` là `RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD`.
- Audio output switch chạy qua `ConversationViewController`.

## Error Handling Notes

- Cache APIs trả Promise.
- Audio playback side effect có thể được phản ánh qua `observeAudioRecord`.
- Nếu gọi `sortPinYin(...)` ở nền tảng không có module `PinYin`, consumer app cần tự guard.

## Gaps

- JS type không chú thích rõ `sortPinYin(...)` là Android-only dù native registration hiện chỉ thấy ở Android.
