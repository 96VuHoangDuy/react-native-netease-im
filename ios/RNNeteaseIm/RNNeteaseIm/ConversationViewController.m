//
//  ConversationViewController.m
//  NIM
//
//  Created by Dowin on 2017/5/5.
//  Copyright © 2017年 Dowin. All rights reserved.
//

#import "ConversationViewController.h"
#import <Photos/PhotosTypes.h>
#import "NIMMessageMaker.h"
#import "ContactViewController.h"
#import "NIMKitLocationPoint.h"
#import <AVFoundation/AVFoundation.h>
//#import "NIMKitMediaFetcher.h"

#define NTESNotifyID        @"id"
#define NTESCustomContent  @"content"

#define NTESCommandTyping  (1)
#define NTESCustom         (2)
#import "NSDictionary+NTESJson.h"
@interface ConversationViewController ()<NIMMediaManagerDelegate,NIMMediaManagerDelegate,NIMSystemNotificationManagerDelegate>{
    NSString *_sessionID;
    NSString *_myUserName;
    NSString *_myUserID;
    NSString *_type;
    NSInteger _index;
    
    NSMutableArray *_sessionArr;
    
}
@property (nonatomic,strong) AVAudioPlayer *player; //播放提示音
@property (nonatomic,strong) AVAudioPlayer *redPacketPlayer; //播放提示音
@property (nonatomic,strong) NIMSession *_session;
//@property (nonatomic,strong) NIMKitMediaFetcher *mediaFetcher;
@property (nonatomic) BOOL *isSeenMessage;

@end

@implementation ConversationViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
}


+(instancetype)initWithConversationViewController{
    static ConversationViewController *conVC = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        conVC = [[ConversationViewController alloc]init];
        
    });
    return conVC;
}

- (instancetype)init {
    self = [super init];
    if(self) {
        NSURL *url = [[NSBundle mainBundle] URLForResource:@"message" withExtension:@"wav"];
        _player = [[AVAudioPlayer alloc] initWithContentsOfURL:url error:nil];
        _player.volume = 1.0;
        NSURL *redPackUrl = [[NSBundle mainBundle] URLForResource:@"packet_tip" withExtension:@"wav"];
        _redPacketPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:redPackUrl error:nil];
        _redPacketPlayer.volume = 1.0;
    }
    return self;
}

//- (NIMKitMediaFetcher *)mediaFetcher
//{
//    if (!_mediaFetcher) {
//        _mediaFetcher = [[NIMKitMediaFetcher alloc] init];
//    }
//    return _mediaFetcher;
//}

-(void)updateIsSeenMessage:(BOOL *)isSeenMessage {
    _isSeenMessage = isSeenMessage;
}


-(void)startSession:(NSString *)sessionID withType:(NSString *)type myUserName:(NSString *)myUserName myUserID:(NSString *)myUserID{
    _sessionID = sessionID;
    _type = type;
    _myUserName = [myUserName length] ? myUserName : @"";
    _myUserID = [myUserID length] ? myUserID : @"";
    self._session = [NIMSession session:_sessionID type:[_type integerValue]];
    _sessionArr = [NSMutableArray array];
    [self addListener];
}
//本地历史记录
-(void)localSessionList:(NSString *)sessionId sessionType:(NSString *)sessionType timeLong:(NSString *)timeLong direction:(NSString *)direction limit:(NSString *)limit asc:(BOOL)asc success:(Success)succe{
    // NIMMessageSearchOption *option = [[NIMMessageSearchOption alloc]init];
}

-(void)updateMessageSentStickerBirthday:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId success:(Success)success err:(Errors)err {
    NIMSession *session = [NIMSession session:sessionId type:[sessionType integerValue]];
    NSArray *messages = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:session messageIds:@[messageId]];
    NIMMessage *message = messages[0];
    
    NSMutableDictionary *localExt = message.localExt ? [message.localExt mutableCopy] : [[NSMutableDictionary alloc] init];
    
    [localExt setObject:@(YES) forKey:@"isSentBirthday"];
    
    message.localExt = localExt;
    
    [[NIMSDK sharedSDK].conversationManager updateMessage:message forSession:session completion:^(NSError * _Nullable error) {
        if (error != nil) {
            err(error);
            return;
        }
        
        success(@"success");
    }];
}

//重发消息
- (void)resendMessage:(NSString *)messageID success:(Success)succe err:(Errors)err{
    NSArray *currentMessage = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:self._session messageIds:@[messageID] ];
    NIMMessage *currentM = currentMessage[0];
//    NSString *isFriend = [currentM.localExt objectForKey:@"isFriend"];
    
    if (self._session.sessionType == NIMSessionTypeP2P && ![self isFriendToSendMessage:currentM]) {
        return;
    }
    
    if (currentM.isReceivedMsg) {
        [[[NIMSDK sharedSDK] chatManager] fetchMessageAttachment:currentM error:nil];
        return;
    }
    
    NSError *error;
    [[[NIMSDK sharedSDK] chatManager] resendMessage:currentM error:&error];
    
    if (error != nil) {
        NSLog(@"resendMessage =>>> %@", error);
        err(error);
    } else {
        succe(@"200");
    }
}

-(void)readAllMessageOnlineServiceByListSession:(NSArray *)listSessionId {
    NSLog(@"listSessionId => %@", listSessionId);
    for(int i = 0; i < [listSessionId count]; i++) {
        NSString *sessionId = [listSessionId objectAtIndex:i];
        NIMSession *session = [NIMSession session:sessionId type:NIMSessionTypeP2P];
        if (session != nil) {
            [[NIMSDK sharedSDK].conversationManager markAllMessagesReadInSession:session completion:^(NSError * __nullable error){
                if (error != nil) {
                    NSLog(@"readAllMessageOnlineServiceByListSession error: %@", error);
                }
            }];
        }
    }
}

-(NSDictionary *)updateLastReadMessageId:(NIMSession *)session {
    NIMRecentSession *recent = [[NIMSDK sharedSDK].conversationManager recentSessionBySession:session];
    
    if (recent != nil) {
        NIMMessage *lastMessage = recent.lastMessage;
        NSDictionary *localExt = recent.localExt?:@{};
        NSString *messageId = [localExt objectForKey:@"lastReadMessageId"];
        NSInteger unreadCount = recent.unreadCount;
        NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
        
        if (messageId != nil && [messageId isEqual:lastMessage.messageId]) {
            return nil;
        }
        
        if (messageId != nil) {
            [result setObject:messageId forKey:@"lastMessageId"];
        }
        [result setObject:@(unreadCount) forKey:@"unreadCount"];
        
        NSMutableDictionary *dict = [localExt mutableCopy];
        
        [dict setObject:lastMessage.messageId forKey:@"lastReadMessageId"];
        [[NIMSDK sharedSDK].conversationManager updateRecentLocalExt:dict recentSession:recent];
        
        return result;
    }
    
    return nil;
}

//聊天界面历史记录
- (void)localSession:(NSInteger)limit currentMessageID:(NSString *)currentMessageID direction:(int)direction sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType success:(Success)succe err:(Errors)err{
    NIMSession *session = [sessionId length] && [sessionType length] ? [NIMSession session:sessionId type:[sessionType integerValue]] : self._session;
    NSDictionary *data;
    if (currentMessageID.length == 0) {
        data = [self updateLastReadMessageId:session];
    }
    

    [[NIMSDK sharedSDK].conversationManager markAllMessagesReadInSession:self._session];
    
    NIMGetMessagesDynamicallyParam *param = [[NIMGetMessagesDynamicallyParam alloc] init];
    
    param.session = session;
    param.limit = limit;
  
    if (currentMessageID.length != 0) {
        NSArray *currentMessage = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:session messageIds:@[currentMessageID] ];
        NIMMessage *currentM = currentMessage[0];

        param.anchorClientId = currentMessageID;
        
        param.startTime = direction == 1 ? currentM.timestamp : 0;
        param.endTime = direction == 0 ? currentM.timestamp : 0;
    }
    param.order = direction == 1 ? NIMMessageSearchOrderAsc : NIMMessageSearchOrderDesc;

        [[[NIMSDK sharedSDK] conversationManager]getMessagesDynamically:param completion:^(NSError * _Nullable error, BOOL isReliable, NSArray<NIMMessage *> * _Nullable messageArr) {
            if (error) {
                err(@"暂无更多");
            } else {
                NIMMessage *lastMessage = direction == 0 ? [messageArr firstObject] : [messageArr lastObject];
                NIMMessageReceipt *receipt = [[NIMMessageReceipt alloc] initWithMessage:lastMessage];

                if ([self isSeenMessage]) {
                    if (lastMessage.session.sessionType == NIMSessionTypeTeam) {
                       [[[NIMSDK sharedSDK] chatManager] sendTeamMessageReceipts:@[receipt] completion:nil];
                    } else {
                       [[[NIMSDK sharedSDK] chatManager] sendMessageReceipt:receipt completion:nil];
                    }
                }
                
                if (currentMessageID.length == 0 && [self setTimeArr:messageArr].count != 0) {
                    NSMutableDictionary *dic = [[self setTimeArr:messageArr] objectAtIndex:[self setTimeArr:messageArr].count - 1];
                    [[NSUserDefaults standardUserDefaults]setObject:[dic objectForKey:@"time"] forKey:@"timestamp"];
                }
                
                NSArray *messages = [self setTimeArr:messageArr];
                
                if (currentMessageID.length > 0) {
                    succe(messages);
                } else {
                    NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
                    if (data != nil) {
                        [result setObject:data forKey:@"data"];
                    }
                    
                    [result setObject:messages forKey:@"messages"];
                    
                    succe(result);
                }
            }
        }];
//    }
}
//更新录音消息为已播放
- (void)updateAudioMessagePlayStatus:(NSString *)messageID{
    NSArray *messages = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:self._session messageIds:@[messageID] ];
    if (messages.count) {
        NIMMessage *tmpMessage = messages.firstObject;
        tmpMessage.isPlayed = YES;
    }
}

//- (void) queryUnreadMessagesInSession:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId {
//    NIMSession *session = [NIMSession session:sessionId type:[sessionType integerValue]];
//
//    NSArray *messageArr = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:session messageIds:@[messageId]];
//    NIMMessage *message = messageArr.firstObject;
//}

- (void) getMessageById:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId success:(Success)success {
    NIMSession *session = [NIMSession session:sessionId type:[sessionType integerValue]];
    NSArray *messageArr = [self setTimeArr:[[[NIMSDK sharedSDK] conversationManager] messagesInSession:session messageIds:@[messageId]]];
    NIMMessage *message = messageArr.firstObject;
    success(message);
}

-(void) searchTextMessages:(NSString *)searchContent success:(Success)success err:(Errors)err {
    NIMMessageSearchOption *option = [[NIMMessageSearchOption alloc] init];
    option.searchContent = searchContent;
    option.messageTypes = @[[NSNumber numberWithInt:NIMMessageTypeText]];
    option.order = NIMMessageSearchOrderDesc;
    
    [[NIMSDK sharedSDK].conversationManager searchAllMessages:option result:^(NSError * _Nullable error, NSDictionary<NIMSession *,NSArray<NIMMessage *> *> * _Nullable messages) {
        NSMutableDictionary *dict = [NSMutableDictionary dictionary];

        if (!error) {
           for (NIMSession* key in messages) {
                id value = [messages objectForKey:key];
                
                [dict setValue:[self setTimeArr:value] forKey:key.sessionId];
            }
            success(dict);
        } else {
            err(error);
        }
    }];
}

-(void) searchFileMessages:(Success)success err:(Errors)err {
    NIMMessageSearchOption *option = [[NIMMessageSearchOption alloc] init];
    option.searchContent = @"";
    option.messageTypes = @[[NSNumber numberWithInt:NIMMessageTypeFile]];
    option.order = NIMMessageSearchOrderDesc;
    
    [[NIMSDK sharedSDK].conversationManager searchAllMessages:option result:^(NSError * _Nullable error, NSDictionary<NIMSession *,NSArray<NIMMessage *> *> * _Nullable messages) {
        NSMutableDictionary *dict = [NSMutableDictionary dictionary];

        if (!error) {
           for (NIMSession* key in messages) {
                id value = [messages objectForKey:key];
                
                [dict setValue:[self setTimeArr:value] forKey:key.sessionId];
            }
            success(dict);
        } else {
            err(error);
        }
    }];
}

//search local Messages
- (void)searchMessages:(NSString *)keyWords success:(Success)succe err:(Errors)err{
    NIMMessageSearchOption *option = [[NIMMessageSearchOption alloc] init];
    option.limit = 100;
    option.searchContent = keyWords;
    option.allMessageTypes = YES;

    [[NIMSDK sharedSDK].conversationManager searchAllMessages:option result:^(NSError * _Nullable error, NSDictionary<NIMSession *,NSArray<NIMMessage *> *> * _Nullable messages) {
        NSMutableDictionary *dict = [NSMutableDictionary dictionary];

        if (!error) {
           for (NIMSession* key in messages) {
                id value = [messages objectForKey:key];
                
                [dict setValue:[self setTimeArr:value] forKey:key.sessionId];
            }
            succe(dict);
            NSLog(@"searchAllMessages: %@]", dict);
        } else {
            err(error);
        }
    }];
}


//search local Messages
- (void)searchMessagesinCurrentSession:(NSString *)keyWords anchorId:(NSString *)anchorId limit:(int)limit messageType:(NSArray *)messageType direction:(int)direction messageSubTypes:(NSArray *)messageSubTypes  success:(Success)succe err:(Errors)err{
    NIMMessageSearchOption *option = [[NIMMessageSearchOption alloc] init];
    option.limit = limit;
    if (keyWords.length != 0) {
        option.searchContent = keyWords;
    }
    if (messageSubTypes != nil) {
        option.messageSubTypes = messageSubTypes;
    }
    
    option.order = direction == 1 ? NIMMessageSearchOrderAsc : NIMMessageSearchOrderDesc;
    
    if (messageType.count != 0) {
        const NSDictionary* keysMessageType = @{
          @"text": @(NIMMessageTypeText),
          @"voice": @(NIMMessageTypeAudio),
          @"image": @(NIMMessageTypeImage),
          @"video": @(NIMMessageTypeVideo),
          @"file": @(NIMMessageTypeFile),
        };
        
        NSMutableArray * messageTypeOptions = [[NSMutableArray alloc] init];
        
        for (NSString *_messageKey in messageType) {
            [messageTypeOptions addObject:[keysMessageType objectForKey:_messageKey]];
        }
        
        option.messageTypes = messageTypeOptions;
    }

    
    
    if (anchorId.length != 0) {
        NSArray *currentMessage = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:self._session messageIds:@[anchorId] ];
        NIMMessage *currentM = currentMessage[0];
        
        option.startTime = direction == 1 ? currentM.timestamp : 0;
        option.endTime = direction == 0 ? currentM.timestamp : 0;
    }
    

    NSLog(@"searchAllMessages option: %@]", option);

    [[NIMSDK sharedSDK].conversationManager searchMessages:self._session option:option result:^(NSError * _Nullable error, NSArray<NIMMessage *> * __nullable messages) {
        NSLog(@"searchAllMessages messages: %@]", messages);

        NSMutableDictionary *dict = [NSMutableDictionary dictionary];
        [dict setValue:[self setTimeArr:messages] forKey:self._session.sessionId];

        if (!error) {
            succe(dict);
        } else {
            err(error);
        }
    }];
}

