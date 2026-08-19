# 聊天室消息管理

NetEase Yunxin IM supports the sending and receiving of various types of messages in the chat room, and the reception of notification messages related to chat room operations.

This article introduces how to realize chat room message sending and receiving, and chat room history message query.

## Support platform

The development platform or framework applicable to this article is shown in the following table. For the interfaces involved, please refer to the following [relevant interface](#%E7%9B%B8%E5%85%B3%E6%8E%A5%E5%8F%A3)chapters:

| Android | iOS | macOS/Windows | Web/uni-app/applet | Node.js/Electron | Hongmeng | Flutter |
| ------- | --- | ------------- | ------------------ | ---------------- | -------- | ------- |
| ✔️️️️      | ✔️️️️  | ✔️️️️            | ✔️️️️                 | ✔️️️️               | ✔️️️️       | ✔️️️️      |

## Prerequisites

Before sending and receiving messages, please make sure that:

- [Chat room login](https://doc.yunxin.163.com/messaging2/guide/DI2NDc1NzQ?platform=client)has been [realized](https://doc.yunxin.163.com/messaging2/guide/DI2NDc1NzQ?platform=client).
- I have understood [the usage restrictions](https://doc.yunxin.163.com/messaging2/guide/TQyODQ2ODQ?platform=client#%E6%B6%88%E6%81%AF%E5%8A%9F%E8%83%BD)of each message type.
- 在使用聊天室服务中的 API 前，需要先调用 `getChatroomService` 方法获取聊天室服务类。

## Send and receive messages in the chat room

### Message flow control mechanism

In order to ensure the user experience (such as avoiding server overload), there is a flow control mechanism (hereinafter referred to as the **flow control mechanism**) in the chat room:

- **For ordinary messages**: chat room users can receive up to 20 messages per second, and the excess part will be randomly discarded due to flow control.
- **For high-priority messages**: chat room users receive up to 10 messages per second, and more than one may be lost. In order to avoid the loss of important messages (usually server-side messages), important messages can be set to high-priority messages, so as to ensure that messages within the upper limit of high-priority message flow control (10 messages per second) are not lost. How to **achieve high-priority messages? Please**refer to the [message sending configuration options](#%E6%B6%88%E6%81%AF%E5%8F%91%E9%80%81%E9%85%8D%E7%BD%AE%E9%80%89%E9%A1%B9).

### API call timing

Take sending and receiving chat room text messages as an example, the timing of API calls is as follows:

```
#mermaid-render-0 {font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:16px;fill:#333;}#mermaid-render-0 .error-icon{fill:#552222;}#mermaid-render-0 .error-text{fill:#552222;stroke:#552222;}#mermaid-render-0 .edge-thickness-normal{stroke-width:2px;}#mermaid-render-0 .edge-thickness-thick{stroke-width:3.5px;}#mermaid-render-0 .edge-pattern-solid{stroke-dasharray:0;}#mermaid-render-0 .edge-pattern-dashed{stroke-dasharray:3;}#mermaid-render-0 .edge-pattern-dotted{stroke-dasharray:2;}#mermaid-render-0 .marker{fill:#333333;stroke:#333333;}#mermaid-render-0 .marker.cross{stroke:#333333;}#mermaid-render-0 svg{font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:16px;}#mermaid-render-0 .actor{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-0 text.actor>tspan{fill:black;stroke:none;}#mermaid-render-0 .actor-line{stroke:grey;}#mermaid-render-0 .messageLine0{stroke-width:1.5;stroke-dasharray:none;stroke:#333;}#mermaid-render-0 .messageLine1{stroke-width:1.5;stroke-dasharray:2,2;stroke:#333;}#mermaid-render-0 #arrowhead path{fill:#333;stroke:#333;}#mermaid-render-0 .sequenceNumber{fill:white;}#mermaid-render-0 #sequencenumber{fill:#333;}#mermaid-render-0 #crosshead path{fill:#333;stroke:#333;}#mermaid-render-0 .messageText{fill:#333;stroke:none;}#mermaid-render-0 .labelBox{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-0 .labelText,#mermaid-render-0 .labelText>tspan{fill:black;stroke:none;}#mermaid-render-0 .loopText,#mermaid-render-0 .loopText>tspan{fill:black;stroke:none;}#mermaid-render-0 .loopLine{stroke-width:2px;stroke-dasharray:2,2;stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);}#mermaid-render-0 .note{stroke:#aaaa33;fill:#fff5ad;}#mermaid-render-0 .noteText,#mermaid-render-0 .noteText>tspan{fill:black;stroke:none;}#mermaid-render-0 .activation0{fill:#f4f4f4;stroke:#666;}#mermaid-render-0 .activation1{fill:#f4f4f4;stroke:#666;}#mermaid-render-0 .activation2{fill:#f4f4f4;stroke:#666;}#mermaid-render-0 .actorPopupMenu{position:absolute;}#mermaid-render-0 .actorPopupMenuPanel{position:absolute;fill:#ECECFF;box-shadow:0px 8px 16px 0px rgba(0,0,0,0.2);filter:drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4));}#mermaid-render-0 .actor-man line{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-0 .actor-man circle,#mermaid-render-0 line{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;stroke-width:2px;}#mermaid-render-0 :root{--mermaid-font-family:"trebuchet ms",verdana,arial,sans-serif;}发送方NIMReceiving partypar[步骤 1：初始化 SDK]par[步骤2：发送方登录聊天室]par[步骤3：接收方注册聊天室监听-并登录]par[步骤4：聊天室消息收发]初始化 SDK初始化 SDK登录聊天室Listen to the chat room message reception(addChatroomListener)登录聊天室构造文本消息(Create TextMessage)发送聊天室消息(sendMessage)投递聊天室消息体SenderNIM接收方
```

```
sequenceDiagram

par 步骤 1：初始化 SDK
发送方 ->> NIM: 初始化 SDK
接收方 ->> NIM: 初始化 SDK
end
par 步骤 2：发送方登录聊天室
发送方 ->> NIM: 登录聊天室
end
par 步骤 3：接收方注册聊天室监听并登录
接收方 ->> NIM: 监听聊天室消息接收<br>(addChatroomListener)
接收方 ->> NIM: 登录聊天室
end
par 步骤 4：聊天室消息收发
发送方 ->> NIM: 构造文本消息<br>(createTextMessage)
发送方 ->> NIM: 发送聊天室消息<br>(sendMessage)
NIM ->> 接收方: 投递聊天室消息体
end

```

### Implementation steps

1. **The recipient**registers the chat room listener and listens to the chat room message receiving callback events `onReceiveMessages`.

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomListener listener = new V2NIMChatroomListener() {
    @Override
    public void onReceiveMessages(List<V2NIMChatroomMessage> messages) {

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

- (void)onReceiveMessages:(NSArray *)messages
{

}
@end

```

macOS/Windows

```
V2NIMChatroomListener listener;
listener.onReceiveMessages = [](nstd::vector<V2NIMChatroomMessage> messages) {
    // handle receive messages
};
chatroomService.addChatroomListener(listener);

```

Web/uni-app/小程序

```
chatroom.V2NIMChatroomService.on('onReceiveMessages', function (messages: V2NIMChatroomMessage[]){})

```

Node.js/Electron

```
chatroom.chatroomService.on('receiveMessages', function (messages: V2NIMChatroomMessage[]){})

```

鸿蒙

```
chatroom.chatroomService.on('onReceiveMessages', (messages: V2NIMChatroomMessage[]) => {})

```

Flutter

```
//首先添加监听
await chatroomClient?.getChatroomService().addChatroomListener();
//然后设置监听
chatroomClient!.getChatroomService().onReceiveMessages.listen((event) {
        print('getChatroomService:onReceiveMessages');
    });

```

安卓iOSmacOS/WindowsWeb/uni-app/小程序Node.js/ElectronHongmengFlutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomListener listener = new V2NIMChatroomListener() {
    @Override
    public void onReceiveMessages(List<V2NIMChatroomMessage> messages) {

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

- (void)onReceiveMessages:(NSArray *)messages
{

}
@end

```

```
V2NIMChatroomListener listener;
listener.onReceiveMessages = [](nstd::vector<V2NIMChatroomMessage> messages) {
    // handle receive messages
};
chatroomService.addChatroomListener(listener);

```

```
chatroom.V2NIMChatroomService.on('onReceiveMessages', function (messages: V2NIMChatroomMessage[]){})

```

```
chatroom.chatroomService.on('receiveMessages', function (messages: V2NIMChatroomMessage[]){})

```

```
chatroom.chatroomService.on('onReceiveMessages', (messages: V2NIMChatroomMessage[]) => {})

```

```
//首先添加监听
await chatroomClient?.getChatroomService().addChatroomListener();
//然后设置监听
chatroomClient!.getChatroomService().onReceiveMessages.listen((event) {
        print('getChatroomService:onReceiveMessages');
    });

```

2. **发送方** 调用 `createTextMessage` 方法，构建一条文本消息。

安卓

```
V2NIMChatroomMessage v2TextMessage = V2NIMChatroomMessageCreator.createTextMessage("text content");

```

iOS

```
V2NIMChatroomMessage *v2TextMessage = [V2NIMChatroomMessageCreator createTextMessage:@"text content"];

```

macOS/Windows

```
auto textMessage = V2NIMChatroomMessageCreator::createTextMessage("hello world");
if(!textMessage) {
    // create text message failed
}

```

Web/uni-app/小程序

```
try {
    const message = chatroom.V2NIMChatroomMessageCreator.createTextMessage('hello world')
} catch(err) {
    // todo error
}

```

Node.js/Electron

```
try {
    const message = v2.chatroomMessageCreator.createTextMessage(text)
} catch(err) {
    // todo error
}

```

鸿蒙

```
const message: V2NIMChatroomMessage = this.chatroomClient.messageCreator.createTextMessage('hello world')

```

Flutter

```
final message = (await V2NIMChatroomMessageCreator.createTextMessage(
            'test text chatroom message'))
        .data;

```

安卓iOSmacOS/WindowsWeb/uni-app/小程序Node.js/Electron鸿蒙Flutter

```
V2NIMChatroomMessage v2TextMessage = V2NIMChatroomMessageCreator.createTextMessage("text content");

```

```
V2NIMChatroomMessage *v2TextMessage = [V2NIMChatroomMessageCreator createTextMessage:@"text content"];

```

```
auto textMessage = V2NIMChatroomMessageCreator::createTextMessage("hello world");
if(!textMessage) {
    // create text message failed
}

```

```
try {
    const message = chatroom.V2NIMChatroomMessageCreator.createTextMessage('hello world')
} catch(err) {
    // todo error
}

```

```
try {
    const message = v2.chatroomMessageCreator.createTextMessage(text)
} catch(err) {
    // todo error
}

```

```
const message: V2NIMChatroomMessage = this.chatroomClient.messageCreator.createTextMessage('hello world')

```

```
final message = (await V2NIMChatroomMessageCreator.createTextMessage(
            'test text chatroom message'))
        .data;

```

3. According to your own business needs, configure the message sending parameters, including sending, copying, anti-spam, labeling, spatial location and other configurations. Support setting chat room messages as high-priority messages and whether they are saved on the server.

| Name                                                                                                                      | Is it required?                           | Default value | Explain                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| ------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------- | ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `messageConfig`                                                                                                           | No                                        | -             | Chat room message-related configuration                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `routeConfig`                                                                                                             | No                                        | -             | Message event CC related configuration                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `antispamConfig`                                                                                                          | No                                        | -             | Message anti-spam related configurations, including local anti-spam or security configuration, need to be [opened in the](https://doc.yunxin.163.com/messaging2/client-apis/zU4ODQ3OTc?platform=client)[NetEase Yunxin console.](https://app.yunxin.163.com/global/home)                                                                                                                                                                                                       |
| `clientAntispamEnabled`                                                                                                   | No                                        | false         | Whether to enable local anti-spamOnly valid for text messagesIf you turn on local spam, local anti-spam detection will be carried out when sending messages, and the detection results will be returned after completion:0: The test is passed, and the message can be sent.1: Send the replaced text message2: The detection failed, the message failed to be sent, and the local error code was returned.3: After the message is sent, it will be intercepted by the server. |
| `clientAntispamReplace`                                                                                                   | 若 `clientAntispamEnabled` 为 true 则必填 | ""            | Text replaced after anti-spam hit                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| `receiverIds`                                                                                                             | No                                        | null          | List of targeted message recipient accounts.                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| If the field is not empty, it means that the message is a chat room-oriented message and **is not stored on the server**. |
| `notifyTargetTags`                                                                                                        | No                                        | null          | For the label of message receiving notification, please refer to the [label expression.](https://doc.yunxin.163.com/messaging2/client-apis/TkxNTg3NTk?platform=client#%E6%A0%87%E7%AD%BE%E8%A1%A8%E8%BE%BE%E5%BC%8F)                                                                                                                                                                                                                                                           |
| `locationInfo`                                                                                                            | No                                        | null          | Message space location information configuration                                                                                                                                                                                                                                                                                                                                                                                                                               |

1. **发送方** 调用 `sendMessage` 方法，发送已构建的文本消息。

发送消息时，设置消息发送成功回调参数 `success` 和消息发送失败回调参数 `failure`，监听消息发送是否成功。若消息发送成功，则通过成功回调返回接收消息对象。若消息发送失败，则通过失败回调获取相关错误码。

The sample code is as follows:

安卓

```
// 新建一个聊天室实例，注意：每次 newInstance 都会返回一个新的实例，实际使用中请一个聊天室对应一个 V2NIMChatroomClient 实例，使用中需要临时缓存
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.newInstance();
// 获取聊天室服务
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
// 创建一条文本消息
V2NIMChatroomMessage v2Message = V2NIMChatroomMessageCreator.createTextMessage("xxx");

V2NIMChatroomMessageConfig messageConfig = new V2NIMChatroomMessageConfig();
// 根据实际情况配置
// 设置是否需要在服务端保存历史消息，默认 true
// messageConfig.setHistoryEnabled(true);
// 设置是否是高优先级消息，默认 false
// messageConfig.setHighPriority(false);

V2NIMMessageRouteConfig routeConfig = V2NIMMessageRouteConfig.V2NIMMessageRouteConfigBuilder.builder()
// 根据实际情况配置
// .withRouteEnabled()
// .withRouteEnvironment()
.build();

V2NIMMessageAntispamConfig antispamConfig = V2NIMMessageAntispamConfig.V2NIMMessageAntispamConfigBuilder.builder()
// 根据实际情况配置
// .withAntispamBusinessId()
// .withAntispamCheating()
// .withAntispamCustomMessage()
// .withAntispamEnabled()
// .withAntispamExtension()
.build();

V2NIMSendChatroomMessageParams params = new V2NIMSendChatroomMessageParams();
// 设置消息相关配置
// params.setMessageConfig(messageConfig);
// 设置路由抄送相关配置
// params.setRouteConfig(routeConfig);
// 设置反垃圾相关配置
// params.setAntispamConfig(antispamConfig);
// 是否开启本地反垃圾，默认 false
// params.setClientAntispamEnabled(false);
// 本地反垃圾的替换文本
// params.setClientAntispamReplace("xxx");
//设置聊天室定向消息接收者账号 ID 列表
// params.setReceiverIds(receiverIds);
// 设置消息的目标标签表达式
// params.setNotifyTargetTags("xxx");
// 设置位置信息
// params.setLocationInfo(locationInfo);
v2ChatroomService.sendMessage(v2Message,params,
new V2NIMSuccessCallback<V2NIMSendChatroomMessageResult>() {
    @Override
    public void onSuccess(V2NIMSendChatroomMessageResult result) {
        // 发送成功
    }
},
new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 发送失败
    }
},
new V2NIMProgressCallback() {
    @Override
    public void onProgress(int progress) {
        // 发送进度
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室服务
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:instanceId] getChatroomService];
// 创建一条文本消息
V2NIMChatroomMessage *message = [V2NIMChatroomMessageCreator createTextMessage:@"xxx"];
V2NIMChatroomMessageConfig *messageConfig = [V2NIMChatroomMessageConfig new];
// 根据实际情况配置
// 设置是否需要在服务端保存历史消息，默认 true
// messageConfig.historyEnabled = YES;
// 设置是否是高优先级消息，默认 false
// messageConfig.highPriority = NO;
V2NIMMessageRouteConfig *routeConfig = [V2NIMMessageRouteConfig new];
// 根据实际情况配置
// routeConfig.routeEnabled
// routeConfig.routeEnvironment
V2NIMMessageAntispamConfig *antispamConfig = [V2NIMMessageAntispamConfig new];
// 根据实际情况配置
// antispamConfig.antispamBusinessId
// antispamConfig.antispamCheating
// antispamConfig.antispamCustomMessage
// antispamConfig.antispamEnabled
// antispamConfig.antispamExtension

V2NIMSendChatroomMessageParams *params = [V2NIMSendChatroomMessageParams new];
// 设置消息相关配置
// params.messageConfig = messageConfig;
// 设置路由抄送相关配置
// params.routeConfig = routeConfig;
// 设置反垃圾相关配置
// params.antispamConfig = antispamConfig;
// 是否开启本地反垃圾，默认 false
// params.clientAntispamEnabled = false;
// 本地反垃圾的替换文本
// params.clientAntispamReplace = @"xxx";
// 设置聊天室定向消息接收者账号 ID 列表
// params.receiverIds = receiverIds;
// 设置消息的目标标签表达式
// params.notifyTargetTags = @"xxx";
// 设置位置信息
// params.locationInfo = locationInfo;
[service sendMessage:message
            params:params
            success:^(V2NIMSendChatroomMessageResult *result)
            {
                // 发送成功
            }
            failure:^(V2NIMError *error)
            {
                // 发送失败
            }
            progress:^(NSUInteger progress)
            {
                // 上传进度
            }];

```

macOS/Windows

```
// 创建一条文本消息
auto message = V2NIMChatroomMessageCreator::createTextMessage("hello world");
auto params = V2NIMSendChatroomMessageParams();
// 发送消息
chatroomService.sendMessage(
    message,
    params,
    [](V2NIMSendChatroomMessageResult result) {
        // send message succeeded
    },
    [](V2NIMError error) {
        // send message failed, handle error
    },
    [](uint32_t progress) {
        // upload progress
    });

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomService.sendMessage(
    message,
    // V2NIMSendChatroomMessageParams
    {
        locationInfo: {x: 0, y: 100, z: 0}
    },
    progress: (percentage) => {console.log('上传进度: ' + percentage)}
)

```

Node.js/Electron

```
const message = V2NIMChatroomMessageCreator.createTextMessage('Hello NTES IM')
await chatroomService.sendMessage(message, {})

```

鸿蒙

```
// 准备代发送的消息
const msg: V2NIMChatroomMessage = this.chatroomClient.messageCreator.createTextMessage(text)
// 发送聊天室消息时的参数
const params: V2NIMSendChatroomMessageParams = {
// 配置参数，如
locationInfo: {x: 0, y: 100, z: 0}
}
// 发送进度回调，如上传附件时由该 cb 回调
const progressCb = (percentage: number) => {
this.messageSetProgress(imgMsg, percentage)
console.info(`onUploadProgress: ${JSON.stringify(percentage)}`)
}
// send
const msgRes: V2NIMSendChatroomMessageResult = await this.chatroomClient.chatroomService.sendMessage(msg, params, progressCb)

```

Flutter

```
V2NIMSendChatroomMessageParams params = V2NIMSendChatroomMessageParams();
final messageSender = await chatroomService?.sendMessage(message, params);

```

AndroidiOSmacOS/WindowsWeb/uni-app/小程序Node.js/Electron鸿蒙Flutter

```
// 新建一个聊天室实例，注意：每次 newInstance 都会返回一个新的实例，实际使用中请一个聊天室对应一个 V2NIMChatroomClient 实例，使用中需要临时缓存
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.newInstance();
// 获取聊天室服务
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
// 创建一条文本消息
V2NIMChatroomMessage v2Message = V2NIMChatroomMessageCreator.createTextMessage("xxx");

V2NIMChatroomMessageConfig messageConfig = new V2NIMChatroomMessageConfig();
// 根据实际情况配置
// 设置是否需要在服务端保存历史消息，默认 true
// messageConfig.setHistoryEnabled(true);
// 设置是否是高优先级消息，默认 false
// messageConfig.setHighPriority(false);

V2NIMMessageRouteConfig routeConfig = V2NIMMessageRouteConfig.V2NIMMessageRouteConfigBuilder.builder()
// 根据实际情况配置
// .withRouteEnabled()
// .withRouteEnvironment()
.build();

V2NIMMessageAntispamConfig antispamConfig = V2NIMMessageAntispamConfig.V2NIMMessageAntispamConfigBuilder.builder()
// 根据实际情况配置
// .withAntispamBusinessId()
// .withAntispamCheating()
// .withAntispamCustomMessage()
// .withAntispamEnabled()
// .withAntispamExtension()
.build();

V2NIMSendChatroomMessageParams params = new V2NIMSendChatroomMessageParams();
// 设置消息相关配置
// params.setMessageConfig(messageConfig);
// 设置路由抄送相关配置
// params.setRouteConfig(routeConfig);
// 设置反垃圾相关配置
// params.setAntispamConfig(antispamConfig);
// 是否开启本地反垃圾，默认 false
// params.setClientAntispamEnabled(false);
// 本地反垃圾的替换文本
// params.setClientAntispamReplace("xxx");
//设置聊天室定向消息接收者账号 ID 列表
// params.setReceiverIds(receiverIds);
// 设置消息的目标标签表达式
// params.setNotifyTargetTags("xxx");
// 设置位置信息
// params.setLocationInfo(locationInfo);
v2ChatroomService.sendMessage(v2Message,params,
new V2NIMSuccessCallback<V2NIMSendChatroomMessageResult>() {
    @Override
    public void onSuccess(V2NIMSendChatroomMessageResult result) {
        // 发送成功
    }
},
new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 发送失败
    }
},
new V2NIMProgressCallback() {
    @Override
    public void onProgress(int progress) {
        // 发送进度
    }
});

```

```
// 通过实例 ID 获取聊天室服务
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:instanceId] getChatroomService];
// 创建一条文本消息
V2NIMChatroomMessage *message = [V2NIMChatroomMessageCreator createTextMessage:@"xxx"];
V2NIMChatroomMessageConfig *messageConfig = [V2NIMChatroomMessageConfig new];
// 根据实际情况配置
// 设置是否需要在服务端保存历史消息，默认 true
// messageConfig.historyEnabled = YES;
// 设置是否是高优先级消息，默认 false
// messageConfig.highPriority = NO;
V2NIMMessageRouteConfig *routeConfig = [V2NIMMessageRouteConfig new];
// 根据实际情况配置
// routeConfig.routeEnabled
// routeConfig.routeEnvironment
V2NIMMessageAntispamConfig *antispamConfig = [V2NIMMessageAntispamConfig new];
// 根据实际情况配置
// antispamConfig.antispamBusinessId
// antispamConfig.antispamCheating
// antispamConfig.antispamCustomMessage
// antispamConfig.antispamEnabled
// antispamConfig.antispamExtension

V2NIMSendChatroomMessageParams *params = [V2NIMSendChatroomMessageParams new];
// 设置消息相关配置
// params.messageConfig = messageConfig;
// 设置路由抄送相关配置
// params.routeConfig = routeConfig;
// 设置反垃圾相关配置
// params.antispamConfig = antispamConfig;
// 是否开启本地反垃圾，默认 false
// params.clientAntispamEnabled = false;
// 本地反垃圾的替换文本
// params.clientAntispamReplace = @"xxx";
// 设置聊天室定向消息接收者账号 ID 列表
// params.receiverIds = receiverIds;
// 设置消息的目标标签表达式
// params.notifyTargetTags = @"xxx";
// 设置位置信息
// params.locationInfo = locationInfo;
[service sendMessage:message
            params:params
            success:^(V2NIMSendChatroomMessageResult *result)
            {
                // 发送成功
            }
            failure:^(V2NIMError *error)
            {
                // 发送失败
            }
            progress:^(NSUInteger progress)
            {
                // 上传进度
            }];

```

```
// 创建一条文本消息
auto message = V2NIMChatroomMessageCreator::createTextMessage("hello world");
auto params = V2NIMSendChatroomMessageParams();
// 发送消息
chatroomService.sendMessage(
    message,
    params,
    [](V2NIMSendChatroomMessageResult result) {
        // send message succeeded
    },
    [](V2NIMError error) {
        // send message failed, handle error
    },
    [](uint32_t progress) {
        // upload progress
    });

```

```
await chatroom.V2NIMChatroomService.sendMessage(
    message,
    // V2NIMSendChatroomMessageParams
    {
        locationInfo: {x: 0, y: 100, z: 0}
    },
    progress: (percentage) => {console.log('上传进度: ' + percentage)}
)

```

```
const message = V2NIMChatroomMessageCreator.createTextMessage('Hello NTES IM')
await chatroomService.sendMessage(message, {})

```

```
// 准备代发送的消息
const msg: V2NIMChatroomMessage = this.chatroomClient.messageCreator.createTextMessage(text)
// 发送聊天室消息时的参数
const params: V2NIMSendChatroomMessageParams = {
// 配置参数，如
locationInfo: {x: 0, y: 100, z: 0}
}
// 发送进度回调，如上传附件时由该 cb 回调
const progressCb = (percentage: number) => {
this.messageSetProgress(imgMsg, percentage)
console.info(`onUploadProgress: ${JSON.stringify(percentage)}`)
}
// send
const msgRes: V2NIMSendChatroomMessageResult = await this.chatroomClient.chatroomService.sendMessage(msg, params, progressCb)

```

```
V2NIMSendChatroomMessageParams params = V2NIMSendChatroomMessageParams();
final messageSender = await chatroomService?.sendMessage(message, params);

```

2. **接收方** 通过 `onReceiveMessages` 回调收到聊天室消息。

If it is a rich media message (picture/audio/video/file message), you need to download rich media message resources **by yourself**. 3. （可选）如发送富媒体消息，发送后可调用 `cancelMessageAttachmentUpload` 方法取消附件的上传。

If the attachment has been uploaded successfully, the operation will fail. IF THE UPLOAD OF THE ATTACHMENT IS SUCCESSFULLY CANCELED, THE CORRESPONDING MESSAGE WILL FAIL TO BE SENT. THE MESSAGE STATUS IS `SENDING_STATE_FAILED`, AND THE ATTACHMENT UPLOAD STATUS IS `ATTACHMENT_UPLOAD_STATE_FAILED`.

安卓

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
v2ChatroomService.cancelMessageAttachmentUpload(v2Message, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // 取消成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 取消失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室服务
id<V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];
[service cancelMessageAttachmentUpload:message
                            success:^{
                                // 取消成功
                            }
                            failure:^(V2NIMError *error) {
                                // 取消失败
                            }];

```

macOS/Windows

```
V2NIMChatroomMessage message;
// ...
chatroomService.cancelMessageAttachmentUpload(
    message,
    []() {
        // cancel message attachment upload succeeded
    },
    [](V2NIMError error) {
        // cancel message attachment upload failed, handle error
    });

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomService.cancelMessageAttachmentUpload(message)

```

Node.js/Electron

```
await chatroomService.cancelMessageAttachmentUpload(message)

```

鸿蒙

```
await this.chatroomClient.chatroomService.cancelMessageAttachmentUpload(message)

```

Flutter

```
chatroomService?.cancelMessageAttachmentUpload(message);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
v2ChatroomService.cancelMessageAttachmentUpload(v2Message, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // 取消成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 取消失败
    }
});

```

```
// 通过实例 ID 获取聊天室服务
id<V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];
[service cancelMessageAttachmentUpload:message
                            success:^{
                                // 取消成功
                            }
                            failure:^(V2NIMError *error) {
                                // 取消失败
                            }];

```

```
V2NIMChatroomMessage message;
// ...
chatroomService.cancelMessageAttachmentUpload(
    message,
    []() {
        // cancel message attachment upload succeeded
    },
    [](V2NIMError error) {
        // cancel message attachment upload failed, handle error
    });

```

```
await chatroom.V2NIMChatroomService.cancelMessageAttachmentUpload(message)

```

```
await chatroomService.cancelMessageAttachmentUpload(message)

```

```
await this.chatroomClient.chatroomService.cancelMessageAttachmentUpload(message)

```

```
chatroomService?.cancelMessageAttachmentUpload(message);

```

## Chat room space location message

The spatial location message function is used to send messages to chat room users within the specified range under the chat room space coordinate scenario, such as sending messages to each other in the specified range of the game map.

### Preset the location information of the chat room space

在调用 `enter` 方法进入聊天室时，您可以通过配置进入聊天室参数中的空间位置配置字段 `locationConfig`，来预设进入聊天室时的初始空间坐标位置，并且可以订阅接收指定距离内的消息。

| Name           | Is it required? | Explain                                                                              |
| -------------- | --------------- | ------------------------------------------------------------------------------------ |
| `locationInfo` | Yes             | Chat room space location coordinate information, configure x, y, z coordinate values |
| `distance`     | Yes             | The distance of subscribing to chat room messages                                    |

`enter`For other parameter settings and sample codes, please refer to the [chat room login document](https://doc.yunxin.163.com/messaging2/client-apis/DI2NDc1NzQ?platform=client).

### Send the chat room space location message

调用 `sendMessage` 方法发送聊天室消息时，您可以通过配置聊天室消息发送配置参数中的空间位置信息字段 `locationInfo`，设置该消息的空间坐标信息属性。

| Name | Type   | Is it required? | Description                   |
| ---- | ------ | --------------- | ----------------------------- |
| `x`  | double | Yes             | Chat room space x coordinates |
| `y`  | double | Yes             | Chat room space y coordinates |
| `z`  | double | Yes             | Chat room space z coordinates |

The sample code is as follows:

安卓

```
// 新建一个聊天室实例，注意：每次 newInstance 都会返回一个新的实例，实际使用中请一个聊天室对应一个 V2NIMChatroomClient 实例，使用中需要临时缓存
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.newInstance();
// 获取聊天室服务
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
// 创建一条文本消息
V2NIMChatroomMessage v2Message = V2NIMChatroomMessageCreator.createTextMessage("xxx");

V2NIMChatroomMessageConfig messageConfig = new V2NIMChatroomMessageConfig();
// 根据实际情况配置
// 设置是否需要在服务端保存历史消息，默认 true
// messageConfig.setHistoryEnabled(true);
// 设置是否是高优先级消息，默认 false
// messageConfig.setHighPriority(false);

V2NIMMessageRouteConfig routeConfig = V2NIMMessageRouteConfig.V2NIMMessageRouteConfigBuilder.builder()
// 根据实际情况配置
// .withRouteEnabled()
// .withRouteEnvironment()
.build();

V2NIMMessageAntispamConfig antispamConfig = V2NIMMessageAntispamConfig.V2NIMMessageAntispamConfigBuilder.builder()
// 根据实际情况配置
// .withAntispamBusinessId()
// .withAntispamCheating()
// .withAntispamCustomMessage()
// .withAntispamEnabled()
// .withAntispamExtension()
.build();

V2NIMSendChatroomMessageParams params = new V2NIMSendChatroomMessageParams();
// 设置位置信息
// params.setLocationInfo(locationInfo);
v2ChatroomService.sendMessage(v2Message,params,
new V2NIMSuccessCallback<V2NIMSendChatroomMessageResult>() {
    @Override
    public void onSuccess(V2NIMSendChatroomMessageResult result) {
        // 发送成功
    }
},
new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 发送失败
    }
},
new V2NIMProgressCallback() {
    @Override
    public void onProgress(int progress) {
        // 发送进度
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室服务
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:instanceId] getChatroomService];
// 创建一条文本消息
V2NIMChatroomMessage *message = [V2NIMChatroomMessageCreator createTextMessage:@"xxx"];
V2NIMChatroomMessageConfig *messageConfig = [V2NIMChatroomMessageConfig new];
// 根据实际情况配置
// 设置是否需要在服务端保存历史消息，默认 true
// messageConfig.historyEnabled = YES;
// 设置是否是高优先级消息，默认 false
// messageConfig.highPriority = NO;
V2NIMMessageRouteConfig *routeConfig = [V2NIMMessageRouteConfig new];
// 根据实际情况配置
// routeConfig.routeEnabled
// routeConfig.routeEnvironment
V2NIMMessageAntispamConfig *antispamConfig = [V2NIMMessageAntispamConfig new];
// 根据实际情况配置
// antispamConfig.antispamBusinessId
// antispamConfig.antispamCheating
// antispamConfig.antispamCustomMessage
// antispamConfig.antispamEnabled
// antispamConfig.antispamExtension

V2NIMSendChatroomMessageParams *params = [V2NIMSendChatroomMessageParams new];
// 设置位置信息
// params.locationInfo = locationInfo;
[service sendMessage:message
            params:params
            success:^(V2NIMSendChatroomMessageResult *result)
            {
                // 发送成功
            }
            failure:^(V2NIMError *error)
            {
                // 发送失败
            }
            progress:^(NSUInteger progress)
            {
                // 上传进度
            }];

```

macOS/Windows

```
// 创建一条文本消息
auto message = V2NIMChatroomMessageCreator::createTextMessage("hello world");
auto params = V2NIMSendChatroomMessageParams();
// 发送消息
chatroomService.sendMessage(
    message,
    params,
    [](V2NIMSendChatroomMessageResult result) {
        // send message succeeded
    },
    [](V2NIMError error) {
        // send message failed, handle error
    },
    [](uint32_t progress) {
        // upload progress
    });

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomService.sendMessage(
    message,
    // V2NIMSendChatroomMessageParams
    {
        locationInfo: {x: 0, y: 100, z: 0}
    },
    progress: (percentage) => {console.log('上传进度: ' + percentage)}
)

```

Node.js/Electron

```
const message = V2NIMChatroomMessageCreator.createTextMessage('Hello NTES IM')
await chatroomService.sendMessage(message, {})

```

鸿蒙

```
// 准备代发送的消息
const msg: V2NIMChatroomMessage = this.chatroomClient.messageCreator.createTextMessage(text)
// 发送聊天室消息时的参数
const params: V2NIMSendChatroomMessageParams = {
// 配置参数，如
locationInfo: {x: 0, y: 100, z: 0}
}
// 发送进度回调，如上传附件时由该 cb 回调
const progressCb = (percentage: number) => {
this.messageSetProgress(imgMsg, percentage)
console.info(`onUploadProgress: ${JSON.stringify(percentage)}`)
}
// send
const msgRes: V2NIMSendChatroomMessageResult = await this.chatroomClient.chatroomService.sendMessage(msg, params, progressCb)

```

Flutter

```
V2NIMSendChatroomMessageParams params = V2NIMSendChatroomMessageParams();
      params.locationInfo = V2NIMLocationInfo(
        x: 100,y: 100,z: 100,
      );
      final messageSender = await chatroomService?.sendMessage(message, params);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
// 新建一个聊天室实例，注意：每次 newInstance 都会返回一个新的实例，实际使用中请一个聊天室对应一个 V2NIMChatroomClient 实例，使用中需要临时缓存
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.newInstance();
// 获取聊天室服务
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
// 创建一条文本消息
V2NIMChatroomMessage v2Message = V2NIMChatroomMessageCreator.createTextMessage("xxx");

V2NIMChatroomMessageConfig messageConfig = new V2NIMChatroomMessageConfig();
// 根据实际情况配置
// 设置是否需要在服务端保存历史消息，默认 true
// messageConfig.setHistoryEnabled(true);
// 设置是否是高优先级消息，默认 false
// messageConfig.setHighPriority(false);

V2NIMMessageRouteConfig routeConfig = V2NIMMessageRouteConfig.V2NIMMessageRouteConfigBuilder.builder()
// 根据实际情况配置
// .withRouteEnabled()
// .withRouteEnvironment()
.build();

V2NIMMessageAntispamConfig antispamConfig = V2NIMMessageAntispamConfig.V2NIMMessageAntispamConfigBuilder.builder()
// 根据实际情况配置
// .withAntispamBusinessId()
// .withAntispamCheating()
// .withAntispamCustomMessage()
// .withAntispamEnabled()
// .withAntispamExtension()
.build();

V2NIMSendChatroomMessageParams params = new V2NIMSendChatroomMessageParams();
// 设置位置信息
// params.setLocationInfo(locationInfo);
v2ChatroomService.sendMessage(v2Message,params,
new V2NIMSuccessCallback<V2NIMSendChatroomMessageResult>() {
    @Override
    public void onSuccess(V2NIMSendChatroomMessageResult result) {
        // 发送成功
    }
},
new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 发送失败
    }
},
new V2NIMProgressCallback() {
    @Override
    public void onProgress(int progress) {
        // 发送进度
    }
});

```

```
// 通过实例 ID 获取聊天室服务
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:instanceId] getChatroomService];
// 创建一条文本消息
V2NIMChatroomMessage *message = [V2NIMChatroomMessageCreator createTextMessage:@"xxx"];
V2NIMChatroomMessageConfig *messageConfig = [V2NIMChatroomMessageConfig new];
// 根据实际情况配置
// 设置是否需要在服务端保存历史消息，默认 true
// messageConfig.historyEnabled = YES;
// 设置是否是高优先级消息，默认 false
// messageConfig.highPriority = NO;
V2NIMMessageRouteConfig *routeConfig = [V2NIMMessageRouteConfig new];
// 根据实际情况配置
// routeConfig.routeEnabled
// routeConfig.routeEnvironment
V2NIMMessageAntispamConfig *antispamConfig = [V2NIMMessageAntispamConfig new];
// 根据实际情况配置
// antispamConfig.antispamBusinessId
// antispamConfig.antispamCheating
// antispamConfig.antispamCustomMessage
// antispamConfig.antispamEnabled
// antispamConfig.antispamExtension

