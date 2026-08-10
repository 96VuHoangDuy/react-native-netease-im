<!-- ===== Dg2ODg5NDE | 跑通示例项目 | platform=iOS ===== -->

# 跑通示例项目

在视频呼叫示例项目中使用了呼叫组件实现音视频呼叫。本文介绍如何快速跑通音视频呼叫的示例项目，体验音视频呼叫功能。

## 开发环境

示例项目对开发环境的要求如下所示：

| 环境要求 | 说明 |
| --- | --- |
| Xcode 版本 | 10 及以上版本 |
| iOS | 9.0 及以上版本的 iOS 设备 |

- 请确保您的项目已设置有效的开发者签名。
- 请使用 iOS 真机，因为内部引用的音视频 SDK 无法在模拟器上运行。

## 前提条件

在开始运行示例项目之前，请确保您已完成以下操作：

- 已 [创建应用并获取应用的 App Key](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console)。
- 已开通以下服务，若未开通，请参考 [开通服务](https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console) 进行开通。
  - IM 即时通讯。当使用呼叫组件自带的话单功能时，需开通 IM。
  - 信令。用于实现点对点呼叫邀请以及音视频通话。
  - 音视频通话 2.0。用于实现实时音视频通话。

建议开通音视频通话的 **调试模式** （[鉴权方式](https://doc.yunxin.163.com/nertc/quick-start/TQ0MTI2ODQ?platform=android)），调试模式建议只在集成开发阶段使用，请在应用正式上线前改回安全模式。
  - 如需要抄送，请提前开通消息抄送中的 **话单** 抄送服务，实现在一通通话结束后，发送事件通知消息，标记此次通话是否接通以及通话时间、类型等数据。

## 操作步骤

- 示例源码仅供开发者接入参考，实际应用开发场景中，请结合具体业务需求修改使用。
- 若您计划将源码用于生产环境，请确保应用正式上线前已经过全面测试，以免因兼容性等问题造成损失。
- 示例代码中包含业务登录逻辑，如果您的业务中需要使用登录相关功能，请修改登录相关的逻辑。

1. 前往 GitHub 克隆 [示例项目源码](https://github.com/netease-kit/NECallKit/tree/main/iOS) 仓库至本地。
2. 打开 `NLiteAVDemo/AppKey.h` 文件，将 `kAppKey` 的值替换您的 App Key。

```
// app key for code
static NSString * const kAppKey = @"your app key";

```
3. 连接 iOS 设备后，用 Xcode 打开示例项目，编译并运行示例项目。


<!-- ===== jg0MzU3NjM | 单聊呼叫（含 UI） | platform=iOS ===== -->

# 实现单聊呼叫（含 UI）

呼叫组件（NERTCCallkit）通过 UI 组件化的方式，简化了呼叫流程，您只需要调用几行代码，就可以实现 1 对 1 呼叫，即点对点呼叫，并包含呼叫的 UI 界面。本文介绍呼叫组件的集成和实现方法。

## 注意事项

针对呼叫组件中的回调信息，开发者要做好相应回调数据的上报及存储，以便于后期上线之后排查问题。

## 基本概念

- **AccId/accountId**：IM 账号 ID，用于登录 IM。[注册 IM 账号时](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)，IM 服务器会返回对应的账号 ID和密钥（Token），应用客户端需要负责保存账号和 IM Token 的映射关系。
- **Token**：呼叫组件中涉及的 Token 包括 IM Token，用于登录 IM 时进行 IM 账号鉴权。应用服务器调用 IM 服务器的 [注册账号 API](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)，获取的 IM Token。

## 开发环境

在开始运行工程之前，请您准备以下开发环境：

- Xcode 14 及以上版本。
- iOS 10.0 及以上版本的 iOS 设备。
- 已安装 CocoaPods。

## 准备工作

- 在 [网易云信控制台](https://app.yunxin.163.com/global/home) [创建应用](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console)，并获取了对应的 App Key。
- 已 [开通](https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console) IM 即时通讯、音视频通话 2.0、话单功能以及信令产品。

## 示例项目源码

网易云信提供 [示例项目源码](https://github.com/netease-kit/NECallKit/tree/main/iOS)，您可以基于该源码进行修改适配。

## 集成呼叫组件

呼叫组件（NERTCCallkit）基于网易云信 NIM SDK（V10）和 NERTC SDK 实现通话呼叫。呼叫组件中已集成 NIM SDK，您只需集成 NERTCCallkit 和 NERTC SDK 即可。

1. 安装 CocoaPods 后，在项目所在目录下执行以下命令，创建 Podfile 文件。

```
pod init

```
2. 在 Podfile 文件中添加对应的资源。

 引入呼叫组件（不指定依赖的版本）

直接引入呼叫组件，组件会自动使用当前兼容的 IM 依赖版本，无需单独指定。

```
pod 'NERtcCallKit'  # 呼叫组件
pod 'NERtcCallUIKit'  # 呼叫组件 UI 库
pod 'NERtcSDK/RtcBasic'  # RTC 音视频基础组件

```

 引入呼叫组件（指定依赖的版本）

若您的项目已集成 NIM SDK 或 NERTC SDK，需指定具体版本的内置依赖。

```
pod 'NERtcCallKit/NOS_Special'  # 呼叫组件
pod 'NERtcCallUIKit/NOS_Special'  # 呼叫组件 UI 库
pod 'NIMSDK_LITE', '10.9.70'  # IM 基础功能包示例版本，请使用实际版本
// pod 'NIMSDK_LITE/FTS', '10.9.70' # IM 本地检索库功能包示例版本，请使用实际版本
pod 'NERtcSDK/RtcBasic', '5.9.0' # RTC 音视频基础组件包示例版本，请使用实际版本

```

- 呼叫组件各个版本的依赖版本请参考 [呼叫组件更新日志](https://doc.yunxin.163.com/nertccallkit/concept/jIzNDA4Nzc?platform=client)。
- IM 相关组件的依赖版本需要一致（比如 IM 基础包和检索库的版本相同），否则会报错。

   引入呼叫组件（不指定依赖的版本）引入呼叫组件（指定依赖的版本）

直接引入呼叫组件，组件会自动使用当前兼容的 IM 依赖版本，无需单独指定。

```
pod 'NERtcCallKit'  # 呼叫组件
pod 'NERtcCallUIKit'  # 呼叫组件 UI 库
pod 'NERtcSDK/RtcBasic'  # RTC 音视频基础组件

```

若您的项目已集成 NIM SDK 或 NERTC SDK，需指定具体版本的内置依赖。

```
pod 'NERtcCallKit/NOS_Special'  # 呼叫组件
pod 'NERtcCallUIKit/NOS_Special'  # 呼叫组件 UI 库
pod 'NIMSDK_LITE', '10.9.70'  # IM 基础功能包示例版本，请使用实际版本
// pod 'NIMSDK_LITE/FTS', '10.9.70' # IM 本地检索库功能包示例版本，请使用实际版本
pod 'NERtcSDK/RtcBasic', '5.9.0' # RTC 音视频基础组件包示例版本，请使用实际版本

```

- 呼叫组件各个版本的依赖版本请参考 [呼叫组件更新日志](https://doc.yunxin.163.com/nertccallkit/concept/jIzNDA4Nzc?platform=client)。
- IM 相关组件的依赖版本需要一致（比如 IM 基础包和检索库的版本相同），否则会报错。

1. 执行以下命令安装 SDK：

```
pod install

```

## 初始化

需要先完成 IM SDK 和呼叫组件的初始化才能使用其他功能。

1. 在项目文件中引入头文件 `NIMSDK.h`。

```
#import <NIMSDK/NIMSDK.h>

```
2. 调用 [registerWithOptionV2](https://doc.yunxin.163.com/messaging2/references/iOS/doxygen/Latest/zh/de/de3/interface_n_i_m_s_d_k.html#a4140971377bec8212dabd66fdea0bbb3) 方法初始化 SDK，推荐在应用程序启动时初始化，目的是配置 SDK 并准备进行登录和即时通讯功能。

```
NIMSDKOption *option = [NIMSDKOption optionWithAppKey:appKey];
option.apnsCername = @"your apns certificate";
option.pkCername = @"your push kit certificate";
// 配置 useV1Login
V2NIMSDKOption *v2Option = [[V2NIMSDKOption alloc] init];
//激活 V10 所有 API，默认使用 V10 的登录接口登录 IM
v2Option.useV1Login = NO;
//若仍使用 V9 的登录接口登录 IM
//v2Option.useV1Login = YES;
[[NIMSDK sharedSDK] registerWithOptionV2:option v2Option:v2Option];

```

以上提供了一个简化的初始化示例，更多初始化信息请参考 [初始化 SDK](https://doc.yunxin.163.com/messaging2/guide/jU5MTUwMDY?platform=client)。
3. 呼叫组件通过 [`NECallEngine.sharedInstance`](https://doc.yunxin.163.com/nertccallkit/references/iOS/doxygen/Latest/zh/html/interface_n_e_call_engine.html#ae49b53ff34cda8bbbc7a78a49423cca4) 获取实例，然后调用 [`setup:`](https://doc.yunxin.163.com/nertccallkit/references/iOS/doxygen/Latest/zh/html/interface_n_e_call_engine.html#a3d65dcfd5e21ad67a2aac822f3b086bd) 方法完成初始化。

`setup:` 方法为使用组件前必须调用的方法，若未初始化直接调用其他组件方法，会发生不可预知的问题或故障。

```
#import <NERtcCallKit/NERtcCallKit.h>
@interface SomeViewController()<NECallEngineDelegate>
@end
@implementation SomeViewController

- (void)viewDidLoad {
    [self setupSDK];
}

- (void)setupSDK {
    NESetupConfig *setupConfig = [[NESetupConfig alloc] initWithAppkey:kAppKey];
    [[NECallEngine sharedInstance] setup:setupConfig];

    NECallUIKitConfig *config = [[NECallUIKitConfig alloc] init];
    [[NERtcCallUIKit sharedInstance] setupWithConfig:config];
}
@end

```

## 登录

调用 [`login`](https://doc.yunxin.163.com/messaging2/client-apis/TQ5NTUwNzQ?platform=client#login) 方法进行登录。

本文以实现 **静态 Token** 登录为例，动态 Token 登录以及自动登录的实现方法请参考 [登录 IM](https://doc.yunxin.163.com/messaging2/guide/Dk1MTY4MzA?platform=client)。

示例代码如下：

```
- (void)login
{
    NSString *accountId = @"accountId";
    NSString *token = @"token";
    [[NIMSDK sharedSDK].v2LoginService login:accountId token:token
            option:nil
              success:^{
        NSLog(@"login succ");
    }
              failure:^(V2NIMError * _Nonnull error) {
        NSLog(@"login fail: error = %@", error);
    }];
}

```

## 实现单聊呼叫

呼叫组件（NERTCCallkit）内部已包括呼叫的相关逻辑，您只需要调用几行代码触发呼叫即可。

```
NEUICallParam *callParam = [[NEUICallParam alloc] init];
NEUser *localUser = [NEAccount shared].userModel;
callParam.currentUserAccid = @"current user accid";
callParam.remoteUserAccid = @"remote Accid";
callParam.remoteShowName = @"remote show name";
[[NERtcCallUIKit sharedInstance] callWithParam:callParam withCallType:callType];

```


<!-- ===== DE1OTA2NjA | 群组通话（含 UI） | platform=iOS ===== -->

# 实现群组通话（含 UI）

本文介绍了如何通过网易云信呼叫组件（CallKit）提供的 API 进行群组通话功能开发的详细步骤和代码示例。

群组通话功能目前在 Beta 测试阶段，若需要使用，请联系您的网易云信商务经理开通。

  ![image.png](https://yx-web-nosdn.netease.im/common/544b40530ae385be0ff51801b338ccf0/image.png)

## 适用场景

群组通话功能是现代通信应用的核心功能之一，它允许多个用户同时进行实时的视频和音频交流。无论是企业会议、在线教育、社交互动还是远程医疗咨询，这一功能都能提供高效的沟通手段，增强团队协作和信息共享。

- **在线教育**：教师和学生可以通过多人视频通话进行实时互动，共享屏幕和文档，提升在线学习体验。
- **企业会议**：团队成员无论身处何地，都能通过视频会议进行有效的远程协作和决策讨论。
- **社交互动**：朋友和家人可以通过群视频通话保持联系，共享生活瞬间。
- **远程医疗咨询**：医生和患者可以进行远程视频咨询，进行初步诊断和健康建议。
- **紧急服务**：紧急服务人员可以与现场人员进行实时视频通话，快速响应紧急情况。

## 前提条件

根据本文操作前，请确保您已经完成了以下设置：

- 在 [网易云信控制台](https://app.yunxin.163.com/global/home) 上创建至少一个应用。详细步骤请参考 [创建应用并获取 AppKey](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console)。
- 集成 **V3.7.0** 版本及之后的呼叫组件到示例项目。详细步骤请参考 [实现单聊呼叫](https://doc.yunxin.163.com/nertccallkit/guide/jg0MzU3NjM?platform=iOS)。

## 实现流程

### 初始化

以下示例代码描述了在 iOS 应用中如何初始化群组通话功能，包括设置配置参数。

```
// 初始化呼叫组件
NESetupConfig *setupConfig = [[NESetupConfig alloc] initWithAppkey:kAppKey];
[[NECallEngine sharedInstance] setup:setupConfig];

//初始化 UI 组件
NECallUIKitConfig *config = [[NECallUIKitConfig alloc] init];
[[NERtcCallUIKit sharedInstance] setupWithConfig:config];
[NERtcCallUIKit sharedInstance].delegate = self;

//初始化 群呼
GroupConfigParam *param = [[GroupConfigParam alloc] init];
param.appid = kAppKey;
param.rtcSafeMode = YES;
[[NEGroupCallKit sharedInstance] setupGroupCall:param];

```

### 开始群呼

以下示例代码提供了开始一个群组通话的代码示例，包括设置通话参数和处理通话结果。

```
// 创建群组通话参数
NEUIGroupCallParam *groupCallParam = [[NEUIGroupCallParam alloc] init];
groupCallParam.remoteUsers = remoteUserIds; //传入远端用户列表

// 发起群组通话
[[NERtcCallUIKit sharedInstance] groupCallWithParam:groupCallParam];

```

### 通话中添加（邀请）成员

在通话中添加成员时，如果您需要直接修改对应的交互（UI），请参考以下示例代码。

```
//在初始化的时候开启邀请功能
NECallUIKitConfig *config = [[NECallUIKitConfig alloc] init];
config.uiConfig.enableGroupCallInviteOthersWhenCalling = YES;
[[NERtcCallUIKit sharedInstance] setupWithConfig:config];

//设置 NERtcCallUIKit 监听回调
[NERtcCallUIKit sharedInstance].delegate = self;

//监听 inviteUsersWithCallId 回调
- (void)inviteUsersWithCallId:(NSString *)callId
                inCallUsers:(NSArray<NSString *> *)inCallUsers
                  completion:(void (^)(NSArray<NSString *> *_Nullable users))completion {

  //通过 completion 回调把需要添加的成员告诉组件，组件内部会处理邀请逻辑。
  completion(newInvitedUsers);
}

```


<!-- ===== TcwNDA4OTg | 实现单呼转群聊 | platform=iOS ===== -->

# 实现单呼转群聊

自 V4.7.0 版本起，呼叫组件支持单呼转群呼功能。本文主要介绍如何通过集成呼叫组件（含 UI），在已接通的 1v1 单聊通话中继续邀请其他用户加入当前通话，实现单呼平滑升级为多人通话，并复用组件内置 UI。

该功能并不是原有的群呼能力。单呼转群呼基于当前 1v1 信令房间和 RTC 房间，通过 `NECallEngine inviteMembers:completion:` 邀请新成员加入，不需要重新发起群呼。

## 注意事项

- 呼叫组件基于网易云信 NIM SDK 和 NERTC SDK 实现通话呼叫。
- 针对呼叫组件中的回调信息，开发者要做好相应回调数据的上报及存储，以便于后期上线之后排查问题。
- 参与单呼转群呼的端都需要使用支持该能力的新版本 SDK，并开启单呼转群呼能力。任一端未开启或版本不支持时，不展示邀请入口。
- 单呼转群呼功能仅允许在 1v1 呼叫已接通后发起。初始被叫未接听前，CallKit-UI 不展示邀请入口「+」。
- 多人通话人数上限为 10 人。组件会按当前已加入成员、待接听成员和本次邀请账号数做校验。
- 邀请发送成功只表示邀请信令已发出，不表示对方已接听或已加入通话。成员真正加入以组件收到成员状态 `NECallMemberStateJoined` 为准。
- `onCallConnected` 只表示当前端 1v1 通话建立。含 UI 场景下，原 1v1 通话方由组件根据 `onCallModeChanged` 切换多人布局；第三方被邀请人接听 `multiCallInvite == YES` 的邀请成功后，组件会立即进入多人布局，再通过成员快照刷新加入状态。
- 通话一旦进入多人模式，本次通话内会保持多人模式；即使后续只剩 2 人，也不会恢复 1v1 大画面和音视频切换能力。
- 进入多人模式后，不支持通话中音视频类型切换，CallKit-UI 会自动处理提示和禁用状态。
- 单呼转群呼话单由云信服务端生成，需要联系云信技术支持开通。开启 `enableSingleToGroupCall` 后，本地 SDK 默认 1v1 话单发送会被跳过；目前暂不支持通过 `setCallRecordProvider` 自行实现单呼转群呼话单。如需自定义话单，请联系云信技术支持。

## 基本概念

- `account_id`：`account_id` 是 IM 账号 ID，用于登录 IM。[注册 IM 账号时](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)，IM 服务器会返回对应的账号 ID（account_id）和密钥（Token），应用客户端需要负责保存 account_id 和 IM Token 的映射关系。
- `Token`：呼叫组件中涉及的 Token 包括 IM Token，用于登录 IM 时进行 IM 账号鉴权。应用服务器调用 IM 服务器的 [注册账号 API](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)，获取的 IM Token。
- `RTC uid`：用户加入 RTC 房间时使用的 ID，由呼叫组件在通话过程中维护，业务侧通常不需要在单呼转群呼接入中单独处理。
- `callId`：CallKit 业务通话 ID，用于回调、日志和邀请批次关联，而并非 NIM 信令房间 ID。
- `channelId`：NIM 信令房间 ID。业务通常不需要直接处理，可用于日志和问题排查。

## 开发环境

在开始运行工程之前，请您准备以下开发环境：

- Xcode 14 及以上版本。
- iOS 10.0 及以上版本的 iOS 设备。
- 已安装 CocoaPods。

## 准备工作

根据本文操作前，请确保您已经完成了以下设置：

- 在 [网易云信控制台](https://app.yunxin.163.com/global/home) [创建应用](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console)，并获取了对应的 App Key。
- 已 [开通](https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console) IM 即时通讯、音视频通话 2.0、话单功能以及信令产品。
- 已实现 [单聊呼叫](https://doc.yunxin.163.com/nertccallkit/guide/jg0MzU3NjM?platform=iOS)。

使用单呼转群呼功能前，请先完成 1v1 单聊呼叫接入，并确认可以正常发起、接听和挂断 1v1 音视频通话。

## 示例项目源码

网易云信提供 [示例项目源码](https://github.com/netease-kit/NECallKit/tree/main/iOS)，您可以基于该源码进行修改适配。

## 实现单呼转群呼

1. 初始化呼叫组件。

在初始化时需要开启单呼转群呼功能，设置 `NECallUIConfig.singleToGroupInviteMode`。如果由 CallKit-UI 内部初始化 `NECallEngine`，可把 `NESetupConfig` 传给 `NECallUIKitConfig.config`。

```
#import <NERtcCallUIKit/NERtcCallUIKit.h>

- (void)setupCallUIKitWithAppKey:(NSString *)appKey {
    NESetupConfig *engineConfig = [[NESetupConfig alloc] initWithAppkey:appKey];
    engineConfig.enableSingleToGroupCall = YES;

    NECallUIKitConfig *uiKitConfig = [[NECallUIKitConfig alloc] init];
    uiKitConfig.appKey = appKey;
    uiKitConfig.config = engineConfig;
    uiKitConfig.uiConfig.singleToGroupInviteMode =
        NECallSingleToGroupInviteModeAfter1V1Connected;

    [[NERtcCallUIKit sharedInstance] setupWithConfig:uiKitConfig];
    [NERtcCallUIKit sharedInstance].delegate = self;
}

```

如果业务已经自行调用过 `[[NECallEngine sharedInstance] setup:]`，请不要重复初始化 `NECallEngine`，但必须确保初始化时设置：

```
setupConfig.enableSingleToGroupCall = YES;

```

然后只初始化 UI 配置：

```
NECallUIKitConfig *uiKitConfig = [[NECallUIKitConfig alloc] init];
uiKitConfig.appKey = appKey;
uiKitConfig.uiConfig.singleToGroupInviteMode =
    NECallSingleToGroupInviteModeAfter1V1Connected;

[[NERtcCallUIKit sharedInstance] setupWithConfig:uiKitConfig];
[NERtcCallUIKit sharedInstance].delegate = self;

```
2. 发起 1v1 呼叫。

1v1 接通前，CallKit-UI 不展示邀请入口。1v1 接通后，如果本端、对端均支持并开启单呼转群呼能力，通话页会展示邀请入口「+」。

```
NEUICallParam *callParam = [[NEUICallParam alloc] init];
callParam.remoteUserAccid = @"callee_account_id";
callParam.remoteShowName = @"callee_name";
callParam.remoteAvatar = @"callee_avatar";
callParam.callType = NECallTypeVideo;
callParam.isCaller = YES;

[[NERtcCallUIKit sharedInstance] callWithParam:callParam];

```
3. 实现添加用户加入呼叫的回调。

点击「+」后，CallKit-UI 会调用 `selectInviteUsersWithContext:completion:`。业务侧在该回调中展示自己的联系人选择页。

```
#pragma mark - NECallUIKitDelegate

- (void)selectInviteUsersWithContext:(NECallInviteUIContext *)context
                        completion:(void (^)(NSArray<NSString *> *_Nullable userIDs))completion {
    NSLog(@"invite users, callId: %@, remaining: %ld, inCall: %@",
        context.callId, (long)context.remainingCount, context.inCallUserIDs);

    [self presentContactPickerWithDisabledUserIDs:context.inCallUserIDs
                                        maxSelect:context.remainingCount
                                    completion:^(NSArray<NSString *> *selectedAccountIds) {
        if (completion) {
            completion(selectedAccountIds);
        }
    }];
}

```

`NECallInviteUIContext` 字段说明：

| 字段 | 说明 |
| --- | --- |
| `callId` | 当前通话 ID，用于业务日志和问题排查。 |
| `channelId` | 当前信令房间 ID，通常只用于调试。 |
| `currentUserID` | 当前登录账号。 |
| `inCallUserIDs` | 已在通话中或已处于邀请中的账号，业务选人页应过滤或置灰。 |
| `remainingCount` | 当前还可邀请的人数，业务选人页应限制最大选择数。 |
| `maxMembers` | 本次通话最大人数，一期默认 10。 |

如果用户取消选择，返回 `nil` 或空数组即可：

```
completion(nil);

```

## 相关接口

| API | 说明 |
| --- | --- |
| `NESetupConfig.enableSingleToGroupCall` | Core 层单呼转群呼能力开关，默认 `NO`。 |
| `NECallUIConfig.singleToGroupInviteMode` | 含 UI 场景邀请入口模式，默认禁用。 |
| `NECallSingleToGroupInviteModeDisabled` | 禁用单呼转群呼邀请入口。 |
| `NECallSingleToGroupInviteModeAfter1V1Connected` | 1v1 接通后可展示邀请入口。 |
| `selectInviteUsersWithContext:completion:` | 点击「+」后业务侧选人回调。 |
| `onCallConnected:` | 当前端 1v1 通话建立回调，不用于判断单呼转群呼是否应切多人 UI。 |
| `onCallModeChanged:` | 原 1v1 通话方首次进入多人模式时切换多人布局。 |
| `onCallMembersChanged:` | 主入口，刷新成员宫格、待接听占位和成员音视频快照，不作为切换多人 UI 的唯一入口。 |
| `onVideoMuted:userID:` / `onVideoAvailable:userID:` | 对单个成员格子做视频状态增量刷新。 |
| `onAudioMuted:userID:` / `onLocalAudioMuted:` | 同步音频 mute 状态。 |
| `onCallInviteStateChanged:` | 展示拒绝、超时、忙线、不支持等邀请结果。 |
| `onReceiveInvited:` | 多人邀请来电时展示邀请人信息。 |

## 常见问题

**为什么 1v1 接通后没有展示邀请入口「+」？**

请确认是否满足以下条件：

1. 本端初始化时是否设置 `enableSingleToGroupCall = YES`。
2. 是否设置 `singleToGroupInviteMode = NECallSingleToGroupInviteModeAfter1V1Connected`。
3. 是否已设置 `NERtcCallUIKit.sharedInstance.delegate`，并实现 `selectInviteUsersWithContext:completion:`。
4. 对端是否为支持单呼转群呼的新版本，并同样开启能力。
5. 当前是否已达到人数上限。

**点击「+」后组件是否会提供默认账号输入面板？**

不会。CallKit-UI 只提供入口、基础过滤、发送和多人布局。账号输入面板、联系人选择页、组织架构页由业务通过 `selectInviteUsersWithContext:completion:` 自行实现。

**`selectInviteUsersWithContext` 返回哪些账号？**

返回 IM 账号 ID 数组。业务侧应过滤或置灰 `context.inCallUserIDs` 中的账号，并限制选择数量不超过 `context.remainingCount`。

**多人通话退回 2 人后，可以恢复 1v1 UI 吗？**

不建议恢复。目前在进入多人模式后，本次通话保持多人模式；退回 2 人时仍展示多人双人宫格，并继续禁用音视频切换。

**可以直接用 `NEGroupCallKit` 发起多人通话吗？**

单呼转群呼不使用旧版 `NEGroupCallKit` 群呼链路。该能力是在已有 1v1 通话内邀请成员加入当前房间。


<!-- ===== zM4MjgwMTY | 通话话单 | platform=iOS ===== -->

# 话单

呼叫组件提供通话话单功能，在一通通话结束后，您会收到对应的通话话单。通话话单是一条事件通知消息，标记此次呼叫的状态。话单以 IM 会话类型消息抄送的形式发送，收到话单后，您可以解析消息体，获得通话时间等通话详情。

云信呼叫组件的话单消息共有 5 类，其中 4 类为 **未接通时的话单**（主叫方客户端发送），1 类为 **带有通话时长的正常话单**（服务端直接发送）。

由主叫方客户端发送的未接通话单包括 **拒接话单**、**占线话单**、**超时未接听话单**、**主叫取消话单**。

下图为常见话单示例，从上至下分别为主叫取消话单、被叫拒绝话单、超时未接听话单、被叫占线话单、正常通话带有时长的话单。

 ![image-netease](https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdoc%2FNERtcCallKit-CallTicket01.png)

多人通话默认未封装话单功能。若您需要使用话单，请通过其他方式自行实现。

## 使用话单功能

### 方式一：使用组件封装的话单

#### **第一步：开通话单抄送**

1. 登录网易云信控制台。开通 IM 即时通讯、音视频通话以及信令（信令是 IM 即时通讯的子功能，需要单独开通）。具体请参考 [开通或关闭功能](https://doc.yunxin.163.com/console/concept/TQ2NzE5MzQ?platform=console)。
2. 在抄送消息中开通呼叫组件的话单抄送。

![话单开通.png](https://yx-web-nosdn.netease.im/common/26c20659a94204cd4b1027fe64521e4b/%E8%AF%9D%E5%8D%95%E5%BC%80%E9%80%9A.png)

#### **第二步：发送话单**

开通话单功能后，默认发送话单消息。

呼叫组件不包含话单接收及解析功能，用户需要参考示例项目源码自行实现。详细说明请参考 [消息收发](https://doc.yunxin.163.com/messaging2/guide/DYzMjA0Njc?platform=client#%E6%94%B6%E5%8F%91%E8%87%AA%E5%AE%9A%E4%B9%89%E6%B6%88%E6%81%AF)。

#### **第三步：接收并解析话单**

话单消息同普通消息一样通过 NIM SDK 进行消息接收，示例代码如下：

```
@interface NEMenuViewController ()<V2NIMMessageListener>
@end

@implementation NEMenuViewController

- (void)dealloc {
    [[[NIMSDK sharedSDK] v2MessageService] addMessageListener:self];
}

- (void)viewDidLoad {
    [[[NIMSDK sharedSDK] v2MessageService] removeMessageListener:self];
}

#pragma mark - IM delegate
- (void)onSendMessage:(V2NIMMessage *)message {
    if (message.sendingState == V2NIM_MESSAGE_SENDING_STATE_SENDING) {
        [self assmebleRecordWithV2Message:message withCaller:YES];
    }
}

- (void)onReceiveMessages:(NSArray<V2NIMMessage *> *)messages;
    for (V2NIMMessage *message in messages) {
        if ([message.senderId isEqualToString:[NEAccount shared].userModel.imAccid]) {
            [self assmebleRecordWithMessage:message withCaller:YES];
            return;
        }
        [self assmebleRecordWithV2Message:message withCaller:NO];
    }

}

@end

```

**话单消息结构**

| 话单类型 | 值 | 说明 |
| --- | --- | --- |
| `NIMRtcCallStatusComplete` | 1 | 正常通话话单，通话双方都进入音视频通话后进行挂断。由服务器发送。 |
| `NIMRtcCallStatusCanceled` | 2 | 主叫取消话单，主叫呼叫后主动取消的话单。由客户端主叫方发送。 |
| `NIMRtcCallStatusRejected` | 3 | 被叫拒接话单，被叫拒接接听后的话单。客户端主叫方收到被叫拒接消息后进行发送。 |
| `NIMRtcCallStatusTimeout` | 4 | 超时话单，被叫收到通话邀请后不操作等待超时产生的话单。客户端主叫方发送。 |
| `NIMRtcCallStatusBusy` | 5 | 占线话单（用户忙），当主叫呼叫被叫时，被叫仍处于通话以及呼叫/被叫中，此时被叫会拒绝主叫的通话邀请。客户端主叫收到消息后会发送占线话单。 |

话单以 IM 消息抄送的形式发送，抄送类型为会话类型，即 `eventType` 为 1。会话类型的消息体中一般包含 `eventType`、`convType`、`to`、`fromAccount`、`msgTimestamp、msgType`、`msgidClient`、`msgidServer`、`attach` 等字段，全量字段请参考 [返回数据的 JSON 字段说明](https://doc.yunxin.163.com/messaging2/server-apis/DI3OTg5Mjg?platform=server#%E6%B6%88%E6%81%AF%E4%BD%93%E4%B8%AD%E7%9A%84-json-%E5%AD%97%E6%AE%B5%E8%AF%B4%E6%98%8E)。其中：

- 话单消息的 `msgType` 字段的值为 `NRTC_NETCALL`，表示音视频话单消息抄送。
- 话单消息中 `attach` 字段中包含通话类型、呼叫状态等通话详情，请参考以下表格：

| 字段 | 类型 | 示例 | 说明 |
| --- | --- | --- | --- |
| type | Number | 1 | 通话类型。  **1**：音频通话。 **2**：视频通话。 |
| channelId | Number | 123 | 房间 ID。 |
| status | Number | 1 | 呼叫状态。  **1**：通话正常结束。 **2**：主叫取消呼叫。 **3**：被叫拒绝通话。 **4**：被叫未接听呼叫，呼叫因超时被取消。 **5**：被叫忙线，通话未接通。 |
| durations | JsonArray | 无 | 通话过程详情，JSON 数组格式，其中包括：  **accid**：通话成员的 accid。 **duration**：对应成员的通话时长。 |

收到的话单的 JSON 结构：

```
{
   "type": 1,                       //1 表示音频，2 表示视频
   "channelId": 123,                //G2 的 channelId
   "status": 1,                     //1 表示正常结束通话话单，对应上表的话单类型
   "durations": [
           {
               "accid":"acc01",
               "duration":10
           },
           {
               "accid":"acc02",
               "duration":12
           }
   ]
}

```

接收到消息后进行消息解析。示例代码如下：

```
- (void)assmebleRecordWithV2Message:(V2NIMMessage *)message withCaller:(BOOL)isCaller {
    V2NIMMessageCallAttachment *recordObject = (V2NIMMessageCallAttachment *)message.attachment;

    // 音频/视频 类型通话
    NIMRtcCallType type = (NIMRtcCallType)recordObject.type;

    // 话单类型
    NIMRtcCallStatus status = (NIMRtcCallStatus)recordObject.status;

    //// 时长列表
    NSArray<V2NIMMessageCallDuration *>*durations = recordObject.durations;

    switch (status) {
        case NIMRtcCallStatusComplete:
            // 成功接听
            break;
        case NIMRtcCallStatusCanceled:
            // 主叫用户取消
            break;
        case NIMRtcCallStatusRejected:
            // 被叫用户拒接
            break;
        case NIMRtcCallStatusTimeout:
            // 被叫接听超时
            break;
        case NIMRtcCallStatusBusy:
            // 被叫用户在通话中，占线
            break;
        default:
            break;
    }
}

```

### 方式二：自行实现话单

若组件自带的话单功能无法满足您的业务需求，可以参考以下步骤自行实现话单。

1. 关闭 **服务端** 话单抄送。具体步骤请参考 [开通话单功能](#%E7%AC%AC%E4%B8%80%E6%AD%A5%E5%BC%80%E9%80%9A%E8%AF%9D%E5%8D%95%E6%8A%84%E9%80%81)。
2. 在 [网易云信控制台](https://app.yunxin.163.com/index?#/) 开通房间时长消息抄送（eventType=8）。详细步骤请参考 [开通消息抄送](https://doc.yunxin.163.com/nertc/server-apis/DExNjg2MDc?platform=server)。
3. 确定话单协议，通常用 JSON 表示。

```
{
  "type": 1   // 话单类型
  "data": ... // 话单消息内容，如通话时长等信息
}

```
4. 设置 [`setCallRecordProvider`](https://doc.yunxin.163.com/nertccallkit/references/iOS/doxygen/Latest/zh/html/interface_n_e_call_engine.html#a4bd8685fea71f130d3e1cbf59b0d0de6)，实现开发者自己的话单逻辑（设置 `setCallRecordProvider` 后客户端本地不再发送组件内部话单），参考 [自定义消息发送](https://doc.yunxin.163.com/messaging2/guide/DYzMjA0Njc?platform=client#%E6%94%B6%E5%8F%91%E8%87%AA%E5%AE%9A%E4%B9%89%E6%B6%88%E6%81%AF)，将步骤 3 中确定的话单协议作为自定义内容进行发送。

您可以通过设置自定义的回调来发送话单。

此处仅回调未接通话单，接通话单需要您在通话结束后独立处理。

```
//设置自定义话单回调
[[NECallEngine sharedInstance] setCallRecordProvider:self];

// 接收话单回调，自定义发送内容，并发送话单
- (void)onRecordSend:(NERecordConfig *)config{
// 发送未成功通话话单
}

```

**NERecordConfig** 通话话单字段说明：

| 字段 | 类型 | 示例 |
| --- | --- | --- |
| accId | NSString | 通话对端的用户 ID。 |
| callType | NECallType | 通话类型，包括：`NECallType.AUDIO`：音频通话`NECallType.VIDEO`：视频通话 |
| callState | NIMRtcCallStatus | 通话话单类型（`NERecordCallStatus`），具体请参考 [话单消息结构](#%E8%AF%9D%E5%8D%95%E6%B6%88%E6%81%AF%E7%BB%93%E6%9E%84) |

## 关闭话单功能

若您不需要使用话单功能，可以参考以下步骤关闭：

1. 关闭 **服务端** 话单抄送。具体步骤请参考 [开通话单功能](#%E7%AC%AC%E4%B8%80%E6%AD%A5%E5%BC%80%E9%80%9A%E8%AF%9D%E5%8D%95%E6%8A%84%E9%80%81)。
2. 关闭 **客户端** 话单发送。设置 `setCallRecordProvider`，然后空实现 `onRecordSend` 回调。

```
//设置自定义话单回调
[[NECallEngine sharedInstance] setCallRecordProvider:self];

// 接收话单回调，并空实现
- (void)onRecordSend:(NERecordConfig *)config{
    //空实现即可
}

```


<!-- ===== TE3Mjk2NzU | 悬浮窗 | platform=iOS ===== -->

# 悬浮窗

网易云信音视频呼叫组件支持悬浮窗功能。

## 效果展示

| 开启悬浮窗按钮 | 语音通话悬浮窗 | 视频通话悬浮窗 |
| --- | --- | --- |
| ![](https://yx-web-nosdn.netease.im/common/0af830a3a21ca2d00c350c83dc48ac43/开启悬浮窗按钮.png) | ![](https://yx-web-nosdn.netease.im/common/6130a513c071e55ae62dd46d90baca5b/语音通话悬浮窗.png) | ![](https://yx-web-nosdn.netease.im/common/57b79ec6ba29e7562e63894642db2030/视频通话悬浮窗.png) |

## 初始化配置项

使用 `setupWithConfig` 初始化音视频呼叫组件时，提前配置初始化配置项即可开启应用内悬浮窗或应用外悬浮窗功能。

| 配置项 | 参数类型 | 说明 |
| --- | --- | --- |
| `enableFloatingWindow` | BOOL | 是否开启应用内悬浮窗功能（通过 Rtc Canvas View 实现），默认不开启。
指在一个应用程序中，将某个窗口缩小并固定在屏幕的一角或一侧，同时允许用户在同一应用程序的其他部分进行操作。 |
| `enableFloatingWindowOutOfApp` | BOOL | 是否开启应用外悬浮窗功能（通过系统画中画实现），默认不开启。
指在一个应用程序以外，将某个应用程序的窗口缩小并固定在屏幕的一角或一侧，同时允许用户在屏幕上使用其他应用程序。 |

## 实现示例代码

```
NECallUIKitConfig *config = [[NECallUIKitConfig alloc] init];
...
// 开启应用内悬浮窗
config.uiConfig.enableFloatingWindow = YES;
// 开启应用外悬浮窗，目前只支持iOS16+
config.uiConfig.enableFloatingWindowOutOfApp = YES;
...
[[NERtcCallUIKit sharedInstance] setupWithConfig:config];

```

开启应用外悬浮窗需要引用额外转码库，只需要在主工程引入依赖即可，无需其他处理，内部会判断此插件扩展包是否集成。

```
pod 'NETranscodingKit'

```


<!-- ===== jY1NTU0MDE | 来电横幅 | platform=iOS ===== -->

# 来电横幅

自 v4.3.0 起，网易云信音视频呼叫组件支持来电横幅功能。当收到来电时，在屏幕顶部展示轻量横幅，支持一键接听或拒绝，不打断用户当前操作。

## 概述

音视频呼叫组件（CallKit）默认关闭来电横幅功能（与原有全屏来电界面行为一致）。若开启该功能，来电时展示顶部横幅，原全屏来电页不再出现。

- 该功能支持在运行时随时切换，并在 **下一次来电** 时生效（对当前已展示的横幅不生效）。
- 若在横幅展示期间有第二路来电，SDK 自动回复忙线。
- 横幅展示期间正常播放被叫铃声；点击横幅主体进入全屏来电页时，铃声 **不中断**。

## 效果展示

 ![image-netease](https://yx-web-nosdn.netease.im/common/c9362efa9b53f8b1a395bd0f0e62c8b5/横幅.png)

## 启用来电横幅功能

云信音视频组件在收到来电时，会在屏幕顶部展示轻量横幅，支持一键接听或拒绝，不打断用户当前操作。

iOS 横幅基于独立 `UIWindow` 实现，无需额外权限申请。如果需要启用该功能，可以使用 `enableIncomingBanner` 方法，在 NERtcCallKit 组件初始化后开启该功能：

```
// 开启
[[NERtcCallUIKit sharedInstance] enableIncomingBanner:YES];

// 关闭
[[NERtcCallUIKit sharedInstance] enableIncomingBanner:NO];

```

## 常见问题

**Q：横幅开启后，原有全屏来电界面还会出现吗？**

不会。开启横幅模式后，全屏来电界面被完全替换为横幅展示。若需恢复，调用 `enableIncomingBanner:NO` 即可。

**Q：横幅展示期间收到第二路来电怎么处理？**

SDK 会自动向第二路来电方回复忙线，行为与通话中收到新来电一致。

**Q：点击横幅主体进入全屏来电页，铃声会重新播放吗？**

不会。铃声会保持连续播放，进入全屏来电页时不会中断也不会重新触发。铃声统一在通话结束（接听/拒绝/超时/取消）时停止。


<!-- ===== Tk0MDU1Njg | 虚拟背景 | platform=iOS ===== -->

# 虚拟背景

网易云信音视频呼叫组件支持虚拟背景（背景虚化）功能。

## 效果展示

| 默认效果 | 开启虚拟背景效果 |
| --- | --- |
| ![](https://yx-web-nosdn.netease.im/common/76827f6d8381e14f05ebe4ef63bce979/音视频通话原效果.jpg) | ![](https://yx-web-nosdn.netease.im/common/0fb0a4c32d83b967e7903f07a1637917/音视频通话虚拟背景.jpg) |

## 初始化配置

使用 `setupWithConfig` 初始化时，您可以开启被叫预览功能。

| 配置项 | 参数类型 | 说明 |
| --- | --- | --- |
| `enableVirtualBackground` | BOOL | 是否开启背景虚化（通话中）功能，默认不开启。
指在通话过程中，通过自动识别用户人像，虚化用户周围的真实环境，从而保护用户的隐私。 |

## 启用虚拟背景功能

云信音视频组件允许用户在通话时使用虚拟背景功能，将视频通话画面的背景进行虚化。

如果需要启用该功能，可以使用 `enableVirtualBackground` 方法进行开启：

```
NECallUIKitConfig *config = [[NECallUIKitConfig alloc] init];
...
// 开启虚化(在视频通话中显示虚化按钮)
config.uiConfig.enableVirtualBackground = YES;
...
[[NERtcCallUIKit sharedInstance] setupWithConfig:config];

```

开启虚化功能需要增加 RTC 底层能力库，只需要在主工程引入依赖即可，无需其他处理。

```
pod 'NERtcSDK/Nenn'
pod 'NERtcSDK/Segment'

```


<!-- ===== DcyMjIwMjI | 自定义呼叫铃声 | platform=iOS ===== -->

# 自定义铃声

本文介绍在呼叫组件中如何自定义呼叫铃声，包括被叫铃声、呼叫等待铃声等。

## 功能介绍

`NERingFile` 类主要用于设置呼叫等待时、被叫时的铃声。

- 只有带UI的呼叫组件支持自定义铃声，不带UI的呼叫组件铃声需要业务自行实现。
- 提前加入 RTC 的模式不支持铃声，请引入 UI 组件源码自行实现。

## 实现方法

如果您希望自定义呼叫组件中用户的响铃，请在初始化呼叫组件时，设置 `NERingFile` 对象和铃声路径。

## 操作步骤

1. 将自定义铃声放在工程文件夹下。
2. 用 `pathForResource` 方法中获取铃声的存放路径。

可以设置不同场景的铃声，具体如下：

| 类型 | 说明 |
| --- | --- |
| callerRingFilePath | 主叫呼叫提示铃声 |
| calleeRingFilePath | 被叫收到邀请提示铃声 |
| rejectRingFilePath | 拒绝提示铃声 |
| busyRingFilePath | 忙线提示铃声 |
| noResponseFilePath | 无响应提示铃声 |
3. 调用 UI 组件单例赋值给暴露出来的路径变量（具体参考示例代码）。
4. （可选）自实现铃声播放。

主叫提前加入房间的配置下，实现铃声播放存在一些限制，可以根据业务需求自行实现此场景下的铃声播放。

呼叫组件 V2.2.0 版本才支持该功能。

## 示例代码

```
// 设置铃声路径
NSString *mp3Path = [[NSBundle mainBundle] pathForResource:@"custom" ofType:@"mp3"];
NERtcCallUIKit.sharedInstance.ringFile.callerRingFilePath = mp3Path;

// 铃声数据对象类
@interface NERingFile : NSObject

/// 主叫呼叫提示音
@property(nonatomic, strong, nullable) NSString *callerRingFilePath;

/// 被叫收到邀请提示音
@property(nonatomic, strong, nullable) NSString *calleeRingFilePath;

/// 拒绝提示音
@property(nonatomic, strong, nullable) NSString *rejectRingFilePath;

/// 忙线提示音
@property(nonatomic, strong, nullable) NSString *busyRingFilePath;

/// 无响应提示音
@property(nonatomic, strong, nullable) NSString *noResponseFilePath;

/// 初始化
- (instancetype)initWithBundle:(NSBundle *)bundle;

@end

```


<!-- ===== DEwMjEyOTg | 自定义接听背景图 | platform=iOS ===== -->

# 自定义接听背景图

本文介绍在呼叫组件中如何自定义接听音视频通话的背景图。

## 功能介绍

自 V4.8.0 起，iOS CallKitUI 支持配置接收方来电响铃/待接听音视频通话页面的自定义背景图。

接入方可使用本地 `UIImage` 资源或公开可访问的 `http/https` 图片 URL 作为背景图。

未配置背景图、资源不可用或 URL 加载失败时，组件会继续使用默认头像虚化背景。

## 实现方法

如果您希望自定义接听音视频通话的背景图，请配置本地资源或者 URL 图片作为接听背景图。

- 配置本地资源作为接听背景图。

```
UIImage *image = [UIImage imageNamed:@"custom_answer_bg"];
if (image) {
NECallUIDynamicConfig *config = [[NECallUIDynamicConfig alloc] init];
config.incomingCallBackground = [NECallUIIncomingBackgroundSource imageSource:image];
[[NERtcCallUIKit sharedInstance] setDynamicUIConfig:config];
}

```
- 配置 URL 图片作为接听背景图。

```
NSURL *url = [NSURL URLWithString:@"https://example.com/custom_answer_bg.jpg"];
if (url) {
NECallUIDynamicConfig *config = [[NECallUIDynamicConfig alloc] init];
config.incomingCallBackground = [NECallUIIncomingBackgroundSource urlSource:url];
[[NERtcCallUIKit sharedInstance] setDynamicUIConfig:config];
}

```

若您需要恢复默认背景图，也可以调用 `setDynamicUIConfig` 清除自定义接听背景图。

```
[[NERtcCallUIKit sharedInstance] setDynamicUIConfig:nil];

```

## API 参考

| API | 说明 |
| --- | --- |
| `NECallUIIncomingBackgroundSource` | 接听背景图来源。 |
| `+[NECallUIIncomingBackgroundSource imageSource:]` | 使用本地 `UIImage` 创建背景图来源。 |
| `+[NECallUIIncomingBackgroundSource urlSource:]` | 使用公开图片 URL 创建背景图来源。 |
| `NECallUIDynamicConfig.incomingCallBackground` | 接收方来电响铃/待接听页面背景图配置。 |
| `-[NERtcCallUIKit setDynamicUIConfig:]` | 设置动态 UI 配置；传 `nil` 表示清除配置。 |

## 常见问题

Q：什么时候生效？

A：配置后对后续展示的来电响铃/待接听页面生效，不保证刷新当前已经展示的来电页面。

Q：URL 图片有什么要求？

A：URL 需要为公开可访问的 `http/https` 图片地址。建议使用稳定、可快速访问的 HTTPS 地址，避免图片加载过慢影响展示效果。

Q：配置失败或图片加载失败会怎样？

A：组件会回退到默认头像虚化背景，不影响正常来电、接听、拒绝等呼叫流程。

Q：通话接通后是否继续使用该背景？

A：不会。该能力仅作用于接收方来电响铃/待接听页面，不会改变通话中页面背景。


<!-- ===== zE1OTYyNTQ | 私有化配置 | platform=iOS ===== -->

# 私有化配置

本文介绍呼叫组件如何将私有化配置相关参数透传给 NERTC SDK。

## 注意事项

- 呼叫组件集成 IM SDK 和 NERTC SDK 功能，因此呼叫组件的私有化配置需要配置 IM SDK 的私有化配置以及 NERTC SDK 的私有化配置。
- 呼叫组件没有包含 IM SDK 的初始化，因此 IM SDK 的私有化配置需要您自行实现，具体步骤请咨询网易云信技术支持工程师。

## 实现方式

以下是呼叫组件中，将私有化配置相关参数透传给 NERTC SDK的示例代码。

在初始化呼叫组件时，创建 `NERtcEngineContext` 方法，配置 RTC 私有化服务器相关参数。

```
NESetupConfig *setupConfig = [[NESetupConfig alloc] initWithAppkey:@"app key"];
NERtcEngineContext *context = [[NERtcEngineContext alloc] init];
NERtcServerAddresses *address = [[NERtcServerAddresses alloc] init];
address.channelServer = @"your channel server"; // 设置RTC私有化服务地址
...
// 设置私有化服务器地址信息
context.serverAddress = address;
setupConfig.rtcInfo = context;

[[NECallEngine sharedInstance] setup:setupConfig];

```

参数说明如下：

- `address.channelServer`：请配置 RTC 私有化服务地址，具体请参见[NERtcServerAddresses](https://doc.yunxin.163.com/nertc/api-refer/iOS/doxygen/Latest/zh/html/interface_n_e_rtc_server_addresses.html)。
- `context.serverAddress`：请配置为应用服务在私有化中的 URL 地址。


<!-- ===== TQ4MjIxOTU | 获取主被叫用户信息 | platform=iOS ===== -->

# 获取主被叫用户信息

在报表统计等场景中，您可能需要上报主被叫的一些用户信息。本文介绍获取主被叫的用户 ID（uid）、房间 ID （cid）、房间名称（cname）等信息。

## 呼叫时透传扩展信息给被叫

您可以在发起呼叫时设置扩展信息，例如用户昵称等。

1. 调用 `call` 接口发起呼叫时，在 `attachment` 参数中设置扩展信息，发起呼叫的详情参数参考如下：

```
/// 开始呼叫
/// @param userID 呼叫的用户ID
/// @param type 通话类型 {@link NERtcCallKitConsts =>  NERtcCallType}
/// @param attachment 扩展信息，透传到onInvited
/// @param extra 全局抄送
/// @param token  安全模式token，如果用户直接传入，不需要实现  tokenHandler  block回调
/// @param channelName  自定义channelName，不传会默认生成
/// @param completion 回调
- (void)call:(NSString *)userID
          type:(NERtcCallType)type
    attachment:(nullable NSString *)attachment
    globalExtra:(nullable NSString *)extra
      withToken:(nullable NSString *)token
    channelName:(nullable NSString *)channelName
    completion:(nullable void (^)(NSError *_Nullable error))completion;

```

1. 被叫用户获取相应的扩展信息。

被叫用户在收到来电通知时，通过 `NERtcCallKitDelegate#onInvited` 回调中的 `attachment` 参数获取主叫方透传的扩展信息。

```
/// 收到邀请的回调
/// @param invitor 邀请方
/// @param userIDs 房间中的被邀请的所有人（不包含邀请者）
/// @param isFromGroup 是否是群组
/// @param groupID 群组ID
/// @param type 通话类型
- (void)onInvited:(NSString *)invitor
          userIDs:(NSArray<NSString *> *)userIDs
      isFromGroup:(BOOL)isFromGroup
          groupID:(nullable NSString *)groupID
            type:(NERtcCallType)type
      attachment:(nullable NSString *)attachment;

```

## 获取本端的用户信息

主叫和被叫在加入 RTC 房间成功后，会触发`NERtcCallKitDelegate#onJoinChannel`回调，您可以在`NERtcCallKitDelegate#onJoinChannel` 回调中获取本端的 RTC 用户 ID（uid）、IM 用户 ID （accid）、RTC 房间 ID（cid）、RTC 房间名称（cname）。

`NERtcCallKitDelegate#onJoinChannel` 回调的接口模型如下：

```
/// 自己加入成功的回调，通常用来上报、统计等
/// @param event 回调参数
- (void)onJoinChannel:(NERtcCallKitJoinChannelEvent *)event;

@interface NERtcCallKitJoinChannelEvent : NSObject

/// IM userID
@property(nonatomic, copy) NSString *accid;

/// 音视频用户id
@property(nonatomic, assign) uint64_t uid;

/// 音视频channelId
@property(nonatomic, assign) uint64_t cid;

/// 音视频channelName
@property(nonatomic, copy) NSString *cname;

@end

```

请在收到 `onJoinChannel` 回调时，自行保存这些信息。

## 获取对端的用户信息

### 获取对端用户的 IM accid

被叫收到呼叫邀请时会触发`NERtcCallKitDelegate#onInvited`回调，在该回调的 `invitor` 参数中会返回对端用户的 IM accid。

`NERtcCallKitDelegate#onInvited` 回调的接口模型如下：

```
/// 收到邀请的回调
/// @param invitor 邀请方
/// @param userIDs 房间中的被邀请的所有人（不包含邀请者）
/// @param isFromGroup 是否是群组
/// @param groupID 群组ID
/// @param type 通话类型
- (void)onInvited:(NSString *)invitor
          userIDs:(NSArray<NSString *> *)userIDs
      isFromGroup:(BOOL)isFromGroup
          groupID:(nullable NSString *)groupID
             type:(NERtcCallType)type
       attachment:(nullable NSString *)attachment;

```

### 获取对端用户的 RTC Uid

主叫在调用 [call](https://doc.yunxin.163.com/nertccallkit/api-refer/iOS/doxygen/Latest/zh/html/interface_n_e_rtc_call_kit.html#a0231125b069d71c9b12cb54cde020056) 接口成功后，在通话过程中，您可以调用 `memberOfAccid` 接口，根据对端用户的 IM accid 获取对端用户的 uid。

根据 IM accid 获取用户的 uid 的接口如下：

```
/// @param accid  IM 用户id
/// @discussion 只有在通话中才能通过accid获取获取uid
- (void)memberOfAccid:(NSString *)accid
           completion:(nullable void (^)(NIMSignalingMemberInfo *_Nullable info))completion;

```


<!-- ===== TQwMTU2NjM | 拦截呼入请求 | platform=iOS ===== -->

# 拦截呼入请求

本文介绍在呼叫组件中如何拦截呼入请求以便开发者干预来电流程。

## 功能介绍

`NECallUIKitDelegate` 代理主要用于在用户使用呼叫组件 UI 层时接收呼叫，展示通知并启动配置的目标来电页面。如果您不想要呼叫组件自带的来电页面，想要拦截呼入请求后，自定义来电页面，只需要在返回值设为 NO 。

## 实现方法（V2）

```
- (void)didCallComingWithInviteInfo:(NEInviteInfo *)inviteInfo
                      withCallParam:(NEUICallParam *)callParam
                     withCompletion:(void (^)(BOOL))completion {
  // 此回调会携带会呼叫信息，UI组件参数
  // 自定义缺省头像被叫传入示例
  callParam.remoteDefaultImage = [[SettingManager shareInstance] remoteDefaultImage];
  callParam.muteDefaultImage = [[SettingManager shareInstance] muteDefaultImage];

  // completion(NO); 拦截被叫呼叫页面弹出，传YES时被叫弹出呼叫页面
  completion(YES);
}

```

## 实现方法（V1.8.2）

初始化呼叫组件时，调用 `NERtcCallUIConfig` 对象中的 `disableShowCalleeView` 方法拦截呼入请求，再通过自定义 UI 修改来电通知页面，自定义 UI 的具体方法请参见[自定义 UI](https://doc.yunxin.163.com/nertccallkit/docs/zM2Mzk2MjY?platform=iOS)。

以下示例代码展示如何拦截呼入请求：

```
NERtcCallUIConfig *config = [[NERtcCallUIConfig alloc] init];
config.uiConfig.disableShowCalleeView = YES; // 设置ui组件不弹出被叫页面，如果设置为YES，请自己监听回调实现被叫UI
[[NERtcCallUIKit sharedInstance] setupWithConfig:config];

```


<!-- ===== DMzMTExNjU | 实现单聊呼叫（无 UI） | platform=iOS ===== -->

# 实现 1 对 1 呼叫（无 UI）

本文介绍如何通过集成呼叫组件（NERTCCallkit）基础包（不含 UI），实现 1 对 1 呼叫相关业务逻辑。根据业务需求，您需要自行实现相关 UI 界面。本文介绍呼叫组件的集成和实现方法。

推荐您使用含 UI 集成呼叫组件版本，请参见 [实现1对1呼叫（含UI集成）](https://doc.yunxin.163.com/nertccallkit/guide/jg0MzU3NjM?platform=iOS)。

## 注意事项

针对呼叫组件中的回调信息，开发者要做好相应回调数据的上报及存储，以便于后期上线之后排查问题。

## 基本概念

- **account_id/AccId/accountId**：IM 账号 ID，用于登录 IM。[注册 IM 账号时](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)，IM 服务器会返回对应的账号 ID和密钥（Token），应用客户端需要负责保存账号和 IM Token 的映射关系。
- **Token**：呼叫组件中涉及的 Token 包括 IM Token，用于登录 IM 时进行 IM 账号鉴权。应用服务器调用 IM 服务器的 [注册账号 API](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)，获取的 IM Token。

## 开发环境

在开始运行工程之前，请您准备以下开发环境：

- Xcode 10 及以上版本。
- iOS 9.0 及以上版本的 iOS 设备。
- 已安装 CocoaPods。

## 准备工作

- 已在 [网易云信控制台](https://app.yunxin.163.com/global/home) [创建应用](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console)，并获取了对应的 App Key。
- 已 [开通](https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console) IM 即时通讯、音视频通话 2.0、话单功能以及信令产品。

## 集成呼叫组件

呼叫组件（NERTCCallkit）基于网易云信 NIM SDK（V10）和 NERTC SDK 实现通话呼叫。

### 第一步：集成 IM SDK

 单击查看集成 NIM SDK 的操作步骤。如果您的项目中已经集成了 NIM SDK，请忽略该步骤。
1. 安装 CocoaPods 后，在项目所在目录下执行以下命令，创建 Podfile 文件。

```
pod init

```
2. 在 Podfile 文件中添加对应的资源。

`CocoaPods` 集成资源关键词说明：

| 关键词 | 所含资源 |
| --- | --- |
| NIMSDK_LITE | IM 即时通讯，默认引入网易对象存储（NetEase Object Storage，NOS）文件存储能力 |
| NIMSDK_LITE/FCS | IM 即时通讯，默认引入 S3 文件存储能力 |

现以只需要集成即时通讯功能（NOS）为例，您可在 `Podfile` 中写入：

```
pod 'NIMSDK_LITE'

```
3. 执行以下命令更新本地仓库，查看版本信息。最新版本信息请参考 [更新日志](https://doc.yunxin.163.com/messaging2/docs/TIxNTgxOTk?platform=client)。

```
pod search NIMSDK_LITE   //本地仓库中查询资源的版本信息
pod repo update          //更新本地仓库

```
4. 执行以下命令安装 SDK：

```
pod install

```

**SDK 版本限制：**

呼叫组件与 NIM SDK、NERTC SDK 之间存在映射关系，使用呼叫组件时，必须使用指定版本的 NIM SDK 和 NERTC SDK。当前最新版本 V3.3.0 适配 NIM SDK V10.6.0 和 NERTC SDK V5.6.50，其他版本的适配关系请参考 [更新日志](https://doc.yunxin.163.com/messaging2/concept/TIxNTgxOTk?platform=client)。

### 第二步：初始化 IM SDK

将 IM SDK 集成到客户端后，需要先完成 IM SDK 的初始化才能使用其他功能。

1. 在项目文件中引入头文件 `NIMSDK.h`。

```
#import <NIMSDK/NIMSDK.h>

```
2. 调用 [registerWithOptionV2](https://doc.yunxin.163.com/messaging2/references/iOS/doxygen/Latest/zh/de/de3/interface_n_i_m_s_d_k.html#a4140971377bec8212dabd66fdea0bbb3) 方法初始化 SDK，推荐在应用程序启动时初始化，目的是配置 SDK 并准备进行登录和即时通讯功能。

```
NIMSDKOption *option = [NIMSDKOption optionWithAppKey:appKey];
option.apnsCername = @"your apns certificate";
option.pkCername = @"your push kit certificate";
// 配置 useV1Login
V2NIMSDKOption *v2Option = [[V2NIMSDKOption alloc] init];
//激活 V10 所有 API，默认使用 V10 的登录接口登录 IM
v2Option.useV1Login = NO;
//若仍使用 V9 的登录接口登录 IM
//v2Option.useV1Login = YES;
[[NIMSDK sharedSDK] registerWithOptionV2:option v2Option:v2Option];

```

以上提供了一个简化的初始化示例，更多初始化信息请参考 [初始化 SDK](https://doc.yunxin.163.com/messaging2/guide/jU5MTUwMDY?platform=client)。

### 第三步：实现登录 IM

调用 [`login`](https://doc.yunxin.163.com/messaging2/client-apis/TQ5NTUwNzQ?platform=client#login) 方法登录 IM。

本文以实现 **静态 Token** 登录为例，动态 Token 登录以及自动登录的实现方法请参考 [登录 IM](https://doc.yunxin.163.com/messaging2/guide/Dk1MTY4MzA?platform=client)。

示例代码如下：

```
- (void)login
{
    NSString *accountId = @"accountId";
    NSString *token = @"token";
    [[NIMSDK sharedSDK].v2LoginService login:accountId token:token
            option:nil
              success:^{
        NSLog(@"login succ");
    }
              failure:^(V2NIMError * _Nonnull error) {
        NSLog(@"login fail: error = %@", error);
    }];
}

```

### 第四步：引入呼叫组件

建议使用 CocoaPods 进行管理。呼叫组件内部没有引入 NERTC，请在 `Podfile` 中添加以下内容，单独引入 NERTC SDK。

```
pod 'NERtcCallKit', '3.3.0'
pod 'NERtcSDK', '5.6.50', :subspecs => ['RtcBasic']    // subspecs => ['RtcBasic']用于指定使用 RTC 基础版本，不包含美颜功能。若要使用美颜功能，此行请修改为 pod 'NERtcSDK', '5.6.50'

```

**SDK 版本限制：**

呼叫组件与 NIM SDK、NERTC SDK 之间存在映射关系，使用呼叫组件时，必须使用指定版本的 NIM SDK 和 NERTC SDK。当前最新版本 V3.3.0 适配 NIM SDK V10.6.0 和 NERTC SDK V5.6.50，其他版本的适配关系请参考 [更新日志](https://doc.yunxin.163.com/messaging2/concept/TIxNTgxOTk?platform=client)。

### 第五步：初始化呼叫组件

呼叫组件的实现为单实例，通过接口 [`NECallEngine.sharedInstance`](https://doc.yunxin.163.com/nertccallkit/references/iOS/doxygen/Latest/zh/html/interface_n_e_call_engine.html#ae49b53ff34cda8bbbc7a78a49423cca4) 获取此实例，调用实例方法 [`setup:`](https://doc.yunxin.163.com/nertccallkit/references/iOS/doxygen/Latest/zh/html/interface_n_e_call_engine.html#a3d65dcfd5e21ad67a2aac822f3b086bd) 完成初始化。

`setup:` 方法为使用组件前必须调用的方法，若未初始化直接调用其他组件方法，会发生不可预知的问题或故障。

```
#import <NERtcCallKit/NERtcCallKit.h>
@interface SomeViewController()<NECallEngineDelegate>
@end

@implementation SomeViewController

- (void)viewDidLoad {
    [self setupSDK];
}

- (void)setupSDK {
    NESetupConfig *config = [[NESetupConfig alloc] initWithAppkey:@"your app key"];
    [[NECallEngine sharedInstance] setup:config];
}

@end

```

## （可选）设置预览的分辨率

在初始化呼叫组件之后，调用 RTC 的 [`setLocalVideoConfig`](https://doc.yunxin.163.com/nertc/references/iOS/doxygen/Latest/zh/html/protocol_i_n_e_rtc_engine-p.html#a040ee549ad4a2b1d74d275defda38140) 接口，通过 `width` 和 `height` 参数设置采集分辨率。默认预览分辨率为 640*480。

```
NERtcEngine *coreEngine = [NERtcEngine sharedEngine];
NERtcVideoEncodeConfiguration *config = [[NERtcVideoEncodeConfiguration alloc] init];
config.width = 640;
config.height = 360;
[coreEngine setLocalVideoConfig:config];

```

## 实现一对一呼叫

您也可以调用呼叫组件的 API，自己实现呼叫相关功能，实现方法如下：

### 实现方法

呼叫组件的典型应用场景为 1 对 1 呼叫场景，即用户 A 发起视频呼叫用户 B，用户 B 同意呼叫，通话接通、两人进行实时音视频通信。

1. 用户 A 以及用户 B 均完成网易云信 IM SDK 的登录，并成功初始化呼叫组件。
2. 用户 A 获取到自己以及用户 B 登录网易云信 IM SDK 的账号。
3. （可选）配置自定义推送(发起呼叫时携带下文参数，具体可参考 API 文档)。

```
@interface NECallPushConfig : NSObject

/// 推送标题
@property(nonatomic, strong) NSString *pushTitle;

/// 推送内容
@property(nonatomic, strong) NSString *pushContent;

/// 推送自定义字段
@property(nonatomic, strong) NSMutableDictionary *pushPayload;

/// 是否计入未读计数，默认 YES
@property(nonatomic, assign) BOOL needBadge;

/// 是否需要推送，YES 表示推送，NO 表示不推送，默认 YES
@property(nonatomic, assign) BOOL needPush;

@end

```

  - 推送参数需要在 `pushPayload` 的内层推送参数 `apsField` 中设置。详细的配置方法请参考 [[KB0198] 关于 IM 提供的推送配置 apsField 字段](https://faq.yunxin.163.com/kb/main/#/item/KB0198)。
  - `pushPayload` 的外层推送参数中只支持 iOS 部分属性的设置，例如 sound、自定义参数等。
4. 用户 A 通过如下代码触发呼叫用户 B 的操作。

```
NECallParam *callParam = [[NECallParam alloc] initWithAccId:@"callee accid" withCallType:NECallTypeVideo];
[[NECallEngine sharedInstance] call:callParam completion:^(NSError * _Nullable error) {

}];

```

如果需要实现音视频录制，请在调用 `call` 方法之前，调用 [setParameters](https://doc.yunxin.163.com/nertc/references/iOS/doxygen/Latest/zh/html/protocol_i_n_e_rtc_engine-p.html#a2a674564c41bbd4b5556f08a9d65a558) 方法，设置 `kNERtcKeyRecordAudioEnabled` 和 `kNERtcKeyRecordVideoEnabled` 的值为 `YES`。
5. 用户 B 实现呼叫组件监听并且在有呼叫回调发生时候调用 [`accept`](https://doc.yunxin.163.com/nertccallkit/references/iOS/doxygen/Latest/zh/html/interface_n_e_rtc_call_kit.html#a25860582f125a1144a6960eeeff7beba) 方法即可实现通话。

  - 被叫通话页面的前置页面监听。

```
- (void)onReceiveInvited:(NEInviteInfo *)info {
    [NIMSDK.sharedSDK.userManager fetchUserInfos:@[info.callerAccId] completion:^(NSArray<NIMUser *> * _Nullable users, NSError * _Nullable error) {
            if (error) {
                NSLog(@"fetchUserInfo failed : %@", error);
            }else {
                //调起通话页面(根据业务自己实现或者参考示例工程中的通话页面 NECallViewController )
            }
        }];
}

```
  - 被叫通话页面中接受通话邀请。

```
- (void)acceptCall {
    __weak typeof(self) weakSelf = self;
    [[NECallEngine sharedInstance] accept:^(NSError * _Nullable error) {
        if (error) {
            NSLog(@"接听失败 : %@", error);
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                // 销毁当前通话页面
            });
        }else {
        //设置通话页面相关显示或者参考示例工程中的通话页面 NECallViewController
        }
    }];
}

```
6. 通话完成后单击挂断即可。

```
- (void)hangup{
    NEHangupParam *hangupParam = [[NEHangupParam alloc] init];
    [[NECallEngine sharedInstance] hangup:hangupParam completion:^(NSError * _Nullable error) {

    }];
}

```

### 示例代码

由于完成一次视频通话还需要预览视图设置等复杂调用，如果要接入工程并完成视频呼叫流程还需参考如下代码，如果需要实现一些复杂操作请参考 [呼叫组件示例 GitHub 项目源码](https://github.com/netease-kit/NECallKit/tree/master/iOS)。

1 对 1 通话在呼叫或收到呼叫邀请时需要设置相应的回调监听，用于接收对应通话的控制消息。首先在需要收到监听的地方实现 [`NERtcCallKitDelegate`](https://doc.yunxin.163.com/nertccallkit/references/iOS/doxygen/Latest/zh/html/protocol_n_e_rtc_call_kit_delegate-p.html)。

```
#import <NERtcCallKit/NERtcCallKit.h>
@interface SomeViewController: UIViewController <NECallEngineDelegate>

- (void)dealloc {
    [NECallEngine.sharedInstance removeCallDelegate:self];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [NECallEngine.sharedInstance addCallDelegate:self];
}

#pragma mark - NERtcVideoCallDelegate
// 被叫实现监听回调
- (void)onReceiveInvited:(NEInviteInfo *)info {
    [NIMSDK.sharedSDK.userManager fetchUserInfos:@[info.callerAccId] completion:^(NSArray<NIMUser *> * _Nullable users, NSError * _Nullable error) {
            if (error) {
                NSLog(@"fetchUserInfo failed : %@", error);
            }else {
                NIMUser *imUser = users.firstObject;
                NECallViewController *callVC = [[NECallViewController alloc] init];
                //callVC.isCaller = NO;
                //callVC.remoteImAccid = imUser.userId;
                [self.navigationController presentViewController:callVC animated:YES completion:nil];
            }
        }];
}

@end

```

`SomeViewController` 为通话页面的前置页面，可能是通讯录 IM 消息等页面，通话页面的使用参考下文代码或者示例工程。

```
@interface NECallViewController : UIViewController<NECallEngineDelegate>

@property(strong,nonatomic) UIView *smallVideoView;

@property(strong,nonatomic) UIView *bigVideoView;

@end

@implementation NECallViewController {

- (void)dealloc {
    [[NECallEngine.sharedInstance removeCallDelegate:self];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupUI];
    [self setupSDK];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [[NECallEngine sharedInstance] setupLocalView:nil];
}

- (void)setupUI {
    [self.view addSubview:self.bigVideoView];
    self.bigVideoView.frame = self.view.bounds;
    [self.view addSubview:self.smallVideoView];
    self.smallVideoView.frame = CGRectMake(0, 0, 100, 100);
}

- (void)setupSDK {
    [NECallEngine.sharedInstance addCallDelegate:self];
    [[NECallEngine sharedInstance] setTimeout:30];
    [[NERtcEngine sharedEngine] setLoudspeakerMode:YES];
    [[NERtcEngine sharedEngine] enableLocalVideo:YES];
}

// 主叫发起呼叫
- (void)didCall {

    NECallParam *callParam = [[NECallParam alloc] initWithAccId:@"callee accid" withCallType:NECallTypeVideo];
    [[NECallEngine sharedInstance] call:callParam completion:^(NSError * _Nullable error) {
        NSLog(@"call error code : %@", error);

                [[NECallEngine sharedInstance] setupLocalView:self.bigVideoView.videoView];
                if (error) {
                    /// 对方离线时 通过 APNS 推送 UI 不弹框提示
                    if (error.code == 10202 || error.code == 10201) {
                        return;
                    }

                    if (error.code == 21000 || error.code == 21001) {
                        //呼叫失败销毁当前通话页面
                    }
                }else {

                }
    }];
}

// 当被叫 onInvited 回调发生，调用 accept 接听呼叫
- (void)acceptCall {
    __weak typeof(self) weakSelf = self;
    [NECallEngine sharedInstance] accept:^(NSError * _Nullable error) {
        if (error) {
                    NSLog(@"接听失败 : %@", error);
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                        // 销毁当前通话页面
                    });
                }else {
                  [[NECallEngine sharedInstance] setupLocalView:weakSelf.smallVideoView];
                  [[NECallEngine sharedInstance] setupRemoteView:weakSelf.bigVideoView];
                }
    }];
}

// 主叫被叫结束通话
- (void)hangup{
    NEHangupParam *hangupParam = [[NEHangupParam alloc] init];
    [[NECallEngine sharedInstance] hangup:hangupParam completion:^(NSError * _Nullable error) {

    }];
}

- (UIView *)bigVideoView {
    if (!_bigVideoView) {
        _bigVideoView = [[UIView alloc] init];
        _bigVideoView.backgroundColor = [UIColor darkGrayColor];
    }
    return _bigVideoView;
}

- (UIView *)smallVideoView {
    if (!_smallVideoView) {
        _smallVideoView = [[UIView alloc] init];
        _smallVideoView.backgroundColor = [UIColor darkGrayColor];
    }
    return _smallVideoView;
}

#pragma mark - NERtcVideoCallDelegate

- (void)onUserEnter:(NSString *)userID {
    // 被叫加入可以进行视频通话，设置本地音视频相关 API
    [[NECallEngine sharedInstance] setupLocalView:self.smallVideoView];
    [[NECallEngine sharedInstance] setupRemoteView:self.bigVideoView];
}

```


<!-- ===== TcxNzQxNTY | 实现群组通话（无 UI） | platform=iOS ===== -->

# 群组通话

本文介绍了如何通过网易云信呼叫组件（CallKit）提供的 API 进行群组通话功能开发的详细步骤和代码示例。

群组通话功能目前在 Beta 测试阶段，若需要使用，请联系您的网易云信商务经理开通。

  ![image.png](https://yx-web-nosdn.netease.im/common/544b40530ae385be0ff51801b338ccf0/image.png)

## 适用场景

群组通话功能是现代通信应用的核心功能之一，它允许多个用户同时进行实时的视频和音频交流。无论是企业会议、在线教育、社交互动还是远程医疗咨询，这一功能都能提供高效的沟通手段，增强团队协作和信息共享。

- **在线教育**：教师和学生可以通过多人视频通话进行实时互动，共享屏幕和文档，提升在线学习体验。
- **企业会议**：团队成员无论身处何地，都能通过视频会议进行有效的远程协作和决策讨论。
- **社交互动**：朋友和家人可以通过群视频通话保持联系，共享生活瞬间。
- **远程医疗咨询**：医生和患者可以进行远程视频咨询，进行初步诊断和健康建议。
- **紧急服务**：紧急服务人员可以与现场人员进行实时视频通话，快速响应紧急情况。

## 前提条件

根据本文操作前，请确保您已经完成了以下设置：

- 在 [网易云信控制台](https://app.yunxin.163.com/global/home) 上创建至少一个应用。详细步骤请参考 [创建应用并获取 AppKey](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console)。
- 集成呼叫组件到示例项目。详细步骤请参考 [实现 1 对 1 呼叫（含 UI 集成 V2）](https://doc.yunxin.163.com/nertccallkit/guide/jg0MzU3NjM?platform=iOS)。

## 调用时序

以下流程图描述了一个群组通话的基本流程，包括初始化、通话过程、邀请过程和通话结束。

```
#mermaid-render-0 {font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:16px;fill:#333;}#mermaid-render-0 .error-icon{fill:#552222;}#mermaid-render-0 .error-text{fill:#552222;stroke:#552222;}#mermaid-render-0 .edge-thickness-normal{stroke-width:2px;}#mermaid-render-0 .edge-thickness-thick{stroke-width:3.5px;}#mermaid-render-0 .edge-pattern-solid{stroke-dasharray:0;}#mermaid-render-0 .edge-pattern-dashed{stroke-dasharray:3;}#mermaid-render-0 .edge-pattern-dotted{stroke-dasharray:2;}#mermaid-render-0 .marker{fill:#333333;stroke:#333333;}#mermaid-render-0 .marker.cross{stroke:#333333;}#mermaid-render-0 svg{font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:16px;}#mermaid-render-0 .actor{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-0 text.actor>tspan{fill:black;stroke:none;}#mermaid-render-0 .actor-line{stroke:grey;}#mermaid-render-0 .messageLine0{stroke-width:1.5;stroke-dasharray:none;stroke:#333;}#mermaid-render-0 .messageLine1{stroke-width:1.5;stroke-dasharray:2,2;stroke:#333;}#mermaid-render-0 #arrowhead path{fill:#333;stroke:#333;}#mermaid-render-0 .sequenceNumber{fill:white;}#mermaid-render-0 #sequencenumber{fill:#333;}#mermaid-render-0 #crosshead path{fill:#333;stroke:#333;}#mermaid-render-0 .messageText{fill:#333;stroke:none;}#mermaid-render-0 .labelBox{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-0 .labelText,#mermaid-render-0 .labelText>tspan{fill:black;stroke:none;}#mermaid-render-0 .loopText,#mermaid-render-0 .loopText>tspan{fill:black;stroke:none;}#mermaid-render-0 .loopLine{stroke-width:2px;stroke-dasharray:2,2;stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);}#mermaid-render-0 .note{stroke:#aaaa33;fill:#fff5ad;}#mermaid-render-0 .noteText,#mermaid-render-0 .noteText>tspan{fill:black;stroke:none;}#mermaid-render-0 .activation0{fill:#f4f4f4;stroke:#666;}#mermaid-render-0 .activation1{fill:#f4f4f4;stroke:#666;}#mermaid-render-0 .activation2{fill:#f4f4f4;stroke:#666;}#mermaid-render-0 .actorPopupMenu{position:absolute;}#mermaid-render-0 .actorPopupMenuPanel{position:absolute;fill:#ECECFF;box-shadow:0px 8px 16px 0px rgba(0,0,0,0.2);filter:drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4));}#mermaid-render-0 .actor-man line{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-0 .actor-man circle,#mermaid-render-0 line{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;stroke-width:2px;}#mermaid-render-0 :root{--mermaid-font-family:"trebuchet ms",verdana,arial,sans-serif;}用户 A（发起者）网易云信呼叫组件用户 BCD...（接受方）应用启动时自动初始化par[通话过程]par[邀请过程]par[通话结束]初始化1初始化2开始群呼 groupCall3发起通话4加入通话 groupJoin5邀请他人 groupInvite6发送通话邀请7接受邀请 groupAccept8通话结束 groupHangup9通知通话结束10确认通话结束11用户 A（发起者）网易云信呼叫组件用户 BCD...（接受方）
```

```
sequenceDiagram
autonumber
  actor U as 用户 A（发起者）
  participant SDK as 网易云信呼叫组件
  actor E as 用户 BCD...（接受方）

  %% 初始化过程
  Note over U,SDK: 应用启动时自动初始化
  U->>SDK: 初始化
  E->>SDK: 初始化

  %% 通话过程
  par 通话过程
  U->>SDK: 开始群呼 groupCall
  SDK->>E: 发起通话
  E->>SDK: 加入通话 groupJoin
  end

  %% 邀请过程
  par 邀请过程
  U->>SDK: 邀请他人 groupInvite
  SDK->>E: 发送通话邀请
  E->>SDK: 接受邀请 groupAccept
  end

  %% 通话结束
  par 通话结束
  U->>SDK: 通话结束 groupHangup
  SDK->>E: 通知通话结束
  E-->U: 确认通话结束
  end

```

## 初始化

以下示例代码描述了在 iOS 应用中如何初始化群组通话功能，包括设置配置参数。

```
  GroupConfigParam *param = [[GroupConfigParam alloc] init];
  param.appid = kAppKey;
  param.rtcSafeMode = YES;
  [[NEGroupCallKit sharedInstance] setupGroupCall:param];

```

## 开始群呼

以下示例代码提供了调用 [`groupCall`](https://doc.yunxin.163.com/nertccallkit/references/iOS/doxygen/Latest/zh/html/interface_n_e_group_call_kit.html#a56d560bb0dedaedf52f6f012bc24056a) 开始一个群组通话的代码示例，包括设置通话参数和处理通话结果。

```
GroupCallParam *param = [[GroupCallParam alloc] init];
    NSMutableArray *calleeList = [[NSMutableArray alloc] init];
    for (NEUser *user in self.datas) {
      if ([user.imAccid isEqualToString:self.caller.imAccid]) {
        user.state = GroupMemberStateInChannel;
        continue;
      }
      [calleeList addObject:user.imAccid];
    }
    param.calleeList = calleeList;
    NSString *uuid = [self getRandomString];
    param.callId = uuid;
    self.callId = uuid;
    NSLog(@"call ID : %@", param.callId);
    NSLog(@"call ID length : %lu", (unsigned long)param.callId.length);
    if ([[SettingManager shareInstance] isGroupPush] == YES) {
      param.pushParam.pushMode = GroupPushModeOpen;
      if ([[SettingManager shareInstance] customPushContent].length > 0) {
        param.pushParam.pushContent = [[SettingManager shareInstance] customPushContent];
      }
    } else {
      param.pushParam.pushMode = GroupPushModeClose;
    }

    [[NEGroupCallKit sharedInstance]
         groupCall:param
        completion:^(NSError *_Nullable error, GroupCallResult *_Nullable result) {
          if (error != nil) {
            [UIApplication.sharedApplication.keyWindow ne_makeToast:error.localizedDescription];
            [self didBack];
            return;
          }
          NSLog(@"group call :%@ result : %@", error, result);
        }];

```

## 群呼邀请

以下示例代码展示了如何调用 [`groupInvite`](https://doc.yunxin.163.com/nertccallkit/references/iOS/doxygen/Latest/zh/html/interface_n_e_group_call_kit.html#abd1cd60a7ac48a5bc403b21f756b7952) 邀请其他用户加入通话。

```
[[NEGroupCallKit sharedInstance]
    groupInvite:param
     completion:^(NSError *_Nullable error, GroupInviteResult *_Nullable result) {
       NSLog(@"groupInvite : %@", error);
       if (error != nil) {
         [UIApplication.sharedApplication.keyWindow ne_makeToast:error.localizedDescription];
         return;
       }
     }];

```

## 加入群呼

以下示例代码介绍了如何调用 [`groupAccept`](https://doc.yunxin.163.com/nertccallkit/references/iOS/doxygen/Latest/zh/html/interface_n_e_group_call_kit.html#af32cff48bb53c6406e31c27d9bd6390a) 接受通话邀请并加入通话。

```
GroupAcceptParam *param = [[GroupAcceptParam alloc] init];
  param.callId = self.callId;
  __weak typeof(self) weakSelf = self;
  [self startTimer];
  [[NEGroupCallKit sharedInstance]
      groupAccept:param
       completion:^(NSError *_Nullable error, GroupAcceptResult *_Nullable result) {
         if (error != nil) {
           [UIApplication.sharedApplication.keyWindow ne_makeToast:error.localizedDescription];
           [weakSelf didBack];
           return;
         }
         NSLog(@"call member user list : %@", result.groupCallInfo.calleeList);
         [DataManager.shareInstance
             fetchUserWithMembers:result.groupCallInfo.calleeList
                       completion:^(NSError *_Nullable error, NSArray<NEUser *> *_Nonnull users) {
                         NSLog(@"call neuser list : %@", users);
                         [weakSelf.datas removeAllObjects];
                         [weakSelf.datas addObjectsFromArray:users];
                         [weakSelf refreshCollection];
                       }];
       }];

```

## 挂断群呼

以下示例代码介绍了调用 [`groupHangup`](https://doc.yunxin.163.com/nertccallkit/references/iOS/doxygen/Latest/zh/html/interface_n_e_group_call_kit.html#af6b4571b2f90eb00b2742838fc7f0194) 结束通话的实现方法。

```
  GroupHangupParam *param = [[GroupHangupParam alloc] init];
  param.callId = self.callId;
  [[NEGroupCallKit sharedInstance]
      groupHangup:param
       completion:^(NSError *_Nullable error, GroupHangupResult *_Nullable result) {
         if (error != nil) {
           [[UIApplication sharedApplication].keyWindow ne_makeToast:error.localizedDescription];
           return;
         }
       }];

```

## 通话中添加成员

在通话中添加成员时，如果您需要直接修改对应的交互（UI），请参考 [Github 上的开源示例项目](https://github.com/netease-kit/NECallKit/tree/main/iOS)，在 `NEGroupCallViewController.m` 文件中的 `inviteBtn` 按钮，如需帮助您可以 [提交工单](https://app.yunxin.163.com/global/service/ticket/create) 联系网易云信技术支持工程师。

```
//UI 上添加成员按钮 具体方法参考 inviteUsers
- (NEExpandButton *)inviteBtn {
  if (!_inviteBtn) {
    _inviteBtn = [[NEExpandButton alloc] init];
    [_inviteBtn setImage:[UIImage imageNamed:@"group_add"] forState:UIControlStateNormal];
    [_inviteBtn addTarget:self
                   action:@selector(inviteUsers)
         forControlEvents:UIControlEventTouchUpInside];
  }
  return _inviteBtn;
}

```


<!-- ===== TM0MjUzNTQ | 实现单呼转群聊（无 UI） | platform=iOS ===== -->

# 实现单呼转群聊

自 V4.7.0 版本起，呼叫组件支持单呼转群呼功能。本文主要介绍如何通过集成呼叫组件（无 UI），在已接通的 1v1 通话中继续邀请其他用户加入当前通话，实现单呼平滑升级为多人通话。无 UI 接入方式下，通话界面由业务侧根据自身产品形态自行实现。

该功能并不是原有的群呼能力。单呼转群呼基于当前 1v1 信令房间和 RTC 房间，通过 `NECallEngine inviteMembers:completion:` 邀请新成员加入，不需要重新发起群呼。

## 注意事项

- 呼叫组件基于网易云信 NIM SDK 和 NERTC SDK 实现通话呼叫。
- 针对呼叫组件中的回调信息，开发者要做好相应回调数据的上报及存储，以便于后期上线之后排查问题。
- 参与单呼转群呼的端都需要使用支持该能力的新版本 SDK，并开启 `enableSingleToGroupCall`。任一端未开启或版本不支持时，`canInviteMembers` 返回 `NO`。
- 单呼转群呼功能仅允许在 1v1 呼叫已接通后发起。初始被叫未接听前，不支持邀请其他用户。
- 多人通话人数上限为 10 人。SDK 会按当前已加入成员、待接听成员和本次邀请账号数做校验。
- 邀请发送成功只表示邀请信令已发出，不表示对方已接听或已加入通话。被邀请方真正成为通话成员以 `onCallMembersChanged` 中成员状态变为 `NECallMemberStateJoined` 为准。
- 通话一旦进入多人模式，本次通话内会保持多人模式；即使后续只剩 2 人，也不会恢复 1v1 大画面和音视频切换能力。
- 进入多人模式后，不支持通话中音视频类型切换，业务侧应隐藏或禁用切换入口。
- 单呼转群呼话单由云信服务端生成，需要联系云信技术支持开通。开启 `enableSingleToGroupCall` 后，本地 SDK 默认 1v1 话单发送会被跳过；目前暂不支持通过 `setCallRecordProvider` 自行实现单呼转群呼话单。如需自定义话单，请联系云信技术支持。

## 基本概念

- `account_id`：`account_id` 是 IM 账号 ID，用于登录 IM。[注册 IM 账号时](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)，IM 服务器会返回对应的账号 ID（account_id）和密钥（Token），应用客户端需要负责保存 account_id 和 IM Token 的映射关系。
- `Token`：呼叫组件中涉及的 Token 包括 IM Token，用于登录 IM 时进行 IM 账号鉴权。应用服务器调用 IM 服务器的 [注册账号 API](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)，获取的 IM Token。
- `RTC uid`：用户加入 RTC 房间时使用的 ID，由呼叫组件在通话过程中维护，业务侧通常不需要在单呼转群呼接入中单独处理。
- `callId`：CallKit 业务通话 ID，用于回调、日志和邀请批次关联，而并非 NIM 信令房间 ID。
- `channelId`：NIM 信令房间 ID。业务通常不需要直接处理，可用于日志和问题排查。

## 开发环境

在开始运行工程之前，请您准备以下开发环境：

- Xcode 14 及以上版本。
- iOS 10.0 及以上版本的 iOS 设备。
- 已安装 CocoaPods。

## 准备工作

根据本文操作前，请确保您已经完成了以下设置：

- 在 [网易云信控制台](https://app.yunxin.163.com/global/home) [创建应用](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console)，并获取了对应的 App Key。
- 已 [开通](https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console) IM 即时通讯、音视频通话 2.0、话单功能以及信令产品。
- 已实现 [单聊呼叫](https://doc.yunxin.163.com/nertccallkit/guide/DMzMTExNjU?platform=iOS)。

使用单呼转群呼功能前，请先完成 1v1 单聊呼叫接入，并确认可以正常发起、接听和挂断 1v1 音视频通话。

## 实现单呼转群呼

1. 初始化呼叫组件。

在初始化时需要在 `NESetupConfig` 中开启 `enableSingleToGroupCall`，并注册 `NECallEngineDelegate`。

```
#import <NERtcCallKit/NERtcCallKit.h>

@interface CallManager () <NECallEngineDelegate>
@end

@implementation CallManager

- (void)setupCallEngineWithAppKey:(NSString *)appKey {
    NESetupConfig *config = [[NESetupConfig alloc] initWithAppkey:appKey];
    config.enableSingleToGroupCall = YES;

    [[NECallEngine sharedInstance] setup:config];
    [[NECallEngine sharedInstance] addCallDelegate:self];
}

@end

```
2. 发起 1v1 呼叫。

```
NECallParam *param = [[NECallParam alloc] initWithAccId:@"callee_account_id"
                                        withCallType:NECallTypeVideo];
param.extraInfo = @"business attachment";
param.globalExtraCopy = @"business global extra";

[[NECallEngine sharedInstance] call:param
                        completion:^(NSError *_Nullable error,
                                    NECallInfo *_Nullable callInfo) {
    if (error) {
        NSLog(@"call failed: %@", error);
        return;
    }
    NSLog(@"call sent, callId: %@", callInfo.callId);
}];

```
3. 处理普通来电和多人邀请来电。

收到邀请时，通过 `NEInviteInfo.multiCallInvite` 区分普通 1v1 来电和多人邀请。

```
- (void)onReceiveInvited:(NEInviteInfo *)info {
    if (info.multiCallInvite) {
        [self showMultiInvitePageWithInviter:info.callerAccId
                                    callType:info.callType
                                attachment:info.extraInfo];
    } else {
        [self showOneToOneIncomingPageWithCaller:info.callerAccId
                                        callType:info.callType
                                    attachment:info.extraInfo];
    }
}

```

 接听

```
[[NECallEngine sharedInstance] accept:^(NSError *_Nullable error,
                                        NECallInfo *_Nullable callInfo) {
    if (error) {
        NSLog(@"accept failed: %@", error);
        return;
    }
    NSLog(@"accept success, callId: %@", callInfo.callId);
}];

```

 拒绝、取消或挂断当前呼叫

```
NEHangupParam *param = [[NEHangupParam alloc] init];
param.extraString = @"business hangup extra";

[[NECallEngine sharedInstance] hangup:param completion:^(NSError *_Nullable error) {
    if (error) {
        NSLog(@"hangup failed: %@", error);
    }
}];

```

  接听拒绝、取消或挂断当前呼叫

```
[[NECallEngine sharedInstance] accept:^(NSError *_Nullable error,
                                        NECallInfo *_Nullable callInfo) {
    if (error) {
        NSLog(@"accept failed: %@", error);
        return;
    }
    NSLog(@"accept success, callId: %@", callInfo.callId);
}];

```

```
NEHangupParam *param = [[NEHangupParam alloc] init];
param.extraString = @"business hangup extra";

[[NECallEngine sharedInstance] hangup:param completion:^(NSError *_Nullable error) {
    if (error) {
        NSLog(@"hangup failed: %@", error);
    }
}];

```
4. 展示邀请入口。

业务侧应在 1v1 接通后调用 `canInviteMembers` 判断是否允许展示邀请入口。

```
- (void)refreshInviteButton {
    BOOL canInvite = [[NECallEngine sharedInstance] canInviteMembers];
    self.inviteButton.hidden = !canInvite;
}

- (void)onCallConnected:(NECallInfo *)info {
    // 这里只代表 1v1 通话已建立，不用于判断单呼转群呼是否应切多人 UI。
    [self refreshInviteButton];
}

```

`canInviteMembers` 会综合当前通话状态、是否开启单呼转群呼、对端能力、当前通话信息等条件。业务侧不要只根据本地开关决定是否展示入口。
5. 发起通话中邀请。

当用户在业务 UI 中选择成员后，调用 `inviteMembers:completion:`。

```
- (void)inviteUsers:(NSArray<NSString *> *)userIDs {
    if (![[NECallEngine sharedInstance] canInviteMembers]) {
        [self showToast:@"当前通话不支持邀请成员"];
        return;
    }

    NECallInviteParam *param = [[NECallInviteParam alloc] init];
    param.userIDs = userIDs;
    param.attachment = @"invite attachment";
    param.globalExtra = @"invite global extra";
    param.maxMembers = 10;

    NECallPushConfig *pushConfig = [[NECallPushConfig alloc] init];
    pushConfig.pushTitle = @"多人通话邀请";
    pushConfig.pushContent = @"邀请你加入多人通话";
    pushConfig.needPush = YES;
    pushConfig.needBadge = YES;
    param.pushConfig = pushConfig;

    [[NECallEngine sharedInstance] inviteMembers:param
                                    completion:^(NSError *_Nullable error,
                                                NECallInviteResult *_Nullable result) {
        if (error) {
            NSLog(@"invite failed: %@", error);
            [self showToast:error.localizedDescription ?: @"邀请失败"];
            return;
        }

        NSLog(@"invite sent, callId: %@, inviteBatchId: %@",
            result.callId, result.inviteBatchId);

        NSInteger successCount = 0;
        NSInteger failedCount = 0;
        for (NECallInviteItemResult *item in result.results) {
            if (item.isSuccess) {
                successCount += 1;
            } else {
                failedCount += 1;
                NSLog(@"invite item failed, user: %@, code: %ld, message: %@",
                    item.inviteeUserID, (long)item.code, item.message);
            }
        }

        if (successCount > 0 && failedCount == 0) {
            [self showToast:@"邀请已发送"];
        } else if (successCount > 0 && failedCount > 0) {
            [self showToast:@"部分邀请已发送，部分失败"];
        } else {
            [self showToast:@"邀请失败"];
        }
    }];
}

```

`NECallInviteParam` 字段说明：

| 字段 | 说明 |
| --- | --- |
| `userIDs` | 被邀请账号列表。SDK 会自动忽略无效账号、本端账号、已在通话成员和仍处于待接听的成员。 |
| `attachment` | 业务透传扩展，会透传到被邀请端 `NEInviteInfo.extraInfo`。 |
| `globalExtra` | 全局抄送扩展。 |
| `pushConfig` | 多人邀请通知和离线推送配置。 |
| `maxMembers` | 本次通话人数上限。不设置或小于等于 0 时使用默认值 10。 |
6. 切换多人 UI。

不同角色切换多人 UI 的时机不同：

  - 原 1v1 通话方：收到 `onCallModeChanged:` 且 `newMode == NECallModeMulti` 时，切换多人布局。
  - 第三方被邀请人：`onReceiveInvited:` 中 `info.multiCallInvite == YES` 表示这是多人邀请；用户点击接听后，`accept` 成功即可切换多人布局。
  - 兜底刷新：如果先收到 `onCallMembersChanged:`，且 `isInMultiCall == YES` 或成员快照中出现 `NECallMemberStateWaiting`，也可以先切换多人布局再刷新成员。

切换多人 UI 的逻辑建议做成幂等，避免多个回调连续触发时重复创建页面。

```
onReceiveInvited(info):
    if info.multiCallInvite == YES:
        展示多人邀请来电页
        记录当前来电为多人邀请

accept completion(error, callInfo):
    if error == nil 且当前来电是多人邀请:
        切换到多人 UI
        先用 currentMembers 渲染已有成员
        等待 onCallMembersChanged 补齐成员列表和媒体状态

onCallModeChanged(info):
    if info.newMode == NECallModeMulti:
        切换到多人布局
        隐藏或禁用音视频类型切换入口

```

`NECallModeChangeInfo` 字段说明：

| 字段 | 说明 |
| --- | --- |
| `oldMode` | 变化前通话模式。 |
| `newMode` | 变化后通话模式。 |
| `memberCount` | 当前有效成员数量，只统计已加入成员。 |
| `hasEverMulti` | 本次通话是否已经进入过多人模式；成功发起多人邀请并出现待接听成员后即为 `YES`。 |
7. 监听成员变化并刷新 UI。

邀请发出后，业务侧应根据 `onCallMembersChanged:` 刷新完整成员列表。

`onCallMembersChanged:` 不作为切换多人 UI 的唯一入口。第三方被邀请人刚接听成功时，成员快照可能先只有自己，随后才逐步补齐原 1v1 双方；因此应先切换多人 UI，再用该回调刷新宫格内容。

```
onCallMembersChanged(info):
    members = info.members
    if 当前还未进入多人 UI 且 (isInMultiCall == YES 或 members 中存在 Waiting 成员):
        切换到多人 UI

    按 members 重建或刷新多人宫格：
        - Waiting 成员：展示头像 / 昵称 / 等待接听占位
        - Joined 且 videoAvailable == YES 且 videoMuted == NO：展示视频画面
        - Joined 但未开视频：展示头像或音频占位
        - Leaving 成员：从宫格中移除，或展示离开态后移除

```

成员状态说明：

| 状态 | 说明 | UI 建议 |
| --- | --- | --- |
| `NECallMemberStateWaiting` | 待接听，还未加入 RTC。 | 展示头像/昵称占位和“等待接听”。 |
| `NECallMemberStateJoined` | 已加入 RTC。 | 展示音视频画面或音频头像。 |
| `NECallMemberStateLeaving` | 正在离开或已离开。 | 从列表移除或展示离开态后移除。 |

业务侧可以随时调用 `currentMembers` 获取当前完整成员快照：

```
NSArray<NECallMemberInfo *> *members = [[NECallEngine sharedInstance] currentMembers];

```
8. 监听邀请生命周期。

`onCallInviteStateChanged:` 只通知本端发出的邀请，不会通知被邀请端收到邀请或接听动作。

```
- (void)onCallInviteStateChanged:(NSArray<NECallInviteStateInfo *> *)infos {
    for (NECallInviteStateInfo *info in infos) {
        switch (info.state) {
            case NECallInviteStateSent:
                [self showInviteWaitingForUser:info.inviteeUserID];
                break;
            case NECallInviteStateJoined:
                [self showToast:@"对方已加入通话"];
                break;
            case NECallInviteStateRejected:
                [self showToast:@"对方已拒绝"];
                break;
            case NECallInviteStateTimeout:
                [self showToast:@"对方未接听"];
                break;
            case NECallInviteStateBusy:
                [self showToast:@"对方正在通话中"];
                break;
            case NECallInviteStateUnsupported:
                [self showToast:@"对方客户端不支持多人通话"];
                break;
            case NECallInviteStateFailed:
            case NECallInviteStateCanceled:
                [self showToast:@"邀请已结束"];
                break;
            default:
                break;
        }
    }
}

```

`NECallInviteStateInfo` 字段说明：

| 字段 | 说明 |
| --- | --- |
| `callId` | 当前通话 ID。 |
| `channelId` | 当前信令房间 ID。 |
| `inviteBatchId` | 本次批量邀请 ID，可关联 `inviteMembers` 的返回结果。 |
| `requestId` | 当前账号本次邀请请求 ID。 |
| `inviterUserID` | 邀请人账号。 |
| `inviteeUserID` | 被邀请人账号。 |
| `state` | 邀请生命周期状态。 |
| `reasonCode` | 状态原因码，可用于区分拒绝、忙线、超时、加入失败等。 |
| `message` | 兜底描述。UI 展示建议优先使用业务侧本地化文案。 |

邀请生命周期状态：

| 状态 | 说明 |
| --- | --- |
| `NECallInviteStateSent` | 邀请已发送，进入待接听。 |
| `NECallInviteStateJoined` | 被邀请方已加入通话。 |
| `NECallInviteStateRejected` | 被邀请方拒绝。 |
| `NECallInviteStateTimeout` | 邀请超时或接听后加入 RTC 超时。 |
| `NECallInviteStateBusy` | 被邀请方忙线。 |
| `NECallInviteStateFailed` | 邀请发送或加入通话失败。 |
| `NECallInviteStateCanceled` | 邀请被取消。 |
| `NECallInviteStateUnsupported` | 被邀请端不支持多人通话。 |
9. 渲染多人音视频画面。

无 UI 场景下，多人宫格建议以 `NECallMemberInfo` 为数据源。对已加入成员：

  - 本端用户：使用 NERTC 本地画布接口绑定本地视图。
  - 远端用户：使用 `member.uid` 调用 NERTC 远端画布接口。
  - 待接听成员：不要绑定 RTC 画布，只展示占位。

```
- (void)bindVideoForMember:(NECallMemberInfo *)member view:(UIView *)view {
    if (member.state != NECallMemberStateJoined) {
        return;
    }

    NERtcVideoCanvas *canvas = [[NERtcVideoCanvas alloc] init];
    canvas.container = view;
    canvas.renderMode = kNERtcVideoRenderScaleCropFill;

    NSString *currentUser = [[NIMSDK sharedSDK].v2LoginService getLoginUser];
    if ([member.userID isEqualToString:currentUser]) {
        [[NERtcEngine sharedEngine] setupLocalVideoCanvas:canvas];
        [[NERtcEngine sharedEngine] startPreview];
    } else {
        [[NERtcEngine sharedEngine] setupRemoteVideoCanvas:canvas
                                                forUserID:member.uid];
    }
}

```

`NECallEngine setupRemoteView:` 主要面向 1v1 远端画面。多人宫格中需要按成员 `uid` 分别绑定远端画布，建议直接使用 NERTC SDK 的 `setupRemoteVideoCanvas:forUserID:`。
10. 处理成员媒体状态变化。

Demo / CallKit-UI 的单呼转群呼页面主监听 `onCallMembersChanged:`：成员加入、离开、待接听占位，以及成员快照里的当前音视频状态都从 `NECallMemberChangeInfo.members` 获取。单呼转群呼场景下，Core 在远端视频开始、停止、mute 状态变化时也会更新成员媒体状态，并通过 `onCallMembersChanged:` 下发新的成员快照。

同时，Demo 也保留了 `onVideoMuted:userID:` 和 `onVideoAvailable:userID:`，用于对单个成员格子做视频状态的增量刷新。因此无 UI 接入建议以 `onCallMembersChanged:` 为主入口，再按需补充视频和音频回调。

推荐无 UI 接入按相同方式处理：

  - `onCallMembersChanged:`：主监听。刷新完整多人成员列表，并读取 `NECallMemberInfo.audioMuted`、`videoMuted`、`videoAvailable` 作为当前快照状态。
  - `onVideoMuted:userID:`：补充监听。远端或本端视频 mute 状态变化时，更新对应成员的视频开关状态。
  - `onVideoAvailable:userID:`：补充监听。远端视频流可用性变化时，更新对应成员的视频画面显示。
  - `onAudioMuted:userID:` / `onLocalAudioMuted:`：如果业务 UI 需要展示麦克风图标，再监听这两个音频 mute 回调。

处理逻辑可参考以下伪代码：

```
onCallMembersChanged(info):
    members = info.members，如果为空则读取 currentMembers

    对每个 member 刷新宫格数据：
        - 记录 member.userID / uid / state
        - 读取 member.videoMuted 和 member.videoAvailable，决定展示视频画面还是头像占位
        - 读取 member.audioMuted，决定是否展示麦克风关闭图标

onVideoMuted(muted, userId):
    找到 userId 对应成员
    更新该成员 videoMuted = muted
    只刷新该成员格子的视频开关状态

onVideoAvailable(available, userId):
    找到 userId 对应成员
    更新该成员 videoAvailable = available
    available == NO 时隐藏视频画面，available == YES 时恢复视频画面

onAudioMuted(muted, userId):
    如果业务展示麦克风状态，更新 userId 对应成员的音频 mute 图标

onLocalAudioMuted(muted):
    如果业务展示本端麦克风状态，更新当前登录用户的音频 mute 图标

```

如果业务不展示成员麦克风状态，只处理 `onCallMembersChanged:`、`onVideoMuted:userID:` 和 `onVideoAvailable:userID:` 即可满足 Demo 同款多人视频宫格刷新。

## API 参考

| API | 说明 |
| --- | --- |
| `NESetupConfig.enableSingleToGroupCall` | 是否开启单呼转群呼能力，默认 `NO`。 |
| `canInviteMembers` | 当前通话是否允许继续邀请成员。 |
| `isInMultiCall` | 当前通话是否已经进入过多人模式。 |
| `currentMembers` | 当前完整成员快照。 |
| `accept:` | 接听来电。第三方被邀请人接听 `multiCallInvite == YES` 的多人邀请成功后，即可切换多人 UI。 |
| `inviteMembers:completion:` | 通话中邀请成员加入当前通话。 |
| `onCallConnected:` | 当前端 1v1 通话建立回调，不用于判断单呼转群呼是否应切多人 UI。 |
| `onCallModeChanged:` | 通话模式变化，原 1v1 通话方首次进入多人模式时触发，适合切换多人布局。 |
| `onCallMembersChanged:` | 通话成员或成员媒体状态变化，返回完整成员快照；用于刷新多人宫格，不应等成员数达到 3 才切换多人 UI。 |
| `onVideoMuted:userID:` | 视频 mute 状态变化，用于单成员视频状态增量刷新。 |
| `onVideoAvailable:userID:` | 远端视频流可用性变化，用于单成员视频画面增量刷新。 |
| `onAudioMuted:userID:` / `onLocalAudioMuted:` | 音频 mute 状态变化，业务展示麦克风状态时监听。 |
| `onCallInviteStateChanged:` | 本端发出的邀请生命周期变化。 |
| `onReceiveInvited:` | 收到普通 1v1 邀请或多人邀请。多人邀请时 `multiCallInvite == YES`。 |

## 常见问题

**为什么 1v1 接通后没有展示邀请入口？**

请确认是否满足以下条件：

1. 本端初始化时是否设置 `enableSingleToGroupCall = YES`。
2. 是否已建立 1v1 通话，且当前状态为通话中。
3. 对端是否为支持单呼转群呼的新版本，并同样开启能力。
4. 当前是否已达到人数上限。

业务侧建议直接以 `[[NECallEngine sharedInstance] canInviteMembers]` 的返回值作为入口展示依据。

**`inviteMembers` 返回成功后，为什么成员还没出现在通话中？**

`inviteMembers` 的 completion 只表示邀请信令发送结果。成员真正加入以 `onCallMembersChanged:` 中 `NECallMemberStateJoined` 为准。邀请发送后可以先展示 `NECallMemberStateWaiting` 占位。

**被邀请方如何区分普通 1v1 来电和多人邀请？**

在 `onReceiveInvited:` 中判断 `NEInviteInfo.multiCallInvite`。

```
if (info.multiCallInvite) {
    // 多人邀请
} else {
    // 普通 1v1 呼叫
}

```

**多人通话退回 2 人后，可以恢复 1v1 UI 吗？**

不建议恢复。目前在进入多人模式后，本次通话保持多人模式；退回 2 人时仍展示多人双人宫格，并继续禁用音视频切换。

**可以直接用 `NEGroupCall` 发起多人通话吗？**

单呼转群呼不使用旧版 `NEGroupCallKit` 群呼链路。该能力是在已有 1v1 通话内邀请成员加入当前房间，请使用 `NECallEngine inviteMembers:completion:`。

**无 UI 场景必须处理哪些回调？**

至少需要处理以下回调：

- `onReceiveInvited:`：展示普通来电页或多人邀请页；`multiCallInvite == YES` 时记录为多人邀请。
- `accept:`：第三方被邀请人接听多人邀请成功后切换多人 UI。
- `onCallConnected:`：1v1 通话建立后展示通话中页面，并刷新邀请入口。
- `onCallModeChanged:`：原 1v1 通话方进入多人模式后切换多人布局。
- `onCallMembersChanged:`：刷新成员列表、待接听占位和音视频画面。
- `onCallInviteStateChanged:`：展示邀请拒绝、超时、忙线、不支持等提示。
- `onCallEnd:`：收口页面和释放资源。