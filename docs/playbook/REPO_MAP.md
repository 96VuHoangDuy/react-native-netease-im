# Repo Map

## Top Level

```text
.
├── README.md
├── AI_PLAYBOOK.md
├── AGENTS.md
├── CLAUDE.md
├── package.json
├── index.ts
├── Utils.ts
├── src/
├── android/
├── ios/
└── docs/
```

## Public JS Surface

- `index.ts`
  - export toàn bộ module public
- `Utils.ts`
  - utility module public nhưng nằm ngoài `src/`
- `src/Session/*`
  - session, recent contact, message API và types
- `src/Friend/*`
  - friend API và types
- `src/Team/*`
  - team API và types
- `src/SystemMsg/*`
  - system notification API và types
- `src/Chatroom/*`
  - chatroom API và types
- `src/Message/message.type.ts`
  - type contract cho message/reaction/media
- `src/User/user.type.ts`
  - type contract cho user
- `src/utils/*`
  - enum và response code chung

## Android

### Bridge / Runtime Entry

- `android/src/main/java/com/netease/im/RNNeteaseImModule.java`
- `android/src/main/java/com/netease/im/RNPinYinModule.java`
- `android/src/main/java/com/netease/im/RNNeteaseImPackage.java`
- `android/src/main/java/com/netease/im/IMApplication.java`
- `android/src/main/AndroidManifest.xml`

### Domain Packages

- `android/src/main/java/com/netease/im/login`
  - auth, recent, system message
- `android/src/main/java/com/netease/im/session`
  - conversation, attachment, audio
- `android/src/main/java/com/netease/im/session/extension`
  - custom attachment implementation
- `android/src/main/java/com/netease/im/contact`
  - friend, blacklist
- `android/src/main/java/com/netease/im/team`
  - team list và observer
- `android/src/main/java/com/netease/im/receiver`
  - network và custom notification receiver
- `android/src/main/java/com/netease/im/uikit`
  - cache/query/helper code được nhúng trong library

## iOS

### Bridge

- `ios/RNNeteaseIm/RNNeteaseIm/RNNeteaseIm.m`
- `ios/RNNeteaseIm/RNNeteaseIm/RNNeteaseIm.h`

### Controllers / Runtime

- `NIMViewController.*`
- `ConversationViewController.*`
- `ContactViewController.*`
- `TeamViewController.*`
- `NoticeViewController.*`
- `ChatroomViewController.*`
- `RNNotificationCenter.*`
- `UserStrangers.*`
- `CacheUsers.*`

### Utilities

- `ios/RNNeteaseIm/RNNeteaseIm/Util/*`
  - message maker, NIM kit helper, SDK config delegate, spelling helper

## Historical / Packaging Artifacts

- `react-native-netease-im-3.0.0.tgz`
  - artifact cũ nằm trong repo
- `dist/`
  - chưa có trong repo hiện tại
