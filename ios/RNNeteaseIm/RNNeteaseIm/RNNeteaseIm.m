//
//  RNNeteaseIm.m
//  RNNeteaseIm
//
//  Created by Dowin on 2017/5/9.
//  Copyright © 2017年 Dowin. All rights reserved.
//

#import "RNNeteaseIm.h"
#import <React/RCTUtils.h>
#import "RNNotificationCenter.h"
#import "NIMModel.h"
#import "NIMViewController.h"
#import "ContactViewController.h"
#import "NoticeViewController.h"
#import "TeamViewController.h"
#import "ConversationViewController.h"
#import "BankListViewController.h"
#import "ImConfig.h"
#import <React/RCTLog.h>
#import "NIMMessageMaker.h"
#import "ChatroomViewController.h"
#import "CacheUsers.h"
#import <Photos/Photos.h>
#import "UserStrangers.h"
#import "EventSender.h"

#define kDevice_Is_iPhoneX ([UIScreen instancesRespondToSelector:@selector(currentMode)] ? CGSizeEqualToSize(CGSizeMake(1125, 2436), [[UIScreen mainScreen] currentMode].size) : NO)

@interface RNNeteaseIm(){
    NSString *strUserAgent;
}

@property (nonatomic, strong) EventSender *eventSenderReceive;
@property (nonatomic, strong) EventSender *eventSenderStatus;
@property (nonatomic, strong) EventSender *eventSenderProgress;
@property (nonatomic, strong) NSTimer *debounceTimer; // Timer to debounce the event

@end

@implementation RNNeteaseIm

- (void)dealloc{
    [[NSNotificationCenter defaultCenter]removeObserver:self];
}

- (instancetype)init{
    if (self = [super init]) {
        
    }
    [self initController];
    
    self.eventSenderReceive = [[EventSender alloc] initWithIm:self];
    self.eventSenderStatus = [[EventSender alloc] initWithIm:self];
    self.eventSenderProgress = [[EventSender alloc] initWithIm:self];

    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(clickObserveNotification:) name:@"ObservePushNotification" object:nil];
    return self;
}

- (void)clickObserveNotification:(NSNotification *)noti{
    NSDictionary *dict = noti.object;
    NSMutableDictionary *param = [NSMutableDictionary dictionaryWithDictionary:[dict objectForKey:@"dict"]];
    NSString *strDict = [param objectForKey:@"sessionBody"];
    if ([strDict length]) {
        NSDictionary *dataDict = [self dictChangeFromJson:strDict];
        NSMutableDictionary *mutaDict = [NSMutableDictionary dictionaryWithDictionary:dataDict];
        NSString *strType = [mutaDict objectForKey:@"sessionType"];
        NSString *strSessionId = [mutaDict objectForKey:@"sessionId"];
        NSString *strSessionName = @"";
        if ([strType isEqualToString:@"0"]) {//点对点
            NIMUser *user = [[NIMSDK sharedSDK].userManager userInfo:strSessionId];
            if ([user.alias length]) {
                strSessionName = user.alias;
            }else{
                NIMUserInfo *userInfo = user.userInfo;
                strSessionName = userInfo.nickName;
            }
        }else{//群主
            NIMTeam *team = [[[NIMSDK sharedSDK] teamManager]teamById:strSessionId];
            strSessionName = team.teamName;
        }
        if (!strSessionName) {
            strSessionName = @"";
        }
        [mutaDict setObject:strSessionName forKey:@"sessionName"];
        [param setObject:mutaDict forKey:@"sessionBody"];
        if ([[dict objectForKey:@"type"] isEqualToString:@"launch"]) {
            [_bridge.eventDispatcher sendDeviceEventWithName:@"observeLaunchPushEvent" body:param];
        }else{
            [_bridge.eventDispatcher sendDeviceEventWithName:@"observeBackgroundPushEvent" body:param];
        }
    }
    
}

// Debounce function
- (void)debounceSendEventWithParam:(NSDictionary *)param debounceInterval:(NSTimeInterval)interval {
    // Invalidate any existing timer to reset the debounce window
    [self.debounceTimer invalidate];
    
    // Schedule a new timer
    self.debounceTimer = [NSTimer scheduledTimerWithTimeInterval:interval
                                                          target:self
                                                        selector:@selector(fireDebouncedEvent:)
                                                        userInfo:param
                                                         repeats:NO];
}

// This is the function that actually sends the event
- (void)fireDebouncedEvent:(NSTimer *)timer {
    NSDictionary *param = (NSDictionary *)timer.userInfo;
    [_bridge.eventDispatcher sendDeviceEventWithName:@"observeRecentContact" body:param];
}

- (void)onSendEventMsgReceive:(NSArray *)param {
    NSDictionary *message = [param firstObject];
    [self.eventSenderReceive addParam:message withIdKey:@"msgId"];
    
    if (self.eventSenderReceive.mainArray.count >= 5) {
        [self.eventSenderReceive sendEventToReactNativeWithType:@"observeReceiveMessage" eventName:@"observeReceiveMessage" countLimit:5];
    } else {
        [self.eventSenderReceive triggerSendEventAfterDelay:@"observeReceiveMessage" eventName:@"observeReceiveMessage" countLimit:5];
    }
}


- (void)onSendEventMsgStatus:(NSArray *)param {
    NSDictionary *message = [param firstObject];
    NSString *msgType = message[@"msgType"];
//    [dic2 setObject:@"image" forKey:@"msgType"];

    if ([msgType isEqual:@"image"] || [msgType isEqual:@"video"]) {
        [self.eventSenderStatus addParam:message withIdKey:@"msgId"];
        
        if (self.eventSenderStatus.mainArray.count >= 5) {
            [self.eventSenderStatus sendEventToReactNativeWithType:@"observeMsgStatus" eventName:@"observeMsgStatus" countLimit:5];
        } else {
            [self.eventSenderStatus triggerSendEventAfterDelay:@"observeMsgStatus" eventName:@"observeMsgStatus" countLimit:5];
        }
    } else{
        [_bridge.eventDispatcher sendDeviceEventWithName:@"observeMsgStatus" body:@{@"data": param}];
    }
    
}

- (void)onUploadQueueAttachment:(NSDictionary *)param {
    [self.eventSenderProgress addParam:param withIdKey:@"messageId"];
    
    if (self.eventSenderProgress.mainArray.count >= 10) {
        [self.eventSenderProgress sendEventToReactNativeWithType:@"observeProgressSend" eventName:@"observeProgressSend" countLimit:10];
    } else {
        [self.eventSenderProgress triggerSendEventAfterDelay:@"observeProgressSend" eventName:@"observeProgressSend" countLimit:10];
    }
}

- (NSDictionary *)dictChangeFromJson:(NSString *)strJson{
    NSData* data = [strJson dataUsingEncoding:NSUTF8StringEncoding];
    __autoreleasing NSError* error = nil;
    id result = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&error];
    if (error != nil) return nil;
    return result;
}

@synthesize bridge = _bridge;
- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

// Implement the change observer method
- (void)photoLibraryDidChange:(PHChange *)changeInstance {
    // This method is called whenever the photo library changes
    NSLog(@"Photo library has changed.");
    NSDictionary *param = @{@"photoLibraryDidChange": @YES};
    [_bridge.eventDispatcher sendDeviceEventWithName:@"observePhotoLibraryDidChange" body:param];

    // You can handle specific changes, such as detecting new photos added here
}

RCT_EXPORT_MODULE()

//手动登录
RCT_EXPORT_METHOD(login:(nonnull NSString *)account token:(nonnull NSString *)token appKey:(NSString *)appKey
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject){
    //请将 NIMMyAccount 以及 NIMMyToken 替换成您自己提交到此App下的账号和密码
    [[NIMSDK sharedSDK] registerWithAppID:appKey cerName:@"ZYZJIM_IOS"];
    
    [[[NIMSDK sharedSDK] loginManager] login:account token:token authType:NIMSDKAuthTypeDynamicToken loginExt:@"" completion:^(NSError *error)
     {
        if (!error) {
            resolve(account);
        }else{
            NSString *strEorr = @"登录失败";
            reject(@"-1",strEorr, nil);
            NSLog(@"%@:%@",strEorr,error);
        }
    }];
    
    [NIMViewController initWithController].strToken = token;
    [NIMViewController initWithController].strAccount = account;
}

