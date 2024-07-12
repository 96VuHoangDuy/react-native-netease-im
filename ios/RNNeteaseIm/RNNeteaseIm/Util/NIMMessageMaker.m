//
//  NIMMessageMaker.m
//  NIMKit
//
//  Created by chris.
//  Copyright (c) 2015年 NetEase. All rights reserved.
//

#import "NIMMessageMaker.h"
#import "NSString+NIMKit.h"
#import "NIMKitLocationPoint.h"
#import "ConversationViewController.h"
#import "NIMViewController.h"

@implementation NIMMessageMaker

+(NIMMessage *)msgWithRemoveReaction:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId accId:(NSString *)accId {
    NIMMessage *message = [[NIMMessage alloc] init];
    
    message.text = @"";
    message.messageSubType = 3;
    
    NSMutableDictionary *remoteExt = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *dataRemoveReaction = [[NSMutableDictionary alloc] init];
    [dataRemoveReaction setObject:sessionId forKey:@"sessionId"];
    [dataRemoveReaction setObject:sessionType forKey:@"sessionType"];
    [dataRemoveReaction setObject:messageId forKey:@"messageId"];
    [dataRemoveReaction setObject:accId forKey:@"accId"];
    [remoteExt setObject:dataRemoveReaction forKey:@"dataRemoveReaction"];
    
    message.remoteExt = remoteExt;
    
    NIMMessageSetting *setting = [[NIMMessageSetting alloc] init];
    setting.apnsEnabled = NO;
    setting.shouldBeCounted = NO;
    
    message.setting = setting;
    
    return message;
}

+(NIMMessage *)msgWithReaction:(NSString *)messageId reaction:(NSDictionary *)reaction {
    NIMMessage *message = [[NIMMessage alloc] init];
    message.text = messageId;

    NSMutableDictionary *removeExt = [[NSMutableDictionary alloc] init];
    [removeExt setObject:reaction forKey:@"reaction"];
    
    message.remoteExt = removeExt;
    message.messageSubType = 2;
    
    NIMMessageSetting *setting = [[NIMMessageSetting alloc] init];
    setting.apnsEnabled = NO;
    setting.shouldBeCounted = NO;
    
    message.setting = setting;
    
    return message;
}

+ (NIMMessage*)msgWithText:(NSString*)text andApnsMembers:(NSArray *)members andeSession:(NIMSession *)session senderName:(NSString *)senderName messageSubType:(NSInteger)messageSubType
{
    NIMMessage *message = [[NIMMessage alloc] init];
    message.text    = text;
    message.apnsContent = text;
    if (messageSubType != 0) {
        message.messageSubType = messageSubType;
    }
    
    if (members.count) {
        NIMMessageApnsMemberOption *apnsMemberOption = [[NIMMessageApnsMemberOption alloc]init];
        apnsMemberOption.userIds = members;
        apnsMemberOption.forcePush = YES;
        apnsMemberOption.apnsContent = @"有人@了你";
        message.apnsMemberOption = apnsMemberOption;
    }
    
    [NIMMessageMaker setupMessagePushBody:message andSession:session senderName:senderName];
    return message;
}

+(NSString *)getMsgType:(NIMMessage *)lastMessage {
    NSString *msgType = @"text";
    switch (lastMessage.messageType) {
        case NIMMessageTypeText:
        {
            NSDictionary *remoteExt = lastMessage.remoteExt;
            
            if (remoteExt != nil && [[remoteExt objectForKey:@"extendType"] isEqual:@"TEAM_NOTIFICATION_MESSAGE"]) {
                msgType = @"notification";
            }
            
            if (remoteExt != nil && [[remoteExt objectForKey:@"extendType"] isEqual:@"forwardMultipleText"]) {
                msgType = @"forwardMultipleText";
            }
            
            if (remoteExt != nil && [[remoteExt objectForKey:@"extendType"] isEqual:@"card"]) {
                msgType = @"card";
            }
            
            if (remoteExt != nil && [[remoteExt objectForKey:@"extendType"] isEqual:@"gif"]) {
                msgType = @"gif";
            }
            break;
        }
        case NIMMessageTypeImage:
            msgType = @"image";
            break;
        case NIMMessageTypeVideo:
            msgType = @"video";
            break;
        case NIMMessageTypeFile:
            msgType = @"file";
            break;
        case NIMMessageTypeAudio:
            msgType = @"voice";
            break;
        case NIMMessageTypeLocation:
            msgType = @"location";
            break;
        case NIMMessageTypeNotification:
            msgType = @"notification";
            break;
        default:
            msgType = @"unknown";
            break;
    }
    
    return msgType;
}

