# 聊天室标签管理

NetEase Yunxin IM supports the chat room tag function, which can achieve a personalized chat room effect by setting tag information when logging in to the chat room. With this function, you can create a personalized and hierarchical chat experience to meet the needs of a variety of scenarios such as live interaction and community operation.

## Support platform

The development platform or framework applicable to this article is shown in the following table. For the interfaces involved, please refer to the following [relevant interface](#%E7%9B%B8%E5%85%B3%E6%8E%A5%E5%8F%A3)chapters:

| Android | iOS | macOS/Windows | Web/uni-app/applet | Node.js/Electron | Hongmeng | Flutter |
| ------- | --- | ------------- | ------------------ | ---------------- | -------- | ------- |
| ✔️️️️️️      | ✔️️️️️️  | ✔️️️️️️            | ✔️️️️️️                 | ✔️️️️️️               | ✔️️️️️️       | ✔️️      |

## Overview of functions

The chat room tag function of NetEase Yunxin IM allows developers to classify and manage chat room users according to user information, and realize accurate message delivery and user grouping, so as to create a personalized chat experience. Through the label, you can:

- Assign specific tags to users (up to 10 tags per user)
- Accurately control the scope of message delivery
- Manage member ban status by label
- Query online users under specific tags
- Realize group interaction in the chat room
- Match user accounts through standard regular expressions

## Core concept

**标签表达式** 是标签功能的核心机制，用于精确描述目标用户群体。标签表达式有两个关键字段 `tags` 和 `notifyTargetTags`。

- **`tags`**: Identify the label collection to which the user belongs.
- **`notifyTargetTags`**: Specify the target tag user who needs notifications or messages.

`tags` 和 `notifyTargetTags` 必须设置其一，否则返回参数错误。

A single expression is limited to a maximum of 128 characters. Label expressions support the following advanced features:

| Characteristics of expressions                                                     | Example                   |
| ---------------------------------------------------------------------------------- | ------------------------- |
| **Specified matching**: use ordinary assignment to match the specified label value | Match the label abc:      |
| `{"tag": "abc"}`                                                                   |
| **Regular matching**: use regular expression fuzzy matching                        | Match the label with abc: |

`{"tag": "abc.*", "matchType": "regex"}`
Match all tags:
`{"tag": ".*", "matchType": "regex"}` |
| **Logical operation**: use and (and), or (or) operations to match multiple results | Match the tags abc and def:
`{"tag": "abc"} and {"tag": "def"}`
Match the label abc or def:
`{"tag": "abc"} or {"tag": "def"}` |
| **Control priority**: set the priority of logical operations through half-width brackets (`()`) | Match the tag abc or def, and use the regular to match the tag with 123 or 456:
`({"tag": "abc"} or {"tag": "def"}) and ({"tag": ".*123", "matchType": "regex"} or {"tag": "456.*", "matchType": "regex"})` |

## Preparation work

Before following this article, please make sure that you have completed the following settings:

- [Chat room login](https://doc.yunxin.163.com/messaging2/guide/DI2NDc1NzQ?platform=client)has been [realized](https://doc.yunxin.163.com/messaging2/guide/DI2NDc1NzQ?platform=client).
- 在使用聊天室服务中的 API 前，需要先调用 [`getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService) 方法获取聊天室服务类。

## Listen to the chat room label change

在设置聊天室标签信息前，您可以先注册监听 `onChatroomTagsUpdated` 聊天室标签信息变更事件。监听后，在更新聊天室标签后，聊天室内所有成员会收到对应的通知。

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
V2NIMChatroomListener listener = new V2NIMChatroomListener() {
    @Override
    public void onChatroomTagsUpdated(List<String> tags) {
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
- (void)onChatroomTagsUpdated:(NSArray<NSString *> *)tags
{
}
@end

```

macOS/Windows

```
V2NIMChatroomListener listener;
listener.onChatroomTagsUpdated = [](nstd::vector<nstd::string> tags) {
    // handle chatroom tags updated
};
chatroomService.addChatroomListener(listener);

```

Web/uni-app/小程序

```
chatroom.V2NIMChatroomService.on('onChatroomTagsUpdated', function (tags: Array<string>){})

```

Node.js/Electron

```
chatroom.chatroomService.on('chatroomTagsUpdated', function (tags: Array<string>){})

```

鸿蒙

```
chatroom.chatroomService.on('onChatroomTagsUpdated', (tags: Array<string>) => {})

```

Flutter

```
//首先添加监听
await chatroomClient?.getChatroomService().addChatroomListener();
//然后设置监听
chatroomClient!.getChatroomService().onChatroomTagsUpdated.listen((event) {
//todo something
});

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
V2NIMChatroomListener listener = new V2NIMChatroomListener() {
    @Override
    public void onChatroomTagsUpdated(List<String> tags) {
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
- (void)onChatroomTagsUpdated:(NSArray<NSString *> *)tags
{
}
@end

```

```
V2NIMChatroomListener listener;
listener.onChatroomTagsUpdated = [](nstd::vector<nstd::string> tags) {
    // handle chatroom tags updated
};
chatroomService.addChatroomListener(listener);

```

```
chatroom.V2NIMChatroomService.on('onChatroomTagsUpdated', function (tags: Array<string>){})

```

```
chatroom.chatroomService.on('chatroomTagsUpdated', function (tags: Array<string>){})

```

```
chatroom.chatroomService.on('onChatroomTagsUpdated', (tags: Array<string>) => {})

```

```
//首先添加监听
await chatroomClient?.getChatroomService().addChatroomListener();
//然后设置监听
chatroomClient!.getChatroomService().onChatroomTagsUpdated.listen((event) {
//todo something
});

```

## Set the chat room tag information

You can set the label information in the following two ways.

- Set the tag information when logging in

调用 `enter` 方法登录聊天室时，通过配置 `tagConfig` 来设置标签相关参数。

- Update the existing tag information

调用 `updateChatroomTags` 方法批量更新已有的聊天室标签信息。

### Set the tag information when logging in

调用 `enter` 方法登录聊天室时，通过配置 `tagConfig` 来设置以下标签相关参数：

- **`tags`**: It is used to identify the tag belonging to this login. The same long connection supports up to 10 tags, and each label can be up to 32 characters, such as `["abc", "def"]`.
- **`notifyTargetTags`**: Specify the tag users who log in/out of the chat room to notify the broadcast. Please refer to the [label expression](#%E6%A0%B8%E5%BF%83%E6%A6%82%E5%BF%B5)for details. If it is defaulted, the server will automatically generate a label expression based on `tags`.

For the specific implementation of logging in to the chat room, please refer to the [chat room login document](https://doc.yunxin.163.com/messaging2/guide/DI2NDc1NzQ?platform=client).

### Update the chat room tag information

调用 `updateChatroomTags` 方法批量更新聊天室标签信息。

After the update, all members in the chat room will receive a chat room tag information change callback `onChatroomTagsUpdated`, as well as notification messages of the type `TAGS_UPDATE`.

After modifying the tag of the chat room user, it will notify all the online terminals of the modified person, and broadcast to notify all users in the chat room.

The sample code is as follows:

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomTagsUpdateParams updateParams = new V2NIMChatroomTagsUpdateParams();
// tag 列表
List<String> tags = getTags();
updateParams.setTags(tags);
updateParams.setNotifyTargetTags("xxx");
// 以上两个字段至少需要设置一个，否则会返回参数错误

v2ChatroomService.updateChatroomTags(updateParams, new V2NIMSuccessCallback<Void>() {
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

V2NIMChatroomTagsUpdateParams *updateParams = [[V2NIMChatroomTagsUpdateParams alloc] init];
// tag 列表
NSArray<NSString *> *tags = @[ @"tag0", @"tag1" ]];
updateParams.tags = tags;
updateParams.notifyTargetTags = @"xxx";
// 以上两个字段至少需要设置一个，否则会返回参数错误
[service updateChatroomTags:updateParams
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
V2NIMChatroomTagsUpdateParams updateParams;
updateParams.tags = {"tag1", "tag2"};
updateParams.notifyTargetTags = R"({"tag": "tag1"})";
chatroomService.updateChatroomTags(
    updateParams,
    []() {
        // update chatroom tags succeeded
    },
    [](V2NIMError error) {
        // update chatroom tags failed, handle error
    });

```

Web/uni-app/小程序

```
await chatroomV2.V2NIMChatroomService.updateChatroomTags(
  {
    "tags": [
      "tag1",
      "tag2"
    ],
    "notifyTargetTags": "{tag: \"tag1\"}",
    "notificationEnabled": true,
    "notificationExtension": "notificationExtension"
  }
)

```

Node.js/Electron

```
await chatroomService.updateChatroomTags({
    tags: ['tag1', 'tag2']
})

```

鸿蒙

```
await this.chatroomClient.chatroomService.updateChatroomTags(
  {
    "tags": [
      "tag1",
      "tag2"
    ],
    "notifyTargetTags": "{tag: \"tag1\"}",
    "notificationEnabled": true,
    "notificationExtension": "notificationExtension"
  }
)

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
final params = V2NIMChatroomTagsUpdateParams();
var result = await chatroomService?.updateChatroomTags(params);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomTagsUpdateParams updateParams = new V2NIMChatroomTagsUpdateParams();
// tag 列表
List<String> tags = getTags();
updateParams.setTags(tags);
updateParams.setNotifyTargetTags("xxx");
// 以上两个字段至少需要设置一个，否则会返回参数错误

v2ChatroomService.updateChatroomTags(updateParams, new V2NIMSuccessCallback<Void>() {
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

V2NIMChatroomTagsUpdateParams *updateParams = [[V2NIMChatroomTagsUpdateParams alloc] init];
// tag 列表
NSArray<NSString *> *tags = @[ @"tag0", @"tag1" ]];
updateParams.tags = tags;
updateParams.notifyTargetTags = @"xxx";
// 以上两个字段至少需要设置一个，否则会返回参数错误
[service updateChatroomTags:updateParams
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
V2NIMChatroomTagsUpdateParams updateParams;
updateParams.tags = {"tag1", "tag2"};
updateParams.notifyTargetTags = R"({"tag": "tag1"})";
chatroomService.updateChatroomTags(
    updateParams,
    []() {
        // update chatroom tags succeeded
    },
    [](V2NIMError error) {
        // update chatroom tags failed, handle error
    });

```

```
await chatroomV2.V2NIMChatroomService.updateChatroomTags(
  {
    "tags": [
      "tag1",
      "tag2"
    ],
    "notifyTargetTags": "{tag: \"tag1\"}",
    "notificationEnabled": true,
    "notificationExtension": "notificationExtension"
  }
)

```

```
await chatroomService.updateChatroomTags({
    tags: ['tag1', 'tag2']
})

```

```
await this.chatroomClient.chatroomService.updateChatroomTags(
  {
    "tags": [
      "tag1",
      "tag2"
    ],
    "notifyTargetTags": "{tag: \"tag1\"}",
    "notificationEnabled": true,
    "notificationExtension": "notificationExtension"
  }
)

```

```
final chatroomService = chatroomClient?.getChatroomService();
final params = V2NIMChatroomTagsUpdateParams();
var result = await chatroomService?.updateChatroomTags(params);

```

## Chat room tag message management

### Send chat room tag messages

调用 `sendMessage` 方法发送聊天室消息时，设置 `notifyTargetTags` 参数指定聊天室消息投递的标签对象，实现消息的精准投递。具体请参考 [标签表达式](#%E6%A0%B8%E5%BF%83%E6%A6%82%E5%BF%B5)。若缺省则服务器会使用登录时设置的 `notifyTargetTags`。

For the specific implementation of message sending, please refer to [chat room message management](https://doc.yunxin.163.com/messaging2/guide/DQzNjE0MDU?platform=client).

### Get chat room messages according to the tag

通过调用 `getMessageListByTag` 方法按照标签信息分页获取所有聊天室历史消息，包含聊天室通知消息。

This method is used to paging to obtain chat room message data until the full amount of chat room messages are obtained, and the obtained messages do not include deleted messages.

The sample code is as follows:

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomTagMessageOption messageOption = new V2NIMChatroomTagMessageOption();
// tag 列表
List<String> tags = getTags();
// 设置查询的 tag 列表，必传字段，传 null 或者 size 为 0，会返回参数错误
messageOption.setTags(tags);
// 设置查询数量
messageOption.setLimit(100);
List<V2NIMMessageType> messageTypes = getMessageTypes();
// 设置查询的消息类型，如果列表为空，表示查询所有类型的消息
messageOption.setMessageTypes(messageTypes);
messageOption.setDirection(V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
// 设置查询开始时间，首次传 0，单位毫秒
messageOption.setBeginTime(0L);
// 设置查询结束时间，默认 0 表示当前时间，单位毫秒
messageOption.setEndTime(0L);

v2ChatroomService.getMessageListByTag(messageOption, new V2NIMSuccessCallback<List<V2NIMChatroomMessage>>() {
    @Override
    public void onSuccess(List<V2NIMChatroomMessage> v2NIMChatroomMessages) {
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
V2NIMChatroomTagMessageOption *messageOption = [[V2NIMChatroomTagMessageOption alloc] init];
// 设置查询的 tag 列表，必传字段，传 null 或者 size 为 0，会返回参数错误
messageOption.tags = @[@"tag1", @"tag2"];
// 设置查询数量
messageOption.limit = 100;
// 设置查询的消息类型，如果列表为空，表示查询所有类型的消息
messageOption.messageTypes = @[@(V2NIM_MESSAGE_TYPE_TEXT), @(V2NIM_MESSAGE_TYPE_FILE)];
// 设置查询方向
messageOption.direction = V2NIM_QUERY_DIRECTION_DESC;
// 设置查询开始时间，单位秒
messageOption.beginTime = 0;
// 设置查询结束时间，默认 0 表示当前时间，单位秒
messageOption.endTime = 0;
[service getMessageListByTag:messageOption
                    success:^(NSArray<V2NIMChatroomMessage *> *messages)
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
V2NIMChatroomTagMessageOption messageOption;
messageOption.tags = {"tag1", "tag2"};
messageOption.limit = 10;
chatroomService.getMessageListByTag(
    messageOption,
    [](nstd::vector<V2NIMChatroomMessage> messages) {
        // get message list by tag succeeded
    },
    [](V2NIMError error) {
        // get message list by tag failed, handle error
    });

```

Web/uni-app/小程序

```
const messages = await chatroom.V2NIMChatroomService.getMessageListByTag({
    tags: ['tag1', 'tag2'], // 查询的 tags
    limit: 100,
    direction: V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC
})

```

Node.js/Electron

```
const result = await chatroomService.getMessageListByTag({
    tags: ['tag1', 'tag2'],
    limit: 10
})

```

鸿蒙

```
const messages = await this.chatroomClient.chatroomService.getMessageListByTag({
    tags: ['tag1', 'tag2'], //查询的 tags
    limit: 100,
    direction: V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC
})

```

Flutter

```
final option = V2NIMChatroomTagMessageOption();
final messageList = (await chatroomService.getMessageListByTag(option));

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomTagMessageOption messageOption = new V2NIMChatroomTagMessageOption();
// tag 列表
List<String> tags = getTags();
// 设置查询的 tag 列表，必传字段，传 null 或者 size 为 0，会返回参数错误
messageOption.setTags(tags);
// 设置查询数量
messageOption.setLimit(100);
List<V2NIMMessageType> messageTypes = getMessageTypes();
// 设置查询的消息类型，如果列表为空，表示查询所有类型的消息
messageOption.setMessageTypes(messageTypes);
messageOption.setDirection(V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
// 设置查询开始时间，首次传 0，单位毫秒
messageOption.setBeginTime(0L);
// 设置查询结束时间，默认 0 表示当前时间，单位毫秒
messageOption.setEndTime(0L);

v2ChatroomService.getMessageListByTag(messageOption, new V2NIMSuccessCallback<List<V2NIMChatroomMessage>>() {
    @Override
    public void onSuccess(List<V2NIMChatroomMessage> v2NIMChatroomMessages) {
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
V2NIMChatroomTagMessageOption *messageOption = [[V2NIMChatroomTagMessageOption alloc] init];
// 设置查询的 tag 列表，必传字段，传 null 或者 size 为 0，会返回参数错误
messageOption.tags = @[@"tag1", @"tag2"];
// 设置查询数量
messageOption.limit = 100;
// 设置查询的消息类型，如果列表为空，表示查询所有类型的消息
messageOption.messageTypes = @[@(V2NIM_MESSAGE_TYPE_TEXT), @(V2NIM_MESSAGE_TYPE_FILE)];
// 设置查询方向
messageOption.direction = V2NIM_QUERY_DIRECTION_DESC;
// 设置查询开始时间，单位秒
messageOption.beginTime = 0;
// 设置查询结束时间，默认 0 表示当前时间，单位秒
messageOption.endTime = 0;
[service getMessageListByTag:messageOption
                    success:^(NSArray<V2NIMChatroomMessage *> *messages)
                    {
                        // 查询成功
                    }
                    failure:^(V2NIMError *error)
                    {
                        // 查询失败
                    }];

```

```
V2NIMChatroomTagMessageOption messageOption;
messageOption.tags = {"tag1", "tag2"};
messageOption.limit = 10;
chatroomService.getMessageListByTag(
    messageOption,
    [](nstd::vector<V2NIMChatroomMessage> messages) {
        // get message list by tag succeeded
    },
    [](V2NIMError error) {
        // get message list by tag failed, handle error
    });

```

```
const messages = await chatroom.V2NIMChatroomService.getMessageListByTag({
    tags: ['tag1', 'tag2'], // 查询的 tags
    limit: 100,
    direction: V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC
})

```

```
const result = await chatroomService.getMessageListByTag({
    tags: ['tag1', 'tag2'],
    limit: 10
})

```

```
const messages = await this.chatroomClient.chatroomService.getMessageListByTag({
    tags: ['tag1', 'tag2'], //查询的 tags
    limit: 100,
    direction: V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC
})

```

```
final option = V2NIMChatroomTagMessageOption();
final messageList = (await chatroomService.getMessageListByTag(option));

```

## Chat room tag member management

### Get chat room members according to the tag

通过调用 `getMemberListByTag` 方法根据标签分页获取聊天室成员列表。

Query the list of online members under a tab. For users who log in to multiple terminals, multiple records will be returned.

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

### Get the number of chat room members according to the tag

通过调用 `getMemberCountByTag` 方法获取指定标签下的聊天室成员人数。

Query the number of online users under a certain label. In the case of multiple terminals with the same account, the number of online users is counted as 1.

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

### Set up a temporary ban on chat room tag members

调用 `setTempChatBannedByTag` 方法设置聊天室标签临时禁言。设置后，被临时禁言的聊天室标签中的所有成员，无法在聊天室发送消息。临时禁言结束后，恢复发送消息权限。

Only chat room creators and administrators are allowed to call this interface, otherwise the error code 109432 will be returned.

After the local or multi-terminal synchronous setting of the chat room tag is temporarily banned:

只有在配置参数 `V2NIMChatroomTagTempChatBannedParams` 中传入 `notifyTargetTags`（需要通知的聊天室标签），临时禁言成功后，设置的聊天室标签下的成员才会收到以下回调和通知，若不传入 `notifyTargetTags`，则不会触发。

- **Chat room tag members who have been temporarily banned**: receive a temporary ban status change callback `onSelfTempChatBannedUpdated`.
- **All members in the chat room**:
  - 收到被临时禁言的标签成员信息变更回调 `onChatroomMemberInfoUpdated`。
  - Received a chat room notification message, the notification message type is `TAG_TEMP_CHAT_BANNED_ADDED(14)`. After lifting the temporary ban of the chat room tag, all chat room members received the chat room notification message type `TAG_TEMP_CHAT_BANNED_REMOVED(15)`

Android

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
V2NIMChatroomTagTempChatBannedParams params = new V2NIMChatroomTagTempChatBannedParams();
//设置禁言的 tag,必传字段，如果不传，会返回参数错误
params.setTargetTag("xxx");
//设置禁言时长，单位：秒，单次最大：30 天，取消则设置为：0
params.setDuration(1000);
//是否需要通知，true：通知，false：不通知
params.setNotificationEnabled(true);
//设置通知扩展字段，可不传
params.setNotificationExtension("xxx");

v2ChatroomService.setTempChatBannedByTag(params, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        //设置成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //设置失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

V2NIMChatroomTagTempChatBannedParams *params = [[V2NIMChatroomTagTempChatBannedParams alloc] init];
//设置禁言的 tag，必传字段，如果不传，会返回参数错误
params.targetTag = @"xxx";
//设置禁言时长，单位：秒，单次最大：30 天，取消则设置为：0
params.duration = 1000;
//是否需要通知，true：通知，false：不通知
params.notificationEnabled = YES;
//设置通知扩展字段，可不传
params.notificationExtension = @"xxx";
[service setTempChatBannedByTag:params
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
V2NIMChatroomTagTempChatBannedParams params;
params.tags = "tag1";
params.duration = 60;
params.notificationEnabled = true;
params.notificationExtension = "notificationExtension";
chatroomService.setTempChatBannedByTag(
    params,
    []() {
        // set temp chat banned by tag succeeded
    },
    [](V2NIMError error) {
        // set temp chat banned by tag failed, handle error
    });

```

Web/uni-app/小程序

```
await chatroomV2.V2NIMChatroomService.setTempChatBannedByTag(
  {
    "targetTag": "ccc", // 被禁言 tag
    "notifyTargetTags": "{tag: \"ccc\"}", // 接收通知的目标表达式
    "duration": 1000, // 禁言时长
    "notificationEnabled": true // 是否通知
  }
)

```

Node.js/Electron

```
await chatroomService.setTempChatBannedByTag({
    "targetTag": "ccc", // 被禁言 tag
    "notifyTargetTags": "{tag: \"ccc\"}", // 接收通知的目标表达式
    "duration": 1000, // 禁言时长
    "notificationEnabled": true // 是否通知
})

```

HarmonyOS

```
await this.chatroomClient.chatroomService.setTempChatBannedByTag(
  {
    "targetTag": "ccc", // 被禁言 tag
    "notifyTargetTags": "{tag: \"ccc\"}", // 接收通知的目标表达式
    "duration": 1000, // 禁言时长
    "notificationEnabled": true // 是否通知
  }
)

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
    final params = V2NIMChatroomTagTempChatBannedParams();
    var result = await chatroomService?.setTempChatBannedByTag(params);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHarmony OSFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
V2NIMChatroomTagTempChatBannedParams params = new V2NIMChatroomTagTempChatBannedParams();
//设置禁言的 tag,必传字段，如果不传，会返回参数错误
params.setTargetTag("xxx");
//设置禁言时长，单位：秒，单次最大：30 天，取消则设置为：0
params.setDuration(1000);
//是否需要通知，true：通知，false：不通知
params.setNotificationEnabled(true);
//设置通知扩展字段，可不传
params.setNotificationExtension("xxx");

v2ChatroomService.setTempChatBannedByTag(params, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        //设置成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //设置失败
    }
});

```

```
// 通过实例 ID 获取聊天室实例
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

V2NIMChatroomTagTempChatBannedParams *params = [[V2NIMChatroomTagTempChatBannedParams alloc] init];
//设置禁言的 tag，必传字段，如果不传，会返回参数错误
params.targetTag = @"xxx";
//设置禁言时长，单位：秒，单次最大：30 天，取消则设置为：0
params.duration = 1000;
//是否需要通知，true：通知，false：不通知
params.notificationEnabled = YES;
//设置通知扩展字段，可不传
params.notificationExtension = @"xxx";
[service setTempChatBannedByTag:params
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
V2NIMChatroomTagTempChatBannedParams params;
params.tags = "tag1";
params.duration = 60;
params.notificationEnabled = true;
params.notificationExtension = "notificationExtension";
chatroomService.setTempChatBannedByTag(
    params,
    []() {
        // set temp chat banned by tag succeeded
    },
    [](V2NIMError error) {
        // set temp chat banned by tag failed, handle error
    });

```

```
await chatroomV2.V2NIMChatroomService.setTempChatBannedByTag(
  {
    "targetTag": "ccc", // 被禁言 tag
    "notifyTargetTags": "{tag: \"ccc\"}", // 接收通知的目标表达式
    "duration": 1000, // 禁言时长
    "notificationEnabled": true // 是否通知
  }
)

```

```
await chatroomService.setTempChatBannedByTag({
    "targetTag": "ccc", // 被禁言 tag
    "notifyTargetTags": "{tag: \"ccc\"}", // 接收通知的目标表达式
    "duration": 1000, // 禁言时长
    "notificationEnabled": true // 是否通知
})

```

```
await this.chatroomClient.chatroomService.setTempChatBannedByTag(
  {
    "targetTag": "ccc", // 被禁言 tag
    "notifyTargetTags": "{tag: \"ccc\"}", // 接收通知的目标表达式
    "duration": 1000, // 禁言时长
    "notificationEnabled": true // 是否通知
  }
)

```

```
final chatroomService = chatroomClient?.getChatroomService();
    final params = V2NIMChatroomTagTempChatBannedParams();
    var result = await chatroomService?.setTempChatBannedByTag(params);

```

## Best practices

### Tag usage strategy

Effective label design can improve the efficiency and user experience of chat room management. For example:

- **User role tags**: admin, vip, guest, new_user
- **Device tags**: ios, android, web, pc
- **Geographical labels**: north, south, overseas
- **Interest tags**: game, music, sports, tech
- **Activity tags**: event_2023, campaign_summer

### Performance and capacity optimization

- **Control the number of labels**: There should not be too many single-user labels, and it is recommended to control them within 10.
- **Cache tag data**: SDK does not cache tag-related data. Please implement the cache logic by yourself.
- **Batch processing of large-scale tag operations**: For large chat rooms, consider setting or updating tags in batches.

## Reference documents

- [Chat room login](https://doc.yunxin.163.com/messaging2/guide/DI2NDc1NzQ?platform=client)
- [Chat room message management](https://doc.yunxin.163.com/messaging2/guide/DQzNjE0MDU?platform=client)
- [Chat room member management](https://doc.yunxin.163.com/messaging2/guide/Dc4NTY1ODQ?platform=client)

## Related interfaces

安卓/iOS/macOS/Windows

| API                                                                                                                                                  | 说明                 |
| ---------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)          | 获取聊天室服务类     |
| [`V2NIMChatroomService.addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#addChatroomListener)       | 注册聊天室监听器     |
| [`V2NIMChatroomService.removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#removeChatroomListener) | 取消注册聊天室监听器 |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#enter)                                                        | 进入聊天室           |
| [`V2NIMChatroomEnterParams.tagConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams)        | 聊天室标签相关配置   |
| [`sendMessage`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#sendMessage)                                            | 发送聊天室消息       |
| [`V2NIMSendChatroomMessageParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMSendChatroomMessageParams)      | 发送消息的配置参数   |
| [`updateChatroomTags`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateChatroomTags)                              | 更新聊天室标签       |

Web/uni-app/小程序/Node.js/Electron/鸿蒙

| API                                                                                                                                             | 说明                 |
| ----------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)     | 获取聊天室服务类     |
| [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#on)                                            | 注册聊天室监听器     |
| [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#off)                                          | 取消注册聊天室监听器 |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#enter)                                                   | 进入聊天室           |
| [`V2NIMChatroomEnterParams.tagConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams)   | 聊天室标签相关配置   |
| [`sendMessage`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#sendMessage)                                       | 发送聊天室消息       |
| [`V2NIMSendChatroomMessageParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMSendChatroomMessageParams) | 发送消息的配置参数   |
| [`updateChatroomTags`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateChatroomTags)                         | 更新聊天室标签       |

Flutter

| API                                                                                                                                                  | 说明                 |
| ---------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomService)          | 获取聊天室服务类     |
| [`V2NIMChatroomService.addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#addChatroomListener)       | 注册聊天室监听器     |
| [`V2NIMChatroomService.removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#removeChatroomListener) | 取消注册聊天室监听器 |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#enter)                                                        | 进入聊天室           |
| [`V2NIMChatroomEnterParams.tagConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomEnterParams)        | 聊天室标签相关配置   |
| [`sendMessage`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#sendMessage)                                            | 发送聊天室消息       |
| [`V2NIMSendChatroomMessageParams`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMSendChatroomMessageParams)      | 发送消息的配置参数   |
| [`updateChatroomTags`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#updateChatroomTags)                              | 更新聊天室标签       |

Android/iOS/macOS/WindowsWeb/uni-app/applet/Node.js/Electron/HongmengFlutter

| API                                                                                                                                                  | Explain                                       |
| ---------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)          | Get chat room services                        |
| [`V2NIMChatroomService.addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#addChatroomListener)       | Register the chat room listener               |
| [`V2NIMChatroomService.removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#removeChatroomListener) | Unregister the chat room listener             |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#enter)                                                        | Enter the chat room                           |
| [`V2NIMChatroomEnterParams.tagConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams)        | Chat room tag related configuration           |
| [`sendMessage`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#sendMessage)                                            | Send chat room messages                       |
| [`V2NIMSendChatroomMessageParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMSendChatroomMessageParams)      | Configuration parameters for sending messages |
| [`updateChatroomTags`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateChatroomTags)                              | Update the chat room tag                      |

| API                                                                                                                                             | 说明                 |
| ----------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)     | 获取聊天室服务类     |
| [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#on)                                            | 注册聊天室监听器     |
| [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#off)                                          | 取消注册聊天室监听器 |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#enter)                                                   | 进入聊天室           |
| [`V2NIMChatroomEnterParams.tagConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams)   | 聊天室标签相关配置   |
| [`sendMessage`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#sendMessage)                                       | 发送聊天室消息       |
| [`V2NIMSendChatroomMessageParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMSendChatroomMessageParams) | 发送消息的配置参数   |
| [`updateChatroomTags`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateChatroomTags)                         | 更新聊天室标签       |

| API                                                                                                                                                  | 说明                 |
| ---------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomService)          | 获取聊天室服务类     |
| [`V2NIMChatroomService.addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#addChatroomListener)       | 注册聊天室监听器     |
| [`V2NIMChatroomService.removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#removeChatroomListener) | 取消注册聊天室监听器 |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#enter)                                                        | 进入聊天室           |
| [`V2NIMChatroomEnterParams.tagConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomEnterParams)        | 聊天室标签相关配置   |
| [`sendMessage`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#sendMessage)                                            | 发送聊天室消息       |
| [`V2NIMSendChatroomMessageParams`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMSendChatroomMessageParams)      | 发送消息的配置参数   |
| [`updateChatroomTags`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#updateChatroomTags)                              | 更新聊天室标签       |
