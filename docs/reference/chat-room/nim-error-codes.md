# NIM (Yunxin) — Bảng mã lỗi đầy đủ | Error codes

**Nguồn:** docs chính thức Yunxin (owner cung cấp 2026-08-27, bản EN).
**Cách dùng cho AI/dev — đọc 4 dòng này trước:**

1. ⚠️ **Mã dạng `50000xx` (vd `5000004 禁言聊天室失败`, `5000005 发送广播失败`) KHÔNG có trong bảng này** — đó là mã do **server middleware TQ tự đặt** (lớp bọc của họ), không phải mã NIM. Gặp mã đó → vấn đề nằm ở middleware/console, hỏi team TQ.
2. Bridge iOS reject kèm mã NIM thật (`113404`/`102302`/`102404`…) — tra nhóm **Chat room (113xxx)** và **Chat room member (114xxx)** trước tiên.
3. Hai mã đáng nhớ nhất cho dự án chatroom:
   - **`113308`** — "notification for joining or leaving chatrooms disabled": callback member vào/ra bị tắt ở console (**IM > Chat Room > Sub-function Configuration > Chat Room User In-out Message System**) — đúng gốc bug member-in đã gặp.
   - **`111410`** — "broadcasting notification service disabled": broadcast toàn hệ chưa bật ở console (**IM > Global Function > Full Broadcast**) — nghi là gốc thật bên dưới mã `5000005` của middleware.
4. `113303` — phòng đã đóng không mở lại được qua đường thường (liên quan yêu cầu `toggle_close`).

---

## General status / error

| code | Desc |
|---|---|
| 0 | unknown error |
| 200 | success |
| 190001 | internal error |
| 190002 | illegal state |
| 191001 | misuse |
| 191002 | cancelled |
| 191003 | callback failed |
| 191004 | invalid parameter |
| 191005 | timeout |
| 191006 | resource does not exist |
| 191007 | resource already exists |

## Connection

| code | Desc |
|---|---|
| 192001 | connect failed |
| 192002 | connect timeout |
| 192003 | disconnected |
| 192004 | protocol timeout |
| 192005 | protocol send failed |
| 192006 | request failed |

## Database / File (client SDK local)

| code | Desc |
|---|---|
| 193001–193004 | database open/upgrade/write/read failed |
| 194001–194007 | file not found / create / open / write / read / upload / download failed |

## Application

| code | Desc |
|---|---|
| 101301 | IM disabled (chưa bật IM ở console) |
| 101302 | service address invalid |
| 101303 | Appkey does not exist |
| 101304 | bundleid check failed (console: App Key Management > Identification) |
| 101305 | illegal auth type |

## Login / Global (trùng nhau phần lớn)

| code | Desc |
|---|---|
| 201/202/203/204 | handshake / encryption algorithm errors |
| 398 | request temporarily forbidden |
| 399 | server unit error |
| 403 | forbidden (no permission) |
| 404 | not found |
| 414 | parameter error |
| 416 | rate limit exceeded |
| 417 | multi-terminal login prohibited |
| 449 | need to retry |
| 463 | third-party callback rejected |
| 500 | internal server error |
| 503 | server busy |
| 511 | app server unreachable |
| 514 | service not available |
| 599 | protocol filtered by blackhole rule |
| 997 | appid has no permission to call the protocol |
| 998/999 | unpack / pack error |
| 102301 | login record not found |
| 102302 | invalid token |
| 102303 | robot not allowed |
| 102304 | not an AI account |
| 102404 | account does not exist |
| 102405 | account already exists |
| 102421 | account chat banned |
| 102422 | account banned (disabled) |
| 102426 | account in block list |
| 102434 | limit of accounts exceeded |
| 102449 | account operation need retry |

## Conversation / group of conversations

| code | Desc |
|---|---|
| 110301 | accounts for conversations not unique |
| 110302 | conversation and account mismatch |
| 110303 | conversation stick top limit |
| 110304 | conversation group limit |
| 110404 | conversation not exist |
| 110449 | conversation operation need retry |
| 116404 | conversation group does not exist |
| 116435 | conversation group limit |
| 116437 | conversations in group limit |

## User info / Friend / DND / Blocklist (P2P)

