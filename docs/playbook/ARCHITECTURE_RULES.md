# Architecture Rules

1. Mọi thay đổi public API phải được kiểm tra ở ba lớp:
   JS wrapper, Android bridge, iOS bridge.

2. Event name và payload shape là một phần của public contract, không chỉ là implementation detail.

3. Các key sau phải được coi là contract cross-platform:
   `extendType`, `messageSubType`, `localExt`, `remoteExt`, custom attachment type.

4. Không di chuyển logic message/business lớn từ native sang JS nếu không có kế hoạch parity đầy đủ cho Android và iOS.

5. Khi thêm behavior mới cho session/message/team/system notification, phải xác định:
   Promise result đi đâu, event nào phát ra, type nào cần update.

6. Platform-specific behavior phải được ghi rõ trong docs nếu vẫn giữ chung một JS method.

7. Bootstrap app-level là trách nhiệm của consumer app. Nếu source thêm phụ thuộc mới vào AppDelegate, Manifest, env var hoặc push flow, docs phải cập nhật ngay.

8. Nếu native emit event mới mà JS enum chưa biết, hoặc một nền tảng có event extra, phải ghi rõ vào docs và cân nhắc bổ sung type.