V2NIMSendChatroomMessageParams *params = [V2NIMSendChatroomMessageParams new];
// 设置位置信息
// params.locationInfo = locationInfo;
[service sendMessage:message
            params:params
            success:^(V2NIMSendChatroomMessageResult *result)
            {
                // 发送成功
            }
            failure:^(V2NIMError *error)
            {
                // 发送失败
            }
            progress:^(NSUInteger progress)
            {
                // 上传进度
            }];

```

```
// 创建一条文本消息
auto message = V2NIMChatroomMessageCreator::createTextMessage("hello world");
auto params = V2NIMSendChatroomMessageParams();
// 发送消息
chatroomService.sendMessage(
    message,
    params,
    [](V2NIMSendChatroomMessageResult result) {
        // send message succeeded
    },
    [](V2NIMError error) {
        // send message failed, handle error
    },
    [](uint32_t progress) {
        // upload progress
    });

```

```
await chatroom.V2NIMChatroomService.sendMessage(
    message,
    // V2NIMSendChatroomMessageParams
    {
        locationInfo: {x: 0, y: 100, z: 0}
    },
    progress: (percentage) => {console.log('上传进度: ' + percentage)}
)

```

```
const message = V2NIMChatroomMessageCreator.createTextMessage('Hello NTES IM')
await chatroomService.sendMessage(message, {})