+(NIMMessage *) msgWithNotificationBirthday:(NIMMessage *)lastMessage memberContactId:(NSString *)memberContactId memberName:(NSString *)memberName {
    NIMMessage *message = [[NIMMessage alloc] init];
    
    NSString *msgType = @"text";
    NSMutableDictionary *localExt = [[NSMutableDictionary alloc] init];
    NSString *content = @"(NO_TEXT)";
    
    [localExt setObject:@"BIRTHDAY" forKey:@"notificationType"];
    [localExt setObject:@(NO) forKey:@"isSentBirthday"];
    
    if (memberContactId != nil) {
        [localExt setObject:memberContactId forKey:@"birthdayMemberContactId"];
    }
    
    if (lastMessage != nil) {
        NSDictionary *remoteExt = lastMessage.remoteExt;
        msgType = [self getMsgType:lastMessage];
        
        if (remoteExt != nil && [[remoteExt objectForKey:@"extendType"]  isEqual: @"TEAM_NOTIFICATION_MESSAGE"]) {
            [localExt setObject:remoteExt forKey:@"notificationExtend"];
        }
        
        if (lastMessage.messageType == NIMMessageTypeNotification) {
            [localExt setObject:[[ConversationViewController initWithConversationViewController] setNotiTeamObj:lastMessage] forKey:@"notificationExtend"];
        }
        
        if (lastMessage.text != nil && ![lastMessage.text isEqual:@""] && ![lastMessage.text isEqual:@"(null)"]) {
            content = [NSString stringWithFormat:@"(%@)", [[NIMViewController initWithController] messageContent:lastMessage]];
        }
    }
    
    if (memberName != nil) {
        [localExt setObject:memberName forKey:@"birthdayMemberName"];
    }
    
    NSString *text = [NSString stringWithFormat:@"NOTIFICATION_BIRTHDAY:%@:%@:[%@]", msgType, content,memberName];
    
    message.text = text;    
    message.localExt = localExt;
    
    return message;
}

+ (NIMMessage*)msgWithAudio:(NSString*)filePath andeSession:(NIMSession *)session senderName:(NSString *)senderName
{
    AVAudioPlayer * sound = [[AVAudioPlayer alloc] initWithContentsOfURL:[NSURL fileURLWithPath:filePath] error:nil];
    
    NIMAudioObject *audioObject = [[NIMAudioObject alloc] initWithSourcePath:filePath];
    NIMMessage *message = [[NIMMessage alloc] init];
    message.messageObject = audioObject;
    NSString *content = [NSString stringWithFormat:@"[语音] %.0fs", round(sound.duration)];
    message.text = content;
    message.apnsContent = content;
    [NIMMessageMaker setupMessagePushBody:message andSession:session senderName:senderName];
    return message;
}

+ (NIMMessage*)msgWithFile:(NSString*)filePath fileName:(NSString *)fileName andeSession:(NIMSession *)session senderName:(NSString *)senderName
{
    
    NIMFileObject *fileObject = [[NIMFileObject alloc] initWithSourcePath:filePath];
    NIMMessage *message = [[NIMMessage alloc] init];
    message.messageObject = fileObject;
    NSString *content = @"文件";
    message.text = fileName;
    message.apnsContent = content;
    [NIMMessageMaker setupMessagePushBody:message andSession:session senderName:senderName];
    return message;
}

