//
//  NIMViewController.m
//  NIM
//
//  Created by Dowin on 2017/5/8.
//  Copyright © 2017年 Dowin. All rights reserved.
//

#import "NIMViewController.h"
#import "ContactViewController.h"
#import "ConversationViewController.h"
#import "TeamViewController.h"
#import "ChatroomViewController.h"
#import "react-native-config/RNCConfig.h"

@interface NIMViewController ()<NIMLoginManagerDelegate,NIMConversationManagerDelegate>{
    //    BOOL isLoginFailed;
    
//    NSDictionary *listUserInfo;
//    NSDictionary *listCsrOrChatbot;
//    BOOL isFetchCsrAndChatbot;
}

//@property (nonatomic, strong) NSDictionary *listUserInfo;
//@property (nonatomic, strong) NSDictionary *listCsrOrChatbot;
//@property (nonatomic, strong) BOOL *isFetchCsrAndChatbot;

@end

@implementation NIMViewController
+(instancetype)initWithController{
    static NIMViewController *nimVC = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        nimVC = [[NIMViewController alloc]init];
    });
    return nimVC;
}
-(instancetype)initWithNIMController{
    self = [super init];
    if (self) {
        
    }
    return self;
}

-(instancetype)init {
    self = [super init];
    if (self) {
        // _listUserInfo = [[NSMutableDictionary alloc] init];
        // _listCsrOrChatbot = [[NSMutableDictionary alloc] init];
        // _isFetchCsrAndChatbot = NO;
    }
    
    return self;
}

-(void)addDelegate{
    [[NIMSDK sharedSDK].loginManager addDelegate:self];
    [[NIMSDK sharedSDK].conversationManager addDelegate:self];
    
}
- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

//监听网络
#pragma mark - NIMLoginManagerDelegate
- (void)onLogin:(NIMLoginStep)step{
    NSString *strStatus = @"0";
    switch (step) {
        case NIMLoginStepLinking://连接服务器
            strStatus = @"3";
            break;
        case NIMLoginStepLinkOK://连接服务器成功
            strStatus = @"5";
            //            [self backLogin];
            [self getResouces];
            break;
        case NIMLoginStepLinkFailed://连接服务器失败
            strStatus = @"2";
            break;
        case NIMLoginStepLogining://登录
            strStatus = @"4";
            break;
        case NIMLoginStepLoginOK://登录成功
            strStatus = @"6";
            break;
        case NIMLoginStepLoginFailed://登录失败
            strStatus = @"10";
            //            isLoginFailed = YES;
            break;
        case NIMLoginStepSyncing://开始同步
            strStatus = @"13";
            [self getResouces];
            break;
        case NIMLoginStepSyncOK://同步完成
            strStatus = @"14";
            [self getResouces];
            break;
        case NIMLoginStepLoseConnection://连接断开
            strStatus = @"2";
            break;
        case NIMLoginStepNetChanged://网络切换
            strStatus = @"15";
            break;
        case NIMLoginStepLogout:
            strStatus = @"7";
            break;
        default:
            break;
    }
    //    NSLog(@"--------------------%@",strStatus);
    [NIMModel initShareMD].NetStatus = strStatus;
}
//删除一行
-(void)deleteCurrentSession:(NSString *)recentContactId andback:(ERROR)error{
    NSArray *NIMlistArr = [[NIMSDK sharedSDK].conversationManager.allRecentSessions mutableCopy];
    for (NIMRecentSession *recent in NIMlistArr) {
        if ([recent.session.sessionId isEqualToString:recentContactId]) {
            id<NIMConversationManager> manager = [[NIMSDK sharedSDK] conversationManager];
            //            [manager deleteRecentSession:recent];
            NIMDeleteMessagesOption *option = [[NIMDeleteMessagesOption alloc]init];
            option.removeSession = YES;
            [manager deleteAllmessagesInSession:recent.session option:option];
            //清除历史记录
            [self getResouces];
        }
    }
}

-(void)removeSession:(NSString *)sessionId sessionType:(NSString *)sessionType {
    NIMSession *session = [NIMSession session:sessionId type:[sessionType intValue]];
    NIMDeleteMessagesOption *option = [[NIMDeleteMessagesOption alloc] init];
    option.removeSession = YES;
    [[NIMSDK sharedSDK].conversationManager deleteAllmessagesInSession:session option:option];
}

/*
 //登录失败后重新手动登录
 - (void)backLogin{
 if (isLoginFailed) {
 isLoginFailed = NO;
 NSLog(@":%@   :%@",_strAccount,_strToken);
 [[NIMSDK sharedSDK].loginManager login:_strAccount token:_strToken completion:^(NSError * _Nullable error) {
 NSLog(@"error:%@",error);
 }];
 }
 }*/

#pragma NIMLoginManagerDelegate
-(void)onKick:(NIMKickReason)code clientType:(NIMLoginClientType)clientType
{
    
    switch (code) {
        case NIMKickReasonByClient:{//被另外一个客户端踢下线 (互斥客户端一端登录挤掉上一个登录中的客户端)
            [NIMModel initShareMD].NIMKick = @"1";
        }
            break;
        case NIMKickReasonByClientManually:{//被另外一个客户端手动选择踢下线
            [NIMModel initShareMD].NIMKick = @"3";
        }
            break;
        case NIMKickReasonByServer:{//你被服务器踢下线
            [NIMModel initShareMD].NIMKick = @"2";
        }
            break;
        default:
            break;
    }
    [[[NIMSDK sharedSDK] loginManager] logout:^(NSError *error) {
    }];
}

- (void)onAutoLoginFailed:(NSError *)error{
    
    NSLog(@"自动登录失败");
}



#pragma mark - NIMConversationManagerDelegate
- (void)didAddRecentSession:(NIMRecentSession *)recentSession
           totalUnreadCount:(NSInteger)totalUnreadCount{
    //    BOOL isMyFriend    = [[NIMSDK sharedSDK].userManager isMyFriend:recentSession.session.sessionId];
    //    BOOL isSessionP2P = recentSession.session.sessionType == NIMSessionTypeP2P;
    //
    //    if (!isMyFriend && isSessionP2P) {
    //        NSDictionary *localExt = recentSession.localExt?:@{};
    //        NSMutableDictionary *dict = [localExt mutableCopy];
    //        [dict setObject:@(NO) forKey:@"isReplyStranger"];
    //        [[NIMSDK sharedSDK].conversationManager updateRecentLocalExt:dict recentSession:recentSession];
    //    }
    
    //    [[ConversationViewController initWithConversationViewController]handleInComeMultiMediaMessage: recentSession.lastMessage callFrom:@"NIMViewController"];
    
    [self getResouces];
}