```

```
// 准备代发送的消息
const msg: V2NIMChatroomMessage = this.chatroomClient.messageCreator.createTextMessage(text)
// 发送聊天室消息时的参数
const params: V2NIMSendChatroomMessageParams = {
// 配置参数，如
locationInfo: {x: 0, y: 100, z: 0}
}
// 发送进度回调，如上传附件时由该 cb 回调
const progressCb = (percentage: number) => {
this.messageSetProgress(imgMsg, percentage)
console.info(`onUploadProgress: ${JSON.stringify(percentage)}`)
}
// send
const msgRes: V2NIMSendChatroomMessageResult = await this.chatroomClient.chatroomService.sendMessage(msg, params, progressCb)

```

```
V2NIMSendChatroomMessageParams params = V2NIMSendChatroomMessageParams();
      params.locationInfo = V2NIMLocationInfo(
        x: 100,y: 100,z: 100,
      );
      final messageSender = await chatroomService?.sendMessage(message, params);

```

### Update the location information of the chat room space

您可以调用 `updateChatroomLocationInfo` 方法更新当前在聊天室的空间坐标位置，及消息订阅范围。

| Name           | Is it required? | Description                                                                          |
| -------------- | --------------- | ------------------------------------------------------------------------------------ |
| `locationInfo` | Yes             | Chat room space location coordinate information, configure x, y, z coordinate values |
| `distance`     | Yes             | The distance of subscribing chat room messages based on spatial location             |

The sample code is as follows:

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomLocationConfig locationConfig = new V2NIMChatroomLocationConfig();
V2NIMLocationInfo locationInfo = new V2NIMLocationInfo(0.0, 0.0, 0,0);

locationConfig.setLocationInfo(locationInfo);
locationConfig.setDistance(100);
// 以上两个字段必填，否则会返回参数错误

v2ChatroomService.updateChatroomLocationInfo(locationConfig, new V2NIMSuccessCallback<Void>() {
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

V2NIMChatroomLocationConfig *locationConfig = [[V2NIMChatroomLocationConfig alloc] init];
V2NIMLocationInfo *locationInfo = [[V2NIMLocationInfo alloc] init];

locationConfig.locationInfo = locationInfo;
locationConfig.distance = 100;
// 以上两个字段必填，否则会返回参数错误
[service updateChatroomLocationInfo:locationConfig
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
V2NIMChatroomLocationConfig locationConfig;
locationConfig.locationInfo.x = 1.0;
locationConfig.locationInfo.y = 1.0;
locationConfig.locationInfo.z = 1.0;
locationConfig.distance = 100;
chatroomService.updateChatroomLocationInfo(
    locationConfig,
    []() {
        // update chatroom location info succeeded
    },
    [](V2NIMError error) {
        // update chatroom location info failed, handle error
    });

```