- (NSNumber *) getTypeOpretationType:(NIMTeamOperationType) operationType {
    NSNumber *result = @-1;

    switch(operationType) {
        case NIMTeamOperationTypeInvite:
            result = @0;
            break;
        case NIMTeamOperationTypeKick:
            result = @1;
            break;
        case NIMTeamOperationTypeLeave:
            result = @2;
            break;
        case NIMTeamOperationTypeUpdate:
            result = @3;
            break;
        case NIMTeamOperationTypeDismiss:
            result = @4;
            break;
        case NIMTeamOperationTypeApplyPass:
            result = @5;
            break;
        case NIMTeamOperationTypeTransferOwner:
            result = @6;
            break;
        case NIMTeamOperationTypeAddManager:
            result = @7;
            break;
        case NIMTeamOperationTypeRemoveManager:
            result = @8;
            break;
        case NIMTeamOperationTypeAcceptInvitation:
            result = @9;
            break;
        case NIMTeamOperationTypeMute:
            result = @10;
            break;
        default:
            break;
    }
    return result;
}

- (NSDictionary *)teamNotificationSourceName:(NIMMessage *)message{
    NIMNotificationObject *object = message.messageObject;
    NIMTeamNotificationContent *content = (NIMTeamNotificationContent*)object.content;
//    NSString *currentAccount = [[NIMSDK sharedSDK].loginManager currentAccount];
//    if ([content.sourceID isEqualToString:currentAccount]) {
//        source = @"你";
//    }else{
    const NSString *sourceName = [NIMKitUtil showNick:content.sourceID inSession:message.session];
//    }
    const NSDictionary *source = @{@"sourceName": sourceName, @"sourceId":content.sourceID};
    
    return source;
}

- (NSArray *)teamNotificationTargetNames:(NIMMessage *)message{
    NSMutableArray *targets = [[NSMutableArray alloc] init];
    NIMNotificationObject *object = message.messageObject;
    NIMTeamNotificationContent *content = (NIMTeamNotificationContent*)object.content;
//    NSString *currentAccount = [[NIMSDK sharedSDK].loginManager currentAccount];
    for (NSString *item in content.targetIDs) {
//        if ([item isEqualToString:currentAccount]) {
//            [targets addObject:@"你"];
//        }else{
        NSString *targetShowName = [NIMKitUtil showNick:item inSession:message.session];
        const NSDictionary *target = @{@"targetName":targetShowName, @"targetId":item};
        [targets addObject:target];
//        }
    }
    return targets;
}


- (NSMutableDictionary *)setNotiTeamObj:(NIMMessage *)message {
    NSMutableDictionary *notiObj = [NSMutableDictionary dictionary];
    NIMNotificationObject *messageObject = message.messageObject;
    NIMTeamNotificationContent *content = (NIMTeamNotificationContent*)messageObject.content;
    NSMutableArray *targets = [[NSMutableArray alloc] init];
    for (NSString *item in content.targetIDs) {
        [targets addObject:item];
    }
    
    
    switch (messageObject.notificationType) {
        case NIMNotificationTypeTeam:
        case NIMNotificationTypeChatroom:
        {
            NSNumber *operationtype = [self getTypeOpretationType:content.operationType];
            [notiObj setObject:[self teamNotificationSourceName:message] forKey:@"sourceId"];
            [notiObj setObject:[self teamNotificationTargetNames:message] forKey:@"targets"];

            if ([operationtype isEqualToNumber:@10]) {
                id attachment = [content attachment];
                if ([attachment isKindOfClass:[NIMMuteTeamMemberAttachment class]]) {
                    BOOL mute = [(NIMMuteTeamMemberAttachment *)attachment flag];
                    NSString *muteStr = mute? @"mute" : @"unmute";
                    [notiObj setObject:muteStr  forKey:@"isMute"];
                }
            }
            [notiObj setObject:operationtype  forKey:@"operationType"];
            
            if ([content.notifyExt isEqual:@"from_request"]) {
                [notiObj setObject:@12  forKey:@"operationType"];
            }
            
            if (content.operationType == NIMTeamOperationTypeUpdate) {
                id attachment = [content attachment];
                if ([attachment isKindOfClass:[NIMUpdateTeamInfoAttachment class]]) {
                    NIMUpdateTeamInfoAttachment *teamAttachment = (NIMUpdateTeamInfoAttachment *)attachment;
                    
                    for (id key in teamAttachment.values) {
                        NSLog(@"key: %@, value: %@ \n", key, [teamAttachment.values objectForKey:key]);
                    }
                    
                    if ([teamAttachment.values count] == 1) {
                        const NSDictionary* keys = @{
                          @(NIMTeamUpdateTagName): @"NIMTeamUpdateTagName",
                          @(NIMTeamUpdateTagIntro): @"NIMTeamUpdateTagIntro",
                          @(NIMTeamUpdateTagAnouncement): @"NIMTeamUpdateTagAnouncement",
                          @(NIMTeamUpdateTagJoinMode): @"NIMTeamUpdateTagJoinMode",
                          @(NIMTeamUpdateTagAvatar): @"NIMTeamUpdateTagAvatar",
                          @(NIMTeamUpdateTagInviteMode): @"NIMTeamUpdateTagInviteMode",
                          @(NIMTeamUpdateTagBeInviteMode): @"NIMTeamUpdateTagBeInviteMode",
                          @(NIMTeamUpdateTagUpdateInfoMode): @"NIMTeamUpdateTagUpdateInfoMode",
                          @(NIMTeamUpdateTagMuteMode): @"NIMTeamUpdateTagMuteMode",
                        };
                        
                        NSDictionary *mapDict = [[NSMutableDictionary alloc] init];

                        for (id key in teamAttachment.values) {
                            NSLog(@"keyzzz: %@, value: %@ \n", key, [teamAttachment.values objectForKey:key]);

                            NSNumber *keyId = [keys objectForKey: key];
                            NSString *value = [teamAttachment.values objectForKey:key];
                            mapDict = @{@"type": keyId, @"value": value};
                        }
                        
                        NSLog(@"Testtt %@", mapDict);

                        [notiObj setObject:mapDict  forKey:@"updateDetail"];
                    }
                }
            }
           
            break;
        }
        case NIMNotificationTypeNetCall:{
            [notiObj setObject:[NIMKitUtil messageTipContent:message] forKey:@"tipMsg"];
            break;
        }
        default:
            break;
    }
    
    return notiObj;
}

-(NSDictionary *) makeExtendImage:(NIMMessage *)message {
    NIMImageObject *object = message.messageObject;
    NSMutableDictionary *imgObj = [NSMutableDictionary dictionary];
    [imgObj setObject:[NSString stringWithFormat:@"%@",[object url] ] forKey:@"url"];
    [imgObj setObject:[NSString stringWithFormat:@"%@",[object displayName] ] forKey:@"displayName"];
    [imgObj setObject:[NSString stringWithFormat:@"%f",[object size].height] forKey:@"imageHeight"];
    [imgObj setObject:[NSString stringWithFormat:@"%f",[object size].width] forKey:@"imageWidth"];
    
    NSString *mediaPath = [self moveFiletoSessionDir:message isThumb:nil];
    NSString *mediaCoverPath = [self moveFiletoSessionDir:message isThumb:@1];
    NSString *isReplaceSuccess = [message.localExt objectForKey:@"isReplaceSuccess"];
    NSString *downloadAttStatus = [message.localExt objectForKey:@"downloadAttStatus"];
    
    if ([message.remoteExt objectForKey:@"parentId"] != nil) {
        [imgObj setObject:[message.remoteExt objectForKey:@"parentId"] forKey:@"parentId"];
    }
    
    if ([message.remoteExt objectForKey:@"indexCount"] != nil) {
        [imgObj setObject:[message.remoteExt objectForKey:@"indexCount"] forKey:@"indexCount"];
    }

    if ([downloadAttStatus length]) {
        [imgObj setObject:downloadAttStatus forKey:@"downloadAttStatus"];
    }
    if ([isReplaceSuccess length]) {
        [imgObj setObject:isReplaceSuccess forKey:@"isReplaceSuccess"];
    }
    
    if (mediaPath != nil) {
        if (message.localExt != nil && [isReplaceSuccess length] && [isReplaceSuccess isEqual:@"YES"] && ![[NSFileManager defaultManager] fileExistsAtPath:mediaPath] && ([downloadAttStatus length] && [downloadAttStatus isEqual:@"downloadSuccess"]) ){
            [imgObj setObject:[NSNumber numberWithBool: true] forKey:@"isFilePathDeleted"];
        } else {
            [imgObj setObject:[NSString stringWithFormat:@"%@",mediaPath] forKey:@"path"];
        }
    } else if ([downloadAttStatus length] && [downloadAttStatus isEqual:@"downloading"]) {
        [imgObj setObject:@true forKey:@"isFileDownloading"];
    }
    
    if (mediaCoverPath != nil) {
        [imgObj setObject:[NSString stringWithFormat:@"%@",mediaCoverPath] forKey:@"coverPath"];
    }
    
    if (message.deliveryState == NIMMessageDeliveryStateDeliveried  && [isReplaceSuccess length] && [isReplaceSuccess isEqual:@"YES"] && ([downloadAttStatus length] && [downloadAttStatus isEqual:@"downloadSuccess"])) {
        if ([[NSFileManager defaultManager] fileExistsAtPath:object.path]){
            NSError *removeItemError = nil;
            if (![[NSFileManager defaultManager] removeItemAtPath:object.path error:&removeItemError]) {
                NSLog(@"[removeItemError description]: %@", [removeItemError description]);
            }
        }
    }
    
    return imgObj;
}

-(NSDictionary *) makeExtendFile:(NIMMessage *)message {
    NIMFileObject *object = message.messageObject;
    NSString *displayFileSize = [NSByteCountFormatter stringFromByteCount:object.fileLength countStyle:NSByteCountFormatterCountStyleFile];
    
    NSMutableDictionary *fileObj = [NSMutableDictionary dictionary];
    [fileObj setObject:[NSString stringWithFormat:@"%@", object.path ] forKey:@"filePath"];
    [fileObj setObject:[NSString stringWithFormat:@"%@", message.text ] forKey:@"fileName"];
    [fileObj setObject:[NSString stringWithFormat:@"%@", displayFileSize ] forKey:@"fileSize"];
    [fileObj setObject:[NSString stringWithFormat:@"%@", object.md5 ] forKey:@"fileMd5"];
    [fileObj setObject:[NSString stringWithFormat:@"%@", object.url ] forKey:@"fileUrl"];
    
    NSString *mediaPath = [self moveFiletoSessionDir:message isThumb:nil];
    NSString *isReplaceSuccess = [message.localExt objectForKey:@"isReplaceSuccess"];
    NSString *downloadAttStatus = [message.localExt objectForKey:@"downloadAttStatus"];
    if ([downloadAttStatus length]) {
        [fileObj setObject:downloadAttStatus forKey:@"downloadAttStatus"];
    }
    if ([isReplaceSuccess length]) {
        [fileObj setObject:isReplaceSuccess forKey:@"isReplaceSuccess"];
    }
    
    if (mediaPath != nil) {
        if (message.localExt != nil && [isReplaceSuccess length] && [isReplaceSuccess isEqual:@"YES"] && ![[NSFileManager defaultManager] fileExistsAtPath:mediaPath] && ([downloadAttStatus length] && [downloadAttStatus isEqual:@"downloadSuccess"]) ){
            [fileObj setObject:[NSNumber numberWithBool: true] forKey:@"isFilePathDeleted"];
        } else {
            [fileObj setObject:[NSString stringWithFormat:@"%@",mediaPath] forKey:@"path"];
        }
    } else if ([downloadAttStatus length] && [downloadAttStatus isEqual:@"downloading"]) {
        [fileObj setObject:@true forKey:@"isFileDownloading"];
    }
    
    
    if (message.deliveryState == NIMMessageDeliveryStateDeliveried  && [isReplaceSuccess length] && [isReplaceSuccess isEqual:@"YES"] && ([downloadAttStatus length] && [downloadAttStatus isEqual:@"downloadSuccess"])) {
        if ([[NSFileManager defaultManager] fileExistsAtPath:object.path]){
            NSError *removeItemError = nil;
            if (![[NSFileManager defaultManager] removeItemAtPath:object.path error:&removeItemError]) {
                NSLog(@"[removeItemError description]: %@", [removeItemError description]);
            }
        }
    }
    
    return fileObj;
}

-(NSDictionary *) makeExtendVideo:(NIMMessage *)message {
    NIMVideoObject *object = message.messageObject;
    
    NSMutableDictionary *videoObj = [NSMutableDictionary dictionary];
    [videoObj setObject:[NSString stringWithFormat:@"%@",object.url ] forKey:@"url"];
    [videoObj setObject:[NSString stringWithFormat:@"%@", object.coverUrl ] forKey:@"coverUrl"];
    [videoObj setObject:[NSString stringWithFormat:@"%@", object.displayName ] forKey:@"displayName"];
    [videoObj setObject:[NSString stringWithFormat:@"%f",object.coverSize.height ] forKey:@"coverSizeHeight"];
    [videoObj setObject:[NSString stringWithFormat:@"%f", object.coverSize.width ] forKey:@"coverSizeWidth"];
    [videoObj setObject:[NSString stringWithFormat:@"%ld",object.duration ] forKey:@"duration"];
    [videoObj setObject:[NSString stringWithFormat:@"%lld",object.fileLength] forKey:@"fileLength"];

    NSString *mediaPath = [self moveFiletoSessionDir:message isThumb:nil];
    NSString *mediaThumbPath = [self moveFiletoSessionDir:message isThumb:@1];
    NSString *isReplaceSuccess = [message.localExt objectForKey:@"isReplaceSuccess"];
    NSString *downloadAttStatus = [message.localExt objectForKey:@"downloadAttStatus"];
    
    if ([message.remoteExt objectForKey:@"parentId"] != nil) {
        [videoObj setObject:[message.remoteExt objectForKey:@"parentId"] forKey:@"parentId"];
    }
    
    
    if ([message.remoteExt objectForKey:@"indexCount"] != nil) {
        [videoObj setObject:[message.remoteExt objectForKey:@"indexCount"] forKey:@"indexCount"];
    }
    
    if ([downloadAttStatus length]) {
        [videoObj setObject:downloadAttStatus forKey:@"downloadAttStatus"];
    }
    if ([isReplaceSuccess length]) {
        [videoObj setObject:isReplaceSuccess forKey:@"isReplaceSuccess"];
    }
    if (mediaPath != nil) {
        if (message.localExt != nil && [isReplaceSuccess length] && [isReplaceSuccess isEqual:@"YES"] && ![[NSFileManager defaultManager] fileExistsAtPath:mediaPath] && ([downloadAttStatus length] && [downloadAttStatus isEqual:@"downloadSuccess"])){
            [videoObj setObject:[NSNumber numberWithBool: true] forKey:@"isFilePathDeleted"];
        } else {
            [videoObj setObject:[NSString stringWithFormat:@"%@",mediaPath] forKey:@"path"];
        }
    } else if ([downloadAttStatus length] && [downloadAttStatus isEqual:@"downloading"]) {
        [videoObj setObject:@true forKey:@"isFileDownloading"];
    }
    
    if (mediaThumbPath != nil) {
        [videoObj setObject:[NSString stringWithFormat:@"%@",mediaThumbPath] forKey:@"coverPath"];
    }
    
    if (message.deliveryState == NIMMessageDeliveryStateDeliveried && message.localExt != nil && [isReplaceSuccess length] && [isReplaceSuccess isEqual:@"YES"] && ([downloadAttStatus length] && [downloadAttStatus isEqual:@"downloadSuccess"])) {
        if ([[NSFileManager defaultManager] fileExistsAtPath:object.path]){
            NSError *removeItemError = nil;
            if (![[NSFileManager defaultManager] removeItemAtPath:object.path error:&removeItemError]) {
                NSLog(@"[removeItemError description]: %@", [removeItemError description]);
            }
        }
    }
    
    return videoObj;
}