| code | Desc |
|---|---|
| 103404 | user profile not exist |
| 103451 | user profile hit antispam |
| 104301 | peer friend limit exceeded |
| 104404 | friend not exist |
| 104405 | friend already exists |
| 104429 | self friend operation not allowed |
| 104435 | friend accounts limit exceeded |
| 104449 | friend operation rate limit |
| 104451 | friend hit antispam |
| 105435 | mute (DND) list limit exceeded |
| 105429 | self-mute not allowed |
| 106435 | blocklist limit exceeded |
| 106429 | self blocklist not allowed |
| 106403 | cannot blocklist an AI account |

## Message (107xxx — trích các mã hay gặp)

| code | Desc |
|---|---|
| 107404 | message does not exist |
| 107410 | messaging function disabled |
| 107451 | message hit antispam |
| 107301 | revoke third-party message not allowed |
| 107303/107315 | only sender or manager can revoke |
| 107304 | rate limit of high-priority messages exceeded (chatroom flow control) |
| 107306 | duplicate client message ID |
| 107314 | revoke exceeded time limit (console cấu hình) |
| 107323 | message operation rate limit |
| 107319/107320/107322 | PIN limit / not exist / already exists |
| 107325–107335, 107341–107347 | các function switch chưa bật ở console (read receipt, quick comment, PIN, voice-to-text, cloud search, one-way delete, modify msg, targeted msg…) |
| 111301 | resent message not exist |
| 189301/189302/189449 | collection limit / not exist / concurrent failed |

## Team (108xxx) & Team member (109xxx) — group chat thường, KHÔNG phải chatroom

| code | Desc |
|---|---|
| 108404 | team does not exist |
| 108406 | get online users count disabled (function switch) |
| 108423 | all team members chat banned |
| 108303 | team manager limit exceeded |
| 108430 | assign owner as manager not allowed |
| 108434/108435/108437/108305 | các limit số nhóm/số người |
| 109301/109303/109304 | ban list chứa non-member / operator / owner |
| 109305 | operation on team manager not allowed |
| 109307 | team owner quit not allowed |
| 109310 | kick operator not allowed |
| 109312 | operation on self not allowed |
| 109314 | operation on team owner not allowed |
| 109404 | team member not exist |
| 109424 | team member chat banned |
| 109427/109432 | owner (/or manager) permission required |

## Broadcast notification (toàn hệ)

| code | Desc |
|---|---|
| **111410** | 🔴 **broadcasting notification service disabled** — console: IM > Global Function > Full Broadcast |
| 111404 | broadcasting notification not exist |
| 111451/113309/113310 | hit antispam / service disabled (biến thể) |

## ⭐ Chat room (113xxx) — TRA ĐẦU TIÊN khi bridge chatroom lỗi

| code | Desc |
|---|---|
| 113301 | chatroom temporarily chat banned (cả phòng bị cấm tạm) |
| 113302 | tagged members chat banned |
| **113303** | chat room closing, **reopen not allowed** |
| 113304 | failed to get chatroom link (addr) |
| 113305 | IM connection abnormal |
| 113306 | illegal auth type |
| 113307 | identical field values (update không có gì đổi) |
| **113308** | 🔴 **notification for joining/leaving chatrooms DISABLED** — console: IM > Chat Room > Sub-function Config > User In-out Message System (gốc bug member-in) |
| 113404 | chatroom does not exist |
| 113406 | chatroom closed |
| 113409 | repeated operation |
| 113410 | chatroom service disabled |
| 113423 | all chatroom members chat banned |
| 113428 | chatroom manager permission required |
| 113434 | chatroom count limit exceeded |
| 113451 | chatroom info hit antispam |

## ⭐ Chat room member (114xxx)

| code | Desc |
|---|---|
| 114301 | target account in blocklist or chat-banned list |
| 114303 | anonymous member operation forbidden |
| 114304 | target chatroom member offline |
| 114404 | chatroom member not exist |
| 114405 | repeated operation |
| 114408 | member already deleted |
| 114421 | chatroom member chat banned |
| 114426 | account in chatroom block list (danh sách đen phòng) |
| 114427 | chatroom OWNER permission required |
| 114429 | operator in member operation list (tự thao tác lên mình) |
| 114432 | chatroom owner OR manager permission required |
| 114437 | chatroom member limit |
| 114449 | concurrent operation failed |
| 114451 | member info hit antispam |

## Subscription / Anti-spam / Server API

| code | Desc |
|---|---|
| 119410/119434/119301–119303 | subscription disabled / limit / errors |
| 195001/195002 | client / server anti-spam |
| 403 (server API) | auth header missing · invalid AppKey · CurTime lệch ±5 phút · checksum failed · access forbidden |
| 400/414 (server API) | wrong HTTP method · invalid body/path/param |