Web/uni-app/小程序

```
await chatroomV2.V2NIMChatroomService.updateChatroomLocationInfo(
{
    "locationInfo": {
    "x": 33,
    "y": 44,
    "z": 55
    },
    "distance": 77
}
)

```

Node.js/Electron

```
await chatroomService.updateChatroomLocationInfo({
    latitude: 30.5,
    longitude: 120.5
})

```

鸿蒙

```
await this.chatroomClient.chatroomService.updateChatroomLocationInfo(
{
    "locationInfo": {
    "x": 33,
    "y": 44,
    "z": 55
    },
    "distance": 77
}
)

```

Flutter

```
final chatroomService = chatroomClient?.getChatroomService();
final params = V2NIMChatroomLocationConfig(
        distance: 100,
        locationInfo:  V2NIMLocationInfo(
          x: 100,y: 100,z: 100,
        )
      );
var result = await chatroomService?.updateChatroomLocationInfo(params);

```

AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/Electron鸿蒙Flutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomLocationConfig locationConfig = new V2NIMChatroomLocationConfig();
V2NIMLocationInfo locationInfo = new V2NIMLocationInfo(0.0, 0.0, 0,0);

locationConfig.setLocationInfo(locationInfo);
locationConfig.setDistance(100);
// 以上两个字段必填，否则会返回参数错误

