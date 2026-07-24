# 跑通示例项目

In the video call sample project, the call component is used to realize audio and video calls. This article introduces the sample project of how to quickly run through audio and video calls and experience the audio and video call function.

## Development environment

The requirements for the development environment of the sample project are as follows:

| Environmental requirements | Explain                                                                                                  |
| -------------------------- | -------------------------------------------------------------------------------------------------------- |
| JDK version                | Version 1.8.0 and above                                                                                  |
| Android API version        | API 21, Android 5.0 and above                                                                            |
| CPU architecture           | ARM64, ARMV7                                                                                             |
| Ide                        | Android Studio                                                                                           |
| Other                      | Rely on Androidx and do not support the support library. Mobile devices with Android system 4.3 or above |

## Prerequisites

Before starting to run the sample project, please make sure that you have completed the following operations:

- [The app](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console)has been [created and the App Key of the application has been obtained](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console).
- The following services have been opened. If not, please refer to the [opening service](https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console)to open.
  - IM instant messaging. When using the call component's own order function, you need to open IM.
  - signal. It is used to realize peer-to-point call invitations and audio and video calls.
  - Audio and video calls 2.0. It is used to realize real-time audio and video calls.

It is recommended to open the **debugging mode**([acontification method](https://doc.yunxin.163.com/nertc/server-apis/TcxNDAxMTI?platform=server)) of audio and video calls. It is recommended that the debugging mode can only be used in the integrated development stage. Please change back to the security mode before the application is officially launched.

- If you need to copy, please open the **call list**copy service in the message copy in advance, so as to send an event notification message after the end of a call, mark whether the call is connected and call time, type and other data.

## Operation steps

- The sample source code is only for developers to access for reference. In the actual application development scenario, please modify it according to specific business needs.
- If you plan to use the source code in the production environment, please make sure that the application has been fully tested before it is officially launched to avoid losses caused by compatibility and other problems.
- The sample code contains business login logic. If you need to use login-related functions in your business, please modify the login-related logic.

1. Go to GitHub to clone the [NECallKit sample project source code](https://github.com/netease-kit/NECallKit/tree/main/Android)repository to your local.
2. 找到 app 下的 `build.gradle` 文件替换为您的 App Key。

```
def appKey = "your appKey"
// app key for code
defaultConfig {
  buildConfigField "String", "APP_KEY", "\"${appKey}\""
}

```

3. Compile and run the sample project.

................

# 实现单聊呼叫

The call component (NERTCCallkit) simplifies the call process through UI componentization. You only need to call a few lines of code to realize single chat (1 to 1) call, that is, point-to-point call, and includes the UI interface of the call. This article introduces the integration and implementation methods of calling components.

## Notes

- The call component (NERTCCallkit) realizes calls based on NetEase Yunxin NIM SDK and NERTC SDK.
- For the callback information in the call component, developers should do a good job of reporting and storing the corresponding callback data, so as to facilitate troubleshooting problems after going online later.

## Basic concept

- `account_id`：`account_id` 是 IM 账号 ID，用于登录 IM。[注册 IM 账号时](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)，IM 服务器会返回对应的账号 ID（account_id）和密钥（Token），应用客户端需要负责保存 account_id 和 IM Token 的映射关系。
- Token: The Token involved in the call component includes IM Token, which is used to identify IM accounts when logging in to IM. The application server calls the [registration account API of the](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)IM server and obtains the IM Token.

## Development environment

| Environmental requirements                                                                                                                                                                                        | Explain                                                                                                                                                                                                               |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Android Studio version                                                                                                                                                                                            | Android Studio 5.0 and above.Please refer to the [Android Studio version description](https://developer.android.google.cn/studio/releases/index.html)for changes to the Android Studio version number system.         |
| Android API version                                                                                                                                                                                               | Level is version 21 and above.                                                                                                                                                                                        |
| Android SDK version                                                                                                                                                                                               | Android SDK 31, Android SDK Platform-Tools 31.x.x and above versions.                                                                                                                                                 |
| Gradle and the required dependency library                                                                                                                                                                        | Download the corresponding version of Gradle and the required dependency library on the [Gradle Services](https://services.gradle.org/distributions/)page. Gradle Version: 7.4.1Android Gradle Plug-in Version: 7.1.3 |
| For the version dependency between Android Gradle plug-in, Gradle and SDK Tool, please refer to the [version description of Android Gradle plug-in](https://developer.android.com/studio/releases/gradle-plugin). |
| [Kotlin](https://blog.jetbrains.com/kotlin/category/releases/)                                                                                                                                                    | Version 1.6.21 and above.                                                                                                                                                                                             |
| CPU architecture                                                                                                                                                                                                  | ARM 64, ARMV7.                                                                                                                                                                                                        |
| Ide                                                                                                                                                                                                               | Android Studio.                                                                                                                                                                                                       |
| Other                                                                                                                                                                                                             | Rely on Androidx and do not support the support library.                                                                                                                                                              |
| The real machine of Android system 5.0 and above.Due to the lack of camera and microphone capabilities of the simulator, the project needs to be run on the real machine.                                         |

## Preparation work

Before following this article, please make sure that you have completed the following settings:

- [Create an application](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console)in the [NetEase Cloud Trust Console](https://app.yunxin.163.com/global/home)and obtain the corresponding App Key.
- [Open](https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console)IM instant messaging, audio and video call 2.0, signaling products and call list functions.

## Sample project source code

NetEase Yunxin provides [sample project source code](https://github.com/netease-kit/NECallKit/tree/main/Android), which you can modify and adapt based on.

## Integrated call components

The call component (NERTCCallkit) realizes call calls based on NetEase Yunxin NIM SDK (V10) and NERTC SDK, and NIM SDK and NERTC SDK have been integrated into the call component. You just need to integrate NERTCCallkit.

1. 在项目根目录下的 **build.gradle** 文件中，配置 `repositories`（使用 maven）。示例代码如下：

```
allprojects {
    repositories {
        //...
        mavenCentral()
        //...
    }
}

```

2. In the **build.gradle**file under the **app**directory, configure the supported SO library architecture. The sample code is as follows:

```
android {
defaultConfig {
    ndk {
        //设置支持的 SO 库架构
        abiFilters "armeabi-v7a", "x86","arm64-v8a","x86_64"
        }
}
}

```

3. Introduce call components according to the needs of the developer's project.

The bottom layer of the call component needs to rely on NIM SDK and NERTC SDK.

引入呼叫组件（不指定依赖的版本）

直接引入呼叫组件，组件会自动使用当前兼容的依赖版本，无需单独指定。

```
implementation 'com.netease.yunxin.kit.call:call-ui:3.3.0'

```

引入呼叫组件（指定依赖的版本）

若您的项目已集成 NIM SDK 或 NERTC SDK，需指定具体版本并排除组件内置依赖。

```
//如果因业务需求或其他原因无法使用指定版本 SDK时xu'yao
implementation('com.netease.yunxin.kit.call:call-ui:3.3.0') {
    exclude group: 'com.netease.nimlib'
    exclude group: 'com.netease.yunxin', module: 'nertc-base-sdk'
    }

// 使用您项目中已有的 SDK 版本
implementation 'com.netease.nimlib:basesdk:10.6.0'  // IM 基础功能包示例版本，请使用实际版本
// implementation "com.netease.nimlib:chatroom:10.6.0" // IM 聊天室功能包示例版本，请使用实际版本
implementation 'com.netease.yunxin:nertc:5.6.50'    // RTC 包示例版本，请使用实际版本

```

- 呼叫组件各个版本的依赖版本请参考 [呼叫组件更新日志](https://doc.yunxin.163.com/nertccallkit/concept/DMzOTI3NTA?platform=client)。
- IM 相关组件的依赖版本需要一致（比如 IM 基础包和聊天室的版本相同），否则会报错。

Introduce call components (no dependent version specified)Introduce call components (specify the dependent version)

Directly introduce the call component, and the component will automatically use the current compatible dependent version without specifying it separately.

```
implementation 'com.netease.yunxin.kit.call:call-ui:3.3.0'

```

若您的项目已集成 NIM SDK 或 NERTC SDK，需指定具体版本并排除组件内置依赖。

```
//如果因业务需求或其他原因无法使用指定版本 SDK时xu'yao
implementation('com.netease.yunxin.kit.call:call-ui:3.3.0') {
    exclude group: 'com.netease.nimlib'
    exclude group: 'com.netease.yunxin', module: 'nertc-base-sdk'
    }

// 使用您项目中已有的 SDK 版本
implementation 'com.netease.nimlib:basesdk:10.6.0'  // IM 基础功能包示例版本，请使用实际版本
// implementation "com.netease.nimlib:chatroom:10.6.0" // IM 聊天室功能包示例版本，请使用实际版本
implementation 'com.netease.yunxin:nertc:5.6.50'    // RTC 包示例版本，请使用实际版本

```

- 呼叫组件各个版本的依赖版本请参考 [呼叫组件更新日志](https://doc.yunxin.163.com/nertccallkit/concept/DMzOTI3NTA?platform=client)。
- IM 相关组件的依赖版本需要一致（比如 IM 基础包和聊天室的版本相同），否则会报错。

4. Add permissions.

根据实际应用需求，在 `AndroidManifest.xml` 中添加以下配置，并请将 `com.netease.nim.demo` 替换为自己的包名。

Specific sample code

```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="com.netease.nim.demo">

    <!-- 权限声明 -->
    <!-- 访问网络状态-->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>

    <!-- 外置存储存取权限 -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

    <!-- 多媒体相关 -->
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <!-- Android11：V8.6.1 及之后的版本不需要。其他：V4.4.0 及之后的版本不需要。 -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/>

    <!-- 控制呼吸灯，振动器等，用于新消息提醒 -->
    <uses-permission android:name="android.permission.FLASHLIGHT" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <!-- 8.0+系统需要-->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

    <!-- 下面的 uses-permission 一起加入到您的 AndroidManifest 文件中。-->
    <permission
        android:name="${applicationId}.permission.RECEIVE_MSG"
        android:protectionLevel="signature"/>
    <uses-permission android:name="${applicationId}.permission.RECEIVE_MSG"/>

    <application
        ...>
        <!-- App Key, 可以在这里设置，也可以在 SDKOptions 中提供。
            如果 SDKOptions 中提供了，则取 SDKOptions 中的值。-->
        <meta-data
            android:name="com.netease.nim.appKey"
            android:value="key_of_your_app" />

        <!-- 网易云信后台服务，请使用独立进程。-->
        <service
            android:name="com.netease.nimlib.service.NimService"
            android:process=":core"/>
        <!-- 网易云信后台服务，使用 V10 接口需要添加以下代码。-->
        <service
            android:name="com.netease.nimlib.service.NimServiceV2" />

        <!-- 网易云信后台辅助服务 -->
        <service
            android:name="com.netease.nimlib.job.NIMJobService"
            android:exported="false"
            android:permission="android.permission.BIND_JOB_SERVICE"
            android:process=":core"/>

        <!-- 网易云信监视系统启动和网络变化的广播接收器，保持和 NimService 同一进程 -->
        <receiver android:name="com.netease.nimlib.service.NimReceiver"
            android:process=":core"
            android:exported="false">
            <intent-filter>
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
            </intent-filter>
        </receiver>

        <!-- 网易云信进程间通信 Receiver -->
        <receiver android:name="com.netease.nimlib.service.ResponseReceiver"/>

        <!-- 网易云信进程间通信 service -->
        <service android:name="com.netease.nimlib.service.ResponseService"/>

        <!-- 网易云信进程间通信 provider -->
        <provider
            android:name="com.netease.nimlib.ipc.NIMContentProvider"
            android:authorities="${applicationId}.ipc.provider"
            android:exported="false"
            android:process=":core" />
        <!-- 网易云信内部使用的进程间通信 provider -->
        <!-- SDK 启动时会强制检测该组件的声明是否配置正确，如果检测到该声明不正确，SDK 会主动抛出异常引发崩溃 -->
        <provider
            android:name="com.netease.nimlib.ipc.cp.provider.PreferenceContentProvider"
            android:authorities="${applicationId}.ipc.provider.preference"
            android:exported="false" />

        <!-- 网易云信内部使用的进程间通信 provider -->
        <!-- SDK 启动时会强制检测该组件的声明是否配置正确，如果检测到该声明不正确，SDK 会主动抛出异常引发崩溃 -->
        <provider
            android:name="com.netease.nimlib.ipc.cp.provider.PreferenceContentProvider"
            android:authorities="com.netease.nim.demo.ipc.provider.preference"
            android:exported="false" />
    </application>
</manifest>

```

5. Configure to prevent code confusion.

Code confusion refers to the use of short and meaningless names to rename classes, methods, attributes, etc. to increase the difficulty of reverse engineering and ensure the security of the Android program source code. In order to avoid abnormal call components caused by renaming classes, you need to configure anti-code confusion.

请在 `proguard-rules.pro` 配置文件中加入以下代码防止混淆：

```
# NIM SDK 的类，如果集成 IM 时已经添加，请忽略
-dontwarn com.netease.nim.**
-keep class com.netease.nim.** {*;}

-dontwarn com.netease.nimlib.**
-keep class com.netease.nimlib.** {*;}

-dontwarn com.netease.share.**
-keep class com.netease.share.** {*;}

-dontwarn com.netease.mobsec.**
-keep class com.netease.mobsec.** {*;}

# NERTC SDK 的类
-keep class com.netease.lava.** {*;}
-keep class com.netease.yunxin.** {*;}

# 呼叫组件的类
-dontwarn com.netease.yunxin.kit.**
-keep class com.netease.yunxin.kit.** {*;}
-keep public class * extends com.netease.yunxin.kit.corekit.XKitInitOptions
-keep class * implements com.netease.yunxin.kit.corekit.XKitService {*;}

```

## Initialize the call component

You can initialize it anywhere in the application code.

1. 调用 [`NIMClient#initV2`](https://doc.yunxin.163.com/messaging2/references/android/doxygen/Latest/zh/classcom_1_1netease_1_1nimlib_1_1sdk_1_1_n_i_m_client.html#aa8e0e524f223d29e8f2499557a2ae2e5) 方法进行 IM 的初始化。

SDK 的配置信息请参考 [`SDKOptions`](https://doc.yunxin.163.com/messaging2/references/android/doxygen/Latest/zh/classcom_1_1netease_1_1nimlib_1_1sdk_1_1_s_d_k_options.html)。

The sample code is as follows:

```
// 激活 V10 API 后，可以根据该字段选项选择是否禁用 V10 API 登录，默认 false，即使用 V10 API 登录
// sdkOptions.disableV2Login = true;
...
// 按需设置其它 SDKOptions 设置项

NIMClient.initV2(context, sdkOptions);

```

The above provides a simplified initialization example. For more initialization information, please refer to [Initialization NIM SDK](https://doc.yunxin.163.com/messaging2/guide/TY4OTgyMDk?platform=client). 2. 调用 [`init`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a51657e9a1752b501cba27df1060402f4) 接口进行呼叫组件的初始化。初始化时必须设置 `rtcAppKey` 参数，其他参数可以按需配置。

`rtcAppKey`For the AppKey of NetEase Yunxin application, please check the AppKey of the specified application on the application details page in the [NetEase Yunxin console](https://app.yunxin.163.com/global/home).

App users can normally receive calls from other people or initiate calls actively after initialization on this terminal. If the initialization is not completed, the component may prompt the application to initialize, or the called app may not prompt when it is called. If the App repeatedly calls the initialization interface, the previous initialization settings will be destroyed, subject to the new initialization settings.

呼叫组件初始化相关代码内容可以放在工程的 `MainActivity` 中执行，尽量避免在 `MainActivity#onDestroy()` 方法中做组件的释放。建议在 App 用户登出时释放，登入时进行初始化。

For the description of core function parameters, please refer to the [initialization parameter configuration](https://doc.yunxin.163.com/nertccallkit/guide/DI5Nzg0OTM?platform=android).

The sample code is as follows:

```
CallKitUIOptions options = new CallKitUIOptions.Builder()
// 必要：音视频通话 sdk appKey，用于通话中使用
.rtcAppKey(appKey)
// 通话接听成功的超时时间单位 毫秒，默认 30s
.timeOutMillisecond(30 * 1000L)
// 此处为 收到来电时展示的 notification 相关配置，如图标，提示语等。
.notificationConfigFetcher(neInviteInfo -> new CallKitNotificationConfig(R.drawable.ic_logo))
// 收到被叫时若 app 在后台，在恢复到前台时是否自动唤起被叫页面，默认为 true
.resumeBGInvitation(true)
// 请求 rtc token 服务，若非安全模式则不需设置（V1.8.0 版本之前需要配置，V1.8.0 及之后版本无需配置）
//.rtcTokenService((uid, callback) -> requestRtcToken(appKey, uid, callback)) // 自己实现的 token 请求方法
// 设置初始化 RTC SDK 相关配置，按照所需进行配置
.rtcSdkOption(new NERtcOption())
// 呼叫组件初始化 rtc 范围，NECallInitRtcMode.GLOBAL-全局初始化，
// NECallInitRtcMode.IN_NEED-每次通话进行初始化以及销毁，全局初始化有助于更快进入首帧页面，
// 当结合其他组件使用时存在 rtc 初始化冲突可设置 NECallInitRtcMode.IN_NEED
// 或当结合其他组件使用时存在 rtc 初始化冲突可设置 NECallInitRtcMode.IN_NEED_DELAY_TO_ACCEPT
.initRtcMode(NECallInitRtcMode.IN_NEED)
.build();
// 不要重复初始化组件可能会产生 sdk 初始化失败问题
CallKitUI.init(getApplicationContext(), options);

```

## Log in

调用 [`login`](https://doc.yunxin.163.com/messaging2/client-apis/TQ5NTUwNzQ?platform=client#login) 方法进行登录。

This article takes **static Token**login as an example. Please refer to the implementation methods of dynamic Token login and automatic login.

The sample code is as follows:

```
NIMClient.getService(V2NIMLoginService.class).login("account", "token", null, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // TODO
    }
},
    new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        int code = error.getCode();
        String desc = error.getDesc();
        // TODO
    }
});

```

## Realize single chat call

The typical application scenario of the call component (NERTCCallkit) is the single chat call scenario, that is, user A initiates a video call to user B, user B agrees to call, the call is connected, and the two people carry out real-time audio and video communication.

The call component UI kit has included the relevant logic of the call. You only need to call a few lines of code to trigger the call.

The business process of single chat call is as follows:

1. Both user A and user B have completed the login of NetEase Yunxin IM SDK and successfully initialized the call component.
2. User A obtains the account number (account_id) of himself and user B logging in to NetEase Yunxin IM SDK.
3. User A calls user B through `startSingleCall`.

```
// @param type：呼叫类型 NECallType.AUDIO-音频呼叫，NECallType.VIDEO-视频呼叫
// @param calledAccId：被叫方 IM 账号 account_id
CallParam param = new CallParam.Builder()
  .callType(NECallType.VIDEO)
  .calledAccId(calledAccId)
  .build();
CallKitUI.startSingleCall(getActivity(), param);

```

4. User B clicks the answer button on the called page to make a video call.
5. Just click to hang up after the call is completed.

## Custom UI

You can customize the interface according to the existing settings of the call component. For details, please refer to [Custom UI](https://doc.yunxin.163.com/nertccallkit/guide/zYzNzI5NDI?platform=android).

## Advanced functions

In addition to the basic call process, the call component (NERTCCallkit) also supports the call list function, custom UI, etc. You can refer to the advanced function document to realize the relevant business process.

- [Talk list](https://doc.yunxin.163.com/nertccallkit/guide/TkyODAzMDQ?platform=android)
- [Set the audio and video attributes of RTC](https://doc.yunxin.163.com/nertccallkit/guide/DA3ODA0MDQ?platform=android)
- [Custom call ringtone](https://doc.yunxin.163.com/nertccallkit/guide/zkyMTI3ODA?platform=android)
- [Intercept inbound requests](https://doc.yunxin.163.com/nertccallkit/guide/zY2MDY2MDI?platform=android)

......................

# 实现群组通话

This article introduces the detailed steps and code examples of how to develop the group call function through the API provided by the NetEase Cloud Call Component (CallKit).

The group call function is currently in the Beta testing stage. If you need to use it, please contact your NetEase Yunxin business manager to open it.

![Image.png](https://yx-web-nosdn.netease.im/common/544b40530ae385be0ff51801b338ccf0/image.png)

## Applicable scene

The group call function is one of the core functions of modern communication applications. It allows multiple users to communicate in real time with video and audio at the same time. Whether it is enterprise meetings, online education, social interaction or telemedicine consultation, this function can provide efficient means of communication and enhance teamwork and information sharing.

- **Online education**: Teachers and students can interact in real time through multiple video calls, share screens and documents, and improve the online learning experience.
- **Enterprise meeting**: Team members can conduct effective remote collaboration and decision-making discussion through video conferencing no matter where they are.
- **Social interaction**: Friends and family can keep in touch through group video calls and share life moments.
- **Telemedicine consultation**: Doctors and patients can conduct remote video consultation for preliminary diagnosis and health advice.
- **Emergency service**: Emergency service personnel can make real-time video calls with on-site personnel to respond quickly to emergencies.

## Prerequisites

Before following this article, please make sure that you have completed the following settings:

- Create at least one application on the [NetEase Cloud Trust console](https://app.yunxin.163.com/global/home). For detailed steps, please refer to [Create an app and get AppKey](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console).
- Integrate the call components to the sample project. For detailed steps, please refer to [Realize 1-to-1 call (including UI integration V3).](https://doc.yunxin.163.com/nertccallkit/guide/jcyNDE3MDM?platform=android)

## Initialize

The following sample code introduces how to initialize the group call function in the Android app, including setting SDK options and starting the call interface.

```
CallKitUIOptions options = new CallKitUIOptions();
options....
options.enableGroup = true;
CallKitUI.init(getApplicationContext(), options);

```

## Start the group call

The following sample code provides an example of the code to start a group call, including creating call parameters and starting a call.

```
GroupCallParam param =
    new GroupCallParam.Builder()
        .callId(UUID.randomUUID().toString())
        .callees(userArray)
        .extraInfo(extraInfo.toString())
        .build();
CallKitUI.startGroupCall(this, param);

```

## Join the group call

The following sample code shows how to join an ongoing multi-person call.

```
GroupJoinParam param = new GroupJoinParam(callId);
CallKitUI.joinGroupCall(GroupSettingActivity.this, param);

```

## Add members in the call

The following sample code explains how to invite other members to join the call.

```
CallKitUIOptions options =
    new CallKitUIOptions.Builder()
        // 音视频通话 sdk appKey，用于通话中使用
        .rtcAppKey(ConfigCenter.getAppKey())
        // 当前用户 accId
        .currentUserAccId(AuthManager.getInstance().getUserModel().imAccid)
        .currentUserRtcUId(SettingActivity.RTC_CHANNEL_UID)
        // 通话接听成功的超时时间单位 毫秒，默认 30s
        .timeOutMillisecond(30 * 1000L)
        // 当系统版本为 Android Q 及以上时，若应用在后台系统限制不直接展示页面
        // 而是展示 notification，通过单击 notification 跳转呼叫页面
        // 此处为 notification 相关配置，如图标，提示语等。
        .notificationConfigFetcher(new DemoSelfNotificationConfigFetcher<>())
        .notificationConfigFetcherForGroup(
            new DemoSelfNotificationConfigFetcher<>())
        // 收到被叫时若 app 在后台，在恢复到前台时是否自动唤起被叫页面，默认为 true
        .resumeBGInvitation(true)
        .enableGroup(true)
        .enableAutoJoinWhenCalled(SettingActivity.ENABLE_AUTO_JOIN)
        // 设置用户信息
        .userInfoHelper(new SelfUserInfoHelper())
        // rtc 初始化模式
        .initRtcMode(SettingActivity.RTC_INIT_MODE)
        // 主叫加入 rtc 的时机
        .joinRtcWhenCall(SettingActivity.ENABLE_JOIN_RTC_WHEN_CALL)
        .contactSelector(
            (context, groupId, strings, listNEResultObserver) -> {
              if (listNEResultObserver == null) {
                return null;
              }
              TransHelper.launchTask(
                  context,
                  CODE_REQUEST_INVITE_USERS,
                  (innerContext, code) -> {
                    NERTCSelectCallUserActivity.startSelectUser(
                        innerContext,
                        CODE_REQUEST_INVITE_USERS,
                        CallModeType.RTC_GROUP_INVITE,
                        strings);
                    return null;
                  },
                  intentResultInfo -> {
                    if (intentResultInfo == null
                        || intentResultInfo.getValue() == null) {
                      return null;
                    }
                    Intent data = intentResultInfo.getValue();
                    if (intentResultInfo.getSuccess()) {
                      ArrayList<String> selectorList =
                          data.getStringArrayListExtra(
                              NERTCSelectCallUserActivity.KEY_CALL_USER_LIST);
                      listNEResultObserver.onResult(selectorList);
                    }
                    return null;
                  });
              return null;
            })
        .language(NECallUILanguage.AUTO)
        .build();

```

....................

# 实现单呼转群呼

Since version V4.7.0, the call component supports the single-call to group call function. This article mainly introduces how to continue to invite other users to join the current call in the connected 1v1 single chat call by integrating the call component (including UI), so as to smoothly upgrade the single call to a multi-person call, and reuse the built-in UI of the component.

该功能并不是原有的群呼能力（`NEGroupCall`）。单呼转群呼基于当前 1v1 信令房间和 RTC 房间，通过 `NECallEngine.inviteMembers` 邀请新成员加入，不需要重新发起群呼。

## Notes

- The call component realizes call calls based on NetEase Yunxin NIM SDK and NERTC SDK.
- For the callback information in the call component, developers should do a good job of reporting and storing the corresponding callback data, so as to facilitate troubleshooting problems after going online later.
- The end that participates in single-call to group call needs to use the new version of SDK that supports this ability and turn on the single-call to group call ability. When either end is not open or the version is not supported, the invitation entrance will not be displayed.
- The single-call-to-group-call function is only allowed to be initiated after the 1v1 call is connected. CallKit-UI does not display the invitation entrance before the initial call is answered.
- The maximum number of people on multi-person calls is 10. The component will be verified according to the current number of members who have joined, members to be answered and the number of accounts invited this time.
- The successful sending of the invitation only means that the invitation signal has been sent, and does not mean that the other party has answered or joined the call. The real membership is subject to the component receiving the member status `NECallMemberState.JOINED`.
- `onCallConnected`It only indicates that the current 1v1 call is established. In the UI scenario, the original 1v1 caller switches the multi-person layout by the component according to `onCallModeChanged`; after the third-party invitee successfully answers the invitation of `multiCallInvite == true`, the component will immediately enter the multi-person layout. Bureau, and then refresh the joining status through the member snapshot.
- Once the call enters the multiplayer mode, the multiplayer mode will be maintained in this call; even if there are only 2 people left in the follow-up, the 1v1 large screen and audio and video switching ability will not be restored.
- After entering the multiplayer mode, the switching of audio and video types during the call is not supported, and CallKit-UI will automatically handle the prompts and disable status.
- The single call to group call list is generated by the Yunxin server, and you need to contact the Yunxin technical support to open it. After turning on `enableSingleToGroupCall`, the local SDK default 1v1 call order sending will be skipped; at present, it is not supported to realize single call to group call list by yourself through `setCallRecordProvider`. If you need to customize the call list, please contact Yunxin Technical Support.

## Basic concept

- `account_id`：`account_id` 是 IM 账号 ID，用于登录 IM。[注册 IM 账号时](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)，IM 服务器会返回对应的账号 ID（account_id）和密钥（Token），应用客户端需要负责保存 account_id 和 IM Token 的映射关系。
- `Token`: The Token involved in the call component includes IM Token, which is used for IM account authentication when logging in to IM. The application server calls the [registration account API of the](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)IM server and obtains the IM Token.
- `RTC uid`: The ID used by the user when joining the RTC room is maintained by the call component during the call, and the business side usually does not need to be processed separately in the single-call group call access.
- `callId`: Call component business call ID, which is used for callback, log and invitation batch association, not NIM signaling room ID.
- `channelId`: NIM signaling room ID. Business usually does not need direct processing and can be used for logs and troubleshooting.

## Development environment

| Environmental requirements                                                                                                                                                                                        | Explain                                                                                                                                                                                                               |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Android Studio version                                                                                                                                                                                            | Android Studio 5.0 and above.Please refer to the [Android Studio version description](https://developer.android.google.cn/studio/releases/index.html)for changes to the Android Studio version number system.         |
| Android API version                                                                                                                                                                                               | Level is version 21 and above.                                                                                                                                                                                        |
| Android SDK version                                                                                                                                                                                               | Android SDK 31, Android SDK Platform-Tools 31.x.x and above versions.                                                                                                                                                 |
| Gradle and the required dependency library                                                                                                                                                                        | Download the corresponding version of Gradle and the required dependency library on the [Gradle Services](https://services.gradle.org/distributions/)page. Gradle Version: 7.4.1Android Gradle Plug-in Version: 7.1.3 |
| For the version dependency between Android Gradle plug-in, Gradle and SDK Tool, please refer to the [version description of Android Gradle plug-in](https://developer.android.com/studio/releases/gradle-plugin). |
| [Kotlin](https://blog.jetbrains.com/kotlin/category/releases/)                                                                                                                                                    | Version 1.6.21 and above.                                                                                                                                                                                             |
| CPU architecture                                                                                                                                                                                                  | ARM 64, ARMV7.                                                                                                                                                                                                        |
| Ide                                                                                                                                                                                                               | Android Studio.                                                                                                                                                                                                       |
| Other                                                                                                                                                                                                             | Rely on Androidx and do not support the support library.                                                                                                                                                              |
| The real machine of Android system 5.0 and above.Due to the lack of camera and microphone capabilities of the simulator, the project needs to be run on the real machine.                                         |

## Preparation work

Before following this article, please make sure that you have completed the following settings:

- [Create an application](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console)in the [NetEase Cloud Trust Console](https://app.yunxin.163.com/global/home)and obtain the corresponding App Key.
- [Open](https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console)IM instant messaging, audio and video call 2.0, signaling products and call list functions.
- [Single chat call](https://doc.yunxin.163.com/nertccallkit/guide/jcyNDE3MDM?platform=android)has been realized.

Before using the single-call to group-call function, please complete the 1v1 single chat call access first, and confirm that you can initiate, answer and hang up 1v1 audio and video calls normally.

## Sample project source code

NetEase Yunxin provides [sample project source code](https://github.com/netease-kit/NECallKit/tree/main/Android), which you can modify and adapt based on.

## Realize single call to group call

1. Initialize the call component.

在初始化时需要开启单呼转群呼功能。设置 `singleToGroupInviteMode` 和 `inviteContactSelector`。CallKit-UI 会根据 `singleToGroupInviteMode != DISABLED` 自动在底层 `NESetupConfig` 中开启 `enableSingleToGroupCall`。

```
CallKitUIOptions options =
    new CallKitUIOptions.Builder()
        .rtcAppKey(appKey)
        .singleToGroupInviteMode(NECallSingleToGroupInviteMode.AFTER_1V1_CONNECTED)
        .inviteContactSelector(
            (context, inviteContext, observer) -> {
            if (observer == null) {
                return;
            }

            // 展示业务自己的联系人选择页。
            // inviteContext.getInCallUserAccIds() 中的账号应置灰或过滤。
            openContactSelector(
                context,
                inviteContext.getInCallUserAccIds(),
                inviteContext.getRemainingCount(),
                selectedAccIds -> observer.onResult(selectedAccIds));
            })
        .build();

CallKitUI.init(getApplicationContext(), options);

```

2. Launch a 1v1 call.

Before 1v1 is connected, CallKit-UI does not display the invitation entrance. After 1v1 is connected, if both this end and the other end support and turn on the single call to group call ability, the call page will display the invitation entrance.

```
CallParam callParam =
    new CallParam.Builder()
        .callType(NECallType.VIDEO)
        .calledAccId("callee_account_id")
        .callExtraInfo("business attachment")
        .build();

CallKitUI.startSingleCall(context, callParam);

```

3. Realize the callback to add users to join the call.

After clicking the invitation entrance, CallKit-UI will call `NECallInviteContactSelector.onSelectInviteMembers`. The business side displays its contact selection page in the callback.

```
.inviteContactSelector(
    (context, inviteContext, observer) -> {
    if (observer == null) {
        return;
    }

    List<String> disabledAccIds = inviteContext.getInCallUserAccIds();
    int maxSelect = inviteContext.getRemainingCount();

    openContactSelector(
        context,
        disabledAccIds,
        maxSelect,
        selectedAccIds -> observer.onResult(selectedAccIds));
    })

```

`NECallInviteUIContext`Field description:

| Field              | Explain                                                                                                                         |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------- |
| `callId`           | The current call ID is used for business logs and troubleshooting.                                                              |
| `channelId`        | The current channel room ID is usually only used for debugging.                                                                 |
| `currentUserAccId` | The current login account.                                                                                                      |
| `inCallUserAccIds` | Accounts that are already in the call or have been invited, the business selection page should be filtered or grayed out.       |
| `remainingCount`   | At present, the number of people who can be invited, the business selection page should limit the maximum number of selections. |
| `maxMembers`       | The maximum number of people in this call is 10 by default.                                                                     |

如果用户取消选择，返回 `null` 或空列表即可：

```
observer.onResult(null);

```

## Related interfaces

| API                                                 | Explain                                                                                                                                                                     |
| --------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `CallKitUIOptions.Builder.singleToGroupInviteMode`  | Including UI scene invitation entrance mode, disabled by default.                                                                                                           |
| `NECallSingleToGroupInviteMode.DISABLED`            | Disable the invitation entrance of single call to group call.                                                                                                               |
| `NECallSingleToGroupInviteMode.AFTER_1V1_CONNECTED` | 1v1 The invitation entrance can be displayed after connecting.                                                                                                              |
| `CallKitUIOptions.Builder.inviteContactSelector`    | After clicking the invitation entrance, the business side selects people to call back.                                                                                      |
| `NESetupConfig.enableSingleToGroupCall`             | Core layer single call to group call ability switch, CallKit-UI will be automatically set according to the invitation entrance mode.                                        |
| `onCallConnected`                                   | At present, the front-end 1v1 call establishes a callback, which is not used to judge whether a single call should be cut from a group call to a multi-person UI.           |
| `onCallModeChanged`                                 | The original 1v1 caller switches the multiplayer layout when entering the multiplayer mode for the first time.                                                              |
| `onCallMembersChanged`                              | The main entrance refreshes the member's palace, the waiting place and the member's audio and video snapshots, which is not the only entrance to switch the multiplayer UI. |
| `onVideoMuted`/`onVideoAvailable`                   | Incrementally refresh the video status of a single member grid.                                                                                                             |
| `onAudioMuted`                                      | Synchronize the audio mute state.                                                                                                                                           |
| `onCallInviteStateChanged`                          | Display the invitation results such as rejection, timeout, busy line, not supported, etc.                                                                                   |
| `onReceiveInvited`                                  | Display the invitee's information when multiple people invite calls.                                                                                                        |

## Frequently asked questions

**Why is the invitation entrance not displayed after 1v1 is connected?**

Please confirm whether the following conditions are met:

1. Whether to set `singleToGroupInviteMode = NECallSingleToGroupInviteMode.AFTER_1V1_CONNECTED`.
2. Whether to set `inviteContactSelector`.
3. Whether a 1v1 call has been established and the current status is in call.
4. Whether the other end is a new version that supports single-call to group call, and also turns on the ability.
5. Has the upper limit of the number of people been reached at present?

**After clicking the invitation entrance, will the component provide the default account input panel?**

No way. CallKit-UI only provides entrance, basic filtering, sending and multi-person layout. The account input panel, contact selection page and organizational structure page are implemented by the business through `inviteContactSelector`.

**`inviteContactSelector`Which accounts to return?**

Return the list of IM account IDs. The business side should filter or gray out the accounts in `inviteContext.inCallUserAccIds`, and limit the number of selections to no more than `inviteContext.remainingCount`.

**Can the 1v1 UI be restored after the multiplayer call is returned to 2 people?**

Recovery is not recommended. At present, after entering the multiplayer mode, this call remains in multiplayer mode; when returning to 2 people, it still displays the multiplayer double-player palace, and continues to disable audio and video switching.

**可以直接用 `NEGroupCall` 发起多人通话吗？**

单呼转群呼不使用旧版 `NEGroupCall` 群呼链路。该能力是在已有 1v1 通话内邀请成员加入当前房间。

................

# 通话话单

The call component provides the call list function. After a call, you will receive the corresponding call list. The call list is an event notification message that marks the status of this call. The call list is sent in the form of an IM session type message copy. After receiving the call list, you can parse the message body and get call details such as call time.

There are 5 types of single messages of the cloud message call component, of which 4 types are **the list when it is not connected**(sent by the caller client), and 1 type is the **normal list with the call time**(sent directly by the server).

The unanswered call orders sent by the caller's client include **refusal call orders**, **busy call orders**, **timeout unanswered call orders**, and **caller cancellation call orders**.

The following figure shows examples of common call lists. From top to bottom, they are the main caller cancellation list, the called rejection list, the timeout call list, the called busy call list, and the normal call list with duration.

![image-netease](https://yx-web-nosdn.netease.im/quickhtml%2Fassets%2Fyunxin%2Fdoc%2FNERtcCallKit-CallTicket01.png)

Multi-person calls default to unencapsulated call list function. If you need to use the speech list, please do it yourself in other ways.

## Use the call list function

### Method 1: Use the word list of component packaging

#### **Step 1: Open the call list and copy**

1. Log in to the NetEase Cloud Console. Open IM instant messaging, audio and video calls and signaling (senaling is a sub-function of IM instant messaging and needs to be opened separately). For details, please refer to the [function of opening or closing](https://doc.yunxin.163.com/console/concept/TQ2NzE5MzQ?platform=console).
2. Open the call component in the CC message and copy the single copy.

![Opening of the call list.png](https://yx-web-nosdn.netease.im/common/26c20659a94204cd4b1027fe64521e4b/%E8%AF%9D%E5%8D%95%E5%BC%80%E9%80%9A.png)

#### **Step 2: Send the order**

After turning on the call list function, the call list message is sent by default.

The call component does not include the function of receiving and parsing the call list. Users need to refer to the source code of the sample project to implement it by themselves. For detailed instructions, please refer to [Messages](https://doc.yunxin.163.com/messaging2/guide/DYzMjA0Njc?platform=client#%E6%94%B6%E5%8F%91%E8%87%AA%E5%AE%9A%E4%B9%89%E6%B6%88%E6%81%AF).

#### **Step 3: Receive and parse the call list**

Single messages are received through NIM SDK like ordinary messages. The sample code is as follows:

```
/**
  * 话单消息接收注册，同正常消息接收一样
  */
private void registerObserver() {
    V2NIMMessageListener messageListener = new V2NIMMessageListener() {
        @Override
        public void onReceiveMessages(List<V2NIMMessage> messages) {
            if (messages != null) {
                for (V2NIMMessage msg : messages) {
                    V2NIMMessageAttachment attachment = msg.getAttachment();
                    if (attachment instanceof V2NIMMessageCallAttachment) {
                        parseForNetCall(msg, (V2NIMMessageCallAttachment) attachment);
                    }
                }
            }
        }

        @Override
        public void onSendMessage(V2NIMMessage message) {
            if (message.getSendingState() == V2NIMMessageSendingState.V2NIM_MESSAGE_SENDING_STATE_SUCCEEDED) {
                V2NIMMessageAttachment attachment = message.getAttachment();
                if (attachment instanceof V2NIMMessageCallAttachment) {
                    parseForNetCall(message, (V2NIMMessageCallAttachment) attachment);
                }
            }
        }
    };

    NIMClient.getService(V2NIMMessageService.class).addMessageListener(messageListener);
}

```

**Message structure of the single message**

| Type of conversation list     | Value | Explain                                                                                                                                                                                                                                                           |
| ----------------------------- | ----- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `NERecordCallStatus.COMPLETE` | 1     | Normal call list, both parties to the call enter the audio and video call and hang up. Sent by the server.                                                                                                                                                        |
| `NERecordCallStatus.CANCELED` | 2     | The caller cancels the call list, and the caller takes the initiative to cancel the call list after calling. Sent by the client caller.                                                                                                                           |
| `NERecordCallStatus.REJECTED` | 3     | The call to be refused to answer the phone, to be called to be refused to answer the phone list. The client's caller sends it after receiving the call rejection message.                                                                                         |
| `NERecordCallStatus.TIMEOUT`  | four  | Timeout order, after receiving the call invitation, do not operate the timeout to generate the call list. Sent by the client's caller.                                                                                                                            |
| `NERecordCallStatus.BUSY`     | 5     | Occupying the line (user is busy). When the caller is called, the caller is still on the call and in the call/called. At this time, the caller will reject the caller's call invitation. The client caller will send an account list after receiving the message. |

话单以 IM 消息抄送的形式发送，抄送类型为会话类型，即 `eventType` 为 1。会话类型的消息体中一般包含 `eventType`、`convType`、`to`、`fromAccount`、`msgTimestamp、msgType`、`msgidClient`、`msgidServer`、`attach` 等字段，全量字段请参考 [返回数据的 JSON 字段说明](https://doc.yunxin.163.com/messaging2/server-apis/DI3OTg5Mjg?platform=server#%E6%B6%88%E6%81%AF%E4%BD%93%E4%B8%AD%E7%9A%84-json-%E5%AD%97%E6%AE%B5%E8%AF%B4%E6%98%8E)。其中：

- 话单消息的 `msgType` 字段的值为 `NRTC_NETCALL`，表示音视频话单消息抄送。
- 话单消息中 `attach` 字段中包含通话类型、呼叫状态等通话详情，请参考以下表格：

| Field      | Type       | Example | Explain                                                                                                                                                                                                                                                   |
| ---------- | ---------- | ------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| type       | number     | 1       | Call type. 1: Audio call. 2: Video call.                                                                                                                                                                                                                  |
| channel id | number     | 123     | Room ID.                                                                                                                                                                                                                                                  |
| status     | number     | 1       | Call status. **1:**The call ends normally. 2: The caller cancels the call. 3: Being called and refused to call. 4: The call was not answered, and the call was canceled due to timeout. **5:**I was called the busy line, and the call was not connected. |
| durations  | Json Array | Nothing | Details of the call process, JSON array format, including: **accid**: the accid of the call member. **duration**: corresponds to the call time of the member.                                                                                             |

JSON structure of the received list:

```
{
   "type": 1,                       //1 表示音频，2 表示视频
   "channelId": 123,                //音视频通话 2.0 的 channelId
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

Parse the message after receiving it. The sample code is as follows:

```
/**
 * 解析话单消息数据，一般用于 recyclerView adapter 中渲染
 *
 * @param message 当前 IM 消息
 * @param attachment 话单附件
 */
private void parseForNetCall(V2NIMMessage message, V2NIMMessageCallAttachment attachment) {
    if (message == null || attachment == null) {
        return;
    }

    // 获取通话对端用户 ID
    String targetId = V2NIMConversationIdUtil.conversationTargetId(message.getConversationId());
    // 音频/视频 类型通话
    int type = attachment.getType();
    // 房间 ID
    long channelId = attachment.getChannelId();
    // 话单类型
    int status = attachment.getStatus();
    // 时长列表
    List<V2NIMMessageCallDuration> durations = attachment.getDurations();

    // 按照话单类型解析
    switch (status) {
        case NERecordCallStatus.COMPLETE:
            // 成功接听
            if (durations != null) {
                // 通话时长渲染
                for (V2NIMMessageCallDuration duration : durations) {
                    if (duration != null) {
                        // 参与通话用户
                        String accId = duration.getAccountId();
                        // 通话时长 单位为 秒
                        int seconds = duration.getDuration();
                    }
                }
            }
            break;
        case NERecordCallStatus.CANCELED:
            // 主叫用户取消
            break;
        case NERecordCallStatus.REJECTED:
            // 被叫用户拒接
            break;
        case NERecordCallStatus.TIMEOUT:
            // 被叫接听超时
            break;
        case NERecordCallStatus.BUSY:
            // 被叫用户在通话中，占线
            break;
    }
}

```

### Method 2: Realize the list by yourself

If the component's own speech function cannot meet your business needs, you can refer to the following steps to implement the call list by yourself.

1. Turn off the **server-side**call list copy. For specific steps, please refer to the [function of opening the call bill](#%E7%AC%AC%E4%B8%80%E6%AD%A5%E5%BC%80%E9%80%9A%E8%AF%9D%E5%8D%95%E6%8A%84%E9%80%81).
2. Open the room duration message copy (eventType=8) in the [NetEase Cloud Message console](https://app.yunxin.163.com/index?#/). For detailed steps, please refer to the [opening message CC](https://doc.yunxin.163.com/nertc/server-apis/DExNjg2MDc?platform=server).
3. Determine the call list protocol, usually represented by JSON.

```
{
  "type": 1   // 话单类型
  "data": ... // 话单消息内容，如通话时长等信息
}

```

4. 设置 [`setCallRecordProvider`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1p2p_1_1_n_e_call_engine.html#a22c577f947ccece7f7a5174e97c99e1b)，实现开发者自己的话单逻辑（设置 `setCallRecordProvider` 后客户端本地不再发送组件内部话单），参考 [自定义消息发送](https://doc.yunxin.163.com/messaging2/guide/DYzMjA0Njc?platform=client#%E6%94%B6%E5%8F%91%E8%87%AA%E5%AE%9A%E4%B9%89%E6%B6%88%E6%81%AF)，将步骤 3 中确定的话单协议作为自定义内容进行发送。

You can send the call list by setting `NERecordProvider`.

Here, only callbacks are not received. You need to handle the call independently after the call.

```
NECallEngine.sharedInstance().setCallRecordProvider(new NERecordProvider() {
@Override
public void onRecordSend(NERecord record) {
    // 发送未成功通话话单
}
});

```

**NERecord**call list field description:

| Field      | Type   | Example                                                                                                                                                      |
| ---------- | ------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| accid      | string | The user ID of the other end of the call.                                                                                                                    |
| call type  | Int    | Call types, including:`NECallType.AUDIO`: Audio call`NECallType.VIDEO`: Video call                                                                           |
| call state | Int    | Call list type (`NERecordCallStatus`), please refer to the [call list message structure](#%E8%AF%9D%E5%8D%95%E6%B6%88%E6%81%AF%E7%BB%93%E6%9E%84)for details |

## Turn off the call list function

If you don't need to use the call list function, you can refer to the following steps to turn off the call list function by yourself.

1. Turn off the **server-side**call list copy. For specific steps, please refer to the [function of opening the call bill](#%E7%AC%AC%E4%B8%80%E6%AD%A5%E5%BC%80%E9%80%9A%E8%AF%9D%E5%8D%95%E6%8A%84%E9%80%81).
2. 关闭 **客户端** 话单发送。设置 `setCallRecordProvider`，然后空实现 `onRecordSend` 回调。

```
NECallEngine.sharedInstance().setCallRecordProvider(new NERecordProvider() {
@Override
public void onRecordSend(NERecord record) {
    //空实现即可
}
});

```

..............

# 悬浮窗

NetEase Cloud Voice Video Call Component supports the floating window function.

## Effect display

| Open the floating window button                                                                 | Voice call floating window                                                                      | Video call floating window                                                                      |
| ----------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- |
| ![](https://yx-web-nosdn.netease.im/common/0af830a3a21ca2d00c350c83dc48ac43/开启悬浮窗按钮.png) | ![](https://yx-web-nosdn.netease.im/common/6130a513c071e55ae62dd46d90baca5b/语音通话悬浮窗.png) | ![](https://yx-web-nosdn.netease.im/common/57b79ec6ba29e7562e63894642db2030/视频通话悬浮窗.png) |

## Initialize the configuration

`CommonCallActivity`Methods related to floating windows provided:

| Method                        | Parameters             | Return                        | Explain                                                                                                                                                                   |
| ----------------------------- | ---------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `getUiConfig`                 | -                      | [`P2PUIConfig`](#P2PUIConfig) | 获取 `provideUIConfig` 方法返回的实例。                                                                                                                                   |
| `provideUIConfig`             | `CallParam`            | [`P2PUIConfig`](#P2PUIConfig) | Return the corresponding page configuration according to the call parameters, which is used to configure the front-end service on/off, floating window click switch, etc. |
| `showOverlayPermissionDialog` | `View.OnClickListener` | -                             | Display the pop-up window to apply for floating window permission.                                                                                                        |
| `doShowFloatingWindow`        | -                      | -                             | After having the floating window permission, display the floating window.                                                                                                 |

`P2PUIConfig`Supported floating window configuration fields:

| Configuration item     | Explain                                                                                   |
| ---------------------- | ----------------------------------------------------------------------------------------- |
| `enableFloatingWindow` | Configure whether to turn on the floating window function, and turn off false by default. |

## Enable the floating window function

The cloud audio video component allows users to use the floating window button in the upper left corner of the call interface to reduce the call interface to a floating window when making a call.

如果需要启用该功能，可以使用 `enableFloatingWindow` 方法，在 NERtcCallKit 组件初始化时开启该功能：

```
CallKitUIOptions options = new CallKitUIOptions.Builder()
  .p2pVideoActivity(CustomP2PCallActivity.class).build();
CallKitUI.init(context, options);

public class CustomP2PCallActivity extends CommonCallActivity {

  private static final String TAG = "CustomP2PCallActivity";

  @NonNull
  @Override
  protected P2PUIConfig provideUIConfig(CallParam param) {
    ALog.d(TAG, new ParameterMap("provideUIConfig").append("param", param).toValue());
    return new P2PUIConfig.Builder().enableFloatingWindow(true)
                                    .enableAutoFloatingWindowWhenHome(true)
                                    .build();
  }
 @Override
protected int provideLayoutId() {
  //自定义的layout
  return 0;
}

}

```

...................

# 来电横幅

Since v4.3.0, NetEase Cloud Voice and Video Call Components have supported the call banner function. When receiving a call, a lightweight banner is displayed at the top of the screen, which supports one-click answering or rejection without interrupting the user's current operation.

## Overview

The audio and video call component (CallKit) turns off the call banner function by default (consistent with the original full-screen call interface behavior). If you turn on this function, the top banner will be displayed when you receive a call, and the original full-screen call page will no longer appear.

- This function supports switching at any time during operation and takes effect on the **next call**(not effective for the currently displayed banner).
- If there is a second call during the banner display, the SDK will automatically reply to the busy line.
- During the banner display, the ringtone is played normally; when clicking the main body of the banner to enter the full-screen incoming call page, the ringtone **is not interrupted**.

## Effect display

![image-netease](https://yx-web-nosdn.netease.im/common/c9362efa9b53f8b1a395bd0f0e62c8b5/横幅.png)

## Permission configuration

The banner on the Android side is implemented using the system floating window (`SYSTEM_ALERT_WINDOW`), and you need to apply for permissions on **Android 6.0+**devices.

If the Android terminal does not have floating window permission, the SDK will automatically jump to the system settings page (`ACTION_MANAGE_OVERLAY_PERMISSION`) to guide the user to authorize and return the error code through callback to prompt the business layer.

```
// 检查权限
if (!Settings.canDrawOverlays(context)) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${packageName}")
    )
    startActivity(intent)
}

```

## Enable the call banner function

When the cloud audio video component receives an incoming call, it will display a lightweight banner at the top of the screen, which supports one-click answering or rejection without interrupting the user's current operation.

如果需要启用该功能，可以使用 `enableIncomingBanner` 方法，在 NERtcCallKit 组件初始化后开启该功能：

```
// 开启
CallKitUI.enableIncomingBanner(true)

// 关闭（恢复全屏来电界面）
CallKitUI.enableIncomingBanner(false)

```

`enableIncomingBanner`It is a **runtime method**and cannot be passed through `CallKitUIOptions.Builder`. The attribute of the same name in Builder is an internal parameter and is not used as a public API.

## Frequently asked questions

**Q: After the banner is turned on, will the original full-screen call interface still appear?**

No way. After turning on the banner mode, the full-screen incoming call interface is completely replaced by the banner display. If you need to restore, just call `enableIncomingBanner(false)`

**Q: How to deal with receiving a second call during the banner display?**

SDK will automatically reply to the busy line to the second caller, and the behavior is consistent with the new call received during the call.

**Q: Will the banner still be displayed when Android does not have the floating window permission?**

No way. When there is no floating window permission, the SDK will downgrade the display system notification to prompt incoming calls and guide users to the settings page for authorization. After authorization, the next call can display the banner normally.

**Q: Click the main body of the banner to enter the full-screen call page. Will the ringtone play again?**

No way. The ringtone will be played continuously, and it will not be interrupted or re-triggered when entering the full-screen call page. The ringtone stops at the end of the call (answer/reject/timeout/cancel).

............

# 虚拟背景

NetEase Cloud Voice Video Call Component supports virtual background (background blur) function.

## Effect display

| Default effect                                                                                    | Turn on the virtual background effect                                                               |
| ------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| ![](https://yx-web-nosdn.netease.im/common/76827f6d8381e14f05ebe4ef63bce979/音视频通话原效果.jpg) | ![](https://yx-web-nosdn.netease.im/common/0fb0a4c32d83b967e7903f07a1637917/音视频通话虚拟背景.jpg) |

## Initialize the configuration

`CommonCallActivity`Methods related to the virtual background function provided:

| Method            | Parameters  | Return                        | Explain                                                                                                                                                                                                 |
| ----------------- | ----------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `getUiConfig`     | -           | [`P2PUIConfig`](#P2PUIConfig) | 获取 `provideUIConfig` 方法返回的实例。                                                                                                                                                                 |
| `provideUIConfig` | `CallParam` | [`P2PUIConfig`](#P2PUIConfig) | Return the corresponding page configuration according to the call parameters, which is used to configure the front-end service on/off, floating window click switching, virtual background switch, etc. |

`P2PUIConfig`Supported virtual background configuration fields:

| Configuration item                                                                                                                                                                                                                                                                                                                                                                                                                                            | Explain                                                                                                |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| `enableVirtualBlur`                                                                                                                                                                                                                                                                                                                                                                                                                                           | Whether the configuration supports the virtual background of the call video, the default false is off. |
| Virtual background function description, if you need to turn on the virtual background, in the case of configuring this switch true, you also need to introduce **com.netease.yunxin:nertc-nenn**and **com.netease.yunxin:ner**when integrating.**tc-segment**library, please refer to the [integrated audio and video SDK](https://doc.yunxin.163.com/nertc/guide/DcyNDc0ODI?platform=android#%E7%AC%AC%E4%B8%89%E6%AD%A5%E9%9B%86%E6%88%90-sdk)for details. |

## Enable the virtual background function

The cloud audio video component allows users to use the virtual background function during the call to blur the background of the video call.

如果需要启用该功能，可以使用 `enableVirtualBlur` 方法进行开启：

```
public class CustomP2PCallActivity extends CommonCallActivity {

  private static final String TAG = "CustomP2PCallActivity";

  @NonNull
  @Override
  protected P2PUIConfig provideUIConfig(CallParam param) {
    ALog.d(TAG, new ParameterMap("provideUIConfig").append("param", param).toValue());
    return new P2PUIConfig.Builder().enableVirtualBlur(true)
                                    .build();
  }

  @Override
  protected int provideLayoutId() {
    // 自定义布局
    return 0;
  }
}

```

...................

# 自定义用户昵称和头像

The user nicknames and user avatars displayed on the call or call page are obtained from the user information of IM SDK by default. You can also customize the user nickname and avatar to make them different from the user nicknames and avatars in IM SDK.

This article is only applicable to scenarios with UI integration call components. This function is not supported when UI integration is not included.

## Implementation method

在初始化呼叫组件时，通过 `UserInfoHelper` 参数自定义用户昵称和头像。

The following sample code is a specific implementation of `UserInfoHelper`:

```
new UserInfoHelper() {
  		/**
  			* 用户根据 accId 内容，利用 notify 接口将用户昵称通知组件
  			*/
        @Override
        public boolean fetchNickname(@NonNull String accId, @NonNull Function1<? super String, Unit> notify) {
            return false;
        }
  		/**
  			* 用户根据 accId 内容，利用 notify 接口个将用户的头像链接通知组件，
  			* notify 中的两个字段其中一个为头像的url，另一个为加载头像失败后展示占位的本地资源 id
        */
        @Override
  			public boolean fetchAvatar(@NonNull Context context, @NonNull String accId, @NonNull Function2<? super String, ? super Integer, Unit> notify) {
          return false;
        }
}

```

- 若您希望更改昵称，可以通过 `fetchNickname` 方法。若此方法返回 true，则依赖于 `notify.invoke("新昵称")` 返回的新昵称，若为 false 则使用默认昵称。
- 若您希望更改头像，可以通过 `fetchAvatar` 方法中的 accId 获取新的头像，若此方法返回 true，则依赖于 `notify.invoke("新头型url",头像加载失败本地占位资源)` 返回的新头像，若为 false 则使用默认头像。

.................

# 自定义呼叫铃声

This article introduces how to customize the call ringtone in the call component, including the called ringtone, call waiting ringtone, etc.

## Function introduction

`SoundHelper`The class is mainly used to set the ringtone when the call is waiting and when it is called.

## Implementation method

如果您希望自定义呼叫组件中用户的响铃，请在初始化呼叫组件时，在 `CallKitUIOptions` 对象中生成 `soundHelper` 实例，设置呼叫铃声。

1. 将自定义铃声放在 `raw` 文件夹下。
2. 在 `soundResources` 方法中设置铃声的资源 ID。

`soundResources` 方法的 `ringerTypeEnum` 参数为枚举类型。组件在内部实现了响铃逻辑，共分为五种类型的铃声，通过 `AVChatSoundPlayer.RingerTypeEnum` 进行枚举铃声的种类，具体如下：

| RingerTypeEnum Type | Explain                                    |
| ------------------- | ------------------------------------------ |
| CONNECTING          | Caller call prompt.                        |
| NO_RESPONSE         | It was called timeout and did not respond. |
| PEER_BUSY           | It is called a busy line.                  |
| PEER_REJECT         | I was called to refuse to pick up.         |
| ring                | Be called to ring the bell.                |

3. (Optional) Self-realization of ringtone playback.

适用于 2.0.0 及之后的版本

呼叫组件从 2.0.0 版本起，默认通过 `SoundPool` 类实现铃声播放，存在一些铃声文件的限制。您可以重写 `play` 和 `stop` 方法，实现自定义呼叫铃声。

```
CallKitUIOptions options = new CallKitUIOptions.Builder()
.soundHelper(new SoundHelper(){
    /**
    * 若用户需要修改铃声，其他不需要变化，若不需要播放则直接返回 null
    */
    @Nullable
    @Override
    public Integer soundResources(@NonNull AVChatSoundPlayer.RingerTypeEnum type) {
    return super.soundResources(type);
    }
    /**
    * （可选）用户可在此实现自己的铃声播放逻辑和{@link SoundHelper#stop(Context, AVChatSoundPlayer.RingerTypeEnum)}方法同时修改
    */
    @Override
    public void play(@NonNull Context context, @NonNull AVChatSoundPlayer.RingerTypeEnum type) {
    super.play(context, type);
    }
    /**
    * （可选）用户可在此实现自己的铃声停止逻辑和{@link SoundHelper#play(Context, AVChatSoundPlayer.RingerTypeEnum)}方法同时修改
    */
    @Override
    public void stop(@NonNull Context context, @Nullable AVChatSoundPlayer.RingerTypeEnum type) {
    super.stop(context, type);
    }
})
// 初始化呼叫组件时，传入 options 配置信息
CallKitUI.init(getApplicationContext(), options);

```

适用于 2.0.0 之前的版本

`SoundHelper#isEnable` 为 `true` 表示响铃，`false` 表示禁止响铃，您可以通过此方法完成响铃的开关。

```
CallKitUIOptions options = new CallKitUIOptions.Builder()
.soundHelper(new SoundHelper(){
    @Override
    public boolean isEnable() {
        return true;
    }

    @Nullable
    @Override
    public Integer soundResources(@NonNull AVChatSoundPlayer.RingerTypeEnum type) {
    return super.soundResources(type);
    }
}
// 初始化呼叫组件时，传入 options 配置信息
CallKitUI.init(getApplicationContext(), options);

```

Applicable to 2.0.0 and later versionsApplicable to versions before 2.0.0

呼叫组件从 2.0.0 版本起，默认通过 `SoundPool` 类实现铃声播放，存在一些铃声文件的限制。您可以重写 `play` 和 `stop` 方法，实现自定义呼叫铃声。

```
CallKitUIOptions options = new CallKitUIOptions.Builder()
.soundHelper(new SoundHelper(){
    /**
    * 若用户需要修改铃声，其他不需要变化，若不需要播放则直接返回 null
    */
    @Nullable
    @Override
    public Integer soundResources(@NonNull AVChatSoundPlayer.RingerTypeEnum type) {
    return super.soundResources(type);
    }
    /**
    * （可选）用户可在此实现自己的铃声播放逻辑和{@link SoundHelper#stop(Context, AVChatSoundPlayer.RingerTypeEnum)}方法同时修改
    */
    @Override
    public void play(@NonNull Context context, @NonNull AVChatSoundPlayer.RingerTypeEnum type) {
    super.play(context, type);
    }
    /**
    * （可选）用户可在此实现自己的铃声停止逻辑和{@link SoundHelper#play(Context, AVChatSoundPlayer.RingerTypeEnum)}方法同时修改
    */
    @Override
    public void stop(@NonNull Context context, @Nullable AVChatSoundPlayer.RingerTypeEnum type) {
    super.stop(context, type);
    }
})
// 初始化呼叫组件时，传入 options 配置信息
CallKitUI.init(getApplicationContext(), options);

```

`SoundHelper#isEnable` 为 `true` 表示响铃，`false` 表示禁止响铃，您可以通过此方法完成响铃的开关。

```
CallKitUIOptions options = new CallKitUIOptions.Builder()
.soundHelper(new SoundHelper(){
    @Override
    public boolean isEnable() {
        return true;
    }

    @Nullable
    @Override
    public Integer soundResources(@NonNull AVChatSoundPlayer.RingerTypeEnum type) {
    return super.soundResources(type);
    }
}
// 初始化呼叫组件时，传入 options 配置信息
CallKitUI.init(getApplicationContext(), options);

```

............

# 自定义接听背景图

This article introduces how to customize the background map of answering audio and video calls in the call component.

## Function introduction

Since V4.8.0, Android CallKitUI supports the configuration of the custom background map of the receiving party's incoming ringing/panding audio video call page.

接入方可使用本地 drawable 资源或公开可访问的 `http/https` 图片 URL 作为背景图。

When the background map is not configured, resources are not available or the URL load fails, the component will continue to use the default avatar to blur the background.

## Implementation method

If you want to customize the background picture of answering audio and video calls, please configure the local drawable resource or URL picture as the background picture of the answer.

- Configure the local drawable resources as the background map of the answer.

```
val source = NECallUIIncomingBackgroundSource.Builder()
    .resource(R.drawable.custom_answer_bg)
    .build()

val config = NECallUIDynamicConfig.Builder()
    .incomingCallBackground(source)
    .build()

CallKitUI.setDynamicUIConfig(config)

```

- Configure the URL picture as the background picture of the answer.

```
val source = NECallUIIncomingBackgroundSource.Builder()
    .url("https://example.com/custom_answer_bg.jpg")
    .build()

val config = NECallUIDynamicConfig.Builder()
    .incomingCallBackground(source)
    .build()

CallKitUI.setDynamicUIConfig(config)

```

若您需要恢复默认背景图，也可以调用 `setDynamicUIConfig` 清除自定义接听背景图。

```
CallKitUI.setDynamicUIConfig(null)

```

## API reference

| API                                                                      | Explain                                                           |
| ------------------------------------------------------------------------ | ----------------------------------------------------------------- |
| `NECallUIIncomingBackgroundSource.Builder().resource(resId).build()`     | Use local drawable resources to create background map sources.    |
| `NECallUIIncomingBackgroundSource.Builder().url(url).build()`            | Use the public picture URL to create the background image source. |
| `NECallUIDynamicConfig.Builder().incomingCallBackground(source).build()` | Create a dynamic UI configuration.                                |
| `CallKitUI.setDynamicUIConfig(config)`                                   | 设置动态 UI 配置；传 `null` 表示清除配置。                        |

## Frequently asked questions

Q: When will it take effect?

A: After configuration, the subsequent call ring/pant-answer page will take effect, and it is not guaranteed to refresh the currently displayed call page.

Q: What are the requirements for URL pictures?

A：URL 需要为公开可访问的 `http/https` 图片地址。建议使用稳定、可快速访问的 HTTPS 地址，避免图片加载过慢影响展示效果。

Q: What will happen if the configuration fails or the picture load fails?

A: The component will go back to the default avatar blur background, which will not affect the normal call process such as call, answer, rejection, etc.

Q: Will I continue to use the background after the call is connected?

A: No. This ability only works on the receiving party's ringing/panding page, and will not change the page background in the call.

..................

# 私有化配置

This article introduces how the call component transmits the relevant parameters of the privatized configuration to NERTC SDK.

## Notes

- The call component integrates IM SDK and NERTC SDK functions, so the privatized configuration of the call component needs to configure the privatized configuration of IM SDK and the privatized configuration of NERTC SDK.
- The call component does not include the initialization of IM SDK, so you need to implement the privatized configuration of IM SDK by yourself. For specific steps, please consult the technical support engineer of NetEase Yunxin.
- Only the call component version of V1.4.2 and above supports privatized configuration.
- In the privatization scenario, the call component's own order function will no longer take effect. Please [implement the list logic by yourself](https://doc.yunxin.163.com/docs/zMwMzkwNTE/TkyODAzMDQ?platformId=120576#%E8%87%AA%E8%A1%8C%E5%AE%9E%E7%8E%B0%E8%AF%9D%E5%8D%95%E9%80%BB%E8%BE%91).

## Realization method

在初始化呼叫组件时，配置 `rtcSdkOption` 方法。

```
NERtcOption option = new NERtcOption();
// 用户通过 NERtcServerAddresses 类完成私有化配置
NERtcServerAddresses serverAddresses =  new NERtcServerAddresses();
// 私有化配置相关参数
serverAddresses.channelServer = ......
......
// 完成私有化配置赋值
option.serverAddresses = serverAddresses;

CallKitUIOptions options = new CallKitUIOptions.Builder()
				......
				.rtcSdkOption(option) // 配置 NERTC SDK 初始化参数，包含日志及私有化配置等
				......
				.build();
				// 若重复初始化会销毁之前的初始化实例，重新初始化
CallKitUI.init(getApplicationContext(), options);

```

The parameters are explained as follows:

- `channelServer`: Please configure it as the URL address of the application service in privatization.
- `serverAddresses`: Please refer to [NERtcServerAddresses](https://doc.yunxin.163.com/nertc/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1lava_1_1nertc_1_1sdk_1_1_n_e_rtc_server_addresses.html).

..............

# 设置初始化参数

应用在登录用户 IM 账号成功之后，需要调用 CallkitUI 的 `init` 接口初始化呼叫组件。初始化时，您可以通过初始化参数进行高级功能配置。

## Initialize the configuration item

The configuration items when initializing with CallkitUI are shown in the following table.

table th:first-of-type { width: 20%; } table th:nth-of-type(2) { width: 15%; } table th:nth-of-type(3) { width: 15%; } table th:nth-of-type(4) { width: 50%; }

| Configuration item                                                                                                                                                                                                        | Parameter type                                     | Version support          | Explain                                                                                                                                                                                                                                                                                                                                            |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------- | ------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| RtcAppKey                                                                                                                                                                                                                 | string                                             | Full version support     | Must choose. Audio and video call AppKey of NERTC SDK.                                                                                                                                                                                                                                                                                             |
| CurrentUserRtcUId                                                                                                                                                                                                         | long                                               | 1.5.7 and later versions | 可选。设置通话中对应的 `rtcUid`，若不设置，组件内部会生成。If you customize `rtcUid`, you can't use the message function provided by NetEase Cloud Letter. At this time, you need to implement the call list by yourself. For details, please refer to the [call list](https://doc.yunxin.163.com/nertccallkit/guide/TkyODAzMDQ?platform=android). |
| Enable Order                                                                                                                                                                                                              | Boolean                                            | 1.3.3 and later versions | Control whether to send the order of unsuccessful local calls, the default is true.                                                                                                                                                                                                                                                                |
| If you don't want to use the built-in call list function of the call component, you can implement the call list by yourself after turning off the call list function, and the notification configuration is set to false. |
| rtcSdkOption                                                                                                                                                                                                              | NERtcOption                                        | Full version support     | The initialized configuration of NERTC SDK will be transmitted to NERTC SDK.                                                                                                                                                                                                                                                                       |
| timeOutMillisecond                                                                                                                                                                                                        | long                                               | Full version support     | The timeout time of calling/answering is measured in milliseconds, and the default is 30s.                                                                                                                                                                                                                                                         |
| resumeBG invitation                                                                                                                                                                                                       | Boolean                                            | Full version support     | When the application is in the background, the call page is not called when receiving a call invitation. At this time, when clicking the desktop icon to evoke the application, whether the called page is displayed.                                                                                                                              |
| The default is true, indicating support.                                                                                                                                                                                  |
| NotificationConfigFetcher                                                                                                                                                                                                 | Function1<NEInviteInfo, CallKitNotificationConfig> | Full version support     | 根据呼叫邀请信息 `NEInviteInfo` 确定展示的通知提示，可配置通知图标、通知 channelId、标题、通知内容。                                                                                                                                                                                                                                               |
| UserInfoHelper                                                                                                                                                                                                            | UserInfoHelper                                     | Full version support     | The user can confirm the nickname and avatar displayed by the user when calling according to the callback account ID (accId). By default, the current user's setting information in NIM SDK is taken.                                                                                                                                              |
| incoming CallEx                                                                                                                                                                                                           | Incoming CallEx                                    | Full version support     | 用户可通过此方法注册重写被叫收到邀请后的行为，默认存在 `DefaultIncomingCallEx`，也可重写此类实现。                                                                                                                                                                                                                                                 |
| RtcCallExtension                                                                                                                                                                                                          | Call Extension                                     | Full version support     | 您可以通过该参数实现修改 NERTC SDK 的功能。`rtcCallExtension` 封装了通过呼叫组件使用 NERTC SDK 的功能。                                                                                                                                                                                                                                            |
| soundhelper                                                                                                                                                                                                               | SoundHelper                                        | Full version support     | This method supports users to control whether to display ringtones and configurable ringtones when calling/called.                                                                                                                                                                                                                                 |
| InitRtcMode                                                                                                                                                                                                               | Integer                                            | 2.0.0 and later versions | Control the initialization range of NERTC SDK, and the default is `NECallInitRtcMode.GLOBAL`.`NECallInitRtcMode.GLOBAL`: GIVE INITIALIZATION.`NECallInitRtcMode.IN_NEED`: Re-initialize each call.`NECallInitRtcMode.IN_NEED_DELAY_TO_ACCEPT`: 和 `IN_NEED` 模式类似，区别在于被叫接听时才初始化。                                                 |
| audio2video                                                                                                                                                                                                               | Boolean                                            | Full version support     | Does the other party need to confirm the video call to audio call (the call must be configured the same)                                                                                                                                                                                                                                           |
| The default is false. If the configuration of the two parties is different, the configuration of the caller shall prevail.                                                                                                |
| video2Audio                                                                                                                                                                                                               | Boolean                                            | Full version support     | Does the other party need to confirm the audio call to video call (the two parties on the call must be configured the same)                                                                                                                                                                                                                        |
| The default is false. If the configuration of the two parties is different, the configuration of the caller shall prevail.                                                                                                |
| P2pAudioActivity                                                                                                                                                                                                          | class                                              | Full version support     | The point-to-point audio call page realized by the registered user himself.                                                                                                                                                                                                                                                                        |
| P2pVideoActivity                                                                                                                                                                                                          | class                                              | Full version support     | The point-to-point video call page realized by the registered user himself.                                                                                                                                                                                                                                                                        |
| language                                                                                                                                                                                                                  | NECallUI Language                                  | 2.4.0 and later versions | Multi-language settings, the default is `NECallUILanguage.AUTO`.`NECallUILanguage.ZH_HANS`: Chinese`NECallUILanguage.EN`: English`NECallUILanguage.AUTO`: Application system default language                                                                                                                                                      |

## Discarded configuration items

The following configuration items have been discarded in the new version and are reserved in the document for reference only:

| Configuration item                                                | Parameter type                                                                                                                                                                                                                                                                                                              | Version support       | Explain                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| ----------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| pushConfigProvider                                                | PushConfig Provider                                                                                                                                                                                                                                                                                                         | 1.3.3 version support |
| 2.0.0 has been abandoned                                          | After the user finishes the process, the call invitation is configured through push. The user can set the push content according to the invitation information. For push configuration, please refer to the instant messaging IM [push management](https://doc.yunxin.163.com/messaging/guide/DQyMzk0MDQ?platform=android). |
| RtcInitScope                                                      | Boolean                                                                                                                                                                                                                                                                                                                     | 1.8.2 version support |
| 2.0.0 has been abandoned (replace the parameter to `initRtcMode`) | Control the initialization range of NERTC SDK, and the default is true.`true`: GIVE INITIALIZATION.`false`: Re-initialize each call.                                                                                                                                                                                        |
| logrootpath                                                       | string                                                                                                                                                                                                                                                                                                                      | 1.3.3 version support |
| 2.0.0 has been abandoned                                          | Modify the log path.                                                                                                                                                                                                                                                                                                        |
| enableAutoJoinWhenCalled                                          | Boolean                                                                                                                                                                                                                                                                                                                     | 3.5.0 Abandoned       | Whether to automatically join the signaling channel after receiving the call, the default is false.`true`: Automatically join the signaling channel after receiving the call. It will conflict with multi-terminal login. If you use this mode, you need to make sure that only one terminal is online.`false`: There will be an invalidation of switching call types in the call, and only switching call types in the call are supported. |

## Instructions for use

请调用 `CallKitUIOptions` 对象中的某个方法完成上述配置项的配置。具体的模板如下：

```
CallKitUIOptions options = new CallKitUIOptions.Builder()
        .rtcAppKey("音视频通话 NERTC SDK 的 AppKey")
        ......
        // 配置需要的配置选项，如配置接听/呼叫的超时时间为 30s
        .timeOutMillisecond(30*1000) // 单位为毫秒
        ......
        .build();
// 若重复初始化会销毁之前的初始化实例，重新初始化
CallKitUI.init(getApplicationContext(), options);

```

## Sample code

The following shows the configuration instructions and sample codes of some important parameters.

### **NotificationConfigFetcher**

应用在后台时，可能会收到其他用户来电请求。此时，受系统限制，可能无法直接展示来电请求页面。您可以先展示来电的 `notification`，单击此 `notification` 跳转到目标的来电请求页面。

`notificationConfigFetcher` 用于简易控制来电通知的 `notification` 展示内容。

**Sample code**:

```
new Function1<NEInviteInfo, CallKitNotificationConfig>() {
        @Override
        public CallKitNotificationConfig invoke(NEInviteInfo inviteInfo) {
            // 根据 inviteInfo 构建通知配置，不要返回 null
            return new CallKitNotificationConfig(
                R.mipmap.ic_launcher, // 通知图标
                "channel_id",         // 通知渠道 ID
                "您有新的来电通知",     // 通知标题
                "用户 " + inviteInfo.callerAccId + " 邀请您进行网络电话" // 通知内容
            );
        }
}

```

**Parameter description**:

- `NEInviteInfo`Call invitation information for calling users.

`NEInviteInfo#callerAccId`The field is the account ID (`AccId`) of the inviting user, not the nickname of the inviting user.
如果您希望添加主叫用户的昵称，可以通过 `NEInviteInfo#callerAccId` 自己本地映射获取昵称。另外，也可以通过主叫用户呼叫时传入自己的昵称作为呼叫的扩展字段，通过 `NEInviteInfo#extraInfo` 字段解析被叫用户。呼叫传入的扩展字段为字符串，用户可通过 JSON 格式扩展。详细使用请参考 [Demo](https://github.com/netease-kit/NECallKit/tree/main/Android) 。

- `CallKitNotificationConfig` 控制显示的 `notification` 展示内容。其中 `CallKitNotificationConfig` 支持设置 `notification` 的小图标、标题、内容、通道 ID 以及响铃的 Uri。

The following sample code shows you how to build a notification configuration:

```
new CallKitNotificationConfig(R.mipmap.ic_launcher, "channel_id", "您有新的来电通知", "用户邀请您进行网络电话")

```

........

# 设置 RTC 的音视频属性

This article describes how to call the interfaces of NERTC SDK in the call component, such as video resolution and `ChannelProfile`.

## Function introduction

`rtcCallExtension` 类是呼叫组件内部对 NERTC SDK 的抽象，您只需要继承 `NERtcCallExtension` 方法，并重写相关方法即可完成 NERTC SDK 的相关配置，例如修改视频分辨率和 ChannelProfile。

## Implementation method

初始化呼叫组件时，调用 `CallKitUIOptions` 对象中的 `rtcCallExtension` 方法设置 RTC 的音视频参数。

例如，通过 [`setLocalVideoConfig`](https://doc.yunxin.163.com/nertc/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1lava_1_1nertc_1_1sdk_1_1_n_e_rtc.html#a20a4f919dbc80cfe7a95cf631e886cf2)、[`setChannelProfile`](https://doc.yunxin.163.com/nertc/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1lava_1_1nertc_1_1sdk_1_1_n_e_rtc_ex.html#a9a299bfd254abbe461efbd60d5f9dcb3) 设置视频分辨率和 ChannelProfile。

The sample code is as follows:

```
CallKitUIOptions options = new CallKitUIOptions.Builder()
    .rtcCallExtension(
        new NERtcCallExtension(){

            @Override
            protected void configVideoConfigBeforeJoin(){
                // 设置一个默认的 videoConfig
                NERtcVideoConfig videoConfig = new NERtcVideoConfig();
                // 帧率：15
                videoConfig.frameRate = NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_15;
                // 用户修改分辨率不要使用 profile，应该使用 width/height 修改，两种方式都存在时，profile 方式失效。
                //分辨率：360*640（注意此处需要设置宽大于高不用考虑具体角度旋转)
                videoConfig.width = 640;
                videoConfig.height = 360;
                NERtcEx.getInstance().setLocalVideoConfig(videoConfig);
            }

            @Override
            protected void configChannelProfileBeforeJoin(){
                // 设置 channel profile
                NERtcEx.getInstance().setChannelProfile(NERtcConstants.RTCChannelProfile.STANDARD_VIDEOCALL);
            }
        })
// 若重复初始化会销毁之前的初始化实例，重新初始化
CallKitUI.init(getApplicationContext(), options);

```

............

# 拦截呼入请求

This article describes how to intercept incoming requests in the call component so that developers can intervene in the call process.

## Function introduction

`DefaultIncomingCallEx` 类主要用于在用户使用呼叫组件 UI 层时接收呼叫，展示通知并启动配置的目标来电页面。如果您不想要呼叫组件自带的来电页面，想要拦截呼入请求后，自定义来电页面，您只需继承 `DefaultIncomingCallEx` 并重写相关方法。

## Implementation method

初始化呼叫组件时，调用 `CallKitUIOptions` 对象中的 `incomingCallEx` 方法拦截呼入请求，再通过自定义 UI 修改来电通知页面，自定义 UI 的具体方法请参考 [自定义 UI](https://doc.yunxin.163.com/nertccallkit/guide/zYzNzI5NDI?platform=android)。

The following sample code shows how to intercept inbound requests:

```
CallKitUIOptions options = new CallKitUIOptions.Builder()
    .incomingCallEx(new DefaultIncomingCallEx(){
        // 收到来电触发，返回值表示用户在后台收到来电时，单击应用图标是否展示来电页面,
        // true 表示此呼叫已经被消耗不会启动页面，false 表示此呼叫没有被消耗需要启动页面
        @Override
        public boolean onIncomingCall(@NonNull InvitedInfo invitedInfo){
        // 检查参数合理性
        if (!isValidParam(invitedInfo)){
            return true;
        }
        // 直接启动目标页面
        MainActivity.this.startActivity(toCallIntent(invitedInfo));
        // 生成通知并提醒
        generateNotificationAndNotify(invitedInfo);
        return false;
        }
    })
// 若重复初始化会销毁之前的初始化实例，重新初始化
CallKitUI.init(getApplicationContext(), options);

```

.........

# 实现单聊呼叫（无 UI）

This article introduces how to realize single chat (1 to 1) call-related business logic by integrating the call component (NERTCCallkit) base package (excluding UI). According to business needs, you need to implement the relevant UI interface by yourself. This article introduces the integration and implementation methods of calling components.

It is recommended to use UI integrated call components. Please refer to the [implementation of 1 to 1 call (including UI).](https://doc.yunxin.163.com/nertccallkit/guide/jcyNDE3MDM?platform=android)

## Notes

- The call component (NERTCCallkit) realizes calls based on NetEase Yunxin NIM SDK and NERTC SDK. NIM SDK and NERTC SDK have been integrated into the call component.
- For the callback information in the call component, developers should do a good job of reporting and storing the corresponding callback data, so as to facilitate troubleshooting problems after going online later.

## Basic concept

- **account_id**: account_id is an IM account ID used to log in to IM.[When registering an IM account](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server), the IM server will return the corresponding account ID (account_id) and key (Token), and the application client needs to be responsible for saving the mapping relationship between account_id and IM Token.
- **Token**: The Token involved in the call component includes IM Token, which is used to identify IM accounts when logging in to IM. The application server calls the [registration account API of the](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)IM server and obtains the IM Token.

## Development environment

| Environmental requirements                                                                                                                                                                                        | Explain                                                                                                                                                                                                               |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Android Studio version                                                                                                                                                                                            | Android Studio 5.0 and above.Please refer to the [Android Studio version description](https://developer.android.google.cn/studio/releases/index.html)for changes to the Android Studio version number system.         |
| Android API version                                                                                                                                                                                               | Level is version 21 and above.                                                                                                                                                                                        |
| Android SDK version                                                                                                                                                                                               | Android SDK 31, Android SDK Platform-Tools 31.x.x and above versions.                                                                                                                                                 |
| Gradle and the required dependency library                                                                                                                                                                        | Download the corresponding version of Gradle and the required dependency library on the [Gradle Services](https://services.gradle.org/distributions/)page. Gradle Version: 7.4.1Android Gradle Plug-in Version: 7.1.3 |
| For the version dependency between Android Gradle plug-in, Gradle and SDK Tool, please refer to the [version description of Android Gradle plug-in](https://developer.android.com/studio/releases/gradle-plugin). |
| [Kotlin](https://blog.jetbrains.com/kotlin/category/releases/)                                                                                                                                                    | Version 1.6.21 and above.                                                                                                                                                                                             |
| CPU architecture                                                                                                                                                                                                  | ARM 64, ARMV7.                                                                                                                                                                                                        |
| Ide                                                                                                                                                                                                               | Android Studio.                                                                                                                                                                                                       |
| Other                                                                                                                                                                                                             | Rely on Androidx and do not support the support library.                                                                                                                                                              |
| The real machine of Android system 5.0 and above.Due to the lack of camera and microphone capabilities of the simulator, the project needs to be run on the real machine.                                         |

## Preparation work

Before following this article, please make sure that you have completed the following settings:

- [Create an application](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console)in the [NetEase Cloud Trust Console](https://app.yunxin.163.com/global/home)and obtain the corresponding App Key.
- [Open](https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console)IM instant messaging, audio and video call 2.0, signaling products and call list functions.

## Integrated call components

The call component (NERTCCallkit) realizes calls based on NetEase Yunxin NIM SDK and NERTC SDK, and NERTC SDK has been integrated into the call component. You just need to integrate NERTCCallkit.

1. 在项目根目录下的 **build.gradle** 文件中，配置 `repositories`（使用 maven）。示例代码如下：

```
allprojects {
    repositories {
        //...
        mavenCentral()
        //...
    }
}

```

2. In the **build.gradle**file under the **app**directory, configure the supported SO library architecture. The sample code is as follows:

```
android {
defaultConfig {
    ndk {
        //设置支持的 SO 库架构
        abiFilters "armeabi-v7a", "x86","arm64-v8a","x86_64"
        }
}
}

```

3. According to the needs of the developer project, add the corresponding IM-related dependencies.

**SDK version limit**:

There is a mapping relationship between the call component and NIM SDK and NERTC SDK. When using the call component, the specified version of NIM SDK and NERTC SDK must be used. The latest version of V3.3.0 adapts to NIM SDK V10.6.0 and NERTC SDK V5.6.50. Please refer to the [update log for](https://doc.yunxin.163.com/nertccallkit/concept/DMzOTI3NTA?platform=client)the adaptation relationship of other versions.

```
dependencies {
    compile fileTree(dir: 'libs', include: '*.jar')
    // 添加依赖。

    // 基础功能 (必需)
    implementation "com.netease.nimlib:basesdk:${LATEST_VERSION}"

    // 聊天室需要
    implementation "com.netease.nimlib:chatroom:${LATEST_VERSION}"

    // 通过网易云信来集成小米等厂商推送需要
    implementation "com.netease.nimlib:push:${LATEST_VERSION}"

    // 超大群需要
    implementation "com.netease.nimlib:superteam:${LATEST_VERSION}"

    // 全文检索插件
    implementation "com.netease.nimlib:lucene:${LATEST_VERSION}"
}

```

4. 在主工程 `build.gradle` 文件中添加如下代码，引入呼叫组件。

```
// 若出现 More than one file was found with OS independent path 'lib/arm64-v8a/libc++_shared.so'.
// 可以在主 module 的 build.gradle 文件中 android 闭包内追加如下 packageOptions 配置
android{
    //......
    packagingOptions {
    pickFirst 'lib/arm64-v8a/libc++_shared.so'
    pickFirst 'lib/armeabi-v7a/libc++_shared.so'
    }
}
// 引入呼叫组件安装包，导入以下其中一个
dependencies {

    implementation 'com.netease.yunxin.kit.call:call:3.3.0'     // 基础组件包，不含 UI 集成时请导入这个包

}

```

5. (Optional) Remove the SDK dependent on the call component.

If the specified version of the SDK cannot be used due to business needs or other reasons, you can refer to the following steps to remove the SDK of IM and RTC dependent on the call component.

```
implementation('com.netease.yunxin.kit.call:call:3.3.0') {
        exclude group: 'com.netease.nimlib'// 去除组件依赖的 IM sdk
        exclude group: 'com.netease.yunxin', module: 'nertc-base' // 去除组件依赖的 NERTC sdk
}

```

6. Add permissions.

根据实际应用需求，在 `AndroidManifest.xml` 中添加以下配置，并请将 `com.netease.nim.demo` 替换为自己的包名。

Specific sample code

```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="com.netease.nim.demo">

    <!-- 权限声明 -->
    <!-- 访问网络状态-->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>

    <!-- 外置存储存取权限 -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

    <!-- 多媒体相关 -->
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <!-- **Android11：V8.6.1 及之后的版本不需要。其他**：V4.4.0 及之后的版本不需要。-->
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/>

    <!-- 控制呼吸灯，振动器等，用于新消息提醒 -->
    <uses-permission android:name="android.permission.FLASHLIGHT" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <!-- 8.0+系统需要-->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

    <!-- 下面的 uses-permission 一起加入到您的 AndroidManifest 文件中。-->
    <permission
        android:name="${applicationId}.permission.RECEIVE_MSG"
        android:protectionLevel="signature"/>
    <uses-permission android:name="${applicationId}.permission.RECEIVE_MSG"/>

    <application
        ...>
        <!-- App Key, 可以在这里设置，也可以在 SDKOptions 中提供。
            如果 SDKOptions 中提供了，则取 SDKOptions 中的值。-->
        <meta-data
            android:name="com.netease.nim.appKey"
            android:value="key_of_your_app" />

        <!-- 网易云信后台服务，请使用独立进程。-->
        <service
            android:name="com.netease.nimlib.service.NimService"
            android:process=":core"/>
        <!-- 网易云信后台服务，使用 V10 接口需要添加以下代码。-->
        <service
            android:name="com.netease.nimlib.service.NimServiceV2" />

        <!-- 网易云信后台辅助服务 -->
        <service
            android:name="com.netease.nimlib.job.NIMJobService"
            android:exported="false"
            android:permission="android.permission.BIND_JOB_SERVICE"
            android:process=":core"/>

        <!-- 网易云信监视系统启动和网络变化的广播接收器，保持和 NimService 同一进程 -->
        <receiver android:name="com.netease.nimlib.service.NimReceiver"
            android:process=":core"
            android:exported="false">
            <intent-filter>
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
            </intent-filter>
        </receiver>

        <!-- 网易云信进程间通信 Receiver -->
        <receiver android:name="com.netease.nimlib.service.ResponseReceiver"/>

        <!-- 网易云信进程间通信 service -->
        <service android:name="com.netease.nimlib.service.ResponseService"/>

        <!-- 网易云信进程间通信 provider -->
        <provider
            android:name="com.netease.nimlib.ipc.NIMContentProvider"
            android:authorities="${applicationId}.ipc.provider"
            android:exported="false"
            android:process=":core" />
        <!-- 网易云信内部使用的进程间通信 provider -->
        <!-- SDK 启动时会强制检测该组件的声明是否配置正确，如果检测到该声明不正确，SDK 会主动抛出异常引发崩溃 -->
        <provider
            android:name="com.netease.nimlib.ipc.cp.provider.PreferenceContentProvider"
            android:authorities="${applicationId}.ipc.provider.preference"
            android:exported="false" />

        <!-- 网易云信内部使用的进程间通信 provider -->
        <!-- SDK 启动时会强制检测该组件的声明是否配置正确，如果检测到该声明不正确，SDK 会主动抛出异常引发崩溃 -->
        <provider
            android:name="com.netease.nimlib.ipc.cp.provider.PreferenceContentProvider"
            android:authorities="com.netease.nim.demo.ipc.provider.preference"
            android:exported="false" />
    </application>
</manifest>

```

7. Configure to prevent code confusion.

Code confusion refers to the use of short and meaningless names to rename classes, methods, attributes, etc. to increase the difficulty of reverse engineering and ensure the security of the Android program source code. In order to avoid abnormal call components caused by renaming classes, you need to configure anti-code confusion.

请在 `proguard-rules.pro` 配置文件中加入以下代码防止混淆：

```
# NIM SDK 的类，如果集成 IM 时已经添加，请忽略
-dontwarn com.netease.nim.**
-keep class com.netease.nim.** {*;}

-dontwarn com.netease.nimlib.**
-keep class com.netease.nimlib.** {*;}

-dontwarn com.netease.share.**
-keep class com.netease.share.** {*;}

-dontwarn com.netease.mobsec.**
-keep class com.netease.mobsec.** {*;}

# NERTC SDK 的类
-keep class com.netease.lava.** {*;}
-keep class com.netease.yunxin.** {*;}

# 呼叫组件的类
-dontwarn com.netease.yunxin.kit.**
-keep class com.netease.yunxin.kit.** {*;}
-keep public class * extends com.netease.yunxin.kit.corekit.XKitInitOptions
-keep class * implements com.netease.yunxin.kit.corekit.XKitService {*;}

```

If you have integrated NIM SDK and NERTC SDK, or cannot use the specified version of SDK due to business needs and other reasons, please contact NetEase Yunxin Technical Support to confirm whether the SDK version can be replaced, and refer to step 5 above to remove the version dependent on the call component.

## Initialize the call component

You can initialize it anywhere in the application code.

1. 调用 [`NIMClient#initV2`](https://doc.yunxin.163.com/messaging2/references/android/doxygen/Latest/zh/classcom_1_1netease_1_1nimlib_1_1sdk_1_1_n_i_m_client.html#aa8e0e524f223d29e8f2499557a2ae2e5) 方法进行 IM 的初始化。

SDK 的配置信息请参考 [`SDKOptions`](https://doc.yunxin.163.com/messaging2/references/android/doxygen/Latest/zh/classcom_1_1netease_1_1nimlib_1_1sdk_1_1_s_d_k_options.html)。

The sample code is as follows:

```
// 激活 V10 API 后，可以根据该字段选项选择是否禁用 V10 API 登录，默认 false，即使用 V10 API 登录
// sdkOptions.disableV2Login = true;
...
// 按需设置其它 SDKOptions 设置项

NIMClient.initV2(context, sdkOptions);

```

The above provides a simplified initialization example. For more initialization information, please refer to the [initialization SDK](https://doc.yunxin.163.com/messaging2/guide/TY4OTgyMDk?platform=client). 2. 调用 [`setup`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1p2p_1_1_n_e_call_engine.html#ad22e1f2f78cf76a2b300cf6a1e0a2348) 接口进行呼叫组件的初始化。

App users can normally receive calls from other people or initiate calls actively after initialization on this terminal. If the initialization is not completed, the component may prompt the application to initialize, or the called app may not prompt when it is called. If the App repeatedly calls the initialization interface, it will crash due to the internal dependence of RTC SDK and IM SDK, and the need to be re-initialized needs to complete the destruction of the components first.

呼叫组件初始化相关代码内容可以放在工程的 `MainActivity` 中执行，尽量避免在 `MainActivity#onDestroy()` 方法中做组件的释放。建议在 App 用户登出时释放，登入时进行初始化。

For the description of core function parameters, please refer to the [initialization parameter configuration](https://doc.yunxin.163.com/nertccallkit/guide/DI5Nzg0OTM?platform=android).

`NESetupConfig`The parameter description is shown in the following table.

| Parameters                  | Type           | Is it necessary to choose? | Explain                                                                                                                                                                                                                                                                                                                                                                                       |
| --------------------------- | -------------- | -------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| App Key                     | string         | Yes                        | appKey, get it in the [NetEase Cloud Console](https://app.yunxin.163.com/global/home).                                                                                                                                                                                                                                                                                                        |
| CurrentUserRtcUid           | long           | No                         | Set the corresponding RTC SDK uid long shaping, and the component will be automatically generated when the default is 0L.                                                                                                                                                                                                                                                                     |
| RtcConfig                   | NERtcOption    | No                         | NERTC SDK initializes the configuration.                                                                                                                                                                                                                                                                                                                                                      |
| enableAutoJoinSignalChannel | Boolean        | No                         | Whether to allow the called user to automatically join the IM signaling channel after receiving the call invitation, the default is false.**Note: This function and the multi-terminal login function are mutually exclusive.**                                                                                                                                                               |
| enableJoinRtcWhenCall       | Boolean        | No                         | Whether the caller directly joins the RTC room when calling, the default is false.                                                                                                                                                                                                                                                                                                            |
| InitRtcMode                 | Integer        | No                         | RTC SDK initialization mode, the default is `GLOBAL`.`GLOBAL`(1): RTC is initialized globally once.`IN_NEED`(2): Initialize on demand, the caller initializes RTC when calling, initializes RTC when the call is received, and the RTC is destroyed at the end of the call. `IN_NEED_DELAY_TO_ACCEPT`(3): Initialize the RTC when you are called, and destroy the RTC at the end of the call. |
| RtcCallExtension            | Call Extension | No                         | It is mainly used to set the resolution or modify the behavior of components using RTC.                                                                                                                                                                                                                                                                                                       |

The specific initialization code is as follows, and parameter settings can be realized through `NESetupConfig.Builder`.

```
// NERTC SDK 初始化配置
NERtcOption rtcOption = new NERtcOption();
NESetupConfig config = new NESetupConfig.Builder(appKey)
  .rtcOption(rtcOption)
  .build();
// 在 IM sdk 初始化并完成登录后，调用呼叫组件初始化
NECallEngine.sharedInstance().setup(this,config);

```

## Log in

调用 [`login`](https://doc.yunxin.163.com/messaging2/client-apis/TQ5NTUwNzQ?platform=client#login) 方法登录 IM。

This article takes the implementation of **static Token**login as an example. Please refer to [Login IM for](https://doc.yunxin.163.com/messaging2/guide/Dk1MTY4MzA?platform=client)the implementation methods of dynamic Token login and automatic login.

The sample code is as follows:

```
NIMClient.getService(V2NIMLoginService.class).login("account", "token", null, new V2NIMSuccessCallback<Void>() {
    @Override
    public void onSuccess(Void unused) {
        // TODO
    }
},
    new V2NIMFailureCallback() {
    @Override
    public void onFailure(V2NIMError error) {
        int code = error.getCode();
        String desc = error.getDesc();
        // TODO
    }
});

```

## Realize 1 to 1 call (point-to-point call)

### API timing diagram

```
#mermaid-render-0 {font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:16px;fill:#333;}#mermaid-render-0 .error-icon{fill:#552222;}#mermaid-render-0 .error-text{fill:#552222;stroke:#552222;}#mermaid-render-0 .edge-thickness-normal{stroke-width:2px;}#mermaid-render-0 .edge-thickness-thick{stroke-width:3.5px;}#mermaid-render-0 .edge-pattern-solid{stroke-dasharray:0;}#mermaid-render-0 .edge-pattern-dashed{stroke-dasharray:3;}#mermaid-render-0 .edge-pattern-dotted{stroke-dasharray:2;}#mermaid-render-0 .marker{fill:#333333;stroke:#333333;}#mermaid-render-0 .marker.cross{stroke:#333333;}#mermaid-render-0 svg{font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:16px;}#mermaid-render-0 .actor{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-0 text.actor>tspan{fill:black;stroke:none;}#mermaid-render-0 .actor-line{stroke:grey;}#mermaid-render-0 .messageLine0{stroke-width:1.5;stroke-dasharray:none;stroke:#333;}#mermaid-render-0 .messageLine1{stroke-width:1.5;stroke-dasharray:2,2;stroke:#333;}#mermaid-render-0 #arrowhead path{fill:#333;stroke:#333;}#mermaid-render-0 .sequenceNumber{fill:white;}#mermaid-render-0 #sequencenumber{fill:#333;}#mermaid-render-0 #crosshead path{fill:#333;stroke:#333;}#mermaid-render-0 .messageText{fill:#333;stroke:none;}#mermaid-render-0 .labelBox{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-0 .labelText,#mermaid-render-0 .labelText>tspan{fill:black;stroke:none;}#mermaid-render-0 .loopText,#mermaid-render-0 .loopText>tspan{fill:black;stroke:none;}#mermaid-render-0 .loopLine{stroke-width:2px;stroke-dasharray:2,2;stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);}#mermaid-render-0 .note{stroke:#aaaa33;fill:#fff5ad;}#mermaid-render-0 .noteText,#mermaid-render-0 .noteText>tspan{fill:black;stroke:none;}#mermaid-render-0 .activation0{fill:#f4f4f4;stroke:#666;}#mermaid-render-0 .activation1{fill:#f4f4f4;stroke:#666;}#mermaid-render-0 .activation2{fill:#f4f4f4;stroke:#666;}#mermaid-render-0 .actorPopupMenu{position:absolute;}#mermaid-render-0 .actorPopupMenuPanel{position:absolute;fill:#ECECFF;box-shadow:0px 8px 16px 0px rgba(0,0,0,0.2);filter:drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4));}#mermaid-render-0 .actor-man line{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-0 .actor-man circle,#mermaid-render-0 line{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;stroke-width:2px;}#mermaid-render-0 :root{--mermaid-font-family:"trebuchet ms",verdana,arial,sans-serif;}Application layerNERTC Call KitaddCallDelegate Add monitoring1call initiate a call2onReceiveInvited Received a call invitation3accept answer the callfouronCallConnected call establishment5hangup hang up calls, cancel calls, reject incoming calls6callback of onCallEnd canceled, rejected or hung up during the call7Application layerNERTC Call Kit
```

```
sequenceDiagram
    autonumber
    participant 应用层
    participant NERTCCallkit

    应用层->>NERTCCallkit: addCallDelegate 添加监听
    应用层->>NERTCCallkit: call 发起呼叫
    NERTCCallkit-->>应用层: onReceiveInvited 收到呼叫邀请
    应用层->>NERTCCallkit: accept 接听呼叫
    NERTCCallkit-->>应用层: onCallConnected 通话建立
    应用层->>NERTCCallkit: hangup 挂断通话、取消通话、拒接来电
    NERTCCallkit-->>应用层: onCallEnd 通话被取消、拒绝或通话中挂断的回调

```

### Implementation method

1. Add call monitoring.

在初始化呼叫组件后，调用 [`addCallDelegate`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1p2p_1_1_n_e_call_engine.html#a030ee77edc8b533502ce89f9f6430ac7) 方法，添加回调监听。

```
// 监听呼叫邀请，此处可在全局处设置，避免被意外释放导致来电丢失
NECallEngine.sharedInstance().addCallDelegate(new NECallEngineDelegateAbs() {
  @Override
  public void onReceiveInvited(NEInviteInfo info) {
    // 收到来电后可进行展示 notification，或者将 info 传入被叫页面并调起被叫页面。
  }
});

```

2. The caller initiates the call.

进入自己的呼叫页面，调用 `addCallDelegate` 方法添加通话中监听，页面销毁时调用 `removeCallDelegate` 避免内存泄漏，并调用 [`call`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1p2p_1_1_n_e_call_engine.html#a5a6e58fb772f1621cd12caecb200b22e) 接口发起呼叫。

`NECallParam`The relevant parameters are explained in the following table.

| Parameters        | Type              | Is it necessary to choose? | Example          | Explain                                                                                                                                                                                                      |
| ----------------- | ----------------- | -------------------------- | ---------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| accid             | string            | Yes                        | "12345"          | The IM account of the called user.[Register an IM account](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server), and the IM server will return the corresponding account and Token. |
| call type         | Channel type      | Yes                        | NECallType.VIDEO | Call type. Including:NECallType.AUDIO: AudioNECallType.VIDEO: Video                                                                                                                                          |
| Extra information | string            | No                         | -                | Expand the information and transmit it to what is called onReceiveInvited.                                                                                                                                   |
| Global Extra Copy | string            | No                         | -                | Custom call global copy information, and the user server sets its own business identification when receiving the copy.                                                                                       |
| rtcChannelName    | string            | No                         | -                | Custom channelName, it will be generated by default if it is not transmitted.                                                                                                                                |
| pushconfig        | NECall PushConfig | No                         | -                | Users push custom content and use the internal default push configuration when it is empty.                                                                                                                  |

`NECallPushConfig`The relevant parameters are explained in the following table.

| Parameters   | Type         | Is it necessary to choose? | Explain                                                       |
| ------------ | ------------ | -------------------------- | ------------------------------------------------------------- |
| Need Push    | string       | No                         | Whether it is necessary to push, the default is true.         |
| pushtitle    | Channel type | Yes                        | Push the title.                                               |
| push content | string       | Yes                        | Push content.                                                 |
| push payload | string       | No                         | Push the payload content and add key-value pairs by yourself. |

The sample code is as follows:

```
// 构建视频呼叫参数
NECallParam param = new NECallParam.Builder("calledUserAccId")
  .callType(NECallType.VIDEO)
  .build();
NEResultObserver<CommonResult<NECallInfo>> observer = new NEResultObserver<CommonResult<NECallInfo>>() {
  @Override
  public void onResult(CommonResult<NECallInfo> result) {
    // 呼叫成功
    if (result.isSuccessful()) {
      // 返回通话详情
      NECallInfo callInfo = result.data;
    } else {
      // 呼叫失败，错误码
      int code = result.code;
      // 呼叫失败，错误信息
      String message = result.msg;
    }
  }
};
// 发起呼叫，可在此时做呼叫提示音
NECallEngine.sharedInstance().call(param, observer);

```

3. I was called to receive a call invitation.

当收到呼叫邀请时，被叫会收到 [`onReceiveInvited`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/interfacecom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1p2p_1_1model_1_1_n_e_call_engine_delegate.html#a5a7bb5be3fc4f1a412ca7489f1dedf20) 回调，并在回调中告知主叫的相关信息，包括主叫的账号 ID、呼叫类型、信令通道的 ID、自定义信息。

详细的回调参数 `NEInviteInfo` 如下：

| Parameters        | Type    | Explain                                                                                                                                                                                                    |
| ----------------- | ------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| CallerAccid       | string  | The IM account ID of the caller.[Register an IM account](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server), and the IM server will return the corresponding account and Token. |
| call type         | string  | Call type. Including:NECallType.AUDIO: AudioNECallType.VIDEO: Video                                                                                                                                        |
| Extra information | Integer | Expand the information and transmit it to what is called onReceiveInvited.                                                                                                                                 |
| channel id        | string  | IM signaling channel ID.                                                                                                                                                                                   |

When the called user receives the invitation message, he can directly start the Activity of the called page by calling different methods according to the call type (VIDEO/AUDIO).

若被叫方系统在 **Android Q** 及以上时，系统限制不允许后台弹出页面，此时会弹出对应的 Notification，被叫方可通过单击 Notification 跳转至对应的被叫页面。若用户通过 launcher 或其他方式唤起 app 时，通话仍有效则同样会展示被叫页面，进入被叫页面后同样可以调用 `addCallDelegate` 方法用于监听通话中相关回调，页面销毁时调用 `removeCallDelegate` 避免内存泄漏。 4. I was called to answer.

被叫调用 [NECallEngine#accept](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1p2p_1_1_n_e_call_engine.html#ae8d4edb9ada875cdcbaea706df1e5250) 接口，接听当前的呼叫邀请，此时会触发 `onUserEnter` 回调，对方加入 RTC 成功，认为此时通话建立成功。

When the called user clicks the answer button on the call page, if the call is still in call at this time, he can connect the call and join the corresponding audio and video room to make an audio and video call with the caller.

```
NEResultObserver<CommonResult<NECallInfo>> observer = new NEResultObserver<CommonResult<NECallInfo>>() {
  @Override
  public void onResult(CommonResult<NECallInfo> result) {
    // 接听成功
    if (result.isSuccessful()) {
      // 返回通话详情
      NECallInfo callInfo = result.data;
    } else {
      // 接听失败，可做被叫页面销毁等动作，错误码，错误信息等
      int code = result.code;
      String message = result.msg;
    }
  }
};
// 执行通话接听动作
NECallEngine.sharedInstance().accept(observer);

```

5. The call was hung up.

主叫取消/被叫拒绝/通话中挂断均可调用 [`hangup`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1p2p_1_1_n_e_call_engine.html#a8c371fc832b4f9d55587086b4ca8f7e1) 接口实现通话终止，此时另外一端用户会收到 `onCallEnd` 回调，收到详细的终止信息。

`NECallEndInfo`The detailed parameters are shown in the following table.

| Parameters   | Type    | Explain                                                                                                                                                                     |
| ------------ | ------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Reason code  | Integer | For the error code of the call termination, please refer to the [call termination error code](#%E5%91%BC%E5%8F%AB%E7%BB%93%E6%9D%9F%E9%94%99%E8%AF%AF%E7%A0%81)for details. |
| message      | string  | The reason for the termination of the call.                                                                                                                                 |
| extra string | string  | Extended information that comes in when hanging up.                                                                                                                         |

Hang-up parameters `NEHangupParam`, the detailed parameters are as follows.

| Parameters                                                                                                                                                                                                                                                                                       | Type   | Is it optional? | Explain                                                                                                                                                 |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------ | --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| channel id                                                                                                                                                                                                                                                                                       | string | No              | The caller obtains the value of channelId by calling the callback result, and the caller obtains the value of channelId through the onInvited callback. |
| If you fill in null, the current call will be hung up directly. If you fill in a specific value, the internal verification will check whether the current call and the incomed channelId are the same call. If it is different, the hang-up will fail, otherwise the hang-up will be successful. |
| extra string                                                                                                                                                                                                                                                                                     | string | No              | 挂断时传入的扩展信息，另外一端用户在 `onCallEnd` 回调的 `NECallEndInfo#extraString` 中可获取到                                                          |

```
// 构建挂断参数
NEHangupParam param = new NEHangupParam("channelId"，"extraString");
NEResultObserver<CommonResult<Void>> observer = new NEResultObserver<CommonResult<Void>>() {
  @Override
  public void onResult(CommonResult<Void> result) {
    if (result.isSuccessful()) {
      // 挂断成功
    } else {
      // 挂断失败
    }
  }
};
// 执行挂断动作
NECallEngine.sharedInstance().hangup(param, observer);

```

6. Busy line.

当被叫用户不在 `STATE_IDLE` 状态下接收到其他主叫用户的呼叫邀请时，被叫方内部会自动执行 `NECallEngine.sharedInstance().hangup` 动作，主叫方接收到对方的 `reject` 信令消息后会回调 `NECallEngineDelegate.onCallEnd`，其中错误码为 `NEHangupReasonCode.BUSY`，用于 UI 展示，主叫方本地发送忙线话单消息。

## Advanced functions

### Multi-terminal login

NetEase Yunxin IM SDK supports multi-terminal or single-end login. If you are making audio and video calls through the call component at this time, other terminals log in to the same account:

1. IM turns off multi-terminal login: At this time, due to the shutdown of multi-terminal login, the signaling channel is kicked out at the same time, and the message notification cannot be completed through signaling. At this time, the operation of leaving the audio and video room will be done directly. After the user perceives the action of leaving this terminal, it will do the operation of hanging up.
2. IM 开启多端登录：其他端的用户登录不会影响当前通过组件发起的音视频通话。但若多端同时在线时，收到呼叫邀请时会同时展示被邀请页面，如果其中一端接听或拒绝，则其他端会收到相应错误回调。错误码为 `2001` 或 `2002`。

### Call/called timeout

主叫方发起呼叫被叫方时，若主叫方不取消，被叫方既不接听也不挂断，此时会触发超时限制。触发超时限制后主叫方和被叫方都会触发 `onCallEnd`，其中 `NECallEndInfo` 中错误码为 `NEHangupReasonCode#TIME_OUT` 回调，同时主叫方会做取消动作，被叫方会做挂断操作。用户可通过如下接口实现更改超时时间。发生呼叫或收到呼叫邀请前对本次通话生效，否则对下次通话生效。

```
NECallEngine.sharedInstance().setTimeout(long time);// 单位为毫秒

```

### Video call settings local preview and subscription remote screen

After the user initiates the call, he can call the following interface to set the local preview screen:

```
NERtcVideoView videoView;// 用于展示本端画面的布局 UI
NECallEngine.sharedInstance().setupLocalView(videoView);

```

调用如下接口设置远端画面，此方法可在 `onCallConnected` 回调中调用。

```
NERtcVideoView videoView;// 用于展示远端画面的布局 UI
NECallEngine.sharedInstance().setupRemoteView(videoView);

```

### Set the call resolution and related parameters

通过在初始化设置中 [`rtcCallExtension`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1p2p_1_1model_1_1_n_e_setup_config.html#ad4c390e526af5817396c5b00e253eb4c) 的参数。

```
NESetupConfig config = new NESetupConfig.Builder(appKey)
  .rtcCallExtension(new NERtcCallExtension(){
    @Override
    protected void configAudioProfileBeforeJoin() {
      // 音频设置 standard + speech
      NERtcEx.getInstance().setAudioProfile(NERtcConstants.AudioProfile.STANDARD,
                                            NERtcConstants.AudioScenario.SPEECH);
    }
    @Override
    protected void configVideoConfigBeforeJoin(){
      // 设置一个默认的 videoConfig
      NERtcVideoConfig videoConfig = new NERtcVideoConfig();
      // 帧率：15
      videoConfig.frameRate = NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_15;
      // 用户修改分辨率不要使用 profile，应该使用 width/height 修改，两种方式都存在时，profile 方式失效。
      //分辨率：360*640（注意此处需要设置宽大于高不用考虑具体角度旋转)
      videoConfig.width = 640;
      videoConfig.height = 360;
      NERtcEx.getInstance().setLocalVideoConfig(videoConfig);
    }
    @Override
    protected void configChannelProfileBeforeJoin(){
      // 设置 channel profile
      NERtcEx.getInstance().setChannelProfile(NERtcConstants.RTCChannelProfile.COMMUNICATION);
    }
  })
  .build();
// 在 IM sdk 初始化并完成登录后，调用呼叫组件初始化
NECallEngine.sharedInstance().setup(this,config);

```

## Call end error code

`NEHangupReasonCode`List the call termination error code.

| Error message           | Error code | Explain                                                |
| ----------------------- | ---------- | ------------------------------------------------------ |
| `NORMAL`                | zero       | The call is disconnected                               |
| `TOKEN_ERROR`           | 1          | RTC Token error.                                       |
| `TIME_OUT`              | 2          | The call timed out.                                    |
| `BUSY`                  | 3          | The called user occupies the line.                     |
| `RTC_INIT_ERROR`        | four       | RTC initialization failed.                             |
| `JOIN_RTC_ERROR`        | 5          | Failed to join the RTC channel.                        |
| `CANCEL_ERROR_PARAM`    | 6          | Cancel parameter error.                                |
| `CALL_FAILED`           | 7          | The call failed.                                       |
| `KICKED`                | 8          | The user was kicked out.                               |
| `UID_EMPTY`             | 9          | The RTC uid corresponding to accId is empty.           |
| `SELF_RTC_DISCONNECTED` | 10         | The called user is the RTC user of this terminal.      |
| `CALLER_CANCEL`         | 11         | Actively cancel the call.                              |
| `CALLEE_CANCELED`       | 12         | The call has been cancelled.                           |
| `CALLEE_REJECT`         | 13         | Actively refuse the call.                              |
| `CALLER_REJECTED`       | 14         | The call was rejected.                                 |
| `HANG_UP`               | 15         | Take the initiative to hang up the call.               |
| `BE_HUNG_UP`            | 16         | The call was hung up.                                  |
| `OTHER_REJECTED`        | 17         | Multi-terminal login is rejected by other terminals.   |
| `OTHER_ACCEPTED`        | 18         | Multi-terminal login is answered by other terminals.   |
| `USER_RTC_DISCONNECTED` | 19         | The long link of this user's RTC room is disconnected. |
| `USER_RTC_LEAVE`        | 20         | The end user leaves the RTC room.                      |
| `ACCEPT_FAIL`           | 21         | Failed to answer.                                      |

............

# 实现群组通话（无 UI）

This article introduces the detailed steps and code examples of how to develop the group call function through the API provided by the NetEase Cloud Call Component (CallKit).

The group call function is currently in the Beta testing stage. If you need to use it, please contact your NetEase Yunxin business manager to open it.

## Applicable scene

The group call function is one of the core functions of modern communication applications. It allows multiple users to communicate in real time with video and audio at the same time. Whether it is enterprise meetings, online education, social interaction or telemedicine consultation, this function can provide efficient means of communication and enhance teamwork and information sharing.

- **Online education**: Teachers and students can interact in real time through multiple video calls, share screens and documents, and improve the online learning experience.
- **Enterprise meeting**: Team members can conduct effective remote collaboration and decision-making discussion through video conferencing no matter where they are.
- **Social interaction**: Friends and family can keep in touch through group video calls and share life moments.
- **Telemedicine consultation**: Doctors and patients can conduct remote video consultation for preliminary diagnosis and health advice.
- **Emergency service**: Emergency service personnel can make real-time video calls with on-site personnel to respond quickly to emergencies.

## Prerequisites

Before following this article, please make sure that you have completed the following settings:

- Create at least one application on the [NetEase Cloud Trust console](https://app.yunxin.163.com/global/home). For detailed steps, please refer to [Create an app and get AppKey](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console).
- 集成呼叫组件到示例项目。详细步骤请参考 [实现 1 对 1 呼叫（不含 UI 集成 V3）](https://doc.yunxin.163.com/nertccallkit/guide/zEzMTU0NTI?platform=android)。

## Call timing

以下流程图描述了一个群组通话的基本流程，包括初始化、通话过程、邀请过程和通话结束。

```
#mermaid-render-0 {font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:16px;fill:#333;}#mermaid-render-0 .error-icon{fill:#552222;}#mermaid-render-0 .error-text{fill:#552222;stroke:#552222;}#mermaid-render-0 .edge-thickness-normal{stroke-width:2px;}#mermaid-render-0 .edge-thickness-thick{stroke-width:3.5px;}#mermaid-render-0 .edge-pattern-solid{stroke-dasharray:0;}#mermaid-render-0 .edge-pattern-dashed{stroke-dasharray:3;}#mermaid-render-0 .edge-pattern-dotted{stroke-dasharray:2;}#mermaid-render-0 .marker{fill:#333333;stroke:#333333;}#mermaid-render-0 .marker.cross{stroke:#333333;}#mermaid-render-0 svg{font-family:"trebuchet ms",verdana,arial,sans-serif;font-size:16px;}#mermaid-render-0 .actor{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-0 text.actor>tspan{fill:black;stroke:none;}#mermaid-render-0 .actor-line{stroke:grey;}#mermaid-render-0 .messageLine0{stroke-width:1.5;stroke-dasharray:none;stroke:#333;}#mermaid-render-0 .messageLine1{stroke-width:1.5;stroke-dasharray:2,2;stroke:#333;}#mermaid-render-0 #arrowhead path{fill:#333;stroke:#333;}#mermaid-render-0 .sequenceNumber{fill:white;}#mermaid-render-0 #sequencenumber{fill:#333;}#mermaid-render-0 #crosshead path{fill:#333;stroke:#333;}#mermaid-render-0 .messageText{fill:#333;stroke:none;}#mermaid-render-0 .labelBox{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-0 .labelText,#mermaid-render-0 .labelText>tspan{fill:black;stroke:none;}#mermaid-render-0 .loopText,#mermaid-render-0 .loopText>tspan{fill:black;stroke:none;}#mermaid-render-0 .loopLine{stroke-width:2px;stroke-dasharray:2,2;stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);}#mermaid-render-0 .note{stroke:#aaaa33;fill:#fff5ad;}#mermaid-render-0 .noteText,#mermaid-render-0 .noteText>tspan{fill:black;stroke:none;}#mermaid-render-0 .activation0{fill:#f4f4f4;stroke:#666;}#mermaid-render-0 .activation1{fill:#f4f4f4;stroke:#666;}#mermaid-render-0 .activation2{fill:#f4f4f4;stroke:#666;}#mermaid-render-0 .actorPopupMenu{position:absolute;}#mermaid-render-0 .actorPopupMenuPanel{position:absolute;fill:#ECECFF;box-shadow:0px 8px 16px 0px rgba(0,0,0,0.2);filter:drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4));}#mermaid-render-0 .actor-man line{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;}#mermaid-render-0 .actor-man circle,#mermaid-render-0 line{stroke:hsl(259.6261682243, 59.7765363128%, 87.9019607843%);fill:#ECECFF;stroke-width:2px;}#mermaid-render-0 :root{--mermaid-font-family:"trebuchet ms",verdana,arial,sans-serif;}用户 A（发起者）网易云信呼叫组件用户 BCD...（接受方）应用启动时自动初始化par[通话过程]par[邀请过程]par[通话结束]初始化1Initialize2开始群呼 groupCall3发起通话four加入通话 groupJoin5邀请他人 groupInvite6发送通话邀请7接受邀请 groupAccept8通话结束 groupHangup9通知通话结束10确认通话结束11用户 A（发起者）网易云信呼叫组件用户 BCD...（接受方）
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

## Initialize

用户登录成功之后，您需要调用 [`NEGroupCall.instance().init(GroupConfigParam)`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a51657e9a1752b501cba27df1060402f4) 接口初始化呼叫组件的群呼功能。

After the app user completes the initialization on this end, they can normally receive calls from other people or initiate calls actively. If the initialization is not completed, the calling component may prompt the application to initialize, or the called app may not prompt when it is called. If the App repeatedly calls the initialization interface, it will crash under the restrictions of internally dependent RTC SDK and IM SDK. If you need to re-initialize, you need to complete the destruction of the call component first.

`GroupConfigParam`The parameter description is shown in the following table:

| Parameters        | Type           | Is it necessary to choose? | Explain                                                                                                                                                                                                                                                                                                  |
| ----------------- | -------------- | -------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| App Key           | string         | Yes                        | Application key. The application you create in the [NetEase Cloud Trust console](https://app.yunxin.163.com/global/home)will have the corresponding application key.                                                                                                                                     |
| currentUserAccId  | string         | Yes                        | Set the account ID (`accId`) of the current user to log in to IM SDK.                                                                                                                                                                                                                                    |
| CurrentUserRtcUid | long           | No                         | Set the corresponding audio and video RTC SDK user ID (`uid`). When the default is 0L, the call component will automatically generate the corresponding value.                                                                                                                                           |
| InitRtcMode       | Int            | No                         | For the joining mode of the audio and video room, please refer to NEGroupConstants.[RtcSafeMode](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/interfacecom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_constants_1_1_rtc_safe_mode.html)for details. |
| timeout           | Int            | No                         | The timeout of the call.                                                                                                                                                                                                                                                                                 |
| RtcCallExtension  | Call Extension | No                         | It is mainly used to set the resolution or modify the behavior of calling components using RTC SDK.                                                                                                                                                                                                      |

以下示例代码描述了在 Android 应用中如何初始化群组通话功能，包括设置配置参数：

```
GroupConfigParam groupConfigParam = new GroupConfigParam.Builder().build();
NEGroupCall.instance().init(groupConfigParam);

```

## Start the group call

使用 [`NEGroupCall.instance().groupCall(GroupCallParam)`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a10d935fb0921299d2484b2fcd2f4f6b6) 接口进行群呼呼叫。

`GroupConfigParam`The parameter description is shown in the following table:

| Parameters        | Type   | Is it necessary to choose? | Explain                                                                                                                                                                                                                                                                                                     |
| ----------------- | ------ | -------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| call id           | string | Yes                        | The unique ID of the group call.                                                                                                                                                                                                                                                                            |
| Callee List       | list   | Yes                        | List of members called.                                                                                                                                                                                                                                                                                     |
| Group ID          | string | No                         | Group ID.                                                                                                                                                                                                                                                                                                   |
| Group type        | Int    | No                         | 群组类型，请参考 {@link com.netease.yunxin.kit.call.group.NEGroupConstants.[GroupType](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/interfacecom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_constants_1_1_group_type.html)}。                          |
| Invite mode       | Int    | No                         | For the invitation mode, please refer to {@link com.netease.yunxin.kit.call.group.NEGroupConstants.[InviteMode](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/interfacecom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_constants_1_1_invite_mode.html)}. |
| Join mode         | string | No                         | For joining mode, please refer to {@link com.netease.yunxin.kit.call.group.NEGroupConstants.[JoinMode](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/interfacecom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_constants_1_1_join_mode.html)}.            |
| Extra information | string | No                         | Group calls extension parameters.                                                                                                                                                                                                                                                                           |

以下示例代码提供了调用 [`groupCall`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a10d935fb0921299d2484b2fcd2f4f6b6) 开始一个群组通话的代码示例，包括设置通话参数和处理通话结果：

```
GroupCallParam callParam = new GroupCallParam.Builder().build();
NEGroupCall.instance().groupCall(callParam, new NEResultObserver());

```

## Group call invitation

使用 [`NEGroupCall.instance().groupInvite(GroupInviteParam)`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a448dad60ab85ac72650e40fd63ff8f81) 接口进行群呼邀请。

`GroupInviteParam`The parameter description is as follows:

| Parameters  | Type   | Is it necessary to choose? | Explain                          |
| ----------- | ------ | -------------------------- | -------------------------------- |
| call id     | string | Yes                        | The unique ID of the group call. |
| member list | list   | Yes                        | List of members called.          |

以下示例代码展示了如何调用 [`groupInvite`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a448dad60ab85ac72650e40fd63ff8f81) 邀请其他用户加入通话：

```
GroupInviteParam inviteParam = new GroupInviteParam(callId, memberList);
NEGroupCall.instance().groupInvite(inviteParam, new NEResultObserver());

```

## Answer the group call invitation

使用 [`NEGroupCall.instance().groupAccept(GroupAcceptParam)`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a179c14c536ae0ff12becb5a9ed82bc81) 接口进行主动加入群呼。

`GroupAcceptParam`The parameter description is as follows:

| Parameters | Type   | Is it necessary to choose? | Explain                          |
| ---------- | ------ | -------------------------- | -------------------------------- |
| call id    | string | Yes                        | The unique ID of the group call. |

以下示例代码展示了如何调用 [`groupAccept`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a179c14c536ae0ff12becb5a9ed82bc81) 接听群呼邀请：

```
GroupAcceptParam acceptParam = new GroupAcceptParam(callId);
NEGroupCall.instance().groupAccept(acceptParam, new NEResultObserver());

```

## Join the group call

使用 [`NEGroupCall.instance().groupJoin(GroupJoinParam)`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#ac0adf0e3554c624227e82f0d232ac325) 接口进行主动加入群呼。

`GroupJoinParam`The parameter description is as follows:

| Parameters | Type   | Is it necessary to choose? | Explain                          |
| ---------- | ------ | -------------------------- | -------------------------------- |
| call id    | string | Yes                        | The unique ID of the group call. |

以下示例代码展示了如何调用 [`groupJoin`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#ac0adf0e3554c624227e82f0d232ac325) 加入群组通话：

```
GroupJoinParam joinParam = new GroupJoinParam(callId);
NEGroupCall.instance().groupJoin(joinParam, new NEResultObserver());

```

## Hang up the group call

使用 [`NEGroupCall.instance().groupHangup(GroupHangupParam)`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a040a9326a00400b134e8a415f7ffc3f9) 接口进行群呼挂断。

`GroupHangupParam`The parameter description is as follows:

| Parameters | Type   | Is it necessary to choose? | Explain                          |
| ---------- | ------ | -------------------------- | -------------------------------- |
| call id    | string | Yes                        | The unique ID of the group call. |

以下示例代码展示了如何调用 [`groupHangup`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a040a9326a00400b134e8a415f7ffc3f9) 挂断群呼：

```
GroupHangupParam hangupParam = new GroupHangupParam(callId);
NEGroupCall.instance().groupHangup(hangupParam, new NEResultObserver());

```

## Check the details of the group call

使用 [`NEGroupCall.instance().groupQueryCallInfo(GroupQueryCallInfoParam)`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a48e54e565efe4f8e97520e193a76c24b) 接口进行群呼挂断。

`GroupQueryCallInfoParam`The parameter description is as follows:

| Parameters | Type   | Is it necessary to choose? | Explain                          |
| ---------- | ------ | -------------------------- | -------------------------------- |
| call id    | string | Yes                        | The unique ID of the group call. |

查询的结果 `GroupQueryCallInfoResult.NEGroupCallInfo` 参数说明如下所示：

| Parameters        | Type   | Explain                                                           |
| ----------------- | ------ | ----------------------------------------------------------------- |
| call id           | string | The unique ID of the group call.                                  |
| CallerAccid       | string | The account ID of the user who initiated the multi-person call.   |
| member list       | list   | A list of all participants in the multi-person call.              |
| Group ID          | string | Group ID.                                                         |
| Group type        | Int    | 群类型，请参考 {@link NEGroupConstants.GroupType}。               |
| inviteMode        | Int    | 群组通话的邀请模式，请参考 {@link NEGroupConstants.InviteMode}。  |
| Join mode         | Int    | 群组通话的加入模式，请参考 {@link NEGroupConstants.JoinMode}。    |
| startTimestamp    | long   | The start time stamp of the group call.                           |
| timeout           | Int    | The timeout of the call.                                          |
| rtcChannelName    | string | The name of the audio and video room added during the group call. |
| Extra information | string | Additional information for group calls.                           |

以下示例代码展示了如何调用 [`groupQueryCallInfo`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a48e54e565efe4f8e97520e193a76c24b) 查询群呼详情：

```
GroupQueryCallInfoParam param = new GroupQueryCallInfoParam(callId);
NEGroupCall.instance().groupQueryCallInfo(param, new NEResultObserver<GroupQueryCallInfoResult>());

```

## Query the list of group call members

使用 [`NEGroupCall.instance().groupQueryMembers(GroupQueryMembersParam)`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a302d7e46fcb3f496e675729fcf12d89e) 接口进行群呼挂断。

`GroupQueryMembersParam`The parameter description is as follows:

| Parameters | Type   | Is it necessary to choose? | Explain                          |
| ---------- | ------ | -------------------------- | -------------------------------- |
| call id    | string | Yes                        | The unique ID of the group call. |

查询的结果 `GroupQueryMembersResult.GroupCallMember` 参数说明如下所示：

| Parameters | Type   | Explain                                                                                        |
| ---------- | ------ | ---------------------------------------------------------------------------------------------- |
| accid      | string | The account ID of the call user.                                                               |
| uid        | long   | The user ID of the call to join the audio and video.                                           |
| state      | Int    | For the status of multi-person call users, please refer to {@link NEGroupConstants.UserState}. |
| action     | string | Actions performed by the user.                                                                 |
| Reason     | string | The reason why the user performs the action.                                                   |

以下示例代码展示了如何调用 [`groupQueryMembers`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a302d7e46fcb3f496e675729fcf12d89e) 查询群呼成员列表：

```
GroupQueryMembersParam param = new GroupQueryMembersParam(callId);
NEGroupCall.instance().groupQueryMembers(param, new NEResultObserver<GroupQueryMembersResult>());

```

## Configure group call invitations to receive and listen

使用 [`NEGroupCall.instance().configGroupIncomingReceiver(NEGroupIncomingCallReceiver receiver, boolean register)`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a014c56a188bff03488a133a6bdcfbebc) 接口进行监听邀请通知。

以下示例代码展示了如何调用接口监听邀请：

```
NEGroupCall.instance().configGroupIncomingReceiver(new NEGroupIncomingCallReceiver() {
      @Override
      public void onReceiveGroupInvitation(NEGroupCallInfo info) {
        // 收到群组通话邀请时会触发此回调
      }
    }, true);

```

## 配置群组通话通话行为观察

使用 [`NEGroupCall.instance().configGroupActionObserver(NEGroupCallActionObserver receiver, boolean register)`](https://doc.yunxin.163.com/nertccallkit/references/android/doxygen/Latest/zh/html/classcom_1_1netease_1_1yunxin_1_1kit_1_1call_1_1group_1_1_n_e_group_call.html#a89777378c18b1ea149fe5207c71dd4c6) 接口进行监听通话行为通知。

The following sample code shows how to call the interface to listen to the call behavior:

```
NEGroupCall.instance().configGroupActionObserver(new NEGroupCallActionObserver() {
      @Override
      public void onMemberChanged(String callId, List<GroupCallMember> userList) {
        // 多人通话成员变化回调
      }

      @Override
      public void onGroupCallHangup(GroupCallHangupEvent hangupEvent) {
        // 群通话挂断回调
      }
    }, true);

```

..............

# 实现单呼转群聊

Since version V4.7.0, the call component supports the single-call to group call function. This article mainly introduces how to continue to invite other users to join the current call in the connected 1v1 call by integrating call components (no UI), and achieve a smooth upgrade of a single call to a multi-person call. Without UI access, the call interface is realized by the business side according to its own product form.

该功能并不是原有的群呼能力。单呼转群呼基于当前 1v1 信令房间和 RTC 房间，通过 `NECallEngine.inviteMembers` 邀请新成员加入，不需要重新发起群呼。

## Notes

- The call component realizes call calls based on NetEase Yunxin NIM SDK and NERTC SDK.
- For the callback information in the call component, developers should do a good job of reporting and storing the corresponding callback data, so as to facilitate troubleshooting problems after going online later.
- 参与单呼转群呼的端都需要使用支持该能力的新版本 SDK，并开启 `enableSingleToGroupCall`。任一端未开启或版本不支持时，`canInviteMembers` 返回 `false`。
- The single-call-to-group-call function is only allowed to be initiated after the 1v1 call is connected. It is not supported to invite other users before the initial call is answered.
- The maximum number of people on multi-person calls is 10. SDK will check according to the number of members who have been added, members to be answered and the number of accounts invited this time.
- 邀请发送成功只表示邀请信令已发出，不表示对方已接听或已加入通话。被邀请方真正成为通话成员以 `onCallMembersChanged` 中成员状态变为 `NECallMemberState.JOINED` 为准。
- Once the call enters the multiplayer mode, the multiplayer mode will be maintained in this call; even if there are only 2 people left in the follow-up, the 1v1 large screen and audio and video switching ability will not be restored.
- After entering the multiplayer mode, the switching of audio and video types during the call is not supported, and the business side should hide or disable the switching entrance.
- The single call to group call list is generated by the Yunxin server, and you need to contact the Yunxin technical support to open it. After turning on `enableSingleToGroupCall`, the local SDK default 1v1 call order sending will be skipped; at present, it is not supported to realize single call to group call list by yourself through `setCallRecordProvider`. If you need to customize the call list, please contact Yunxin Technical Support.

## Basic concept

- `account_id`：`account_id` 是 IM 账号 ID，用于登录 IM。[注册 IM 账号时](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)，IM 服务器会返回对应的账号 ID（account_id）和密钥（Token），应用客户端需要负责保存 account_id 和 IM Token 的映射关系。
- `Token`: The Token involved in the call component includes IM Token, which is used for IM account authentication when logging in to IM. The application server calls the [registration account API of the](https://doc.yunxin.163.com/messaging2/server-apis/TQyNjgyMzc?platform=server)IM server and obtains the IM Token.
- `RTC uid`: The ID used by the user when joining the RTC room is maintained by the call component during the call, and the business side usually does not need to be processed separately in the single-call group call access.
- `callId`: CallKit business call ID is used for callback, log and invitation batch association, not NIM signaling room ID.
- `channelId`: NIM signaling room ID. Business usually does not need direct processing and can be used for logs and troubleshooting.

## Development environment

| Environmental requirements                                                                                                                                                                                        | Explain                                                                                                                                                                                                               |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Android Studio version                                                                                                                                                                                            | Android Studio 5.0 and above.Please refer to the [Android Studio version description](https://developer.android.google.cn/studio/releases/index.html)for changes to the Android Studio version number system.         |
| Android API version                                                                                                                                                                                               | Level is version 21 and above.                                                                                                                                                                                        |
| Android SDK version                                                                                                                                                                                               | Android SDK 31, Android SDK Platform-Tools 31.x.x and above versions.                                                                                                                                                 |
| Gradle and the required dependency library                                                                                                                                                                        | Download the corresponding version of Gradle and the required dependency library on the [Gradle Services](https://services.gradle.org/distributions/)page. Gradle Version: 7.4.1Android Gradle Plug-in Version: 7.1.3 |
| For the version dependency between Android Gradle plug-in, Gradle and SDK Tool, please refer to the [version description of Android Gradle plug-in](https://developer.android.com/studio/releases/gradle-plugin). |
| [Kotlin](https://blog.jetbrains.com/kotlin/category/releases/)                                                                                                                                                    | Version 1.6.21 and above.                                                                                                                                                                                             |
| CPU architecture                                                                                                                                                                                                  | ARM 64, ARMV7.                                                                                                                                                                                                        |
| Ide                                                                                                                                                                                                               | Android Studio.                                                                                                                                                                                                       |
| Other                                                                                                                                                                                                             | Rely on Androidx and do not support the support library.                                                                                                                                                              |
| The real machine of Android system 5.0 and above.Due to the lack of camera and microphone capabilities of the simulator, the project needs to be run on the real machine.                                         |

## Preparation work

Before following this article, please make sure that you have completed the following settings:

- [Create an application](https://doc.yunxin.163.com/console/concept/TIzMDE4NTA?platform=console)in the [NetEase Cloud Trust Console](https://app.yunxin.163.com/global/home)and obtain the corresponding App Key.
- [Open](https://doc.yunxin.163.com/console/concept/zc3NDYzNzc?platform=console)IM instant messaging, audio and video call 2.0, signaling products and call list functions.
- [Single chat call](https://doc.yunxin.163.com/nertccallkit/guide/zEzMTU0NTI?platform=android)has been realized.

Before using the single-call to group-call function, please complete the 1v1 single chat call access first, and confirm that you can initiate, answer and hang up 1v1 audio and video calls normally.

## Realize single call to group call

1. Initialize the call component.

在初始化时需要在 `NESetupConfig` 中开启 `enableSingleToGroupCall`，并注册 `NECallEngineDelegate`。

```
NESetupConfig config =
    new NESetupConfig.Builder(appKey)
        .enableSingleToGroupCall(true)
        .build();

NECallEngine.sharedInstance().setup(context.getApplicationContext(), config);
NECallEngine.sharedInstance().addCallDelegate(callDelegate);

```

2. Launch a 1v1 call.

```
NECallParam param =
    new NECallParam.Builder("callee_account_id")
        .callType(NECallType.VIDEO)
        .extraInfo("business attachment")
        .globalExtraCopy("business global extra")
        .build();

NECallEngine.sharedInstance()
    .call(
        param,
        result -> {
        if (result == null || !result.isSuccessful()) {
            showToast(result != null ? result.msg : "呼叫失败");
            return;
        }
        NECallInfo callInfo = result.data;
        log("call sent, callId: " + callInfo.callId);
        });

```

3. Handle ordinary calls and multi-person invitation calls.

When receiving an invitation, distinguish between ordinary 1v1 calls and multi-person invitations through `NEInviteInfo.multiCallInvite`.

```
private final NECallEngineDelegate callDelegate =
    new NECallEngineDelegateAbs() {
    @Override
    public void onReceiveInvited(NEInviteInfo info) {
        if (info.multiCallInvite) {
        showMultiInvitePage(info.callerAccId, info.callType, info.extraInfo);
        } else {
        showOneToOneIncomingPage(info.callerAccId, info.callType, info.extraInfo);
        }
    }
    };

```

接听

```
NECallEngine.sharedInstance()
    .accept(
        result -> {
        if (result == null || !result.isSuccessful()) {
            showToast(result != null ? result.msg : "接听失败");
            return;
        }
        if (currentIncomingIsMultiInvite) {
            enterMultiCallUi();
            refreshMembers(NECallEngine.sharedInstance().currentMembers());
        }
        });

```

拒绝、取消或挂断当前呼叫

```
NEHangupParam param = new NEHangupParam("business hangup extra");
NECallEngine.sharedInstance().hangup(param, result -> {
if (result == null || !result.isSuccessful()) {
    showToast(result != null ? result.msg : "挂断失败");
}
});

```

AnswerReject, cancel or hang up the current call

```
NECallEngine.sharedInstance()
    .accept(
        result -> {
        if (result == null || !result.isSuccessful()) {
            showToast(result != null ? result.msg : "接听失败");
            return;
        }
        if (currentIncomingIsMultiInvite) {
            enterMultiCallUi();
            refreshMembers(NECallEngine.sharedInstance().currentMembers());
        }
        });

```

```
NEHangupParam param = new NEHangupParam("business hangup extra");
NECallEngine.sharedInstance().hangup(param, result -> {
if (result == null || !result.isSuccessful()) {
    showToast(result != null ? result.msg : "挂断失败");
}
});

```

4. Display the invitation entrance.

业务侧应在 1v1 接通后调用 `canInviteMembers` 判断是否允许展示邀请入口。

```
private void refreshInviteButton() {
boolean canInvite = NECallEngine.sharedInstance().canInviteMembers();
inviteButton.setVisibility(canInvite ? View.VISIBLE : View.GONE);
}

@Override
public void onCallConnected(NECallInfo info) {
// 这里只代表 1v1 通话已建立，不用于判断单呼转群呼是否应切多人 UI。
refreshInviteButton();
}

```

`canInviteMembers`It will integrate the current call status, whether to turn on single call to group call, the ability of the other side, the current call information and other conditions. The business side should not only decide whether to display the entrance based on the local switch. 5. Initiate an invitation during the call.

When the user selects a member in the business UI, call `inviteMembers`.

```
private void inviteUsers(List<String> userIds) {
if (!NECallEngine.sharedInstance().canInviteMembers()) {
    showToast("当前通话不支持邀请成员");
    return;
}

NECallPushConfig pushConfig =
    new NECallPushConfig(true, "多人通话邀请", "邀请你加入多人通话", null);

NECallInviteParam param =
    new NECallInviteParam.Builder(userIds)
        .attachment("invite attachment")
        .globalExtra("invite global extra")
        .pushConfig(pushConfig)
        .maxMembers(10)
        .build();

NECallEngine.sharedInstance()
    .inviteMembers(
        param,
        result -> {
            if (result == null || !result.isSuccessful()) {
            showToast(result != null ? result.msg : "邀请失败");
            return;
            }

            NECallInviteResult inviteResult = result.data;
            int successCount = 0;
            int failedCount = 0;
            for (NECallInviteItemResult item : inviteResult.results) {
            if (item.success) {
                successCount++;
            } else {
                failedCount++;
                log("invite failed, user: " + item.inviteeUserID
                    + ", code: " + item.code
                    + ", msg: " + item.message);
            }
            }

            if (successCount > 0 && failedCount == 0) {
            showToast("邀请已发送");
            } else if (successCount > 0) {
            showToast("部分邀请已发送，部分失败");
            } else {
            showToast("邀请失败");
            }
        });
}

```

`NECallInviteParam`Field description:

| Field         | Explain                                                                                                                                                                         |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `userIDs`     | List of invited accounts. SDK will automatically ignore invalid accounts, local accounts, members who are already on the call and members who are still waiting to be answered. |
| `attachment`  | Business transmission extension will be transmitted to the invited side `NEInviteInfo.extraInfo`.                                                                               |
| `globalExtra` | Global CC extension.                                                                                                                                                            |
| `pushConfig`  | Multi-person invitation notification and offline push configuration.                                                                                                            |
| `maxMembers`  | The maximum number of people on this call. Use the default value 10 when not set or less than or equal to 0.                                                                    |

6. Switch multi-player UI.

Different roles switch between multiple UIs at different times:

- 原 1v1 通话方：收到 `onCallModeChanged` 且 `newMode == NECallMode.MULTI` 时，切换多人布局。
- 第三方被邀请人：`onReceiveInvited` 中 `info.multiCallInvite == true` 表示这是多人邀请；用户点击接听后，`accept` 成功即可切换多人布局。
- 兜底刷新：如果先收到 `onCallMembersChanged`，且 `isInMultiCall() == true` 或成员快照中出现 `NECallMemberState.WAITING`，也可以先切换多人布局再刷新成员。

The logical suggestion of switching multi-person UI is to make power equal to avoid repeating the creation of pages when multiple callbacks are triggered continuously.

```
onReceiveInvited(info):
    if info.multiCallInvite == true:
        展示多人邀请来电页
        记录当前来电为多人邀请

accept completion(result):
    if result 成功 且 当前来电是多人邀请:
        切换到多人 UI
        先用 currentMembers 渲染已有成员
        等待 onCallMembersChanged 补齐成员列表和媒体状态

onCallModeChanged(info):
    if info.newMode == NECallMode.MULTI:
        切换到多人布局
        隐藏或禁用音视频类型切换入口

```

`NECallModeChangeInfo`Field description:

| Field          | Explain                                                                         |
| -------------- | ------------------------------------------------------------------------------- |
| `oldMode`      | Change the call mode before.                                                    |
| `newMode`      | Change the call mode.                                                           |
| `memberCount`  | The number of current valid members only counts the members who have joined.    |
| `hasEverMulti` | 本次通话是否已经进入过多人模式；成功发起多人邀请并出现待接听成员后即为 `true`。 |

7. Listen to member changes and refresh UI.

After the invitation is issued, the business side should refresh the complete member list according to `onCallMembersChanged`.

`onCallMembersChanged`It is not the only entrance to switch multi-person UI. When the third-party invitee is just successfully answered, the member snapshot may be only himself first, and then gradually completes the original 1v1 parties; therefore, the multi-person UI should be switched first, and then the callback should be used to refresh the content of the palace grid.

```
onCallMembersChanged(info):
    members = info.members
    if 当前还未进入多人 UI 且 (isInMultiCall() == true 或 members 中存在 WAITING 成员):
        切换到多人 UI

    按 members 重建或刷新多人宫格：
        - WAITING 成员：展示头像 / 昵称 / 等待接听占位
        - JOINED 且 videoAvailable == true 且 videoMuted == false：展示视频画面
        - JOINED 但未开视频：展示头像或音频占位
        - LEAVING 成员：从宫格中移除，或展示离开态后移除

```

Member status description:

| State                       | Explain                                   | UI suggestions                                                    |
| --------------------------- | ----------------------------------------- | ----------------------------------------------------------------- |
| `NECallMemberState.WAITING` | To be answered, I haven't joined RTC yet. | Display the avatar/nickname position and "waiting for answer".    |
| `NECallMemberState.JOINED`  | RTC has been added.                       | Display audio and video images or audio avatars.                  |
| `NECallMemberState.LEAVING` | Leaving or already leaving.               | Remove from the list or remove after displaying the out-of-state. |

业务侧可以随时调用 `currentMembers` 获取当前完整成员快照：

```
List<NECallMemberInfo> members = NECallEngine.sharedInstance().currentMembers();

```

8. Listen to the life cycle of the invitation.

`onCallInviteStateChanged`Only notify the invitation sent by this end, and will not notify the invited end to receive the invitation or answer the action.

```
@Override
public void onCallInviteStateChanged(List<NECallInviteStateInfo> infos) {
for (NECallInviteStateInfo info : infos) {
    switch (info.state) {
    case NECallInviteState.SENT:
        showInviteWaiting(info.inviteeUserID);
        break;
    case NECallInviteState.JOINED:
        showToast("对方已加入通话");
        break;
    case NECallInviteState.REJECTED:
        showToast("对方已拒绝");
        break;
    case NECallInviteState.TIMEOUT:
        showToast("对方未接听");
        break;
    case NECallInviteState.BUSY:
        showToast("对方正在通话中");
        break;
    case NECallInviteState.UNSUPPORTED:
        showToast("对方客户端不支持多人通话");
        break;
    case NECallInviteState.FAILED:
    case NECallInviteState.CANCELED:
        showToast("邀请已结束");
        break;
    default:
        break;
    }
}
}

```

`NECallInviteStateInfo`Field description:

| Field           | Explain                                                                                                        |
| --------------- | -------------------------------------------------------------------------------------------------------------- |
| `callId`        | Current call ID.                                                                                               |
| `channelId`     | The current signaling room ID.                                                                                 |
| `inviteBatchId` | This batch invitation ID can be associated with the return result of `inviteMembers`.                          |
| `requestId`     | The current account is the ID of this invitation request.                                                      |
| `inviterUserID` | The account number of the invitee.                                                                             |
| `inviteeUserID` | The account number of the invitee.                                                                             |
| `state`         | Invitation life cycle status.                                                                                  |
| `reasonCode`    | The status reason code can be used to distinguish between rejection, busy line, timeout, joining failure, etc. |
| `message`       | The bottom description. UI display suggests giving priority to business-side localized copywriting.            |

Invitation life cycle status:

| State                           | Explain                                                            |
| ------------------------------- | ------------------------------------------------------------------ |
| `NECallInviteState.SENT`        | The invitation has been sent, and you need to wait for the answer. |
| `NECallInviteState.JOINED`      | The invited party has joined the call.                             |
| `NECallInviteState.REJECTED`    | The invitee refused.                                               |
| `NECallInviteState.TIMEOUT`     | Invitation timeout or RTC timeout after answering.                 |
| `NECallInviteState.BUSY`        | The invited party is busy.                                         |
| `NECallInviteState.FAILED`      | The invitation failed to send or join the call.                    |
| `NECallInviteState.CANCELED`    | The invitation has been cancelled.                                 |
| `NECallInviteState.UNSUPPORTED` | The invited terminal does not support multi-person calls.          |

9. Render multi-person audio video picture.

无 UI 场景下，多人宫格建议以 `NECallMemberInfo` 为数据源。对已加入成员：

- This user: use the NERTC local canvas interface to bind the local view.
- 远端用户：使用 `member.uid` 调用 NERTC 远端画布接口。
- Members to be answered: Do not bind the RTC canvas, only show the place.

```
private void bindVideoForMember(NECallMemberInfo member, NERtcVideoView videoView) {
if (member.state != NECallMemberState.JOINED) {
    return;
}

videoView.setScalingType(IVideoRender.ScalingType.SCALE_ASPECT_FILL);

String currentAccId = NIMClient.getCurrentAccount();
if (TextUtils.equals(member.userID, currentAccId)) {
    NERtcEx.getInstance().setupLocalVideoCanvas(videoView);
} else {
    NERtcEx.getInstance().setupRemoteVideoCanvas(videoView, member.uid);
}
}

```

`NECallEngine.setupRemoteView` 主要面向 1v1 远端画面。多人宫格中需要按成员 `uid` 分别绑定远端画布，建议直接使用 NERTC SDK 的 `setupRemoteVideoCanvas`。 10. Deal with the changes in the status of members' media.

Demo / CallKit-UI 的单呼转群呼页面主监听 `onCallMembersChanged`：成员加入、离开、待接听占位，以及成员快照里的当前音视频状态都从 `NECallMemberChangeInfo.members` 获取。单呼转群呼场景下，Core 在远端视频开始、停止、mute 状态变化时也会更新成员媒体状态，并通过 `onCallMembersChanged` 下发新的成员快照。

同时，Demo 也保留了 `onVideoMuted` 和 `onVideoAvailable`，用于对单个成员格子做视频状态的增量刷新。因此无 UI 接入建议以 `onCallMembersChanged` 为主入口，再按需补充视频和音频回调。

It is recommended to access without UI in the same way:

- `onCallMembersChanged`：主监听。刷新完整多人成员列表，并读取 `NECallMemberInfo.audioMuted`、`videoMuted`、`videoAvailable` 作为当前快照状态。
- `onVideoMuted`: Supplementary monitoring. When the remote or local video mute state changes, update the video switch status of the corresponding member.
- `onVideoAvailable`: Supplementary monitoring. When the availability of remote video stream changes, update the video screen display of the corresponding member.
- `onAudioMuted`: If the business UI needs to display the microphone icon, listen to the audio mute callback.

The processing logic can refer to the following pseudo-code:

```
onCallMembersChanged(info):
    members = info.members，如果为空则读取 currentMembers

    对每个 member 刷新宫格数据：
        - 记录 member.userID / uid / state
        - 读取 member.videoMuted 和 member.videoAvailable，决定展示视频画面还是头像占位
        - 读取 member.audioMuted，决定是否展示麦克风关闭图标

onVideoMuted(userId, muted):
    找到 userId 对应成员
    更新该成员 videoMuted = muted
    只刷新该成员格子的视频开关状态

onVideoAvailable(userId, available):
    找到 userId 对应成员
    更新该成员 videoAvailable = available
    available == false 时隐藏视频画面，available == true 时恢复视频画面

onAudioMuted(userId, muted):
    如果业务展示麦克风状态，更新 userId 对应成员的音频 mute 图标

```

如果业务不展示成员麦克风状态，只处理 `onCallMembersChanged`、`onVideoMuted` 和 `onVideoAvailable` 即可满足 Demo 同款多人视频宫格刷新。

## API reference

| API                                             | Explain                                                                                                                                                                                                                              |
| ----------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `NESetupConfig.Builder.enableSingleToGroupCall` | Whether to turn on the single call to group call ability, the default `false`.                                                                                                                                                       |
| `NECallEngine.canInviteMembers`                 | Whether the current call allows you to continue inviting members.                                                                                                                                                                    |
| `NECallEngine.isInMultiCall`                    | Whether the current call has entered the multi-person mode.                                                                                                                                                                          |
| `NECallEngine.currentMembers`                   | Snapshot of the current complete member.                                                                                                                                                                                             |
| `accept`                                        | Answer the call. After the third-party invitee successfully receives the multi-person invitation of `multiCallInvite == true`, the multi-person UI can be switched.                                                                  |
| `inviteMembers`                                 | Invite members to join the current call during the call.                                                                                                                                                                             |
| `onCallConnected`                               | At present, the front-end 1v1 call establishes a callback, which is not used to judge whether a single call should be cut from a group call to a multi-person UI.                                                                    |
| `onCallModeChanged`                             | The call mode changes, which is triggered when the original 1v1 call party enters the multiplayer mode for the first time, which is suitable for switching the multiplayer layout.                                                   |
| `onCallMembersChanged`                          | Call members or member media status changes, return the complete member snapshot; it is used to refresh the multi-person palace grid, and should not wait for the number of members to reach 3 before switching the multi-person UI. |
| `onVideoMuted`                                  | Video mute status change, which is used for single-member video status incremental refresh.                                                                                                                                          |
| `onVideoAvailable`                              | Changes in the availability of remote video streams are used for single-member video image incremental refresh.                                                                                                                      |
| `onAudioMuted`                                  | Audio mute state changes, and the service monitors when displaying the microphone status.                                                                                                                                            |
| `onCallInviteStateChanged`                      | Changes in the life cycle of invitations sent by this terminal.                                                                                                                                                                      |
| `onReceiveInvited`                              | Receive ordinary 1v1 invitations or multi-person invitations. When multiple people are invited, `multiCallInvite == true`.                                                                                                           |

## Frequently asked questions

**Why is the invitation entrance not displayed after 1v1 is connected?**

Please confirm whether the following conditions are met:

1. 本端初始化时是否设置 `enableSingleToGroupCall(true)`。
2. Whether a 1v1 call has been established and the current status is in call.
3. Whether the other end is a new version that supports single-call to group call, and also turns on the ability.
4. Has the upper limit of the number of people been reached at present?

业务侧建议直接以 `NECallEngine.sharedInstance().canInviteMembers()` 的返回值作为入口展示依据。

**`inviteMembers`After the return is successful, why hasn't the member appeared in the call?**

`inviteMembers` 的 observer 只表示邀请信令发送结果。成员真正加入以 `onCallMembersChanged` 中 `NECallMemberState.JOINED` 为准。邀请发送后可以先展示 `NECallMemberState.WAITING` 占位。

**How can the invited party distinguish between ordinary 1v1 calls and multiple invitations?**

在 `onReceiveInvited` 中判断 `NEInviteInfo.multiCallInvite`。

```
if (info.multiCallInvite) {
  // 多人邀请
} else {
  // 普通 1v1 呼叫
}

```

**Can the 1v1 UI be restored after the multiplayer call is returned to 2 people?**

Recovery is not recommended. At present, after entering the multiplayer mode, this call remains in multiplayer mode; when returning to 2 people, it still displays the multiplayer double-player palace, and continues to disable audio and video switching.

**可以直接用 `NEGroupCall` 发起多人通话吗？**

单呼转群呼不使用旧版 `NEGroupCall` 群呼链路。该能力是在已有 1v1 通话内邀请成员加入当前房间，请使用 `NECallEngine.inviteMembers`。

**What callbacks must be handled without UI scenarios?**

At least the following callbacks need to be handled:

- `onReceiveInvited`：展示普通来电页或多人邀请页；`multiCallInvite == true` 时记录为多人邀请。
- `accept`: The third-party invitee switches the multi-person UI after successfully answering the multi-person invitation.
- `onCallConnected`: 1v1 Display the page in the call after the call is established, and refresh the invitation entrance.
- `onCallModeChanged`: The original 1v1 caller switches the multiplayer layout after entering the multiplayer mode.
- `onCallMembersChanged`: Refresh the member list, the place to be answered and the audio and video screen.
- `onCallInviteStateChanged`: Display prompts such as invitation rejection, timeout, busy line, not supported, etc.
- `onCallEnd`: Close the page and release resources.

............
