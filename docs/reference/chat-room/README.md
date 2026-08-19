# Chat Room — Reference Docs Index

Docs chính thức NetEase NIM **V2 chatroom** (`V2NIMChatroomClient` — [D-015] đã chốt dùng V2) do owner tải về 2026-08-15/17, đã soát và tổ chức lại 2026-08-18.

⛔ **Các file trong `raw-docs/` rất dài (7K–170K, code đủ mọi platform).** Đừng Read nguyên file — dùng bảng dưới để nhảy đúng section (line number), chỉ đọc code block Android/iOS.

## raw-docs/ — NIM V2 Chatroom SDK (client)

| File | Dùng khi nào (Phase 1) | Section quan trọng |
|---|---|---|
| `overview.md` (15K) | Đọc 1 lần lấy khái niệm | `:59` Member role (creator/manager/normal/guest) |
| `open&configure.md` (7K) | Cấu hình console | `:30` Sub-function (config số ngày history) · `:47` login policy |
| `chat-room-auth.md` (147K) | **Bridge login V2** | `:66` Prepare Token (static = IM token) · `:104` Create instance · `:352` Login listener · `:858` Link address (map `chatroom/addr`) · `:1051` Login · `:2985` Logout · `:3081` Destroy |
| `message-management.md` (171K) | **Bridge send/receive + history** | `:23` Send/receive · `:1866` Notification message (ROLE_UPDATE, member in/out…) · `:1930` **History messages (mặc định 10 ngày, `getMessageList`)** |
| `management.md` (22K) | Room info | `:19` Event monitoring (room info update) · `:186` Get info |
| `member-management.md` (99K) | **Member list + role + ban** | `:38` Event listeners (role/ban callbacks) · `:429` Paging get members · `:1515` updateMemberRole (⚠️ creator-only — mobile KHÔNG dùng) · `:1771` Blacklist · `:1975` Ban status · `:2177` Temp ban · `:2393` Kick |
| `queue-management.md` (63K) | ❌ Không dùng Phase 1 | — |
| `tag-management.md` (51K) | ❌ Không dùng Phase 1 | — |

## ../zyzj-im-middleware/ — API middleware team TQ (bản copy từ `~/Documents/ZYZJ _File/API_EN/`)

| File | Nội dung |
|---|---|
| `Readme.md` | Mục lục toàn bộ |
| `comm.md` | **Giao thức bắt buộc**: AES + HmacSHA1 sign (backend đã implement trong `chat.service.ts`) |
| `chatroom.md` | **9 API chatroom**: list/addr/create/save/close (đã nối) + `set_admin`/`mute`/`broadcast` (CHƯA nối) |
| `account.md` | `login` trả `token` + `app_key` → static token cho chatroom |
| `session.md` | Temporary session (liên quan bypass [D-012]) |
| `friend.md`, `group_admin.md`, `service.md`, `system.md`, `translate.md`, `atta_*.md` | Ngoài scope chat-room |

## Liên quan

- Phân tích + quyết định feature: `pyeon-chinese-mobile/docs/features/chat-room/` (nhánh `feat/chat-room`)
- ⚠️ `docs/features/chatroom/README.md` (repo này) phần Android đang SAI (ghi bridge đã có — thực tế chưa) — sửa khi code bridge V2