- (void)didUpdateRecentSession:(NIMRecentSession *)recentSession
              totalUnreadCount:(NSInteger)totalUnreadCount{
    // NSString *lastMessageId = [self lastMessageId];
    // if ([lastMessageId isEqualToString:recentSession.lastMessage.messageId]) {
    //     if (![self isUpdated]) {
    //         return;
    //     }
    
    //     [self setIsUpdated:YES];
    // } else {
    //     [self setIsUpdated:NO];
    // }
    
    // [self setLastMessageId:recentSession.lastMessage.messageId];
    
    //    [[ConversationViewController initWithConversationViewController]handleInComeMultiMediaMessage: recentSession.lastMessage callFrom:@"NIMViewController"];
    
    [self getResouces];
}
//删除所有会话回调
- (void)allMessagesDeleted{
    [self getResouces];
}

- (NSString *)getMessageType:(NIMMessageType)messageType{
    NSString *result = @"";
    switch (messageType) {
        case NIMMessageTypeText:
            result = @"text";
            break;
        case NIMMessageTypeImage:
            result = @"image";
            break;
        case NIMMessageTypeAudio:
            result = @"voice";
            break;
        case NIMMessageTypeVideo:
            result = @"video";
            break;
        case NIMMessageTypeLocation:
            result = @"location";
            break;
        case NIMMessageTypeNotification:
            result = @"notification";
            break;
        case NIMMessageTypeTip:
            result = @"tip";
            break;
        case NIMMessageTypeRobot:
            result = @"robot";
            break;
        case NIMMessageTypeRtcCallRecord:
            result = @"callRecord";
            break;
        case NIMMessageTypeCustom:
            result = @"custom";
            break;
        case NIMMessageTypeFile:
            result = @"file";
            break;
            
        default:
            break;
    }
    
    return result;
};

//-(BOOL) hanldeReplyStrangerByRecentContact:(NIMRecentSession *)recent {
//    NSMutableDictionary *localExt = recent.localExt ? [recent.localExt mutableCopy] : [[NSMutableDictionary alloc] init];
//    if ([localExt objectForKey:@"isReplyStranger"] != nil) {
//        NSNumber *replyStranger = [localExt objectForKey:@"isReplyStranger"];
//        
//        return [replyStranger boolValue];
//    }
//    
//    BOOL isReplyStranger = NO;
//    NIMMessage *lastMessage = recent.lastMessage;
//    if (lastMessage != nil && lastMessage.isOutgoingMsg) {
//        isReplyStranger = YES;
//    }
//    
//    [localExt setObject:@(isReplyStranger) forKey:@"isReplyStranger"];
//    
//    [[NIMSDK sharedSDK].conversationManager updateRecentLocalExt:localExt recentSession:recent];
//    
//    return isReplyStranger;
//}

//-(NSDictionary *)fetchUserInfo:(NSString *)sessionId {
//    NSString *apiUrl = [RNCConfig envFor:@"API_URL"];
//    NSString *authKey = [RNCConfig envFor:@"API_AUTH_KEY"];
//    NSLog(@"env => %@, %@", apiUrl, authKey);
//    if (apiUrl == nil || authKey == nil) return nil;
//    
//    NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"%@/api/v1/client/users/{accid}?accid=%@", apiUrl, sessionId]];
//    NSString *contentType = @"application/json";
//    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
//    [request addValue:contentType forHTTPHeaderField: @"Content-Type"];
//    [request addValue:authKey forHTTPHeaderField:@"customKey"];
//    [request setHTTPMethod:@"GET"];
//    
//    NSError *error = nil;
//    NSData *data = [NSURLConnection sendSynchronousRequest:request returningResponse:nil error:&error];
//    
//    if (error != nil || data == nil) {
//        if (error != nil) {
//            NSLog(@"fetchUserInfo error: %@", error);
//        }
//        
//        return nil;
//    }
//    
//    NSError *jsonError = nil;
//    NSDictionary *dict = [NSDictionary dictionaryWithDictionary:[NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:&jsonError]];
//    
//    if (jsonError != nil) {
//        NSLog(@"fetchUserInfo error: %@", jsonError);
//        return nil;
//    }
//    
//    NSMutableDictionary *updateListUserInfo = _listUserInfo ? [_listUserInfo mutableCopy] : [[NSMutableDictionary alloc] init];
//    
//    [updateListUserInfo setObject:dict forKey:sessionId];
//    
//    _listUserInfo = updateListUserInfo;
//    
//    return dict;
//}