RCT_EXPORT_METHOD(startObserverMediaChange){
    if (!self) return;
    [[PHPhotoLibrary sharedPhotoLibrary] registerChangeObserver:self];
}

RCT_EXPORT_METHOD(stopObserverMediaChange){
    if (!self) return;
    [[PHPhotoLibrary sharedPhotoLibrary] unregisterChangeObserver:self];
}

//手动登录
RCT_EXPORT_METHOD(autoLogin:(nonnull NSString *)account token:(nonnull NSString *)token appKey:(nonnull NSString *)appKey
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject){
    
    
    [[NIMSDK sharedSDK] registerWithAppID:appKey cerName:@"ZYZJIM_IOS"];
    
    NIMAutoLoginData *loginData = [[NIMAutoLoginData alloc] init];
    loginData.account = account;
    loginData.token = token;
    loginData.authType = NIMSDKAuthTypeDynamicToken;
    loginData.loginExtension = @"";
    [[[NIMSDK sharedSDK] loginManager] autoLogin:loginData];
    
    [NIMViewController initWithController].strToken = token;
    [NIMViewController initWithController].strAccount = account;
    
    if ([account length] && [token length]) {
        resolve(account);
    }else{
        NSString *strEorr = @"登录失败";
        reject(@"-1",strEorr, nil);
        NSLog(@"%@:%@",strEorr,@"''");
    }
}

RCT_EXPORT_METHOD(replyMessage:(nonnull NSDictionary *)params
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController] replyMessage:params success:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

// //手动登录
// RCT_EXPORT_METHOD(updateMyCustomInfo:(nonnull NSString *)newInfo teamId:(nonnull NSString *)teamId
//                   resolve:(RCTPromiseResolveBlock)resolve
//                   reject:(RCTPromiseRejectBlock)reject){
//     /// newInfo 自定义属性
//         NIMTeamHandler completion = ^(NSError * __nullable error)
//         {
//             if (error == nil) {
//                 /// 更新自定义属性成功
//                 NSLog(@"[Update my custom info as %@ succeed.]", newInfo);
//                 resolve(@"ok");
//             } else {
//                 /// 更新自定义属性失败
//                 RCTLogWarn(@"[Update my custom info as %@ error.]", error);
//                 reject(@"-1",error.description, nil);
//             }
//         };
//         /// 更新自定义属性
//         [[[NIMSDK sharedSDK] teamManager] updateMyCustomInfo:newInfo
//                                                       inTeam:teamId
//                                                   completion:completion];
// }

//注销用户
RCT_EXPORT_METHOD(logout){
    [[[NIMSDK sharedSDK] loginManager] logout:^(NSError *error){
    }];
}
//手动删除最近聊天列表
RCT_EXPORT_METHOD(deleteRecentContact:(nonnull NSString * )recentContactId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[NIMViewController initWithController]deleteCurrentSession:recentContactId andback:^(NSString *error) {
        if (!error) {
            resolve(@"200");
        }else{
            reject(@"-1",error, nil);
        }
    }];
}

RCT_EXPORT_METHOD(removeSession:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType) {
    [[NIMViewController initWithController] removeSession:sessionId sessionType:sessionType];
}
//回调最近聊天列表
RCT_EXPORT_METHOD(getRecentContactList:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    [[NIMViewController initWithController]getRecentContactListsuccess:^(id param) {
        resolve(param);
    } andError:^(NSString *error) {
        reject(@"-1",error,nil);
    }];
    
}
////清空聊天记录
//RCT_EXPORT_METHOD(clearMessage:(nonnull  NSString *)sessionId type:(nonnull  NSString *)type){
//    [[ConversationViewController initWithConversationViewController] clearMsg:sessionId type:type];
//
//}
//获取本地用户资料
RCT_EXPORT_METHOD(getUserInfo:(nonnull NSString * )contactId  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject){
    [[ContactViewController initWithContactViewController]getUserInFo:contactId Success:^(id param) {
        resolve(param);
    }];
}

RCT_EXPORT_METHOD(removeReactionMessage:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType messageId:(nonnull NSString *)messageId accId:(nonnull NSString *)accId isSendMessage:(BOOL *)isSendMessage resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    NSLog(@"sessionId - %@, sessionType - %@, messageId - %@, accId - %@, isSendMessage - %@", sessionId, sessionType, messageId, accId ,[NSNumber numberWithBool:isSendMessage]);
    [[ConversationViewController initWithConversationViewController] removeReactionMessage:sessionId sessionType:sessionType messageId:messageId accId:accId isSendMessage:isSendMessage success:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(updateReactionMessage:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType messageId:(nonnull NSString *)messageId messageNotifyReactionId:(nonnull NSString *)messageNotifyReactionId reaction:(nonnull NSDictionary *)reaction resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] updateReactionMessage:sessionId sessionType:sessionType messageId:messageId messageNotifyReactionId:messageNotifyReactionId reaction:reaction success:^(id param) {
        resolve(param);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

//获取服务器用户资料
RCT_EXPORT_METHOD(fetchUserInfo:(nonnull NSString * )contactId   resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ContactViewController initWithContactViewController]fetchUserInfos:contactId Success:^(id param) {
        resolve(param);
    } error:^(NSString *error) {
        reject(@"-1",error, nil);
    }];
}
//保存好友备注
RCT_EXPORT_METHOD(updateUserInfo:(nonnull NSString * )contactId  alias:(nonnull NSString *)alias resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    
    [[ContactViewController initWithContactViewController] upDateUserInfo:contactId alias:alias Success:^(id param) {
        resolve(param);
    } error:^(NSString *error) {
        reject(@"-1",error,nil);
    }];
}
//保存用户信息
RCT_EXPORT_METHOD(updateMyUserInfo:(NSDictionary *)userInFo resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ContactViewController initWithContactViewController] updateMyUserInfo:userInFo Success:^(id param) {
        resolve(param);
    } error:^(NSString *error) {
        reject(@"-1",error,nil);
    }];
}
//添加好友
RCT_EXPORT_METHOD(addFriend:(nonnull  NSString * )contactId msg:(nonnull  NSString * )msg resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ContactViewController initWithContactViewController] adduserId:contactId andVerifyType:@"0" andMag:msg Friends:^(NSString *error) {
        reject(@"-1",error, nil);
    } Success:^(NSString *error) {
        resolve(error);
    }];
}
//添加好友，添加验证与否
RCT_EXPORT_METHOD(addFriendWithType:(nonnull  NSString *)contactId verifyType:(nonnull  NSString *)verifyType msg:(nonnull  NSString *)msg resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ContactViewController initWithContactViewController] adduserId:contactId andVerifyType:verifyType andMag:msg Friends:^(NSString *error) {
        reject(@"-1",error, nil);
    } Success:^(NSString *error) {
        resolve(error);
    }];
}
//通过/拒绝对方好友请求
RCT_EXPORT_METHOD(ackAddFriendRequest:(nonnull NSString*)targetId isAccept:(nonnull NSString *)isAccept timestamp:(nonnull  NSString * )timestamp resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    //    if ([msg isEqualToString:@"1"]) {
    NSLog(@"ackAddFriendRequest");
    [[NoticeViewController initWithNoticeViewController]ackAddFriendRequest:targetId isAccept:isAccept timestamp:timestamp sucess:^(id param) {
        resolve(param);
    } error:^(id erro) {
        reject(@"-1",erro, nil);
    }];
    //    }else{
    //        [[NoticeViewController initWithNoticeViewController]onRefuse:targetId timestamp:timestamp sucess:^(id param) {
    //            resolve(param);
    //        } error:^(id erro) {
    //            reject(@"-1",erro, nil);
    //        }];
    //    }
}
//获取通讯录好友
RCT_EXPORT_METHOD(startFriendList){
    [[ContactViewController initWithContactViewController] getAllContactFriends];
}