-(NSDictionary *) makeExtendRecord:(NIMMessage *)message {
    NIMAudioObject *object = message.messageObject;
    NSMutableDictionary *voiceObj = [NSMutableDictionary dictionary];
    [voiceObj setObject:[NSString stringWithFormat:@"%@", [object url]] forKey:@"url"];
    [voiceObj setObject:[NSString stringWithFormat:@"%zd",(object.duration/1000)] forKey:@"duration"];
    [voiceObj setObject:[NSNumber  numberWithBool:message.isPlayed] forKey:@"isPlayed"];
    
    NSString *mediaPath = [self moveFiletoSessionDir:message isThumb:nil];
    NSString *isReplaceSuccess = [message.localExt objectForKey:@"isReplaceSuccess"];
    NSString *downloadAttStatus = [message.localExt objectForKey:@"downloadAttStatus"];
    if ([downloadAttStatus length]) {
        [voiceObj setObject:downloadAttStatus forKey:@"downloadAttStatus"];
    }
    if ([isReplaceSuccess length]) {
        [voiceObj setObject:isReplaceSuccess forKey:@"isReplaceSuccess"];
    }

    if (mediaPath != nil) {
        if (message.localExt != nil && [isReplaceSuccess length] && [isReplaceSuccess isEqual:@"YES"] && ![[NSFileManager defaultManager] fileExistsAtPath:mediaPath] && ([downloadAttStatus length] && [downloadAttStatus isEqual:@"downloadSuccess"])){
            [voiceObj setObject:[NSNumber numberWithBool: true] forKey:@"isFilePathDeleted"];
        } else {
            [voiceObj setObject:[NSString stringWithFormat:@"%@",mediaPath] forKey:@"path"];
        }
    }
    
    if (message.deliveryState == NIMMessageDeliveryStateDeliveried && message.localExt != nil && [isReplaceSuccess length] && [isReplaceSuccess isEqual:@"YES"] && [downloadAttStatus length] && [downloadAttStatus isEqual:@"downloadSuccess"]) {
        if ([[NSFileManager defaultManager] fileExistsAtPath:object.path]){
            NSError *removeItemError = nil;
            if (![[NSFileManager defaultManager] removeItemAtPath:object.path error:&removeItemError]) {
                NSLog(@"[removeItemError description]: %@", [removeItemError description]);
            }
        }
    }
    
    return voiceObj;
}

-(nullable NSString *) moveFiletoSessionDir:(NIMMessage *)message isThumb:(nullable NSNumber *)isThumb {
    NSString *originPath;
    NSString *urlDownload;
    
    NSString *downloadAttStatus = [message.localExt objectForKey:@"downloadAttStatus"];

    if ([downloadAttStatus length] && [downloadAttStatus isEqual:@"downloading"]) {
        return nil;
    }
    
    if (message.messageType == NIMMessageTypeAudio) {
        NIMAudioObject *object = message.messageObject;
        originPath = object.path;
        urlDownload = object.url;
    } else if (message.messageType == NIMMessageTypeImage) {
        NIMImageObject *object = message.messageObject;
        originPath = [isThumb  isEqual: @1] ? object.thumbPath : object.path;
        urlDownload = object.url;
    } else if (message.messageType == NIMMessageTypeVideo) {
        NIMVideoObject *object = message.messageObject;
        originPath = [isThumb  isEqual: @1] ? object.coverPath : object.path;
        urlDownload = object.url;
    }
    else if (message.messageType == NIMMessageTypeFile) {
        NIMVideoObject *object = message.messageObject;
        originPath = [isThumb  isEqual: @1] ? object.path : object.path;
        urlDownload = object.url;
    }
    NSLog(@"originPath: %@ , urlDownload: %@",originPath, urlDownload );
    NSString *documentPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    
    NSString *originMediaCachePath = documentPath;
    NSArray *files = [[NSFileManager defaultManager] subpathsOfDirectoryAtPath:documentPath error:nil];
    if ([[NSFileManager defaultManager] fileExistsAtPath:documentPath]) {
        for (NSString *file in files) {
            if ([file hasSuffix:@"Global/Resources"]) {
                originMediaCachePath = [documentPath stringByAppendingPathComponent:file];
                break;
            }
        }
    }
    //strDocPath: NIMSDK/b62854c9e1779d34fa7d683155581c2b/Global/Resources
    NSString *cacheMediaPath = [originMediaCachePath stringByAppendingPathComponent:message.session.sessionId];
    
    if (![[NSFileManager defaultManager] fileExistsAtPath:cacheMediaPath]) {
        NSLog(@"fileExistsAtPath NO");
        [[NSFileManager defaultManager] createDirectoryAtPath:cacheMediaPath withIntermediateDirectories:YES attributes:nil error:NULL];
    } else {
        NSLog(@"fileExistsAtPath YES");
    }
    
    NSString *theFileName = [originPath lastPathComponent];
    cacheMediaPath = [cacheMediaPath stringByAppendingPathComponent:theFileName];
    
    NSString *isReplaceSuccess = [message.localExt objectForKey:@"isReplaceSuccess"];

    if (([isReplaceSuccess length] && [isReplaceSuccess isEqual:@"YES"] && [downloadAttStatus length] && [downloadAttStatus isEqual:@"downloadSuccess"] && isThumb == nil) || [[NSFileManager defaultManager] fileExistsAtPath:cacheMediaPath]) {
        return cacheMediaPath;
    }
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:originPath]) {
        NSError *copyError = nil;
        if (![[NSFileManager defaultManager] copyItemAtPath:originPath toPath:cacheMediaPath error:&copyError]) {
       NSLog(@"[copyError description]: %@", [copyError description]);
            return nil;
        }
        [self setLocalExtMessage:message key:@"downloadAttStatus" value:@"downloadSuccess"];
        [self setLocalExtMessage:message key:@"isReplaceSuccess" value:@"YES"];
        [self refrashMessage:message From:@"receive"];

        if ([isThumb isEqual:@1]) {
            if ([[NSFileManager defaultManager] fileExistsAtPath:originPath]) {
                NSError *removeItemError = nil;
                if (![[NSFileManager defaultManager] removeItemAtPath:originPath error:&removeItemError]) {
                    NSLog(@"[removeItemError description]: %@", [removeItemError description]);
                }
            }
        }
    }else if (isThumb == nil) {
        [self setLocalExtMessage:message key:@"downloadAttStatus" value:@"downloading"];

        [[NIMObject initNIMObject] downLoadAttachment:urlDownload filePath:cacheMediaPath Error:^(NSError *error) {
            NSLog(@"downLoadVideo error: %@", [error description]);
            if (!error) {
                NSLog(@"download success");
                [self setLocalExtMessage:message key:@"downloadAttStatus" value:@"downloadSuccess"];
                [self setLocalExtMessage:message key:@"isReplaceSuccess" value:@"YES"];
                [self refrashMessage:message From:@"receive"];
            }
        } progress:^(float progress) {
            NSLog(@"视频下载进度%f",progress);
        }];
        return nil;
    }
    
    return nil;
};

- (void) setCancelResendMessage:(NSString *)messageId sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType {
    NIMSession *session = [NIMSession session:sessionId type:[sessionType integerValue]];
    NSArray *messages = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:session messageIds:@[messageId]];
    NIMMessage *message = messages.firstObject;
    NSDictionary *localExt = message.localExt ? : @{};
    NSMutableDictionary *dict = [localExt mutableCopy];
    [dict setValue:[NSNumber numberWithBool:YES] forKey:@"isCancelResend"];
    message.localExt = dict;
    [[NIMSDK sharedSDK].conversationManager updateMessage:message forSession:session completion:nil];
}

- (NSDictionary *) setLocalExtMessage:(NIMMessage *)message key:(NSString *)key value:(NSString *)value {
    
    NSDictionary *localExt = message.localExt ? : @{};
    NSMutableDictionary *dict = [localExt mutableCopy];
    [dict setObject:value forKey:key];
    message.localExt = dict;
    [[NIMSDK sharedSDK].conversationManager updateMessage:message forSession:message.session completion:nil];
    return dict;
}

-(NSDictionary *) updateMessageOfChatBot:(NSString *)messageId sessionId:(NSString *)sessionId chatBotType:(NSString *)chatBotType {
    NIMSession *session = [NIMSession session:sessionId type:NIMSessionTypeP2P];
    NSArray *messages = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:session messageIds:@[messageId]];
    NIMMessage *message = messages.firstObject;
    NSString *chatBotTypeOfLocalExt = [message.localExt objectForKey:@"chatBotType"];
    
    if (message.localExt != nil && chatBotType != nil) {
        return nil;
    }
    
    NSDictionary *dict = [self setLocalExtMessage:message key:@"chatBotType" value:chatBotType];
    return dict;
}

