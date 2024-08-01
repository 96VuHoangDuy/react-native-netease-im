//
//  RNNotificationCenter.m
//  RNNeteaseIm
//
//  Created by Dowin on 2017/5/24.
//  Copyright © 2017年 Dowin. All rights reserved.
//

#import "RNNotificationCenter.h"
#import <AVFoundation/AVFoundation.h>
#import "NSDictionary+NTESJson.h"
#import "NIMMessageMaker.h"
#import "NIMModel.h"
#import "ConversationViewController.h"
#import "ImConfig.h"

@interface RNNotificationCenter () <NIMSystemNotificationManagerDelegate,NIMChatManagerDelegate>
@property (nonatomic,strong) AVAudioPlayer *player; //播放提示音
@end

@implementation RNNotificationCenter

+ (instancetype)sharedCenter
{
    static RNNotificationCenter *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[RNNotificationCenter alloc] init];
    });
    return instance;
}
- (void)start
{
//    DDLogInfo(@"Notification Center Setup");
}
- (instancetype)init {
    self = [super init];
    if(self) {
      
        NSURL *url = [[NSBundle mainBundle] URLForResource:@"message" withExtension:@"wav"];
        _player = [[AVAudioPlayer alloc] initWithContentsOfURL:url error:nil];
        
        [[NIMSDK sharedSDK].systemNotificationManager addDelegate:self];
      
        [[NIMSDK sharedSDK].chatManager addDelegate:self];
    }
    return self;
}
- (void)dealloc{
    [[NIMSDK sharedSDK].systemNotificationManager removeDelegate:self];
    [[NIMSDK sharedSDK].chatManager removeDelegate:self];
}
#pragma mark - NIMChatManagerDelegate
- (void)onRecvMessages:(NSArray *)messages//接收到新消息
{
    static BOOL isPlaying = NO;
    if (isPlaying) {
        return;
    }
    isPlaying = YES;
    [self playMessageAudioTip];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        isPlaying = NO;
    });
    [self checkTranferMessage:messages];
}

- (void)playMessageAudioTip
{
//    UINavigationController *nav = [NTESMainTabController instance].selectedViewController;
//    BOOL needPlay = YES;
//    for (UIViewController *vc in nav.viewControllers) {
//        if ([vc isKindOfClass:[NIMSessionViewController class]] ||  [vc isKindOfClass:[NTESLiveViewController class]] || [vc isKindOfClass:[NTESNetChatViewController class]])
//        {
//            needPlay = NO;
//            break;
//        }
//    }
//    if (needPlay) {
//        [self.player stop];
//        [[AVAudioSession sharedInstance] setCategory: AVAudioSessionCategoryAmbient error:nil];
//        [self.player play];
//    }
}
//检测是不是消息助手发来的转账消息提醒
- (void)checkTranferMessage:(NSArray *)messages{
    for (NIMMessage *message in messages) {
        if ([message.from isEqualToString:@"10000"] && (message.messageType == NIMMessageTypeCustom)) {
            NIMCustomObject *customObject = message.messageObject;
            DWCustomAttachment *obj = customObject.attachment;
            if (obj && (obj.custType == CustomMessgeTypeAccountNotice)) {
//                NSLog(@"dataDict:%@",obj.dataDict);
                NIMModel *mode = [NIMModel initShareMD];
                mode.accountNoticeDict = obj.dataDict;
                
            }
        }
    }
   
}