-(NSDictionary *)handleSessionP2p:(NIMRecentSession *)recent totalUnreadCount:(NSInteger *)totalUnreadCount {
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    BOOL isMyFriend = [[NIMSDK sharedSDK].userManager isMyFriend:recent.session.sessionId];
    NIMUser *user = [[NIMSDK sharedSDK].userManager userInfo:recent.lastMessage.session.sessionId];
    NSString *unreadCount = [NSString stringWithFormat:@"%ld", recent.unreadCount];
    NSMutableDictionary *localExt = recent.localExt ? [recent.localExt mutableCopy] : [[NSMutableDictionary alloc] init];
    NSString *mute = user.notifyForNewMsg?@"0":@"1";
    BOOL isChatBot = NO;
    BOOL isCsr = NO;
    BOOL isHideSession = NO;
    if ([localExt objectForKey:@"isChatBot"] != nil) {
        NSNumber *chatBot = [localExt objectForKey:@"isChatBot"];
        isChatBot = [chatBot boolValue];
    }
    if ([localExt objectForKey:@"isHideSession"] != nil) {
        NSNumber *hideSession = [localExt objectForKey:@"isHideSession"];
        isHideSession = [hideSession boolValue];
    }
    if ([localExt objectForKey:@"isCsr"] != nil) {
        NSNumber *csr = [localExt objectForKey:@"isCsr"];
        isCsr = [csr boolValue];
    }
    
    if (recent.lastMessage == nil) {
        [dic setObject:[NSString stringWithFormat:@"%zd", NIMMessageDeliveryStateDeliveried] forKey:@"msgStatus"];
        [dic setObject:@"" forKey:@"messageId"];
        [dic setObject:@"0" forKey:@"time"];
        [dic setObject:@"" forKey:@"content"];
    }
    
    if (recent.lastMessage != nil) {
        [dic setObject:[NSString stringWithFormat:@"%d",recent.lastMessage.isRemoteRead] forKey:@"isRemoteRead"];
        [dic setObject:[NSNumber numberWithInteger:recent.lastMessage.messageSubType]  forKey:@"messageSubType"];
        [dic setObject: [NSNumber numberWithBool: recent.lastMessage.isOutgoingMsg] forKey:@"isOutgoing"];
        
        if (recent.lastMessage.messageSubType == 7) {
            [dic setObject:[NSString stringWithFormat:@"%zd", NIMMessageDeliveryStateDeliveried] forKey:@"msgStatus"];
            [dic setObject:@"" forKey:@"messageId"];
            [dic setObject:@"0" forKey:@"time"];
            [dic setObject:@"" forKey:@"content"];
            if (recent.lastMessage.messageSubType == 7) {
                [dic setObject:[NSNumber numberWithInteger:recent.lastMessage.messageSubType]  forKey:@"messageSubType"];
            }
        } else {
            [dic setObject:[NSNumber numberWithInteger:recent.lastMessage.messageSubType]  forKey:@"messageSubType"];
            [dic setObject:[NSString stringWithFormat:@"%zd", recent.lastMessage.deliveryState] forKey:@"msgStatus"];
            [dic setObject:[NSString stringWithFormat:@"%@", recent.lastMessage.messageId] forKey:@"messageId"];
            [dic setObject:[NSString stringWithFormat:@"%@", [self contentForRecentSession:recent] ] forKey:@"content"];
            [dic setObject:[NSString stringWithFormat:@"%f", recent.lastMessage.timestamp * 1000 ] forKey:@"time"];
            [dic setObject:[NSNumber numberWithInteger:recent.lastMessage.messageSubType] forKey:@"messageSubType"];
        }
        
        if (isChatBot) {
            BOOL isMessageChatBotUpdated = NO;
            if (recent.lastMessage.localExt != nil && [recent.lastMessage.localExt objectForKey:@"chatBotType"] != nil) {
                isMessageChatBotUpdated = YES;
            }
            
            [localExt setObject:@(isMessageChatBotUpdated) forKey:@"isMessageChatBotUpdated"];
            
            NSString *rawAttachContent = recent.lastMessage.rawAttachContent;
            
            NSData *dataRawAttchContent = [rawAttachContent dataUsingEncoding:NSUTF8StringEncoding];
            if (dataRawAttchContent) {
                NSError *error;
                NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:dataRawAttchContent options:0 error:&error];
                if (error == nil && [dict isKindOfClass:[NSDictionary class]] && [dict objectForKey:@"code"] != nil) {
                    [localExt setObject:@(YES) forKey:@"isChatBotNotifyOutSessionOfCurrentCsr"];
                }
            }
        }
        
        if (isCsr && !recent.lastMessage.isOutgoingMsg) {
            BOOL isMessageCsrUpdated = YES;
            if (recent.lastMessage.localExt != nil && [recent.lastMessage.localExt objectForKey:@"isMessageCsrUpdated"] != nil) {
                NSNumber *messageCsrUpdated = [recent.lastMessage.localExt objectForKey:@"isMessageCsrUpdated"];
                isMessageCsrUpdated = [messageCsrUpdated boolValue];
            }
            
            if (!isMessageCsrUpdated) {
                NSArray *messagesOfCsr = [[ConversationViewController initWithConversationViewController] setTimeArr:@[recent.lastMessage]];
                NSDictionary *messageOfCsr = messagesOfCsr.firstObject;
                
                if (messageOfCsr != nil) {
                    [localExt setObject:messageOfCsr forKey:@"messageOfCsr"];
                }
            }
        }
 
        if (recent.lastMessage.messageType == NIMMessageTypeCustom) {
            NIMCustomObject *messageObject = recent.lastMessage.messageObject;
            DWCustomAttachment *attachment = messageObject.attachment;
            
            if (attachment != nil) {
                switch (attachment.custType) {
                    case CustomMessgeTypeRedpacket: {
                        [dic setObject:attachment.dataDict forKey:@"extend"];
                        [dic setObject:@"redpacket" forKey:@"msgType"];
                        break;
                    }
                        
                    case CustomMessgeTypeBankTransfer: {
                        [dic setObject:attachment.dataDict  forKey:@"extend"];
                        [dic setObject:@"transfer" forKey:@"msgType"];
                        break;
                    }
                        
                    case CustomMessgeTypeRedPacketOpenMessage:{
                        NSDictionary *dataDict = [self dealWithData:attachment.dataDict];
                        if (dataDict != nil) {
                            [dic setObject:dataDict  forKey:@"extend"];
                            [dic setObject:@"redpacketOpen" forKey:@"msgType"];
                        }
                        
                        break;
                    }
                        
                    case CustomMessgeTypeUrl: {
                        [dic setObject:attachment.dataDict  forKey:@"extend"];
                        [dic setObject:@"account_notice" forKey:@"msgType"];
                        break;
                    }
                        
                    case CustomMessgeTypeAccountNotice:{
                        [dic setObject:attachment.dataDict  forKey:@"extend"];
                        [dic setObject:@"url" forKey:@"msgType"];
                        break;
                    }
                        
                    case CustomMessgeTypeBusinessCard:{
                        [dic setObject:attachment.dataDict  forKey:@"extend"];
                        [dic setObject:@"card" forKey:@"msgType"];
                        break;
                    }
                    case CustomMessgeTypeCustom:{
                        [dic setObject:attachment.dataDict  forKey:@"extend"];
                        [dic setObject:@"custom" forKey:@"msgType"];
                        break;
                    }
                    default:{
                        if (attachment.dataDict != nil) {
                            [dic setObject:attachment.dataDict  forKey:@"extend"];
                        }
                        
                        [dic setObject:@"unknown" forKey:@"msgType"];
                        break;
                    }
                        
                }
            }
        } else {
            if ([[recent.lastMessage.remoteExt objectForKey:@"extendType"]  isEqual: @"forwardMultipleText"]) {
                recent.lastMessage.text = @"[聊天记录]";
                NSMutableDictionary *extend = [NSMutableDictionary dictionary];
                [extend setObject:recent.lastMessage.text forKey:@"messages"];
                [dic setObject:extend forKey:@"extend"];
                [dic setObject:@"forwardMultipleText" forKey:@"msgType"];
            } else if ([[recent.lastMessage.remoteExt objectForKey:@"extendType"]  isEqual: @"card"]) {
                [dic setObject:recent.lastMessage.remoteExt forKey:@"extend"];
                [dic setObject:@"card" forKey:@"msgType"];
            } else if ([[recent.lastMessage.remoteExt objectForKey:@"extendType"]  isEqual: @"TEAM_NOTIFICATION_MESSAGE"]) {
                [dic setObject:recent.lastMessage.remoteExt forKey:@"extend"];
                [dic setObject:@"notification" forKey:@"msgType"];
            } else {
                [dic setObject:[NSString stringWithFormat:@"%@", [self getMessageType: recent.lastMessage.messageType]] forKey:@"msgType"];
            }
        }
        
        if (recent.lastMessage.localExt != nil) {
            if ([recent.lastMessage.localExt objectForKey:@"birthdayInfo"] != nil) {
                [localExt setObject:[recent.lastMessage.localExt objectForKey:@"birthdayInfo"] forKey:@"birthdayInfo"];
            }
            
            if ([recent.lastMessage.localExt objectForKey:@"notificationExtend"]  != nil) {
                [localExt setObject:[recent.lastMessage.localExt objectForKey:@"notificationExtend"] forKey:@"notificationExtend"];
            }
        }
        
        if (recent.lastMessage.remoteExt != nil) {
            if ([recent.lastMessage.remoteExt objectForKey:@"reaction"] != nil) {
                [localExt setObject:[recent.lastMessage.remoteExt objectForKey:@"reaction"] forKey:@"reaction"];
            }
            
            if ([recent.lastMessage.remoteExt objectForKey:@"dataRemoveReaction"] != nil) {
                [localExt setObject:[recent.lastMessage.remoteExt objectForKey:@"dataRemoveReaction"] forKey:@"dataRemoveReaction"];
            }
            
            if ([recent.lastMessage.remoteExt objectForKey:@"revokeMessage"] != nil) {
                [localExt setObject:[recent.lastMessage.remoteExt objectForKey:@"revokeMessage"] forKey:@"revokeMessage"];
            }
            
            if ([recent.lastMessage.remoteExt objectForKey:@"parentMediaId"] != nil) {
                [localExt setObject:[recent.lastMessage.remoteExt objectForKey:@"parentMediaId"] forKey:@"parentMediaId"];
            }
            
            if ([recent.lastMessage.remoteExt objectForKey:@"multiMediaType"] != nil) {
                [localExt setObject:[recent.lastMessage.remoteExt objectForKey:@"multiMediaType"] forKey:@"multiMediaType"];
            }
            
            if (recent.lastMessage.messageSubType == 7 && [recent.lastMessage.remoteExt objectForKey:@"temporarySessionRef"] != nil) {
                [localExt setObject:[recent.lastMessage.remoteExt objectForKey:@"temporarySessionRef"] forKey:@"temporarySessionRef"];
            }
            
            NSString *extendType = [recent.lastMessage.remoteExt objectForKey:@"extendType"];
            if (extendType != nil && [extendType isEqual:@"gif"]) {
                [dic setObject:recent.lastMessage.remoteExt forKey:@"extend"];
            }
        }
    }
    
