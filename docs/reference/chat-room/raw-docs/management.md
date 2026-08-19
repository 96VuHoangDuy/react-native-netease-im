# 聊天室管理

NetEase Yunxin IM supports chat room management functions, including obtaining and maintaining chat room information, coordinates, labels and other information.

This article introduces how to manage chat rooms through NetEase IM SDK (NIM SDK).

## Support platform

The development platform or framework applicable to this article is shown in the following table. For the interfaces involved, please refer to the following [relevant interface](#%E7%9B%B8%E5%85%B3%E6%8E%A5%E5%8F%A3)chapters:

| Android | iOS | macOS/Windows | Web/uni-app/applet | Node.js/Electron | Hongmeng | Flutter |
| ------- | --- | ------------- | ------------------ | ---------------- | -------- | ------- |
| ✔️️️️      | ✔️️️️  | ✔️️️️            | ✔️️️️                 | ✔️️️️               | ✔️️️️       | ✔️      |

## Prerequisites

Before operating according to this article, please make sure that you have [logged in to the chat room](https://doc.yunxin.163.com/messaging2/guide/DI2NDc1NzQ?platform=client).

## Chat room-related event monitoring

Before the chat room-related operations, you can register to listen to chat room-related events. After listening, you will receive the corresponding notification after the chat room management operation.

- **相关回调**：`onChatroomInfoUpdated` 聊天室资料信息更新回调，返回更新后的聊天室信息。聊天室内所有成员均会收到该回调。
- **The sample code is as follows**:

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomListener listener = new V2NIMChatroomListener() {

    @Override
    public void onChatroomInfoUpdated(V2NIMChatroomInfo chatroom) {

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

- (void)onChatroomInfoUpdated:(V2NIMChatroomInfo *)chatroomInfo
{

}

@end

```

macOS/Windows

```
V2NIMChatroomListener listener;
listener.onChatroomInfoUpdated = [](V2NIMChatroomInfo chatroomInfo) {
    // handle chatroom info updated
};
chatroomService.addChatroomListener(listener);

```

Web/uni-app/小程序

```
chatroom.V2NIMChatroomService.on('onChatroomInfoUpdated', function (chatroomInfo: V2NIMChatroomInfo){})

```

Node.js/Electron

```
chatroom.chatroomService.on('chatroomInfoUpdated', function (chatroomInfo: V2NIMChatroomInfo){})

```

鸿蒙

```
chatroom.chatroomService.on('onChatroomInfoUpdated', (chatroomInfo: V2NIMChatroomInfo) => {})

```

Flutter

```
//首先添加监听
await chatroomClient?.getChatroomService().addChatroomListener();
//然后设置监听
chatroomClient!.getChatroomService().onChatroomInfoUpdated.listen((event) {
//todo something
});

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomListener listener = new V2NIMChatroomListener() {

    @Override
    public void onChatroomInfoUpdated(V2NIMChatroomInfo chatroom) {

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

- (void)onChatroomInfoUpdated:(V2NIMChatroomInfo *)chatroomInfo
{

}

@end

```

```
V2NIMChatroomListener listener;
listener.onChatroomInfoUpdated = [](V2NIMChatroomInfo chatroomInfo) {
    // handle chatroom info updated
};
chatroomService.addChatroomListener(listener);

```

```
chatroom.V2NIMChatroomService.on('onChatroomInfoUpdated', function (chatroomInfo: V2NIMChatroomInfo){})

```

```
chatroom.chatroomService.on('chatroomInfoUpdated', function (chatroomInfo: V2NIMChatroomInfo){})

```

```
chatroom.chatroomService.on('onChatroomInfoUpdated', (chatroomInfo: V2NIMChatroomInfo) => {})

```

```
//首先添加监听
await chatroomClient?.getChatroomService().addChatroomListener();
//然后设置监听
chatroomClient!.getChatroomService().onChatroomInfoUpdated.listen((event) {
//todo something
});

```

## Get chat room information

通过调用 `getChatroomInfo` 方法获取聊天室信息。

NIM SDK does not support chat room information caching. You need to realize data caching by yourself.

The sample code is as follows:

安卓

```
V2NIMChatroomInfo chatroomInfo = chatroomClient.getChatroomInfo();

```

iOS

```
[[V2NIMChatroomClient getInstance:instanceId] getChatroomInfo];

```

macOS/Windows

```
auto chatroomInfo = chatroomClient.getChatroomInfo();

```

Web/uni-app/小程序

```
const chatroomInfo = chatroom.getChatroomInfo()

```

Node.js/Electron

```
const chatroomInfo = chatroomClient.getChatroomInfo()

```

鸿蒙

```
const client: V2NIMChatroomClient = this.getInstance(instanceId)
const ret = client.getChatroomInfo()

```

Flutter

```
final chatroomInfoResult = await chatroomClient?.getChatroomInfo();

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomInfo chatroomInfo = chatroomClient.getChatroomInfo();

```

```
[[V2NIMChatroomClient getInstance:instanceId] getChatroomInfo];

```

```
auto chatroomInfo = chatroomClient.getChatroomInfo();

```

```
const chatroomInfo = chatroom.getChatroomInfo()

```

```
const chatroomInfo = chatroomClient.getChatroomInfo()

```

```
const client: V2NIMChatroomClient = this.getInstance(instanceId)
const ret = client.getChatroomInfo()

```

```
final chatroomInfoResult = await chatroomClient?.getChatroomInfo();

```

## Modify the chat room information

### Prerequisite for use

在使用聊天室服务中的 API 前，需要先调用 `getChatroomService` 方法获取聊天室服务类。

### Realization method

通过调用 `updateChatroomInfo` 方法修改聊天室资料信息。

支持设置修改资料后是否通知，若设置通知，修改成功后聊天室中所有成员会收到类型为 `ROOM_INFO_UPDATED` 的通知消息。

Only the creator or administrator of the chat room has the permission to modify the information of the chat room.

The sample code is as follows:

安卓

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomUpdateParams updateParams = new V2NIMChatroomUpdateParams();
// 设置聊天室名称
updateParams.setRoomName("xxx");
// 设置聊天室公告
updateParams.setAnnouncement("xxx");
// 设置聊天室扩展字段
updateParams.setServerExtension("xxx");
// 设置聊天室直播地址
updateParams.setLiveUrl("xxx");
// 以上四个字段至少需要设置一个，否则会返回参数错误

// 设置是否需要通知
updateParams.setNotificationEnabled(true);
// 设置本次操作生成的通知中的扩展字段
updateParams.setNotificationExtension("xxx");

// 反垃圾配置，可不传
V2NIMAntispamConfig antispamConfig = new V2NIMAntispamConfig();

v2ChatroomService.updateChatroomInfo(updateParams, antispamConfig, new V2NIMSuccessCallback<Void>() {
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

V2NIMChatroomUpdateParams *updateParams = [[V2NIMChatroomUpdateParams alloc] init];
// 设置聊天室名称
updateParams.roomName = @"xxx";
// 设置聊天室公告
updateParams.announcement = @"xxx";
// 设置聊天室扩展字段
updateParams.serverExtension = @"xxx";
// 设置聊天室直播地址
updateParams.liveUrl = @"xxx";
// 以上四个字段至少需要设置一个，否则会返回参数错误

// 设置是否需要通知
updateParams.notificationEnabled = true;
// 设置本次操作生成的通知中的扩展字段
updateParams.notificationExtension = @"xxx";

// 反垃圾配置,可不传
V2NIMAntispamConfig *antispamConfig = [[V2NIMAntispamConfig alloc] init];
[service updateChatroomInfo:updateParams
             antispamConfig:antispamConfig
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
V2NIMChatroomUpdateParams updateParams;
updateParams.roomName = "roomName";
V2NIMAntispamConfig antispamConfig;
chatroomService.updateChatroomInfo(
    updateParams,
    antispamConfig,
    []() {
        // update chatroom info succeeded
    },
    [](V2NIMError error) {
        // update chatroom info failed, handle error
    });

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomService.updateChatroomInfo(
    {
        roomName: 'name',
        annoucement: 'announcement',
        serverExtension: 'serverExtension',
        notificationEnabled: true,
        notificationExtension: 'notificationExtension'
    }
)

```

Node.js/Electron

```
await chatroomService.updateChatroomInfo({
    roomName: 'new room name'
}, {})

```

鸿蒙

```
await this.chatroomClient.chatroomService.updateChatroomInfo(
    {
        roomName: 'name',
        annoucement: 'announcement',
        serverExtension: 'serverExtension',
        notificationEnabled: true,
        notificationExtension: 'notificationExtension'
    }
)

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
final params = V2NIMChatroomUpdateParams();
var result = await chatroomService?.updateChatroomInfo(params,null);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomUpdateParams updateParams = new V2NIMChatroomUpdateParams();
// 设置聊天室名称
updateParams.setRoomName("xxx");
// 设置聊天室公告
updateParams.setAnnouncement("xxx");
// 设置聊天室扩展字段
updateParams.setServerExtension("xxx");
// 设置聊天室直播地址
updateParams.setLiveUrl("xxx");
// 以上四个字段至少需要设置一个，否则会返回参数错误

// 设置是否需要通知
updateParams.setNotificationEnabled(true);
// 设置本次操作生成的通知中的扩展字段
updateParams.setNotificationExtension("xxx");

// 反垃圾配置，可不传
V2NIMAntispamConfig antispamConfig = new V2NIMAntispamConfig();

v2ChatroomService.updateChatroomInfo(updateParams, antispamConfig, new V2NIMSuccessCallback<Void>() {
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

V2NIMChatroomUpdateParams *updateParams = [[V2NIMChatroomUpdateParams alloc] init];
// 设置聊天室名称
updateParams.roomName = @"xxx";
// 设置聊天室公告
updateParams.announcement = @"xxx";
// 设置聊天室扩展字段
updateParams.serverExtension = @"xxx";
// 设置聊天室直播地址
updateParams.liveUrl = @"xxx";
// 以上四个字段至少需要设置一个，否则会返回参数错误

// 设置是否需要通知
updateParams.notificationEnabled = true;
// 设置本次操作生成的通知中的扩展字段
updateParams.notificationExtension = @"xxx";

// 反垃圾配置,可不传
V2NIMAntispamConfig *antispamConfig = [[V2NIMAntispamConfig alloc] init];
[service updateChatroomInfo:updateParams
             antispamConfig:antispamConfig
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
V2NIMChatroomUpdateParams updateParams;
updateParams.roomName = "roomName";
V2NIMAntispamConfig antispamConfig;
chatroomService.updateChatroomInfo(
    updateParams,
    antispamConfig,
    []() {
        // update chatroom info succeeded
    },
    [](V2NIMError error) {
        // update chatroom info failed, handle error
    });

```

```
await chatroom.V2NIMChatroomService.updateChatroomInfo(
    {
        roomName: 'name',
        annoucement: 'announcement',
        serverExtension: 'serverExtension',
        notificationEnabled: true,
        notificationExtension: 'notificationExtension'
    }
)

```

```
await chatroomService.updateChatroomInfo({
    roomName: 'new room name'
}, {})

```

```
await this.chatroomClient.chatroomService.updateChatroomInfo(
    {
        roomName: 'name',
        annoucement: 'announcement',
        serverExtension: 'serverExtension',
        notificationEnabled: true,
        notificationExtension: 'notificationExtension'
    }
)

```

```
final chatroomService = chatroomClient?.getChatroomService();
final params = V2NIMChatroomUpdateParams();
var result = await chatroomService?.updateChatroomInfo(params,null);

```

## Related interfaces

安卓/iOS/macOS/Windows

| API                                                                                                                                          | 说明                 |
| -------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| [`addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#addchatroomlistener)                    | 注册聊天室监听器     |
| [`removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#removeChatroomListener)              | 取消注册聊天室监听器 |
| [`V2NIMChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomInfo)                        | 聊天室信息           |
| [`V2NIMChatroomClient.getChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomInfo)        | 获取聊天室信息       |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)  | 获取聊天室服务类     |
| [`V2NIMChatroomService.updateChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateChatroomInfo) | 修改聊天室信息       |

Web/uni-app/小程序/Node.js/Electron/鸿蒙

| API                                                                                                                                          | 说明                 |
| -------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#on)                                         | 注册聊天室监听器     |
| [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#off)                                       | 取消注册聊天室监听器 |
| [`V2NIMChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomInfo)                        | 聊天室信息           |
| [`V2NIMChatroomClient.getChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomInfo)        | 获取聊天室信息       |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)  | 获取聊天室服务类     |
| [`V2NIMChatroomService.updateChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateChatroomInfo) | 修改聊天室信息       |

Flutter

| API                                                                                                                                          | 说明                 |
| -------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| [`addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#addChatroomListener)                    | 注册聊天室监听器     |
| [`removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#removeChatroomListener)              | 取消注册聊天室监听器 |
| [`V2NIMChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomInfo)                        | 聊天室信息           |
| [`V2NIMChatroomClient.getChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomInfo)        | 获取聊天室信息       |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomService)  | 获取聊天室服务类     |
| [`V2NIMChatroomService.updateChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#updateChatroomInfo) | 修改聊天室信息       |

Android/iOS/macOS/WindowsWeb/uni-app/applet/Node.js/Electron/HongmengFlutter

| API                                                                                                                                          | Explain                           |
| -------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------- |
| [`addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#addchatroomlistener)                    | Register the chat room listener   |
| [`removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#removeChatroomListener)              | Unregister the chat room listener |
| [`V2NIMChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomInfo)                        | Chat room information             |
| [`V2NIMChatroomClient.getChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomInfo)        | Get chat room information         |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)  | Get chat room services            |
| [`V2NIMChatroomService.updateChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateChatroomInfo) | Modify the chat room information  |

| API                                                                                                                                          | 说明                 |
| -------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#on)                                         | 注册聊天室监听器     |
| [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#off)                                       | 取消注册聊天室监听器 |
| [`V2NIMChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomInfo)                        | 聊天室信息           |
| [`V2NIMChatroomClient.getChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomInfo)        | 获取聊天室信息       |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)  | 获取聊天室服务类     |
| [`V2NIMChatroomService.updateChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateChatroomInfo) | 修改聊天室信息       |

| API                                                                                                                                          | 说明                 |
| -------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| [`addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#addChatroomListener)                    | 注册聊天室监听器     |
| [`removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#removeChatroomListener)              | 取消注册聊天室监听器 |
| [`V2NIMChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomInfo)                        | 聊天室信息           |
| [`V2NIMChatroomClient.getChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomInfo)        | 获取聊天室信息       |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomService)  | 获取聊天室服务类     |
| [`V2NIMChatroomService.updateChatroomInfo`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#updateChatroomInfo) | 修改聊天室信息       |
