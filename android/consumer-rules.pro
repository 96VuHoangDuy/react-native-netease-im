# Consumer ProGuard/R8 rules — tự động merge vào app tiêu thụ thư viện này.
#
# Nguồn: docs/reference/call-android-native/01-integration-android.md §4 ProGuard
# (docs chính chủ NetEase). Đừng tự suy rule từ bytecode — đọc file đó trước.
#
# NERTC/call-ui bị R8 phá theo 2 cơ chế, đều do không có static reference trong code:
#
# 1) Class.forName() từ <meta-data> trong manifest ("xkit startup") → R8 STRIP class:
#      RuntimeException: Unable to get provider ...corekit.startup.InitializationProvider
#      Caused by: ClassNotFoundException: com.netease.yunxin.nertc.ui.CallKitUIService
#
# 2) JNI_OnLoad trong libnertc_sdk.so gọi FindClass() bằng tên chuỗi cứng
#    ("com/netease/lava/nertc/impl/NERtcCore") → R8 RENAME class ⇒ FindClass fail ⇒ abort():
#      Fatal signal 6 (SIGABRT) ... libnertc_sdk.so (JNI_OnLoad+148)
#    Xảy ra lúc bấm call (System.loadLibrary trong NERtcCore/NativeLibLoader).
#    Lưu ý: NERtcCore/NativeLibLoader KHÔNG khai method `native` nào → chỉ keep class
#    có native method là KHÔNG đủ, phải keep cả package.
#
# Cả hai chỉ lộ ở buildType release (minifyEnabled true); debug không minify.
# Các aar NetEase đều có proguard.txt RỖNG → không tự bảo vệ.

# NERTC SDK
-dontwarn com.netease.lava.**
-keep class com.netease.lava.** {*;}
# Bao trùm luôn yunxin.kit (XKitService/corekit.startup) và yunxin.nertc
# (CallKitUIService/CallKitService) → cover cả cơ chế (1).
-dontwarn com.netease.yunxin.**
-keep class com.netease.yunxin.** {*;}