v2ChatroomService.updateChatroomLocationInfo(locationConfig, new V2NIMSuccessCallback<Void>() {
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

V2NIMChatroomLocationConfig *locationConfig = [[V2NIMChatroomLocationConfig alloc] init];
V2NIMLocationInfo *locationInfo = [[V2NIMLocationInfo alloc] init];

locationConfig.locationInfo = locationInfo;
locationConfig.distance = 100;
// 以上两个字段必填，否则会返回参数错误
[service updateChatroomLocationInfo:locationConfig
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
V2NIMChatroomLocationConfig locationConfig;
locationConfig.locationInfo.x = 1.0;
locationConfig.locationInfo.y = 1.0;
locationConfig.locationInfo.z = 1.0;
locationConfig.distance = 100;
chatroomService.updateChatroomLocationInfo(
    locationConfig,
    []() {
        // update chatroom location info succeeded
    },
    [](V2NIMError error) {
        // update chatroom location info failed, handle error
    });

```

```
await chatroomV2.V2NIMChatroomService.updateChatroomLocationInfo(
{
    "locationInfo": {
    "x": 33,
    "y": 44,
    "z": 55
    },
    "distance": 77
}
)

```

```
await chatroomService.updateChatroomLocationInfo({
    latitude: 30.5,
    longitude: 120.5
})

```

```
await this.chatroomClient.chatroomService.updateChatroomLocationInfo(
{
    "locationInfo": {
    "x": 33,
    "y": 44,
    "z": 55
    },
    "distance": 77
}
)

```

```
final chatroomService = chatroomClient?.getChatroomService();
final params = V2NIMChatroomLocationConfig(
        distance: 100,
        locationInfo:  V2NIMLocationInfo(
          x: 100,y: 100,z: 100,
        )
      );
var result = await chatroomService?.updateChatroomLocationInfo(params);

```

## Chat room-oriented message

The chat room-oriented message function supports sending chat room messages to designated objects in the chat room, not everyone in the chat room.

调用 `sendMessage` 方法发送聊天室消息时，您可以通过配置聊天室消息发送配置参数中的字段 `receiverIds`，指定接收定向消息的聊天室用户列表。最多支持指定 100 个用户。

Directed messages do not support saving offline messages (server-side storage). If the recipient is offline when sending the directional message, the directional message cannot be received after subsequent reconnection.

The sample code is as follows:

安卓

```
// 新建一个聊天室实例，注意：每次 newInstance 都会返回一个新的实例，实际使用中请一个聊天室对应一个 V2NIMChatroomClient 实例，使用中需要临时缓存
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.newInstance();
// 获取聊天室服务
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
// 创建一条文本消息
V2NIMChatroomMessage v2Message = V2NIMChatroomMessageCreator.createTextMessage("xxx");

V2NIMChatroomMessageConfig messageConfig = new V2NIMChatroomMessageConfig();
// 根据实际情况配置
// 设置是否需要在服务端保存历史消息，默认 true
// messageConfig.setHistoryEnabled(true);
// 设置是否是高优先级消息，默认 false
// messageConfig.setHighPriority(false);

V2NIMMessageRouteConfig routeConfig = V2NIMMessageRouteConfig.V2NIMMessageRouteConfigBuilder.builder()
// 根据实际情况配置
// .withRouteEnabled()
// .withRouteEnvironment()
.build();

V2NIMMessageAntispamConfig antispamConfig = V2NIMMessageAntispamConfig.V2NIMMessageAntispamConfigBuilder.builder()
// 根据实际情况配置
// .withAntispamBusinessId()
// .withAntispamCheating()
// .withAntispamCustomMessage()
// .withAntispamEnabled()
// .withAntispamExtension()
.build();

V2NIMSendChatroomMessageParams params = new V2NIMSendChatroomMessageParams();
// params.setReceiverIds(receiverIds);
v2ChatroomService.sendMessage(v2Message,params,
new V2NIMSuccessCallback<V2NIMSendChatroomMessageResult>() {
    @Override
    public void onSuccess(V2NIMSendChatroomMessageResult result) {
        // 发送成功
    }
},
new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 发送失败
    }
},
new V2NIMProgressCallback() {
    @Override
    public void onProgress(int progress) {
        // 发送进度
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室服务
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:instanceId] getChatroomService];
// 创建一条文本消息
V2NIMChatroomMessage *message = [V2NIMChatroomMessageCreator createTextMessage:@"xxx"];
V2NIMChatroomMessageConfig *messageConfig = [V2NIMChatroomMessageConfig new];
// 根据实际情况配置
// 设置是否需要在服务端保存历史消息，默认 true
// messageConfig.historyEnabled = YES;
// 设置是否是高优先级消息，默认 false
// messageConfig.highPriority = NO;
V2NIMMessageRouteConfig *routeConfig = [V2NIMMessageRouteConfig new];
// 根据实际情况配置
// routeConfig.routeEnabled
// routeConfig.routeEnvironment
V2NIMMessageAntispamConfig *antispamConfig = [V2NIMMessageAntispamConfig new];
// 根据实际情况配置
// antispamConfig.antispamBusinessId
// antispamConfig.antispamCheating
// antispamConfig.antispamCustomMessage
// antispamConfig.antispamEnabled
// antispamConfig.antispamExtension

V2NIMSendChatroomMessageParams *params = [V2NIMSendChatroomMessageParams new];
// 设置聊天室定向消息接收者账号 ID 列表
// params.receiverIds = receiverIds;
[service sendMessage:message
            params:params
            success:^(V2NIMSendChatroomMessageResult *result)
            {
                // 发送成功
            }
            failure:^(V2NIMError *error)
            {
                // 发送失败
            }
            progress:^(NSUInteger progress)
            {
                // 上传进度
            }];

```

macOS/Windows

```
// 创建一条文本消息
auto message = V2NIMChatroomMessageCreator::createTextMessage("hello world");
auto params = V2NIMSendChatroomMessageParams();
// 发送消息
chatroomService.sendMessage(
    message,
    params,
    [](V2NIMSendChatroomMessageResult result) {
        // send message succeeded
    },
    [](V2NIMError error) {
        // send message failed, handle error
    },
    [](uint32_t progress) {
        // upload progress
    });

```

Web/uni-app/小程序

```
await chatroom.V2NIMChatroomService.sendMessage(
    message,
    // V2NIMSendChatroomMessageParams
    {
        receiverIds: ["account1", "account2", "account3"]
    },
    progress: (percentage) => {console.log('上传进度: ' + percentage)}
)

```

Node.js/Electron

```
const message = V2NIMChatroomMessageCreator.createTextMessage('Hello NTES IM')
await chatroomService.sendMessage(message, {})

```

鸿蒙

```
// 准备代发送的消息
const msg: V2NIMChatroomMessage = this.chatroomClient.messageCreator.createTextMessage(text)
// 发送聊天室消息时的参数
const params: V2NIMSendChatroomMessageParams = {
  // 配置参数，如
  locationInfo: {x: 0, y: 100, z: 0}
}
// 发送进度回调，如上传附件时由该 cb 回调
const progressCb = (percentage: number) => {
  this.messageSetProgress(imgMsg, percentage)
  console.info(`onUploadProgress: ${JSON.stringify(percentage)}`)
}
// send
const msgRes: V2NIMSendChatroomMessageResult = await this.chatroomClient.chatroomService.sendMessage(msg, params, progressCb)

```

Flutter

```
V2NIMSendChatroomMessageParams params = V2NIMSendChatroomMessageParams();
params.receiverIds = ['user1', 'user2'];
final messageSender = await chatroomService?.sendMessage(message, params);

```

AndroidiOSmacOS/WindowsWeb/uni-app/小程序Node.js/ElectronHongmengFlutter

```
// 新建一个聊天室实例，注意：每次 newInstance 都会返回一个新的实例，实际使用中请一个聊天室对应一个 V2NIMChatroomClient 实例，使用中需要临时缓存
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.newInstance();
// 获取聊天室服务
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();
// 创建一条文本消息
V2NIMChatroomMessage v2Message = V2NIMChatroomMessageCreator.createTextMessage("xxx");

V2NIMChatroomMessageConfig messageConfig = new V2NIMChatroomMessageConfig();
// 根据实际情况配置
// 设置是否需要在服务端保存历史消息，默认 true
// messageConfig.setHistoryEnabled(true);
// 设置是否是高优先级消息，默认 false
// messageConfig.setHighPriority(false);

V2NIMMessageRouteConfig routeConfig = V2NIMMessageRouteConfig.V2NIMMessageRouteConfigBuilder.builder()
// 根据实际情况配置
// .withRouteEnabled()
// .withRouteEnvironment()
.build();

V2NIMMessageAntispamConfig antispamConfig = V2NIMMessageAntispamConfig.V2NIMMessageAntispamConfigBuilder.builder()
// 根据实际情况配置
// .withAntispamBusinessId()
// .withAntispamCheating()
// .withAntispamCustomMessage()
// .withAntispamEnabled()
// .withAntispamExtension()
.build();

V2NIMSendChatroomMessageParams params = new V2NIMSendChatroomMessageParams();
// params.setReceiverIds(receiverIds);
v2ChatroomService.sendMessage(v2Message,params,
new V2NIMSuccessCallback<V2NIMSendChatroomMessageResult>() {
    @Override
    public void onSuccess(V2NIMSendChatroomMessageResult result) {
        // 发送成功
    }
},
new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        // 发送失败
    }
},
new V2NIMProgressCallback() {
    @Override
    public void onProgress(int progress) {
        // 发送进度
    }
});

```

```
// 通过实例 ID 获取聊天室服务
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:instanceId] getChatroomService];
// 创建一条文本消息
V2NIMChatroomMessage *message = [V2NIMChatroomMessageCreator createTextMessage:@"xxx"];
V2NIMChatroomMessageConfig *messageConfig = [V2NIMChatroomMessageConfig new];
// 根据实际情况配置
// 设置是否需要在服务端保存历史消息，默认 true
// messageConfig.historyEnabled = YES;
// 设置是否是高优先级消息，默认 false
// messageConfig.highPriority = NO;
V2NIMMessageRouteConfig *routeConfig = [V2NIMMessageRouteConfig new];
// 根据实际情况配置
// routeConfig.routeEnabled
// routeConfig.routeEnvironment
V2NIMMessageAntispamConfig *antispamConfig = [V2NIMMessageAntispamConfig new];
// 根据实际情况配置
// antispamConfig.antispamBusinessId
// antispamConfig.antispamCheating
// antispamConfig.antispamCustomMessage
// antispamConfig.antispamEnabled
// antispamConfig.antispamExtension

V2NIMSendChatroomMessageParams *params = [V2NIMSendChatroomMessageParams new];
// 设置聊天室定向消息接收者账号 ID 列表
// params.receiverIds = receiverIds;
[service sendMessage:message
            params:params
            success:^(V2NIMSendChatroomMessageResult *result)
            {
                // 发送成功
            }
            failure:^(V2NIMError *error)
            {
                // 发送失败
            }
            progress:^(NSUInteger progress)
            {
                // 上传进度
            }];

```

```
// 创建一条文本消息
auto message = V2NIMChatroomMessageCreator::createTextMessage("hello world");
auto params = V2NIMSendChatroomMessageParams();
// 发送消息
chatroomService.sendMessage(
    message,
    params,
    [](V2NIMSendChatroomMessageResult result) {
        // send message succeeded
    },
    [](V2NIMError error) {
        // send message failed, handle error
    },
    [](uint32_t progress) {
        // upload progress
    });

