# CLAUDE

## Purpose

Làm việc trong repo này như một maintainer của thư viện React Native tích hợp NetEase NIM với nhiều logic native, không chỉ là bridge mỏng.

## Start Here

1. `README.md`
2. `docs/README.md`
3. `docs/common/ARCHITECTURE.md`
4. `docs/playbook/NATIVE_INTEGRATION.md`
5. feature doc liên quan
6. `docs/reference/netease-im/INDEX.md` — docs chính chủ NetEase Yunxin IM (source of truth để đối ứng API/behavior)
7. source file bị tác động ở JS, Android và iOS

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

## RULE-FLOW-01 — Trace luồng xong thì lưu lại thành `flows/`

> Luật đầy đủ + format: `RULE-FLOW-01` trong `~/.claude/CLAUDE.md` (global, áp dụng mọi repo).
> Dưới đây chỉ là phần riêng của repo này.

- **Thư mục:** `docs/flows/<tên-luồng>.md` — xem `docs/flows/README.md`.
- Task nào buộc phải trace luồng xuyên tầng (JS API → bridge → native iOS/Android → NIM SDK → event callback → JS) thì **trước khi kết thúc task**
  phải lưu / cập nhật flow tương ứng.
- **Không viết flow đầu cơ.** Chỉ lưu luồng vừa trace thật và đã kiểm chứng. Flow sai còn tệ hơn
  không có, vì nó được tin.
- Mọi chặng phải có `file:line`; chỗ chưa chắc ghi `⚠️ chưa verify`; có mục "Hay hỏng ở đâu".
- `flows/` KHÔNG `@import` vào file này.

## What To Document

- Nếu public API đổi, cập nhật feature doc tương ứng.
- Nếu bootstrap native hoặc env var đổi, cập nhật `docs/playbook/NATIVE_INTEGRATION.md`.
- Nếu phát hiện khoảng trống từ source, thêm vào `docs/playbook/GAPS.md`.

## Verification

- Ghi rõ command đã chạy hoặc chưa chạy.
- Ghi rõ scope ảnh hưởng theo platform.
- Không khẳng định behavior cross-platform nếu chưa soát Android và iOS.

## Docs Navigation & Staleness

- `docs/README.md` là index của toàn bộ tài liệu — **đọc file này trước** khi glob hoặc search trong `docs/**`.
- Nếu docs mâu thuẫn với source code thì **source thắng**. Làm theo source, đồng thời báo lại cho user biết docs nào đã lỗi thời — không im lặng đi theo docs.
- `docs/ai-output/` đã được chuyển ra ngoài repo (`~/Documents/AI-Output/<project>/`). Đó là output cũ đã tiêu thụ xong, KHÔNG phải tài liệu tham chiếu — đừng tìm và đừng tạo lại thư mục này trong repo.