-(NSMutableArray *)setTimeArr:(NSArray *)messageArr{
    NSMutableArray *sourcesArr = [NSMutableArray array];
    for (NIMMessage *message in messageArr) {
        NSMutableDictionary *dic = [NSMutableDictionary dictionary];
        NSMutableDictionary *fromUser = [NSMutableDictionary dictionary];
        NIMUser   *messageUser = [[NIMSDK sharedSDK].userManager userInfo:message.from];
        NIMRecentSession *recent = [[NIMSDK sharedSDK].conversationManager recentSessionBySession:message.session];
            
        NSNumber *isCsrNumber = [recent.localExt objectForKey:@"isCsr"];
        NSNumber *isChatBotNumber = [recent.localExt objectForKey:@"isChatBot"];
        BOOL isCsr = [isCsrNumber boolValue];
        BOOL isChatBot = [isChatBotNumber boolValue];
        
        NSDictionary *localExt = message.localExt;
                
        if (localExt != nil) {
            [dic setObject:localExt forKey:@"localExt"];
        }
        
        if (recent.localExt != nil && [messageUser.userId isEqual:message.session.sessionId]) {
            [fromUser setObject:[NSString stringWithFormat:@"%@", @(isChatBot)] forKey:@"isChatBot"];
            
            [fromUser setObject:[NSString stringWithFormat:@"%@", @(isCsr)] forKey:@"isCsr"];
        }
    
        [fromUser setObject:[NSString stringWithFormat:@"%@",messageUser.userInfo.avatarUrl] forKey:@"avatar"];
        NSString *strAlias = messageUser.alias;
        if (strAlias.length) {
            [fromUser setObject:strAlias forKey:@"name"];
        }else if(messageUser.userInfo.nickName.length){
             [fromUser setObject:[NSString stringWithFormat:@"%@",messageUser.userInfo.nickName] forKey:@"name"];
        }else{
            if (recent.localExt != nil && isCsr) {
                NSString *nickname = [recent.localExt objectForKey:@"name"];
                
                if ([nickname length]) {
                    [fromUser setObject:[NSString stringWithFormat:@"%@", nickname] forKey:@"name"];
                } else {
                    [fromUser setObject:[NSString stringWithFormat:@"%@", @"CSR"] forKey:@"name"];
                }
            } else {
                [fromUser setObject:[NSString stringWithFormat:@"%@",messageUser.userId] forKey:@"name"];
            }
        }
        [fromUser setObject:[NSString stringWithFormat:@"%@", message.from] forKey:@"_id"];
        NSArray *key = [fromUser allKeys];
        for (NSString *tem  in key) {
            if ([[fromUser objectForKey:tem] isEqualToString:@"(null)"]) {
                [fromUser setObject:@"" forKey:tem];
            }
        }
        [dic setObject:[NSString stringWithFormat:@"%@", message.text] forKey:@"text"];
        [dic setObject:[NSString stringWithFormat:@"%@", message.session.sessionId] forKey:@"sessionId"];
        [dic setObject:[NSString stringWithFormat:@"%ld", message.session.sessionType] forKey:@"sessionType"];
        
        [dic setObject:[NSString stringWithFormat:@"%d",message.isRemoteRead] forKey:@"isRemoteRead"];

        switch (message.deliveryState) {
            case NIMMessageDeliveryStateFailed:
                [dic setObject:@"send_failed" forKey:@"status"];
                break;
            case NIMMessageDeliveryStateDelivering:
                [dic setObject:@"send_going" forKey:@"status"];
                break;
            case NIMMessageDeliveryStateDeliveried:
                [dic setObject:@"send_succeed" forKey:@"status"];
                break;
            default:
                [dic setObject:@"send_failed" forKey:@"status"];
                break;
        }
        NSString *strSessionId = self._session.sessionId;
          if (message.session.sessionType == NIMSessionTypeP2P && !isCsr && !isChatBot && message.localExt != nil) {
              NSString *isFriend = [message.localExt objectForKey:@"isFriend"];
              if (isFriend != nil && [isFriend isEqual:@"NO"]) {
                  [dic setObject:@"send_failed" forKey:@"status"];
              }
          }
        [dic setObject: [NSNumber numberWithBool:message.isOutgoingMsg] forKey:@"isOutgoing"];
        [dic setObject:[NSString stringWithFormat:@"%f", message.timestamp] forKey:@"timeString"];
        [dic setObject:[NSNumber numberWithBool:NO] forKey:@"isShowTime"];
        [dic setObject:[NSString stringWithFormat:@"%@", message.messageId] forKey:@"msgId"];
        
        if (message.messageType == NIMMessageTypeText) {
            [dic setObject:@"text" forKey:@"msgType"];
            NSLog(@"message exten =>> %@", message.remoteExt);
            if ([[message.remoteExt objectForKey:@"extendType"]  isEqual: @"forwardMultipleText"]) {
                NSMutableDictionary *extend = [NSMutableDictionary dictionary];
                [extend setObject:message.text forKey:@"messages"];
                
                [dic setObject:extend forKey:@"extend"];
                [dic setObject:@"forwardMultipleText" forKey:@"msgType"];
            }
            
            if ([[message.remoteExt objectForKey:@"extendType"]  isEqual: @"card"]) {
                [dic setObject:message.remoteExt forKey:@"extend"];
                [dic setObject:@"card" forKey:@"msgType"];
            }
            
            if ([[message.remoteExt objectForKey:@"extendType"]  isEqual: @"gif"]) {
                [dic setObject:message.remoteExt forKey:@"extend"];
                [dic setObject:@"image" forKey:@"msgType"];
            }
            
            if ([[message.remoteExt objectForKey:@"extendType"]  isEqual: @"TEAM_NOTIFICATION_MESSAGE"]) {
                [dic setObject:message.remoteExt forKey:@"extend"];
                [dic setObject:@"notification" forKey:@"msgType"];
            }
        }else if (message.messageType  == NIMMessageTypeImage) {
            // image coming is not have object.path, just have thumb_path.
            [dic setObject:@"image" forKey:@"msgType"];

            [dic setObject:[self makeExtendImage:message] forKey:@"extend"];
        }
        else if (message.messageType  == NIMMessageTypeFile) {
            [dic setObject:@"file" forKey:@"msgType"];

            [dic setObject:[self makeExtendFile:message] forKey:@"extend"];
        }
        else if(message.messageType == NIMMessageTypeAudio){
            [dic setObject:@"voice" forKey:@"msgType"];

            [dic setObject:[self makeExtendRecord:message] forKey:@"extend"];
        }else if(message.messageType == NIMMessageTypeVideo){
            [dic setObject:@"video" forKey:@"msgType"];
            
            [dic setObject:[self makeExtendVideo:message] forKey:@"extend"];
        }else if(message.messageType == NIMMessageTypeLocation){
            [dic setObject:@"location" forKey:@"msgType"];
            NIMLocationObject *object = message.messageObject;
            NSMutableDictionary *locationObj = [NSMutableDictionary dictionary];
            [locationObj setObject:[NSString stringWithFormat:@"%f", object.latitude ] forKey:@"latitude"];
            [locationObj setObject:[NSString stringWithFormat:@"%f", object.longitude ] forKey:@"longitude"];
            [locationObj setObject:[NSString stringWithFormat:@"%@", object.title ] forKey:@"title"];
            [dic setObject:locationObj forKey:@"extend"];
            
        }else if(message.messageType == NIMMessageTypeTip){//提醒类消息
            [dic setObject:@"notification" forKey:@"msgType"];
            NSMutableDictionary *notiObj = [NSMutableDictionary dictionary];
            [notiObj setObject:message.text forKey:@"tipMsg"];
            [dic setObject:notiObj forKey:@"extend"];
        }else if (message.messageType == NIMMessageTypeNotification) {
            [dic setObject:@"notification" forKey:@"msgType"];
           
            [dic setObject:[self setNotiTeamObj:message] forKey:@"extend"];
        }else if (message.messageType == NIMMessageTypeCustom) {
            NIMCustomObject *customObject = message.messageObject;
            DWCustomAttachment *obj = customObject.attachment;
            NSLog(@"DWCustomAttachment *obj %ld %@", (long)obj.custType, obj.dataDict);
            if (obj) {
                switch (obj.custType) {
//                    case CustomMessageTypeFowardMultipleText: //红包
//                    {
//                        [dic setObject:obj.dataDict forKey:@"extend"];
//                        [dic setObject:@"forwardMultipleText" forKey:@"msgType"];
//                    }
//                        break;
                    case CustomMessgeTypeRedpacket: //红包
                    {
                        [dic setObject:obj.dataDict forKey:@"extend"];
//                        [dic setObject:@"redpacket" forKey:@"custType"];
                        [dic setObject:@"redpacket" forKey:@"msgType"];
                    }
                        break;
                    case CustomMessgeTypeBankTransfer: //转账
                    {
                        [dic setObject:obj.dataDict  forKey:@"extend"];
//                        [dic setObject:@"transfer" forKey:@"custType"];
                        [dic setObject:@"transfer" forKey:@"msgType"];
                    }
                        break;
                    case CustomMessgeTypeRedPacketOpenMessage: //拆红包消息
                    {
                        NSDictionary *dataDict = [self dealWithData:obj.dataDict];
                        if (dataDict) {
                            [dic setObject:dataDict  forKey:@"extend"];
//                            [dic setObject:@"redpacketOpen" forKey:@"custType"];
                            [dic setObject:@"redpacketOpen" forKey:@"msgType"];
                        }else{

                            continue;//终止本次循环
                        }
                    }
                        break;
                    case CustomMessgeTypeUrl: //链接
                    case CustomMessgeTypeAccountNotice: //账户通知，与账户金额相关变动
                    {
                        [dic setObject:[NSString stringWithFormat:@"%d",message.isRemoteRead] forKey:@"isRemoteRead"];
//                        [dic setObject:[NSString stringWithFormat:@"%ld", message.messageType] forKey:@"msgType"];
                        if (obj.custType == CustomMessgeTypeAccountNotice) {
                            [dic setObject:obj.dataDict  forKey:@"extend"];
                            [dic setObject:@"account_notice" forKey:@"msgType"];
                        }else{
                            [dic setObject:obj.dataDict  forKey:@"extend"];
                            [dic setObject:@"url" forKey:@"msgType"];
                        }
                    }
                        break;
//                    case CustomMessgeTypeBusinessCard://名片
//                    {
//                        [dic setObject:obj.dataDict  forKey:@"extend"];
//                        [dic setObject:@"card" forKey:@"msgType"];
//                    }
//                        break;
                    case CustomMessgeTypeCustom://自定义
                    {
                        [dic setObject:obj.dataDict  forKey:@"extend"];
                        [dic setObject:@"custom" forKey:@"msgType"];
                    }
                        break;
                    default:
                    {
                        if (obj.dataDict != nil) {
                            [dic setObject:obj.dataDict  forKey:@"extend"];
                        }
                        [dic setObject:@"unknown" forKey:@"msgType"];
                    }
                        break;
                        
                }
            }
        }else{
            [dic setObject:@"unknown" forKey:@"msgType"];
            NSMutableDictionary *unknowObj = [NSMutableDictionary dictionary];
            [dic setObject:unknowObj  forKey:@"extend"];
        }
        
        if (isChatBot) {
            [dic setObject:@"unknown" forKey:@"msgType"];
        }
        [dic setObject:fromUser forKey:@"fromUser"];
        [sourcesArr addObject:dic];
    }
    
    return sourcesArr;
    
}
//取消录音
- (void)onCancelRecording
{
    [[NIMSDK sharedSDK].mediaManager cancelRecord];
}
//结束录音
- (void)onStopRecording
{
    
    [[NIMSDK sharedSDK].mediaManager stopRecord];
    
}
//开始录音
- (void)onStartRecording
{
    NIMAudioType type = NIMAudioTypeAAC;
    NSTimeInterval duration = 60.0;
    
    [[NIMSDK sharedSDK].mediaManager addDelegate:self];
    
    [[NIMSDK sharedSDK].mediaManager record:type
                                   duration:duration];
}
//开始播放录音
- (void)play:(NSString *)filepath isExternalSpeaker:(BOOL *)isExternalSpeaker {
    [[NIMSDK sharedSDK].mediaManager addDelegate:self];
    if (filepath) {
        [[NIMSDK sharedSDK].mediaManager setNeedProximityMonitor: NO];
        [[NIMSDK sharedSDK].mediaManager switchAudioOutputDevice: isExternalSpeaker ? NIMAudioOutputDeviceSpeaker : NIMAudioOutputDeviceReceiver];
        [[NIMSDK sharedSDK].mediaManager play:filepath];
    }
}
//停止播放
- (void)stopPlay {
    [[NIMSDK sharedSDK].mediaManager stopPlay];
}

//停止播放
- (BOOL)isPlayingRecord {
    return [[NIMSDK sharedSDK].mediaManager isPlaying];
}

- (void)switchAudioOutputDevice: (BOOL *)isExternalSpeaker {
    [[NIMSDK sharedSDK].mediaManager switchAudioOutputDevice: isExternalSpeaker ? NIMAudioOutputDeviceSpeaker : NIMAudioOutputDeviceReceiver];
}


//发送录音
-(void)sendAudioMessage:(  NSString *)file duration:(  NSString *)duration isCustomerService:(BOOL *)isCustomerService{
    if (file) {
        NIMMessage *message = [NIMMessageMaker msgWithAudio:file andeSession:self._session senderName:_myUserName];
        if (isCustomerService || [self isFriendToSendMessage:message]) {
             [[[NIMSDK sharedSDK] chatManager] sendMessage:message toSession:self._session error:nil];
        }
    }
}

-(void)createNotificationBirthday:(NSString *)sessionId sessionType:(NSString *)sessionType memberContactId:(NSString *)memberContactId memberName:(NSString *)memberName success:(Success)success err:(Errors)err {
    NIMSession *session = [NIMSession session:sessionId type:[sessionType integerValue]];
    NIMRecentSession *recent = [[NIMSDK sharedSDK].conversationManager recentSessionBySession:session];
    NSString *name = memberName;
    if (name == nil) {
        NIMUser *user = [[NIMSDK sharedSDK].userManager userInfo:sessionId];
        if (user != nil) {
            name = user.userInfo.nickName;
        }
    }
    
    NIMMessage *lastMessage = recent.lastMessage;
    NIMMessage *message = [NIMMessageMaker msgWithNotificationBirthday:lastMessage memberContactId:memberContactId memberName:name];
    
    [[NIMSDK sharedSDK].conversationManager saveMessage:message forSession:session completion:^(NSError * _Nullable error) {
        if (error != nil) {
            err(error);
            return;
        }
        
        success(@"success");
    }];
}

-(void)sendTextMessageWithSession:(NSString *)msgContent sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType sessionName:(NSString *)sessionName messageSubType:(NSInteger)messageSubType {
    NIMSession *session = [NIMSession session:sessionId type:[sessionType intValue]];
    NIMMessage *message = [NIMMessageMaker msgWithText:msgContent andApnsMembers:@[] andeSession:session senderName:sessionName messageSubType:messageSubType];
    
    [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:session error:nil];
}

//发送文字消息
-(void)sendMessage:(NSString *)mess andApnsMembers:(NSArray *)members isCustomerService:(BOOL *)isCustomerService messageSubType:(NSInteger)messageSubType {
    NIMMessage *message = [NIMMessageMaker msgWithText:mess andApnsMembers:members andeSession:self._session senderName:_myUserName messageSubType:messageSubType];
    //发送消息
    if (isCustomerService || [self isFriendToSendMessage:message]) {
        [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:self._session error:nil];
    }
}

-(void)queryAllTeams:(Success)success err:(Errors)err {
    NSTimeInterval timeInterval = 0;
    NIMTeamFetchTeamsHandler completion = ^(NSError * __nullable error, NSArray<NIMTeam *> * __nullable teams){
        if(error == nil) {
            NSMutableArray *arr = [[NSMutableArray alloc] init];
            
            for(NIMTeam *team in teams) {
                NSMutableDictionary *teamDic = [[NSMutableDictionary alloc] init];
                [teamDic setObject:[NSString stringWithFormat:@"%@",team.teamId] forKey:@"teamId"];
                [teamDic setObject:[NSString stringWithFormat:@"%@",team.teamName] forKey:@"name"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld", team.type] forKey:@"type"];
                [teamDic setObject:[NSString stringWithFormat:@"%@",team.announcement]forKey:@"announcement"];
                [teamDic setObject:[NSString stringWithFormat:@"%@",team.owner] forKey:@"creator"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld", team.memberNumber ] forKey:@"memberCount"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld",team.level] forKey:@"memberLimit"];
                [teamDic setObject:[NSString stringWithFormat:@"%f", team.createTime ] forKey:@"createTime"];
                NSString *strMute = team.notifyStateForNewMsg == NIMTeamNotifyStateAll ? @"1" : @"0";
                [teamDic setObject:[NSString stringWithFormat:@"%@", strMute ] forKey:@"mute"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld",team.joinMode] forKey:@"verifyType"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld",team.beInviteMode] forKey:@"teamBeInviteMode"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld",team.inviteMode] forKey:@"teamInviteMode"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld",team.updateInfoMode] forKey:@"teamUpdateMode"];
                if (team.intro == nil || [team.intro isEqual:@"(null)"]) {
                    [teamDic setObject:@"" forKey:@"introduce"];
                } else {
                    [teamDic setObject:[NSString stringWithFormat:@"%@",team.intro] forKey:@"introduce"];
                }
                if (team.avatarUrl == nil || [team.avatarUrl isEqual:@"(null)"]) {
                    [teamDic setObject:@"" forKey:@"avatar"];
                } else {
                    [teamDic setObject:[NSString stringWithFormat:@"%@", team.avatarUrl] forKey:@"avatar"];
                }
                [arr addObject:teamDic];
            }
            
            success(arr);
        } else {
            err(error);
        }
    };
    
    [[[NIMSDK sharedSDK] teamManager] fetchTeamsWithTimestamp:timeInterval completion:completion];
}

-(void)queryTeamByName:(NSString *)search success:(Success)success err:(Errors)err {
    NIMTeamSearchOption *option = [NIMTeamSearchOption new];
    /// 设置搜索选项为 匹配TeamID
    [option setSearchContentOption:NIMTeamSearchContentOptiontName];
    /// 设置搜索内容为 @"6271272396"
    [option setSearchContent:search];
    /// completion 完成后的回调
    NIMTeamSearchHandler completion = ^(NSError * __nullable error, NSArray<NIMTeam *> * __nullable teams)
    {
        if (error == nil) {
            NSMutableArray *arr = [[NSMutableArray alloc] init];
            
            for(NIMTeam *team in teams) {
                NSMutableDictionary *teamDic = [[NSMutableDictionary alloc] init];
                [teamDic setObject:[NSString stringWithFormat:@"%@",team.teamId] forKey:@"teamId"];
                [teamDic setObject:[NSString stringWithFormat:@"%@",team.teamName] forKey:@"name"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld", team.type] forKey:@"type"];
                [teamDic setObject:[NSString stringWithFormat:@"%@",team.announcement]forKey:@"announcement"];
                [teamDic setObject:[NSString stringWithFormat:@"%@",team.owner] forKey:@"creator"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld", team.memberNumber ] forKey:@"memberCount"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld",team.level] forKey:@"memberLimit"];
                [teamDic setObject:[NSString stringWithFormat:@"%f", team.createTime ] forKey:@"createTime"];
                NSString *strMute = team.notifyStateForNewMsg == NIMTeamNotifyStateAll ? @"1" : @"0";
                [teamDic setObject:[NSString stringWithFormat:@"%@", strMute ] forKey:@"mute"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld",team.joinMode] forKey:@"verifyType"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld",team.beInviteMode] forKey:@"teamBeInviteMode"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld",team.inviteMode] forKey:@"teamInviteMode"];
                [teamDic setObject:[NSString stringWithFormat:@"%ld",team.updateInfoMode] forKey:@"teamUpdateMode"];
                if (team.intro == nil || [team.intro isEqual:@"(null)"]) {
                    [teamDic setObject:@"" forKey:@"introduce"];
                } else {
                    [teamDic setObject:[NSString stringWithFormat:@"%@",team.intro] forKey:@"introduce"];
                }
                if (team.avatarUrl == nil || [team.avatarUrl isEqual:@"(null)"]) {
                    [teamDic setObject:@"" forKey:@"avatar"];
                } else {
                    [teamDic setObject:[NSString stringWithFormat:@"%@", team.avatarUrl] forKey:@"avatar"];
                }
                [arr addObject:teamDic];
            }
            
            success(arr);
        } else {
            err(error);
        }
    };
    /// 查询群信息
    [[[NIMSDK sharedSDK] teamManager] searchTeamWithOption:option
                                                completion:completion];
}

