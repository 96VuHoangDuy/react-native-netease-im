# 聊天室登录

Before sending and receiving messages in the chat room, users need to create a chat room instance and call the chat room login interface of SDK to enter the chat room. After successful login, users can send and receive messages and use other chat room-related functions normally in the chat room.

This article introduces the process of creating chat room instances and realizing chat room login, as well as related frequently asked questions.

## Support platform

The development platform or framework applicable to this article is shown in the following table. For the interfaces involved, please refer to the following [relevant interface](#%E7%9B%B8%E5%85%B3%E6%8E%A5%E5%8F%A3)chapters:

| Android | iOS | macOS/Windows | Web/uni-app/applet | Node.js/Electron | Hongmeng | Flutter |
| --- | --- | --- | --- | --- | --- | --- |
| ✔️️️ | ✔️️️ | ✔️️️ | ✔️️️ | ✔️️️ | ✔️️️ | ✔️️️ |

## Technical principle

The login process of the chat room includes the following three stages:

```
#mermaid-render-0 {font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:16px;fill:#5409DA;}#mermaid-render-0 .error-icon{fill:#F8FAFC;}#mermaid-render-0 .error-text{fill:#070503;stroke:#070503;}#mermaid-render-0 .edge-thickness-normal{stroke-width:2px;}#mermaid-render-0 .edge-thickness-thick{stroke-width:3.5px;}#mermaid-render-0 .edge-pattern-solid{stroke-dasharray:0;}#mermaid-render-0 .edge-pattern-dashed{stroke-dasharray:3;}#mermaid-render-0 .edge-pattern-dotted{stroke-dasharray:2;}#mermaid-render-0 .marker{fill:#4E71FF;stroke:#4E71FF;}#mermaid-render-0 .marker.cross{stroke:#4E71FF;}#mermaid-render-0 svg{font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:16px;}#mermaid-render-0 .label{font-family:"trebuchet ms",verdana,arial,sans-serif;color:#5409DA;}#mermaid-render-0 .cluster-label text{fill:#070503;}#mermaid-render-0 .cluster-label span{color:#070503;}#mermaid-render-0 .label text,#mermaid-render-0 span{fill:#5409DA;color:#5409DA;}#mermaid-render-0 .node rect,#mermaid-render-0 .node circle,#mermaid-render-0 .node ellipse,#mermaid-render-0 .node polygon,#mermaid-render-0 .node path{fill:#AFDDFF;stroke:#4E71FF;stroke-width:1px;}#mermaid-render-0 .node .label{text-align:center;}#mermaid-render-0 .node.clickable{cursor:pointer;}#mermaid-render-0 .arrowheadPath{fill:undefined;}#mermaid-render-0 .edgePath .path{stroke:#4E71FF;stroke-width:2.0px;}#mermaid-render-0 .flowchart-link{stroke:#4E71FF;fill:none;}#mermaid-render-0 .edgeLabel{background-color:#FF9149;text-align:center;}#mermaid-render-0 .edgeLabel rect{opacity:0.5;background-color:#FF9149;fill:#FF9149;}#mermaid-render-0 .cluster rect{fill:#F8FAFC;stroke:hsl(210, 0%, 88.0392156863%);stroke-width:1px;}#mermaid-render-0 .cluster text{fill:#070503;}#mermaid-render-0 .cluster span{color:#070503;}#mermaid-render-0 div.mermaidTooltip{position:absolute;text-align:center;max-width:200px;padding:2px;font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:12px;background:#F8FAFC;border:1px solid undefined;border-radius:2px;pointer-events:none;z-index:100;}#mermaid-render-0 :root{--mermaid-font-family:"trebuchet ms",verdana,arial,sans-serif;}Get the chat room Link addressLogin authentication (establish a chat room manager connection)Enter the chat room
```

```
graph LR
%%{init: {'theme': 'base', 'themeVariables': { 'primaryColor': '#AFDDFF', 'primaryTextColor': '#5409DA', 'primaryBorderColor': '#4E71FF', 'lineColor': '#4E71FF', 'secondaryColor': '#FF9149', 'tertiaryColor': '#F8FAFC' }}}%%

A("获取聊天室 Link 地址") --> B("登录鉴权（建立聊天室长连接）") --> C("进入聊天室")

```

If any of the above processes fail, it will trigger a failed callback to enter the chat room.

本地端或多端同步进入聊天室成功后，会收到 `onChatroomEntered` 回调。如果已在 [网易云信控制台](https://app.yunxin.163.com/global/home) 开启了 **聊天室用户进出消息系统下发** 功能，聊天室内所有其他成员会收到回调 `onChatroomMemberEnter`。如未开启，可参考 [开通和配置聊天室功能](https://doc.yunxin.163.com/console/concept/zYyMjAzNDY?platform=console#%E9%85%8D%E7%BD%AE%E8%81%8A%E5%A4%A9%E5%AE%A4%E5%AD%90%E5%8A%9F%E8%83%BD)。

According to the authentication method, the login method is divided into static Token login, dynamic Token login and login through third-party callback.

You can implement **one or more**login methods on demand.

| Login method | Authentication method | Description |
| --- | --- | --- |
| [Static Token login](#%E9%9D%99%E6%80%81-token-%E7%99%BB%E5%BD%95) | [Static Token Authentication](https://doc.yunxin.163.com/messaging2/server-apis/jA1MTQ4MDU?platform=server#%E9%9D%99%E6%80%81-token-%E9%89%B4%E6%9D%83) | Static Token is permanently valid by default and constant, unless the [server interface](https://doc.yunxin.163.com/messaging2/server-apis/DUwODIwMTg?platform=server)is actively called to refresh. |
| [Dynamic Token Login](#%E5%8A%A8%E6%80%81-token-%E7%99%BB%E5%BD%95) | [Dynamic Token Authentication](https://doc.yunxin.163.com/messaging2/server-apis/jA1MTQ4MDU?platform=server#%E5%8A%A8%E6%80%81-token-%E9%89%B4%E6%9D%83) | Dynamic Token is time-effective and suitable for business scenarios with high requirements for user information security. |
| [Log in through a third-party callback](#%E7%AC%AC%E4%B8%89%E6%96%B9%E5%9B%9E%E8%B0%83%E7%99%BB%E5%BD%95) | [Through third-party callback authentication](https://doc.yunxin.163.com/messaging2/server-apis/jA1MTQ4MDU?platform=server#%E5%9F%BA%E4%BA%8E%E7%AC%AC%E4%B8%89%E6%96%B9%E5%9B%9E%E8%B0%83%E7%9A%84%E9%89%B4%E6%9D%83) | The authentication work when the user logs in to the chat room is carried out by the designated third-party server (which can be the application server). **The NetEase Yunxin server does not do the chat room login authentication**. |

## Prerequisites

Before logging in to the chat room, please make sure that:

- [The chat room function](https://doc.yunxin.163.com/console/concept/zYyMjAzNDY?platform=console)has been [opened and configured](https://doc.yunxin.163.com/console/concept/zYyMjAzNDY?platform=console).
- The chat room has been created. At present, the [chat room](https://doc.yunxin.163.com/messaging2/server-apis/DU5NjcwNTk?platform=server)can only be [created](https://doc.yunxin.163.com/messaging2/server-apis/DU5NjcwNTk?platform=server)through the server API.

## Step 1: Configure the chat room login policy

The login policy refers to one or more chat room login methods that your application needs to adopt. Before logging in to the chat room, you need to configure the chat room login policy of the application in the NetEase Cloud Office console. If the corresponding login policy is not configured, an error may be reported due to no login permission when calling the login interface in the future (status code: 403).

1. Select the application in the home page **application management**of [NetEase Yunxin console](https://app.yunxin.163.com/global/home), and then click the **Function Configuration**button under **IM Instant Messaging**to enter the function configuration page.

 ![Image.png](https://yx-web-nosdn.netease.im/common/b4c08cb9c2fa97f2bcc99523ffae697a/image.png)
2. Select the **chat room**tab at the top, turn on the chat room function, and then click **Subfunction Configuration**.

 ![](https://yx-web-nosdn.netease.im/common/28bb6b1867fc957835c4c01ac2ca580d/聊天室子功能配置.png)
3. Select the configuration **chat room login policy**on the sub-function configuration page.

 ![](https://yx-web-nosdn.netease.im/common/de6644c8128f77e13cc7e98ede0b2bfc/聊天室登录策略.png)

## Step 2: Prepare Token

According to the login policy configured above, you need to obtain the corresponding Token for subsequent authentication.

- **Static Token**: Permanently valid by default. If necessary, you can [actively refresh Token](https://doc.yunxin.163.com/messaging2/server-apis/DUwODIwMTg?platform=server)through the new version of NetEase Yunxin server API.
- **Dynamic Token**: It has time validity, and the validity period of the token can be set at the time of generation.
- **动态登录扩展数据（`LoginExtension`）**：适用于所有登录模式。如果在第三方回调登录模式中设置动态登录扩展数据，第三方服务器可使用该值来进行鉴权。

### Get static Token

You can obtain static Token in the following two ways for authentication of static Token login.

- **Method**1: Get static Token in the [NetEase Cloud Trust Console](https://app.yunxin.163.com/global/home)

If you only need a simple **experience or a quick test**, you can create an IM account for testing in the **NetEase Yunxin console**and obtain the **static token**corresponding to the IM account. Please refer to the [registration of an IM account for the](https://doc.yunxin.163.com/messaging2/guide/jU0Mzg0MTU?platform=client#%E7%AC%AC%E4%BA%8C%E6%AD%A5%E6%B3%A8%E5%86%8C-im-%E8%B4%A6%E5%8F%B7)acquisition method.
- **Method**2: Call the server API to get the static Token

If you are in a formal **production environment**, in order to **ensure the security of users' information**, you need to register an IM account through the new version of NetEase Yunxin server API and obtain the corresponding **static token**. For the acquisition method, please refer to the [registration of IM account](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server).

### Get the dynamic Token

If you have a formal **production environment**and your business **has high requirements for user information security**, you can choose dynamic Token login. The steps to obtain the dynamic Token are as follows:

1. Register an IM account and get `accountId`.

  - **Method**1: Register an IM test account in the [NetEase Cloud Console](https://app.yunxin.163.com/global/home)

If you only need to have a simple **experience or a quick test**, you can create an IM account for testing in the **NetEase Yunxin console**. Please refer to the [registration of an IM account](https://doc.yunxin.163.com/messaging2/guide/jU0Mzg0MTU?platform=client#%E7%AC%AC%E4%BA%8C%E6%AD%A5%E6%B3%A8%E5%86%8C-im-%E8%B4%A6%E5%8F%B7)for the acquisition method.
  - **Method**2: Call the server API to register the official IM account

If you are in the formal **production environment**, in order to **ensure the security of users' information**, you can [register an IM account](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)through the new version of NetEase Yunxin IM server API.
2. Based on App Key, App Secret and `accountId`, generate **dynamic tokens**on **the application server**through the [conventional algorithm](https://doc.yunxin.163.com/messaging2/server-apis/jA1MTQ4MDU?platform=server#%E5%8A%A8%E6%80%81token%E9%89%B4%E6%9D%83).
3. 客户端可通过在登录聊天室时实现 `tokenProvider` 方法，从回调中获取聊天室动态 token。具体实现方式请参考下文 [动态 Token 登录](#%E5%8A%A8%E6%80%81-token-%E7%99%BB%E5%BD%95)。

### Get dynamic login extension data

客户端可通过在登录聊天室时实现 `loginExtensionProvider` 方法，从回调中获取聊天室动态扩展数据 `loginExtension`。具体实现方式请参考下文 [通过第三方回调登录](%E7%AC%AC%E4%B8%89%E6%96%B9%E5%9B%9E%E8%B0%83%E7%99%BB%E5%BD%95)。

## Step 3: Create a chat room instance

调用 `newInstance` 方法创建聊天室实例。调用成功后，返回聊天室实例（`instanceId`），聊天室实例与聊天室（`roomId`）形成一一绑定关系。

 安卓

```
V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.newInstance();

```

 iOS

```
V2NIMChatroomClient *chatroomClient = [V2NIMChatroomClient newInstance];

```

 macOS/Windows

在创建聊天室实例前，需要先调用 `init` 进行初始化。

```
V2NIMInitOption option;
option.appkey = "your app key";
option.appDataPath = "your app name";
auto error = V2NIMChatroomClient::init(option);
if (error) {
    // init failed
    // ...
    return;
}

```

完成初始化后，再创建聊天室实例。

```
auto chatroomClient = V2NIMChatroomClient::newInstance();
if (!chatroomClient) {
    // create instance failed
    // ...
    return;
}
auto instanceId = chatroomClient->getInstanceId();
// save instanceId to cache
// ...

```

 Web/uni-app/小程序

需要在创建实例时传入聊天室初始化参数。

| 参数名称 | 类型 | 是否必填 | 描述 |
| --- | --- | --- | --- |
| initParams | `V2NIMChatroomInitParams` | 是 | 聊天室初始化参数，包括应用 AppKey 和自定义的设备类型。 |

```
const chatroom = V2NIMChatroomClient.newInstance(
    {
        appkey: 'YOUR_APPKEY'
    }
)

```

 Node.js/Electron

在创建聊天室实例前，需要先调用 `init` 进行初始化。

```
// 引入 node-nim
const NIM = require('node-nim')

const appkey = 'Your appkey'
const account = 'Your account ID'
const token = 'Token of your account ID'
const chatroomId = 'Chatroom ID'

// 初始化聊天室
NIM.V2NIMChatroomClient.init({ appkey })

```

完成初始化后，再创建聊天室实例。

```
const chatroomInstance = NIM.V2NIMChatroomClient.newInstance()

```

 鸿蒙

需要在创建实例时传入聊天室初始化参数。

| 参数名称 | 类型 | 是否必填 | 描述 |
| --- | --- | --- | --- |
| initParams | `V2NIMChatroomInitParams` | 是 | 聊天室初始化参数，包括应用 AppKey 和自定义的设备类型。 |

```
const context: common.Context = getContext(this).getApplicationContext()
const chatroom = V2NIMChatroomClient.newInstance(context，
    {
        appkey: 'YOUR_APPKEY'
    }
)

```

 Flutter

若您使用 PC 或者鸿蒙平台进行开发，那么在创建聊天室实例前，需要先调用 `init` 进行初始化，然后再创建聊天室实例。（其他平台直接调用 `newInstance` 创建聊天室实例即可。）

```
//PC 跨平台
V2NIMChatroomClient.init(NIMPCSDKOptions(basicOption: NIMBasicOption(), appKey: 'appKey'));

//鸿蒙平台
V2NIMChatroomClient.init(NIMOHOSSDKOptions(appKey: 'xxxx'));

```

创建聊天室实例。

```
chatroomClient = (await V2NIMChatroomClient.newInstance()).data;

```

初始化参数具体请参考 [`NIMOHOSSDKOptions`](https://doc.yunxin.163.com/messaging2/references/flutter/Dartdoc/V10.9.2/nim_core_v2/NIMOHOSSDKOptions/NIMOHOSSDKOptions.html) 和 [`NIMPCSDKOptions`](https://doc.yunxin.163.com/messaging2/references/flutter/Dartdoc/V10.9.2/nim_core_v2/NIMPCSDKOptions/NIMPCSDKOptions.html)。

   AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.newInstance();

```

```
V2NIMChatroomClient *chatroomClient = [V2NIMChatroomClient newInstance];

```

在创建聊天室实例前，需要先调用 `init` 进行初始化。

```
V2NIMInitOption option;
option.appkey = "your app key";
option.appDataPath = "your app name";
auto error = V2NIMChatroomClient::init(option);
if (error) {
    // init failed
    // ...
    return;
}

```

完成初始化后，再创建聊天室实例。

```
auto chatroomClient = V2NIMChatroomClient::newInstance();
if (!chatroomClient) {
    // create instance failed
    // ...
    return;
}
auto instanceId = chatroomClient->getInstanceId();
// save instanceId to cache
// ...

```

需要在创建实例时传入聊天室初始化参数。

| 参数名称 | 类型 | 是否必填 | 描述 |
| --- | --- | --- | --- |
| initParams | `V2NIMChatroomInitParams` | 是 | 聊天室初始化参数，包括应用 AppKey 和自定义的设备类型。 |

```
const chatroom = V2NIMChatroomClient.newInstance(
    {
        appkey: 'YOUR_APPKEY'
    }
)

```

在创建聊天室实例前，需要先调用 `init` 进行初始化。

```
// 引入 node-nim
const NIM = require('node-nim')

const appkey = 'Your appkey'
const account = 'Your account ID'
const token = 'Token of your account ID'
const chatroomId = 'Chatroom ID'

// 初始化聊天室
NIM.V2NIMChatroomClient.init({ appkey })

```

完成初始化后，再创建聊天室实例。

```
const chatroomInstance = NIM.V2NIMChatroomClient.newInstance()

```

需要在创建实例时传入聊天室初始化参数。

| 参数名称 | 类型 | 是否必填 | 描述 |
| --- | --- | --- | --- |
| initParams | `V2NIMChatroomInitParams` | 是 | 聊天室初始化参数，包括应用 AppKey 和自定义的设备类型。 |

```
const context: common.Context = getContext(this).getApplicationContext()
const chatroom = V2NIMChatroomClient.newInstance(context，
    {
        appkey: 'YOUR_APPKEY'
    }
)

```

若您使用 PC 或者鸿蒙平台进行开发，那么在创建聊天室实例前，需要先调用 `init` 进行初始化，然后再创建聊天室实例。（其他平台直接调用 `newInstance` 创建聊天室实例即可。）

```
//PC 跨平台
V2NIMChatroomClient.init(NIMPCSDKOptions(basicOption: NIMBasicOption(), appKey: 'appKey'));

//鸿蒙平台
V2NIMChatroomClient.init(NIMOHOSSDKOptions(appKey: 'xxxx'));

```

创建聊天室实例。

```
chatroomClient = (await V2NIMChatroomClient.newInstance()).data;

```

初始化参数具体请参考 [`NIMOHOSSDKOptions`](https://doc.yunxin.163.com/messaging2/references/flutter/Dartdoc/V10.9.2/nim_core_v2/NIMOHOSSDKOptions/NIMOHOSSDKOptions.html) 和 [`NIMPCSDKOptions`](https://doc.yunxin.163.com/messaging2/references/flutter/Dartdoc/V10.9.2/nim_core_v2/NIMPCSDKOptions/NIMPCSDKOptions.html)。

## Step 4: Register the chat room and log in to listen to related events

Chat room instance related callback:

- **`onChatroomStatus`**: Chat room connection status change callback. All members in the chat room will receive the callback.
- **`onChatroomEntered`**: Enter the chat room to call back.
- **`onChatroomKicked`**：被踢出聊天室回调。被踢出聊天室后，会同时触发 `onChatroomExited` 回调。
- **`onChatroomExited`**: Exit the chat room callback. After exiting the chat room, you will not continue to reconnect. Exiting the chat room includes the following scenarios:

  - After the account change, the login failed and the chat room was logged out.
  - Being added to the blacklist of the chat room leads to withdrawal from the chat room.
  - Withdraw from the chat room because the account was banned.
  - Exit the chat room because the chat room was closed.

 安卓

调用 [`addChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#addChatroomClientListener) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
chatroomClient.addChatroomClientListener(new V2NIMChatroomClientListener() {
    @Override
    public void onChatroomStatus(V2NIMChatroomStatus status, V2NIMError error) {
    }
    @Override
    public void onChatroomEntered() {
    }
    @Override
    public void onChatroomExited(V2NIMError error) {
    }
    @Override
    public void onChatroomKicked(V2NIMChatroomKickedInfo kickedInfo) {
    }
});

```

 iOS

调用 [`addChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#addChatroomClientListener) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
@interface ClientListener : NSObject <V2NIMChatroomClientListener>
- (void)addToClient:(NSInteger)clientId;
@end

@implementation ClientListener
- (void)addToClient:(NSInteger)clientId
{
    V2NIMChatroomClient *instance = [V2NIMChatroomClient getInstance:clientId];
    [instance addChatroomClientListener:self];
}
- (void)onChatroomStatus:(V2NIMChatroomStatus)status
                   error:(nullable V2NIMError *)error
{
}
- (void)onChatroomEntered
{
}
- (void)onChatroomExited:(nullable V2NIMError *)error
{
}
- (void)onChatroomKicked:(V2NIMChatroomKickedInfo *)kickedInfo
{
}
@end

```

 macOS/Windows

调用 [`addChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#addChatroomClientListener) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
V2NIMChatroomClientListener listener;
listener.onChatroomStatus = [](V2NIMChatroomStatus status, nstd::optional<V2NIMError> error) {
    // handle chatroom status
};
listener.onChatroomEntered = []() {
    // handle chatroom entered
};
listener.onChatroomExited = [](nstd::optional<V2NIMError> error) {
   // handle chatroom exited
};
listener.onChatroomKicked = [](V2NIMChatroomKickedInfo kickedInfo) {
    // handle chatroom kicked
};
chatroomClient.addChatroomClientListener(listener);

```

 Web/uni-app/小程序

调用 [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#on) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
chatroom.on("onChatroomStatus", function (status: V2NIMChatroomStatus, err?: V2NIMError) {})
chatroom.on("onChatroomEntered", function () {})
chatroom.on("onChatroomExited", function (err?: V2NIMError) {})
chatroom.on("onChatroomKicked", function (kickedInfo: V2NIMChatroomKickedInfo) {})

```

 Node.js/Electron

调用 [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#on) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
// 在创建的实例对象上监听事件
chatroomInstance.on('chatroomStatus', (status, error) => {
    console.log('Chatroom status:', status, error)
})
chatroomInstance.on('chatroomEntered', () => {
    console.log('Chatroom entered successfully')
})
chatroomInstance.on('chatroomExited', (error) => {
    console.log('Chatroom exited:', error)
})
chatroomInstance.on('chatroomKicked', (kickedInfo) => {
    console.log('Chatroom kicked:', kickedInfo)
})

```

 鸿蒙

调用 [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#on) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
const chatroom = this.getInstance(instanceId)
chatroom.on("onChatroomStatus", (status: V2NIMChatroomStatus, err?: V2NIMError) => {
})
chatroom.on("onChatroomEntered", ()=> {
})
chatroom.on("onChatroomExited", (err?: V2NIMError) => {
})
chatroom.on("onChatroomKicked", (kickedInfo: V2NIMChatroomKickedInfo)=> {
})

```

 Flutter

调用 [`addChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#addChatroomClientListener) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
//首先添加监听
await chatroomClient.addChatroomClientListener();
//添加成功后可以监听回到
chatroomClient!.onChatroomEntered.listen((event) {
    //print('ChatroomClient:onChatroomEntered');
}),
chatroomClient!.onChatroomExited.listen((event) {
    // print('ChatroomClient:onChatroomExited');
}),
chatroomClient!.onChatroomStatus.listen((event) {
    //print('ChatroomClient:onChatroomStatus');
}),
chatroomClient!.onChatroomKicked.listen((event) {
    // print('ChatroomClient:onChatroomKicked');
})

```

  AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

调用 [`addChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#addChatroomClientListener) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
chatroomClient.addChatroomClientListener(new V2NIMChatroomClientListener() {
    @Override
    public void onChatroomStatus(V2NIMChatroomStatus status, V2NIMError error) {
    }
    @Override
    public void onChatroomEntered() {
    }
    @Override
    public void onChatroomExited(V2NIMError error) {
    }
    @Override
    public void onChatroomKicked(V2NIMChatroomKickedInfo kickedInfo) {
    }
});

```

调用 [`addChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#addChatroomClientListener) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
@interface ClientListener : NSObject <V2NIMChatroomClientListener>
- (void)addToClient:(NSInteger)clientId;
@end

@implementation ClientListener
- (void)addToClient:(NSInteger)clientId
{
    V2NIMChatroomClient *instance = [V2NIMChatroomClient getInstance:clientId];
    [instance addChatroomClientListener:self];
}
- (void)onChatroomStatus:(V2NIMChatroomStatus)status
                   error:(nullable V2NIMError *)error
{
}
- (void)onChatroomEntered
{
}
- (void)onChatroomExited:(nullable V2NIMError *)error
{
}
- (void)onChatroomKicked:(V2NIMChatroomKickedInfo *)kickedInfo
{
}
@end

```

调用 [`addChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#addChatroomClientListener) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
V2NIMChatroomClientListener listener;
listener.onChatroomStatus = [](V2NIMChatroomStatus status, nstd::optional<V2NIMError> error) {
    // handle chatroom status
};
listener.onChatroomEntered = []() {
    // handle chatroom entered
};
listener.onChatroomExited = [](nstd::optional<V2NIMError> error) {
   // handle chatroom exited
};
listener.onChatroomKicked = [](V2NIMChatroomKickedInfo kickedInfo) {
    // handle chatroom kicked
};
chatroomClient.addChatroomClientListener(listener);

```

调用 [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#on) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
chatroom.on("onChatroomStatus", function (status: V2NIMChatroomStatus, err?: V2NIMError) {})
chatroom.on("onChatroomEntered", function () {})
chatroom.on("onChatroomExited", function (err?: V2NIMError) {})
chatroom.on("onChatroomKicked", function (kickedInfo: V2NIMChatroomKickedInfo) {})

```

调用 [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#on) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
// 在创建的实例对象上监听事件
chatroomInstance.on('chatroomStatus', (status, error) => {
    console.log('Chatroom status:', status, error)
})
chatroomInstance.on('chatroomEntered', () => {
    console.log('Chatroom entered successfully')
})
chatroomInstance.on('chatroomExited', (error) => {
    console.log('Chatroom exited:', error)
})
chatroomInstance.on('chatroomKicked', (kickedInfo) => {
    console.log('Chatroom kicked:', kickedInfo)
})

```

调用 [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#on) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
const chatroom = this.getInstance(instanceId)
chatroom.on("onChatroomStatus", (status: V2NIMChatroomStatus, err?: V2NIMError) => {
})
chatroom.on("onChatroomEntered", ()=> {
})
chatroom.on("onChatroomExited", (err?: V2NIMError) => {
})
chatroom.on("onChatroomKicked", (kickedInfo: V2NIMChatroomKickedInfo)=> {
})

```

调用 [`addChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#addChatroomClientListener) 方法注册聊天室实例监听器，包括聊天室连接状态变更、进出聊天室、被踢出聊天室。

```
//首先添加监听
await chatroomClient.addChatroomClientListener();
//添加成功后可以监听回到
chatroomClient!.onChatroomEntered.listen((event) {
    //print('ChatroomClient:onChatroomEntered');
}),
chatroomClient!.onChatroomExited.listen((event) {
    // print('ChatroomClient:onChatroomExited');
}),
chatroomClient!.onChatroomStatus.listen((event) {
    //print('ChatroomClient:onChatroomStatus');
}),
chatroomClient!.onChatroomKicked.listen((event) {
    // print('ChatroomClient:onChatroomKicked');
})

```

   安卓

如需移除聊天室实例相关监听器，可调用 [`removeChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#removeChatroomClientListener)。

```
chatroomClient.removeChatroomClientListener(chatroomClientListener);

```

 iOS

如需移除聊天室实例相关监听器，可调用 [`removeChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#removeChatroomClientListener)。

```
@interface ClientListener : NSObject <V2NIMChatroomClientListener>
- (void)addToClient:(NSInteger)clientId;
- (void)removeFromClient:(NSInteger)clientId;
@end

@implementation ClientListener
- (void)addToClient:(NSInteger)clientId
{
    V2NIMChatroomClient *instance = [V2NIMChatroomClient getInstance:clientId];
    [instance addChatroomClientListener:self];
}
- (void)removeFromClient:(NSInteger)clientId
{
    V2NIMChatroomClient *instance = [V2NIMChatroomClient getInstance:clientId];
    [instance removeChatroomClientListener:self];
}
- (void)onChatroomStatus:(V2NIMChatroomStatus)status
                   error:(nullable V2NIMError *)error
{
}
- (void)onChatroomEntered
{
}
- (void)onChatroomExited:(nullable V2NIMError *)error
{
}
- (void)onChatroomKicked:(V2NIMChatroomKickedInfo *)kickedInfo
{
}
@end

```

 macOS/Windows

如需移除聊天室实例相关监听器，可调用 [`removeChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#removeChatroomClientListener)。

```
V2NIMChatroomClientListener listener;
// ...
chatroomClient.addChatroomClientListener(listener);
// ...
chatroomClient.removeChatroomClientListener(listener);

```

 Web/uni-app/小程序

如需移除登录相关监听器，可调用 [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#off)。

```
chatroom.off("onChatroomStatus", function (status: V2NIMChatroomStatus, err?: V2NIMError) {})
chatroom.off("onChatroomEntered", function () {})
chatroom.off("onChatroomExited", function (err?: V2NIMError) {})
chatroom.off("onChatroomKicked", function (kickedInfo: V2NIMChatroomKickedInfo) {})

```

 Node.js/Electron

如需移除登录相关监听器，可调用 [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#off)。

```
// 在已有实例上取消监听事件
chatroomInstance.off('chatroomStatus')
chatroomInstance.off('chatroomEntered')
chatroomInstance.off('chatroomExited')
chatroomInstance.off('chatroomKicked')
// 或移除所有事件监听
chatroomInstance.removeAllListeners()

```

 鸿蒙

如需移除登录相关监听器，可调用 [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#off)。

```
chatroom.off("onChatroomStatus", function (status: V2NIMChatroomStatus, err?: V2NIMError) {})
chatroom.off("onChatroomEntered", function () {})
chatroom.off("onChatroomExited", function (err?: V2NIMError) {})
chatroom.off("onChatroomKicked", function (kickedInfo: V2NIMChatroomKickedInfo) {})

```

 Flutter

如需移除登录相关监听器，可调用 [`removeChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#removeChatroomClientListener)。

```
//取消后 SDK 将不会再触发回调
chatroomClient!.removeChatroomClientListener();

```

  AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

If you need to remove the chat room instance-related listener, you can call [`removeChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#removeChatroomClientListener).

```
chatroomClient.removeChatroomClientListener(chatroomClientListener);

```

如需移除聊天室实例相关监听器，可调用 [`removeChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#removeChatroomClientListener)。

```
@interface ClientListener : NSObject <V2NIMChatroomClientListener>
- (void)addToClient:(NSInteger)clientId;
- (void)removeFromClient:(NSInteger)clientId;
@end

@implementation ClientListener
- (void)addToClient:(NSInteger)clientId
{
    V2NIMChatroomClient *instance = [V2NIMChatroomClient getInstance:clientId];
    [instance addChatroomClientListener:self];
}
- (void)removeFromClient:(NSInteger)clientId
{
    V2NIMChatroomClient *instance = [V2NIMChatroomClient getInstance:clientId];
    [instance removeChatroomClientListener:self];
}
- (void)onChatroomStatus:(V2NIMChatroomStatus)status
                   error:(nullable V2NIMError *)error
{
}
- (void)onChatroomEntered
{
}
- (void)onChatroomExited:(nullable V2NIMError *)error
{
}
- (void)onChatroomKicked:(V2NIMChatroomKickedInfo *)kickedInfo
{
}
@end

```

如需移除聊天室实例相关监听器，可调用 [`removeChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#removeChatroomClientListener)。

```
V2NIMChatroomClientListener listener;
// ...
chatroomClient.addChatroomClientListener(listener);
// ...
chatroomClient.removeChatroomClientListener(listener);

```

如需移除登录相关监听器，可调用 [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#off)。

```
chatroom.off("onChatroomStatus", function (status: V2NIMChatroomStatus, err?: V2NIMError) {})
chatroom.off("onChatroomEntered", function () {})
chatroom.off("onChatroomExited", function (err?: V2NIMError) {})
chatroom.off("onChatroomKicked", function (kickedInfo: V2NIMChatroomKickedInfo) {})

```

如需移除登录相关监听器，可调用 [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#off)。

```
// 在已有实例上取消监听事件
chatroomInstance.off('chatroomStatus')
chatroomInstance.off('chatroomEntered')
chatroomInstance.off('chatroomExited')
chatroomInstance.off('chatroomKicked')
// 或移除所有事件监听
chatroomInstance.removeAllListeners()

```

如需移除登录相关监听器，可调用 [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#off)。

```
chatroom.off("onChatroomStatus", function (status: V2NIMChatroomStatus, err?: V2NIMError) {})
chatroom.off("onChatroomEntered", function () {})
chatroom.off("onChatroomExited", function (err?: V2NIMError) {})
chatroom.off("onChatroomKicked", function (kickedInfo: V2NIMChatroomKickedInfo) {})

```

如需移除登录相关监听器，可调用 [`removeChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#removeChatroomClientListener)。

```
//取消后 SDK 将不会再触发回调
chatroomClient!.removeChatroomClientListener();

```

## Step 5: Get the chat room Link address

Before logging in to the chat room, you need to get the chat room Link address in advance. It can be obtained in the following ways:

在初始化 SDK 时，您可以设置聊天室动态取址失败后的兜底地址列表（默认不需要）。当采用方式一或者方式二动态取址未获取到聊天室 Link 有效地址时，SDK 都会尝试使用 `chatroomLinkList` 兜底。

- Android：通过 `SDKOptions.serverConfig` 配置 `chatRoomLinkList`，设置聊天室地址列表。
- iOS：在 `NIMServerSetting` 中配置 `chatroomLinkList`，设置聊天室地址列表。
- 鸿蒙：在 `V2NIMChatroomInitParams` 中配置 `chatroomLinkList`，设置聊天室地址列表。

### Method 1 (recommended): Automatically obtain through LBS

Starting from version 10.9.20, NIM SDK supports the automatic acquisition of the optimal chat room address through LBS (Location Based Service), and you do not need to obtain the address manually.

在第六步登录聊天室时设置 `enableLbs = true`，那么 SDK 会优先通过 LBS 获取聊天室地址。`enableLbs = false` 时，SDK 会通过业务方提供的 `linkProvider` 获取聊天室地址。

### Method 2: Get through LinkProvider

- **场景一**：若当前客户端已 [登录 IM](https://doc.yunxin.163.com/messaging2/guide/Dk1MTY4MzA?platform=client)，那么可以通过 `getChatroomLinkAddress` 方法获取指定聊天室的地址。

 安卓

```
NIMClient.getService(V2NIMLoginService.class).getChatroomLinkAddress("123", new V2NIMSuccessCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                // get success
            }
        }, new V2NIMFailureCallback() {
            @Override
            public void onFailure(V2NIMError error) {
                // get failed
            }
        });

```

 iOS

```
NSString *roomId = @"36";
[NIMSDK.sharedSDK.v2LoginService getChatroomLinkAddress:roomId
                                                success:^(NSArray<NSString *> *links) {
                                                    // get success
                                                }
                                                failure:^(V2NIMError *error) {
                                                    // get failed
                                                }];

```

 macOS/Windows

```
loginService.getChatroomLinkAddress(
    "roomId",
    [](nstd::vector<nstd::string> linkAddresses) {
        // handle link addresses
    },
    [](V2NIMError error) {
        // handle error
    });

```

 Web/uni-app/小程序

```
const addressArray = await nim.V2NIMLoginService.getChatroomLinkAddress('36', isMiniApp)

```

 Node.js/Electron

```
// 如果要通过客户端获取聊天室 Link 地址，需要先登录 IM
// 创建 IM Client 实例
const nimClient = new NIM.V2NIMClient()
// 初始化 IM SDK
nimClient.init({ appkey })
// 登录 IM SDK
try {
    await nimClient.getLoginService().login(account, token, {})
} catch (error) {
    console.error('Login failed:', error)
    return
}
// 获取聊天室 Link 地址
let chatroomLinkAddresses;
try {
    chatroomLinkAddresses = await nimClient.getLoginService().getChatroomLinkAddress(chatroomId)
} catch (error) {
    console.error('Get chatroom link address failed:', error)
}

```

 鸿蒙

```
const addressArray = await nim.V2NIMLoginService.getChatroomLinkAddress('36')

```

 Flutter

```
final links = await NimCore.instance.loginService
.getChatroomLinkAddress(chatroomId1);

```

  AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
NIMClient.getService(V2NIMLoginService.class).getChatroomLinkAddress("123", new V2NIMSuccessCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                // get success
            }
        }, new V2NIMFailureCallback() {
            @Override
            public void onFailure(V2NIMError error) {
                // get failed
            }
        });

```

```
NSString *roomId = @"36";
[NIMSDK.sharedSDK.v2LoginService getChatroomLinkAddress:roomId
                                                success:^(NSArray<NSString *> *links) {
                                                    // get success
                                                }
                                                failure:^(V2NIMError *error) {
                                                    // get failed
                                                }];

```

```
loginService.getChatroomLinkAddress(
    "roomId",
    [](nstd::vector<nstd::string> linkAddresses) {
        // handle link addresses
    },
    [](V2NIMError error) {
        // handle error
    });

```

```
const addressArray = await nim.V2NIMLoginService.getChatroomLinkAddress('36', isMiniApp)

```

```
// 如果要通过客户端获取聊天室 Link 地址，需要先登录 IM
// 创建 IM Client 实例
const nimClient = new NIM.V2NIMClient()
// 初始化 IM SDK
nimClient.init({ appkey })
// 登录 IM SDK
try {
    await nimClient.getLoginService().login(account, token, {})
} catch (error) {
    console.error('Login failed:', error)
    return
}
// 获取聊天室 Link 地址
let chatroomLinkAddresses;
try {
    chatroomLinkAddresses = await nimClient.getLoginService().getChatroomLinkAddress(chatroomId)
} catch (error) {
    console.error('Get chatroom link address failed:', error)
}

```

```
const addressArray = await nim.V2NIMLoginService.getChatroomLinkAddress('36')

```

```
final links = await NimCore.instance.loginService
.getChatroomLinkAddress(chatroomId1);

```
- **Scenario**2: If the current client is not logged in to IM, it is obtained through the server API, because at this time NIM SDK cannot obtain the address of the chat room server. The client needs to request the address from the developer application server, and the application server needs to request the NetEase Yunxin server, and then it will ask Return the result to the client in the same way. For details, please refer to the server API [to get the chat room address](https://doc.yunxin.163.com/messaging2/server-apis/DU5MDQ1MDQ?platform=server).

## Step 6: Log in to the chat room

通过调用 `enter` 方法建立聊天室长连接，登录聊天室，对应用户手动输入登录账号密码的场景。

After calling, the SDK will automatically connect to the chat room, pass user information and return the login result. During the login process, users can actively cancel the login. If the server does not respond for a long time due to the network or other reasons and the user does not actively cancel the login, the SDK will automatically reconnect after 45 seconds and return the error code. For details, please refer to the [error code](https://doc.yunxin.163.com/messaging2/client-apis/DUxNjU3MzU?platform=client).

 安卓

从 10.9.20 版本开始，NIM SDK 支持以下两种地址获取方式：

- **LBS 自动获取**：设置 `enableLbs = true`，NIM SDK 自动获取最优服务器地址。
- **Link Provider**：自定义地址提供器，作为备用方案。

**配置规则：**

1. `enableLbs` 和 `linkProvider` 不能同时为空，至少必须指定其一。
2. 建议同时配置 LBS 和 Link Provider 确保连接稳定性。启用 LBS 后，如果 LBS 获取地址失败，将自动降级使用 `linkProvider`。

```
// 方式一：推荐配置（LBS + Link Provider 双重保障）
V2NIMChatroomEnterParams params =
        V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder.builder(linkProvider)
                .withEnableLbs(true) // 启用 LBS 自动获取最优地址
                .withLoginOption(loginOption)
                .build();

// 方式二：仅使用 LBS（不推荐，缺少备用方案）
V2NIMChatroomEnterParams params =
        V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder.builder(null)
                .withEnableLbs(true)
                .withLoginOption(loginOption)
                .build();

// 方式三：传统方式（仅使用 Link Provider）
V2NIMChatroomEnterParams params =
        V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder.builder(linkProvider)
                .withLoginOption(loginOption)
                .build();

V2NIMChatroomClient.getInstance(client.getInstanceId()).enter(roomId, params, success, failure);

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | String | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |
| `success` | `V2NIMSuccessCallback` | 是 | - | 进入聊天室成功回调，返回 `V2NIMChatroomEnterResult`。 |
| `failure` | `V2NIMFailureCallback` | 是 | - | 进入聊天室失败回调，返回 [错误码](https://doc.yunxin.163.com/messaging2/client-apis/DUxNjU3MzU?platform=client)。 |

 iOS

从 10.9.20 版本开始，NIM SDK 支持以下两种地址获取方式：

- **LBS 自动获取**：设置 `enableLbs = true`，NIM SDK 自动获取最优服务器地址。
- **Link Provider**：自定义地址提供器，作为备用方案。

**配置规则：**

1. `enableLbs` 和 `linkProvider` 不能同时为空，至少必须指定其一。
2. 建议同时配置 LBS 和 Link Provider 确保连接稳定性。启用 LBS 后，如果 LBS 获取地址失败，将自动降级使用 `linkProvider`。

```
// 方式一：推荐配置（LBS + Link Provider 双重保障）
id<V2NIMChatroomLinkProvider> linkProvider = // NIM SDK 发起或通过服务端 API 获取

V2NIMChatroomEnterParams *params = [[V2NIMChatroomEnterParams alloc] init];
params.enableLbs = YES; // 启用 LBS 自动获取最优地址
params.linkProvider = linkProvider; // 备用地址提供器
params.loginOption = loginOption;

// 方式二：仅使用 LBS（不推荐，缺少备用方案）
V2NIMChatroomEnterParams *params = [[V2NIMChatroomEnterParams alloc] init];
params.linkProvider = nil;
params.loginOption = loginOption;

// 方式三：传统方式（仅使用 Link Provider）
V2NIMChatroomEnterParams *params = [[V2NIMChatroomEnterParams alloc] init];
params.linkProvider = linkProvider;
params.loginOption = loginOption;

[client enter:roomId
  enterParams:params
      success:^(V2NIMChatroomEnterResult * _Nonnull result) {
    // 登录成功
} failure:^(V2NIMError * _Nonnull error) {
    // 登录失败
}];

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | NSString * | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |
| `success` | `V2NIMChatroomEnterResultCallback` | 是 | - | 进入聊天室成功回调，可自定义。 |
| `failure` | `V2NIMFailureCallback` | 是 | - | 进入聊天室失败回调，返回 [错误码](https://doc.yunxin.163.com/messaging2/client-apis/DUxNjU3MzU?platform=client)。 |

 macOS/Windows

```
virtual void enter(std::string roomId,
                   V2NIMChatroomEnterParams enterParams,
                   V2NIMSuccessCallback<V2NIMChatroomEnterResult> success,
                   V2NIMFailureCallback failure) = 0;

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | std::string | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |
| `success` | `V2NIMSuccessCallback` | 是 | - | 进入聊天室成功回调，返回 `V2NIMChatroomEnterResult`。 |
| `failure` | `V2NIMFailureCallback` | 是 | - | 进入聊天室失败回调，返回 [错误码](https://doc.yunxin.163.com/messaging2/client-apis/DUxNjU3MzU?platform=client)。 |

 Web/uni-app/小程序

```
enter(roomId: string, enterParams: V2NIMChatroomEnterParams): Promise<V2NIMChatroomEnterResult>

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | string | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |

 Node.js/Electron

```
enter(roomId, enterParams): Promise<V2NIMChatroomEnterResult>

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | string | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |

 鸿蒙

```
// 登录聊天室
try {
    const result = await chatroomInstance.enter(chatroomId, {
        accountId: account,
        token: token,
        roomNick: 'Your nickname',
        roomAvatar: 'https://example.com/your-avatar.png',
        linkProvider: (roomId, account) => {
            return chatroomLinkAddresses
        }
    })
    console.log(result)
} catch (error) {
    console.error('Enter chatroom failed:', error)
}

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | string | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |

 Flutter

```
Future<NIMResult<V2NIMChatroomEnterResult>> enter(String roomId,V2NIMChatroomEnterParams enterParams)

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | string | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |

  AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

Starting from version 10.9.20, NIM SDK supports the following two address acquisition methods:

- **LBS automatic acquisition**: set `enableLbs = true`, and NIM SDK will automatically obtain the optimal server address.
- **Link Provider**: Custom address provider as an alternative plan.

**Configuration rules:**

1. `enableLbs` 和 `linkProvider` 不能同时为空，至少必须指定其一。
2. It is recommended to configure LBS and Link Provider at the same time to ensure connection stability. After enabling LBS, if LBS fails to obtain the address, it will be automatically downgraded to `linkProvider`.

```
// 方式一：推荐配置（LBS + Link Provider 双重保障）
V2NIMChatroomEnterParams params =
        V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder.builder(linkProvider)
                .withEnableLbs(true) // 启用 LBS 自动获取最优地址
                .withLoginOption(loginOption)
                .build();

// 方式二：仅使用 LBS（不推荐，缺少备用方案）
V2NIMChatroomEnterParams params =
        V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder.builder(null)
                .withEnableLbs(true)
                .withLoginOption(loginOption)
                .build();

// 方式三：传统方式（仅使用 Link Provider）
V2NIMChatroomEnterParams params =
        V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder.builder(linkProvider)
                .withLoginOption(loginOption)
                .build();

V2NIMChatroomClient.getInstance(client.getInstanceId()).enter(roomId, params, success, failure);

```

| Parameter name | Type | Is it required? | Default value | Explain |
| --- | --- | --- | --- | --- |
| `roomId` | string | Yes | - | Chat room ID. If it is empty, illegal and does not exist, it will return the 191004 parameter error. |
| `enterParams` | `V2NIMChatroomEnterParams` | Yes | - | Enter the relevant parameters of the chat room. |
| `success` | `V2NIMSuccessCallback` | Yes | - | Enter the chat room and successfully call back, and return to `V2NIMChatroomEnterResult`. |
| `failure` | `V2NIMFailureCallback` | Yes | - | Entering the chat room failed to call back, returning the [error code](https://doc.yunxin.163.com/messaging2/client-apis/DUxNjU3MzU?platform=client). |

从 10.9.20 版本开始，NIM SDK 支持以下两种地址获取方式：

- **LBS 自动获取**：设置 `enableLbs = true`，NIM SDK 自动获取最优服务器地址。
- **Link Provider**：自定义地址提供器，作为备用方案。

**配置规则：**

1. `enableLbs` 和 `linkProvider` 不能同时为空，至少必须指定其一。
2. 建议同时配置 LBS 和 Link Provider 确保连接稳定性。启用 LBS 后，如果 LBS 获取地址失败，将自动降级使用 `linkProvider`。

```
// 方式一：推荐配置（LBS + Link Provider 双重保障）
id<V2NIMChatroomLinkProvider> linkProvider = // NIM SDK 发起或通过服务端 API 获取

V2NIMChatroomEnterParams *params = [[V2NIMChatroomEnterParams alloc] init];
params.enableLbs = YES; // 启用 LBS 自动获取最优地址
params.linkProvider = linkProvider; // 备用地址提供器
params.loginOption = loginOption;

// 方式二：仅使用 LBS（不推荐，缺少备用方案）
V2NIMChatroomEnterParams *params = [[V2NIMChatroomEnterParams alloc] init];
params.linkProvider = nil;
params.loginOption = loginOption;

// 方式三：传统方式（仅使用 Link Provider）
V2NIMChatroomEnterParams *params = [[V2NIMChatroomEnterParams alloc] init];
params.linkProvider = linkProvider;
params.loginOption = loginOption;

[client enter:roomId
  enterParams:params
      success:^(V2NIMChatroomEnterResult * _Nonnull result) {
    // 登录成功
} failure:^(V2NIMError * _Nonnull error) {
    // 登录失败
}];

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | NSString * | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |
| `success` | `V2NIMChatroomEnterResultCallback` | 是 | - | 进入聊天室成功回调，可自定义。 |
| `failure` | `V2NIMFailureCallback` | 是 | - | 进入聊天室失败回调，返回 [错误码](https://doc.yunxin.163.com/messaging2/client-apis/DUxNjU3MzU?platform=client)。 |

```
virtual void enter(std::string roomId,
                   V2NIMChatroomEnterParams enterParams,
                   V2NIMSuccessCallback<V2NIMChatroomEnterResult> success,
                   V2NIMFailureCallback failure) = 0;

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | std::string | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |
| `success` | `V2NIMSuccessCallback` | 是 | - | 进入聊天室成功回调，返回 `V2NIMChatroomEnterResult`。 |
| `failure` | `V2NIMFailureCallback` | 是 | - | 进入聊天室失败回调，返回 [错误码](https://doc.yunxin.163.com/messaging2/client-apis/DUxNjU3MzU?platform=client)。 |

```
enter(roomId: string, enterParams: V2NIMChatroomEnterParams): Promise<V2NIMChatroomEnterResult>

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | string | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |

```
enter(roomId, enterParams): Promise<V2NIMChatroomEnterResult>

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | string | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |

```
// 登录聊天室
try {
    const result = await chatroomInstance.enter(chatroomId, {
        accountId: account,
        token: token,
        roomNick: 'Your nickname',
        roomAvatar: 'https://example.com/your-avatar.png',
        linkProvider: (roomId, account) => {
            return chatroomLinkAddresses
        }
    })
    console.log(result)
} catch (error) {
    console.error('Enter chatroom failed:', error)
}

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | string | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |

```
Future<NIMResult<V2NIMChatroomEnterResult>> enter(String roomId,V2NIMChatroomEnterParams enterParams)

```

| 参数名称 | 类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `roomId` | string | 是 | - | 聊天室 ID。如果为空、不合法、不存在则返回 191004 参数错误。 |
| `enterParams` | `V2NIMChatroomEnterParams` | 是 | - | 进入聊天室相关参数。 |

`V2NIMChatroomEnterParams`Parameter description:

| Parameter name | Type | Is it required? | Default value | Explain |
| --- | --- | --- | --- | --- |
| `anonymousMode` | Boolean | No | false | Whether to enter the chat room in anonymous mode.
You can't send messages in anonymous mode, you can only receive messages. |
| `accountId` | string | No | null | User account number.If it is anonymous mode, you can not fill it in, and the account will be generated inside the SDK. Rule: nimanon_ UUID.randomUUID().toString(), global cache is recommended.If it is a non-anonymous mode, the parameter is required and must be a legal account. |
| `token` | string | No | null | Static token, static Token authentication (V2NIMChatroomLoginOption.authType=0) is required, otherwise it can be omit. |
| `roomNick` | string | No | Corresponding information of account | The nickname displayed after entering the chat room.
You can not fill in. By default, the relevant information corresponding to the account will be displayed. For example, in the anonymous mode, the default nickname is the account name. |
| `roomAvatar` | string | No | Corresponding information of account | 进入聊天室后显示的头像，可通过 `V2NIMStorageService.uploadFile` 方法上传头像。
You can leave it blank. By default, the relevant information corresponding to the account will be displayed. For example, in anonymous mode, the default avatar is empty. |
| `timeout` | Integer | No | 60 | 调用 `enter` 方法进入聊天室的超时时间。
If you fail to enter the chat room after that time, you will return to the failed callback. |
| `loginOption` | `V2NIMChatroomLoginOption` | No | null | Chat room login configuration. Including:鉴权方式 `authType`（`V2NIMLoginAuthType`），包括静态 Token 鉴权（默认）、动态 Token 鉴权、通过第三方回调鉴权。动态 Token 回调 `tokenProvider`（`V2NIMChatroomTokenProvider`），若使用动态 Token 鉴权（authType=1）则为必填。该回调在登录连接完成、登录鉴权校验前触发。若函数执行有异常或返回非预期内容（非法字符串），则登录中止并报错。用户登录业务扩展数据回调 `loginExtensionProvider`（`V2NIMChatroomLoginExtensionProvider`），若使用第三方回调鉴权（authType=2）则为必填。该回调在登录连接完成、登录鉴权校验前触发。若函数执行有异常或返回非预期内容（非法字符串），则登录中止并报错。 |
| `enableLbs` | Boolean | No | false | Whether to enable LBS.
If it is turned on, the SDK will automatically obtain the optimal server address.
若不开启，则 SDK 会使用 `linkProvider`（`V2NIMChatroomLinkProvider`）获取服务器地址。 |
| `linkProvider` | `V2NIMChatroomLinkProvider` | No | - | If LBS is turned on, SDK will automatically obtain the optimal server address.
若不开启 LBS，则 SDK 会使用 `linkProvider`（`V2NIMChatroomLinkProvider`）获取服务器地址，此时 NIM SDK 通过两种方式获取地址：若已登录 IM 且在线，则调用 `V2NIMLoginService.getChatroomLinkAddress` 方法获取聊天室连接地址。If you do not log in to IM, request the business server to [obtain the chat room address](https://doc.yunxin.163.com/messaging2/client-apis/DU5MDQ1MDQ?platform=server)through the new version of the server API.At present, this field is optional for Android/iOS/Hongmeng terminal, and other terminals are required. |
| `serverExtension` | string | No | null | For server-side expansion fields, it is recommended to use JSON format and multi-terminal synchronization. |
| `notificationExtension` | string | No | null | The maximum length of the extended field for entering the chat room notification is 2048 characters. |
| `tagConfig` | `V2NIMChatroomTagConfig` | No | null | Enter the tag information configuration of the chat room. |
| `locationConfig` | `V2NIMChatroomLocationConfig` | No | null | Enter the spatial location information configuration of the chat room. |
| `antispamConfig` | `V2NIMAntispamConfig` | No | null | Easy shield anti-garbage detection.If you don't need anti-spam detection, or if you have turned on the security pass, you don't need to configure it.If you do not use the default configuration of Security Pass and need to customize the audit rules, configure this field. |

### Static Token login

使用静态 Token 登录时，`V2NIMChatroomEnterParams.token` 必填，`V2NIMChatroomLoginOption.authType` 设置为 0（默认）。

 安卓

```
//创建 V2NIMChatroomClient 注：不要每次都 newInstance，用完不再使用需要 destroyInstance
V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.newInstance();
//获取 chatroomClient 的实例 ID，可以缓存起来，后面通过 instanceId 可以得到 V2NIMChatroomClient
int instanceId = chatroomClient.getInstanceId()

……

V2NIMChatroomLinkProvider chatroomLinkProvider = new V2NIMChatroomLinkProvider() {
    @Override
    public List<String> getLinkAddress(String roomId, String accountId) {
        return "聊天室 Link 地址";
    }
};
V2NIMChatroomEnterParams enterParams = V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder.builder(chatroomLinkProvider)
.withAccountId("账号名")
.withToken("静态 token")
// 按需设置
//.withRoomNick("进入聊天室后显示的昵称")
//.withRoomAvatar("进入聊天室后显示的头像")
//.withTimeout("进入方法超时时间")
//.withServerExtension("用户扩展字段")
//.withNotificationExtension("通知扩展字段，进入聊天室通知开发者扩展字段")
//.withTagConfig("进入聊天室标签信息配置")
//.withLocationConfig("进入聊天室空间位置信息配置")
//.withAntispamConfig("用户资料反垃圾检测配置");
.build();

V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.getInstance(instanceId);
if(chatroomClient != null){
    chatroomClient.enter(roomId, enterParams,
        new V2NIMSuccessCallback<V2NIMChatroomEnterResult>() {
            @Override
            public void onSuccess(V2NIMChatroomEnterResult result) {
                //进入成功
            }
        },
        new V2NIMFailureCallback() {
            @Override
            public void onFailure(V2NIMError error) {
                //进入失败
            }
        });
}

```

 iOS

```
@interface V2NIMEnterChatroom: NSObject <V2NIMChatroomLinkProvider>
@end
@implementation V2NIMEnterChatroom
- (void)enter
{
NSString *roomId = @"36";
//创建 V2NIMChatroomClient 注：不要每次都 newInstance，用完不再使用需要 destroyInstance
V2NIMChatroomClient *client = [V2NIMChatroomClient newInstance];
//获取 chatroomClient 的实例 ID，可以缓存起来，后面通过 instanceId 可以得到 V2NIMChatroomClient
NSInteger instanceId = client.getInstanceId;

V2NIMChatroomEnterParams *enterParams = [[V2NIMChatroomEnterParams alloc] init];
enterParams.linkProvider = self;
enterParams.accountId = @"账号名";
enterParams.token = @"静态 token";
// 按需设置
// enterParams.roomNick: 进入聊天室后显示的昵称
// enterParams.roomAvatar: 进入聊天室后显示的头像
// enterParams.timeout: 进入方法超时时间
// enterParams.serverExtension: 用户扩展字段
// enterParams.notificationExtension: 通知扩展字段，进入聊天室通知开发者扩展字段
// enterParams.tagConfig: 进入聊天室标签信息配置
// enterParams.locationConfig: 进入聊天室空间位置信息配置
// enterParams.antispamConfig: 用户资料反垃圾检测配置

V2NIMChatroomClient *chatroomClient = [V2NIMChatroomClient getInstance:instanceId];
[chatroomClient enter:roomId
          enterParams:enterParams
              success:^(V2NIMChatroomEnterResult *result)
              {
                  //进入成功
              }
              failure:^(V2NIMError *error)
              {
                    //进入失败
              }];
}
- (nullable NSArray<NSString *> *)getLinkAddress:(NSString *)roomId
                                       accountId:(NSString *)accountId
{
    return @[@"聊天室 Link 地址"];
}
@end

```

 macOS/Windows

```
V2NIMChatroomEnterParams enterParams;
enterParams.accountId = "accountId";
enterParams.token = "token";
enterParams.roomNick = "nick";
enterParams.roomAvatar = "avatar";
enterParams.linkProvider = [](nstd::string roomId, nstd::string account) {
    nstd::vector<nstd::string> linkAddresses;
    // get link addresses
    // ...
    return linkAddresses;
};
enterParams.serverExtension = "server extension";
enterParams.notificationExtension = "notification extension";
chatroomClient.enter(
    "roomId",
    enterParams,
    [](V2NIMChatroomEnterResult result) {
        // enter succeeded
    },
    [](V2NIMError error) {
        // enter failed, handle error
    });

```

 Web/uni-app/小程序

```
try {
    const chatroom = V2NIMChatroomClient.newInstance({
      appkey: 'YOUR_APPKEY',
      debugLevel: 'debug'
    })
    await chatroom.enter('YOUR_ROOM_ID', {
        accountId: 'YOUR_ACCOUNT_ID',
        token: 'YOUR_TOKEN'
    })
} catch (err) {
    // TODO failed, check code
    // console.log(err.code)
}

```

 Node.js/Electron

```
try {
    const result = await chatroomInstance.enter(chatroomId, {
        accountId: account,
        token: token,
        roomNick: 'Your nickname',
        roomAvatar: 'https://example.com/your-avatar.png',
        linkProvider: (roomId, account) => {
            return chatroomLinkAddresses
        }
    })
    console.log(result)
} catch (error) {
    console.error('Enter chatroom failed:', error)
    return
}

```

 鸿蒙

```
try {
    const chatroom = V2NIMChatroomClient.newInstance({
      appkey: 'YOUR_APPKEY',
      debugLevel: 'debug'
    })
    await chatroom.enter('YOUR_ROOM_ID', {
        accountId: 'YOUR_ACCOUNT_ID',
        token: 'YOUR_TOKEN'
    })
} catch (err) {
    // TODO failed, check code
    // console.log(err.code)
}

```

 Flutter

```
final links = await NimCore.instance.loginService
          .getChatroomLinkAddress(chatroomId1);

      final enterParams = V2NIMChatroomEnterParams(
          authType: NIMLoginAuthType.authTypeDefault,
          accountId: 'account',
          token: 'token');

      chatroomClient!.linkProvider =
          (int instanceId, String roomId, String accountId) async {
        return links.data!;
      };

      final enterResult = await chatroomClient!.enter(chatroomId1, enterParams);

```

  AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
//创建 V2NIMChatroomClient 注：不要每次都 newInstance，用完不再使用需要 destroyInstance
V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.newInstance();
//获取 chatroomClient 的实例 ID，可以缓存起来，后面通过 instanceId 可以得到 V2NIMChatroomClient
int instanceId = chatroomClient.getInstanceId()

……

V2NIMChatroomLinkProvider chatroomLinkProvider = new V2NIMChatroomLinkProvider() {
    @Override
    public List<String> getLinkAddress(String roomId, String accountId) {
        return "聊天室 Link 地址";
    }
};
V2NIMChatroomEnterParams enterParams = V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder.builder(chatroomLinkProvider)
.withAccountId("账号名")
.withToken("静态 token")
// 按需设置
//.withRoomNick("进入聊天室后显示的昵称")
//.withRoomAvatar("进入聊天室后显示的头像")
//.withTimeout("进入方法超时时间")
//.withServerExtension("用户扩展字段")
//.withNotificationExtension("通知扩展字段，进入聊天室通知开发者扩展字段")
//.withTagConfig("进入聊天室标签信息配置")
//.withLocationConfig("进入聊天室空间位置信息配置")
//.withAntispamConfig("用户资料反垃圾检测配置");
.build();

V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.getInstance(instanceId);
if(chatroomClient != null){
    chatroomClient.enter(roomId, enterParams,
        new V2NIMSuccessCallback<V2NIMChatroomEnterResult>() {
            @Override
            public void onSuccess(V2NIMChatroomEnterResult result) {
                //进入成功
            }
        },
        new V2NIMFailureCallback() {
            @Override
            public void onFailure(V2NIMError error) {
                //进入失败
            }
        });
}

```

```
@interface V2NIMEnterChatroom: NSObject <V2NIMChatroomLinkProvider>
@end
@implementation V2NIMEnterChatroom
- (void)enter
{
NSString *roomId = @"36";
//创建 V2NIMChatroomClient 注：不要每次都 newInstance，用完不再使用需要 destroyInstance
V2NIMChatroomClient *client = [V2NIMChatroomClient newInstance];
//获取 chatroomClient 的实例 ID，可以缓存起来，后面通过 instanceId 可以得到 V2NIMChatroomClient
NSInteger instanceId = client.getInstanceId;

V2NIMChatroomEnterParams *enterParams = [[V2NIMChatroomEnterParams alloc] init];
enterParams.linkProvider = self;
enterParams.accountId = @"账号名";
enterParams.token = @"静态 token";
// 按需设置
// enterParams.roomNick: 进入聊天室后显示的昵称
// enterParams.roomAvatar: 进入聊天室后显示的头像
// enterParams.timeout: 进入方法超时时间
// enterParams.serverExtension: 用户扩展字段
// enterParams.notificationExtension: 通知扩展字段，进入聊天室通知开发者扩展字段
// enterParams.tagConfig: 进入聊天室标签信息配置
// enterParams.locationConfig: 进入聊天室空间位置信息配置
// enterParams.antispamConfig: 用户资料反垃圾检测配置

V2NIMChatroomClient *chatroomClient = [V2NIMChatroomClient getInstance:instanceId];
[chatroomClient enter:roomId
          enterParams:enterParams
              success:^(V2NIMChatroomEnterResult *result)
              {
                  //进入成功
              }
              failure:^(V2NIMError *error)
              {
                    //进入失败
              }];
}
- (nullable NSArray<NSString *> *)getLinkAddress:(NSString *)roomId
                                       accountId:(NSString *)accountId
{
    return @[@"聊天室 Link 地址"];
}
@end

```

```
V2NIMChatroomEnterParams enterParams;
enterParams.accountId = "accountId";
enterParams.token = "token";
enterParams.roomNick = "nick";
enterParams.roomAvatar = "avatar";
enterParams.linkProvider = [](nstd::string roomId, nstd::string account) {
    nstd::vector<nstd::string> linkAddresses;
    // get link addresses
    // ...
    return linkAddresses;
};
enterParams.serverExtension = "server extension";
enterParams.notificationExtension = "notification extension";
chatroomClient.enter(
    "roomId",
    enterParams,
    [](V2NIMChatroomEnterResult result) {
        // enter succeeded
    },
    [](V2NIMError error) {
        // enter failed, handle error
    });

```

```
try {
    const chatroom = V2NIMChatroomClient.newInstance({
      appkey: 'YOUR_APPKEY',
      debugLevel: 'debug'
    })
    await chatroom.enter('YOUR_ROOM_ID', {
        accountId: 'YOUR_ACCOUNT_ID',
        token: 'YOUR_TOKEN'
    })
} catch (err) {
    // TODO failed, check code
    // console.log(err.code)
}

```

```
try {
    const result = await chatroomInstance.enter(chatroomId, {
        accountId: account,
        token: token,
        roomNick: 'Your nickname',
        roomAvatar: 'https://example.com/your-avatar.png',
        linkProvider: (roomId, account) => {
            return chatroomLinkAddresses
        }
    })
    console.log(result)
} catch (error) {
    console.error('Enter chatroom failed:', error)
    return
}

```

```
try {
    const chatroom = V2NIMChatroomClient.newInstance({
      appkey: 'YOUR_APPKEY',
      debugLevel: 'debug'
    })
    await chatroom.enter('YOUR_ROOM_ID', {
        accountId: 'YOUR_ACCOUNT_ID',
        token: 'YOUR_TOKEN'
    })
} catch (err) {
    // TODO failed, check code
    // console.log(err.code)
}

```

```
final links = await NimCore.instance.loginService
          .getChatroomLinkAddress(chatroomId1);

      final enterParams = V2NIMChatroomEnterParams(
          authType: NIMLoginAuthType.authTypeDefault,
          accountId: 'account',
          token: 'token');

      chatroomClient!.linkProvider =
          (int instanceId, String roomId, String accountId) async {
        return links.data!;
      };

      final enterResult = await chatroomClient!.enter(chatroomId1, enterParams);

```

### Dynamic Token Login

使用动态 Token 登录时，`V2NIMChatroomLoginOption.authType` 设置为 1，并设置获取动态 Token 回调 `V2NIMChatroomLoginOption.tokenProvider`。

 安卓

```
//创建 V2NIMChatroomClient 注：不要每次都 newInstance，用完不再使用需要 destroyInstance
V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.newInstance();
//获取 chatroomClient 的实例 ID，可以缓存起来，后面通过 instanceId 可以得到 V2NIMChatroomClient
int instanceId = chatroomClient.getInstanceId()

……

V2NIMChatroomLinkProvider chatroomLinkProvider = new V2NIMChatroomLinkProvider() {
    @Override
    public List<String> getLinkAddress(String roomId, String accountId) {
        return "聊天室 Link 地址";
    }
};
V2NIMChatroomLoginOption loginOption = V2NIMChatroomLoginOption.V2NIMChatroomLoginOptionBuilder.builder()
.withAuthType(V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN)
.withTokenProvider(new V2NIMChatroomTokenProvider() {
    @Override
    public String getToken(String roomId, String account) {
        return "动态登录 Token"
    }
})
.build();

V2NIMChatroomEnterParams enterParams = V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder.builder(chatroomLinkProvider)
.withAccountId("账号名")
.withLoginOption(loginOption)
// 按需设置
//.withRoomNick("进入聊天室后显示的昵称")
//.withRoomAvatar("进入聊天室后显示的头像")
//.withTimeout("进入方法超时时间")
//.withServerExtension("用户扩展字段")
//.withNotificationExtension("通知扩展字段，进入聊天室通知开发者扩展字段")
//.withTagConfig("进入聊天室标签信息配置")
//.withLocationConfig("进入聊天室空间位置信息配置")
//.withAntispamConfig("用户资料反垃圾检测配置");
.build();

V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.getInstance(instanceId);
if(chatroomClient != null){
    chatroomClient.enter(roomId, enterParams,
        new V2NIMSuccessCallback<V2NIMChatroomEnterResult>() {
            @Override
            public void onSuccess(V2NIMChatroomEnterResult result) {
                //进入成功
            }
        },
        new V2NIMFailureCallback() {
            @Override
            public void onFailure(V2NIMError error) {
                //进入失败
            }
        });
}

```

 iOS

```
@interface V2NIMEnterChatroom: NSObject <V2NIMChatroomLinkProvider, V2NIMChatroomTokenProvider>
@end
@implementation V2NIMEnterChatroom
- (void)enter
{
    NSString *roomId = @"36";
    //创建 V2NIMChatroomClient 注：不要每次都 newInstance，用完不再使用需要 destroyInstance
    V2NIMChatroomClient *client = [V2NIMChatroomClient newInstance];
    //获取 chatroomClient 的实例 ID，可以缓存起来，后面通过 instanceId 可以得到 V2NIMChatroomClient
    NSInteger instanceId = client.getInstanceId;

    ……

    V2NIMChatroomLoginOption *option = [[V2NIMChatroomLoginOption alloc] init];
    option.authType = V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN;
    option.tokenProvider = self;

    V2NIMChatroomEnterParams *enterParams = [[V2NIMChatroomEnterParams alloc] init];
    enterParams.linkProvider = self;
    enterParams.accountId = @"账号名";
    enterParams.loginOption = option;
    // 按需设置
    // enterParams.roomNick: 进入聊天室后显示的昵称
    // enterParams.roomAvatar: 进入聊天室后显示的头像
    // enterParams.timeout: 进入方法超时时间
    // enterParams.serverExtension: 用户扩展字段
    // enterParams.notificationExtension: 通知扩展字段，进入聊天室通知开发者扩展字段
    // enterParams.tagConfig: 进入聊天室标签信息配置
    // enterParams.locationConfig: 进入聊天室空间位置信息配置
    // enterParams.antispamConfig: 用户资料反垃圾检测配置

    V2NIMChatroomClient *chatroomClient = [V2NIMChatroomClient getInstance:instanceId];
    [chatroomClient enter:roomId
              enterParams:enterParams
                  success:^(V2NIMChatroomEnterResult *result)
                  {
                      //进入成功
                  }
                  failure:^(V2NIMError *error)
                  {
                        //进入失败
                  }];
    }

- (nullable NSArray<NSString *> *)getLinkAddress:(NSString *)roomId
                                       accountId:(NSString *)accountId
{
    return @[@"聊天室 Link 地址"];
}
- (nullable NSString *)getToken:(NSString *)roomId accountId:(NSString *)accountId {
    return @"动态登录 Token";
}
@end

```

 macOS/Windows

```
V2NIMChatroomEnterParams enterParams;
enterParams.accountId = "accountId";
enterParams.roomNick = "nick";
enterParams.roomAvatar = "avatar";
enterParams.linkProvider = [](nstd::string roomId, nstd::string account) {
    nstd::vector<nstd::string> linkAddresses;
    // get link addresses
    // ...
    return linkAddresses;
};
enterParams.loginOption.authType = V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN;
enterParams.loginOption.tokenProvider = [](nstd::string roomId, nstd::string account) {
    nstd::string dynamicToken;
    // get dynamic token
    // ...
    // return null on fail
    return dynamicToken;
};
enterParams.serverExtension = "server extension";
enterParams.notificationExtension = "notification extension";
chatroomClient.enter(
    "roomId",
    enterParams,
    [](V2NIMChatroomEnterResult result) {
        // enter succeeded
    },
    [](V2NIMError error) {
        // enter failed, handle error
    });

```

 Web/uni-app/小程序

```
try {
    const chatroom = V2NIMChatroomClient.newInstance({
      appkey: 'YOUR_APPKEY',
      debugLevel: 'debug'
    })
    await chatroom.enter('YOUR_ROOM_ID', {
        accountId: 'YOUR_ACCOUNT_ID',
        loginOption: {
            // authType == 1 时，登录方式为动态 token
            authType: 1,
            // 服务器动态签算动态 token
            tokenProvider: async function(appkey, roomId, account) {
                return await fetch('YOUR_SERVER_URL', {
                    body: JSON.stringify({
                        appkey, roomId, account
                    })
                })
            }
        }
    })
} catch (err) {
    // TODO failed, check code
    // console.log(err.code)
}

```

 Node.js/Electron

```
try {
    const result = await chatroomInstance.enter(chatroomId, {
        accountId: account,
        roomNick: 'Your nickname',
        roomAvatar: 'https://example.com/your-avatar.png',
        linkProvider: (roomId, account) => {
            return chatroomLinkAddresses
        },
        loginOption: {
            authType: 1, // V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN
            tokenProvider: async () => {
                // 从应用服务器获取动态Token
                const response = await fetch('https://your-server.com/api/get-dynamic-token', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ roomId, accountId })
                })
                const data = await response.json()
                return data.token
            }
        }
    })
    console.log(result)
} catch (error) {
    console.error('Enter chatroom failed:', error)
    return
}

```

 鸿蒙

```
try {
    const chatroom = V2NIMChatroomClient.newInstance({
      appkey: 'YOUR_APPKEY',
      debugLevel: 'debug'
    })
    await chatroom.enter('YOUR_ROOM_ID', {
        accountId: 'YOUR_ACCOUNT_ID',
        loginOption: {
            // authType == 1 时，登录方式为动态 token
            authType: 1,
            // 服务器动态签算动态 token
            tokenProvider: async function(appkey, roomId, account) {
                return await fetch('YOUR_SERVER_URL', {
                    body: JSON.stringify({
                        appkey, roomId, account
                    })
                })
            }
        }
    })
} catch (err) {
    // TODO failed, check code
    // console.log(err.code)
}

```

 Flutter

```
 final links = await NimCore.instance.loginService
          .getChatroomLinkAddress(chatroomId1);

      final enterParams = V2NIMChatroomEnterParams(
          authType: NIMLoginAuthType.authTypeDynamicToken,
          accountId: 'account');

      chatroomClient!.linkProvider =
          (int instanceId, String roomId, String accountId) async {
        return links.data!;
      };

      chatroomClient!.tokenProvider = (int instanceId, String roomId, String accountId) async {
        return 'token';
      };

      final enterResult = await chatroomClient!.enter(chatroomId1, enterParams);

```

  AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
//创建 V2NIMChatroomClient 注：不要每次都 newInstance，用完不再使用需要 destroyInstance
V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.newInstance();
//获取 chatroomClient 的实例 ID，可以缓存起来，后面通过 instanceId 可以得到 V2NIMChatroomClient
int instanceId = chatroomClient.getInstanceId()

……

V2NIMChatroomLinkProvider chatroomLinkProvider = new V2NIMChatroomLinkProvider() {
    @Override
    public List<String> getLinkAddress(String roomId, String accountId) {
        return "聊天室 Link 地址";
    }
};
V2NIMChatroomLoginOption loginOption = V2NIMChatroomLoginOption.V2NIMChatroomLoginOptionBuilder.builder()
.withAuthType(V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN)
.withTokenProvider(new V2NIMChatroomTokenProvider() {
    @Override
    public String getToken(String roomId, String account) {
        return "动态登录 Token"
    }
})
.build();

V2NIMChatroomEnterParams enterParams = V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder.builder(chatroomLinkProvider)
.withAccountId("账号名")
.withLoginOption(loginOption)
// 按需设置
//.withRoomNick("进入聊天室后显示的昵称")
//.withRoomAvatar("进入聊天室后显示的头像")
//.withTimeout("进入方法超时时间")
//.withServerExtension("用户扩展字段")
//.withNotificationExtension("通知扩展字段，进入聊天室通知开发者扩展字段")
//.withTagConfig("进入聊天室标签信息配置")
//.withLocationConfig("进入聊天室空间位置信息配置")
//.withAntispamConfig("用户资料反垃圾检测配置");
.build();

V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.getInstance(instanceId);
if(chatroomClient != null){
    chatroomClient.enter(roomId, enterParams,
        new V2NIMSuccessCallback<V2NIMChatroomEnterResult>() {
            @Override
            public void onSuccess(V2NIMChatroomEnterResult result) {
                //进入成功
            }
        },
        new V2NIMFailureCallback() {
            @Override
            public void onFailure(V2NIMError error) {
                //进入失败
            }
        });
}

```

```
@interface V2NIMEnterChatroom: NSObject <V2NIMChatroomLinkProvider, V2NIMChatroomTokenProvider>
@end
@implementation V2NIMEnterChatroom
- (void)enter
{
    NSString *roomId = @"36";
    //创建 V2NIMChatroomClient 注：不要每次都 newInstance，用完不再使用需要 destroyInstance
    V2NIMChatroomClient *client = [V2NIMChatroomClient newInstance];
    //获取 chatroomClient 的实例 ID，可以缓存起来，后面通过 instanceId 可以得到 V2NIMChatroomClient
    NSInteger instanceId = client.getInstanceId;

    ……

    V2NIMChatroomLoginOption *option = [[V2NIMChatroomLoginOption alloc] init];
    option.authType = V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN;
    option.tokenProvider = self;

    V2NIMChatroomEnterParams *enterParams = [[V2NIMChatroomEnterParams alloc] init];
    enterParams.linkProvider = self;
    enterParams.accountId = @"账号名";
    enterParams.loginOption = option;
    // 按需设置
    // enterParams.roomNick: 进入聊天室后显示的昵称
    // enterParams.roomAvatar: 进入聊天室后显示的头像
    // enterParams.timeout: 进入方法超时时间
    // enterParams.serverExtension: 用户扩展字段
    // enterParams.notificationExtension: 通知扩展字段，进入聊天室通知开发者扩展字段
    // enterParams.tagConfig: 进入聊天室标签信息配置
    // enterParams.locationConfig: 进入聊天室空间位置信息配置
    // enterParams.antispamConfig: 用户资料反垃圾检测配置

    V2NIMChatroomClient *chatroomClient = [V2NIMChatroomClient getInstance:instanceId];
    [chatroomClient enter:roomId
              enterParams:enterParams
                  success:^(V2NIMChatroomEnterResult *result)
                  {
                      //进入成功
                  }
                  failure:^(V2NIMError *error)
                  {
                        //进入失败
                  }];
    }

- (nullable NSArray<NSString *> *)getLinkAddress:(NSString *)roomId
                                       accountId:(NSString *)accountId
{
    return @[@"聊天室 Link 地址"];
}
- (nullable NSString *)getToken:(NSString *)roomId accountId:(NSString *)accountId {
    return @"动态登录 Token";
}
@end

```

```
V2NIMChatroomEnterParams enterParams;
enterParams.accountId = "accountId";
enterParams.roomNick = "nick";
enterParams.roomAvatar = "avatar";
enterParams.linkProvider = [](nstd::string roomId, nstd::string account) {
    nstd::vector<nstd::string> linkAddresses;
    // get link addresses
    // ...
    return linkAddresses;
};
enterParams.loginOption.authType = V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN;
enterParams.loginOption.tokenProvider = [](nstd::string roomId, nstd::string account) {
    nstd::string dynamicToken;
    // get dynamic token
    // ...
    // return null on fail
    return dynamicToken;
};
enterParams.serverExtension = "server extension";
enterParams.notificationExtension = "notification extension";
chatroomClient.enter(
    "roomId",
    enterParams,
    [](V2NIMChatroomEnterResult result) {
        // enter succeeded
    },
    [](V2NIMError error) {
        // enter failed, handle error
    });

```

```
try {
    const chatroom = V2NIMChatroomClient.newInstance({
      appkey: 'YOUR_APPKEY',
      debugLevel: 'debug'
    })
    await chatroom.enter('YOUR_ROOM_ID', {
        accountId: 'YOUR_ACCOUNT_ID',
        loginOption: {
            // authType == 1 时，登录方式为动态 token
            authType: 1,
            // 服务器动态签算动态 token
            tokenProvider: async function(appkey, roomId, account) {
                return await fetch('YOUR_SERVER_URL', {
                    body: JSON.stringify({
                        appkey, roomId, account
                    })
                })
            }
        }
    })
} catch (err) {
    // TODO failed, check code
    // console.log(err.code)
}

```

```
try {
    const result = await chatroomInstance.enter(chatroomId, {
        accountId: account,
        roomNick: 'Your nickname',
        roomAvatar: 'https://example.com/your-avatar.png',
        linkProvider: (roomId, account) => {
            return chatroomLinkAddresses
        },
        loginOption: {
            authType: 1, // V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN
            tokenProvider: async () => {
                // 从应用服务器获取动态Token
                const response = await fetch('https://your-server.com/api/get-dynamic-token', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ roomId, accountId })
                })
                const data = await response.json()
                return data.token
            }
        }
    })
    console.log(result)
} catch (error) {
    console.error('Enter chatroom failed:', error)
    return
}

```

```
try {
    const chatroom = V2NIMChatroomClient.newInstance({
      appkey: 'YOUR_APPKEY',
      debugLevel: 'debug'
    })
    await chatroom.enter('YOUR_ROOM_ID', {
        accountId: 'YOUR_ACCOUNT_ID',
        loginOption: {
            // authType == 1 时，登录方式为动态 token
            authType: 1,
            // 服务器动态签算动态 token
            tokenProvider: async function(appkey, roomId, account) {
                return await fetch('YOUR_SERVER_URL', {
                    body: JSON.stringify({
                        appkey, roomId, account
                    })
                })
            }
        }
    })
} catch (err) {
    // TODO failed, check code
    // console.log(err.code)
}

```

```
 final links = await NimCore.instance.loginService
          .getChatroomLinkAddress(chatroomId1);

      final enterParams = V2NIMChatroomEnterParams(
          authType: NIMLoginAuthType.authTypeDynamicToken,
          accountId: 'account');

      chatroomClient!.linkProvider =
          (int instanceId, String roomId, String accountId) async {
        return links.data!;
      };

      chatroomClient!.tokenProvider = (int instanceId, String roomId, String accountId) async {
        return 'token';
      };

      final enterResult = await chatroomClient!.enter(chatroomId1, enterParams);

```

### Third-party callback login

If this login method is adopted, the NetEase Yunxin chat room will not do login authentication, and the authentication work needs to be carried out by the designated third-party server (which can be an application server).

1. You need to [open and configure third-party services](https://doc.yunxin.163.com/messaging/server-apis/jI3ODc2ODE?platform=server#%E5%BC%80%E9%80%9A%E5%92%8C%E9%85%8D%E7%BD%AE%E7%AC%AC%E4%B8%89%E6%96%B9%E5%9B%9E%E8%B0%83)in the NetEase Cloud Console first.
2. 调用 `enter` 登录聊天室。
3. Launch a [login-related callback](https://doc.yunxin.163.com/messaging2/server-apis/jc3MzA5NTk?platform=server)request on the server, and the third-party server will vertify and determine whether the chat room login event is released. If it does not pass, the NetEase Cloud xin server will return the error code 302.

需要采用第三方服务器的动态登录扩展数据或动态 Token 进行鉴权，那么需要将 `V2NIMChatroomLoginOption.authType` 设置为 2，并设置获取动态登录扩展数据 `V2NIMChatroomLoginOption.loginExtensionProvider` 回调和动态 Token 回调 `V2NIMChatroomLoginOption.tokenProvider`，SDK 会在登录过程中获取第三方回调的动态扩展数据和动态 Token。

 安卓

```
//创建 V2NIMChatroomClient 注：不要每次都 newInstance，用完不再使用需要 destroyInstance
V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.newInstance();
//获取 chatroomClient 的实例 ID，可以缓存起来，后面通过 instanceId 可以得到 V2NIMChatroomClient
int instanceId = chatroomClient.getInstanceId()

……

V2NIMChatroomLinkProvider chatroomLinkProvider = new V2NIMChatroomLinkProvider() {
    @Override
    public List<String> getLinkAddress(String roomId, String accountId) {
        return "聊天室 Link 地址";
    }
};
V2NIMChatroomLoginOption loginOption = V2NIMChatroomLoginOption.V2NIMChatroomLoginOptionBuilder.builder()
.withAuthType(V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_THIRD_PARTY)
.withTokenProvider(new V2NIMChatroomTokenProvider() {
    @Override
    public String getToken(String roomId, String account) {
        return "第三方登录 Token"
    }
})
.withLoginExtensionProvider(new V2NIMChatroomLoginExtensionProvider() {
    @Override
    public String getLoginExtension(String roomId, String accountId) {
        return "聊天室登录扩展"
    }
});
.build();

V2NIMChatroomEnterParams enterParams = V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder.builder(chatroomLinkProvider)
.withAccountId("账号名")
.withLoginOption(loginOption)
// 按需设置
//.withRoomNick("进入聊天室后显示的昵称")
//.withRoomAvatar("进入聊天室后显示的头像")
//.withTimeout("进入方法超时时间")
//.withServerExtension("用户扩展字段")
//.withNotificationExtension("通知扩展字段，进入聊天室通知开发者扩展字段")
//.withTagConfig("进入聊天室标签信息配置")
//.withLocationConfig("进入聊天室空间位置信息配置")
//.withAntispamConfig("用户资料反垃圾检测配置");
.build();

V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.getInstance(instanceId);
if(chatroomClient != null){
    chatroomClient.enter(roomId, enterParams,
        new V2NIMSuccessCallback<V2NIMChatroomEnterResult>() {
            @Override
            public void onSuccess(V2NIMChatroomEnterResult result) {
                //进入成功
            }
        },
        new V2NIMFailureCallback() {
            @Override
            public void onFailure(V2NIMError error) {
                //进入失败
            }
        });
}

```

 iOS

```
@interface V2NIMEnterChatroom: NSObject <V2NIMChatroomLinkProvider, V2NIMChatroomTokenProvider, V2NIMChatroomLoginExtensionProvider>
@end
@implementation V2NIMEnterChatroom

- (void)enter
{
    NSString *roomId = @"36";
    //创建 V2NIMChatroomClient 注：不要每次都 newInstance，用完不再使用需要 destroyInstance
    V2NIMChatroomClient *client = [V2NIMChatroomClient newInstance];
    //获取 chatroomClient 的实例 ID，可以缓存起来，后面通过 instanceId 可以得到 V2NIMChatroomClient
    NSInteger instanceId = client.getInstanceId;

    V2NIMChatroomLoginOption *option = [[V2NIMChatroomLoginOption alloc] init];
    option.authType = V2NIM_LOGIN_AUTH_TYPE_THIRD_PARTY;
    option.tokenProvider = self;
    option.loginExtensionProvider = self;

    V2NIMChatroomEnterParams *enterParams = [[V2NIMChatroomEnterParams alloc] init];
    enterParams.linkProvider = self;
    enterParams.accountId = @"账号名";
    enterParams.loginOption = option;
    // 按需设置
    // enterParams.roomNick: 进入聊天室后显示的昵称
    // enterParams.roomAvatar: 进入聊天室后显示的头像
    // enterParams.timeout: 进入方法超时时间
    // enterParams.serverExtension: 用户扩展字段
    // enterParams.notificationExtension: 通知扩展字段，进入聊天室通知开发者扩展字段
    // enterParams.tagConfig: 进入聊天室标签信息配置
    // enterParams.locationConfig: 进入聊天室空间位置信息配置
    // enterParams.antispamConfig: 用户资料反垃圾检测配置

    V2NIMChatroomClient *chatroomClient = [V2NIMChatroomClient getInstance:instanceId];
    [chatroomClient enter:roomId
              enterParams:enterParams
                  success:^(V2NIMChatroomEnterResult *result)
                  {
                      //进入成功
                  }
                  failure:^(V2NIMError *error)
                  {
                        //进入失败
                  }];
    }

- (nullable NSArray<NSString *> *)getLinkAddress:(NSString *)roomId
                                       accountId:(NSString *)accountId
{
    return @[@"聊天室 Link 地址"];
}
- (nullable NSString *)getToken:(NSString *)roomId accountId:(NSString *)accountId {
    return @"动态登录 Token";
}
- (nullable NSString *)getLoginExtension:(NSString *)roomId accountId:(NSString *)accountId {
    return @"聊天室登录扩展";
}
@end

```

 macOS/Windows

```
V2NIMChatroomEnterParams enterParams;
enterParams.accountId = "accountId";
enterParams.roomNick = "nick";
enterParams.roomAvatar = "avatar";
enterParams.linkProvider = [](nstd::string roomId, nstd::string account) {
    nstd::vector<nstd::string> linkAddresses;
    // get link addresses
    // ...
    return linkAddresses;
};
enterParams.loginOption.authType = V2NIM_LOGIN_AUTH_TYPE_THIRD_PARTY;
enterParams.loginOption.loginExtensionProvider = [](nstd::string roomId, nstd::string account) {
    nstd::string loginExtension;
    // get login extension
    // ...
    // return null on fail
    return loginExtension;
};
enterParams.serverExtension = "server extension";
enterParams.notificationExtension = "notification extension";
chatroomClient.enter(
    "roomId",
    enterParams,
    [](V2NIMChatroomEnterResult result) {
        // enter succeeded
    },
    [](V2NIMError error) {
        // enter failed, handle error
    });

```

 Web/uni-app/小程序

```
try {
    const chatroom = V2NIMChatroomClient.newInstance({
      appkey: 'YOUR_APPKEY',
      debugLevel: 'debug'
    })
    await chatroom.enter('YOUR_ROOM_ID', {
        accountId: 'YOUR_ACCOUNT_ID',
        loginOption: {
            // authType == 2 时，登录方式为通过第三方回调鉴权
            authType: 2,
            // 服务器动态签算动态 token
            loginExtensionProvider: async function(account) {
                return await fetch('YOUR_SERVER_URL', {
                    body: JSON.stringify({
                        account
                    })
                })
            }
        }
    })
} catch (err) {
    // TODO failed, check code
    // console.log(err.code)
}

```

 Node.js/Electron

```
try {
    const result = await chatroomInstance.enter(chatroomId, {
        accountId: account,
        roomNick: 'Your nickname',
        roomAvatar: 'https://example.com/your-avatar.png',
        linkProvider: (roomId, account) => {
            return chatroomLinkAddresses
        },
        loginOption: {
            authType: 2, // V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_THIRD_PARTY
            tokenProvider: async () => {
                // 获取登录扩展数据用于第三方服务器鉴权
                const response = await fetch('https://your-server.com/api/get-login-extension', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ roomId, accountId })
                })
                const data = await response.json()
                return data.loginExtension
            }
        }
    })
    console.log(result)
} catch (error) {
    console.error('Enter chatroom failed:', error)
    return
}

```

 鸿蒙

```
try {
    const chatroom = V2NIMChatroomClient.newInstance({
      appkey: 'YOUR_APPKEY',
      debugLevel: 'debug'
    })
    await chatroom.enter('YOUR_ROOM_ID', {
        accountId: 'YOUR_ACCOUNT_ID',
        loginOption: {
            // authType == 2 时，登录方式为通过第三方回调鉴权
            authType: 2,
            // 服务器动态签算动态 token
            loginExtensionProvider: async function(account) {
                return await fetch('YOUR_SERVER_URL', {
                    body: JSON.stringify({
                        account
                    })
                })
            }
        }
    })
} catch (err) {
    // TODO failed, check code
    // console.log(err.code)
}

```

 Flutter

```
final links = await NimCore.instance.loginService
          .getChatroomLinkAddress(chatroomId1);

      final enterParams = V2NIMChatroomEnterParams(
          authType: NIMLoginAuthType.authTypeThirdParty,
          accountId: 'account');

      chatroomClient!.linkProvider =
          (int instanceId, String roomId, String accountId) async {
        return links.data!;
      };

      chatroomClient!.tokenProvider = (int instanceId, String roomId, String accountId) async {
        return 'third token';
      };

      final enterResult = await chatroomClient!.enter(chatroomId1, enterParams);

```

  AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
//创建 V2NIMChatroomClient 注：不要每次都 newInstance，用完不再使用需要 destroyInstance
V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.newInstance();
//获取 chatroomClient 的实例 ID，可以缓存起来，后面通过 instanceId 可以得到 V2NIMChatroomClient
int instanceId = chatroomClient.getInstanceId()

……

V2NIMChatroomLinkProvider chatroomLinkProvider = new V2NIMChatroomLinkProvider() {
    @Override
    public List<String> getLinkAddress(String roomId, String accountId) {
        return "聊天室 Link 地址";
    }
};
V2NIMChatroomLoginOption loginOption = V2NIMChatroomLoginOption.V2NIMChatroomLoginOptionBuilder.builder()
.withAuthType(V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_THIRD_PARTY)
.withTokenProvider(new V2NIMChatroomTokenProvider() {
    @Override
    public String getToken(String roomId, String account) {
        return "第三方登录 Token"
    }
})
.withLoginExtensionProvider(new V2NIMChatroomLoginExtensionProvider() {
    @Override
    public String getLoginExtension(String roomId, String accountId) {
        return "聊天室登录扩展"
    }
});
.build();

V2NIMChatroomEnterParams enterParams = V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder.builder(chatroomLinkProvider)
.withAccountId("账号名")
.withLoginOption(loginOption)
// 按需设置
//.withRoomNick("进入聊天室后显示的昵称")
//.withRoomAvatar("进入聊天室后显示的头像")
//.withTimeout("进入方法超时时间")
//.withServerExtension("用户扩展字段")
//.withNotificationExtension("通知扩展字段，进入聊天室通知开发者扩展字段")
//.withTagConfig("进入聊天室标签信息配置")
//.withLocationConfig("进入聊天室空间位置信息配置")
//.withAntispamConfig("用户资料反垃圾检测配置");
.build();

V2NIMChatroomClient chatroomClient = V2NIMChatroomClient.getInstance(instanceId);
if(chatroomClient != null){
    chatroomClient.enter(roomId, enterParams,
        new V2NIMSuccessCallback<V2NIMChatroomEnterResult>() {
            @Override
            public void onSuccess(V2NIMChatroomEnterResult result) {
                //进入成功
            }
        },
        new V2NIMFailureCallback() {
            @Override
            public void onFailure(V2NIMError error) {
                //进入失败
            }
        });
}

```

```
@interface V2NIMEnterChatroom: NSObject <V2NIMChatroomLinkProvider, V2NIMChatroomTokenProvider, V2NIMChatroomLoginExtensionProvider>
@end
@implementation V2NIMEnterChatroom

- (void)enter
{
    NSString *roomId = @"36";
    //创建 V2NIMChatroomClient 注：不要每次都 newInstance，用完不再使用需要 destroyInstance
    V2NIMChatroomClient *client = [V2NIMChatroomClient newInstance];
    //获取 chatroomClient 的实例 ID，可以缓存起来，后面通过 instanceId 可以得到 V2NIMChatroomClient
    NSInteger instanceId = client.getInstanceId;

    V2NIMChatroomLoginOption *option = [[V2NIMChatroomLoginOption alloc] init];
    option.authType = V2NIM_LOGIN_AUTH_TYPE_THIRD_PARTY;
    option.tokenProvider = self;
    option.loginExtensionProvider = self;

    V2NIMChatroomEnterParams *enterParams = [[V2NIMChatroomEnterParams alloc] init];
    enterParams.linkProvider = self;
    enterParams.accountId = @"账号名";
    enterParams.loginOption = option;
    // 按需设置
    // enterParams.roomNick: 进入聊天室后显示的昵称
    // enterParams.roomAvatar: 进入聊天室后显示的头像
    // enterParams.timeout: 进入方法超时时间
    // enterParams.serverExtension: 用户扩展字段
    // enterParams.notificationExtension: 通知扩展字段，进入聊天室通知开发者扩展字段
    // enterParams.tagConfig: 进入聊天室标签信息配置
    // enterParams.locationConfig: 进入聊天室空间位置信息配置
    // enterParams.antispamConfig: 用户资料反垃圾检测配置

    V2NIMChatroomClient *chatroomClient = [V2NIMChatroomClient getInstance:instanceId];
    [chatroomClient enter:roomId
              enterParams:enterParams
                  success:^(V2NIMChatroomEnterResult *result)
                  {
                      //进入成功
                  }
                  failure:^(V2NIMError *error)
                  {
                        //进入失败
                  }];
    }

- (nullable NSArray<NSString *> *)getLinkAddress:(NSString *)roomId
                                       accountId:(NSString *)accountId
{
    return @[@"聊天室 Link 地址"];
}
- (nullable NSString *)getToken:(NSString *)roomId accountId:(NSString *)accountId {
    return @"动态登录 Token";
}
- (nullable NSString *)getLoginExtension:(NSString *)roomId accountId:(NSString *)accountId {
    return @"聊天室登录扩展";
}
@end

```

```
V2NIMChatroomEnterParams enterParams;
enterParams.accountId = "accountId";
enterParams.roomNick = "nick";
enterParams.roomAvatar = "avatar";
enterParams.linkProvider = [](nstd::string roomId, nstd::string account) {
    nstd::vector<nstd::string> linkAddresses;
    // get link addresses
    // ...
    return linkAddresses;
};
enterParams.loginOption.authType = V2NIM_LOGIN_AUTH_TYPE_THIRD_PARTY;
enterParams.loginOption.loginExtensionProvider = [](nstd::string roomId, nstd::string account) {
    nstd::string loginExtension;
    // get login extension
    // ...
    // return null on fail
    return loginExtension;
};
enterParams.serverExtension = "server extension";
enterParams.notificationExtension = "notification extension";
chatroomClient.enter(
    "roomId",
    enterParams,
    [](V2NIMChatroomEnterResult result) {
        // enter succeeded
    },
    [](V2NIMError error) {
        // enter failed, handle error
    });

```

```
try {
    const chatroom = V2NIMChatroomClient.newInstance({
      appkey: 'YOUR_APPKEY',
      debugLevel: 'debug'
    })
    await chatroom.enter('YOUR_ROOM_ID', {
        accountId: 'YOUR_ACCOUNT_ID',
        loginOption: {
            // authType == 2 时，登录方式为通过第三方回调鉴权
            authType: 2,
            // 服务器动态签算动态 token
            loginExtensionProvider: async function(account) {
                return await fetch('YOUR_SERVER_URL', {
                    body: JSON.stringify({
                        account
                    })
                })
            }
        }
    })
} catch (err) {
    // TODO failed, check code
    // console.log(err.code)
}

```

```
try {
    const result = await chatroomInstance.enter(chatroomId, {
        accountId: account,
        roomNick: 'Your nickname',
        roomAvatar: 'https://example.com/your-avatar.png',
        linkProvider: (roomId, account) => {
            return chatroomLinkAddresses
        },
        loginOption: {
            authType: 2, // V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_THIRD_PARTY
            tokenProvider: async () => {
                // 获取登录扩展数据用于第三方服务器鉴权
                const response = await fetch('https://your-server.com/api/get-login-extension', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ roomId, accountId })
                })
                const data = await response.json()
                return data.loginExtension
            }
        }
    })
    console.log(result)
} catch (error) {
    console.error('Enter chatroom failed:', error)
    return
}

```

```
try {
    const chatroom = V2NIMChatroomClient.newInstance({
      appkey: 'YOUR_APPKEY',
      debugLevel: 'debug'
    })
    await chatroom.enter('YOUR_ROOM_ID', {
        accountId: 'YOUR_ACCOUNT_ID',
        loginOption: {
            // authType == 2 时，登录方式为通过第三方回调鉴权
            authType: 2,
            // 服务器动态签算动态 token
            loginExtensionProvider: async function(account) {
                return await fetch('YOUR_SERVER_URL', {
                    body: JSON.stringify({
                        account
                    })
                })
            }
        }
    })
} catch (err) {
    // TODO failed, check code
    // console.log(err.code)
}

```

```
final links = await NimCore.instance.loginService
          .getChatroomLinkAddress(chatroomId1);

      final enterParams = V2NIMChatroomEnterParams(
          authType: NIMLoginAuthType.authTypeThirdParty,
          accountId: 'account');

      chatroomClient!.linkProvider =
          (int instanceId, String roomId, String accountId) async {
        return links.data!;
      };

      chatroomClient!.tokenProvider = (int instanceId, String roomId, String accountId) async {
        return 'third token';
      };

      final enterResult = await chatroomClient!.enter(chatroomId1, enterParams);

```

## Step 7: Get the chat room service

登录聊天室成功后，调用 `getChatroomService` 方法获取聊天室服务。后续聊天室相关操作（聊天室成员、消息等）均在返回的 `V2NIMChatroomService` 类中实现。

 安卓

```
V2NIMChatroomService chatroomService = chatroomClient.getChatroomService()

```

 iOS

```
[[V2NIMChatroomClient getInstance:instanceId] getChatroomService];

```

 macOS/Windows

```
auto& chatroomService = client.getChatroomService();

```

 Web/uni-app/小程序

Web/uni-app/小程序可跳过此步骤。

 Node.js/Electron

```
const chatroomService = chatroomInstance.getChatroomService()

```

 鸿蒙

```
const client: V2NIMChatroomClient = this.getInstance(instanceId)
const ret = client.chatroomService

```

 Flutter

```
var chatroomService = chatroomClient?.getChatroomService()

```

  AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomService chatroomService = chatroomClient.getChatroomService()

```

```
[[V2NIMChatroomClient getInstance:instanceId] getChatroomService];

```

```
auto& chatroomService = client.getChatroomService();

```

Web/uni-app/小程序可跳过此步骤。

```
const chatroomService = chatroomInstance.getChatroomService()

```

```
const client: V2NIMChatroomClient = this.getInstance(instanceId)
const ret = client.chatroomService

```

```
var chatroomService = chatroomClient?.getChatroomService()

```

## Step 8: Log out of the chat room

注销聊天室登录（即退出聊天室）后，会断开聊天室对应的连接，不再接收关于此聊天室的任何消息。如果应用退出时聊天室仍处于登录状态，请手动调用 `exit` 方法退出聊天室。

调用 `exit` 方法退出聊天室成功后，会收到 `onChatroomExited` 回调。如果已在网易云信控制台开启了聊天室用户进出消息系统下发功能，聊天室内所有其他成员会收到回调 `onChatroomMemberExit`。如未开启，可参考 [开通和配置聊天室功能](https://doc.yunxin.163.com/messaging2/guide/DUyMzAxNzg?platform=client#%E9%85%8D%E7%BD%AE%E8%81%8A%E5%A4%A9%E5%AE%A4%E5%AD%90%E5%8A%9F%E8%83%BD)。

After exiting the chat room, the chat room instance (instanceId) will be unbinded from the chat room (roomId).

 安卓

```
chatroomCore.exit();

```

 iOS

```
[[V2NIMChatroomClient getInstance:instanceId] exit];

```

 macOS/Windows

```
chatroomClient.exit();

```

 Web/uni-app/小程序

```
chatroom.exit()

```

 Node.js/Electron

```
chatroomInstance.exit()

```

 鸿蒙

```
const client = this.getInstance(instanceId)
client.exit()

```

 Flutter

```
chatroomClient!.exit();

```

  AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
chatroomCore.exit();

```

```
[[V2NIMChatroomClient getInstance:instanceId] exit];

```

```
chatroomClient.exit();

```

```
chatroom.exit()

```

```
chatroomInstance.exit()

```

```
const client = this.getInstance(instanceId)
client.exit()

```

```
chatroomClient!.exit();

```

## Step 9: Destroy the chat room example

调用 `destroyInstance` 方法销毁聊天室实例。

 安卓

```
V2NIMChatroomClient.destroyInstance(instanceId);

```

 iOS

```
[V2NIMChatroomClient destroyInstance:instanceId];

```

 macOS/Windows

```
uint32_t instanceId{0};
// get instanceId from cache
// ...
V2NIMChatroomClient::destroyInstance(instanceId);

```

 Web/uni-app/小程序

```
V2NIMChatroomClient.destroyInstance(this.instanceId)

```

 Node.js/Electron

```
// 销毁聊天室实例，NIM is required from node-nim
NIM.V2NIMChatroomClient.destroyInstance(chatroomInstance.getInstanceId())
NIM.V2NIMChatroomClient.uninit()

```

 鸿蒙

```
V2NIMChatroomClient.destroyInstance(this.instanceId)

```

 Flutter

```
V2NIMChatroomClient.destroyInstance(instanceId);

```

  AndroidiOSmacOS/WindowsWeb/uni-app/appletNode.js/ElectronHongmengFlutter

```
V2NIMChatroomClient.destroyInstance(instanceId);

```

```
[V2NIMChatroomClient destroyInstance:instanceId];

```

```
uint32_t instanceId{0};
// get instanceId from cache
// ...
V2NIMChatroomClient::destroyInstance(instanceId);

```

```
V2NIMChatroomClient.destroyInstance(this.instanceId)

```

```
// 销毁聊天室实例，NIM is required from node-nim
NIM.V2NIMChatroomClient.destroyInstance(chatroomInstance.getInstanceId())
NIM.V2NIMChatroomClient.uninit()

```

```
V2NIMChatroomClient.destroyInstance(this.instanceId)

```

```
V2NIMChatroomClient.destroyInstance(instanceId);

```

## Relevant information

### Disconnect the network and reconnect

SDK provides an automatic reconnection mechanism (automatically re-establish the connection with the NetEase cloud server and log in again).

After successfully logging in to the chat room, if the network timeout is caused by poor network status or other problems, the SDK will continue to reconnect automatically and strategically. **At this time**, **there is no need for upper developers to do additional re-login logic**.

### Multi-terminal login and mutual kick

At present, NIM SDK supports the configuration of two different chat room multi-terminal login and mutual kicking strategies: only one end is allowed to log in, and both ends can log in online at the same time.

You can realize multi-terminal login and kicking in the chat room in two ways.

**Method 1: Configure through NetEase Cloud Trust Console**

At present, NIM SDK supports the configuration of two different chat room multi-terminal login strategies through the NetEase Cloud Trust console:

- Only one end is allowed to log in, and Windows, Web, Android and iOS kick each other. The same account is only allowed to log in on one device. When the account is successfully logged in on another device, the new device will kick the old device offline.
- All terminals can log in online at the same time. Up to 10 devices can be online at the same time. Within the maximum number of devices, all new devices will log in again and the old devices online will not be kicked offline.

Through this configuration, the multi-terminal login of the chat room can be automatically controlled. For specific configuration, please refer to the [opening and configuration of chat room functions](https://doc.yunxin.163.com/messaging2/guide/DUyMzAxNzg?platform=client#%E9%85%8D%E7%BD%AE%E8%81%8A%E5%A4%A9%E5%AE%A4%E5%AD%90%E5%8A%9F%E8%83%BD).

- After the console modifies the logic of multi-terminal kicking, the next time a new device logs in, it will be verified based on the new multi-terminal kicking strategy. Devices that have been connected will not be forcibly kicked out because of the modification of the policy.
- If a device repeatedly logs in to the same chat room, the later login will disconnect the previous long connection. At this time, it will trigger another CC to enter the chat room, but it will not trigger the copy to leave the chat room. For the copy of entering and leaving the chat room (eventType=9), please refer to the [chat room members entering and leaving the chat room event copy](https://doc.yunxin.163.com/messaging/server-apis/TcxNzU4NzU?platform=server#%E8%81%8A%E5%A4%A9%E5%AE%A4%E6%88%90%E5%91%98%E8%BF%9B%E5%87%BA%E8%81%8A%E5%A4%A9%E5%AE%A4%E4%BA%8B%E4%BB%B6%E6%8A%84%E9%80%81).

**Method 2: Actively kick the other end off the line**

```
#mermaid-render-1 {font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:16px;fill:#333;}#mermaid-render-1 .error-icon{fill:#552222;}#mermaid-render-1 .error-text{fill:#552222;stroke:#552222;}#mermaid-render-1 .edge-thickness-normal{stroke-width:2px;}#mermaid-render-1 .edge-thickness-thick{stroke-width:3.5px;}#mermaid-render-1 .edge-pattern-solid{stroke-dasharray:0;}#mermaid-render-1 .edge-pattern-dashed{stroke-dasharray:3;}#mermaid-render-1 .edge-pattern-dotted{stroke-dasharray:2;}#mermaid-render-1 .marker{fill:#333333;stroke:#333333;}#mermaid-render-1 .marker.cross{stroke:#333333;}#mermaid-render-1 svg{font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:16px;}#mermaid-render-1 .actor{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-1 text.actor>tspan{fill:black;stroke:none;}#mermaid-render-1 .actor-line{stroke:grey;}#mermaid-render-1 .messageLine0{stroke-width:1.5;stroke-dasharray:none;stroke:#333;}#mermaid-render-1 .messageLine1{stroke-width:1.5;stroke-dasharray:2,2;stroke:#333;}#mermaid-render-1 #arrowhead path{fill:#333;stroke:#333;}#mermaid-render-1 .sequenceNumber{fill:white;}#mermaid-render-1 #sequencenumber{fill:#333;}#mermaid-render-1 #crosshead path{fill:#333;stroke:#333;}#mermaid-render-1 .messageText{fill:#333;stroke:none;}#mermaid-render-1 .labelBox{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-1 .labelText,#mermaid-render-1 .labelText>tspan{fill:black;stroke:none;}#mermaid-render-1 .loopText,#mermaid-render-1 .loopText>tspan{fill:black;stroke:none;}#mermaid-render-1 .loopLine{stroke-width:2px;stroke-dasharray:2,2;stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);}#mermaid-render-1 .note{stroke:#aaaa33;fill:#fff5ad;}#mermaid-render-1 .noteText,#mermaid-render-1 .noteText>tspan{fill:black;stroke:none;}#mermaid-render-1 .activation0{fill:#f4f4f4;stroke:#666;}#mermaid-render-1 .activation1{fill:#f4f4f4;stroke:#666;}#mermaid-render-1 .activation2{fill:#f4f4f4;stroke:#666;}#mermaid-render-1 .actorPopupMenu{position:absolute;}#mermaid-render-1 .actorPopupMenuPanel{position:absolute;fill:#ECECFF;box-shadow:0px 8px 16px 0px rgba(0,0,0,0.2);filter:drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4));}#mermaid-render-1 .actor-man line{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-1 .actor-man circle,#mermaid-render-1 line{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;stroke-width:2px;}#mermaid-render-1 :root{--mermaid-font-family:"trebuchet ms",verdana,arial,sans-serif;}This endNIMOther endspar[Step 1: Log in to the chat room on this terminal]Use the same account to log in to the chat roompar[Steps2: Log in to the chat room on other terminals]The result includes the reason for being kicked (kickedReason)And the kicked extension field (serverExtension)par[Step 3: Kick the other end offline at this end]Log in to the chat roomListen to the incident of being kicked off the line(onChatroomExited)Log in to the chat roomTake the initiative to kick the other ends off the line(V2NIMChatroomServiceInformation on the result of being kicked(V2NIMChatroomKickedInfo)This endNIMOther ends
```

```
sequenceDiagram

par 步骤 1：本端登录聊天室
本端 ->> NIM: 登录聊天室
end
par 步骤 2：其他端登录聊天室
其他端 ->> NIM: 监听被踢下线事件<br>(onChatroomExited)
其他端 ->> NIM: 登录聊天室
note left of 其他端: 使用相同账号登录聊天室
end
par 步骤 3：本端将其他端踢下线
本端 ->> NIM: 主动将其他端踢下线<br>(V2NIMChatroomService#kickMember)
NIM -> 其他端: 被踢的结果信息<br>(V2NIMChatroomKickedInfo)
note left of 其他端: 结果包含被踢原因(kickedReason)<br>和被踢的扩展字段(serverExtension)
end

```

1. Log in to the chat room on this terminal (the kicker).
2. 同一账号的其他客户端（被踢方）注册 `onChatroomExited` 事件回调并登录聊天室。
3. 本端（踢人方）调用 `V2NIMChatroomService#kickMember` 方法将其他同时登录的客户端踢下线。

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
4. After other clients (the kicked party) are kicked offline, they will receive the kicked callback message (`V2NIMChatroomKickedInfo`), including the kicked Reason (`kickedReason`) and the kicked extension field (`serverExtension`).

After receiving the kickback callback, it is recommended to log out and switch to the login interface.

## Involving interfaces

 安卓/iOS/macOS/Windows

| API | 说明 |
| --- | --- |
| [`newInstance`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#newInstance) | 创建聊天室实例 |
| [`addChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#addChatroomClientListener) | 注册聊天室实例监听器 |
| [`removeChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#removeChatroomClientListener) | 取消注册聊天室实例监听器 |
| [`getChatroomLinkAddress`](https://doc.yunxin.163.com/messaging2/client-apis/TQ5NTUwNzQ?platform=client#getChatroomLinkAddress) | 获取指定聊天室的地址 |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#enter) | 进入聊天室 |
| [`V2NIMStorageService.uploadFile`](https://doc.yunxin.163.com/messaging2/client-apis/zQ0MDc5MjI?platform=client#uploadFile) | 上传文件 |
| [`V2NIMChatroomLoginOption.tokenProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginOption) | 获取动态 Token |
| [`V2NIMChatroomLoginOption.loginExtensionProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginOption) | 获取动态登录扩展字段 |
| [`getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService) | 获取聊天室服务 |
| [`exit`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#exit) | 退出聊天室 |
| [`destroyInstance`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#destroyInstance) | 销毁聊天室实例 |
| [`V2NIMChatroomService#kickMember`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#kickMember) | 将其他同时登录的客户端踢下线 |
| [`V2NIMChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client) | 聊天室服务类 |
| [`V2NIMChatroomEnterParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams) | 进入聊天室相关参数 |
| [`V2NIMChatroomEnterResult`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterResult) | 进入聊天室成功回调结果 |
| [`V2NIMChatroomLoginOption`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginOption) | 聊天室登录配置 |
| [`V2NIMLoginAuthType`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMLoginAuthType) | 登录鉴权方式 |
| [`V2NIMChatroomTokenProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomTokenProvider) | 动态 Token 回调 |
| [`V2NIMChatroomLoginExtensionProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginExtensionProvider) | 动态登录扩展数据回调 |
| [`V2NIMChatroomLinkProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLinkProvider) | 聊天室连接地址回调 |
| [`V2NIMChatroomTagConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomTagConfig) | 进入聊天室的标签信息配置 |
| [`V2NIMChatroomLocationConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLocationConfig) | 进入聊天室的空间位置信息配置 |
| [`V2NIMAntispamConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMAntispamConfig) | 易盾反垃圾检测配置信息 |
| [`V2NIMChatroomKickedInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomKickedInfo) | 被踢出聊天室的回调信息 |

 Web/uni-app/小程序/鸿蒙/Node.js/Electron

| API | 说明 |
| --- | --- |
| [`newInstance`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#newInstance) | 创建聊天室实例 |
| [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#on) | 注册聊天室实例监听器 |
| [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#off) | 取消注册聊天室实例监听器 |
| [`getChatroomLinkAddress`](https://doc.yunxin.163.com/messaging2/client-apis/TQ5NTUwNzQ?platform=client#getChatroomLinkAddress) | 获取指定聊天室的地址 |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#enter) | 进入聊天室 |
| [`V2NIMStorageService.uploadFile`](https://doc.yunxin.163.com/messaging2/client-apis/zQ0MDc5MjI?platform=client#uploadFile) | 上传文件 |
| [`V2NIMChatroomLoginOption.tokenProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginOption) | 获取动态 Token |
| [`V2NIMChatroomLoginOption.loginExtensionProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginOption) | 获取动态登录扩展字段 |
| [`getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService) | 获取聊天室服务 |
| [`exit`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#exit) | 退出聊天室 |
| [`destroyInstance`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#destroyInstance) | 销毁聊天室实例 |
| [`V2NIMChatroomService#kickMember`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#kickMember) | 将其他同时登录的客户端踢下线 |
| [`V2NIMChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client) | 聊天室服务类 |
| [`V2NIMChatroomInitParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomInitParams) | 聊天室初始化参数（仅 Web 和 鸿蒙） |
| [`V2NIMChatroomEnterParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams) | 进入聊天室相关参数 |
| [`V2NIMChatroomEnterResult`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterResult) | 进入聊天室成功回调结果 |
| [`V2NIMChatroomLoginOption`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginOption) | 聊天室登录配置 |
| [`V2NIMLoginAuthType`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMLoginAuthType) | 登录鉴权方式 |
| [`V2NIMChatroomTokenProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomTokenProvider) | 动态 Token 回调 |
| [`V2NIMChatroomLoginExtensionProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginExtensionProvider) | 动态登录扩展数据回调 |
| [`V2NIMChatroomLinkProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLinkProvider) | 聊天室连接地址回调 |
| [`V2NIMChatroomTagConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomTagConfig) | 进入聊天室的标签信息配置 |
| [`V2NIMChatroomLocationConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLocationConfig) | 进入聊天室的空间位置信息配置 |
| [`V2NIMAntispamConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMAntispamConfig) | 易盾反垃圾检测配置信息 |
| [`V2NIMChatroomKickedInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomKickedInfo) | 被踢出聊天室的回调信息 |

 Flutter

| API | 说明 |
| --- | --- |
| [`newInstance`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#newInstance) | 创建聊天室实例 |
| [`addChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#addChatroomClientListener) | 注册聊天室实例监听器 |
| [`removeChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#removeChatroomClientListener) | 取消注册聊天室实例监听器 |
| [`LoginService.getChatroomLinkAddress`](https://doc.yunxin.163.com/messaging2/client-apis/Dc3NDM0NTI?platform=client#getChatroomLinkAddress) | 获取指定聊天室的地址 |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#enter) | 进入聊天室 |
| [`StorageService.uploadFile`](https://doc.yunxin.163.com/messaging2/client-apis/zQ0MDc5MjI?platform=client#uploadFile) | 上传文件 |
| [`V2NIMChatroomTokenProvider`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomTokenProvider) | 获取动态 Token |
| [`V2NIMChatroomLoginExtensionProvider`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomLoginExtensionProvider) | 获取动态登录扩展字段 |
| [`getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomService) | 获取聊天室服务 |
| [`exit`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#exit) | 退出聊天室 |
| [`destroyInstance`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#destroyInstance) | 销毁聊天室实例 |
| [`V2NIMChatroomService#kickMember`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#kickMember) | 将其他同时登录的客户端踢下线 |
| [`V2NIMChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client) | 聊天室服务类 |
| [`V2NIMChatroomEnterParams`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomEnterParams) | 进入聊天室相关参数 |
| [`V2NIMChatroomEnterResult`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomEnterResult) | 进入聊天室成功回调结果 |
| [`NIMLoginAuthType`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMLoginAuthType) | 登录鉴权方式 |
| [`V2NIMChatroomTokenProvider`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomTokenProvider) | 动态 Token 回调 |
| [`V2NIMChatroomLoginExtensionProvider`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomLoginExtensionProvider) | 动态登录扩展数据回调 |
| [`V2NIMChatroomLinkProvider`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomLinkProvider) | 聊天室连接地址回调 |
| [`V2NIMChatroomTagConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomTagConfig) | 进入聊天室的标签信息配置 |
| [`V2NIMChatroomLocationConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomLocationConfig) | 进入聊天室的空间位置信息配置 |
| [`NIMAntispamConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMAntispamConfig) | 易盾反垃圾检测配置信息 |
| [`V2NIMChatroomKickedInfo`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomKickedInfo) | 被踢出聊天室的回调信息 |

   Android/iOS/macOS/WindowsWeb/uni-app/applet/Hongmeng/Node.js/ElectronFlutter

| API | Explain |
| --- | --- |
| [`newInstance`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#newInstance) | Create a chat room instance |
| [`addChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#addChatroomClientListener) | Register the chat room instance listener |
| [`removeChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#removeChatroomClientListener) | Unregister the chat room instance listener |
| [`getChatroomLinkAddress`](https://doc.yunxin.163.com/messaging2/client-apis/TQ5NTUwNzQ?platform=client#getChatroomLinkAddress) | Get the address of the designated chat room |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#enter) | Enter the chat room |
| [`V2NIMStorageService.uploadFile`](https://doc.yunxin.163.com/messaging2/client-apis/zQ0MDc5MjI?platform=client#uploadFile) | Upload files |
| [`V2NIMChatroomLoginOption.tokenProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginOption) | Get the dynamic Token |
| [`V2NIMChatroomLoginOption.loginExtensionProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginOption) | Get the dynamic login extension field |
| [`getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService) | Get chat room service |
| [`exit`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#exit) | Exit the chat room |
| [`destroyInstance`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#destroyInstance) | Destroy the chat room example |
| [`V2NIMChatroomService#kickMember`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#kickMember) | Kick other clients logged in at the same time offline |
| [`V2NIMChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client) | Chat room service category |
| [`V2NIMChatroomEnterParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams) | Enter the relevant parameters of the chat room |
| [`V2NIMChatroomEnterResult`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterResult) | Enter the chat room and successfully call back the result |
| [`V2NIMChatroomLoginOption`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginOption) | Chat room login configuration |
| [`V2NIMLoginAuthType`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMLoginAuthType) | Login authentication method |
| [`V2NIMChatroomTokenProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomTokenProvider) | Dynamic Token callback |
| [`V2NIMChatroomLoginExtensionProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginExtensionProvider) | Dynamic login extended data callback |
| [`V2NIMChatroomLinkProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLinkProvider) | Chat room connection address callback |
| [`V2NIMChatroomTagConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomTagConfig) | Tag information configuration to enter the chat room |
| [`V2NIMChatroomLocationConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLocationConfig) | Enter the spatial location information configuration of the chat room |
| [`V2NIMAntispamConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMAntispamConfig) | Yidun anti-spam detection configuration information |
| [`V2NIMChatroomKickedInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomKickedInfo) | Callback message kicked out of the chat room |

| API | 说明 |
| --- | --- |
| [`newInstance`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#newInstance) | 创建聊天室实例 |
| [`on("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#on) | 注册聊天室实例监听器 |
| [`off("EventName")`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#off) | 取消注册聊天室实例监听器 |
| [`getChatroomLinkAddress`](https://doc.yunxin.163.com/messaging2/client-apis/TQ5NTUwNzQ?platform=client#getChatroomLinkAddress) | 获取指定聊天室的地址 |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#enter) | 进入聊天室 |
| [`V2NIMStorageService.uploadFile`](https://doc.yunxin.163.com/messaging2/client-apis/zQ0MDc5MjI?platform=client#uploadFile) | 上传文件 |
| [`V2NIMChatroomLoginOption.tokenProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginOption) | 获取动态 Token |
| [`V2NIMChatroomLoginOption.loginExtensionProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginOption) | 获取动态登录扩展字段 |
| [`getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#getChatroomService) | 获取聊天室服务 |
| [`exit`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#exit) | 退出聊天室 |
| [`destroyInstance`](https://doc.yunxin.163.com/messaging2/client-apis/DYyMTk0NjE?platform=client#destroyInstance) | 销毁聊天室实例 |
| [`V2NIMChatroomService#kickMember`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client#kickMember) | 将其他同时登录的客户端踢下线 |
| [`V2NIMChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/DQyODIyODI?platform=client) | 聊天室服务类 |
| [`V2NIMChatroomInitParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomInitParams) | 聊天室初始化参数（仅 Web 和 鸿蒙） |
| [`V2NIMChatroomEnterParams`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterParams) | 进入聊天室相关参数 |
| [`V2NIMChatroomEnterResult`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomEnterResult) | 进入聊天室成功回调结果 |
| [`V2NIMChatroomLoginOption`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginOption) | 聊天室登录配置 |
| [`V2NIMLoginAuthType`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMLoginAuthType) | 登录鉴权方式 |
| [`V2NIMChatroomTokenProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomTokenProvider) | 动态 Token 回调 |
| [`V2NIMChatroomLoginExtensionProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLoginExtensionProvider) | 动态登录扩展数据回调 |
| [`V2NIMChatroomLinkProvider`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLinkProvider) | 聊天室连接地址回调 |
| [`V2NIMChatroomTagConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomTagConfig) | 进入聊天室的标签信息配置 |
| [`V2NIMChatroomLocationConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomLocationConfig) | 进入聊天室的空间位置信息配置 |
| [`V2NIMAntispamConfig`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMAntispamConfig) | 易盾反垃圾检测配置信息 |
| [`V2NIMChatroomKickedInfo`](https://doc.yunxin.163.com/messaging2/client-apis/DAxNjk0Mzc?platform=client#V2NIMChatroomKickedInfo) | 被踢出聊天室的回调信息 |

| API | 说明 |
| --- | --- |
| [`newInstance`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#newInstance) | 创建聊天室实例 |
| [`addChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#addChatroomClientListener) | 注册聊天室实例监听器 |
| [`removeChatroomClientListener`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#removeChatroomClientListener) | 取消注册聊天室实例监听器 |
| [`LoginService.getChatroomLinkAddress`](https://doc.yunxin.163.com/messaging2/client-apis/Dc3NDM0NTI?platform=client#getChatroomLinkAddress) | 获取指定聊天室的地址 |
| [`enter`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#enter) | 进入聊天室 |
| [`StorageService.uploadFile`](https://doc.yunxin.163.com/messaging2/client-apis/zQ0MDc5MjI?platform=client#uploadFile) | 上传文件 |
| [`V2NIMChatroomTokenProvider`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomTokenProvider) | 获取动态 Token |
| [`V2NIMChatroomLoginExtensionProvider`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomLoginExtensionProvider) | 获取动态登录扩展字段 |
| [`getChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#getChatroomService) | 获取聊天室服务 |
| [`exit`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#exit) | 退出聊天室 |
| [`destroyInstance`](https://doc.yunxin.163.com/messaging2/client-apis/zE5NTI4MjU?platform=client#destroyInstance) | 销毁聊天室实例 |
| [`V2NIMChatroomService#kickMember`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client#kickMember) | 将其他同时登录的客户端踢下线 |
| [`V2NIMChatroomService`](https://doc.yunxin.163.com/messaging2/client-apis/TQzMTI2MjU?platform=client) | 聊天室服务类 |
| [`V2NIMChatroomEnterParams`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomEnterParams) | 进入聊天室相关参数 |
| [`V2NIMChatroomEnterResult`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomEnterResult) | 进入聊天室成功回调结果 |
| [`NIMLoginAuthType`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMLoginAuthType) | 登录鉴权方式 |
| [`V2NIMChatroomTokenProvider`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomTokenProvider) | 动态 Token 回调 |
| [`V2NIMChatroomLoginExtensionProvider`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomLoginExtensionProvider) | 动态登录扩展数据回调 |
| [`V2NIMChatroomLinkProvider`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomLinkProvider) | 聊天室连接地址回调 |
| [`V2NIMChatroomTagConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomTagConfig) | 进入聊天室的标签信息配置 |
| [`V2NIMChatroomLocationConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomLocationConfig) | 进入聊天室的空间位置信息配置 |
| [`NIMAntispamConfig`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMAntispamConfig) | 易盾反垃圾检测配置信息 |
| [`V2NIMChatroomKickedInfo`](https://doc.yunxin.163.com/messaging2/client-apis/zExMjk2NzY?platform=client#V2NIMChatroomKickedInfo) | 被踢出聊天室的回调信息 |

## Best practices

The following mainly provides examples of iOS as an example, and other clients can refer to the usage of iOS.

The application can manage chat room instances by pressing the following key-value pairs:

`accountId + roomId -> V2NIMChatroomClient`

### Enter the same chat room with the same account

复用同一个 `V2NIMChatroomClient` 实例：

```
V2NIMChatroomClient *client = [V2NIMChatroomClient newInstance];

V2NIMChatroomEnterParams *params = [[V2NIMChatroomEnterParams alloc] init];
params.accountId = @"account_1";
params.token = @"token";

NSString *roomId = @"room_1";

[client enter:roomId enterParams:params success:success failure:failure];

```

If the same account needs to enter the same chat room again, continue to use the same instance:

```
objectivec
[client enter:roomId enterParams:params success:reenterSuccess failure:reenterFailure];

```

当同一账号进入同一聊天室时，应优先复用已有的实例。当该实例不再需要时，请调用 `exit` 方法退出或销毁对应的实例。

### Enter different chat rooms

If the application needs to enter multiple different chat rooms at the same time, create an independent instance for each chat room:

```
V2NIMChatroomClient *roomClientA = [V2NIMChatroomClient newInstance];
V2NIMChatroomClient *roomClientB = [V2NIMChatroomClient newInstance];

V2NIMChatroomEnterParams *params = [[V2NIMChatroomEnterParams alloc] init];
params.accountId = @"account_1";
params.token = @"token";

[roomClientA enter:@"room_1" enterParams:params success:successA failure:failureA];
[roomClientB enter:@"room_2" enterParams:params success:successB failure:failureB];

```

### Common mistakes

The following usage is not recommended:

```
V2NIMChatroomClient *clientA = [V2NIMChatroomClient newInstance];
V2NIMChatroomClient *clientB = [V2NIMChatroomClient newInstance];

V2NIMChatroomEnterParams *params = [[V2NIMChatroomEnterParams alloc] init];
params.accountId = @"account_1";
params.token = @"token";

NSString *roomId = @"room_1";

[clientA enter:roomId enterParams:params success:successA failure:failureA];
[clientB enter:roomId enterParams:params success:successB failure:failureB];

```

## Frequently asked questions

### How to deal with the rejection of the login request

If you have turned on the application identity security verification, you need to write the allowed client application identity in the list. When the client application identification requesting login is not on the list, the login request will be rejected.

On the homepage of [NetEase Yunxin console,](https://app.yunxin.163.com/global/home)select the application to enter the **application configuration**page, select the **identification management**tab at the top, and add the corresponding client application identification.

 ![Application Identification Management.png](https://yx-web-nosdn.netease.im/common/da2de7d1dfb244f321bcc582843ac26d/应用标识管理.png)

### Daily activity calculation

- Non-anonymous login, no matter how many chat rooms the same account enters on the same day, only one day's activity is counted.
- Log in anonymously, and every time you enter the chat room, you will be counted for a daily life.