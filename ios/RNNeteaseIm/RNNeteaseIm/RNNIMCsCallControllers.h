//
//  RNNIMCsCallControllers.h
//  RNNeteaseIm
//
//  Subclass UI state controller của NERtcCallUIKit để ép tên + logo CSKH lúc render.
//  Đăng ký qua [NERtcCallUIKit setCustomCallClass:] với key kCalledState / kAudioInCall.
//  Lưới an toàn cho các path không đi qua delegate didCallComing (resume từ VoIP push khi
//  app killed) và cho trường hợp SDK refreshUI ghi đè label sau khi fetch profile async.
//  Cuộc gọi non-CSR giữ nguyên UI mặc định SDK.
//

#import <NERtcCallUIKit/NERtcCallUIKit.h>

NS_ASSUME_NONNULL_BEGIN

/// Màn đổ chuông phía callee (kCalledState).
@interface RNNIMCsCalledViewController : NECalledViewController
@end

/// Màn đang trong cuộc gọi audio (kAudioInCall). Thay thanh pill nhỏ mặc định của SDK bằng
/// 3 nút tròn to có label (麦克风 / 取消 / 扬声器), đồng bộ với màn caller/callee.
@interface RNNIMCsAudioInCallController : NEAudioInCallController
@end

NS_ASSUME_NONNULL_END
