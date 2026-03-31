# AGENTS

## Mission

Bảo trì repo này như một thư viện React Native có native runtime phức tạp. Không coi đây là wrapper mỏng.

## Read First

1. `README.md`
2. `docs/README.md`
3. `docs/common/ARCHITECTURE.md`
4. `docs/playbook/NATIVE_INTEGRATION.md`
5. feature doc liên quan trong `docs/features/*`
6. file JS/native thực tế sẽ sửa

## Guardrails

- `index.ts` và `src/*` là public JS surface.
- `RNNeteaseImModule.java` và `RNNeteaseIm.m` là public native bridge surface.
- Event name phát về JS là public contract, không được đổi tên tùy ý.
- Các key custom attachment và `messageSubType` là contract cross-platform.
- Nếu đổi behavior của message, recent session, push, cache, audio, team/system message thì phải soát cả Android và iOS.
- Không giả định app host đã được bootstrap đúng; tài liệu phải nêu rõ trách nhiệm của host app.

## Native Rules

- Android:
  - đọc ít nhất `RNNeteaseImModule.java`
  - nếu liên quan session/message thì đọc `session/SessionService.java`
  - nếu liên quan login/recent/system message thì đọc `login/*`
- iOS:
  - đọc ít nhất `RNNeteaseIm.m`
  - nếu liên quan session/message thì đọc `ConversationViewController.*`
  - nếu liên quan recent/system/friend/team/chatroom thì đọc controller tương ứng

## Safety Rules

- Không xóa hoặc đổi tên event contract nếu chưa có yêu cầu rõ.
- Không đổi version dependency native lớn nếu chưa được yêu cầu.
- Không thêm suy đoán vào docs; thiếu dữ liệu thì ghi gap.
- Không coi README cũ là nguồn sự thật duy nhất; luôn ưu tiên source hiện tại.

## Verify Before Handoff

- Nêu rõ command đã chạy.
- Nếu không build/test được thì nói rõ lý do.
- Xác nhận parity JS/Android/iOS cho phần đã sửa hoặc nói rõ phần nào chưa xác minh.

## Response Contract

- Nêu thay đổi chính trước.
- Nêu verification.
- Nêu risk hoặc gap còn lại.
- Nêu docs nào đã cập nhật.

## Docs Rule

- Nếu sửa feature hiện có, cập nhật `docs/features/<feature>/README.md` trong cùng thay đổi nếu contract hoặc behavior đổi.
- Nếu thêm feature mới, tạo feature doc tương ứng hoặc ghi rõ chưa đủ dữ liệu để tạo.