//获取通讯录好友回调
RCT_EXPORT_METHOD(getFriendList:(nonnull NSString *)keyword resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ContactViewController initWithContactViewController] getFriendList:^(id param) {
        resolve(param);
    } error:^(NSString *error) {
        reject(@"-1",error,nil);
    }];
}

//通讯录好友
RCT_EXPORT_METHOD(stopFriendList){
    
    [[ContactViewController initWithContactViewController] disealloc];
    
}
//删除好友
RCT_EXPORT_METHOD(deleteFriend:(nonnull  NSString *)contactId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ContactViewController initWithContactViewController]deleteFriends:contactId Success:^(id param) {
        resolve(param);
    } error:^(NSString *error) {
        reject(@"-1",error,nil);
    }];
}


//好友消息提醒开关
RCT_EXPORT_METHOD(setMessageNotify:(nonnull NSString *)contactId needNotify:(nonnull NSString *)needNotify resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    __weak typeof(self)weakSelf = self;
    [[ConversationViewController initWithConversationViewController]muteMessage:contactId mute:needNotify Succ:^(id param) {
        resolve(param);
        [weakSelf updateMessageList];
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

RCT_EXPORT_METHOD(searchFileMessages:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] searchFileMessages:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(updateTeamAvatar:(NSString *)teamId avatarUrl:(NSString *)avatarUrl resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[TeamViewController initWithTeamViewController] updateTeamAvatar:teamId avatarUrl:avatarUrl success:^(id params){
        resolve(params);
    } error:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(searchTextMessages:(NSString *)searchContent resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] searchTextMessages:searchContent success:^(id params) {
        resolve(params);
    } err:^(id err) {
        reject(@"-1", err, nil);
    }];
}

//search local Messages
RCT_EXPORT_METHOD(searchMessages:(nonnull NSString *)keyWords resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController] searchMessages:keyWords success:^(id param) {
        resolve(param);
    } err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

RCT_EXPORT_METHOD(getMessageById:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType messageId:(nonnull NSString *)messageId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController] getMessageById:sessionId sessionType:sessionType messageId:messageId success:^(id params) {
        resolve(params);
    }];
}

//search local Messages in session
RCT_EXPORT_METHOD(searchMessagesinCurrentSession:(NSString *)keyWords anchorId:(NSString *)anchorId limit:(int)limit messageType:(NSArray *)messageType direction:(int)direction messageSubTypes:(NSArray *)messageSubTypes resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController] searchMessagesinCurrentSession:keyWords anchorId:anchorId limit:limit messageType:messageType direction:direction messageSubTypes:messageSubTypes success:^(id param) {
        NSLog(@"paramparamparamparam %@", param);
        resolve(param);
    } err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}


//刷新最近会话列表
- (void)updateMessageList{
    [[NIMViewController initWithController]getResouces];
    NSLog(@"---updateMessageList");
}

//删除最近会话列表
- (void)removAllRecentSessions{
    id<NIMConversationManager> manager = [[NIMSDK sharedSDK] conversationManager];
    //    [manager deleteAllMessages:YES];
    NIMDeleteMessagesOption *option = [[NIMDeleteMessagesOption alloc]init];
    option.removeSession = YES;
    [manager deleteAllMessages:option];
}

//获取系统消息
RCT_EXPORT_METHOD(startSystemMsg){
    [[NoticeViewController initWithNoticeViewController] initWithDelegate];
}
//停止系统消息
RCT_EXPORT_METHOD(stopSystemMsg){
    [[NoticeViewController initWithNoticeViewController] stopSystemMsg];
}
//删除系统消息一列
RCT_EXPORT_METHOD(deleteSystemMessage:(nonnull  NSString *)targetId timestamp:(nonnull NSString *)timestamp){
    [[NoticeViewController initWithNoticeViewController] deleteNotice:targetId timestamp:timestamp];
}
//将系统消息标记为已读
RCT_EXPORT_METHOD(setAllread){
    [[NoticeViewController initWithNoticeViewController] setAllread];
}
//清空系统信息
RCT_EXPORT_METHOD(clearSystemMessages){
    [[NoticeViewController initWithNoticeViewController] deleAllNotic];
}

RCT_EXPORT_METHOD(sendFileMessageWithSession:(nonnull NSString *)path fileName:(nonnull NSString *)fileName fileType:(NSString*)fileType sessionId:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType sessionName:(nonnull NSString *)sessionName resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] sendFileMessageWithSession:path fileName:fileName fileType:fileType sessionId:sessionId sessionType:sessionType sessionName:sessionName success:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(sendTextMessageWithSession:(nonnull NSString *)msgContent sessionId:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType sessionName:(nonnull NSString *)sessionName messageSubType:(NSInteger)messageSubType) {
    [[ConversationViewController initWithConversationViewController] sendTextMessageWithSession:msgContent sessionId:sessionId sessionType:sessionType sessionName:sessionName messageSubType:messageSubType];
}

RCT_EXPORT_METHOD(sendGifMessageWithSession:(NSString *)url aspectRatio:(NSString *)aspectRatio sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType sessionName:(NSString *)sessionName) {
    [[ConversationViewController initWithConversationViewController] sendGifMessageWithSession:url aspectRatio:aspectRatio sessionId:sessionId sessionType:sessionType sessionName:sessionName];
}

RCT_EXPORT_METHOD(sendImageMessageWithSession:(nonnull NSString *)path isHighQuality:(nonnull BOOL *)isHighQuality sessionId:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType sessionName:(nonnull NSString *)sessionName) {
    [[ConversationViewController initWithConversationViewController] sendImageMessageWithSession:path isHighQuality:isHighQuality sessionId:sessionId sessionType:sessionType sessionName:sessionName];
}

RCT_EXPORT_METHOD(sendVideoMessageWithSession:(nonnull NSString *)path sessionId:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType sessionName:(nonnull NSString *)sessionName) {
    [[ConversationViewController initWithConversationViewController] sendVideoMessageWithSession:path sessionId:sessionId sessionType:sessionType sessionName:sessionName];
}

//会话开始
RCT_EXPORT_METHOD(startSession:(nonnull  NSString *)sessionId type:(nonnull  NSString *)type myUserName:(NSString *)myUserName myUserID:(NSString *)myUserID){
    [[ConversationViewController initWithConversationViewController]startSession:sessionId withType:type myUserName:myUserName myUserID:myUserID];
}
//会话通知返回按钮
RCT_EXPORT_METHOD(stopSession){
    [[ConversationViewController initWithConversationViewController]stopSession];
}

RCT_EXPORT_METHOD(addEmptyTemporarySession:(NSString *)sessionId temporarySessionRef:(NSDictionary *)temporarySessionRef resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] addEmptyTemporarySession:sessionId temporarySessionRef:temporarySessionRef success:^(id params) {
        resolve(params);
    } error:^(id err) {
        reject(@"-1", err, nil);
    }];
}

RCT_EXPORT_METHOD(removeTemporarySessionRef:(nonnull NSString *)sessionId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] removeTemporarySessionRef:sessionId success:^(id params) {
        resolve(params);
    }];
}

//聊天界面历史记录
RCT_EXPORT_METHOD(queryMessageListEx:(nonnull  NSString *)messageId limit:(int)limit direction:(int)direction sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController]localSession:limit currentMessageID:messageId direction:direction sessionId:sessionId sessionType:sessionType success:^(id param) {
        resolve(param);
    } err:^(id erro) {
        reject(@"-1",erro, nil);
    }];
}


RCT_EXPORT_METHOD(addEmptyRecentSessionWithoutMessage:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] addEmptyRecentSessionWithoutMessage:sessionId sessionType:sessionType];
    resolve(@"200");
}