-(void) sendGifMessageWithSession:(NSString *)url aspectRatio:(NSString *)aspectRatio sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType sessionName:(NSString *)sessionName {
    NIMSession *session = [NIMSession session:sessionId type:[sessionType intValue]];
    NIMMessage *message = [NIMMessageMaker msgWithText:@"[动图]" andApnsMembers:@[] andeSession:session senderName:sessionName messageSubType:0];
    NSDictionary  *remoteExt = @{@"extendType": @"gif", @"path": url, @"aspectRatio": aspectRatio};
    message.remoteExt = remoteExt;
    
    [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:session error:nil];
 }

//send gif message
-(void)sendGifMessage:(NSString *)url aspectRatio:(NSString *)aspectRatio andApnsMembers:(NSArray *)members isCustomerService:(BOOL *)isCustomerService{
    NIMMessage *message = [NIMMessageMaker msgWithText:@"[动图]" andApnsMembers:members andeSession:self._session senderName:_myUserName messageSubType:0];
    NSDictionary  *remoteExt = @{@"extendType": @"gif", @"path": url, @"aspectRatio": aspectRatio};
    message.remoteExt = remoteExt;
    NSLog(@"message.remoteExt: %@", message.remoteExt);
    //发送消息
    if (isCustomerService || [self isFriendToSendMessage:message]) {
        [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:self._session error:nil];
    }
}

//send gif message
-(void)sendMessageTeamNotificationRequestJoin:(nonnull  NSDictionary *)sourceId targets:(nonnull NSArray *)targets type:(nonnull NSNumber*)type success:(Success)succe Err:(Errors)err{
    NIMMessage *message = [NIMMessageMaker msgWithText:@"TEAM_NOTIFICATION_MESSAGE" andApnsMembers:@[] andeSession:self._session senderName:_myUserName messageSubType:0];
    NSDictionary  *remoteExt = @{@"extendType": @"TEAM_NOTIFICATION_MESSAGE",@"operationType": type, @"sourceId": sourceId, @"targets": targets};
    message.remoteExt = remoteExt;
    NIMMessageSetting *seting = [[NIMMessageSetting alloc]init];
    seting.apnsEnabled = NO;
    seting.shouldBeCounted = NO;
    message.setting = seting;
    NSLog(@"sendMessageTeamNotificationRequestJoin message.remoteExt: %@", message.remoteExt);

    NSError *error;

    [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:self._session error:&error];
   
    if (error != nil) {
        err(error);
    } else {
        succe(@"200");
    }
}

-(void) sendImageMessageWithSession:(NSString *)path isHighQuality:(BOOL *)isHighQuality sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType sessionName:(NSString *)sessionName {
    UIImage *image = [[UIImage alloc] initWithContentsOfFile:path];
    NIMSession *session = [NIMSession session:sessionId type:[sessionType intValue]];
    NIMMessage *message = [NIMMessageMaker msgWithImage:image andeSession:session isHighQuality:isHighQuality senderName:sessionName];
    
    [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:session error:nil];
}

//发送图片
-(void)sendImageMessages:(NSString *)path displayName:(NSString *)displayName isCustomerService:(BOOL *)isCustomerService isHighQuality:(BOOL *)isHighQuality parentId:(nullable NSString *)parentId indexCount:(nullable NSNumber*)indexCount {
    UIImage *img = [[UIImage alloc]initWithContentsOfFile:path];
    NIMMessage *message = [NIMMessageMaker msgWithImage:img andeSession:self._session isHighQuality:isHighQuality senderName:_myUserName];
    
    NSMutableDictionary *msgRemoteExt = [[NSMutableDictionary alloc] initWithDictionary: message.remoteExt ? message.remoteExt : @{}];
    
    if (parentId != nil) {
        [msgRemoteExt setValue:parentId forKey:@"parentId"];
    }
    if (indexCount != nil) {
        [msgRemoteExt setValue:indexCount forKey:@"indexCount"];
    }
    message.remoteExt = msgRemoteExt;
//    NIMMessage *message = [NIMMessageMaker msgWithImagePath:path];
    if (isCustomerService || [self isFriendToSendMessage:message]) {
        [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:self._session error:nil];
    }
}

-(void) sendMultiMediaMessage:(NSArray *)listMedia parentId:(nullable NSString *)parentId isCustomerService:(BOOL *)isCustomerService success:(Success)succes error:(Errors)error {
    if (parentId != nil) {
        NIMMessage *message = [[NIMMessage alloc] init];
        message.text    = parentId;
        message.localExt = @{@"isLocalMsg": @YES, @"parentMediaId": parentId };
        NIMMessageSetting *seting = [[NIMMessageSetting alloc]init];
        seting.apnsEnabled = NO;
        seting.shouldBeCounted = NO;
        message.setting = seting;
        
        [[NIMSDK sharedSDK].conversationManager saveMessage:message forSession:self._session completion:^(NSError * _Nullable error) {
            if (error != nil) {
    //            err(error);s
                return;
            }
            
    //        success(@"success");
        }];
    }
    
    for (NSDictionary *media in listMedia) {
        NSString *mediaType = [media objectForKey:@"type"];
        if (mediaType == nil || (![mediaType isEqual:@"image"] && ![mediaType isEqual:@"video"])) {
            error(@"media type is invalid");
            return;
        }
        
        NSDictionary *mediaData = [media objectForKey:@"data"];
        if (mediaData == nil) {
            error(@"media data is invalid");
            return;
        }
        
        if ([mediaType isEqual:@"image"]) {
            BOOL isHighQuality = [mediaData objectForKey:@"isHighQuality"];
            
            [self sendImageMessages:[mediaData objectForKey:@"file"] displayName:[mediaData objectForKey:@"displayName"] isCustomerService:isCustomerService isHighQuality:&isHighQuality parentId:parentId indexCount:[media objectForKey:@"indexCount"]];
            continue;
        }
        
        [self sendVideoMessage:[mediaData objectForKey:@"file"] duration:[mediaData objectForKey:@"duration"] width:[mediaData objectForKey:@"width"] height:[mediaData objectForKey:@"height"] displayName:[mediaData objectForKey:@"displayName"] isCustomerService:isCustomerService parentId:parentId indexCount:[media objectForKey:@"indexCount"]];
    }
    
    succes(@"success");
}

-(void)sendVideoMessageWithSession:(NSString *)path sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType sessionName:(NSString *)sessionName {
    if ([path hasPrefix:@"file:///private"]) {
        path = [path stringByReplacingOccurrencesOfString:@"file:///private" withString:@""];
    }
    
    NIMSession *session = [NIMSession session:sessionId type:[sessionType intValue]];
    NIMMessage *message = [NIMMessageMaker msgWithVideo:path andeSession:session senderName:sessionName];
    
    [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:session error:nil];
}

//发送视频
-(void)sendVideoMessage:(  NSString *)path duration:(  NSString *)duration width:(  NSNumber *)width height:(  NSNumber *)height displayName:(  NSString *)displayName isCustomerService:(BOOL *)isCustomerService parentId:(nullable NSString *)parentId indexCount:(nullable NSNumber*)indexCount {
//    __weak typeof(self) weakSelf = self;
//    [self.mediaFetcher fetchMediaFromCamera:^(NSString *path, UIImage *image) {
        NIMMessage *message;
//        if (image) {
//            message = [NIMMessageMaker msgWithImage:image andeSession:_session];
//        }else{
    if ([path hasPrefix:@"file:///private"]) {
        path = [path stringByReplacingOccurrencesOfString:@"file:///private" withString:@""];
    }
    
    message = [NIMMessageMaker msgWithVideo:path andeSession:self._session senderName:_myUserName];
//        }
    
    NSMutableDictionary *msgRemoteExt = [[NSMutableDictionary alloc] initWithDictionary: message.remoteExt ? message.remoteExt : @{}];
    
    if (parentId != nil) {
        [msgRemoteExt setValue:parentId forKey:@"parentId"];
    }
    if (indexCount != nil) {
        [msgRemoteExt setValue:indexCount forKey:@"indexCount"];
    }
    message.remoteExt = msgRemoteExt;
    
    if (isCustomerService || [self isFriendToSendMessage:message]) {
        [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:self._session error:nil];
    }
//    }];
}

////发送自定义消息
//-(void)sendCustomMessage:(NSDictionary *)dataDict{
//    NSString *strW = [dataDict objectForKey:@"Width"] ? [NSString stringWithFormat:@"%@",[dataDict objectForKey:@"Width"]] : @"0";
//    NSString *strH = [dataDict objectForKey:@"Height"] ? [NSString stringWithFormat:@"%@",[dataDict objectForKey:@"Height"]] : @"0";
//    [dataDict setValue:strW forKey:@"Width"];
//    [dataDict setValue:strH forKey:@"Height"];
//    [self sendCustomMessage:CustomMessgeTypeCustom data:dataDict];
//}

-(void) sendFileMessageWithSession:(NSString *)path fileName:(NSString *)fileName sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType sessionName:(NSString *)sessionName success:(Success)success err:(Errors)err {
    NIMSession *session = [NIMSession session:sessionId type:[sessionType intValue]];
    NIMMessage *message = [NIMMessageMaker msgWithFile:path fileName:fileName andeSession:session senderName:sessionName];
    
    NSError *error;
    [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:session error:&error];
    if (error != nil) {
        NSLog(@"sendFileMessageWithSession error: %@", error);
        err(error);
    } else {
        success(@"200");
    }
}

-(void)sendFileMessage:(NSString *)filePath fileName:(NSString *)fileName isCustomerService:(BOOL *)isCustomerService success:(Success)succe Err:(Errors)err{
    NIMMessage *message = [NIMMessageMaker msgWithFile:filePath fileName:fileName andeSession:self._session senderName:_myUserName];
                           
    if (isCustomerService || [self isFriendToSendMessage:message]) {
        NSError *error;
        [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:self._session error:&error];
       
        if (error != nil) {
            err(error);
        } else {
            succe(@"200");
        }
    }
};

//发送自定义消息2
-(void)sendCustomMessage:(NSInteger )custType data:(NSDictionary *)dataDict {
    NIMMessage *message;
    DWCustomAttachment *obj = [[DWCustomAttachment alloc]init];
    NSLog(@"custType %ld", (long)custType);
    obj.custType = custType;
    obj.dataDict = dataDict;
    message = [NIMMessageMaker msgWithCustomAttachment:obj andeSession:self._session senderName:_myUserName];
    if ([self isFriendToSendMessage:message]) {
        [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:self._session error:nil];
    }
}

//发送自定义消息2
-(void)forwardMultipleTextMessage:(NSDictionary *)dataDict sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType content:(NSString *)content {

    NIMSession *session = [NIMSession session:sessionId type:[sessionType integerValue]];
    
    NIMMessage *message = [NIMMessageMaker msgWithText:[dataDict objectForKey:@"messages"] andApnsMembers:@[] andeSession:session senderName:_myUserName messageSubType:0];
    //发送消息
    NSDictionary  *remoteExt = @{@"extendType": @"forwardMultipleText"};
    message.remoteExt = remoteExt;
    message.apnsContent = @"[聊天记录]";
    [NIMMessageMaker setupMessagePushBody:message andSession:session senderName:_myUserName];

    if ([self isFriendToSendMessage:message]) {
        [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:session error:nil];

        if ([content length] != 0) {
            NIMMessage *_message = [[NIMMessage alloc] init];
            _message.text    = content;
            [[NIMSDK sharedSDK].chatManager sendMessage:_message toSession:session error:nil];
        }
    }
    
//    NIMMessage *message;
//    DWCustomAttachment *obj = [[DWCustomAttachment alloc]init];
//    NSLog(@"custType %ld", (long)custType);
//    obj.custType = custType;
//    obj.dataDict = dataDict;
    
//    message = [NIMMessageMaker msgWithCustomAttachment:obj andeSession:session];
//    message.text = content;
//
//    if ([self isFriendToSendMessage:message]) {
//        [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:session error:nil];
//
//        NIMMessage *messages = [[NIMMessage alloc] init];
//        messages.text    = content;
//        [[NIMSDK sharedSDK].chatManager sendMessage:messages toSession:session error:nil];
//    }
}


//发送地理位置消息
-(void)sendLocationMessage:(NSString *)sessionId sessionType:(NSString *)sessionType latitude:(  NSString *)latitude longitude:(  NSString *)longitude address:(  NSString *)address success:(Success)succe Err:(Errors)err{
    NIMLocationObject *locaObj = [[NIMLocationObject alloc]initWithLatitude:[latitude doubleValue] longitude:[longitude doubleValue] title:address];
    NIMKitLocationPoint *locationPoint = [[NIMKitLocationPoint alloc]initWithLocationObject:locaObj];
    NIMSession *session = [NIMSession session:sessionId type:[sessionType integerValue]];
    
    NIMMessage *message = [NIMMessageMaker msgWithLocation:locationPoint andeSession:session senderName:_myUserName];
    if ([self isFriendToSendMessage:message]) {
        NSError *error;
        [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:session error:&error];
       
        if (error != nil) {
            err(error);
        } else {
            succe(@"200");
        }
    }
}

//发送提醒消息
-(void)sendTipMessage:( NSString *)content{
    
}
//- (NIMKitMediaFetcher *)mediaFetcher
//{
//    if (!_mediaFetcher) {
//        _mediaFetcher = [[NIMKitMediaFetcher alloc] init];
//    }
//    return _mediaFetcher;
//}

//发送红包消息
- (void)sendRedPacketMessage:(NSString *)type comments:(NSString *)comments serialNo:(NSString *)serialNo{
    NSDictionary *dict = @{@"type":type,@"comments":comments,@"serialNo":serialNo};
    [self sendCustomMessage:CustomMessgeTypeRedpacket data:dict];
}
//发送转账消息
- (void)sendBankTransferMessage:(NSString *)amount comments:(NSString *)comments serialNo:(NSString *)serialNo{
    NSDictionary *dict = @{@"amount":amount,@"comments":comments,@"serialNo":serialNo};
    [self sendCustomMessage:CustomMessgeTypeBankTransfer data:dict];
}

