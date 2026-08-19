# 聊天室成员管理

NetEase Yunxin IM supports users to query, add and remove group members, manage member roles and other functions. It has a perfect member permission system and management functions. All users in the group are divided into group owners, administrators, and ordinary members according to their permissions.

## Support platform

The development platform or framework applicable to this article is shown in the following table. For the interfaces involved, please refer to the following [relevant interface](#%E7%9B%B8%E5%85%B3%E6%8E%A5%E5%8F%A3)chapters:

| Android | iOS | macOS/Windows | Web/uni-app/applet | Node.js/Electron | Hongmeng | Flutter |
| ------- | --- | ------------- | ------------------ | ---------------- | -------- | ------- |
| ✔️️️️      | ✔️️️️  | ✔️️️️            | ✔️️️️                 | ✔️️️️               | ✔️️️️       | ✔️      |

## Technical principle

NetEase Yunxin NIM SDK provides relevant methods for managing chat room members to help you quickly implement and use the member permission system and management functions of the chat room.

The roles of chat room members are divided into two categories: fixed members and non-fixed members:

- **Fixed members**: creator, administrator, ordinary member. The total upper limit is 1,000 people.
  - Administrators are assigned and managed by the creator. Administrators cannot operate on creators and other administrators.
  - Blacklisted users are blocked chat room members who are disconnected from the chat room and cannot send and receive messages.
  - Permanently banned users are chat room members who are permanently banned and can receive messages but cannot send messages. After the permanent ban is lifted, it will not affect the expiration time of the temporary ban.
  - If you repeatedly set a temporary ban, the last setting will overwrite the expiration time of the previous setting (not cumulative).
- **No upper limit on the number of non-fixed members**:
  - **Tourists**: ordinary tourists, anonymous tourists.
    - Except for the creator, other members are all tourists by default when they first join the chat room. According to the login parameters (`anonymousMode`), they are judged as ordinary tourists or anonymous tourists.
    - Tourists are non-fixed members of the chat room only when they are online. After the tourist enters and exits the chat room, he/she is not a member of the chat room (which has nothing to do with the chat room).
  - **Fictional members**: fictional users. Only support [adding chat room fictitious users](https://doc.yunxin.163.com/messaging2/server-apis/TA3OTM3ODU?platform=server)through the new version of the server API.

## Preparation work

Before following this article, please make sure that you have completed the following settings:

- [Chat room login](https://doc.yunxin.163.com/messaging2/guide/DI2NDc1NzQ?platform=client)has been [realized](https://doc.yunxin.163.com/messaging2/guide/DI2NDc1NzQ?platform=client).
- It has been issued in the [NetEase Yunxin console. The](https://app.yunxin.163.com/global/home)**chat room user's entry and exit message system has been issued**. For details, please refer to the [opening and configuration of the chat room](https://doc.yunxin.163.com/console/concept/zYyMjAzNDY?platform=console). Only when this function is turned on will the chat room callback be triggered.
- 在使用聊天室服务中的 API 前，需要先调用 `getChatroomService` 方法获取聊天室服务类。

## Chat room-related event monitoring

Before the chat room-related operations, you can register to listen to chat room-related events. After listening, you will receive the corresponding notification after the chat room management operation.

- **Related callback**:
  - **`onChatroomMemberEnter`**: Members enter the chat room to call back and return to the list of chat room members. All members in the chat room will receive the callback.
  - **`onChatroomMemberExit`**: Members exit the chat room callback and return to `accountId`. All members in the chat room will receive the callback.
  - **`onChatroomMemberRoleUpdated`**: Chat room member role type change callback, return the chat room member and the changed member role. All members in the chat room will receive the callback.
  - **`onChatroomMemberInfoUpdated`**: The chat room member information changes back and returns the changed chat room member information. All members in the chat room will receive the callback.
  - **`onSelfChatBannedUpdated`**：聊天室成员本人禁言状态变更回调，返回 `chatBanned` 禁言状态。
  - **`onSelfTempChatBannedUpdated`**：聊天室成员本人临时禁言状态变更回调，返回 `tempChatBanned` 临时禁言状态和 `tempChatBannedDuration` 临时禁言时长（秒）。
  - **`onChatroomChatBannedUpdated`**：聊天室整体禁言状态变更回调，返回 `chatBanned` 禁言状态。

- **Sample code**:

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
V2NIMChatroomListener listener = new V2NIMChatroomListener() {
    @Override
    public void onChatroomMemberEnter(V2NIMChatroomMember member) {
    }

    @Override
    public void onChatroomMemberExit(String accountId) {
    }

    @Override
    public void onChatroomMemberRoleUpdated(V2NIMChatroomMemberRole previousRole, V2NIMChatroomMember member) {
    }

    @Override
    public void onChatroomMemberInfoUpdated(V2NIMChatroomMember member) {
    }

    @Override
    public void onSelfChatBannedUpdated(boolean chatBanned) {
    }

    @Override
    public void onSelfTempChatBannedUpdated(boolean tempChatBanned, long tempChatBannedDuration) {
    }

    @Override
    public void onChatroomChatBannedUpdated(boolean chatBanned) {
    }
};

v2ChatroomService.addChatroomListener(listener);

```

iOS

```
@interface Listener: NSObject<V2NIMChatroomListener>
- (void)addToService;
@end

@implementation Listener

- (void)addToService
{
    id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];
    [service addChatroomListener:self];
}

- (void)onChatroomMemberEnter:(V2NIMChatroomMember *)member
{
}

- (void)onChatroomMemberExit:(NSString *)accountId
{
}

- (void)onChatroomMemberRoleUpdated:(V2NIMChatroomMemberRole)previousRole
                            member:(V2NIMChatroomMember *)member
{
}
- (void)onChatroomMemberInfoUpdated:(V2NIMChatroomMember *)member
{
}

- (void)onSelfChatBannedUpdated:(BOOL)chatBanned
{
}

- (void)onSelfTempChatBannedUpdated:(BOOL)tempChatBanned
            tempChatBannedDuration:(NSInteger)tempChatBannedDuration
{
}

- (void)onChatroomChatBannedUpdated:(BOOL)chatBanned
{
}

- (void)onMessageRevokedNotification:(NSString *)messageClientId
                        messageTime:(NSTimeInterval)messageTime
{
}

@end

```

macOS/Windows

```
V2NIMChatroomListener listener;
listener.onChatroomMemberEnter = [](V2NIMChatroomMember member) {
    // handle chatroom member enter
};
listener.onChatroomMemberExit = [](nstd::string accountId) {
    // handle chatroom member exit
};
listener.onChatroomMemberRoleUpdated = [](V2NIMChatroomMemberRole previousRole, V2NIMChatroomMember member) {
    // handle chatroom member role updated
};
listener.onChatroomMemberInfoUpdated = [](V2NIMChatroomMember member) {
    // handle chatroom member info updated
};
listener.onSelfChatBannedUpdated = [](bool chatBanned) {
    // handle self chat banned updated
};
listener.onSelfTempChatBannedUpdated = [](bool tempChatBanned, uint64_t tempChatBannedDuration) {
    // handle self temp chat banned updated
};
listener.onChatroomChatBannedUpdated = [](bool chatBanned) {
    // handle chatroom chat banned updated
};
listener.onMessageRevokedNotification = [](nstd::string messageClientId, uint64_t messageTime) {
    // handle message revoked notification
};
chatroomService.addChatroomListener(listener);

```

Web/uni-app/小程序

```
chatroom.V2NIMChatroomService.on('onChatroomMemberEnter', function (member: V2NIMChatroomMember){})
chatroom.V2NIMChatroomService.on('onChatroomMemberExit', function (accountId: string){})
chatroom.V2NIMChatroomService.on('onChatroomMemberRoleUpdated', function (previousRole: V2NIMChatroomMemberRole, currentMember: V2NIMChatroomMember){})
chatroom.V2NIMChatroomService.on('onChatroomMemberInfoUpdated', function (member: V2NIMChatroomMember){})
chatroom.V2NIMChatroomService.on('onSelfChatBannedUpdated', function (chatBanned: boolean){})
chatroom.V2NIMChatroomService.on('onSelfTempChatBannedUpdated', function (tempChatBanned: boolean, tempChatBannedDuration: number){})
chatroom.V2NIMChatroomService.on('onChatroomChatBannedUpdated', function (chatBanned: boolean){})

```

Node.js/Electron

```
chatroom.chatroomService.on('chatroomMemberEnter', function (member: V2NIMChatroomMember){})
chatroom.chatroomService.on('chatroomMemberExit', function (accountId: string){})
chatroom.chatroomService.on('chatroomMemberRoleUpdated', function (previousRole: V2NIMChatroomMemberRole, currentMember: V2NIMChatroomMember){})
chatroom.chatroomService.on('chatroomMemberInfoUpdated', function (member: V2NIMChatroomMember){})
chatroom.chatroomService.on('selfChatBannedUpdated', function (chatBanned: boolean){})
chatroom.chatroomService.on('selfTempChatBannedUpdated', function (tempChatBanned: boolean, tempChatBannedDuration: number){})
chatroom.chatroomService.on('chatroomChatBannedUpdated', function (chatBanned: boolean){})

```

鸿蒙

```
chatroom.chatroomService.on('onChatroomMemberEnter', (member: V2NIMChatroomMember) => {})
chatroom.chatroomService.on('onChatroomMemberExit', (accountId: string) => {})
chatroom.chatroomService.on('onChatroomMemberRoleUpdated', (previousRole: V2NIMChatroomMemberRole, currentMember: V2NIMChatroomMember) => {})
chatroom.chatroomService.on('onChatroomMemberInfoUpdated', (member: V2NIMChatroomMember) => {})
chatroom.chatroomService.on('onSelfChatBannedUpdated', (chatBanned: boolean) => {})
chatroom.chatroomService.on('onSelfTempChatBannedUpdated', (tempChatBanned: boolean, tempChatBannedDuration: number) => {})
chatroom.chatroomService.on('onChatroomChatBannedUpdated', (chatBanned: boolean) => {})

```

Flutter

```
//首先添加监听
await chatroomClient?.getChatroomService().addChatroomListener();
//然后设置监听
chatroomClient!.getChatroomService().onChatroomMemberEnter.listen((event) {
//todo something
});
chatroomClient!.getChatroomService().onChatroomChatBannedUpdated.listen((event) {
//todo something
});
chatroomClient!.getChatroomService().onChatroomMemberExit.listen((event) {
//todo something
});
chatroomClient!.getChatroomService().onChatroomMemberInfoUpdated.listen((event) {
//todo something
});
chatroomClient!.getChatroomService().onChatroomMemberRoleUpdated.listen((event) {
//todo something
});
chatroomClient!.getChatroomService().onSelfChatBannedUpdated.listen((event) {
//todo something
});
chatroomClient!.getChatroomService().onSelfTempChatBannedUpdated.listen((event) {
//todo something
});

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
V2NIMChatroomListener listener = new V2NIMChatroomListener() {
    @Override
    public void onChatroomMemberEnter(V2NIMChatroomMember member) {
    }

    @Override
    public void onChatroomMemberExit(String accountId) {
    }

    @Override
    public void onChatroomMemberRoleUpdated(V2NIMChatroomMemberRole previousRole, V2NIMChatroomMember member) {
    }

    @Override
    public void onChatroomMemberInfoUpdated(V2NIMChatroomMember member) {
    }

    @Override
    public void onSelfChatBannedUpdated(boolean chatBanned) {
    }

    @Override
    public void onSelfTempChatBannedUpdated(boolean tempChatBanned, long tempChatBannedDuration) {
    }

    @Override
    public void onChatroomChatBannedUpdated(boolean chatBanned) {
    }
};

v2ChatroomService.addChatroomListener(listener);

```

```
@interface Listener: NSObject<V2NIMChatroomListener>
- (void)addToService;
@end

@implementation Listener

- (void)addToService
{
    id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];
    [service addChatroomListener:self];
}

- (void)onChatroomMemberEnter:(V2NIMChatroomMember *)member
{
}

- (void)onChatroomMemberExit:(NSString *)accountId
{
}

- (void)onChatroomMemberRoleUpdated:(V2NIMChatroomMemberRole)previousRole
                            member:(V2NIMChatroomMember *)member
{
}
- (void)onChatroomMemberInfoUpdated:(V2NIMChatroomMember *)member
{
}

- (void)onSelfChatBannedUpdated:(BOOL)chatBanned
{
}

- (void)onSelfTempChatBannedUpdated:(BOOL)tempChatBanned
            tempChatBannedDuration:(NSInteger)tempChatBannedDuration
{
}

- (void)onChatroomChatBannedUpdated:(BOOL)chatBanned
{
}

- (void)onMessageRevokedNotification:(NSString *)messageClientId
                        messageTime:(NSTimeInterval)messageTime
{
}

@end

```

```
V2NIMChatroomListener listener;
listener.onChatroomMemberEnter = [](V2NIMChatroomMember member) {
    // handle chatroom member enter
};
listener.onChatroomMemberExit = [](nstd::string accountId) {
    // handle chatroom member exit
};
listener.onChatroomMemberRoleUpdated = [](V2NIMChatroomMemberRole previousRole, V2NIMChatroomMember member) {
    // handle chatroom member role updated
};
listener.onChatroomMemberInfoUpdated = [](V2NIMChatroomMember member) {
    // handle chatroom member info updated
};
listener.onSelfChatBannedUpdated = [](bool chatBanned) {
    // handle self chat banned updated
};
listener.onSelfTempChatBannedUpdated = [](bool tempChatBanned, uint64_t tempChatBannedDuration) {
    // handle self temp chat banned updated
};
listener.onChatroomChatBannedUpdated = [](bool chatBanned) {
    // handle chatroom chat banned updated
};
listener.onMessageRevokedNotification = [](nstd::string messageClientId, uint64_t messageTime) {
    // handle message revoked notification
};
chatroomService.addChatroomListener(listener);

```

```
chatroom.V2NIMChatroomService.on('onChatroomMemberEnter', function (member: V2NIMChatroomMember){})
chatroom.V2NIMChatroomService.on('onChatroomMemberExit', function (accountId: string){})
chatroom.V2NIMChatroomService.on('onChatroomMemberRoleUpdated', function (previousRole: V2NIMChatroomMemberRole, currentMember: V2NIMChatroomMember){})
chatroom.V2NIMChatroomService.on('onChatroomMemberInfoUpdated', function (member: V2NIMChatroomMember){})
chatroom.V2NIMChatroomService.on('onSelfChatBannedUpdated', function (chatBanned: boolean){})
chatroom.V2NIMChatroomService.on('onSelfTempChatBannedUpdated', function (tempChatBanned: boolean, tempChatBannedDuration: number){})
chatroom.V2NIMChatroomService.on('onChatroomChatBannedUpdated', function (chatBanned: boolean){})

```

```
chatroom.chatroomService.on('chatroomMemberEnter', function (member: V2NIMChatroomMember){})
chatroom.chatroomService.on('chatroomMemberExit', function (accountId: string){})
chatroom.chatroomService.on('chatroomMemberRoleUpdated', function (previousRole: V2NIMChatroomMemberRole, currentMember: V2NIMChatroomMember){})
chatroom.chatroomService.on('chatroomMemberInfoUpdated', function (member: V2NIMChatroomMember){})
chatroom.chatroomService.on('selfChatBannedUpdated', function (chatBanned: boolean){})
chatroom.chatroomService.on('selfTempChatBannedUpdated', function (tempChatBanned: boolean, tempChatBannedDuration: number){})
chatroom.chatroomService.on('chatroomChatBannedUpdated', function (chatBanned: boolean){})

```

```
chatroom.chatroomService.on('onChatroomMemberEnter', (member: V2NIMChatroomMember) => {})
chatroom.chatroomService.on('onChatroomMemberExit', (accountId: string) => {})
chatroom.chatroomService.on('onChatroomMemberRoleUpdated', (previousRole: V2NIMChatroomMemberRole, currentMember: V2NIMChatroomMember) => {})
chatroom.chatroomService.on('onChatroomMemberInfoUpdated', (member: V2NIMChatroomMember) => {})
chatroom.chatroomService.on('onSelfChatBannedUpdated', (chatBanned: boolean) => {})
chatroom.chatroomService.on('onSelfTempChatBannedUpdated', (tempChatBanned: boolean, tempChatBannedDuration: number) => {})
chatroom.chatroomService.on('onChatroomChatBannedUpdated', (chatBanned: boolean) => {})

```

```
//首先添加监听
await chatroomClient?.getChatroomService().addChatroomListener();
//然后设置监听
chatroomClient!.getChatroomService().onChatroomMemberEnter.listen((event) {
//todo something
});
chatroomClient!.getChatroomService().onChatroomChatBannedUpdated.listen((event) {
//todo something
});
chatroomClient!.getChatroomService().onChatroomMemberExit.listen((event) {
//todo something
});
chatroomClient!.getChatroomService().onChatroomMemberInfoUpdated.listen((event) {
//todo something
});
chatroomClient!.getChatroomService().onChatroomMemberRoleUpdated.listen((event) {
//todo something
});
chatroomClient!.getChatroomService().onSelfChatBannedUpdated.listen((event) {
//todo something
});
chatroomClient!.getChatroomService().onSelfTempChatBannedUpdated.listen((event) {
//todo something
});

```

## Chat room member management

### **Paging to get chat room members**

通过调用 `getMemberListByOption` 方法分页获取所有聊天室成员信息。支持获取指定多种类型/被加入黑名单/被禁言/在线的聊天室成员：

- **`memberRoles`**: Get the specified type of chat room members
- **`onlyBlocked`For `true`**: Get the blacklisted chat room members
- **`onlyChatBanned`For `true`**: get banned chat room members
- **`onlyOnline`For `true`**: Get the fixed members of the online chat room (creator, administrator, general member)

This method and operation synchronize data from the server, which may take a long time. SDK does not cache data. You need to cache it by yourself as needed.

The sample code is as follows:

安卓

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomMemberQueryOption queryOption = new V2NIMChatroomMemberQueryOption();
// 设置需要查询的成员类型，如果列表为空，表示查询所有类型的成员
List<V2NIMChatroomMemberRole> memberRoles = getMemberRoles();
queryOption.setMemberRoles(memberRoles);
// 是否只返回黑名单成员, 默认 false
queryOption.setOnlyBlocked(false);
// 是否只返回禁言用户, 默认 false
queryOption.setOnlyChatBanned(false);
// 是否只返回在线成员, 默认 false
queryOption.setOnlyOnline(false);
// 设置查询数量
queryOption.setLimit(100);
// 设置分页标识，首页传""，下一页传上次返回的 pageToken
queryOption.setPageToken("");

v2ChatroomService.getMemberListByOption(queryOption, new V2NIMSuccessCallback<V2NIMChatroomMemberListResult>() {
    @Override
    public void onSuccess(V2NIMChatroomMemberListResult v2NIMChatroomMemberListResult) {
        // 查询成功
        List<V2NIMChatroomMember> memberList = v2NIMChatroomMemberListResult.getMemberList();
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 查询失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室服务
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

V2NIMChatroomMemberQueryOption *queryOption = [[V2NIMChatroomMemberQueryOption alloc] init];
// 设置需要查询的成员类型,如果列表为空，表示查询所有类型的成员
queryOption.memberRoles = @[@(V2NIM_CHATROOM_MEMBER_ROLE_NORMAL_GUEST), @(V2NIM_CHATROOM_MEMBER_ROLE_ANONYMOUS_GUEST)];
// 是否只返回黑名单成员, 默认 false
queryOption.onlyBlocked = NO;
// 是否只返回禁言用户, 默认 false
queryOption.onlyChatBanned = NO;
// 是否只返回在线成员, 默认 false
queryOption.onlyOnline = NO;
// 设置查询数量
queryOption.limit = 100;
// 设置分页标识，首页传""，下一页传上次返回的 pageToken
queryOption.pageToken = @"";
[service getMemberListByOption:queryOption
                       success:^(V2NIMChatroomMemberListResult *result)
                       {
                           // 查询成功
                       }
                       failure:^(V2NIMError *error)
                       {
                           // 查询失败
                       }];

```

macOS/Windows

```
V2NIMChatroomMemberQueryOption queryOption;
queryOption.pageToken = 0; // first page
queryOption.limit = 10;
chatroomService.getMemberListByOption(
    queryOption,
    [](V2NIMChatroomMemberListResult result) {
        // get member list succeeded
    },
    [](V2NIMError error) {
        // get member list failed, handle error
    });

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomService.getMemberListByOption({
    // 普通成员
    memberRoles: [V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_NORMAL],
    onlyBlocked: false,
    onlyChatBanned: false,
    onlyOnline: false,
    limit: 100
})

```

Node.js/Electron

```
const result = await chatroomService.getMemberListByOption({
    limit: 10
})
console.log(result)

```

鸿蒙

```
await this.chatroomClient.chatroomService.getMemberListByOption({
    // 普通成员
    memberRoles: [V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_NORMAL],
    onlyBlocked: false,
    onlyChatBanned: false,
    onlyOnline: false,
    limit: 100
})

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
final queryOption = V2NIMChatroomMemberQueryOption();
chatroomService?.getMemberListByOption(queryOption);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomMemberQueryOption queryOption = new V2NIMChatroomMemberQueryOption();
// 设置需要查询的成员类型，如果列表为空，表示查询所有类型的成员
List<V2NIMChatroomMemberRole> memberRoles = getMemberRoles();
queryOption.setMemberRoles(memberRoles);
// 是否只返回黑名单成员, 默认 false
queryOption.setOnlyBlocked(false);
// 是否只返回禁言用户, 默认 false
queryOption.setOnlyChatBanned(false);
// 是否只返回在线成员, 默认 false
queryOption.setOnlyOnline(false);
// 设置查询数量
queryOption.setLimit(100);
// 设置分页标识，首页传""，下一页传上次返回的 pageToken
queryOption.setPageToken("");

v2ChatroomService.getMemberListByOption(queryOption, new V2NIMSuccessCallback<V2NIMChatroomMemberListResult>() {
    @Override
    public void onSuccess(V2NIMChatroomMemberListResult v2NIMChatroomMemberListResult) {
        // 查询成功
        List<V2NIMChatroomMember> memberList = v2NIMChatroomMemberListResult.getMemberList();
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 查询失败
    }
});

```

```
// 通过实例 ID 获取聊天室服务
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

V2NIMChatroomMemberQueryOption *queryOption = [[V2NIMChatroomMemberQueryOption alloc] init];
// 设置需要查询的成员类型,如果列表为空，表示查询所有类型的成员
queryOption.memberRoles = @[@(V2NIM_CHATROOM_MEMBER_ROLE_NORMAL_GUEST), @(V2NIM_CHATROOM_MEMBER_ROLE_ANONYMOUS_GUEST)];
// 是否只返回黑名单成员, 默认 false
queryOption.onlyBlocked = NO;
// 是否只返回禁言用户, 默认 false
queryOption.onlyChatBanned = NO;
// 是否只返回在线成员, 默认 false
queryOption.onlyOnline = NO;
// 设置查询数量
queryOption.limit = 100;
// 设置分页标识，首页传""，下一页传上次返回的 pageToken
queryOption.pageToken = @"";
[service getMemberListByOption:queryOption
                       success:^(V2NIMChatroomMemberListResult *result)
                       {
                           // 查询成功
                       }
                       failure:^(V2NIMError *error)
                       {
                           // 查询失败
                       }];

```

```
V2NIMChatroomMemberQueryOption queryOption;
queryOption.pageToken = 0; // first page
queryOption.limit = 10;
chatroomService.getMemberListByOption(
    queryOption,
    [](V2NIMChatroomMemberListResult result) {
        // get member list succeeded
    },
    [](V2NIMError error) {
        // get member list failed, handle error
    });

```

```
await chatroom.V2NIMChatroomService.getMemberListByOption({
    // 普通成员
    memberRoles: [V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_NORMAL],
    onlyBlocked: false,
    onlyChatBanned: false,
    onlyOnline: false,
    limit: 100
})

```

```
const result = await chatroomService.getMemberListByOption({
    limit: 10
})
console.log(result)

```

```
await this.chatroomClient.chatroomService.getMemberListByOption({
    // 普通成员
    memberRoles: [V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_NORMAL],
    onlyBlocked: false,
    onlyChatBanned: false,
    onlyOnline: false,
    limit: 100
})

```

```
final chatroomService = chatroomClient?.getChatroomService();
final queryOption = V2NIMChatroomMemberQueryOption();
chatroomService?.getMemberListByOption(queryOption);

```

### **Get the designated chat room members in bulk**

通过调用 `getMemberByIds` 方法批量获取指定聊天室成员信息。

- The maximum number of single queries is 200.
- Return the chat room member information and sort it in the order of entering `accountIds`.

The sample code is as follows:

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
// 待查询的账号列表，为空或者 size==0，或者 size>200，返回参数错误
List<String> accountIds = getAccountIds();
v2ChatroomService.getMemberByIds(accountIds, new V2NIMSuccessCallback<List<V2NIMChatroomMember>>() {
    @Override
    public void onSuccess(List<V2NIMChatroomMember> v2NIMChatroomMembers) {
        // 查询成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 查询失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

// 待查询的账号列表，为空或者 size==0，或者 size>200，返回参数错误
NSArray<NSString *> *accountIds = @[@"accountId0", @"accountId1", @"accountId2"];

[service getMemberByIds:accountIds
                success:^(NSArray<V2NIMChatroomMember *> *members) {
                    // 获取成功
                }
                failure:^(V2NIMError *error) {
                    // 获取失败
                }];

```

macOS/Windows

```
chatroomService.getMemberByIds(
    {"accountId1", "accountId2"},
    [](nstd::vector<V2NIMChatroomMember> members) {
        // get members by account ids succeeded
    },
    [](V2NIMError error) {
        // get members by account ids failed, handle error
    });

```

Web/uni-app/小程序

```
getMemberByIds(accountIds: string[]): Promise<V2NIMChatroomMember[]>

```

Node.js/Electron

```
const result = await chatroomService.getMemberByIds(['accountId1', 'accountId2'])
console.log(result)

```

鸿蒙

```
const members = await this.chatroomClient.chatroomService.getMemberByIds(['accid1', 'accid2'])

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
chatroomService?.getMemberByIds(['memberId']);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
// 待查询的账号列表，为空或者 size==0，或者 size>200，返回参数错误
List<String> accountIds = getAccountIds();
v2ChatroomService.getMemberByIds(accountIds, new V2NIMSuccessCallback<List<V2NIMChatroomMember>>() {
    @Override
    public void onSuccess(List<V2NIMChatroomMember> v2NIMChatroomMembers) {
        // 查询成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 查询失败
    }
});

```

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

// 待查询的账号列表，为空或者 size==0，或者 size>200，返回参数错误
NSArray<NSString *> *accountIds = @[@"accountId0", @"accountId1", @"accountId2"];

[service getMemberByIds:accountIds
                success:^(NSArray<V2NIMChatroomMember *> *members) {
                    // 获取成功
                }
                failure:^(V2NIMError *error) {
                    // 获取失败
                }];

```

```
chatroomService.getMemberByIds(
    {"accountId1", "accountId2"},
    [](nstd::vector<V2NIMChatroomMember> members) {
        // get members by account ids succeeded
    },
    [](V2NIMError error) {
        // get members by account ids failed, handle error
    });

```

```
getMemberByIds(accountIds: string[]): Promise<V2NIMChatroomMember[]>

```

```
const result = await chatroomService.getMemberByIds(['accountId1', 'accountId2'])
console.log(result)

```

```
const members = await this.chatroomClient.chatroomService.getMemberByIds(['accid1', 'accid2'])

```

```
final chatroomService = chatroomClient?.getChatroomService();
chatroomService?.getMemberByIds(['memberId']);

```

### **Get chat room members according to tab page**

通过调用 `getMemberListByTag` 方法根据标签分页获取聊天室成员列表。

This method and operation synchronize data from the server, which may take a long time. SDK does not cache data. You need to cache it by yourself as needed.

The sample code is as follows:

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomTagMemberOption option = new V2NIMChatroomTagMemberOption();
// 设置查询 Tag，必传字段，如果不传，会返回参数错误
option.setTag("xxx");
// 设置查询数量
option.setLimit(100);
// 设置分页标识，首页传""，下一页传上次返回的 pageToken
option.setPageToken("");

v2ChatroomService.getMemberListByTag(option, new V2NIMSuccessCallback<V2NIMChatroomMemberListResult>() {
    @Override
    public void onSuccess(V2NIMChatroomMemberListResult v2NIMChatroomMemberListResult) {
        // 查询成功
        String pageToken = v2NIMChatroomMemberListResult.getPageToken();
        List<V2NIMChatroomMember> memberList = v2NIMChatroomMemberListResult.getMemberList();
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 查询失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

V2NIMChatroomTagMemberOption *option = [[V2NIMChatroomTagMemberOption alloc] init];
// 设置查询 Tag，必传字段，如果不传，会返回参数错误
option.tag = @"xxx";
// 设置查询数量
option.limit = 100;
// 设置分页标识，首页传""，下一页传上次返回的 pageToken
option.pageToken = @"";

[service getMemberListByTag:option
                    success:^(V2NIMChatroomMemberListResult *result)
                    {
                        // 获取成功
                    }
                    failure:^(V2NIMError *error)
                    {
                        // 获取失败
                    }];

```

macOS/Windows

```
V2NIMChatroomTagMemberOption option;
option.tag = "tag1";
option.pageToken = "";
option.limit = 10;
chatroomService.getMemberListByTag(
    option,
    [](V2NIMChatroomMemberListResult result) {
        // get member list by tag succeeded
    },
    [](V2NIMError error) {
        // get member list by tag failed, handle error
    });

```

Web/uni-app/小程序

```
const result = await chatroom.V2NIMChatroom.getMemberListByTag({
    tag: 'abc', // 查询的 tag
    limit: 100
})

```

Node.js/Electron

```
const result = await chatroomService.getMemberListByTag({
    tag: 'tag',
    limit: 10
})

```

鸿蒙

```
const result: V2NIMChatroomMemberListResult = await this.chatroomClient.chatroomService.getMemberListByTag({
    tag: 'abc', // 查询的 tag
    limit: 100
})

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
var option = V2NIMChatroomTagMemberOption();
var result = await chatroomService?.getMemberListByTag(option);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomTagMemberOption option = new V2NIMChatroomTagMemberOption();
// 设置查询 Tag，必传字段，如果不传，会返回参数错误
option.setTag("xxx");
// 设置查询数量
option.setLimit(100);
// 设置分页标识，首页传""，下一页传上次返回的 pageToken
option.setPageToken("");

v2ChatroomService.getMemberListByTag(option, new V2NIMSuccessCallback<V2NIMChatroomMemberListResult>() {
    @Override
    public void onSuccess(V2NIMChatroomMemberListResult v2NIMChatroomMemberListResult) {
        // 查询成功
        String pageToken = v2NIMChatroomMemberListResult.getPageToken();
        List<V2NIMChatroomMember> memberList = v2NIMChatroomMemberListResult.getMemberList();
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 查询失败
    }
});

```

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

V2NIMChatroomTagMemberOption *option = [[V2NIMChatroomTagMemberOption alloc] init];
// 设置查询 Tag，必传字段，如果不传，会返回参数错误
option.tag = @"xxx";
// 设置查询数量
option.limit = 100;
// 设置分页标识，首页传""，下一页传上次返回的 pageToken
option.pageToken = @"";

[service getMemberListByTag:option
                    success:^(V2NIMChatroomMemberListResult *result)
                    {
                        // 获取成功
                    }
                    failure:^(V2NIMError *error)
                    {
                        // 获取失败
                    }];

```

```
V2NIMChatroomTagMemberOption option;
option.tag = "tag1";
option.pageToken = "";
option.limit = 10;
chatroomService.getMemberListByTag(
    option,
    [](V2NIMChatroomMemberListResult result) {
        // get member list by tag succeeded
    },
    [](V2NIMError error) {
        // get member list by tag failed, handle error
    });

```

```
const result = await chatroom.V2NIMChatroom.getMemberListByTag({
    tag: 'abc', // 查询的 tag
    limit: 100
})

```

```
const result = await chatroomService.getMemberListByTag({
    tag: 'tag',
    limit: 10
})

```

```
const result: V2NIMChatroomMemberListResult = await this.chatroomClient.chatroomService.getMemberListByTag({
    tag: 'abc', // 查询的 tag
    limit: 100
})

```

```
final chatroomService = chatroomClient?.getChatroomService();
var option = V2NIMChatroomTagMemberOption();
var result = await chatroomService?.getMemberListByTag(option);

```

### **Get the number of chat room members according to the tag**

通过调用 `getMemberCountByTag` 方法获取指定标签下的聊天室成员人数。

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
// 查询的 tag，必传字段，传 null 或者 ""，会返回参数错误
String tag = "xxx";
v2ChatroomService.getMemberCountByTag(tag, new V2NIMSuccessCallback<Long>() {
    @Override
    public void onSuccess(Long count) {
        // 查询成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 查询失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

// 查询的 tag，必传字段，传 null 或者 ""，会返回参数错误
NSString *tag = @"xxx";
[service getMemberCountByTag:tag
                    success:^(NSInteger memberCount)
                    {
                        // 获取成功
                    }
                    failure:^(V2NIMError *error)
                    {
                        // 获取失败
                    }];

```

macOS/Windows

```
chatroomService.getMemberCountByTag(
    "tag1",
    [](uint64_t count) {
        // get member count by tag succeeded
    },
    [](V2NIMError error) {
        // get member count by tag failed, handle error
    });

```

Web/uni-app/小程序

```
const count = await chatroom.V2NIMChatroomService.getMemberCountByTag('tagName')

```

Node.js/Electron

```
const count = await chatroomService.getMemberCountByTag('tag')

```

鸿蒙

```
const count = await this.chatroomClient.chatroomService.getMemberCountByTag('tagName')

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
var result = await chatroomService?.getMemberCountByTag('tag');

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
// 查询的 tag，必传字段，传 null 或者 ""，会返回参数错误
String tag = "xxx";
v2ChatroomService.getMemberCountByTag(tag, new V2NIMSuccessCallback<Long>() {
    @Override
    public void onSuccess(Long count) {
        // 查询成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 查询失败
    }
});

```

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

// 查询的 tag，必传字段，传 null 或者 ""，会返回参数错误
NSString *tag = @"xxx";
[service getMemberCountByTag:tag
                    success:^(NSInteger memberCount)
                    {
                        // 获取成功
                    }
                    failure:^(V2NIMError *error)
                    {
                        // 获取失败
                    }];

```

```
chatroomService.getMemberCountByTag(
    "tag1",
    [](uint64_t count) {
        // get member count by tag succeeded
    },
    [](V2NIMError error) {
        // get member count by tag failed, handle error
    });

```

```
const count = await chatroom.V2NIMChatroomService.getMemberCountByTag('tagName')

```

```
const count = await chatroomService.getMemberCountByTag('tag')

```

```
const count = await this.chatroomClient.chatroomService.getMemberCountByTag('tagName')

```

```
final chatroomService = chatroomClient?.getChatroomService();
var result = await chatroomService?.getMemberCountByTag('tag');

```

### Modify the chat room members' own information

通过调用 `updateSelfMemberInfo` 方法更新本人的聊天室成员信息。目前支持更新昵称、头像、服务端扩展字段及反垃圾配置项。

Support setting whether to notify after the update. If notification is set, chat room members will receive a notification message of the type `MEMBER_INFO_UPDATED`.

After the update is successful, chat room members will receive a callback from `onChatroomMemberInfoUpdated`.

The sample code is as follows:

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomSelfMemberUpdateParams updateParams = new V2NIMChatroomSelfMemberUpdateParams();
// 设置聊天室中显示的昵称
updateParams.setRoomNick("xxx");
// 设置头像
updateParams.setRoomAvatar("xxx");
// 设置成员扩展字段
updateParams.setServerExtension("xxx");
// 以上三个字段至少需要设置一个，否则会返回参数错误

// 设置是否需要通知
updateParams.setNotificationEnabled(true);
// 设置本次操作生成的通知中的扩展字段
updateParams.setNotificationExtension("xxx");
// 设置更新信息持久化，只针对固定成员身份生效
updateParams.setPersistence(true);

// 反垃圾配置，可不传
V2NIMAntispamConfig antispamConfig = new V2NIMAntispamConfig();

v2ChatroomService.updateSelfMemberInfo(updateParams, antispamConfig, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // 更新成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 更新失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

V2NIMChatroomSelfMemberUpdateParams *updateParams = [[V2NIMChatroomSelfMemberUpdateParams alloc] init];
// 设置聊天室中显示的昵称
updateParams.roomNick = @"xxx";
// 设置头像
updateParams.roomAvatar = @"xxx";
// 设置成员扩展字段
updateParams.serverExtension = @"xxx";
// 以上三个字段至少需要设置一个，否则会返回参数错误

// 设置是否需要通知
updateParams.notificationEnabled = YES;
// 设置本次操作生成的通知中的扩展字段
updateParams.notificationExtension = @"xxx";
// 设置更新信息持久化，只针对固定成员身份生效
updateParams.persistence = YES;

// 反垃圾配置，可不传
V2NIMAntispamConfig *antispamConfig = [[V2NIMAntispamConfig alloc] init];
[service updateSelfMemberInfo:updateParams
               antispamConfig:antispamConfig
                      success:^()
                      {
                          // 更新成功
                      }
                      failure:^(V2NIMError *error)
                      {
                          // 更新失败
                      }];

```

macOS/Windows

```
V2NIMChatroomSelfMemberUpdateParams updateParams;
updateParams.roomNick = "roomNick";
V2NIMAntispamConfig antispamConfig;
chatroomService.updateSelfMemberInfo(
    updateParams,
    antispamConfig,
    []() {
        // update self member info succeeded
    },
    [](V2NIMError error) {
        // update self member info failed, handle error
    });

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomService.updateSelfMemberInfo({
    roomNick: 'nick', // 聊天室昵称
    roomAvatar: 'https://www.xxx.com/1.jpg', // 头像
    serverExtension: 'extension', // 扩展字段
    notificationEnabled: true, // 是否通知
    persistence: true // 是否持久化。若为否，则信息在下线后失效
})

```

Node.js/Electron

```
await chatroomService.updateSelfMemberInfo({
    roomNick: 'new room nickname'
}, {})

```

鸿蒙

```
await this.chatroomClient.chatroomService.updateSelfMemberInfo({
    roomNick: 'nick', // 聊天室昵称
    roomAvatar: 'https://www.xxx.com/1.jpg', // 头像
    serverExtension: 'extension', //扩展字段
    notificationEnabled: true, //是否通知
    persistence: true //是否持久化。若为否，则信息在下线后失效
})

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
final params = V2NIMChatroomSelfMemberUpdateParams();
var result = await chatroomService?.updateSelfMemberInfo(params,null);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomSelfMemberUpdateParams updateParams = new V2NIMChatroomSelfMemberUpdateParams();
// 设置聊天室中显示的昵称
updateParams.setRoomNick("xxx");
// 设置头像
updateParams.setRoomAvatar("xxx");
// 设置成员扩展字段
updateParams.setServerExtension("xxx");
// 以上三个字段至少需要设置一个，否则会返回参数错误

// 设置是否需要通知
updateParams.setNotificationEnabled(true);
// 设置本次操作生成的通知中的扩展字段
updateParams.setNotificationExtension("xxx");
// 设置更新信息持久化，只针对固定成员身份生效
updateParams.setPersistence(true);

// 反垃圾配置，可不传
V2NIMAntispamConfig antispamConfig = new V2NIMAntispamConfig();

v2ChatroomService.updateSelfMemberInfo(updateParams, antispamConfig, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // 更新成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 更新失败
    }
});

```

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

V2NIMChatroomSelfMemberUpdateParams *updateParams = [[V2NIMChatroomSelfMemberUpdateParams alloc] init];
// 设置聊天室中显示的昵称
updateParams.roomNick = @"xxx";
// 设置头像
updateParams.roomAvatar = @"xxx";
// 设置成员扩展字段
updateParams.serverExtension = @"xxx";
// 以上三个字段至少需要设置一个，否则会返回参数错误

// 设置是否需要通知
updateParams.notificationEnabled = YES;
// 设置本次操作生成的通知中的扩展字段
updateParams.notificationExtension = @"xxx";
// 设置更新信息持久化，只针对固定成员身份生效
updateParams.persistence = YES;

// 反垃圾配置，可不传
V2NIMAntispamConfig *antispamConfig = [[V2NIMAntispamConfig alloc] init];
[service updateSelfMemberInfo:updateParams
               antispamConfig:antispamConfig
                      success:^()
                      {
                          // 更新成功
                      }
                      failure:^(V2NIMError *error)
                      {
                          // 更新失败
                      }];

```

```
V2NIMChatroomSelfMemberUpdateParams updateParams;
updateParams.roomNick = "roomNick";
V2NIMAntispamConfig antispamConfig;
chatroomService.updateSelfMemberInfo(
    updateParams,
    antispamConfig,
    []() {
        // update self member info succeeded
    },
    [](V2NIMError error) {
        // update self member info failed, handle error
    });

```

```
await chatroom.V2NIMChatroomService.updateSelfMemberInfo({
    roomNick: 'nick', // 聊天室昵称
    roomAvatar: 'https://www.xxx.com/1.jpg', // 头像
    serverExtension: 'extension', // 扩展字段
    notificationEnabled: true, // 是否通知
    persistence: true // 是否持久化。若为否，则信息在下线后失效
})

```

```
await chatroomService.updateSelfMemberInfo({
    roomNick: 'new room nickname'
}, {})

```

```
await this.chatroomClient.chatroomService.updateSelfMemberInfo({
    roomNick: 'nick', // 聊天室昵称
    roomAvatar: 'https://www.xxx.com/1.jpg', // 头像
    serverExtension: 'extension', //扩展字段
    notificationEnabled: true, //是否通知
    persistence: true //是否持久化。若为否，则信息在下线后失效
})

```

```
final chatroomService = chatroomClient?.getChatroomService();
final params = V2NIMChatroomSelfMemberUpdateParams();
var result = await chatroomService?.updateSelfMemberInfo(params,null);

```

### Update the role of chat room member

通过调用 `updateMemberRole` 方法更新聊天室成员角色，包括以下场景：

- Set ordinary visitors and ordinary members as administrators or vice versa. This operation is only authorized by **the creator of the**chat room.
- Set ordinary members as ordinary visitors or vice versa. This operation is only authorized by the chat room **administrator and creator**.
- It is not allowed to operate fictitious users and anonymous tourists.

更新成功后，聊天室内所有成员收到 `onChatroomMemberRoleUpdated` 回调，及通类型为 `ROLE_UPDATE` 的通知消息。

The sample code is as follows:

安卓

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
// 被操作的账号 ID
String accountId = "test";
V2NIMChatroomMemberRoleUpdateParams updateParams = new V2NIMChatroomMemberRoleUpdateParams();
// 设置成员角色，必传字段，如果不传，会返回参数错误
// 不支持设置为
// V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_CREATOR
// V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_ANONYMOUS_GUEST
// V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_VIRTUAL
updateParams.setMemberRole(V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_MANAGER);
// 设置成员等级,可不传
updateParams.setMemberLevel(1);
// 设置通知扩展字段，可不传
updateParams.setNotificationExtension("xxx");

v2ChatroomService.updateMemberRole(accountId, updateParams, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        //更新成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //更新失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:instanceId] getChatroomService];
// 被操作的账号 ID
NSString *accountId = @"test";
V2NIMChatroomMemberRoleUpdateParams *updateParams = [[V2NIMChatroomMemberRoleUpdateParams alloc] init];
// 设置成员角色，必传字段，如果不传，会返回参数错误
// 不支持设置为
// V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_CREATOR
// V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_ANONYMOUS_GUEST
// V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_VIRTUAL
updateParams.memberRole = V2NIM_CHATROOM_MEMBER_ROLE_MANAGER;
// 设置成员等级,可不传
updateParams.memberLevel = 1;
// 设置通知扩展字段，可不传
updateParams.notificationExtension = @"xxx";
[service updateMemberRole:accountId
             updateParams:updateParams
                 success:^()
                 {
                     // 更新成功
                 }
                 failure:^(V2NIMError *error)
                 {
                     // 更新失败
                 }];

```

macOS/Windows

```
V2NIMChatroomMemberRoleUpdateParams updateParams;
updateParams.memberRole = V2NIM_CHATROOM_MEMBER_ROLE_MANAGER;
chatroomService.updateMemberRole(
    "accountId",
    updateParams,
    []() {
        // update member role succeeded
    },
    [](V2NIMError error) {
        // update member role failed, handle error
    });

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomService.updateMemberRole('accid', {
    // 设置为管理员
    memberRole: V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_MANAGER,
    // 用户登记
    memberLevel: 10,
    // 通知的扩展字段
    notificationExtension: 'ps'
})

```

Node.js/Electron

```
await chatroomService.updateMemberRole('accountId', {
    memberRole: 2
})

```

鸿蒙

```
await this.chatroomClient.chatroomService.updateMemberRole('accid', {
    // 设置为管理员
    memberRole: V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_MANAGER,
    // 用户登记
    memberLevel: 10,
    // 通知的扩展字段
    notificationExtension: 'ps'
})

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
final params = V2NIMChatroomMemberRoleUpdateParams();
var result = await chatroomService?.updateMemberRole('account_id', params);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
// 被操作的账号 ID
String accountId = "test";
V2NIMChatroomMemberRoleUpdateParams updateParams = new V2NIMChatroomMemberRoleUpdateParams();
// 设置成员角色，必传字段，如果不传，会返回参数错误
// 不支持设置为
// V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_CREATOR
// V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_ANONYMOUS_GUEST
// V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_VIRTUAL
updateParams.setMemberRole(V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_MANAGER);
// 设置成员等级,可不传
updateParams.setMemberLevel(1);
// 设置通知扩展字段，可不传
updateParams.setNotificationExtension("xxx");

v2ChatroomService.updateMemberRole(accountId, updateParams, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        //更新成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //更新失败
    }
});

```

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:instanceId] getChatroomService];
// 被操作的账号 ID
NSString *accountId = @"test";
V2NIMChatroomMemberRoleUpdateParams *updateParams = [[V2NIMChatroomMemberRoleUpdateParams alloc] init];
// 设置成员角色，必传字段，如果不传，会返回参数错误
// 不支持设置为
// V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_CREATOR
// V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_ANONYMOUS_GUEST
// V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_VIRTUAL
updateParams.memberRole = V2NIM_CHATROOM_MEMBER_ROLE_MANAGER;
// 设置成员等级,可不传
updateParams.memberLevel = 1;
// 设置通知扩展字段，可不传
updateParams.notificationExtension = @"xxx";
[service updateMemberRole:accountId
             updateParams:updateParams
                 success:^()
                 {
                     // 更新成功
                 }
                 failure:^(V2NIMError *error)
                 {
                     // 更新失败
                 }];

```

```
V2NIMChatroomMemberRoleUpdateParams updateParams;
updateParams.memberRole = V2NIM_CHATROOM_MEMBER_ROLE_MANAGER;
chatroomService.updateMemberRole(
    "accountId",
    updateParams,
    []() {
        // update member role succeeded
    },
    [](V2NIMError error) {
        // update member role failed, handle error
    });

```

```
await chatroom.V2NIMChatroomService.updateMemberRole('accid', {
    // 设置为管理员
    memberRole: V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_MANAGER,
    // 用户登记
    memberLevel: 10,
    // 通知的扩展字段
    notificationExtension: 'ps'
})

```

```
await chatroomService.updateMemberRole('accountId', {
    memberRole: 2
})

```

```
await this.chatroomClient.chatroomService.updateMemberRole('accid', {
    // 设置为管理员
    memberRole: V2NIMChatroomMemberRole.V2NIM_CHATROOM_MEMBER_ROLE_MANAGER,
    // 用户登记
    memberLevel: 10,
    // 通知的扩展字段
    notificationExtension: 'ps'
})

```

```
final chatroomService = chatroomClient?.getChatroomService();
final params = V2NIMChatroomMemberRoleUpdateParams();
var result = await chatroomService?.updateMemberRole('account_id', params);

```

### Set the blacklist status of chat room members

通过调用 `setMemberBlockedStatus` 方法来设置聊天室成员黑名单状态，支持将成员加入黑名单或从黑名单中移除。

After joining the blacklist:

- 聊天室所有成员收到成员信息变更回调 `onChatroomMemberInfoUpdated` 和类型为 `MEMBER_BLOCK_ADDED` 的通知消息。
- 聊天室黑名单用户收到被踢出聊天室回调 `onChatroomKicked`，被踢原因为拉黑，同时与聊天室服务器断开连接，无法收发聊天室消息。

After removing the blacklist:

- 聊天室所有成员收到成员信息变更回调 `onChatroomMemberInfoUpdated` 和类型为 `MEMBER_BLOCK_REMOVED` 的通知消息。
- From an ordinary member of the chat room to an ordinary tourist.

- Only the creator and administrator of the chat room have permission for this operation.
- If the operator is the administrator, only the creator of the chat room has the permission.
- It is not allowed to operate fictitious users and anonymous tourists.

The sample code is as follows:

安卓

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

// 被操作的账号 ID
String accountId = "test";
// 设置黑名单状态
boolean blocked = true;
// 设置通知扩展字段，可不传
String notificationExtension = "xxx";

v2ChatroomService.setMemberBlockedStatus(accountId, blocked, notificationExtension, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // 设置成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 设置失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];
// 被操作的账号 ID
NSString *accountId = @"test";
// 设置黑名单状态
BOOL blocked = YES;
// 设置通知扩展字段，可不传
NSString *notificationExtension = @"xxx";
[service setMemberBlockedStatus:accountId
                        blocked:blocked
          notificationExtension:notificationExtension
                        success:^()
                        {
                            // 设置成功
                        }
                        failure:^(V2NIMError *error)
                        {
                            // 设置失败
                        }];

```

macOS/Windows

```
chatroomService.setMemberBlockedStatus(
    "accountId",
    true,
    "notificationExtension",
    []() {
        // set member blocked status succeeded
    },
    [](V2NIMError error) {
        // set member blocked status failed, handle error
    });

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomService.setMemberBlockedStatus('accountId', true, 'extension')

```

Node.js/Electron

```
await chatroomService.setMemberBlockedStatus('accountId', true, 'your notification extension')

```

鸿蒙

```
await this.chatroomClient.chatroomService.setMemberBlockedStatus('accid', true, 'extension')

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
var result = await chatroomService?.setMemberBlockedStatus('account_id',true);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

// 被操作的账号 ID
String accountId = "test";
// 设置黑名单状态
boolean blocked = true;
// 设置通知扩展字段，可不传
String notificationExtension = "xxx";

v2ChatroomService.setMemberBlockedStatus(accountId, blocked, notificationExtension, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // 设置成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 设置失败
    }
});

```

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];
// 被操作的账号 ID
NSString *accountId = @"test";
// 设置黑名单状态
BOOL blocked = YES;
// 设置通知扩展字段，可不传
NSString *notificationExtension = @"xxx";
[service setMemberBlockedStatus:accountId
                        blocked:blocked
          notificationExtension:notificationExtension
                        success:^()
                        {
                            // 设置成功
                        }
                        failure:^(V2NIMError *error)
                        {
                            // 设置失败
                        }];

```

```
chatroomService.setMemberBlockedStatus(
    "accountId",
    true,
    "notificationExtension",
    []() {
        // set member blocked status succeeded
    },
    [](V2NIMError error) {
        // set member blocked status failed, handle error
    });

```

```
await chatroom.V2NIMChatroomService.setMemberBlockedStatus('accountId', true, 'extension')

```

```
await chatroomService.setMemberBlockedStatus('accountId', true, 'your notification extension')

```

```
await this.chatroomClient.chatroomService.setMemberBlockedStatus('accid', true, 'extension')

```

```
final chatroomService = chatroomClient?.getChatroomService();
var result = await chatroomService?.setMemberBlockedStatus('account_id',true);

```

### Set the chat room member ban status

通过调用 `setMemberChatBannedStatus` 方法来设置聊天室成员禁言状态，支持将成员永久禁言或解除永久禁言。

After being permanently banned:

- 聊天室所有成员收到成员信息变更回调 `onChatroomMemberInfoUpdated` 和类型为 `MEMBER_CHAT_BANNED_ADDED` 的通知消息。
- Chat room ban users receive the ban status change callback `onSelfChatBannedUpdated`. They cannot send messages in the chat room, but can receive messages.

After lifting the permanent ban:

- 聊天室所有成员收到成员信息变更回调 `onChatroomMemberInfoUpdated` 和类型为 `MEMBER_CHAT_BANNED_REMOVED` 的通知消息。
- The chat room ban user receives the ban status change callback `onSelfChatBannedUpdated`, and restores the permission to send messages in the chat room.

- Only the creator and administrator of the chat room have permission for this operation.
- If the operator is the administrator, only the creator of the chat room has the permission.
- It is not allowed to operate fictitious users and anonymous tourists.

The sample code is as follows:

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

// 被操作的账号 ID
String accountId = "test";
// 设置禁言状态
boolean chatBanned = true;
// 设置通知扩展字段，可不传
String notificationExtension = "xxx";

v2ChatroomService.setMemberChatBannedStatus(accountId, chatBanned, notificationExtension, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // 设置成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 设置失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];
// 被操作的账号 ID
NSString *accountId = @"test";
//设置禁言状态
BOOL chatBanned = YES;
// 设置通知扩展字段，可不传
NSString *notificationExtension = @"xxx";
[service setMemberChatBannedStatus:accountId
                        chatBanned:chatBanned
          notificationExtension:notificationExtension
                        success:^()
                        {
                            // 设置成功
                        }
                        failure:^(V2NIMError *error)
                        {
                            // 设置失败
                        }];

```

macOS/Windows

```
chatroomService.setMemberChatBannedStatus(
    "accountId",
    true,
    "notificationExtension",
    []() {
        // set member chat banned status succeeded
    },
    [](V2NIMError error) {
        // set member chat banned status failed, handle error
    });

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomService.setMemberChatBannedStatus('accid', true, 'extension')

```

Node.js/Electron

```
await chatroomService.setMemberChatBannedStatus('accountId', true, 'your notification extension')

```

鸿蒙

```
await this.chatroomClient.chatroomService.setMemberChatBannedStatus('accid', true, 'extension')

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
var result = await chatroomService?.setMemberChatBannedStatus('account_id',true);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

// 被操作的账号 ID
String accountId = "test";
// 设置禁言状态
boolean chatBanned = true;
// 设置通知扩展字段，可不传
String notificationExtension = "xxx";

v2ChatroomService.setMemberChatBannedStatus(accountId, chatBanned, notificationExtension, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // 设置成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 设置失败
    }
});

```

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];
// 被操作的账号 ID
NSString *accountId = @"test";
//设置禁言状态
BOOL chatBanned = YES;
// 设置通知扩展字段，可不传
NSString *notificationExtension = @"xxx";
[service setMemberChatBannedStatus:accountId
                        chatBanned:chatBanned
          notificationExtension:notificationExtension
                        success:^()
                        {
                            // 设置成功
                        }
                        failure:^(V2NIMError *error)
                        {
                            // 设置失败
                        }];

```

```
chatroomService.setMemberChatBannedStatus(
    "accountId",
    true,
    "notificationExtension",
    []() {
        // set member chat banned status succeeded
    },
    [](V2NIMError error) {
        // set member chat banned status failed, handle error
    });

```

```
await chatroom.V2NIMChatroomService.setMemberChatBannedStatus('accid', true, 'extension')

```

```
await chatroomService.setMemberChatBannedStatus('accountId', true, 'your notification extension')

```

```
await this.chatroomClient.chatroomService.setMemberChatBannedStatus('accid', true, 'extension')

```

```
final chatroomService = chatroomClient?.getChatroomService();
var result = await chatroomService?.setMemberChatBannedStatus('account_id',true);

```

### Set the temporary ban status of chat room members

通过调用 `setMemberTempChatBanned` 方法来设置聊天室成员临时禁言状态，支持将成员临时禁言或解除临时禁言。

After being temporarily banned (the duration of temporary ban is not 0):

- 聊天室所有成员收到成员信息变更回调 `onChatroomMemberInfoUpdated` 和类型为 `MEMBER_TEMP_CHAT_BANNED_ADDED` 的通知消息。
- Chat room temporary ban users receive a temporary ban status change callback `onSelfTempChatBannedUpdated`. During the temporary ban, they cannot send messages in the chat room, but can receive messages. After the temporary ban is over, the permission to send messages is restored.

After lifting the permanent ban (the duration of the temporary ban is 0):

- 聊天室所有成员收到成员信息变更回调 `onChatroomMemberInfoUpdated` 和类型为 `MEMBER_TEMP_CHAT_BANNED_REMOVED` 的通知消息。
- The chat room temporary ban user receives the ban status change callback `onSelfTempChatBannedUpdated`, and restores the permission to send messages in the chat room.

- Only the creator and administrator of the chat room have permission for this operation.
- If the operator is the administrator, only the creator of the chat room has the permission.
- It is not allowed to operate fictitious users and anonymous tourists.

The sample code is as follows:

安卓

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

// 被操作的账号 ID
String accountId = "test";
// 设置临时禁言时长，单位：秒，单次最大：30 天，取消则设置为：0
long tempChatBannedDuration = 1000L;
// 是否需要发送广播通知，true：通知，false：不通知
boolean notificationEnabled = true;
// 设置通知扩展字段，可不传
String notificationExtension = "xxx";

v2ChatroomService.setMemberTempChatBanned(accountId, tempChatBannedDuration, notificationEnabled, notificationExtension, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // 设置成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 设置失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];
// 被操作的账号 ID
NSString *accountId = @"test";
// 设置临时禁言时长，单位：秒，单次最大：30 天，取消则设置为：0
long tempChatBannedDuration = 1000L;
// 是否需要发送广播通知，true：通知，false：不通知
BOOL notificationEnabled = YES;
// 设置通知扩展字段，可不传
NSString *notificationExtension = @"xxx";
[service setMemberTempChatBanned:accountId
          tempChatBannedDuration:tempChatBannedDuration
             notificationEnabled:notificationEnabled
           notificationExtension:notificationExtension
                         success:^()
                         {
                             // 设置成功
                         }
                         failure:^(V2NIMError *error)
                         {
                             // 设置失败
                         }];

```

macOS/Windows

```
chatroomService.setMemberTempChatBanned(
    "accountId",
    60,
    true,
    "notificationExtension",
    []() {
        // set member temp chat banned succeeded
    },
    [](V2NIMError error) {
        // set member temp chat banned failed, handle error
    });

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomService.setMemberTempChatBanned('accid', 60 * 1000, true, 'extension')

```

Node.js/Electron

```
await chatroomService.setMemberTempChatBanned('accountId', 10, true, 'your notification extension')

```

鸿蒙

```
await this.chatroomClient.chatroomService.setMemberTempChatBanned('accid', 60 * 1000, true, 'extension')

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
var result = await chatroomService?.setMemberTempChatBanned('account_id',60,true);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

// 被操作的账号 ID
String accountId = "test";
// 设置临时禁言时长，单位：秒，单次最大：30 天，取消则设置为：0
long tempChatBannedDuration = 1000L;
// 是否需要发送广播通知，true：通知，false：不通知
boolean notificationEnabled = true;
// 设置通知扩展字段，可不传
String notificationExtension = "xxx";

v2ChatroomService.setMemberTempChatBanned(accountId, tempChatBannedDuration, notificationEnabled, notificationExtension, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // 设置成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 设置失败
    }
});

```

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];
// 被操作的账号 ID
NSString *accountId = @"test";
// 设置临时禁言时长，单位：秒，单次最大：30 天，取消则设置为：0
long tempChatBannedDuration = 1000L;
// 是否需要发送广播通知，true：通知，false：不通知
BOOL notificationEnabled = YES;
// 设置通知扩展字段，可不传
NSString *notificationExtension = @"xxx";
[service setMemberTempChatBanned:accountId
          tempChatBannedDuration:tempChatBannedDuration
             notificationEnabled:notificationEnabled
           notificationExtension:notificationExtension
                         success:^()
                         {
                             // 设置成功
                         }
                         failure:^(V2NIMError *error)
                         {
                             // 设置失败
                         }];

```

```
chatroomService.setMemberTempChatBanned(
    "accountId",
    60,
    true,
    "notificationExtension",
    []() {
        // set member temp chat banned succeeded
    },
    [](V2NIMError error) {
        // set member temp chat banned failed, handle error
    });

```

```
await chatroom.V2NIMChatroomService.setMemberTempChatBanned('accid', 60 * 1000, true, 'extension')

```

```
await chatroomService.setMemberTempChatBanned('accountId', 10, true, 'your notification extension')

```

```
await this.chatroomClient.chatroomService.setMemberTempChatBanned('accid', 60 * 1000, true, 'extension')

```

```
final chatroomService = chatroomClient?.getChatroomService();
var result = await chatroomService?.setMemberTempChatBanned('account_id',60,true);

```

### Kick out the chat room members

通过调用 `kickMember` 方法将指定成员踢出聊天室。踢出的同时退出聊天室。

After the successful kickout:

- Kicked users receive kicked out of the chat room callback `onChatroomKicked`, and exit the chat room callback `onChatroomExited`.
- 聊天室内所有成员收到成员退出聊天室 `onChatroomMemberExit` 回调，及类型为 `MEMBER_KICKED` 的通知消息。

- Only the creator and administrator of the chat room have permission for this operation.
- If the operator is the administrator, only the creator of the chat room has the permission.
- It is not allowed to operate fictitious users and anonymous tourists.

The sample code is as follows:

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

// 被踢的成员 ID
String accountId = "test";
// 设置通知扩展字段，可不传
String notificationExtension = "xxx";

v2ChatroomService.kickMember(accountId, notificationExtension, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // 踢出成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 踢出失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

// 被踢的成员 ID
NSString *accountId = @"accountId";
// 设置通知扩展字段，可不传
NSString *notificationExtension = @"xxx";
[service kickMember:accountId
notificationExtension:notificationExtension
            success:^() {
                // 踢出成功
            }
            failure:^(V2NIMError *error) {
                // 踢出失败
            }];

```

macOS/Windows

```
chatroomService.kickMember(
    "accountId",
    "notificationExtension",
    []() {
        // kick member succeeded
    },
    [](V2NIMError error) {
        // kick member failed, handle error
    });

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomService.kickMember('account', 'notificationExtension')

```

Node.js/Electron

```
await chatroomService.kickMember('accountId', 'your notification extension')

```

鸿蒙

```
await this.chatroomClient.chatroomService.kickMember('account', 'notificationExtension')

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
chatroomService?.kickMember('accountId');

```

:::

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

// 被踢的成员 ID
String accountId = "test";
// 设置通知扩展字段，可不传
String notificationExtension = "xxx";

v2ChatroomService.kickMember(accountId, notificationExtension, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // 踢出成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 踢出失败
    }
});

```

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

// 被踢的成员 ID
NSString *accountId = @"accountId";
// 设置通知扩展字段，可不传
NSString *notificationExtension = @"xxx";
[service kickMember:accountId
notificationExtension:notificationExtension
            success:^() {
                // 踢出成功
            }
            failure:^(V2NIMError *error) {
                // 踢出失败
            }];

```

```
chatroomService.kickMember(
    "accountId",
    "notificationExtension",
    []() {
        // kick member succeeded
    },
    [](V2NIMError error) {
        // kick member failed, handle error
    });

```

```
await chatroom.V2NIMChatroomService.kickMember('account', 'notificationExtension')

```

```
await chatroomService.kickMember('accountId', 'your notification extension')

```

```
await this.chatroomClient.chatroomService.kickMember('account', 'notificationExtension')

```

```
final chatroomService = chatroomClient?.getChatroomService();
chatroomService?.kickMember('accountId');

```

## Relevant information

- [Chat room-related API](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#%E8%81%8A%E5%A4%A9%E5%AE%A4%E6%88%90%E5%91%98)
- [Chat room member-related error code](https://doc.yunxin.163.com/messaging2/client-apis/DUxNjU3MzU?platform=client#%E8%81%8A%E5%A4%A9%E5%AE%A4%E6%88%90%E5%91%98%E9%94%99%E8%AF%AF)

## Related interfaces

安卓/iOS/macOS/Windows

| API                                                                                                                                               | 说明                           |
| ------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------ |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)       | 获取聊天室服务类               |
| [`addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#addChatroomListener)                         | 注册聊天室监听器               |
| [`removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#removeChatroomListener)                   | 取消注册聊天室监听器           |
| [`V2NIMChatroomMemberRole`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMemberRole)                 | 聊天室成员角色                 |
| [`V2NIMChatroomEnterParams.anonymousMode`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams) | 聊天室登录参数                 |
| [`V2NIMChatroomMember`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMember)                         | 聊天室成员                     |
| [`getMemberListByOption`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberListByOption)                     | 分页获取所有聊天室成员信息     |
| [`V2NIMChatroomMemberQueryOption`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMemberQueryOption)   | 分页查询选项配置               |
| [`getMemberByIds`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberByIds)                                   | 批量获取指定聊天室成员信息     |
| [`getMemberListByTag`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberListByTag)                           | 根据标签分页获取聊天室成员列表 |
| [`getMemberCountByTag`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberCountByTag)                         | 获取指定标签下的聊天室成员人数 |
| [`updateSelfMemberInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateSelfMemberInfo)                       | 更新本人的聊天室成员信息       |
| [`updateMemberRole`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateMemberRole)                               | 更新聊天室成员角色             |
| [`setMemberBlockedStatus`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#setMemberBlockedStatus)                   | 设置聊天室成员黑名单状态       |
| [`setMemberChatBannedStatus`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#setMemberChatBannedStatus)             | 设置聊天室成员禁言状态         |
| [`setMemberTempChatBanned`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#setMemberTempChatBanned)                 | 设置聊天室成员临时禁言状态     |
| [`kickMember`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#kickMember)                                           | 将指定成员踢出聊天室           |

Web/uni-app/小程序/Node.js/Electron/鸿蒙

| API                                                                                                                                               | 说明                           |
| ------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------ |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)       | 获取聊天室服务类               |
| [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#on)                                              | 注册聊天室监听器               |
| [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#off)                                            | 取消注册聊天室监听器           |
| [`V2NIMChatroomMemberRole`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMemberRole)                 | 聊天室成员角色                 |
| [`V2NIMChatroomEnterParams.anonymousMode`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams) | 聊天室登录参数                 |
| [`V2NIMChatroomMember`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMember)                         | 聊天室成员                     |
| [`getMemberListByOption`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberListByOption)                     | 分页获取所有聊天室成员信息     |
| [`V2NIMChatroomMemberQueryOption`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMemberQueryOption)   | 分页查询选项配置               |
| [`getMemberByIds`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberByIds)                                   | 批量获取指定聊天室成员信息     |
| [`getMemberListByTag`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberListByTag)                           | 根据标签分页获取聊天室成员列表 |
| [`getMemberCountByTag`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberCountByTag)                         | 获取指定标签下的聊天室成员人数 |
| [`updateSelfMemberInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateSelfMemberInfo)                       | 更新本人的聊天室成员信息       |
| [`updateMemberRole`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateMemberRole)                               | 更新聊天室成员角色             |
| [`setMemberBlockedStatus`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#setMemberBlockedStatus)                   | 设置聊天室成员黑名单状态       |
| [`setMemberChatBannedStatus`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#setMemberChatBannedStatus)             | 设置聊天室成员禁言状态         |
| [`setMemberTempChatBanned`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#setMemberTempChatBanned)                 | 设置聊天室成员临时禁言状态     |
| [`kickMember`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#kickMember)                                           | 将指定成员踢出聊天室           |

Flutter

| API                                                                                                                                               | 说明                           |
| ------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------ |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomService)       | 获取聊天室服务类               |
| [`addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#addChatroomListener)                         | 注册聊天室监听器               |
| [`removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#removeChatroomListener)                   | 取消注册聊天室监听器           |
| [`V2NIMChatroomMemberRole`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomMemberRole)                 | 聊天室成员角色                 |
| [`V2NIMChatroomEnterParams.anonymousMode`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomEnterParams) | 聊天室登录参数                 |
| [`V2NIMChatroomMember`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomMember)                         | 聊天室成员                     |
| [`getMemberListByOption`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#getMemberListByOption)                     | 分页获取所有聊天室成员信息     |
| [`V2NIMChatroomMemberQueryOption`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomMemberQueryOption)   | 分页查询选项配置               |
| [`getMemberByIds`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#getMemberByIds)                                   | 批量获取指定聊天室成员信息     |
| [`getMemberListByTag`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#getMemberListByTag)                           | 根据标签分页获取聊天室成员列表 |
| [`getMemberCountByTag`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#getMemberCountByTag)                         | 获取指定标签下的聊天室成员人数 |
| [`updateSelfMemberInfo`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#updateSelfMemberInfo)                       | 更新本人的聊天室成员信息       |
| [`updateMemberRole`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#updateMemberRole)                               | 更新聊天室成员角色             |
| [`setMemberBlockedStatus`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#setMemberBlockedStatus)                   | 设置聊天室成员黑名单状态       |
| [`setMemberChatBannedStatus`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#setMemberChatBannedStatus)             | 设置聊天室成员禁言状态         |
| [`setMemberTempChatBanned`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#setMemberTempChatBanned)                 | 设置聊天室成员临时禁言状态     |
| [`kickMember`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#kickMember)                                           | 将指定成员踢出聊天室           |

Android/iOS/macOS/WindowsWeb/uni-app/applet/Node.js/Electron/HongmengFlutter

| API                                                                                                                                               | Explain                                                         |
| ------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)       | Get chat room services                                          |
| [`addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#addChatroomListener)                         | Register the chat room listener                                 |
| [`removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#removeChatroomListener)                   | Unregister the chat room listener                               |
| [`V2NIMChatroomMemberRole`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMemberRole)                 | Chat room member role                                           |
| [`V2NIMChatroomEnterParams.anonymousMode`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams) | Chat room login parameters                                      |
| [`V2NIMChatroomMember`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMember)                         | Chat room members                                               |
| [`getMemberListByOption`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberListByOption)                     | Get all chat room member information by paging                  |
| [`V2NIMChatroomMemberQueryOption`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMemberQueryOption)   | Configuration of paging query options                           |
| [`getMemberByIds`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberByIds)                                   | Batch access to the information of designated chat room members |
| [`getMemberListByTag`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberListByTag)                           | Get the list of chat room members according to the tab page     |
| [`getMemberCountByTag`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberCountByTag)                         | Get the number of chat room members under the specified label   |
| [`updateSelfMemberInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateSelfMemberInfo)                       | Update my chat room member information                          |
| [`updateMemberRole`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateMemberRole)                               | Update the role of chat room member                             |
| [`setMemberBlockedStatus`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#setMemberBlockedStatus)                   | Set the blacklist status of chat room members                   |
| [`setMemberChatBannedStatus`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#setMemberChatBannedStatus)             | Set the chat room member ban status                             |
| [`setMemberTempChatBanned`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#setMemberTempChatBanned)                 | Set the temporary ban status of chat room members               |
| [`kickMember`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#kickMember)                                           | Kick the designated members out of the chat room                |

| API                                                                                                                                               | 说明                           |
| ------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------ |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)       | 获取聊天室服务类               |
| [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#on)                                              | 注册聊天室监听器               |
| [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#off)                                            | 取消注册聊天室监听器           |
| [`V2NIMChatroomMemberRole`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMemberRole)                 | 聊天室成员角色                 |
| [`V2NIMChatroomEnterParams.anonymousMode`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams) | 聊天室登录参数                 |
| [`V2NIMChatroomMember`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMember)                         | 聊天室成员                     |
| [`getMemberListByOption`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberListByOption)                     | 分页获取所有聊天室成员信息     |
| [`V2NIMChatroomMemberQueryOption`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMemberQueryOption)   | 分页查询选项配置               |
| [`getMemberByIds`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberByIds)                                   | 批量获取指定聊天室成员信息     |
| [`getMemberListByTag`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberListByTag)                           | 根据标签分页获取聊天室成员列表 |
| [`getMemberCountByTag`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMemberCountByTag)                         | 获取指定标签下的聊天室成员人数 |
| [`updateSelfMemberInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateSelfMemberInfo)                       | 更新本人的聊天室成员信息       |
| [`updateMemberRole`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateMemberRole)                               | 更新聊天室成员角色             |
| [`setMemberBlockedStatus`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#setMemberBlockedStatus)                   | 设置聊天室成员黑名单状态       |
| [`setMemberChatBannedStatus`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#setMemberChatBannedStatus)             | 设置聊天室成员禁言状态         |
| [`setMemberTempChatBanned`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#setMemberTempChatBanned)                 | 设置聊天室成员临时禁言状态     |
| [`kickMember`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#kickMember)                                           | 将指定成员踢出聊天室           |

| API                                                                                                                                               | 说明                           |
| ------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------ |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomService)       | 获取聊天室服务类               |
| [`addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#addChatroomListener)                         | 注册聊天室监听器               |
| [`removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#removeChatroomListener)                   | 取消注册聊天室监听器           |
| [`V2NIMChatroomMemberRole`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomMemberRole)                 | 聊天室成员角色                 |
| [`V2NIMChatroomEnterParams.anonymousMode`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomEnterParams) | 聊天室登录参数                 |
| [`V2NIMChatroomMember`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomMember)                         | 聊天室成员                     |
| [`getMemberListByOption`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#getMemberListByOption)                     | 分页获取所有聊天室成员信息     |
| [`V2NIMChatroomMemberQueryOption`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomMemberQueryOption)   | 分页查询选项配置               |
| [`getMemberByIds`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#getMemberByIds)                                   | 批量获取指定聊天室成员信息     |
| [`getMemberListByTag`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#getMemberListByTag)                           | 根据标签分页获取聊天室成员列表 |
| [`getMemberCountByTag`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#getMemberCountByTag)                         | 获取指定标签下的聊天室成员人数 |
| [`updateSelfMemberInfo`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#updateSelfMemberInfo)                       | 更新本人的聊天室成员信息       |
| [`updateMemberRole`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#updateMemberRole)                               | 更新聊天室成员角色             |
| [`setMemberBlockedStatus`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#setMemberBlockedStatus)                   | 设置聊天室成员黑名单状态       |
| [`setMemberChatBannedStatus`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#setMemberChatBannedStatus)             | 设置聊天室成员禁言状态         |
| [`setMemberTempChatBanned`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#setMemberTempChatBanned)                 | 设置聊天室成员临时禁言状态     |
| [`kickMember`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#kickMember)                                           | 将指定成员踢出聊天室           |
