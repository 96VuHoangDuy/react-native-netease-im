//
//  RNNIMCsCallBranding.h
//  RNNeteaseIm
//
//  Branding CSKH cho call UI: accid prefix "csr" → tên + logo cố định của app,
//  bỏ qua NIM profile cá nhân của nhân viên. Dùng chung cho RNNeteaseIm.m (fill
//  callParam) và các subclass UI controller (ép lại lúc render).
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface RNNIMCsCallBranding : NSObject

/// accid của CSKH theo quy ước prefix "csr".
+ (BOOL)isCsrAccid:(nullable NSString *)accid;

/// Tên hiển thị CSKH. Ưu tiên tên JS đã localize; chưa set thì fallback tiếng Trung.
+ (NSString *)displayName;

/// JS setCustomerServiceCallName gọi vào (mỗi lần login/đổi ngôn ngữ).
+ (void)setDisplayName:(nullable NSString *)name;

/// file:// URL tới cs_call_logo.png trong bundle app (SDWebImage load được file URL).
+ (nullable NSString *)logoFileUrl;

/// UIImage của logo (nền TRONG SUỐT) — chỉ dùng dựng nền blur.
+ (nullable UIImage *)logoImage;

/// file:// URL tới cs_call_avatar.png — bản logo nền TRẮNG ĐẶC cho ô avatar.
+ (nullable NSString *)avatarFileUrl;

/// UIImage cho ô avatar. Tách khỏi logoImage vì logo nền trong suốt bị chìm vào nền blur tối.
+ (nullable UIImage *)avatarImage;

/// JS setCallControlLabels gọi vào (mỗi lần login/đổi ngôn ngữ). Key: micOn, micOff, hangup,
/// speakerOn, speakerOff. nil/thiếu key → dùng fallback tiếng Trung.
+ (void)setCallControlLabels:(nullable NSDictionary *)labels;

/// Text trạng thái cho nút in-call theo key trên. Luôn trả chuỗi (fallback tiếng Trung kiểu WeChat)
/// — cần cho case app killed → vào call trước khi JS kịp set.
+ (NSString *)callControlLabelForKey:(NSString *)key;

@end

NS_ASSUME_NONNULL_END