RCT_EXPORT_METHOD(sendCustomMessageOfChatbot:(nonnull NSString *)sessionId customerServiceType:(nonnull NSString *)customerServiceType resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] sendCustomMessageOfChatbot:sessionId customerServiceType:customerServiceType success:^(id param) {
        resolve(param);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(reactionMessage:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType messageId:(nonnull NSString *)messageId reaction:(nonnull NSDictionary *)reaction resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] reactionMessage:sessionId sessionType:sessionType messageId:messageId reaction:reaction success:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

//本地历史记录
RCT_EXPORT_METHOD(queryMessageListHistory:(nonnull  NSString *)sessionId sessionType:(nonnull  NSString *)sessionType timeLong:(nonnull  NSString *)timeLong direction:(nonnull  NSString *)direction limit:(nonnull  NSString *)limit   asc:(BOOL)asc resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController]localSessionList:sessionId sessionType:sessionType timeLong:timeLong direction:direction limit:limit asc:asc success:^(id param) {
        resolve(param);
    }];
}

RCT_EXPORT_METHOD(addEmptyPinRecentSession:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType) {
    [[ConversationViewController initWithConversationViewController] addEmptyPinRecentSession:sessionId sessionType:sessionType];
}

RCT_EXPORT_METHOD(forwardMessagesToMultipleRecipients:(nonnull NSDictionary *)params resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] forwardMessagesToMultipleRecipients:params success:^(id param) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

//转发消息
RCT_EXPORT_METHOD(sendForwardMessage:(nonnull NSArray *)messageIds sessionId:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType content:(nonnull NSString *)content parentId:(nonnull NSString *)parentId isHaveMultiMedia:(BOOL *)isHaveMultiMedia resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    
    @try {
        [[ConversationViewController initWithConversationViewController]forwardMessage:messageIds sessionId:sessionId sessionType:sessionType content:content parentId:parentId isHaveMultiMedia:isHaveMultiMedia success:^(id param) {
            resolve(param);
        }];
        // Process the arguments and send the message
    } @catch (NSException *exception) {
        NSLog(@"Error: %@", exception.reason);
    }
}
//撤回消息
RCT_EXPORT_METHOD(revokeMessage:(nonnull NSString *)messageId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController]revokeMessage:messageId success:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

RCT_EXPORT_METHOD(sendCustomNotification:( NSDictionary *)dataDict toSessionId:(NSString*)toSessionId toSessionType:(NSString*)toSessionType resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController]sendCustomNotification:dataDict toSessionId:toSessionId toSessionType:toSessionType success:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}


RCT_EXPORT_METHOD(updateMessageSentStickerBirthday:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType messageId:(nonnull NSString *)messageId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] updateMessageSentStickerBirthday:sessionId sessionType:sessionType messageId:messageId success:^(id param) {
        resolve(param);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(removeMessage:(NSString *)messageId sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType) {
    [[ConversationViewController initWithConversationViewController] removeMessage:messageId sessionId:sessionId sessionType:sessionType];
}

RCT_EXPORT_METHOD(createNotificationBirthday:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType memberContactId:(NSString *)memberContactId memberName:(NSString *)memberName  resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] createNotificationBirthday:sessionId sessionType:sessionType memberContactId:memberContactId memberName:memberName success:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(cancelSendingMessage:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] cancelSendingMessage:sessionId sessionType:sessionType messageId:messageId success:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

//重发消息
RCT_EXPORT_METHOD(resendMessage:(nonnull NSString *)messageId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController]resendMessage:messageId success:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
    
}

//删除会话内容
RCT_EXPORT_METHOD(deleteMessage:(nonnull NSString *)messageId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController] deleteMsg:messageId success:^(id param) {
        resolve(param);
    } err:^(id err) {
        reject(@"-1", err, nil);
    }];
}
//清空聊天记录
RCT_EXPORT_METHOD(clearMessage:(nonnull  NSString *)sessionId sessionId:(nonnull  NSString *)type){
    [[ConversationViewController initWithConversationViewController] clearMsg:sessionId type:type];
}
//发送文字消息,atUserIds为@用户名单，@功能仅适用于群组
RCT_EXPORT_METHOD(sendTextMessage:(nonnull  NSString *)content atUserIds:(NSArray *)atUserIds messageSubType:(NSInteger )messageSubType isSkipFriendCheck:(BOOL *)isSkipFriendCheck isSkipTipForStranger:(BOOL *)isSkipTipForStranger) {
    [[ConversationViewController initWithConversationViewController]sendMessage:content andApnsMembers:atUserIds messageSubType:messageSubType isSkipFriendCheck:isSkipFriendCheck isSkipTipForStranger:isSkipTipForStranger];
    RCTLogWarn(@"RCT_EXPORT_METHOD sendTextMessage at %@", content);
}

RCT_EXPORT_METHOD(setCancelResendMessage:(nonnull NSString *)messageId sessionId:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType) {
    [[ConversationViewController initWithConversationViewController] setCancelResendMessage:messageId sessionId:sessionId sessionType:sessionType];
}

//发送文字消息,atUserIds为@用户名单，@功能仅适用于群组
RCT_EXPORT_METHOD(sendGifMessage:(nonnull  NSString *)url aspectRatio:(NSString *)aspectRatio atUserIds:(NSArray *)atUserIds isSkipFriendCheck:(BOOL *)isSkipFriendCheck isSkipTipForStranger:(BOOL *)isSkipTipForStranger) {
    [[ConversationViewController initWithConversationViewController] sendGifMessage:url aspectRatio:aspectRatio andApnsMembers:atUserIds isSkipFriendCheck:isSkipFriendCheck isSkipTipForStranger:isSkipTipForStranger];
}

RCT_EXPORT_METHOD(sendMultiMediaMessage:(NSArray *)listMedia parentId:(nullable NSString *)parentId isSkipFriendCheck:(BOOL *)isSkipFriendCheck isSkipTipForStranger:(BOOL *)isSkipTipForStranger resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] sendMultiMediaMessage:listMedia parentId:parentId isSkipFriendCheck:isSkipFriendCheck isSkipTipForStranger:isSkipTipForStranger success:^(id params) {
        resolve(params);
    } error:^(id err) {
        reject(@"-1", err, nil);
    }];
}

//发送图片消息
RCT_EXPORT_METHOD(sendImageMessages:(nonnull NSString *)path displayName:(nonnull NSString *)displayName isHighQuality:(nonnull BOOL *)isHighQuality isSkipCheckFriend:(BOOL *)isSkipCheckFriend isSkipTipForStranger:(BOOL *)isSkipTipForStranger) {
    [[ConversationViewController initWithConversationViewController] sendImageMessages:path displayName:displayName isHighQuality:isHighQuality isSkipCheckFriend:isSkipCheckFriend isSkipTipForStranger:isSkipTipForStranger parentId:nil indexCount:nil];
}

RCT_EXPORT_METHOD(sendFileMessage:(nonnull  NSString *)filePath fileName:(nonnull  NSString *)fileName fileType:(NSString *)fileType resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController]sendFileMessage:filePath fileName:fileName fileType:fileType success:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

RCT_EXPORT_METHOD(sendMessageTeamNotificationRequestJoin:(nonnull  NSDictionary *)sourceId targets:(nonnull NSArray *)targets type:(nonnull NSNumber*)type resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController]sendMessageTeamNotificationRequestJoin:sourceId targets:targets type:type success:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

//RCT_EXPORT_METHOD(checkIsMyFriend:(nonnull  NSString *)sessionId callBack:(RCTResponseSenderBlock)callback) {
//    BOOL isMyFriend    = [[NIMSDK sharedSDK].userManager isMyFriend:sessionId];
//    callBack(isMyFriend);
//}

