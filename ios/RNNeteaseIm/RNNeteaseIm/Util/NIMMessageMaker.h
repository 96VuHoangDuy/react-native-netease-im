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

+ (NIMMessage *)msgWithCard:(NSString *)cardSessionId cardSessionType:(NSString *)cardSessionType cardSessionName:(NSString *)cardSessionName avatar:(NSString *)avatar andSession:(NIMSession *)session senderName:(NSString *)senderName;

+(NIMMessage *)msgWithRemoveReaction:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId accId:(NSString *)accId;

+(NIMMessage *)msgWithReaction:(NSString *)messageId reaction:(NSDictionary *)reaction;

+ (NIMMessage *)msgWithNotificationBirthday:(NIMMessage *)lastMessage memberContactId:(NSString *)memberContactId memberName:(NSString *)memberName;

+ (NIMMessage*)msgWithText:(NSString*)text andApnsMembers:(NSArray *)members andeSession:(NIMSession *)session senderName:(NSString *)senderName messageSubType:(NSInteger)messageSubType;

+ (NIMMessage *)msgWithAudio:(NSString *)filePath andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage *)msgWithGif:(NSString *)url aspectRatio:(NSString *)aspectRatio andSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage *)msgWithImage:(UIImage *)image andeSession:(NIMSession *)session isHighQuality:(BOOL *)isHighQuality senderName:(NSString *)senderName;

+ (NIMMessage *)msgWithImagePath:(NSString *)path andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage *)msgWithVideo:(NSString *)filePath andeSession:(NIMSession *)session senderName:(NSString *)senderName duration:(NSString *)duration;

+ (NIMMessage *)msgWithLocation:(NIMKitLocationPoint*)locationPoint andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage*)msgWithCustom:(NIMObject *)attachment andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage*)msgWithCustomAttachment:(DWCustomAttachment *)attachment andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (NIMMessage *)msgWithFile:(NSString *)filePath fileName:(NSString *)fileName fileType:(NSString *)fileType andeSession:(NIMSession *)session senderName:(NSString *)senderName;

+ (void)setupMessagePushBody:(NIMMessage *)message andSession:(NIMSession *)session senderName:(NSString *)senderName;

@end
