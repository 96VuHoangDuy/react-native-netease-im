# react-native-netease-im

Thư viện React Native tích hợp NetEase NIM SDK với phần native runtime lớn ở cả Android và iOS. Repo này không chỉ bọc `NativeModules`, mà còn chứa logic observer, cache, push, custom attachment, audio và session handling trong native code.

## Tài liệu chính

- `docs/README.md`
- `docs/common/ARCHITECTURE.md`
- `docs/playbook/NATIVE_INTEGRATION.md`
- `docs/playbook/GAPS.md`

## Public JS Modules

- `NimSession`
- `NimFriend`
- `NimSystemMsg`
- `NimTeam`
- `NimUtils`
- `NimChatroom`

Chi tiết từng module nằm ở:

- `docs/features/session/README.md`
- `docs/features/friend/README.md`
- `docs/features/team/README.md`
- `docs/features/system-msg/README.md`
- `docs/features/chatroom/README.md`
- `docs/features/utils/README.md`

## Install

Trong app host:

```bash
npm install react-native-netease-im
npx pod-install
```

## Điều Phải Biết Trước Khi Tích Hợp

Thư viện này cần app host tự bootstrap native:

- Android:
  - gọi `IMApplication.init(...)`
  - thêm `com.netease.nim.appKey` metadata vào manifest app
  - xử lý push launch intent
  - giữ flow runtime permission cho media/audio nếu dùng
- iOS:
  - cấu hình APNs
  - forward push tap qua `ObservePushNotification`
  - cung cấp env/config cần thiết nếu build iOS source hiện tại

Chi tiết đầy đủ nằm ở:

- `docs/playbook/NATIVE_INTEGRATION.md`

## Kiến Trúc Tóm Tắt

```text
JS API (index.ts, src/*, Utils.ts)
  -> NativeModules.RNNeteaseIm / NativeModules.PinYin
  -> Android bridge hoặc iOS bridge
  -> Native services/controllers
  -> NetEase NIM SDK
  -> Native observers emit event về JS
```

## Những Điểm Cần Lưu Ý

- Repo hiện không có CI hay test suite thực tế.
- `npm test` chỉ là placeholder lỗi.
- `dist/` chưa có trong repo dù `package.json` trỏ types tới `dist/index.d.ts`.
- iOS source đang import `react-native-config`, nhưng package metadata chưa khai báo dependency này.

Các khoảng trống khác nằm ở:

- `docs/playbook/GAPS.md`