//    NSString *sessionName =  [self nameForRecentSession:recent];
//    if ([sessionName isEqual:recent.session.sessionId] && !isCsr && !isChatBot) {
//        NSDictionary *userWithApi = [_listUserInfo objectForKey:recent.session.sessionId];
//        if (userWithApi == nil) {
//            userWithApi = [self fetchUserInfo:recent.session.sessionId];
//            NSLog(@"fetch api user info: %@", recent.session.sessionId);
//        }
//        if (userWithApi == nil) {
//            userWithApi = [[NSMutableDictionary alloc] init];
//        }
//        
//        NSString *nickname = [userWithApi objectForKey:@"nickName"];
//        NSString *sex = [userWithApi objectForKey:@"sex"];
//        NSString *avatar = [userWithApi objectForKey:@"avatar"];
//        
//        if (nickname != nil && ![nickname isEqual:@"<null>"]) {
//            [dic setObject:nickname forKey:@"name"];
//        } else {
//            [dic setObject:@"USER_NAME_DEFAULT" forKey:@"name"];
//        }
//        
//        if (sex != nil && ![sex isEqual:@"<null>"]) {
//            [dic setObject:sex forKey:@"sex"];
//        }
//        
//        if (avatar != nil && ![avatar isEqual:@"<null>"]) {
//            [dic setObject:avatar forKey:@"imagePath"];
//        } else {
//            [dic setObject:@"" forKey:@"imagePath"];
//        }
//    } else {
//        [dic setObject:[NSString stringWithFormat:@"%@", [self nameForRecentSession:recent] ] forKey:@"name"];
//        [dic setObject:[NSString stringWithFormat:@"%@", [self imageUrlForRecentSession:recent] ?  [self imageUrlForRecentSession:recent] : @""] forKey:@"imagePath"];
//    }
    
    [dic setObject:[NSString stringWithFormat:@"%@", [self nameForRecentSession:recent] ] forKey:@"name"];
    [dic setObject:[NSString stringWithFormat:@"%@", [self imageUrlForRecentSession:recent] ?  [self imageUrlForRecentSession:recent] : @""] forKey:@"imagePath"];
    [dic setObject:[NSString stringWithFormat:@"%@",recent.session.sessionId] forKey:@"contactId"];
    [dic setObject:[NSString stringWithFormat:@"%zd", recent.session.sessionType] forKey:@"sessionType"];
    [dic setObject: [NSNumber numberWithBool: isMyFriend] forKey:@"isMyFriend"];
    [dic setObject:mute forKey:@"mute"];
    [dic setObject:unreadCount forKey:@"unreadCount"];
    [dic setObject:localExt forKey:@"localExt"];
    
    if (user.notifyForNewMsg && !isHideSession && ![recent.session.sessionId isEqual:@"cmd10000"]) {
        *totalUnreadCount = *totalUnreadCount + [unreadCount integerValue];
    }
    
    return dic;
}

