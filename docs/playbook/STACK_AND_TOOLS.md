# Stack And Tools

## Package

- Name: `react-native-netease-im`
- Version: `3.1.0`
- Main entry: `index.ts`
- Type entry trong `package.json`: `dist/index.d.ts`
- Peer dependency:
  - `react-native >= 0.60.0`

## TypeScript / JS Layer

- `tsconfig.json`
  - `module: commonjs`
  - `target: ES6`
  - `declaration: true`
  - `outDir: ./dist`
  - `include: ./src/**/*`
- `babel.config.js`
  - `module:metro-react-native-babel-preset`

## Android Native Stack

- Gradle plugin: `com.android.library`
- `android/build.gradle`
  - default `compileSdkVersion 34`
  - default `targetSdkVersion 34`
  - default `minSdkVersion 24`
  - `aidl true`
- Native React dependency:
  - `com.facebook.react:react-native:+`
- NIM dependencies (Phase 2 — bumped to V10 `10.9.52`):
  - `com.netease.nimlib:basesdk:10.9.52`
  - `com.netease.nimlib:push:10.9.52`
  - `com.netease.nimlib:lucene:10.9.52`
- Other Android deps:
  - `net.zetetic:android-database-sqlcipher:4.5.3`
  - `androidx.sqlite:sqlite:2.1.0`
  - `com.alibaba:fastjson:1.2.57`
  - `com.nostra13.universalimageloader:universal-image-loader:1.9.5`
- Bundled asset/jar:
  - `android/libs/MiPush_SDK_Client_3_6_2.jar`
  - `android/src/main/assets/pinyin/index.dat`

## iOS Native Stack

- Podspec: `RNNeteaseIm.podspec`
  - `platform :ios, "12.0"`
  - dependency `React-Core`
  - dependency `NIMSDK`, version `10.9.53` (Phase 2 — bumped to V10; pod vẫn ship `NIMAVChat.xcframework`)
  - dependency `Reachability`
- ObjC source lives under `ios/RNNeteaseIm/RNNeteaseIm`
- Xcode project exists in `ios/RNNeteaseIm/RNNeteaseIm.xcodeproj`
- Xcode project `IPHONEOS_DEPLOYMENT_TARGET = 12.0` (đã align với podspec ở Phase 2)

## Extra Runtime Dependencies Observed In Source

### iOS

Source imports `react-native-config/RNCConfig.h` tại nhiều file:

- `RNNeteaseIm.m`
- `CacheUsers.m`
- `NIMMessageMaker.m`
- `NIMViewController.m`

Các env key được đọc:

- `IM_CER_NAME`
- `API_URL`
- `API_AUTH_KEY`
- `IM_BUSINESS_ID`

### Android

Source có custom backend cache và anti-spam hook:

- `CacheUsers.setApiUrl(...)`
- `CacheUsers.setAuthKey(...)`
- `SessionService.setBusinessId(...)`

Nhưng repo hiện không expose các setter này ra JS public API.

## Testing / Verification Tooling

- Không có test runner hữu dụng trong repo.
- Không có lint script.
- Không có build script publish type artifact.
- `node_modules/` đang có trong repo, nhưng điều này không thay thế cho quy trình verify chuẩn.