//发送拆红包消息
-(void)sendRedPacketOpenMessage:(NSString *)sendId hasRedPacket:(NSString *)hasRedPacket serialNo:(NSString *)serialNo{
    NSString *strMyId = [NIMSDK sharedSDK].loginManager.currentAccount;
    NSDictionary *dict = @{@"sendId":sendId,@"openId":strMyId,@"hasRedPacket":hasRedPacket,@"serialNo":serialNo};
    NIMMessage *message;
    DWCustomAttachment *obj = [[DWCustomAttachment alloc]init];
    obj.custType = CustomMessgeTypeRedPacketOpenMessage;
    obj.dataDict = dict;
    message = [NIMMessageMaker msgWithCustomAttachment:obj andeSession:self._session senderName:_myUserName];
    NSTimeInterval timestamp = [[NSDate date] timeIntervalSince1970];
    message.timestamp = timestamp;
    if(![sendId isEqualToString:strMyId]){
        NSDictionary *dataDict = @{@"type":@"2",@"data":@{@"dict":dict,@"timestamp":[NSString stringWithFormat:@"%f",timestamp],@"sessionId":self._session.sessionId,@"sessionType":[NSString stringWithFormat:@"%zd",self._session.sessionType]}};
        
        NSString *content = [self jsonStringWithDictionary:dataDict];
        NIMSession *redSession = [NIMSession session:sendId type:NIMSessionTypeP2P];
        NIMCustomSystemNotification *notifi = [[NIMCustomSystemNotification alloc]initWithContent:content];
        notifi.sendToOnlineUsersOnly = NO;
        NIMCustomSystemNotificationSetting *setting = [[NIMCustomSystemNotificationSetting alloc]init];
        setting.shouldBeCounted = NO;
        setting.apnsEnabled = NO;
        notifi.setting = setting;
        notifi.apnsPayload = dataDict;
        [[NIMSDK sharedSDK].systemNotificationManager sendCustomNotification:notifi toSession:redSession completion:nil];//发送自定义通知
    }
    [[NIMSDK sharedSDK].conversationManager saveMessage:message forSession:self._session completion:nil];
    
}

//发送名片
- (void)sendCardMessage:(NSString *)toSessionType sessionId:(NSString *)toSessionId name:(NSString *)name imgPath:(NSString *)strImgPath cardSessionId:(NSString *)cardSessionId cardSessionType:(NSString *)cardSessionType {
    NIMSession *session = [NIMSession session:toSessionId type:[toSessionType integerValue]];

    NIMMessage *message = [NIMMessageMaker msgWithText:@"[个人名片]" andApnsMembers:@[] andeSession:session senderName:_myUserName messageSubType:0];
    //发送消息
    NSDictionary  *remoteExt = @{@"extendType": @"card", @"type":cardSessionType, @"name":name, @"imgPath":strImgPath, @"sessionId":cardSessionId};
    message.remoteExt = remoteExt;
    
//    if ([self isFriendToSendMessage:message]) {
        [[NIMSDK sharedSDK].chatManager sendMessage:message toSession:session error:nil];
//    }
}

// dict字典转json字符串
- (NSString *)jsonStringWithDictionary:(NSDictionary *)dict
{
    if (dict && 0 != dict.count)
    {
        NSError *error = nil;
        // NSJSONWritingOptions 是"NSJSONWritingPrettyPrinted"的话有换位符\n；是"0"的话没有换位符\n。
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:0 error:&error];
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        return jsonString;
    }
    
    return nil;
}


//设置好友消息提醒
-(void)muteMessage:(NSString *)contactId mute:(NSString *)mute Succ:(Success)succ Err:(Errors)err{
    BOOL on;
    if ([mute isEqualToString:@"1"]) {
        on = true;
    }else{
        on = false;
    }
    [[NIMSDK sharedSDK].userManager updateNotifyState:on forUser:contactId completion:^(NSError *error) {
        if (!error) {
            succ(@"200");
        }else{
            err(@"操作失败");
        }
    }];
}

-(void)handleInComeMultiMediaMessage:(NIMMessage *)message callFrom:(NSString *)callFrom
{    
    if ([message.session.sessionId isEqual:self._session.sessionId] && [callFrom isEqual:@"NIMViewController"]) return;
    
    if ([message.remoteExt objectForKey:@"parentId"] == nil) return;
    
    NSString *parentMediaId = [message.remoteExt objectForKey:@"parentId"];
    NIMMessageSearchOption *option = [[NIMMessageSearchOption alloc] init];
    option.searchContent = parentMediaId;
    
    [[NIMSDK sharedSDK].conversationManager searchMessages:message.session option:option result:^(NSError * _Nullable error, NSArray<NIMMessage *> * __nullable messages) {        
        BOOL isParentMessageExits = NO;
        
        for (NIMMessage *messageResult in messages) {
            if ([messageResult.text isEqual:parentMediaId]) {
                isParentMessageExits = YES;
                break;
            }
        }
        if (isParentMessageExits == YES) return;
        NIMMessage *localMessage = [[NIMMessage alloc] init];
        localMessage.text    = parentMediaId;
        localMessage.timestamp = message.timestamp - 1;
        localMessage.localExt = @{@"isLocalMsg": @YES, @"parentMediaId": parentMediaId };
        localMessage.from = message.from;
        NIMMessageSetting *seting = [[NIMMessageSetting alloc]init];
        seting.apnsEnabled = NO;
        seting.shouldBeCounted = NO;
        localMessage.setting = seting;
        
        [[NIMSDK sharedSDK].conversationManager saveMessage:localMessage forSession:message.session completion:^(NSError * _Nullable error) {
            if (error != nil) {
                NSLog(@"?????? %@", error);
                return;
            }
            
            [[NIMSDK sharedSDK].conversationManager updateMessage:localMessage forSession:message.session completion:^(NSError * _Nullable error) {
                if (error != nil) {
                    NSLog(@"?????? %@", error);
                    return;
                }
            }];
        }];
    }];
}

#pragma mark - NIMChatManagerDelegate

- (void)willSendMessage:(NIMMessage *)message
{
    [self setLocalExtMessage:message key:@"isReplaceSuccess" value:@"NO"];
    [self setLocalExtMessage:message key:@"downloadAttStatus" value:@"unDownload"];
    
    [self refrashMessage:message From:@"send"];
    NIMModel *model = [NIMModel initShareMD];
    model.startSend = @{@"start":@"true"};
}
//发送结果
- (void)sendMessage:(NIMMessage *)message didCompleteWithError:(NSError *)error
{
    NSLog(@"sendMessage didCompleteWithError %@", error);
    if (!error) {
        [self refrashMessage:message From:@"send"];
        [[NSUserDefaults standardUserDefaults]setObject: [NSString stringWithFormat:@"%f", message.timestamp] forKey:@"timestamp"];
    }else{
        NSDictionary *userInfo = error.userInfo;
        NSString *strEnum = [userInfo objectForKey:@"enum"];
        if ([strEnum isEqualToString:@"NIMRemoteErrorCodeInBlackList"]) {
            NSString * tip = @"消息已发出，但被对方拒收了";
            NIMMessage *tipMessage = [self msgWithTip:tip];
            tipMessage.timestamp = message.timestamp;
            [[NIMSDK sharedSDK].conversationManager saveMessage:tipMessage forSession:self._session completion:nil];
        }
        
        message.localExt = @{@"isFriend":@"NO"};
        
        [[NIMSDK sharedSDK].conversationManager updateMessage:message forSession:self._session completion:nil];
        [self refrashMessage:message From:@"send"];
    }
    NIMModel *model = [NIMModel initShareMD];
    if ([[NSString stringWithFormat:@"%@", error] isEqualToString:@"(null)"]) {
        model.endSend = @{@"end":@"true",@"error":@""};
    }else{
        model.endSend = @{@"end":@"true",@"error":[NSString stringWithFormat:@"%@", error]};
    }
}

//发送进度
-(void)sendMessage:(NIMMessage *)message progress:(float)progress
{
    [self refrashMessage:message From:@"send" ];
    NIMModel *model = [NIMModel initShareMD];
    model.endSend = @{@"progress":[NSString stringWithFormat:@"%f",progress]};
}


//接收消息
- (void)onRecvMessages:(NSArray *)messages
{
    
    NIMMessage *message = messages.firstObject;
    
    NSLog(@"onRecvMessages >>>>> %@", message);
    
    if ([message.session.sessionId isEqualToString:_sessionID]) {
        [self handleInComeMultiMediaMessage: message callFrom:@""];

        [self refrashMessage:message From:@"receive" ];
        NIMMessageReceipt *receipt = [[NIMMessageReceipt alloc] initWithMessage:message];
        NIMRecentSession *recent = [[NIMSDK sharedSDK].conversationManager recentSessionBySession:receipt.session];
        NSDictionary *localExt = recent.localExt?:@{};
        
        NSMutableDictionary *dict = [localExt mutableCopy];
        
        [dict setObject:message.messageId forKey:@"lastReadMessageId"];
        [[NIMSDK sharedSDK].conversationManager updateRecentLocalExt:dict recentSession:recent];
        
        if ([self isSeenMessage]) {
            if (message.session.sessionType == NIMSessionTypeTeam) {
                [[[NIMSDK sharedSDK] chatManager] sendTeamMessageReceipts:@[receipt] completion:nil];
            } else {
                [[[NIMSDK sharedSDK] chatManager] sendMessageReceipt:receipt completion:nil];
            }
        }
        
        //标记已读消息
        [[NIMSDK sharedSDK].conversationManager markAllMessagesReadInSession:self._session];
        
        
        if (![message.from isEqualToString:[NIMSDK sharedSDK].loginManager.currentAccount]) {
            [self playTipsMusicWithMessage:message];
        }
    }
}

- (void)onRecvMessageReceipts:(NSArray<NIMMessageReceipt *> *)receipts
{
    for (NIMMessageReceipt *receipt in receipts) {
        NSArray *messageArr =  [[[NIMSDK sharedSDK] conversationManager]messagesInSession:receipt.session message:nil limit: 1];
        
        NIMModel *model = [NIMModel initShareMD];
                
        model.ResorcesArr = [self setTimeArr:messageArr]; // onObserveReceiveMessage

//        NSLog(@"onRecv MessageReceipts session %@", receipt.session);
//        NSLog(@"onRecv MessageReceipts messageId %@", receipt.messageId);
//        NSLog(@"onRecv MessageReceipts teamReceiptInfo %@", receipt.teamReceiptInfo);

//        [messageIds addObject: receipt.messageId];
//
//        if (receipt.teamReceiptInfo != nil) {
//            NSLog(@"receipt teamInfo %@", receipt.teamReceiptInfo);
//        }
    }
    
//    NSArray<NIMMessage *> *currentMessage = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:self._session messageIds:messageIds];
//
//    NSMutableArray *messages = [self setTimeArr:currentMessage];
//
//
}

- (void)playTipsMusicWithMessage:(NIMMessage *)message{
    BOOL needToPlay = NO;
    if (message.messageType == 100) {
        NIMCustomObject *customObject = message.messageObject;
        DWCustomAttachment *obj = customObject.attachment;
        if (obj.custType == CustomMessgeTypeRedPacketOpenMessage){
            return;
        }else if(obj.custType == CustomMessgeTypeRedpacket){//红包消息
            [self.player stop];
            [self.redPacketPlayer stop];
            [[AVAudioSession sharedInstance] setCategory: AVAudioSessionCategoryAmbient error:nil];
            [self.redPacketPlayer play];
            return;
        }
    }
    if (message.messageType == NIMMessageTypeNotification) return;
    if (message.session.sessionType == NIMSessionTypeP2P) {//个人
        NIMUser *user = [[NIMSDK sharedSDK].userManager userInfo:message.session.sessionId];
        needToPlay = user.notifyForNewMsg;
        
    }else if(message.session.sessionType == NIMSessionTypeTeam){//群
        
        NIMTeam *team = [[[NIMSDK sharedSDK] teamManager]teamById:message.session.sessionId];
        needToPlay = team.notifyStateForNewMsg == NIMTeamNotifyStateAll ? YES : NO;
    }
    if (needToPlay) {
        [self.player stop];
        [self.redPacketPlayer stop];
        [[AVAudioSession sharedInstance] setCategory: AVAudioSessionCategoryAmbient error:nil];
        [self.player play];
    }
}

- (void)fetchMessageAttachment:(NIMMessage *)message progress:(float)progress
{
    NSLog(@"下载图片");
    //    if ([message.session isEqual:_session]) {
    //        [self.interactor updateMessage:message];
    //    }
}

- (void)fetchMessageAttachment:(NIMMessage *)message didCompleteWithError:(NSError *)error
{
//    NIMVideoObject *object = message.messageObject;
//    [self setLocalExtMessage:message key:@"downloadAttStatus" value:@"downloadSuccess"];
    [self refrashMessage:message From:@"receive"];
//    [[NSNotificationCenter defaultCenter]postNotificationName:@"RNNeteaseimDidCompletePic" object:nil];
    //    if ([message.session isEqual:_session]) {
    //        NIMMessageModel *model = [self.interactor findMessageModel:message];
    //        //下完缩略图之后，因为比例有变化，重新刷下宽高。
    //        [model calculateContent:self.tableView.frame.size.width force:YES];
    //        [self.interactor updateMessage:message];
    //    }
}

- (void)onRecvMessageReceipt:(NIMMessageReceipt *)receipt
{
    
    NIMModel *mode = [NIMModel initShareMD];
    mode.receipt = @"1";
}

#pragma mark - NIMMediaManagerDelegate
- (void)recordAudio:(NSString *)filePath didBeganWithError:(NSError *)error {
    if (!filePath || error) {
        [self onRecordFailed:error];
    }
    NSLog(@"recordAudio ^^^^%@",error);
}

- (void)recordAudio:(NSString *)filePath didCompletedWithError:(NSError *)error {
    if(!error) {
        if ([self recordFileCanBeSend:filePath]) {
            [[[NIMSDK sharedSDK] chatManager] sendMessage:[NIMMessageMaker msgWithAudio:filePath andeSession:self._session senderName:_myUserName] toSession:self._session error:nil];
        }else{
            [self showRecordFileNotSendReason];
        }
    } else {
        NSLog(@"^^^^%@",error);
    }
}

- (void)recordAudioDidCancelled {
    
}
//监听录音状态
- (void)recordAudioProgress:(NSTimeInterval)currentTime{
    NIMModel *model = [NIMModel initShareMD];
    NSDictionary *Audic = @{@"currentTime":[NSString stringWithFormat:@"%f",currentTime],@"recordPower":[NSString stringWithFormat:@"%f",[[NIMSDK sharedSDK].mediaManager recordPeakPower]]};
    model.audioDic = Audic;
}
//播放结束回调
- (void)playAudio:(NSString *)filePath didBeganWithError:(nullable NSError *)error{
    NSLog(@"didBeganWithError");
    if(!error) {
        NIMModel *model = [NIMModel initShareMD];
        NSDictionary *Audic = @{@"status":@"start"};
        model.audioDic = Audic;
    } else {
        NSLog(@"%@",error);
    }
}

//播放结束回调
- (void)playAudio:(NSString *)filePath didCompletedWithError:(nullable NSError *)error{
    NSLog(@"didCompletedWithError");

    if(!error) {
        NIMModel *model = [NIMModel initShareMD];
        NSDictionary *Audic = @{@"status":@"completed"};
        model.audioDic = Audic;
    } else {
        NSLog(@"%@",error);
    }
}

- (void)playAudio:(NSString *)filePath progress:(float)value {
    NSLog(@"progress");

    NIMModel *model = [NIMModel initShareMD];
    NSDictionary *Audic = @{@"status":@"progress", @"current": @(value)};
    model.audioDic = Audic;
}

