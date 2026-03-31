# Contributing

## Scope

File này mô tả cách làm việc với repo theo những gì quan sát được từ source hiện tại. Những phần không thấy trong repo sẽ được ghi rõ là chưa có dữ liệu.

## Code Conventions Quan Sát Được

### TypeScript

- Public API export từ `index.ts`.
- Mỗi domain có một singleton default export:
  - `new NimSession()`
  - `new NimFriend()`
  - `new NimSystemMsg()`
  - `new NimTeam()`
  - `new NimUtils()`
  - `new NimChatroom()`
- Type contract nằm ở `src/*/*.type.ts` và `src/Message/message.type.ts`.

### Android

- Bridge tập trung trong `RNNeteaseImModule.java`.
- Nhiều domain dùng singleton service hoặc observer:
  - `LoginService`
  - `RecentContactObserver`
  - `SysMessageObserver`
  - `SessionService`
  - `TeamListService`
- Event emit đi qua `ReactCache`.

### iOS

- Bridge tập trung trong `RNNeteaseIm.m`.
- Domain logic chia thành singleton-style controller:
  - `NIMViewController`
  - `ConversationViewController`
  - `ContactViewController`
  - `TeamViewController`
  - `NoticeViewController`
  - `ChatroomViewController`
- Event emit đi qua `bridge.eventDispatcher`.

## Khi Nào Phải Soát Cả Android và iOS

Bắt buộc soát cả hai nền tảng nếu thay đổi:

- public method trong `src/*`
- event name hoặc payload
- custom attachment / `extendType` / `messageSubType`
- recent session shaping
- push open flow
- audio record/playback
- cache/enrich user info

## Quality Gates Hiện Có

Repo hiện không có quality gate tự động hoàn chỉnh.

Quan sát được:

- `package.json` có `npm test` nhưng script này chỉ trả lỗi placeholder.
- không có CI config trong repo
- không có app ví dụ trong repo để build end-to-end
- `tsconfig.json` chỉ include `src/**/*`, không include `index.ts` và `Utils.ts`

Vì vậy, trước handoff nên làm tối thiểu:

- soát parity JS/Android/iOS bằng source
- chạy `npx tsc --noEmit` nếu chỉ cần kiểm tra phần `src/**/*`
- nêu rõ những gì chưa verify được

## Branching / PR Rules

Không thấy rule branch hoặc PR nào được khai báo trong repo hiện tại.

Owner cần tự bổ sung nếu muốn chuẩn hóa.

## Docs Update Policy

- Nếu public contract đổi, cập nhật feature doc tương ứng trong `docs/features/*`.
- Nếu bootstrap native đổi, cập nhật `docs/playbook/NATIVE_INTEGRATION.md`.
- Nếu phát hiện inconsistency mới, thêm vào `docs/playbook/GAPS.md`.
- Nếu thay đổi ảnh hưởng kiến trúc, cập nhật `docs/common/ARCHITECTURE.md` hoặc `docs/common/DECISIONS.md`.