-(NSDictionary *)handleSessionTeam:(NIMRecentSession *)recent totalUnreadCount:(NSInteger *) totalUnreadCount {
    NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
    
    NIMTeam *team = [[[NIMSDK sharedSDK] teamManager]teamById:recent.lastMessage.session.sessionId];
    NSMutableDictionary *localExt = recent.localExt ? [recent.localExt mutableCopy]: [[NSMutableDictionary alloc] init];
    BOOL isHideSession = NO;
    NSString *unreadCount = [NSString stringWithFormat:@"%ld", recent.unreadCount];
    if ([localExt objectForKey:@"isHideSession"] != nil) {
        NSNumber *hideSession = [localExt objectForKey:@"isHideSession"];
        isHideSession = [hideSession boolValue];
    }
    
    if (recent.lastMessage == nil) {
        [result setObject:[NSString stringWithFormat:@"%zd", NIMMessageDeliveryStateDeliveried] forKey:@"msgStatus"];
        [result setObject:@"" forKey:@"messageId"];
        [result setObject:@"0" forKey:@"time"];
        [result setObject:@"" forKey:@"content"];
    }
    
    if (recent.lastMessage != nil) {
        [result setObject:[NSString stringWithFormat:@"%@", recent.lastMessage.from] forKey:@"account"];
        
        if (recent.lastMessage.messageType == NIMMessageTypeNotification) {
            [result setObject:[[ConversationViewController initWithConversationViewController]setNotiTeamObj:recent.lastMessage] forKey:@"extend"];
        }
        
        if (recent.lastMessage == nil || recent.lastMessage.messageSubType == 7) {
            [result setObject:[NSString stringWithFormat:@"%zd", NIMMessageDeliveryStateDeliveried] forKey:@"msgStatus"];
            [result setObject:@"" forKey:@"messageId"];
            [result setObject:@"0" forKey:@"time"];
            [result setObject:@"" forKey:@"content"];
            if (recent.lastMessage.messageSubType == 7) {
                [result setObject:[NSNumber numberWithInteger:recent.lastMessage.messageSubType]  forKey:@"messageSubType"];
            }
        } else {
            [result setObject:[NSNumber numberWithInteger:recent.lastMessage.messageSubType]  forKey:@"messageSubType"];
            [result setObject:[NSString stringWithFormat:@"%zd", recent.lastMessage.deliveryState] forKey:@"msgStatus"];
            [result setObject:[NSString stringWithFormat:@"%@", recent.lastMessage.messageId] forKey:@"messageId"];
            [result setObject:[NSString stringWithFormat:@"%@", [self contentForRecentSession:recent] ] forKey:@"content"];
            [result setObject:[NSString stringWithFormat:@"%f", recent.lastMessage.timestamp * 1000 ] forKey:@"time"];
            [result setObject:[NSNumber numberWithInteger:recent.lastMessage.messageSubType] forKey:@"messageSubType"];
        }
        
        if (recent.lastMessage.localExt != nil) {
            if ([recent.lastMessage.localExt objectForKey:@"notificationExtend"] != nil) {
                [localExt setObject:[recent.lastMessage.localExt objectForKey:@"notificationExtend"] forKey:@"notificationExtend"];
            }
            
            if ([recent.lastMessage.localExt objectForKey:@"birthdayInfo"] != nil) {
                [localExt setObject:[recent.lastMessage.localExt objectForKey:@"birthdayInfo"] forKey:@"birthdayInfo"];
            }
            
        }
        
        if (recent.lastMessage != nil && recent.lastMessage.remoteExt != nil) {
            if ([recent.lastMessage.remoteExt objectForKey:@"reaction"] != nil) {
                [localExt setObject:[recent.lastMessage.remoteExt objectForKey:@"reaction"] forKey:@"reaction"];
            }
            
            if ([recent.lastMessage.remoteExt objectForKey:@"dataRemoveReaction"] != nil) {
                [localExt setObject:[recent.lastMessage.remoteExt objectForKey:@"dataRemoveReaction"] forKey:@"dataRemoveReaction"];
            }
            
            if ([recent.lastMessage.remoteExt objectForKey:@"revokeMessage"] != nil) {
                [localExt setObject:[recent.lastMessage.remoteExt objectForKey:@"revokeMessage"] forKey:@"revokeMessage"];
            }
            
            if ([recent.lastMessage.remoteExt objectForKey:@"parentMediaId"] != nil) {
                [localExt setObject:[recent.lastMessage.remoteExt objectForKey:@"parentMediaId"] forKey:@"parentMediaId"];
            }
            
            if ([recent.lastMessage.remoteExt objectForKey:@"multiMediaType"] != nil) {
                [localExt setObject:[recent.lastMessage.remoteExt objectForKey:@"multiMediaType"] forKey:@"multiMediaType"];
            }
            
            if (recent.lastMessage.messageSubType == 7 && [recent.lastMessage.remoteExt objectForKey:@"temporarySessionRef"] != nil) {
                [localExt setObject:[recent.lastMessage.remoteExt objectForKey:@"temporarySessionRef"] forKey:@"temporarySessionRef"];
            }
            
            NSString *extendType = [recent.lastMessage.remoteExt objectForKey:@"extendType"];
            if (extendType != nil && [extendType isEqual:@"gif"]) {
                [result setObject:recent.lastMessage.remoteExt forKey:@"extend"];
            }
        }
        
        if (recent.lastMessage.messageType == NIMMessageTypeCustom) {
            NIMCustomObject *messageObject = recent.lastMessage.messageObject;
            DWCustomAttachment *attachment = messageObject.attachment;
            
            if (attachment) {
                switch (attachment.custType) {
                    case CustomMessgeTypeRedpacket: {
                        [result setObject:attachment.dataDict forKey:@"extend"];
                        [result setObject:@"redpacket" forKey:@"msgType"];
                        break;
                    }
                        
                    case CustomMessgeTypeBankTransfer: {
                        [result setObject:attachment.dataDict  forKey:@"extend"];
                        [result setObject:@"transfer" forKey:@"msgType"];
                        break;
                    }
                        
                    case CustomMessgeTypeRedPacketOpenMessage:{
                        NSDictionary *dataDict = [self dealWithData:attachment.dataDict];
                        if (dataDict != nil) {
                            [result setObject:dataDict  forKey:@"extend"];
                            [result setObject:@"redpacketOpen" forKey:@"msgType"];
                        }
                        
                        break;
                    }
                        
                    case CustomMessgeTypeUrl: {
                        [result setObject:attachment.dataDict  forKey:@"extend"];
                        [result setObject:@"account_notice" forKey:@"msgType"];
                        break;
                    }
                        
                    case CustomMessgeTypeAccountNotice:{
                        [result setObject:attachment.dataDict  forKey:@"extend"];
                        [result setObject:@"url" forKey:@"msgType"];
                        break;
                    }
                        
                    case CustomMessgeTypeBusinessCard:{
                        [result setObject:attachment.dataDict  forKey:@"extend"];
                        [result setObject:@"card" forKey:@"msgType"];
                        break;
                    }
                    case CustomMessgeTypeCustom:{
                        [result setObject:attachment.dataDict  forKey:@"extend"];
                        [result setObject:@"custom" forKey:@"msgType"];
                        break;
                    }
                    default:{
                        if (attachment.dataDict != nil) {
                            [result setObject:attachment.dataDict  forKey:@"extend"];
                        }
                        
                        [result setObject:@"unknown" forKey:@"msgType"];
                        break;
                    }
                        
                }
            }
        } else {
            if ([[recent.lastMessage.remoteExt objectForKey:@"extendType"]  isEqual: @"forwardMultipleText"]) {
                recent.lastMessage.text = @"[聊天记录]";
                NSMutableDictionary *extend = [NSMutableDictionary dictionary];
                [extend setObject:recent.lastMessage.text forKey:@"messages"];
                [result setObject:extend forKey:@"extend"];
                [result setObject:@"forwardMultipleText" forKey:@"msgType"];
            } else if ([[recent.lastMessage.remoteExt objectForKey:@"extendType"]  isEqual: @"card"]) {
                [result setObject:recent.lastMessage.remoteExt forKey:@"extend"];
                [result setObject:@"card" forKey:@"msgType"];
            } else if ([[recent.lastMessage.remoteExt objectForKey:@"extendType"]  isEqual: @"TEAM_NOTIFICATION_MESSAGE"]) {
                [result setObject:recent.lastMessage.remoteExt forKey:@"extend"];
                [result setObject:@"notification" forKey:@"msgType"];
            } else {
                [result setObject:[NSString stringWithFormat:@"%@", [self getMessageType: recent.lastMessage.messageType]] forKey:@"msgType"];
            }
        }
    }
    
    [result setObject:[NSString stringWithFormat:@"%@", recent.session.sessionId] forKey:@"contactId"];
    [result setObject:[NSString stringWithFormat:@"%zd", recent.session.sessionType] forKey:@"sessionType"];
    [result setObject:unreadCount forKey:@"unreadCount"];
    [result setObject:[NSString stringWithFormat:@"%@", [self nameForRecentSession:recent]] forKey:@"name"];
    [result setObject:@"" forKey:@"imagePath"];
    [result setObject:[NSString stringWithFormat:@"%zd",team.memberNumber] forKey:@"memberCount"];
    NSString *strMute = team.notifyStateForNewMsg == NIMTeamNotifyStateAll ? @"1" : @"0";
    [result setObject:[NSString stringWithFormat:@"%@", strMute ] forKey:@"mute"];
    [result setObject:[[TeamViewController initWithTeamViewController]convertTeamInfo:team] forKey:@"teamInfo"];
    [[TeamViewController initWithTeamViewController] getTeamMemberList:team.teamId Succ:^(id param) {
        [result setObject:param forKey:@"teamMembers"];
    } Err:^(id erro) {
    }];

    if (team.notifyStateForNewMsg == NIMTeamNotifyStateAll && !isHideSession) {
        *totalUnreadCount = *totalUnreadCount + [unreadCount integerValue];
    }
    
    return result;
}

