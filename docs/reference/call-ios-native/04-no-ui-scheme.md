# 04 — No-UI Scheme (raw engine)

> Integrate the Call Kit **base package (no UI)** and drive calls directly through `NECallEngine`. Use this only if the prebuilt UI ([`01`](./01-integration-ios.md)) can't match the Figma design — you then build the call UI yourself.
> ⬅️ Back to [README](./README.md).

> Yunxin recommends the UI-included path. Consider this for open decision **Q3** (prebuilt vs custom vs hybrid) in [README §6](./README.md#6-open-decisions--risks-phase-3).

Console prerequisites are the same as [`01`](./01-integration-ios.md). The difference is you import only `NERtcCallKit` (no `NERtcCallUIKit`) and integrate NERTC separately.

---

## Integrate (no UI)

The base package does **not** bundle NERTC — add it separately in the Podfile:

```ruby
pod 'NERtcCallKit', '3.3.0'                                  # use the project's actual version
pod 'NERtcSDK', '5.6.50', :subspecs => ['RtcBasic']         # RtcBasic = no beauty; drop subspecs for beauty
```

> SDK version mapping is enforced (e.g. call `V3.3.0` ↔ NIM `V10.6.0` + NERTC `V5.6.50`). Use the project's Phase-2 target versions (iOS NIM `10.9.53`); keep all NIM sub-packages on the same version. [Yunxin changelog (iOS)](https://doc.yunxin.163.com/nertccallkit/concept/jIzNDA4Nzc?platform=client).

IM SDK integration (`NIMSDK_LITE`), init (`registerWithOptionV2`), and login (`v2LoginService login:`) are the same as [`01` §4–5](./01-integration-ios.md#4-initialize-nim--call-kit).

CocoaPods keyword note: `NIMSDK_LITE` = IM with NetEase Object Storage (NOS); `NIMSDK_LITE/FCS` = IM with S3 file storage.

---

## Initialize the engine (no UI)

```objc
#import <NERtcCallKit/NERtcCallKit.h>
@interface SomeViewController () <NECallEngineDelegate>
@end

@implementation SomeViewController
- (void)setupSDK {
    NESetupConfig *config = [[NESetupConfig alloc] initWithAppkey:@"your app key"];
    [[NECallEngine sharedInstance] setup:config];
}
@end
```

**(Optional) preview resolution** — after init, set capture resolution via RTC (`width`/`height`; default preview 640×480):

```objc
NERtcEngine *coreEngine = [NERtcEngine sharedEngine];
NERtcVideoEncodeConfiguration *config = [[NERtcVideoEncodeConfiguration alloc] init];
config.width = 640;
config.height = 360;
[coreEngine setLocalVideoConfig:config];
```

---

## 1-to-1 call (point-to-point)

Flow: `addCallDelegate` → `call:` → peer `onReceiveInvited`/`onInvited` → `accept:` → `onUserEnter` (connected) → `hangup:`.

**Add the delegate** (add on the page, remove on `dealloc` to avoid leaks):

```objc
- (void)viewDidLoad {
    [super viewDidLoad];
    [NECallEngine.sharedInstance addCallDelegate:self];
    [[NECallEngine sharedInstance] setTimeout:30];   // seconds
}
- (void)dealloc {
    [NECallEngine.sharedInstance removeCallDelegate:self];
}
```

**Caller initiates** — build `NECallParam` (use `NECallTypeAudio` for this feature):

```objc
NECallParam *callParam = [[NECallParam alloc] initWithAccId:csrAccid
                                             withCallType:NECallTypeAudio];
[[NECallEngine sharedInstance] call:callParam completion:^(NSError * _Nullable error) {
    if (error) {
        // Peer offline (pushed via APNs) — don't show an error dialog:
        if (error.code == 10202 || error.code == 10201) { return; }
        // Call failed → tear down the call page:
        if (error.code == 21000 || error.code == 21001) { /* destroy call VC */ }
    }
}];
```

Optional custom push — `NECallPushConfig` (`pushTitle`, `pushContent`, `pushPayload`, `needBadge` default YES, `needPush` default YES); push params go inside `pushPayload.apsField`.

**Callee receives** — `onReceiveInvited:` / `onInvited:`; fetch the caller's user info and present your call page:

```objc
- (void)onReceiveInvited:(NEInviteInfo *)info {
    [NIMSDK.sharedSDK.userManager fetchUserInfos:@[info.callerAccId]
        completion:^(NSArray<NIMUser *> * _Nullable users, NSError * _Nullable error) {
            if (!error) {
                // present your own call page (see sample NECallViewController)
            }
        }];
}
```

**Callee answers** — `accept:` (triggers `onUserEnter` when the peer joins; that = connected):

```objc
- (void)acceptCall {
    [[NECallEngine sharedInstance] accept:^(NSError * _Nullable error) {
        if (error) {
            // answer failed → destroy call page
        } else {
            // for video: setupLocalView / setupRemoteView
        }
    }];
}

- (void)onUserEnter:(NSString *)userID {
    // connected — for video calls, bind views here
    // [[NECallEngine sharedInstance] setupLocalView:self.smallVideoView];
    // [[NECallEngine sharedInstance] setupRemoteView:self.bigVideoView];
}
```

**Hang up** — caller cancel / callee reject / in-call hangup all use `hangup:`:

```objc
- (void)hangup {
    NEHangupParam *hangupParam = [[NEHangupParam alloc] init];
    [[NECallEngine sharedInstance] hangup:hangupParam completion:^(NSError * _Nullable error) {}];
}
```

> For audio-only you generally don't call `setupLocalView` / `setupRemoteView` (those bind video views). The `NECallViewController` example in the raw source is a video sample — strip the video-view wiring for a voice call.

### Known error codes (from `call:` completion `NSError.code`)

| Code | Meaning |
|:---:|---|
| 10201 / 10202 | Peer offline (invitation delivered via APNs) — do not show an error dialog |
| 21000 / 21001 | Call failed — tear down the call page |

> Full error-code reference: see the Yunxin API docs; the iOS draft only documents the codes above inline.

---

## Group call & single-to-group (no UI) — reference only

> ⚪ **Not needed for this project** (1v1 audio only). Kept for completeness. Group call is **Beta** — contact your Yunxin manager to enable.

**Group call** uses `NEGroupCallKit` — init via `setupGroupCall:` (`GroupConfigParam` with `appid`, `rtcSafeMode`), then group call / invite / accept / join / hangup / query APIs (parallel to the [Android group API](../call-android-native/04-no-ui-scheme.md#group-call--single-to-group-no-ui--reference-only)).

```objc
GroupConfigParam *param = [[GroupConfigParam alloc] init];
param.appid = kAppKey;
param.rtcSafeMode = YES;
[[NEGroupCallKit sharedInstance] setupGroupCall:param];
```

**Single-call → group call** (since v4.7.0): based on the existing 1v1 signaling + RTC room; invite new members via `[NECallEngine inviteMembers:completion:]` (does **not** use the legacy `NEGroupCallKit`). Requires `setupConfig.enableSingleToGroupCall = YES`, and both ends on a supporting SDK version. Max 10 people. Only after the 1v1 call is connected. A member truly joins when the state becomes `NECallMemberStateJoined`. Once in multi-person mode it stays there (no revert to 1v1, no audio/video switch). The single-to-group ticket is server-generated — contact Yunxin support to enable.

Full details: see the raw source in [`99-reference-raw.md`](./99-reference-raw.md) or the official docs.
