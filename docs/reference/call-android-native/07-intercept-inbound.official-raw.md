# 07 — 拦截呼入请求 / Intercept incoming call requests (OFFICIAL RAW — Android)

> **Nguồn:** docs chính chủ NetEase Yunxin (NERTC CallKit — 拦截呼入请求, Android). Lưu verbatim để tra cứu nhanh.
> Bản chắt lọc + call site thật + phạm vi tác dụng theo app state: [`03-advanced.md` §Intercept inbound](./03-advanced.md#intercept-inbound-requests).
> Bài liên quan **自定义UI / Custom UI**: <https://doc.yunxin.163.com/nertccallkit/guide/zYzNzI5NDI?platform=android>
> — ⚠️ chưa lấy được nội dung (site chặn fetch tự động). Ai có quyền truy cập thì paste vào `08-custom-ui.official-raw.md`.

---

## Function Introduction

`DefaultIncomingCallEx` This class is primarily used to receive calls, display notifications, and launch the
configured target incoming call page when a user uses the call component's UI layer. If you don't want the
call component's default incoming call page and want to intercept incoming call requests to create a custom
incoming call page, you only need to inherit from this class `DefaultIncomingCallEx` and override the
relevant methods.

## Implementation method

When initializing the call component, the method `incomingCallEx` in the `CallKitUIOptions` object is called
to intercept the incoming call request, and then the incoming call notification page is modified through
custom UI.

The following example code demonstrates how to intercept incoming call requests:

```java
CallKitUIOptions options = new CallKitUIOptions.Builder()
    .incomingCallEx(new DefaultIncomingCallEx(){
        // 收到来电触发，返回值表示用户在后台收到来电时，单击应用图标是否展示来电页面,
        // true 表示此呼叫已经被消耗不会启动页面，false 表示此呼叫没有被消耗需要启动页面
        @Override
        public boolean onIncomingCall(@NonNull InvitedInfo invitedInfo){
        // 检查参数合理性
        if (!isValidParam(invitedInfo)){
            return true;
        }
        // 直接启动目标页面
        MainActivity.this.startActivity(toCallIntent(invitedInfo));
        // 生成通知并提醒
        generateNotificationAndNotify(invitedInfo);
        return false;
        }
    })
// 若重复初始化会销毁之前的初始化实例，重新初始化
CallKitUI.init(getApplicationContext(), options);
```

---

## Ghi chú khi đọc bản raw này

Comment tiếng Trung ở dòng đầu code mẫu là chỗ **duy nhất** định nghĩa ngữ nghĩa giá trị trả về, nên giữ
nguyên văn. Dịch sát:

> Được gọi khi có cuộc gọi đến. Giá trị trả về cho biết: khi user nhận cuộc gọi lúc app ở nền, bấm vào icon
> app thì có hiện màn hình cuộc gọi hay không. `true` = cuộc gọi này **đã bị tiêu thụ**, sẽ không dựng màn
> hình. `false` = cuộc gọi **chưa bị tiêu thụ**, cần dựng màn hình.

Hai điểm bản raw không nói, đã verify bằng bytecode `call-ui:4.3.0` — chi tiết ở
[`03-advanced.md` §Intercept inbound](./03-advanced.md#intercept-inbound-requests):

- Implementation mặc định trả về `false` ở **cả ba** nhánh, không nhánh nào trả `true`.
- `InvitedInfo` trong code mẫu là tên cũ; class thực tế ở 4.3.0 là
  `com.netease.yunxin.kit.call.p2p.model.NEInviteInfo`.
