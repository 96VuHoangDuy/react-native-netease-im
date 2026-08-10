//
//  RNNIMCsCallBranding.m
//  RNNeteaseIm
//

#import "RNNIMCsCallBranding.h"

static NSString *sCsCallName = nil;

@implementation RNNIMCsCallBranding

+ (BOOL)isCsrAccid:(NSString *)accid {
    return [accid hasPrefix:@"csr"];
}

+ (NSString *)displayName {
    return sCsCallName.length ? sCsCallName : @"中越之家客服";
}

+ (void)setDisplayName:(NSString *)name {
    sCsCallName = name.length ? name : nil;
}

+ (NSString *)logoFileUrl {
    NSString *path = [[NSBundle mainBundle] pathForResource:@"cs_call_logo" ofType:@"png"];
    return path.length ? [NSString stringWithFormat:@"file://%@", path] : nil;
}

+ (UIImage *)logoImage {
    NSString *path = [[NSBundle mainBundle] pathForResource:@"cs_call_logo" ofType:@"png"];
    return path.length ? [UIImage imageWithContentsOfFile:path] : nil;
}

+ (NSString *)avatarFileUrl {
    NSString *path = [[NSBundle mainBundle] pathForResource:@"cs_call_avatar" ofType:@"png"];
    return path.length ? [NSString stringWithFormat:@"file://%@", path] : nil;
}

+ (UIImage *)avatarImage {
    NSString *path = [[NSBundle mainBundle] pathForResource:@"cs_call_avatar" ofType:@"png"];
    return path.length ? [UIImage imageWithContentsOfFile:path] : nil;
}

static NSDictionary *sCallControlLabels = nil;

+ (void)setCallControlLabels:(NSDictionary *)labels {
    sCallControlLabels = labels.count ? [labels copy] : nil;
}

+ (NSString *)callControlLabelForKey:(NSString *)key {
    NSString *value = sCallControlLabels[key];
    if ([value isKindOfClass:NSString.class] && value.length) return value;
    // Fallback tiếng Trung kiểu WeChat — JS chưa kịp set (app killed → vào call ngay).
    static NSDictionary *defaults;
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        defaults = @{
            @"micOn": @"麦克风已开",
            @"micOff": @"麦克风已关",
            @"hangup": @"挂断",
            @"speakerOn": @"扬声器已开",
            @"speakerOff": @"扬声器已关",
            // Màn đổ chuông callee (SDK chỉ có en/zh-Hans → máy tiếng Việt bị rơi về English).
            @"incomingSubtitle": @"邀请您语音通话…",
            @"accept": @"接听",
            @"reject": @"拒绝",
        };
    });
    return defaults[key] ?: @"";
}

@end