+ (NIMMessage*)msgWithCustom:(NIMObject *)attachment andeSession:(NIMSession *)session senderName:(NSString *)senderName
{
    
    NIMMessage *message               = [[NIMMessage alloc] init];
    NIMCustomObject *customObject     = [[NIMCustomObject alloc] init];
    customObject.attachment           = attachment;
    message.messageObject             = customObject;
    message.apnsContent = @"发来了一条未知消息";
    [NIMMessageMaker setupMessagePushBody:message andSession:session senderName:senderName];
    return message;
}
+ (NIMMessage*)msgWithCustomAttachment:(DWCustomAttachment *)attachment andeSession:(NIMSession *)session senderName:(NSString *)senderName
{
    
    NIMMessage *message               = [[NIMMessage alloc] init];
    NIMCustomObject *customObject     = [[NIMCustomObject alloc] init];
    customObject.attachment           = attachment;
    message.messageObject             = customObject;
    NSString *text = @"";
    switch (attachment.custType) {
        case CustomMessgeTypeRedpacket:
            text = [NSString stringWithFormat:@"[红包]%@", [attachment.dataDict objectForKey:@"comments"]];
            break;
        case CustomMessgeTypeBankTransfer:
            text = [NSString stringWithFormat:@"[转账]%@", [attachment.dataDict objectForKey:@"comments"]];
            break;
        case CustomMessgeTypeUrl:
            text = [attachment.dataDict objectForKey:@"title"];
            break;
        case CustomMessgeTypeAccountNotice:
            text = [attachment.dataDict objectForKey:@"title"];
            break;
        case CustomMessgeTypeRedPacketOpenMessage:{
            text = @"";
            NIMMessageSetting *seting = [[NIMMessageSetting alloc]init];
            seting.apnsEnabled = NO;
            seting.shouldBeCounted = NO;
            message.setting = seting;
        }
            break;
        case CustomMessgeTypeBusinessCard: //名片
        {
            text = [NSString stringWithFormat:@"[名片]%@", [attachment.dataDict objectForKey:@"name"]];
        }
            break;
        case CustomMessgeTypeCustom: //自定义
        {
            text = [NSString stringWithFormat:@"%@", [attachment.dataDict objectForKey:@"pushContent"]];
        }
            break;
        default:
            text = @"发来了一条未知消息";
            break;
    }
    message.apnsContent = text;
    [NIMMessageMaker setupMessagePushBody:message andSession:session senderName:senderName];
    return message;
}

+ (NIMMessage*)msgWithVideo:(NSString*)filePath andeSession:(NIMSession *)session senderName:(NSString *)senderName
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm"];
    NSString *dateString = [dateFormatter stringFromDate:[NSDate date]];
    NIMVideoObject *videoObject = [[NIMVideoObject alloc] initWithSourcePath:filePath];
    videoObject.displayName = [NSString stringWithFormat:@"视频发送于%@",dateString];
    NIMMessage *message = [[NIMMessage alloc] init];
    message.messageObject = videoObject;
    NSString *content = @"[视频]";
    message.text = content;
    message.apnsContent = content;
//    message.apnsContent = @"发来了一段视频";
    [NIMMessageMaker setupMessagePushBody:message andSession:session senderName:senderName];
    return message;
}
+ (NIMMessage*)msgWithImage:(UIImage*)image andeSession:(NIMSession *)session isHighQuality:(BOOL *)isHighQuality senderName:(NSString *)senderName
{
    // to keep image not rotating
    UIGraphicsBeginImageContext(image.size);
    [image drawAtPoint:CGPointZero];
    UIImage *formattedImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    NIMImageObject *imageObject = [[NIMImageObject alloc] initWithImage:formattedImage];
    NIMImageOption *option  = [[NIMImageOption alloc] init];
    option.compressQuality  = isHighQuality ? 1: 0.7;
    option.format           = isHighQuality ? NIMImageFormatPNG : NIMImageFormatJPEG;
    imageObject.option      = option;
    return [NIMMessageMaker generateImageMessage:imageObject andeSession:session senderName:senderName];
}