```

```
await chatroom.V2NIMChatroomService.sendMessage(
    message,
    // V2NIMSendChatroomMessageParams
    {
        receiverIds: ["account1", "account2", "account3"]
    },
    progress: (percentage) => {console.log('上传进度: ' + percentage)}
)

```

```
const message = V2NIMChatroomMessageCreator.createTextMessage('Hello NTES IM')
await chatroomService.sendMessage(message, {})

```

```
// 准备代发送的消息
const msg: V2NIMChatroomMessage = this.chatroomClient.messageCreator.createTextMessage(text)
// 发送聊天室消息时的参数
const params: V2NIMSendChatroomMessageParams = {
  // 配置参数，如
  locationInfo: {x: 0, y: 100, z: 0}
}
// 发送进度回调，如上传附件时由该 cb 回调
const progressCb = (percentage: number) => {
  this.messageSetProgress(imgMsg, percentage)
  console.info(`onUploadProgress: ${JSON.stringify(percentage)}`)
}
// send
const msgRes: V2NIMSendChatroomMessageResult = await this.chatroomClient.chatroomService.sendMessage(msg, params, progressCb)

```

```
V2NIMSendChatroomMessageParams params = V2NIMSendChatroomMessageParams();
params.receiverIds = ['user1', 'user2'];
final messageSender = await chatroomService?.sendMessage(message, params);

```

## Chat room notification message

Some operations in the chat room will generate a chat room notification message.

### Chat room notification message type

At present, when the following events occur, a notification message will be generated:

| Chat room notification message enumeration | Corresponding value | Description                                                                                                                                                                                                                                                                                                                                                                          |
| ------------------------------------------ | ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| MEMBER_ENTER                               | zero                | Members can enter the chat room through the NetEase Yunxin console. Whether the [sub-function configuration of the chat room](https://doc.yunxin.163.com/messaging2/guide/DUyMzAxNzg?platform=client#%E9%85%8D%E7%BD%AE%E8%81%8A%E5%A4%A9%E5%AE%A4%E5%AD%90%E5%8A%9F%E8%83%BD)is turned on. The **chat room user's entry and exit message system is sent**(not turned on by default) |
| MEMBER_EXIT                                | 1                   | Members can exit the chat room through the NetEase Cloud Trust console. Whether the [chat room sub-function configuration](https://doc.yunxin.163.com/messaging2/guide/DUyMzAxNzg?platform=client#%E9%85%8D%E7%BD%AE%E8%81%8A%E5%A4%A9%E5%AE%A4%E5%AD%90%E5%8A%9F%E8%83%BD)is turned on. The **chat room user's entry and exit message system is sent**(not turned on by default)    |
| MEMBER_BLOCK_ADDED                         | 2                   | Chat room members are added to the blacklist.                                                                                                                                                                                                                                                                                                                                        |
| MEMBER_BLOCK_REMOVED                       | 3                   | Chat room members have been removed from the blacklist.                                                                                                                                                                                                                                                                                                                              |
| MEMBER_CHAT_BANNED_ADDED                   | four                | Chat room members were banned.                                                                                                                                                                                                                                                                                                                                                       |
| MEMBER_CHAT_BANNED_REMOVED                 | 5                   | Chat room members were banned.                                                                                                                                                                                                                                                                                                                                                       |
| ROOM_INFO_UPDATED                          | 6                   | Chat room information update                                                                                                                                                                                                                                                                                                                                                         |
| MEMBER_KICKED                              | 7                   | The chat room members were kicked.                                                                                                                                                                                                                                                                                                                                                   |
| MEMBER_TEMP_CHAT_BANNED_ADDED              | 8                   | Chat room members were temporarily banned.                                                                                                                                                                                                                                                                                                                                           |
| MEMBER_TEMP_CHAT_BANNED_REMOVED            | 9                   | Chat room members were temporarily banned.                                                                                                                                                                                                                                                                                                                                           |
| MEMBER_INFO_UPDATED                        | 10                  | Chat room member information update (nick/avatar/extension)                                                                                                                                                                                                                                                                                                                          |
| QUEUE_CHANGE                               | 11                  | Chat room queue change                                                                                                                                                                                                                                                                                                                                                               |
| CHAT_BANNED                                | 12                  | The chat room is in a banned state.                                                                                                                                                                                                                                                                                                                                                  |
| CHAT_BANNED_REMOVED                        | 13                  | The chat room is in a non-banned state.                                                                                                                                                                                                                                                                                                                                              |
| TAG_TEMP_CHAT_BANNED_ADDED                 | 14                  | Chat room tag members were temporarily banned                                                                                                                                                                                                                                                                                                                                        |
| TAG_TEMP_CHAT_BANNED_REMOVED               | 15                  | Chat room tag members were temporarily banned.                                                                                                                                                                                                                                                                                                                                       |
| MESSAGE_REVOKE                             | 16                  | Chat room message withdrawal                                                                                                                                                                                                                                                                                                                                                         |
| TAGS_UPDATE                                | 17                  | Chat room label update                                                                                                                                                                                                                                                                                                                                                               |
| ROLE_UPDATE                                | 18                  | Chat room member role update                                                                                                                                                                                                                                                                                                                                                         |

Support to set whether to issue notifications for members entering and leaving the chat room:

- **Application level**:
  - NetEase Yunxin console, whether the [sub-function configuration of the chat room](https://doc.yunxin.163.com/messaging2/guide/DUyMzAxNzg?platform=client#%E9%85%8D%E7%BD%AE%E8%81%8A%E5%A4%A9%E5%AE%A4%E5%AD%90%E5%8A%9F%E8%83%BD)is turned on, the **chat room user's entry and exit message system is issued**(not turned on by default)
  - NetEase Yunxin console, whether the [sub-function configuration of the chat room](https://doc.yunxin.163.com/messaging2/guide/DUyMzAxNzg?platform=client#%E9%85%8D%E7%BD%AE%E8%81%8A%E5%A4%A9%E5%AE%A4%E5%AD%90%E5%8A%9F%E8%83%BD)is turned on, **chat room user entry and exit message history storage**(turned on by default)
- **Single chat room**: Call the new version of the server API [to turn on/off the event notification of entering and leaving the chat room](https://doc.yunxin.163.com/messaging2/server-apis/DEyMDExMTU?platform=server)

### Chat room notification message analysis

All chat room notification messages are encapsulated in the form of `V2NIMChatroomMessage`. The analysis of the chat room notification message is as follows:

The steps for parsing chat room notification messages are as follows:

1. Determine whether the chat room notification message (`V2NIM_MESSAGE_TYPE_NOTIFICATION`) through `V2NIMChatroomMessage.messageType`
2. 将 `V2NIMChatroomMessage.attachment` 附件对象强类型转换为 `V2NIMChatroomNotificationAttachment`。
3. 通过 `V2NIMChatroomNotificationAttachment.type` 获取具体的通知类型 `V2NIMChatroomMessageNotificationType`。
4. Construct the corresponding display information according to the type of the corresponding `V2NIMChatroomMessageNotificationType`:

- **`V2NIMChatroomNotificationAttachment.operatorId`**: The operator ID of the event indicates who took the initiative to perform the operation.
- **`V2NIMChatroomNotificationAttachment.targetIds`**: The list of the operator ID of the event indicates the bearer of the operation. ( Event type 0, 1, 5, 6, 7, 8, 9, 10 with this field)

`V2NIMChatroomNotificationAttachment`Parameter description:

| 名称                    | Type                                   | Is it required? | Default value | Description                       |
| ----------------------- | -------------------------------------- | --------------- | ------------- | --------------------------------- |
| `type`                  | `V2NIMChatroomMessageNotificationType` | Yes             | -             | Notification type                 |
| `targetIds`             | list                                   | No              | null          | List of accountId of the operator |
| `targetNicks`           | list                                   | No              | null          | List of nicknames of the operator |
| `targetTag`             | String                                 | No              | null          | Label of the operator             |
| `operatorId`            | string                                 | No              | null          | Operator account (accountId)      |
| `operatorNick`          | string                                 | No              | null          | Operator's nickname               |
| `notificationExtension` | string                                 | No              | null          | Notification extension field      |
| `tags`                  | list                                   | No              | null          | Notification label list           |

## Chat room history messages

The chat room saves the chat room history messages of the last 10 days by default, and does not support saving offline messages and roaming messages. You can set the **number of days of chat room history messages**by yourself under the **chat room sub-function configuration**of [NetEase Yunxin console](https://app.yunxin.163.com/)IM instant messaging.

The URL link address of the message attachment (picture, audio, video, etc.) sent 10 days ago is still valid, but it does not support query. You need to save the URL by yourself.

### Check the chat room history

通过调用 `getMessageList` 方法分页获取所有聊天室历史消息，包含聊天室通知消息。

The sample code is as follows:

安卓

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomMessageListOption option = new V2NIMChatroomMessageListOption();
// 设置查询数量
option.setLimit(100);
// 设置消息查询起始时间
option.setBeginTime(0L);
// 设置消息查询方向
option.setDirection(V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
// 设置查询的消息类型，如果列表为空，表示查询所有类型的消息
List<V2NIMMessageType> messageTypes = getMessageTypes();
option.setMessageTypes(messageTypes);

v2ChatroomService.getMessageList(option, new V2NIMSuccessCallback<List<V2NIMChatroomMessage>>() {
    @Override
    public void onSuccess(List<V2NIMChatroomMessage> v2NIMChatroomMessages) {
        //查询成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //查询失败
    }
});

```

iOS

```
// 通过实例 ID 获取聊天室服务
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

V2NIMChatroomMessageListOption *option = [[V2NIMChatroomMessageListOption alloc] init];
// 设置查询数量
option.limit = 100;
// 设置消息查询起始时间
option.beginTime = 0L;
// 设置消息查询方向
option.direction = V2NIM_QUERY_DIRECTION_DESC;
// 设置查询的消息类型，如果列表为空，表示查询所有类型的消息
option.messageTypes = @[
    @(V2NIM_MESSAGE_TYPE_TEXT),
    @(V2NIM_MESSAGE_TYPE_IMAGE),
    @(V2NIM_MESSAGE_TYPE_LOCATION),
    @(V2NIM_MESSAGE_TYPE_NOTIFICATION),
    @(V2NIM_MESSAGE_TYPE_FILE),
    @(V2NIM_MESSAGE_TYPE_TIP),
    @(V2NIM_MESSAGE_TYPE_CUSTOM)
];
[service getMessageList:option
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
V2NIMChatroomMessageListOption option;
option.beginTime = 0;
option.limit = 10;
chatroomService.getMessageList(
    option,
    [](nstd::vector<V2NIMChatroomMessage> messages) {
        // get message list succeeded
    },
    [](V2NIMError error) {
        // get message list failed, handle error
    });

```

Web/uni-app/小程序