-(NSDictionary *)handleSessionChatroom:(NIMRecentSession *)recent totalUnreadCount:(NSInteger *)totalUnreadCount {
    NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
    
    NIMChatroom *chatroomInfo = [[ChatroomViewController initWithChatroomViewController] getChatroomInfo:recent.session.sessionId];
    NSString *unreadCount = [NSString stringWithFormat:@"%ld", recent.unreadCount];
    
    if (recent.lastMessage == nil) {
        [result setObject:[NSString stringWithFormat:@"%zd", NIMMessageDeliveryStateDeliveried] forKey:@"msgStatus"];
        [result setObject:@"" forKey:@"messageId"];
        [result setObject:@"0" forKey:@"time"];
        [result setObject:@"" forKey:@"content"];
    }
    
    if (recent.lastMessage != nil) {
        [result setObject:[NSString stringWithFormat:@"%@", [self getMessageType: recent.lastMessage.messageType]] forKey:@"msgType"];
        
        if (recent.lastMessage == nil || recent.lastMessage.messageSubType == 7) {
            [result setObject:[NSString stringWithFormat:@"%zd", NIMMessageDeliveryStateDeliveried] forKey:@"msgStatus"];
            [result setObject:@"" forKey:@"messageId"];
            [result setObject:@"0" forKey:@"time"];
            [result setObject:@"" forKey:@"content"];
            if (recent.lastMessage.messageSubType == 7) {
                [result setObject:[NSNumber numberWithInteger:recent.lastMessage.messageSubType]  forKey:@"messageSubType"];
            }
        } else {
            [result setObject:[NSNumber numberWithInteger:recent.lastMessage.messageSubType]  forKey:@"messageSubType"];
            [result setObject:[NSString stringWithFormat:@"%zd", recent.lastMessage.deliveryState] forKey:@"msgStatus"];
            [result setObject:[NSString stringWithFormat:@"%@", recent.lastMessage.messageId] forKey:@"messageId"];
            [result setObject:[NSString stringWithFormat:@"%@", [self contentForRecentSession:recent] ] forKey:@"content"];
            [result setObject:[NSString stringWithFormat:@"%f", recent.lastMessage.timestamp * 1000 ] forKey:@"time"];
            [result setObject:[NSNumber numberWithInteger:recent.lastMessage.messageSubType] forKey:@"messageSubType"];
        }
    }
    
    [result setObject:unreadCount forKey:@"unreadCount"];
    [result setObject:@"" forKey:@"imagePath"];
    [result setObject:[NSString stringWithFormat:@"%@", chatroomInfo.roomId] forKey:@"contactId"];
    [result setObject:chatroomInfo.name forKey:@"name"];
    [result setObject:[NSNumber numberWithLong:chatroomInfo.onlineUserCount] forKey:@"onlineUserCount"];
    [result setObject:[NSNumber numberWithBool:[[ChatroomViewController initWithChatroomViewController] checkChatroomLoginStatus:recent.session.sessionId]] forKey:@"isLoginChatroom"];
    
    return result;
}

