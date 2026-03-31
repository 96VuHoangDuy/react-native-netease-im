# Current Context

## Audit Scope

Ngày audit tài liệu hiện tại tập trung vào:

- dựng lại docs top-down cho thư viện
- rà kỹ JS public modules
- đối chiếu native Android và iOS
- ghi rõ bootstrap requirement của app host

## Hotspots

- `src/Session/Session.ts`
- `android/src/main/java/com/netease/im/RNNeteaseImModule.java`
- `android/src/main/java/com/netease/im/session/SessionService.java`
- `ios/RNNeteaseIm/RNNeteaseIm/RNNeteaseIm.m`
- `ios/RNNeteaseIm/RNNeteaseIm/ConversationViewController.m`
- `ios/RNNeteaseIm/RNNeteaseIm/NIMViewController.m`

## Owner Inputs Còn Thiếu

- quy trình release/publish package
- app host chuẩn đang dùng thư viện này là app nào
- chính sách branch / PR / code review
- có muốn expose đầy đủ event extra ra type JS hay không
- có muốn đồng bộ hóa config anti-spam và user-cache giữa Android/iOS hay không

## Notes

- File này có thể chỉnh tay sau mỗi đợt maintain lớn.
- Nếu thêm feature mới, cập nhật docs feature trước rồi mới sửa file này.
