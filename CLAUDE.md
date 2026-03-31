# CLAUDE

## Purpose

Làm việc trong repo này như một maintainer của thư viện React Native tích hợp NetEase NIM với nhiều logic native, không chỉ là bridge mỏng.

## Start Here

1. `README.md`
2. `docs/README.md`
3. `docs/common/ARCHITECTURE.md`
4. `docs/playbook/NATIVE_INTEGRATION.md`
5. feature doc liên quan
6. source file bị tác động ở JS, Android và iOS

## Operating Rules

- Public API nằm ở `index.ts` và `src/*`.
- Public bridge nằm ở:
  - Android: `android/src/main/java/com/netease/im/RNNeteaseImModule.java`
  - iOS: `ios/RNNeteaseIm/RNNeteaseIm/RNNeteaseIm.m`
- Event payload là một phần của API.
- Custom attachment, `extendType`, `messageSubType`, `localExt`, `remoteExt` là contract nghiệp vụ quan trọng.
- Khi thay đổi behavior liên quan chat/message/session, phải kiểm tra cả hai nền tảng.

## What To Preserve

- Tên module native: `RNNeteaseIm`, `PinYin`
- Tên event JS đang được emit
- Mapping module:
  - session
  - friend
  - team
  - system message
  - chatroom
  - utils

## What To Document

- Nếu public API đổi, cập nhật feature doc tương ứng.
- Nếu bootstrap native hoặc env var đổi, cập nhật `docs/playbook/NATIVE_INTEGRATION.md`.
- Nếu phát hiện khoảng trống từ source, thêm vào `docs/playbook/GAPS.md`.

## Verification

- Ghi rõ command đã chạy hoặc chưa chạy.
- Ghi rõ scope ảnh hưởng theo platform.
- Không khẳng định behavior cross-platform nếu chưa soát Android và iOS.