//发送音频消息
RCT_EXPORT_METHOD(sendAudioMessage:(nonnull NSString *)file duration:(nonnull NSString *)duration isSkipFriendCheck:(nonnull BOOL *)isSkipFriendCheck isSkipTipForStranger:(nonnull BOOL *)isSkipTipForStranger){
    [[ConversationViewController initWithConversationViewController]sendAudioMessage:file duration:duration isSkipFriendCheck:isSkipFriendCheck isSkipTipForStranger:isSkipTipForStranger];
}

//发送自定义消息
RCT_EXPORT_METHOD(sendCustomMessage:(NSInteger)custType attachment: (nonnull  NSDictionary *)attachment){
    //    [[ConversationViewController initWithConversationViewController]sendCustomMessage:custType data:attachment];
}

RCT_EXPORT_METHOD(updateRecentToTemporarySession:(nonnull NSString *)sessionId messageId:(nonnull NSString *)messageId temporarySessionRef:(nonnull NSDictionary *)temporarySessionRef) {
    [[ConversationViewController initWithConversationViewController] updateRecentToTemporarySession:sessionId messageId:messageId temporarySessionRef:temporarySessionRef];
}

//发送自定义消息
RCT_EXPORT_METHOD(forwardMultipleTextMessage: (nonnull  NSDictionary *)attachment  sessionId:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType content:(NSString *)content){
    //forwardMessagesText:CustomMessageTypeFowardMultipleText data:attachment sessionId:sessionId sessionType:sessionType content:content data:attachment
    [[ConversationViewController initWithConversationViewController] forwardMultipleTextMessage:attachment sessionId:sessionId sessionType:sessionType content:content];
}

RCT_EXPORT_METHOD(forwardMultiTextMessageToMultipleRecipients:(nonnull NSDictionary *)params resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] forwardMultiTextMessageToMultipleRecipients:params success:^(id param) {
        resolve(param);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(updateRecentSessionIsCsrOrChatbot:(nonnull NSString *)sessionId type:(nonnull NSString *)type name:(NSString *)name) {
    [[ConversationViewController initWithConversationViewController]updateRecentSessionIsCsrOrChatbot:sessionId type:type name:name];
}

RCT_EXPORT_METHOD(addEmptyRecentSessionCustomerService:(nonnull NSArray *)data) {
    [[ConversationViewController initWithConversationViewController] addEmptyRecentSessionCustomerService:data];
}

RCT_EXPORT_METHOD(addEmptyRecentSession:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType) {
    [[ConversationViewController initWithConversationViewController]addEmptyRecentSession:sessionId sessionType:sessionType];
}

RCT_EXPORT_METHOD(updateMessageOfCsr:(nonnull NSString *)messageId sessionId:(nonnull NSString *)sessionId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] updateMessageOfCsr:messageId sessionId:sessionId success:^(id param) {
        resolve(param);
    } error:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(updateMessageOfChatBot:(nonnull NSString *)messageId sessionId:(nonnull NSString *)sessionId chatBotType:(nonnull NSString *)chatBotType chatBotInfo:(NSDictionary *)chatBotInfo resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController] updateMessageOfChatBot:messageId sessionId:sessionId chatBotType:chatBotType chatBotInfo:chatBotInfo success:^(id params) {
        resolve(params);
    } error:^(id error) {
        reject(@"-1", error, nil);
    }];
}

//发送视频消息
RCT_EXPORT_METHOD(sendVideoMessage:(nonnull NSString *)file duration:(nonnull NSString *)duration width:(nonnull  NSNumber *)width height:(nonnull  NSNumber *)height displayName:(nonnull  NSString *)displayName isSkipFriendCheck:(BOOL *)isSkipFriendCheck isSkipTipForStranger:(BOOL *)isSkipTipForStranger){
    [[ConversationViewController initWithConversationViewController] sendVideoMessage:file duration:duration width:width height:height displayName:displayName isSkipFriendCheck:isSkipFriendCheck isSkipTipForStranger:isSkipTipForStranger parentId:nil indexCount:nil];
}

//
RCT_EXPORT_METHOD(setStrangerRecentReplyed:(nonnull  NSString *)sessionId) {
    NIMSession *session = [NIMSession session:sessionId type:NIMSessionTypeP2P];
    NIMRecentSession *recent = [[NIMSDK sharedSDK].conversationManager recentSessionBySession:session];
    
    if (!session) return;
    
    NSMutableDictionary *localExt = recent.localExt ? [recent.localExt mutableCopy] : [[NSMutableDictionary alloc] init];
    [localExt setObject:@(YES) forKey:@"isReplyStranger"];
    [[NIMSDK sharedSDK].conversationManager updateRecentLocalExt:localExt recentSession:recent];
}

//发送地理位置消息
RCT_EXPORT_METHOD(sendLocationMessage:(nonnull NSString *)sessionId sessionType:(nonnull NSString *)sessionType latitude:(nonnull  NSString *)latitude longitude:(nonnull  NSString *)longitude address:(nonnull  NSString *)address resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController] sendLocationMessage:sessionId sessionType: sessionType latitude:latitude longitude:longitude address:address success:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

RCT_EXPORT_METHOD(queryAllTeams:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] queryAllTeams:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(queryTeamByName:(nonnull NSString *)search resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] queryTeamByName:search success:^(id params){
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

//开启录音权限
RCT_EXPORT_METHOD(onTouchVoice:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ConversationViewController initWithConversationViewController]onTouchVoiceSucc:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}
//  开始录音
RCT_EXPORT_METHOD(startAudioRecord){
    [[ConversationViewController initWithConversationViewController]onStartRecording];
}
//  结束录音
RCT_EXPORT_METHOD(endAudioRecord){
    [[ConversationViewController initWithConversationViewController]onStopRecording];
}
//  取消录音
RCT_EXPORT_METHOD(cancelAudioRecord){
    [[ConversationViewController initWithConversationViewController]onCancelRecording];
}

//更新录音消息为已播放
RCT_EXPORT_METHOD(updateAudioMessagePlayStatus:(nonnull NSString *)strMessageID){
    [[ConversationViewController initWithConversationViewController]updateAudioMessagePlayStatus:strMessageID];
}

//开始播放录音
RCT_EXPORT_METHOD(play:(nonnull NSString *)filepath isExternalSpeaker:(BOOL *)isExternalSpeaker){
    [[ConversationViewController initWithConversationViewController]play:filepath isExternalSpeaker:isExternalSpeaker];
}
//播放本地资源录音
RCT_EXPORT_METHOD(playLocal:(nonnull NSString *)name type:(nonnull NSString *)type){
    NSString *path = [[NSBundle mainBundle] pathForResource:name ofType:type];
    [[ConversationViewController initWithConversationViewController]play:path isExternalSpeaker: YES];
}

RCT_EXPORT_METHOD(getIsPlayingRecord: (RCTResponseSenderBlock)callback){
    BOOL isPlayingRecord = [[ConversationViewController initWithConversationViewController] isPlayingRecord];
    
    callback(@[@(isPlayingRecord)]);
}

//停止播放
RCT_EXPORT_METHOD(stopPlay){
    [[ConversationViewController initWithConversationViewController]stopPlay];
}

RCT_EXPORT_METHOD(switchAudioOutputDevice: (BOOL *)isExternalSpeaker){
    [[ConversationViewController initWithConversationViewController]switchAudioOutputDevice:isExternalSpeaker];
}

//发送红包消息
RCT_EXPORT_METHOD(sendRedPacketMessage:(NSString *)type comments:(NSString *)comments serialNo:(NSString *)serialNo){
    [[ConversationViewController initWithConversationViewController] sendRedPacketMessage:type comments:comments serialNo:serialNo];
}

//发送拆红包消息
RCT_EXPORT_METHOD(sendRedPacketOpenMessage:(NSString *)sendId hasRedPacket:(NSString *)hasRedPacket serialNo:(NSString *)serialNo){
    [[ConversationViewController initWithConversationViewController] sendRedPacketOpenMessage:sendId hasRedPacket:hasRedPacket serialNo:serialNo];
}

