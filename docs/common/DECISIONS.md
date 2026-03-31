# Decisions

## ADR-001

- Status: accepted, quan sát từ source
- Decision: JS public API dùng `NativeModules` imperative thay vì TurboModule hoặc JS state layer riêng.
- Why: `index.ts`, `src/*` và `Utils.ts` chỉ forward sang native module.
- Tradeoff: dễ dùng với RN cũ hơn, nhưng contract runtime phụ thuộc mạnh vào native implementation và event name.

## ADR-002

- Status: accepted, quan sát từ source
- Decision: trạng thái recent session, incoming message, unread count và nhiều cập nhật khác được đẩy về JS bằng native event.
- Why: Android dùng `ReactCache.emit(...)`, iOS route qua `NIMModel.myBlock` và `bridge.eventDispatcher`.
- Tradeoff: JS side phải subscribe đúng event; payload drift giữa hai nền tảng là rủi ro thực tế.

## ADR-003

- Status: accepted, quan sát từ source
- Decision: logic chat core được giữ trong native service/controller lớn thay vì chia nhỏ ở JS.
- Why:
  - Android có `SessionService.java`
  - iOS có `ConversationViewController.m`
- Tradeoff: behavior chat sát NIM SDK hơn, nhưng refactor khó và cần parity cross-platform cao.

## ADR-004

- Status: accepted, quan sát từ source
- Decision: business semantics của message được encode bằng custom attachment, `remoteExt`, `localExt` và `messageSubType`.
- Why: source có custom attachment cho red packet, transfer, forward multiple text, chatbot, temporary session, reaction.
- Tradeoff: linh hoạt cho nghiệp vụ, nhưng khó discover nếu không có docs và rất dễ lệch giữa Android/iOS.

## ADR-005

- Status: accepted, quan sát từ source
- Decision: app host chịu trách nhiệm bootstrap phần app-level như app key, APNs/push forwarding, activity/app delegate wiring.
- Why: Android cần `IMApplication.init(...)`, manifest meta-data và intent handling; iOS cần APNs registration và `ObservePushNotification`.
- Tradeoff: library linh hoạt cho nhiều app host, nhưng integration cost cao và dễ thiếu bước.

## ADR-006

- Status: accepted, quan sát từ source
- Decision: repo chấp nhận divergence có kiểm soát theo nền tảng trong cùng JS module.
- Why:
  - `NimUtils.fetchNetInfo()` ghi chú Android only
  - `playLocal` build path khác nhau giữa iOS/Android
  - `getLaunch()` chỉ có ý nghĩa trên Android
  - iOS đọc env qua `RNCConfig`, Android lại có setter nội bộ chưa bridge
- Tradeoff: API JS tiện hơn cho consumer, nhưng docs phải ghi rất rõ platform note.