-(void)getRecentContactListsuccess:(SUCCESS)suc andError:(ERROR)err{
    NSInteger allUnreadNum = 0;
    NSArray *NIMlistArr = [[NIMSDK sharedSDK].conversationManager.allRecentSessions mutableCopy];
    NSMutableArray *sessionList = [NSMutableArray array];
    for (NIMRecentSession *recent in NIMlistArr) {
        switch (recent.session.sessionType) {
            case NIMSessionTypeP2P: {
                NSDictionary *dic = [self handleSessionP2p:recent totalUnreadCount:&allUnreadNum];
                [sessionList addObject:dic];
                break;
            }
                
            case NIMSessionTypeTeam: {
                NSDictionary *dic = [self handleSessionTeam:recent totalUnreadCount:&allUnreadNum];
                [sessionList addObject:dic];
                break;
            }
                
            case NIMSessionTypeChatroom: {
                NSDictionary *dic = [self handleSessionChatroom:recent totalUnreadCount:&allUnreadNum];
                [sessionList addObject:dic];
                break;
            }
                
            default:
                break;
        }
        
        if (recent != nil && recent.localExt != nil) {
            NSMutableDictionary *localExt = [recent.localExt mutableCopy];
            NSNumber *pinSessionWithEmpty = [localExt objectForKey:@"isPinSessionWithEmpty"];
            
            if (pinSessionWithEmpty != nil) {
                BOOL isPinSessionWithEmpty = [pinSessionWithEmpty boolValue];
                
                if (isPinSessionWithEmpty && recent.lastMessage != nil) {
                    [localExt setObject:@(NO) forKey:@"isPinSessionWithEmpty"];
                    
                    [[NIMSDK sharedSDK].conversationManager updateRecentLocalExt:localExt  recentSession:recent];
                }
            }
        }
    }
    
    NSDictionary *recentDict = @{@"recents":sessionList,@"unreadCount":[NSString stringWithFormat:@"%zd",allUnreadNum]};
    if (sessionList) {
        suc(recentDict);
    }else{
        err(@"Network error");
    }
    
}

-(void)getResouces {
    NSInteger allUnreadNum = 0;
    NSArray *NIMlistArr = [[NIMSDK sharedSDK].conversationManager.allRecentSessions mutableCopy];
    
    NSMutableArray *sessionList = [NSMutableArray array];
    for (NIMRecentSession *recent in NIMlistArr) {
        switch (recent.session.sessionType) {
            case NIMSessionTypeP2P: {
                NSDictionary *dic = [self handleSessionP2p:recent totalUnreadCount:&allUnreadNum];
                [sessionList addObject:dic];
                break;
            }
                
            case NIMSessionTypeTeam: {
                NSDictionary *dic = [self handleSessionTeam:recent totalUnreadCount:&allUnreadNum];
                [sessionList addObject:dic];
                break;
            }
                
            case NIMSessionTypeChatroom: {
                NSDictionary *dic = [self handleSessionChatroom:recent totalUnreadCount:&allUnreadNum];
                [sessionList addObject:dic];
                break;
            }
                
            default:
                break;
        }
        
        if (recent != nil && recent.localExt != nil) {
            NSMutableDictionary *localExt = [recent.localExt mutableCopy];
            NSNumber *pinSessionWithEmpty = [localExt objectForKey:@"isPinSessionWithEmpty"];
            
            NSLog(@"pinSessionWithEmpty >> %@, %@", pinSessionWithEmpty, recent.lastMessage);
            
            if (pinSessionWithEmpty != nil) {
                BOOL isPinSessionWithEmpty = [pinSessionWithEmpty boolValue];
                
                if (isPinSessionWithEmpty && recent.lastMessage != nil) {
                    [localExt setObject:@(NO) forKey:@"isPinSessionWithEmpty"];
                    
                    [[NIMSDK sharedSDK].conversationManager updateRecentLocalExt:localExt recentSession:recent];
                }
            }
        }
    }
    
    NSDictionary *recentDict = @{@"recents":sessionList,@"unreadCount":[NSString stringWithFormat:@"%zd",allUnreadNum]};
    [NIMModel initShareMD].recentDict = recentDict;
}
//会话标题
- (NSString *)nameForRecentSession:(NIMRecentSession *)recent{
    if (recent.session.sessionType == NIMSessionTypeP2P) {
        return [NIMKitUtil showNick:recent.session.sessionId inSession:recent.session];
    }else{
        NIMTeam *team = [[NIMSDK sharedSDK].teamManager teamById:recent.session.sessionId];
        return team.teamName;
    }
}
//会话头像
-(NSString *)imageUrlForRecentSession:(NIMRecentSession *)recent{
    NIMKitInfo *info = nil;
    if (recent.session.sessionType == NIMSessionTypeTeam)
    {
        info = [[NIMKit sharedKit] infoByTeam:recent.session.sessionId option:nil];
    }
    else
    {
        NIMKitInfoFetchOption *option = [[NIMKitInfoFetchOption alloc] init];
        option.session = recent.session;
        info = [[NIMKit sharedKit] infoByUser:recent.session.sessionId option:option];
    }
    NSURL *url = info.avatarUrlString ? [NSURL URLWithString:info.avatarUrlString] : nil;
    return url;
}
//会话内容
- (NSString *)contentForRecentSession:(NIMRecentSession *)recent{
    NSString *content = [self messageContent:recent.lastMessage];
    return content;
}
//会话时间
- (NSString *)timestampDescriptionForRecentSession:(NIMRecentSession *)recent{
    return [NIMKitUtil showTime:recent.lastMessage.timestamp showDetail:NO];
}