//发送转账消息
RCT_EXPORT_METHOD(sendBankTransferMessage:(NSString *)amount comments:(NSString *)comments serialNo:(NSString *)serialNo){
    [[ConversationViewController initWithConversationViewController] sendBankTransferMessage:amount comments:comments serialNo:serialNo];
}
//发送名片消息
RCT_EXPORT_METHOD(sendCardMessage:(NSString *)toSessionType sessionId:(NSString *)toSessionId name:(NSString *)name imgPath:(NSString *)strImgPath cardSessionId:(NSString *)cardSessionId cardSessionType:(NSString *)cardSessionType){
    
    [[ConversationViewController initWithConversationViewController] sendCardMessage:toSessionType sessionId:toSessionId name:name imgPath:strImgPath cardSessionId:cardSessionId cardSessionType:cardSessionType];
}

//发送提醒消息
RCT_EXPORT_METHOD(sendTipMessage:(nonnull  NSString *)content){
    //    [[ConversationViewController initWithConversationViewController]sendMessage:content];
}
//获取黑名单列表
RCT_EXPORT_METHOD(startBlackList){
    [[BankListViewController initWithBankListViewController]getBlackList];
}
RCT_EXPORT_METHOD(stopBlackList){
}
//添加黑名单
RCT_EXPORT_METHOD(addToBlackList:(nonnull NSString *)contactId  resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[BankListViewController initWithBankListViewController]addToBlackList:contactId success:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro, nil);
    }];
}
//移出黑名单
RCT_EXPORT_METHOD(removeFromBlackList:(nonnull NSString *)contactId  resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[BankListViewController initWithBankListViewController]removeFromBlackList:contactId success:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro, nil);
    }];
}


#pragma mark -- team方法 -----------------------------------------------

//获取群回调列表
RCT_EXPORT_METHOD(getTeamList:(nonnull NSString *)keyWord resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[TeamViewController initWithTeamViewController]getTeamList:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

//开始获取群组
RCT_EXPORT_METHOD(startTeamList){
    
    [[TeamViewController initWithTeamViewController]initWithDelegate];
}

//退出讨论组列表
RCT_EXPORT_METHOD(stopTeamList){
    [[TeamViewController initWithTeamViewController]stopTeamList];
}

//获取本地群资料
RCT_EXPORT_METHOD(getTeamInfo:(nonnull NSString *)teamId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[TeamViewController initWithTeamViewController]getTeamInfo:teamId Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro, nil);
    }];
}