```
const messageArr = await chatroom.V2NIMChatroomService.getMessageList({
    // 0 是降序查找。从最新的消息开始查询
    direction: V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC,
    // 查询开始时间
    beginTime: 0,
    limit: 100
})

```

Node.js/Electron

```
const result = await chatroomService.getMessageList({
    limit: 10
})
console.log(result)

```

鸿蒙

```
const messages: V2NIMChatroomMessage[] = await this.chatroomClient.chatroomService.getMessageList({
    // 0 是降序查找。从最新的消息开始查询
    direction: V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC,
    // 查询开始时间
    beginTime: 0,
    limit: 100
})

```

Flutter

```
final option = V2NIMChatroomMessageListOption(limit: 100);
final messageList = (await chatroomService.getMessageList(option));

```

安卓iOSmacOS/WindowsWeb/uni-app/小程序Node.js/Electron鸿蒙Flutter

```
// 通过实例 ID 获取聊天室实例
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomMessageListOption option = new V2NIMChatroomMessageListOption();
// 设置查询数量
option.setLimit(100);
// 设置消息查询起始时间
option.setBeginTime(0L);
// 设置消息查询方向
option.setDirection(V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
// 设置查询的消息类型，如果列表为空，表示查询所有类型的消息
List<V2NIMMessageType> messageTypes = getMessageTypes();
option.setMessageTypes(messageTypes);

v2ChatroomService.getMessageList(option, new V2NIMSuccessCallback<List<V2NIMChatroomMessage>>() {
    @Override
    public void onSuccess(List<V2NIMChatroomMessage> v2NIMChatroomMessages) {
        //查询成功
    }
}, new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        //查询失败
    }
});

```

```
// 通过实例 ID 获取聊天室服务
id <V2NIMChatroomService> service = [[V2NIMChatroomClient getInstance:1] getChatroomService];

V2NIMChatroomMessageListOption *option = [[V2NIMChatroomMessageListOption alloc] init];
// 设置查询数量
option.limit = 100;
// 设置消息查询起始时间
option.beginTime = 0L;
// 设置消息查询方向
option.direction = V2NIM_QUERY_DIRECTION_DESC;
// 设置查询的消息类型，如果列表为空，表示查询所有类型的消息
option.messageTypes = @[
    @(V2NIM_MESSAGE_TYPE_TEXT),
    @(V2NIM_MESSAGE_TYPE_IMAGE),
    @(V2NIM_MESSAGE_TYPE_LOCATION),
    @(V2NIM_MESSAGE_TYPE_NOTIFICATION),
    @(V2NIM_MESSAGE_TYPE_FILE),
    @(V2NIM_MESSAGE_TYPE_TIP),
    @(V2NIM_MESSAGE_TYPE_CUSTOM)
];
[service getMessageList:option
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
V2NIMChatroomMessageListOption option;
option.beginTime = 0;
option.limit = 10;
chatroomService.getMessageList(
    option,
    [](nstd::vector<V2NIMChatroomMessage> messages) {
        // get message list succeeded
    },
    [](V2NIMError error) {
        // get message list failed, handle error
    });

```

```
const messageArr = await chatroom.V2NIMChatroomService.getMessageList({
    // 0 是降序查找。从最新的消息开始查询
    direction: V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC,
    // 查询开始时间
    beginTime: 0,
    limit: 100
})

```

```
const result = await chatroomService.getMessageList({
    limit: 10
})
console.log(result)

```

```
const messages: V2NIMChatroomMessage[] = await this.chatroomClient.chatroomService.getMessageList({
    // 0 是降序查找。从最新的消息开始查询
    direction: V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC,
    // 查询开始时间
    beginTime: 0,
    limit: 100
})

```

```
final option = V2NIMChatroomMessageListOption(limit: 100);
final messageList = (await chatroomService.getMessageList(option));

```

### Check the history of the chat room by tag

通过调用 `getMessageListByTag` 方法按照标签信息分页获取所有聊天室历史消息，包含聊天室通知消息。

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

安卓iOSmacOS/WindowsWeb/uni-app/小程序Node.js/Electron鸿蒙Flutter

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

### Chat room message withdrawal

