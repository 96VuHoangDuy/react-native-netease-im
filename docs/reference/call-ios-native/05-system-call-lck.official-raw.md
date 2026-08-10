# 05 — 接听系统电话 / System call answering (OFFICIAL RAW — iOS)

> **Nguồn:** docs chính chủ NetEase Yunxin (NERTC CallKit — 接听系统电话 / LiveCommunicationKit, iOS). Lưu verbatim để tra cứu nhanh.
> Bản chắt lọc + những gì project thực sự làm: [`05-system-call-lck.md`](./05-system-call-lck.md).
> ⚠️ Android **không có** trang tương đương — LiveCommunicationKit là framework của Apple.

---

This article mainly introduces how to introduce Apple's native LiveCommunicationKit library into the NetEase Cloud Call component (NERtcCallKit) and realize the answering function of system calls.

## Effect display

After realizing the pop-up window quick answer function, voice calls can also be answered like ordinary dial-up calls. When a friend calls, the friend's nickname will be displayed on the Apple Dynamic Island interface, providing two options: accept and reject. After Answering The Phone, You Can Also Switch Out, Mute And Hang Up Like A System Phone. Apple models without the design of Lingdong Island will pop up a card above, which also has these functions.

![Image.png](https://yx-web-nosdn.netease.im/common/8ada402e76f3327d0d8e4670b9f1ef99/image.png)
![Image.png](https://yx-web-nosdn.netease.im/common/f44c2f210f2902895f8ec24feb7d6a28/image.png)

## Program introduction

NetEase Cloud Calling Component will replace the system's phone answering function through the [PushKit](https://developer.apple.com/documentation/pushkit) + [LiveCommunicationKit](https://developer.apple.com/documentation/LiveCommunicationKit) scheme. For the specific implementation principle, please refer to the following timing diagram:

```
sequenceDiagram
autonumber
  actor A as 呼叫方
  participant Server as 网易云信服务器
  actor B as 接听方
  participant SDK as 网易云信呼叫组件

  A->>Server: 发起呼叫
  Server->>B: PushKit 推送（携带呼叫信息）
  B->>B: 后台启动 App
  B->>B: 弹出系统 UI
  B->>SDK: 接听
  B->>SDK: 挂断
```

## Development environment

Before starting the project, please prepare the following development environment:

- Xcode 15.3 and above.
- iOS devices with iOS 17.4 and above.

## Prerequisites

Before following this article, please make sure that you have completed the following settings:

- Create at least one application on the [NetEase Cloud Trust console](https://app.yunxin.163.com/global/home). For detailed steps, please refer to [Create an app and get AppKey](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console).
- Integrate the call components to the sample project. For detailed steps, please refer to [Realize 1-to-1 call (including UI integration V3).](https://doc.yunxin.163.com/nertccallkit/guide/jg0MzU3NjM?platform=iOS)
- Before realizing system call answering, you need to configure [iOS PushKit](https://doc.yunxin.163.com/messaging/guide/Tc4MjEzODA?platform=iOS) to obtain the VoIP push certificate.

## Notes

- LiveCommunicationKit will not pop up in full screen under the lock screen, and will no longer leave call records in the address book.
- When registering PushKit, it is recommended to specify a sub-queue to avoid the situation that the UI may not pop up and cause a crash.

```objc
self.pushRegistry = [[PKPushRegistry alloc] initWithQueue:dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)];
```

- The system phone answering function needs to be used in iOS 17.4 and above. 17.4 Devices below the system do not support registering Pushkit and configuring certificates, otherwise it will crash. For an example of configuring the certificate, please refer to the second step in Implementing PushKit.

The new function is online. If you have any questions or need support or help, you can [submit a work order](https://app.yunxin.163.com/global/service/ticket/create) and contact the technical support engineer of NetEase Yunxin.

## Realize the process

### Step 1: Implement PushKit

1. Register PushKit in the system.

```objc
//这边推荐使用子队列，防止可能出现系统异常情况
self.pushRegistry = [[PKPushRegistry alloc]
    initWithQueue:dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)];

self.pushRegistry.delegate = self;
self.pushRegistry.desiredPushTypes = [NSSet setWithObject:PKPushTypeVoIP];
```

2. Configure the PushKit certificate in NetEase Cloud.

```objc
NIMSDKOption *option = [NIMSDKOption optionWithAppKey:kAppKey];
option.apnsCername = @"请输入远程推送证书名字";
if (@available(iOS 17.4, *)) {
    option.pkCername = @"请输入您的 VoIP 推送证书";
}
option.v2 = YES;
[NIMSDK.sharedSDK registerWithOptionV2:option v2Option:nil];
```

3. Pass the PushKit token to NetEase Yunxin.

```objc
- (void)pushRegistry:(PKPushRegistry *)registry
    didUpdatePushCredentials:(PKPushCredentials *)credentials
                    forType:(PKPushType)type {
    if ([credentials.token length] == 0) {
        NSLog(@"voip token NULL");
        return;
    }
    //Pushkit token 传给网易云信
    [[NIMSDK sharedSDK] updatePushKitToken:credentials.token];
}
```

### Step 2: Analyze and pop up the answer prompt UI

After receiving the PushKit message, the App layer transmits the message to NERtcCallKit, parses the field by the call component, and pops up the corresponding UI.

```objc
- (void)pushRegistry:(PKPushRegistry *)registry didReceiveIncomingPushWithPayload:(PKPushPayload *)payload forType:(PKPushType)type withCompletionHandler:(void (^)(void))completion{

    NSDictionary *dictionaryPayload = payload.dictionaryPayload;
    //判断是否是网易云信发的 payload
    if (![dictionaryPayload objectForKey:@"nim"]) {
        NSLog(@"not found nim payload");
        return;
    }

    if (@available(iOS 17.4, *)) {
        //传入 payload
        NECallSystemIncomingCallParam *param = [[NECallSystemIncomingCallParam alloc] init];
        param.payload = dictionaryPayload;
        param.ringtoneName = @"avchat_ring.mp3";

        //若展示信息无法满足需求，可以自定义展示信息
        NECallSystemIncomingCustomCallParam *customPayload = [[NECallSystemIncomingCustomCallParam alloc] init];
        customPayload.displayContent = @"自定义展示信息";
        param.customPayload = customPayload;

        //弹出系统接听 UI
        [[NECallEngine sharedInstance] reportIncomingCallWithParam:param acceptCompletion:^(NSError * _Nullable error, NECallInfo * _Nullable callInfo) {
            if (error) {
                NSLog(@"accept failed %@", error);
            }
            // update 业务 UI
         } hangupCompletion:^(NSError * _Nullable error) {
            if (error) {
                NSLog(@"hangup failed %@", error);
            }
            //update 业务 UI
         } muteCompletion:^(NSError * _Nullable error, BOOL mute) {
           if (error) {
               NSLog(@"mute failed %@", error);
           }
           // update 业务 UI
         }];
    }

    completion();
}
```

## Related interfaces

| Class/Method/Cambback/Error Code | Explain |
| --- | --- |
| `reportIncomingCallWithParam` | New interface, according to the incoming display information, parse and pop up the system call prompt/answer prompt UI. |
| `NECallSystemIncomingCallParam` | New parameter class is added to configure the incoming call information. |
| `NECallSystemIncomingCustomCallParam` | New parameter classes are added to customize the call information. With a higher priority, it will override the payload display information, such as custom display of incoming call information, call type, etc. |