//开启/关闭群组消息提醒
RCT_EXPORT_METHOD(setTeamNotify:(nonnull NSString *)teamId needNotify:(nonnull NSString *)needNotify resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    __weak typeof(self)weakSelf = self;
    [[TeamViewController initWithTeamViewController]muteTeam:teamId mute:needNotify Succ:^(id param) {
        resolve(param);
        [weakSelf updateMessageList];
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

//群成员禁言 mute字符串:0是false 1是true
RCT_EXPORT_METHOD(setTeamMemberMute:(nonnull NSString *)teamId contactId:(nonnull NSString *)contactId mute:(nonnull NSString *)mute resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[TeamViewController initWithTeamViewController]setTeamMemberMute:teamId contactId:contactId mute:mute Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

RCT_EXPORT_METHOD(downloadAttachment:(nonnull NSString *)messageId sessionId:(nonnull NSString *)sessionId toSessionType:(nonnull NSString *)toSessionType) {
    [[ConversationViewController initWithConversationViewController]downloadAttachment:messageId sessionId:sessionId toSessionType:toSessionType];
}

RCT_EXPORT_METHOD(readAllMessageOnlineServiceByListSession:(nonnull NSArray *)listSessionId) {
    [[ConversationViewController initWithConversationViewController]readAllMessageOnlineServiceByListSession:listSessionId];
}

//获取服务器群资料
RCT_EXPORT_METHOD(fetchTeamInfo:(nonnull NSString *)teamId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[TeamViewController initWithTeamViewController]fetchTeamInfo:teamId Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}
//获取服务器群成员资料
RCT_EXPORT_METHOD(fetchTeamMemberList:(nonnull NSString *)teamId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[TeamViewController initWithTeamViewController] getTeamMemberList:teamId Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
//        reject(@"-1",erro,nil);
    }];
}

//获取群成员资料及设置
RCT_EXPORT_METHOD(fetchTeamMemberInfo:(nonnull NSString *)teamId contactId:(nonnull NSString *)contactId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[TeamViewController initWithTeamViewController] fetchTeamMemberInfo:teamId  contactId:contactId Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

//更新群成员名片
RCT_EXPORT_METHOD(updateMemberNick:(nonnull NSString *)teamId contactId:(nonnull NSString *)contactId nick:(nonnull NSString*)nick resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[TeamViewController initWithTeamViewController]updateMemberNick:teamId contactId:contactId nick:nick Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

//创建群组
RCT_EXPORT_METHOD(createTeam:(nonnull NSDictionary *)filelds type:(nonnull NSString *)type accounts:(nonnull NSArray *)accounts resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[TeamViewController initWithTeamViewController]createTeam:filelds type:type accounts:accounts Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

//更新群资料,
RCT_EXPORT_METHOD(updateTeam:(nonnull NSString *)teamId fieldType:(nonnull NSString *)fieldType value:(nonnull NSString*)value resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[TeamViewController initWithTeamViewController]updateTeam:teamId fieldType:fieldType value:value Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}


//申请加入群组
RCT_EXPORT_METHOD(applyJoinTeam:(nonnull NSString *)teamId reason:(nonnull NSString *)reason  resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    
    [[TeamViewController initWithTeamViewController]applyJoinTeam:teamId message:reason Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

//解散群组
RCT_EXPORT_METHOD(dismissTeam:(nonnull NSString *)teamId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[TeamViewController initWithTeamViewController] dismissTeam:teamId Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

//拉人入群
RCT_EXPORT_METHOD(addMembers:(nonnull NSString *)teamId accounts:(nonnull NSArray *)accounts type:(nonnull NSString *)type resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[TeamViewController initWithTeamViewController] addMembers:teamId accounts:accounts type:type Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

RCT_EXPORT_METHOD(updateActionHideRecentSession:(NSString *)sessionId sessionType:(NSString *)sessionType isHideSession:(BOOL *)isHideSession isPinCode:(BOOL *)isPinCode resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ConversationViewController initWithConversationViewController] updateActionHideRecentSession:sessionId sessionType:sessionType isHideSession:isHideSession isPinCode:isPinCode success:^(id param) {
        resolve(param);
    } error:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(updateIsSeenMessage:(BOOL *)isSeenMessage) {
    [[ConversationViewController initWithConversationViewController] updateIsSeenMessage:isSeenMessage];
}
//踢人出群
RCT_EXPORT_METHOD(removeMember:(nonnull NSString *)teamId accounts:(nonnull NSArray *)count resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[TeamViewController initWithTeamViewController] removeMember:teamId accounts:count Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}
//主动退群
RCT_EXPORT_METHOD(quitTeam:(nonnull NSString *)teamId  resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[TeamViewController initWithTeamViewController]quitTeam:teamId Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}
//转让群组
RCT_EXPORT_METHOD(transferTeam:(nonnull NSString *)teamId account:(nonnull NSString *)account quit:(nonnull NSString *)quit resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[TeamViewController initWithTeamViewController] transferManagerWithTeam:teamId newOwnerId:account quit:quit Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

// Add user to manager in team
RCT_EXPORT_METHOD(addManagersToTeam:(nonnull NSString *)teamId userIds:(nonnull NSArray<NSString *>*)userIds resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    
    NIMTeamHandler completion = ^(NSError * __nullable error)
    {
        if (error == nil) {
            NSLog(@"[Add manager in team %@ succeed.]", teamId);
            resolve(teamId);
            /// your code ...
        } else {
            NSLog(@"[NSError message: %@]", error);
            reject(@"-1",error,nil);
        }
    };
    [[[NIMSDK sharedSDK] teamManager] addManagersToTeam:teamId
                                                  users:userIds
                                             completion:completion];
}

// Remove manager in team
RCT_EXPORT_METHOD(removeManagersFromTeam:(nonnull NSString *)teamId userIds:(nonnull NSArray<NSString *>*)userIds resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    
    NIMTeamHandler completion = ^(NSError * __nullable error)
    {
        if (error == nil) {
            NSLog(@"[Remove manager in team %@ succeed.]", teamId);
            resolve(teamId);
            /// your code ...
        } else {
            NSLog(@"[NSError message: %@]", error);
            reject(@"-1",error,nil);
        }
    };
    [[[NIMSDK sharedSDK] teamManager] removeManagersFromTeam:teamId
                                                       users:userIds
                                                  completion:completion];
}

//修改群昵称
RCT_EXPORT_METHOD(updateTeamName:(nonnull NSString *)teamId nick:(nonnull NSString *)nick  resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[TeamViewController initWithTeamViewController] updateTeamName:teamId nick:nick Succ:^(id param) {
        resolve(param);
    } Err:^(id erro) {
        reject(@"-1",erro,nil);
    }];
}

RCT_EXPORT_METHOD(setListCustomerServiceAndChatbot:(nonnull NSDictionary *)data resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[CacheUsers initWithCacheUsers] setListCustomerServiceAndChatbot:data];
    resolve(@"success");
}

#pragma mark ---- 获得缓存和处理缓存
//获取缓存大小
RCT_EXPORT_METHOD(getSessionCacheSize:(NSString *)sessionId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    NSString *documentPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *strDocPath = documentPath;
    NSArray *files = [[NSFileManager defaultManager] subpathsOfDirectoryAtPath:documentPath error:nil];
    if ([[NSFileManager defaultManager] fileExistsAtPath:documentPath]) {
        for (NSString *file in files) {
            if ([file hasSuffix:@"Global/Resources"]) {
                strDocPath = [strDocPath stringByAppendingPathComponent:file];
                break;
            }
        }
    }
    NSString *cacheMediaPath = [strDocPath stringByAppendingPathComponent:sessionId];
    
    CGFloat cacheSize = [self folderSizeAtPath:cacheMediaPath];
    
    NSString *displayFileSize = [NSByteCountFormatter stringFromByteCount:cacheSize
                                                               countStyle:NSByteCountFormatterCountStyleFile];
    resolve(displayFileSize);
}

//清除数据缓存
RCT_EXPORT_METHOD(cleanSessionCache:(NSString *)sessionId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    //Document
    NSString *documentPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *strDocPath = documentPath;
    NSArray *files = [[NSFileManager defaultManager] subpathsOfDirectoryAtPath:documentPath error:nil];
    if ([[NSFileManager defaultManager] fileExistsAtPath:documentPath]) {
        for (NSString *file in files) {
            if ([file hasSuffix:@"Global/Resources"]) {
                strDocPath = [strDocPath stringByAppendingPathComponent:file];
                break;
            }
        }
    }
    
    NSArray *documentListFile = [[NSFileManager defaultManager] subpathsOfDirectoryAtPath:documentPath error:nil];
    if ([[NSFileManager defaultManager] fileExistsAtPath:documentPath]) {
        for (NSString *file in documentListFile) {
            if ([[file lastPathComponent] containsString:@"png"] || [[file lastPathComponent] containsString:@"jpg"]) {
                NSFileManager *fileMgr = [NSFileManager defaultManager];
                NSError *err;
                NSString *filePath = [documentPath stringByAppendingPathComponent:file];
                [fileMgr removeItemAtPath:filePath error:&err];
            }
        }
    }
    
    NSString *cacheMediaPath = [strDocPath stringByAppendingPathComponent:sessionId];
    
    NSArray *cacheMediaFiles = [[NSFileManager defaultManager] subpathsOfDirectoryAtPath:cacheMediaPath error:nil];
    [self deleteFilesWithPath:cacheMediaPath andFiles:cacheMediaFiles];
    
    resolve(@"deleteSuccess");
}

RCT_EXPORT_METHOD(getListSessionsCacheSize:(NSArray *)sessionIds resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    NSString *documentPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSLog(@"documentPath %@", documentPath);
    NSString *strDocPath = documentPath;
    NSArray *files = [[NSFileManager defaultManager] subpathsOfDirectoryAtPath:documentPath error:nil];
    if ([[NSFileManager defaultManager] fileExistsAtPath:documentPath]) {
        for (NSString *file in files) {
            if ([file hasSuffix:@"Global/Resources"]) {
                strDocPath = [strDocPath stringByAppendingPathComponent:file];
                break;
            }
        }
    }
    
    float allSize = 0;
    NSMutableArray *arraySize = [[NSMutableArray alloc] init];
    for (NSString *sessionId in sessionIds) {
        NSString *cacheMediaPath = [strDocPath stringByAppendingPathComponent:sessionId];
        float cacheSize = [self folderSizeAtPath:cacheMediaPath];
        allSize += cacheSize;
        NSString *displayFileSize = [NSByteCountFormatter stringFromByteCount:cacheSize
                                                                   countStyle:NSByteCountFormatterCountStyleFile];
        [arraySize addObject:@{@"sessionId": sessionId, @"size": displayFileSize}];
    }
    
    resolve(@{@"data": arraySize, @"totalSize": [NSByteCountFormatter stringFromByteCount:allSize
                                                                               countStyle:NSByteCountFormatterCountStyleFile]});
}

//清除数据缓存
RCT_EXPORT_METHOD(cleanListSessionsCache:(NSArray *)sessionIds resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    //Document
    NSString *documentPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSLog(@"documentPath %@", documentPath);
    NSString *strDocPath = documentPath;
    NSArray *files = [[NSFileManager defaultManager] subpathsOfDirectoryAtPath:documentPath error:nil];
    if ([[NSFileManager defaultManager] fileExistsAtPath:documentPath]) {
        for (NSString *file in files) {
            if ([file hasSuffix:@"Global/Resources"]) {
                strDocPath = [strDocPath stringByAppendingPathComponent:file];
                break;
            }
        }
    }
    
    NSArray *documentListFile = [[NSFileManager defaultManager] subpathsOfDirectoryAtPath:documentPath error:nil];
    if ([[NSFileManager defaultManager] fileExistsAtPath:documentPath]) {
        for (NSString *file in documentListFile) {
            if ([[file lastPathComponent] containsString:@"png"] || [[file lastPathComponent] containsString:@"jpg"] || [[file lastPathComponent] containsString:@"acc"]) {
                NSFileManager *fileMgr = [NSFileManager defaultManager];
                NSError *err;
                NSString *filePath = [documentPath stringByAppendingPathComponent:file];
                [fileMgr removeItemAtPath:filePath error:&err];
            }
        }
    }
    
    for (NSString *sessionId in sessionIds) {
        NSString *cacheMediaPath = [strDocPath stringByAppendingPathComponent:sessionId];
        
        NSArray *cacheMediaFiles = [[NSFileManager defaultManager] subpathsOfDirectoryAtPath:cacheMediaPath error:nil];
        [self deleteFilesWithPath:cacheMediaPath andFiles:cacheMediaFiles];
    }
    
    resolve(@"deleteSuccess");
}

#pragma mark -- chatroom -----------------------------------------------

RCT_EXPORT_METHOD(loginChatroom:(nonnull NSDictionary *)params resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    [[ChatroomViewController initWithChatroomViewController] loginChatroom:params success:^(id param) {
        resolve(param);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(logoutChatroom:(nonnull NSString *)roomId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ChatroomViewController initWithChatroomViewController] logoutChatroom:roomId success:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(fetchChatroomInfo:(nonnull NSString *)roomId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ChatroomViewController initWithChatroomViewController] fetchChatroomInfo:roomId success:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(fetchChatroomMember:(nonnull NSString *)roomId userId:(nonnull NSString *)userId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ChatroomViewController initWithChatroomViewController] fetchChatroomMember:roomId userId:userId success:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(fetchChatroomMembers:(nonnull NSString *)roomId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ChatroomViewController initWithChatroomViewController] fetchChatroomMembers:roomId success:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

RCT_EXPORT_METHOD(fetchMessageHistory:(nonnull NSString *)roomId limit:(NSInteger)limit currentMessageId:(NSString *)currentMessageId orderBy:(NSString *)orderBy resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [[ChatroomViewController initWithChatroomViewController] fetchMessageHistory:roomId limit:limit currentMessageId:currentMessageId orderBy:orderBy success:^(id params) {
        resolve(params);
    } err:^(id error) {
        reject(@"-1", error, nil);
    }];
}

//删除文件夹下所有文件
- (void)deleteFilesWithPath:(NSString *)path andFiles:(NSArray *)files{
    if ([[NSFileManager defaultManager] fileExistsAtPath:path]) {
        for (NSString *file in files) {
            NSFileManager *fileMgr = [NSFileManager defaultManager];
            NSError *err;
            NSString *filePath = [path stringByAppendingPathComponent:file];
            [fileMgr removeItemAtPath:filePath error:&err];
        }
    }
}

//单个文件的大小
- (long long)fileSizeAtPath:(NSString*) filePath{
    NSFileManager* manager = [NSFileManager defaultManager];
    if ([manager fileExistsAtPath:filePath]){
        return [[manager attributesOfItemAtPath:filePath error:nil] fileSize];
    }
    return 0;
}

//遍历文件夹获得文件夹大小，返回多少M
- (float )folderSizeAtPath:(NSString*) folderPath{
    NSFileManager* manager = [NSFileManager defaultManager];
    if (![manager fileExistsAtPath:folderPath]) return 0;
    //    NSEnumerator *childFilesEnumerator = [[manager subpathsAtPath:folderPath] objectEnumerator];
    NSArray* childFilesEnumerator = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:folderPath
                                                                                        error:NULL];
    NSLog(@"childFilesEnumerator =>> folderPath:%@, %@",folderPath, childFilesEnumerator);
    
    long long folderSize = 0;
    
    for (NSString *filename in childFilesEnumerator) {
        folderSize += [self fileSizeAtPath:[folderPath stringByAppendingPathComponent:filename]];
    }
    
    //    NSString* fileName;
    //    while ((fileName = [childFilesEnumerator nextObject]) != nil){
    //        NSString* fileAbsolutePath = [folderPath stringByAppendingPathComponent:fileName];
    //    }
    
    return folderSize;
}


- (void)initController{
    [self setSendState];
    [[NIMViewController initWithController] addDelegate];
    [[NoticeViewController initWithNoticeViewController]initWithDelegate];
    [RNNotificationCenter sharedCenter];
    [[UserStrangers initWithUserStrangers] setIm:self];
}

-(void)setSendState{
    NIMModel *mod = [NIMModel initShareMD];
    mod.myBlock = ^(NSInteger index, id param) {
        switch (index) {
            case 0:
                //网络状态
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeOnlineStatus" body:@{@"status":param}];
                break;
            case 1:
                //最近会话列表
                //                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeRecentContact" body:@{@"sessionList":param}];
//                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeRecentContact" body:(NSDictionary *)param];
                [self debounceSendEventWithParam:param debounceInterval:1];

                break;
            case 2:
                //被踢出 status：1是被挤下线，2是被服务器踢下线，3是另一个客户端手动踢下线（这是在支持多客户端登录的情况下）
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeOnKick" body:@{@"status":param}];
                break;
            case 3:
                //通讯录
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeFriend" body:param];
                break;
            case 4:
                //群
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeTeam" body:param];
                break;
            case 5:
                //系统通知
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeReceiveSystemMsg" body:param];
                break;
            case 6:
                //系统通知未读条数
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeUnreadCountChange" body:param];
                break;
            case 7:
                //收到新消息、聊天记录
//                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeReceiveMessage" body:param];
                [self onSendEventMsgReceive:param];
                break;
            case 8:
                //开始发送
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeStartSend" body:param];
                break;
            case 9:
                //发送结束
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeEndSend" body:param];
                break;
            case 10:
                //发送进度（'附件图片等'）
//                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeProgressSend" body:param];
                [self onUploadQueueAttachment: param];
                
                break;
            case 11:
                //已读回执
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeReceipt" body:param];
                break;
            case 12:
                //发送消息
//                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeMsgStatus" body:param];
                [self onSendEventMsgStatus: param];
                break;
            case 13:
                //黑名单列表
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeBlackList" body:param];
                break;
            case 14:
                //录音进度 分贝
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeAudioRecord" body:param];
                break;
            case 15:
                //删除撤销消息通知
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeDeleteMessage" body:param];
                break;
            case 16:
                //资金变动通知
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeAccountNotice" body:param];
                break;
            case 17:
                //下载视频完成通知
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeDownloadVideoNotice" body:param];
                break;
                
            case 18:
                //custom notification
                [_bridge.eventDispatcher sendDeviceEventWithName:@"observeCustomNotification" body:param];
                break;
            default:
                break;
        }
        
    };
}

//获取网络状态权限
// RCT_EXPORT_METHOD(getNetWorkStatus:(RCTPromiseResolveBlock)resolve
//                   reject:(RCTPromiseRejectBlock)reject){
//     //int type = 0;//0:无网络, 1:2G, 2:3G, 3:4G, 4:LTE准4G，5：wifi
//     if (kDevice_Is_iPhoneX){//iPhone X 目前未找到获取状态栏网络状态，先设置为1
//         resolve(@(1));
//     }else{
//         NSString *strNetWork = [self getNetStatus];
//         if ([strNetWork isEqualToString:@"NOTFOUND"]) {
//             resolve(@(0));
//         }else{
//             resolve(@(1));
//         }
//     }
// }
// //设置webview UA
// RCT_EXPORT_METHOD(setupWebViewUserAgent){
//     if (!strUserAgent.length) {
//         NSString *version = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"];
//         NSString *userAgent = [[[UIWebView alloc] init] stringByEvaluatingJavaScriptFromString:@"navigator.userAgent"];
//         strUserAgent = [userAgent stringByAppendingFormat:@" Feima/%@ NetType/", version];
//     }
//     NSString *strNetWork = [self getNetStatus];
//     [[NSUserDefaults standardUserDefaults] registerDefaults:@{@"UserAgent":[NSString stringWithFormat:@"%@%@",strUserAgent,strNetWork]}];
// }

//获取网络状态
- (NSString *)getNetStatus{
    NSString *strNetWork = @"";
    int type = 0;//0:无网络, 1:2G, 2:3G, 3:4G, 4:LTE准4G，5：wifi,6:iphone x
    if (kDevice_Is_iPhoneX){//iPhone X 目前未找到获取状态栏网络状态，先设置为1
        type = 6;
    }else{
        UIApplication *app = [UIApplication sharedApplication];
        NSArray *children = [[[app valueForKeyPath:@"statusBar"] valueForKeyPath:@"foregroundView"] subviews];
        for (id child in children) {
            if ([child isKindOfClass:[NSClassFromString(@"UIStatusBarDataNetworkItemView") class]]) {
                type = [[child valueForKeyPath:@"dataNetworkType"] intValue];
            }
        }
    }
    switch (type) {
        case 0:
            strNetWork = @"NOTFOUND";
            break;
        case 1:
            strNetWork = @"2G";
            break;
        case 2:
            strNetWork = @"3G";
            break;
        case 3:
            strNetWork = @"4G";
            break;
        case 4:
            strNetWork = @"LTE";
            break;
        case 5:
            strNetWork = @"WIFI";
            break;
        case 6:
            strNetWork = @"NOTFOUND";
            break;
        default:
            strNetWork = @"NOTFOUND";
            break;
    }
    return strNetWork;
}

+ (BOOL)requiresMainQueueSetup{
    return YES;
}

@end