- (void)stopPlayAudio:(NSString *)filePath didCompletedWithError:(nullable NSError *)error {
    NSLog(@"stopPlayAudio didBeganWithError");
    if(!error) {
        NIMModel *model = [NIMModel initShareMD];
        NSDictionary *Audic = @{@"status":@"stop"};
        model.audioDic = Audic;
    } else {
        NSLog(@"%@",error);
    }
}

- (void)recordAudioInterruptionBegin {
    [[NIMSDK sharedSDK].mediaManager cancelRecord];
}
#pragma mark - 录音相关接口
- (void)onRecordFailed:(NSError *)error{}

- (BOOL)recordFileCanBeSend:(NSString *)filepath
{
    return YES;
}

- (void)showRecordFileNotSendReason{}


#pragma mark - NIMConversationManagerDelegate
- (void)messagesDeletedInSession:(NIMSession *)session{
    //    [self.interactor resetMessages];
    //    [self.tableView reloadData];
}

- (void)didAddRecentSession:(NIMRecentSession *)recentSession
           totalUnreadCount:(NSInteger)totalUnreadCount{
    [self changeUnreadCount:recentSession totalUnreadCount:totalUnreadCount];
}

- (void)didUpdateRecentSession:(NIMRecentSession *)recentSession
              totalUnreadCount:(NSInteger)totalUnreadCount{
    [self changeUnreadCount:recentSession totalUnreadCount:totalUnreadCount];
}

- (void)didRemoveRecentSession:(NIMRecentSession *)recentSession
              totalUnreadCount:(NSInteger)totalUnreadCount{
    [self changeUnreadCount:recentSession totalUnreadCount:totalUnreadCount];
}


- (void)changeUnreadCount:(NIMRecentSession *)recentSession
         totalUnreadCount:(NSInteger)totalUnreadCount{
    
    //    if ([recentSession.session isEqual:self.session]) {
    //        return;
    //    }
    //    [self changeLeftBarBadge:totalUnreadCount];
}

- (void)addListener
{
    [[NIMSDK sharedSDK].chatManager addDelegate:self];
    [[NIMSDK sharedSDK].conversationManager addDelegate:self];
    [[NIMSDK sharedSDK].systemNotificationManager addDelegate:self];
}


#pragma mark - NIMSystemNotificationManagerDelegate
- (void)onReceiveCustomSystemNotification:(NIMCustomSystemNotification *)notification
{
    if (!notification.sendToOnlineUsersOnly) {
        return;
    }
    NSData *data = [[notification content] dataUsingEncoding:NSUTF8StringEncoding];
    if (data) {
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data
                                                             options:0
                                                               error:nil];
        if ([dict jsonInteger:NTESNotifyID] == NTESCommandTyping && self._session.sessionType == NIMSessionTypeP2P && [notification.sender isEqualToString:self._session.sessionId])
        {
            NSLog(@"正在输入...");
        }
    }
}


-(void)refrashMessage:(NIMMessage *)message From:(NSString *)from {
    NSMutableArray *messageArr = [NSMutableArray array];
    NSMutableDictionary *dic2 = [NSMutableDictionary dictionary];
    NIMUser   *user = [[NIMSDK sharedSDK].userManager userInfo:message.from];
    NSMutableDictionary *fromUser = [NSMutableDictionary dictionary];
    NIMRecentSession *recent = [[NIMSDK sharedSDK].conversationManager recentSessionBySession:message.session];
    NSNumber *isChatBotNumber = [recent.localExt objectForKey:@"isChatBot"];
    NSNumber *isCsrNumber = [recent.localExt objectForKey:@"isCsr"];
    BOOL isChatBot = [isChatBotNumber boolValue];
    BOOL isCsr = [isCsrNumber boolValue];
    
    if (recent.localExt != nil && [user.userId isEqual:message.session.sessionId]) {
        [fromUser setObject:[NSString stringWithFormat:@"%@", isChatBot ? @"true" : @"false"] forKey:@"isChatBot"];
        [fromUser setObject:[NSString stringWithFormat:@"%@", isCsr ? @"true" : @"false"] forKey:@"isCsr"];
    }
    
    NSDictionary *localExt = message.localExt;
            
    if (localExt != nil) {
        [dic2 setObject:localExt forKey:@"localExt"];
    }
    
    [fromUser setObject:[NSString stringWithFormat:@"%@",user.userInfo.avatarUrl] forKey:@"avatar"];
    NSString *strAlias = user.alias;
    if (strAlias.length) {
        [fromUser setObject:strAlias forKey:@"name"];
    }else if(user.userInfo.nickName.length){
        [fromUser setObject:[NSString stringWithFormat:@"%@",user.userInfo.nickName] forKey:@"name"];
    }else{
        [fromUser setObject:[NSString stringWithFormat:@"%@",user.userId] forKey:@"name"];
    }
    [fromUser setObject:[NSString stringWithFormat:@"%@", message.from] forKey:@"_id"];
    NSArray *key = [fromUser allKeys];
    for (NSString *tem  in key) {
        if ([[fromUser objectForKey:tem] isEqualToString:@"(null)"]) {
            [fromUser setObject:@"" forKey:tem];
        }
    }
    [dic2 setObject:[NSString stringWithFormat:@"%@", message.text] forKey:@"text"];
    [dic2 setObject:[NSString stringWithFormat:@"%@", message.session.sessionId] forKey:@"sessionId"];
    [dic2 setObject:[NSString stringWithFormat:@"%ld", message.session.sessionType] forKey:@"sessionType"];
    
    [dic2 setObject:[NSString stringWithFormat:@"%d", message.isRemoteRead] forKey:@"isRemoteRead"];

    switch (message.deliveryState) {
        case NIMMessageDeliveryStateFailed:
            [dic2 setObject:@"send_failed" forKey:@"status"];
            break;
        case NIMMessageDeliveryStateDelivering:
            [dic2 setObject:@"send_going" forKey:@"status"];
            break;
        case NIMMessageDeliveryStateDeliveried:
            [dic2 setObject:@"send_succeed" forKey:@"status"];
            break;
        default:
            [dic2 setObject:@"send_failed" forKey:@"status"];
            break;
    }
    
    NSString *strSessionId = self._session.sessionId;
    if (message.session.sessionType == NIMSessionTypeP2P && ![[NIMSDK sharedSDK].userManager isMyFriend:strSessionId] && !isCsr && !isChatBot) {
            [dic2 setObject:@"send_failed" forKey:@"status"];
    }
    
    [dic2 setObject: [NSNumber numberWithBool:message.isOutgoingMsg] forKey:@"isOutgoing"];
    [dic2 setObject:[NSString stringWithFormat:@"%f", message.timestamp] forKey:@"timeString"];
    [dic2 setObject:[NSNumber numberWithBool:NO] forKey:@"isShowTime"];
    [dic2 setObject:[NSString stringWithFormat:@"%@", message.messageId] forKey:@"msgId"];
    [dic2 setObject:fromUser forKey:@"fromUser"];
    if (message.messageType == NIMMessageTypeText) {
        [dic2 setObject:@"text" forKey:@"msgType"];
        
        NSLog(@"message exten =>> %@", message.remoteExt);
        if ([[message.remoteExt objectForKey:@"extendType"]  isEqual: @"forwardMultipleText"]) {
            NSMutableDictionary *extend = [NSMutableDictionary dictionary];
            [extend setObject:message.text forKey:@"messages"];
            
            [dic2 setObject:extend forKey:@"extend"];
            [dic2 setObject:@"forwardMultipleText" forKey:@"msgType"];
        }
        
        if ([[message.remoteExt objectForKey:@"extendType"]  isEqual: @"card"]) {
            [dic2 setObject:message.remoteExt forKey:@"extend"];
            [dic2 setObject:@"card" forKey:@"msgType"];
        }
        
        if ([[message.remoteExt objectForKey:@"extendType"]  isEqual: @"gif"]) {
            [dic2 setObject:message.remoteExt forKey:@"extend"];
            [dic2 setObject:@"image" forKey:@"msgType"];
        }
        
        if ([[message.remoteExt objectForKey:@"extendType"]  isEqual: @"TEAM_NOTIFICATION_MESSAGE"]) {
            [dic2 setObject:message.remoteExt forKey:@"extend"];
            [dic2 setObject:@"notification" forKey:@"msgType"];
        }
    }else if (message.messageType  == NIMMessageTypeImage) {
        [dic2 setObject:@"image" forKey:@"msgType"];
        
        [dic2 setObject:[self makeExtendImage:message] forKey:@"extend"];
    }
    else if (message.messageType  == NIMMessageTypeFile) {
        [dic2 setObject:@"file" forKey:@"msgType"];

        [dic2 setObject:[self makeExtendFile:message] forKey:@"extend"];
    }
    else if(message.messageType == NIMMessageTypeAudio){
        [dic2 setObject:@"voice" forKey:@"msgType"];
       
        [dic2 setObject:[self makeExtendRecord:message] forKey:@"extend"];
    }else  if(message.messageType == NIMMessageTypeVideo ){
        [dic2 setObject:@"video" forKey:@"msgType"];
        
        [dic2 setObject:[self makeExtendVideo:message] forKey:@"extend"];
    }else if(message.messageType == NIMMessageTypeLocation){
        [dic2 setObject:@"location" forKey:@"msgType"];
        NIMLocationObject *object = message.messageObject;
        NSMutableDictionary *locationObj = [NSMutableDictionary dictionary];
        [locationObj setObject:[NSString stringWithFormat:@"%f", object.latitude ] forKey:@"latitude"];
        [locationObj setObject:[NSString stringWithFormat:@"%f", object.longitude ] forKey:@"longitude"];
        [locationObj setObject:[NSString stringWithFormat:@"%@", object.title ] forKey:@"title"];
        [dic2 setObject:locationObj forKey:@"extend"];
        
    }else if(message.messageType == NIMMessageTypeTip){//提醒类消息
        [dic2 setObject:@"notification" forKey:@"msgType"];
        NSMutableDictionary *notiObj = [NSMutableDictionary dictionary];
        [notiObj setObject:message.text forKey:@"tipMsg"];
        [dic2 setObject:notiObj forKey:@"extend"];
    }else if (message.messageType == NIMMessageTypeNotification) {
        [dic2 setObject:@"notification" forKey:@"msgType"];
        [dic2 setObject:[self setNotiTeamObj:message] forKey:@"extend"];
    }else if (message.messageType == NIMMessageTypeCustom) {
        NIMCustomObject *customObject = message.messageObject;
        DWCustomAttachment *obj = customObject.attachment;
        if (obj) {
            switch (obj.custType) {
//                case CustomMessageTypeFowardMultipleText: //红包
//                {
//                    [dic2 setObject:obj.dataDict forKey:@"extend"];
//                    [dic2 setObject:@"forwardMultipleText" forKey:@"msgType"];
//                }
//                    break;
                case CustomMessgeTypeRedpacket: //红包
                {
                    [dic2 setObject:obj.dataDict forKey:@"extend"];
                    [dic2 setObject:@"redpacket" forKey:@"msgType"];
                }
                    break;
                case CustomMessgeTypeBankTransfer: //转账
                {
                    [dic2 setObject:obj.dataDict  forKey:@"extend"];
                    [dic2 setObject:@"transfer" forKey:@"msgType"];
                }
                    break;
                case CustomMessgeTypeRedPacketOpenMessage: //拆红包消息
                {
                    NSDictionary *dataDict = [self dealWithData:obj.dataDict];
                    if (dataDict) {
                        [dic2 setObject:dataDict  forKey:@"extend"];
                        [dic2 setObject:@"redpacketOpen" forKey:@"msgType"];
                    }else{
                        return;
                    }
                }
                    break;
                    
                case CustomMessgeTypeAccountNotice: //账户通知，与账户金额相关变动
                case CustomMessgeTypeUrl: //链接
                {
                    [dic2 setObject:[NSString stringWithFormat:@"%d",message.isRemoteRead] forKey:@"isRemoteRead"];
//                    [dic2 setObject:[NSString stringWithFormat:@"%ld", message.messageType] forKey:@"msgType"];
                    if (obj.custType == CustomMessgeTypeAccountNotice) {
                        [dic2 setObject:obj.dataDict  forKey:@"extend"];
                        [dic2 setObject:@"account_notice" forKey:@"msgType"];
                    }else{
                        [dic2 setObject:obj.dataDict  forKey:@"extend"];
                        [dic2 setObject:@"url" forKey:@"msgType"];
                    }
                }
                    break;
                case CustomMessgeTypeBusinessCard://名片
                {
                    [dic2 setObject:obj.dataDict  forKey:@"extend"];
                    [dic2 setObject:@"card" forKey:@"msgType"];
                }
                    break;
                case CustomMessgeTypeCustom://自定义
                {
                    [dic2 setObject:obj.dataDict  forKey:@"extend"];
                    [dic2 setObject:@"custom" forKey:@"msgType"];
                }
                    break;
                default:
                {
                    if (obj.dataDict != nil) {
                        [dic2 setObject:obj.dataDict  forKey:@"extend"];
                    }
                    [dic2 setObject:@"unknown" forKey:@"msgType"];
                }
                    break;
            }
        }
    }else{
        [dic2 setObject:@"unknown" forKey:@"msgType"];
        NSMutableDictionary *unknowObj = [NSMutableDictionary dictionary];
        [dic2 setObject:unknowObj  forKey:@"extend"];
    }
    [messageArr addObject:dic2];
    //接收消息
    NIMModel *model = [NIMModel initShareMD];
    if ([from isEqualToString:@"receive"]) {
        model.ResorcesArr = messageArr;
    }else if ([from isEqualToString:@"send"]){
        //发送消息
        model.sendState = messageArr;
    }
}
//处理拆红包消息
- (NSDictionary *)dealWithData:(NSDictionary *)dict{
    NSString *strOpenId = [self stringFromKey:@"openId" andDict:dict];
    NSString *strSendId = [self stringFromKey:@"sendId" andDict:dict];
    NSString *strNo = [self stringFromKey:@"serialNo" andDict:dict];
    NSString *strMyId = [NIMSDK sharedSDK].loginManager.currentAccount;
    NSString *strContent;
    NSString *lastString = @"";
    NSInteger hasRedPacket = [[dict objectForKey:@"hasRedPacket"] integerValue];
    if (hasRedPacket == 1) {//红包已领完
        lastString = @"，你的红包已被领完";
    }
    if ([strOpenId isEqualToString:strMyId]&&[strSendId isEqualToString:strMyId]) {
        strContent = [NSString stringWithFormat:@"你领取了自己发的红包%@",lastString ];
    }else if ([strOpenId isEqualToString:strMyId]){
        NSString *strSendName = [self getUserName:strSendId];
        strContent = [NSString stringWithFormat:@"你领取了%@的红包",strSendName];
    }else if([strSendId isEqualToString:strMyId]){
        NSString *strOpenName = [self getUserName:strOpenId];
        strContent = [NSString stringWithFormat:@"%@领取了你的红包%@",strOpenName,lastString];
    }else{//别人发的别人领的
        return nil;
    }
    NSDictionary *dataDict = @{@"tipMsg":strContent,@"serialNo":strNo};
    return dataDict;
}