#pragma mark -- NIMChatManagerDelegate
- (void)onRecvRevokeMessageNotification:(NIMRevokeMessageNotification *)notification
{
    NSString * tip = [[ConversationViewController initWithConversationViewController] tipOnMessageRevoked:notification.message];
    NIMMessage *tipMessage = [[ConversationViewController initWithConversationViewController] msgWithTip:tip];
    tipMessage.timestamp = notification.timestamp;
    NIMMessage *deleMess = notification.message;
    
    if (deleMess) {
        NSDictionary *deleteDict = @{@"msgId":deleMess.messageId, @"sessionId": deleMess.session.sessionId, @"isObserveReceiveRevokeMessage": @(YES)};
        
        [[NIMSDK sharedSDK].conversationManager deleteMessage:notification.message];
        
        [NIMModel initShareMD].deleteMessDict = deleteDict;
    }

//     saveMessage 方法执行成功后会触发 onRecvMessages: 回调，但是这个回调上来的 NIMMessage 时间为服务器时间，和界面上的时间有一定出入，所以要提前先在界面上插入一个和被删消息的界面时间相符的 Tip, 当触发 onRecvMessages: 回调时，组件判断这条消息已经被插入过了，就会忽略掉。
//    [[NIMSDK sharedSDK].conversationManager saveMessage:tipMessage
//                                             forSession:notification.session
//                                             completion:nil];
}
#pragma mark - NIMSystemNotificationManagerDelegate
- (void)onReceiveCustomSystemNotification:(NIMCustomSystemNotification *)notification{//接收自定义通知
    NSDictionary *notiDict = [self jsonDictWithString:notification.content];
    NSDictionary *dataDict = [notiDict objectForKey:@"data"];
    
    if (dataDict){
        NSInteger notiType = [[dataDict objectForKey:@"type"] integerValue];
                
        switch (notiType) {
            case 0: //OBSERVE_RECEIVE_REVOKE_MESSAGE
            case 1: //OBSERVE_RECEIVE_REVOKE_MESSAGE
            {
//                NSString *sessionId = [dataDict objectForKey:@"sessionId"];
//                NSString *messageId = [dataDict objectForKey:@"messageId"];
//                
//                NIMSession *session = [NIMSession session:sessionId type:[@(notification.receiverType) integerValue]];
//                
//                NSArray *currentMessages = [[[NIMSDK sharedSDK] conversationManager] messagesInSession:session messageIds:@[messageId]];
//                NIMMessage *revokedMessge = currentMessages[0];
//                
//                [[NIMSDK sharedSDK].conversationManager deleteMessage:revokedMessge];
            }
                break;
            case 2: //OBSERVE_RECEIVE_FRIEND_REMOVED_ME
                break;
            case 3: //OBSERVE_RECEIVE_REVOKE_FRIEND_REQUEST,
                break;
            case 4: //OBERSVE_FRIEND_ACCEPT_MY_FRIEND_REQUEST
                break;
            case 5: //IS_TYPING_MESSAGE
                break;
//            case 4://拆红包消息
////            {
////                [self saveTheRedPacketOpenMsg:[notiDict objectForKey:@"data"] andTime:timestamp];
////            }
//                break;
                
            default:
                break;
        }
        [NIMModel initShareMD].customNotification = dataDict;
    }
}
//保存拆红包消息到本地
- (void)saveTheRedPacketOpenMsg:(NSDictionary *)dict andTime:(NSTimeInterval)times{
//    NSDictionary *datatDict = [dict objectForKey:@"dict"];
//    NSTimeInterval timestamp = times;
//    NSString *sessionId = [dict objectForKey:@"sessionId"];
//    NSInteger sessionType = [[dict objectForKey:@"sessionType"] integerValue];
//    NIMSession *session = [NIMSession session:sessionId type:sessionType];
//    NIMMessage *message;
//    DWCustomAttachment *obj = [[DWCustomAttachment alloc]init];
//    obj.custType = CustomMessgeTypeRedPacketOpenMessage;
//    obj.dataDict = datatDict;
//    message = [NIMMessageMaker msgWithCustomAttachment:obj andeSession:session];
//    message.timestamp = timestamp;
//    [[NIMSDK sharedSDK].conversationManager saveMessage:message forSession:session completion:nil];
}

// json字符串转dict字典
- (NSDictionary *)jsonDictWithString:(NSString *)string
{
    if (string && 0 != string.length)
    {
        NSError *error;
        NSData *jsonData = [string dataUsingEncoding:NSUTF8StringEncoding];
        NSDictionary *jsonDict = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingMutableContainers error:&error];
        if (error)
        {
            NSLog(@"json解析失败：%@", error);
            return nil;
        }
        return jsonDict;
    }
    return nil;
}

@end
