# Notification cuộc gọi đến có nút Trả lời / Từ chối (process còn sống)

## Context

Sau khi fix phương án B (notification fallback không sound + text tử tế), owner muốn nâng cấp tiếp:
notification cuộc gọi đến (khi thiếu overlay, app còn sống) phải có **2 nút Answer/Reject** thao tác
được ngay, không cần mở app.

Trả lời câu hỏi của owner: `notificationConfigFetcher` **không** truyền custom notification được — nó
chỉ trả 4 trường (icon/channel/title/content) cho SDK tự build. Muốn có button phải thay người build:
override `generateNotificationAndNotify` trong một `IncomingCallEx` custom.

Đã verify bytecode call-ui **4.3.0** (AAR trong `~/.gradle/caches/modules-2/files-2.1/com.netease.yunxin.kit.call/call-ui/4.3.0/`, ngày 2026-07-31):
- `DefaultIncomingCallEx.generateNotificationAndNotify(NEInviteInfo)` = `protected` → override được,
  và SDK **chỉ gọi nó ở nhánh fallback** (thiếu overlay) — nhánh banner không đụng → override điểm này
  là giữ nguyên toàn bộ flow còn lại.
- `INCOMING_CALL_NOTIFY_ID` public static; `cancelNotification()` protected — post đè cùng id thì
  `onIncomingCallInvalid` của SDK (caller hủy/timeout) **tự cancel notification của mình**, không phải
  viết cleanup.
- `toCallIntent(NEInviteInfo)` = `protected` → tái dùng làm contentIntent + Answer intent.
- `CallKitUIOptions.Builder.incomingCallEx(IncomingCallEx)` tồn tại.
- `NECallEngine.accept(NEResultObserver)`, `hangup(NEHangupParam, NEResultObserver)` — signature 4.3.0.

**Quyết định thiết kế: KHÔNG dùng `CallStyle`** (OS bắt notification CallStyle phải gắn foreground
service hoặc full-screen intent — FSI đang bị strip khỏi manifest vì rủi ro Play review). Dùng
`addAction` thường: 2 nút text, chạy mọi API level, không ràng buộc.

## Việc 1 — `CsIncomingCallEx` (file mới, lib android)

`react-native-netease-im/android/src/main/java/com/netease/im/CsIncomingCallEx.java`
(⚠️ tên file này từng tồn tại ở round đã revert — viết MỚI hoàn toàn, không lấy lại code cũ):

```java
public class CsIncomingCallEx extends DefaultIncomingCallEx {
    @Override
    protected void generateNotificationAndNotify(NEInviteInfo invitedInfo) {
        // KHÔNG gọi super — thay notification của SDK bằng của mình, CÙNG INCOMING_CALL_NOTIFY_ID.
        Intent callIntent = toCallIntent(invitedInfo);
        PendingIntent contentPi = activity PI (requestCode 0) từ callIntent;
        PendingIntent answerPi  = activity PI (requestCode 1) từ callIntent.clone() + extra EXTRA_AUTO_ACCEPT=true;
        PendingIntent rejectPi  = broadcast PI (requestCode 2) → CallActionReceiver ACTION_REJECT;
        NotificationCompat.Builder(context, CallService.CHANNEL_INCOMING_CALL_ALIVE)
            .setSmallIcon(IMApplication.getNotify_msg_drawable_id())
            .setContentTitle("Cuộc gọi đến").setContentText("Bạn có cuộc gọi thoại đến")
            .setCategory(CATEGORY_CALL).setPriority(PRIORITY_HIGH)
            .setContentIntent(contentPi).setOngoing(true).setAutoCancel(false)
            .addAction(0, "Từ chối", rejectPi)
            .addAction(0, "Trả lời", answerPi);
        NotificationManagerCompat.notify(INCOMING_CALL_NOTIFY_ID, ...);
    }
}
```