- (NSString *)getUserName:(NSString *)userID{
    NSString *strTmpName = @"";
    NIMUser *user = [[NIMSDK sharedSDK].userManager userInfo:userID];
    strTmpName = user.alias;
    if (![strTmpName length]) {
            strTmpName = user.userInfo.nickName;
    }
    if (![strTmpName length]) {//从服务器获取
        [[ContactViewController initWithContactViewController]fetchUserInfos:userID Success:^(id param) {

        } error:^(NSString *error) {

        }];
        strTmpName = userID;
    }
    return strTmpName;
}


- (NSString *)stringFromKey:(NSString *)strKey andDict:(NSDictionary *)dict{
    NSString *text = [dict objectForKey:strKey];
    return text?text:@" ";
}



-(void)updateActionHideRecentSession:(NSString *)sessionId sessionType:(NSString *)sessionType isHideSession:(BOOL *)isHideSession isPinCode:(BOOL *)isPinCode success:(Success)success error:(Errors)error {
    NIMSession *session = [NIMSession session:sessionId type:[sessionType intValue]];
    NIMRecentSession *recent = [[NIMSDK sharedSDK].conversationManager recentSessionBySession:session];
    
    if (recent == nil) {
        [self addEmptyRecentSession:sessionId sessionType:sessionType];
    }
    
    recent = [[NIMSDK sharedSDK].conversationManager recentSessionBySession:session];
    
    if (recent) {
        NSMutableDictionary *dict = recent.localExt ? [recent.localExt mutableCopy] : [[NSMutableDictionary alloc] init];
        
        if (isHideSession) {
            [dict setObject:[NSNumber numberWithBool:YES] forKey:@"isHideSession"];
            [dict setObject:[NSNumber numberWithBool:isPinCode] forKey:@"isPinCode"];
        } else {
            [dict setObject:[NSNumber numberWithBool:NO] forKey:@"isHideSession"];
            [dict setObject:[NSNumber numberWithBool:NO] forKey:@"isPinCode"];
        }
        
        [[NIMSDK sharedSDK].conversationManager updateRecentLocalExt:dict recentSession:recent];
        
        success(@"success");
        return;
    }
    
    error(@"error");
}

-(void)updateRecentSessionIsCsrOrChatbot:(NSString *)sessionId type:(NSString *)type name:(NSString *)name {
    NIMSession *session = [NIMSession session:sessionId type:NIMSessionTypeP2P];
    NIMRecentSession *recent = [[NIMSDK sharedSDK].conversationManager recentSessionBySession:session];
    if (recent) {
        BOOL isChatBot = NO;
        BOOL isCsr = NO;
        
        if ([type  isEqual: @"chatbot"]) {
            isChatBot = YES;
        } else if ([type isEqual:@"csr"]) {
            isCsr = YES;
        }
        
        NSDictionary *localExt = recent.localExt?:@{};
        NSMutableDictionary *dict = [localExt mutableCopy];
        [dict setObject:@(isCsr) forKey:@"isCsr"];
        [dict setObject:@(isChatBot) forKey:@"isChatBot"];
        [dict setObject:@(YES) forKey:@"isUpdated"];
        
        if (name) {
            [dict setObject:[NSString stringWithFormat:@"%@", name] forKey:@"name"];
        }
        
        [[NIMSDK sharedSDK].conversationManager updateRecentLocalExt:dict recentSession:recent];
    }
}

-(void)cancelSendingMessage:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId success:(Success)success err:(Errors)err {
    NIMSession *session = [NIMSession session:sessionId type:[sessionType intValue]];
    NSArray *messages = [[NIMSDK sharedSDK].conversationManager messagesInSession:session messageIds:@[messageId]];
    NIMMessage *message = messages.firstObject;
    
    if ([[NIMSDK sharedSDK].chatManager cancelSendingMessage:message]) {
        [[NIMSDK sharedSDK].conversationManager deleteMessage:message];
        success(@"success");
    } else {
        err(@"error");
    }
}

-(void)addEmptyRecentSession:(NSString *)sessionId sessionType:(NSString *)sessionType {
    NIMSession *session = [NIMSession session:sessionId type:[sessionType integerValue]];
    [[NIMSDK sharedSDK].conversationManager addEmptyRecentSessionBySession:session];
}

//转发消息
-(void)forwardMessage:(NSArray *)messageIds sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType content:(NSString *)content parentId:(NSString *)parentId isHaveMultiMedia:(BOOL *)isHaveMultiMedia success:(Success)succe{
    NIMSession *session = [NIMSession session:sessionId type:[sessionType integerValue]];
    
    if (parentId != nil && isHaveMultiMedia) {
        NIMMessage *message = [[NIMMessage alloc] init];
        message.text    = parentId;
        message.localExt = @{@"isLocalMsg": @YES, @"parentMediaId": parentId };
        NIMMessageSetting *seting = [[NIMMessageSetting alloc]init];
        seting.apnsEnabled = NO;
        seting.shouldBeCounted = NO;
        message.setting = seting;
        
        [[NIMSDK sharedSDK].conversationManager saveMessage:message forSession:session completion:^(NSError * _Nullable error) {
        }];
    }
    
    NSArray *currentMessages = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:self._session messageIds:messageIds];
    //    NIMMessage *message = currentMessage[0];
   
    for (NIMMessage *message in currentMessages) {
        if ([message.remoteExt objectForKey:@"parentId"] != nil) {
            NSMutableDictionary *msgRemoteExt = [[NSMutableDictionary alloc] initWithDictionary:message.remoteExt];

            if (isHaveMultiMedia) {
                [msgRemoteExt setObject:parentId forKey:@"parentId"];
            } else {
                [msgRemoteExt removeObjectForKey:@"parentId"];
            }
            
            message.remoteExt = msgRemoteExt;

        }
        
        [[NIMSDK sharedSDK].chatManager forwardMessage:message toSession:session error:nil];
    }
    
    //发送消息
    if([content length] != 0){
        NIMMessage *messages = [[NIMMessage alloc] init];
        messages.text    = content;
        
        [[NIMSDK sharedSDK].chatManager sendMessage:messages toSession:session error:nil];
    }
    succe(@"已发送");
}
//撤回消息
-(void)revokeMessage:(NSString *)messageId success:(Success)succe Err:(Errors)err{
    NSArray *currentMessage = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:self._session messageIds:@[messageId]];
    NIMMessage *currentmessage = currentMessage[0];
//    __weak typeof(self) weakSelf = self;
    
//    NIMRevokeMessageOption *option;
//    option.apnsContent = @"";
//    option.shouldBeCounted = NO;

//    [[NIMSDK sharedSDK].chatManager revokeMessage:currentmessage completion:^(NSError * _Nullable error) {
//        WARNIING: DOT NOT DELETE THIS COMMENT CODE
//        if (error) {
//            if (error.code == NIMRemoteErrorCodeDomainExpireOld) {
//                err(@"expired");
//            }else{
//                err(@"fail");
//            }
//        }
//        else
//        {
//            succe(@"success");
//            
//            NSString *tip = [self tipOnMessageRevoked:currentmessage];
//            
//            NIMMessage *tipMessage = [self msgWithTip:tip];
//            tipMessage.timestamp = currentmessage.timestamp;
//            
//            NSDictionary *remoteExt = @{@"extendType": @"revoked_success"};
//            tipMessage.remoteExt = remoteExt;
//        
//            NSDictionary *deleteDict = @{@"msgId":messageId};
//            [NIMModel initShareMD].deleteMessDict = deleteDict;
//
//            // saveMessage 方法执行成功后会触发 onRecvMessages: 回调，但是这个回调上来的 NIMMessage 时间为服务器时间，和界面上的时间有一定出入，所以要提前先在界面上插入一个和被删消息的界面时间相符的 Tip, 当触发 onRecvMessages: 回调时，组件判断这条消息已经被插入过了，就会忽略掉。
//            [[NIMSDK sharedSDK].conversationManager saveMessage:tipMessage forSession:self._session completion:nil];
//        }
//    }];
    
    BOOL isOutOfTime;
    NSTimeInterval currentTime = [[NSDate date] timeIntervalSince1970];
    NSTimeInterval timeInterval = currentTime - currentmessage.timestamp;
    
    if (timeInterval > 300) {
        isOutOfTime = YES;
    } else {
        isOutOfTime = NO;
    }
    
    if (isOutOfTime) {
        err(@"expired");
    }
    else
    {
        
//        send custom notification
        NSDictionary *dataDict = @{
            @"data":@{
            @"type":@(1),
            @"messageId":messageId,
            @"sessionId":self._session.sessionId,
            @"isObserveReceiveRevokeMessage": @(YES)}
        };
        
        NSString *content = [self jsonStringWithDictionary:dataDict];
        
        NIMCustomSystemNotification *notifi = [[NIMCustomSystemNotification alloc]initWithContent:content];
        
        [[NIMSDK sharedSDK].systemNotificationManager sendCustomNotification:notifi toSession:self._session completion:nil];
//        save tip message
        NSString *tip = [self tipOnMessageRevoked:currentmessage];
        
        NIMMessage *tipMessage = [self msgWithTip:tip];
        tipMessage.timestamp = currentmessage.timestamp;
        
        NSDictionary *remoteExt = @{@"extendType": @"revoked_success"};
        tipMessage.remoteExt = remoteExt;
        
        [[NIMSDK sharedSDK].conversationManager deleteMessage:currentmessage];
        
        // saveMessage 方法执行成功后会触发 onRecvMessages: 回调，但是这个回调上来的 NIMMessage 时间为服务器时间，和界面上的时间有一定出入，所以要提前先在界面上插入一个和被删消息的界面时间相符的 Tip, 当触发 onRecvMessages: 回调时，组件判断这条消息已经被插入过了，就会忽略掉。
        [[NIMSDK sharedSDK].conversationManager saveMessage:tipMessage forSession:self._session completion:nil];
        
        succe(@"success");
    }
}

-(void)sendCustomNotification:(NSDictionary *)dataDict toSessionId:(NSString*)toSessionId toSessionType:(NSString*)toSessionType success:(Success)succe Err:(Errors)err{
    NIMSession *session = [NIMSession session:toSessionId type:[toSessionType intValue]];
    
    NSString *content = [self jsonStringWithDictionary:dataDict];
    NIMCustomSystemNotification *notifi = [[NIMCustomSystemNotification alloc]initWithContent:content];
    [[NIMSDK sharedSDK].systemNotificationManager sendCustomNotification:notifi toSession:session completion:^(NSError *error) {
        if (error) {
            err(@"Send custom notification failed");
        }else{
            succe(@"success");
        }
    }];
}

-(void)removeMessage:(NSString *)messageId sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType {
    NIMSession *session = [NIMSession session:sessionId type:[sessionType intValue]];
    NSArray *messages = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:session messageIds:@[messageId]];
    NIMMessage *message = messages[0];
    
    [[NIMSDK sharedSDK].conversationManager deleteMessage:message];
}

//删除一条信息
-(void)deleteMsg:(NSString *)messageId{
    NSArray *currentMessage = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:self._session messageIds:@[messageId]];
    NIMMessage *message = currentMessage[0];
    [[NIMSDK sharedSDK].conversationManager deleteMessage:message];
}
//清空聊天记录
-(void)clearMsg:(NSString *)contactId type:(NSString *)type{
    NIMSession  *session = [NIMSession session:contactId type:[type integerValue]];
    NIMDeleteMessagesOption *option = [[NIMDeleteMessagesOption alloc]init];
    option.removeSession = NO;
    [[NIMSDK sharedSDK].conversationManager deleteAllmessagesInSession:session option:option];
//    [[NIMSDK sharedSDK].conversationManager deleteAllmessagesInSession:session removeRecentSession:NO];
}
- (NIMMessage *)msgWithTip:(NSString *)tip
{
    NIMMessage *message        = [[NIMMessage alloc] init];
    NIMTipObject *tipObject    = [[NIMTipObject alloc] init];
    message.messageObject      = tipObject;
    message.text               = tip;
    NIMMessageSetting *setting = [[NIMMessageSetting alloc] init];
    setting.apnsEnabled        = NO;
    setting.shouldBeCounted    = NO;
    message.setting            = setting;
    return message;
}

- (NSString *)tipOnMessageRevoked:(NIMMessage *)message
{
    NSString *fromUid = message.from;
    NIMSession *session = message.session;
    
    BOOL isFromMe = message.isOutgoingMsg;
    
    if (fromUid == nil) {
        return [NSString stringWithFormat:@"revoked_success"];
    }
    
    NSString *tip = @"你";

    if (!isFromMe) {
        switch (session.sessionType) {
            case NIMSessionTypeP2P:
                tip = [self getUserName:fromUid];
                break;
            case NIMSessionTypeTeam:{
                NIMKitInfoFetchOption *option = [[NIMKitInfoFetchOption alloc] init];
                option.session = session;
                NIMKitInfo *info = [[NIMKit sharedKit] infoByUser:fromUid option:option];
                tip = info.showName;
            }
                break;
            default:
                break;
        }
    }

    return [NSString stringWithFormat:@" %@ revoked_success", tip];
}
//麦克风权限
- (void)onTouchVoiceSucc:(Success)succ Err:(Errors)err{
    if ([[AVAudioSession sharedInstance] respondsToSelector:@selector(requestRecordPermission:)]) {
        [[AVAudioSession sharedInstance] performSelector:@selector(requestRecordPermission:) withObject:^(BOOL granted) {
            if (granted) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    succ(@"200");
                });
            }
            else {
                dispatch_async(dispatch_get_main_queue(), ^{
                    err(@"没有麦克风权限");
                });
            }
        }];
    }
}


-(void)stopSession;
{
    self._session = nil;
    [[NIMSDK sharedSDK].chatManager removeDelegate:self];
    [[NIMSDK sharedSDK].conversationManager removeDelegate:self];
    [[NIMSDK sharedSDK].systemNotificationManager removeDelegate:self];
}
//判断是不是好友
- (BOOL)isFriendToSendMessage:(NIMMessage *)message{
    if (self._session.sessionType == NIMSessionTypeP2P) {//点对点
        NSString *strSessionId = self._session.sessionId;
        if ([[NIMSDK sharedSDK].userManager isMyFriend:strSessionId]) {//判断是否为自己好友
            return YES;
        }else{
            message.localExt = @{@"isFriend":@"NO", @"isCancelResend":[NSNumber numberWithBool:YES]};
            [[NIMSDK sharedSDK].conversationManager saveMessage:message forSession:self._session completion:nil];
            NSString *strSessionName = @"";
            NIMUser *user = [[NIMSDK sharedSDK].userManager userInfo:strSessionId];
            if ([user.alias length]) {
                strSessionName = user.alias;
            }else{
                NIMUserInfo *userInfo = user.userInfo;
                strSessionName = userInfo.nickName;
            }
            
            NSString * tip = @"SEND_MESSAGE_FAILED_WIDTH_STRANGER";
            NIMMessage *tipMessage = [self msgWithTip:tip];
            tipMessage.timestamp = message.timestamp+1;
            [[NIMSDK sharedSDK].conversationManager saveMessage:tipMessage forSession:self._session completion:nil];
            return NO;
        }
    }else{
        return YES;
    }
}

@end
