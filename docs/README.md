# Docs Map

## Mục tiêu

Bộ docs này dùng để:

- giúp maintain repo `react-native-netease-im`
- giúp tích hợp module vào app React Native khác
- giúp AI agent hoặc developer mới hiểu được boundary JS, Android, iOS

## Đọc theo thứ tự

1. `../README.md`
2. `common/ARCHITECTURE.md`
3. `playbook/NATIVE_INTEGRATION.md`
4. `playbook/REPO_MAP.md`
5. feature doc liên quan trong `features/*`
6. `playbook/GAPS.md`

## Common

- `common/ARCHITECTURE.md`: kiến trúc tổng quan và boundaries
- `common/DECISIONS.md`: các quyết định kiến trúc quan sát được từ source
- `common/CONTRIBUTING.md`: quy ước làm việc và giới hạn verify hiện tại

## Features

- `features/session/README.md`
- `features/session/media-attachment.md` — gửi/nhận/download media, field mapping native→JS, event progress, bất đối xứng Android/iOS
- `features/friend/README.md`
- `features/team/README.md`
- `features/system-msg/README.md`
- `features/chatroom/README.md`
- `features/utils/README.md`
- `features/call/README.md` — thực trạng luồng gọi thoại CSKH (NERTC Call Kit): public API `NimCall`, flow outbound/inbound, branding, gaps
- `features/_TEMPLATE.md`

## Call (NERTC Call Kit — voice customer service)

> Knowledge base tích hợp NECallKit (V3) cho tính năng gọi thoại tới CSR. Thực trạng implementation trong module: xem `features/call/README.md`; hai bộ dưới đây là docs "how" chính chủ NetEase. Version-of-truth: Yunxin changelog + target hiện tại NIM 10.9.5x + call-ui 4.3.0. ⚠️ Bỏ qua repo legacy `NERtcCallKit-iOS/-Android` (V1/V2, API khác).

- `call-android-native/README.md` — Android: context, business flow, dependency, integration, call-list, advanced, no-UI, raw source
- `call-ios-native/README.md` — iOS: cùng cấu trúc (pods, `NECallEngine`/`NERtcCallUIKit`, intercept inbound, ...)

## Playbook

- `playbook/STACK_AND_TOOLS.md`
- `playbook/REPO_MAP.md`
- `playbook/COMMANDS.md`
- `playbook/ARCHITECTURE_RULES.md`
- `playbook/NATIVE_INTEGRATION.md`
- `playbook/GAPS.md`
- `playbook/CURRENT_CONTEXT.md`
