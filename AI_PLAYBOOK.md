# AI Playbook

## Mission

Giữ repo này ở trạng thái có thể bảo trì như một thư viện React Native tích hợp chat NetEase NIM, không làm vỡ contract giữa:

- JS public API trong `index.ts` và `src/*`
- Android bridge trong `android/src/main/java/com/netease/im/*`
- iOS bridge trong `ios/RNNeteaseIm/RNNeteaseIm/*`
- event payload mà app host đang lắng nghe

## Project Snapshot

- Package: `react-native-netease-im`
- Version trong repo: `3.1.0`
- Peer dependency: `react-native >= 0.60.0`
- Public JS modules:
  - `NimSession`
  - `NimFriend`
  - `NimSystemMsg`
  - `NimTeam`
  - `NimUtils`
  - `NimChatroom`
- Public native modules:
  - `RNNeteaseIm`
  - `PinYin` (Android)

## Read First

1. `README.md`
2. `docs/README.md`
3. `docs/common/ARCHITECTURE.md`
4. `docs/playbook/NATIVE_INTEGRATION.md`
5. Feature doc liên quan trong `docs/features/*`
6. File native liên quan ở cả Android và iOS nếu thay đổi public behavior

## Tech Stack

Tóm tắt stack và dependency chính nằm ở:

- `docs/playbook/STACK_AND_TOOLS.md`

## Repo Map

Sơ đồ repo và vai trò từng cụm thư mục nằm ở:

- `docs/playbook/REPO_MAP.md`

## Run / Build / Verify

Danh sách command có thể dùng và giới hạn xác thực hiện tại nằm ở:

- `docs/playbook/COMMANDS.md`

Điểm quan trọng:

- Repo không có CI.
- `npm test` hiện chỉ là placeholder lỗi.
- `dist/` chưa có trong repo dù `package.json` trỏ `types` tới `dist/index.d.ts`.

## Workflow

### 1. Plan

- Xác định module public bị ảnh hưởng.
- Xác định parity cần kiểm tra ở:
  - `src/*`
  - `android/src/main/java/com/netease/im/RNNeteaseImModule.java`
  - `ios/RNNeteaseIm/RNNeteaseIm/RNNeteaseIm.m`
- Nếu thay đổi message, recent session, push, custom attachment, reaction hoặc temporary session thì bắt buộc đọc cả native Android và iOS.

### 2. Execute

- Sửa JS wrapper trước nếu contract thay đổi từ phía JS.
- Đối chiếu export native hai nền tảng.
- Giữ nguyên event name và payload shape nếu chưa có yêu cầu đổi contract.
- Nếu chạm vào bootstrap native hoặc biến môi trường, cập nhật `docs/playbook/NATIVE_INTEGRATION.md`.

### 3. Verify

- Chạy hoặc nêu rõ command đã kiểm tra.
- Soát link docs và file path.
- Nêu rõ platform impact:
  - JS only
  - Android only
  - iOS only
  - Cross-platform

## Architecture Conventions

Các rule kiến trúc ngắn gọn nằm ở:

- `docs/playbook/ARCHITECTURE_RULES.md`

## Output Contract

Khi handoff, câu trả lời nên có:

- thay đổi chính
- file đã đổi
- cách verify đã chạy hoặc chưa chạy
- rủi ro/gap còn lại
- platform affected

## Current Context

Context audit hiện tại và các gap cần owner điền thêm nằm ở:

- `docs/playbook/CURRENT_CONTEXT.md`
- `docs/playbook/GAPS.md`