- PendingIntent flags: `FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE`; 3 requestCode khác nhau.
- **Answer đi đường activity-PendingIntent** (không broadcast-rồi-startActivity — dính chặn
  background-activity-launch Android 10+). Bấm notification action = user interaction → OS cho mở
  activity hợp lệ.
- Text nút/title tạm hardcode tiếng Việt, khớp các string hiện có (localize là việc riêng owner chưa chốt).

## Việc 2 — Auto-accept trong `CsP2PCallFragmentActivity` (file có sẵn)

`react-native-netease-im/android/src/main/java/com/netease/im/CsP2PCallFragmentActivity.java`:

- Nhận `EXTRA_AUTO_ACCEPT` ở cả `onCreate` lẫn `onNewIntent` (activity có thể đã mở sẵn do SDK
  fallback startActivity), consume một lần.
- Guard: chỉ auto-accept khi đã có quyền `RECORD_AUDIO` (accept không mic sẽ fail/hỏng flow SDK) —
  thiếu quyền thì chỉ mở màn đổ chuông như tap thường, user bấm nút accept trên UI để đi qua luồng
  xin quyền chuẩn của SDK.
- Accept: `NECallEngine.sharedInstance().accept(observer-null-safe)` — màn callee đang hiển thị sẽ tự
  chuyển sang on-the-call vì SDK lắng nghe engine state.

## Việc 3 — `CallActionReceiver` (file mới) + manifest

- `react-native-netease-im/android/src/main/java/com/netease/im/CallActionReceiver.java`:
  `ACTION_REJECT` → `NECallEngine.sharedInstance().hangup(new NEHangupParam(null, null), null)` +
  `NotificationManagerCompat.cancel(INCOMING_CALL_NOTIFY_ID)`. Try/catch toàn thân (receiver không
  được crash app).
- `react-native-netease-im/android/src/main/AndroidManifest.xml`: đăng ký receiver `exported="false"`.

## Việc 4 — Đấu dây trong `CallService.init`

- Options builder: **thay** `.notificationConfigFetcher(...)` (vừa thêm ở round trước, sẽ thành dead
  config vì override không đọc fetcher nữa) bằng `.incomingCallEx(new CsIncomingCallEx())`.
- Giữ nguyên: channel `cskh_incoming_call_alive_v2` (không sound — SoundHelper lo chuông),
  `cskh_incoming_call_v2`, string override `tip_notification_channel_name`, pushPayload.

## Việc 5 — Sync + build + cài

1. Sync 4 file (3 java + manifest) sang `pyeon-chinese-mobile/node_modules/react-native-netease-im/`
   (KHÔNG patch-package), `diff -rq` sạch.
2. Build APK release (một gradle build tại một thời điểm) → cài vivo (WiFi `192.168.55.109:36039`) +
   Xiaomi (khi cắm lại USB), verify md5.

## Verification (trên vivo, overlay đang bị thu)

| # | Case | Kỳ vọng |
|---|---|---|
| 1 | App background, gọi tới | Heads-up có 2 nút Từ chối/Trả lời, không sound notification (chuông = caller_ring từ SoundHelper) |
| 2 | Bấm **Từ chối** | Cuộc gọi kết thúc phía caller, notification biến mất, không mở app |
| 3 | Bấm **Trả lời** (đã có quyền mic) | Mở màn gọi, đã ở trạng thái on-the-call (không phải màn đổ chuông) |
| 4 | Bấm **Trả lời** (chưa cấp mic) | Mở màn đổ chuông bình thường, không crash |
| 5 | Caller hủy trước khi bấm | Notification tự biến mất (cancelNotification của SDK theo NOTIFY_ID chung) |
| 6 | Tap thân notification | Mở màn đổ chuông như hiện tại |
| 7 | Regression: cấp lại overlay (`appops set ... allow`) rồi gọi | Banner hiện như cũ, không có notification thừa |

Log check: `adb logcat -d | grep -E "CsIncomingCallEx|DefaultIncomingCallEx|IncomingCallBanner"`.
