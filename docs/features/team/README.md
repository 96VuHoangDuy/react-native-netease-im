# Team Module

## Scope

`NimTeam` quản lý:

- team list
- local/remote team detail
- member list và member info
- create/update/dismiss/quit/transfer team
- team notify setting
- admin operations
- custom team notification message cho request-list flow

## Entry Points

### JS

- `src/Team/Team.ts`
- `src/Team/team.type.ts`

### Android

- `android/src/main/java/com/netease/im/team/TeamListService.java`
- `android/src/main/java/com/netease/im/team/TeamObserver.java`
- `android/src/main/java/com/netease/im/uikit/cache/TeamDataCache.java`
- `android/src/main/java/com/netease/im/RNNeteaseImModule.java`

### iOS

- `ios/RNNeteaseIm/RNNeteaseIm/TeamViewController.h`
- `ios/RNNeteaseIm/RNNeteaseIm/TeamViewController.m`
- `ios/RNNeteaseIm/RNNeteaseIm/ConversationViewController.m`

## State And Data Flow

1. `startTeamList()` bật native delegate/observer.
2. Team list hoặc team change emit qua `observeTeam`.
3. CRUD team/member chạy qua native team manager.
4. Một phần team notification message được encode như text/custom message trong session layer.

## Public API

- `getTeamList`
- `startTeamList`
- `stopTeamList`
- `getTeamInfo`
- `setTeamNotify`
- `setMessageNotify`
- `setTeamMemberMute`
- `fetchTeamInfo`
- `fetchTeamMemberList`
- `fetchTeamMemberInfo`
- `updateMemberNick`
- `createTeam`
- `updateTeam`
- `updateTeamAvatar`
- `applyJoinTeam`
- `dismissTeams`
- `addMembers`
- `removeMember`
- `quitTeams`
- `transferTeam`
- `updateTeamName`
- `addManagersToTeam`
- `removeManagersFromTeam`
- `queryTeamByName`
- `queryAllTeams`
- `sendMessageTeamNotificationRequestJoin`

## Events

- `observeTeam`

## Business Rules

- Team type:
  - `Normal = "0"`
  - `Advanced = "1"`
- Team config fields có meaning rõ:
  - `verifyType`
  - `inviteMode`
  - `beInviteMode`
  - `teamUpdateMode`
- Module này còn hỗ trợ custom operation type:
  - thêm user vào request list
  - chấp nhận user trong request list
- `queryAllTeams()` trả cả danh sách team và `ownedGroupCount`.

## Platform Notes

### Android

- Team list search/load dựa trên contact data task trong `TeamListService`.
- Cache team tập trung ở `TeamDataCache`.
- Bridge Android đồng thời shape một phần team info trả về JS.

### iOS

- `TeamViewController` handle hầu hết thao tác team manager.
- Một số team notification message được dựng trong `ConversationViewController`.

## Error Handling Notes

- Hầu hết method trả `NIMResponseCode`.
- Một số thao tác thành viên/team detail có thể thành công một phần ở tầng SDK, cần app host tự quyết định UX dựa trên code trả về.

## Gaps

- Repo không có docs business cho custom request-list workflow ngoài source.
- Session layer và team layer cùng tham gia team notification message, nên maintain cần đọc cả hai khi sửa behavior này.
