# 03 — Advanced (UI-included)

> Advanced configuration for the UI-included Call Kit on iOS: intercept inbound, floating window, call banner, virtual background, custom ringtone, custom answer background, privatization, get caller/callee user info.
> ⬅️ Back to [README](./README.md) · Base integration: [`01`](./01-integration-ios.md).

**Relevance to this project (audio-only outbound to CSR):**
- 🟢 **Implemented:** [Call banner](#call-banner) (enabled 2026-07-17).
- 🔵 **Directly useful:** [Intercept inbound](#intercept-inbound-requests) (block CSR→user), [Custom ringtone](#custom-ringtone), [Get caller/callee info](#get-callercallee-user-info) (for reporting).
- ⚪ **Reference-only:** [Virtual background](#virtual-background) (video feature), [Floating window](#floating-window), [Custom answer background](#custom-answer-background) — kept for completeness.

> **Related:** system call answering (LiveCommunicationKit / PushKit VoIP) has its own file → [`05-system-call-lck.md`](./05-system-call-lck.md). Banner and LCK solve **different** problems — see the comparison there.

---

## Intercept inbound requests

> 🔵 Key for this project: the requirement is **outbound only**. `NECallUIKitDelegate` surfaces the built-in callee page. Return `NO` in the completion to intercept — route to a custom page, or (for this project) suppress the unwanted CSR→user incoming UI.

### V2 (current)

```objc
- (void)didCallComingWithInviteInfo:(NEInviteInfo *)inviteInfo
                      withCallParam:(NEUICallParam *)callParam
                     withCompletion:(void (^)(BOOL))completion {
    // Carries invite info + UI params; customize placeholder images if needed
    callParam.remoteDefaultImage = [[SettingManager shareInstance] remoteDefaultImage];
    callParam.muteDefaultImage   = [[SettingManager shareInstance] muteDefaultImage];

    // completion(NO) → intercept (do NOT show the built-in callee page)
    // completion(YES) → show the built-in callee page
    completion(YES);
}
```

### V1.8.2 (legacy)

Set `disableShowCalleeView = YES` at init, then implement your own callee UI via the delegate callbacks:

```objc
NERtcCallUIConfig *config = [[NERtcCallUIConfig alloc] init];
config.uiConfig.disableShowCalleeView = YES;   // do not pop the built-in callee page
[[NERtcCallUIKit sharedInstance] setupWithConfig:config];
```

> For this project, `completion(NO)` (V2) / `disableShowCalleeView = YES` (legacy) is the lever to block incoming CSR calls — pending decision Q4. Custom UI guide: <https://doc.yunxin.163.com/nertccallkit/docs/zM2Mzk2MjY?platform=iOS>.

---

## Custom ringtone

> 🔵 Only UI-included integration supports custom ringtones (no-UI path implements ringing itself; "join RTC early" mode doesn't support ringtones). Supported since v2.2.0.

Put custom sounds in the project bundle, get their paths via `pathForResource`, and assign to `NERtcCallUIKit.sharedInstance.ringFile`. Ring slots:

| Property | Meaning |
|---|---|
| `callerRingFilePath` | Caller call prompt |
| `calleeRingFilePath` | Callee received-invite prompt |
| `rejectRingFilePath` | Reject prompt |
| `busyRingFilePath` | Busy prompt |
| `noResponseFilePath` | No-response prompt |

```objc
NSString *mp3Path = [[NSBundle mainBundle] pathForResource:@"custom" ofType:@"mp3"];
NERtcCallUIKit.sharedInstance.ringFile.callerRingFilePath = mp3Path;
```

`NERingFile` interface:

```objc
@interface NERingFile : NSObject
@property(nonatomic, strong, nullable) NSString *callerRingFilePath;
@property(nonatomic, strong, nullable) NSString *calleeRingFilePath;
@property(nonatomic, strong, nullable) NSString *rejectRingFilePath;
@property(nonatomic, strong, nullable) NSString *busyRingFilePath;
@property(nonatomic, strong, nullable) NSString *noResponseFilePath;
- (instancetype)initWithBundle:(NSBundle *)bundle;
@end
```

---

## Display name & avatar in call UI

> 🟢 **Implemented** (2026-07-20). iOS **không** tự lấy NIM info cho call UI → không set thì hiện accid (khác Android có `UserInfoHelper` default). Hai touch point trong `RNNeteaseIm.m`:
> - **Caller** (`startVoiceCall`): `RNNIMFillCallUserInfo(callParam, accid)` set `callParam.remoteShowName`/`remoteAvatar` từ `[[NIMSDK sharedSDK].userManager userInfo:accid]`.
> - **Callee** (`RNNIMCallUIDelegate` implement `NECallUIKitDelegate`, gán `[NERtcCallUIKit sharedInstance].delegate` trong `setupCallKitWithAppKey:`): `didCallComingWithInviteInfo:` điền param từ `inviteInfo.callerAccId`, **cold cache → `fetchUserInfos:` async** rồi `completion(YES)`.
>
> Nguồn = NIM profile (app đẩy nick/avatar lúc connect). Xem `CALLKIT_INTEGRATION_NOTES.md#16`.

## Get caller/callee user info

> 🔵 Useful for reporting/statistics.

**Pass extra info to the callee on call** — set `attachment` when calling; the callee reads it in `onInvited`:

```objc
- (void)call:(NSString *)userID
        type:(NERtcCallType)type
  attachment:(nullable NSString *)attachment   // passed through to onInvited
 globalExtra:(nullable NSString *)extra
   withToken:(nullable NSString *)token
 channelName:(nullable NSString *)channelName
  completion:(nullable void (^)(NSError *_Nullable error))completion;
```

**Own info after joining RTC** — `onJoinChannel:` gives your `accid` (IM), `uid` (RTC), `cid` (RTC channel), `cname`:

```objc
- (void)onJoinChannel:(NERtcCallKitJoinChannelEvent *)event;

@interface NERtcCallKitJoinChannelEvent : NSObject
@property(nonatomic, copy)   NSString *accid;   // IM userID
@property(nonatomic, assign) uint64_t  uid;     // RTC uid
@property(nonatomic, assign) uint64_t  cid;     // RTC channelId
@property(nonatomic, copy)   NSString *cname;   // RTC channelName
@end
```

**Peer info** — `onInvited:` gives the caller's IM `accid` (`invitor`); during a call, `memberOfAccid:completion:` maps an IM accid → RTC uid:

```objc
- (void)onInvited:(NSString *)invitor        // peer IM accid
          userIDs:(NSArray<NSString *> *)userIDs
      isFromGroup:(BOOL)isFromGroup
          groupID:(nullable NSString *)groupID
             type:(NERtcCallType)type
       attachment:(nullable NSString *)attachment;

// Only works during a call:
- (void)memberOfAccid:(NSString *)accid
           completion:(nullable void (^)(NIMSignalingMemberInfo *_Nullable info))completion;
```

---

## Floating window

> ⚪ Reference. Minimize the call to a floating window. Configure at `setupWithConfig`:

| Config | Type | Meaning |
|---|---|---|
| `enableFloatingWindow` | BOOL | In-app floating window (via RTC Canvas View), default off |
| `enableFloatingWindowOutOfApp` | BOOL | Out-of-app floating window (system PiP), default off — **iOS 16+ only** |

```objc
NECallUIKitConfig *config = [[NECallUIKitConfig alloc] init];
config.uiConfig.enableFloatingWindow = YES;
config.uiConfig.enableFloatingWindowOutOfApp = YES;   // iOS 16+
[[NERtcCallUIKit sharedInstance] setupWithConfig:config];
```

Out-of-app floating window needs an extra transcoding pod (main target only):

```ruby
pod 'NETranscodingKit'
```

---

## Call banner

> 🟢 **Implemented** (2026-07-17). Since v4.3.0. A lightweight top banner on incoming call with one-tap answer/reject, instead of the full-screen page. Off by default — **we turn it on**.

**Where:** `RNNeteaseIm.m` → `setupCallKitWithAppKey:`, right after `setupWithConfig:` (must be after — the singleton has to be initialised first). Inside the existing `sCallKitSetup` guard + `@try`.

> ⚠️ **Banner only works while the app is alive.** For incoming calls when the app is backgrounded/killed you need PushKit → [`05-system-call-lck.md`](./05-system-call-lck.md).

- Runtime toggle (takes effect **next** call): `[[NERtcCallUIKit sharedInstance] enableIncomingBanner:YES]` / `:NO`.
- On iOS the banner uses an independent `UIWindow` → **no extra permission needed** (unlike Android).
- A second call during the banner → SDK auto-replies busy. Ringtone plays during the banner and is not interrupted when tapping into the full-screen page; it stops at call end (answer/reject/timeout/cancel).

```objc
[[NERtcCallUIKit sharedInstance] enableIncomingBanner:YES];  // enable
[[NERtcCallUIKit sharedInstance] enableIncomingBanner:NO];   // restore full-screen
```

---

## Virtual background

> ⚪ Reference (video-only; N/A for audio-only). Background blur during a video call. Enable at init, plus add RTC ability pods.

```objc
NECallUIKitConfig *config = [[NECallUIKitConfig alloc] init];
config.uiConfig.enableVirtualBackground = YES;   // show blur button during video call
[[NERtcCallUIKit sharedInstance] setupWithConfig:config];
```

```ruby
pod 'NERtcSDK/Nenn'
pod 'NERtcSDK/Segment'
```

---

## Custom answer background

> ⚪ Reference (since v4.8.0). Custom background on the callee ringing/answering page. Local `UIImage` or public `http/https` URL. Falls back to default avatar-blur on failure.

```objc
// Local image:
UIImage *image = [UIImage imageNamed:@"custom_answer_bg"];
NECallUIDynamicConfig *config = [[NECallUIDynamicConfig alloc] init];
config.incomingCallBackground = [NECallUIIncomingBackgroundSource imageSource:image];
[[NERtcCallUIKit sharedInstance] setDynamicUIConfig:config];

// URL image:
NSURL *url = [NSURL URLWithString:@"https://example.com/custom_answer_bg.jpg"];
config.incomingCallBackground = [NECallUIIncomingBackgroundSource urlSource:url];
[[NERtcCallUIKit sharedInstance] setDynamicUIConfig:config];

// Reset to default:
[[NERtcCallUIKit sharedInstance] setDynamicUIConfig:nil];
```

Only affects the ringing/answering page (not in-call). Applies to subsequent calls, not the currently-displayed one.

---

## Privatization

> ⚪ Reference. Point the component's NERTC at a privatized deployment. IM SDK privatization must be configured separately (contact Yunxin support).

```objc
NESetupConfig *setupConfig = [[NESetupConfig alloc] initWithAppkey:@"app key"];
NERtcEngineContext *context = [[NERtcEngineContext alloc] init];
NERtcServerAddresses *address = [[NERtcServerAddresses alloc] init];
address.channelServer = @"your channel server";   // privatized RTC service URL
context.serverAddress = address;
setupConfig.rtcInfo = context;

[[NECallEngine sharedInstance] setup:setupConfig];
```
