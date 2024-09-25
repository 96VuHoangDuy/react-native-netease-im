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
#import "TeamViewController.h"

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

+(NIMMessage *)getAndCheckLastMessage:(NIMMessage *)lastMessage {
    if (lastMessage == nil) {
        return nil;
    }
    
    if (lastMessage.messageSubType != 8) {
        return lastMessage;
    }
    
    if (lastMessage.localExt != nil) {
        return nil;
    }
    
    NSDictionary *birthdayInfo = [lastMessage.localExt objectForKey:@"birthdayInfo"];
    if (birthdayInfo == nil || [birthdayInfo objectForKey:@"lastMessageId"] == nil) {
        return nil;
    }
    
    NSArray<NIMMessage *> *messages = [[NIMSDK sharedSDK].conversationManager messagesInSession:lastMessage.session messageIds:@[[birthdayInfo objectForKey:@"lastMessageId"]]];
    
    if (messages == nil || messages.count != 1) {
        return nil;
    }
    
    NIMMessage *message = messages.firstObject;
    if (message.messageSubType == 8) {
        return nil;
    }
    
    return message;
}

+(NIMMessage *) msgWithNotificationBirthday:(NIMMessage *)lastMessage memberContactId:(NSString *)memberContactId memberName:(NSString *)memberName {
    NIMMessage *message = [[NIMMessage alloc] init];
    
    NSString *msgType = @"text";
    NSMutableDictionary *localExt = [[NSMutableDictionary alloc] init];
    
    NIMMessage *_lastMessage = [self getAndCheckLastMessage:lastMessage];
    
    [localExt setObject:@(NO) forKey:@"isSentBirthday"];
    
    NSMutableDictionary *birthdayInfo = [[NSMutableDictionary alloc] init];
    
    [birthdayInfo setObject:@"" forKey:@"contentLasted"];
    
    if (memberContactId != nil) {
        [birthdayInfo setObject:memberContactId forKey:@"memberContactId"];
    }
    
    if (_lastMessage != nil) {
        NSDictionary *remoteExt = _lastMessage.remoteExt;
        msgType = [self getMsgType:_lastMessage];
        
        [birthdayInfo setObject:_lastMessage.messageId forKey:@"lastMessageId"];
        
        if (remoteExt != nil && [[remoteExt objectForKey:@"extendType"]  isEqual: @"TEAM_NOTIFICATION_MESSAGE"]) {
            [localExt setObject:remoteExt forKey:@"notificationExtend"];
        }
        
        if (_lastMessage.messageType == NIMMessageTypeNotification) {
            [localExt setObject:[[ConversationViewController initWithConversationViewController] setNotiTeamObj:_lastMessage] forKey:@"notificationExtend"];
        }
        
        if (_lastMessage.text != nil && ![_lastMessage.text isEqual:@""] && ![_lastMessage.text isEqual:@"(null)"]) {
            [birthdayInfo setObject:[[NIMViewController initWithController] messageContent:_lastMessage] forKey:@"contentLasted"];
        }
    }
    
    [birthdayInfo setObject:msgType forKey:@"msgType"];
    
    if (memberName != nil) {
        [birthdayInfo setObject:memberName forKey:@"memberName"];
    }
    
    [localExt setObject:birthdayInfo forKey:@"birthdayInfo"];
    
    message.text = @"";
    message.localExt = localExt;
    message.messageSubType = 8;
    
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

+ (NIMMessage*)msgWithFile:(NSString*)filePath fileName:(NSString *)fileName fileType:(NSString *)fileType andeSession:(NIMSession *)session senderName:(NSString *)senderName
{
    
    NIMFileObject *fileObject = [[NIMFileObject alloc] initWithSourcePath:filePath];
    NIMMessage *message = [[NIMMessage alloc] init];
    message.messageObject = fileObject;
    NSString *content = @"文件";
    message.text = fileName;
    message.apnsContent = content;
    
    NSDictionary *remoteExt= @{@"fileType": fileType};
    message.remoteExt = remoteExt;
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

+ (NIMMessage *)msgWithGif:(NSString *)url aspectRatio:(NSString *)aspectRatio andSession:(NIMSession *)session senderName:(NSString *)senderName {
    NIMMessage *message = [[NIMMessage alloc] init];
    message.text = @"[动图]";
    
    NSMutableDictionary *remoteExt = [[NSMutableDictionary alloc] init];
    [remoteExt setObject:@"gif" forKey:@"extendType"];
    [remoteExt setObject:url forKey:@"path"];
    [remoteExt setObject:aspectRatio forKey:@"aspectRatio"];
    
    message.remoteExt = remoteExt;
    message.apnsContent = @"[动图]";
    
    [self setupMessagePushBody:message andSession:session senderName:senderName];
    
    return message;
}

+ (NIMMessage *)msgWithCard:(NSString *)cardSessionId cardSessionType:(NSString *)cardSessionType cardSessionName:(NSString *)cardSessionName avatar:(NSString *)avatar andSession:(NIMSession *)session senderName:(NSString *)senderName {
    NIMMessage *message = [[NIMMessage alloc] init];
    message.text = @"[个人名片]";
    message.apnsContent = @"[个人名片]";
    
    NSMutableDictionary *remoteExt = [[NSMutableDictionary alloc] init];
    [remoteExt setObject:@"card" forKey:@"extendType"];
    [remoteExt setObject:cardSessionType forKey:@"type"];
    [remoteExt setObject:cardSessionName forKey:@"name"];
    [remoteExt setObject:avatar forKey:@"imgPath"];
    [remoteExt setObject:cardSessionId forKey:@"sessionId"];
    
    message.remoteExt = remoteExt;
    
    [self setupMessagePushBody:message andSession:session senderName:senderName];
    
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
    float newHeight = image.size.height;
    float newWidth = image.size.width;

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(newWidth, newHeight), NO, isHighQuality ? 1: 0.4);
    [image drawInRect:CGRectMake(0, 0, newWidth, newHeight)];
    UIImage *formattedImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    NIMImageObject *imageObject = [[NIMImageObject alloc] initWithImage:formattedImage];
    NIMImageOption *option  = [[NIMImageOption alloc] init];
    option.compressQuality  = isHighQuality ? 1: 0.4;
    option.format           = NIMImageFormatJPEG;
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

+ (NSDictionary *)getMetadata:(NIMMessage *)message session:(NIMSession *)session senderName:(NSString *)senderName {
    NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
    
    if (session.sessionType != NIMSessionTypeP2P) {
        [result setObject:senderName forKey:@"senderName"];
    }
    
    if (message.remoteExt != nil && [message.remoteExt objectForKey:@"extendType"] != nil) {
        NSString *extendType = [message.remoteExt objectForKey:@"extendType"];
        NSLog(@"remoteExt test => %@", extendType);
        
        if ([extendType isEqual:@"forwardMultipleText"]) {
            [result setObject:@"forward" forKey:@"messageType"];
        }
        
        if ([extendType isEqual:@"card"]) {
            [result setObject:@"card" forKey:@"messageType"];
        }
        
        if ([extendType isEqual:@"gif"]) {
            [result setObject:@"gif" forKey:@"messageType"];
        }
    }
    
    if (message.remoteExt == nil || (message.remoteExt != nil && [message.remoteExt objectForKey:@"extendType"] == nil)) {
        switch (message.messageType) {
            case NIMMessageTypeImage:
                [result setObject:@"image" forKey:@"messageType"];
                break;
            
            case NIMMessageTypeVideo:
                [result setObject:@"video" forKey:@"messageType"];
                break;
                
            case NIMMessageTypeFile:
                [result setObject:@"file" forKey:@"messageType"];
                break;
                
            case NIMMessageTypeAudio:
                [result setObject:@"audio" forKey:@"messageType"];
                break;
                
            case NIMMessageTypeLocation:
                [result setObject:@"location" forKey:@"messageType"];
                break;
                
            default:
                return nil;
        }
    }
    
    return result;
}

+ (void)setupMessagePushBody:(NIMMessage *)message andSession:(NIMSession *)session senderName:(NSString *)senderName{
    NSString *body = [[NSString alloc] initWithString:message.apnsContent];
    NSMutableDictionary *apsField = [[NSMutableDictionary alloc] init];
    [apsField setObject:[NSNumber numberWithBool:YES] forKey:@"mutable-content"];
    
    NSString *pattern = @"@\\[(.+?)\\]\\((.+?)\\)";
    NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:pattern options:NSRegularExpressionCaseInsensitive error:nil];
    
    if (regex != nil) {
        NSRange range = NSMakeRange(0, body.length);
        NSString *modifiedString = [regex stringByReplacingMatchesInString:body options:NSMatchingReportProgress range:range withTemplate:@"@$1"];
        body = modifiedString;
    }
    
    NSDictionary *metadata = [self getMetadata:message session:session senderName:senderName];
    if (metadata != nil) {
        [apsField setObject:metadata forKey:@"metadata"];
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
            NSMutableDictionary *alert = [[NSMutableDictionary alloc] init];
            [alert setObject:senderName forKey:@"title"];
            [alert setObject:body forKey:@"body"];
            [apsField setObject:alert forKey:@"alert"];
            
            [payload setObject:apsField forKey:@"apsField"];
        } else {
            NIMTeam *team = [[NIMSDK sharedSDK].teamManager teamById:session.sessionId];
            
            NSString *teamName = team.teamName;
            if (teamName == nil || [teamName isEqual:@"TEAM_NAME_DEFAULT"]) {
                NSString *teamNameDefault = [[TeamViewController initWithTeamViewController] getTeamNameDefault:team.teamId];
                if (teamNameDefault != nil) {
                    teamName = teamNameDefault;
                } else {
                    teamName = @"群聊";
                }
            }
            
            NSMutableDictionary *alert = [[NSMutableDictionary alloc] init];
            [alert setObject:teamName forKey:@"title"];
            [alert setObject:[NSString stringWithFormat:@"%@: %@", senderName, body] forKey:@"body"];
            [apsField setObject:alert forKey:@"alert"];
            
            [payload setObject:apsField forKey:@"apsField"];
        }
    }
    
    message.apnsPayload = payload;
}

@end
