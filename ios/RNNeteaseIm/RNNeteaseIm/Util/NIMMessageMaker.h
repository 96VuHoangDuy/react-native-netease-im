//
//  NIMMessageMaker.h
//  NIMKit
//
//  Created by chris.
//  Copyright (c) 2015年 NetEase. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ImConfig.h"

@class NIMKitLocationPoint;

@interface NIMMessageMaker : NSObject

+ (NIMMessage*)msgWithText:(NSString*)text andApnsMembers:(NSArray *)members andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage *)msgWithAudio:(NSString *)filePath andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage *)msgWithImage:(UIImage *)image andeSession:(NIMSession *)session isHighQuality:(BOOL *)isHighQuality senderName:(NSString *)senderName;

+ (NIMMessage *)msgWithImagePath:(NSString *)path andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage *)msgWithVideo:(NSString *)filePath andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage *)msgWithLocation:(NIMKitLocationPoint*)locationPoint andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage*)msgWithCustom:(NIMObject *)attachment andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage*)msgWithCustomAttachment:(DWCustomAttachment *)attachment andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage *)msgWithFile:(NSString *)filePath fileName:(NSString *)fileName andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (void)setupMessagePushBody:(NIMMessage *)message andSession:(NIMSession *)session senderName:(NSString *)senderName;

@end
