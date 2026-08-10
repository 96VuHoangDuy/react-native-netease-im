# 01 — Call Kit Integration (iOS, with UI)

> How to integrate the **NERTC Call Kit** (UI-included) and place a 1-to-1 call on iOS.
> This is the recommended path (prebuilt call UI). For the raw-engine / no-UI path see [`04-no-ui-scheme.md`](./04-no-ui-scheme.md).
> ⬅️ Back to [README](./README.md) · Prerequisites (console app + services): [README §7](./README.md#7-console-prerequisites-one-time-before-any-code).

> **Project note:** this feature is **audio-only outbound to a CSR**. Wherever the doc uses `NECallTypeVideo`, use `NECallTypeAudio`, and skip camera usage. See [README §5](./README.md#5-project-specific-constraints-differ-from-generic-yunxin-doc).

---

## 1. Development environment

| Requirement | Value |
|---|---|
| Xcode | 14+ (UI-included path); 10+ for the base (no-UI) path |
| iOS deployment target | 10.0+ (9.0+ for the base package) |
| CocoaPods | Installed |
| Device | **Real device required** (the internal audio/video SDK can't run on the simulator) |
| Signing | Valid developer signing configured |

The call component is built on **NIM SDK (V10)** + **NERTC SDK**. On iOS the **NIM SDK is bundled** inside the Call Kit, so you integrate `NERtcCallKit` + `NERtcCallUIKit` + the NERTC RTC pod.

---

## 2. CocoaPods dependencies

Create a Podfile (`pod init`), then add the component. Two options:

*Option A — do not pin the SDK version* (component auto-picks a compatible IM dependency):

```ruby
pod 'NERtcCallKit'          # call component
pod 'NERtcCallUIKit'        # call component UI kit
pod 'NERtcSDK/RtcBasic'     # RTC audio/video base
```

*Option B — the project already integrates NIM/NERTC and needs to pin versions* (use the `NOS_Special` subspec, then declare your own SDK versions):

```ruby
pod 'NERtcCallKit/NOS_Special'      # call component
pod 'NERtcCallUIKit/NOS_Special'    # call component UI kit
pod 'NIMSDK_LITE', '10.9.70'        # IM base (example version — use the project's actual)
# pod 'NIMSDK_LITE/FTS', '10.9.70'  # IM full-text-search pkg (same version as base)
pod 'NERtcSDK/RtcBasic', '5.9.0'    # RTC base (example version)
```

Then:

```ruby
pod install
```

> ⚠️ **This project uses Option B** — NIM is already integrated via `react-native-netease-im`. Pin to the Phase-2 target (iOS NIM `10.9.53`), and keep **all NIM sub-packages on the same version** (base and FTS must match or it errors). Version ↔ version mapping: [Yunxin changelog (iOS)](https://doc.yunxin.163.com/nertccallkit/concept/jIzNDA4Nzc?platform=client).

---

## 3. Info.plist — permissions

Audio-only needs the microphone; add camera only for video.

```xml
<key>NSMicrophoneUsageDescription</key>
<string>Used for voice calls with customer service</string>
<!-- Only if video is enabled: -->
<!-- <key>NSCameraUsageDescription</key><string>Used for video calls</string> -->
```

Enable **Background Modes → Audio, AirPlay, and Picture in Picture** and **Voice over IP** if the call must survive backgrounding, and configure the audio session per your app's needs.

---

## 4. Initialize NIM + Call Kit

Init the IM SDK first, then `NECallEngine`, then the UI kit. Recommended at app launch. `setup:` **must** be called before any other component method.

```objc
#import <NIMSDK/NIMSDK.h>

// 1) IM SDK init (V10)
NIMSDKOption *option = [NIMSDKOption optionWithAppKey:appKey];
option.apnsCername = @"your apns certificate";
option.pkCername   = @"your push kit certificate";
V2NIMSDKOption *v2Option = [[V2NIMSDKOption alloc] init];
v2Option.useV1Login = NO;   // NO = use V10 login (default); YES = keep V9 login
[[NIMSDK sharedSDK] registerWithOptionV2:option v2Option:v2Option];
```

```objc
#import <NERtcCallKit/NERtcCallKit.h>
@interface SomeViewController () <NECallEngineDelegate>
@end

@implementation SomeViewController

- (void)setupSDK {
    // 2) Call engine init
    NESetupConfig *setupConfig = [[NESetupConfig alloc] initWithAppkey:kAppKey];
    [[NECallEngine sharedInstance] setup:setupConfig];

    // 3) UI kit init
    NECallUIKitConfig *config = [[NECallUIKitConfig alloc] init];
    [[NERtcCallUIKit sharedInstance] setupWithConfig:config];
}
@end
```

---

## 5. Log in to IM

Static-token login example (dynamic-token & auto-login: see Yunxin IM docs):

```objc
- (void)login {
    NSString *accountId = @"accountId";
    NSString *token = @"token";
    [[NIMSDK sharedSDK].v2LoginService login:accountId token:token
        option:nil
        success:^{ NSLog(@"login succ"); }
        failure:^(V2NIMError * _Nonnull error) { NSLog(@"login fail: %@", error); }];
}
```

---

## 6. Place a 1v1 (audio) call

With the UI kit, a call is a few lines. Business flow: both sides logged into IM + Call Kit initialized → caller has both accounts → caller calls `callWithParam:`.

```objc
// For this project: resolve the CSR accid from service/idle_tc_csr first (README §4),
// then use it as remoteUserAccid. Use NECallTypeAudio for audio-only.
NEUICallParam *callParam = [[NEUICallParam alloc] init];
callParam.currentUserAccid = @"current user accid";
callParam.remoteUserAccid  = csrAccid;               // accid from idle_tc_csr
callParam.remoteShowName   = @"CSR name";
// audio-only:
[[NERtcCallUIKit sharedInstance] callWithParam:callParam withCallType:NECallTypeAudio];
```

- Callee taps answer on the called page → call connects.
- Either side taps hang up to end.

---

## 7. Next

- Custom ringtone, banner, floating window, intercept inbound, caller/callee info → [`03-advanced.md`](./03-advanced.md).
- Call ticket (话单) after a call → [`02-call-list.md`](./02-call-list.md).
- Raw engine (no UI) if the prebuilt UI can't match Figma → [`04-no-ui-scheme.md`](./04-no-ui-scheme.md).
- Official custom-UI guide (iOS): <https://doc.yunxin.163.com/nertccallkit/docs/zM2Mzk2MjY?platform=iOS>