+ (NIMMessage *)msgWithImagePath:(NSString*)path andeSession:(NIMSession *)session senderName:(NSString *)senderName
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm"];
    NSString *dateString = [dateFormatter stringFromDate:[NSDate date]];
    NIMImageObject * imageObject = [[NIMImageObject alloc] initWithFilepath:path];
    imageObject.displayName = [NSString stringWithFormat:@"图片发送于%@",dateString];
    NIMMessage *message     = [[NIMMessage alloc] init];
    message.messageObject   = imageObject;
    message.apnsContent = @"发来了一张图片";
    return [NIMMessageMaker generateImageMessage:imageObject andeSession:session senderName:senderName];
}

+ (NIMMessage *)generateImageMessage:(NIMImageObject *)imageObject andeSession:(NIMSession *)session senderName:(NSString *)senderName
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm"];
    NSString *dateString = [dateFormatter stringFromDate:[NSDate date]];
    imageObject.displayName = [NSString stringWithFormat:@"图片发送于%@",dateString];
    NIMMessage *message     = [[NIMMessage alloc] init];
    message.messageObject   = imageObject;
    NSString *content = @"[图片]";
    message.text = content;
    message.apnsContent = content;
    [NIMMessageMaker setupMessagePushBody:message andSession:session senderName:senderName];
    return message;
}


+ (NIMMessage*)msgWithLocation:(NIMKitLocationPoint *)locationPoint andeSession:(NIMSession *)session senderName:(NSString *)senderName{
    NIMLocationObject *locationObject = [[NIMLocationObject alloc] initWithLatitude:locationPoint.coordinate.latitude
                                                                          longitude:locationPoint.coordinate.longitude
                                                                              title:locationPoint.title];
    NIMMessage *message               = [[NIMMessage alloc] init];
    message.messageObject             = locationObject;
    message.apnsContent = @"发来了一条位置信息";
    [NIMMessageMaker setupMessagePushBody:message andSession:session senderName:senderName];
    return message;
}

+ (void)setupMessagePushBody:(NIMMessage *)message andSession:(NIMSession *)session senderName:(NSString *)senderName{
    NSLog(@"messagemessage => %@", message);
    NSString *pattern = @"@\\[(.+?)\\]\\((.+?)\\)";
    NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:pattern options:NSRegularExpressionCaseInsensitive error:nil];
    NSString *body = [[NSString alloc] initWithString:message.apnsContent];
    
    if (regex != nil) {
        NSRange range = NSMakeRange(0, body.length);
        NSString *modifiedString = [regex stringByReplacingMatchesInString:body options:NSMatchingReportProgress range:range withTemplate:@"@$1"];
        body = modifiedString;
    }
    
    NSMutableDictionary *payload = [NSMutableDictionary dictionary];
//    NSMutableDictionary *apsField = [NSMutableDictionary dictionary];
    NSString *strSessionID = @"";
    if (session.sessionType == NIMSessionTypeP2P) {//点对点
        strSessionID = [NIMSDK sharedSDK].loginManager.currentAccount;
    }else{
        strSessionID = [NSString stringWithFormat:@"%@",session.sessionId];
    }
    NSString *strSessionType = [NSString stringWithFormat:@"%zd",session.sessionType];
    [payload setObject:@{@"sessionId":strSessionID,@"sessionType":strSessionType} forKey:@"sessionBody"];
    
   if ([senderName length]) {
        if (session.sessionType == NIMSessionTypeP2P) {
            [payload setObject:@{@"alert": @{@"title": senderName, @"body": body}} forKey:@"apsField"];
        } else {
            NIMTeam *team = [[NIMSDK sharedSDK].teamManager teamById:session.sessionId];
            
            [payload setObject:@{@"alert": @{@"title": team.teamName, @"body": [NSString stringWithFormat:@"%@: %@", senderName, body]}} forKey:@"apsField"];
        }
    }
    
    
    
    message.apnsPayload = payload;
}

@end
