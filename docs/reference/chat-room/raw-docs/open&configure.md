# 开通和配置聊天室

NetEase Yunxin IM chat room adopts a multi-layer architecture design, which can realize a real large-scale chat room. There is no upper limit on the number of participants, and it can meet the real-time requirements of message arrival. It is mainly used in entertainment live broadcast, education live broadcast and other scenarios. Chat room is a paid expansion capability, which needs to be purchased under the condition of purchasing the basic functions of IM.

This article introduces how to open and configure chat room functions.

## Prerequisites

Before following the operation, please make sure that you have:

- At least one application has been created on the [NetEase Cloud Trust Console](https://app.yunxin.163.com/global/home). If there is no application, please refer to [Create Application](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console).
- IM products have been opened for the application. For detailed steps, please refer to the [opening or trial service](https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console).

## Opening steps

1. Select the application in the home page **application management**of [NetEase Yunxin console](https://app.yunxin.163.com/global/home), and then click the **Function Configuration**button under **IM Instant Messaging**to enter the function configuration page.

 ![Image.png](https://yx-web-nosdn.netease.im/common/b4c08cb9c2fa97f2bcc99523ffae697a/image.png)
2. Select the **chat room**tab at the top to turn on the chat room function.

 ![](https://yx-web-nosdn.netease.im/common/9b5499aff581b79a9fabb26b094d914d/开启聊天室.png)
3. After reading and confirming the message, click **Confirm**to turn on the chat room function.

## Configure the sub-functions of the chat room

For some sub-functions under the chat room function, if you need to expand, you need to open them separately. You can click **Sub-function Configuration to**open the required sub-functions.

 ![](https://yx-web-nosdn.netease.im/common/28bb6b1867fc957835c4c01ac2ca580d/聊天室子功能配置.png)

### Sub-function description

For the description of the sub-functions of the chat room that need to be opened and configured separately, please refer to the following table:

| Chat room function | Function introduction | Default value |
| --- | --- | --- |
| Chat room full service broadcast | Send a full-service broadcast message to the chat room, and all chat rooms can receive the message. | Don't turn it on by default |
| Chat room message callback | Chat room message third-party callback ability | Don't turn it on by default |
| The number of chat rooms that a single user can create | The number of chat rooms that a single user can create | 200 by default |
| Chat room user entry and exit message history storage | Do the messages of chat room users entering and leaving need to be stored in historical messages? | Turn on by default |
| Chat room users enter and exit the message system | Do chat room users need to send messages when they enter and exit? | Don't turn it on by default |
| The number of days of chat room history messages | The time limit for storing historical messages in the chat room | The default is 10 days |
| [Chat room login policy](#%E9%85%8D%E7%BD%AE%E8%81%8A%E5%A4%A9%E5%AE%A4%E7%99%BB%E5%BD%95%E7%AD%96%E7%95%A5) | Set the way for chat room users to log in to the application server or NetEase Cloud Server | Default static token login |
| [Chat room multi-terminal login mode](#%E9%85%8D%E7%BD%AE%E8%81%8A%E5%A4%A9%E5%AE%A4%E5%A4%9A%E7%AB%AF%E7%99%BB%E5%BD%95%E6%A8%A1%E5%BC%8F) | Set the user's login mode at different ends | By default, only one end is allowed to log in. |
| The copy of entering and leaving the chat room is fully corresponding. | In the process of logging in to the chat room, there will be a misalignment of copy login and logout due to network abnormalities, so this function can be turned on if the status judgment is confused. | Don't turn it on by default |
| Automatic destruction mechanism of chat room | The chat room supports automatic shutdown/destruction according to the configuration policy to reduce resource consumption. | Don't turn it on by default |

### Configure the chat room login policy

Login strategy refers to one or more IM login methods that the application needs to adopt. NetEase Yunxin SDK supports the following three login methods:

- **Static token**: Users need to pass static token when logging in to IM manually. Static token is permanently valid by default and constant, unless the [server API is](https://doc.yunxin.163.com/messaging/guide/DUxNDQ3NjA?platform=server)actively called to refresh.
- **Dynamic token**: Users need to pass in dynamic token when logging in to IM manually. Dynamic token is time-effective and suitable for business scenarios with high requirements for user information security.
- **Third-party callback**: The authentication work when the user manually logs in to IM is carried out by the designated third-party server (which can be the application server), and **the NetEase Yunxin server does not do IM login authentication**. After selecting the third-party callback, please go to the **third-party callback configuration**for relevant settings.

 ![](https://yx-web-nosdn.netease.im/common/de6644c8128f77e13cc7e98ede0b2bfc/聊天室登录策略.png)

If the corresponding login policy is not selected, it may cause the user to report an error because he has no login permission when calling the login interface (status code: 403).

### Configure the chat room multi-terminal login mode

NetEase Yunxin SDK supports the configuration of two different chat room multi-terminal login modes:

- Only one end is allowed to log in, and Windows, Web, Android and iOS kick each other. The same account is only allowed to log in on one device. When the account is successfully logged in on another device, the new device will kick the old device offline.
- All terminals can log in online at the same time. Up to 10 devices can be online at the same time. Within the maximum number of devices, all new devices will log in again and the old devices online will not be kicked offline.

 ![](https://yx-web-nosdn.netease.im/common/6ead74d8378ce3fe5c27a659eda38156/聊天室多端登录.png)
- After the console modifies the logic of multi-terminal kicking, the next time a new device logs in, it will be verified based on the new multi-terminal kicking strategy. Devices that have been connected will not be forcibly kicked out because of the modification of the policy.
- If a device repeatedly logs in to the same chat room, the later login will disconnect the previous long connection. At this time, it will trigger another CC to enter the chat room, but it will not trigger the copy to leave the chat room. For the copy of entering and leaving the chat room (eventType=9), please refer to the [chat room members entering and leaving the chat room event copy](https://doc.yunxin.163.com/messaging/guide/TcxNzU4NzU?platform=server#%E8%81%8A%E5%A4%A9%E5%AE%A4%E6%88%90%E5%91%98%E8%BF%9B%E5%87%BA%E8%81%8A%E5%A4%A9%E5%AE%A4%E4%BA%8B%E4%BB%B6%E6%8A%84%E9%80%81).

## Integrated development

For details, please refer to the [chat room overview](https://doc.yunxin.163.com/messaging/guide/jk3OTc5NjE?platform=android).