- (NSString *)convertMessageMedia:(NIMMessage *)message contentMessage:(NSString *)contentMessage{
    NSString *text = @"";
    
    if ([message.from isEqualToString:[NIMSDK sharedSDK].loginManager.currentAccount]) {
        text = contentMessage;
    } else {
        text = message.session.sessionType == NIMSessionTypeP2P ? [NSString stringWithFormat:@"%@", contentMessage] : [NSString stringWithFormat:@"%@ :%@", message.senderName, contentMessage];
    }
    
    return text;
}

- (NSString *)messageContent:(NIMMessage*)lastMessage{
    if (lastMessage.messageSubType == 2 || lastMessage.messageSubType == 3) {
        return lastMessage.text;
    }
    
    NSString *text = @"";
    switch (lastMessage.messageType) {
        case NIMMessageTypeText:
            if ([lastMessage.text isEqual:@"[动图]"] || [lastMessage.text isEqual:@"[个人名片]"]) {
                return [self convertMessageMedia:lastMessage contentMessage:lastMessage.text];
            }
            
            text = lastMessage.text;
            break;
        case NIMMessageTypeAudio:
            return [self convertMessageMedia:lastMessage contentMessage:@"[语音]"];
        case NIMMessageTypeImage:
            return [self convertMessageMedia:lastMessage contentMessage:@"[图片]"];
        case NIMMessageTypeVideo:
            return [self convertMessageMedia:lastMessage contentMessage:@"[视频]"];
        case NIMMessageTypeLocation:
            return [self convertMessageMedia:lastMessage contentMessage:@"[位置]"];
        case NIMMessageTypeNotification:{
            return [self notificationMessageContent:lastMessage];
        }
        case NIMMessageTypeFile:
            return [self convertMessageMedia:lastMessage contentMessage:@"[文件]"];
        case NIMMessageTypeTip:
            text = lastMessage.text;
            break;
        case NIMMessageTypeCustom:{
            text = [self getCustomType:lastMessage];
        }
            break;
        default:
            text = @"[未知消息]";
    }
    if ((lastMessage.session.sessionType == NIMSessionTypeP2P) || (lastMessage.messageType == NIMMessageTypeTip)||([lastMessage.from isEqualToString:[NIMSDK sharedSDK].loginManager.currentAccount]) ) {
        return text;
    }else{
        NSString *nickName = [NIMKitUtil showNick:lastMessage.from inSession:lastMessage.session];
        return nickName.length ? [nickName stringByAppendingFormat:@":%@",text] : @"";
    }
}
//获得数据类型
- (NSString *)getCustomType:(NIMMessage *)message{
    NIMCustomObject *customObject = message.messageObject;
    DWCustomAttachment *obj = customObject.attachment;
    NSString *text = @"[未知消息]";
    if (obj) {
        switch (obj.custType) {
            case CustomMessgeTypeRedpacket: //红包
            {
                text = [NSString stringWithFormat:@"[红包]%@", [obj.dataDict objectForKey:@"comments"]];
            }
                break;
            case CustomMessgeTypeBankTransfer: //转账
            {
                text = [NSString stringWithFormat:@"[转账]%@", [obj.dataDict objectForKey:@"comments"]];
            }
                break;
            case CustomMessgeTypeUrl: //链接
            {
                text = [obj.dataDict objectForKey:@"title"];
            }
                break;
            case CustomMessgeTypeAccountNotice: //账户通知
            {
                text = [obj.dataDict objectForKey:@"title"];
            }
                break;
            case CustomMessgeTypeRedPacketOpenMessage: //拆红包
            {
                text = [self dealWithData:obj.dataDict];
            }
                break;
            case CustomMessgeTypeBusinessCard: //名片
            {
                if([message.from isEqualToString:[NIMSDK sharedSDK].loginManager.currentAccount]){//如果是自己
                    text = [NSString stringWithFormat:@"你推荐了%@", [obj.dataDict objectForKey:@"name"]];
                }else{
                    text = [NSString stringWithFormat:@"向你推荐了%@", [obj.dataDict objectForKey:@"name"]];
                }
            }
                break;
            case CustomMessgeTypeCustom: //自定义
            {
                text = [self dealWithCustomData:obj.dataDict];
            }
                break;
            default:
                text = @"[未知消息]";
                break;
        }
    }
    return text;
}

//处理自定义消息
- (NSString *)dealWithCustomData:(NSDictionary *)dict{
    NSString *recentContent = [self stringFromKey:@"recentContent" andDict:dict];
    return recentContent;
}

//处理拆红包消息
- (NSString *)dealWithData:(NSDictionary *)dict{
    NSString *strOpenId = [self stringFromKey:@"openId" andDict:dict];
    NSString *strSendId = [self stringFromKey:@"sendId" andDict:dict];
    NSString *strMyId = [NIMSDK sharedSDK].loginManager.currentAccount;
    NSString *strContent = @"";
    
    if ([strOpenId isEqualToString:strMyId]&&[strSendId isEqualToString:strMyId]) {
        strContent = [NSString stringWithFormat:@"你领取了自己发的红包" ];
    }else if ([strOpenId isEqualToString:strMyId]){
        NSString *strSendName = [self getUserName:strSendId];
        strContent = [NSString stringWithFormat:@"你领取了%@的红包",strSendName];
    }else if([strSendId isEqualToString:strMyId]){
        NSString *strOpenName = [self getUserName:strOpenId];
        strContent = [NSString stringWithFormat:@"%@领取了你的红包",strOpenName];
    }
    //    else{
    //        NSString *strSenderName = [self getUserName:strSendId];
    //        NSString *strOpenName = [self getUserName:strOpenId];
    //        strContent = [NSString stringWithFormat:@"%@领取了%@的红包",strOpenName,strSenderName];
    //    }
    return strContent;
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


- (NSString *)notificationMessageContent:(NIMMessage *)lastMessage{
    NIMNotificationObject *object = lastMessage.messageObject;
    if (object.notificationType == NIMNotificationTypeNetCall) {
        NIMNetCallNotificationContent *content = (NIMNetCallNotificationContent *)object.content;
        if (content.callType == NIMNetCallTypeAudio) {
            return @"[网络通话]";
        }
        return @"[视频聊天]";
    }
    if (object.notificationType == NIMNotificationTypeTeam) {
        NSString *strContent = [NIMKitUtil teamNotificationFormatedMessage:lastMessage];
        return strContent;
    }
    return @"[未知消息]";
}

- (void)dealloc{
    [[NIMSDK sharedSDK].loginManager removeDelegate:self];
}

@end