At present, it is only supported to call the new version of the server API [to withdraw/delete the chat room history messages](https://doc.yunxin.163.com/messaging2/server-apis/Tk5MTE3MDY?platform=server)to realize the message withdrawal function. You need to register the chat room listener in advance to listen to the chat room message withdrawal callback `onMessageRevokedNotification`.

安卓

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomListener listener = new V2NIMChatroomListener() {

    @Override
    public void onMessageRevokedNotification(String messageClientId, long messageTime) {

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

- (void)onMessageRevokedNotification:(NSString *)messageClientId
                         messageTime:(NSTimeInterval)messageTime
{

}

@end

```

macOS/Windows

```
V2NIMChatroomListener listener;
listener.onMessageRevokedNotification = [](nstd::string messageClientId, uint64_t messageTime) {
    // handle message revoked notification
};
chatroomService.addChatroomListener(listener);

```

Web/uni-app/小程序

```
chatroom.V2NIMChatroomService.on('onMessageRevokedNotification', function (messageClientId: string, messageTime: number){})

```

Node.js/Electron

```
chatroom.chatroomService.on('messageRevokedNotification', function (messageClientId: string, messageTime: number){})

```

鸿蒙

```
chatroom.chatroomService.on('onMessageRevokedNotification', (messageClientId: string, messageTime: number) => {})

```

Flutter

```
//首先添加监听
await chatroomClient?.getChatroomService().addChatroomListener();
//然后设置监听
chatroomClient!.getChatroomService().onMessageRevokedNotification.listen((event) {
//todo something
});

```

安卓iOSmacOS/WindowsWeb/uni-app/小程序Node.js/Electron鸿蒙Flutter

```
V2NIMChatroomClient v2ChatroomClient = V2NIMChatroomClient.getInstance(instanceId);
V2NIMChatroomService v2ChatroomService = v2ChatroomClient.getChatroomService();

V2NIMChatroomListener listener = new V2NIMChatroomListener() {

    @Override
    public void onMessageRevokedNotification(String messageClientId, long messageTime) {

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

- (void)onMessageRevokedNotification:(NSString *)messageClientId
                         messageTime:(NSTimeInterval)messageTime
{

}

@end

```

```
V2NIMChatroomListener listener;
listener.onMessageRevokedNotification = [](nstd::string messageClientId, uint64_t messageTime) {
    // handle message revoked notification
};
chatroomService.addChatroomListener(listener);

```

```
chatroom.V2NIMChatroomService.on('onMessageRevokedNotification', function (messageClientId: string, messageTime: number){})

```

```
chatroom.chatroomService.on('messageRevokedNotification', function (messageClientId: string, messageTime: number){})

```

```
chatroom.chatroomService.on('onMessageRevokedNotification', (messageClientId: string, messageTime: number) => {})

```

```
//首先添加监听
await chatroomClient?.getChatroomService().addChatroomListener();
//然后设置监听
chatroomClient!.getChatroomService().onMessageRevokedNotification.listen((event) {
//todo something
});

```

## Relevant information

- [Chat room-related API](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client)
- [Chat room-related error code](https://doc.yunxin.163.com/messaging2/client-apis/DUxNjU3MzU?platform=client#%E8%81%8A%E5%A4%A9%E5%AE%A4%E9%94%99%E8%AF%AF)

## Related interfaces

安卓/iOS/macOS/Windows

| API                                                                                                                                                                | 说明                                   |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)                        | 获取聊天室服务类                       |
| [`addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#addChatroomListener)                                          | 注册聊天室监听器                       |
| [`removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#removeChatroomListener)                                    | 取消注册聊天室监听器                   |
| [`V2NIMChatroomMessageCreator.createTextMessage`](https://doc.yunxin.163.com/messaging2/client-apis/jE1MTA1MDY?platform=client#createTextMessage)                  | 构建一条聊天室文本消息                 |
| [`V2NIMSendChatroomMessageParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMSendChatroomMessageParams)                    | 发送消息的配置参数                     |
| [`V2NIMChatroomMessageConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMessageConfig)                            | 聊天室消息相关配置                     |
| [`V2NIMMessageRouteConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMMessageRouteConfig)                                  | 消息事件抄送相关配置                   |
| [`V2NIMMessageAntispamConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMMessageAntispamConfig)                            | 消息反垃圾相关配置                     |
| [`V2NIMLocationInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMLocationInfo)                                              | 消息空间位置信息配置                   |
| [`V2NIMChatroomService.sendMessage`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#sendMessage)                                     | 发送聊天室消息                         |
| [`V2NIMMessageAttachment`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMMessageAttachment)                                    | 消息附件类                             |
| [`V2NIMChatroomService.cancelMessageAttachmentUpload`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#cancelMessageAttachmentUpload) | 取消附件上传                           |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#enter)                                                                      | 进入聊天室                             |
| [`V2NIMChatroomEnterParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams)                                | 进入聊天室配置参数                     |
| [`updateChatroomLocationInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateChatroomLocationInfo)                            | 更新在当前聊天室中的空间位置坐标       |
| [`V2NIMChatroomMessage`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMessage)                                        | 聊天室消息对象                         |
| [`V2NIMChatroomNotificationAttachment`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomNotificationAttachment)          | 聊天室通知消息附件对象                 |
| [`V2NIMChatroomMessageNotificationType`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMessageNotificationType)        | 聊天室通知类消息类型                   |
| [`getMessageList`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMessageList)                                                    | 分页获取所有聊天室历史消息             |
| [`getMessageListByTag`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMessageListByTag)                                          | 按照标签信息分页获取所有聊天室历史消息 |
| [`V2NIMClientAntispamOperateType`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMClientAntispamOperateType)                    | 客户端本地反垃圾命中后的操作类型       |

Web/uni-app/小程序/Node.js/Electron

| API                                                                                                                                                                | 说明                                   |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)                        | 获取聊天室服务类                       |
| [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#on)                                                               | 注册聊天室监听器                       |
| [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#off)                                                             | 取消注册聊天室监听器                   |
| [`V2NIMChatroomMessageCreator.createTextMessage`](https://doc.yunxin.163.com/messaging2/client-apis/jE1MTA1MDY?platform=client#createTextMessage)                  | 构建一条聊天室文本消息                 |
| [`V2NIMSendChatroomMessageParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMSendChatroomMessageParams)                    | 发送消息的配置参数                     |
| [`V2NIMChatroomMessageConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMessageConfig)                            | 聊天室消息相关配置                     |
| [`V2NIMMessageRouteConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMMessageRouteConfig)                                  | 消息事件抄送相关配置                   |
| [`V2NIMMessageAntispamConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMMessageAntispamConfig)                            | 消息反垃圾相关配置                     |
| [`V2NIMLocationInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMLocationInfo)                                              | 消息空间位置信息配置                   |
| [`V2NIMChatroomService.sendMessage`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#sendMessage)                                     | 发送聊天室消息                         |
| [`V2NIMMessageAttachment`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMMessageAttachment)                                    | 消息附件类                             |
| [`V2NIMChatroomService.cancelMessageAttachmentUpload`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#cancelMessageAttachmentUpload) | 取消附件上传                           |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#enter)                                                                      | 进入聊天室                             |
| [`V2NIMChatroomEnterParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams)                                | 进入聊天室配置参数                     |
| [`updateChatroomLocationInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateChatroomLocationInfo)                            | 更新在当前聊天室中的空间位置坐标       |
| [`V2NIMChatroomMessage`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMessage)                                        | 聊天室消息对象                         |
| [`V2NIMChatroomNotificationAttachment`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomNotificationAttachment)          | 聊天室通知消息附件对象                 |
| [`V2NIMChatroomMessageNotificationType`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMessageNotificationType)        | 聊天室通知类消息类型                   |
| [`getMessageList`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMessageList)                                                    | 分页获取所有聊天室历史消息             |
| [`getMessageListByTag`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMessageListByTag)                                          | 按照标签信息分页获取所有聊天室历史消息 |
| [`V2NIMClientAntispamOperateType`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMClientAntispamOperateType)                    | 客户端本地反垃圾命中后的操作类型       |

Flutter

| API                                                                                                                                                                | 说明                                   |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomService)                        | 获取聊天室服务类                       |
| [`addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#addChatroomListener)                                          | 注册聊天室监听器                       |
| [`removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#removeChatroomListener)                                    | 取消注册聊天室监听器                   |
| [`V2NIMChatroomMessageCreator.createTextMessage`](https://doc.yunxin.163.com/messaging2/client-apis/TAxMjQ2Mzk?platform=client#createTextMessage)                  | 构建一条聊天室文本消息                 |
| [`V2NIMSendChatroomMessageParams`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMSendChatroomMessageParams)                    | 发送消息的配置参数                     |
| [`V2NIMChatroomMessageConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomMessageConfig)                            | 聊天室消息相关配置                     |
| [`NIMMessageRouteConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#NIMMessageRouteConfig)                                      | 消息事件抄送相关配置                   |
| [`NIMMessageAntispamConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#NIMMessageAntispamConfig)                                | 消息反垃圾相关配置                     |
| [`V2NIMLocationInfo`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMLocationInfo)                                              | 消息空间位置信息配置                   |
| [`V2NIMChatroomService.sendMessage`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#sendMessage)                                     | 发送聊天室消息                         |
| [`NIMMessageAttachment`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#NIMMessageAttachment)                                        | 消息附件类                             |
| [`V2NIMChatroomService.cancelMessageAttachmentUpload`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#cancelMessageAttachmentUpload) | 取消附件上传                           |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#enter)                                                                      | 进入聊天室                             |
| [`V2NIMChatroomEnterParams`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomEnterParams)                                | 进入聊天室配置参数                     |
| [`updateChatroomLocationInfo`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#updateChatroomLocationInfo)                            | 更新在当前聊天室中的空间位置坐标       |
| [`V2NIMChatroomMessage`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomMessage)                                        | 聊天室消息对象                         |
| [`V2NIMChatroomNotificationAttachment`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomNotificationAttachment)          | 聊天室通知消息附件对象                 |
| [`V2NIMChatroomMessageNotificationType`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomMessageNotificationType)        | 聊天室通知类消息类型                   |
| [`getMessageList`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#getMessageList)                                                    | 分页获取所有聊天室历史消息             |
| [`getMessageListByTag`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#getMessageListByTag)                                          | 按照标签信息分页获取所有聊天室历史消息 |
| [`NIMClientAntispamOperateType`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#NIMClientAntispamOperateType)                        | 客户端本地反垃圾命中后的操作类型       |

安卓/iOS/macOS/WindowsWeb/uni-app/小程序/Node.js/ElectronFlutter

| API                                                                                                                                                                | 说明                                   |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)                        | 获取聊天室服务类                       |
| [`addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#addChatroomListener)                                          | 注册聊天室监听器                       |
| [`removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#removeChatroomListener)                                    | 取消注册聊天室监听器                   |
| [`V2NIMChatroomMessageCreator.createTextMessage`](https://doc.yunxin.163.com/messaging2/client-apis/jE1MTA1MDY?platform=client#createTextMessage)                  | 构建一条聊天室文本消息                 |
| [`V2NIMSendChatroomMessageParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMSendChatroomMessageParams)                    | 发送消息的配置参数                     |
| [`V2NIMChatroomMessageConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMessageConfig)                            | 聊天室消息相关配置                     |
| [`V2NIMMessageRouteConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMMessageRouteConfig)                                  | 消息事件抄送相关配置                   |
| [`V2NIMMessageAntispamConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMMessageAntispamConfig)                            | 消息反垃圾相关配置                     |
| [`V2NIMLocationInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMLocationInfo)                                              | 消息空间位置信息配置                   |
| [`V2NIMChatroomService.sendMessage`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#sendMessage)                                     | 发送聊天室消息                         |
| [`V2NIMMessageAttachment`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMMessageAttachment)                                    | 消息附件类                             |
| [`V2NIMChatroomService.cancelMessageAttachmentUpload`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#cancelMessageAttachmentUpload) | 取消附件上传                           |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#enter)                                                                      | 进入聊天室                             |
| [`V2NIMChatroomEnterParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams)                                | 进入聊天室配置参数                     |
| [`updateChatroomLocationInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateChatroomLocationInfo)                            | 更新在当前聊天室中的空间位置坐标       |
| [`V2NIMChatroomMessage`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMessage)                                        | 聊天室消息对象                         |
| [`V2NIMChatroomNotificationAttachment`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomNotificationAttachment)          | 聊天室通知消息附件对象                 |
| [`V2NIMChatroomMessageNotificationType`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMessageNotificationType)        | 聊天室通知类消息类型                   |
| [`getMessageList`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMessageList)                                                    | 分页获取所有聊天室历史消息             |
| [`getMessageListByTag`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMessageListByTag)                                          | 按照标签信息分页获取所有聊天室历史消息 |
| [`V2NIMClientAntispamOperateType`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMClientAntispamOperateType)                    | 客户端本地反垃圾命中后的操作类型       |

| API                                                                                                                                                                | 说明                                   |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService)                        | 获取聊天室服务类                       |
| [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#on)                                                               | 注册聊天室监听器                       |
| [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#off)                                                             | 取消注册聊天室监听器                   |
| [`V2NIMChatroomMessageCreator.createTextMessage`](https://doc.yunxin.163.com/messaging2/client-apis/jE1MTA1MDY?platform=client#createTextMessage)                  | 构建一条聊天室文本消息                 |
| [`V2NIMSendChatroomMessageParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMSendChatroomMessageParams)                    | 发送消息的配置参数                     |
| [`V2NIMChatroomMessageConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMessageConfig)                            | 聊天室消息相关配置                     |
| [`V2NIMMessageRouteConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMMessageRouteConfig)                                  | 消息事件抄送相关配置                   |
| [`V2NIMMessageAntispamConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMMessageAntispamConfig)                            | 消息反垃圾相关配置                     |
| [`V2NIMLocationInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMLocationInfo)                                              | 消息空间位置信息配置                   |
| [`V2NIMChatroomService.sendMessage`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#sendMessage)                                     | 发送聊天室消息                         |
| [`V2NIMMessageAttachment`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMMessageAttachment)                                    | 消息附件类                             |
| [`V2NIMChatroomService.cancelMessageAttachmentUpload`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#cancelMessageAttachmentUpload) | 取消附件上传                           |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#enter)                                                                      | 进入聊天室                             |
| [`V2NIMChatroomEnterParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams)                                | 进入聊天室配置参数                     |
| [`updateChatroomLocationInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#updateChatroomLocationInfo)                            | 更新在当前聊天室中的空间位置坐标       |
| [`V2NIMChatroomMessage`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMessage)                                        | 聊天室消息对象                         |
| [`V2NIMChatroomNotificationAttachment`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomNotificationAttachment)          | 聊天室通知消息附件对象                 |
| [`V2NIMChatroomMessageNotificationType`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomMessageNotificationType)        | 聊天室通知类消息类型                   |
| [`getMessageList`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMessageList)                                                    | 分页获取所有聊天室历史消息             |
| [`getMessageListByTag`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#getMessageListByTag)                                          | 按照标签信息分页获取所有聊天室历史消息 |
| [`V2NIMClientAntispamOperateType`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMClientAntispamOperateType)                    | 客户端本地反垃圾命中后的操作类型       |

| API                                                                                                                                                                | 说明                                   |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------- |
| [`V2NIMChatroomClient.getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomService)                        | 获取聊天室服务类                       |
| [`addChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#addChatroomListener)                                          | 注册聊天室监听器                       |
| [`removeChatroomListener`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#removeChatroomListener)                                    | 取消注册聊天室监听器                   |
| [`V2NIMChatroomMessageCreator.createTextMessage`](https://doc.yunxin.163.com/messaging2/client-apis/TAxMjQ2Mzk?platform=client#createTextMessage)                  | 构建一条聊天室文本消息                 |
| [`V2NIMSendChatroomMessageParams`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMSendChatroomMessageParams)                    | 发送消息的配置参数                     |
| [`V2NIMChatroomMessageConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomMessageConfig)                            | 聊天室消息相关配置                     |
| [`NIMMessageRouteConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#NIMMessageRouteConfig)                                      | 消息事件抄送相关配置                   |
| [`NIMMessageAntispamConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#NIMMessageAntispamConfig)                                | 消息反垃圾相关配置                     |
| [`V2NIMLocationInfo`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMLocationInfo)                                              | 消息空间位置信息配置                   |
| [`V2NIMChatroomService.sendMessage`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#sendMessage)                                     | 发送聊天室消息                         |
| [`NIMMessageAttachment`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#NIMMessageAttachment)                                        | 消息附件类                             |
| [`V2NIMChatroomService.cancelMessageAttachmentUpload`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#cancelMessageAttachmentUpload) | 取消附件上传                           |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#enter)                                                                      | 进入聊天室                             |
| [`V2NIMChatroomEnterParams`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomEnterParams)                                | 进入聊天室配置参数                     |
| [`updateChatroomLocationInfo`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#updateChatroomLocationInfo)                            | 更新在当前聊天室中的空间位置坐标       |
| [`V2NIMChatroomMessage`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomMessage)                                        | 聊天室消息对象                         |
| [`V2NIMChatroomNotificationAttachment`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomNotificationAttachment)          | 聊天室通知消息附件对象                 |
| [`V2NIMChatroomMessageNotificationType`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomMessageNotificationType)        | 聊天室通知类消息类型                   |
| [`getMessageList`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#getMessageList)                                                    | 分页获取所有聊天室历史消息             |
| [`getMessageListByTag`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#getMessageListByTag)                                          | 按照标签信息分页获取所有聊天室历史消息 |
| [`NIMClientAntispamOperateType`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#NIMClientAntispamOperateType)                        | 客户端本地反垃圾命中后的操作类型       |
