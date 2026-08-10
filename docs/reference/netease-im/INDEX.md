# NetEase Yunxin IM — Reference Index (source of truth)

Kho tham chiếu docs **chính chủ** của NetEase Yunxin IM, mirror làm source-of-truth cục bộ để
đối ứng API/behavior khi maintain module `react-native-netease-im`.

- **Nguồn**: [`netease-kit/yunxin-im-skills`](https://github.com/netease-kit/yunxin-im-skills) (git submodule, sparse-checkout).
- **Ngôn ngữ docs**: Tiếng Trung (bản gốc NetEase).
- **Phạm vi mirror**: chỉ nền tảng liên quan module này — **android, ios, web (JS/RN), react** (SDK + UIKit).
  Đã loại: flutter, harmony, node, pc, electron, vue3, miniapp, uniapp.
- **Vị trí**: `docs/reference/netease-im/yunxin-im-skills/`

> ⚠️ Đây là **submodule sparse**. Sau khi clone repo trên máy mới, phải chạy:
> ```bash
> git submodule update --init docs/reference/netease-im/yunxin-im-skills
> ```

---

## 0. Project implementation notes (nội bộ — "những gì thực sự chạy")

Khác với mirror crawl bên dưới (docs upstream NetEase), đây là ghi chép triển khai thật của project:

| File | Nội dung |
|---|---|
| [`LOGIN_V9_TO_V10_MIGRATION.md`](LOGIN_V9_TO_V10_MIGRATION.md) | **Vì sao** migrate login V9→V10 + **mọi thay đổi & lý do** (2 repo, iOS+Android): init/login API, appKey (`updateAppKey`), currentAccount (`zyzjCurrentAccount`), autoLogin token, display bug. **Đọc đầu tiên** khi debug login/chat. |
| [`CALLKIT_INTEGRATION_NOTES.md`](CALLKIT_INTEGRATION_NOTES.md) | Cấu hình thật + gotcha khi tích hợp NERTC Call Kit (voice call): version pin, bridge, lỗi native đã fix, gap rtcAppKey, **来电横幅 + LiveCommunicationKit (#10–#14)**. |
| [`CALLKIT_VERSION_COMPAT.md`](CALLKIT_VERSION_COMPAT.md) | Ma trận tương thích call-ui ↔ NIM ↔ NERTC. **Android 4.3.0** (bump cho banner), **iOS thực tế 4.8.1** (podspec không pin → skew, đã ghi rõ). |
| [`../call-ios-native/05-system-call-lck.md`](../call-ios-native/05-system-call-lck.md) | 接听系统电话 (LiveCommunicationKit / PushKit VoIP) — **iOS only**, nhận cuộc gọi khi app bị kill. Android không có đối ứng (framework của Apple). |

---

## 1. IMSDK reference (lõi — quan trọng nhất cho native bridge)

`yunxin-im-skills/im/sdk/reference/<platform>/<topic>.md` — platform ∈ `android` | `ios` | `web`.

| Chủ đề | Topic file | Native (android/ios) | JS/RN (web) |
|---|---|---|---|
| Khởi tạo SDK | `initialization` | ✅ | `web/RN_initialization.md` (RN-specific) + `web/*` không có `initialization` |
| Đăng nhập / đăng xuất | `login_logout` | ✅ | ✅ |
| Gửi / nhận message | `send_receive_message` | ✅ | ✅ |
| Lịch sử message | `history_message` | ✅ | ✅ |
| Cloud conversation (session server) | `cloud_conversation` | ✅ | ✅ |
| Local conversation | `local_conversation` | ✅ | ✅ |
| Quan hệ bạn bè | `friend_relationship` | ✅ | ✅ |
| Danh sách chặn (block) | `block_list` | ✅ | ✅ |
| Hồ sơ người dùng | `user_profile` | ✅ | ✅ |
| Đăng ký trạng thái online | `user_status_subscription` | ✅ | ✅ |
| API reference (chữ ký method) | `api_reference` | chỉ có ở **android** | — |

RN-specific: [`im/sdk/reference/web/RN_initialization.md`](yunxin-im-skills/im/sdk/reference/web/RN_initialization.md)

## 2. IMUIKit reference (tham chiếu behavior UI component)

`yunxin-im-skills/im/uikit/reference/<platform>/<topic>.md` — platform ∈ `android` | `ios` | `react` | `h5_react`.

- **Native**: `android` (12 topics: chat, contact, conversation, conversation_contact, faq,
  import_components, initialization, integration, kit_config, login_logout, permission, proguard),
  `ios` (8 topics: chat, contact, conversation, faq, initialization, integration, kit_config, login_logout).
- **React (web, gần RN nhất)**: `react` (chat, chat_standalone, contact, conversation_list,
  import_components, initialization, integration, login_logout, search), `h5_react` (initialization).

## 3. Coverage tổng quan

Bảng liệt kê đầy đủ mọi platform/topic của repo gốc (kể cả nền tảng **không** mirror):
[`im/reference/coverage.md`](yunxin-im-skills/im/reference/coverage.md)

---

## Cross-link sang docs nội bộ project

| Docs nội bộ (`docs/features/…`) | NetEase reference tương ứng |
|---|---|
| `session/` | SDK `cloud_conversation`, `local_conversation`; UIKit `conversation` |
| `friend/` | SDK `friend_relationship`, `block_list`, `user_profile`; UIKit `contact` |
| `team/` | SDK `send_receive_message` (team message), `cloud_conversation` (team session) |
| `system-msg/` | SDK `friend_relationship` (verify/notification), `send_receive_message` (custom/system) |
| `chatroom/` | ⚠️ Không có trong mirror này — chatroom là sản phẩm NIM riêng, tra online khi cần |
| `utils/` | SDK `api_reference` (android) |

---

## External refs (KHÔNG mirror — chỉ link)

Hub demo app: [`netease-kit/NetEase-IM`](https://github.com/netease-kit/NetEase-IM) → trỏ tới các demo code:

- [`NIM_iOS_Demo`](https://github.com/netease-kit/NIM_iOS_Demo) — Objective-C. ⚠️ Cũ (last push 2022), ~98MB.
- [`NIM_Android_Demo`](https://github.com/netease-kit/NIM_Android_Demo) — Java. ⚠️ Cũ (2022), ~285MB.

> Cố ý không mirror: nặng + stale, không phải docs. Nếu cần đọc code demo, clone tạm vào scratchpad.

---

## Cách update mirror

```bash
git submodule update --remote docs/reference/netease-im/yunxin-im-skills
git add docs/reference/netease-im/yunxin-im-skills && git commit -m "chore: bump netease-im docs mirror"
```

Sparse set hiện tại (thêm platform nếu cần):
```bash
git -C docs/reference/netease-im/yunxin-im-skills sparse-checkout add im/sdk/reference/flutter
```
